/* 
 * <copyright>
 * Copyright 2001 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).

 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.component;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import org.cougaar.util.DBProperties;
import org.cougaar.util.Parameters;
import org.cougaar.util.DBConnectionPool;

/**
 * This class takes a structure of ComponentData objects and populates
 * the configuration database with some or all of the components
 * described by the data. The selection of applicable components is
 * still an issue.
 **/
public class PopulateDb extends PDbBase {
    private String exptId;
    private String trialId;
    private String hnaAssemblyId;
    private String csmiAssemblyId;

    private Map propertyInfos = new HashMap();
    private Set preexistingItems = new HashSet();
    private Set writableComponents = new HashSet();
    private boolean writeEverything = false;

    /**
     * Inner class to serve as the key to information about
     * PropertyInfo that has been resolved. The key consists of the
     * property group name and the property name within that group.
     **/
    private static class PropertyKey {
        private String pgName;
        private String propName;
        private int hc;

        /**
         * Constructor from property group name and property name
         **/
        public PropertyKey(String pgName, String propName) {
            this.pgName = pgName;
            this.propName = propName;
            hc = pgName.hashCode() + propName.hashCode();
        }

        /**
         * Get the property group name part of the key
         * @return the property group name of this key
         **/
        public String getPGName() {
            return pgName;
        }

        /**
         * Get the property name part of the key
         * @return the property name of this key
         **/
        public String getPropName() {
            return propName;
        }

        /**
         * The usual Object.hashCode() method
         **/
        public int hashCode() {
            return hc;
        }

        /**
         * Equality comparison.
         * @return true if both the property group name and the
         * property name are equal
         **/
        public boolean equals(Object o) {
            if (!(o instanceof PropertyKey)) return false;
            PropertyKey that = (PropertyKey) o;
            return this.pgName.equals(that.pgName) &&
                that.propName.equals(that.propName);
        }

        /**
         * @return concatenation of property group name and property
         * name separated with vertical bar.
         **/
        public String toString() {
            return pgName + "|" + propName;
        }
    }

    /**
     * Inner class for recording information about a property within a
     * property group. Used to record information from the database to
     * avoid fetching the same information multiple times.
     **/
    private static class PropertyInfo {
        PropertyKey key;
        String attributeLibId;
        String attributeType;
        String aggregateType;
        public PropertyInfo(PropertyKey key,
                            String attributeLibId,
                            String attributeType,
                            String aggregateType)
        {
            this.key = key;
            this.attributeLibId = attributeLibId;
            this.attributeType = attributeType;
            this.aggregateType = aggregateType;
        }

        public String getAttributeLibId() {
            return attributeLibId;
        }

        public boolean isCollection() {
            return !aggregateType.equals("SINGLE");
        }
        public String toString() {
            return key.toString()
                + "=" + attributeLibId
                + "(" + attributeType + "," + aggregateType + ")";
        }
    }

    /**
     * Constructor from an assembly id.
     * @param assemblyId is used to identify all components added to
     * the database.
     **/
    public PopulateDb(String cmtType, String hnaType, String csmiType,
                      String experimentName,
                      String exptId, String trialId, boolean createNew)
        throws SQLException, IOException
    {
        super();
        if (cmtType == null) throw new IllegalArgumentException("null cmtType");
        if (hnaType == null) throw new IllegalArgumentException("null hnaType");
        if (csmiType == null) throw new IllegalArgumentException("null csmiType");
        if (exptId == null) throw new IllegalArgumentException("null exptId");
        if (trialId == null) throw new IllegalArgumentException("null trialId");
        this.exptId = exptId;
        this.trialId = trialId;
        substitutions.put(":expt_id:", exptId);
        substitutions.put(":cmt_type:", cmtType);
        if (createNew) {
            cloneTrial(hnaType, trialId, experimentName);
            writeEverything = true;
        } else {
            cleanTrial(cmtType, hnaType, csmiType, trialId);
            writeEverything = false;
        }
        hnaAssemblyId = addAssembly(hnaType);
        if (hnaType.equals(csmiType))
            csmiAssemblyId = hnaAssemblyId;
        else
            csmiAssemblyId = addAssembly(csmiType);
        setAssemblyMatch();
    }

    private void setAssemblyMatch() throws SQLException {
        ResultSet rs = executeQuery(stmt, dbp.getQuery("queryTrialAssemblies", substitutions));
        StringBuffer q = new StringBuffer();
        boolean first = true;
        q.append("in (");
        while (rs.next()) {
            if (first) {
                first = false;
            } else {
                q.append(", ");
            }
            q.append("'").append(rs.getString(1)).append("'");
        }
        q.append(')');
        if (first) {            // No matches
            substitutions.put(":assembly_match:", "is null");
        } else {
            substitutions.put(":assembly_match:", q.toString());
        }
    }

    public String getExperimentId() {
        return exptId;
    }

    public String getHNAAssemblyId() {
        return hnaAssemblyId;
    }

    public String getCSMIAssemblyId() {
        return csmiAssemblyId;
    }

    private void cloneTrial(String idType, String oldTrialId, String experimentName)
        throws SQLException
    {
        newExperiment(idType, experimentName);
        newTrial(idType, experimentName); // Trial and experiment have same name
        copyCMTAssemblies(oldTrialId, trialId);
        copyCMTThreads(oldTrialId, trialId);
    }

    /**
     * Clean out a trial by removing all assemblies except the CMT and
     * idType assemblies
     **/
    private void cleanTrial(String cmtType, String hnaType, String csmiType, String oldTrialId)
        throws SQLException
    {
        substitutions.put(":trial_id:", oldTrialId);
        substitutions.put(":cmt_type:", cmtType);
        substitutions.put(":hna_type:", hnaType);
        substitutions.put(":csmi_type:", csmiType);
        ResultSet rs =
            executeQuery(stmt, dbp.getQuery("queryAssembliesToClean", substitutions));
        if (rs.next()) {
            boolean first = true;
            StringBuffer assembliesToDelete = new StringBuffer();
            assembliesToDelete.append("(");
            do {
                if (first) {
                    first = false;
                } else {
                    assembliesToDelete.append(", ");
                }
                assembliesToDelete.append(sqlQuote(rs.getString(1)));
            } while (rs.next());
            assembliesToDelete.append(")");
            substitutions.put(":assemblies_to_clean:", assembliesToDelete.toString());
            executeUpdate(dbp.getQuery("cleanTrialAssembly", substitutions));
        }
        rs.close();
        executeUpdate(dbp.getQuery("cleanTrialRecipe", substitutions));
    }

    private void newExperiment(String idType, String experimentName) throws SQLException {
        String exptIdPrefix = idType + "-";
        substitutions.put(":expt_type:", idType);
        substitutions.put(":expt_name:", experimentName);
        substitutions.put(":description:", experimentName);
        exptId = getNextId("queryMaxExptId", exptIdPrefix);
        substitutions.put(":expt_id:", exptId);
        executeUpdate(dbp.getQuery("insertExptId", substitutions));
    }

    public String getTrialId() {
        return trialId;
    }

    private void newTrial(String idType, String trialName)
        throws SQLException
    {
        String trialIdPrefix = idType + "-";
        substitutions.put(":trial_type:", idType);
        trialId = getNextId("queryMaxTrialId", trialIdPrefix);
        substitutions.put(":trial_id:", trialId);
        substitutions.put(":description:", "Modified Trial");
        substitutions.put(":trial_name:", trialName);
        executeUpdate(dbp.getQuery("insertTrialId", substitutions));
    }

    private String addAssembly(String idType)
        throws SQLException
    {
        String assemblyId;
        String assemblyIdPrefix = idType + "-";
        substitutions.put(":assembly_id_pattern:", assemblyIdPrefix + "____");
        substitutions.put(":assembly_type:", idType);
        assemblyId = getNextId("queryMaxAssemblyId", assemblyIdPrefix);
        substitutions.put(":assembly_id:", sqlQuote(assemblyId));
        substitutions.put(":trial_id:", trialId);
        executeUpdate(dbp.getQuery("insertAssemblyId", substitutions));
        executeUpdate(dbp.getQuery("insertTrialAssembly", substitutions));
        return assemblyId;
    }

    private void copyCMTAssemblies(String oldTrialId, String newTrialId)
        throws SQLException
    {
        substitutions.put(":old_trial_id:", oldTrialId);
        substitutions.put(":new_trial_id:", newTrialId);
        String qs = dbp.getQuery("copyCMTAssembliesQueryNames", substitutions);
        StringTokenizer queries = new StringTokenizer(qs);
        while (queries.hasMoreTokens()) {
            String queryName = queries.nextToken();
            executeUpdate(dbp.getQuery(queryName, substitutions));
        }
    }

    private void copyCMTThreads(String oldTrialId, String newTrialId)
        throws SQLException
    {
        substitutions.put(":old_trial_id:", oldTrialId);
        substitutions.put(":new_trial_id:", newTrialId);
        String qs = dbp.getQuery("copyCMTThreadsQueryNames", substitutions);
        StringTokenizer queries = new StringTokenizer(qs);
        while (queries.hasMoreTokens()) {
            String queryName = queries.nextToken();
            executeUpdate(dbp.getQuery(queryName, substitutions));
        }
    }

    public void setPreexistingItems(ComponentData data) {
        preexistingItems.add(data);
        for (int i = 0, n = data.parameterCount(); i < n; i++) {
            preexistingItems.add(data.getParameter(i));
        }
        AgentAssetData aad = data.getAgentAssetData();
        if (aad != null) {
            for (int i = 0, n = aad.getRelationshipCount(); i < n; i++) {
                RelationshipData rd = aad.getRelationship(i);
                preexistingItems.add(rd);
            }
        }
        ComponentData[] children = data.getChildren();
        for (int i = 0; i < children.length; i++) {
            setPreexistingItems(children[i]);
        }
    }

    public void setModRecipes(List recipes) throws SQLException, IOException {
        //        dbp.setDebug(true);
        int order = 0;
        for (Iterator i = recipes.iterator(); i.hasNext(); ) {
            RecipeComponent rc = (RecipeComponent) i.next();
            addTrialRecipe(rc, order++);
        }
    }

    public void addTrialRecipe(RecipeComponent rc, int recipeOrder)
        throws SQLException
    {
        String recipeId = insureLibRecipe(rc);
        substitutions.put(":recipe_id:", recipeId);
        substitutions.put(":recipe_order:", String.valueOf(recipeOrder));
        executeUpdate(dbp.getQuery("insertTrialRecipe", substitutions));
    }

    /**
     * Populate the tables for a particular item. Children are
     * populated recursively. Agent components get additional
     * processing related to the organization they represent.
     * @param data the ComponentData of the starting point
     * @param insertionOrder the position among siblings that the
     * component should occupy.
     **/
    public boolean populate(ComponentData data, float insertionOrder)
        throws SQLException
    {
        return populate(data, insertionOrder, false);
    }

    /**
     * This is inordinately difficult because of the haphazard way the
     * the db is populated. Our aim is two-fold: to write an assembly
     * describing the host-node-agent assignments and to write a
     * different assembly describing the effect of recipes.
     * Both types of assemblies have agent-node assigments. The
     * distinction is made using the preexistingItems Set.
     * Components in this set are either part of the CMT assembly
     * (which we don't touch) or are part of the CSMART assembly if
     * they involve agent-node assignments
     **/
    private boolean populate(ComponentData data, float insertionOrder, boolean force)
        throws SQLException
    {
        boolean result = writeEverything;
        boolean isAgent = data.getType().equals(ComponentData.AGENT);
        boolean isSociety = data.getType().equals(ComponentData.SOCIETY);
        boolean isAdded = isAdded(data);
        substitutions.put(":assembly_id:", sqlQuote(isAdded ? csmiAssemblyId : hnaAssemblyId));
        if (!isSociety) {
            addComponentDataSubstitutions(data);
            ResultSet rs = executeQuery(stmt, dbp.getQuery("checkLibComponent", substitutions));
            if (rs.next()) {    // Already exists
            } else {            // Need to add it
                executeUpdate(dbp.getQuery("insertLibComponent", substitutions));
            }
            rs.close();
            rs = executeQuery(stmt, dbp.getQuery("checkAlibComponent", substitutions));
            if (rs.next()) {    // Already exists
                // Use the one that is there
            } else {
                executeUpdate(dbp.getQuery("insertAlibComponent", substitutions));
                result = true;
            }
            rs.close();
            if (isAdded) {
                Object[] params = data.getParameters();
                for (int i = 0; i < params.length; i++) {
//                      System.out.println("param value = " + params[i]);
                    substitutions.put(":argument_value:", sqlQuote(params[i].toString()));
                    substitutions.put(":argument_order:", sqlQuote(String.valueOf(i + 1)));
                    executeUpdate(dbp.getQuery("insertComponentArg", substitutions));
                    result = true;
                }
            }
        }
        if (isAgent) {
            // Must be a recipe agent because that's all that gets added
            populateAgent(data, isAdded);
            result = true;
            force = true;   // Force writing of all plugins, too
        }

        ComponentData parent = data.getParent();
        if (parent != null) {
            String parentType = parent.getType();
            if (!parentType.equals(ComponentData.SOCIETY)) {
                substitutions.put(":parent_component_alib_id:", sqlQuote(getComponentAlibId(parent)));
                substitutions.put(":component_alib_id:", sqlQuote(getComponentAlibId(data)));
                substitutions.put(":insertion_order:", String.valueOf(insertionOrder));
                ResultSet rs =
                    executeQuery(stmt, dbp.getQuery("checkComponentHierarchy", substitutions));
                if (!rs.next()) {
                    executeUpdate(dbp.getQuery("insertComponentHierarchy", substitutions));
                    result = true;
                }
            }
        }

        ComponentData[] children = data.getChildren();

        // The following assumes that the insertion order of old
        // children equals their index in the array and that the
        // insertion order of all new children should be the their
        // index  in the array as well.
        for (int i = 0, n = children.length; i < n; i++) {
            result |= populate(children[i], i, force);
        }
        return result;
    }

    private void addComponentDataSubstitutions(ComponentData data) {
        substitutions.put(":component_name:", sqlQuote(data.getName()));
        substitutions.put(":component_lib_id:", getComponentLibId(data));
        substitutions.put(":component_alib_id:", sqlQuote(getComponentAlibId(data)));
        substitutions.put(":component_category:", getComponentCategory(data));
        substitutions.put(":component_class:", sqlQuote(data.getClassName()));
        substitutions.put(":insertion_point:", getComponentInsertionPoint(data));
        substitutions.put(":description:", sqlQuote("Added " + data.getType()));
    }

    /**
     * Special processing for an agent component because agents
     * represent organizations have relationships and property groups.
     **/
    private void populateAgent(ComponentData data, boolean isAdded) throws SQLException {
        AgentAssetData assetData = data.getAgentAssetData();
        if (assetData == null) return;
        substitutions.put(":component_lib_id:", getComponentLibId(data));
        substitutions.put(":agent_org_prototype:", sqlQuote(assetData.getAssetClass()));
        if (isAdded) {
            // finish populating a new agent
            executeUpdate(dbp.getQuery("insertAgentOrg", substitutions));
            PropGroupData[] pgs = assetData.getPropGroups();
            for (int i = 0; i < pgs.length; i++) {
                PropGroupData pg = pgs[i];
                String pgName = pg.getName();
                PGPropData[] props = pg.getProperties();
                for (int j = 0; j < props.length; j++) {
                    PGPropData prop = props[i];
                    PropertyInfo propInfo = getPropertyInfo(pgName, prop.getName());
                    substitutions.put(":component_alib_id:", sqlQuote(getComponentAlibId(data)));
                    substitutions.put(":pg_attribute_lib_id:", propInfo.getAttributeLibId());
                    substitutions.put(":start_date:", sqlQuote("2000-01-01 00:00:00"));
                    substitutions.put(":end_date:", sqlQuote(null));
                    if (propInfo.isCollection()) {
                        if (!prop.isListType())
                            throw new RuntimeException("Property is not a collection: "
                                                       + propInfo.toString());
                        String[] values = ((PGPropMultiVal) prop.getValue()).getValuesStringArray();
                        for (int k = 0; k < values.length; k++) {
                            substitutions.put(":attribute_value:", sqlQuote(values[k]));
                            substitutions.put(":attribute_order:", String.valueOf(k + 1));
                            executeUpdate(dbp.getQuery("insertAttribute", substitutions));
                        }
                    } else {
                        if (prop.isListType())
                            throw new RuntimeException("Property is not a single value: "
                                                       + propInfo.toString());
                        substitutions.put(":attribute_value:", sqlQuote(prop.getValue().toString()));
                        substitutions.put(":attribute_order:", "1");
                        executeUpdate(dbp.getQuery("insertAttribute", substitutions));
                    }
                }
            }
        }
        RelationshipData[] relationships = assetData.getRelationshipData();
        //        dbp.setDebug(true);
        for (int i = 0; i < relationships.length; i++) {
            RelationshipData r = relationships[i];
            long startTime = r.getStartTime();
            long endTime = r.getEndTime();
            substitutions.put(":role:", sqlQuote(r.getRole()));
            substitutions.put(":supporting:", sqlQuote(getComponentAlibId(data)));
            substitutions.put(":supported:", getAgentAlibId(r.getSupported()));
            substitutions.put(":start_date:", "?");
            substitutions.put(":end_date:", "?");
            String query = dbp.getQuery("checkRelationship", substitutions);
            PreparedStatement pstmt = dbConnection.prepareStatement(query);
            pstmt.setTimestamp(1, new Timestamp(r.getStartTime()));
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                query = dbp.getQuery("insertRelationship", substitutions);
                PreparedStatement pstmt2 = dbConnection.prepareStatement(query);
                pstmt2.setTimestamp(1, new Timestamp(startTime));
                if (endTime > 0L) {
                    pstmt2.setTimestamp(2, new Timestamp(endTime));
                } else {
                    pstmt2.setNull(2, Types.TIMESTAMP);
                }
                pstmt2.executeUpdate();
                pstmt2.close();
            }
            pstmt.close();
        }
    }

    public Set executeQuery(String queryName) throws SQLException {
        Statement stmt = dbConnection.createStatement();
        ResultSet rs = executeQuery(stmt, dbp.getQuery(queryName, substitutions));
        Set results = new HashSet();
        while (rs.next()) {
            results.add(rs.getString(1));
        }
        rs.close();
        stmt.close();
        return results;
    }

    public String[][] executeQueryForComponent(String queryName, ComponentData cd) throws SQLException {
        addComponentDataSubstitutions(cd);
        Statement stmt = dbConnection.createStatement();
        ResultSet rs = executeQuery(stmt, dbp.getQuery(queryName, substitutions));
        List rows = new ArrayList();
        int ncols = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            String[] row = new String[ncols];
            for (int i = 0; i < ncols; i++) {
                row[i] = rs.getString(i + 1);
            }
            rows.add(row);
        }
        rs.close();
        stmt.close();
        return (String[][]) rows.toArray(new String[rows.size()][]);
    }

    private boolean isAdded(ComponentData data) {
        if (preexistingItems.contains(data)) return false;
        return true;
    }

    /**
     * Get the PropertyInfo for a pg/prop pair. If the information is
     * not in the propertyInfos cache, the cache is filled from the
     * database.
     **/
    private PropertyInfo getPropertyInfo(String pgName, String propName) throws SQLException {
        PropertyKey key = new PropertyKey(pgName, propName);
        PropertyInfo result = (PropertyInfo) propertyInfos.get(key);
        if (result == null) {
            Statement stmt = dbConnection.createStatement();
            substitutions.put(":pg_name:", pgName);
            substitutions.put(":attribute_name:", propName);
            ResultSet rs = executeQuery(stmt, dbp.getQuery("queryLibPGAttribute", substitutions));
            while (rs.next()) {
                PropertyKey key1 = new PropertyKey(rs.getString(1), rs.getString(2));
                PropertyInfo info = new PropertyInfo(key1,
                                                     rs.getString(3),
                                                     rs.getString(4),
                                                     rs.getString(5));
                propertyInfos.put(key1, info);
                if (key1.equals(key)) result = info;
            }
            rs.close();
            stmt.close();
        }
        return result;
    }

    /**
     * Get the component lib id of the underlying lib component for
     * the component described by the specified ComponentData. Each
     * type of component has a different convention for constructing
     * its lib id.
     **/
    private String getComponentLibId(ComponentData data) {
        if (data == null) return sqlQuote(null);
        String componentType = data.getType();
        if (componentType.equals(ComponentData.PLUGIN)) {
            String agentName = findAncestorOfType(data, ComponentData.AGENT).getName();
            return sqlQuote(data.getType() + "|" + data.getClassName());
        }
        if (componentType.equals(ComponentData.BINDER)) {
            String agentName = findAncestorOfType(data, ComponentData.AGENT).getName();
            return sqlQuote(data.getType() + "|" + data.getClassName());
        }
        if (componentType.equals(ComponentData.AGENT)) {
            String agentName = data.getName();
            return sqlQuote(agentName);
        }
        if (componentType.equals(ComponentData.NODE)) {
            String nodeName = data.getName();
            return sqlQuote(nodeName);
        }
        if (componentType.equals(ComponentData.HOST)) {
            String hostName = data.getName();
            return sqlQuote(hostName);
        }
        ComponentData parent = data.getParent();
        return sqlQuote(data.getType() + "|" + getFullName(data));
    }

    /**
     * Get the component insertion point for the component described
     * by the specified ComponentData. Each type of component has a
     * different insertion point. Some have none.
     **/
    private String getComponentInsertionPoint(ComponentData data) {
        if (data == null) return sqlQuote(null);
        String componentType = data.getType();
        if (componentType.equals(ComponentData.PLUGIN)) {
            return sqlQuote("Node.AgentManager.Agent.PluginManager.Plugin");
        }
        if (componentType.equals(ComponentData.BINDER)) {
            return sqlQuote("Node.AgentManager.Agent.PluginManager.Binder");
        }
        if (componentType.equals(ComponentData.AGENT)) {
            return sqlQuote("Node.AgentManager.Agent");
        }
        if (componentType.equals(ComponentData.NODE)) {
            return sqlQuote("Node");
        }
        if (componentType.equals(ComponentData.HOST)) {
            return sqlQuote(null);
        }
        return sqlQuote(null);
    }

    /**
     * Create a component alib id for this component. Again, each kind
     * of component has a different convention for constructing its
     * alib id.
     **/
    public String getComponentAlibId(ComponentData data) {
        if (data == null) return null;
        String result = data.getAlibID();
        if (result == null) {
            String componentType = data.getType();
            if (componentType.equals(ComponentData.PLUGIN)) {
                String agentName = findAncestorOfType(data, ComponentData.AGENT).getName();
                result = agentName + "|" + data.getClassName();
            } else if (componentType.equals(ComponentData.BINDER)) {
                String agentName = findAncestorOfType(data, ComponentData.AGENT).getName();
                result = agentName + "|" + data.getClassName();
            } else {
                result = data.getName();
            }
            data.setAlibID(result);
        }
        return result;
    }

    /**
     * The convention for the alib id of an agent component is that it
     * is the base agent name prefixed with a clone set id and a
     * hyphen. We are not present concerned with clone set ids, so we
     * use a fixed CLONE_SET_ID.
     **/
    private String getAgentAlibId(String agentName) {
        return sqlQuote("" + agentName);
    }

    private String getFullName(ComponentData data) {
        ComponentData parent = data.getParent();
        if (parent == null) return data.getName();
        return getFullName(parent) + "|" + data.getName();
    }

    /**
     * We have conveniently arranged that the type of a ComponentData
     * is the same as the category of a database component. We simple
     * wrap it in quotes and return.
     **/
    private String getComponentCategory(ComponentData data) {
        return sqlQuote(data.getType());
    }

    /**
     * Search up the parent links for an ancestor of a particular type.
     **/
    private ComponentData findAncestorOfType(ComponentData data, String type) {
        for (ComponentData parent = data.getParent(); parent != null; parent = parent.getParent()) {
            if (parent.getType().equals(type)) return parent;
        }
        return null;
    }
}

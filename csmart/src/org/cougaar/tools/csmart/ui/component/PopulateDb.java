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
import java.util.SortedSet;
import java.util.TreeSet;
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
    private Set alibComponents = new HashSet();
    private Map componentArgs = new HashMap();
    private boolean writeEverything = false;
    private ConflictHandler conflictHandler;
    private boolean keepAll = false;
    private boolean overwriteAll = false;

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
     * Constructor
     * @param cmtType the cmt assembly type
     * @param hnaType the hna assembly type
     * @param csmiType the csmi assembly type
     * @param experimentName the name of the experiment being written
     * @param exptId the experiment id of the source experiment
     * @param trialId the trial id
     * @param createNew clone the experiment first
     **/
    public PopulateDb(String cmtType, String hnaType, String csmiType,
                      String experimentName,
                      String exptId, String trialId, boolean createNew,
                      ConflictHandler ch)
        throws SQLException, IOException
    {
        super();
        if (cmtType == null) throw new IllegalArgumentException("null cmtType");
        if (hnaType == null) throw new IllegalArgumentException("null hnaType");
        if (csmiType == null) throw new IllegalArgumentException("null csmiType");
        if (exptId == null) throw new IllegalArgumentException("null exptId");
        if (trialId == null) throw new IllegalArgumentException("null trialId");
        if (ch == null) throw new IllegalArgumentException("null conflict handler");
        this.exptId = exptId;
        this.trialId = trialId;
        this.conflictHandler = ch;
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
     * Populate the HNA assembly for a particular item. Children are
     * populated recursively. Agent components get additional
     * processing related to the organization they represent.
     * @param data the ComponentData of the starting point
     * @param insertionOrder the position among siblings that the
     * component should occupy.
     **/
    public boolean populateHNA(ComponentData data, float insertionOrder)
        throws SQLException
    {
        return populate(data, insertionOrder, hnaAssemblyId);
    }

    /**
     * Populate the CSMI assembly for a particular item. Children are
     * populated recursively. Agent components get additional
     * processing related to the organization they represent.
     * @param data the ComponentData of the starting point
     * @param insertionOrder the position among siblings that the
     * component should occupy.
     **/
    public boolean populateCSMI(ComponentData data, float insertionOrder)
        throws SQLException
    {
        return populate(data, insertionOrder, csmiAssemblyId);
    }

    /**
     * This is inordinately difficult
     **/
    private boolean populate(ComponentData data, float insertionOrder, String assemblyId)
        throws SQLException
    {
        boolean result = false;
        ComponentData parent = data.getParent();
        String id = getComponentAlibId(data);
        boolean isAdded = false;
        addComponentDataSubstitutions(data);
        substitutions.put(":assembly_id:", sqlQuote(assemblyId));
        substitutions.put(":insertion_order:", String.valueOf(insertionOrder));
        SortedSet oldArgs = (SortedSet) componentArgs.get(id);
        if (oldArgs == null) {  // Don't know what the current args are
            insureAlib();       // Insure that the alib and lib components are defined
            ResultSet rs;
            rs = executeQuery(stmt, dbp.getQuery("queryComponentArgs", substitutions));
            oldArgs = new TreeSet();
            while (rs.next()) {
                oldArgs.add(new Argument(rs.getString(1), rs.getFloat(2)));
            }
            rs.close();
            if (parent != null) {
                rs = executeQuery(stmt, dbp.getQuery("checkComponentHierarchy",
                                                     substitutions));
                if (!rs.next()) {
                    executeUpdate(dbp.getQuery("insertComponentHierarchy",
                                               substitutions));
                    isAdded = true;
                    result = true; // Modified the experiment in the database 
                }
                rs.close();
            }
            componentArgs.put(id, oldArgs); // Avoid doing this again
        }
        // Now build a SortedSet of what the args should be
        SortedSet newArgs = new TreeSet();
        Object[] params = data.getParameters();
        for (int i = 0; i < params.length; i++) {
            newArgs.add(new Argument(params[i].toString(), i));
        }
        // The new args must only contain additions. There must be no
        // deletions or alterations of the old args nor is it allowed
        // to change the relative order of the existing arguments. If
        // there are fewer newArgs than oldArgs, there is clearly a
        // violation of this premise.
        int excess = newArgs.size() - oldArgs.size();
        if (excess < 0) {
            throw new IllegalArgumentException("Attempt to remove "
                                               + (-excess)
                                               + " args from "
                                               + data);
        }
        // Prepare to iterate through both old and new args.
        // argsToInsert accumulates the new arguments to be inserted.
        // Each of the new args is compared with the next unaccounted
        // for old arg. If the new arg has a value different from the
        // next old arg it is assumed to be an insertion since
        // deletions and modifications are disallowed. But, if the
        // excess new arg count has been reduced to zero, then no new
        // args are possible because there won't be enought old args
        // to match the remaining new ones so we throw an exception.
        // The argument order is interpolated between the preceding
        // and following old argument.
        Iterator newIter = newArgs.iterator();
        Iterator oldIter = oldArgs.iterator();
        List argsToInsert = new ArrayList();
        float prev = Float.NaN;
        while (oldIter.hasNext()) {
            Argument newArg = (Argument) newIter.next();
            Argument oldArg = (Argument) oldIter.next();
            while (!newArg.argument.equals(oldArg.argument)) {
                // Assume arguments were inserted
                excess--;
                if (excess < 0) {
                    throw new IllegalArgumentException("Component args cannot be modified or removed: " + data);
                }
                if (Float.isNaN(prev)) prev = oldArg.order - 1f;
                argsToInsert.add(new Argument(newArg.argument, (prev + oldArg.order) * 0.5f));
                newArg = (Argument) newIter.next();
            }
            prev = oldArg.order;
        }
        // Finally, any remaining new args are appended
        if (Float.isNaN(prev)) prev = 0f;
        while (newIter.hasNext()) {
            Argument newArg = (Argument) newIter.next();
            prev += 1f;
            argsToInsert.add(new Argument(newArg.argument, prev));
        }
        // Now write all the new arguments that were not previously
        // present to the database.
        for (Iterator i = argsToInsert.iterator(); i.hasNext(); ) {
            Argument arg = (Argument) i.next();
            substitutions.put(":argument_value:", sqlQuote(arg.argument));
            substitutions.put(":argument_order:", sqlQuote(String.valueOf(arg.order)));
            executeUpdate(dbp.getQuery("insertComponentArg", substitutions));
            oldArgs.add(arg);
            result = true;
        }

        if (data.getType().equals(ComponentData.AGENT)) {
            populateAgent(data, isAdded);
            result = true;
        }

        ComponentData[] children = data.getChildren();

        // The following assumes that the insertion order of old
        // children equals their index in the array and that the
        // insertion order of all new children should be the their
        // index in the array as well.
        for (int i = 0, n = children.length; i < n; i++) {
            result |= populate(children[i], i, assemblyId);
        }
        return result;
    }

    private boolean isOverwrite(Object msg) {
        if (overwriteAll) return true;
        if (keepAll) return false;
        if (conflictHandler == null) return false;
        switch (conflictHandler.handleConflict(msg,
                                               ConflictHandler.STANDARD_CHOICES,
                                               ConflictHandler.STANDARD_CHOICES[ConflictHandler.KEEP]))
            {
            case ConflictHandler.KEEP:
                return false;
            case ConflictHandler.OVERWRITE:
                return true;
            case ConflictHandler.KEEP_ALL:
                keepAll = true;
                return false;
            case ConflictHandler.OVERWRITE_ALL:
                overwriteAll = true;
                return true;
            }
        return false;
    }

    private void insureLib() throws SQLException {
        ResultSet rs = executeQuery(stmt, dbp.getQuery("checkLibComponent", substitutions));
        if (!rs.next()) {
            executeUpdate(dbp.getQuery("insertLibComponent", substitutions));
        } else if (rs.getBoolean(1)) {
            // Value is ok
        } else if (conflictHandler != null) {
            String id = (String) substitutions.get(":component_lib_id:");
            String query = dbp.getQuery("updateLibComponent", substitutions);
            String[] msg = {
                "You are attempting to redefine lib component: " + id,
                "using query:",
                query,
                "Do you want to overwrite the definition?"
            };
            if (isOverwrite(msg)) {
                executeUpdate(query);
            }
        }
        rs.close();
    }

    private void insureAlib() throws SQLException {
        String id = (String) substitutions.get(":component_alib_id:");
        if (alibComponents.contains(id)) return; // Already present
        // may need to be added
        insureLib();
        ResultSet rs = executeQuery(stmt, dbp.getQuery("checkAlibComponent", substitutions));
        if (!rs.next()) {
            executeUpdate(dbp.getQuery("insertAlibComponent", substitutions));
        } else if (rs.getBoolean(1)) {
            // Value is ok
        } else if (conflictHandler != null) {
            String query = dbp.getQuery("updateAlibComponent", substitutions);
            String[] msg = {
                "You are attempting to redefine alib component: " + id,
                "using query:",
                query,
                "Do you want to overwrite the definition?"
            };
            if (isOverwrite(msg)) {
                executeUpdate(query);
            }
        }
        alibComponents.add(id); // Avoid re-querying later
        rs.close();
    }

    private void addComponentDataSubstitutions(ComponentData data) {
        substitutions.put(":component_name:", sqlQuote(data.getName()));
        substitutions.put(":component_lib_id:", getComponentLibId(data));
        substitutions.put(":component_alib_id:", sqlQuote(getComponentAlibId(data)));
        substitutions.put(":component_category:", getComponentCategory(data));
        substitutions.put(":component_class:", sqlQuote(data.getClassName()));
        substitutions.put(":insertion_point:", getComponentInsertionPoint(data));
        substitutions.put(":description:", sqlQuote("Added " + data.getType()));
        ComponentData parent = data.getParent();
        if (parent != null) {
            substitutions.put(":parent_component_alib_id:",
                              sqlQuote(getComponentAlibId(parent)));
        } else {
            substitutions.remove(":parent_component_alib_id:");
        }
    }

    /**
     * Special processing for an agent component because agents
     * represent organizations having relationships and property groups.
     * The substitutions Map already has most of the needed substitutions
     **/
    private void populateAgent(ComponentData data, boolean isAdded) throws SQLException {
        AgentAssetData assetData = data.getAgentAssetData();
        if (assetData == null) return;
        substitutions.put(":agent_org_class:", sqlQuote(assetData.getAssetClass()));
        substitutions.put(":agent_lib_name:", sqlQuote(data.getName()));
        if (isAdded) {
            // finish populating a new agent
            ResultSet rs = executeQuery(stmt, dbp.getQuery("checkAgentOrg", substitutions));
            if (!rs.next()) {
                executeUpdate(dbp.getQuery("insertAgentOrg", substitutions));
            }
            rs.close();
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
            return sqlQuote(data.getType() + "|" + data.getClassName());
        }
        if (componentType.equals(ComponentData.NODEBINDER)) {
            return sqlQuote(data.getType() + "|" + data.getClassName());
        }
        if (componentType.equals(ComponentData.AGENTBINDER)) {
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
        if (componentType.equals(ComponentData.SOCIETY)) {
            String societyName = data.getName();
            return sqlQuote(data.getType() + "|" + societyName);
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
        if (componentType.equals(ComponentData.NODEBINDER)) {
            return sqlQuote("Node.Agent.Binder");
        }
        if (componentType.equals(ComponentData.AGENTBINDER)) {
            return sqlQuote("Node.AgentManager.Agent.PluginManager.Binder");
        }
        if (componentType.equals(ComponentData.AGENT)) {
            return sqlQuote("Node.AgentManager.Agent");
        }
        if (componentType.equals(ComponentData.NODE)) {
            return sqlQuote("Node");
        }
        if (componentType.equals(ComponentData.HOST)) {
            return sqlQuote("Host");
        }
        if (componentType.equals(ComponentData.SOCIETY)) {
            return sqlQuote("Society");
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
                String agentName =
                    findAncestorOfType(data, ComponentData.AGENT).getName();
                result = agentName + "|" + data.getClassName();
            } else if (componentType.equals(ComponentData.NODEBINDER)) {
              String nodeName =
                findAncestorOfType(data, ComponentData.NODE).getName();
              result = nodeName + "|" + data.getClassName();
            } else if (componentType.equals(ComponentData.AGENTBINDER)) {
                String agentName =
                    findAncestorOfType(data, ComponentData.AGENT).getName();
                result = agentName + "|" + data.getClassName();
            } else if (componentType.equals(ComponentData.SOCIETY)) {
                result = ComponentData.SOCIETY + "|" + data.getName();
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

    private static class Argument implements Comparable {
        public String argument;
        public float order;
        public Argument(String a, float f) {
            argument = a;
            order = f;
        }

        public int compareTo(Object o) {
            Argument that = (Argument) o;
            float diff = this.order - that.order;
            if (diff < 0) return -1;
            if (diff > 0) return 1;
            return this.argument.compareTo(that.argument);
        }

        public boolean equals(Object o) {
            if (o instanceof Argument) return compareTo(o) == 0;
            return false;
        }

        public String toString() {
            return "[" + order + "]=" + argument;
        }
    }

    public interface ConflictHandler {
        Object KEEP_CHOICE = "Keep Existing Definition";
        Object OVERWRITE_CHOICE = "Overwrite";
        Object KEEP_ALL_CHOICE = "Keep All";
        Object OVERWRITE_ALL_CHOICE = "Overwrite All";
        Object[] STANDARD_CHOICES = {
            KEEP_CHOICE,
            OVERWRITE_CHOICE,
            KEEP_ALL_CHOICE,
            OVERWRITE_ALL_CHOICE
        };
        int KEEP = 0;
        int OVERWRITE = 1;
        int KEEP_ALL = 2;
        int OVERWRITE_ALL = 3;
        int handleConflict(Object msg, Object[] options, Object defaultOption);
    }
}

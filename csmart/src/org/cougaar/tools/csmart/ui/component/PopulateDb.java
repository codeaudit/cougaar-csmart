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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import org.cougaar.util.DBProperties;
import org.cougaar.util.Parameters;
import org.cougaar.util.DBConnectionPool;

/**
 * This class takes a structure of ComponentData objects and populates
 * the configuration database with some or all of the components
 * described by the data. The selection of applicable components is
 * still an issue.
 **/
public class PopulateDb {
    public static final String DATABASE = "org.cougaar.configuration.database";
    public static final String QUERY_FILE = "PopulateDb.q";
    public static final String INSERT_ALIB_COMPONENT = "insertAlibComponent";
    public static final String INSERT_LIB_COMPONENT = "insertLibComponent";
    public static final String CHECK_LIB_COMPONENT = "checkLibComponent";
    public static final String INSERT_COMPONENT_HIERARCHY = "insertComponentHierarchy";
    public static final String CHECK_COMPONENT_HIERARCHY = "checkComponentHierarchy";
    public static final String INSERT_COMPONENT_ARG = "insertComponentArg";
    public static final String INSERT_AGENT_ORG = "insertAgentOrg";
    public static final String INSERT_ATTRIBUTE = "insertAttribute";
    public static final String INSERT_RELATIONSHIP = "insertRelationship";
    public static final String CHECK_RELATIONSHIP = "checkRelationship";
    public static final String QUERY_LIB_PG_ATTRIBUTE = "queryLibPGAttribute";
    public static final String QUERY_MAX_ASSEMBLY_ID = "queryMaxAssemblyId";
    public static final String QUERY_MAX_TRIAL_ID = "queryMaxTrialId";
    public static final String QUERY_MAX_EXPT_ID = "queryMaxExptId";
    public static final String INSERT_ASSEMBLY_ID = "insertAssemblyId";
    public static final String INSERT_TRIAL_ID = "insertTrialId";
    public static final String INSERT_EXPT_ID = "insertExptId";
    public static final String INSERT_TRIAL_ASSEMBLY = "insertTrialAssembly";
    public static final String CLEAN_TRIAL_ASSEMBLY = "cleanTrialAssembly";
    public static final String CHECK_ALIB_COMPONENT = "checkAlibComponent";
    public static final String COPY_CMT_ASSEMBLIES = "copyCMTAssemblies";
    public static final String CLONE_SET_PREFIX = "";
    private Map substitutions = new HashMap() {
        public Object put(Object key, Object val) {
            if (val == null) throw new IllegalArgumentException("Null value for " + key);
            return super.put(key, val);
        }
    };
    private DBProperties dbp;
    private String exptId;
    private String trialId;
    private String hnaAssemblyId;
    private String csmiAssemblyId;

    private Connection dbConnection;
    private Statement stmt;
    private Statement updateStmt;
    private Map propertyInfos = new HashMap();
    private Set preexistingItems = new HashSet();
    private Set writableComponents = new HashSet();
    private boolean writeEverything = false;
    private boolean debug = false;
    private PrintWriter log;

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
        if (cmtType == null) throw new IllegalArgumentException("null cmtType");
        if (hnaType == null) throw new IllegalArgumentException("null hnaType");
        if (csmiType == null) throw new IllegalArgumentException("null csmiType");
        if (exptId == null) throw new IllegalArgumentException("null exptId");
        if (trialId == null) throw new IllegalArgumentException("null trialId");
        log = new PrintWriter(new FileWriter("PopulateDbQuery.log"));
        dbp = DBProperties.readQueryFile(DATABASE, QUERY_FILE);
//          dbp.setDebug(true);
        String database = dbp.getProperty("database");
        String username = dbp.getProperty("username");
        String password = dbp.getProperty("password");
        String dbtype = dbp.getDBType();
        String driverParam = "driver." + dbtype;
        String driverClass = Parameters.findParameter(driverParam);
        if (driverClass == null)
            throw new SQLException("Unknown driver " + driverParam);
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException cnfe) {
            throw new SQLException("Driver class not found: " + driverClass);
        }
        dbConnection = DBConnectionPool.getConnection(database, username, password);
        stmt = dbConnection.createStatement();
        updateStmt = dbConnection.createStatement();
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

    private void setAssemblyMatch() {
        substitutions.put(":assembly_match:",
                          "in (select assembly_id from v4_expt_trial_assembly where trial_id = '"
                          + trialId
                          + "')");
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
        executeUpdate(dbp.getQuery(CLEAN_TRIAL_ASSEMBLY, substitutions));
    }

    private void newExperiment(String idType, String experimentName) throws SQLException {
        String exptIdPrefix = idType + "-";
        substitutions.put(":expt_id_pattern:", exptIdPrefix + "____");
        substitutions.put(":expt_type:", idType);
        substitutions.put(":expt_name:", experimentName);
        substitutions.put(":description:", experimentName);
        DecimalFormat exptIdFormat =
            new DecimalFormat(exptIdPrefix + "0000");
        ResultSet rs =
            executeQuery(stmt, dbp.getQuery(QUERY_MAX_EXPT_ID,
                                            substitutions));
        exptId = null;
        if (rs.next()) {
            String maxId = rs.getString(1);
            if (maxId != null) {
                try {
                    int n = exptIdFormat.parse(maxId).intValue();
                    exptId = exptIdFormat.format(n + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Use default
                }
            }
        }
        if (exptId == null) exptId = exptIdFormat.format(1);
        substitutions.put(":expt_id:", exptId);
        executeUpdate(dbp.getQuery(INSERT_EXPT_ID, substitutions));
    }

    public String getTrialId() {
        return trialId;
    }

    private void newTrial(String idType, String trialName)
        throws SQLException
    {
        String trialIdPrefix = idType + "-";
        substitutions.put(":trial_id_pattern:", trialIdPrefix + "____");
        substitutions.put(":trial_type:", idType);
        DecimalFormat trialIdFormat =
            new DecimalFormat(trialIdPrefix + "0000");
        ResultSet rs =
            executeQuery(stmt, dbp.getQuery(QUERY_MAX_TRIAL_ID,
                                            substitutions));
        trialId = null;
        if (rs.next()) {
            String maxId = rs.getString(1);
            if (maxId != null) {
                try {
                    int n = trialIdFormat.parse(maxId).intValue();
                    trialId = trialIdFormat.format(n + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Use default
                }
            }
        }
        if (trialId == null) trialId = trialIdFormat.format(1);
        substitutions.put(":trial_id:", trialId);
        substitutions.put(":description:", "Modified Trial");
        substitutions.put(":trial_name:", trialName);
        executeUpdate(dbp.getQuery(INSERT_TRIAL_ID, substitutions));
    }

    private String addAssembly(String idType)
        throws SQLException
    {
        String assemblyId;
        String assemblyIdPrefix = idType + "-";
        substitutions.put(":assembly_id_pattern:", assemblyIdPrefix + "____");
        substitutions.put(":assembly_type:", idType);
        DecimalFormat assemblyIdFormat =
            new DecimalFormat(assemblyIdPrefix + "0000");
        ResultSet rs =
            executeQuery(stmt, dbp.getQuery(QUERY_MAX_ASSEMBLY_ID,
                                            substitutions));
        assemblyId = null;
        if (rs.next()) {
            String maxId = rs.getString(1);
            if (maxId != null) {
                try {
                    int n = assemblyIdFormat.parse(maxId).intValue();
                    assemblyId = assemblyIdFormat.format(n + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Use default
                }
            }
        }
        if (assemblyId == null) assemblyId = assemblyIdFormat.format(1);
        substitutions.put(":assembly_id:", sqlQuote(assemblyId));
        substitutions.put(":trial_id:", trialId);
        executeUpdate(dbp.getQuery(INSERT_ASSEMBLY_ID, substitutions));
        executeUpdate(dbp.getQuery(INSERT_TRIAL_ASSEMBLY, substitutions));
        return assemblyId;
    }

    private void copyCMTAssemblies(String oldTrialId, String newTrialId)
        throws SQLException
    {
        substitutions.put(":old_trial_id:", oldTrialId);
        substitutions.put(":new_trial_id:", newTrialId);
        executeUpdate(dbp.getQuery(COPY_CMT_ASSEMBLIES, substitutions));
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

    /**
     * Utility method to perform an executeUpdate statement. Also
     * additional code to be added for each executeUpdate for
     * debugging purposes.
     **/
    private int executeUpdate(String query) throws SQLException {
        if (query == null) throw new IllegalArgumentException("executeUpdate: null query");
        try {
            long startTime = 0;
            if (log != null)
                startTime = System.currentTimeMillis();
            int result = updateStmt.executeUpdate(query);
            if (log != null) {
                long endTime = System.currentTimeMillis();
                log.println((endTime - startTime) + " " + query);
            }
            return result;
        } catch (SQLException sqle) {
            System.err.println("SQLException query: " + query);
            throw sqle;
        }
    }

    /**
     * Utility method to perform an executeQuery statement. Also
     * additional code to be added for each executeQuery for
     * debugging purposes.
     **/
    private ResultSet executeQuery(Statement stmt, String query) throws SQLException {
        if (query == null) throw new IllegalArgumentException("executeQuery: null query");
        try {
            long startTime = 0;
            if (log != null)
                startTime = System.currentTimeMillis();
            ResultSet rs = stmt.executeQuery(query);
            if (log != null) {
                long endTime = System.currentTimeMillis();
                log.println((endTime - startTime) + " " + query);
            }
            return rs;
        } catch (SQLException sqle) {
            System.err.println("SQLException query: " + query);
            throw sqle;
        }
    }

    /**
     * Enables debugging
     **/
    public void setDebug(boolean newDebug) {
        debug = newDebug;
        dbp.setDebug(newDebug);
    }

    /**
     * Indicates that this is no longer needed. Closes the database
     * connection. Well-behaved users of this class will close when
     * done. Otherwise, the finalizer will close it.
     **/
    public synchronized void close() throws SQLException {
        if (dbConnection != null) {
            dbConnection.commit();
            dbConnection.close();
            dbConnection = null;
        }
    }

    protected void finalize() {
        try {
            if (dbConnection != null) close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
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
     * different assembly describing the effect of impacts an metrics.
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
            substitutions.put(":component_name:", sqlQuote(data.getName()));
            substitutions.put(":component_lib_id:", getComponentLibId(data));
            substitutions.put(":component_alib_id:", getComponentAlibId(data));
            substitutions.put(":component_category:", getComponentCategory(data));
            ResultSet rs = executeQuery(stmt, dbp.getQuery(CHECK_LIB_COMPONENT, substitutions));
            if (rs.next()) {    // Already exists
            } else {            // Need to add it
                substitutions.put(":component_class:", sqlQuote(data.getClassName()));
                substitutions.put(":insertion_point:", getComponentInsertionPoint(data));
                substitutions.put(":description:", sqlQuote("Added " + data.getType()));
                executeUpdate(dbp.getQuery(INSERT_LIB_COMPONENT, substitutions));
            }
            rs.close();
            rs = executeQuery(stmt, dbp.getQuery(CHECK_ALIB_COMPONENT, substitutions));
            if (rs.next()) {    // Already exists
                // Use the one that is there
            } else {
                executeUpdate(dbp.getQuery(INSERT_ALIB_COMPONENT, substitutions));
                result = true;
            }
            rs.close();
            if (isAdded) {
                Object[] params = data.getParameters();
                for (int i = 0; i < params.length; i++) {
                    System.out.println("param value = " + params[i]);
                    substitutions.put(":argument_value:", sqlQuote(params[i].toString()));
                    substitutions.put(":argument_order:", sqlQuote(String.valueOf(i + 1)));
                    executeUpdate(dbp.getQuery(INSERT_COMPONENT_ARG, substitutions));
                    result = true;
                }
            }
        }
        if (isAgent) {
            // Must be a metric or impact agent because that's all that gets added
            populateAgent(data, isAdded);
            result = true;
            force = true;   // Force writing of all plugins, too
        }

        ComponentData parent = data.getParent();
        if (parent != null) {
            String parentType = parent.getType();
            if (!parentType.equals(ComponentData.SOCIETY)) {
                substitutions.put(":parent_component_alib_id:", getComponentAlibId(parent));
                substitutions.put(":component_alib_id:", getComponentAlibId(data));
                substitutions.put(":insertion_order:", String.valueOf(insertionOrder));
                ResultSet rs =
                    executeQuery(stmt, dbp.getQuery(CHECK_COMPONENT_HIERARCHY, substitutions));
                if (!rs.next()) {
                    executeUpdate(dbp.getQuery(INSERT_COMPONENT_HIERARCHY, substitutions));
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
            executeUpdate(dbp.getQuery(INSERT_AGENT_ORG, substitutions));
            PropGroupData[] pgs = assetData.getPropGroups();
            for (int i = 0; i < pgs.length; i++) {
                PropGroupData pg = pgs[i];
                String pgName = pg.getName();
                PGPropData[] props = pg.getProperties();
                for (int j = 0; j < props.length; j++) {
                    PGPropData prop = props[i];
                    PropertyInfo propInfo = getPropertyInfo(pgName, prop.getName());
                    substitutions.put(":component_alib_id:", getComponentAlibId(data));
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
                            executeUpdate(dbp.getQuery(INSERT_ATTRIBUTE, substitutions));
                        }
                    } else {
                        if (prop.isListType())
                            throw new RuntimeException("Property is not a single value: "
                                                       + propInfo.toString());
                        substitutions.put(":attribute_value:", sqlQuote(prop.getValue().toString()));
                        substitutions.put(":attribute_order:", "1");
                        executeUpdate(dbp.getQuery(INSERT_ATTRIBUTE, substitutions));
                    }
                }
            }
        }
        RelationshipData[] relationships = assetData.getRelationshipData();
        for (int i = 0; i < relationships.length; i++) {
            RelationshipData r = relationships[i];
            long startTime = r.getStartTime();
            long endTime = r.getEndTime();
            substitutions.put(":role:", sqlQuote(r.getRole()));
            substitutions.put(":supporting:", getComponentAlibId(data));
            substitutions.put(":supported:", getAgentAlibId(r.getSupported()));
            substitutions.put(":start_date:", "?");
            substitutions.put(":end_date:", "?");
            String query = dbp.getQuery(CHECK_RELATIONSHIP, substitutions);
            PreparedStatement pstmt = dbConnection.prepareStatement(query);
            pstmt.setTimestamp(1, new Timestamp(r.getStartTime()));
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                query = dbp.getQuery(INSERT_RELATIONSHIP, substitutions);
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
            ResultSet rs = executeQuery(stmt, dbp.getQuery(QUERY_LIB_PG_ATTRIBUTE, substitutions));
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
    private String getComponentAlibId(ComponentData data) {
        if (data == null) return sqlQuote(null);
        String result = data.getAlibID();
        if (result != null) return sqlQuote(result);
        String componentType = data.getType();
        if (componentType.equals(ComponentData.PLUGIN)) {
            String agentName = findAncestorOfType(data, ComponentData.AGENT).getName();
            return sqlQuote(agentName + "|" + data.getClassName());
        }
        if (componentType.equals(ComponentData.AGENT)) {
            return getAgentAlibId(data.getName());
        }
        if (componentType.equals(ComponentData.NODE)) {
            return sqlQuote(data.getName());
        }
        ComponentData parent = data.getParent();
        return sqlQuote(getFullName(data));
    }

    /**
     * The convention for the alib id of an agent component is that it
     * is the base agent name prefixed with a clone set id and a
     * hyphen. We are not present concerned with clone set ids, so we
     * use a fixed CLONE_SET_ID.
     **/
    private String getAgentAlibId(String agentName) {
        return sqlQuote(CLONE_SET_PREFIX + agentName);
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
     * Quote a string for SQL. We don't double quotes that appear in
     * strings because we have no cases where such quotes occur.
     **/
    private static String sqlQuote(String s) {
        if (s == null) return "null";
        int quoteIndex = s.indexOf('\'');
        while (quoteIndex >= 0) {
            s = s.substring(0, quoteIndex) + "''" + s.substring(quoteIndex + 1);
            quoteIndex = s.indexOf('\'', quoteIndex + 2);
        }
        return "'" + s + "'";
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

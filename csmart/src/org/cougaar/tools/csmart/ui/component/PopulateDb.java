package org.cougaar.tools.csmart.ui.component;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
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
    public static final String INSERT_COMPONENT_HIERARCHY = "insertComponentHierarchy";
    public static final String INSERT_COMPONENT_ARG = "insertComponentArg";
    public static final String INSERT_AGENT_ORG = "insertAgentOrg";
    public static final String INSERT_ATTRIBUTE = "insertAttribute";
    public static final String INSERT_RELATIONSHIP = "insertRelationship";
    public static final String QUERY_LIB_PG_ATTRIBUTE = "queryLibPGAttribute";
    public static final String QUERY_MAX_ASSEMBLY_ID = "queryMaxAssemblyId";
    public static final String INSERT_ASSEMBLY_ID = "insertAssemblyId";
    public static final String INSERT_TRIAL_ASSEMBLY = "insertTrialAssembly";
    public static final String CHECK_ALIB_COMPONENT = "checkAlibComponent";
    public static final String CLONE_SET_ID = "1";
    private Map substitutions = new HashMap();
    private DBProperties dbp;
    private String assemblyId;

    private Connection dbConnection;
    private Statement stmt;
    private Map propertyInfos = new HashMap();
    private Set readOnlyComponents = new HashSet();
    private boolean debug = false;

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
    public PopulateDb(String assemblyIdPrefix, String exptId, String trialId)
        throws SQLException, IOException
    {
        dbp = DBProperties.readQueryFile(DATABASE, QUERY_FILE);
        dbp.setDebug(true);
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
        setAssemblyId(assemblyIdPrefix, exptId, trialId);
    }

    public String getAssemblyId() {
        return assemblyId;
    }

    private void setAssemblyId(String assemblyIdPrefix, String exptId, String trialId)
        throws SQLException
    {
        substitutions.put(":assembly_id_pattern", assemblyIdPrefix + "____");
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
        substitutions.put(":assembly_id", sqlQuote(assemblyId));
        substitutions.put(":expt_id", exptId);
        substitutions.put(":trial_id", trialId);
        executeUpdate(stmt, dbp.getQuery(INSERT_ASSEMBLY_ID, substitutions));
        executeUpdate(stmt, dbp.getQuery(INSERT_TRIAL_ASSEMBLY, substitutions));
    }

    public void setReadOnlyComponents(ComponentData data) {
        System.out.println("readonly component " + data.getName());
        readOnlyComponents.add(data);
        ComponentData[] children = data.getChildren();
        for (int i = 0; i < children.length; i++) {
            setReadOnlyComponents(children[i]);
        }
    }

    /**
     * Utility method to perform an executeUpdate statement. Also
     * additional code to be added for each executeUpdate for
     * debugging purposes.
     **/
    private void executeUpdate(Statement stmt, String query) throws SQLException {
        if (query == null) throw new IllegalArgumentException("executeUpdate: null query");
        try {
            stmt.executeUpdate(query);
        } catch (SQLException sqle) {
            if (debug) sqle.printStackTrace();
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
            return stmt.executeQuery(query);
        } catch (SQLException sqle) {
            if (debug) sqle.printStackTrace();
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
    public void close() throws SQLException {
        dbConnection.commit();
        dbConnection.close();
        dbConnection = null;
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
    public void populate(ComponentData data, float insertionOrder)
        throws SQLException
    {
        if (needsPopulating(data)) {
            ComponentData parent = data.getParent();
            substitutions.put(":component_name", sqlQuote(data.getName()));
            substitutions.put(":component_lib_id", getComponentLibId(data));
            substitutions.put(":component_alib_id", getComponentAlibId(data));
            substitutions.put(":component_category", getComponentCategory(data));
            ResultSet rs = executeQuery(stmt, dbp.getQuery(CHECK_ALIB_COMPONENT, substitutions));
            if (rs.next()) {    // Already exists
                // Use the one that is there
            } else {
                executeUpdate(stmt, dbp.getQuery(INSERT_ALIB_COMPONENT, substitutions));
            }
            rs.close();
            if (parent != null) {
                substitutions.put(":parent_component_alib_id", getComponentAlibId(parent));
                substitutions.put(":insertion_order", String.valueOf(insertionOrder));
                executeUpdate(stmt, dbp.getQuery(INSERT_COMPONENT_HIERARCHY, substitutions));
            }
            Object[] params = data.getParameters();
            for (int i = 0; i < params.length; i++) {
                substitutions.put(":argument_value", sqlQuote(params[i].toString()));
                substitutions.put(":argument_order", sqlQuote(String.valueOf(i + 1)));
                executeUpdate(stmt, dbp.getQuery(INSERT_COMPONENT_ARG, substitutions));
            }
            if (data.getType().equals(ComponentData.AGENT)) {
                populateAgent(data);
            }
        } else {
            System.out.println("Database write not needed for "
                               + data.getName());
        }

        ComponentData[] children = data.getChildren();

        // The following assumes that the insertion order of old
        // children equals their index in the array and that the
        // insertion order of all new children should be the their
        // index  in the array as well.
        for (int i = 0, n = children.length; i < n; i++) {
            populate(children[i], i);
        }
    }

    /**
     * Special processing for an agent component because agents
     * represent organizations have relationships and property groups.
     **/
    private void populateAgent(ComponentData data) throws SQLException {
        AgentAssetData assetData = data.getAgentAssetData();
        if (assetData == null) return;
        substitutions.put(":component_lib_id", getComponentLibId(data));
        substitutions.put(":agent_org_prototype", sqlQuote(assetData.getAssetClass()));
        executeUpdate(stmt, dbp.getQuery(INSERT_AGENT_ORG, substitutions));
        RelationshipData[] relationships = assetData.getRelationshipData();
        for (int i = 0; i < relationships.length; i++) {
            RelationshipData r = relationships[i];
            if (r.getRole().equals(RelationshipData.SUPERIOR)) {
                substitutions.put(":role", sqlQuote("Subordinate"));
            } else {
                substitutions.put(":role", sqlQuote(r.getRole()));
            }
            substitutions.put(":supporting", getComponentAlibId(data));
            substitutions.put(":supported", getAgentAlibId(r.getCluster()));
            substitutions.put(":start_date", sqlQuote(r.getStartTime()));
            substitutions.put(":stop_date", sqlQuote(r.getStopTime()));
            executeUpdate(stmt, dbp.getQuery(INSERT_RELATIONSHIP, substitutions));
        }
        PropGroupData[] pgs = assetData.getPropGroups();
        for (int i = 0; i < pgs.length; i++) {
            PropGroupData pg = pgs[i];
            String pgName = pg.getName();
            PGPropData[] props = pg.getProperties();
            for (int j = 0; j < props.length; j++) {
                PGPropData prop = props[i];
                PropertyInfo propInfo = getPropertyInfo(pgName, prop.getName());
                substitutions.put(":component_alib_id", getComponentAlibId(data));
                substitutions.put(":pg_attribute_lib_id", propInfo.getAttributeLibId());
                substitutions.put(":start_date", sqlQuote("2000-01-01 00:00:00"));
                substitutions.put(":end_date", sqlQuote(null));
                if (propInfo.isCollection()) {
                    if (!prop.isListType())
                        throw new RuntimeException("Property is not a collection: "
                                               + propInfo.toString());
                    String[] values = ((PGPropMultiVal) prop.getValue()).getValuesStringArray();
                    for (int k = 0; k < values.length; k++) {
                        substitutions.put(":attribute_value", sqlQuote(values[k]));
                        substitutions.put(":attribute_order", String.valueOf(k + 1));
                        executeUpdate(stmt, dbp.getQuery(INSERT_ATTRIBUTE, substitutions));
                    }
                } else {
                    if (prop.isListType())
                        throw new RuntimeException("Property is not a single value: "
                                               + propInfo.toString());
                    substitutions.put(":attribute_value", sqlQuote(prop.getValue().toString()));
                    substitutions.put(":attribute_order", "1");
                    executeUpdate(stmt, dbp.getQuery(INSERT_ATTRIBUTE, substitutions));
                }
            }
        }
    }

    /**
     * Override this to select which components should be written to
     * the database. Default implementation writes all non-readonly
     * components.
     **/
    protected boolean needsPopulating(ComponentData data) {
        return !readOnlyComponents.contains(data);
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
            substitutions.put(":pg_name", pgName);
            substitutions.put(":attribute_name", propName);
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
        ComponentData parent = data.getParent();
        return sqlQuote(data.getType() + "|" + getFullName(data));
    }

    /**
     * Create a component alib id for this component. Again, each kind
     * of component has a different convention for constructing its
     * alib id.
     **/
    private String getComponentAlibId(ComponentData data) {
        if (data == null) return sqlQuote(null);
        String componentType = data.getType();
        if (componentType.equals(ComponentData.PLUGIN)) {
            String agentName = findAncestorOfType(data, ComponentData.AGENT).getName();
            return sqlQuote(agentName + "|" + data.getClassName());
        }
        if (componentType.equals(ComponentData.AGENT)) {
            return getAgentAlibId(data.getName());
        }
        ComponentData parent = data.getParent();
        return sqlQuote(assemblyId + "|" + getFullName(data));
    }

    /**
     * The convention for the alib id of an agent component is that it
     * is the base agent name prefixed with a clone set id and a
     * hyphen. We are not present concerned with clone set ids, so we
     * use a fixed CLONE_SET_ID.
     **/
    private String getAgentAlibId(String agentName) {
        return sqlQuote(CLONE_SET_ID + "-" + agentName);
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

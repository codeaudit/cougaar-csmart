package org.cougaar.tools.csmart.ui.component;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.Map;
import java.util.HashMap;
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
    public static final String CLONE_SET_ID = "1";
    private Map substitutions = new HashMap();
    private DBProperties dbp;
    private String assemblyId;

    private Connection dbConnection;
    private Statement stmt;
    private Map propertyInfos = new HashMap();

    private static class PropertyKey {
        private String pgName;
        private String propName;
        private int hc;

        public PropertyKey(String pgName, String propName) {
            this.pgName = pgName;
            this.propName = propName;
            hc = pgName.hashCode() + propName.hashCode();
        }

        public String getPGName() {
            return pgName;
        }

        public String getPropName() {
            return propName;
        }

        public int hashCode() {
            return hc;
        }

        public boolean equals(Object o) {
            if (!(o instanceof PropertyKey)) return false;
            PropertyKey that = (PropertyKey) o;
            return this.pgName.equals(that.pgName) &&
                that.propName.equals(that.propName);
        }
        public String toString() {
            return pgName + "|" + propName;
        }
    }

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

    public PopulateDb(String assemblyId) throws SQLException, IOException, ClassNotFoundException {
        dbp = DBProperties.readQueryFile(DATABASE, QUERY_FILE);
        String database = dbp.getProperty("database");
        String username = dbp.getProperty("username");
        String password = dbp.getProperty("password");
        String dbtype = dbp.getDBType();
        String driverParam = "driver." + dbtype;
        String driverClass = Parameters.findParameter(driverParam);
        if (driverClass == null)
            throw new SQLException("Unknown driver " + driverParam);
        Class.forName(driverClass);
        dbConnection = DBConnectionPool.getConnection(database, username, password);
        stmt = dbConnection.createStatement();
        this.assemblyId = assemblyId;
        substitutions.put(":assembly_id", sqlQuote(assemblyId));
    }

    private static void executeUpdate(Statement stmt, String query) throws SQLException {
        if (query == null) throw new IllegalArgumentException("executeUpdate: null query");
        try {
            stmt.executeUpdate(query);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.exit(-1);
            throw sqle;
        }
    }

    private static ResultSet executeQuery(Statement stmt, String query) throws SQLException {
        if (query == null) throw new IllegalArgumentException("executeQuery: null query");
        try {
            return stmt.executeQuery(query);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            throw sqle;
        }
    }

    public void setDebug(boolean newDebug) {
        dbp.setDebug(newDebug);
    }

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
     * Populating the table for a particular item is pretty much just
     * the insertion of a row into the ALIB_COMPONENT and ASB_HIERARCHY table.
     **/
    public void populate(ComponentData data, float insertionOrder) throws SQLException {
        ComponentData parent = data.getParent();
        substitutions.put(":component_name", sqlQuote(data.getName()));
        substitutions.put(":component_lib_id", getComponentLibId(data));
        substitutions.put(":component_alib_id", getComponentAlibId(data));
        substitutions.put(":component_category", getComponentCategory(data));
        executeUpdate(stmt, dbp.getQuery(INSERT_ALIB_COMPONENT, substitutions));
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
        ComponentData[] children = data.getChildren();
        System.out.println(children.length + " children");
        int i0 = -1;
        float insertionPoint = 0f;
        for (int i = 0, n = children.length; i <= n; i++) {
            if (i == n || !needsPopulating(children[i])) {
                if (i0 >= 0) {
                    float step = 1f / (i - i0 + 1f);
                    for (int j = i0; j < i; j++) {
                        populate(children[j], insertionPoint + (j - i0 + 1f) * step);
                    }
                    i0 = -1;    // Nothing deferred
                }
            }
            if (i < n && i0 < 0 && needsPopulating(children[i])) {
                i0 = i;
            }
        }
    }

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

    private boolean needsPopulating(ComponentData data) {
        return true;
    }

    private PropertyInfo getPropertyInfo(String pgName, String propName) throws SQLException {
        PropertyKey key = new PropertyKey(pgName, propName);
        PropertyInfo result = (PropertyInfo) propertyInfos.get(key);
        if (result == null) {
            Statement stmt = dbConnection.createStatement();
            substitutions.put(":pg_name", pgName);
            substitutions.put(":attribute_name", propName);
            ResultSet rs = executeQuery(stmt, dbp.getQuery(QUERY_LIB_PG_ATTRIBUTE, substitutions));
            if (!rs.next()) throw new RuntimeException("No such property: " + pgName + "|" + propName);
            result = new PropertyInfo(key,
                                      rs.getString(1),
                                      rs.getString(2),
                                      rs.getString(3));
            rs.close();
            stmt.close();
            propertyInfos.put(key, result);
        }
        return result;
    }

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
     * Create a component alib id for this component
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

    private String getAgentAlibId(String agentName) {
        return sqlQuote(CLONE_SET_ID + "-" + agentName);
    }

    private String getFullName(ComponentData data) {
        ComponentData parent = data.getParent();
        if (parent == null) return data.getName();
        return getFullName(parent) + "|" + data.getName();
    }

    private String getComponentCategory(ComponentData data) {
        return sqlQuote(data.getType());
    }

    private static String sqlQuote(String s) {
        if (s == null) return "null";
        return "'" + s + "'";
    }

    private ComponentData findAncestorOfType(ComponentData data, String type) {
        for (ComponentData parent = data.getParent(); parent != null; parent = parent.getParent()) {
            if (parent.getType().equals(type)) return parent;
        }
        return null;
    }
}

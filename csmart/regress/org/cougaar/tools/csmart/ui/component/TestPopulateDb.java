package org.cougaar.tools.csmart.ui.component;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import junit.framework.*;
import org.cougaar.core.cluster.ClusterImpl;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.society.DBInitializerServiceProvider;
import org.cougaar.core.society.InitializerService;
import org.cougaar.core.society.InitializerServiceException;
import org.cougaar.core.society.Node;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.DBProperties;
import org.cougaar.util.Parameters;

/**
 * A JUnit TestCase for testing the NamingDirContext and server
 **/

public class TestPopulateDb extends TestCase {
    public static final String AGENT_CLASS = ClusterImpl.class.getName();
    public static final String AGENT_INSERTION_POINT = "Node.AgentManager.Agent";
    public static final String AGENT_TYPE = "agent";
    public static final String CMT_TYPE = "REGRESSION-CMT";
    public static final String HNA_TYPE = "REGRESSION-HNA";
    public static final String CSMI_TYPE = "REGRESSION-CSMI";
    public static final String DATABASE = "org.cougaar.configuration.database";
    public static final String DELETE_ALIB_COMPONENT = "deleteAlibComponent";
    public static final String DELETE_ASSEMBLY = "deleteAssembly";
    public static final String DELETE_EXPT = "deleteExpt";
    public static final String DELETE_FROM_TABLE_INITIAL = "deleteFromTableInitial";
    public static final String DELETE_FROM_TABLE_MORE = "deleteFromTableMore";
    public static final String DELETE_FROM_TABLE_WITH_ASSEMBLY_ID = "deleteFromTableWithAssemblyId";
    public static final String DELETE_FROM_TABLE_WITH_EXPT_ID = "deleteFromTableWithExptId";
    public static final String DELETE_FROM_TABLE_WITH_TRIAL_ID = "deleteFromTableWithTrialId";
    public static final String DELETE_LIB_COMPONENT = "deleteLibComponent";
    public static final String DELETE_TRIAL = "deleteTrial";
    public static final String EXPT_PREFIX = "REGRESSION-";
    public static final String INSERT_ASSEMBLY = "insertAssembly";
    public static final String INSERT_EXPT = "insertExpt";
    public static final String INSERT_LIB_COMPONENT = "insertLibComponent";
    public static final String INSERT_TRIAL = "insertTrial";
    public static final String INSERT_TRIAL_ASSEMBLY = "insertTrialAssembly";
    public static final String INSERT_TRIAL_EXPT = "insertTrialExpt";
    public static final String INSERT_TRIAL_TRIAL = "insertTrialTrial";
    public static final String NODE_CLASS = Node.class.getName();
    public static final String NODE_INSERTION_POINT = "Node";
    public static final String NODE_TYPE = "node";
    public static final String PLUGIN_INSERTION_POINT = "Node.AgentManager.Agent.PluginManager.Plugin";
    public static final String PLUGIN_TYPE = "plugin";
    public static final String QUERY_ALL_TABLE_COLUMNS = "queryAllTableColumns";
    public static final String QUERY_FILE = "TestPopulateDb.q";
    public static final String QUERY_LIB_COMPONENT = "queryLibComponent";
    public static final String QUERY_MAX_ASSEMBLY = "queryMaxAssembly";
    public static final String QUERY_MAX_EXPT = "queryMaxExpt";
    public static final String QUERY_MAX_TRIAL = "queryMaxTrial";
    public static final String QUERY_TABLE_WITH_ASSEMBLY_ID = "queryTableWithAssemblyId";
    public static final String QUERY_TABLE_WITH_EXPT_ID = "queryTableWithExptId";
    public static final String QUERY_TABLE_WITH_TRIAL_ID = "queryTableWithTrialId";
    public static final String TRIAL_PREFIX = "REGRESSION-";

    private static class NodeData {
        public String name;
        public String className;
        AgentData[] agentData;
        public NodeData(String name, AgentData[] agentData) {
            this.name = name;
            this.className = NODE_CLASS;
            this.agentData = agentData;
        }
    }
    private static class AgentData {
        public String name;
        public String className;
        PluginData[] pluginData;
        public AgentData(String name, PluginData[] pluginData) {
            this.name = name;
            this.className = AGENT_CLASS;
            this.pluginData = pluginData;
        }
    }
    private static class PluginData {
        public String name;
        public String className;
        String[] paramData;
        public PluginData(String name, String[] paramData) {
            this.name = name;
            this.className = name;
            this.paramData = paramData;
        }
    }

    private static final NodeData[] nodeData = {
        new NodeData("REGRESSION-NODE-1",
                     new AgentData[] {
                         new AgentData("REGRESSION-AGENT-1-1",
                                       new PluginData[] {
                                           new PluginData("REGRESSION-PLUGIN-1-1-1",
                                                          new String[] {
                                                              "Param-1-1-1-1",
                                                              "Param-1-1-1-2",
                                                              "Param-1-1-1-3"
                                                          }),
                                           new PluginData("REGRESSION-PLUGIN-1-1-2",
                                                          new String[] {
                                                              "Param-1-1-2-1",
                                                              "Param-1-1-2-2",
                                                              "Param-1-1-2-3"
                                                          })}),
                         new AgentData("REGRESSION-AGENT-1-2",
                                       new PluginData[] {
                                           new PluginData("REGRESSION-PLUGIN-1-2-1",
                                                          new String[] {
                                                              "Param-1-2-1-1",
                                                              "Param-1-2-1-2",
                                                              "Param-1-2-1-3"
                                                          }),
                                           new PluginData("REGRESSION-PLUGIN-1-2-2",
                                                          new String[] {
                                                              "Param-1-2-2-1",
                                                              "Param-1-2-2-2",
                                                              "Param-1-2-2-3"
                                                          }),
                                           new PluginData("REGRESSION-PLUGIN-1-2-3",
                                                          new String[] {
                                                              "Param-1-2-3-1",
                                                              "Param-1-2-3-2"
                                                          })}),
                         new AgentData("REGRESSION-AGENT-1-3",
                                       new PluginData[] {
                                           new PluginData("REGRESSION-PLUGIN-1-3-1",
                                                          new String[] {
                                                              "Param-1-3-1-1",
                                                              "Param-1-3-1-2",
                                                              "Param-1-3-1-3"
                                                          }),
                                           new PluginData("REGRESSION-PLUGIN-1-3-2",
                                                          new String[] {
                                                              "Param-1-3-2-1"
                                                          })})})
    };

    private PopulateDb pdb;
    private DBProperties dbp;
    private String cmtAssemblyId;
    private String exptId;
    private String trialId;
    private ServiceProvider initializerServiceProvider;
    private InitializerService initializerService;
    Map substitutions = new HashMap();

    public TestPopulateDb(String name) {
        super(name);
    }

    private static int executeUpdate(Statement stmt, String query) throws SQLException {
        if (query == null) throw new IllegalArgumentException("executeUpdate: null query");
        try {
            return stmt.executeUpdate(query);
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

    private DBProperties getDBProperties() throws IOException {
        URL url = getClass().getResource(QUERY_FILE);
        assertNotNull("Null url for " + QUERY_FILE, url);
        DBProperties dbp = DBProperties.readQueryFile(url);
        return dbp;
    }

    private static String sqlQuote(String s) {
        if (s == null) return "null";
        return "'" + s + "'";
    }

    private Connection openConnection() throws SQLException, ClassNotFoundException {
        String database = dbp.getProperty("database");
        String username = dbp.getProperty("username");
        String password = dbp.getProperty("password");
        String dbtype = dbp.getDBType();
        String driverParam = "driver." + dbtype;
        String driverClass = Parameters.findParameter(driverParam);
        if (driverClass == null)
            throw new SQLException("Unknown driver " + driverParam);
        Class.forName(driverClass);
        return DBConnectionPool.getConnection(database, username, password);
    }

    public void cleanAll(String cleanType) throws SQLException, IOException, ClassNotFoundException {
        dbp = getDBProperties();
        Connection dbConnection = openConnection();
        try {
            removeAll(dbConnection, cleanType);
            dbConnection.commit();
        } finally {
            dbConnection.close();
        }
    }

    public void setUp() throws SQLException, IOException, ClassNotFoundException {
        Map substitutions = new HashMap();
        dbp = getDBProperties();
//          dbp.setDebug(true);
        Connection dbConnection = openConnection();
        try {
            insertLib(dbConnection);
            insertExpt(dbConnection);
            insertTrial(dbConnection);
            cmtAssemblyId = insertAssembly(dbConnection, CMT_TYPE);
            dbConnection.commit();
        } finally {
            dbConnection.close();
        }
        initializerServiceProvider = new DBInitializerServiceProvider(trialId);
        initializerService = (InitializerService)
            initializerServiceProvider.getService(null, this, InitializerService.class);
    }

    public void tearDown() throws SQLException, ClassNotFoundException {
//          Connection dbConnection = openConnection();
//          removeAll(dbConnection);
//          dbConnection.close();
        initializerServiceProvider.releaseService(null,
                                                  this,
                                                  InitializerService.class,
                                                  initializerService);
        dbp = null;
        substitutions = null;
    }

    /**
     * Insert a regression assembly with the next higher number
     **/
    private String insertAssembly(Connection dbConnection, String type) throws SQLException {
        MessageFormat assemblyIdFormat = new MessageFormat(type + "-" + "{0,number,0000}");
        substitutions.put(":assembly_type", CMT_TYPE);
        String assemblyId = getNextId(dbConnection, QUERY_MAX_ASSEMBLY, assemblyIdFormat);
        Statement stmt = dbConnection.createStatement();
        substitutions.put(":assembly_id", assemblyId);
        substitutions.put(":description", "TestPopulateDb " + new Date().toString());
        executeUpdate(stmt, dbp.getQuery(INSERT_ASSEMBLY, substitutions));
        executeUpdate(stmt, dbp.getQuery(INSERT_TRIAL_ASSEMBLY, substitutions));
        stmt.close();
        return assemblyId;
    }

    /**
     * Insert a regression trial with the next higher number
     **/
    private void insertTrial(Connection dbConnection) throws SQLException {
        MessageFormat trialIdFormat = new MessageFormat(TRIAL_PREFIX + "{0,number,0000}");
        trialId = getNextId(dbConnection, QUERY_MAX_TRIAL, trialIdFormat);
        Statement stmt = dbConnection.createStatement();
        substitutions.put(":trial_id", trialId);
        substitutions.put(":description", "TestPopulateDb " + new Date().toString());
        String insert = dbp.getQuery(INSERT_TRIAL, substitutions);
        executeUpdate(stmt, insert);
        stmt.close();
    }

    /**
     * Insert a regression experiment with the next higher number
     **/
    private void insertExpt(Connection dbConnection) throws SQLException {
        MessageFormat exptIdFormat = new MessageFormat(EXPT_PREFIX + "{0,number,0000}");
        exptId = getNextId(dbConnection, QUERY_MAX_EXPT, exptIdFormat);
        Statement stmt = dbConnection.createStatement();
        substitutions.put(":expt_id", exptId);
        substitutions.put(":description", "TestPopulateDb " + new Date().toString());
        String insert = dbp.getQuery(INSERT_EXPT, substitutions);
        executeUpdate(stmt, insert);
        stmt.close();
    }

    /**
     * Insert lib definitions for components to be inserted during testing
     **/
    private void insertLib(Connection dbConnection) throws SQLException {
        for (int k = 0; k < nodeData.length; k++) {
            insertLibComponent(dbConnection,
                               nodeData[k].name,
                               NODE_TYPE,
                               nodeData[k].className,
                               NODE_INSERTION_POINT);
            for (int i = 0; i < nodeData[k].agentData.length; i++) {
                AgentData agentData = nodeData[k].agentData[i];
                insertLibComponent(dbConnection,
                                   agentData.name,
                                   AGENT_TYPE,
                                   agentData.className,
                                   AGENT_INSERTION_POINT);
                for (int j = 0; j < agentData.pluginData.length; j++) {
                    PluginData pluginData = agentData.pluginData[j];
                    insertLibComponent(dbConnection,
                                       "plugin|" + pluginData.className,
                                       PLUGIN_TYPE,
                                       pluginData.className,
                                       PLUGIN_INSERTION_POINT);
                }
            }
        }
    }

    private void insertLibComponent(Connection dbConnection,
                                    String libId, String type,
                                    String className, String insertionPoint)
        throws SQLException
    {
        Statement stmt = dbConnection.createStatement();
        try {
            substitutions.put(":component_lib_id", libId);
            substitutions.put(":component_type", type);
            substitutions.put(":component_class", className);
            substitutions.put(":insertion_point", insertionPoint);
            ResultSet rs = executeQuery(stmt, dbp.getQuery(QUERY_LIB_COMPONENT, substitutions));
            if (rs.next()) {
                assertEquals("Lib mismatched type", type, rs.getString(2));
                assertEquals("Lib mismatched class", className, rs.getString(3));
                assertEquals("Lib mismatched insertion point", insertionPoint, rs.getString(4));
            } else {
                executeUpdate(stmt, dbp.getQuery(INSERT_LIB_COMPONENT, substitutions));
            }
        } finally {
            stmt.close();
        }
    }

    /**
     * Remove all regression test data that was inserted during
     * testing. Because of integrity constraints, some deletions will
     * fail so we repeat as long as progress is being made.
     **/
    private void removeAll(Connection dbConnection, String cleanType) throws SQLException {
        Map substitutions = new HashMap();
        Set doneTables = new HashSet();
        Statement stmt = dbConnection.createStatement();
        Statement stmt2 = dbConnection.createStatement();
        boolean done = false;
        SQLException e = null;
        String q = null;
        substitutions.put(":clean_type", cleanType);
        try {
            while (!done) {
                done = true;        // Unless something is found to do
                e = null;
                q = null;
                ResultSet rs = executeQuery(stmt, dbp.getQuery(QUERY_ALL_TABLE_COLUMNS, null));
                String currentTableName = null;
                StringBuffer qbuf = new StringBuffer();
                while (true) {
                    String tableName = null;
                    String columnName = null;
                    if (rs.next()) {
                        tableName = rs.getString(1);
                        if (doneTables.contains(tableName)) continue;
                        columnName = rs.getString(2);
                        substitutions.put(":column", columnName);
                    }
                    if (tableName == null || !tableName.equals(currentTableName)) {
                        if (currentTableName != null) {
                            try {
                                System.out.println("Delete: " + qbuf);
                                int nrows = stmt2.executeUpdate(qbuf.toString());
                                System.out.println("    " + nrows + " rows");
                                if (nrows > 0)
                                    done = false;
                                doneTables.add(currentTableName);
                            } catch (SQLException sqle) {
                                if (e == null) {
                                    e = sqle;
                                    q = qbuf.toString();
                                }
                                System.out.println("    failed");
                                // Probably a constraint violation due to random order of deletion
                            }
                        }
                        if (tableName == null) break; // Done with this loop
                        currentTableName = tableName; // Start next table
                        qbuf.setLength(0);
                        substitutions.put(":table", currentTableName);
                        qbuf.append(dbp.getQuery(DELETE_FROM_TABLE_INITIAL, substitutions));
                    } else {
                        qbuf.append(dbp.getQuery(DELETE_FROM_TABLE_MORE, substitutions));
                    }
                }
                rs.close();
            }
        } finally {
            stmt.close();
            stmt2.close();
        }
        if (e != null) {
            System.err.println(q);
            throw e;
        }
    }

    private String getNextId(Connection dbConnection, String queryName, MessageFormat format) {
        String id = format.format(new Object[] {new Integer(1)}); // Default
        try {
            Statement stmt = dbConnection.createStatement();
            try {
                String query = dbp.getQuery(queryName, substitutions);
                ResultSet rs = executeQuery(stmt, query);
                try {
                    if (rs.next()) {
                        String maxId = rs.getString(1);
                        int n = ((Number) format.parse(maxId)[0]).intValue();
                        id = format.format(new Object[] {new Integer(n + 1)});
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Ignore exceptions and use default
        }
        return id;
    }

    /**
     * Populate the CMT assembly. Since PopulateDb does not usually
     * modify the CMT assembly, we specify the CMT_TYPE for all
     * assemblies. This causes the entire component structure to be
     * stored in the with an assembly id that is identified as CMT.
     **/
    private void populateCMT() throws SQLException, IOException {
        pdb = new PopulateDb("", CMT_TYPE, CMT_TYPE, "REGRESSION Experiment " + exptId,
                             exptId, trialId, false);
//          pdb.setDebug(true);
        for (int i = 0; i < nodeData.length; i++) {
            ComponentData node = new GenericComponentData();
            node.setType(ComponentData.NODE);
            node.setName(nodeData[i].name);
            node.setClassName(nodeData[i].className);
            AgentData[] agentData = nodeData[i].agentData;
            for (int j = 0; j < agentData.length; j++) {
                ComponentData agent = new GenericComponentData();
                agent.setType(ComponentData.AGENT);
                agent.setName(agentData[j].name);
                agent.setClassName(agentData[j].className);
                agent.setParent(node);
                node.addChild(agent);
                PluginData[] pluginData = agentData[j].pluginData;
                for (int k = 0; k < pluginData.length; k++) {
                    ComponentData plugin = new GenericComponentData();
                    plugin.setType(ComponentData.PLUGIN);
                    plugin.setName(pluginData[k].name);
                    plugin.setClassName(pluginData[k].className);
                    plugin.setParameters(pluginData[k].paramData);
                    plugin.setParent(agent);
                    agent.addChild(plugin);
                }
                pdb.populate(agent, 1f);
            }
        }
        pdb.close();
    }

    private void checkCMT() throws SQLException, IOException, InitializerServiceException {
        for (int k = 0; k < nodeData.length; k++) {
            ComponentDescription[] agents =
                initializerService.getComponentDescriptions(nodeData[k].name, AGENT_INSERTION_POINT);
            assertEquals("Wrong number of agents", nodeData[k].agentData.length, agents.length);
            for (int i = 0; i < agents.length; i++) {
                String agentName = agents[i].getName();
                String className = agents[i].getClassname();
                AgentData agentData = nodeData[k].agentData[i];
                assertEquals("Wrong agent name", agentData.name, agentName);
                assertEquals("Wrong agent class ", agentData.className, className);
                ComponentDescription[] plugins =
                    initializerService.getComponentDescriptions(agentData.name, PLUGIN_INSERTION_POINT);
                assertEquals("Wrong number of plugins", agentData.pluginData.length, plugins.length);
                for (int j = 0; j < plugins.length; j++) {
                    String pluginName = plugins[j].getName();
                    className = plugins[j].getClassname();
                    PluginData pluginData = agentData.pluginData[j];
                    assertEquals("Wrong plugin name", pluginData.name, pluginName);
                    assertEquals("Wrong plugin class ", pluginData.className, className);
                    Vector vParams = (Vector) plugins[j].getParameter();
                    String[] params = (String[]) vParams.toArray(new String[0]);
                    assertEquals("Wrong number of parameters", pluginData.paramData.length, params.length);
                    for (int q = 0; q < params.length; q++) {
                        assertEquals("Wrong parameter value", pluginData.paramData[q], params[q]);
                    }
                }
            }
        }
    }

    /**
     *
     **/
    public void test() throws SQLException, IOException, InitializerServiceException {
//          populateCMT();
//          checkCMT();
    }

    /**
     * Run tests. Only used for standalone testing. Usually, the Regress master harness is used.
     **/
    public static void launch(String[] args) {
        String what = "test";
        String cleanType = "REGRESSION";
        if (args.length > 0) what = args[0];
        if (args.length > 1) cleanType = args[1];
        if (what.equals("clean")) {
            try {
                new TestPopulateDb("cleanAll").cleanAll(cleanType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            junit.textui.TestRunner.run(new TestSuite(TestPopulateDb.class));
        }
        System.exit(0);         // May be pool management threads still running, so must explicitly exit
    }
}

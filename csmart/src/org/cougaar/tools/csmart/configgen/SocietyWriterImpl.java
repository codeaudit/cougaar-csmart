/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.configgen;

import java.util.*;
import java.io.*;

import org.cougaar.util.*;

/**
 * 
 * @deprecated
 */
public class SocietyWriterImpl implements SocietyWriter {

  private String toPath;

  public SocietyWriterImpl() {
  }

  public void initialize(String[] args) throws Exception {
    if ((args == null) ||
        (args.length < 2)) {
      throw new IllegalArgumentException(
          "SocietyWriterImpl expecting a second argument"+
          " for the \"toPath\"");
    }

    toPath = args[1];

    // make sure the path ends in "/"
    if (!toPath.endsWith(String.valueOf(File.separatorChar))) {
      toPath = toPath + File.separatorChar;
    }

  }

  public void write(
      Society soc) throws Exception {

    // create the various files for the society
    createRoutingFile(soc);

    createNodeFiles(soc);

    createSocietyFile(soc);

    createClusterFiles(soc);
  }

  /**
   * Creates the <code>cluster-data</code> file, soon to be removed.
   */
  protected void createRoutingFile(
      Society soc) throws Exception {

    String fname = toPath + "cluster-data.csv";
    FileWriter fw = new FileWriter(new File(fname));

    List agents = soc.getAgents();
    for (int i = 0; i < agents.size(); i++) {
      Agent ai = (Agent)agents.get(i);
      fw.write(
          ai.getName() + ", " +
          ai.getName() + "\n");
    }

    fw.close();
  }

  /** 
   * Creates the Cougaar Node files for the agents.
   */
  protected void createNodeFiles(
      Society soc) throws Exception {

    // get the Map of (NodeNames, List of Agents in that Node)
    Map nodes = soc.getNodes();
    if (nodes == null) {
      return;
    }

    // create Node files
    for (Iterator iter = nodes.entrySet().iterator();
        iter.hasNext();
        ) {
      Map.Entry me = (Map.Entry)iter.next();
      String nodeName = (String)me.getKey();
      List nodeAgents = (List)me.getValue();

      createNodeFile(nodeName, nodeAgents);
    }
  }

  /** 
   * Creates a Cougaar Node file for a List of agents.
   * 
   * @param nodeName The name of the node + "Node.ini"
   * @param agents List of all Agents for this Node
   */
  protected void createNodeFile(
      String nodeName, 
      List agents) throws Exception {

    String fname = toPath + nodeName + "Node.ini";
    FileWriter fw = new FileWriter(new File(fname));

    fw.write("[ Clusters ]\n");

    for (int i = 0; i < agents.size(); i++) {
      Agent ai = (Agent)agents.get(i);
      fw.write("cluster=");
      fw.write(ai.getName());
      fw.write("\n");
    }

    fw.close();
  }

  /**
   * Create a society-level agent name and lat/lon position file.
   */
  protected void createSocietyFile(
      Society soc) throws Exception {

    String fname = toPath + "Society.dat";
    FileWriter fw = new FileWriter(new File(fname));

    fw.write("# <AgentName>, <lat>, <lon>\n");

    List agents = soc.getAgents();
    for (int i = 0; i < agents.size(); i++) {
      Agent ai = (Agent)agents.get(i);
      fw.write(ai.getName());
      fw.write(", ");
      fw.write(Float.toString(ai.getLatitude()));
      fw.write(", ");
      fw.write(Float.toString(ai.getLongitude()));
      fw.write("\n");
    }

    fw.close();
  }

  /**
   * Create all cluster-config files for all agents in the society.
   */
  protected void createClusterFiles(
      Society soc) throws Exception {

    List agents = soc.getAgents();
    for (int i = 0; i < agents.size(); i++) {
      Agent ai = (Agent)agents.get(i);
      createClusterFiles(ai);
    }
  }

  /**
   * Creates all cluster-config files required for an Agent.
   */
  protected void createClusterFiles(
      Agent a) throws Exception {

    long customerDemand = a.getCustomerDemand();

    List localAssets = a.getLocalAssets();
    if ((localAssets != null) &&
        (localAssets.size() <= 0)) {
      localAssets = null;
    }

    createClusterINIFile(
        a.getName(), 
        (customerDemand > 0),
        (localAssets != null),
        a.getStartMillis(), 
        a.getStopMillis(), 
        a.getSniffInterval());

    createClusterDATFile(
        a.getName(), 
        a.getLatitude(), 
        a.getLongitude(), 
        a.getRoles(),
        a.getSupportedAgentNames());

    createCustomerTasksFile(
        a.getName(), 
        a.getCustomerTasks(), 
        customerDemand);

    createLocalAssetsFile(
        a.getName(), 
        a.getLocalProduction(),
        localAssets);

    createAllocationTableFile(
        a.getName(), 
        a.getAllocationTable());
  }

  /**
   * Creates a Cougaar Agent File. 
   * 
   * @param agentName Name of the agent
   * @param isCustomer is this agent a Cusomter?
   * @param startMillis Scenario start time
   * @param stopMillis Scenario Stop time
   * @param sniffInterval The sniffer interval
   */
  protected void createClusterINIFile(
      String agentName, 
      boolean isCustomer, 
      boolean hasLocalAssets, 
      long startMillis, 
      long stopMillis, 
      long sniffInterval) throws Exception {

    String fname = 
      toPath + agentName + ".ini";
    FileWriter fw = new FileWriter(new File(fname));

    fw.write("[ Cluster ] \n");
    fw.write("class = org.cougaar.core.cluster.ClusterImpl \n");
    fw.write("uic = " + agentName + "\n");
    fw.write("cloned = false \n");
    fw.write("\n");
    fw.write("[ Plugins ] \n");

    if (hasLocalAssets) {
      fw.write("plugin = org.cougaar.tools.csmart.plugin.LocalAssetBuilder(");
      fw.write(agentName + "_LocalAssets.dat)\n");
    }

    if (isCustomer) {
      fw.write("plugin = org.cougaar.tools.csmart.CustomerPlugIn(");
      fw.write(startMillis + ", ");
      fw.write(stopMillis + ", ");
      fw.write(agentName + "Tasks.dat) \n");
    }

    fw.write("plugin = org.cougaar.tools.csmart.plugin.ExecutorPlugIn \n");
    fw.write("plugin = org.cougaar.tools.csmart.plugin.AllocatorPlugIn(");
    fw.write(agentName + "_Alloc.dat) \n");

    // Add in the Sniffer PlugIn for intelligent attackers
    //fw.write("plugin = org.cougaar.tools.csmart.plugin.SnifferPlugIn(");
    // Need to figure out where is is coming from
    //fw.write("Experiment-Generator, ");  
    //fw.write(sniffInterval + ", "); 
    //fw.write(stopMillis + ")\n");

    fw.write("plugin = org.cougaar.tools.csmart.plugin.ABCImpactPlugIn \n");

    fw.write(
        "plugin = "+
        "org.cougaar.domain.planning.plugin.AssetDataPlugIn\n");
    fw.write(
        "plugin = "+
        "org.cougaar.domain.planning.plugin.AssetReportPlugIn\n");
    fw.write(
        "plugin = org.cougaar.lib.planserver.PlanServerPlugIn\n");

    fw.close();
  }

  protected void createClusterDATFile(
      String agentName,
      float latitude,
      float longitude,
      Object roles,
      List supportedAgentNames) throws Exception {

    // create the file
    String fname = 
      toPath + agentName + "-prototype-ini.dat";
    FileWriter fw = new FileWriter(new File(fname));

    // write identity information
    fw.write(
        "# basic identification\n"+
        "[Prototype] Organization\n"+
        "[UIC] \"UIC/");
    fw.write(agentName);
    fw.write(
        "\"\n"+
        "[TypeIdentificationPG] TypeIdentification String \"");
    fw.write(agentName);
    fw.write(
        "\"\n"+
        "[ClusterPG] ClusterIdentifier ClusterIdentifier \"");
    fw.write(agentName);
    fw.write("\"\n\n");

    String strRoles = MergeUtils.toString(roles);

    // write all the roles offered by this agent
    fw.write(
        "# self-capabilities\n"+
        "[OrganizationPG]\n"+
        "Roles Collection<Role> \"");
    fw.write(strRoles);
    fw.write("\"\n\n");

    // write the relationships with other agents
    fw.write(
        "# relationship with other Orgs\n"+
        "[Relationship]\n");
    int nSupportedAgentNames = 
      ((supportedAgentNames != null) ? 
       supportedAgentNames.size() : 
       0);
    for (int i = 0; i < nSupportedAgentNames; i++) {
      String ani = (String)supportedAgentNames.get(i);
      fw.write("Supporting \"");
      fw.write(ani);
      fw.write("\" \"");
      fw.write(strRoles);
      fw.write("\"\n");
    }

    fw.close();
  }

  /**
   * Creates a customer tasks file used by the Customer Plugin.
   * The customer tasks file contains an entry for each task that
   * the customer will request to have done.  It also contains task
   * related values such as rate of request, duration, etc.
   * <br>
   * @param customerTasks All Tasks that this customer will issue.
   * @param customerDemand How demanding this customer is on the society, avg num. of Tasks requested
   */
  protected void createCustomerTasksFile(
      String agentName, 
      List customerTasks, 
      long customerDemand) throws Exception {

    if (customerDemand <= 0) {
      // zero tasks to generate
      return;
    }

    String fname = toPath + agentName + "Tasks.dat";
    FileWriter fw = new FileWriter(new File(fname));

    fw.write(
        "# <WorldState>, "+
        "<TaskType>, "+
        "<Rate as num tasks over simulation>, "+
        "<Deviation>, "+
        "<Vitality>, "+
        "<Duration>\n");

    for (int i = 0; i < customerTasks.size(); i++) {

      CustomerTask ct = (CustomerTask)customerTasks.get(i);

      fw.write(ct.getWorldState() + ", ");
      fw.write(ct.getTaskName() + ", ");
      fw.write(customerDemand + ", ");
      fw.write(ct.getChaos() + ", ");
      fw.write(ct.getVital() + ", ");
      fw.write(ct.getDuration() + "\n");
    }

    fw.close();
  }

  /**
   * The local asset file is a list of all assets that are local
   * to the specified agent.  This file contains the name of the
   * asset, what roles it can perform and inventory and time information.
   * <br>
   * @param agentName Agent name 
   * @param localProduction Production rate for this agent
   * @param localAssets all assets local to this agent
   */
  protected void createLocalAssetsFile(
      String agentName, 
      long localProduction,
      List localAssets) throws Exception { 

    if ((localAssets == null) ||
        (localAssets.size() <= 0)) {
      // zero localAssets
      return;
    }

    String fname =
      toPath + agentName + "_LocalAssets.dat";
    FileWriter fw = 
      new FileWriter(
          new File(fname));

    fw.write(
        "# <name>, "+
        "<dec_pct - percent of total inventory used in each rquest>, "+
        "<avgTime - gtr than 0>, "+
        "<invDev - at least 0>, "+
        "<timeDev -- at least 0>, "+
        "<roles>\n");


    for (int i = 0; i < localAssets.size(); i++) {
      LocalAsset la = (LocalAsset)localAssets.get(i);

      fw.write(la.getAssetName() + ", ");

      // Higher localProduction number (from intermediate file) means has more
      // capacity, means each request reduces the amount available by _less_
      // Higher depletion factor means each request depletes the inventory 
      // by _more_

      // This is the decrementRate for the local Asset
      // This number should be a percentage - between 0 and 100.
      // If it is more than 100, then requests will always fail
      long decRate;
      if (localProduction <= 0) {
        decRate = 100;
      } else if (la.getDepleteFactor() <= 0) {
        decRate = 0;
      } else {
        decRate = (long)(la.getDepleteFactor() / localProduction);
      }
       
      fw.write(decRate + ", ");
      fw.write(la.getAvgCompleteTime() + ", ");
      fw.write(la.getInventoryChaos() + ", ");
      fw.write(la.getTimeChaos() + ", ");
      fw.write(MergeUtils.toString(la.getRoles()));

      fw.write("\n");
    }

    fw.close();
  }

  /**
   * @param agentName Agent Name
   * @param allocTable a Map of all allocations
   */
  protected void createAllocationTableFile(
      String agentName, 
      Map allocTable) throws Exception {

    String fname = toPath + agentName + "_Alloc.dat";
    FileWriter fw = new FileWriter(new File(fname));

    // write a concise "BNF" usage
    fw.write(
        "# [config, <fSuccess>, <tResp> (, <tAlloc>, <tTrans>, <tTry>)]\n");
    fw.write(
        "# [[rule, ] <task>, <role> (, <role>)*]\n");
    fw.write(
        "# <more \"rule\" lines as necessary>\n");

    // hardcode the config
    fw.write("config, 0.5, 50, 60, 150, 540000\n");

    // write the rules
    Iterator iter = allocTable.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry me = (Map.Entry)iter.next();
      String taskName = (String)me.getKey();
      Object roles = me.getValue();

      fw.write("rule, "+taskName+", ");
      fw.write(MergeUtils.toString(roles));
      fw.write("\n");
    }

    fw.close();
  }

} // GenerateSociety

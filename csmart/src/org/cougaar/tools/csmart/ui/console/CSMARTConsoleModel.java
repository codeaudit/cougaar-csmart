/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 *
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.csmart.experiment.DBExperiment;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.util.*;

/**
 * org.cougaar.tools.csmart.ui.console
 *
 */
public class CSMARTConsoleModel extends Observable implements Observer {

  /** Used by listeners to determine which event was fired. **/
  public static final String ADD_GLS_WINDOW = "Add GLS";
  public static final String APP_SERVERS_REFRESH = "App Servers Refresh";
  public static final String APP_SERVER_ADDED = "App Server Added";
  public static final String APP_SERVER_DELETED = "App Server Deleted";
  public static final String NODE_ADDED = "Node Added";
  public static final String NODE_REMOVED = "Node Removed";
  public static final String STOP_EXPERIMENT_TIMER = "Stop Timer";
  public static final String START_EXPERIMENT_TIMER = "Start Timer";

  public static final String COMMAND_ARGUMENTS = "Command$Arguments";

  public static final int DEFAULT_VIEW_SIZE = 300000; // 60 pages of text or 300K

  private Experiment experiment = null;
  private transient Logger log;
  private AppServerSupport appServerSupport;
  private static Date runStart = null;
  // set this flag when you first run an experiment
  // it's purpose is to ignore a non-null experiment if you're only attaching to nodes
  private boolean usingExperiment = false;
  private CSMART csmart;

  // Time in milliseconds between looking for new Nodes to attach to.
  private static int asPollInterval = 30000;
  private transient volatile java.util.Timer asPollTimer = null;
  private TimerTask monitorAppServerTask;
  private Hashtable nodeModels;    // Hashtable of all node name to node models mappings.
  private Hashtable nodeViews;
  private Object appServerLock = new Object(); // used to lock nodeToAppServer and appServers
  private Hashtable nodeToAppServer; // maps node name to AppServerDesc
  private AppServerList appServers;// known app servers; array of AppServerDesc
  public int viewSize = DEFAULT_VIEW_SIZE; // default view size for nodes
  private String selectedNodeName = null; // name of node whose status button is selected
  private String notifyCondition = "exception";
  private boolean notifyOnStdErr;

  // Default contact info for the GLSInit UI
  private String[] glsContactInfo = { "http", "localhost", "8800", "NCA" };

  public CSMARTConsoleModel(Experiment experiment, CSMART csmart) {
    this.csmart = csmart;
    this.runStart = new Date();
    this.experiment = experiment;
    this.appServerSupport = new AppServerSupport(this);
    this.nodeModels = new Hashtable(5);
    this.nodeViews = new Hashtable(5);
    this.nodeToAppServer = new Hashtable(5);
    this.appServers = new AppServerList();

    if (experiment != null)
      getAppServersFromExperiment();
    createLogger();
    resetASPoller(asPollInterval); // start polling app servers
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Add any app servers on the hosts and ports that the experiment
   * will use to the list of app servers we're monitoring.
   */
  private void getAppServersFromExperiment() {
    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      String hostName = hosts[i].getShortName();
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
        NodeComponent nodeComponent = nodes[j];
        Properties properties = getNodeMinusD(nodeComponent, hostName);
        int port = Experiment.APP_SERVER_DEFAULT_PORT;
        if (properties != null) {
          try {
            String tmp = properties.getProperty(Experiment.CONTROL_PORT);
            if (tmp != null)
              port = Integer.parseInt(tmp);
          } catch (NumberFormatException nfe) {
            // use default port
          }
          if (port < 1)
            port = Experiment.APP_SERVER_DEFAULT_PORT;
        }
        appServerSupport.add(hostName, port);
      }
    }
  }

  // Find the Node on which the GLSInitServlet is running, if any
  private String findGLSNode() {
    // Will have no experiment if just attached to Nodes
    // Return empty String to avoid null pointer
    if (experiment == null)
      return "";

    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
        AgentComponent[] agents = nodes[j].getAgents();
        for (int k = 0; k < agents.length; k++) {
          AgentComponent agent = agents[k];
	  // FIXME: This assumes that if this agent exists, the servlet exists.
	  // Mostly this is a reasonable shortcut, but not always.
	  // Alternative is to do experiment.getSocietyComponentData
	  // and then walk the ComponentData tree down to AgentsComponentData,
	  // and then do getPluginNames, and look for one that ends with GLSInitServlet
          if (agent.getShortName().equalsIgnoreCase("NCA") || agent.getShortName().equalsIgnoreCase("OSD.GOV")) {
	    glsContactInfo[3] = agent.getShortName();
	    glsContactInfo[1] = hosts[i].getShortName();

	    // Pull out the gls servlet protocol and port here too
	    getGLSPort(nodes[j].getArguments());

            return nodes[j].getShortName();
          }
        }
      }
    }
    return "";
  }

  public boolean haveAttached() {
    Collection models = nodeModels.values();
    Iterator i = models.iterator();
    while (i.hasNext()) {
      NodeModel nodeModel = (NodeModel) i.next();
      String state = nodeModel.getState();
      if (!state.equals(NodeModel.STATE_INITTED) &&
          !state.equals(NodeModel.STATE_STOPPED))
        return true;
    }
    return false;
  }

  public void detachFromSociety() {
    // This flushes the output and disconnects it
    appServerSupport.killListeners();

    setChanged();
    notifyObservers(STOP_EXPERIMENT_TIMER);

    updateExperimentStatus(false);
  }

  /**
   * Start or stop the nodes.
   * Called when the user selects the Run or Stop buttons.
   *
   * @param doRun -- The new running state.
   */
  public void setRunning(boolean doRun) {
    if (doRun) {
      destroyOldNodes(); // remove any GUIs from previous runs
      if (experiment == null)
        return;          // can't run if no experiment
      createNodeModels();// create node models if they don't exist
      startNodes();      // start nodes
    } else {             // stopping
      stopNodes();
    }
  }

  /**
   * Start nodes that are not in the Running state.
   */
  private void startNodes() {
    Enumeration enum = this.nodeModels.keys();
    while (enum.hasMoreElements()) {
      Object key = enum.nextElement();
      NodeModel model = (NodeModel) nodeModels.get(key);
      if (model.getState().equals(NodeModel.STATE_RUNNING))
        continue;
      model.start();
    }
    setChanged();
    notifyObservers(START_EXPERIMENT_TIMER); // start Experiment timer
  }

  /**
   * Tell the node model to stop the node, but the GUIs are not updated
   * until the ConsoleNodeListener gets a node destroyed message.
   * Only act on nodes that are in the "Running" state.
   */
  public void stopNodes() {
    // FIXME: Really should call glsClient.stop() if we have a glsClient
    Enumeration enum = this.nodeModels.elements();
    while (enum.hasMoreElements()) {
      NodeModel nodeModel = (NodeModel) enum.nextElement();
      if (nodeModel.getState().equals(NodeModel.STATE_RUNNING)) {
        if (log.isDebugEnabled()) {
          log.debug("Stopping Node: " + nodeModel.getNodeName());
        }
        nodeModel.stop();
      }
    }
    setChanged();
    notifyObservers(STOP_EXPERIMENT_TIMER); // stop experiment timer
  }

  /**
   * This is called from the node start thread, when a node
   * is successfully started.
   * The node view and node status button are created if necessary;
   * and CSMARTConsoleView is notified, and adds them to the display.
   */
  public void createGUI(String nodeName) {
    SwingUtilities.invokeLater(new CreateNodeViewThread(nodeName,
                                                        (NodeModel) nodeModels.get(nodeName), nodeViews));
  }

  class CreateNodeViewThread implements Runnable {
    String nodeName;
    NodeModel nodeModel;
    Hashtable nodeViews;

    public CreateNodeViewThread(String nodeName, NodeModel nodeModel, Hashtable nodeViews) {
      this.nodeName = nodeName;
      this.nodeModel = nodeModel;
      this.nodeViews = nodeViews;
    }

    public void run() {
      if (nodeViews.get(nodeName) == null) {
        NodeView view = new NodeView(nodeModel);
        nodeViews.put(nodeName, view);
        setChanged();
        notifyObservers(view);
      }
      String glsNode = findGLSNode();
//       if(glsNode != null) {
//         if(runningNodes.containsKey(glsNode)) {
//           setChanged();
//           notifyObservers(ADD_GLS_WINDOW);
//         }
//       }

      // notify observers if added GLS window
      // TODO: is this right?
      NodeModel model = (NodeModel) nodeModels.get(glsNode);
      if (model != null) {
	// record info from the Node model on the protocol, host, port, and agent for the GLS servlet
	recordGLSContactInfo(model);
        setChanged();
        notifyObservers(ADD_GLS_WINDOW);
      }
    }
  }

  // create node models for nodes that do not already exist
  private void createNodeModels() {
    // Create all Node Models, either from the Experiment or from the XML file?
    usingExperiment = true;

    HostComponent[] hostsToRunOn = experiment.getHostComponents();
    for (int i = 0; i < hostsToRunOn.length; i++) {
      String hostName = hostsToRunOn[i].getShortName();
      NodeComponent[] nodesToRun = hostsToRunOn[i].getNodes();
      for (int j = 0; j < nodesToRun.length; j++) {
        NodeComponent nodeComponent = nodesToRun[j];
        String nodeName = nodeComponent.getShortName();
        if (nodeModels.get(nodeName) != null)
          continue; // don't create node model if one already exists

        // get arguments from NodeComponent and pass them to ApplicationServer
        // note that these properties augment any properties that
        // are passed to the server in a properties file on startup
        Properties properties = getNodeMinusD(nodeComponent, hostName);
        List args = getNodeArguments(nodeComponent);

        if (experiment.getTrialID() != null) {
          properties.setProperty(Experiment.EXPERIMENT_ID,
                                 experiment.getTrialID());
        } else {
          log.error("Null trial ID for experiment!");
        }
        // get the app server to use
        int port = getAppServerPort(properties);
        requestAppServerAdd(hostName, port);
        AppServerDesc appServerDesc = getAppServer(hostName, port);
        if (appServerDesc == null) {
          continue;
        }
        NodeInfo info = new NodeInfo(appServerDesc.appServer,
                                     nodeName, hostName,
                                     properties, args);
        NodeModel nodeModel = new NodeModel(info, this);
        nodeModel.addObserver(this);
        this.nodeModels.put(nodeName, nodeModel);
      }
    }
  }

  public static int getAppServerPort(Properties properties) {
    int port = Experiment.APP_SERVER_DEFAULT_PORT;
    if (properties == null)
      return port;
    try {
      String tmp = properties.getProperty(Experiment.CONTROL_PORT);
      if (tmp != null)
        port = Integer.parseInt(tmp);
    } catch (NumberFormatException nfe) {
      // use default port
    }
    if (port < 1)
      port = Experiment.APP_SERVER_DEFAULT_PORT;
    return port;
  }

  /**
   * Update the gui controls when experiments (or attached nodes)
   * are started or stopped.
   */
  private void updateExperimentStatus(boolean isRunning) {
    if (experiment != null && usingExperiment) {
      if (!isRunning) {
        experiment.experimentStopped();
        csmart.removeRunningExperiment(experiment);
      } else
        csmart.addRunningExperiment(experiment);
      experiment.getSocietyComponent().setRunning(isRunning);
    }
  }

  /**
   * Destroy any previous node GUIs and models before running again.
   * Destroys nodes whose state is STOPPED.
   */
  private void destroyOldNodes() {
    Enumeration enum = nodeModels.keys();
    while (enum.hasMoreElements()) {
      String nodeName = (String) enum.nextElement();
      NodeModel model = (NodeModel) nodeModels.get(nodeName);
      if (model.getState().equals(NodeModel.STATE_STOPPED)) {
        setChanged();
        notifyObservers(new NodeChange(nodeName, NODE_REMOVED));
        nodeModels.remove(nodeName);
        nodeViews.remove(nodeName);
      }
    }
  }

  public int getASPollInterval() {
    return asPollInterval;
  }

  /**
   * Cancel old AppServer poller if any.
   * If new interval is greater than 0,
   * start a new timer to poll every interval milliseconds.
   */
  public void resetASPoller(int newInterval) {
    if (asPollTimer != null) {
      if (log.isDebugEnabled()) {
        log.debug("Canceling old ASPoller timer");
      }
      asPollTimer.cancel();
      monitorAppServerTask.cancel();
    }

    if (newInterval != 0) {
      asPollInterval = newInterval;
      if (log.isDebugEnabled()) {
        log.debug("creating new ASPoller with interval " + asPollInterval);
      }

      // contact known app servers periodically to get lists of their nodes
      // and update controls when new nodes detected
      monitorAppServerTask = new TimerTask() {
        public void run() {
          appServerSupport.refreshAppServers();
        }
      };

      asPollTimer = new java.util.Timer();
      asPollTimer.schedule(monitorAppServerTask, new Date(), asPollInterval);
    }
  }

  /**
   * Get node -d arguments.
   * Substitute host name for $HOST value if it occurs.
   * @param nc for which to get the -d arguments
   * @return properties the -d arguments
   */
  public Properties getNodeMinusD(NodeComponent nc, String hostName) {
    Properties result = new Properties();
    Properties props = nc.getArguments();
    boolean foundclass = false;
    for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
      String pname = (String) e.nextElement();
      if (pname.equals(COMMAND_ARGUMENTS)) continue;
      if (pname.equals(Experiment.BOOTSTRAP_CLASS)) foundclass = true;
      String value = props.getProperty(pname);

      // Allow $HOST to stand for hostName
      int index = value.indexOf("$HOST");
      if (index != -1)
        value = value.substring(0, index) + hostName +
            value.substring(index + 5);

      // Could add other substitutions here - for the node name, for example

      result.put(pname, value);
    }
    // make sure that the classname is "Node"
    //
    // this can be removed once the CMT and all "node.props"
    // are sure to have this property.

    if (foundclass == false)
      result.put(
          Experiment.BOOTSTRAP_CLASS,
          Experiment.DEFAULT_BOOTSTRAP_CLASS);
    return result;
  }

  private List getNodeArguments(NodeComponent nc) {
    Properties props = nc.getArguments();
    String commandArguments =
        props.getProperty(COMMAND_ARGUMENTS);
    if (commandArguments == null || commandArguments.trim().equals("")) {
      // Warning: If you are running the bootstrapper and supply
      // nothing here, nothing will run!
      //  So if were using the default bootstrapper and have no arguments
      // Give it an argument with the default node class
      if (props.getProperty(Experiment.BOOTSTRAP_CLASS) == null || props.getProperty(Experiment.BOOTSTRAP_CLASS).equals(Experiment.DEFAULT_BOOTSTRAP_CLASS))
        return Collections.singletonList(Experiment.DEFAULT_NODE_CLASS);
      return Collections.EMPTY_LIST;
    }
    StringTokenizer tokens =
        new StringTokenizer(commandArguments.trim(), "\n");
    String[] result = new String[tokens.countTokens()];
    for (int i = 0; i < result.length; i++) {
      result[i] = tokens.nextToken();
    }

    List l = Arrays.asList(result);
    return l;
  }

  public NodeModel getNodeModel(String name) {
    return (NodeModel) this.nodeModels.get(name);
  }

  public ArrayList getNodeModels() {
    return new ArrayList(nodeModels.values());
  }

  // New Application Server Support
  // We maintain a list of known app servers
  // and a hashtable of node-to-appServer mappings.
  // The separate list of app Servers is needed
  // because we may have app Servers that do not have nodes.

  /**
   * User selected "add app server" menu item or
   * we're creating a node that specified this app server.
   * Notify AppServerSupport.
   */
  public void requestAppServerAdd(String hostName, int port) {
    setChanged();
    notifyObservers(new AppServerRequest(hostName, port,
                                         AppServerRequest.ADD));
  }

  /**
   * Called by AppServerSupport to add an app server.
   */
  public void addAppServer(AppServerDesc desc) {
    synchronized (appServerLock) {
      appServers.add(desc);
      setChanged();
      notifyObservers(APP_SERVER_ADDED);
    }
  }

  /**
   * Add node to node-to-appServer mapping.
   */
  public void addNodeToAppServerMapping(String name,
                                        AppServerDesc appServerDesc) {
    // first ensure that app server is in list of app servers
    addAppServer(appServerDesc);
    boolean nodeAdded = false;
    synchronized (appServerLock) {
      if (nodeToAppServer.get(name) == null)
        nodeAdded = true;
      nodeToAppServer.put(name, appServerDesc);
    }
    if (nodeAdded) {
      setChanged();
      notifyObservers(NODE_ADDED);
    }
  }

  /**
   * Return array list of AppServerDesc of known app servers.
   */
  public ArrayList getAppServers() {
    synchronized (appServerLock) {
      return appServers;
    }
  }

  /**
   * Get app server description for a node.
   */
  public AppServerDesc getAppServer(String name) {
    AppServerDesc server = null;
    synchronized (appServerLock) {
      server = (AppServerDesc) nodeToAppServer.get(name);
    }
    return server;
  }

  /**
   * Get app server on this host and port.
   */
  public AppServerDesc getAppServer(String hostName, int port) {
    AppServerDesc foundDesc = null;
    synchronized (appServerLock) {
      for (int i = 0; i < appServers.size(); i++) {
        AppServerDesc desc = (AppServerDesc) appServers.get(i);
        if (desc.hostName.equals(hostName) &&
            desc.port == port) {
          foundDesc = desc;
          break;
        }
      }
    }
    return foundDesc;
  }

  /**
   * Delete app server; just remove entries for this app server.
   */
  public void appServerDelete(AppServerDesc appServerDesc) {
    synchronized (appServerLock) {
      Enumeration nodes = nodeToAppServer.keys();
      while (nodes.hasMoreElements()) {
        String nodeName = (String) nodes.nextElement();
        AppServerDesc desc = getAppServer(nodeName);
        if (desc.hostName.equals(appServerDesc.hostName) &&
            desc.port == appServerDesc.port) {
          nodeToAppServer.remove(nodeName);
          // if app server had any running nodes, then treat them as stopped
          NodeModel nodeModel = (NodeModel) nodeModels.get(nodeName);
          if (nodeModel != null &&
              nodeModel.getState().equals(NodeModel.STATE_RUNNING)) {
            nodeModel.stop();
          }
        }
      }
      appServers.remove(appServerDesc);
    }
    setChanged();
    notifyObservers(APP_SERVER_DELETED);
  }

  /**
   * Refresh app servers; notify AppServerSupport.
   */
  public void refreshAppServers() {
    setChanged();
    notifyObservers(APP_SERVERS_REFRESH);
  }

  /**
   * Kill all processes known by all known AppServers,
   * whether or not we're attached to them.
   */
  public void killAllProcesses() {
    appServerSupport.killAllProcesses();
  }
  /**
   * Attach to a running node.
   * Create a node model and node view.
   */
  public void attachToNode(String name) {
    // get the app server for the node
    RemoteHost appServer = getAppServer(name).appServer;
    // get information about the node from the app server
    ProcessDescription pd = null;
    try {
      pd = appServer.getProcessDescription(name);
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Exception attaching to: " + name, e);
      }
      // Remove this node from nodeToAppServer since we can't access it
      nodeToAppServer.remove(name);
    }
    if (pd == null)
      return;
    Properties properties = (Properties) pd.getJavaProperties();
    String hostName = (String) properties.get(Experiment.NAME_SERVER);
    // Must remove the port numbers...
    int pStart = hostName.indexOf(':');
    if (pStart != -1) {
      hostName = hostName.substring(0, pStart).trim();
    }
    java.util.List args = pd.getCommandLineArguments();
    NodeInfo ni =
        new NodeInfo(appServer, name, hostName, properties, args);
    NodeModel nodeModel = new NodeModel(ni, this);
    nodeModel.addObserver(this);
    nodeModels.put(name, nodeModel);
    // attach to running node
    // node view is created and added when attach succeeds
    nodeModel.attach();
  }

  /**
   * This is called from the node start thread, when attaching to a node fails.
   * It uses a Swing thread to remove the node model, so that we don't
   * have to synchronize the nodeModels hashtable.
   * @param nodeName name of node to remove
   */
  public void removeNodeModel(String nodeName) {
    SwingUtilities.invokeLater(new NodeModelRemover(nodeName, nodeModels));
  }

  class NodeModelRemover implements Runnable {
    String nodeName;
    Hashtable nodeModels;

    public NodeModelRemover(String nodeName, Hashtable nodeModels) {
      this.nodeName = nodeName;
      this.nodeModels = nodeModels;
    }

    public void run() {
      nodeModels.remove(nodeName);
    }
  }

  /**
   * Get list of nodes to which CSMART is not attached.
   * Returns an array of Strings (node names).
   */
  public ArrayList getUnattachedNodes() {
    return appServerSupport.getUnattachedNodes();
  }

  /**
   * Get list of nodes to which CSMART is attached.
   * Returns an array of Strings (node names).
   */
  public ArrayList getAttachedNodes() {
    ArrayList nodes = null;
    synchronized (appServerLock) {
      nodes = new ArrayList(nodeToAppServer.keySet());
    }
    return nodes;
  }

  /**
   * Add an unique app server to the list of known app servers.
   * This assumes that there is one app server per host & port.
   */
  class AppServerList extends ArrayList {
    public void add(AppServerDesc descToAdd) {
      if (descToAdd == null) return;
      for (int i = 0; i < size(); i++) {
        AppServerDesc desc = (AppServerDesc) get(i);
        if (descToAdd.hostName.equals(desc.hostName) &&
            descToAdd.port == desc.port) {
          return;
        }
      }
      super.add(descToAdd);
    }
  }

  public Experiment getExperiment() {
    return experiment;
  }

  /**
   * Set the view size for all the nodes.
   */
  public void setViewSize(int size) {
    Enumeration enum = this.nodeViews.elements();
    while (enum.hasMoreElements()) {
      NodeView nodeView = (NodeView) enum.nextElement();
      nodeView.setViewSize(size);
    }
    viewSize = size;
  }

  /**
   * Return the view size used for all nodes.
   * This value may be overwritten for individual nodes.
   */
  public int getViewSize() {
    return viewSize;
  }

  /**
   * Set the filter for all the nodes.
   */
  public void setFilter(ConsoleNodeOutputFilter filter) {
    Enumeration enum = this.nodeModels.elements();
    while (enum.hasMoreElements()) {
      NodeModel nodeModel = (NodeModel) enum.nextElement();
      nodeModel.setFilter(filter);
    }
  }

  /**
   * Set the name of the node whose status button is selected.
   */
  public void setSelectedNodeName(String name) {
    selectedNodeName = name;
  }

  /**
   * Get the name of the node whose status button is selected.
   */
  public String getSelectedNodeName() {
    return selectedNodeName;
  }

  public void addGLSWindow() {
    // Try to get reasonable contact info for the GLS servlet
    NodeModel glsNode = getNodeModel(findGLSNode());
    if (glsNode == null) {
      ArrayList models = getNodeModels();
      if (! models.isEmpty())
	glsNode = (NodeModel) models.get(0);
    }
    recordGLSContactInfo(glsNode);

    setChanged();
    notifyObservers(ADD_GLS_WINDOW);
  }

  public static Date getRunStart() {
    return runStart;
  }

  public void setNotification(String notifyCondition, boolean notifyOnStdErr) {
    Enumeration enum = this.nodeModels.elements();
    while (enum.hasMoreElements()) {
      NodeModel nodeModel = (NodeModel) enum.nextElement();
      nodeModel.setNotification(notifyCondition, notifyOnStdErr);
    }
    this.notifyCondition = notifyCondition;
    this.notifyOnStdErr = notifyOnStdErr;
  }

  public String getNotifyCondition() {
    return notifyCondition;
  }

  public boolean getNotifyOnStandardError() {
    return notifyOnStdErr;
  }

  public boolean isRunnable() {
    if (experiment != null) {
      HostComponent[] hosts = experiment.getHostComponents();
      for (int i = 0; i < hosts.length; i++) {
        NodeComponent[] nodes = hosts[i].getNodes();
        if (nodes != null && nodes.length > 0) {
          // Bug 1763: Perhaps allow running a society with just Nodes, no Agents?
          for (int j = 0; j < nodes.length; j++) {
            AgentComponent[] agents = nodes[j].getAgents();
            if (agents != null && agents.length > 0) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public void resetNotifyStatus() {
    Enumeration enum = this.nodeViews.elements();
    while (enum.hasMoreElements()) {
      NodeView nodeView = (NodeView) enum.nextElement();
      nodeView.resetNotify();
    }
  }

  /**
   * This is an observer on all the node models.
   * It notifies its observers when node states change.
   * It updates the experiment status --
   * if any nodes is running, then the experiment is running
   * if all nodes are stopped, then the experiment is stopped
   */
  public void update(Observable o, Object arg) {
    if (((String) arg).startsWith("NODE_STATE")) {
      // notify our observers, which includes CSMARTConsoleView
      setChanged();
      notifyObservers(arg);
      Collection models = nodeModels.values();
      Iterator i = models.iterator();
      while (i.hasNext()) {
        NodeModel nodeModel = (NodeModel) i.next();
        String state = nodeModel.getState();
        if (state.equals(NodeModel.STATE_RUNNING)) {
          updateExperimentStatus(true);
          refreshAppServers(); // ensure that app server reports new node
          return;
        } else if (!state.equals(NodeModel.STATE_STOPPED))
          return;
      }
      // all nodes are stopped
      updateExperimentStatus(false);
    }
  }

  // Given the node that has the GLS servlet, record the contact info for it
  private void recordGLSContactInfo (NodeModel glsNode) {
    if (glsNode == null)
      return;

    // find the correct protocol, host, port, and agent name for the GLS servlet
    // and set in the glsContactInfo array

    String host = glsNode.getInfo().getHostName();
    glsContactInfo[1] = host;

    // Pull out the gls servlet protocol and port here too
    getGLSPort(glsNode.getInfo().getProperties());
  }

  // Called from the ConsoleView to get the correct contact info for the GLS servlet
  public String[] getGLSContactInfo() {
    return glsContactInfo;
  }

  // Helper function that takes Nodes arguments
  // and tries to find non-default protocol and port for servlets
  private void getGLSPort(Properties arguments) {
    if (arguments != null) {
      String s = arguments.getProperty("org.cougaar.lib.web.https.port");
      if (s != null) {
	glsContactInfo[2] = s;
	glsContactInfo[0] = "https";
      } else {
	s = arguments.getProperty("org.cougaar.lib.web.http.port");
	if (s != null)
	  glsContactInfo[2] = s;
      }
    }
  }
}

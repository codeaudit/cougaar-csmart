package org.cougaar.tools.csmart.ui.console;

import org.cougaar.mlm.ui.glsinit.GLSClient;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.tools.server.RemoteListenable;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.*;

/**
 * org.cougaar.tools.csmart.ui.console
 *
 */
public class CSMARTConsoleModel extends Observable {

  /** Used by listeners to determine which event was fired. **/
  public static final String TOGGLE_RUNNING_STATE = "Running State";
  public static final String TOGGLE_ATTACHED_STATE = "Attach State";
  public static final String APP_SERVERS_CHANGED = "App Server State";
  public static final String NEW_NODE_VIEW = "New Node View";
  public static final String SELECT_GLS_INIT = "Select GLS" ;
  public static final String APP_SERVERS_REFRESH = "App Servers Refresh";
  public static final String APP_SERVER_ADDED = "App Server Added";
  public static final String APP_SERVER_DELETED = "App Server Deleted";
  public static final String NODE_ADDED = "Node Added";

  public static final int APP_SERVER_DEFAULT_PORT = 8484;

  private static final String DEFAULT_BOOTSTRAP_CLASS = "org.cougaar.bootstrap.BootStrapper";
  private static final String DEFAULT_NODE_CLASS = "org.cougaar.core.node.Node";
  public static final String COMMAND_ARGUMENTS = "Command$Arguments";


  public static final int DEFAULT_VIEW_SIZE = 300000; // 60 pages of text or 300K

  private Experiment experiment = null;
  private transient Logger log;
  private AppServerSupport appServerSupport;
  private Hashtable runningNodes; // maps node names to RemoteProcesses
  private Object runningNodesLock = new Object();
  private static Date runStart = null;
  private static String resultDirectory = null;
  // set this flag when you first run an experiment
  // it's purpose is to ignore a non-null experiment if you're only attaching to nodes
  private boolean usingExperiment = false;
  private GLSClient glsClient = null;
  private volatile boolean stopNodeCreation;
  private Thread nodeCreator;
  private boolean stopping = false; // user is stopping the experiment
  private javax.swing.Timer experimentTimer;
  private CSMART csmart;

  private boolean isRunning = false;
  private boolean isAttached = false;

  private ConsoleDesktop desktop;

  // Time in milliseconds between looking for new Nodes to attach to.
  // Note that the currently selected value stays constant
  // For a given invocation of CSMART
  private static int asPollInterval = 30000;
  private transient volatile java.util.Timer asPollTimer = null;
  private TimerTask monitorAppServerTask;
  private long startExperimentTime;

  private String globalNotifyCondition = "exception";

  private Hashtable nodeModels;    // Hashtable of all node name to node models mappings.
  private Hashtable nodeViews;
  private Object appServerLock = new Object(); // used to lock nodeToAppServer and appServers
  private Hashtable nodeToAppServer; // maps node name to AppServerDesc
  private AppServerList appServers;// known app servers; array of AppServerDesc

  public CSMARTConsoleModel() {
    this(null, null);
  }

  public CSMARTConsoleModel(Experiment experiment) {
    this(experiment, null);
  }

  public CSMARTConsoleModel(CSMART csmart) {
    this(null, csmart);
  }

  public CSMARTConsoleModel(Experiment experiment, CSMART csmart) {
    this.csmart = csmart;
    this.runStart = new Date();
    this.experiment = experiment;
    this.appServerSupport = new AppServerSupport(this);
    this.runningNodes = new Hashtable(5);
    this.nodeModels = new Hashtable(5);
    this.nodeToAppServer = new Hashtable(5);
    this.appServers = new AppServerList();
    //this.resultDirectory = makeResultDirectory();
    createLogger();
    resetASPoller(asPollInterval); // start polling app servers
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }


  public boolean haveAttached() {
    synchronized (runningNodesLock) {
      if (runningNodes == null || runningNodes.isEmpty())
        return false;
    } // end synchronized
    return true;
  }

  public void detachFromSociety() {
    // this is what this method does now
    // kill any node output listeners that this instance of CSMART started

    // if there's a thread still creating nodes, stop it
    stopNodeCreation = true;
    // FIXME: This means the AWT thread will block on the Node creation
    // thread, which in turn has to wait on RMI stuff.
    // This is probably bad. However, the nodeCreator does check stopNodeCreation
    // when it can to try to bail out, so unless we're doing the RMI
    // thing now, this _shouldn't_ be too bad.
    // wait for the creating thread to stop
    if (nodeCreator != null) {
      try {
        nodeCreator.join();
      } catch (InterruptedException ie) {
        if (log.isErrorEnabled()) {
          log.error("Exception waiting for node creation thread to die: ", ie);
        }
      }
      nodeCreator = null;
    }

    stopping = true;

    // stop the GLS Client
    if (glsClient != null) {
      glsClient.stop();
      glsClient = null;
    }

    // This flushes the output and disconnects it
    appServerSupport.killListeners();

    // Maybe grab result files?
    // This will contact all the AppServers again
//    saveResults();

    if (experimentTimer != null)
      experimentTimer.stop();

    updateControls(false);
  }


  public void killAllNodes() {
    // This next ends up calling stopAllNodes
    // which causes nodeStopped to be called for each Node,
    // but not until we've waited on the RMI / network
    // so unless I take out that wait, we're blocking here (in AWT thread)
    // on the network. But if I don't block, this method
    // will later destroy some state that the nodeStopped method
    // probably wants, and this may cause errors
//    doStop();
    // don't stop experiments when exiting the console
    //    stopExperiments();
  }

  private void doStop() {
    // FIXME Now work with running state.
    //setStopButtonSelected(true);
//    stopAllNodes(); // all nodes must be stopped manually for now
  }

  /**
   * Checks to see if the experiment is currently running.
   *
   * @return True if the experiment is running, else false
   */
  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Sets the experiment running state.
   *
   * @param running -- The new running state.
   */
  public void setRunning(boolean running) {
    // FIXME:  This should get moved out of run.  Nodes should be created long before run is ever pushed.
    createAllNodes();
    createAllNodeViews();
    startNodes();
    isRunning = running;
    setChanged();
    notifyObservers(TOGGLE_RUNNING_STATE);
  }

  private void startNodes() {
    Enumeration enum = this.nodeModels.keys();
    while (enum.hasMoreElements()) {
      Object key = enum.nextElement();
      NodeModel model = (NodeModel) nodeModels.get(key);
      model.start();
    }
  }

  private void createAllNodeViews() {
    Enumeration enum = this.nodeModels.keys();
    while (enum.hasMoreElements()) {
      Object key = enum.nextElement();
      NodeView view = new NodeView((NodeModel)nodeModels.get(key));
      this.nodeViews.put(key, view);
      notifyObservers(view);
    }
  }

  private void createAllNodes() {
    // Create all Node Models, either from the Experiment or from the XML file?

    if (experiment != null) {
      usingExperiment = true;

      HostComponent[] hostsToRunOn = experiment.getHostComponents();
      for (int i = 0; i < hostsToRunOn.length; i++) {
        String hostName = hostsToRunOn[i].getShortName();
        NodeComponent[] nodesToRun = hostsToRunOn[i].getNodes();
        for (int j = 0; j < nodesToRun.length; j++) {
          NodeComponent nodeComponent = nodesToRun[j];
          String nodeName = nodeComponent.getShortName();

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
          this.nodeModels.put(nodeName, new NodeModel(info, this));
        }
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
   * Checks to see if the Console is currently attached to
   * any application servers.
   *
   * @return True if currently attached, else false.
   */
  public boolean isAttached() {
    return isAttached;
  }

  /**
   * Sets the console attached state
   *
   * @param attached -- The new attached state.
   */
  public void setAttached(boolean attached) {
//     try {
//       attachToNode();
//     } catch (IllegalStateException e) {
//       if (log.isInfoEnabled()) {
//         log.info("Attach Error: " + e.getMessage());
//       }
//       attached = false;
//     }
//     isAttached = attached;
//     setChanged();
//     notifyObservers(TOGGLE_ATTACHED_STATE);
  }

  //  public void attachToNode() throws IllegalStateException {

    // Before getting this list, make sure our list of possible
    // things to attach to is up-to-date
//     appServerSupport.refreshAppServers();
//     ArrayList nodesToAttach = appServerSupport.getNodesToAttach();
//     if (nodesToAttach == null) {
//       throw new IllegalStateException("No new Nodes to Attach to.");
//     } else if (nodesToAttach.isEmpty()) {
//       // User just selected 'None'.
//       return;
//     }

//     // Loop over nodes to attach to
//     for (int i = 0; i < nodesToAttach.size(); i++) {
//       NodeInfo nodeInfo = (NodeInfo) nodesToAttach.get(i);
//       boolean haveAlready = false;
//       String name = nodeInfo.getNodeName();
//       synchronized (runningNodesLock) {
//         if (runningNodes.get(nodeInfo.getNodeName()) != null) {
//           haveAlready = true;
//         }
//       }

//       // Are we already atached to a node of that name?
//       if (haveAlready) {
//         if (log.isDebugEnabled()) {
//           log.debug("Already have attached node of name " + nodeInfo.getNodeName());
//         }
//         // So how do I get the old process name?
//         // FIXME: Use the new NodeInfo.equals() method.
//         NodeModel nm = (NodeModel) nodeModels.get(nodeInfo.getNodeName());
//         if (nm.getInfo().getProcessName().equals(nodeInfo.getProcessName())) {
//           if (log.isDebugEnabled()) {
//             log.debug("They have the same process name: " + nodeInfo.getProcessName());
//           }
//           // FIXME: Maybe compare nodeInfo.properties too?
//           if (log.isDebugEnabled()) {
//             log.debug("Asked to attach to already attached node " + nodeInfo.getNodeName());
//           }
//           continue;
//         } else {
//           // Going to allow attaching
//           // But need to pretend it has a different name or else
//           // I'll have trouble stopping it and whatnot

//           // Must effectively reset nodeInfo.nodeName
//           name = name + "-attached";
//         }
//       } // end of block to see if this Node already attached

//       runStart = new Date();
//      NodeCreationInfo nci = prepareToCreateNode(nodeInfo.appServer,
//                                                 name,
//                                                 nodeInfo.hostName,
//                                                 nodeInfo.properties,
//                                                 nodeInfo.args);
//      nodeToNodeInfo.put(name, nodeInfo);
//      RemoteProcess remoteNode = null;
//      // add listener to node
//      try {
//	// FIXME: Next few lines do RMI, could block. Avoid doing it in AWT thread?
//        remoteNode =
//          nodeInfo.appServer.getRemoteProcess(nodeInfo.processName);
//	if (remoteNode != null) {
//	  RemoteListenable rl =
//	    remoteNode.getRemoteListenable();
//	  if (log.isDebugEnabled())
//	    log.debug("Adding listener: " +
//		      CSMART.getNodeListenerId() +
//		      " for: " +
//		      nodeInfo.processName);
//	  rl.addListener(nci.listener,
//			 CSMART.getNodeListenerId());
//	} else {
//	  if (log.isWarnEnabled())
//	    log.warn("Got null process from AppServer for node to attach to: " + name);
//	  throw new Exception("Null RemoteProcess for " + nodeInfo.processName);
//	}
//      } catch (Exception e) {
//        if (log.isErrorEnabled()) {
//          log.error("Exception attaching to: " + nodeInfo.processName, e);
//        }
//	// remove status button
//	// kill doc, textPane, listener
//	// remove from nodeListeners and nodePanes
//	((ConsoleNodeListener)nci.listener).cleanUp();
//	nodeListeners.remove(name);
//	ConsoleTextPane textPane = (ConsoleTextPane)nodePanes.remove(name);
//	if (textPane != null)
//	  textPane.cleanUp();
//	//	removeStatusButton(nci.statusButton);
//
//        continue;
//      }
//
//      synchronized (runningNodesLock) {
//        runningNodes.put(name, remoteNode);
//      }
//
//      addStatusButton(nci.statusButton);
//      ConsoleInternalFrame frame =
//        new ConsoleInternalFrame(nci.nodeName,
//                                 nci.hostName,
//                                 nci.properties,
//                                 nci.args,
//                                 (ConsoleNodeListener)nci.listener,
//                                 nci.scrollPane,
//                                 nci.statusButton,
//                                 nci.logFileName,
//                                 remoteNode,
//                                 console);
//      frame.addInternalFrameListener(new CSMARTConsole.NodeFrameListener());
//      desktop.addNodeFrame(frame, nci.nodeName);
//    }
//    updateControls(true);
//
//    // If have an experiment to run
//    // but have not run it yet, make that possible still
//    if (! usingExperiment && experiment != null) {
//      runButton.setEnabled(true);
//      runButton.setSelected(false);
//    }
//  }


  /**
   * Update the gui controls when experiments (or attached nodes)
   * are started or stopped.
   */
  private void updateControls(boolean isRunning) {
    if (experiment != null && usingExperiment) {
      if (!isRunning) {
        experiment.experimentStopped();
        csmart.removeRunningExperiment(experiment);
      } else
        csmart.addRunningExperiment(experiment);
      // update society
      experiment.getSocietyComponent().setRunning(isRunning);
    }

//    // if not running, don't allow the user to restart individual nodes
//    JInternalFrame[] frames = desktop.getAllFrames();
//    for (int i = 0; i < frames.length; i++) {
//      String s = frames[i].getTitle();
////       if (log.isDebugEnabled()) {
//// 	log.debug("Considering toggling restart for frame " + s + ". Will tell it: " + !isRunning);
////       }
//      if (!s.equals(configWindowTitle) &&
//          !s.equals(glsWindowTitle)) {
//// 	if (log.isDebugEnabled())
//// 	  log.debug("updateControls setting enableRestart to " + !isRunning + " for node " + s);
//        ((ConsoleInternalFrame)frames[i]).enableRestart(!isRunning);
//      }
//    }
  }




  // Kill any existing output frames
  private void destroyOldNodes() {
//    Iterator nodeNames = oldNodes.iterator();
//    while (nodeNames.hasNext()) {
//      String nodeName = (String) nodeNames.next();
////      removeStatusButton(nodeName);
////      nodePanes.remove(nodeName);
//      oldNodes.remove(nodeNames);
//    }
//    JInternalFrame[] frames = desktop.getAllFrames();
//    for (int i = 0; i < frames.length; i++) {
//      String s = frames[i].getTitle();
//      if (!s.equals(configWindowTitle)) {
//        if (log.isDebugEnabled())
//          log.debug("destroyOldNodes killing frame " + s);
//        // FIXME: Is the title what it's under in the hash?
//        // Note: this completely kills the frame, listener, textpane,
//        // and document in that order
//        desktop.removeFrame(frames[i]);
//      }
//    }
    cleanListeners(); // mostly just to clear out the array
    // it will clear out nodeListeners
  }

// Flush and dispose of old node listeners
  private void cleanListeners() {
//    if (nodeListeners == null || nodeListeners.isEmpty())
//      return;
//    Collection c = nodeListeners.values();
//    for (Iterator i = c.iterator(); i.hasNext();) {
//      ConsoleNodeListener listener = (ConsoleNodeListener) i.next();
//      if (listener == null)
//        continue;
////      if (listener.statusButton == null || listener.statusButton.status == NodeStatusButton.STATUS_NODE_DESTROYED || listener.statusButton.status == NodeStatusButton.STATUS_NO_ANSWER) {
////        listener.cleanUp();
////      } else {
////        // FIXME!!
////        // now what?
////        // wait somehow?
////        // what about STATUS_UNKNOWN
////        if (log.isDebugEnabled())
////          log.debug("cleanListeners found listener for " + listener.nodeName + " possibly not ready (" + listener.statusButton.getStatusDescription() + "), not cleaning.");
////      }
//    }
//    nodeListeners.clear();
  }

  public AppServerSupport getAppServerSupport() {
    return this.appServerSupport;
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

    if (asPollInterval != 0) {
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

  // Go through running nodes. If that Node's AppServer is dead,
  // then mark it dead
  private void noticeIfServerDead() {
    Enumeration nodeNames;
    synchronized (runningNodesLock) {
      nodeNames = runningNodes.keys();
    }

//    while (nodeNames.hasMoreElements()) {
//      String nodeName = (String) nodeNames.nextElement();
//      NodeInfo ni = (NodeInfo) nodeToNodeInfo.get(nodeName);
//      if (ni == null)
//        continue;
//      if (ni.appServer == null) {
//        log.warn("Lost contact with AppServer on " + ni.hostName + " for node " + nodeName + " (null RemoteHost in NodeInfo). Assuming it is dead.");
//        markNodeDead(nodeName);
//        continue;
//      }
//      if (!appServerSupport.isValidRemoteHost(ni.appServer)) {
//        if (log.isWarnEnabled())
//          log.warn("Lost contact with AppServer on " + ni.hostName + " for node " + nodeName + " (Marked as not valid remote host). Assuming it is dead.");
//        // Note - this could be a timeout, so don't do it
//        // so that it's not reversable
//        markNodeDead(nodeName, false);
//        continue;
//      }
//
//      RemoteProcess rp = null;
//      synchronized (runningNodesLock) {
//        rp = (RemoteProcess) runningNodes.get(nodeName);
//      }
//
//      if (rp == null) {
//        if (!stopping) {
//          if (log.isWarnEnabled())
//            log.warn("Remote process suddenly null for " + nodeName + ". Assuming it is dead.");
//          markNodeDead(nodeName);
//        } else {
//          if (log.isInfoEnabled())
//            log.info("Remote process suddenly null for " + nodeName + ", but we're in process of stopping, so its OK.");
//        }
//        continue;
//      }

//      try {
//        if (!rp.isAlive()) {
//          // FIXME: see if it's now not in runningNodes?
//          // If so, a timing issue, and someone killed it...
//          RemoteProcess rp2 = null;
//          synchronized (runningNodesLock) {
//            rp2 = (RemoteProcess) runningNodes.get(nodeName);
//          }
//
//          // FIXME: This doesnt seem to help.
//          // Need to be able to look up which Nodes
//          // are in process of being stopped,
//          // and skip those?
//
//          // OK: We'll check the stopping flag: If set,
//          // we're in the process of stopping the Nodes,
//          // so it's OK if the Node claims to not be Alive.
//
//          if (rp2 == null || stopping) {
//            if (log.isInfoEnabled())
//              log.info("Remote Process must have just been killed by someone else, so it's OK - no need to mark it dead.");
//          } else {
//            if (log.isWarnEnabled())
//              log.warn("Remote Process for " + nodeName + " says it is not alive. Marking it dead.");
//            markNodeDead(nodeName);
//          }
//          continue;
//        }
//      } catch (Exception e) {
//        // Todd W says this should never happen
//        if (log.isWarnEnabled())
//          log.warn("Got exception trying to ask remote process for " + nodeName + " if it is alive. Marking it dead.", e);
//        markNodeDead(nodeName, false);
//        continue;
//      }
//    }
  }

  // Mark a node as unexpectedly dead
  private void markNodeDead(String nodeName) {
    markNodeDead(nodeName, true);
  }

  private void markNodeDead(String nodeName, boolean completely) {
    if (log.isDebugEnabled())
      log.debug("markNodeDead for " + nodeName + " doing it " + (completely ? "completely." : "partially."));

    // if there's a thread still creating nodes, stop it
    // FIXME: Really? Just cause one node had problems, we give up
    // creating all the Nodes?
    // Or do I just want to wait till all the Nodes have been created?
    stopNodeCreation = true;
    // wait for the creating thread to stop
    // note that this means blocking on that thread which waits on RMI
    // So if this method is called from the AWT thread, that's probably bad.
    // Note though that nodeCreator will try to bail out quickly if
    // stopNodeCreation is true. So unless we're in the process of doing the RMI
    // thing, this shouldn't be too bad.
    if (nodeCreator != null) {
      try {
        nodeCreator.join();
      } catch (InterruptedException ie) {
        if (log.isErrorEnabled()) {
          log.error("Exception waiting for node creation thread to die: ", ie);
        }
      }
    }

    // This is destructive - do carefully
    if (completely) {
      RemoteProcess bremoteNode = null;
      synchronized (runningNodesLock) {
        bremoteNode = (RemoteProcess) runningNodes.get(nodeName);
        if (bremoteNode != null) {
//          oldNodes.add(nodeName);
          runningNodes.remove(nodeName);
        }
      } // end synchronized

      nodeStopped(nodeName);
    }

    NodeStatusButton but = getNodeStatusButton(nodeName);
//    if (but != null)
//      but.setStatus(NodeStatusButton.STATUS_NO_ANSWER);

// ConsoleInternalFrame is replaced by NodeView
//    ConsoleInternalFrame frame = desktop.getNodeFrame(nodeName);
//    if (frame != null) {
//      if (log.isDebugEnabled())
//        log.debug("markNodeDead disabling restart for node " + nodeName);
//      frame.enableRestart(false);
//    }
  }

  /**
   * Called by ConsoleNodeListener when node has stopped;
   * called within the Swing thread.
   * If all nodes are stopped, then run is stopped.
   * Update the gui controls.
   */
  public void nodeStopped(String nodeName) {
    if (log.isDebugEnabled())
      log.debug("nodeStopped for node " + nodeName);
    RemoteProcess remoteNode = null;
    synchronized (runningNodesLock) {
      if (runningNodes != null)
        remoteNode = (RemoteProcess) runningNodes.remove(nodeName);
//      if (oldNodes != null)
//        oldNodes.add(nodeName);
    } // end synchronized

    // remove the node listener
    if (remoteNode != null) {
      try {
        RemoteListenable rl = remoteNode.getRemoteListenable();
        if (rl != null) {
          rl.flushOutput();
          rl.removeListener(CSMART.getNodeListenerId());
        }
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception removing listener for remote node " + nodeName, e);
        }
        // FIXME: update AppServer list?
      }
    } // end of block to kill remote listener

    ConsoleNodeListener list = null;
//    if (nodeListeners != null)
//      list = (ConsoleNodeListener) nodeListeners.get(nodeName);

    if (list != null) {
      // do this only if the Node Status Button
      // indicates we got the last output, or otherwise
      // know we got it all?
      // STATUS_DESTROYED is definitely safe
      // STATUS_NO_ANSWER shouldn't happen
      // STATUS_UNKOWN I don't know about
      // but the problem is that nodeStopped
      // gets called in general _before_
      // we set the status to NO_ANSWER if there was an error
      list.closeLogFile();
//       if (list.statusButton == null || list.statusButton.status == NodeStatusButton.STATUS_NODE_DESTROYED || list.statusButton.status == NodeStatusButton.STATUS_NO_ANSWER) {
// 	list.cleanUp();
//       } else {
// 	// FIXME: Wait?
// 	if (log.isDebugEnabled()) {
// 	  log.debug("nodeStopped for node " + nodeName + " wanted to close log for listener, but status says it might still be busy: " + list.statusButton.getStatusDescription());
// 	}
//      }
    } // end of block to kill Node Listener

    // enable restart command on node output window, only if we're not stopping
    // ConsoleInternalFrame is replaced by NodeView
    //    ConsoleInternalFrame frame = null;
    //    if (desktop != null)
    //      frame = desktop.getNodeFrame(nodeName);
    //    if (frame != null && !stopping) {
//       if (log.isDebugEnabled())
// 	log.debug("nodeStopped enabling restart for node " + nodeName);
//      frame.enableRestart(true);
//    }

    // ignore condition in which we temporarily have
    // no running nodes while starting
//    if (starting)
//      return;

    // when all nodes have stopped, save results
    // and update the gui controls
    boolean finishedRun = false;
    synchronized (runningNodesLock) {
      if (runningNodes == null || runningNodes.isEmpty())
        finishedRun = true;
//       else if (log.isDebugEnabled())
// 	log.debug("nodeStopped for node " + nodeName + " still have " + runningNodes.size() + " running nodes.");
    } // end synchronized

    if (finishedRun) {
      if (log.isDebugEnabled())
        log.debug("nodeStopped: finished run after killing node " + nodeName);
      stopping = false;
//      experimentFinished();
    }
  }

  private NodeStatusButton getNodeStatusButton(String nodeName) {
//    Enumeration buttons = statusButtons.getElements();
//    while (buttons.hasMoreElements()) {
//      NodeStatusButton button = (NodeStatusButton) buttons.nextElement();
//      if (button.getActionCommand().equals(nodeName))
//        return button;
//    }
    return null;
  }

  /**
   * Run nodes using info in nodeToNodeInfo hashtable,
   * which maps node names to NodeInfo objects.
   * This is called both when running an experiment and when
   * restarting nodes that were attached to.
   */
  private void runTrial() {
  }

  private void startTimers() {
    startExperimentTime = new Date().getTime();
    experimentTimer.start();
  }

  public String getGlobalNotifyCondition() {
    return globalNotifyCondition;
  }

  public void setGlobalNotifyCondition(String globalNotifyCondition) {
    this.globalNotifyCondition = globalNotifyCondition;
  }

  /**
   * If the console was invoked with a non-null experiment
   * then extract NodeInfo from its nodes and save it
   * in the nodeToNodeInfo hashtable which is used to run the nodes.
   */
  private void initNodesFromExperiment() {
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
          DEFAULT_BOOTSTRAP_CLASS);
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
      if (props.getProperty(Experiment.BOOTSTRAP_CLASS) == null || props.getProperty(Experiment.BOOTSTRAP_CLASS).equals(DEFAULT_BOOTSTRAP_CLASS))
        return Collections.singletonList(DEFAULT_NODE_CLASS);
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
   * Called by AppServerSupport to update add an app server.
   */
  public void addAppServer(AppServerDesc desc) {
    synchronized (appServerLock) {
      appServers.add(desc);
    }
  }

  /**
   * Add node to node-to-appServer mapping.
   */
  public void addNodeToAppServerMapping(String name,
                                        AppServerDesc appServerDesc) {
    boolean nodeAdded = false;
    boolean serverAdded = false;
    synchronized (appServerLock) {
      if (nodeToAppServer.get(name) == null) 
        nodeAdded = true;
      Collection servers = nodeToAppServer.values();
      for (Iterator i = servers.iterator(); i.hasNext();) {
        AppServerDesc serverDesc = (AppServerDesc)i.next();
        if (appServerDesc.hostName.equals(serverDesc.hostName) &&
            appServerDesc.port == serverDesc.port) {
          serverAdded = true;
          break;
        }
      }
      nodeToAppServer.put(name, appServerDesc);
    }
    if (nodeAdded) {
      setChanged();
      notifyObservers(NODE_ADDED);
    }
    if (serverAdded) {
      setChanged();
      notifyObservers(APP_SERVER_ADDED);
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
      server = (AppServerDesc)nodeToAppServer.get(name);
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
        AppServerDesc desc = (AppServerDesc)appServers.get(i);
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
    boolean appServerDeleted = false;
    synchronized (appServerLock) {
      Enumeration nodes = nodeToAppServer.keys();
      while (nodes.hasMoreElements()) {
        String node = (String)nodes.nextElement();
        AppServerDesc desc = getAppServer(node);
        if (desc.hostName.equals(appServerDesc.hostName) &&
            desc.port == appServerDesc.port) {
          nodeToAppServer.remove(node);
          appServerDeleted = true;
        }
      }
      appServers.remove(appServerDesc);
    }
    if (appServerDeleted) {
      setChanged();
      notifyObservers(APP_SERVER_DELETED);
    }
  }

  /**
   * Refresh app servers; notify AppServerSupport.
   */
  public void refreshAppServers() {
    setChanged();
    notifyObservers(APP_SERVERS_REFRESH);
  }

  /**
   * Attach to a running node.
   * Create a node model and add to the list of node models.
   */
  public void attachToNode(String name) {
    RemoteHost appServer = getAppServer(name).appServer;
    // if you're attaching to a node discovered through its app server,
    // then you know nothing about these
    String hostName = "";
    Properties properties = null;
    List args = null;
    NodeInfo ni =
      new NodeInfo(appServer, name, hostName, properties, args);
    NodeModel nodeModel = new NodeModel(ni, this);
    // what detects the new node model and contacts it and adds its views?
    nodeModels.put(name, nodeModel);
  }

  /**
   * Get list of nodes to which CSMART is not attached.
   */
  public ArrayList getUnattachedNodes() {
    return appServerSupport.getUnattachedNodes();
  }

  /**
   * Get list of nodes to which CSMART is attached.
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
        AppServerDesc desc = (AppServerDesc)get(i);
        if (descToAdd.hostName.equals(desc.hostName) &&
            descToAdd.port == desc.port) {
          return;
        }
      }
      super.add(descToAdd);
    }
  }

}

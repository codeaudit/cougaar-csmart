package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteListenableConfig;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.util.Parameters;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * org.cougaar.tools.csmart.ui.console
 *
 */
public class CreateNodeThread extends SwingWorker {

  private boolean reset = false;
  private Logger log = CSMART.createLogger(this.getClass().getName());
  private Object runningNodesLock = new Object();
  private Hashtable runningNodes; // maps node names to RemoteProcesses
  private JToggleButton runButton;
  private Experiment experiment;
  private volatile boolean stopNodeCreation;
  private AppServerSupport appServerSupport;
  private NodeModel model;
  RemoteProcess remoteNode = null;
  private boolean success = false;

  // set this flag when you first run an experiment
  // it's purpose is to ignore a non-null experiment if you're only
  // attaching to nodes
  private boolean usingExperiment = false;

  public CreateNodeThread(NodeModel model) {
    super();
    this.model = model;
  }

  public Object construct() {
    createNode();
    return remoteNode;
  }

  public void finished() {
    if(!success) {
      String node = model.getInfo().getNodeName();
      String host = model.getInfo().getHostName();
      JOptionPane.showMessageDialog(null, "Cannot create node " + node + " on: " +
                                          host + "; check that server is running");
    }
    model.setRunning(success);
  }

  /**
   * Runs in a separate thread, creates the nodes by calling the
   * appserver.
   * Uses the NodeCreationInfo object returned by prepareToCreateNodes
   * to get all the information needed to actually create the node.
   * Does RMI, so may block on the network.
   */
  private void createNode() {

    String experimentName = "Experiment";

    // Create the process description, then the proccess
    try {
      String procName = appServerSupport.getProcessName(experimentName, model.getInfo().getNodeName());

      // Check that a process with description desc is not already running, else modify process name
      while (appServerSupport.isProcessNameUsed(procName)) {
        if (log.isDebugEnabled()) {
          log.debug("ctNodes: process with name " + procName + " already running");
        }
        procName = procName + "1";
      }

      ProcessDescription desc = new ProcessDescription(procName, "csmart",
                                 model.getInfo().getProperties(),
                                 model.getInfo().getArgs());

      RemoteListenableConfig conf = new RemoteListenableConfig(model.getListener(),
                                     CSMART.getNodeListenerId(), null, model.getOutputPolicy());

      // Next line does the actual creation -- including RMI stuff that could take a while
      remoteNode = model.getInfo().getAppServer().createRemoteProcess(desc, conf);

      if (log.isDebugEnabled()) {
        log.debug("Adding listener: " + CSMART.getNodeListenerId() + " for: " + procName);
      }
      success = true;
    } catch (Exception e) {
      // FIXME: Could look for exception: java.lang.RuntimeException:
      // Process name "Experiment for minitestconfig-MiniNode" is already
      // in use by another (running) process
      // which indicates the process is already running, basically

      if (log.isErrorEnabled()) {
        log.error("CSMARTConsole: cannot create node: " + model.getInfo().getNodeName(), e);
      }
    }

//    synchronized (runningNodesLock) {
//      runningNodes.put(nodeInfo.getNodeInfo().getNodeName(), remoteNode);
//    } // end synchronized
//
//    // Set up the UI for the Node
//    SwingUtilities.invokeLater(new ExtractRunAction.NodeCreateThread(nodeInfo, remoteNode));
  }
}

package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteListenableConfig;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.util.log.Logger;

import javax.swing.*;

/**
 * org.cougaar.tools.csmart.ui.console
 *
 */
public class CreateNodeThread extends Thread {

  private Logger log = CSMART.createLogger(this.getClass().getName());
  private NodeModel model;
  RemoteProcess remoteNode = null;
  private boolean success = false;
  private boolean stop = false;

  public CreateNodeThread(NodeModel model) {
    this.model = model;
  }

  public void run() {
    createNode();
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
    // Create the process description, then the proccess
    String name = model.getInfo().getNodeName();
    try {
      ProcessDescription desc =
        new ProcessDescription(name, "csmart",
                               model.getInfo().getProperties(),
                               model.getInfo().getArgs());
      RemoteListenableConfig conf =
        new RemoteListenableConfig(model.getListener(),
                                   CSMART.getNodeListenerId(),
                                   null, model.getOutputPolicy());

      // Next line does the actual creation -- including RMI stuff that could take a while
      remoteNode = model.getInfo().getAppServer().createRemoteProcess(desc, conf);

      if (log.isDebugEnabled()) {
        log.debug("Adding listener: " + CSMART.getNodeListenerId() + " for: " + name);
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
  }
}

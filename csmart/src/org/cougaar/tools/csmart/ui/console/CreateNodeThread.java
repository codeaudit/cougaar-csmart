package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.csmart.experiment.XMLExperiment;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteFileSystem;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.tools.server.RemoteListenable;
import org.cougaar.tools.server.RemoteListenableConfig;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.FileReader;
import java.io.OutputStream;

/**
 * org.cougaar.tools.csmart.ui.console
 *
 */
public class CreateNodeThread extends Thread {

  /** In milliseconds **/
  private static final int KILL_PAUSE_TIME = 2000;

  /** Number of times to attempt to connect before giving up. **/
  private static final int CONNECTION_RETRIES = 5;

  private Logger log = CSMART.createLogger(this.getClass().getName());
  private CSMARTConsoleModel cmodel;
  private NodeModel model;
  RemoteProcess remoteNode = null;
  private boolean attach = false; // true to attach to a running node

  public CreateNodeThread(CSMARTConsoleModel cmodel, NodeModel model) {
    this.cmodel = cmodel;
    this.model = model;
  }

  /**
   * Set to make run method attach to a node, rather than run a node.
   */
  public void setAttach(boolean attach) {
    this.attach = attach;
  }

  /**
   * Start a node or attach to a node.
   * If fail, add the node to the list of failed nodes in the model.
   * If successful, add the node to the list of running nodes in the model.
   */
  public void run() {
    String errorMsg = null;
    if (attach)
      errorMsg = attachToNode();
    else
      errorMsg = createNode();
    String node = model.getInfo().getNodeName();
    if(errorMsg != null) {
      String host = model.getInfo().getHostName();
      JOptionPane.showMessageDialog(null,
                                    "Cannot create node " + node + " on: " +
                                    host + "; " + errorMsg);
      if (attach)
        cmodel.removeNodeModel(node);
      else
        model.setState(NodeModel.STATE_INITTED);
    } else {
      model.setState(NodeModel.STATE_RUNNING);
      cmodel.createGUI(node);
    }
  }

  /**
   * Create a node by calling the appserver.
   * Does RMI, so may block on the network.
   * Returns an error message if there's an exception; else returns null.
   */
  private String createNode() {
    // Create the process description, then the proccess
    String name = model.getInfo().getNodeName();
    ProcessDescription desc =
      new ProcessDescription(name, "csmart",
                             model.getInfo().getProperties(),
                             model.getInfo().getArgs());
    RemoteListenableConfig conf =
      new RemoteListenableConfig(model.getListener(),
                                 CSMART.getNodeListenerId(),
                                 null, model.getOutputPolicy());


    if(cmodel.getExperiment() instanceof XMLExperiment) {
      try {
        RemoteHost rh = model.getInfo().getAppServer();
        RemoteFileSystem rfs = rh.getRemoteFileSystem();
        String filename = ((XMLExperiment)cmodel.getExperiment()).getSocietyFileName();

        OutputStream os = rfs.write("./" + filename);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        FileReader in = new FileReader(cmodel.getXMLFile());
        int c;
        while((c = in.read()) != -1) {
          bos.write(c);
        }
        in.close();
        bos.flush();
        bos.close();
      } catch (Exception e) {
        e.printStackTrace();
      }

    }

    try {
      // Next line does the actual creation -- including RMI stuff that could take a while
      remoteNode = model.getInfo().getAppServer().createRemoteProcess(desc, conf);

      if (log.isDebugEnabled()) {
        log.debug("Adding listener: " + CSMART.getNodeListenerId() + " for: " + name);
      }
      return null; // no errors
    } catch (IllegalArgumentException iae) {
      if (log.isErrorEnabled()) {
        log.error("Cannot create node: " + model.getInfo().getNodeName(), iae);
      }
      return iae.toString();
    } catch (RuntimeException re) {
      if (log.isErrorEnabled()) {
        log.error("Cannot create node: " + model.getInfo().getNodeName(), re);
      }
      return re.toString();
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Cannot create node: " + model.getInfo().getNodeName(), e);
      }
      return e.toString();
    }
  }

  /**
   * Destroy the node.
   * Tries to kill the node 5 times, in case the remote machine or network is busy.
   * Note that this is a different thread from above!
   */
  public void killNode() {
    Thread nodeDestroyer = new Thread("DestroyNode" + model.getInfo().getNodeName()) {

      public void run() {
        // Try to kill the node. Try up to 5 times for now.
        RemoteListenable rl = null;
        for (int retries = CONNECTION_RETRIES; retries > 0; retries--) {
          try {
            rl = remoteNode.getRemoteListenable();
            if (rl != null) {
              rl.flushOutput();
              remoteNode.destroy();
              return;
            }
          } catch (Exception ex) {
            if (rl != null) {
              // Got the RemoteListenable, but couldn't destroy it
              if (log.isWarnEnabled()) {
                log.warn("Unable to destroy node " + model.getNodeName() + ", assuming it's dead: ", ex);
              }
              continue;
            }
          } // end of catch block
          if (log.isWarnEnabled()) {
            log.warn("Never got RemoteListenable for node " + model.getNodeName());
          }
          try {
            sleep(KILL_PAUSE_TIME);
          } catch (InterruptedException ie) {
              // Ignore this exception, it is not critical.
          }
        } // end loop trying to kill node
        // call the method that would have been called if the node stopped
        model.stopped();
      }
      };
    nodeDestroyer.start();
  } // end killNode

  /**
   * Just attach listener to node.
   * Returns an error message if there's an exception; else returns null.
   */
  private String attachToNode() {
    remoteNode = null;
    String nodeName = model.getNodeName();
    String id = CSMART.getNodeListenerId();
    try {
      remoteNode =
        model.getInfo().getAppServer().getRemoteProcess(nodeName);
      if (remoteNode != null) {
        RemoteListenable rl = remoteNode.getRemoteListenable();
        if (log.isDebugEnabled())
          log.debug("Adding listener: " + id + " for: " + nodeName);
        rl.addListener(model.getListener(), id);
        return null; // no errors
      } else {
        if (log.isWarnEnabled())
          log.warn("Got null process from AppServer for node to attach to: " +
                   nodeName);
        throw new Exception("Null RemoteProcess for " + nodeName);
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Exception attaching to: " + nodeName, e);
      }
      return e.toString();
    }
  }

}

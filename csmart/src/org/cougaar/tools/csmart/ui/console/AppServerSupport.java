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

import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.tools.server.RemoteHostRegistry;
import org.cougaar.tools.server.RemoteListenable;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.util.*;

/**
 * Support for communications with the AppServer (Application Server)
 * that handles starting and stopping nodes and directing node output
 * to listeners.
 */
public class AppServerSupport implements Observer {
  private RemoteHostRegistry remoteHostRegistry;
  //  private AppServerList appServers;// known app servers; array of AppServerDesc
  private CSMARTConsoleModel model;

  // this maps the "experiment name-node name" to an AppServerDesc
  // and provides the app server on which the node is running
  // note that the same name must be used for the ProcessDescription
  //  private Hashtable nodeToAppServer;
  private transient Logger log;

  public AppServerSupport(CSMARTConsoleModel model) {
    createLogger();
    remoteHostRegistry = RemoteHostRegistry.getInstance();
    //    appServers = new AppServerList();
    //    nodeToAppServer = new Hashtable();
    this.model = model;
    model.addObserver(this);
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

//   /**
//    * Get app server port from properties.
//    * @param properties properties that may define port
//    * @return the port from the properties or the default port
//    */
   public static int getAppServerPort(Properties properties) {
     // TODO: called from CSMARTConsole
     return -1;
     // determine if still needed
//     int port = Experiment.APP_SERVER_DEFAULT_PORT;
//     if (properties == null)
//       return port;
//     try {
//       String tmp = properties.getProperty(Experiment.CONTROL_PORT);
//       if (tmp != null)
//         port = Integer.parseInt(tmp);
//     } catch (NumberFormatException nfe) {
//       // use default port
//     }
//     if (port < 1)
//       port = Experiment.APP_SERVER_DEFAULT_PORT;
//     return port;
   }

//   /**
//    * Add an appserver at this host and with the port optionally
//    * specified in these properties.  This app server is contacted
//    * and added to the list of known app servers if it exists.
//    * @param hostName host on which the app server is running
//    * @param properties optionally contains app server port
//    * @return the new app server or null
//    */
   public synchronized RemoteHost addAppServerForExperiment(String hostName,
                                                            Properties properties) {
     // TODO: CSMARTConsoleModel calls this
     // but are we still going to have experiments
     // from which to get app servers?
//     if (hostName == null || hostName.equals(""))
//       return null;
//     int remotePort = getAppServerPort(properties);
//     RemoteHost appServer = getAppServer(hostName, remotePort);
//     if (appServer != null) {
//       AppServerDesc desc =
//         new AppServerDesc(appServer, hostName, remotePort);
//       appServers.add(desc);
//       if (log.isDebugEnabled())
// 	log.debug("Adding app server for: " + hostName + " " + remotePort);
//     }
//     return appServer;
     return (RemoteHost)null;
   }

  /**
   * Kill all running processes on all known AppServers,
   * without first trying to attach to them
   */
  public void killAllProcesses() {
    // TODO: called by CSMARTConsole; determine if this can be removed
//     refreshAppServers();
//     killAllProcessesWorker();
  }

//   /**
//    * Synchronized worker for above
//    */
//   private synchronized void killAllProcessesWorker() {
//     Set names = nodeToAppServer.keySet();
//     for (Iterator i = names.iterator(); i.hasNext(); ) {
//       String name = (String)i.next();
//       AppServerDesc desc = (AppServerDesc)nodeToAppServer.get(name);
//       RemoteHost appServer = desc.appServer;
//       try {
//         RemoteProcess node = appServer.getRemoteProcess(name);
// 	if (node != null) {
// 	  try {
// 	    node.getRemoteListenable().flushOutput();
// 	  } catch (Exception e) {
// 	    if (log.isWarnEnabled()) {
// 	      log.warn("killAllProceses: Exception flushing output for " + name, e);
// 	    }
// 	  }
// 	  if (log.isDebugEnabled()) {
// 	    log.debug("About to kill process " + name);
// 	  }
// 	  node.destroy();
// 	} else {
// 	  if (log.isWarnEnabled())
// 	    log.warn("killAllProcesses: couldn't get remote process " + name);
// 	}
//       } catch (Exception e) {
//         if (log.isErrorEnabled()) {
//           log.error("Exception killing process for " + name + ": ", e);
//         }
//       }
//     }
//   }

//   /**
//    * Get the nodes from the known app servers;
//    * get the listeners for those nodes;
//    * kill any listeners started by this instance of CSMART.
//    */
  public void killListeners() {
    // TODO: CSMARTConsoleModel calls this,
    // but need to determine how listeners should actually be killed
//     refreshAppServers();
//     killListenerWorker();
  }

//   /**
//    * Synchronized worker for above.
//    */
//   private synchronized void killListenerWorker() {
//     Set names = nodeToAppServer.keySet();
//     for (Iterator i = names.iterator(); i.hasNext(); ) {
//       String name = (String)i.next();
//       AppServerDesc desc = (AppServerDesc)nodeToAppServer.get(name);
//       RemoteHost appServer = desc.appServer;
//       try {
//         RemoteProcess node = appServer.getRemoteProcess(name);
// 	if (node != null) {
// 	  RemoteListenable rl = node.getRemoteListenable();
// 	  List listenerNames = rl.list();
// 	  for (int j = 0; j < listenerNames.size(); j++) {
// 	    String s = (String)listenerNames.get(j);
// 	    if (s.equals(CSMART.getNodeListenerId())) {
// 	      if (log.isDebugEnabled()) {
// 		log.debug("Killing node listener: " + s +
// 			  " for node: " + name);
// 	      }

// 	      try {
// 		rl.flushOutput();
// 	      } catch (Exception e) {
// 		if (log.isErrorEnabled()) {
// 		  log.error("killListener: Exception flushing output for " + name + ": ", e);
// 		}
// 	      }

// 	      try {
// 		rl.removeListener(s);
// 	      } catch (Exception e) {
// 		if (log.isErrorEnabled()) {
// 		  log.error("killListener: Exception killing listener for " + name + ": ", e);
// 		}
// 	      } // end of block to remove listener
// 	    } // end of block for a CSMART listener
// 	  } // end of loop over listeners
// 	} // end of check for non-null RemoteProcess
// 	else {
// 	  if (log.isWarnEnabled())
// 	    log.warn("killListener got null RemoteProcess for " + name);
// 	}
//       } catch (Exception e) {
//         if (log.isErrorEnabled()) {
//           log.error("killListener: Exception getting listener for " + name + ": ", e);
//         }
// 	// FIXME: Update the list of appservers here? -- but avoid sync problems
//       }
//     }
//   }
  /**
   * Returns true if it finds a listener on the node
   * that this instance of CSMART created.
   * @param appServer the application server to contact
   * @param name the process name
   */
  private boolean findListener(RemoteHost appServer, String name) {
    try {
      RemoteProcess node = appServer.getRemoteProcess(name);
      if (node != null) {
	RemoteListenable rl = node.getRemoteListenable();
	List listenerNames = rl.list();
	for (int j = 0; j < listenerNames.size(); j++) {
	  String s = (String)listenerNames.get(j);
	  if (s.equals(CSMART.getNodeListenerId()))
	    return true;
	}
      } else {
	if (log.isWarnEnabled())
	  log.warn("findListener got null RemoteProcess from appServer for node " + name);
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Exception searching for CSMART listeners on process " + name, e);
      }
      // FIXME: Really should remove this from the active appSever list
    }
    return false;
  }

  /**
   * Returns true if new nodes were discovered since this method
   * was last called, and these new nodes don't have listeners from
   * this instance of CSMART.
   * Note that this method is called periodically from a timer thread
   * so methods in this class are synchronized.
   * @return true if there are new nodes with no listeners from this CSMART
   */
   public boolean haveNewNodes() {
     // TODO: CSMARTConsoleModel calls this
     // need to determine what to do
     return false;
//     return refreshAppServers();
   }

  /**
   * Used to see if we know of any AppServers out there. Return
   * <code>true</code> if we do
   */
  public boolean haveValidAppServers() {
    // TODO: determine if this is still needed
    return true;
//     return checkASListWorker();
  }

//   /**
//    * Synchronized helper for above
//    */
//   private synchronized boolean checkASListWorker() {
//     return ! appServers.isEmpty();
//   }

   /**
    * See if this AppServer (<code>RemoteHost</code>) is
    * still listed as a valid one.
    **/
   public boolean isValidRemoteHost(RemoteHost rh) {
// TODO: this is being called from CSMARTConsole
// determine if it's still needed
     return true;
//     if (rh == null)
//       return false;
//     try {
//       rh.ping();
//       return true;
//     } catch (Exception e) {
//       if (log.isWarnEnabled())
// 	log.warn("isValidRemoteHost: Ping failed on remoteHost");
//       return false;
//     }
   }

  /**
   * Used to see if we know of any running processes out there,
   * regardless of whether we might now be attached to them. Return
   * <code>true</code> if we do.
   */
  public boolean thereAreRunningNodes() {
    // TODO: determine if this is still needed
    return true;
    //    return checkRunningNodesWorker();
  }

//   /**
//    * Synchronized helper for above
//    */
//   private synchronized boolean checkRunningNodesWorker() {
//     return ! nodeToAppServer.isEmpty();
//   }

//   // for debugging
//   private void printInfoFromAppServer(ProcessDescription[] attachToNodes) {
//     for (int i = 0; i < attachToNodes.length; i++) {
//       System.out.println("Name: " + attachToNodes[i].getName());
//       System.out.println("Group: " + attachToNodes[i].getGroup());
//       Map properties = attachToNodes[i].getJavaProperties();
//       Set keys = properties.keySet();
//       for (Iterator j = keys.iterator(); j.hasNext(); ) {
//         Object key = j.next();
//         System.out.println("Property: " + key + ", " + properties.get(key));
//       }
//       java.util.List args = attachToNodes[i].getCommandLineArguments();
//       for (Iterator j = args.iterator(); j.hasNext(); ) {
//         System.out.println("Arg: " + j.next());
//       }
//     }
//   }


  /*****************************************************
   * Start new code
   *****************************************************/

  public void update(Observable o,  Object arg) {
    if (arg instanceof AppServerRequest) {
      AppServerRequest request = (AppServerRequest)arg;
      if (request.action == AppServerRequest.ADD)
        add(request.hostName, request.port);
    } else if (arg instanceof String) {
      if (((String)arg).equals(model.APP_SERVERS_REFRESH))
        refresh();
    }
  }

  /**
   * Attempt to contact an app server on the host and port.
   * If successful, get a list of the nodes on that app server
   * and update the model's node-to-app server mapping.
   */
  public void add(String hostName, int port) {
    // first, make sure that we can contact this app server
    RemoteHost appServer = contactAppServer(hostName, port);
    if (appServer == null)
      return;
    // next, add new nodes if any
    addNewNodes(new AppServerDesc(appServer, hostName, port));
  }

  /**
   * Contact the app server on the host and port.
   * If successful, return a reference to the app server;
   * else return null.
   */
  private RemoteHost contactAppServer(String hostName, int port) {
    RemoteHost appServer = null;
    try {
      // Third argument is whether AppServer should be verbose
      // -- it will print to STDOUT in the CSMART window
      appServer = remoteHostRegistry.lookupRemoteHost(hostName, port, false);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Unable to contact app-server on " +
                  hostName + ":" + port, e);
      }
      final String myHost = hostName;
      final int myPort = port;
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    JOptionPane.showMessageDialog(null,
					  "Unable to contact app-server on " +
					  myHost + ":" + myPort +
					  "; check that server is running");
	  }
	});
      return null;
    }
    return appServer;
  }

  /**
   * Get names of nodes that this app server knows about.
   */

  private java.util.List getNodesOfAppServer(RemoteHost appServer) {
    java.util.List nodes = null;
    try {
      nodes = appServer.listProcessDescriptions();
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Exception getting info from app server: ", e);
      }
      return null;
    }
    return nodes;
  }

  /**
   * After contacting a new app server,
   * update the node to app server mapping.
   */
  private void addNewNodes(AppServerDesc appServerDesc) {
    RemoteHost appServer = appServerDesc.appServer;
    java.util.List nodes = getNodesOfAppServer(appServer);
    if (nodes != null) {
      for (Iterator j = nodes.iterator(); j.hasNext(); ) {
        ProcessDescription pd = (ProcessDescription)j.next();
        String name = pd.getName();
        if (log.isDebugEnabled()) {
          log.debug("Adding app server for: " + name +
                    " " + appServerDesc);
        }
        model.addNodeToAppServerMapping(name, appServerDesc);
      }
    }
  }

  private void refresh() {
    ArrayList appServerDescs = model.getAppServers();
    for (int i = 0; i < appServerDescs.size(); i++) {
      AppServerDesc appServerDesc = (AppServerDesc)appServerDescs.get(i);
      try {
        RemoteHost appServer =
          remoteHostRegistry.lookupRemoteHost(appServerDesc.hostName, 
                                              appServerDesc.port, false);
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception contacting app server: ", e);
        }
        // remove app server that isn't responding
        model.appServerDelete(appServerDesc.hostName,
                              appServerDesc.port);
        continue;
      }
      java.util.List nodes = null;
      try {
        nodes = appServerDesc.appServer.listProcessDescriptions();
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception getting info from app server: ", e);
        }
        // remove app server that isn't responding
        model.appServerDelete(appServerDesc.hostName,
                              appServerDesc.port);
        continue;
      }
      if (nodes != null) {
        for (Iterator j = nodes.iterator(); j.hasNext(); ) {
          ProcessDescription pd = (ProcessDescription)j.next();
          String name = pd.getName();
          AppServerDesc asd = model.getAppServer(name);
          if (asd == null) {
            // add app server mapping for newly discovered nodes
            if (log.isDebugEnabled()) {
              log.debug("Adding app server for: " + name +
                        " " + appServerDesc);
            }
            model.addNodeToAppServerMapping(name, appServerDesc);
          }
        }
      }
    }
  }

  /** 
   * Get the nodes for each app server.  If CSMART isn't listening
   * on the node, then return it as an unattached node.
   */
  public ArrayList getUnattachedNodes() {
    ArrayList unattachedNodes = new ArrayList();
    ArrayList appServers = model.getAppServers();
    for (int i = 0; i < appServers.size(); i++) {
      AppServerDesc appServerDesc = (AppServerDesc)appServers.get(i);
      java.util.List nodes = getNodesOfAppServer(appServerDesc.appServer);
      for (int j = 0; j < nodes.size(); j++) {
        String node = (String)nodes.get(j);
        if (!findListener(appServerDesc.appServer, node))
          unattachedNodes.add(node);
      }
    }
    return unattachedNodes;
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

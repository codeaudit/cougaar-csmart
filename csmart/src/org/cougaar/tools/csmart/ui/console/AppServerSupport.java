/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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

import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.tools.server.RemoteHostRegistry;
import org.cougaar.tools.server.RemoteListenable;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Support for communications with the AppServer (Application Server)
 * that handles starting and stopping nodes and directing node output
 * to listeners.
 */
public class AppServerSupport {
  private RemoteHostRegistry remoteHostRegistry;
  private AppServerList appServers;// known app servers; array of AppServerDesc
  // this maps the "experiment name-node name" to an AppServerDesc
  // and provides the app server on which the node is running
  // note that the same name must be used for the ProcessDescription
  private Hashtable nodeToAppServer; 
  private transient Logger log;

  public AppServerSupport() {
    createLogger();
    remoteHostRegistry = RemoteHostRegistry.getInstance();
    appServers = new AppServerList();
    nodeToAppServer = new Hashtable();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }


  /**
   * Return a handle on the app server running on the specified host
   * and port.
   * @param hostName the host on which to find an app server
   * @param port the port on which to find the app server
   * @return the app server or null
   */
  public RemoteHost getAppServer(String hostName, int port) {
    RemoteHost remoteAppServer = null;
    try {
      remoteAppServer = 
        remoteHostRegistry.lookupRemoteHost(hostName, port, true);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Unable to contact app-server on " + 
                  hostName + " : " + port, e);
      }
      JOptionPane.showMessageDialog(null,
                                    "Unable to contact app-server on " +
                                    hostName + " : " + port +
                                    "; check that server is running");
      return null;
    }
    return remoteAppServer;
  }

  /**
   * Get app server port from properties.
   * @param properties properties that may define port
   * @return the port from the properties or the default port
   */
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
    return port;
  }

  /**
   * Add an appserver at this host and with the port optionally
   * specified in these properties.  This app server is contacted
   * and added to the list of known app servers if it exists.
   * @param hostName host on which the app server is running
   * @param properties optionally contains app server port
   * @return the new app server or null
   */
  public synchronized RemoteHost addAppServerForExperiment(String hostName,
                                                           Properties properties) {
    int remotePort = getAppServerPort(properties);
    RemoteHost appServer = getAppServer(hostName, remotePort);
    if (appServer != null) {
      AppServerDesc desc = 
        new AppServerDesc(appServer, hostName, remotePort);
      appServers.add(desc);
      System.out.println("Adding app server for: " + hostName + " " + remotePort);
    }
    return appServer;
  }

  /**
   * Return the name to be used as the process name when
   * the node is started via the app server.
   * @param experimentName name of the experiment
   * @param nodeName name of the node
   * @return name to be used in creating the ProcessDescription
   */
  public String getProcessName(String experimentName,
                               String nodeName) {
    return experimentName + "-" + nodeName;
  }

  /**
   * Called from menu to display known app servers.
   */
  public synchronized void displayAppServers() {
    Util.showObjectsInList(null, appServers, "Application Servers", 
                           "Application Servers");
  }

  /**
   * Get the nodes from the known app servers;
   * get the listeners for those nodes;
   * kill any listeners started by this instance of CSMART.
   */
  public void killListeners() {
    refreshAppServers();
    killListenerWorker();
  }

  /**
   * Synchronized worker for above.
   */
  private synchronized void killListenerWorker() {
    Set names = nodeToAppServer.keySet();
    for (Iterator i = names.iterator(); i.hasNext(); ) {
      String name = (String)i.next();
      AppServerDesc desc = (AppServerDesc)nodeToAppServer.get(name);
      RemoteHost appServer = desc.appServer;
      try {
        RemoteProcess node = appServer.getRemoteProcess(name);
        RemoteListenable rl = node.getRemoteListenable();
        List listenerNames = rl.list();
        for (int j = 0; j < listenerNames.size(); j++) {
          String s = (String)listenerNames.get(j);
          if (s.equals(CSMART.getNodeListenerId())) {
            System.out.println("Killing node listener: " + s +
                               " for node: " + name);
            rl.removeListener(s);
          }
        }
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception killing listener: " + e);
        }
      }
    }
  }

  /**
   * Recontact and update the known app servers
   * and reconstitute the process name to app server map.
   * @return true if there are new nodes
   */
  public synchronized boolean refreshAppServers() {
    ArrayList appServersToDelete = new ArrayList();
    for (int i = 0; i < appServers.size(); i++) {
      AppServerDesc desc = (AppServerDesc)appServers.get(i);
      RemoteHost rh = getAppServer(desc.hostName, desc.remotePort);
      if (rh == null) 
        appServersToDelete.add(desc);
      else 
        desc.appServer = rh;
    }
    removeAppServers(appServersToDelete);
    return updateNodeToAppServerMapping();
  }

  /**
   * Remove app servers from the list of known app servers
   * and recompute node-to-app-server mappings.
   * @param appServersToDelete AppServerDescriptions for app servers to delete
   */
  private void removeAppServers(ArrayList appServersToDelete) {
    for (int i = 0; i < appServersToDelete.size(); i++) {
      AppServerDesc appServerToDelete = 
        (AppServerDesc)appServersToDelete.get(i);
      appServers.remove(appServerToDelete);
    }
  }

  /**
   * Called from menu to delete app servers from the list of known servers.
   */
  public synchronized void deleteAppServers() {
    Object[] appServersSelected =
      Util.getObjectsFromList(null, appServers, "Application Servers",
                              "Select Application Servers To Ignore:");
    if (appServersSelected == null) return;
    ArrayList appServersToDelete = new ArrayList();
    for (int i = 0; i < appServersSelected.length; i++)
      appServersToDelete.add(appServersSelected[i]);
    removeAppServers(appServersToDelete);
    updateNodeToAppServerMapping();
  }

  /**
   * Called from menu to query user for a hostname and port
   * for a new app server.
   * If create AppServer successfully, then add it to the list of appServers.
   * Immediately update the node to app server mappings to get any new nodes.
   */
  public void addAppServer() {
    JTextField tf = new JTextField("", 20);
    JPanel panel = new JPanel();
    panel.add(new JLabel("Enter HostName:Port:"));
    panel.add(tf);
    int result = 
      JOptionPane.showOptionDialog(null, panel, "Add Application Server",
				   JOptionPane.OK_CANCEL_OPTION,
				   JOptionPane.PLAIN_MESSAGE,
				   null, null, null);
    if (result != JOptionPane.OK_OPTION)
      return;
    String s = tf.getText();
    int index = s.indexOf(':');
    if (index == -1)
      return;
    String hostName = s.substring(0, index);
    hostName = hostName.trim();
    String port = s.substring(index+1);
    port = port.trim();
    int remotePort = 0;
    try {
      remotePort = Integer.parseInt(port);
    } catch (Exception e) {
      return;
    }
    RemoteHost appServer = getAppServer(hostName, remotePort);
    if (appServer != null)
      addAppServerWorker(new AppServerDesc(appServer, hostName, remotePort));
  }

  private synchronized void addAppServerWorker(AppServerDesc desc) {
    appServers.add(desc);
    updateNodeToAppServerMapping();
  }

  /**
   * Query all known app servers for process descriptions of their
   * nodes and update the node to app server mapping.
   * Returns true if there are new nodes that don't have
   * a listener from this version of CSMART.
   * This checks the listeners, because we find all the nodes
   * through this method, and we want to ignore nodes that we're already
   * listening to, because we started them.
   * @return true if there are new nodes
   */
  private boolean updateNodeToAppServerMapping() {
    System.out.println("Updating app servers");
    boolean haveNewNodes = false;
    // get process descriptions from all known app servers
    // and store info in a new hashtable
    Hashtable newNodeToAppServer = new Hashtable();
    for (int i = 0; i < appServers.size(); i++) {
      AppServerDesc appServerDesc = (AppServerDesc)appServers.get(i);
      RemoteHost appServer = appServerDesc.appServer;
      java.util.List someNodes = null;
      try {
        someNodes = appServer.listProcessDescriptions();
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception getting info from app server: " + e);
        }
      }
      if (someNodes != null) {
        for (Iterator j = someNodes.iterator(); j.hasNext(); ) {
          ProcessDescription pd = (ProcessDescription)j.next();
          String name = pd.getName();
          System.out.println("Adding app server for: " + name +
                             " " + appServerDesc);
          newNodeToAppServer.put(name, appServerDesc);
          // if the new node is mapped to an
          // app server on the same machine and port as the old mapping
          // then don't consider it new, because we've previously discovered it
          AppServerDesc desc = (AppServerDesc)nodeToAppServer.get(name);
          if (desc != null &&
              desc.hostName.equals(appServerDesc.hostName) &&
              desc.remotePort == appServerDesc.remotePort)
            continue;
          else if (!findListener(appServer, name))
            haveNewNodes = true; 
        }
      }
    }
    nodeToAppServer = newNodeToAppServer; // set new mapping
    return haveNewNodes;
  }

  /**
   * Returns true if it finds a listener on the node
   * that this instance of CSMART created.
   * @param appServer the application server to contact
   * @param the process name
   */
  private boolean findListener(RemoteHost appServer, String name) {
    try {
      RemoteProcess node = appServer.getRemoteProcess(name);
      RemoteListenable rl = node.getRemoteListenable();
      List listenerNames = rl.list();
      for (int j = 0; j < listenerNames.size(); j++) {
        String s = (String)listenerNames.get(j);
        if (s.equals(CSMART.getNodeListenerId())) 
          return true;
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Exception searching for listeners", e);
      }
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
    return refreshAppServers();
  }

  /**
   * Display all process names from app servers and return the names
   * that the user selects.  Ignore nodes that have a listener
   * from this instance of CSMART.
   */
  private String[] getNodesToAttach(ArrayList nodes) {
    Object[] selected = 
      Util.getObjectsFromList(null, nodes, 
                              "Attach to Nodes", "Select Nodes:");
    if (selected != null) {
      ArrayList sel = new ArrayList(selected.length);
      for (int i = 0; i < selected.length; i++)
        sel.add(selected[i]);
      return (String[])sel.toArray(new String[selected.length]);
    } else
      return null;
  }

  /**
   * Called from attach button to attach to nodes that user specifies.
   * @return list of NodeInfo objects for nodes to attach
   */
  public synchronized ArrayList getNodesToAttach() {
    if (appServers.size() == 0)
      return null;
    Set processNames = nodeToAppServer.keySet();
    if (processNames.size() == 0)
      return null;
    ArrayList results = new ArrayList();
    String[] attachToNodes = getNodesToAttach(new ArrayList(processNames));
    if (attachToNodes == null)
      return null;
    for (int i = 0; i < attachToNodes.length; i++) {
      String processName = attachToNodes[i];
      AppServerDesc desc = (AppServerDesc)nodeToAppServer.get(processName);
      RemoteHost appServer = desc.appServer;
      ProcessDescription pd = null;
      try {
        pd = appServer.getProcessDescription(processName);
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception attaching to: " + processName, e);
        }
        continue;
      }
      Properties properties = (Properties)pd.getJavaProperties();
      String nodeName = (String)properties.get("org.cougaar.node.name");
      String hostName = (String)properties.get("org.cougaar.name.server");
      java.util.List args = pd.getCommandLineArguments();
      results.add(new NodeInfo(appServer, nodeName, hostName,
                               processName,
                               properties, args));
    }
    return results;
  }

  // for debugging
  private void printInfoFromAppServer(ProcessDescription[] attachToNodes) {
    for (int i = 0; i < attachToNodes.length; i++) {
      System.out.println("Name: " + attachToNodes[i].getName());
      System.out.println("Group: " + attachToNodes[i].getGroup());
      Map properties = attachToNodes[i].getJavaProperties();
      Set keys = properties.keySet();
      for (Iterator j = keys.iterator(); j.hasNext(); ) {
        Object key = j.next();
        System.out.println("Property: " + key + ", " + properties.get(key));
      }
      java.util.List args = attachToNodes[i].getCommandLineArguments();
      for (Iterator j = args.iterator(); j.hasNext(); ) {
        System.out.println("Arg: " + j.next());
      }
    }
  }

  /**
   * This contains the information about an application server,
   * needed to "display app servers" for the user.
   */

  class AppServerDesc {
    RemoteHost appServer;
    String hostName;
    int remotePort;

    public AppServerDesc(RemoteHost appServer, String hostName,
                         int remotePort) {
      this.appServer = appServer;
      this.hostName = hostName;
      this.remotePort = remotePort;
    }

    public String toString() {
      return hostName + ":" + remotePort;
    }
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
            descToAdd.remotePort == desc.remotePort) {
          return;
        }
      }
      super.add(descToAdd);
    }
  }
  
}

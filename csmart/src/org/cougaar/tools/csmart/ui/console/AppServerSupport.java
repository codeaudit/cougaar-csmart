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

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.tools.server.RemoteHostRegistry;
import org.cougaar.tools.server.RemoteListenable;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Support for communications with the AppServer (Application Server)
 * that handles starting and stopping nodes and directing node output
 * to listeners.
 */
public class AppServerSupport implements Observer {
  private RemoteHostRegistry remoteHostRegistry;
  private CSMARTConsoleModel model;
  private transient Logger log;

  public AppServerSupport(CSMARTConsoleModel model) {
    createLogger();
    remoteHostRegistry = RemoteHostRegistry.getInstance();
    this.model = model;
    model.addObserver(this);
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Kill all running processes on all known AppServers.
   */
  public void killAllProcesses() {
    refreshAppServers();
    ArrayList appServers = model.getAppServers();
    for (int i = 0; i < appServers.size(); i++) {
      AppServerDesc desc = (AppServerDesc)appServers.get(i);
      RemoteHost appServer = desc.appServer;
      List nodes = getNodesOfAppServer(appServer);
      if (nodes == null)
        continue;
      for (Iterator j = nodes.iterator(); j.hasNext(); ) {
        ProcessDescription pd = (ProcessDescription)j.next();
        String name = pd.getName();
        try {
          RemoteProcess node = appServer.getRemoteProcess(name);
          if (node != null) {
            try {
              node.getRemoteListenable().flushOutput();
            } catch (Exception e) {
              if (log.isWarnEnabled()) {
                log.warn("killAllProceses: Exception flushing output for " + name, e);
              }
            }
            if (log.isDebugEnabled()) {
              log.debug("About to kill process " + name);
            }
            node.destroy();
          } else {
            if (log.isWarnEnabled())
              log.warn("killAllProcesses: couldn't get remote process " + name);
          }
        } catch (Exception e) {
          if (log.isErrorEnabled()) {
            log.error("Exception killing process for " + name + ": ", e);
          }
        }
      }
    }
  }

   /**
    * Get the list of attached nodes and kill their listeners.
    * Used when detaching from a running society.
    */
  public void killListeners() {
    refreshAppServers();
    ArrayList nodes = model.getAttachedNodes();
    String myListener = CSMART.getNodeListenerId();
    for (int i = 0; i < nodes.size(); i++) {
      String name = (String)nodes.get(i);
      AppServerDesc desc = model.getAppServer(name);
      RemoteHost appServer = desc.appServer;
      RemoteListenable rl = findListener(appServer, name);
      if (rl == null)
        continue;
      if (log.isDebugEnabled()) {
        log.debug("Killing node listener for node: " + name);
      }
      try {
        rl.flushOutput();
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("killListener: Exception flushing output for " + name + ": ", e);
        }
      }
      try {
        rl.removeListener(myListener);
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("killListener: Exception killing listener for " + name + ": ", e);
        }
      }
    }
  }

  /**
   * Add or refresh app servers.
   */
  public void update(Observable o,  Object arg) {
    if (arg instanceof AppServerRequest) {
      AppServerRequest request = (AppServerRequest)arg;
      if (request.action == AppServerRequest.ADD)
        add(request.hostName, request.port);
    } else if (arg instanceof String) {
      if (((String)arg).equals(model.APP_SERVERS_REFRESH))
        refreshAppServers();
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
    AppServerDesc desc = new AppServerDesc(appServer, hostName, port);
    model.addAppServer(desc);
    // next, add new nodes if any
    addNewNodes(desc);
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
   * Get list of nodes that this app server knows about.
   * Returns list of ProcessDescriptions.
   */
  private List getNodesOfAppServer(RemoteHost appServer) {
    List nodes = null;
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
    List nodes = getNodesOfAppServer(appServer);
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

  /**
   * Updates both the App Servers and the nodes they control.
   */
  public void refreshAppServers() {
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
        model.appServerDelete(appServerDesc);
        continue;
      }
      List nodes = null;
      try {
        nodes = appServerDesc.appServer.listProcessDescriptions();
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception getting info from app server: ", e);
        }
        // remove app server that isn't responding
        model.appServerDelete(appServerDesc);
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
   * Returns an array of Strings (node names).
   */
  public ArrayList getUnattachedNodes() {
    ArrayList unattachedNodes = new ArrayList();
    ArrayList appServers = model.getAppServers();
    for (int i = 0; i < appServers.size(); i++) {
      AppServerDesc appServerDesc = (AppServerDesc)appServers.get(i);
      List nodes = getNodesOfAppServer(appServerDesc.appServer);
      if (nodes != null) {
        for (Iterator j = nodes.iterator(); j.hasNext(); ) {
          ProcessDescription pd = (ProcessDescription)j.next();
          String name = pd.getName();
          if (findListener(appServerDesc.appServer, name) == null)
            unattachedNodes.add(name);
        }
      }
    }
    return unattachedNodes;
  }

  /**
   * Returns listener if it finds a listener on the node
   * that this instance of CSMART created.
   */
  private RemoteListenable findListener(RemoteHost appServer, String name) {
    try {
      RemoteProcess node = appServer.getRemoteProcess(name);
      if (node != null) {
	RemoteListenable rl = node.getRemoteListenable();
	List listenerNames = rl.list();
	for (int j = 0; j < listenerNames.size(); j++) {
	  String s = (String)listenerNames.get(j);
	  if (s.equals(CSMART.getNodeListenerId()))
	    return rl;
	}
      } else {
	if (log.isWarnEnabled())
	  log.warn("findListener got null RemoteProcess from appServer for node " + name);
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Exception searching for CSMART listeners on process " + name, e);
      }
    }
    return null;
  }
}

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

package org.cougaar.tools.csmart.experiment;

import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Maintains information about a host and generates the
 * host .ini file.
 **/
public class ExperimentHost
  extends ModifiableConfigurableComponent
  implements Serializable, HostComponent
{
  private static final long serialVersionUID = 9111998126122353108L;

  public static final String DEFAULT_HOST_NAME = "localhost";

  private List nodes = new ArrayList();
  private int serverPort;
  private int monitoringPort;
  private transient Logger log;

  public ExperimentHost(String hostName) {
      super(hostName);
      createLogger();
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if(o instanceof ExperimentHost) {
      return getShortName().equals(((ExperimentHost)o).getShortName());
    } else {
      return false;
    }
  }

  private void createLogger() {
      log = CSMART.createLogger(this.getClass().getName());
  }

  public void initProperties() {
    addProperty("NodeNames", new ArrayList());
  }

  private int getNodeCount() {
    return nodes.size();
  }

  public NodeComponent[] getNodes() {
    return (NodeComponent[]) nodes.toArray(new NodeComponent[getNodeCount()]);
  }

  public NodeComponent addNode(NodeComponent node) {
    Property prop = getProperty("NodeNames");
    if (prop == null)
      prop = addProperty("NodeNames", new ArrayList());
    ArrayList names = (ArrayList)prop.getValue();
    names.add(node.getShortName());
    ExperimentNode sa = (ExperimentNode) node;
    if(log.isDebugEnabled()) {
      log.debug("ExperimentHost: " + getShortName() +
                " added node: " + node.getShortName());
    }
    nodes.add(sa);
    fireModification();
    return sa;
  }

  public NodeComponent getNode(int ix) {
    return (NodeComponent) nodes.get(ix);
  }

  public void removeNode(NodeComponent node) {
    Property prop = getProperty("NodeNames");
    if (prop != null) {
      ArrayList names = (ArrayList)prop.getValue();
      int index = names.indexOf(node.getShortName());
      if (index != -1)
        names.remove(node.getShortName());
    }
    if(log.isDebugEnabled()) {
      log.debug("ExperimentHost: " + getShortName() +
                " removed node: " + node.getShortName());
    }
    nodes.remove(node);
    fireModification();
  }

  public void dispose() {
    nodes.clear();
    fireModification();
  }

  /**
   * Get the port on which to contact the server on this host.
   * @return the server port
   */
  public int getServerPort() {
    return serverPort;
  }

  /**
   * Set the port on which to contact the server on this host.
   * @param serverPort
   */
  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  /**
   * Get the port on which to monitor nodes running on this host.
   * @return the monitoring port
   */
  public int getMonitoringPort() {
    return monitoringPort;
  }

  /**
   * Set the port on which to monitor nodes running on this host.
   * @param monitoringPort on which to monitor nodes
   */
  public void setMonitoringPort(int monitoringPort) {
    this.monitoringPort = monitoringPort;
  }

  // FIXME: Add a copy method that takes avoids copying the NodeNames property


  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

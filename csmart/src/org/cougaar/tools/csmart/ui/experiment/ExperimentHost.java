/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.experiment;

import org.cougaar.tools.server.ConfigurationWriter;
import org.cougaar.tools.csmart.ui.component.*;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
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

  public ExperimentHost(String hostName) {
      super(hostName);
  }

  public void initProperties() {
  }

  public int getNodeCount() {
    return nodes.size();
  }

  public NodeComponent[] getNodes() {
    return (NodeComponent[]) nodes.toArray(new NodeComponent[getNodeCount()]);
  }

  public void addNode(NodeComponent node) {
    ExperimentNode sa = (ExperimentNode) node;
    nodes.add(sa);
    fireModification();
  }

  public NodeComponent getNode(int ix) {
    return (NodeComponent) nodes.get(ix);
  }

  public void removeNode(NodeComponent node) {
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
   * @param the server port
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
   * @param the monitoring port
   */

  public void setMonitoringPort(int monitoringPort) {
    this.monitoringPort = monitoringPort;
  }
}

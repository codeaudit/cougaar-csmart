/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.component;

import java.io.File;
import java.io.IOException;

/**
 * The interface for adding and removing hosts from a society.
 */

public interface HostComponent extends ComponentProperties {

  /**
   * Add a node to the host.
   * @param node the node to add
   */

  void addNode(NodeComponent node);

  /**
   * Remove the specified node from the host.
   * @param node the node to remove
   */

  void removeNode(NodeComponent node);

  /**
   * Get the nodes in this host.
   * @return array of node components
   */

  NodeComponent[] getNodes();

  /**
   * Get the port on which to contact the server on this host.
   * Defaults to the port specified in: ???
   * @return the server port
   */

  public int getServerPort();

  /**
   * Set the port on which to contact the server on this host.
   * @param the server port
   */

  public void setServerPort(int serverPort);

  /**
   * Get the port on which to monitor nodes running on this host.
   * Defaults to the port specified in: ???
   * @return the monitoring port
   */

  public int getMonitoringPort();

  /**
   * Set the port on which to monitor nodes running on this host.
   * @param the monitoring port
   */

  public void setMonitoringPort(int monitoringPort);

}

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

import org.cougaar.tools.csmart.core.property.BaseComponent;

/**
 * The interface for adding and removing hosts from a society.
 */
public interface HostComponent extends BaseComponent {

  /**
   * Add a node to the host.
   * @param node the node to add
   */
  NodeComponent addNode(NodeComponent node);

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
  int getServerPort();

  /**
   * Set the port on which to contact the server on this host.
   * @param serverPort the server port
   */
  void setServerPort(int serverPort);

  /**
   * Get the port on which to monitor nodes running on this host.
   * Defaults to the port specified in: ???
   * @return the monitoring port
   */
  int getMonitoringPort();

  /**
   * Set the port on which to monitor nodes running on this host.
   * @param monitoringPort port on which to monitor nodes
   */
  void setMonitoringPort(int monitoringPort);
}

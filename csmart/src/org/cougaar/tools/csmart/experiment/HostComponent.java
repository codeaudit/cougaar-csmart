/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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

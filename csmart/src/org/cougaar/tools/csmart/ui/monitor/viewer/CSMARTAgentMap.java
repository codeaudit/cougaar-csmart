/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.viewer;

import org.cougaar.tools.csmart.ui.psp.AgentMapping;

public class CSMARTAgentMap implements AgentMapping {
  String agentHost;
  int agentPort;

  public CSMARTAgentMap(String agentHost, int agentPort) {
    this.agentHost = agentHost;
    this.agentPort = agentPort;
  }

  /**
   * AgentMapping interface.
   */

  public String getHost(String agentName) {
    return agentHost;
  }

  public int getPort(String agentName) {
    return agentPort;
  }
}

/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.configgen;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of <code>Society</code>.
 */
public class SocietyImpl implements Society {

  private List agents;
  private Map nodes;

  public SocietyImpl() {
  }

  /** @return a List of all Agents in the Society */
  public List getAgents() {
    return agents;
  }

  public void addAgent(Agent a) {
    if (agents == null) {
      agents = new ArrayList();
    }
    agents.add(a);
  }

  /** @return a Map of (NodeName, List of Agents in that Node) */
  public Map getNodes() {
    return nodes;
  }

  public void addNode(String nodeName, List nodeAgents) {
    if (nodes == null) {
      nodes = new HashMap(15);
    }
    nodes.put(nodeName, nodeAgents);
  }

} // SocietyImpl

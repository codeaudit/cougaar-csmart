/*
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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

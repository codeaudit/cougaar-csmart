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

import java.util.List;
import java.util.Map;

/**
 * A Society is simply a List of <code>Agent</code>s and 
 * a Map of (NodeName, List of Agents in that Node).
 */
public interface Society  {

  /** @return a List of all Agents in the Society */
  public List getAgents();

  public void addAgent(Agent a);

  /** @return a Map of (NodeName, List of Agents in that Node) */
  public Map getNodes();

  public void addNode(String nodeName, List nodeAgents);

} // Society

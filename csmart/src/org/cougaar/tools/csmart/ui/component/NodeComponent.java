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
 * The interface for adding and removing nodes from a society.
 */

public interface NodeComponent extends ComponentProperties {

  /**
   * Add an agent to the node.
   * @param agent the agent to add
   */

  void addAgent(AgentComponent agent);

  /**
   * Remove the specified agent from the node.
   * @param agent the agent to remove
   */

  void removeAgent(AgentComponent agent);

  /**
   * Get the agents in this node.
   * @return array of agent components
   */

  AgentComponent[] getAgents();
}

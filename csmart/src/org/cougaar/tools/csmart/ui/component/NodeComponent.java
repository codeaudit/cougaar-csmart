/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.component;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

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

  /**
   * Set arguments.
   */

  void setArguments(Properties arguments);

  /**
   * Get arguments.
   */

  Properties getArguments();
}

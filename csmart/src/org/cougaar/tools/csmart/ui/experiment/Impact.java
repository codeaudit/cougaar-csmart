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

import org.cougaar.tools.csmart.ui.viewer.Organizer;
import org.cougaar.tools.csmart.ui.component.NodeComponent;
import org.cougaar.tools.csmart.ui.component.AgentComponent;

import org.cougaar.tools.server.ConfigurationWriter;

/**
 * Parent interface for all Impacts to a CSMART system.
 * Impacts may have Agents, and may need to be able to write
 * out information in the various Nodes. These interfaces are
 * exposed.
 */
public interface Impact extends java.io.Serializable {

  void setName(String newName);
  String getName();
  Impact copy(Organizer organizer, Object context);
  
  /**
   * Get the agents, both assigned and unassigned.
   * @return array of agent components
   */
  AgentComponent[] getAgents();

  /**
   * Get a configuration writer for this Impact.
   */
  public ConfigurationWriter getConfigurationWriter(NodeComponent[] nodes);

  /**
   * This is the opportunity for an impact to specify additional
   * components to load into non-Impact Agents
   *
   * @return a <code>String</code> Node file addition, possibly null
   */
  public String getNodeFileAddition();
}





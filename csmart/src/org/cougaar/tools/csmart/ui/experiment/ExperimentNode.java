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

package org.cougaar.tools.csmart.ui.experiment;

import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.console.CSMARTConsole;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Maintains information about a node and generates the
 * node .ini file.
 **/
public class ExperimentNode
  extends ModifiableConfigurableComponent
  implements Serializable, NodeComponent, ModifiableComponent
{
  private static final long serialVersionUID = 253490717511962460L;

  public static final String DEFAULT_NODE_NAME = "DefaultNode";

  private List agents = new ArrayList();
  private Experiment experiment;
  private Properties arguments = null;

  public ExperimentNode(String nodeName, Experiment experiment) {
    super(nodeName);
    this.experiment = experiment;
    addProperty("Experiment", experiment);
    addDefaultArgumentsToNode(nodeName);
  }

  public void initProperties() {
    addProperty("AgentNames", new ArrayList());
  }

  /**
   * Adds default arguments to a node, if and only if, there are
   * no arguments on the node.
   */

  private void addDefaultArgumentsToNode(String nodeName) {
    arguments = new Properties();
    arguments.put("org.cougaar.core.cluster.persistence.enabled", "false");
    arguments.put("user.timezone", "GMT");
    arguments.put("org.cougaar.core.cluster.startTime", "08/10/2005");
    arguments.put("csmart.log.severity", "PROBLEM");
    arguments.put("org.cougaar.domain.planning.ldm.lps.ComplainingLP.level",
                  "0");
    arguments.put("org.cougaar.message.transport.aspects",
                  "org.cougaar.core.mts.StatisticsAspect");
    arguments.put("org.cougaar.tools.server.nameserver.ports", 
                  CSMARTConsole.NAME_SERVER_PORTS);
    arguments.put("org.cougaar.control.port", 
                  Integer.toString(CSMARTConsole.APP_SERVER_DEFAULT_PORT));
    if (experiment.isInDatabase()) {
      arguments.put("org.cougaar.configuration.database", 
                    CSMART.getDatabaseConfiguration());
      arguments.put("org.cougaar.configuration.user", 
                    CSMART.getDatabaseUserName());
      arguments.put("org.cougaar.configuration.password", 
                    CSMART.getDatabaseUserPassword());
      arguments.put("org.cougaar.experiment.id", experiment.getTrialID());
      arguments.put("org.cougaar.node.name", nodeName);
    }
    try {
      arguments.put("env.DISPLAY", InetAddress.getLocalHost().getHostName() +
                     ":0.0");
    } catch (UnknownHostException uhe) {
      System.out.println(uhe);
    }
  }

  public int getAgentCount() {
    return agents.size();
  }

  public AgentComponent[] getAgents() {
    return (AgentComponent[]) agents.toArray(new AgentComponent[getAgentCount()]);
  }

  public void addAgent(AgentComponent agent) {
    Property prop = getProperty("AgentNames");
    if (prop == null) 
      prop = addProperty("AgentNames", new ArrayList());
    ArrayList names = (ArrayList)prop.getValue();
    names.add(agent.getShortName());
    agents.add(agent);
    fireModification();
  }

  public AgentComponent getAgent(int ix) {
    return (AgentComponent) agents.get(ix);
  }

  public void removeAgent(AgentComponent agent) {
    Property prop = getProperty("AgentNames");
    if (prop != null) {
      ArrayList names = (ArrayList)prop.getValue();
      int index = names.indexOf(agent.getShortName());
      if (index != -1)
        names.remove(agent.getShortName());
    }
    agents.remove(agent);
    fireModification();
  }

  public void dispose() {
    agents.clear();
    fireModification();
  }

  /**
   * Set arguments.
   */

  public void setArguments(Properties arguments) {
    this.arguments = arguments;
  }

  /**
   * Get arguments.
   */

  public Properties getArguments() {
    return arguments;
  }

  /**
   * Copy this node and return the new node.
   * @param the experiment which will contain this node
   */

  public NodeComponent copy(Experiment experimentCopy) {
    NodeComponent nodeCopy = new ExperimentNode(getShortName(), 
                                                experimentCopy);
    Properties newProperties = new Properties();
    Enumeration propertyNames = arguments.propertyNames();
    while (propertyNames.hasMoreElements()) {
      String key = (String)propertyNames.nextElement();
      newProperties.setProperty(key, arguments.getProperty(key));
    }
    nodeCopy.setArguments(newProperties);
    return nodeCopy;
  }
}

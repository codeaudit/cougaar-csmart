/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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

import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.util.ReadOnlyProperties;
import java.io.Serializable;
import java.util.*;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * Maintains information about a node and generates the
 * node .ini file.
 **/
public class ExperimentNode
  extends ModifiableConfigurableComponent
  implements Serializable, NodeComponent
{
  private static final long serialVersionUID = 253490717511962460L;

  public static final String DEFAULT_NODE_NAME = "DefaultNode";

  private List agents = new ArrayList();
  private Experiment experiment;
  private ReadOnlyProperties arguments = null;
  private transient Logger log;

  public ExperimentNode(String nodeName, Experiment experiment) {
    super(nodeName);
    this.experiment = experiment;
    addProperty("Experiment", experiment);
    addDefaultArgumentsToNode(nodeName);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void initProperties() {
    addProperty("AgentNames", new ArrayList());
  }

  public boolean equals(Object o) {
    if(o instanceof ExperimentNode) {
      return getShortName().equals(((ExperimentNode)o).getShortName());
    } else {
      return false;
    }
  }

  /**
   * Adds node name as an argument to the node.
   */

  private void addDefaultArgumentsToNode(String nodeName) {
    arguments =
      new ReadOnlyProperties(Collections.singleton("org.cougaar.node.name"),
                             experiment.getDefaultNodeArguments());
    arguments.setReadOnlyProperty("org.cougaar.node.name", nodeName);
  }

  public void rename(String newName) {
    arguments.setReadOnlyProperty("org.cougaar.node.name", newName);
    setName(newName); // invokes setName in ConfigurableComponent
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
//      if(log.isDebugEnabled()) {
//        log.debug("ExperimentNode: " + getShortName() +
//                  " added agent: " + agent.getShortName());
//      }
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
    if(log.isDebugEnabled()) {
      log.debug("ExperimentNode: " + getShortName() +
                " removed agent: " + agent.getShortName());
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

//    public void setArguments(Properties arguments) {
//      this.arguments = arguments;
//    }

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
    Properties newProps = nodeCopy.getArguments();
    newProps.clear();
    newProps.putAll(getArguments());
    return nodeCopy;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

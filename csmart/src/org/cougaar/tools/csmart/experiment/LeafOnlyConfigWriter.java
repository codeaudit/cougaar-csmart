/* 
 * <copyright>
 * Copyright 2001 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).

 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.tools.csmart.experiment;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;

import org.cougaar.tools.server.ConfigurationWriter;

import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.society.AgentComponent;

import org.cougaar.tools.csmart.core.property.name.ComponentName;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.BaseComponent;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.LeafComponentData;

import java.io.PrintWriter;
import java.io.FileWriter;

/**
 * Config writer that writes out only the LeafComponentData - that is, only writes files
 * that are parameters to Plugins and are not stored in the ConfigDB
 * Note this should be fixed - it constructs the _complete_ ComponentData
 * and then inefficiently finds the Leaf files to write them.
 **/
public class LeafOnlyConfigWriter implements ConfigurationWriter {
  transient NodeComponent[] nodesToWrite;
  transient List components;
  ComponentData theSoc;

  public LeafOnlyConfigWriter(List components, NodeComponent[] nodesToWrite, Experiment exp) {
    this.nodesToWrite = nodesToWrite;
    this.components = components;
    theSoc = new GenericComponentData();
    theSoc.setType(ComponentData.SOCIETY);
    theSoc.setName(exp.getExperimentName()); // this should be experiment: trial FIXME
    theSoc.setClassName(""); // leave this out? FIXME
    theSoc.setOwner(exp); // the experiment
    theSoc.setParent(null);
    // For each node, create a GenericComponentData, and add it to the society
    addNodes(exp);

    // Some components will want access to the complete set of Nodes in the society, etc.
    // To get that, they must get back to the root soc object,
    // and do a getOwner and go from there. Ugly.
    
    // Now ask each component in turn to add its stuff
    for (int i = 0; i < components.size(); i++) {
      BaseComponent soc = (BaseComponent) components.get(i);
      soc.addComponentData(theSoc);
    }
    // Then give everyone a chance to modify what they've collectively produced
    for (int i = components.size() - 1; i >= 0; i--) {
      BaseComponent soc = (BaseComponent) components.get(i);
      soc.modifyComponentData(theSoc);
    }    
  }
  
  public void writeConfigFiles(File configDir) throws IOException {
    // Call writeNodeFile for each of the nodes in theSoc.
    ComponentData[] nodes = theSoc.getChildren();
    for (int i = 0; i < theSoc.childCount(); i++) {
      writeNodeFile(configDir, nodes[i]);
    }
  }
  
  private void writeNodeFile(File configDir, ComponentData nc) throws IOException {
    // loop over children
    // if there are binder or such, do those
    ComponentData[] children = nc.getChildren();
    for (int i = 0; i < nc.childCount(); i++) {
      // write out any leaf components
      writeLeafData(configDir, children[i]);
    }
    for (int i = 0; i < nc.childCount(); i++) {
      if (children[i] instanceof AgentComponentData) {
	writeAgentFile(configDir, (AgentComponentData)children[i]);
      }
    }
  }
  
  private void writeChildrenOfComp(File configDir, ComponentData comp) throws IOException {
    if (comp == null || comp.childCount() == 0)
      return;
    ComponentData[] children = comp.getChildren();
    for (int i = 0; i < children.length; i++) {
      // write out any leaf components
      writeLeafData(configDir, children[i]);
    }
  }
  
  private void writeAgentFile(File configDir, AgentComponentData ac) throws IOException {
    writeChildrenOfComp(configDir, (ComponentData)ac);
    // write any other leaf component data files
    writeLeafData(configDir, (ComponentData)ac);
  }
  
  private void addNodes(Experiment exp) {
    for (int i = 0; i < nodesToWrite.length; i++) {
      ComponentData nc = new GenericComponentData();
      nc.setType(ComponentData.NODE);
      nc.setName(nodesToWrite[i].getShortName());
      nc.setClassName(""); // leave this out?? FIXME
      nc.setOwner(exp); // the experiment? FIXME
      nc.setParent(theSoc);
      ComponentName name = 
	  new ComponentName((BaseComponent)nodesToWrite[i], 
                            "ConfigurationFileName");
      nc.addParameter(nodesToWrite[i].getProperty(name).getValue().toString());
      theSoc.addChild(nc);
      addAgents(nodesToWrite[i], nc);
    }
  }

  private void addAgents(NodeComponent node, ComponentData nc) {
    AgentComponent[] agents = node.getAgents();
    if (agents == null || agents.length == 0)
      return;
    for (int i = 0; i < agents.length; i++) {
      AgentComponentData ac = new AgentComponentData();
      ac.setName(agents[i].getFullName().toString());
      // FIXME!!
      ac.setOwner(null); // the society that contains this agent FIXME!!!
      ac.setParent(nc);
      nc.addChild((ComponentData)ac);
    }
  }

  private void writeLeafData(File configDir, ComponentData me) throws IOException {
    if (me.leafCount() < 1)
      return;
    LeafComponentData[] leaves = me.getLeafComponents();
    for (int i = 0; i < me.leafCount(); i++) {
      LeafComponentData leaf = leaves[i];
      if (leaf == null)
	continue;
      if (!leaf.getType().equals(LeafComponentData.FILE)) {
	System.err.println("Got unknown LeafComponent type: " + leaf.getType());
	continue;
      }
      PrintWriter writer = new PrintWriter(new FileWriter(new File(configDir, leaf.getName())));
      try {
	writer.println(leaf.getValue().toString());
      } catch (Exception e) {
	System.out.println("Error writing config file: " + e);
      }
      finally {
	writer.close();
      }
    } // end of loop over leaves
  } // end of writeLeafData  
  
}

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
package org.cougaar.tools.csmart.ui.experiment;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.Date;
import java.text.DateFormat;

import org.cougaar.tools.server.ConfigurationWriter;

import org.cougaar.tools.csmart.ui.experiment.Experiment;
import org.cougaar.tools.csmart.ui.component.*;


import org.cougaar.tools.csmart.ui.component.ComponentName;
import org.cougaar.tools.csmart.ui.component.ConfigurableComponent;
import org.cougaar.tools.csmart.societies.abcsociety.ABCAgent;
import org.cougaar.tools.csmart.societies.abcsociety.ABCSociety;
import org.cougaar.tools.csmart.scalability.ScalabilityXAgent;
import org.cougaar.tools.csmart.scalability.ScalabilityXSociety;

import java.io.PrintWriter;
import java.io.FileWriter;

// Create a society ComponentData
public class ExpConfigWriterNew implements ConfigurationWriter {
  transient NodeComponent[] nodesToWrite;
  transient List components;
  ComponentData theSoc;

  // Remove when prototypes are fixed.
  private String metricsInitializer = null;

  public ExpConfigWriterNew(List components, NodeComponent[] nodesToWrite, Experiment exp) {
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
      ComponentProperties soc = (ComponentProperties) components.get(i);
      soc.addComponentData(theSoc);
    }
    // Then give everyone a chance to modify what they've collectively produced
    for (int i = components.size() - 1; i >= 0; i--) {
      ComponentProperties soc = (ComponentProperties) components.get(i);
      soc.modifyComponentData(theSoc);
    }    
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
	  new ComponentName((ConfigurableComponent)nodesToWrite[i], "ConfigurationFileName");
      nc.addParameter(((ComponentProperties)nodesToWrite[i]).getProperty(name).getValue().toString());
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

  public void writeConfigFiles(File configDir) throws IOException {
    // Call writeNodeFile for each of the nodes in theSoc.
    ComponentData[] nodes = theSoc.getChildren();
    for (int i = 0; i < theSoc.childCount(); i++) {
      writeNodeFile(configDir, nodes[i]);
    }
  }

  private String writeParam(Object param) {
    // do fancy stuff based on type here??? FIXME!!
    return param.toString();
  }

  private String quote(String string) {
    return "\"" + string + "\"";
  }

  private void writeChildLine(PrintWriter writer, ComponentData me) throws IOException {
    writer.print(me.getName());
    if (me.parameterCount() == 0) {
      writer.println();
      return;
    }
    writer.print("(");
    Object[] params = me.getParameters();
    writer.print(writeParam(params[0]));
    for (int i = 1; i < params.length; i++) {
      writer.print(",");
      // write out each parameter, comma separated
      writer.print(writeParam(params[i]));
    }
    writer.println(")");
    return;
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
  
  private void writeNodeFile(File configDir, ComponentData nc) throws IOException {
   Object[] parameters = nc.getParameters();
   String configFileName = (String)parameters[0] + ".ini";
   PrintWriter writer = new PrintWriter(new FileWriter(new File(configDir, configFileName)));
    try {
    // loop over children
    // if there are binder or such, do those
      ComponentData[] children = nc.getChildren();
      for (int i = 0; i < nc.childCount(); i++) {
	if (children[i] instanceof AgentComponentData) {
	  continue;
	} else if (children[i].getType().equals(ComponentData.NODE) || 
                   children[i].getType().equals(ComponentData.SOCIETY) || 
                   children[i].getType().equals(ComponentData.AGENT) || 
                   children[i].getType().equals(ComponentData.PLUGIN)) {
	  System.err.println("Got unexpected child of Node type: " + children[i]);
	} else {
	  // What is the prefix line I write here?
	  // FIXME!!!!!!
	  // This assumes the name is always the prefix.
	  writer.print(children[i].getName() + " = ");
	  writeChildLine(writer, children[i]);
	  // Could one of these guys have children?
	  writeChildrenOfComp(writer, configDir, children[i]);
	  // write out any leaf components
	  writeLeafData(configDir, children[i]);
	}
      } // end of loop over children
      writer.println("[ Clusters ]");
      children = nc.getChildren();

      // Get the name if the initializer plugin.
      for(int i=0; i < nc.childCount(); i++) {
	if(children[i] instanceof AgentComponentData) {
	  AgentComponentData agent = (AgentComponentData)children[i];
	  ComponentData[] plugins = agent.getChildren();
	  for(int j=0; j < plugins.length; j++) {
	    ComponentData aa = plugins[j];
	    if(aa.getName().equals("org.cougaar.tools.csmart.plugin.MetricsInitializerPlugin")) {
	      metricsInitializer = agent.getName().toString();
	      break;
	    }
	  }
	}
      }

      for (int i = 0; i < nc.childCount(); i++) {
	if (children[i] instanceof AgentComponentData) {
	  writer.print("cluster = ");
	  writeChildLine(writer, children[i]);
	  // Write the children of this agent if there are any
	  // write the leaf components of this agent
	  writeAgentFile(configDir, (AgentComponentData)children[i]);
	} else if (!children[i].getType().equals(ComponentData.NODEBINDER) 
                   || !children[i].getType().equals(ComponentData.AGENTBINDER)) {
	  System.err.println("Got a child of a Node that wasn't an Agent: " + children[i]);
	}
      }
      writer.println();
      writer.println("[ AlpProcess ]");
      writer.println();
      writer.println("[ Policies ]");
      writer.println();
      writer.println("[ Permission ]");
      writer.println();
      writer.println("[ AuthorizedOperation ]");
    } catch (Exception e) {
      System.out.println("Error writing config file: " + e);
    }
    finally {
      writer.close();
    }    
  } // end of writeNodeFile

  private void writeChildrenOfComp(PrintWriter writer, File configDir, ComponentData comp) throws IOException {
    if (comp == null || comp.childCount() == 0)
      return;
    ComponentData[] children = comp.getChildren();
    for (int i = 0; i < children.length; i++) {
      if (writer != null) {
	writer.print(children[i].getType() + " = ");
	writeChildLine(writer, children[i]);
      }
      // Could one of these guys have children?
      writeChildrenOfComp(writer, configDir, children[i]);
      // write out any leaf components
      writeLeafData(configDir, children[i]);
    }
  }

  protected DateFormat myDateFormat = DateFormat.getInstance(); 

  private void writeAgentFile(File configDir, AgentComponentData ac) throws IOException {
    PrintWriter writer = new PrintWriter(new FileWriter(new File(configDir, ac.getName() + ".ini")));
    try {
      writer.println("[ Cluster ]");
      writer.println("class = " + ac.getClassName());
      writer.println("uic = \"" + ac.getName() + "\"");
      writer.println("cloned = false");
      writer.println();
      writer.println("[ PlugIns ]");
      // loop over the children - but what if one is not a PLUGIN?
      // This does no type checking, and writes out all the children here
      writeChildrenOfComp(writer, configDir, (ComponentData)ac);
      writer.println();
      writer.println("[ Policies ]");
      writer.println();
      writer.println("[ Permission ]");
      writer.println();
      writer.println("[ AuthorizedOperation ]");
    } catch (Exception e) {
      System.out.println("Error writing config file: " + e);
    }
    finally {
      writer.close();
    }
    // write the prototype-ini file
    writePrototypeINI(configDir, ac);
    
    // write any other leaf component data files
    writeLeafData(configDir, (ComponentData)ac);

  } // end of writeAgentFile
  
  private void writePrototypeINI(File configDir, AgentComponentData agent) throws IOException {
    PrintWriter writer = new PrintWriter(new FileWriter(new File(configDir, agent.getName() + "-prototype-ini.dat")));

    AgentAssetData assetData = agent.getAgentAssetData();

    try {
      writer.print("[Prototype] ");
      writer.println(assetData.getAssetClass());
      writer.println();

      if(!assetData.isEntity()) {
	writer.print("[UniqueId] ");
	writer.println(quote(assetData.getUniqueID()));
	writer.println();

	if(assetData.getUnitName() != null) {
	  writer.print("[UnitName] ");
	  writer.println(assetData.getUnitName());
	  writer.println();
	}
	
	writer.print("[UIC] ");
	writer.println(quote(assetData.getUIC()));
	writer.println();
      }
      
      // Write Relationships.
      Iterator iter = assetData.getRelationshipIterator();
      writer.println("[Relationship]");
      while(iter.hasNext()) {
	RelationshipData rel = (RelationshipData)iter.next();

	if (assetData.isEntity()) {	  
	  writer.print(quote(rel.getRole()) + "  ");
	  writer.print(quote(rel.getItem()) + "  ");
	  writer.print(quote(rel.getType()) + "  ");
	  writer.print(quote(rel.getSupported()) + "  ");
          long startTime = rel.getStartTime();
          long endTime = rel.getEndTime();
          if (startTime == 0L || endTime == 0L) {
            writer.print(quote("") + "  ");
            writer.println(quote(""));
          } else {
            writer.print(quote(myDateFormat.format(new Date(startTime))) + "  ");
            writer.println(quote(myDateFormat.format(new Date(endTime))));
          }
	} else if(assetData.isOrg()) {
	  writer.print(rel.getType() + " ");
	  writer.print(quote(rel.getSupported()) + " ");
	  writer.println(quote(rel.getRole()));
	} else if(assetData.isTPOrg()){
	  // To Do: Deals with Realtionship.ini file
	} else {
	  throw new RuntimeException("Asset Data Type Must be set: Entity, Org or TPOrg");
	}
      }		    
      
      writer.println();

      iter = assetData.getPropGroupsIterator();
      while(iter.hasNext()) {
	PropGroupData pgData = (PropGroupData)iter.next();

	writer.println("[" + pgData.getName() + "]");
	Iterator iter2 = pgData.getPropertiesIterator();	
	while(iter2.hasNext()) {
	  PGPropData propData = (PGPropData)iter2.next();
	  writer.print(propData.getName() + " ");
	  writer.print(propData.getType());
	  if(propData.isListType()) {
	    writer.print("<" + propData.getSubType() + ">");
	    writer.print(" " + quote(((PGPropMultiVal)propData.getValue()).toString()));
	    writer.println();
	  } else {
	    writer.println(" " + quote((String)propData.getValue()));
	  }
	}
	writer.println();
      }

    } finally {
      writer.close();
    }
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("ExpConfigWriterNew: ");
    buf.append(theSoc.toString());
    return buf.toString();
  }
} // end of ExpConfigWriterNew.java


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

import org.cougaar.tools.server.ConfigurationWriter;

import org.cougaar.tools.csmart.ui.component.NodeComponent;
import org.cougaar.tools.csmart.ui.component.AgentComponent;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;
import org.cougaar.tools.csmart.ui.component.ImpactComponent;
import org.cougaar.tools.csmart.ui.component.GenericComponentData;
import org.cougaar.tools.csmart.ui.experiment.Experiment;
import org.cougaar.tools.csmart.ui.component.ComponentData;
import org.cougaar.tools.csmart.ui.component.ComponentProperties;
import org.cougaar.tools.csmart.ui.component.AgentComponentData;
import org.cougaar.tools.csmart.ui.component.LeafComponentData;
import java.io.PrintWriter;
import java.io.FileWriter;

// Create a society ComponentData
public class ExpConfigWriterNew implements ConfigurationWriter {
  transient NodeComponent[] nodesToWrite;
  transient List components;
  ComponentData theSoc;

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
  
  private void writeChildLine(PrintWriter writer, ComponentData me) throws IOException {
    writer.print(me.getClassName());
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
      if (leaf.getType() != LeafComponentData.FILE) {
	System.err.println("Got unknown LeafComponent type: " + leaf.getType());
	continue;
      }
      System.err.println("Writing leaf data file: " + leaf.getName());
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
    PrintWriter writer = new PrintWriter(new FileWriter(new File(configDir, nc.getName() + ".ini")));
    try {
    // loop over children
    // if there are binder or such, do those
      ComponentData[] children = nc.getChildren();
      for (int i = 0; i < nc.childCount(); i++) {
	if (children[i] instanceof AgentComponentData) {
	  continue;
	} else if (children[i].getType().equals(ComponentData.NODE) || children[i].getType().equals(ComponentData.SOCIETY) || children[i].getType().equals(ComponentData.AGENT) || children[i].getType().equals(ComponentData.PLUGIN)) {
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
      for (int i = 0; i < nc.childCount(); i++) {
	if (children[i] instanceof AgentComponentData) {
	  writer.print("cluster = ");
	  writeChildLine(writer, children[i]);
	  // Write the children of this agent if there are any
	  // write the leaf components of this agent
	  writeAgentFile(configDir, (AgentComponentData)children[i]);
	} else {
	  System.err.println("Got a child of a Node that wasnt an Agent: " + children[i]);
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
    try {
      // FIXME!!!!
      writer.println("This is the prototype ini file");
      // FIXME!!!!!

      // This might look different for Organization or Entities, right?

      // Here is what Scalability does:
//        writer.println("[Prototype] CombatOrganization");
//        writer.println();
//        writer.println("[UniqueId] \"UTC/CombatOrg\"");
//        writer.println();
//        writer.println("[UIC] \"UIC/" + agent.getName() + "\"");
//        writer.println();
//        writer.println("[Relationship]");
//        for (Iterator iter = supporting.iterator(); iter.hasNext(); ) {
//          writer.println("Supporting \"" + ((ScalabilityXAgent) iter.next()).getFullName() + "\" \"ScalabilityProvider\"");
//        }
//        if (superior != null) {
//          writer.println("Supporting \"" + superior.getFullName() + "\" \"ScalabilityControlProvider\"");
//          writer.println("Supporting \"" + superior.getFullName() + "\" \"ScalabilityStatisticsProvider\"");
//          writer.println("Superior  \"" + superior.getFullName() + "\" \"\"");
//        }
//        writer.println();
//        writer.println("[TypeIdentificationPG]");
//        writer.println("TypeIdentification String \"UTC/RTOrg\"");
//        writer.println("Nomenclature String \"" + agent.getName() + "\"");
//        writer.println();
//        writer.println("[ClusterPG]");
//        writer.println("ClusterIdentifier ClusterIdentifier \"" + agent.getName() + "\"");
//        writer.println();
//        writer.println("[OrganizationPG]");
//        writer.println("Roles Collection<Role> \"ScalabilityProvider, ScalabilityControlProvider, ScalabilityStatisticsProvider\"");

      // And here is what ABC Does:

//        writer.println("[Prototype] Entity");
//        writer.println();
//        writer.println("[Relationship]");
//        writer.println("#Role     ItemIdentification      TypeIdentification     Cluster   Start    End");
//        Iterator iter = getAllRoles().iterator();
//        while(iter.hasNext()) {
//  	String role = (String)iter.next();
//  	String[] supplies = (String[])getProperty(PROP_SUPPLIES).getValue();
//  	for(int i=0; i < supplies.length; i++) {
//  	  String type = supplies[i].substring(supplies[i].lastIndexOf(".")+1);
//  	  writer.println("\"" + role + "\"  \"" + supplies[i].trim() + "\"  \"" + type + "\" \"" + supplies[i].trim() 
//  			 + "\" \"\" \"\"");	  
//  	}

//  	// Add MetricPlugin Role.
//  	String initializer = (String)getProperty(PROP_INITIALIZER).getValue();
//  	writer.println("\"MetricsControlProvider\"  \"" + initializer +
//  		       "\"  \"" + initializer.substring(initializer.lastIndexOf(".")+1) + 
//  		       "\"  \"" + initializer + "\"  \"\"  \"\"");
//        }
//        writer.println();
//        writer.println("[ItemIdentificationPG]");
//        writer.println("ItemIdentification String \"" + getFullName().toString() + "\"");
//        // Make these next two be Community#.Customer/Provider#
//        // maybe getName().get(1).toString() or getName().get(1).toString() + "." + getName().get(2).toString()
//        writer.println("Nomenclature String \"" + getFullName().get(1).toString() + "." + getFullName().get(2).toString() + "\"");
//        writer.println("AlternateItemIdentification String \"" + getFullName().get(1).toString() + "." + getFullName().get(2).toString() + "\"");
//        //      writer.println("Nomenclature String \"" + getName().get(2).toString() + "\"");
//        //      writer.println("AlternateItemIdentification String \"" + getName().get(2).toString() + "\"");
//        writer.println();
//        writer.println("[TypeIdentificationPG]");
//        writer.println("TypeIdentification String \"" + getFullName().get(2).toString() + "\"");
//        writer.println("Nomenclature String \"" + getFullName().get(2).toString() + "\"");
//        writer.println("AlternateTypeIdentification String \"" + getFullName().get(2).toString() + "\"");
//        writer.println();
//        writer.println("[ClusterPG]");
//        writer.println("ClusterIdentifier ClusterIdentifier \"" + getFullName().toString() + "\"");
//        writer.println();
//        writer.println("[EntityPG]");
//        writer.print("Roles Collection<Role> ");
//        // Only print the quoted collection of roles if there are any
//        if (! getAllRoles().isEmpty()) {
//  	iter = getAllRoles().iterator();
//  	String role = null;
//  	// No comma before the first role
//  	if (iter.hasNext()) {
//  	  role = (String)iter.next();
//  	  // If this agent is a customer, dont write this as having provider roles
//  	  // say it has the customer equivalent
//  	  if (type.equalsIgnoreCase("customer")) {
//  //  	    if (role.endsWith("rovider"))
//  //  	      role = Role.getRole(role).getConverse().getName();
//  	    // For now, just skip writing out the role
//  	    while (role.endsWith("rovider") && iter.hasNext())
//  	      role = (String)iter.next();
//  	    if (role.endsWith("rovider"))
//  	      role = null;
//  	  }
//  	  if (role != null) {
//  	    writer.print("\"");
//  	    writer.print(role);
//  	  }
//  	}
//  	// For all subsequent roles, do a comma, a space, then the role
//  	while(iter.hasNext()) {
//  	  role = (String)iter.next();
//  	  // If this agent is a customer, dont write this as having provider roles
//  	  // say it has the customer equivalent
//  	  if (type.equalsIgnoreCase("customer"))
//  	    if (role.endsWith("rovider"))
//  	      //	      role = Role.getRole(role).getConverse().getName();
//  	      continue;
//  	  writer.print(", " + role);
//  	  //	writer.print(" \"" + iter.next() + "\" ");
//  	}
//  	// close the list of roles with quotes
//  	writer.print("\"");
//        } // end of printing Collection of Roles
//        writer.println();
//        writer.println();
//        writer.println("[CommunityPG]");
//        writer.println("TimeSpan TimeSpan \"\"");
//        writer.print("Communities    Collection<String> ");
//        writer.println("\"" + getFullName().get(1).toString() + "\"");
      
    } catch (Exception e) {
      System.out.println("Error writing config file: " + e);
    }
    finally {
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


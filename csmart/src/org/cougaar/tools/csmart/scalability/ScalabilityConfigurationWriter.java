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

package org.cougaar.tools.csmart.scalability;

import java.io.*;

import org.cougaar.tools.csmart.ui.component.AgentComponent;
import org.cougaar.tools.csmart.ui.component.ComponentName;
import org.cougaar.tools.csmart.ui.component.ComponentProperties;
import org.cougaar.tools.csmart.ui.component.ConfigurableComponent;
import org.cougaar.tools.csmart.ui.component.NodeComponent;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;
import org.cougaar.tools.server.ConfigurationWriter;

public class ScalabilityConfigurationWriter implements ConfigurationWriter {
  SocietyComponent society;
  NodeComponent[] nodes;
  String nodeFileAddition;

  public ScalabilityConfigurationWriter(SocietyComponent society,
					NodeComponent[] nodes, String nodeFileAddition) {
    this.society = society;
    this.nodes = nodes;
    this.nodeFileAddition = nodeFileAddition;
  }

  public void writeConfigFiles(File configDir) throws IOException {
    ((ScalabilityXSociety)society).finishConfiguration();
    for (int i = 0; i < nodes.length; i++) {
      NodeComponent node = nodes[i];
      //      File nodeFile = 
      //	new File(configDir, node.getShortName() + ".ini");
      // temporary fix to use unique node names to circumvent server problem
      File nodeFile = 
        new File(configDir, 
           ((ComponentProperties)node).getProperty(new ComponentName((ConfigurableComponent)node, "ConfigurationFileName")).getValue() + ".ini");
      PrintWriter writer = new PrintWriter(new FileWriter(nodeFile));
      // Ensure that this node is for my agents only? Or at least no Impact
      // agents?
      // Then, write out this extra line if there is one
      // FIXME!!!
      // As it stands, this means the Generator & Transducer Agents will
      // be bound!!
      if (nodeFileAddition != null)
	writer.println(nodeFileAddition);
      writeConfigFilesForAgents(writer, configDir, node.getAgents());
    }
  }

  private void writeConfigFilesForAgents(PrintWriter writer,
					 File configDir,
					 AgentComponent[] agents) {
//     try {
//       writer.println("[ Clusters ]");
//       for (int i = 0; i < agents.length; i++) {
// 	agents[i].writeIniFile(configDir);
//         writer.println(agents[i].getConfigLine());
// 	if (agents[i] instanceof ScalabilityXAgent) {
// 	  ScalabilityXAgent agent = (ScalabilityXAgent)agents[i];
// 	  //agent.writePrototypeIniFile(configDir);
// 	}
//       }
//       writer.println();
//       writer.println("[ AlpProcess ]");
//       writer.println();
//       writer.println("[ Policies ]");
//       writer.println();
//       writer.println("[ Permission ]");
//       writer.println();
//       writer.println("[ AuthorizedOperation ]");
//     } catch (Exception e) {
//       System.out.println("Error writing config file: " + e);
//     }
//     finally {
//       writer.close();
//     }
  }

}


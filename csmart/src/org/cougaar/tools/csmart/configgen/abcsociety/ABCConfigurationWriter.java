/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.configgen.abcsociety;

import java.io.*;

import org.cougaar.tools.csmart.ui.component.AgentComponent;
import org.cougaar.tools.csmart.ui.component.ConfigurableComponent;
import org.cougaar.tools.csmart.ui.component.NodeComponent;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;
import org.cougaar.tools.server.ConfigurationWriter;

public class ABCConfigurationWriter implements ConfigurationWriter {
  SocietyComponent society;
  NodeComponent[] nodes;
  String nodeFileAddition;

  public ABCConfigurationWriter(SocietyComponent society,
					NodeComponent[] nodes, String nodeFileAddition) {
    this.society = society;
    this.nodes = nodes;
    this.nodeFileAddition = nodeFileAddition;
  }

  public void writeConfigFiles(File configDir) throws IOException {
    // ((ABCSociety)society).finishConfiguration();
    for (int i = 0; i < nodes.length; i++) {
      NodeComponent node = nodes[i];
      File nodeFile = 
	new File(configDir, node.getShortName() + ".ini");
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

    try {
      writer.println("[ Clusters ]");
      for(int i = 0 ; i < agents.length; i++) {
	agents[i].writeIniFile(configDir);
	writer.println(agents[i].getConfigLine());
	if (agents[i] instanceof ABCAgent) {
	  ABCAgent agent = (ABCAgent)agents[i];
	  agent.writePrototypeIniFile(configDir);
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
    }
    catch(IOException e) {}
    finally {
      writer.close();
    }
  }
}

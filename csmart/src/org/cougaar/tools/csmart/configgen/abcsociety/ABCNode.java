/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.configgen.abcsociety;

import org.cougaar.tools.server.ConfigurationWriter;
import org.cougaar.tools.csmart.ui.component.*;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Maintains information about a scalability node and generates the
 * node .ini file.
 **/
public class ABCNode
  extends ConfigurableComponent
  implements Serializable, NodeComponent, ConfigurationWriter
{
  public static final String DEFAULT_NODE_NAME = "DefaultNode";

  private List agents = new ArrayList();

  public ABCNode(String nodeName) {
      super(nodeName);
  }

  public void initProperties() {
  }

  public int getAgentCount() {
    return agents.size();
  }

  public AgentComponent[] getAgents() {
    return (AgentComponent[]) agents.toArray(new AgentComponent[getAgentCount()]);
  }

  public void addAgent(AgentComponent agent) {
    ABCAgent sa = (ABCAgent) agent;
    agents.add(sa);
  }

  public AgentComponent getAgent(int ix) {
    return (AgentComponent) agents.get(ix);
  }

  public void removeAgent(AgentComponent agent) {
    agents.remove(agent);
  }

  public void dispose() {
    agents.clear();
  }

  public void writeConfigFiles(File configDir) {
    File nodeFile = new File(configDir, getShortName() + ".ini");
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(new FileWriter(nodeFile));
      writer.println("[ Clusters ]");
      for (Iterator ci = agents.iterator(); ci.hasNext(); ) {
        ABCAgent agent = (ABCAgent) ci.next();
	agent.writeIniFile(configDir);
	//	agent.writePrototypeIniFile(configDir);
        writer.println(agent.getConfigLine());
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

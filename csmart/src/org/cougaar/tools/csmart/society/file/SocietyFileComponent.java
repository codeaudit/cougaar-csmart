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
package org.cougaar.tools.csmart.society.file;

import java.util.Collection;
import java.util.Iterator;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.tools.csmart.core.cdata.ComponentConnector;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyBase;
import org.cougaar.tools.csmart.society.file.AgentFileComponent;

public class SocietyFileComponent
  extends SocietyBase {

  private String name;
  private ComponentDescription[] desc;
  private String[] filenames = null;

  /**
   * Construct a society from a single file which
   * enumerates the agents in the society.
   * The names of files that define individual agents are assumed
   * to be of the form: <agentname>.ini
   * @param filename the name of the file that enumerates the agents
   */

  public SocietyFileComponent(String filename) {
    super(filename);
    this.name = filename;
  }

  /**
   * Construct a society from files, each of which defines
   * an agent in the society.
   * @param name the name of the society
   * @param filenames the names of the files that define the agents
   */

  public SocietyFileComponent(String name, String[] filenames) {
    super(name);
    this.filenames = filenames;
  }

  public void initProperties() {
    if (filenames == null)
      initFromSingleFile();
    else
      initFromMultiFiles();
  }

  private void initFromSingleFile() {
    String filename = name;
    if(log.isDebugEnabled()) {
      log.debug("Parse File: " + filename);
    }
    desc = ComponentConnector.parseFile(filename);
    if (desc == null)
      return;
    for(int i=0; i < desc.length; i++) {
      String agentName = ComponentConnector.getAgentName(desc[i]);
      if(log.isDebugEnabled()) {
        log.debug("Name: " + agentName + " Class: " + desc[i].getClassname());
      }
      AgentComponent agent = 
        (AgentComponent)new AgentFileComponent(agentName, 
                                               desc[i].getClassname());
      agent.initProperties();
      addChild(agent);
    }
  }

  private void initFromMultiFiles() {
    for (int i = 0; i < filenames.length; i++) {
      if(log.isDebugEnabled()) {
        log.debug("Parse File: " + filenames[i]);
      }
      String agentName = filenames[i];
      if (agentName.endsWith(".ini"))
        agentName = agentName.substring(0, agentName.length()-4);
      AgentComponent agent = 
        (AgentComponent)new AgentFileComponent(agentName, 
                                               "org.cougaar.core.agent.ClusterImpl");
      agent.initProperties();
      addChild(agent);
    }
  }

}
/**
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
 *  </copyright>
 */
package org.cougaar.tools.csmart.core.cdata;

/**
 * AgentComponentDataTest.java
 *
 *
 * Created: Thu Jan 31 15:30:17 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

import junit.framework.*;

public class AgentComponentDataTest extends TestCase {

  public AgentComponentDataTest (String name){
    super(name);
  }

  protected void setUp() {
  }

  private AgentComponentData createAgentComponentData() {
    AgentComponentData agent = new AgentComponentData();

    agent.setName("Agent");
    agent.addChild(createPlugin("Plugin1", 4));
    agent.addChild(createPlugin("Plugin2", 0));
    agent.addChild(createPlugin("Plugin3", 1));

    return agent;
  }

  private GenericComponentData createPlugin(String name, int args) {
    GenericComponentData plugin = new GenericComponentData();
    plugin.setName(name);
    plugin.setType(ComponentData.PLUGIN);
    plugin.setClassName("PluginClass");
    
    for(int i=0; i < args; i++) {
      plugin.addParameter("DummyParam_" + i);
    }
    return plugin;
  }

  public void testGetPluginNames() {
    AgentComponentData agent = createAgentComponentData();
    String [] names = agent.getPluginNames();
    assertEquals("Check amount returned", 3, names.length);
    assertEquals("Check name 1", "Plugin1", names[0]);
    assertEquals("Check name 2", "Plugin2", names[1]);
    assertEquals("Check name 3", "Plugin3", names[2]);
  }

  public void testGetPluginArgs() {
    AgentComponentData agent = createAgentComponentData();
    Object[] args = agent.getPluginArgs("Plugin1");
    assertEquals("Check size", 4, args.length);
    assertEquals("Check value 1", "DummyParam_0", args[0]);
    assertEquals("Check value 4", "DummyParam_3", args[3]);
  }

  public static Test suite() {
    return new TestSuite(AgentComponentDataTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}// AgentComponentDataTest

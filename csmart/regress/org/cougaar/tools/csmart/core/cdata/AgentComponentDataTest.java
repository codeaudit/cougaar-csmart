/**
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
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

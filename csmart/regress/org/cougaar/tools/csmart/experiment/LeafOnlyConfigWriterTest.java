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
package org.cougaar.tools.csmart.experiment;

import junit.framework.*;
import java.io.ByteArrayOutputStream;

import org.cougaar.tools.csmart.core.cdata.*;
import java.io.IOException;
import java.util.Iterator;

/**
 * LeafOnlyConfigWriterTest.java
 *
 *
 * Created: Wed Jan 30 11:46:38 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class LeafOnlyConfigWriterTest extends TestCase {
  private LeafOnlyConfigWriter cw = null;
  private ComponentData society;

  public LeafOnlyConfigWriterTest (String name){
    super(name);
  }

  protected void setUp() {
    society = createComponentData();
    cw = new LeafOnlyConfigWriter(society);
  }

  private ComponentData createComponentData() {
    ComponentData cd = new GenericComponentData();
    cd.setType(ComponentData.SOCIETY);
    cd.setName("TestName");
    cd.setClassName("TestClass");

    GenericComponentData node = new GenericComponentData();
    node.setType(ComponentData.NODE);
    node.setName("Node");
    node.setClassName("TestNode");
    node.addLeafComponent(createDummyLeaf("NodeLeaf"));

    ComponentData agent1 = new AgentComponentData();
    agent1.setName("Agent1");
    agent1.addChild(createPlugin("Plugin1", 4));
    agent1.addChild(createPlugin("Plugin2", 0));
    agent1.addChild(createPlugin("Plugin3", 1));
    agent1.addLeafComponent(createDummyLeaf("Agent1_Leaf"));
    node.addChild(agent1);  

    AgentComponentData agent2 = new AgentComponentData();
    agent2.setName("Agent2");
    agent2.addChild(createPlugin("Plugin1", 0));
    agent2.addChild(createPlugin("Plugin2", 0));
    agent2.addChild(createPlugin("Plugin3", 3));
    node.addChild(agent2);  

    cd.addChild(node);

    return cd;
  }

  private GenericComponentData createPlugin(String name, int leafCnt) {
    GenericComponentData plugin = new GenericComponentData();
    plugin.setName(name);
    plugin.setType(ComponentData.PLUGIN);
    plugin.setClassName("PluginClass");
    
    for(int i=0; i < leafCnt; i++) {
      plugin.addLeafComponent(createDummyLeaf(name+"_Leaf_"+i));
    }

    return plugin;
  }

  private LeafComponentData createDummyLeaf(String name) {
    GenericLeafComponentData dummy = new GenericLeafComponentData();
    dummy.setType(LeafComponentData.FILE);
    dummy.setName(name);
    dummy.setValue(new String("This is the contents of the " + name + " leaf"));
    return dummy;
  }

  /**
   * Tests finding the leaf component on a Node.
   * (Can a node even have a leaf component?)
   *
   */
//   public void testNodeLeaf() {
//     ByteArrayOutputStream fos = new ByteArrayOutputStream();

//     try {
//       cw.writeFile("NodeLeaf_Leaf", fos);
//     } catch(Exception e) {
//       fail("Caught Unexpected Exception" + e);
//     }

//     String contents = fos.toString();
//     assertEquals("Test Node Leaf", 
//                  "This is the contents of the NodeLeaf_Leaf leaf",
//                  contents);

//     try {
//       fos.close();
//     } catch(IOException ioe) {
//       fail("Caught Unexpected Excpetion" + ioe);
//     }

//   }


  public void testAgentLeaf() {
    ByteArrayOutputStream fos = new ByteArrayOutputStream();

    try {
      cw.writeFile("Agent1_Leaf", fos);
    } catch(Exception e) {
      fail("Caught Unexpected Exception" + e);
    }

    String contents = fos.toString();
    assertEquals("Test Node Leaf", 
                 "This is the contents of the Agent1_Leaf leaf",
                 contents);

    try {
      fos.close();
    } catch(IOException ioe) {
      fail("Caught Unexpected Excpetion" + ioe);
    }
    
  }

  public void testPluginLeaf() {
    ByteArrayOutputStream fos = new ByteArrayOutputStream();
    Iterator iter = cw.getFileNames();

    try {
      cw.writeFile("Plugin3_Leaf_1", fos);
    } catch(Exception e) {
      fail("Caught Unexpected Exception" + e);
    }

    String contents = fos.toString();
    assertEquals("Test Node Leaf", 
                 "This is the contents of the Plugin3_Leaf_1 leaf",
                 contents);

    try {
      fos.close();
    } catch(IOException ioe) {
      fail("Caught Unexpected Excpetion" + ioe);
    }
  }

}// LeafOnlyConfigWriterTest

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
package org.cougaar.tools.csmart.ui.component;

import junit.framework.*;

/**
 * Unit test for class ConfigurableComponent.
 */
public class ConfigurableComponentTest extends TestCase {
  
  private SimpleComponent sc = null;

  public ConfigurableComponentTest(String name) {
    super(name);
  }

  protected void setUp() {
    sc = new SimpleComponent();
  }


  public void testAddChild() {
    assertEquals("Test addChild()", 0, sc.addChild(new SimpleComponent("Child")));         	 
  }

  public void testRemoveChild() {
    SimpleComponent child = new SimpleComponent("Child");
    sc.addChild(child);
    sc.removeChild(0);
    assertEquals("Test removeChild(int)", 0, sc.getChildCount());

    sc.addChild(child);
    sc.removeChild(child);
    assertEquals("Test removeChild(ConfigurableComponent", 0, sc.getChildCount());
  }

  public void testRemoveAllChildren() {
    sc.addChild(new SimpleComponent("Child1"));
    sc.addChild(new SimpleComponent("Child2"));
    sc.addChild(new SimpleComponent("Child3"));
    sc.addChild(new SimpleComponent("Child4"));

    sc.removeAllChildren();
    assertEquals("Test removeAllChildren()", 0, sc.getChildCount());
  }

  public void testGetChildCount() {
    sc.addChild(new SimpleComponent("Child1"));
    assertEquals("1. Test getChildCount()", 1, sc.getChildCount());
    sc.addChild(new SimpleComponent("Child2"));
    assertEquals("2. Test getChildCount()", 2, sc.getChildCount());
    sc.addChild(new SimpleComponent("Child3"));
    assertEquals("3. Test getChildCount()", 3, sc.getChildCount());
    sc.addChild(new SimpleComponent("Child4"));
    assertEquals("4. Test getChildCount()", 4, sc.getChildCount());
  }

  public void testgetChild() {
    SimpleName name = new SimpleName("ChildName");
    SimpleComponent tmp = new SimpleComponent("ChildName");

    sc.addChild(tmp);
    assertEquals("Test getChild(int)", tmp, sc.getChild(0));
 
    assertEquals("Test getChild(CompositeName)", tmp, sc.getChild(name));
  }

  public void testaddProperty() {
    ConfigurableComponentProperty ccp = new ConfigurableComponentProperty(new SimpleComponent(), "Prop1", new Integer(1));
    sc.addProperty(ccp);
    assertEquals("Test addProperty", ccp, sc.getProperty(ccp.getName()));
  }

  public static Test suite() {
    return new TestSuite(ConfigurableComponentTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public class SimpleComponent extends ConfigurableComponent {
    public SimpleComponent() {
      super("SimpleComponent");
    }

    public SimpleComponent(String name) {
      super(name);
    }

    public void initProperties() {
    }    
  }

}

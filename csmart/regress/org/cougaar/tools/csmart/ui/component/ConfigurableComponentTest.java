/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
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

  public void testName() {
    sc.setName("MyName");
    assertEquals("Test getName()", new SimpleName("MyName"), sc.getName());
  }

  public void testAddChild() {
    assertEquals("Test addChild()", 1, sc.addChild(new SimpleComponent("Child")));         	 
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

  public static Test suite() {
    return new TestSuite(LongRangeTest.class);
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

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
 * Unit test for class ComponentName.
 */
public class ComponentNameTest extends TestCase {

  private ComponentName tst = null;
  private String componentName = "Name";
  private SimpleComponent sc = null;

  public ComponentNameTest(String name) {
    super(name);
  }

  protected void setUp() {    
    sc = new SimpleComponent(componentName);
    sc.initProperties();

    tst = new ComponentName(sc, componentName);
  }

  protected void tearDown() {
  }

  public void testGetConfigurableComponent() {
    assertEquals(sc, tst.getConfigurableComponent());
  }

  public void testSetComponent() {
    SimpleComponent nc = new SimpleComponent("New Component");
    tst.setComponent(nc);
    assertEquals(nc, tst.getConfigurableComponent());
  }

  public static Test suite() {
    return new TestSuite(ComponentNameTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public class SimpleComponent extends ConfigurableComponent {
    public SimpleComponent(String name) {
      super(name);
    }
    public void initProperties() {
    }
  }
}

/* 
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.core.property.name;

import junit.framework.*;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;

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

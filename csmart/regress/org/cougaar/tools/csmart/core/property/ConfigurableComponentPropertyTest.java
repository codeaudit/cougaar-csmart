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
package org.cougaar.tools.csmart.core.property;

import junit.framework.*;
import java.util.HashSet;
import java.util.ArrayList;
import java.net.URL;

import org.cougaar.tools.csmart.core.property.name.SimpleName;
import org.cougaar.tools.csmart.core.property.name.ConcatenatedName;

/**
 * Unit test for class ConfigurableComponentProperty.
 */
public class ConfigurableComponentPropertyTest extends TestCase {

  private SimpleComponent sc = null;
  private ConfigurableComponentProperty tst = null;
  private ConfigurableComponentProperty ccp = null;
  private ConfigurableComponentProperty ccp2 = null;

  public ConfigurableComponentPropertyTest(String name) {
    super(name);
  }

  protected void setUp() {    
    sc = new SimpleComponent("Component");
    ccp = new ConfigurableComponentProperty(sc, "Component", new String("obj"));
    ccp2 = new ConfigurableComponentProperty(sc, "Component", ccp);
  }

  protected void tearDown() {
  }

  public void testPropertyClass() {
    ccp2.setPropertyClass(ConfigurableComponentProperty.class);
    assertEquals("Test getPropertyClass()", ConfigurableComponentProperty.class, ccp2.getPropertyClass());
  }

   public void testLabel() {
    String expected = "Label";
    ccp.setLabel(expected);
    assertEquals("Test getLabel()", expected, ccp.getLabel());
  }

  public void testDefaultValue() {
    Integer expected = new Integer(1);
    ccp.setDefaultValue(expected);
    assertEquals("Test defaultValue()", expected, ccp.getDefaultValue());
  }

  public void testValue() {
    Integer expected = new Integer(1);
    ccp.setValue(expected);
    assertEquals("Test value()", expected, ccp.getValue());
  }

  public void testExperimentValues() {
    ArrayList al = new ArrayList();
    al.add(new Integer(1));
    ccp.setExperimentValues(al);
    assertEquals("Test getExperimentValues()", al, ccp.getExperimentValues());
  }

  public void testAllowedValues() {
    HashSet hs = new HashSet();
    hs.add(new Integer(1));
    ccp.setAllowedValues(hs);
    assertEquals("Test getAllowedValues()", hs, ccp.getAllowedValues());
  }

  public void testName() {
    ConcatenatedName cn = new ConcatenatedName(new SimpleName("Component"), "Component");
    assertEquals("Test getName()", cn, ccp.getName());
  }

  public void testValueSet() {
    ccp.setValue(new Integer(34));
    assertTrue("Test isValueSet()", ccp.isValueSet());
  }

  public static Test suite() {
    return new TestSuite(ConfigurableComponentPropertyTest.class);
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

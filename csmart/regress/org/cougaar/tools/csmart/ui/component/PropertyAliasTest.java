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
import java.util.HashSet;
import java.util.ArrayList;
import java.net.URL;

/**
 * Unit test for class PropertyAlias.
 */
public class PropertyAliasTest extends TestCase {

  private SimpleComponent sc = null;
  private PropertyAlias tst = null;
  private ConfigurableComponentProperty ccp = null;

  public PropertyAliasTest(String name) {
    super(name);
  }

  protected void setUp() {    
    sc = new SimpleComponent("Component");
    ccp = new ConfigurableComponentProperty(sc, "Component", new String("obj"));
    tst = new PropertyAlias(sc, "Name", ccp);
  }

  protected void tearDown() {
  }

  public void testPropertyClass() {
    tst.setPropertyClass(ConfigurableComponentProperty.class);
    assertEquals("Test getPropertyClass()", ConfigurableComponentProperty.class, tst.getPropertyClass());
  }

   public void testLabel() {
    String expected = "Label";
    tst.setLabel(expected);
    assertEquals("Test getLabel()", expected, tst.getLabel());
  }

  public void testDefaultValue() {
    Integer expected = new Integer(1);
    tst.setDefaultValue(expected);
    assertEquals("Test defaultValue()", expected, tst.getDefaultValue());
  }

  public void testValue() {
    Integer expected = new Integer(1);
    tst.setValue(expected);
    assertEquals("Test value()", expected, tst.getValue());
  }

  public void testExperimentValues() {
    ArrayList al = new ArrayList();
    al.add(new Integer(1));
    tst.setExperimentValues(al);
    assertEquals("Test getExperimentValues()", al, tst.getExperimentValues());
  }

  public void testAllowedValues() {
    HashSet hs = new HashSet();
    hs.add(new Integer(1));
    tst.setAllowedValues(hs);
    assertEquals("Test getAllowedValues()", hs, tst.getAllowedValues());
  }

  public void testName() {
    ConcatenatedName cn = new ConcatenatedName(new SimpleName("Component"), "Name");
    assertEquals("Test getName()", cn, tst.getName());
  }

  public void testValueSet() {
    tst.setValue(new Integer(34));
    assertTrue("Test isValueSet()", tst.isValueSet());
  }

  public void testToolTip() {
    tst.setToolTip("Tool Tip");
    assertEquals("Test getToolTip()", "Tool Tip", tst.getToolTip());
  }

  public void testHelp() {
    URL url = null;
    try {
      url = new URL("http://www.bbn.com");
    } catch(Exception e) {}

    tst.setHelp(url);
    assertEquals("Test getHelp()", url, tst.getHelp());
  }

  public static Test suite() {
    return new TestSuite(PropertyAliasTest.class);
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

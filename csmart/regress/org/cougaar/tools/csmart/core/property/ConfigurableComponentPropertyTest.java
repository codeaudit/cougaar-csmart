/* 
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

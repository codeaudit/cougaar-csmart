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
import java.util.Iterator;

public class GenericLeafComponentDataTest extends TestCase {

  private GenericLeafComponentData data = null;

  public GenericLeafComponentDataTest(String name) {
    super(name);
  }

  protected void setUp() {
    data = new GenericLeafComponentData();
  }

  public void testType() {
    data.setType(LeafComponentData.FILE);
    assertEquals("Test Type", LeafComponentData.FILE, data.getType());
  }

  public void testName() {
    data.setName("Name");
    assertEquals("Test Name", "Name", data.getName());
  }

  public void testValue() {
    data.setValue(new String("Value"));
    assertEquals("Test Value", new String("Value"), (String)data.getValue());
  }

  public static Test suite() {
    return new TestSuite(GenericLeafComponentDataTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}

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
 * Unit test for class FloatRange.
 */
public class FloatRangeTest extends TestCase {

  private FloatRange fr = null;
  private float minimum = 0.0F;
  private float maximum = 10.0F;

  public FloatRangeTest(String name) {
    super(name);
  }

  protected void setUp() {    
    fr = new FloatRange(minimum, maximum);
  }

  protected void tearDown() {
  }

  public void testGetMinimumValue() {
    assertEquals("Test getMinimunValue", minimum, ((Float)fr.getMinimumValue()).floatValue(), 0.0);
  }
 
  public void testGetMaximumValue() {
    assertEquals("Test getMaximumValue", maximum, ((Float)fr.getMaximumValue()).floatValue(), 0.0);
  }

  public void testIsInRange() {
    assertTrue("Test isInRange()", fr.isInRange(new Float(8.5)));
    assertTrue("Test isInRange()", fr.isInRange(new Float(0.01)));
    assertTrue("Test isInRange()", !fr.isInRange(new Integer(5)));
    assertTrue("Test isInRange()", !fr.isInRange(new Float(10.1)));
    assertTrue("Test isInRange()", !fr.isInRange(new String("Hello")));
  }

  public void testToString() {
    String expect = new String(minimum + ":" + maximum);
    assertEquals("Test toString()", expect, fr.toString());
  }

  public static Test suite() {
    return new TestSuite(FloatRangeTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
}

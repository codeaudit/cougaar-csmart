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
 * Unit test for class DoubleRange
 */
public class DoubleRangeTest extends TestCase {

  private DoubleRange dr = null;
  private double minimum = 0.0;
  private double maximum = 10.0;

  public DoubleRangeTest(String name) {
    super(name);
  }

  protected void setUp() {    
    dr = new DoubleRange(minimum, maximum);
  }

  protected void tearDown() {
  }

  public void testGetMinimumValue() {
    assertEquals("Test getMinimunValue", minimum, ((Double)dr.getMinimumValue()).doubleValue(), 0.0);
  }
 
  public void testGetMaximumValue() {
    assertEquals("Test getMaximumValue", maximum, ((Double)dr.getMaximumValue()).doubleValue(), 0.0);
  }

  public void testIsInRange() {
    assertTrue("Test isInRange()", dr.isInRange(new Double(8.5)));
    assertTrue("Test isInRange()", dr.isInRange(new Double(0.01)));
    assertTrue("Test isInRange()", !dr.isInRange(new Integer(5)));
    assertTrue("Test isInRange()", !dr.isInRange(new Double(10.1)));
    assertTrue("Test isInRange()", !dr.isInRange(new String("Hello")));
  }

  public void testToString() {
    String expect = new String(minimum + ":" + maximum);
    assertEquals("Test toString()", expect, dr.toString());
  }

  public static Test suite() {
    return new TestSuite(DoubleRangeTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
}

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
 * Unit test for class IntegerRange.
 */
public class IntegerRangeTest extends TestCase {

  private IntegerRange ir = null;
  private int minimum = 0;
  private int maximum = 10;

  public IntegerRangeTest(String name) {
    super(name);
  }

  protected void setUp() {    
    ir = new IntegerRange(minimum, maximum);
  }

  protected void tearDown() {
  }

  public void testGetMinimumValue() {
    assertEquals("Test getMinimunValue", minimum, ((Integer)ir.getMinimumValue()).intValue());
  }
 
  public void testGetMaximumValue() {
    assertEquals("Test getMaximumValue", maximum, ((Integer)ir.getMaximumValue()).intValue());
  }

  public void testIsInRange() {
    assertTrue("Test isInRange()", ir.isInRange(new Integer(8)));
    assertTrue("Test isInRange()", ir.isInRange(new Integer(1)));
    assertTrue("Test isInRange()", !ir.isInRange(new Float(5.2)));
    assertTrue("Test isInRange()", !ir.isInRange(new Float(10.1)));
    assertTrue("Test isInRange()", !ir.isInRange(new String("Hello")));
  }

  public void testToString() {
    String expect = new String(minimum + ":" + maximum);
    assertEquals("Test toString()", expect, ir.toString());
  }

  public static Test suite() {
    return new TestSuite(IntegerRangeTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
}

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
 * Unit test for class LongRange.
 */
public class LongRangeTest extends TestCase {

  private LongRange lr = null;
  private long minimum = 0;
  private long maximum = 10;

  public LongRangeTest(String name) {
    super(name);
  }

  protected void setUp() {    
    lr = new LongRange(minimum, maximum);
  }

  protected void tearDown() {
  }

  public void testGetMinimumValue() {
    assertEquals("Test getMinimunValue", minimum, ((Long)lr.getMinimumValue()).longValue());
  }
 
  public void testGetMaximumValue() {
    assertEquals("Test getMaximumValue", maximum, ((Long)lr.getMaximumValue()).longValue());
  }

  public void testIsInRange() {
    assert("Test isInRange()", lr.isInRange(new Long(8)));
    assert("Test isInRange()", lr.isInRange(new Long(1)));
    assert("Test isInRange()", !lr.isInRange(new Float(5.2)));
    assert("Test isInRange()", !lr.isInRange(new Float(10.1)));
    assert("Test isInRange()", !lr.isInRange(new String("Hello")));
  }

  public void testToString() {
    String expect = new String(minimum + ":" + maximum);
    assertEquals("Test toString()", expect, lr.toString());
  }

  public static Test suite() {
    return new TestSuite(LongRangeTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
}

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
 * Unit test for class StringRange.
 */
public class StringRangeTest extends TestCase {

  private StringRange tst = null;
  private String testString = "Test String";

  public StringRangeTest(String name) {
    super(name);
  }

  protected void setUp() {    
    tst = new StringRange(testString);
  }

  protected void tearDown() {
  }

  public void testGetMinimumValue() {
    assertEquals("Test getMinimunValue", testString, (String)tst.getMinimumValue());
  }
 
  public void testGetMaximumValue() {
    assertEquals("Test getMaximumValue", testString, (String)tst.getMaximumValue());
  }

  public void testIsInRange() {
    assertTrue("Test isInRange()", tst.isInRange(testString));
    assertTrue("Test isInRange()", !tst.isInRange(new String("Hello")));
  }

  public void testToString() {
    assertEquals("Test toString()", testString, tst.toString());
  }

  public static Test suite() {
    return new TestSuite(StringRangeTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
}

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
package org.cougaar.tools.csmart.core.property.range;

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

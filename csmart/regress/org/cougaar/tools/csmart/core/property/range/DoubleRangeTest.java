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
package org.cougaar.tools.csmart.core.property.range;

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

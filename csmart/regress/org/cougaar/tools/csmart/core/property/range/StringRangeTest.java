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

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
package org.cougaar.tools.csmart.util;

import junit.framework.*;

/**
 * Unit test for class ArgValue.
 */
public class ArgValueTest extends TestCase {
  
  private ArgValue av = null;
  private ArgValue str = null;

  public ArgValueTest(String name) {
    super(name);
  }

  protected void setUp() {
    av = new ArgValue("Value", new Integer(34));
    str = new ArgValue("String:strval", ":");
  }

  public void testConstruction() {
    try {
      ArgValue tmp = new ArgValue(null, new String());
      fail("Expected IllegalArgumentException!");
    } catch(IllegalArgumentException e) {}

    try {
      ArgValue tmp = new ArgValue(null, ":");
      fail("Expected IllegalArgumentException!");
    } catch(IllegalArgumentException e) {}

    try {
      ArgValue tmp = new ArgValue("This,Test", null);
      fail("Expected IllegalArgumentException!");
    } catch(IllegalArgumentException e) {}

    try {
      ArgValue tmp = new ArgValue("Thistest", ":");
      fail("Expected IllegalArgumentException!");
    } catch(IllegalArgumentException e) {}

    try {
      ArgValue tmp = new ArgValue(":test", ":");
      fail("Expected IllegalArgumentException!");
    } catch(IllegalArgumentException e) {}
  }

  public void testGetArg() {
    assertEquals("1. Test getArg()", "Value", av.getArg());
    assertEquals("2. Test getArg()", "String", str.getArg());
  }

  public void testGetValue() {
    assertEquals("1. Test getValue()", new Integer(34), av.getValue());
    assertEquals("2. Test getValue()", "strval", str.getValue());
  }
  
  public void testSetValue() {
    av.setValue(new String("New Val"));
    assertEquals("Test setValue", "New Val", av.getValue());
  }

  public void testEquals() {
    ArgValue av1 = new ArgValue("Value1:val1", ":");
    ArgValue av2 = new ArgValue("Value1:val1", ":");

    assertTrue("Test equals()", av1.equals(av2));
  }

  public static Test suite() {
    return new TestSuite(ArgValueTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }  
}

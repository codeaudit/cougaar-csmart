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

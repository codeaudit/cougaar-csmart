/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
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

    assert("Test equals()", av1.equals(av2));
  }

  public static Test suite() {
    return new TestSuite(ArgValueTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }  
}

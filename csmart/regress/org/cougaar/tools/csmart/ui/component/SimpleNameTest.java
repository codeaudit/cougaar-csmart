/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.ui.component;

import junit.framework.*;

/**
 * Unit test for class SimpleName.
 */
public class SimpleNameTest extends TestCase {

  private SimpleName tst = null;
  private String name = "SimpleName";

  public SimpleNameTest(String name) {
    super(name);
  }

  protected void setUp() {    
    tst = new SimpleName(name);
  }

  protected void tearDown() {
  }

  public void testSize() {
    assertEquals("Test size()", 1, tst.size());
  }

  public void testGet() {
    assertEquals("Test get()", new SimpleName(name), tst.get(0));

    // Test Exceptions
    try {
      tst.get(4);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}
  }

  public void testGetPrefix() {
    assertNull("Test getPrefix()", tst.getPrefix());
  }

  public void testLast() {
    assertEquals("Test last()", new SimpleName(name), tst.last());
  }

  public void testEndsWith() {
    assertTrue("Test endsWith()", tst.endsWith(new SimpleName(name)));
    assertTrue("Test endsWith()", !tst.endsWith(new SimpleMulti(name)));
  }

  public void testStartsWith() {
    assertTrue("Test startsWith()", tst.startsWith(new SimpleName(name)));
    assertTrue("Test startsWith()", !tst.startsWith(new SimpleMulti(name)));    
  }

  public void testEquals() {
    assertTrue("Test equals()", tst.equals(new SimpleName(name)));
  }

  public void testCompareTo() {
    assertTrue("Test compareTo()", (tst.compareTo(new SimpleName(name)) == 0));
    assertTrue("Test compareTo()", (tst.compareTo(new SimpleMulti(name)) > 0));
    assertTrue("Test compareTo()", (tst.compareTo(new SimpleName("a")) < 0));
  }

  public static Test suite() {
    return new TestSuite(SimpleNameTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public class SimpleMulti extends MultiName {
    public SimpleMulti(String name) {
      super(new SimpleName(name));
    }
    
    protected CompositeName getParentName() {
      return new SimpleName("Parent");
    }
  }

}

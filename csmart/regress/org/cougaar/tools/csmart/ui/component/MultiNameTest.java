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
 * Unit test for class MultiName.
 */
public class MultiNameTest extends TestCase {

  private SimpleMultiName tst = null;
  private NoParentMultiName noParent = null;
  private String componentName = "Child";
  private String noParentName = "NoParent";

  public MultiNameTest(String name) {
    super(name);
  }

  protected void setUp() {    
    tst = new SimpleMultiName(componentName);
    noParent = new NoParentMultiName(noParentName);
  }

  protected void tearDown() {
  }


  public void testSize() {
    assertEquals("Test size() [2]", 2, tst.size());
    assertEquals("Test size() [1]", 1, noParent.size());
  }

  public void testGet() {
    assertEquals("Test get()", new SimpleName("Parent"), tst.get(0));
    assertEquals("Test get()", new SimpleName(componentName), tst.get(1));

    // Test Exceptions
    try {
      tst.get(15);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    try {
      tst.get(-5);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}
  }

  public void testGetPrefix() {
    assertEquals("Test getPrefix()", new SimpleName("Parent"), tst.getPrefix());
    assertNull(noParent.getPrefix());
  }

  public void testGetPrefixN() {

    assertEquals("Test getPrefix(int)", new SimpleName("Parent"), tst.getPrefix(0));
    
    // Test Exceptions
    try {
      tst.getPrefix(15);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}
    
    try {
      tst.getPrefix(-5);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}
  }

  public void testLast() {
    assertEquals("Test last()", new SimpleName(componentName), tst.last());
  }

  public void testEndsWith() {
    assertTrue("Test endsWith()", tst.endsWith(new SimpleName(componentName)));
  }

  public void testStartsWith() {
    assertTrue("Test starsWith()", tst.startsWith(new SimpleName("Parent")));
  }

  public void testEquals() {
    assertTrue("Test equals()", tst.equals(tst));
  }

  public void testCompareTo() {
    assertTrue("Test compareTo() [0]", (tst.compareTo(new SimpleMultiName(componentName)) == 0));
    SimpleName sn = new SimpleName("Testing");
    assertTrue("Test compareTo() [>0]", (tst.compareTo(new SimpleMultiName(sn)) > 0));
    assertTrue("Test compareTo() [<0]", (tst.compareTo(new SimpleName("a")) < 0));
  }

  public static Test suite() {
    return new TestSuite(MultiNameTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public class SimpleMultiName extends MultiName {    
    private SimpleName parent = null;

    public SimpleMultiName(String name) {
      super(new SimpleName(name));
      parent = new SimpleName("Parent");
    }

    public SimpleMultiName(SimpleName name) {
      super(name);
      parent = new SimpleName("Different Name");
    }

    protected CompositeName getParentName() {
      return parent;
    }
  }

  public class NoParentMultiName extends MultiName {
    public NoParentMultiName(String name) {
      super(new SimpleName(name));
    }

    protected CompositeName getParentName() {
      return null;
    }
  }

}

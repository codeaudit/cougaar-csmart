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
    assert("Test endsWith()", tst.endsWith(new SimpleName(componentName)));
  }

  public void testStartsWith() {
    assert("Test starsWith()", tst.startsWith(new SimpleName("Parent")));
  }

  public void testEquals() {
    assert("Test equals()", tst.equals(tst));
  }

  public void testCompareTo() {
    assert("Test compareTo() [0]", (tst.compareTo(new SimpleMultiName(componentName)) == 0));
    SimpleName sn = new SimpleName("Testing");
    assert("Test compareTo() [>0]", (tst.compareTo(new SimpleMultiName(sn)) > 0));
    assert("Test compareTo() [<0]", (tst.compareTo(new SimpleName("a")) < 0));
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

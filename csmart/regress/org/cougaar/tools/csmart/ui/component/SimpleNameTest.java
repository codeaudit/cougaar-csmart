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

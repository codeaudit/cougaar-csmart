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
 * Unit test for class ConcatenatedName.
 */
public class ConcatenatedNameTest extends TestCase {

  private ConcatenatedName tst = null;

  public ConcatenatedNameTest(String name) {
    super(name);
  }

  protected void setUp() {    
    tst = new ConcatenatedName(new SimpleName("name"), "tail");
  }

  protected void tearDown() {
  }

  public void testGetParentName() {
    assertEquals("Test getParentName()", new SimpleName("name"), tst.getParentName());
  }

  public static Test suite() {
    return new TestSuite(ConcatenatedNameTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
}

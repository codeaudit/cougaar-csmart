/**
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
package org.cougaar.tools.csmart.core.property.name;

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

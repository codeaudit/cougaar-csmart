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
package org.cougaar.tools.csmart.core.property.name;

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

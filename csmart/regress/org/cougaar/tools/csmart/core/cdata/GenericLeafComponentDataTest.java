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

package org.cougaar.tools.csmart.core.cdata;

import junit.framework.*;
import java.util.Iterator;

public class GenericLeafComponentDataTest extends TestCase {

  private GenericLeafComponentData data = null;

  public GenericLeafComponentDataTest(String name) {
    super(name);
  }

  protected void setUp() {
    data = new GenericLeafComponentData();
  }

  public void testType() {
    data.setType(LeafComponentData.FILE);
    assertEquals("Test Type", LeafComponentData.FILE, data.getType());
  }

  public void testName() {
    data.setName("Name");
    assertEquals("Test Name", "Name", data.getName());
  }

  public void testValue() {
    data.setValue(new String("Value"));
    assertEquals("Test Value", new String("Value"), (String)data.getValue());
  }

  public void testEquals() {
    GenericLeafComponentData c1 = new GenericLeafComponentData();
    GenericLeafComponentData c2 = new GenericLeafComponentData();
    GenericLeafComponentData c3 = new GenericLeafComponentData();

    c1.setType("My Type");
    c2.setType("My Type");

    c1.setName("Name");
    c2.setName("Name");
    c3.setName("Name");

    c1.setValue(new Integer(1));
    c2.setValue(new Integer(1));
    c3.setValue(new Integer(1));

    assertEquals("Test Equals Operator", true, c1.equals(c2));
    assertEquals("Test Equals Operator", false, c1.equals(c3));

  }

  public static Test suite() {
    return new TestSuite(GenericLeafComponentDataTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}

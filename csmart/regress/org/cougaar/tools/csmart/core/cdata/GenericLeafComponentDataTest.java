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

  public static Test suite() {
    return new TestSuite(GenericLeafComponentDataTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}

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
package org.cougaar.tools.csmart.configgen;

import java.util.ArrayList;
import junit.framework.*;

public class CustomerTaskTest extends TestCase {

  private CustomerTask customer = new CustomerTask();

  public CustomerTaskTest(String name) {
    super(name);
  }
  
  public void testTaskName() {
    customer.setTaskName("TaskName");
    
    try {
      customer.setTaskName("TaskName");
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertEquals("TaskName", customer.getTaskName());
  }

  public void testVital() {
    customer.setVital(0.4);
    
    assertEquals(0.4, customer.getVital(), 0.01);
  }

  public void testDuration() {
    customer.setDuration(10);
    
    assertEquals(10, customer.getDuration());
  }

  public void testChaos() {
    customer.setChaos(25);

    try {
      customer.setChaos(25);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertEquals(25, customer.getChaos());
  }

  public static Test suite() {
    return new TestSuite(CustomerTaskTest.class);
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
} // CustomerTest

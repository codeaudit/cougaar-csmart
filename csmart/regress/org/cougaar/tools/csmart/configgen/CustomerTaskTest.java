/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
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

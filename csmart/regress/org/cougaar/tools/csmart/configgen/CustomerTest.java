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
/**
 * CustomerTest.java
 *
 *
 * Created: Thu Feb 22 11:33:14 2001
 *
 * @author Brian Krisler
 * @version 1.0
 */

import java.util.ArrayList;
import junit.framework.*;

public class CustomerTest extends TestCase {

  private Customer customer = new Customer();

  public CustomerTest(String name) {
    super(name);
  }
  
  public void testName() {
    customer.setName("Name");
    
    try {
      customer.setName("Name");
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertEquals("Name", customer.getName());
  }

  public void testFactor() {
    customer.setFactor(10);

    try {
      customer.setFactor(10);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}
    
    assertEquals(10, customer.getFactor());
  }

  public void testAssets() {
    ArrayList alist = new ArrayList(1);
    alist.add("Asset");

    customer.setAssets(alist);

    try {
      customer.setAssets(alist);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertNotNull(customer.getAssets());
    ArrayList blist = customer.getAssets();
    assertEquals("Asset", blist.get(0));
    
  }

  public void testVital() {
    customer.setVital(0.4);
    
    try {
      customer.setVital(0.4);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}
    
    assertEquals(0.4, customer.getVital(), 0.01);
  }

  public void testDuration() {
    customer.setDuration(10);
    
    try {
      customer.setDuration(10);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}
    
    assertEquals(10, customer.getDuration());
  }

  public void testDeviation() {
    customer.setDeviation(25);

    try {
      customer.setDeviation(25);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertEquals(25, customer.getDeviation());
  }

  public static Test suite() {
    return new TestSuite(CustomerTest.class);
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
} // CustomerTest

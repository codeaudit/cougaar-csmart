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

package org.cougaar.tools.csmart.ui.component;

import junit.framework.*;
import java.util.Iterator;

public class RelationshipTimePhasedDataTest extends TestCase {

  private RelationshipTimePhasedData data = null;

  public RelationshipTimePhasedDataTest(String name) {
    super(name);
  }

  protected void setUp() {
    data = new RelationshipTimePhasedData();
  }

  public void testRole() {
    data.setRole("Role");
    assertEquals("Test Role", "Role", data.getRole());
  }

  public void testItem() {
    data.setItem("Item");
    assertEquals("Test Item", "Item", data.getItem());
  }

  public void testType() {
    data.setType("Type");
    assertEquals("Test Type", "Type", data.getType());
  }

  public void testCluster() {
    data.setCluster("Cluster");
    assertEquals("Test Cluster", "Cluster", data.getCluster());
  }

  public static Test suite() {
    return new TestSuite(RelationshipTimePhasedDataTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}

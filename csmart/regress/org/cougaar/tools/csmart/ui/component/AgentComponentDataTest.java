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

public class AgentComponentDataTest extends TestCase {

  private AgentComponentData data = null;

  public AgentComponentDataTest(String name) {
    super(name);
  }

  protected void setUp() {
    data = new AgentComponentData();
  }

  public void testRoles() {
    String[] roles = {"Role1", "Role2", "Role3", "Role4"};

    data.setRoles(roles);
    assertEquals("Test setRoles()", roles.length, data.getRoleCount());
    Iterator iter = data.getRolesIterator();
    String role = (String)iter.next();
    assertEquals("Test setRoles()", "Role1", role);
    String[] r2 = data.getRoles();
    assertEquals("Test getRoles()", roles[0], r2[0]);
    assertEquals("Test getRoles()", roles[3], r2[3]);
    data.addRole("NewRole");
    r2 = data.getRoles();
    assertEquals("Test addRole()", "NewRole", r2[data.getRoleCount()-1]);

    try {
      data.setRole(10, "Should Fail");
      fail("IndexOutOfBoundsException Expected");
    } catch(IndexOutOfBoundsException e) {}
  }


//   public void testRelationshipTimePhased() {
//     RelationshipTimePhasedData[] rel = new RelationshipTimePhasedData[3];
//     RelationshipTimePhasedData r1 =  new RelationshipTimePhasedData();
//     RelationshipTimePhasedData r2 =  new RelationshipTimePhasedData();
//     RelationshipTimePhasedData r3 =  new RelationshipTimePhasedData();
//     r1.setRole("Role1");
//     r1.setItem("Item1");
//     r2.setRole("Role2");
//     r2.setItem("Item2");
//     r3.setRole("Role3");
//     r3.setItem("Item3");

//     rel[0] = r1;
//     rel[1] = r2;
//     rel[2] = r3;

//     data.setRelationshipData(rel);
//     assertEquals("Test setRelationshipData", 3, data.getRelationshipCount()); 
//     RelationshipTimePhasedData r4 = new RelationshipTimePhasedData();
//     r4.setRole("NewRole");
//     r4.setItem("NewItem");
//     data.addRelationship(r4);
//     RelationshipTimePhasedData[] all = data.getRelationshipData();
//     assertEquals("Test addRelationship()", r4, all[3]);
//     Iterator iter = data.getRelationshipIterator();
//     RelationshipTimePhasedData rr = (RelationshipTimePhasedData)iter.next();
//     assertEquals("Test getRelationshipIterator()", r1, rr);

//     try {
//       data.setRelationship(10, r4);
//       fail("IndexOutOfBoundsException Expected");
//     } catch(IndexOutOfBoundsException e) {}

//   }

  public static Test suite() {
    return new TestSuite(AgentComponentDataTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}

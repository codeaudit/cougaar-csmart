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
      data.addRole(10, "Should Fail");
      fail("IndexOutOfBoundsException Expected");
    } catch(IndexOutOfBoundsException e) {}
  }

  public void testCommunityTimePhased() {
    CommunityTimePhasedData[] ctpd = new CommunityTimePhasedData[3];
    CommunityTimePhasedData c1 =  new CommunityTimePhasedData();
    CommunityTimePhasedData c2 =  new CommunityTimePhasedData();
    CommunityTimePhasedData c3 =  new CommunityTimePhasedData();
    c1.addCommunity("Comm1");
    c2.addCommunity("Comm2");
    c3.addCommunity("Comm3");
    ctpd[0] = c1;
    ctpd[1] = c2;
    ctpd[2] = c3;

    data.setCommunityData(ctpd);
    assertEquals("Test setCommunityData", 3, data.getCommunityCount()); 
    CommunityTimePhasedData c4 = new CommunityTimePhasedData();
    c4.addCommunity("NewEntry");
    data.addCommunity(c4);
    CommunityTimePhasedData[] all = data.getCommunityData();
    assertEquals("Test addCommunity()", c4, all[3]);
    Iterator iter = data.getCommunityIterator();
    CommunityTimePhasedData cc = (CommunityTimePhasedData)iter.next();
    assertEquals("Test getCommunityIterator()", c1, cc);

    try {
      data.addCommunity(10, c4);
      fail("IndexOutOfBoundsException Expected");
    } catch(IndexOutOfBoundsException e) {}

  }

  public void testRelationshipTimePhased() {
    RelationshipTimePhasedData[] rel = new RelationshipTimePhasedData[3];
    RelationshipTimePhasedData r1 =  new RelationshipTimePhasedData();
    RelationshipTimePhasedData r2 =  new RelationshipTimePhasedData();
    RelationshipTimePhasedData r3 =  new RelationshipTimePhasedData();
    r1.setRole("Role1");
    r1.setItem("Item1");
    r2.setRole("Role2");
    r2.setItem("Item2");
    r3.setRole("Role3");
    r3.setItem("Item3");

    rel[0] = r1;
    rel[1] = r2;
    rel[2] = r3;

    data.setRelationshipData(rel);
    assertEquals("Test setRelationshipData", 3, data.getRelationshipCount()); 
    RelationshipTimePhasedData r4 = new RelationshipTimePhasedData();
    r4.setRole("NewRole");
    r4.setItem("NewItem");
    data.addRelationship(r4);
    RelationshipTimePhasedData[] all = data.getRelationshipData();
    assertEquals("Test addRelationship()", r4, all[3]);
    Iterator iter = data.getRelationshipIterator();
    RelationshipTimePhasedData rr = (RelationshipTimePhasedData)iter.next();
    assertEquals("Test getRelationshipIterator()", r1, rr);

    try {
      data.addRelationship(10, r4);
      fail("IndexOutOfBoundsException Expected");
    } catch(IndexOutOfBoundsException e) {}

  }

  public static Test suite() {
    return new TestSuite(AgentComponentDataTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}

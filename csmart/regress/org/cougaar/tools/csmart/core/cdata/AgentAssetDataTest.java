/**
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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

/**
 * AgentAssetDataTest.java
 *
 *
 * Created: Tue Jan 15 13:04:34 2002
 *
 */
public class AgentAssetDataTest extends TestCase {

  private AgentAssetData aad;
  private AgentComponentData acd;
  
  private static final String AGENT_NAME = "My Name";
  private static final String UID = "My UID";
  private static final String UIC = "My UIC";
  private static final String UNIT_NAME = "Unit Name";

  public AgentAssetDataTest (String name){
    super(name);
  }

  public void setUp() {
    acd = new AgentComponentData();
    acd.setName(AGENT_NAME);
    aad = new AgentAssetData(acd);
  }

  public static Test suite() {
    return new TestSuite(AgentAssetDataTest.class);
  }

  public void testGetAgentAndParent() {
    assertEquals("Test Get Agent", acd, aad.getAgent());
    assertEquals("Test Get Parent", acd, aad.getParent());
  }

  public void testSetGetType() {
    aad.setType(AgentAssetData.ORG);
    assertEquals("Test Get Type", AgentAssetData.ORG, aad.getType());
  }

  public void testIsEntity() {
    // Test Default Type
    assertEquals("Test IsEntity 1", true, aad.isEntity());
    
    // Test Invalid
    aad.setType(AgentAssetData.ORG);
    assertEquals("Test isEntity 2", false, aad.isEntity());

    // Test set valid
    aad.setType(AgentAssetData.ENTITY);
    assertEquals("Test IsEntity 2", true, aad.isEntity());
  }

  public void testIsOrg() {
    assertEquals("Test IsOrg 1", false, aad.isOrg());
    aad.setType(AgentAssetData.ORG);
    assertEquals("Test IsOrg 2", true, aad.isOrg());
  }

  public void testIsTPOrg() {
    assertEquals("Test IsTPOrg 1", false, aad.isTPOrg());
    aad.setType(AgentAssetData.TPORG);
    assertEquals("Test ITPOrg 2", true, aad.isTPOrg());
  }

  public void testGetClusterID() {
    assertEquals("Test GetClusterID", AGENT_NAME, aad.getClusterID());

    AgentAssetData nullTest = new AgentAssetData(null);
    assertNull("Test GetClusterID", nullTest.getClusterID());
  }

  public void testSetGetAssetClass() {
    aad.setAssetClass(AgentAssetData.ENTITY_ASSETCLASS);
    assertEquals("Test getAssetClass", AgentAssetData.ENTITY_ASSETCLASS, 
                 aad.getAssetClass());
  }

  public void testGetSetUniqueID() {
    aad.setUniqueID(UID);
    assertEquals("Test getUniqueID", UID, aad.getUniqueID());
  }

  public void testGetSetUnitName() {
    aad.setUnitName(UNIT_NAME);
    assertEquals("Test getUnitName()", UNIT_NAME, aad.getUnitName());
  }

  public void testUIC() {
    aad.setType(AgentAssetData.ENTITY);
    assertNull("Test UIC, Entity Type", aad.getUIC());
    
    // Test with null Cluster ID.
    AgentAssetData nullCluster = new AgentAssetData(null);
    nullCluster.setType(AgentAssetData.ORG);
    assertNull("Test UIC, Null Case", nullCluster.getUIC());

    aad.setType(AgentAssetData.ORG);
    String uicName = "UIC/" + AGENT_NAME;
    assertEquals("Test getUIC", uicName, aad.getUIC());

    aad.setUIC(UIC);
    assertEquals("Test UIC", UIC, aad.getUIC());
  }

  public void testPropGroups() {
    PropGroupData[] pgs = new PropGroupData[5];
    for(int i=0; i < pgs.length; i++) {
      pgs[i] = new PropGroupData("TestPG"+i);
      pgs[i].setSingleValue("Val "+i);
    }
    aad.setPropGroups(pgs);
    assertEquals("Test PGCount", pgs.length, aad.getPGCount());
    assertEquals("Test getPropGroups 1", pgs.length, aad.getPropGroups().length);
    PropGroupData[] ret = aad.getPropGroups();
    assertEquals("Test getPropGroups 2", pgs[0], ret[0]);
    assertEquals("Test getPropGrops 3", pgs[pgs.length-1], ret[ret.length-1]);
  }

  public void testAddPropGroup() {
    aad.addPropertyGroup(new PropGroupData("AddPG"));
    assertEquals("Test PGCount", 1, aad.getPGCount());    
  }

  public void testSetPropGroup() {
    PropGroupData pg1 = new PropGroupData("SetPG 1");
    PropGroupData pg2 = new PropGroupData("SetPG 2");

    PropGroupData[] pgs = new PropGroupData[5];
    for(int i=0; i < pgs.length; i++) {
      pgs[i] = new PropGroupData("TestPG"+i);
      pgs[i].setSingleValue("Val "+i);
    }
    aad.setPropGroups(pgs);

    try {
      aad.setPropertyGroup(0, pg1);
    } catch(IndexOutOfBoundsException e) {
      fail("Unexpected Exception Caught");
    }
    assertEquals("Test setPropertyGroup 1", pgs.length, aad.getPGCount());
    PropGroupData[] data = aad.getPropGroups();
    assertEquals("Test setPropertyGroup 2", pg1, data[0]);
    
    // Test boundries
    try {
      aad.setPropertyGroup(-1, pg1);
      fail("Expected Exception!");
    } catch(IndexOutOfBoundsException e) {}
    
    try {
      aad.setPropertyGroup(aad.getPGCount(), pg1);
      fail("Expected Exception!");
    } catch(IndexOutOfBoundsException e) {}

    try {
      aad.setPropertyGroup(4, pg2);
    } catch(IndexOutOfBoundsException e) {
      fail("Unexpected Exception Caught");
    }
    assertEquals("Test setPropertyGroup 3", pgs.length, aad.getPGCount());
    data = aad.getPropGroups();
    assertEquals("Test setPropertyGroup 4", pg2, data[4]);

    assertEquals("Test getPropGroupsIterator", true, 
                 (aad.getPropGroupsIterator() instanceof Iterator));
  }

  public void testRoles() {
    String[] roles = new String[4];
    for(int i=0; i < roles.length; i++) {
      roles[i] = new String("Role"+i);
    }

    aad.setRoles(roles);
    assertEquals("Test setRoles" , roles.length, aad.getRoleCount());
    assertEquals("Test getRolesIterator", true, 
                 (aad.getRolesIterator() instanceof Iterator));

    String[] results = aad.getRoles();
    for(int i=0; i < aad.getRoleCount(); i++) {
      assertEquals("Test getRoles["+i+"]", roles[i], results[i]);
    }

    aad.addRole("New Role");
    assertEquals("Test addRole", roles.length+1, aad.getRoleCount());
    
    String ROLE_0 = "Role at 0";
    String ROLE_4 = "Role at 4";

    try {
      aad.setRole(0, ROLE_0);
    } catch(IndexOutOfBoundsException e) {
      fail("Unexpected Exception, " + ROLE_0);
    }

    results = aad.getRoles();
    assertEquals("Test setRole 1", ROLE_0, results[0]);

    try {
      aad.setRole(4, ROLE_4);
    } catch(IndexOutOfBoundsException e) {
      fail("Unexpected Exception, " + ROLE_4);
    }

    results = aad.getRoles();
    assertEquals("Test setRole 2", ROLE_4, results[4]);

    // Test boundries.
    try {
      aad.setRole(-1, ROLE_0);
      fail("Exception Expected");
    } catch(IndexOutOfBoundsException e) {}

    try {
      aad.setRole(aad.getRoleCount()+1, ROLE_4);
      fail("Exception Expected");
    } catch(IndexOutOfBoundsException e) {}
    
  }

  public void testRelationships() {
    RelationshipData r1 = new RelationshipData();
    RelationshipData r2 = new RelationshipData();

    RelationshipData[] rel = new RelationshipData[5];
    for(int i=0; i < rel.length; i++) {
      rel[i] = new RelationshipData();
      rel[i].setRole("Role "+i);
    }
    aad.setRelationshipData(rel);

    try {
      aad.setRelationship(0, r1);
    } catch(IndexOutOfBoundsException e) {
      fail("Unexpected Exception Caught");
    }
    assertEquals("Test setRelationship 1", rel.length, aad.getRelationshipCount());
    RelationshipData[] data = aad.getRelationshipData();
    assertEquals("Test setRelationship 2", r1, data[0]);
    assertEquals("Test getRelationship 1", r1, aad.getRelationship(0));

    // Test boundries
    try {
      aad.setRelationship(-1, r1);
      fail("Expected Exception!");
    } catch(IndexOutOfBoundsException e) {}
    
    try {
      aad.setRelationship(aad.getRelationshipCount(), r1);
      fail("Expected Exception!");
    } catch(IndexOutOfBoundsException e) {}

    try {
      aad.setRelationship(4, r2);
    } catch(IndexOutOfBoundsException e) {
      fail("Unexpected Exception Caught");
    }
    assertEquals("Test setRelationship 3", rel.length, aad.getRelationshipCount());
    data = aad.getRelationshipData();
    assertEquals("Test setRelationship 4", r2, data[4]);
    assertEquals("Test getRelationship", r2, aad.getRelationship(4));
 
    assertEquals("Test getRelationshipIterator", true, 
                 (aad.getRelationshipIterator() instanceof Iterator));
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}// AgentAssetDataTest

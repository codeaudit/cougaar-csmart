/* 
 * <copyright>
 * Copyright 2001 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).

 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.component;

import java.lang.IndexOutOfBoundsException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Hold the data to describe the local Asset in an Agent.<br>
 * This is usually an Organization or Entity, such as would be described
 * in a prototype-ini.dat file.<br>
 * Elements:<ul>
 * <li>Class: Class of Asset to instantiate.</li>
 * <li>Type: Entity, Organization, or Organization with Time-Phased Relationships. This governs what data should be sent to the Agent.</li>
 * <li>Agent: The Agent containing this Asset.</li>
 * <li>ClusterID: (Calculated from the Agent.</li>
 * <li>UniqueID: (Orgs only, unused) UTC/RTOrg or UTC/CombatOrg usually</li>
 * <li>UnitName: (Orgs only, optional)</li>
 * <li>UIC: (Orgs only) Usually UIC/[ClusterID]</li>
 * <li>Roles: A collection of String role names.</li>
 * <li>Relationships: The relationships this Asset has with others.
 * The format of these differs based on the Asset type.</li>
 * <li>PropertyGroups: The remainder of the data is a set of PropertyGroups.<br>
 * These PGs have a name, and then a collection of properties.
 * They always include a ClusterPG, and often include a TypeIdentificationPG
 * (Entities only), ItemIdentificationPG (Entities only), OrganizationPG
 * or EntityPG as appropriate, CommunityPG, MilitaryOrgPG (for
 * Organizations only), etc.</li>
 * </ul><br>
 *
 */
public class AgentAssetData implements Serializable {
  // Asset of type entity
  // Item & type should be specified explicitly
  // EntityPG not OrgPG will be included
  // UniqueID, UnitName & UIC will be excluded
  // Relationships will include type, item, start, & end
  public static final int ENTITY = 0;
  public static final int ORG = 1;

  // Org with time-phased relationships
  // The -relationships.ini file will be written out
  public static final int TPORG = 2; 
  
  // Common Asset Class Types
  public static final String ENTITY_ASSETCLASS = "Entity";
  public static final String COMBAT_ASSETCLASS = "CombatOrganization";

  // The default type is ENTITY
  private int type = 0;

  private String assetClass = null; // CombatOrganization, Entity, etc
  private String uniqID = null; // For Orgs only, not used, UTC/RTOrg or CombatOrg
  private String unitname = null; // Org only, optional
  private String uic = null; // Org only, usu UIC/ClusterID

  // Note: ClusterID is retrieved from the parent
  private AgentComponentData agent; // The Agent in which this sits

  private List roles; // Strings
  private List propGroups; // PropGroupData objects
  private List relats; // RelatData objects

  public AgentAssetData(AgentComponentData parent) {
    roles = new ArrayList();
    propGroups = new ArrayList();
    relats = new ArrayList();
    agent = parent;
  }

  public AgentComponentData getParent() {
    return agent;
  }

  public AgentComponentData getAgent() {
    return agent;
  }

  public void setAgent(AgentComponentData agent) {
    this.agent = agent;
  }
  
  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public boolean isEntity() {
    return this.type == AgentAssetData.ENTITY;
  }

  public boolean isOrg() {
    return this.type == AgentAssetData.ORG;
  }

  public boolean isTPOrg() {
    return this.type == AgentAssetData.TPORG;
  }

  public String getClusterID() {
    if (agent != null) {
      return agent.getName();
    } else {
      return null;
    }
  }

  public String getAssetClass() {
    return assetClass;
  }

  public void setAssetClass(String clss) {
    assetClass = clss;
  }

  public String getUniqueID() {
    return uniqID;
  }

  public void setUniqueID(String uid) {
    uniqID = uid;
  }

  public String getUnitName() {
    return unitname;
  }

  public void setUnitName(String unit) {
    unitname = unit;
  }

  public String getUIC() {
    return (uic == null ? ((! isEntity()) && (getClusterID() != null)
			   ? "UIC/" + getClusterID()
			   : null)
	    : uic);
  }

  public void setUIC(String uic) {
    this.uic = uic;
  }

  // Add stuff to set the list of roles/relats/pgs, clear the list, add to the list, get the list, get by index
  // Add convenience methods to get/set values in the common PGs
  
  /**
   * Sets all property groups for this Agent
   *
   * @param PropGroupData[] array of property groups
   */
  public void setPropGroups(PropGroupData[] newPropgroups) {
    propGroups.clear();
    for(int i=0; i < newPropgroups.length; i++) {
      propGroups.add(newPropgroups[i]);
    }
  }

  /**
   * Adds a property group for this Agent
   *
   * @param PropGroupData property group
   */
  public void addPropertyGroup(PropGroupData propgroup) {
    this.propGroups.add(propgroup);
  }

  /**
   * Sets a property group for this Agent, replacing the previous PG at this index
   *
   * @param int index for role
   * @param PropGroupData PG to replace with
   */
  public void setPropertyGroup(int index, PropGroupData propgroup) 
                              throws IndexOutOfBoundsException{
    this.propGroups.set(index, propgroup);
  }

  /**
   * Returns an array of property groups for this agent.
   *
   * @return property groups
   */
  public PropGroupData[] getPropGroups() {
    return (PropGroupData[])propGroups.toArray(new String[propGroups.size()]);
  }

  /**
   * Returns an iterator of propgroups for this agent.
   *
   * @return iterator
   */
  public Iterator getPropGroupsIterator() {
    return propGroups.iterator();
  }

  /**
   * Returns a count of all propgroups for this agent.
   *
   * @return count
   */
  public int getPGCount() {
    return propGroups.size();
  }

  ////////////////////////////
  
  /**
   * Sets all roles for this Agent
   *
   * @param String[] array of roles
   */
  public void setRoles(String[] newRoles) {
    roles.clear();
    for(int i=0; i < newRoles.length; i++) {
      roles.add(newRoles[i]);
    }
  }

  /**
   * Adds a role for this Agent
   *
   * @param String role
   */
  public void addRole(String role) {
    this.roles.add(role);
  }

  /**
   * Sets a role for this Agent, replacing the previous role at this index
   *
   * @param int index for role
   * @param String role to replace with
   */
  public void setRole(int index, String role) 
                              throws IndexOutOfBoundsException{
    this.roles.set(index, role);
  }

  /**
   * Returns an array of roles for this agent.
   *
   * @return roles
   */
  public String[] getRoles() {
    return (String[])roles.toArray(new String[roles.size()]);
  }

  /**
   * Returns an iterator of roles for this agent.
   *
   * @return iterator
   */
  public Iterator getRolesIterator() {
    return roles.iterator();
  }

  /**
   * Returns a count of all roles for this agent.
   *
   * @return count
   */
  public int getRoleCount() {
    return roles.size();
  }

  /**
   * Sets the Relationship information for this agent.
   *
   * @param RelationshipData[] array of relationship objects.
   */
  public void setRelationshipData(RelationshipData[] relationships) {
    this.relats.clear();
    for(int i=0; i < relationships.length; i++) {
      this.relats.add(relationships[i]);
    }
  }

  /**
   * Adds a relationship for this Agent
   *
   * @param RelationshipData relationship
   */
  public void addRelationship(RelationshipData relationship) {
    this.relats.add(relationship);
  }

  /**
   * Sets a relationship for this Agent, replacing the previous relationship
   * at this index.
   *
   * @param int index for relationship
   * @param RelationshipData  relationship
   */
  public void setRelationship(int index, RelationshipData relationship) 
                        throws IndexOutOfBoundsException {
    this.relats.set(index, relationship);
  }

  /**
   * Returns a count of all relationships for this agent.
   *
   * @return count
   */
  public int getRelationshipCount() {
    return relats.size();
  }

  /**
   * Returns an Iterator of all relationships for this agent.
   *
   * @return iterator
   */
  public Iterator getRelationshipIterator() {
    return relats.iterator();
  }

  /**
   * Returns an array of relationship data for this agent.
   *
   * @return RelationshipData
   */
  public RelationshipData[] getRelationshipData() {
    return (RelationshipData[])relats.toArray(new RelationshipData[relats.size()]);
  }
  
} // end of AgentAssetData.java

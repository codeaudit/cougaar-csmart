/* 
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Hold the data to describe the local Asset in an Agent. <br>
 * This is usually an Organization or Entity, such as would be described
 * in a prototype-ini.dat file.<br>
 * Elements:<ul>
 * <li><b>Class:</b> Class of Asset to instantiate.</li>
 * <li><b>Type:</b> Entity, Organization, or Organization with Time-Phased Relationships. This governs what data should be sent to the Agent.</li>
 * <li><b>Agent:</b> The Agent containing this Asset.</li>
 * <li><b>ClusterID:</b> Calculated from the Agent.</li>
 * <li><b>UniqueID:</b> (Orgs only, unused) UTC/RTOrg or UTC/CombatOrg usually</li>
 * <li><b>UnitName:</b> (Orgs only, optional)</li>
 * <li><b>UIC:</b> (Orgs only) Usually UIC/[ClusterID]</li>
 * <li><b>Roles:</b> A collection of String role names.</li>
 * <li><b>Relationships:</b> The relationships this Asset has with others.
 * The format of these differs based on the Asset type.</li>
 * <li><b>PropertyGroups:</b> The remainder of the data is a set of PropertyGroups.<br>
 * These PGs have a name, and then a collection of properties.
 * They always include a ClusterPG, and often include a TypeIdentificationPG
 * (Entities only), ItemIdentificationPG (Entities only), OrganizationPG
 * or EntityPG as appropriate, CommunityPG, MilitaryOrgPG (for
 * Organizations only), etc.</li>
 * </ul><br>
 *
 */
public class AgentAssetData implements Serializable {

  /** All Possible Entity Types **/

  // Item & type should be specified explicitly
  // EntityPG not OrgPG will be included
  // UniqueID, UnitName & UIC will be excluded
  // Relationships will include type, item, start, & end
  /** Asset of type Entity**/
  public static final int ENTITY = 0;

  /** Asset of type Organization **/
  public static final int ORG = 1;

  /** Asset of type Organization with Time-Phased Relationships **/
  // The -relationships.ini file will be written out
  public static final int TPORG = 2; 

  /** Ini file format is new style (uses AssetDataPlugin) **/
  public static final int NEW_FORMAT = 0;
  
  /** Ini file format is the old style **/
  public static final int OLD_FORMAT = 1;

  /** Common Asset Class Types **/

  /** Entity Asset Class **/
  public static final String ENTITY_ASSETCLASS = "Entity";

  /** Combat Asset Class **/
  public static final String COMBAT_ASSETCLASS = "CombatOrganization";

  // The default type is ENTITY
  private int type = AgentAssetData.ENTITY;

  private String assetClass = null; // CombatOrganization, Entity, etc
  private String uniqID = null; // For Orgs only, not used, UTC/RTOrg or CombatOrg
  private String unitname = null; // Org only, optional
  private String uic = null; // Org only, usu UIC/ClusterID

  // Note: ClusterID is retrieved from the parent
  private AgentComponentData agent; // The Agent in which this sits

  private List roles; // Strings
  private List propGroups; // PropGroupData objects
  private List relats; // RelatData objects

  private int iniStyle = AgentAssetData.NEW_FORMAT;

  // Flag if this object has been modified and needs to be (re) saved
  private boolean modified = false;
  
  /**
   * Creates a new <code>AgentAssetData</code> instance.
   * Assigned to a specific agent.
   *
   * @param parent an <code>AgentComponentData</code> value
   * indicating the agent that owns this asset data.
   */
  public AgentAssetData(AgentComponentData parent) {
    roles = new ArrayList();
    propGroups = new ArrayList();
    relats = new ArrayList();
    agent = parent;
  }

  /**
   * Returns the parent of this asset.
   * (This is the same value as {@link #getAgent})
   *
   * @return an <code>AgentComponentData</code> value
   * for the parent of this asset.
   */
  public AgentComponentData getParent() {
    return agent;
  }

  /**
   * Returns the agent associated with this asset.
   * (This is the same value as {@link #getParent})
   *
   * @return an <code>AgentComponentData</code> for the
   * agent of this asset.
   */
  public AgentComponentData getAgent() {
    return agent;
  }

  /**
   * Returns the type of asset.  Currently, there are
   * three possible agent types:
   * <ul>
   *   <li>{@link #ORG} - Organization</li>
   *   <li>{@link #ENTITY} - Entity</li>
   *   <li>{@link #TPORG} - Time Phased Organization</li>
   * </ul>
   *
   * @return an <code>int</code> value
   * Representating the type of this asset.
   */
  public int getType() {
    return type;
  }

  /**
   * Sets the type of this asset.
   *
   * @see #getType for possible types.
   * @param type an <code>int</code> value
   * representating the type of this asset.
   */
  public void setType(int type) {
    this.type = type;
  }

  /**
   * Indicates if this asset is an Entity Asset.
   *
   * @return a <code>boolean</code> value
   */
  public boolean isEntity() {
    return this.type == AgentAssetData.ENTITY;
  }

  /**
   * Indicates if this asset is an Org Asset.
   *
   * @return a <code>boolean</code> value
   */
  public boolean isOrg() {
    return this.type == AgentAssetData.ORG;
  }

  /**
   * Indicates if this asset is an Time-Phased Org Asset.
   *
   * @return a <code>boolean</code> value
   */
  public boolean isTPOrg() {
    return this.type == AgentAssetData.TPORG;
  }

  /**
   * Gets the ClusterID for this asset.
   * This field is auto-calculated from the agent name.
   *
   * @return a <code>String</code> value of the ClusterID
   */
  public String getClusterID() {
    if (agent != null) {
      return agent.getName();
    } else {
      return null;
    }
  }

  /**
   * Sets the ini file format style.
   *
   * @param style 
   */
  public void setIniFormat(int style) {
    this.iniStyle = style;
  }

  /**
   * Gets the current ini format style
   *
   * @return an <code>int</code> value
   */
  public int getIniFormat() {
    return this.iniStyle;
  }

  /**
   * Returns true if the ini file is of the new
   * format.  
   *
   * @return a <code>boolean</code> value
   */
  public boolean isNewIniFormat() {
    return (this.iniStyle == AgentAssetData.NEW_FORMAT);
  }

  /**
   * Gets the Asset Class for this asset.
   *
   * @return a <code>String</code> value of the Asset Class
   */
  public String getAssetClass() {
    return assetClass;
  }

  /**
   * Sets the asset class for this asset.
   *
   * @param clss a <code>String</code> value of the Asset Class
   */
  public void setAssetClass(String clss) {
    assetClass = clss;
    fireModified();
  }

  /**
   * Gets the UniqueID for this asset.
   * This field is only in Org Assets and
   * it is <b>REQUIRED</b>.
   *
   * @return a <code>String</code> value of the UniqueID
   */
  public String getUniqueID() {
    return uniqID;
  }

  /**
   * Sets the UniqueID for this asset.
   * This field is only in Org Assets and
   * it is <b>REQUIRED</b>.
   *
   * @param uid a <code>String</code> value of the UniqueID
   */
  public void setUniqueID(String uid) {
    uniqID = uid;
    fireModified();
  }

  /**
   * Gets the Unit name for this asset.
   * This field is only on Org assets, and it
   * is optional.
   *
   * @return a <code>String</code> value of the Unit Name
   */
  public String getUnitName() {
    return unitname;
  }

  /**
   * Sets the unit name for this asset.
   * This field is only on Org assets, and it
   * is optional.
   *
   * @param unit a <code>String</code> value of the Unit Name
   */
  public void setUnitName(String unit) {
    unitname = unit;
    fireModified();
  }

  /**
   * Gets the UIC for this asset.
   * Only Org assets have a UIC.
   *
   * @return a <code>String</code> value of the UIC
   */
  public String getUIC() {
    return (uic == null ? ((! isEntity()) && (getClusterID() != null)
			   ? "UIC/" + getClusterID()
			   : null)
	    : uic);
  }

  /**
   * Sets the UIC for this asset.
   * Only Org assets have a UIC.
   *
   * @param uic a <code>String</code> value of the UIC
   */
  public void setUIC(String uic) {
    this.uic = uic;
    fireModified();
  }

  // Add stuff to set the list of roles/relats/pgs, 
  // clear the list, add to the list, get the list, get by index
  // Add convenience methods to get/set values in the common PGs
  
  /**
   * Sets all property groups for this Agent.  Any
   * previous property groups are removed before this
   * set takes place.
   *
   * @see PropGroupData
   * @param PropGroupData[] array of property groups
   */
  public void setPropGroups(PropGroupData[] newPropgroups) {
    propGroups.clear();
    for(int i=0; i < newPropgroups.length; i++) {
      propGroups.add(newPropgroups[i]);
    }
    fireModified();
  }

  /**
   * Adds a property group for this Agent.  The
   * new property group is added to the end of
   * the list.
   *
   * @param PropGroupData property group
   */
  public void addPropertyGroup(PropGroupData propgroup) {
    this.propGroups.add(propgroup);
    fireModified();
  }

  /**
   * Sets a property group for this Agent, 
   * replacing the previous PG at the specified index
   *
   * @param int index for the Property Group
   * @param PropGroupData new Property Group for the specified index
   */
  public void setPropertyGroup(int index, PropGroupData propgroup) 
                              throws IndexOutOfBoundsException{
    this.propGroups.set(index, propgroup);
    fireModified();
  }

  /**
   * Returns an array of all property groups for this agent.
   *
   * @see PropGroupData
   * @return all property groups
   */
  public PropGroupData[] getPropGroups() {
    return (PropGroupData[]) propGroups.toArray(new PropGroupData[propGroups.size()]);
  }

  /**
   * Returns an <code>Iterator</code> of all property groups for this agent.
   *
   * @return <code>Iterator</code> of all property groups
   */
  public Iterator getPropGroupsIterator() {
    return propGroups.iterator();
  }

  /**
   * Returns a count of all property groups for this agent.
   *
   * @return number of property groups
   */
  public int getPGCount() {
    return propGroups.size();
  }

  ////////////////////////////
  
  /**
   * Sets all roles for this Agent.  Any previous roles are deleted.
   *
   * @param String[] array of roles
   */
  public void setRoles(String[] newRoles) {
    roles.clear();
    for(int i=0; i < newRoles.length; i++) {
      roles.add(newRoles[i]);
    }
    fireModified();
  }

  /**
   * Adds a role for this Agent
   *
   * @param String new role
   */
  public void addRole(String role) {
    this.roles.add(role);
    fireModified();
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
    fireModified();
  }

  /**
   * Returns an array of all roles for this agent.
   *
   * @return all roles for this agent
   */
  public String[] getRoles() {
    return (String[])roles.toArray(new String[roles.size()]);
  }

  /**
   * Returns an <code>Iterator</code> of all roles for this agent.
   *
   * @return </code>Iterator</code> of all roles
   */
  public Iterator getRolesIterator() {
    return roles.iterator();
  }

  /**
   * Returns a count of all roles for this agent.
   *
   * @return number of roles in this agent
   */
  public int getRoleCount() {
    return roles.size();
  }

  /**
   * Sets the Relationship information for this agent.
   *
   * @see RelationshipData
   * @param RelationshipData[] array of relationship objects.
   */
  public void setRelationshipData(RelationshipData[] relationships) {
    this.relats.clear();
    for(int i=0; i < relationships.length; i++) {
      this.relats.add(relationships[i]);
    }
    fireModified();
  }

  /**
   * Adds a relationship for this Agent
   *
   * @see RelationshipData
   * @param RelationshipData new relationship for this agent
   */
  public void addRelationship(RelationshipData relationship) {
    this.relats.add(relationship);
    fireModified();
  }

  /**
   * Sets a relationship for this Agent, replacing the previous relationship
   * at this index.
   *
   * @see RelationshipData
   * @param int index for new relationship
   * @param RelationshipData  new relationship
   */
  public void setRelationship(int index, RelationshipData relationship) 
                        throws IndexOutOfBoundsException {
    this.relats.set(index, relationship);
    fireModified();
  }

  /**
   * Gets a specific Relationship, based on an index, for this agent.
   *
   * @see RelationshipData
   * @param index Index of the relationship.
   * @return <code>RelationshipData</code> value from the given index
   */
  public RelationshipData getRelationship(int index) {
    return (RelationshipData) relats.get(index);
  }

  /**
   * Returns a count of all relationships for this agent.
   *
   * @return number of relationships for this agent
   */
  public int getRelationshipCount() {
    return relats.size();
  }

  /**
   * Returns an <code>Iterator</code> of all relationships for this agent.
   *
   * @return <code>Iterator</code> of all relationships
   */
  public Iterator getRelationshipIterator() {
    return relats.iterator();
  }

  /**
   * Returns an array of <code>RelationshipData</code> for this agent.
   *
   * @see RelationshipData
   * @return <code>RelationshipData</code> array containing all relationships.
   */
  public RelationshipData[] getRelationshipData() {
    return (RelationshipData[])relats.toArray(new RelationshipData[relats.size()]);
  }

  /**
   * Has this Component been modified by a recipe, requiring possible save?
   */
  public boolean isModified() {
    return modified;
  }

  /**
   * The component has been modified from its initial state.
   * Mark it and all ancestors modified.
   **/
  public void fireModified() {
    // Problem: I only want to call this after the society generates
    // the CData, and before the recipes are applied....

    // make this private?

    // If this is already modified, so will the parents
    if (modified) return;
    modified = true;
    // recurse _up_
    if (agent != null)
      agent.fireModified();
  }

  /**
   * The component has been saved. Mark it as saved.
   */
  public void resetModified() {
    modified = false;
  }
} // end of AgentAssetData.java

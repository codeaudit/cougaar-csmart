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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * AgentComponentData extends ComponentData and adds in
 * fields specific to an Agent such as all communites
 * that it belongs to, all of its roles and all its relationships.
 */
public class AgentComponentData extends GenericComponentData {

  ArrayList roles = null;
  ArrayList communities = null;
  ArrayList relationships = null;

  /** Default Constructor **/
  public AgentComponentData() {
    roles = new ArrayList();
    communities = new ArrayList();
    relationships = new ArrayList();
  }

  /**
   * Sets all roles for this Agent
   *
   * @param String[] array of roles
   */
  public void setRoles(String[] newRoles) {
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
   * Adds a role for this Agent
   *
   * @param int index for role
   * @param String role
   */
  public void addRole(int index, String role) 
                              throws IndexOutOfBoundsException{
    this.roles.add(index, role);
  }

  /**
   * Returns an array of roles for this agent.
   *
   * @return roles
   */
  public String[] getRoles() {
    return (String[])roles.toArray(new String[0]);
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
   * Sets the Community Information for this Agent
   *
   * @param CommunityTimePhasedData[] Array of Community objects
   */
  public void setCommunityData(CommunityTimePhasedData[] communities) {
    for(int i=0; i < communities.length; i++) {
      this.communities.add(communities[i]);
    }
  }

  /**
   * Adds a community for this Agent
   *
   * @param CommunityTimePhasedData community
   */
  public void addCommunity(CommunityTimePhasedData community) {
    this.communities.add(community);
  }

  /**
   * Adds a community for this Agent
   *
   * @param int index for community
   * @param CommunityTimePhasedData community
   */
  public void addCommunity(int index, CommunityTimePhasedData community) 
                     throws IndexOutOfBoundsException {
    this.communities.add(index, community);
  }

  /**
   * Returns a count of all communities for this agent.
   *
   * @return count
   */
  public int getCommunityCount() {
    return communities.size();
  }

  /**
   * Returns an Iterator of all communities for this agent.
   *
   * @return iterator
   */
  public Iterator getCommunityIterator() {
    return communities.iterator();
  }

  /**
   * Returns an array of CommunintyTimePhased Data for this agent.
   *
   * @return CommunityTimePhasedData
   */
  public CommunityTimePhasedData[] getCommunityData() {
    return (CommunityTimePhasedData[]) communities.toArray(new CommunityTimePhasedData[0]);
  }

  /**
   * Sets the Relationship information for this agent.
   *
   * @param RelationshipTimePhasedData[] array of relationship objects.
   */
  public void setRelationshipData(RelationshipTimePhasedData[] relationships) {
    for(int i=0; i < relationships.length; i++) {
      this.relationships.add(relationships[i]);
    }
  }

  /**
   * Adds a relationship for this Agent
   *
   * @param RelationshipTimePhasedData relationship
   */
  public void addRelationship(RelationshipTimePhasedData relationship) {
    this.relationships.add(relationship);
  }

  /**
   * Adds a relationship for this Agent
   *
   * @param int index for relationship
   * @param RelationshipTimePhasedData  relationship
   */
  public void addRelationship(int index, RelationshipTimePhasedData relationship) 
                        throws IndexOutOfBoundsException {
    this.relationships.add(index, relationship);
  }

  /**
   * Returns a count of all relationships for this agent.
   *
   * @return count
   */
  public int getRelationshipCount() {
    return relationships.size();
  }

  /**
   * Returns an Iterator of all relationships for this agent.
   *
   * @return iterator
   */
  public Iterator getRelationshipIterator() {
    return relationships.iterator();
  }

  /**
   * Returns an array of relationship data for this agent.
   *
   * @return RelationshipTimePhasedData
   */
  public RelationshipTimePhasedData[] getRelationshipData() {
    return (RelationshipTimePhasedData[])relationships.toArray(new RelationshipTimePhasedData[0]);
  }
}

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

/** 
 *  Data Structure used to store Time Phased RelationshipData
 *  In the prototype.ini.dat file, each relationship line is
 *  of the form:  </br>
 *  #  role     itemID     typeID    cluster    start    end
 *
 */
public class RelationshipTimePhasedData extends TimePhasedData {
  String role;
  String item;
  String type;
  String cluster;

  public RelationshipTimePhasedData() {
    super();
  }

  /** 
   * Sets the role for this relationship 
   *
   * @param String role
   **/
  public void setRole(String role) {
    this.role = role;
  }

  /** 
   * Sets the item ID for this relationship 
   * 
   * @param String item ID for this relationship
   **/
  public void setItem(String item) {
    this.item = item;
  }

  /** 
   * Sets the Type ID for this relationship.
   *
   * @param String type ID for this relationship
   **/
  public void setType(String type) {
    this.type = type;
  }

  /** 
   * Sets the cluster the relationship belongs to 
   *
   * @param String cluster name.
   **/
  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  /**
   * Returns the Role for this Relationship
   *
   * @return role
   */
  public String getRole() {
    return role;
  }

  /**
   * Returns the Item for this relationship
   *
   * @return Item
   */
  public String getItem() {
    return item;
  }

  /**
   * Returns the type for this relationship
   *
   * @return type
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the cluster for this realtionship
   *
   * @return cluster
   */
  public String getCluster() {
    return cluster;
  }
}

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
    setStartTime("");
    setStopTime("");
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

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

import java.io.Serializable;
import java.util.Iterator;
import java.io.Serializable;

public class RelationshipData implements Serializable {
  
  public static final String SUPERIOR = "Superior";
  public static final String SUPPORTING = "Supporting";

  private String cluster = null;
  private String relationship = null;
  private String type = null;
  private String role = null;
  private String item = null;
  private String startTime = null;
  private String stopTime = null;

  public RelationshipData() {
    setStartTime("");
    setStopTime("");
  }

  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  public String getCluster() {
    return this.cluster;
  }
  
  public void setRelationship(String relationship) {
    this.relationship = relationship;
  }

  public String getRelationship() {
    return this.relationship;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }

  public void setItem(String item) {
    this.item = item;
  }

  public String getItem() {
    return this.item;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getRole() {
    return this.role;
  }

  /**
   * Sets the start time for the time-phased object
   * Data is in the format of: 03/25/1959 12:00 am
   *
   * @param String start time.
   */
  public void setStartTime(String start) {
    this.startTime = start;
  }

  /**
   * Sets the Stop time for the time-phased object.
   * Data is in the format of: 03/25/1959 12:00 am
   *
   * @param String stop time.
   */
  public void setStopTime(String stop) {
    this.stopTime = stop;
  }

  /**
   * Gets the Start Time for the time
   * phased object
   *
   * @return start time.
   */
  public String getStartTime() {
    return startTime;
  }

  /**
   * Gets the Stop time for the time 
   * phased object. 
   *
   * @return stop time
   */
  public String getStopTime() {
    return stopTime;
  }
  
}

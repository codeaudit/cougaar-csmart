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
  
  private String type = null;
  private String role = null;
  private String item = null;
  private long startTime = 0L;
  private long endTime = 0L;
  private String supported = null;

  // Types of relationship (not really important if everything else is correct)
  public static final String SUPERIOR = "Superior";
  public static final String SUPPORTING = "Supporting";

  public RelationshipData() {
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
  public void setStartTime(long start) {
    this.startTime = start;
  }

  /**
   * Sets the End time for the time-phased object.
   * Data is in the format of: 03/25/1959 12:00 am
   *
   * @param String end time.
   */
  public void setEndTime(long end) {
    this.endTime = end;
  }

  /**
   * Gets the Start Time for the time
   * phased object
   *
   * @return start time.
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * Gets the End time for the time 
   * phased object. 
   *
   * @return end time
   */
  public long getEndTime() {
    return endTime;
  }
  
  public void setSupported(String supported) {
     this.supported = supported;
   }
 
   public String getSupported() {
     return this.supported;
   }

}

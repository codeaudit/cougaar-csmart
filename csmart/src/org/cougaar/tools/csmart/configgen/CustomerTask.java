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
package org.cougaar.tools.csmart.configgen;

/**
 * A single customer task.
 */
public class CustomerTask {
  
  public CustomerTask() {    
  }
  
  private String taskName;
  
  /**
   * Get the value of taskName.
   * @return Value of taskName.
   */
  public String getTaskName() {
    return taskName;
  }
  
  /**
   * Set the value of taskName.
   * @param v  Value to assign to taskName.
   */
  public void setTaskName(String  v) {
    if (this.taskName != null) {
      throw new IllegalArgumentException("Task Name already set.");
    }
    this.taskName = v;
  }


  private String worldState;
  
  /**
   * Get the value of worldState.
   * @return Value of worldState.
   */
  public String getWorldState() {
    return worldState;
  }
  
  /**
   * Set the value of worldState.
   * @param v  Value to assign to worldState.
   */
  public void setWorldState(String  v) {
    this.worldState = v;
  }
  
  private double vital;
  
  /**
   * Get the value of vital.
   * @return Value of vital.
   */
  public double getVital() {
    return vital;
  }
  
  /**
     * Set the value of vital.
     * @param v  Value to assign to vital.
     */
  public void setVital(double  v) {
    this.vital = v;
  }
  
  private long duration;
  
  /**
   * Get the value of duration.
   * @return Value of duration.
   */
  public long getDuration() {
    return duration;
  }
  
  /**
   * Set the value of duration.
   * @param v  Value to assign to duration.
   */
  public void setDuration(long  v) {
    this.duration = v;
  }

  private long chaos;
  
  /**
   * Get the value of chaos.
   * @return Value of chaos.
   */
  public long getChaos() {
    return chaos;
  }
  
  /**
   * Set the value of chaos.
   * @param v  Value to assign to chaos.
   */
  public void setChaos(long  v) {
    if (this.chaos != 0) {
      throw new IllegalArgumentException("Value already set.");
    }
    this.chaos = v;
  }

} // CustomerTask

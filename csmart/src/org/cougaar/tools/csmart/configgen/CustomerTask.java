/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
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

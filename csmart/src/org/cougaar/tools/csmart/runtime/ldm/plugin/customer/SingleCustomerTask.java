/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.runtime.ldm.plugin.customer;

import org.cougaar.tools.csmart.Constants;

/**
 * Bean holding all the attributes of a new Task
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 */
public class SingleCustomerTask {

  /** The current state of the world for this task. **/
  private String state;
  
  /** The Type of Task **/
  private String type;
  
  /** The rate at which this task will be issued **/
  private long rate;
  
  /** The deviation from the rate to slightly randomize the issue rate **/
  private long deviation;
  
  /** How Vital this task is to overall Customer Happiness **/
  private double vital;
  
  /** The time the system has to complete the task **/
  private long duration;   	
  
  /** Creates a new <code>SingleCustomerTask</code> instance. **/
  public SingleCustomerTask() {}

  /**
   * Gets the current World State for this task.
   * World State values are defined in <code>Constants</code>
   * <br>
   * @return current world state
   *
   */
  public String getWorldState() {
     return this.state;
  }
 
  /**
   * Gets the type of this task
   * Task types are defined in <code>Constants</code>
   * <br>
   * @return current task type
   *
   */
  public String getTaskType() {
     return this.type;
  }

  /**
   * Gets the rate at which the customer injects this task
   * into the system.
   * <br>
   * @return the task rate
   *
   */
  public long getRate() {
     return this.rate;
  }
  
  /**
   * Gets the deviation from the rate of task interjection.
   * To help change the frequency at which tasks are entered into
   * the system, <i>deviation</i> is used to alter the <i>rate</i> at which
   * the task is entered into the system.
   * <br>
   *
   */
  public long getDeviation() {
     return this.deviation;
  }
  
  /**
   * Returns the vitiality of this task.  Vitality is used to determine
   * how vital this task is to the overall customer happiness.
   * This value is a float in the range of 0-1.  Where 0 indicates
   * no effect on Happiness and 1 indicates a great effect on happiness.
   * <br>
   * @return the current vitality
   *
   */
  public double getVitality() {
     return this.vital;
  }
  
  /**
   * Gets the duration that this task should be completed within.
   * If the customer does not receive back a response before the
   * duration is elasped, the customers happiness level will decrease.
   *
   * <br>
   * @return the duration time for this task
   *
   */
  public long getDuration() {
     return this.duration;
  }
  
  /**
   * Sets the world state for this task.  World state values are
   * defined in the <code>Constants</code> file.
   * <br>
   * @param state The world state for this task
   * @see org.cougaar.tools.csmart.Constants
   */
  public void setWorldState(String state) {
     this.state = state;
  }
  
  /**
   * Sets the Task type for this task. Task types are defined
   * in <code>Constants</code>
   * <br>
   * @param type of this task
   *
   */
  public void setTaskType(String type) {
     this.type = type;
  }
  
  /**
   * Sets the rate at which this task is injected into the system
   * by the customer.
   * <br>
   * @param rate this task should be entered into the system
   *
   */
  public void setRate(long rate) {
     this.rate = rate;
  }
  
  /**
   * Sets the deviation from the specified rate to slightly randomize
   * the injection of tasks into the system.
   * <br>
   * @param deviation from the rate of injection
   */
  public void setDeviation(long deviation) {
     this.deviation = deviation;
  }
  
  /**
   * Sets the vitality of this task.  The vitality indicates how vital this
   * task is to overall customer performance.
   * <br>
   * @param vitality of this task
   * @see #getVitality()
   */
  public void setVitality(double vital) {
     this.vital = vital;
  }
  
  /**
   * Sets the duration at which this task is valid.  If the customer
   * does not receive a task response before the duration time, its
   * happiness decreases.
   * <br>
   * @param duration for this task.
   */
  public void setDuration(long duration) {
     this.duration = duration;
  }
    
} // end of SingleCustomerTasks.java



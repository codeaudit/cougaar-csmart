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
package org.cougaar.tools.csmart.runtime.ldm.event;

import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.core.util.UID;
import org.cougaar.core.mts.MessageAddress;

/**
 * Indicates the change in customer satisfaction due to the linked
 * <code>PlanElement</code>'s result. <br>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see org.cougaar.planning.ldm.plan.PlanElement
 * @see org.cougaar.tools.csmart.runtime.plugin.CustomerPlugin
 */
public class HappinessChangeEventImpl implements NewHappinessChangeEvent {
  
  private PlanElement pe = null;

  private long time = 0l;

  private UID id;

  /** Status of the Task response **/   
  private float rating;

  /** Time Task Completed **/
  private long completedTime;

  /** The Current Customer Happiness **/
  private double currentHappiness;

  protected String publisher;
  protected MessageAddress source;

  public HappinessChangeEventImpl(UID uid) {
    this.id = uid;
  }
  
  /**
   * Get the object whose result caused a change in happiness.
   *
   * @return a <code>PlanElement</code>, usually a Task
   */
  public PlanElement getRegarding() {
    return pe;
  }

  public void setRegarding(PlanElement pe) {
    if (this.pe != null) {
      throw new IllegalArgumentException("Regarding already set");
    }
    this.pe = pe;
  }

  // Vis time?
  public long getTime() {
    return time;
  }
  
  public void setTime(long time) {
  if (this.time == 0) {
      throw new IllegalArgumentException("Time already set");
    }
    this.time = time;
  }

  /**
   * The current Happiness Level of the Customer.  Customer
   * Happiness always starts at a (1), the highest happiness level.
   * After each Response to Tasks is received, the Happiness is 
   * recalculated.
   * <br>
   * @return The current Happiness Level of the Customer.
   */
  public double getCurrentHappiness() {
    return this.currentHappiness;
  }

  public void setCurrentHappiness(double hap) {
    if(this.currentHappiness != 0) {
      throw new IllegalArgumentException("Status already set");
    } else {
      this.currentHappiness = hap;
    }
  }

  /**
   * The time that this task was completed.  This should
   * be set whether the task completion was a success or not.
   * <br>
   * <b>Note:</b><i>This will not be the same as the time when this object
   * appears on the Blackboard</i>
   * <br>
   * @return Time that this task was Completed
   */
  public long getTimeCompleted() {
    return this.completedTime;
  }

  public void setTimeCompleted(long time) {
    if(this.completedTime != 0) {
      throw new IllegalArgumentException("Time Completed already set");
    } else {
      this.completedTime = time;
    }
  }

  /**
   * Indicates if the execution of this event was a success (1) or
   * failure (0).
   * <br>
   * @return Rating for the Task response causing the change in Happiness
   */
  public float getRating() {
     return this.rating;
  }

  public void setRating(float rate) {
    if(this.rating != 0) {
        throw new IllegalArgumentException("Status already set");
     } else {
        this.rating = rate;
     }
  }

  // description?
  
  public void setPublisher(String publisher) {
    if (this.publisher != null) {
      throw new IllegalArgumentException("Publisher already set");
    }
    this.publisher = publisher;
  }

  public String getPublisher() {
    return this.publisher;
  }
  
  // origCluster
  public MessageAddress getSource() {
    return source;
  }
  
  public void setSource(MessageAddress source) {
    if (this.source != null) {
      throw new IllegalArgumentException("Source already set");
    }
    this.source = source;
    
  }
  
  // Publishable gives us boolean isPersistable(), for which for now we can say no
  public boolean isPersistable() { return false; }
  
  // UniqueObject gives UID getUID() and void setUID(UID uid)
  public UID getUID() {
    return id;
  }

  public void setUID(UID uid) {
    if (this.id != null) {
      throw new IllegalArgumentException("UID already set");
    }
    this.id = uid;
  }

  /**
   * Give a short (25-35) character description of the Event,
   * one which does not include the Class, times, or ID.
   * For use by the UI.
   *
   * @return a <code>String</code> description
   */
  public String getDescription() {
    return "Happiness delta: " + getRating()
     + " for work done at " + getTimeCompleted() + ", New Happiness: " + getCurrentHappiness();
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("<HappinessChangeEvent ");
    buf.append(getUID().toString());
    buf.append(" Rating Delta: ");
    buf.append(getRating());
    buf.append(" Time Completed: ");
    buf.append(getTimeCompleted());
    buf.append(" Current Happiness: ");
    buf.append(getCurrentHappiness());
    if(getRegarding() != null) {
      buf.append(" Regarding ");
      buf.append(getRegarding().toString());
    }

    buf.append(">");

    return buf.toString();
  }
} // end of HappinessChangeEventImpl


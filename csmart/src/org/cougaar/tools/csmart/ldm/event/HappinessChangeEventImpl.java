/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ldm.event;

import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.core.society.UID;
import org.cougaar.core.cluster.ClusterIdentifier;

/**
 * Indicates the change in customer satisfaction due to the linked
 * <code>PlanElement</code>'s result.<br>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see org.cougaar.domain.planning.ldm.plan.PlanElement
 * @see org.cougaar.tools.csmart.plugin.CustomerPlugIn
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
  protected ClusterIdentifier source;

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
  public ClusterIdentifier getSource() {
    return source;
  }
  
  public void setSource(ClusterIdentifier source) {
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


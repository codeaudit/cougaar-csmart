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

import java.io.Serializable;

import org.cougaar.core.society.UID;
import org.cougaar.core.society.UniqueObject;

import org.cougaar.core.cluster.Publishable;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.plan.PlanElement;

/**
 * Interface that handles HappinessChangeEvents.   <br><br>
 *
 * HappinessChangeEvents are published by the customer whenever
 * its happiness level changes.
 *
 */
public interface HappinessChangeEvent extends Serializable, Publishable, UniqueObject {

  /**
   * Indicates if the execution of this event was a success (1) or
   * failure (0).
   * <br>
   * @return Rating for the Task response causing the change in Happiness
   */
  public float getRating();

  /**
   * The time that this task was completed.  This should
   * be set whether the task completion was a success or not.
   * <br>
   * <b>Note:</b><i>This will not be the same as the time when this object
   * appears on the Blackboard</i>
   * <br>
   * @return Time that this task was Completed
   */
  public long getTimeCompleted();

  /**
   * The current Happiness Level of the Customer.  Customer
   * Happiness always starts at a (1), the highest happiness level.
   * After each Response to Tasks is received, the Happiness is 
   * recalculated.
   * <br>
   * @return The current Happiness Level of the Customer.
   */
  public double getCurrentHappiness();

  /**
   * Get the object whose result caused a change in happiness.
   *
   * @return a <code>PlanElement</code>, usually a Task
   */
  public PlanElement getRegarding();

  // Vis time?
  public long getTime();
  
  // description?
  public String getDescription();
  
  /**
   * Get the <code>String</code> name of the publisher of this <code>Event</code>.
   */
  public String getPublisher();

  /**
   * Get the <code>ClusterIdentifier</code> "source" publisher of 
   * this <code>Event</code>.
   */
  public ClusterIdentifier getSource();

}

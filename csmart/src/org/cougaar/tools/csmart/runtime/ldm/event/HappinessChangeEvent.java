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

import java.io.Serializable;

import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;

import org.cougaar.core.blackboard.Publishable;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.planning.ldm.plan.PlanElement;

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
  float getRating();

  /**
   * The time that this task was completed.  This should
   * be set whether the task completion was a success or not.
   * <br>
   * <b>Note:</b><i>This will not be the same as the time when this object
   * appears on the Blackboard</i>
   * <br>
   * @return Time that this task was Completed
   */
  long getTimeCompleted();

  /**
   * The current Happiness Level of the Customer.  Customer
   * Happiness always starts at a (1), the highest happiness level.
   * After each Response to Tasks is received, the Happiness is 
   * recalculated.
   * <br>
   * @return The current Happiness Level of the Customer.
   */
  double getCurrentHappiness();

  /**
   * Get the object whose result caused a change in happiness.
   *
   * @return a <code>PlanElement</code>, usually a Task
   */
  PlanElement getRegarding();

  // Vis time?
  long getTime();
  
  // description?
  String getDescription();
  
  /**
   * Get the <code>String</code> name of the publisher of this <code>Event</code>.
   */
  String getPublisher();

  /**
   * Get the <code>ClusterIdentifier</code> "source" publisher of 
   * this <code>Event</code>.
   */
  ClusterIdentifier getSource();
}

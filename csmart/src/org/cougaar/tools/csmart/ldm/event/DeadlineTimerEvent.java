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

import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.core.society.UniqueObject;
import org.cougaar.core.cluster.Publishable;
import org.cougaar.core.cluster.ClusterIdentifier;

/**
 * An object whose appearance indicates that a response was expected
 * on the given <code>PlanElement</code>.  It should appear at roughly
 * the time indicated.<br>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see PlanElement
 */
public interface DeadlineTimerEvent extends Serializable, UniqueObject, Publishable {
  // Need access to the thing for which you are expecting an answer
  public PlanElement getRegarding();

  // Time at which this deadline timer goes off
  public long getTime();

  /**
   * Get the <code>String</code> name of the publisher of this <code>Event</code>.
   */
  public String getPublisher();

  /**
   * Get the <code>ClusterIdentifier</code> "source" publisher of 
   * this <code>Event</code>.
   */
  public ClusterIdentifier getSource();


  // Publishable gives us boolean isPersistable(), for which for now we can say no
  // UniqueObject gives UID getUID() and void setUID(UID uid)

  // What about description?
  public String getDescription();
}


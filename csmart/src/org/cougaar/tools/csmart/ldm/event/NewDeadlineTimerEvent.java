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
import org.cougaar.core.cluster.ClusterIdentifier;

/**
 * A timer that appears when some deadline has been reached.  It refers,
 * usually, to a <code>PlanElement</code> with which this deadline
 * is associated.  The typical semantics are that an <code>AllocationResult</code>
 * should bet set on the <code>PlanElement</code> in advance of the deadline.<br>
 * This interface is used for creating new instances.
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see PlanElement
 * @see org.cougaar.tools.csmart.plugin.AllocatorPlugIn
 */
public interface NewDeadlineTimerEvent extends DeadlineTimerEvent {
  public void setRegarding(PlanElement pe);

  /**
   * @param time a <code>long</code> time in milliseconds at which this Event should appear
   */
  public void setTime(long time);

  /**
   * Set the <code>String</code> name of the publisher of this <code>Event</code>.
   */
  public void setPublisher(String publisher);

  /**
   * Set the <code>ClusterIdentifier</code> "source" publisher of 
   * this <code>Event</code>.
   */
  public void setSource(ClusterIdentifier source);

}

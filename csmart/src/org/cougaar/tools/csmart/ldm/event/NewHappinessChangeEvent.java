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
 * Create a new <code>HappinessChangeEvent</code> to indicate the
 * impact on overall customer satisfaction of a Tasks' result.
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 */
public interface NewHappinessChangeEvent extends HappinessChangeEvent {
  public void setRegarding(PlanElement pe);

  public void setTime(long time);

  public void setCurrentHappiness(double hap);

  public void setTimeCompleted(long time);

  public void setRating(float rate);

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

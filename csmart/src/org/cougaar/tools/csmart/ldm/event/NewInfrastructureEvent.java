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

import org.cougaar.domain.planning.ldm.plan.NewDirective;
import org.cougaar.domain.planning.ldm.plan.Plan;

/**
 * Modifiable <code>InfrastructureEvent</code> interface that should only
 * be used <u>before</u> it is published -- once 
 * published an Event is immutable.
 * 
 * @see InfrastructureEvent
 */
public interface NewInfrastructureEvent 
    extends InfrastructureEvent, NewDirective {

  /** 
   * @param aPlan - The Plan for which this Directive is a part of.
   **/
			
  void setPlan(Plan aPlan);
  
  /**
   * Set the <tt>Constants.InfEventType</tt> "type" of this event.
   * <pre>
   * The valid types are:
   *   WIRE_BUSY
   *   WIRE_DOWN
   *   NODE_BUSY
   *   NODE_DOWN
   * </pre>
   * @see org.cougaar.tools.csmart.Constants
   */
  public void setType(String type);
  
  /**
   * Set the duration in milliseconds.
   */
  public void setDuration(long duration);

  /**
   * Set the intensity value (0.0 &lt;= <tt>intensity</tt> &lt;= 1.0).
   */
  public void setIntensity(double intensity);

  /**
   * Set the time in milliseconds at which the given effect should take place
   */
  public void setTime(long time);

  /**
   * Set the <code>String</code> name of the publisher of this <code>Event</code>.
   */
  public void setPublisher(String publisher);

  /**
   * Set the event which should be drawn as the parent of this event
   *
   * @param parent a <code>RealWorldEvent</code> cause
   */
  public void setParent(RealWorldEvent parent);
}

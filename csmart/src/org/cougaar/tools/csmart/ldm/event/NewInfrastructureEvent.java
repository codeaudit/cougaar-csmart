/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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

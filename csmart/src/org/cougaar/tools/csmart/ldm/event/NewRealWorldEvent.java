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
 * Parent for all new Real world adverse events, both kinetic and cyber.  
 * Set the basic attributes of the event with this interface
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see RealWorldEvent
 */
public interface NewRealWorldEvent extends RealWorldEvent, NewDirective {

  /** 
   * @param aPlan - The Plan for which this Directive is a part of.
   **/			
  void setPlan(Plan aPlan);
  
  /**
   * Set the type of adverse event, using a String value
   * from <code>org.cougaar.tools.csmart.Constants</code>
   * <br>
   * @param type a <code>String</code> event type constant
   */
  public void setType(String type);
  
  /**
   * @param time a <code>long</code> time in milliseconds at which this Event should occur
   */
  public void setTime(long time);

  /**
   * Set the <code>String</code> name of the publisher of this <code>Event</code>.
   */
  public void setPublisher(String publisher);
} // end of NewRealWorldEvent.java

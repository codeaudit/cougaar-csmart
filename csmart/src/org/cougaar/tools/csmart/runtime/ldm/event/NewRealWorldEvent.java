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

import org.cougaar.planning.ldm.plan.NewDirective;
import org.cougaar.planning.ldm.plan.Plan;

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
  void setType(String type);
  
  /**
   * @param time a <code>long</code> time in milliseconds at which this Event should occur
   */
  void setTime(long time);

  /**
   * Set the <code>String</code> name of the publisher of this <code>Event</code>.
   */
  void setPublisher(String publisher);
} // end of NewRealWorldEvent.java

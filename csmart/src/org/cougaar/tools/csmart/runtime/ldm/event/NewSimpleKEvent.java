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
package org.cougaar.tools.csmart.runtime.ldm.event;

import org.cougaar.tools.csmart.util.LatLonPoint;

/**
 * Parent for all new kinetic events
 *
 * @author <a href="mailto:wfarrell@bbn.com">Wilson Farrell</a>
 * @see RealWorldEvent
 */
public interface NewSimpleKEvent extends SimpleKEvent, NewKineticEvent {
  /**
   * Set the location of the kinetic event.
   *
   * @param agent a <code>LatLongPoint</code> location of the event
   */
  void setLocation(LatLonPoint location);

  /**
   * Set the peak intensity of the kinetic event, as a value from 0 to 1.
   *
   * @param mag a <code>double</code> peak intensity
   */
  void setIntensity(double mag);

  /**
   * Set the length of the kinetic event, including time for the event
   * to ramp up and down.
   *
   * @param length a <code>long</code> length of the attack in milliseconds
   */
  void setDuration(long length);
}
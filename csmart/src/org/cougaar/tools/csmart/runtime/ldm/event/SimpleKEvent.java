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

import org.cougaar.tools.csmart.util.LatLonPoint;

/**
 * A simple kind of kinetic events whose geoloc does not change over time.
 * Its intensity does not vary and its duration is either instantaneous or
 * of fixed duration.  Its impact is modeled with a cone.
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see RealWorldEvent
 */
public interface SimpleKEvent extends KineticEvent {

  /**
   * gets the geographic location of the kinetic event
   *
   * @return a <code>LatLonPoint</code> geographic location
   */
  LatLonPoint getLocation();

  /**
   * Get the peak intensity of the kinetic event.
   * Values are <code>0</code> to <code>1</code>.
   * @return a <code>double</code> kinetic event peak intensity
   */
  double getIntensity();

  /**
   * Get the duration of the kinetic event, including time for the
   * event to begin to take effect and to wear off.
   *
   * @return a <code>long</code> kinetic event duration in milliseconds
   */
  long getDuration();

} // end of SimpleKEvent.java

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
  public LatLonPoint getLocation();

  /**
   * Get the peak intensity of the kinetic event.
   * Values are <code>0</code> to <code>1</code>.
   * @return a <code>double</code> kinetic event peak intensity
   */
  public double getIntensity();

  /**
   * Get the duration of the kinetic event, including time for the
   * event to begin to take effect and to wear off.
   *
   * @return a <code>long</code> kinetic event duration in milliseconds
   */
  public long getDuration();

} // end of SimpleKEvent.java

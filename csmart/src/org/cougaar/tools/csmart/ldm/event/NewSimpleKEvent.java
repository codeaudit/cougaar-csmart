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
  public void setLocation(LatLonPoint location);

  /**
   * Set the peak intensity of the kinetic event, as a value from 0 to 1.
   *
   * @param mag a <code>double</code> peak intensity
   */
  public void setIntensity(double mag);

  /**
   * Set the length of the kinetic event, including time for the event
   * to ramp up and down.
   *
   * @param length a <code>long</code> length of the attack in milliseconds
   */
  public void setDuration(long length);
}

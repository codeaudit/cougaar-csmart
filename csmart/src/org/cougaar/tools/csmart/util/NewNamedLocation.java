/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.util;

/**
 * Create a new object to hold a location name and position, as a
 * <code>LatLonPoint</code>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see NamedLocation
 * @see LatLonPoint
 */
public interface NewNamedLocation extends NamedLocation {
  /**
   * Set the name of this location
   *
   * @param name a <code>String</code> location name
   */
  public void setName(String name);

  /**
   * Set the position of this location
   *
   * @param pos a <code>LatLonPoint</code>
   */
  public void setPosition(LatLonPoint pos);
} // NewNamedLocation.java

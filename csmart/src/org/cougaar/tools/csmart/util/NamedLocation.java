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
 * A location with a name and a position, as a <code>LatLonPoint</code>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 */
public interface NamedLocation {
  /**
   * Get the name of this location
   *
   * @return a <code>String</code> name
   */
  public String getName();

  /**
   * Get the position of this location
   *
   * @return a <code>LatLonPoint</code> 
   */
  public LatLonPoint getPosition();
} // Named Location.java

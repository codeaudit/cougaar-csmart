/*
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.util;



/**
 * A location (as a <code>LatLonPoint</code>) with a name.
 * Multiple constructors are available.
 * Utility functions to find the distance from this location to another
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see NewNamedLocation
 * @see LatLonPoint
 */
public class NamedLocationImpl implements NewNamedLocation {

  private String name = null; // name of this location
  private LatLonPoint pos = null; // position of this location

  public NamedLocationImpl() {}
  
  /**
   * Creates a new <code>NamedLocationImpl</code> instance.
   * This constructor will try to parse the String to find
   * a name, lat, and long (float degrees). If it cant, it treats the String
   * as a Name.<br>
   * The 3 fields are expected to be separated with a vertical bar (|).<br>
   * If there are errors parsing the lat & long, the default is 0.0 for each.
   *
   * @param name a <code>String</code> location name or composite location, lat, long
   */
  public NamedLocationImpl (String name) {
    if (name.indexOf('|') == -1) {
      //System.err.println("setting name to full thing: " + name);
      this.setName(name);
    } else {
      this.setName(name.substring(0, name.indexOf('|')));
      //System.err.println("Found composite name, set it to " + this.name);
      name = name.substring(name.indexOf('|') + 1);
      String lat = name.substring(0, name.indexOf('|'));
      String lon = name.substring(name.indexOf('|') + 1);
      //System.err.println("lat will be: " + lat + ", long: " + lon);
      float flat = 0.0f;
      float flon = 0.0f;
      try {
	flat = Float.parseFloat(lat);
	flon = Float.parseFloat(lon);
      } catch (NumberFormatException nfe) {
      }
      this.setPosition(new LatLonPoint(flat, flon));
      //System.err.println("Got position: " + getPosition());
    }
  }

  /**
   * Creates a new <code>NamedLocationImpl</code> instance.
   *
   * @param pos a <code>LatLonPoint</code> position
   */
  public NamedLocationImpl(LatLonPoint pos) {
    this.setPosition(pos);
  }

  /**
   * Creates a new <code>NamedLocationImpl</code> instance.
   *
   * @param name a <code>String</code> name
   * @param pos a <code>LatLonPoint</code> 
   */
  public NamedLocationImpl(String name, LatLonPoint pos) {
    this.setPosition(pos);
    this.setName(name);
  }
  
  /**
   * Creates a new <code>NamedLocationImpl</code> instance.
   *
   * @param name a <code>String</code> name
   * @param lat a <code>float</code> latitude in degrees
   * @param lon a <code>float</code> longitude in degrees
   */
  public NamedLocationImpl (String name, float lat, float lon) {
    this.setPosition(new LatLonPoint(lat, lon));
    this.setName(name);
  }

  /**
   * Creates a new <code>NamedLocationImpl</code> instance.
   *
   * @param name a <code>String</code> location name
   * @param lat a <code>float</code> latitude in radians
   * @param lon a <code>float</code> longitdue in radians
   * @param isRadian a <code>boolean</code> that should be true and is always ignored
   */
  public NamedLocationImpl (String name, float lat, float lon, boolean isRadian) {
    this.setPosition(new LatLonPoint(lat, lon, true));
    this.setName(name);
  }

  /**
   * Creates a new <code>NamedLocationImpl</code> instance.
   *
   * @param pt a <code>NamedLocation</code> to copy
   */
  public NamedLocationImpl (NamedLocation pt) {
    this.setPosition(pt.getPosition());
    this.setName(pt.getName());
  }

  /**
   * Creates a new <code>NamedLocationImpl</code> instance.
   *
   * @param name a <code>String</code> name
   * @param lat a <code>double</code> latitude in degrees
   * @param lon a <code>double</code> longitude in degrees
   */
  public NamedLocationImpl (String name, double lat, double lon) {
    this.setPosition(new LatLonPoint(lat, lon));
    this.setName(name);
  }

  /**
   * Get the name of this location, possibly null
   *
   * @return a <code>String</code> location name, possibly null
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of this location, overriding any previous name
   *
   * @param name a <code>String</code> location name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the position of this location
   *
   * @return a <code>LatLonPoint</code> 
   */
  public LatLonPoint getPosition() {
    return pos;
  }

  /**
   * Set the position of this location
   *
   * @param pos a <code>LatLonPoint</code>
   */
  public void setPosition(LatLonPoint pos) {
    this.pos = pos;
  }

  /**
   * Get the distance in kilometers from this point to the given point
   *
   * @param other a <code>LatLonPoint</code> to find the distance to
   * @return a <code>double</code> distance in kilometers
   */
  public double distanceFrom(LatLonPoint other) {
    return EarthConstants.DistanceBetweenLatLonPoints(this.getPosition(), other);
  }
  
  /**
   * Get the distance in kilometers from this point to the given point
   *
   * @param other a <code>NamedLocation</code> value
   * @return a <code>double</code> distance in kilometers
   */
  public double distanceFrom(NamedLocation other) {
    return EarthConstants.DistanceBetweenLatLonPoints(this.getPosition(), other.getPosition());
  }

  /**
   * Get the distance in kilometers from this point to the given point
   *
   * @param lat a <code>double</code> latitude in degrees
   * @param lon a <code>double</code> longitude in degrees
   * @return a <code>float</code> distance in kilometers
   */
  public float distanceFrom(double lat, double lon) {
    return EarthConstants.DistanceBetweenPoints(this.getPosition().getLatitude(), this.getPosition().getLongitude(), (float)lat, (float)lon);
  }

  /**
   * Get the distance in kilometers from this point to the given point
   *
   * @param lat a <code>float</code> latitude in degrees
   * @param lon a <code>float</code> longitude in degrees
   * @return a <code>float</code> distance in kilometers
   */
  public float distanceFrom(float lat, float lon) {
    return EarthConstants.DistanceBetweenPoints(this.getPosition().getLatitude(), this.getPosition().getLongitude(), lat, lon);
  }

  /**
   * 2 <code>NamedLocationImpl</code>s are <code>equals</code> if they have the same name
   * The position is ignored for this purpose.
   *
   * @param obj an <code>Object</code> to compare
   * @return a <code>boolean</code>
   */
  public boolean equals(Object obj) {
    if (obj instanceof NamedLocation) { 
      return getName().equals(((NamedLocation)obj).getName());
    }
    return false;
  }
  
  public String toString() {
    return "NamedLocationImpl[name=" + name + ", pos=" + pos + "]";
  }
} // NamedLocationImpl.java



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
package org.cougaar.tools.csmart.core.cdata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cougaar.planning.ldm.asset.NewPropertyGroup;
import org.cougaar.planning.ldm.asset.PropertyGroup;

// FIXME: An extension of this would be time-phased
// However, there the set of properties would be the same, but the
// values would differ in different time-spans.
// So maybe a TP-PG _contains_ multiple of these guys,
// where for each it has a start + stop time

// Add convenience methods to getAllPropertyNames and getPropertyByName

/**
 * Hold the definition for one property group on an Agent's asset.<br>
 * The PG has a name (the class name), and might have a single value
 * (as is the case for the UniqueNames, etc). Usually, however,
 * it has a list of properties. These properties are instances of
 * <code>PGPropData</code>
 **/
public class PropGroupData implements Serializable, NewPropertyGroup {

  /** Common PG class names **/
  public static final String ITEM_IDENTIFICATION = "ItemIdentificationPG";
  public static final String TYPE_IDENTIFICATION = "TypeIdentificationPG";
  public static final String CLUSTER             = "ClusterPG";
  public static final String ENTITY              = "EntityPG";
  public static final String COMMUNITY           = "CommunityPG";
  public static final String MILITARYORG         = "MilitaryOrgPG";
  public static final String ASSIGNMENT          = "AssignmentPG";
  public static final String ORGANIZATION        = "OrganizationPG";
  public static final String MAINTENANCE         = "MaintenancePG";
  public static final String CSSCAPABILITY       = "CSSCapabilityPG";

  private String name = null;
  private String val = null;
  private List properties;

  /**
   * Creates a new <code>PropGroupData</code> instance.
   *
   * @param name of the Property Group
   */
  public PropGroupData(String name) {
    this.name = name;
    properties = new ArrayList();
  }

  /**
   * Gets the name of the Property Group.
   *
   * @return Name of the Property Group
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the property group
   *
   * @param name of property group
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get a single value for this property group.
   *
   * @return value
   */
  public String getSingleValue() {
    return val;
  }

  /**
   * Determines if this property group has a single value.
   *
   * @return true if only contains one value.
   */
  public boolean hasSingleValue() {
    return val != null;
  }

  /**
   * Sets a single value. For this Property Group
   *
   * @param val of this property group
   */
  public void setSingleValue(String val) {
    this.val = val;
  }
  
  /**
   * Sets all properties for this PropertyGroup
   *
   * @param PGPropData[] array of properties
   */
  public void setProperties(PGPropData[] newProperties) {
    properties.clear();
    for(int i=0; i < newProperties.length; i++) {
      properties.add(newProperties[i]);
    }
  }

  /**
   * Adds a property for this PropertyGroup
   *
   * @param PGPropData property
   */
  public void addProperty(PGPropData property) {
    this.properties.add(property);
  }

  /**
   * Sets a property for this PropertyGroup, replacing the previous property at this index
   *
   * @param int index for property
   * @param PGPropData property to replace with
   */
  public void setProperty(int index, PGPropData property) 
                              throws IndexOutOfBoundsException{
    this.properties.set(index, property);
  }

  /**
   * Returns an array of properties for this PropertyGroup.
   *
   * @return properties
   */
  public PGPropData[] getProperties() {
    return (PGPropData[])properties.toArray(new PGPropData[properties.size()]);
  }

  /**
   * Returns an iterator of properties for this PropertyGroup.
   *
   * @return iterator
   */
  public Iterator getPropertiesIterator() {
    return properties.iterator();
  }

  /**
   * Returns a count of all properties for this PropertyGroup.
   *
   * @return count
   */
  public int getPropertyCount() {
    return properties.size();
  }

  /**
   * Converts PropGroup to a String
   *
   * @return string of Property Group
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("<PropertyGroup: " + name);
    if (hasSingleValue()) {
      buf.append(", value: " + getSingleValue() + ">");
    } else {
      buf.append(", has " + getPropertyCount() + " properties: ");
      Iterator propsIter = getPropertiesIterator();
      while (propsIter.hasNext()) {
	buf.append(((PGPropData)propsIter.next()).toString() + " ");
      }
      buf.append(">");
      // FIXME!!!  Why, whats wrong?

    }
    return buf.toString();
  }

  //Stub NewPropertyGroup implementation
  
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  /** Unlock the PropertyGroup by returning an object which
   * has setter methods that side-effect this object.
   * The key must be == the key that locked the property
   * in the first place or an Exception is thrown.
   * @exception IllegalAccessException
   **/
  public NewPropertyGroup unlock(Object key) {
    return null;
  }

  /** lock a property by returning an immutable object which
   * has a private view into the original object.
   * If key == null, the result is a locked object which cannot be unlocked.
   **/
  public PropertyGroup lock(Object key) {
    return null;
  }

  /** alias for lock(null)
   **/
  public PropertyGroup lock() {
    return null;
  }

  /** Convenience method. equivalent to clone();
   **/
  public PropertyGroup copy() {
    try {
      return (PropertyGroup) clone();
    } catch (CloneNotSupportedException cnse) { return null;}
  }


  /** returns the class of the main property interface for this 
   * property group.  
   **/
  public Class getPrimaryClass() {
    return this.getClass();
  }

  /** @return the method name on an asset to retrieve the PG **/
  public String getAssetGetMethod() {
    return "get" + name;
  }

  /** @return the method name on an asset to set the PG **/
  public String getAssetSetMethod() {
    return "set" + name;
  }
    

  // DataQuality
  /** @return true IFF the instance not only supports DataQuality
   * queries (e.g. is instanceof HasDataQuality), but getDataQuality()
   * will return non-null.
   **/
  public boolean hasDataQuality() {
    return false;
  }

} // end of PropGroupData.java

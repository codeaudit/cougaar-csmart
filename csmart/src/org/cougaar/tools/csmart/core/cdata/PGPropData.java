/* 
 * <copyright>
 * Copyright 2001-2002 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).

 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.tools.csmart.core.cdata;

import java.io.Serializable;

/**
 * Holds the data for one property in a property group
 * The Value is usually a <code>String</code> but could
 * be a list of values.
 * The type might be one of the list types,
 * in which case there is a sub-type.
 **/
public class PGPropData implements Serializable {
  private String name = null; // Name of this field
  private String type = null; // Class of the field
  private String subtype = null; // if type is a list, class of elements
  private Object value = null; // Usu. a String
  private static final String[] listtypes = {"Collection", "List"};

  /**
   * Creates a new <code>PGPropData</code> instance.
   *
   */
  public PGPropData() {
  }

  /**
   * Sets the name of the property.
   *
   * @param name of the property
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the name of the property.
   *
   * @return the property name.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the type of the property.
   *
   * @param type of property.
   */
  public void setType(String type) {
    this.type = type;
    // check if its a list type here?
  }

  /**
   * Gets the type of the property.
   *
   * @return the property type.
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the type of the property when
   * the type is a list.
   *
   * @param list type
   * @param subtype of elements in the list.
   */
  public void setType(String list, String subtype) {
    this.type = list;
    this.subtype = subtype;
  }
  
  /**
   * Gets the property type when the property
   * contains a list of values.
   *
   * @return subtype of property value.
   */
  public String getSubType() {
    return subtype;
  }

  /**
   * Sets the subtype.
   *
   * @param subtype of property
   */
  public void setSubType(String subtype) {
    this.subtype = subtype;
  }

  /**
   * Determines if the value is a known listtype
   * The known list types are:
   * <code>Collection</code>, <code>List<code>
   * 
   * @return True if property is a listtype
   */
  public boolean isListType() {
    if (type == null)
      return false;
    for (int i = 0; i < listtypes.length; i++) {
      if (listtypes[i].equalsIgnoreCase(type))
	return true;
    }
    return false;
  }
  
  /**
   * Sets the value of the property.
   *
   * @param val of the property.
   */
  public void setValue(Object val) {
    // check that its a String of PGPropMultiVal?
    if(isListType() && !(val instanceof PGPropMultiVal)) {
      throw new RuntimeException("Value must be PGPropMultiVal for Lists");
    } else {
      this.value = val;
    }
  }

  /**
   * Returns the value of the property.
   *
   * @return property value.
   */
  public Object getValue() {
    return value;
  }

  /**
   * String of the Property.
   *
   * @return a <code>String</code> of the property.
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("Value: " + getName() + " of type " + getType());
    if (isListType()) {
      buf.append(", subtype: " + getSubType());
    }
    buf.append(" with value " + getValue().toString() + ">");
    return buf.toString();
  }
} //end of PGPropData.java

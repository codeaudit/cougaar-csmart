/* 
 * <copyright>
 * Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.ui.component;

import java.io.Serializable;

/**
 * Hold the data for one property in a property group
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

  public PGPropData() {
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setType(String type) {
    this.type = type;
    // check if its a list type here?
  }

  public String getType() {
    return type;
  }

  public void setType(String list, String subtype) {
    this.type = list;
    this.subtype = subtype;
  }
  
  public String getSubType() {
    return subtype;
  }

  public void setSubType(String subtype) {
    this.subtype = subtype;
  }

  public boolean isListType() {
    if (type == null)
      return false;
    for (int i = 0; i < listtypes.length; i++) {
      if (listtypes[i].equals(type))
	return true;
    }
    return false;
  }
  
  public void setValue(Object val) {
    // check that its a String of PGPropMultiVal?
    if(isListType() && !(val instanceof PGPropMultiVal)) {
      throw new RuntimeException("Value must be PGPropMultiVal for Lists");
    } else {
      this.value = val;
    }
  }

  public Object getValue() {
    return value;
  }

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

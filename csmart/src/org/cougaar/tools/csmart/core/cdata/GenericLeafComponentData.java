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

/**
 * Generic implementation of LeafComponentData.
 *
 * @see LeafComponentData
 */
public class GenericLeafComponentData implements LeafComponentData, Serializable {

  private String type = null;
  private String name = null;
  private Object value = null;

  /**
   * Creates a new <code>GenericLeafComponentData</code> instance.
   *
   */
  public GenericLeafComponentData() {
  }

  /** 
   * Gets the type of the leaf component.
   * Convenience types are defined in the interface.
   * 
   * @return Component type
   **/
  public String getType() {
    return this.type;
  }

  /**
   * Sets the type of the leaf component.
   *
   * @param String component type.
   */
  public void setType(String type) {
    this.type = type;
  }

  /** 
   * Name of Component.  For example,
   * if the leaf is a file, the name
   * is the name of the file.
   *
   * @return name of the component.
   **/
  public String getName() {
    return this.name;
  }
  
  /**
   * Sets the name of the component.
   *
   * @param String name of the component.
   */
  public void setName(String name) {
    this.name = name;
  }

  /** 
   * Value of Component.  For a file,
   * this is the file contents.
   *
   * @return component value
   **/
  public Object getValue() {
    return this.value;
  }

  /**
   * Sets the value of the component.
   *
   * @param Object value of the component
   */
  public void setValue(Object val) {
    this.value = val;
  }

  /**
   * Determines if two GenericLeafComponents are equal.
   *
   * @param o an GenericLeafComponent
   * @return a <code>boolean</code> value
   */
  public boolean equals(Object o) {
    if (o instanceof GenericLeafComponentData) {
      GenericLeafComponentData that = (GenericLeafComponentData)o;
      if (this.getName().equals(that.getName()) && 
          this.getType().equals(that.getType()) &&
          this.getValue().equals(that.getValue())) {
	return true;
      }
    }
    return false;
  }
}

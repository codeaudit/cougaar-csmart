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
package org.cougaar.tools.csmart.core.cdata;

import java.lang.IndexOutOfBoundsException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.cougaar.tools.csmart.util.ArgValue;
import java.io.Serializable;

/**
 * Holds a single set of values. These values might be
 * <code>String</code>s, or might be name-value pairs.
 **/
public class PGPropMultiVal implements Serializable {
  private List values; // of Strings or of name/value pairs
  public PGPropMultiVal() {
    values = new ArrayList();
  }

  /**
   * Sets all values for this Property
   *
   * @param Object[] array of values
   */
  public void setValues(Object[] newValues) {
    values.clear();
    for(int i=0; i < newValues.length; i++) {
      // check that newValues[i] one of String or ArgValue??
      // FIXME!!
      values.add(newValues[i]);
    }
  }

  /**
   * Adds a value for this Property
   *
   * @param String value
   */
  public void addValue(String value) {
    this.values.add(value);
  }

  /**
   * Adds a value for this Property
   *
   * @param ArgValue value
   */
  public void addValue(ArgValue value) {
    this.values.add(value);
  }

  /**
   * Sets a value for this Property, replacing the previous value at this index
   *
   * @param int index for value
   * @param String value to replace with
   */
  public void setValue(int index, String value) 
                              throws IndexOutOfBoundsException{
    this.values.set(index, value);
  }

  /**
   * Sets a value for this Property, replacing the previous value at this index
   *
   * @param int index for value
   * @param ArgValue value to replace with
   */
  public void setValue(int index, ArgValue value) 
                              throws IndexOutOfBoundsException{
    this.values.set(index, value);
  }

  /**
   * Returns an array of values for this property.
   *
   * @return values
   */
  public String[] getValuesStringArray() {
    // Do type checking!!!
    return (String[])values.toArray(new String[values.size()]);
  }

  /**
   * Returns an array of values for this property.
   *
   * @return values
   */
  public ArgValue[] getValuesArgValueArray() {
    // Do type checking!!
    return (ArgValue[])values.toArray(new ArgValue[values.size()]);
  }

  /**
   * Returns an array of values for this property.
   *
   * @return values
   */
  public Object[] getValuesObjectArray() {
    // Do type checking!!
    return values.toArray();
  }

  /**
   * Returns an iterator of values for this property.
   *
   * @return iterator
   */
  public Iterator getValuesIterator() {
    return values.iterator();
  }

  /**
   * Returns a count of all values for this property.
   *
   * @return count
   */
  public int getValueCount() {
    return values.size();
  }
  
  public String toString() {
    StringBuffer buf = new StringBuffer();
    if(values.size() > 0) {
      buf.append(values.get(0));
      for (int i = 1; i < values.size(); i++) {
	buf.append(", " + values.get(i));
      }
    }
    return buf.toString();
  }
  
} // end of PGPropMultiVal.java


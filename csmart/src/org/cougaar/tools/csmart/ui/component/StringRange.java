/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.component;

/**
 * Implementation of Range for Strings.  Note that a StringRange
 * always has a single String value.
 */

public class StringRange implements Range {
  static final long serialVersionUID = -7351616054609766392L;

  String value;

  public StringRange(String value) {
    this.value = value;
  }

  /**
   * Get the minimum value of the range. Values are allowed to be
   * equal to the minimum value
   * @return an object having the minimum value
   **/

  public Object getMinimumValue() {
    return value;
  }

  /**
   * Get the maximum value of the range. Values are allowed to
   * be equal to the maximum value
   * @return an object having the maximum value
   **/

  public Object getMaximumValue() {
    return value;
  }

  /**
   * Test if an Object is in this Range
   * @param o the Object to test
   **/
  
  public boolean isInRange(Object o) {
    return value.equals(o);
  }

  public String toString() {
    return value;
  }
}


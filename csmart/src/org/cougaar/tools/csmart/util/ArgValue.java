/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

import java.io.Serializable;

/**
 * An argument name (a String), and its value, an arbitrary object (possibly null).
 * Construct one with the arg name and the Object value.  Alternatively, a convenience
 * constructor allows you to supply a string, and a separator in that string
 * which separates the Arg and the Value.
 * Note that the argument cannot be null, and in this case the value is a String
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @version 1.0
 * @see Serializable
 */
public class ArgValue implements Serializable {
  private String arg;
  private Object value;

  /**
   * Creates a new <code>ArgValue</code> instance.
   *
   * @param arg a <code>String</code> argument (non-null)
   * @param value an <code>Object</code> value
   */
  public ArgValue(String arg, Object value) {
    if (arg == null) {
      // error!
      throw new IllegalArgumentException("Need a non-null argument");
    }
    this.arg = arg;
    this.value = value;
  }

  /**
   * Creates a new <code>ArgValue</code> instance.
   * The pair is a string with both arg and value in it, with an arbitrary separator.
   * Note that this works only for the case where the value is a String
   *
   * @param pair a <code>String</code> Containing both Arg and Value
   * @param sep a <code>String</code> that separates Arg and Value
   */
  public ArgValue(String pair, String sep) {
    // the pair must be split on sep to produce arg and value
    if (pair == null || sep == null) {
      // error!
      throw new IllegalArgumentException("Can't parse with null Strings");
    }
    
    int start = pair.indexOf(sep);
    if (start < 1) {
      // error!
      throw new IllegalArgumentException("Separator not found, or null Argument");
    }
    
    int stop = start + sep.length();

    //this(pair.substring(0, start), pair.substring(stop, pair.length() - 1));
    this.arg = pair.substring(0, start);
    if (this.arg == null) {
      throw new IllegalArgumentException("Need a non-null argument");
    }
    this.value = pair.substring(stop, pair.length());
  }

  /**
   * Creates a new <code>ArgValue</code> instance.
   * Take a string containg both the argument and the value
   * Assume the separator is " = "
   * Note that this works only for the case where the value is a String
   *
   * @param pair a <code>String</code> with arg and value
   */
  public ArgValue(String pair) {
    this(pair, " = ");
  }
  
  /**
   * @return a <code>String</code> Argument
   */
  public String getArg() {
    return this.arg;
  }

  /**
   * @return an <code>Object</code> Value, possibly null
   */
  public Object getValue() {
    return this.value;
  }

  /**
   * Override the current value for this ArgValue pair with a new value
   * Use with caution!
   *
   * @param val an <code>Object</code> value for this ArgValue pair
   */
  public void setValue(Object val) {
    this.value = val;
  }

  /**
   * Two ArgValues are equal if BOTH the Argument and the value are String.equals
   *
   * @param o an <code>Object</code> to compare
   * @return a <code>boolean</code>, true if equal
   */
  public boolean equals(Object o) {
    if (o instanceof ArgValue) {
      ArgValue t = (ArgValue)o;
      if (t.getValue() != null) {
	return ((t.getValue().equals(this.value)) && (t.getArg().equals(this.arg)));
      } else {
	return (this.value == null && (t.getArg().equals(this.arg)));
      }
    }
    return super.equals(o);
  }
  
  public String toString() {
    if (this.value != null) {
      return (this.arg + " = " + this.value.toString());
    } else {
      return (this.arg + " = (null)");
    }
  }
} // ArgValue.java

    

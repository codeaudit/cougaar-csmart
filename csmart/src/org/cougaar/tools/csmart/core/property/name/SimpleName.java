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

package org.cougaar.tools.csmart.core.property.name;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.HashMap;

/**
 * An implementation of CompositeName have exactly one element that is
 * a String.
 **/
public class SimpleName implements CompositeName {
  static final long serialVersionUID = 3189070403185567285L;

  private String name;

  public SimpleName(String name) {
    this.name = name;
  }
  /**
   * Get the number of elements of the name.
   * @return always 1
   **/
  public int size() {
    return 1;
  }

  /**
   * Get the n-th element of the name
   * @param n the element index
   * @return the Name of the selected element
   **/
  public CompositeName get(int n) {
    if (n != 0) throw new IllegalArgumentException("Bad index: " + n);
    return this;
  }

  public CompositeName getPrefix() {
    return null;
  }

  public CompositeName getPrefix(int n) {
    if (n != 0) throw new IllegalArgumentException("Bad index: " + n);
    return this;
  }

  public CompositeName last() {
    return this;
  }

  /**
   * Test if this name ends with the given name
   * @param name the name to compare to
   * @return true if the final elements of this name are equal to
   * the elements of the given name
   **/
  public boolean endsWith(CompositeName o) {
    if (o.size() > 1) return false;
    return name.toString().endsWith(o.toString());
  }

  /**
   * Test if this name starts with the given name
   * @param name the name to compare to
   * @return true if the initial elements of this name are equal to
   * the elements of the given name
   **/
  public boolean startsWith(CompositeName o) {
    if (o.size() > 1) return false;
    return name.toString().startsWith(o.toString());
  }

  /**
   * Test two names for equality. Names may be of different classes
   * as long as they are equivalent
   * @param name to be compared with. Must be a CompositeName.
   **/
  public boolean equals(Object o) {
    // short-circuit some common cases
    if (this == o) return true;
    if (o instanceof SimpleName && ((SimpleName)o).name == name) return true;

    if (o instanceof CompositeName) {
      return compareTo(o) == 0;
    }
    return false;
  }

  public int hashCode() {
    return name.hashCode();
  }

  /**
   * Compare to another CompositeName. The difference is the
   * difference of our name and the first element of the test name
   * unless they are equal. If the test name is longer it is then
   * greater.
   **/
  public int compareTo(Object o) {
    CompositeName cn = (CompositeName) o;
    int diff = name.compareTo(cn.get(0).toString());
    if (diff == 0 && cn.size() > 1) return -1;
    return diff;
  }

  public String toString() {
    return name;
  }

  public void decache() {}      // noop

  // implement a factory
  private static final HashMap internedNames = new HashMap(97);
  
  /**
   * Get a new <code>SimpleName</code> from an interned list by name
   **/
  public static SimpleName getSimpleName(String name) {
    name = name.intern();
    synchronized (internedNames) {
      SimpleName sn = (SimpleName) internedNames.get(name);
      if (sn == null) {
        sn = new SimpleName(name);
        internedNames.put(name,sn);
      }
      return sn;
    }
  }

  // just do the default for now.
  private void writeObject(ObjectOutputStream stream)
     throws IOException
  {
    stream.defaultWriteObject();
  }

  // just do the default for now.
  private void readObject(ObjectInputStream stream)
    throws IOException, ClassNotFoundException
  {
    stream.defaultReadObject();
  }

  /** keep the names interned **/
  private Object readResolve() throws ObjectStreamException {
    return getSimpleName(name);
  }
 
}

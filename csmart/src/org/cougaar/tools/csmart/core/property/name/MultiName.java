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



/**
 * A CompositeName composed from a parent name and a SimpleName. This
 * class is abstract; the method for getting the parent name is left
 * open so that part of the name can be supplied from subclasses.
 **/
public abstract class MultiName implements CompositeName {
  static final long serialVersionUID = 2707819060093765374L;

  private SimpleName sname;

  protected MultiName(SimpleName name) {
    setName(name);
  }

  public void setName(SimpleName newName) {
    sname = newName;
    decache();                  // just to be certain, decache the hashcode
  }

  protected abstract CompositeName getParentName();

  private transient volatile int _size = 0;

  /**
   * Get the number of elements of the name.
   * @return the number elements of the name
   **/
  public int size() {
    if (_size==0) {
      CompositeName parent = getPrefix();
      if (parent == null){
        _size = sname.size();
      } else {
        _size = sname.size() + parent.size();
      }
    }
    return _size;
  }

  /**
   * Get the n-th element of the name
   * @param n the element index
   * @return the Name of the selected element
   **/
  public CompositeName get(int n) {
    int size = size();
    if (n >= size) throw new IllegalArgumentException("Index out of range: " + n + ">=" + size);
    if (n < 0) throw new IllegalArgumentException("Negative index: " + n);
    CompositeName thisp = this;
    for (int i=(size-1); i>n; i--) {
      thisp = thisp.getPrefix();
    }
    return thisp.last();
  }

  public CompositeName getPrefix() {
    return getParentName();
  }

  public CompositeName getPrefix(int n) {
    int size = size();
    if (n >= size) throw new IllegalArgumentException("Index out of range: " + n + ">=" + size);
    if (n < 0) throw new IllegalArgumentException("Negative index: " + n);
    CompositeName name = this;
    int end = (size-1)-n;
    for (int i = 0; i < end; i++)
      name = name.getPrefix(); // fix typo
    return name;
  }

  public CompositeName last() {
    return sname;
  }

  /**
   * Test if this name ends with the given name
   * @param that the name to compare to
   * @return true if the final elements of this name are equal to
   * the elements of the given name
   **/
  public boolean endsWith(CompositeName that) {
    int thatSize = that.size();
    int thisSize = size();
    if (thatSize > thisSize) return false; // test name has too many components
    
    CompositeName thisp = this;
    CompositeName thatp = that;
    for (int i = thatSize; i>0; i--) {
      CompositeName thisel = thisp.last();
      CompositeName thatel = thisp.last();
      if (! thisel.equals(thatel)) {
        return false;
      }
      thisp = thisp.getPrefix();
      thatp = thatp.getPrefix();
    }
    return true;
  }

  /**
   * Test if this name starts with the given name
   * @param that the name to compare to
   * @return true if the initial elements of this name are equal to
   * the elements of the given name
   **/
  public boolean startsWith(CompositeName that) {
    int thatSize = that.size();
    int thisSize = size();
    if (thatSize > thisSize) return false; // test name has too many components

    // backup thisp to the right spot.
    CompositeName thisp = this;
    for (int i=(thisSize-thatSize); i>0; i--) {
      thisp = thisp.getPrefix();
    }

    CompositeName thatp = that;
    for (int i = thatSize; i>0; i--) {
      CompositeName thisel = thisp.last();
      CompositeName thatel = thisp.last();
      if (! thisel.equals(thatel)) {
        return false;
      }
      thisp = thisp.getPrefix();
      thatp = thatp.getPrefix();
    }
    return true;
  }

  /**
   * Test two names for equality. Names may be of different classes
   * as long as they are equivalent
   * @param o Object to be compared with. Must be a CompositeName.
   **/
  public boolean equals(Object o) {
    if (o == this) return true; // minor optimization
    if (o instanceof CompositeName) {
      int diff = compareTo(o);
      return diff == 0;
    }
    return false;
  }

  /**
   * Compare two CompositeNames element by element.
   **/
  public int compareTo(Object o) {
    CompositeName that = (CompositeName) o;
    int thatSize = that.size();
    int thisSize = size();
    int sz = Math.min(thatSize, thisSize);
    int diff = 0;
    for (int i = 0; diff == 0 && i < sz; i++) {
      Object tn = that.get(i);
      if (tn == null) {
        return 1;
      }
      diff = get(i).compareTo(tn);
    }
    if (diff == 0) diff = thisSize - thatSize;
    return diff;
  }

  /**
   * For debugging, print this name as a series of dot separated strings.
   **/
  public String toString() {
    if (_tostring == null) {
      _tostring = toStringBuffer(new StringBuffer()).toString();
    } 
    return _tostring;
  }

  private transient volatile String _tostring = null;

  protected StringBuffer toStringBuffer(StringBuffer buf) {
    CompositeName parentName = getParentName();
    if (parentName instanceof MultiName) {
      return ((MultiName) parentName).toStringBuffer(buf)
        .append('.')
        .append(sname);
    }
    if (parentName != null) buf.append(parentName.toString()).append('.');
    return buf.append(sname);
  }


  /** cache the hashcode.  Decached by decache() **/
  private transient volatile int _hc = 0;

  public int hashCode() {
    if (_hc == 0) {
      int result = sname.hashCode();
      CompositeName pname = getParentName();
      if (pname != null) result += pname.hashCode() * 7;
      if (result == 0) result = 1;
      _hc = result;
    }
    return _hc;
  }

  /** wipe the hashcode cache **/
  public void decache() {
    _hc = 0; 
    _tostring = null;
    _size = 0;
  }
} 

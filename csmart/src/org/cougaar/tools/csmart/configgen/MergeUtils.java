/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.configgen;

import java.util.*;

/**
 * Some utilities for managing roles, which are Objects that are:<pre>
 *   - null
 *   - String
 *   - a List of Strings, each unique within the List</pre>.
 * <p>
 * FIXME currently some lazy implementations in here that don't quite
 * live up to the specs.  This is fine for now.
 */
public class MergeUtils {

  public static void main(String[] args) {
    // test?
  }


  /**
   * Checks <tt>isValid(o)</tt>, and if <tt>false</tt> then an 
   * <code>Exception</code> is thrown.
   *
   * @see #isValid(Object)
   */
  public static final void assertIsValid(Object o) {
    if (!(isValid(o))) {
      throw new IllegalArgumentException(
          "Invalid: "+o);
    }
  }

  /**
   * check.
   * <pre>
   * test if <tt>o</tt> is:
   *   - null
   *   - a String
   *   - a List of Strings, each unique within the List
   * </pre>
   */
  public static final boolean isValid(Object o) {
    if ((o == null) ||
        (o instanceof String)) {
      return true;
    } else if (o instanceof List) {
      // FIXME should check elements...
      return true;
    } else {
      return false;
    }
  }

  /**
   * Utility to convert an Object to a comma-separated String.
   */
  public static final String toString(
      final Object o) {
    if (o == null) {
      return "";
    } else if (o instanceof String) {
      return (String)o;
    } else if (o instanceof List) {
      List l = (List)o;
      int nl = l.size();
      if (nl <= 0) {
        return "";
      } else {
        String s0 = (String)l.get(0);
        if (nl == 1) {
          return s0;
        } else {
          StringBuffer buf = new StringBuffer();
          buf.append(s0);
          for (int i = 1; i < nl; i++) {
            buf.append(", ");
            buf.append((String)l.get(i));
          }
          return buf.toString();
        }
      }
    } else {
      return "?";
    }
  }

  /**
   * clone.
   * <pre>
   * Assumes:
   *   - <tt>(isValid(o) == true)</tt>.
   *
   * If <tt>o</tt> is a List then the List is cloned.
   * </pre>
   */
  public static final Object clone(Object o) {
    if (o instanceof List) {
      return new ArrayList((List)o);
    } else {
      return o;
    }
  }

  /**
   * contains.
   * <pre>
   * Assumes:
   *   - <tt>(isValid(A) == true)</tt>.
   *   - <tt>(isValid(B) == true)</tt>.
   * 
   * Returns <tt>true</tt> iff:
   *   - all Strings contained in B are contained within A.
   * </pre>
   */
  public static final boolean contains(Object a, Object b) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * merge.
   * <pre>
   * Assumes:
   *   - <tt>(isValid(A) == true)</tt>.
   *   - <tt>(isValid(B) == true)</tt>.
   * 
   * A and B merged into a single "resultObject" that is:
   *   - <tt>(isValid(resultObject) == true)</tt>.
   *   - <tt>(contains(resultObject, A) == true)</tt>.
   *   - <tt>(contains(resultObject, B) == true)</tt>.
   *
   * Note:
   *   - either A, B, or both might be modified and used within
   *     the resultObject.
   * </pre>
   */
  public static final Object merge(Object a, Object b) {
    // FIXME inefficient and lazy...
    List la = makeLazyList(a);
    int nla = ((la != null) ? la.size() : 0);
    if (nla <= 0) {
      return b;
    }
    List lb = makeLazyList(b);
    int nlb = ((lb != null) ? lb.size() : 0);
    if (nlb <= 0) {
      return a;
    }
    for (int j = 0; j < nlb; j++) {
      String sbj = (String)lb.get(j);
      for (int i = 0; ; i++) {
        if (i >= nla) {
          la.add(sbj);  // modify a!
          break;
        }
        String sai = (String)la.get(i);
        if (sai.equals(sbj)) {
          break;
        }
      }
    }
    return la;
  }

  /** lazy utility for <tt>merge</tt>. */
  private static final List makeLazyList(Object o) {
    // FIXME inefficient and lazy...
    if (o == null) {
      return null;
    } else if (o instanceof String) {
      List l = new ArrayList(1);
      l.add(o);
      return l;
    } else if (o instanceof List) {
      return (List)o;
    } else {
      return null;
    }
  }
}

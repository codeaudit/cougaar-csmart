/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.util;

import java.util.*;

public final class Sorting {

  private Sorting() {
    // just utility methods!
  }

  public static final void sort(final List list, final Comparator c) {
    // just use the Collections implementation
    Collections.sort(list, c);
  }

  /**
   * Sort a <code>List</code> of <code>Comparable</code> elements, 
   * writing the result back into the <tt>list</tt>.
   * <p>
   * Currently uses quicksort (<tt>quicksort(List,int,int)</tt>).
   *
   * @param list A List containing Comparable elements
   *
   * @throws ClassCastException if the List contains an element
   *   that doesn't implement <code>Comparable</code>, or the
   *   elements refuse to compare one another (within their
   *   <tt>Comparable.compareTo(Object)</tt> implementations).
   */
  public static final void sort(final List list) {
    int high = ((list != null) ? list.size() : 0);
    if (high > 1) {
      quicksort(list, 0, (high-1));
    }
  }

  /**
   * Quicksort a <code>List</code> of <code>Comparable</code> 
   * elements for the specified range, recursively calling 
   * <tt>quicksort(List,int,int)</tt> as required.
   * 
   * @param list non-null List containing Comparable elements
   * @param low offset in <tt>list</tt> that is &gt;= 0 and 
   *    &lt; <tt>list.size()</tt>.
   * @param high offset in <tt>list</tt> that is 
   *    &lt;= <tt>list.size()</tt> and &gt; <tt>low</tt>.
   *
   * @throws ClassCastException if the List contains an element
   *   that doesn't implement <code>Comparable</code>, or the
   *   elements refuse to compare one another (within their
   *   <tt>Comparable.compareTo(Object)</tt> implementations).
   *
   * @see #sort(List)
   */
  public static final void quicksort(
      final List list, final int low, final int high) {
    if (low >= high) {
      return;
    }
    // partition
    Comparable cur = (Comparable)list.get(low);
    int l = low-1;
    int h = high+1;
    while (true) {
      Object hElem;
      do {
        hElem = list.get(--h);
      } while (cur.compareTo(hElem) < 0);
      Object lElem;
      do {
        lElem = list.get(++l);
      } while (cur.compareTo(lElem) > 0);
      if (l >= h) {
        // middle is h
        break;
      }
      // swap
      list.set(l, hElem);
      list.set(h, lElem);
    }
    // recurse
    quicksort(list, low, h);
    quicksort(list, h+1, high);
  }
}

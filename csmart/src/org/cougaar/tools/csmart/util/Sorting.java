/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.tools.csmart.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.component;

import java.io.Serializable;

/**
 * The interface to the name of a configurable component.
 **/
public interface CompositeName extends Comparable, Serializable {
    /**
     * Get the number of elements of the name.
     * @return the number elements of the name
     **/
    int size();

    /**
     * Get the n-th element of the name
     * @param n the element index
     * @return the Name of the selected element
     **/
    CompositeName get(int n);

    /**
     * Get all but the last element of the name
     * @return the name minus the last element
     */
    CompositeName getPrefix();

  /**
   * Get the first n elements of the name.
   * @return a CompositeName which is the first n elements of the name
   */
  CompositeName getPrefix(int n);

    /**
     * Get the last element of the name
     * @return the Name of the selected element
     **/
    CompositeName last();

    /**
     * Test if this name ends with the given name
     * @param name the name to compare to
     * @return true if the final elements of this name are equal to
     * the elements of the given name
     **/
    boolean endsWith(CompositeName name);

    /**
     * Test if this name starts with the given name
     * @param name the name to compare to
     * @return true if the initial elements of this name are equal to
     * the elements of the given name
     **/
    boolean startsWith(CompositeName name);

    /**
     * Test two names for equality. Names may be of different classes
     * as long as they are equivalent
     * @param name to be compared with. Must be a CompositeName.
     **/
    boolean equals(Object o);
} 

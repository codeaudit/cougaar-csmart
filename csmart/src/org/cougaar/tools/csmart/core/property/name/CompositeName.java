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

package org.cougaar.tools.csmart.core.property.name;

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
   * @param o Object to be compared with. Must be a CompositeName.
   **/
  boolean equals(Object o);

  /** 
   * Flush any information cached about this name, presumably because the
   * prefix has changed in some way.
   **/
  void decache();

} 

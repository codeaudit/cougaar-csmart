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
import java.util.Collection;

/**
 * Interface that defines all component, parent and child operations. 
 */

public interface ComposableComponent extends Serializable {

  /** Component Operations **/
  
  /**
   * Iterates through all components returning all leaf components
   * of the requested class.  
   *
   * @param cls class of descendants to obtain
   * @param c collection to add descendants to
   * @return collection of all descendants
   */
  public Collection getDescendentsOfClass(Class cls, Collection c);

  /**
   * Iterates through all components returning all leaf components
   * of the requested class.  
   *
   * @param cls class of Descendants to obtain
   * @return collection of all descendants 
   */
  public Collection getDescendentsOfClass(Class cls);

  /** Parent Operations **/

  /**
   * Sets the parent of this component.
   * If the parent name is changed, all property names need
   * to be rehashed to reflect the new parent name.
   *
   * @param newParent New parent name.
   */
  public void setParent(ConfigurableComponent newParent);

  /**
   * Returns the parent of this component.
   *
   * @return parent of this component.
   */
  public ConfigurableComponent getParent();
 
  /** Child Operations **/

  /**
   * Add a child to this component.
   * @param c the child to add
   * @return child count.
   */
  public int addChild(ConfigurableComponent c);

  /**
   * Removes a child from this component.
   * @param index of child to remove.
   */
  public void removeChild(int childIndex);

  /**
   * Removes a child from this component.
   * @param c Child to remove
   */
  public void removeChild(ConfigurableComponent c);

  /**
   * Removes all children from this component.
   */
  public void removeAllChildren();

  /**
   * Returns a child of this component.
   * @param childIndex Index of the child within this component.
   */
  public ConfigurableComponent getChild(int childIndex);

  /**
   * Returns a child of this component.
   * @param childName Name of child to retrieve
   */
  public ConfigurableComponent getChild(CompositeName childName);

  /**
   * Count of all children in this component.
   * @return child count.
   */
  public int getChildCount();
}

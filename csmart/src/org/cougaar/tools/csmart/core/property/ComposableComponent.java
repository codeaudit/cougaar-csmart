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

package org.cougaar.tools.csmart.core.property;

import org.cougaar.tools.csmart.core.property.name.CompositeName;

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
  Collection getDescendentsOfClass(Class cls, Collection c);

  /**
   * Iterates through all components returning all leaf components
   * of the requested class.  
   *
   * @param cls class of Descendants to obtain
   * @return collection of all descendants 
   */
  Collection getDescendentsOfClass(Class cls);

  /** Parent Operations **/

  /**
   * Sets the parent of this component.
   * If the parent name is changed, all property names need
   * to be rehashed to reflect the new parent name.
   *
   * @param newParent New parent name.
   */
  void setParent(ComposableComponent newParent);

  /**
   * Returns the parent of this component.
   *
   * @return parent of this component.
   */
  ComposableComponent getParent();
 
  /** Child Operations **/

  /**
   * Add a child to this component.
   * @param c the child to add
   * @return child count.
   */
  int addChild(ComposableComponent c);

  /**
   * Removes a child from this component.
   * @param childIndex of child to remove.
   */
  void removeChild(int childIndex);

  /**
   * Removes a child from this component.
   * @param c Child to remove
   */
  void removeChild(ComposableComponent c);

  /**
   * Removes all children from this component.
   */
  void removeAllChildren();

  /**
   * Returns a child of this component.
   * @param childIndex Index of the child within this component.
   */
  ComposableComponent getChild(int childIndex);

  /**
   * Returns a child of this component.
   * @param childName Name of child to retrieve
   * @return Requested child or null of child not found
   */
  ComposableComponent getChild(CompositeName childName);

  /**
   * Count of all children in this component.
   * @return child count.
   */
  int getChildCount();
}

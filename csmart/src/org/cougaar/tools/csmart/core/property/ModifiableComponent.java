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

package org.cougaar.tools.csmart.core.property;

import java.net.URL;

/**
 * The interface for ConfigurableComponents that can be modified from
 * a society.
 **/
public interface ModifiableComponent extends BaseComponent {
  /**
   * Add a listener for non-property changes to the society. E.g.
   * adding a host.
   * @param l the ModificationListener
   **/
  void addModificationListener(ModificationListener l);

  /**
   * Remove a listener for non-property changes to the society.
   * @param l the ModificationListener
   **/
  void removeModificationListener(ModificationListener l);

  /**
   * Returns whether or not the society can be edited.
   * @return true if society can be edited and false otherwise
   */
  boolean isEditable();

  /**
   * Set whether or not the society can be edited.
   * @param editable true if society is editable and false otherwise
   */
  void setEditable(boolean editable);

  /**
   * Create a copy of the given component
   * with the given name and return the copy.
   * @param name the name for the copied object
   * @param mc the component to copy
   * @return the copy
   */

  ModifiableComponent copy(String name);
}

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
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * The interface that every configurable component must implement.
 * The gui uses this interface to get and set properties.
 */
public interface ComponentProperties extends Serializable {
  String getShortName();
  CompositeName getFullName();

  /**
   * Initialize the properties of a new instance. All components
   * implementing this interface should delay the initialization of
   * their properties until this method is called;
   **/
  void initProperties();
  
  /**
   * Get a <code>URL</code> for a description of the component. May return <code>null</code>.
   *
   * @return an <code>URL</code> describing this component.
   */
  URL getDescription();
  
  Property getProperty(CompositeName name);
  /** Get a property using its local name **/
  Property getProperty(String localName);
  Iterator getPropertyNames();
  Iterator getLocalPropertyNames();
  List getPropertyNamesList();
  void addPropertiesListener(PropertiesListener l);
  void removePropertiesListener(PropertiesListener l);

  ComponentData addComponentData(ComponentData data);
  ComponentData modifyComponentData(ComponentData data);
  ComponentData modifyComponentData(ComponentData data, PopulateDb pdb);
  boolean componentWasRemoved();

  ComponentProperties copy(ComponentProperties result);

  /**
   * Test if this has any unbound properties (properties for which
   * isValueSet() return false)
   * @return true if there are one or more unbound properties
   **/
  boolean hasUnboundProperties();
}

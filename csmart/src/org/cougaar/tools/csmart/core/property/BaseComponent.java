/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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

import java.io.Serializable;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.core.db.PopulateDb;

/**
 * The interface that every configurable component must implement.
 * This interface handles all operations related to the 
 * <code>ComponentData</code> structures of a component and also 
 * all of the Properties within a component.
 * 
 * The gui uses this interface to get and set properties.
 */
public interface BaseComponent extends ComposableComponent, Serializable {

  /**
   * Gets the short name of this component.  All component names
   * are made up of a chain based on the component hierarchy.
   * This chain is: grandparent.parent.child
   * Short name is just 'child'.
   *
   * @return a <code>String</code> value of the short name.
   */
  String getShortName();


  /**
   * Gets the full name of this component. All component names
   * are made up of a chain based on the component hierarchy.
   * This chain is: grandparent.parent.child
   *
   * Full name is the complete chain.
   *
   * @return a <code>CompositeName</code> value of the full component name.
   */
  CompositeName getFullName();

  /**
   * Set the name of this component.  The name is relative to the
   * parent and must be distinct in that context.
   * @param newName the new name for this component.
   */

  void setName(String newName);

  /**
   * Initialize the properties of a new instance. All components
   * implementing this interface should delay the initialization of
   * their properties until this method is called;
   **/
  void initProperties();

  /**
   * Set a bunch of Properties at once. Used when creating a component
   * from the database.
   *
   * @param props a <code>Map</code> of <code>String</code> property names and <code>Object</code> values
   */
  void setProperties(Map props);
  
  /**
   * Get a <code>URL</code> for a description of the component. May return <code>null</code>.
   *
   * @return an <code>URL</code> describing this component.
   */
  URL getDescription();
  
  /**
   * Gets a <code>Property</code> based on the
   * property name specified as a <code>CompositeName</code>
   *
   * @param name of the Property 
   * @return <code>Property</code> object for the property.
   */
  Property getProperty(CompositeName name);

  /**
   * Get a property using its local name 
   *
   * @param localName of property
   * @return <Property> object for the property.
   */
  Property getProperty(String localName);

  /**
   * Add a property with a given value. A new Property is created
   * having the given name and value. In addition the other fields of
   * the property set to default values consistent with the class of
   * the value.
   * @param name the name of the property
   * @param value must be one of the supported value types
   * @return a <code>Property</code> object for the new Property
   **/
  Property addProperty(String name, Object value);

  /**
   * Returns a <code>Iterator</code> of all known property names for
   * this component.
   *
   * @return <code>Iterator</code> of Property Names 
   */
  Iterator getPropertyNames();
  

  Iterator getProperties();

  /**
   * Returns a <code>Iterator</code> of all local property names for this component.
   *
   * @return <code>Iterator</code> of Local Property Names
   */
  Iterator getLocalPropertyNames();

  /**
   * Returns a <code>List</code> of all property names.
   *
   * @return <code>List</code> of all property names
   */
  List getPropertyNamesList();

  /**
   * Adds a <code>PropertiesListener</code> to this component.
   *
   * @param l The <code>PropertiesListener</code>
   * @see PropertiesListener
   */
  void addPropertiesListener(PropertiesListener l);

  /**
   * Removes a <code>PropertiesListener</code> to this component.
   *
   * @param l The <code>PropertiesListener</code>
   */
  void removePropertiesListener(PropertiesListener l);

  /**
   * Adds a <code>ComponentData</code> to this component.
   *
   * @param data <code>ComponentData</code>
   * @return the <code>ComponentData</code> that was just added.
   * @see ComponentData
   */
  ComponentData addComponentData(ComponentData data);

  /**
   * Modifies a <code>ComponentData</code>
   *
   * @param data a modified <code>ComponentData</code>
   * @return the modified <code>ComponentData</code> object.
   */
  ComponentData modifyComponentData(ComponentData data);

  /**
   * Describe <code>modifyComponentData</code> method here.
   *
   * @param data a modified <code>ComponentData</code>
   * @param pdb 
   * @return the modified <code>ComponentData</code> object
   */
  ComponentData modifyComponentData(ComponentData data, PopulateDb pdb);

  boolean componentWasRemoved();

  /**
   * Makes a copy of a <code>BaseComponent</code> Object.
   *
   * @param result object to copy
   * @return a <code>ComponentProperties</code> copy
   */
  BaseComponent copy(BaseComponent result);

  /**
   * Test if this has any unbound properties (properties for which
   * isValueSet() return false)
   *
   * @return true if there are one or more unbound properties
   */
  boolean hasUnboundProperties();
}

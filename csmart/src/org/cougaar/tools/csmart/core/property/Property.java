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

import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URL;

import org.cougaar.tools.csmart.core.property.name.CompositeName;

/**
 * Every property has these attributes:
 * name, propertyClass, label, defaultValue, value, allowedValues, 
 * These values should be get/set through the ComponentProperties interface.
 */
public interface Property extends Serializable {
  
  /**
   * Gets the name of this property
   *
   * @return a <code>CompositeName</code> value
   */
  CompositeName getName();

  /**
   * Gets the class of this property.  The class
   * defines what type of value this property accepts.
   *
   * @return a <code>Class</code> value
   */
  Class getPropertyClass();

  /**
   * Sets the class of this property.  The class
   * defines what type of value this property accepts.
   *
   * @param c Property Class
   */
  void setPropertyClass(Class c);

  /**
   * Gets the Label for this Property.  Each property
   * contains a description label.
   *
   * @return a <code>String</code> value
   */
  String getLabel();

  /**
   * Sets the label for this Property.  Each property
   * contains a description label.
   *
   * @param label 
   */
  void setLabel(String label);

  /**
   * Gets the default value for this property.
   * All properties can contain default values.
   *
   * @return an <code>Object</code> value
   */
  Object getDefaultValue();

  /**
   * Sets the default value for this property.
   * All properties can contain default values.
   *
   * @param defaultValue 
   */
  void setDefaultValue(Object defaultValue);

  /**
   * Gets the current set value for this property.
   *
   * @return an <code>Object</code> value
   */
  Object getValue();

  /**
   * Sets the value for this property.
   *
   * @param value 
   */
  void setValue(Object value);

  /**
   * Gets all Allowed Values for this property. 
   * Each property can define a specific range or set
   * of valid values.
   *
   * @return a <code>Set</code> value
   */
  Set getAllowedValues();

  /**
   * Sets all Allowed Values for this Property
   * Each property can define a specific range or set
   * of valid values.
   *
   * @param allowedValues 
   */
  void setAllowedValues(Set allowedValues);

  /**
   * Indicates if the value for this property has been set.
   *
   * @return a <code>boolean</code> value
   */
  boolean isValueSet();

  /**
   * Adds a PropertyListener to this Property.
   * A property Listener listens for changes to the property
   * a sets an Event if a change has occured.
   *
   * @param l 
   */
  void addPropertyListener(PropertyListener l);

  /**
   * Removes a PropertyListener from this Property
   *
   * @param l 
   */
  void removePropertyListener(PropertyListener l);

  /**
   * Gets an <code>Iterator</code> of all PropertyListeners
   *
   * @return an <code>Iterator</code> value
   */
  Iterator getPropertyListeners();

  /**
   * Gets the ConfigurableComponent associated with this property.
   *
   * @return a <code>ConfigurableComponent</code> value
   */
  ConfigurableComponent getConfigurableComponent();

  /**
   * Gets the GUI tooltip for this property.
   *
   * @return a <code>String</code> value
   */
  String getToolTip();

  /**
   * Sets a GUI tooltip for this property.
   *
   * @param tt 
   * @return a <code>Property</code> value
   */
  Property setToolTip(String tt);

  /**
   * Returns a <code>URL</code> for the html help for this property.
   *
   * @return an <code>URL</code> value
   */
  URL getHelp();

  /**
   * Sets the URL for help for this property.
   *
   * @param url 
   * @return a <code>Property</code> value
   */
  Property setHelp(URL url);

  void setVisible(boolean visible);
  boolean isVisible();

  // For debugging
  void printProperty(PrintStream out);
  void printProperty(PrintStream out, String indent);
}

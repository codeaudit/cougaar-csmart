/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society;

import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.Property;

/**
 * A simple interface implemented by configurable components 
 * that represent generic components. <br>
 */
public interface MiscComponent extends ModifiableComponent {
 
  /**
   * Get the label for folders in the UI and containers
   *
   * @return a <code>String</code> label
   */
  String getFolderLabel();

  /**
   * Gets the classname for this component
   *
   * @return a <code>String</code> value
   */
  String getComponentClassName();

  /**
   * Get the type of this Component, from the property. If it is not
   * one of the values in ComponentData, it should be the full 
   * insertion point.
   *
   * @return a <code>String</code> type from the constants in ComponentData
   */
  String getComponentType();

  /**
   * Allow outside users to set the Component type to one of the
   * values in the constants in ComponentData. If not one
   * of those values, it should be a full insertion point.
   *
   * @param type a <code>String</code> component type or insertion point
   */
  void setComponentType(String type);

  /**
   * Adds a Parameter to this component.
   *
   * @param param Unique Integer for this parameter
   * @return a <code>Property</code> value
   */
  Property addParameter(int param);

  /**
   * Adds a Parameter to this component
   *
   * @param param Unique Object for this parameter's value
   * @return a <code>Property</code> value
   */
  Property addParameter(Object param);

  /**
   * Add a parameter that is based on some other property (typically a
   * property of our parnt.
   **/
  Property addParameter(Property prop);
}

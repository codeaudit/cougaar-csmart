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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.net.URL;

/**
 * Defines a property in terms of another. Only the name is changed.
 * Additionally, other properties can be added and their values will
 * track the aliased property.
 **/

public class PropertyAlias extends PropertyBase implements Property, PropertyListener {
  static final long serialVersionUID = -3114178146196112865L;

  private Property prop;
  private CompositeName name;

  public PropertyAlias(ConfigurableComponent c, String name, Property refProp) {
    super(c);
    if (refProp == null) throw new IllegalArgumentException("null refProp for " + name);
    this.name = new ComponentName(c, name);
    this.prop = refProp;
  }

  public void addPropertyListener(PropertyListener l) {
    if (!haveListeners()) prop.addPropertyListener(this);
    super.addPropertyListener(l);
  }

  public void removePropertyListener(PropertyListener l) {
    super.removePropertyListener(l);
    if (!haveListeners()) prop.removePropertyListener(this);
  }

  public void propertyValueChanged(PropertyEvent e) {
    fireValueChanged(e.getPreviousValue());
  }

  public void propertyOtherChanged(PropertyEvent e) {
    fireOtherChanged(e.getPreviousValue(), e.getWhatChanged());
  }

  public void setPropertyClass(Class c) {
    prop.setPropertyClass(c);
  }

  public void setLabel(String label) {
    prop.setLabel(label);
  }

  public void setDefaultValue(Object defaultValue) {
    prop.setDefaultValue(defaultValue);
  }

  public void setValue(Object value) {
    prop.setValue(value);
  }

  public void setExperimentValues(List experimentValues) {
    prop.setExperimentValues(experimentValues);
  }

  public void setAllowedValues(Set allowedValues) {
    prop.setAllowedValues(allowedValues);
  }

  public CompositeName getName() {
    return name;
  }

  public Class getPropertyClass() {
    return prop.getPropertyClass();
  }

  public String getLabel() {
    return prop.getLabel();
  }

  public Object getDefaultValue() {
    return prop.getDefaultValue();
  }

  public Object getValue() {
    return prop.getValue();
  }

  public List getExperimentValues() {
    return prop.getExperimentValues();
  }

  public Set getAllowedValues() {
    return prop.getAllowedValues();
  }

  public boolean isValueSet()
  { return prop.isValueSet();
  }

  public String getToolTip() {
    String result = super.getToolTip();
    if (result != null) return result;
    return prop.getToolTip();
  }
  public URL getHelp() {
    URL result = super.getHelp();
    if (result != null) return result;
    return prop.getHelp();
  }
}

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

import java.util.List;
import java.util.Set;

/**
 * Every property has these attributes:
 * name, propertyClass, label, defaultValue, value, allowedValues, 
 * and experimentValues.
 * These values should be get/set through the ComponentProperties interface.
 */

public class ConfigurableComponentProperty extends PropertyBase implements Property {
  static final long serialVersionUID = -1591739578094464420L;

  private ComponentName name;
  private Class propertyClass;
  private String label;
  private Object defaultValue;
  private Object value;
  private Set allowedValues;
  private List experimentValues;

  /**
   * Construct a Property with default values based on the name and value
   **/
  public ConfigurableComponentProperty(ConfigurableComponent c, String name, Object value) {
    super(c);
    this.name = new ComponentName(c, name);
    setValue(value);
    setDefaultValue(value);
    setLabel(name);
  }

  /**
   * Copy constructor creates a new ConfigurableComponentProperty from
   * an existing Property
   **/
  public ConfigurableComponentProperty(ConfigurableComponent c, String name, Property prop) {
    super(c);
    this.name = new ComponentName(c, name);
    setValue(prop.getValue());
    setPropertyClass(prop.getPropertyClass());
    setLabel(prop.getLabel());
    setDefaultValue(prop.getDefaultValue());
    setExperimentValues(prop.getExperimentValues());
    setAllowedValues(prop.getAllowedValues());
  }

  public CompositeName getName() {
    return name;
  }

  public Class getPropertyClass() {
    return propertyClass;
  }

  public void setPropertyClass(Class c) {
    Class old = propertyClass;
    propertyClass = c;
    try {
      fireOtherChanged(old, PropertyEvent.CLASS_CHANGED);
    } catch (RuntimeException re) {
      propertyClass = old;
      throw re;
    }
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    String old = this.label;
    this.label = label;
    try {
      fireOtherChanged(old, PropertyEvent.LABEL_CHANGED);
    } catch (RuntimeException re) {
      this.label = old;
      throw re;
    }
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(Object defaultValue) {
    Object old = this.defaultValue;
    this.defaultValue = defaultValue;
    try {
      fireOtherChanged(old, PropertyEvent.DEFAULTVALUE_CHANGED);
      if (value == null) fireValueChanged(old);
    } catch (RuntimeException re) {
      this.defaultValue = old;
      throw re;
    }
  }

  public Object getValue() {
    if (value == null) return defaultValue;
    return value;
  }

  public void setValue(Object value) {
    if(value != this.value) {
      Object old = this.value;
      this.value = value;
      try {
	fireValueChanged(old);
      } catch (RuntimeException re) {
	this.value = old;
	throw re;
      }
    }
  }

  public List getExperimentValues() {
    return experimentValues;
  }

  public void setExperimentValues(List experimentValues) {
    List old = this.experimentValues;
    this.experimentValues = experimentValues;
    try {
      fireOtherChanged(old, PropertyEvent.EXPERIMENTVALUES_CHANGED);
    } catch (RuntimeException re) {
      this.experimentValues = old;
      throw re;
    }
  }

  public Set getAllowedValues() {
    return allowedValues;
  }

  public void setAllowedValues(Set allowedValues) {
    Set old = allowedValues;
    this.allowedValues = allowedValues;
    try {
      fireOtherChanged(old, PropertyEvent.ALLOWEDVALUES_CHANGED);
    } catch (RuntimeException re) {
      this.allowedValues = old;
      throw re;
    }
  }

  public boolean isValueSet() {
    return value != null;
  }
}


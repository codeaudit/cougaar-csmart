/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.component;

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
  private Set experimentValues;

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
    Object old = this.value;
    this.value = value;
    try {
      fireValueChanged(old);
    } catch (RuntimeException re) {
      this.value = old;
      throw re;
    }
  }

  public Set getExperimentValues() {
    return experimentValues;
  }

  public void setExperimentValues(Set experimentValues) {
    Set old = this.experimentValues;
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

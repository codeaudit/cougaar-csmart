/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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
import java.util.Iterator;
import java.util.Collections;
import java.net.URL;
import java.io.*;

import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

/**
 * Base implementation of the <code>Property</code> Interface
 * @see Property for Documentation.
 */
public abstract class PropertyBase implements Property {
  private transient List listeners = null;
  private ConfigurableComponent component;
  private String tooltip;
  private URL help;
  private transient Logger log;

  /**
   * Creates a new <code>PropertyBase</code> instance.
   *
   * @param c 
   */
  protected PropertyBase(ConfigurableComponent c) {
    component = c;
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public final ConfigurableComponent getConfigurableComponent() {
    return component;
  }
  public abstract CompositeName getName();
  public abstract Class getPropertyClass();
  public abstract void setPropertyClass(Class c);
  public abstract String getLabel();
  public abstract void setLabel(String label);
  public abstract Object getDefaultValue();
  public abstract void setDefaultValue(Object defaultValue);
  public abstract Object getValue();
  public abstract void setValue(Object value);
  public abstract List getExperimentValues();
  public abstract void setExperimentValues(List experimentValues);
  public abstract Set getAllowedValues();
  public abstract void setAllowedValues(Set allowedValues);
  public abstract boolean isValueSet();
  public String getToolTip() {
    return tooltip;
  }

  public Property setToolTip(String tt) {
    tooltip = tt;
    return this;
  }

  public URL getHelp() {
    return help;
  }

  public Property setHelp(URL url) {
    help = url;
    return this;
  }

  public void addPropertyListener(PropertyListener l) {
    if (listeners == null) listeners = new ArrayList();
    listeners.add(l);
  }

  public void removePropertyListener(PropertyListener l) {
    if (listeners == null) return;
    listeners.remove(l);
  }

  public Iterator getPropertyListeners() {
    if (listeners == null) return Collections.EMPTY_SET.iterator();
    return listeners.iterator();
  }

  protected boolean haveListeners() {
    return listeners != null && listeners.size() > 0;
  }

  protected void fireValueChanged(Object oldValue) {
    if (listeners != null) {
      PropertyEvent ev = new PropertyEvent(this, PropertyEvent.VALUE_CHANGED, oldValue);
      // Use array in case listeners remove themselves
      PropertyListener[] ls =
        (PropertyListener[]) listeners.toArray(new PropertyListener[listeners.size()]);
      for (int i = 0; i < ls.length; i++) {
        ls[i].propertyValueChanged(ev);
      }
    }
  }

  protected void fireOtherChanged(Object old, int whatChanged) {
    if (listeners != null) {
      PropertyEvent ev = new PropertyEvent(this, whatChanged, old);
      // Use array in case listeners remove themselves
      PropertyListener[] ls =
        (PropertyListener[]) listeners.toArray(new PropertyListener[listeners.size()]);
      for (int i = 0; i < ls.length; i++) {
        ls[i].propertyOtherChanged(ev);
      }
    }
  }

  private void writeObject(ObjectOutputStream stream)
    throws IOException
  {
    stream.defaultWriteObject();
    stream.writeObject(getSerializableListeners(listeners));
  }

  private List getSerializableListeners(List listeners) {
    List result = null;
    if (listeners != null) {
      result = new ArrayList(listeners.size());
      for (int i = 0, n = listeners.size(); i < n; i++) {
        Object o = listeners.get(i);
        if (o instanceof ConfigurableComponentListener) result.add(o);
      }
    }
    return result;
  }

  private void readObject(ObjectInputStream stream)
    throws IOException, ClassNotFoundException
  {
    stream.defaultReadObject();
    listeners = (List) stream.readObject();
    createLogger();
  }

  public void printProperty(PrintStream out) {
    printProperty(out, "");
  }

  public void printProperty(PrintStream out, String indent) {
    out.println(indent + "Name: " + getName()); 
    out.println(indent + "Label: " + getLabel());
    out.println(indent + "Class: " + getPropertyClass());
    out.println(indent + "Value: " + getValue());
    out.println(indent + "Default: " + getDefaultValue());
    out.println(indent + "Allowed Values: " + getAllowedValues());
    out.println(indent + "Experiment Values: " + getExperimentValues());
  }

}

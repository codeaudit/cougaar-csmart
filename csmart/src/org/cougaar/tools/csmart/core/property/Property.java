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
 * and experimentValues.
 * These values should be get/set through the ComponentProperties interface.
 */
public interface Property extends Serializable {
    CompositeName getName();
//      void setName(CompositeName name);
    Class getPropertyClass();
    void setPropertyClass(Class c);
    String getLabel();
    void setLabel(String label);
    Object getDefaultValue();
    void setDefaultValue(Object defaultValue);
    Object getValue();
    void setValue(Object value);
    List getExperimentValues();
    void setExperimentValues(List experimentValues);
    Set getAllowedValues();
    void setAllowedValues(Set allowedValues);
    boolean isValueSet();
    void addPropertyListener(PropertyListener l);
    void removePropertyListener(PropertyListener l);
    Iterator getPropertyListeners();
    ConfigurableComponent getConfigurableComponent();
    String getToolTip();
    Property setToolTip(String tt);
    URL getHelp();
    Property setHelp(URL url);
    // For debugging
    void printProperty(PrintStream out);
    void printProperty(PrintStream out, String indent);
}

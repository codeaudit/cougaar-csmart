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
import java.util.Iterator;
import java.util.List;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URL;

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
    // For debugging
    void printProperty(PrintStream out);
    void printProperty(PrintStream out, String indent);
    String getToolTip();
    Property setToolTip(String tt);
    URL getHelp();
    Property setHelp(URL url);
}

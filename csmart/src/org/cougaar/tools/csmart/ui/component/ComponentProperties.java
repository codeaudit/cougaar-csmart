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

import java.util.Iterator;
import java.util.List;
import java.io.Serializable;

/**
 * The interface that every configurable component must implement.
 * The gui uses this interface to get and set properties.
 */

public interface ComponentProperties extends Serializable {
    /**
     * Initialize the properties of a new instance. All components
     * implementing this interface should delay the initialization of
     * their properties until this method is called;
     **/
    void initProperties();
    Property getProperty(CompositeName name);
    Iterator getPropertyNames();
    List getPropertyNamesList();
    void addPropertiesListener(PropertiesListener l);
    void removePropertiesListener(PropertiesListener l);
}

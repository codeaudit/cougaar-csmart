/**
 * <copyright>
 *  Copyright 2002-2003 BBNT Solutions, LLC
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

import org.cougaar.core.agent.Agent;
import org.cougaar.core.agent.AgentManager;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.core.plugin.PluginManager;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.PropertiesListener;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyAlias;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.PropertyListener;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.recipe.PriorityProperty;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

import java.util.Iterator;

/**
 * ComponentBase is the basic implementation for editing & configuring
 * arbitrary component.
 */
public class ComponentBase
  extends ModifiableConfigurableComponent 
  implements MiscComponent, PropertiesListener {

  /** Component Classname Property Definition **/
  public static final String PROP_CLASSNAME = "Component Class Name";
  public static final String PROP_CLASSNAME_DESC = "Name of the Component Class";

  public static final String PROP_PRIORITY = "Component Priority";
  public static final String PROP_PRIORITY_DESC = "Load Priority of the component in it's container relative to other child components";
  public static final String PROP_PRIORITY_DFLT = 
    ComponentDescription.priorityToString(ComponentDescription.PRIORITY_STANDARD);

  // FIXME:
  // Need to specify the insertion point
  // and actual type of this component

  public static final String PROP_TYPE = "Component Type";
  public static final String PROP_TYPE_DESC = "Type of component (Insertion point if not standard)";
  protected String type = ComponentData.PLUGIN;

  protected String folderLabel = "Other Components";
  protected boolean modified = true;

  private String alibID = null;
  private String libID = null;

  public static final String PROP_PARAM = "param-";
  protected int nParameters = 0;
  protected String classname = "";
  protected String priority = ComponentDescription.priorityToString(ComponentDescription.PRIORITY_STANDARD);

  public ComponentBase(String name) {
    this(name, "", ComponentDescription.priorityToString(ComponentDescription.PRIORITY_STANDARD));
  }

  public ComponentBase(String name, String classname, String priority) {
    this(name, classname, priority, ComponentData.PLUGIN);
  }

  public ComponentBase(String name, String classname, String priority, String type) {
    super(name);
    this.classname = classname;
    if (type != null && !type.trim().equals(""))
      this.type = getCanonicalType(type);
    else {
      if (log.isDebugEnabled())
	log.debug("CompBase constructor got empty type for name " + name, new Throwable());
    }
    if(priority != null && ! priority.trim().equals(""))
      this.priority = priority;
    createLogger();
    installListeners();
  }

  private void createLogger() {
    log = CSMART.createLogger("org.cougaar.tools.csmart.society.ComponentBase");
  }

  /**
   * Initializes properties for this component;
   * in particular, initializes the property class name.
   */
  public void initProperties() {
    Property prop = addProperty(PROP_CLASSNAME, classname);
    prop.setToolTip(PROP_CLASSNAME_DESC);

    if (log.isDebugEnabled()) {
      if (type == null || type.trim().equals(""))
	log.debug("type is null in initProps for " + getFullName().toString());
    }
    prop = addProperty(PROP_TYPE, type);
    prop.setToolTip(PROP_TYPE_DESC);

    prop = addPriorityProperty(PROP_PRIORITY, priority);
    prop.setToolTip(PROP_PRIORITY_DESC);
  }

  private Property addPriorityProperty(String name, String dflt) {
    Property prop = addProperty(new PriorityProperty(this, name, dflt));
    prop.setPropertyClass(String.class);
    return prop;
  }

  /**
   * Adds this component to the ComponentData structure.
   *
   * @param data 
   * @return a <code>ComponentData</code> value
   */
  public ComponentData addComponentData(ComponentData data) {
    if(log.isDebugEnabled()) {
      log.debug("Adding ComponentData for: " + data.getName());
    }

    // Warning: This assumes it has been handed the component in
    // which to add itself    
//     if (data.getType() != ComponentData.AGENT && data.getType() != ComponentData.NODE) {
//       if (log.isErrorEnabled()) {
// 	log.error("Asked to add to non Agent/Node: " + data);
//       }
//       return data;
//     }

    // Create a new componentdata
    // set the type, etc appropriately
    // set self as the owner
    // set data as the parent
    // add self to data
    ComponentData self = new GenericComponentData();

    // We don't try to translate the component type here
    // because that might cause a modification,
    // which isn't the intent at this point
    self.setType(getComponentType());

    self.setOwner(this);
    self.setParent(data);

    self.setClassName(getComponentClassName());
    self.setPriority(getPriority());
    self.setAlibID(getAlibID());
    self.setLibID(getLibID());

    Iterator names = getSortedLocalPropertyNames();
    while (names.hasNext()) {
      CompositeName cname = (CompositeName) names.next();
      String name = cname.toString();
//       if (log.isDebugEnabled()) {
//         log.debug("Looking at property " + name);
//       }
      if (name.indexOf(PROP_PARAM) != -1) {
        if (log.isDebugEnabled()) {
          log.debug("Found parameter " + name);
        }
        self.addParameter(getProperty(cname).getValue());
      }
    }
        
//     for(int i=0; i < nParameters; i++) {
//       // getLocalPropertyNames
//       // Iterate through them, looking for PROP_PARAM
//       // when get one, store it in hash by the number in it?
//       // Then with the count, create an array of that size,
//       // stick the items from the hash in the array, and do
//       // self.setParameters...
//       if (getProperty(PROP_PARAM + i) != null) {
//         if(log.isDebugEnabled()) {
//           log.debug("Adding Parameter: " + PROP_PARAM + i +
//                     " Value: " + getProperty(PROP_PARAM + i).getValue());
//         }
// 	self.addParameter(getProperty(PROP_PARAM + i).getValue());
//       }
//     }


    if (GenericComponentData.alreadyAdded(data, self)) {
      if (log.isDebugEnabled()) {
	log.debug(data.getName() + " already has component " + self);
      }
      return data;
    }

    if(getAlibID() != null) {
      self.setName(getAlibID());
      self.setName(GenericComponentData.getSubComponentUniqueName(data, self));
    } else {
      self.setName(GenericComponentData.getSubComponentUniqueName(data, self));
    }
        
    data.addChildDefaultLoc(self);
//     if (log.isDebugEnabled()) {
//       log.warn("CompBase added comp " + self);
//       log.warn("CompBase says type is " + getComponentType());
//     }
    return data;
  }


  public ComponentData modifyComponentData(ComponentData data) {
    if(log.isDebugEnabled()) {
      log.debug("Adding ComponentData for: " + data.getName());
    }

    // Warning: This assumes it has been handed the component in
    // which to add itself    
    if (data.getType() != ComponentData.AGENT && data.getType() != ComponentData.NODE) {
      if (log.isErrorEnabled()) {
	log.error("Asked to add to non Agent/Node: " + data);
      }
      return data;
    }

    // Create a new componentdata
    // set the type, etc appropriately
    // set self as the owner
    // set data as the parent
    // add self to data
    ComponentData self = new GenericComponentData();

    // We don't try to translate the component type here
    // because that might cause a modification,
    // which isn't the intent at this point
    self.setType(getComponentType());

    self.setOwner(this);
    self.setParent(data);

    self.setClassName(getComponentClassName());
    self.setPriority(getPriority());
    self.setAlibID(getAlibID());
    self.setLibID(getLibID());

    Iterator names = getSortedLocalPropertyNames();
    while (names.hasNext()) {
      CompositeName cname = (CompositeName) names.next();
      String name = cname.toString();
//       if (log.isDebugEnabled()) {
//         log.debug("Looking at property " + name);
//       }
      if (name.indexOf(PROP_PARAM) != -1) {
        if (log.isDebugEnabled()) {
          log.debug("Found parameter " + name);
        }
        self.addParameter(getProperty(cname).getValue());
      }
    }        

    if (GenericComponentData.alreadyAdded(data, self)) {
      if (log.isDebugEnabled()) {
	log.debug(data.getName() + " already has component " + self);
      }
      return data;
    }

    if(getAlibID() != null) {
      self.setName(getAlibID());
      self.setName(GenericComponentData.getSubComponentUniqueName(data, self));
    } else {
      self.setName(GenericComponentData.getSubComponentUniqueName(data, self));
    }
        
    data.addChildDefaultLoc(self);

//     if (log.isDebugEnabled()) {
//       log.warn("CompBase moded comp " + self);
//       log.warn("CompBase says type is " + getComponentType());
//     }

    return data;
  }


  public String getFolderLabel() {
    return folderLabel;
  }

  /**
   * Gets the classname for this component
   *
   * @return a <code>String</code> value
   */
  public String getComponentClassName() {
    Property p = getProperty(PROP_CLASSNAME);
    if (p != null) {
      Object o = p.getValue();
      if (o != null)
        return o.toString();
    }
    return null;
  }

  /**
   * Get the type of this Component, from the property.
   *
   * @return a <code>String</code> type from the constants in ComponentData
   */
  public String getComponentType() {
    Property p = getProperty(PROP_TYPE);
    if (p != null) {
      Object o = p.getValue();
      if (o != null)
        return o.toString();
    }
    return type;
  }

  // Turn random strings into the constants wherever possible
  protected String getCanonicalType(String type) {
    if (type == null || type.equals("")) {
      if (log.isDebugEnabled()) {
	log.debug("getCanonType got empty type in " + getFullName().toString() + "!", new Throwable());
      }
      return "";
    }

    if (type.equalsIgnoreCase(Agent.INSERTION_POINT))
      type = ComponentData.AGENT;
    else if (type.equalsIgnoreCase(AgentManager.INSERTION_POINT + ".Binder"))
      type = ComponentData.NODEBINDER;
    else if (type.equalsIgnoreCase(PluginManager.INSERTION_POINT + ".Binder"))
      type = ComponentData.AGENTBINDER;
    else if (type.equalsIgnoreCase(org.cougaar.core.plugin.PluginBase.INSERTION_POINT))
      type = ComponentData.PLUGIN;
    else if (type.equalsIgnoreCase("binder"))
      type = ComponentData.AGENTBINDER;
    else if (type.equalsIgnoreCase("nodebinder"))
      type = ComponentData.NODEBINDER;
    else if (type.equalsIgnoreCase("agentbinder"))
      type = ComponentData.AGENTBINDER;
    else if (type.equalsIgnoreCase("domain"))
      type = ComponentData.DOMAIN;
    else if (type.equalsIgnoreCase(ComponentData.NODEBINDER))
      type = ComponentData.NODEBINDER;
    else if (type.equalsIgnoreCase(ComponentData.AGENTBINDER))
      type = ComponentData.AGENTBINDER;
    else if (type.equalsIgnoreCase(ComponentData.PLUGIN))
      type = ComponentData.PLUGIN;
    else if (type.equalsIgnoreCase(ComponentData.AGENT))
      type = ComponentData.AGENT;
    else if (type.equalsIgnoreCase(ComponentData.DOMAIN))
      type = ComponentData.DOMAIN;

    return type;
  }

  /**
   * Allow outside users to set the Component type to one of the
   * values in the constants in ComponentData.
   *
   * @param type a <code>String</code> binder type
   */
  public void setComponentType(String type) {
    if (type == null || type.equals("")) {
      if (log.isDebugEnabled())
	log.debug("setCompType got empty type where was " + getComponentType() + " for " + getFullName().toString(), new Throwable());
      return;
    }

    if (type.equals(this.getComponentType()))
      return;

    type = getCanonicalType(type);

    Property p = getProperty(PROP_TYPE);
    if (p != null) {
      p.setValue(type);
      fireModification();
    }
  }

  public String getPriority() {
    Property p = getProperty(PROP_PRIORITY);
    if(p != null) {
      Object o = p.getValue();
      if( o != null) {
	String ret = o.toString();
	if (ret != null && ! ret.trim().equals("") && ! ret.equals("<not set>"))
	  return ret;
      }
    }
    return ComponentDescription.priorityToString(ComponentDescription.PRIORITY_COMPONENT);
  }

  /**
   * Adds a Parameter to this component.
   *
   * @param param Unique Integer for this parameter
   * @return a <code>Property</code> value
   */
  public Property addParameter(int param) {
    return addProperty(PROP_PARAM + nParameters++, new Integer(param));
  }

  /**
   * Adds a Parameter to this component
   *
   * @param param Unique Object for this parameter's value
   * @return a <code>Property</code> value
   */
  public Property addParameter(Object param) {
    return addProperty(PROP_PARAM + nParameters++, param);
  }

  /**
   * Add a parameter that is based on some other property (typically a
   * property of our parent.
   **/
  public Property addParameter(Property prop) {
    return addProperty(new PropertyAlias(this, PROP_PARAM + nParameters++, prop));
  }

  public void setAlibID(String alibID) {
    this.alibID = alibID;
  }

  public String getAlibID() {
    return this.alibID;
  }

  public void setLibID(String libID) {
    this.libID = libID;
  }

  public String getLibID() {
    return this.libID;
  }


  /**
   * Has this Component been modified, such that a save would do something.
   *
   * @return a <code>boolean</code>, false if no save necessary
   */
  public boolean isModified() {
    return modified;
  }

  /**
   * Set the internal modified flag (returned by isModified)
   * and fire a modification event.
   */

  public void fireModification() {
    modified = true;
    super.fireModification();
  }

  private void installListeners() {
    addPropertiesListener(this);
    for (Iterator i = getPropertyNames(); i.hasNext(); ) {
      Property p = getProperty((CompositeName)i.next());
      p.addPropertyListener(myPropertyListener);
    }
  }

  public void propertyAdded(PropertyEvent e) {
    Property addedProperty = e.getProperty();
    setPropertyVisible(addedProperty, true);
    addedProperty.addPropertyListener(myPropertyListener);
    fireModification();
  }

  public void propertyRemoved(PropertyEvent e) {
    e.getProperty().removePropertyListener(myPropertyListener);
    fireModification();
  }

  PropertyListener myPropertyListener =
    new PropertyListener() {
        public void propertyValueChanged(PropertyEvent e) {
          fireModification();
        }

        public void propertyOtherChanged(PropertyEvent e) {
          fireModification();
        }
      };

} // End of ComponentBase

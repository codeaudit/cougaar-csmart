/**
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
 *  </copyright>
 */
package org.cougaar.tools.csmart.society;

import java.util.Iterator;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyAlias;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.recipe.PriorityProperty;

/**
 * ComponentBase is the basic implementation for editing & configuring
 * arbitrary component.
 */
public class ComponentBase
  extends ModifiableConfigurableComponent 
  implements MiscComponent {

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

  public static final String PROP_PARAM = "param-";
  protected int nParameters = 0;
  protected String classname = "";
  protected String priority = ComponentDescription.priorityToString(ComponentDescription.PRIORITY_STANDARD);

  public ComponentBase(String name) {
    super(name);
    createLogger();
  }

  public ComponentBase(String name, String classname, String priority) {
    super(name);
    this.classname = classname;
    if(priority != null)
      this.priority = priority;
    createLogger();
  }

  public ComponentBase(String name, String classname, String priority, String type) {
    super(name);
    this.classname = classname;
    this.type = type;
    if(priority != null)
      this.priority = priority;
    createLogger();
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

    // If the user specified the long version of common insertion points,
    // translate to the short version for consistency
    self.setType(getComponentType());
    Property p = getProperty(PROP_TYPE);
    if (self.getType().equalsIgnoreCase("Node.AgentManager.Binder")) {
      self.setType(ComponentData.NODEBINDER);
      if (p != null)
	p.setValue(ComponentData.NODEBINDER);
    } else if (self.getType() != ComponentData.NODEBINDER && self.getType().equalsIgnoreCase(ComponentData.NODEBINDER)) {
      self.setType(ComponentData.NODEBINDER);
      if (p != null)
	p.setValue(ComponentData.NODEBINDER);
    } else if (self.getType().equalsIgnoreCase("nodebinder")) {
      self.setType(ComponentData.NODEBINDER);
      if (p != null)
	p.setValue(ComponentData.NODEBINDER);
    } else if (self.getType().equalsIgnoreCase("Node.AgentManager.Agent.PluginManager.Binder")) {
      self.setType(ComponentData.AGENTBINDER);
      if (p != null)
	p.setValue(ComponentData.AGENTBINDER);
    } else if (self.getType() != ComponentData.AGENTBINDER && self.getType().equalsIgnoreCase("agent binder")) {
      self.setType(ComponentData.AGENTBINDER);
      if (p != null)
	p.setValue(ComponentData.AGENTBINDER);
    } else if (self.getType().equalsIgnoreCase("agentbinder")) {
      self.setType(ComponentData.AGENTBINDER);
      if (p != null)
	p.setValue(ComponentData.AGENTBINDER);
    } else if (self.getType().equalsIgnoreCase("binder")) {
      self.setType(ComponentData.AGENTBINDER);
      if (p != null)
	p.setValue(ComponentData.AGENTBINDER);
    } else if (self.getType().equalsIgnoreCase("Node.AgentManager.Agent.PluginManager.Plugin")) {
      self.setType(ComponentData.PLUGIN);
      if (p != null)
	p.setValue(ComponentData.PLUGIN);
    } else if (self.getType() != ComponentData.PLUGIN && self.getType().equalsIgnoreCase("Plugin")) {
      self.setType(ComponentData.PLUGIN);
      if (p != null)
	p.setValue(ComponentData.PLUGIN);
    } else if (self.getType().equalsIgnoreCase("Node.AgentManager.Agent")) {
      self.setType(ComponentData.AGENT);
      if (p != null)
	p.setValue(ComponentData.AGENT);
    } else if (self.getType() != ComponentData.AGENT && self.getType().equalsIgnoreCase("Agent")) {
      self.setType(ComponentData.AGENT);
      if (p != null)
	p.setValue(ComponentData.AGENT);
    }

    self.setOwner(this);
    self.setParent(data);

    self.setClassName(getComponentClassName());
    self.setPriority(getPriority());

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

    self.setName(GenericComponentData.getSubComponentUniqueName(data, self));

    data.addChildDefaultLoc(self);
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

  /**
   * Allow outside users to set the Component type to one of the
   * values in the constants in ComponentData.
   *
   * @param type a <code>String</code> binder type
   */
  public void setComponentType(String type) {
    if (type == null || type.equals("") || type.equals(this.getComponentType()))
      return;

    if (type.equalsIgnoreCase("Node.AgentManager.Agent"))
      type = ComponentData.AGENT;
    else if (type.equalsIgnoreCase("Node.AgentManager.Binder"))
      type = ComponentData.NODEBINDER;
    else if (type.equalsIgnoreCase("Node.AgentManager.Agent.PluginManager.Binder"))
      type = ComponentData.AGENTBINDER;
    else if (type.equalsIgnoreCase("Node.AgentManager.Agent.PluginManager.Plugin"))
      type = ComponentData.PLUGIN;
    else if (type.equalsIgnoreCase("binder"))
      type = ComponentData.AGENTBINDER;
    else if (type.equalsIgnoreCase("nodebinder"))
      type = ComponentData.NODEBINDER;
    else if (type.equalsIgnoreCase("agentbinder"))
      type = ComponentData.AGENTBINDER;
    else if (type.equalsIgnoreCase("plugin"))
      type = ComponentData.PLUGIN;
    else if (type.equalsIgnoreCase("agent"))
      type = ComponentData.AGENT;

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
      if( o != null)
        return o.toString();
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
} // End of ComponentBase

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
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyAlias;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.core.property.name.CompositeName;

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

  public ComponentBase(String name) {
    super(name);
    createLogger();
  }

  public ComponentBase(String name, String classname) {
    super(name);
    this.classname = classname;
    createLogger();
  }

  public ComponentBase(String name, String classname, String type) {
    super(name);
    this.classname = classname;
    this.type = type;
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
    if (self.getType().equalsIgnoreCase("Node.AgentManager.Binder")) {
      self.setType(ComponentData.NODEBINDER);
    } else if (self.getType().equalsIgnoreCase("Node.AgentManager.Agent.PluginManager.Binder")) {
      self.setType(ComponentData.AGENTBINDER);
    } else if (self.getType().equalsIgnoreCase("Node.AgentManager.Agent.PluginManager.Plugin")) {
      self.setType(ComponentData.PLUGIN);
    } else if (self.getType().equalsIgnoreCase("Node.AgentManager.Agent")) {
      self.setType(ComponentData.AGENT);
    }

    self.setOwner(this);
    self.setParent(data);

    self.setClassName(getComponentClassName());

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


    if (alreadyAdded(data, self)) {
      if (log.isDebugEnabled()) {
	log.debug(data.getName() + " already has component " + self);
      }
      return data;
    }

    self.setName(GenericComponentData.getSubComponentUniqueName(data, self));

    data.addChildDefaultLoc(self);
    return data;
  }

  // Helper method: Does the given parent already contain a component
  // with the same class, type, and parameters
  // Does not check its children or leaf data


  /**
   * Look for the given ComponentData in the given parent, using its class, type, and parameters.
   * Do not consider the name of the child, or any AssetData or children it might have.
   * That is, if any existing child of the given parent has the same type, class, and parameter list
   * of the given candidate child, return true. Also return true if the parent or child is null. Return
   * false if the parent has no children.
   *
   * @param parent a <code>ComponentData</code> that may already contain the component
   * @param self a <code>ComponentData</code> a component to look for
   * @return a <code>boolean</code>, true if a component with the same type, class, and parameters is already present
   */
  protected boolean alreadyAdded(final ComponentData parent, final ComponentData self) {
    if (self == null || parent == null)
      return true;
    ComponentData[] children = parent.getChildren();
    if (children == null)
      return false;
    if (log.isDebugEnabled() && children.length != parent.childCount()) {
      log.debug(parent + " says childCount is " + parent.childCount() + " but returned array of children was of length " + children.length);
    }
    for (int i = 0; i < children.length; i++) {
      boolean isdiff = false;
      ComponentData kid = children[i];
      if (kid == null) {
	if (log.isWarnEnabled()) {
	  log.warn("Bug 1279: Child " + i + " is null in " + parent.getName());
	}
	// FIXME: Maybe do a parent.setChildren with a new list that doesn't include
	// the null?
      } else if (kid.getClassName().equals(self.getClassName())) {
	if (kid.getType().equals(self.getType())) {
	  if (kid.parameterCount() == self.parameterCount()) {
	    // Then we better compare the parameters in turn.
	    // As soon as we find one that differs, were OK.
	    for (int j = 0; j < kid.parameterCount(); j++) {
	      if (! kid.getParameter(j).equals(self.getParameter(j))) {
		isdiff = true;
		break;
	      }
	    } // loop over params
	    // If we get here, we finished comparing the parameters
            // Either cause we broke out, and isdiff is true
            // Or we completely compared the child, and it is
            // identical.
            // If we did not mark this child as different,
            // then return true - it is the same
	    if (! isdiff)
	      return true;
	  } // check param count
	} // check comp type
      } // check comp class
    } // loop over children

    // If we get here, we did not find any component that is identical
    return false;
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
    Property p = getProperty(PROP_TYPE);
    if (p != null) {
      p.setValue(type);
      fireModification();
    }
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

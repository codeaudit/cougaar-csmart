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

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyAlias;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

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
    log = CSMART.createLogger(this.getClass().getName());
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

    self.setType(getComponentType());

    self.setOwner(this);
    self.setParent(data);

    self.setClassName(getComponentClassName());
    for(int i=0; i < nParameters; i++) {
      if (getProperty(PROP_PARAM + i) != null)
	self.addParameter(getProperty(PROP_PARAM + i).getValue());
    }


    if (alreadyAdded(data, self)) {
      if (log.isDebugEnabled()) {
	log.debug(data.getName() + " already has component " + self);
      }
      return data;
    }

    self.setName(uniqueName(data, self));

    data.addChildDefaultLoc(self);
    return data;
  }

  // Helper method: Uniquely name a component for inclusion in 
  // the given parent. Name is <parent name>|<class name>
  // But if that is taken, try adding the first parameter
  // If that too is taken, add the current number
  // of children in the parent, ie the index at which
  // this will likely be added
  // Note: This method does not _set_ the name, only
  // creates and returns it.
  protected String uniqueName(ComponentData parent, ComponentData self) {
    ComponentData[] children = parent.getChildren();
    String cname = parent.getName() + "|" + getComponentClassName();
    boolean addedparam = false;
    for (int i = 0; i < children.length; i++) {
      ComponentData kid = children[i];
      if (kid.getName().equals(cname)) {
	// OK, must at least add a paramter if it has one
	if (getProperty(PROP_PARAM+0) != null && ! addedparam) {
	    cname = cname + "|" + getProperty(PROP_PARAM+0).getValue();
	    addedparam = true;
	} else {
	  // OK, no params. Add a number? Maybe the others do have a param?
	  cname = cname + children.length;
	}
      }
    }
    return cname;
  }

  // Helper method: Does the given parent already contain a component
  // with the same class, type, and parameters
  // Does not check its children or leaf data
  protected boolean alreadyAdded(ComponentData parent, ComponentData self) {
    ComponentData[] children = parent.getChildren();
    for (int i = 0; i < children.length; i++) {
      boolean isdiff = false;
      ComponentData kid = children[i];
      if (kid.getClassName().equals(self.getClassName())) {
	if (kid.getType().equals(self.getType())) {
	  if (kid.parameterCount() == self.parameterCount()) {
	    // Then we better compare the parameters in turn.
	    // As soon as we find one that differs, were OK.
	    for (int j = 0; j < nParameters; j++) {
	      if (! kid.getParameter(j).equals(self.getParameter(j))) {
		isdiff = true;
		break;
	      }
	    } // loop over params
	    // If we get here, we compared all the parameters.
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
   * @param param Unique string for this parameter
   * @return a <code>Property</code> value
   */
  public Property addParameter(String param) {
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
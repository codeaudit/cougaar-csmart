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
 * PluginBase handles basic properties, adding componentData, etc for most Societies.
 */
public class PluginBase
  extends ModifiableConfigurableComponent 
  implements PluginComponent {

  /** Plugin Classname Property Definition **/

  public static final String PROP_CLASSNAME = "Plugin Class Name";
  public static final String PROP_CLASSNAME_DESC = "Name of the Plugin Class";

  public static final String PROP_PARAM = "param-";
  private int nParameters = 0;
  private String classname;

  public PluginBase(String name, String classname) {
    super(name);
    this.classname = classname;
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
  }

  /**
   * Adds this component to the ComponentData structure. Must hand it
   * the Node/Agent into which you want to add this Plugin.
   *
   * @param data to contain this Plugin
   * @return a <code>ComponentData</code>, now modified
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
    self.setType(ComponentData.PLUGIN);
    self.setOwner(this);
    self.setParent(data);

    self.setClassName(getPluginClassName());
    for(int i=0; i < nParameters; i++) {
      self.addParameter(getProperty(PROP_PARAM + i).getValue());
    }

    // Figure out the component name, making it unique
    ComponentData[] children = data.getChildren();
    String cname = data.getName() + "|" + getPluginClassName();
    boolean addedparam = false;
    for (int i = 0; i < children.length; i++) {
      ComponentData kid = children[i];
      if (kid.getName().equals(cname)) {
	// OK, must at least add a paramter if it has one
	if (nParameters > 0 && ! addedparam) {
	    cname = cname + "|" + getProperty(PROP_PARAM+0).getValue();
	    addedparam = true;
	} else {
	  // OK, no params. Add a number? Maybe the others do have a param?
	  cname = cname + children.length;
	}
      }
    }
    self.setName(cname);

    data.addChildDefaultLoc(self);
    return data;
  }

  /**
   * Gets the classname for this plugin
   *
   * @return a <code>String</code> value
   */
  public String getPluginClassName() {
    Property p = getProperty(PROP_CLASSNAME);
    if (p != null) {
      Object o = p.getValue();
      if (o != null)
        return o.toString();
    }
    return null;
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
   * property of our parnt.
   **/
  public Property addParameter(Property prop) {
    return addProperty(new PropertyAlias(this, PROP_PARAM + nParameters++, prop));
  }
} // End of PluginBase

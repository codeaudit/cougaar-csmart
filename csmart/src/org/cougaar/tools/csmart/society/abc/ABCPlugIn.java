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
package org.cougaar.tools.csmart.society.abc;

import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyAlias;
import org.cougaar.tools.csmart.core.cdata.ComponentData;

import java.io.Serializable;

public class ABCPlugIn
  extends ConfigurableComponent
  implements Serializable
{
  public static final String PROP_PARAM = "param-";
  private int nParameters = 0;
  private String pluginClass;

  /**
   * Creates a new <code>ABCPlugIn</code> instance.
   *
   * @param name Name of the Plugin Component
   * @param pluginClass Name of the plugin class
   */
  public ABCPlugIn(String name, String pluginClass) {
    super(name);
    this.pluginClass = pluginClass;
  }

  /**
   * Initializes any properties for this component.
   *
   */
  public void initProperties() {
    // Currently, no properties require initialization
  }

  /**
   * Gets the classname for this plugin
   *
   * @return a <code>String</code> value
   */
  public String getPlugInClassName() {
    return pluginClass;
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

  /**
   * Returns the configuration line for this plugin.
   * configuration lines are of the format:
   * plugin = ...
   *
   * @return a <code>String</code> value
   */
  public String getConfigLine() {
    StringBuffer buf = new StringBuffer();
    buf.append("plugin = ");
    buf.append(getPlugInClassName());
    if(nParameters != 0 ) {
      buf.append("(");
      for (int i = 0; i < nParameters; i++) {
	Object param = getProperty(PROP_PARAM + i).getValue();
	if (i > 0) {
	  buf.append(",");
	}
	buf.append(param);
      }
      buf.append(")");
    }
    return buf.substring(0);
  }

  /**
   * Adds this component to the ComponentData structure.
   *
   * @param data 
   * @return a <code>ComponentData</code> value
   */
  public ComponentData addComponentData(ComponentData data) {
    data.setName(getPlugInClassName());

    for(int i=0; i < nParameters; i++) {
      data.addParameter(getProperty(PROP_PARAM + i).getValue());
    }
    return data;
  }


  /**
   * Performs any modificaions to the ComponentData structure
   * required by this component
   *
   * @param data 
   * @return a <code>ComponentData</code> value
   */
  public ComponentData modifyComponentData(ComponentData data) {
    return data;
  }
}

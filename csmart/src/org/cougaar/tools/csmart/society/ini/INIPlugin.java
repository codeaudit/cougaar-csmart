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
package org.cougaar.tools.csmart.society.ini;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyAdapter;
import org.cougaar.tools.csmart.core.property.PropertyAlias;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;


/**
 * INIPlugin.java
 *
 *
 * Created: Thu Feb 21 09:35:40 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class INIPlugin 
  extends ConfigurableComponent 
  implements Serializable {

  public static final String PROP_PARAM = "param-";
  private int nParameters = 0;
  private String pluginClass;
  private String name;

  /** Plugin Classname Property Definitions **/
  public static final String PROP_NAME = "Plugin";
  public static final String PROP_NAME_DESC = "Name of the Plugin";

  /** Plugin Classname Property Definitions **/
  public static final String PROP_PARAMC = "Parameters";
  public static final String PROP_PARAMC_DESC = "Number of Parameters for this Plugin";

  private Property propName;
  private Property propParamCount;

  public INIPlugin (String name, String pluginClass){
    super(name.substring(name.lastIndexOf(".")+1));
    this.name = name;
    this.pluginClass = pluginClass;
    createLogger();
    if(log.isDebugEnabled()) {
      log.debug("Create Plugin: " + name);
    }
  }

  /**
   * Initializes any properties for this component.
   *
   */
  public void initProperties() {
    propName = addProperty(PROP_NAME, new String(name));
    propName.setToolTip(PROP_NAME_DESC);
  
//     propParamCount = addProperty(PROP_PARAMC, new Integer(0), new PropertyAdapter() {
//         public void propertyValueChanged(PropertyEvent e) {
//           if(log.isDebugEnabled()) {
//             log.debug("Parameter Count Changed");
//           }
// //           if(((Integer)propParamCount.getValue()).intValue() < 
// //              ((Integer)e.getPreviousValue()).intValue()) {
// //             // For now, do not allow removing of values.  
// //             // Because: How do we choose which one to delete?
// //             propParamCount.setValue(e.getPreviousValue());
// //           } else if (((Integer)propParamCount.getValue()).intValue() ==
// //             ((Integer)e.getPreviousValue()).intValue()) {
// //             // Do nothing.
// //           } else {
// //             addParameter("");
// //           }
//         }
//       });
//     propParamCount.setToolTip(PROP_PARAMC_DESC);
  }

  /**
   * Gets the classname for this plugin
   *
   * @return a <code>String</code> value
   */
  public String getPluginClassName() {
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
    buf.append(getPluginClassName());
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
    data.setName(getPluginClassName());

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

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }
  
}// INIPlugin

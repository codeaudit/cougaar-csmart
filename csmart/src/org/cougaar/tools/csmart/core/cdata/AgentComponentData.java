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
package org.cougaar.tools.csmart.core.cdata;

import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import java.util.ArrayList;

/**
 * AgentComponentData extends ComponentData and fills in the
 * Agent values of some fields.
 */
public class AgentComponentData extends GenericComponentData {

  private static final String defClassName = "org.cougaar.core.agent.ClusterImpl";

  /** Default Constructor **/
  public AgentComponentData() {
    type = ComponentData.AGENT;
    className = defClassName;
  }

  /**
   * Returns a list of all plugin names for this agent.
   *
   * @return a <code>String[]</code> value
   */
  String[] getPluginNames() {
    ArrayList names = new ArrayList();
    ComponentData[] plugins = getChildren();
    
    for(int i=0; i < plugins.length; i++) {
      ComponentData plugin = plugins[i];
      if(plugin.getType().equals(ComponentData.PLUGIN)) {
        names.add(plugin.getName());
      }
    }
    
    return (String[])names.toArray(new String[names.size()]);
  }
  
  /**
   * Returns all arg for a specific plugin
   *
   * @param name Name of the plugin
   * @return an <code>Object[]</code> value
   */
  Object[] getPluginArgs(String name) {
    ComponentData plugin = findPlugin(name);
    if(plugin != null) {
      return plugin.getParameters();
    }
    return null;
  }

  private ComponentData findPlugin(String name) {
    ComponentData[] plugins = getChildren();
    for(int i=0; i < plugins.length; i++) {
      if(plugins[i].getName().equals(name)) {
        return plugins[i];
      }
    }
    return null;
  }

}

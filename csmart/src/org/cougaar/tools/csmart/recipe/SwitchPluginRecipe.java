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
package org.cougaar.tools.csmart.recipe;

import java.io.Serializable;
import java.net.URL;

import org.cougaar.core.agent.Agent;
import org.cougaar.core.agent.AgentManager;
import org.cougaar.core.plugin.PluginManager;
import org.cougaar.core.plugin.PluginBase;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.Property;

/**
 * SwitchPluginRecipe.java
 *
 *
 * Created: Wed Mar 27 09:48:41 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */
public class SwitchPluginRecipe extends RecipeBase 
  implements Serializable {

  private static final String PROP_OLD_CLASS = "Old Plugin Class";
  private static final String PROP_OLD_CLASS_DFLT = "";
  private static final String PROP_OLD_CLASS_DESC = "Plugin Class to be replaced";

  private static final String PROP_NEW_CLASS = "New Plugin Class";
  private static final String PROP_NEW_CLASS_DFLT = "";
  private static final String PROP_NEW_CLASS_DESC = "New Plugin Class";

  private static final String PROP_TYPE = "Component Type";
  private static final String PROP_TYPE_DFLT = ComponentData.PLUGIN;
  private static final String PROP_TYPE_DESC = "Insertion point or type of component to swap";

  private Property propOldPluginClass;
  private Property propNewPluginClass;
  private Property propCompType;

  private static final String DESCRIPTION_RESOURCE_NAME = 
    "switch-plugin-recipe-description.html";

  private boolean compRemoved = false;

  public SwitchPluginRecipe (){
    this("Switch Plugin Recipe");
  }
  
  public SwitchPluginRecipe(String name) {
    super(name);
  }

  public void initProperties() {
    propOldPluginClass = addProperty(PROP_OLD_CLASS, PROP_OLD_CLASS_DFLT);
    propOldPluginClass.setToolTip(PROP_OLD_CLASS_DESC);

    propNewPluginClass = addProperty(PROP_NEW_CLASS, PROP_NEW_CLASS_DFLT);
    propNewPluginClass.setToolTip(PROP_NEW_CLASS_DESC);

    propCompType = addProperty(PROP_TYPE, PROP_TYPE_DFLT);
    propCompType.setToolTip(PROP_TYPE_DESC);
  }

  /**
   * Gets the name of the html help file for this component.
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  public ComponentData addComponentData(ComponentData data) {
    // Reset compRemoved flag
    compRemoved = false;
    return data;
  }

  // Find all Components of type Plugin - btw, this thing should
  // be extended to do any type really - and do a setClass.
  // Must also modify the AlibId and Name appropriately
  public ComponentData modifyComponentData(ComponentData data) {
    if (propOldPluginClass == null || propOldPluginClass.getValue() == null || propOldPluginClass.getValue().toString().equals(""))
      return data;
    if (propNewPluginClass == null || propNewPluginClass.getValue() == null || propNewPluginClass.getValue().toString().equals(""))
      return data;

    String oldClass = propOldPluginClass.getValue().toString();
    String newClass = propNewPluginClass.getValue().toString();
    if (oldClass.equals(newClass))
      return data;

    // Set the type were looking for
    String type;
    if (propCompType != null && propCompType.getValue() != null)
      type = propCompType.getValue().toString();
    else
      return data;
    if (type.equals(""))
      return data;

    // If the user typed in the full insertion point of a common one, use the shorthand
    // We really arent supporting the Agent case now...
    if (type.equalsIgnoreCase(Agent.INSERTION_POINT))
      type = ComponentData.AGENT;
    else if (type.equalsIgnoreCase(AgentManager.INSERTION_POINT + ".Binder")) 
      type = ComponentData.NODEBINDER;
    else if (type.equalsIgnoreCase(PluginManager.INSERTION_POINT + ".Binder"))
      type = ComponentData.AGENTBINDER;
    else if (type.equalsIgnoreCase(PluginBase.INSERTION_POINT))
      type = ComponentData.PLUGIN;

    // Is this the type we're looking for?
    if (data.getType().equals(type)) {
      // Is it the right class?
      if (data.getClassName().equals(oldClass)) {

	// unset AlibID - let PopulateDb recreate
	data.setAlibID(null);

	// Then change classname
	data.setClassName(newClass);

	// Warning: It is possible the old plugin was already there. So we need to check
	// that the name we are setting is in fact still unique. Steal code
	// from PluginBase
	// Change Name
	// FIXME!

	// Mark the fact we did a modification
	compRemoved = true;
      }
    }

    // Now recurse down each of the children
    if (data.childCount() > 0) {
      // for each child, call this same method.
      ComponentData[] children = data.getChildren();
      for (int i = 0; i < children.length; i++) {
	modifyComponentData(children[i]);
      }
    }
      
    return data;
  } // end of modifyComponentData

  public boolean componentWasRemoved() {
    return compRemoved;
  }

}// SwitchPluginRecipe

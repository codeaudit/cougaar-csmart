/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society.cdata;

import java.util.Collection;
import java.util.Iterator;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.AssetComponent;
import org.cougaar.tools.csmart.society.ContainerBase;
import org.cougaar.tools.csmart.society.PluginBase;
import org.cougaar.tools.csmart.society.BinderBase;
import org.cougaar.tools.csmart.society.AgentBase;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;

/**
 * Create a ConfigurableComponent which represents an Agent
 * from ComponentData.
 */

public class AgentCDataComponent 
  extends AgentBase
          implements AgentComponent {

  ComponentData cdata;

  /**
   * Creates a new <code>AgentCDataComponent</code> instance.
   *
   * @param cdata <code>ComponentData</code> to use as base.
   */
  public AgentCDataComponent(ComponentData cdata) {
    super(cdata.getName());
    this.cdata = cdata;
  }

  /**
   * Initializes all <code>Property</code> values for this class.
   *
   */
  public void initProperties() {
    Property p = addProperty(PROP_CLASSNAME, new String(cdata.getClassName()));
    p.setToolTip(PROP_CLASSNAME_DESC);

    addBinders();
    addPlugins();
    addAssetData();
  }

  protected void addBinders() {
    // Add binders
    // FIXME: Is this right?
    ContainerBase container = new ContainerBase("Binders");
    container.initProperties();
    addChild(container);

    ComponentData[] childCData = cdata.getChildren();
    for (int i = 0; i < childCData.length; i++) {
      if (childCData[i].getType().equals(ComponentData.AGENTBINDER)) {
        BinderBase binder = new BinderBase(childCData[i].getName(),
                                           childCData[i].getClassName());
        binder.initProperties();
	binder.setBinderType(childCData[i].getType());
        Object[] parameters = childCData[i].getParameters();
        for (int j = 0; j < parameters.length; j++) 
          binder.addProperty(BinderBase.PROP_PARAM + j, (String)parameters[j]);
        container.addChild(binder);
      }
    }
  }

  protected void addPlugins() {
    // add plugins
    ContainerBase container = new ContainerBase("Plugins");
    container.initProperties();
    addChild(container);

    ComponentData[] childCData = cdata.getChildren();
    for (int i = 0; i < childCData.length; i++) {
      if (childCData[i].getType().equals(ComponentData.PLUGIN)) {
        PluginBase plugin = new PluginBase(childCData[i].getName(),
                                           childCData[i].getClassName());
        plugin.initProperties();
        Object[] parameters = childCData[i].getParameters();
        for (int j = 0; j < parameters.length; j++) 
          plugin.addProperty(PluginBase.PROP_PARAM + j, (String)parameters[j]);
        container.addChild(plugin);
      }
    }

    // FIXME: Add misc components -- how do I find the type though?
  }

  protected void addAssetData() {

    // add asset data components
    BaseComponent asset = 
      (BaseComponent)new AssetCDataComponent(cdata.getAgentAssetData());
    asset.initProperties();
    addChild(asset);
  }

}

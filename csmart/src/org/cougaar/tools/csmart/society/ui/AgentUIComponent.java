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
package org.cougaar.tools.csmart.society.ui;

import java.util.Collection;
import java.util.Iterator;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.tools.csmart.core.cdata.ComponentConnector;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.AssetComponent;
import org.cougaar.tools.csmart.society.ContainerBase;
import org.cougaar.tools.csmart.society.PluginBase;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

public class AgentUIComponent
  extends ModifiableConfigurableComponent
  implements AgentComponent {

  private transient Logger log;
  private String name;
  private static final String DEFAULT_CLASS = "org.cougaar.core.agent.ClusterImpl";

  /** Agent Classname Property Definitions **/

  /** Classname Property Definition **/
  public static final String PROP_CLASSNAME = "Classname";

  /** Classname Description Property Definition **/
  public static final String PROP_CLASSNAME_DESC = "Name of the Agent Class";
  
  /**
   * Creates a new <code>AgentUIComponent</code> instance.
   *
   * @param name Name of the Component
   */
  public AgentUIComponent(String name) {
    super(name);
    this.name = name;
    createLogger();
    if(log.isDebugEnabled()) {
      log.debug("Creating Agent: " + name);
    }
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Initializes all local Properties
   *
   */
  public void initProperties() {
    Property p = addProperty(PROP_CLASSNAME, new String(DEFAULT_CLASS));
    p.setToolTip(PROP_CLASSNAME_DESC);
  }

  /**
   * Adds any relevent data to the global <code>ComponentData</code> tree.
   * No modifications are made in the method, only additions.
   *
   * @param data Pointer to the global tree
   * @return an updated <code>ComponentData</code> value
   */
  public ComponentData addComponentData(ComponentData data) {
    // Process AssetData
    Iterator iter = 
      ((Collection)getDescendentsOfClass(AssetComponent.class)).iterator();
    while(iter.hasNext()) {
      AssetComponent asset = (AssetComponent)iter.next();
      asset.addComponentData(data);
    }

    // Process Plugins
    iter = 
      ((Collection)getDescendentsOfClass(ContainerBase.class)).iterator();
    while(iter.hasNext()) {
      ContainerBase container = (ContainerBase)iter.next();
      if(container.getShortName().equals("Plugins")) {
        for(int i=0; i < container.getChildCount(); i++) {
          GenericComponentData plugin = null;
          PluginBase pg = (PluginBase) container.getChild(i);
          plugin = new GenericComponentData();
          plugin.setType(ComponentData.PLUGIN);
          plugin.setParent(data);
          plugin.setOwner(this);
          data.addChild(pg.addComponentData(plugin));
        }
      }
    }
   
    return data;
  }
}

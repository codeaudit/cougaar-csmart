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
package org.cougaar.tools.csmart.society.file;

import java.io.IOException;
import java.io.ObjectInputStream;
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

public class AgentFileComponent
  extends ModifiableConfigurableComponent
  implements AgentComponent {

  private transient Logger log;
  private String name;
  private String classname;
  private String filename;

  /** Agent Classname Property Definitions **/
  public static final String PROP_CLASSNAME = "Classname";
  public static final String PROP_CLASSNAME_DESC = "Name of the Agent Class";
  
  public AgentFileComponent(String name, String classname) {
    super(name);
    this.name = name;
    this.classname = classname;
    filename = name + ".ini";
    createLogger();
    if(log.isDebugEnabled()) {
      log.debug("Creating Agent: " + name);
    }
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void initProperties() {
    Property p = addProperty(PROP_CLASSNAME, new String(classname));
    p.setToolTip(PROP_CLASSNAME_DESC);

    addPlugins();
    addAssetData();
  }

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

  private void addAssetData() {
    BaseComponent asset = (BaseComponent)new AssetFileComponent(filename);
    asset.initProperties();
    addChild(asset);
  }

  private void addPlugins() {
    ContainerBase container = new ContainerBase("Plugins");
    container.initProperties();
    addChild(container);

    if(log.isDebugEnabled()) {
      log.debug("Parse: " + filename);
    }
    ComponentDescription[] desc = 
      ComponentConnector.parseFile(filename);
    for (int i=0; i < desc.length; i++) {
      if(log.isDebugEnabled()) {
        log.debug("Create Plugin: " + desc[i].getName());
      }
      String name = desc[i].getName();
      int index = name.lastIndexOf('.');
      if (index != -1)
        name = name.substring(index+1);
      PluginBase plugin = 
        new PluginBase(name, desc[i].getClassname());
      plugin.initProperties();
      Iterator iter = ComponentConnector.getPluginProps(desc[i]);
      while(iter.hasNext()) 
        plugin.addParameter((String)iter.next());
      container.addChild(plugin);
    }
  }

  /**
   * Tests equality for agents.
   * Agents are equal if they have the same short name.
   * @return returns true if the object is an AgentComponent with same name
   */

  public boolean equals(Object o) {
    if (o instanceof AgentComponent) {
      AgentComponent that = (AgentComponent)o;
      if (!this.getShortName().equals(that.getShortName())  ) {
	return false;
      }     
      return true;
    }
    return false;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }


}

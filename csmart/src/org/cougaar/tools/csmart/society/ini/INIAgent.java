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
import java.util.Iterator;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.tools.csmart.core.cdata.ComponentConnector;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.util.PrototypeParser;
import org.cougaar.util.log.Logger;
import java.util.Collection;

/**
 * INIAgent.java
 *
 *
 * Created: Thu Feb 21 09:09:17 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class INIAgent extends ConfigurableComponent 
  implements AgentComponent, Serializable {

  private static final String PLUGIN_TYPE = "Plugin";
  private static final String ASSET_TYPE = "Asset";

  private transient Logger log;
  private String classname;
  private String name;

  private Property propAgentName;
  private Property propClassname;

  /** Agent Classname Property Definitions **/
  public static final String PROP_CLASSNAME = "Classname";
  public static final String PROP_CLASSNAME_DESC = "Name of the Agent Class";
  
  public INIAgent(String name, String classname) {
    super(name);
    this.name = name;
    this.classname = classname;
    createLogger();
    if(log.isDebugEnabled()) {
      log.debug("Creating Agent: " + name);
    }
  }

  public void initProperties() {
    propClassname = addProperty(PROP_CLASSNAME, new String(classname));
    propClassname.setToolTip(PROP_CLASSNAME_DESC);

    addPlugins();
    addAssetData();
  }
  
  public ComponentData addComponentData(ComponentData data) {
    // Process AssetData
    Iterator iter = ((Collection)getDescendentsOfClass(INIAsset.class)).iterator();
    while(iter.hasNext()) {
      INIAsset asset = (INIAsset)iter.next();
      asset.addComponentData(data);
    }

    // Process Plugins
    iter = ((Collection)getDescendentsOfClass(INIContainer.class)).iterator();
    while(iter.hasNext()) {
      INIContainer container = (INIContainer)iter.next();
      if(container.getShortName().equals("Plugins")) {
        for(int i=0; i < container.getChildCount(); i++) {
          GenericComponentData plugin = null;
          INIPlugin pg = (INIPlugin) container.getChild(i);
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

  public ComponentData modifyComponentData(ComponentData data) {
    return data;
  }

  public boolean equals(Object o) {
    return false;
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

  private void addAssetData() {
    INIAsset asset = new INIAsset(name);
    asset.initProperties();
    addChild(asset);
  }

  private void addPlugins() {
    INIContainer container = new INIContainer("Plugins");
    container.initProperties();
    addChild(container);

    // Load and parse file.
    String filename = name + ".ini";

    if(log.isDebugEnabled()) {
      log.debug("Parse: " + filename);
    }
    ComponentDescription[] desc = ComponentConnector.parseFile(filename);
    for(int i=0; i < desc.length; i++) {
      if(log.isDebugEnabled()) {
        log.debug("Create Plugin: " + desc[i].getName());
      }
      INIPlugin plugin = new INIPlugin(desc[i].getName(), desc[i].getClassname());
      plugin.initProperties();
      Iterator iter = ComponentConnector.getPluginProps(desc[i]);
      int pCount = 0;
      while(iter.hasNext()) {
        plugin.addParameter((String)iter.next());
        pCount++;
      }
//       plugin.getProperty(INIPlugin.PROP_PARAMC).setValue(new Integer(pCount));
      container.addChild(plugin);
    }
  }


}// INIAgent

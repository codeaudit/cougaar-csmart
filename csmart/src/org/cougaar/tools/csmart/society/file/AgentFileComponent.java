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
import org.cougaar.util.log.Logger;

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
import org.cougaar.tools.csmart.society.AgentBase;
import org.cougaar.tools.csmart.society.BinderBase;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Basic component to hold the definition of a society
 * read in from Files.
 */
public class AgentFileComponent
  extends AgentBase
  implements AgentComponent {

  private transient Logger log;
  private String filename; // that defines this agent

  /**
   * Creates a new <code>AgentFileComponent</code> instance.
   *
   * @param name Name of the new Component, from which the file is derived
   * @param classname Classname for the agent
   */
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

  protected void addAssetData() {
    BaseComponent asset = 
      (BaseComponent)new AssetFileComponent(filename, getShortName());
    asset.initProperties();
    addChild(asset);
  }

  protected void addPlugins() {
    ContainerBase container = new ContainerBase("Plugins");
    container.initProperties();
    addChild(container);

    ComponentDescription[] desc = 
      ComponentConnector.parseFile(filename);
    for (int i=0; i < desc.length; i++) {
      String name = desc[i].getName();
      String insertionPoint = desc[i].getInsertionPoint();
      if(log.isDebugEnabled()) {
        log.debug("Insertion Point: " + insertionPoint);
      }

      if(insertionPoint.endsWith("Plugin")) {
        if(log.isDebugEnabled()) {
          log.debug("Create Plugin: " + desc[i].getName());
        }
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
  }

  protected void addBinders() {
    ContainerBase container = new ContainerBase("Binders");
    container.initProperties();
    addChild(container);

    ComponentDescription[] desc = 
      ComponentConnector.parseFile(filename);
    for (int i=0; i < desc.length; i++) {
      String name = desc[i].getName();
      String insertionPoint = desc[i].getInsertionPoint();
      if(log.isDebugEnabled()) {
        log.debug("Insertion Point: " + insertionPoint);
      }

      if(insertionPoint.endsWith("Binder")) {
        if(log.isDebugEnabled()) {
          log.debug("Create Binder: " + name);
        }
        int index = name.lastIndexOf('.');
        if (index != -1)
          name = name.substring(index+1);
        BinderBase binder = 
          new BinderBase(name, desc[i].getClassname());
        binder.initProperties();
        
        // FIXME: Must I change ComponentConnector in some way here?
        Iterator iter = ComponentConnector.getPluginProps(desc[i]);
        while(iter.hasNext()) 
          binder.addParameter((String)iter.next());
        container.addChild(binder);
      }
    }
  }

} // end of AgentFileComponent

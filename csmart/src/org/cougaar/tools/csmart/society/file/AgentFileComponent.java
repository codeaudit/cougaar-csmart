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
import org.cougaar.util.ConfigFinder;

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
import org.cougaar.tools.csmart.society.ComponentBase;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.viewer.SocietyFinder;

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
   * @param name name of the agent
   * @param filename complete pathname of file that defines agent
   * @param classname Classname for the agent
   */
  public AgentFileComponent(String name, String filename, String classname) {
    super(name);
    this.classname = classname;
    this.filename = filename;
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void initProperties() {
    super.initProperties();
    
    addPlugins();
    addBinders();
    addComponents();
    addAssetData();
  }

  protected void addAssetData() {
    String assetFile = filename.substring(0, filename.lastIndexOf('.'));
    assetFile = assetFile + "-prototype-ini.dat";
    if (log.isDebugEnabled()) {
      log.debug("Trying asset file " + assetFile);
    }
    // Check to see if the file exists before we do anything!
    if(SocietyFinder.getInstance().locateFile(assetFile) != null) {
      BaseComponent asset = 
        (BaseComponent)new AssetFileComponent(filename, getShortName());
      asset.initProperties();
      addChild(asset);
    }
  }

  protected void addPlugins() {
    ContainerBase container = new ContainerBase("Plugins");
    container.initProperties();
    addChild(container);

    ComponentDescription[] desc = 
      ComponentConnector.parseFile(filename);
    if (desc == null) {
      if (log.isWarnEnabled()) {
	log.warn("No data found in file " + filename);
      }
      return;
    }
    for (int i=0; i < desc.length; i++) {
      StringBuffer name = new StringBuffer(desc[i].getName());
      StringBuffer className = new StringBuffer(desc[i].getClassname());
      String insertionPoint = desc[i].getInsertionPoint();
      String priority = desc[i].priorityToString(desc[i].getPriority());
//       if(log.isDebugEnabled()) {
//         log.debug("Insertion Point: " + insertionPoint);
//       }

      if(insertionPoint.endsWith("Plugin")) {
        int start = 0;
        if((start = name.indexOf("OrgRTData")) != -1) {
          name.delete(start, start+2);
          start = className.indexOf("RT");
          className.delete(start, start+2);
        }

	// HACK!
	// When dumping INIs we add an extra parameter to the GLSInitServlet,
	// so strip it off here
	boolean isGLS = false;
	if (className.indexOf("GLSInitServlet") != -1) {
	  isGLS = true;
	}

        int index = name.lastIndexOf(".");
        if (index != -1)
          name.delete(0,index+1);
        PluginBase plugin = 
          new PluginBase(name.substring(0), className.substring(0), priority);
        plugin.initProperties();
        Iterator iter = ComponentConnector.getPluginProps(desc[i]);
        while(iter.hasNext()) {
	  if (isGLS) {
	    String param = (String)iter.next();
	    if (param.startsWith("exptid="))
	      continue;
	    else
	      plugin.addParameter(param);
	  } else
	    plugin.addParameter((String)iter.next());
	}
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
    if (desc == null) {
      if (log.isWarnEnabled()) {
	log.warn("No data found in file " + filename);
      }
      return;
    }
    for (int i=0; i < desc.length; i++) {
      String name = desc[i].getName();
      String insertionPoint = desc[i].getInsertionPoint();
//       if(log.isDebugEnabled()) {
//         log.debug("Insertion Point: " + insertionPoint);
//       }

      if(insertionPoint.endsWith("Binder")) {
        if(log.isDebugEnabled()) {
          log.debug("Create Binder: " + name);
        }
        int index = name.lastIndexOf('.');
        if (index != -1)
          name = name.substring(index+1);
        BinderBase binder = 
          new BinderBase(name, desc[i].getClassname(), 
                         desc[i].priorityToString(desc[i].getPriority()),
                         insertionPoint);
        binder.initProperties();
        
        // FIXME: Must I change ComponentConnector in some way here?
        Iterator iter = ComponentConnector.getPluginProps(desc[i]);
        while(iter.hasNext()) 
          binder.addParameter((String)iter.next());
        container.addChild(binder);
      }
    }
  }

  protected void addComponents() {
    ContainerBase container = new ContainerBase("Other Components");
    container.initProperties();
    addChild(container);

    ComponentDescription[] desc = 
      ComponentConnector.parseFile(filename);
    if (desc == null) {
      if (log.isWarnEnabled()) {
	log.warn("No data found in file " + filename);
      }
      return;
    }
    for (int i=0; i < desc.length; i++) {
      String name = desc[i].getName();
      String insertionPoint = desc[i].getInsertionPoint();
//       if(log.isDebugEnabled()) {
//         log.debug("Insertion Point: " + insertionPoint);
//       }

      if(! insertionPoint.endsWith(".Binder") && ! insertionPoint.endsWith(".Plugin")) {
        if(log.isDebugEnabled()) {
          log.debug("Create Component: " + name);
        }
        int index = name.lastIndexOf('.');
        if (index != -1)
          name = name.substring(index+1);
        ComponentBase binder = 
          new ComponentBase(name, desc[i].getClassname(), 
                            desc[i].priorityToString(desc[i].getPriority()), 
                            insertionPoint);
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

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
package org.cougaar.tools.csmart.core.cdata;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.util.log.Logger;
import org.cougaar.util.ConfigFinder;
import org.cougaar.core.node.INIParser;
import org.cougaar.core.component.ComponentDescription;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.viewer.SocietyFinder;

/**
 * ComponentConnector.java
 *
 * ComponentConnector converts a <code>ComponentDescription</code> to a
 * <code>ComponentData</code> object.  When INI files are parsed, they
 * are parsed into a <code>ComponentDescription</code> object.  To use the
 * parsed data within CSMART, it must be of type <code>ComponentData</code>.
 *
 * Created: Wed Feb 20 09:40:54 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 * @see ComponentData
 * @see org.cougaar.core.component.ComponentDescription
 */

public class ComponentConnector {
  
  public static ComponentData createComponentData(String filename) {
    ComponentConnector cc= new ComponentConnector();
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.cdata.ComponentConnector");
    ComponentData data = new GenericComponentData();
    ComponentDescription[] desc = null;

    // Parse the Node Level file.
    desc = cc.parseFile(filename);

    for(int i=0; i < desc.length; i++) {
      ComponentData agent = cc.createComponentData(desc[i]);
      //Parse the Agent File.
      ComponentDescription[] agentDesc = cc.parseFile(agent.getName());
      for(int j=0; j < agentDesc.length; j++) {
        ComponentData plugin = cc.createComponentData(agentDesc[j]);
        plugin.setParent(agent);
        if(log.isDebugEnabled()) {
          log.debug("Creating: " + plugin.getName() + " with parent: " + agent.getName());
        }
        agent.addChild(plugin);
      }

      data.addChild(agent);
    }

    return data;
  }

  public static ComponentDescription[] parseFile(String filename) {
    ComponentDescription[] desc = null;
    String containerInsertionPoint = "";

    Logger log = 
     CSMART.createLogger("org.cougaar.tools.csmart.core.cdata.ComponentConnector");

    if(!filename.endsWith(".ini")) {
      filename = filename + ".ini";
    }

    if(log.isDebugEnabled()) {
      log.debug("Parsing File: " + filename);
    }

    try {
      InputStream in = SocietyFinder.getInstance().open(filename);
      //InputStream in = ConfigFinder.getInstance().open(filename);
      try { 
        desc = INIParser.parse(in, containerInsertionPoint);
      } finally {
        in.close();
      }
    } catch( Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Caught an error reading file", e);
      }
    }
    return desc;
  }

  private ComponentData createComponentData(ComponentDescription desc) {
    ComponentData data = null;

    Logger log = 
     CSMART.createLogger("org.cougaar.tools.csmart.core.cdata.ComponentConnector");

    String type = desc.getInsertionPoint();
    String shorttype = type.substring(type.lastIndexOf(".")+1);
    if(log.isDebugEnabled()) {
      log.debug("Component Type is: " + shorttype);
    }

    if(shorttype.equalsIgnoreCase("agent")) {
      if(log.isDebugEnabled()) {
        log.debug("Creating AgentComponentData");
      }

      data = new AgentComponentData();
      data.setType(ComponentData.AGENT);
      Vector v = (Vector)desc.getParameter();
      data.setName((String)v.get(0));

    } else {
      if(log.isDebugEnabled()) {
        log.debug("Creating GenericComponentData");
      }

      data = new GenericComponentData();
      data.setType(type);
      data.setName(desc.getName());
      Vector v = (Vector)desc.getParameter();
      for(int i=0; i < v.size(); i++) {
        data.addParameter(v.get(i));
      }
      if(shorttype.equalsIgnoreCase("plugin")) {
        data.setType(ComponentData.PLUGIN);
      } else {
	if (shorttype.equalsIgnoreCase("binder")) {
	  // Node or Agent?
	  if (type.equalsIgnoreCase("Node.AgentManager.Binder")) 
	    data.setType(ComponentData.NODEBINDER);
	  else if (type.equalsIgnoreCase("Node.AgentManager.PluginManager.Binder")) 
	    data.setType(ComponentData.NODEBINDER);
	}
        if(log.isWarnEnabled()) {
          log.warn("Un-Supported Type: " + shorttype);
        }
        // TODO: Add all other types.
      }
    }
    if(log.isDebugEnabled()) {
      log.debug("Name: " + data.getName());
    }

    data.setClassName(desc.getClassname());
    return data;
  }

  public static String getAgentName(ComponentDescription desc) {
    Vector v = (Vector)desc.getParameter();
    return (String)v.get(0);
  }

  public static Iterator getPluginProps(ComponentDescription desc) {
    Vector v = (Vector)desc.getParameter();
    return v.iterator();
  }

//   public static ComponentData createComponentData(String compName, 
//                                                   ComponentDescription desc) {
//     ComponentData data = null;

//     Logger log = 
//      CSMART.createLogger("org.cougaar.tools.csmart.core.cdata.ComponentConnector");

//     String type = desc.getInsertionPoint();
//     type = type.substring(type.lastIndexOf(".")+1);
//     if(log.isDebugEnabled()) {
//       log.debug("Component Type is: " + type);
//     }

//     if(type.equalsIgnoreCase("agent")) {
//       if(log.isDebugEnabled()) {
//         log.debug("Creating AgentComponentData");
//       }

//       data = new AgentComponentData();
//       data.setType(ComponentData.AGENT);
//       Vector v = (Vector)desc.getParameter();
//       data.setName((String)v.get(0));

//     } else {
//       if(log.isDebugEnabled()) {
//         log.debug("Creating GenericComponentData");
//       }

//       data = new GenericComponentData();
//       if(type.equalsIgnoreCase("plugin")) {
//         data.setType(ComponentData.PLUGIN);
//       } else {
//         if(log.isWarnEnabled()) {
//           log.warn("Un-Supported Type: " + type);
//         }
//         // TODO: Add all other types.
//       }
//       data.setName(desc.getName());
//       Vector v = (Vector)desc.getParameter();
//       for(int i=0; i < v.size(); i++) {
//         data.addParameter(v.get(i));
//       }
//     }

//     data.setClassName(desc.getClassname());
//     return data;
//   }

    
}// ComponentConnector

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

import org.cougaar.tools.csmart.society.SocietyBase;
import java.io.Serializable;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.tools.csmart.core.cdata.ComponentConnector;
import org.cougaar.tools.csmart.society.AgentComponent;
import java.util.ArrayList;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import java.util.Collection;
import java.util.Iterator;


/**
 * INISociety.java
 *
 *
 * Created: Thu Feb 21 09:41:24 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class INISociety 
  extends SocietyBase 
  implements Serializable {

  private ComponentDescription[] desc;
  private String name;

  /**
   * Creates a new <code>INISociety</code> instance.
   * 
   * @param name The name of the Node.ini file used to
   * load in all agent data.  For example: MiniNode.ini
   */
  public INISociety (String name){
    super(name);
    this.name = name;
  }
  
  public void initProperties() {
    //    String filename = name + ".ini";
    String filename = name;

    if(log.isDebugEnabled()) {
      log.debug("Parse File: " + filename);
    }
    desc = ComponentConnector.parseFile(filename);
    for(int i=0; i < desc.length; i++) {
      String agentName = ComponentConnector.getAgentName(desc[i]);
      if(log.isDebugEnabled()) {
        log.debug("Name: " + agentName + " Class: " + desc[i].getClassname());
      }
      INIAgent agent = new INIAgent(agentName, desc[i].getClassname());
      agent.initProperties();
      addChild(agent);
    }
  }

  public AgentComponent[] getAgents() {
    ArrayList agents = new ArrayList(getDescendentsOfClass(INIAgent.class));
    return (AgentComponent[])agents.toArray(new AgentComponent[agents.size()]);
  }

  public BaseComponent copy(BaseComponent result) {
    result = super.copy(result);
    return result;
  }

  public ComponentData addComponentData(ComponentData data) {
    ComponentData[] children = data.getChildren();

    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
      if(child.getType() == ComponentData.AGENT) {
        Iterator iter = ((Collection)getDescendentsOfClass(INIAgent.class)).iterator();

        while(iter.hasNext()) {
          INIAgent agent = (INIAgent)iter.next();
          if(child.getName().equals(agent.getFullName().toString())) {
            child.setOwner(this);
            agent.addComponentData(child);
          }
        }
      } else {
        addComponentData(child);
      }
    }
    return data;
  }

}// INISociety

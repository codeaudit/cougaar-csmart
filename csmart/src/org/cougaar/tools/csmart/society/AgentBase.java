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
package org.cougaar.tools.csmart.society;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * AgentBase.java
 *
 * Implements generic methods required by all agents.
 *
 * @author
 * @version
 */

public class AgentBase extends ModifiableConfigurableComponent implements AgentComponent {
  private Property[] propRelations = null;
  private Property[] propRoles = null;
  private static final String PROP_CLASSNAME = "Classname";
  private static final String PROP_CLASSNAME_DESC = "Name of the Agent Class";

  public AgentBase(String name) {
    super(name);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void initProperties(Property[] relations, Property[] roles) {
    this.initProperties();
    this.propRelations = relations;
    this.propRoles = roles;
  }

  /**
   * Just add default class name for agent which is ClusterImpl.
   */

  public void initProperties() {
    Property p = addProperty(PROP_CLASSNAME, 
                             "org.cougaar.core.agent.ClusterImpl");
    p.setToolTip(PROP_CLASSNAME_DESC);
  }

  public ComponentData addComponentData(ComponentData data) {
    if (this.getShortName().equals(data.getName())) {
      // TODO: this section was in INIAgent, should it be in AgentBase?
//          if (((Boolean)propOrgAsset.getValue()).booleanValue()) {
//            // Add the OrgRTData plugin.
//            ComponentData plugin = new GenericComponentData();
//            plugin.setType(ComponentData.PLUGIN);
//            plugin.setName("org.cougaar.mlm.plugin.organization.OrgDataPlugin");
//            plugin.setParent(data);
//            plugin.setClassName("org.cougaar.mlm.plugin.organization.OrgDataPlugin");
//            plugin.setOwner(this);
//            data.addChild(plugin);

//            // Add the OrgReport plugin.
//            plugin = new GenericComponentData();
//            plugin.setType(ComponentData.PLUGIN);
//            plugin.setName("org.cougaar.mlm.plugin.organization.OrgReportPlugin");
//            plugin.setParent(data);
//            plugin.setClassName("org.cougaar.mlm.plugin.organization.OrgReportPlugin");
//            plugin.setOwner(this);
//            data.addChild(plugin);
//          }
    } else if (data.childCount() > 0) {
      // for each child, call this same method.
      ComponentData[] children = data.getChildren();
      for (int i = 0; i < children.length; i++) {
        data = addComponentData(children[i]);
      }
    }
    return data;
  }


  // TODO: from INIAgent; is this needed; if so, why?
//      public boolean equals(Object o) {
//        if (o instanceof AgentComponent) {
//          AgentComponent that = (AgentComponent)o;
//          if (!this.getShortName().equals(that.getShortName())  ) {
//            return false;
//          }     
//          return true;
//        }
//        return false;
//      }
}

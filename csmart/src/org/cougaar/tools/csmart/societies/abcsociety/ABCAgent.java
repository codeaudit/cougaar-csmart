/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.societies.abcsociety;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;

import org.cougaar.tools.csmart.ui.component.*;

import org.cougaar.planning.ldm.plan.Role;

/**
 * Defines an Agent within an ABC Society.
 */

public class ABCAgent
   extends ConfigurableComponent
  implements Serializable, AgentComponent
{

  private int level;
  private int index;
  private String type;

  /** Agent Index Property Definitions **/
  public static final String PROP_AGENT_INDEX = "Index";
  public static final String PROP_AGENT_INDEX_DESC = "Unqiue index for each agent";

  /** Agent Name Property Definitions **/
  public static final String PROP_NAME = "Agent Name";
  public static final String PROP_NAME_DFLT = "Agent";
  public static final String PROP_NAME_DESC = "Name of the Agent";

  /** Distance Property Definitions **/
  public static final String PROP_DISTANCE = "Distance";
  public static final Integer PROP_DISTANCE_DFLT = new Integer(10);
  public static final String PROP_DISTANCE_DESC = "Agents Distance, in meters, from the center of the community";

  /** Direction Property Definitions **/
  public static final String PROP_DIRECTION = "Direction";
  public static final Integer PROP_DIRECTION_DFLT = new Integer(0);
  public static final String PROP_DIRECTION_DESC = "Agents Direction, in degrees relative to North, from the center of the community";

  /** Supplies Property Definitions **/
  public static final String PROP_SUPPLIES = "Supplies";
  public static final String[] PROP_SUPPLIES_DFLT = new String[0];
  public static final String PROP_SUPPLIES_DESC = "All Agents that this agent supplies assets to";

  /** Start Time Property Definitions **/
  public static final String PROP_STARTTIME = "Start Time";
  public static final Long PROP_STARTTIME_DFLT = new Long(0);
  public static final String PROP_STARTTIME_DESC = "Start time for Tasks";

  /** Stop Time Property Definitions **/
  public static final String PROP_STOPTIME = "Stop Time";
  public static final Long PROP_STOPTIME_DFLT = new Long(40000);
  public static final String PROP_STOPTIME_DESC = "Stop Time for Tasks";
  
  private static final String agentClassName = "org.cougaar.core.agent.ClusterImpl";

  /** Full names of all plugins **/
  private static final String CustomerPlugIn_name =
    "org.cougaar.tools.csmart.plugin.CustomerPlugIn";
  private static final String AllocatorPlugIn_name =
    "org.cougaar.tools.csmart.plugin.AllocatorPlugIn";
  private static final String ExecutorPlugIn_name =
    "org.cougaar.tools.csmart.plugin.ExecutorPlugIn";
  private static final String PlanServerPlugIn_name =
    "org.cougaar.lib.planserver.PlanServerPlugIn";
  private static final String AssetBuilderPlugIn_name =
    "org.cougaar.tools.csmart.plugin.LocalAssetBuilder";
  private static final String AssetDataPlugIn_name =
    "org.cougaar.planning.plugin.AssetDataPlugIn";
  private static final String AssetReportPlugIn_name =
    "org.cougaar.planning.plugin.AssetReportPlugIn";

  private Property propDistance;
  private Property propDirection;
  private Property propName;
  private Property propIndex;
  private Property propSupplies;

  ABCAgent(int level, String type, int index) {
    super("Agent-" + level + "-" + type + index);
    this.level = level;
    this.type = type;
    this.index = index;
  }

  ABCAgent(String comm, String type, int index) {
    //    super("Agent-" + comm + "-" + type + index);

    // Shorten agent name by removing the Agent-
    //    super("Agent-" + type + index);
    // Do I want to add the comm to it to ensure the comm name is part of the agent name?
    super(type + index);
    this.level = level;
    this.type = type;
    this.index = index;
  }

  /**
   * Initializes all local properties 
   */
  public void initProperties() {
    propIndex = addProperty(PROP_AGENT_INDEX, new Integer(index));
    propIndex.setToolTip(PROP_AGENT_INDEX_DESC);
    setPropertyVisible(propIndex, false);

    propName = addProperty(PROP_NAME, PROP_NAME_DFLT);
    propName.setToolTip(PROP_NAME_DESC);
    setPropertyVisible(propName, false);

    propDistance = addProperty(PROP_DISTANCE, PROP_DISTANCE_DFLT);
    propDistance.setToolTip(PROP_DISTANCE_DESC);
    propDistance.setAllowedValues(Collections.singleton(new IntegerRange(0, Integer.MAX_VALUE)));
    
    propDirection = addProperty(PROP_DIRECTION, PROP_DIRECTION_DFLT);
    propDirection.setToolTip(PROP_DIRECTION_DESC);
    propDirection.setAllowedValues(Collections.singleton(new IntegerRange(0, 360)));

    propSupplies = addProperty(PROP_SUPPLIES, PROP_SUPPLIES_DFLT, new PropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
        }
    });
    propSupplies.setToolTip(PROP_SUPPLIES_DESC);

    if(type.equalsIgnoreCase("provider")) {
      addProviderPlugIns();
    } else {
      addCustomerPlugIn();
    }
    addStandardPlugIns();
  }


  /**
   * Adds a new alias property to this agent.  An aliased property
   * will reflect the changes made in a parent component.
   *
   * @param prop The new alias property
   * @param id The ID to associate to this new property
   * @return The newly aliased property
   */
  public Property addAliasProperty(Property prop, String id) {
    Property p = addProperty(new PropertyAlias(this, id, prop));
    prop.addPropertyListener(new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
      }); 
    return p;
  }

  /**
   * Builds the <code>ComponentData</code> structure for this
   * agent.  The data strucutre contains all information about
   * this agent required by the server to run.
   *
   * @param data a <code>ComponentData</code> value
   * @return a <code>ComponentData</code> value
   */
  public ComponentData addComponentData(ComponentData data) {

    // Add Asset Data PlugIn
    GenericComponentData plugin = new GenericComponentData();
    plugin.setType(ComponentData.PLUGIN);
    plugin.setParent(data);
    plugin.setName(AssetDataPlugIn_name);
    plugin.setOwner(this);
    data.addChild(plugin);

    // Add Asset Report PlugIn
    plugin = new GenericComponentData();
    plugin.setType(ComponentData.PLUGIN);
    plugin.setParent(data);
    plugin.setOwner(this);
    plugin.setName(AssetReportPlugIn_name);
    data.addChild(plugin);

    for(int i = 0 ; i < getChildCount(); i++) {
      if(getChild(i) instanceof ABCPlugIn) {
	ABCPlugIn pg = (ABCPlugIn) getChild(i);
	plugin = new GenericComponentData();
	plugin.setOwner(this);
	plugin.setParent(data);
	plugin.setType(ComponentData.PLUGIN);
	data.addChild(pg.addComponentData(plugin));
      }	
    }

    plugin = new GenericComponentData();
    plugin.setType(ComponentData.PLUGIN);
    plugin.setParent(data);
    plugin.setOwner(this);
    plugin.setName(PlanServerPlugIn_name);
    data.addChild(plugin);

    // Add data file leaves.
    data = createLeafComponents(data);

    data = addAssetData(data);

    return data;
  }

  
  /**
   * Performs any modifications to the ComponentData structure.  
   * <code>addComponentData</code> sets all the data for this agent,
   * once all agents add their data, <code>modifyComponentData</code>
   * is called for every agent to perform any last minute modifications
   * before the structure is handed off to the server.
   *
   * @param data ComponentData structure with previously set values.
   * @return a <code>ComponentData</code> value
   */
  public ComponentData modifyComponentData(ComponentData data) {
    return data;
  }


  /** Private Methods **/

  /**
   * Adds all plugins required by a Provider
   */
  private void addProviderPlugIns() {
    ABCPlugIn plugin;
    plugin = new ABCPlugIn("LocalAssetBuilder", AssetBuilderPlugIn_name);
    addChild(plugin);
    plugin.initProperties();
    Property p = null;
    Iterator iter = ((Collection)getDescendentsOfClass(ABCLocalAsset.class)).iterator();
    while(iter.hasNext()) {
      ABCLocalAsset ala = (ABCLocalAsset)iter.next();
      p = ala.addProperty(ABCLocalAsset.PROP_ASSETFILENAME, ala.getFullName().toString() + ".dat");
      setPropertyVisible(p, false);
    }
    addPropertyAlias(ABCLocalAsset.PROP_ASSETFILENAME, plugin, (String)p.getValue());
    setPropertyVisible(getProperty(ABCLocalAsset.PROP_ASSETFILENAME), false);

    plugin = new ABCPlugIn("Executor", ExecutorPlugIn_name);
    addChild(plugin);
    plugin.initProperties();
  }

  /**
   * Adds all standard plugins
   */
  private void addStandardPlugIns() {
    ABCPlugIn plugin;

    plugin = new ABCPlugIn("Allocator", AllocatorPlugIn_name);
    addChild(plugin);
    plugin.initProperties();
    Property p = null;
    Iterator iter = ((Collection)getDescendentsOfClass(ABCAllocation.class)).iterator();
    while(iter.hasNext()) {
      ABCAllocation alloc = (ABCAllocation)iter.next();
      p = alloc.addProperty(ABCAllocation.PROP_ALLOCFILENAME, alloc.getFullName().toString() + ".dat");
      setPropertyVisible(p, false);
    }
    addPropertyAlias(ABCAllocation.PROP_ALLOCFILENAME, plugin, (String)p.getValue());    
    setPropertyVisible(getProperty(ABCAllocation.PROP_ALLOCFILENAME), false);
  }


  /**
   * Adds a Customer Plugin
   */
  private void addCustomerPlugIn() {
    ABCPlugIn plugin;

    plugin = new ABCPlugIn("Customer", CustomerPlugIn_name);
    addChild(plugin);
    plugin.initProperties();
    addPropertyAlias(PROP_STARTTIME, plugin, getProperty(PROP_STARTTIME).getValue());
    addPropertyAlias(PROP_STOPTIME, plugin, getProperty(PROP_STOPTIME).getValue());
    Property p = null;
    Iterator iter = ((Collection)getDescendentsOfClass(ABCTaskFile.class)).iterator();
    while(iter.hasNext()) {
      ABCTaskFile t = (ABCTaskFile)iter.next();
      p = t.addProperty(ABCTaskFile.PROP_TASKFILENAME, t.getFullName().toString() + ".dat");
      setPropertyVisible(p, false);
      break;
    }
    addPropertyAlias(ABCTaskFile.PROP_TASKFILENAME, plugin, (String)p.getValue());
    setPropertyVisible(getProperty(ABCTaskFile.PROP_TASKFILENAME), false);
  }


  private Property addPropertyAlias(String propName, ABCPlugIn c, Object value) {
    Property ourProp = addProperty(propName, value);
    Property childProp = addPropertyAlias(c, ourProp);
    return ourProp;
  }

  private Property addPropertyAlias(ABCPlugIn c, Property prop) {
    Property childProp = c.addParameter(prop);
    setPropertyVisible(childProp, false);
    return childProp;
  }

  private ComponentData createLeafComponents(ComponentData data) {
    StringBuffer sb = null;

    for(int i=0; i < getChildCount(); i ++) {
      if(getChild(i) instanceof ABCTaskFile) {
	data.addLeafComponent(((ABCTaskFile) getChild(i)).createTaskFileLeaf());
      } else if(getChild(i) instanceof ABCLocalAsset) {
	data.addLeafComponent(((ABCLocalAsset) getChild(i)).createAssetFileLeaf());
      } else if(getChild(i) instanceof ABCAllocation) {
	data.addLeafComponent(((ABCAllocation) getChild(i)).createAllocationLeaf());
      }
    }    

    return data;
  }


  /**
   * Returns a list of all known roles for all allocations.
   */
  private Collection getAllRoles() {
    ArrayList rlist = new ArrayList(10);

    Iterator allocations = ((Collection)getDescendentsOfClass(ABCAllocation.class)).iterator();
    while(allocations.hasNext()) {
      ABCAllocation alloc = (ABCAllocation)allocations.next();
      Iterator rules = ((Collection)alloc.getDescendentsOfClass(ABCAllocationRule.class)).iterator();
      while(rules.hasNext()) {
	ABCAllocationRule rule = (ABCAllocationRule)rules.next();
	String[] roles = (String[])rule.getProperty(ABCAllocationRule.PROP_ROLES).getValue();
	for(int i=0; i < roles.length; i++) {
	  rlist.add(roles[i]);
	}
      }
    }
    return rlist;
  }

  private PropGroupData getItemIdentificationPG() {
    PropGroupData pgData = new PropGroupData(PropGroupData.ITEM_IDENTIFICATION);
    PGPropData propData = new PGPropData();
    propData.setName("ItemIdentification");
    propData.setType("String");
    propData.setValue(getFullName().toString());
    pgData.addProperty(propData);

    propData = new PGPropData();
    propData.setName("Nomenclature");
    propData.setType("String");
    propData.setValue(getFullName().get(1).toString() + "." + getFullName().get(2).toString());
    pgData.addProperty(propData);

    propData = new PGPropData();
    propData.setName("AlternateItemIdentification");
    propData.setType("String");
    propData.setValue(getFullName().get(1).toString() + "." + getFullName().get(2).toString());
    pgData.addProperty(propData);

    return pgData;
  }

  private PropGroupData getTypeIdentificationPG() {

    PropGroupData pgData = new PropGroupData(PropGroupData.TYPE_IDENTIFICATION);
    PGPropData propData = new PGPropData();
    propData.setName("TypeIdentification");
    propData.setType("String");
    propData.setValue(getFullName().get(2).toString());
    pgData.addProperty(propData);

    propData = new PGPropData();
    propData.setName("Nomenclature");
    propData.setType("String");
    propData.setValue(getFullName().get(2).toString());
    pgData.addProperty(propData);

    propData = new PGPropData();
    propData.setName("AlternateTypeIdentification");
    propData.setType("String");
    propData.setValue(getFullName().get(2).toString());
    pgData.addProperty(propData);

    return pgData;
  }


  private PropGroupData getClusterPG() {

    PropGroupData pgData = new PropGroupData(PropGroupData.CLUSTER);
    PGPropData propData = new PGPropData();
    propData.setName("ClusterIdentifier");
    propData.setType("ClusterIdentifier");
    propData.setValue(getFullName().toString());
    pgData.addProperty(propData);

    return pgData;
  }

  private PropGroupData getEntityPG() {

    PGPropMultiVal values = new PGPropMultiVal();
    PropGroupData pgData = new PropGroupData(PropGroupData.ENTITY);
    PGPropData propData = new PGPropData();
    propData.setName("Roles");
    propData.setType("Collection");
    propData.setSubType("Role");
    
    // Only print the quoted collection of roles if there are any
    if (! getAllRoles().isEmpty()) {
      Iterator iter = getAllRoles().iterator();
      String role = null;
      // No comma before the first role
      if (iter.hasNext()) {
	role = (String)iter.next();
	// If this agent is a customer, dont write this as having provider roles
	// say it has the customer equivalent
	if (type.equalsIgnoreCase("customer")) {
	  // For now, just skip writing out the role
	  while (role.endsWith("rovider") && iter.hasNext())
	    role = (String)iter.next();
	  if (role.endsWith("rovider"))
	    role = null;
	}
	if (role != null) {
	  values.addValue(role);
	}
      }
    }
    propData.setValue(values);

    // If there are no values, we do not want 
    // to write anything out.
    if(values.getValueCount() != 0) {
      return null;
    }

    return pgData;
  }

  private PropGroupData getCommunityPG() {

    PropGroupData pgData = new PropGroupData(PropGroupData.COMMUNITY);
    PGPropData propData = new PGPropData();
    propData.setName("TimeSpan");
    propData.setType("TimeSpan");
    propData.setValue("");       // Leave empty
    pgData.addProperty(propData);
    
    propData = new PGPropData();
    propData.setName("Communities");
    propData.setType("Collection");
    propData.setSubType("String");
    PGPropMultiVal values = new PGPropMultiVal();
    values.addValue(getFullName().get(1).toString());
    propData.setValue(values);
    pgData.addProperty(propData);

    return pgData;
  }

  private ComponentData addAssetData(ComponentData data) {
    AgentAssetData assetData = new AgentAssetData((AgentComponentData)data);

    assetData.setType(AgentAssetData.ENTITY);
    assetData.setAssetClass("Entity");

    // Add all Relationship data
    Iterator iter = getAllRoles().iterator();
    while(iter.hasNext()) {
      String role = (String)iter.next();
      String[] supplies = (String[])getProperty(PROP_SUPPLIES).getValue();
      for(int i=0; i < supplies.length; i++) {
	RelationshipData relData = new RelationshipData();
	relData.setRole(role);
	relData.setItem(supplies[i].trim());
	relData.setType(supplies[i].substring(supplies[i].lastIndexOf(".")+1));
	relData.setSupported(supplies[i].trim());
	assetData.addRelationship(relData);
      }
    }
    
    // Add all the PG's
    assetData.addPropertyGroup(getItemIdentificationPG());
    assetData.addPropertyGroup(getTypeIdentificationPG());
    assetData.addPropertyGroup(getClusterPG());

    PropGroupData pgData = getEntityPG();
    if(pgData != null)
      assetData.addPropertyGroup(pgData);         

    assetData.addPropertyGroup(getCommunityPG());

    data.addAgentAssetData(assetData);

    return data;
  }


}

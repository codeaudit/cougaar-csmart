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
package org.cougaar.tools.csmart.configgen.abcsociety;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;

import org.cougaar.tools.csmart.ui.component.*;

import org.cougaar.domain.planning.ldm.plan.Role;

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

  /** Metrics Properties Definitions **/

  /** Number of Metrics Providers Property Definitions **/
  public static final String PROP_NUMBPROVIDERS = "Metrics: Number of Providers";
  public static final Integer PROP_NUMBPROVIDERS_DFLT = new Integer(0);
  public static final String PROP_NUMBPROVIDERS_DESC = "Number of Agents providing metrics";

  /** Sample Interval Property Definitions **/
  public static final String PROP_SAMPLEINTERVAL = "Metrics: Sample Interval";
  public static final Integer PROP_SAMPLEINTERVAL_DFLT = new Integer(10);
  public static final String PROP_SAMPLEINTERVAL_DESC = "Sample Interval to collect metrics";

  /** Start Delay Property Definitions **/
  public static final String PROP_STARTDELAY = "Metrics: Start Delay";
  public static final Integer PROP_STARTDELAY_DFLT = new Integer(0);
  public static final String PROP_STARTDELAY_DESC = "Delay to wait before metric collection";

  /** Maximum Number of Samples Property Definitions **/
  public static final String PROP_MAXNUMBSAMPLES = "Metrics: Maximum Number of Samples";
  public static final Integer PROP_MAXNUMBSAMPLES_DFLT = new Integer(20);
  public static final String PROP_MAXNUMBSAMPLES_DESC = "Number of metric samples to collect";

  /** Metrics Initiailzer Property Definitions **/
  public static final String PROP_INITIALIZER = "Metrics: Initializer PlugIn";
  public static final String PROP_INITIALIZER_DFLT = "";
  public static final String PROP_INITIALIZER_DESC = "Name of the Metric Initialization plugin";

  
  private static final String agentClassName = "org.cougaar.core.cluster.ClusterImpl";

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
  private static final String MetricsPlugIn_name =
    "org.cougaar.tools.csmart.plugin.MetricsPlugin";
  private static final String MetricsInitializerPlugIn_name =
    "org.cougaar.tools.csmart.plugin.MetricsInitializerPlugin";
  private static final String AssetDataPlugIn_name =
    "org.cougaar.domain.planning.plugin.AssetDataPlugIn";
  private static final String AssetReportPlugIn_name =
    "org.cougaar.domain.planning.plugin.AssetReportPlugIn";
  // HACK: Always include this PlugIn for ABCImpacts, even though
  // only need it if using such an impact in our experiment
  private static final String ABCImpactPlugIn_name =
    "org.cougaar.tools.csmart.plugin.ABCImpactPlugin";

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
   * Writes the agent ini file to the sepcified directory.
   *
   * @param File Directory to place ini file
   * @throws IOException if the file cannot be created
   */
  public void writeIniFile(File configDir) throws IOException {

    File iniFile = new File(configDir, getFullName() + ".ini");
    PrintWriter writer = new PrintWriter(new FileWriter(iniFile));

    try {
      writer.println("# $id$");
      writer.println("[ Cluster ]");
      writer.println("class = " + agentClassName);
      writer.println("uic = \"" + getFullName().toString() + "\"");
      writer.println("cloned = false");
      writer.println();
      writer.println("[ PlugIns ]");
      writer.println("plugin = " + AssetDataPlugIn_name);
      writer.println("plugin = " + AssetReportPlugIn_name);

//       // Add the initializer plugin if this is the Initializer Agent.
//       if(getFullName().toString().equals(getProperty(PROP_INITIALIZER).getValue())) {
// 	ABCPlugIn init = new ABCPlugIn("MetricsInitializer", MetricsInitializerPlugIn_name);
// 	addChild(init);
// 	init.initProperties();
// 	addPropertyAlias(init, getProperty(PROP_NUMBPROVIDERS));
// 	addPropertyAlias(init, getProperty(PROP_SAMPLEINTERVAL));
// 	addPropertyAlias(init, getProperty(PROP_STARTDELAY));
// 	addPropertyAlias(init, getProperty(PROP_MAXNUMBSAMPLES));
// 	writer.println(init.getConfigLine());
//       }
      
      // Add all other plugins
      for(int i=0, n = getChildCount(); i < n; i++) {
	if(getChild(i) instanceof ABCPlugIn) {
	  ABCPlugIn plugin = (ABCPlugIn) getChild(i);
	  writer.println(plugin.getConfigLine());
	}
      }

      writer.println("plugin = " + PlanServerPlugIn_name);
      // HACK: Add this PlugIn always, though only used if have an ABCImpact
      // in the society
      //      writer.println("plugin = " + ABCImpactPlugIn_name);
      writer.println();
      writer.println("[ Policies ]");
      writer.println();
      writer.println("[ Permission ]");
      writer.println();
      writer.println("[ AuthorizedOperation ]");
    }
    finally {
      writer.close();
    }
    
    // Write any other associated data files.
    writeDataFiles(configDir);
  }

  /**
   * Returns the configuration line:   <br>
   * cluster = <agent name>
   *
   * @return configuration line for this agent
   */
  public String getConfigLine() {
    return "cluster = " + getFullName();
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
   * Writes on the Prototype-ini.dat file for this agent.
   *
   * @param File The directory to write the file to.
   * @throws IOException if the file cannot be created.
   */
//   public void writePrototypeIniFile(File configDir) throws IOException {
//     File taskFile = new File(configDir, getFullName().toString() + "-prototype-ini.dat");
//     PrintWriter writer = new PrintWriter(new FileWriter(taskFile));
  
  public void writePrototypeIniFile(PrintWriter writer) {

    try {
      writer.println("[Prototype] Entity");
      writer.println();
      writer.println("[Relationship]");
      writer.println("#Role     ItemIdentification      TypeIdentification     Cluster   Start    End");
      Iterator iter = getAllRoles().iterator();
      while(iter.hasNext()) {
	String role = (String)iter.next();
	String[] supplies = (String[])getProperty(PROP_SUPPLIES).getValue();
	for(int i=0; i < supplies.length; i++) {
	  String type = supplies[i].substring(supplies[i].lastIndexOf(".")+1);
	  writer.println("\"" + role + "\"  \"" + supplies[i].trim() + "\"  \"" + type + "\" \"" + supplies[i].trim() 
			 + "\" \"\" \"\"");	  
	}

// 	// Add MetricPlugin Role.
// 	String initializer = (String)getProperty(PROP_INITIALIZER).getValue();
// 	writer.println("\"MetricsControlProvider\"  \"" + initializer +
// 		       "\"  \"" + initializer.substring(initializer.lastIndexOf(".")+1) + 
// 		       "\"  \"" + initializer + "\"  \"\"  \"\"");
      }
      writer.println();
      writer.println("[ItemIdentificationPG]");
      writer.println("ItemIdentification String \"" + getFullName().toString() + "\"");
      // Make these next two be Community#.Customer/Provider#
      // maybe getName().get(1).toString() or getName().get(1).toString() + "." + getName().get(2).toString()
      writer.println("Nomenclature String \"" + getFullName().get(1).toString() + "." + getFullName().get(2).toString() + "\"");
      writer.println("AlternateItemIdentification String \"" + getFullName().get(1).toString() + "." + getFullName().get(2).toString() + "\"");
      //      writer.println("Nomenclature String \"" + getName().get(2).toString() + "\"");
      //      writer.println("AlternateItemIdentification String \"" + getName().get(2).toString() + "\"");
      writer.println();
      writer.println("[TypeIdentificationPG]");
      writer.println("TypeIdentification String \"" + getFullName().get(2).toString() + "\"");
      writer.println("Nomenclature String \"" + getFullName().get(2).toString() + "\"");
      writer.println("AlternateTypeIdentification String \"" + getFullName().get(2).toString() + "\"");
      writer.println();
      writer.println("[ClusterPG]");
      writer.println("ClusterIdentifier ClusterIdentifier \"" + getFullName().toString() + "\"");
      writer.println();
      writer.println("[EntityPG]");
      writer.print("Roles Collection<Role> ");
      // Only print the quoted collection of roles if there are any
      if (! getAllRoles().isEmpty()) {
	iter = getAllRoles().iterator();
	String role = null;
	// No comma before the first role
	if (iter.hasNext()) {
	  role = (String)iter.next();
	  // If this agent is a customer, dont write this as having provider roles
	  // say it has the customer equivalent
	  if (type.equalsIgnoreCase("customer")) {
//  	    if (role.endsWith("rovider"))
//  	      role = Role.getRole(role).getConverse().getName();
	    // For now, just skip writing out the role
	    while (role.endsWith("rovider") && iter.hasNext())
	      role = (String)iter.next();
	    if (role.endsWith("rovider"))
	      role = null;
	  }
	  if (role != null) {
	    writer.print("\"");
	    writer.print(role);
	  }
	}
	// For all subsequent roles, do a comma, a space, then the role
	while(iter.hasNext()) {
	  role = (String)iter.next();
	  // If this agent is a customer, dont write this as having provider roles
	  // say it has the customer equivalent
	  if (type.equalsIgnoreCase("customer"))
	    if (role.endsWith("rovider"))
	      //	      role = Role.getRole(role).getConverse().getName();
	      continue;
	  writer.print(", " + role);
	  //	writer.print(" \"" + iter.next() + "\" ");
	}
	// close the list of roles with quotes
	writer.print("\"");
      } // end of printing Collection of Roles
      writer.println();
      writer.println();
      writer.println("[CommunityPG]");
      writer.println("TimeSpan TimeSpan \"\"");
      writer.print("Communities    Collection<String> ");
      writer.println("\"" + getFullName().get(1).toString() + "\"");
    } 
    finally {
      writer.close();
    }    
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

    plugin = new ABCPlugIn("Metrics", MetricsPlugIn_name);
    addChild(plugin);
    plugin.initProperties();

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


  /** 
   * Writes all required datafiles.
   * 
   * @param File directory to write data files to
   */
  private void writeDataFiles(File configDir) {
    for(int i=0; i < getChildCount(); i ++) {
      if(getChild(i) instanceof ABCTaskFile) {
	try {
	  ((ABCTaskFile) getChild(i)).writeTaskFile(configDir);
	} catch(IOException e) {System.out.println("Exception writing task file");}
      } else if(getChild(i) instanceof ABCLocalAsset) {
	try {
	  ((ABCLocalAsset) getChild(i)).writeAssetFile(configDir);
	} catch(IOException e) {}
      } else if(getChild(i) instanceof ABCAllocation) {
	try {
	  ((ABCAllocation) getChild(i)).writeAllocationFile(configDir);
	} catch(IOException e) {}
      }
    }
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

  private ComponentData addTimePhasedData(ComponentData data) {
    // Add Community
    CommunityTimePhasedData ctpd = new CommunityTimePhasedData();
    ctpd.addCommunity(getParent().getShortName());
    data.addTimePhasedData(ctpd);
    
    // Add Relationships
    Iterator iter = getAllRoles().iterator();
    while(iter.hasNext()) {
      String role = (String)iter.next();
      String[] supplies = (String[])getProperty(PROP_SUPPLIES).getValue();
      for(int i=0; i < supplies.length; i++) {
	RelationshipTimePhasedData rtpd = new RelationshipTimePhasedData();
	rtpd.setRole(role);
	rtpd.setItem(supplies[i].trim());
	rtpd.setType(supplies[i].substring(supplies[i].lastIndexOf(".")+1));
	rtpd.setCluster(supplies[i].trim());
	data.addTimePhasedData(rtpd);
      }
    }
//     RelationshipTimePhasedData rel = new RelationshipTimePhasedData();
//     String initializer = (String)getProperty(PROP_INITIALIZER).getValue();
//     rel.setRole("MetricsControlProvider");
//     rel.setItem(initializer);
//     rel.setType(initializer.substring(initializer.lastIndexOf(".")+1));
//     rel.setCluster(initializer);
//     data.addTimePhasedData(rel);

    return data;
  }

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

//     if(getFullName().toString().equals(getProperty(PROP_INITIALIZER).getValue())) {
//       ABCPlugIn init = new ABCPlugIn("MetricsInitializer", MetricsInitializerPlugIn_name);
//       addChild(init);
//       init.initProperties();
//       addPropertyAlias(init, getProperty(PROP_NUMBPROVIDERS));
//       addPropertyAlias(init, getProperty(PROP_SAMPLEINTERVAL));
//       addPropertyAlias(init, getProperty(PROP_STARTDELAY));
//       addPropertyAlias(init, getProperty(PROP_MAXNUMBSAMPLES));

//       plugin = new GenericComponentData();
//       plugin.setOwner(this);
//       plugin.setParent(data);
//       data.addChild(init.addComponentData(plugin));
//     }

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

    // Add Time Phased Data.
    data = addTimePhasedData(data);

    return data;
  }

  public ComponentData modifyComponentData(ComponentData data) {
    return data;
  }

}

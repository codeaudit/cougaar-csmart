/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society.scalability;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.core.cdata.PGPropData;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.PGPropMultiVal;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
/**
 * Generate a agent for testing
 **/
public class ScalabilityXAgent
  extends ConfigurableComponent
  implements Serializable, AgentComponent
{
  private static final long serialVersionUID = 3523529460773184838L;

  private static final Integer zero = new Integer(0);
  private static final Integer one = new Integer(1);

  public static final String PROP_INDEX = "Index";
  public static final String PROP_ROOTTASKS = "Root Tasks";
  public static final Object PROP_ROOTTASKS_DFLT = one;
  public static final URL PROP_ROOTTASKS_HELP =
    ScalabilityHelp.getURL("roottasks");
  public static final String PROP_ROOTTASKS_TT = "Number of root tasks";
  public static final String PROP_QUEUETASKS = "Queue Tasks";
  public static final Object PROP_QUEUETASKS_DFLT = zero;
  public static final URL PROP_QUEUETASKS_HELP =
    ScalabilityHelp.getURL("queuetasks");
  public static final String PROP_QUEUETASKS_TT =
    "Number of root tasks simultaneously pending allocation";
  public static final String PROP_PROVIDERCOUNT = "Provider Count";
  public static final String PROP_RESCINDLEVEL = "Rescind Level";
  public static final Object PROP_RESCINDLEVEL_DFLT = zero;
  public static final URL PROP_RESCINDLEVEL_HELP =
    ScalabilityHelp.getURL("rescindlevel");
  public static final String PROP_RESCINDLEVEL_TT =
    "Number of root tasks kept in logplan";
  public static final String PROP_SAMPLELEVEL = "Sample Interval";
  public static final Object PROP_SAMPLELEVEL_DFLT = zero;
  public static final URL PROP_SAMPLELEVEL_HELP =
    ScalabilityHelp.getURL("samplelevel");
  public static final String PROP_SAMPLELEVEL_TT =
    "Seconds between samples";
  public static final String PROP_TASKRATE = "Task Rate";
  public static final Object PROP_TASKRATE_DFLT = zero;
  public static final URL PROP_TASKRATE_HELP =
    ScalabilityHelp.getURL("taskrate");
  public static final String PROP_TASKRATE_TT =
    "Root tasks issued (per minute)";
  public static final String PROP_MSGBYTESPERTASK = "Msg Bytes Per Task";
  public static final Object PROP_MSGBYTESPERTASK_DFLT = zero;
  public static final URL PROP_MSGBYTESPERTASK_HELP =
    ScalabilityHelp.getURL("msgbytes");
  public static final String PROP_MSGBYTESPERTASK_TT =
    "Additional bytes to transmit with every task";
  public static final String PROP_EXPANSION = "Expansion";
  public static final Object PROP_EXPANSION_DFLT = one;
  public static final String PROP_SUBSCRIPTIONCOUNT = "Subscription Count";
  public static final Object PROP_SUBSCRIPTIONCOUNT_DFLT = zero;
  public static final String PROP_AGENTBYTESPERTASK = "Agent Bytes Per Task";
  public static final Object PROP_AGENTBYTESPERTASK_DFLT = zero;
  public static final String PROP_CPUPERTASK = "CPU Per Task";
  public static final Object PROP_CPUPERTASK_DFLT = zero;
  public static final String PROP_LEAFALLOCATORCOUNT = "Leaf Allocator Count";
  public static final Object PROP_LEAFALLOCATORCOUNT_DFLT = one;
  public static final String PROP_ORGALLOCATORCOUNT = "Org Allocator Count";
  public static final Object PROP_ORGALLOCATORCOUNT_DFLT = one;
  public static final String PROP_TOTALALLOCATORCOUNT = "Total Allocator Count";
  public static final Object PROP_TOTALALLOCATORCOUNT_DFLT = new Integer(2);
  public static final String PROP_ASSETCOUNT = "Asset Count";
  public static final Object PROP_ASSETCOUNT_DFLT = one;

  private String agentClassName = "org.cougaar.core.agent.ClusterImpl";
  private List supporting = new ArrayList();
  private ScalabilityXAgent superior;
  private int level;
  private int agentIndex;

  private static final String OrgRTDataPlugin_name =
    "org.cougaar.mlm.plugin.organization.OrgRTDataPlugin";
  private static final String OrgReportPlugin_name =
    "org.cougaar.mlm.plugin.organization.OrgReportPlugin";
  private static final String ScalabilityAllocatorPlugin_name =
    "org.cougaar.tools.scalability.scalability.ScalabilityAllocatorPlugin";
  private static final String ScalabilityDummyLoadPlugin_name =
    "org.cougaar.tools.scalability.scalability.ScalabilityDummyLoadPlugin";
  private static final String ScalabilityExpanderPlugin_name =
    "org.cougaar.tools.scalability.scalability.ScalabilityExpanderPlugin";
  private static final String ScalabilityInitializerPlugin_name =
    "org.cougaar.tools.scalability.scalability.ScalabilityInitializerPlugin";
  private static final String ScalabilityLDMPlugin_name =
    "org.cougaar.tools.scalability.scalability.ScalabilityLDMPlugin";
  private static final String ScalabilityLeafPlugin_name =
    "org.cougaar.tools.scalability.scalability.ScalabilityLeafPlugin";
  private static final String ScalabilityStatisticsPlugin_name =
    "org.cougaar.tools.scalability.scalability.ScalabilityStatisticsPlugin";
  private static final String ScalabilityConsumerPlugin_name =
    "org.cougaar.tools.scalability.scalability.ConsumerPlugin";
  private static final String PlanServerPlugin_name =
    "org.cougaar.lib.planserver.PlanServerPlugin";

  private static HashMap subscriptionCounts = new HashMap(13);

  static {
    subscriptionCounts.put(OrgRTDataPlugin_name,              new Integer(0));
    subscriptionCounts.put(OrgReportPlugin_name,              new Integer(5));
    subscriptionCounts.put(ScalabilityAllocatorPlugin_name,   new Integer(3));
    subscriptionCounts.put(ScalabilityDummyLoadPlugin_name,   new Integer(0));
    subscriptionCounts.put(ScalabilityExpanderPlugin_name,    new Integer(2));
    subscriptionCounts.put(ScalabilityInitializerPlugin_name, new Integer(6));
    subscriptionCounts.put(ScalabilityLDMPlugin_name,         new Integer(2));
    subscriptionCounts.put(ScalabilityLeafPlugin_name,        new Integer(2));
    subscriptionCounts.put(ScalabilityStatisticsPlugin_name,  new Integer(2));
    subscriptionCounts.put(ScalabilityConsumerPlugin_name,    new Integer(2));
    subscriptionCounts.put(PlanServerPlugin_name,             new Integer(0));
  }
  
  private Property propIndex;   // The property specifying our index at this level
  private Property propCPUPerTask;
  private Property propLeafAllocatorCount;
  private Property propOrgAllocatorCount;
  private Property propSubscriptionCount;
  private Property dummyLoadProperty = null;

  private List leafAllocators = new ArrayList();
  private List orgAllocators = new ArrayList();

  public ScalabilityXAgent(int level, int agentIndex, ScalabilityXAgent superior) {
    super("Agent-" + level + "-" + agentIndex);
    this.level = level;
    this.agentIndex = agentIndex;
    this.superior = superior;
  }

  public void initProperties() {
    propIndex = addProperty(PROP_INDEX, new Integer(agentIndex));
    propLeafAllocatorCount =
      addProperty(PROP_LEAFALLOCATORCOUNT, PROP_LEAFALLOCATORCOUNT_DFLT);
    propOrgAllocatorCount =
      addProperty(PROP_ORGALLOCATORCOUNT, PROP_ORGALLOCATORCOUNT_DFLT);
    propLeafAllocatorCount.addPropertyListener(new ConfigurableComponentPropertyAdapter() {
      public void propertyValueChanged(PropertyEvent e) {
        adjustAllocators();
      }
    });
    propOrgAllocatorCount.addPropertyListener(new ConfigurableComponentPropertyAdapter() {
      public void propertyValueChanged(PropertyEvent e) {
        adjustAllocators();
      }
    });
    propSubscriptionCount =
      addProperty(PROP_SUBSCRIPTIONCOUNT, PROP_SUBSCRIPTIONCOUNT_DFLT);
    propSubscriptionCount.addPropertyListener(new ConfigurableComponentPropertyAdapter() {
      public void propertyValueChanged(PropertyEvent e) {
        adjustDummyLoad();
      }
    });
    addStandardPlugins();
    addExpanderPlugin();
    addLdmPlugin();
    addConsumerPlugin();
    addDummyLoadPlugin();
    addStatisticsPlugin();
    if (level == 0) addRootPlugin();

    adjustAllocators();
    adjustDummyLoad();
  }

  public void setSupporting(List newSupporting) {
    supporting.clear();
    supporting.addAll(newSupporting);
  }

  public Iterator getSupporting() {
    return supporting.iterator();
  }

  public ScalabilityXAgent getSuperior() {
    return superior;
  }

  /**
   * Add a property to a plugin that is an alias for one of our properties.
   * @return the property we created for ourselves.
   * @param propName the (final element of the) name of our property.
   * @param component the component to get the property alias
   * @param value the value of the property.
   **/
  private Property addPropertyAlias(String propName, ScalabilityXPlugin c, Object value) {
    Property ourProp = addProperty(propName, value);
    Property childProp = addPropertyAlias(c, ourProp);
    return ourProp;
  }

  /**
   * Add a property to a plugin that is an alias for one of our properties.
   * @param c the component to get the property alias
   * @param prop one of our properties
   **/
  private Property addPropertyAlias(ScalabilityXPlugin c, Property prop) {
    Property childProp = c.addParameter(prop);
    setPropertyVisible(childProp, false);
    return childProp;
  }

  private void addInvisibleParameter(ScalabilityXPlugin c, int value, String label) {
    Property childProp = c.addParameter(value);
    setPropertyVisible(childProp, false);
    if (label != null) childProp.setLabel(label);
  }

  private Property addReadOnlyProperty(String propName, ConfigurableComponent c, Object value) {
    Property p = c.addProperty(propName, value);
    c.setPropertyVisible(p, false);
    return p;
  }

  private void addRootPlugin() {
    ScalabilityXPlugin plugin = new ScalabilityXPlugin("ScalabilityInitializer", ScalabilityInitializerPlugin_name);
    int childIndex = addChild(plugin);
    plugin.initProperties();
    addInvisibleParameter(plugin, level, "Level");
    addPropertyAlias(PROP_ROOTTASKS,       plugin, PROP_ROOTTASKS_DFLT)
      .setToolTip(PROP_ROOTTASKS_TT)
      .setHelp(PROP_ROOTTASKS_HELP);
    addPropertyAlias(PROP_QUEUETASKS,      plugin, PROP_QUEUETASKS_DFLT)
      .setToolTip(PROP_QUEUETASKS_TT)
      .setHelp(PROP_QUEUETASKS_HELP);
    setPropertyVisible(addPropertyAlias(PROP_PROVIDERCOUNT,   plugin, zero),
                       false);
    addPropertyAlias(PROP_RESCINDLEVEL,    plugin, PROP_RESCINDLEVEL_DFLT)
      .setToolTip(PROP_RESCINDLEVEL_TT)
      .setHelp(PROP_RESCINDLEVEL_HELP);
    addPropertyAlias(PROP_SAMPLELEVEL,     plugin, PROP_SAMPLELEVEL_DFLT)
      .setToolTip(PROP_SAMPLELEVEL_TT)
      .setHelp(PROP_SAMPLELEVEL_HELP);
    addPropertyAlias(PROP_TASKRATE,        plugin, PROP_TASKRATE_DFLT)
      .setToolTip(PROP_TASKRATE_TT)
      .setHelp(PROP_TASKRATE_HELP);
    addPropertyAlias(PROP_MSGBYTESPERTASK, plugin, PROP_MSGBYTESPERTASK_DFLT)
      .setToolTip(PROP_MSGBYTESPERTASK_TT)
      .setHelp(PROP_MSGBYTESPERTASK_HELP);
  }

  private void addStatisticsPlugin() {
    ScalabilityXPlugin plugin = new ScalabilityXPlugin("ScalabilityStatistics", ScalabilityStatisticsPlugin_name);
    addChild(plugin);
    plugin.initProperties();
  }

  private void addStandardPlugins() {
    ScalabilityXPlugin plugin;
    plugin = new ScalabilityXPlugin("OrgRTData", OrgRTDataPlugin_name);
    addChild(plugin);
    plugin.initProperties();
    plugin = new ScalabilityXPlugin("OrgReport", OrgReportPlugin_name);
    addChild(plugin);
    plugin.initProperties();
    plugin = new ScalabilityXPlugin("PlanServer", PlanServerPlugin_name);
    addChild(plugin);
    plugin.initProperties();
  }

  private void addExpanderPlugin() {
    ScalabilityXPlugin plugin =
      new ScalabilityXPlugin("ScalabilityExpander", ScalabilityExpanderPlugin_name);
    addChild(plugin);
    plugin.initProperties();
    addInvisibleParameter(plugin, level, "Level");
    addPropertyAlias(PROP_EXPANSION, plugin, PROP_EXPANSION_DFLT);
  }

  private void addDummyLoadPlugin() {
    ScalabilityXPlugin dummyLoadChild =
      new ScalabilityXPlugin("ScalabilityDummyLoad", ScalabilityDummyLoadPlugin_name);
    addChild(dummyLoadChild);
    dummyLoadChild.initProperties();
    dummyLoadProperty = dummyLoadChild.addParameter(0);
    setPropertyVisible(dummyLoadProperty, false);
  }

  private void adjustDummyLoad() {
    int subscriptionCount = ((Integer) propSubscriptionCount.getValue()).intValue();
    int intrinsicCount = getNumberOfSubscriptions();
    if (subscriptionCount > intrinsicCount) {
      Integer val = new Integer(subscriptionCount - intrinsicCount);
      dummyLoadProperty.setValue(val);
    }
  }

  private void adjustAllocators() {
    //    System.err.println(this.getFullName() + " in adjustAllocators");
    int newLeafAllocatorCount = ((Integer) propLeafAllocatorCount.getValue()).intValue();
    int newOrgAllocatorCount = ((Integer) propOrgAllocatorCount.getValue()).intValue();
    int totalAllocatorCount = newLeafAllocatorCount + newOrgAllocatorCount;
//      System.err.println("New Leafcount: " + newLeafAllocatorCount);
//      System.err.println("New Orgcount: " + newOrgAllocatorCount);
//      System.err.println("New Totalcount: " + totalAllocatorCount);
//      System.err.println("Had " + leafAllocators.size() + " leaf allocators");
    while (leafAllocators.size() > 0) {
      ConfigurableComponent child =
        (ConfigurableComponent) leafAllocators.get(leafAllocators.size() - 1);
      removeChild(child);
      leafAllocators.remove(child);
    }
    //System.err.println("Now have " + leafAllocators.size() + " leaf allocators");
    //System.err.println("Had " + orgAllocators.size() + " org allocators. Will trim excess.");
    //while (orgAllocators.size() > newOrgAllocatorCount) {
    while (orgAllocators.size() > 0) {
      //System.err.println("Removing an org allocator");
      ConfigurableComponent child =
        (ConfigurableComponent) orgAllocators.get(orgAllocators.size() - 1);
      removeChild(child);
      orgAllocators.remove(child);
    }
    //System.err.println("Now have " + orgAllocators.size() + " org allocators");
    while (leafAllocators.size() < newLeafAllocatorCount) {
      //System.err.println("Adding a leaf Plugin");
      ScalabilityXPlugin plugin =
        new ScalabilityXPlugin("ScalabilityLeaf", ScalabilityLeafPlugin_name);
      int index = leafAllocators.size();
      leafAllocators.add(plugin);
      addChild(plugin);
      plugin.initProperties();
      addInvisibleParameter(plugin, level + 1, "Level");
      addInvisibleParameter(plugin, index, "Index");
      addInvisibleParameter(plugin, totalAllocatorCount, "TotalAllocatorCount");
    }
    //System.err.println("Now have " + leafAllocators.size() + " leaf allocators");
    //System.err.println("About to add org allocators");
    while (orgAllocators.size() < newOrgAllocatorCount) {
      //System.err.println("Have " + orgAllocators.size() + " orgallocs. Adding an org allocator with args: " + (level + 1) + ", " + (orgAllocators.size() + leafAllocators.size()) + ", " + totalAllocatorCount);
      ScalabilityXPlugin plugin =
        new ScalabilityXPlugin("ScalabilityAllocator", ScalabilityAllocatorPlugin_name);
      int index = orgAllocators.size() + leafAllocators.size();
      orgAllocators.add(plugin);
      addChild(plugin);
      plugin.initProperties();
      addInvisibleParameter(plugin, level + 1, "Level");
      addInvisibleParameter(plugin, index, "Index");
      addInvisibleParameter(plugin, totalAllocatorCount, "TotalAllocatorCount");
    }
    //System.err.println("Now have " + orgAllocators.size() + " org allocators");
    //System.err.println(this.getFullName() + " done adjusting allocators");
  }

  private void addLdmPlugin() {
    ScalabilityXPlugin plugin =
      new ScalabilityXPlugin("ScalabilityLDM", ScalabilityLDMPlugin_name);
    addChild(plugin);
    plugin.initProperties();
    addPropertyAlias(PROP_ASSETCOUNT, plugin, PROP_ASSETCOUNT_DFLT);
  }

  private void addConsumerPlugin() {
    ScalabilityXPlugin plugin =
      new ScalabilityXPlugin("ScalabilityConsumer", ScalabilityConsumerPlugin_name);
    addChild(plugin);
    plugin.initProperties();
    Property prop;
    prop = addProperty(PROP_CPUPERTASK, PROP_CPUPERTASK_DFLT);
    addPropertyAlias(plugin, prop); // min
    addPropertyAlias(plugin, prop); // max
    prop = addProperty(PROP_AGENTBYTESPERTASK, PROP_AGENTBYTESPERTASK_DFLT); // min
    addPropertyAlias(plugin, prop); // min
    addPropertyAlias(plugin, prop); // max
  }

  public int getNumberOfPlugins() {
    return getChildCount();
  }

  public ScalabilityXPlugin getPlugin(int n) {
    return (ScalabilityXPlugin) getChild(n);
  }

  public int getNumberOfSubscriptions() {
    int sum = 0;
    for (int i = 0, n = getChildCount(); i < n; i++) {
      ScalabilityXPlugin plugin = (ScalabilityXPlugin) getChild(i);
      String pluginClassName = plugin.getPluginClassName();
      if (pluginClassName.equals(ScalabilityDummyLoadPlugin_name)) {
        sum += plugin.getIntParameter(0);
      } else {
        sum += ((Integer) subscriptionCounts.get(pluginClassName)).intValue();
      }
    }
    return sum;
  }

//   public String getConfigLine() {
//     return "cluster = " + getFullName();
//   }

//   public void writeIniFile(File configDir) throws IOException {
//     File iniFile = new File(configDir, getFullName() + ".ini");
//     PrintWriter writer = new PrintWriter(new FileWriter(iniFile));
//     adjustAllocators();
//     try {
//       writer.println("[ Cluster ]");
//       writer.println("class = " + agentClassName);
//       writer.println("uic = \"" + getFullName() + "\"");
//       writer.println("cloned = false");
//       writer.println();
//       writer.println("[ Plugins ]");
//       for (int i = 0, n = getChildCount(); i < n; i++) {
//         ScalabilityXPlugin plugin = (ScalabilityXPlugin) getChild(i);
//         writer.println(plugin.getConfigLine());
//       }
// //        writer.println("plugin = org.cougaar.lib.planserver.PlanServerPlugin");
//       writer.println();
//       writer.println("[ Policies ]");
//       writer.println();
//       writer.println("[ Permission ]");
//       writer.println();
//       writer.println("[ AuthorizedOperation ]");
//     }
//     finally {
//       writer.close();
//     }
//   }

//   public void writePrototypeIniFile(PrintWriter writer) {
//     try {
//       writer.println("[Prototype] CombatOrganization");
//       writer.println();
//       writer.println("[UniqueId] \"UTC/CombatOrg\"");
//       writer.println();
//       writer.println("[UIC] \"UIC/" + getFullName() + "\"");
//       writer.println();
//       writer.println("[Relationship]");
//       for (Iterator iter = supporting.iterator(); iter.hasNext(); ) {
//         writer.println("Supporting \"" + ((ScalabilityXAgent) iter.next()).getFullName() + "\" \"ScalabilityProvider\"");
//       }
//       if (superior != null) {
//         writer.println("Supporting \"" + superior.getFullName() + "\" \"ScalabilityControlProvider\"");
//         writer.println("Supporting \"" + superior.getFullName() + "\" \"ScalabilityStatisticsProvider\"");
//         writer.println("Superior  \"" + superior.getFullName() + "\" \"\"");
//       }
//       writer.println();
//       writer.println("[TypeIdentificationPG]");
//       writer.println("TypeIdentification String \"UTC/RTOrg\"");
//       writer.println("Nomenclature String \"" + getFullName() + "\"");
//       writer.println();
//       writer.println("[ClusterPG]");
//       writer.println("ClusterIdentifier ClusterIdentifier \"" + getFullName() + "\"");
//       writer.println();
//       writer.println("[OrganizationPG]");
//       writer.println("Roles Collection<Role> \"ScalabilityProvider, ScalabilityControlProvider, ScalabilityStatisticsProvider\"");
//     }
//     finally {
//       writer.close();
//     }
//   }


  private PropGroupData getTypeIdentificationPG() {

    PropGroupData pgData = new PropGroupData(PropGroupData.TYPE_IDENTIFICATION);
    PGPropData propData = new PGPropData();
    propData.setName("TypeIdentification");
    propData.setType("String");
    propData.setValue("UTC/RTOrg");
    pgData.addProperty(propData);

    propData = new PGPropData();
    propData.setName("Nomenclature");
    propData.setType("String");
    propData.setValue(getFullName().toString());
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

  private PropGroupData getOrgPG() {

    PropGroupData pgData = new PropGroupData(PropGroupData.ORGANIZATION);
    PGPropData propData = new PGPropData();
    propData.setName("Roles");
    propData.setType("Collection");
    propData.setSubType("Role");
    PGPropMultiVal values = new PGPropMultiVal();
    values.addValue("ScalabilityProvider");
    values.addValue("ScalabilityControlProvider");
    values.addValue("ScalabilityStatisticsProvider");
    propData.setValue(values);
    pgData.addProperty(propData);

    return pgData;
  }

  private ComponentData addAssetData(ComponentData data) {
    AgentAssetData assetData = new AgentAssetData((AgentComponentData)data);

    assetData.setType(AgentAssetData.ORG);
    assetData.setAssetClass(AgentAssetData.COMBAT_ASSETCLASS);

    assetData.setUniqueID("UTC/CombatOrg");
    assetData.setUIC("UIC/" + getFullName());

    // Add all Relationship data
    for (Iterator iter = supporting.iterator(); iter.hasNext(); ) {
      RelationshipData relData = new RelationshipData();
      relData.setType(RelationshipData.SUPPORTING);
      relData.setSupported(((ScalabilityXAgent) iter.next()).getFullName().toString());
      relData.setRole("ScalabilityProvider");
      assetData.addRelationship(relData);
    }
    if (superior != null) {
      RelationshipData relData = new RelationshipData();
      
      relData.setType(RelationshipData.SUPPORTING);
      relData.setSupported(superior.getFullName().toString());
      relData.setRole("ScalabilityControlProvider");
      assetData.addRelationship(relData);

      relData = new RelationshipData();
      relData.setType(RelationshipData.SUPPORTING);
      relData.setSupported(superior.getFullName().toString());
      relData.setRole("ScalabilityStatisticsProvider");
      assetData.addRelationship(relData);

      relData = new RelationshipData();
      relData.setType(RelationshipData.SUPERIOR);
      relData.setSupported(superior.getFullName().toString());
      relData.setRole("Subordinate");
      assetData.addRelationship(relData);

    }
    
    // Add all the PG's
    assetData.addPropertyGroup(getTypeIdentificationPG());
    assetData.addPropertyGroup(getClusterPG());
    assetData.addPropertyGroup(getOrgPG());

    data.addAgentAssetData(assetData);

    return data;
  }


  public ComponentData addComponentData(ComponentData data) {
    GenericComponentData plugin;

    // Add all plugins
    for (int i = 0, n = getChildCount(); i < n; i++) {
      ScalabilityXPlugin sxp = (ScalabilityXPlugin) getChild(i);
      plugin = new GenericComponentData();
      plugin.setOwner(this);
      plugin.setParent(data);
      plugin.setType(ComponentData.PLUGIN);
      data.addChild(sxp.addComponentData(plugin));      
    }

    data = addAssetData(data);

    return data;
  }


}

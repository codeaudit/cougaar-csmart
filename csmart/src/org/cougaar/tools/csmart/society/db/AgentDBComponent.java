/* 
 * <copyright>
 * Copyright 2001-2002 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).

 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.tools.csmart.society.db;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.cougaar.util.DBProperties;
import org.cougaar.util.DBConnectionPool;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.AssetComponent;
import org.cougaar.tools.csmart.society.ContainerBase;
import org.cougaar.tools.csmart.society.PluginBase;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

/**
 * A single Agent in a <code>SocietyDBComponent</code>, 
 * i.e. one that was created from the configuration Database.
 * @see org.cougaar.tools.csmart.core.db.CMT
 * @see SocietyDBComponent
 */
public class AgentDBComponent
  extends ModifiableConfigurableComponent 
  implements AgentComponent, Serializable 
{

  /** Agent Property Name **/
  public static final String PROP_AGENT_NAME = "Agent Name";

  /** Assembly ID Property Name **/
  public static final String PROP_ASSEMBLY_ID = "Assembly ID";

  /** Component ID Property Name **/
  public static final String PROP_COMPONENT_ID = "Component ID";
  
  /** Component Category Property Name **/
  public static final String PROP_COMPONENT_CATEGORY = "Component Category";

  private static final String QUERY_AGENT_DATA = "queryAgentData";
  private static final String QUERY_PLUGIN_NAME = "queryPluginNames";
  private static final String QUERY_PLUGIN_ARGS = "queryComponentArgs";
  private static final String QUERY_AGENT_RELATIONS = "queryAgentRelationships";
  private static final String QUERY_AGENT_ASSET_CLASS = "queryAgentAssetClass";

  private transient Logger log;

  // The tree will not display in the builder
  // if there are no properties so put it's name.
  private Property propName;
  private Property propComponentID;

  private String name;
  private List assemblyID;

  private transient DBProperties dbp;
  private Map substitutions = new HashMap();

  /**
   * Creates a new <code>AgentDBComponent</code> instance.
   *
   */
  public AgentDBComponent() {
    this("Name", new ArrayList());
  }

  /**
   * Creates a new <code>AgentDBComponent</code> instance.
   *
   * @param name Name of the new Component
   */
  public AgentDBComponent(String name) {
    super(name);
    this.name = name;
    this.assemblyID = null;
    createLogger();
  }

  /**
   * Creates a new <code>AgentDBComponent</code> instance.
   * This component is created from the database assemblies
   * that are passed in as a parameter.
   *
   * @param name Name of the new component
   * @param assemblyID List of Assembly ID's for the component
   */
  public AgentDBComponent(String name, List assemblyID) {
    super(name);
    this.name = name;
    this.assemblyID = assemblyID;
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Initialize all local properties.
   *
   */
  public void initProperties() {
    String componentID = name;
    String componentCategory = "agent";
    propName = addProperty(PROP_AGENT_NAME, name,
			   new ConfigurableComponentPropertyAdapter() {
			     public void PropertyValueChanged(PropertyEvent e) {
			     }
			   });

    propComponentID = addProperty(PROP_COMPONENT_ID, componentID,
			   new ConfigurableComponentPropertyAdapter() {
			     public void PropertyValueChanged(PropertyEvent e) {
			     }
			   });

    try {
      initDBProperties();
    } catch (IOException ioe) {
      if(log.isErrorEnabled()) {
        log.error("Exception", ioe);
      }
    }

    addPlugins();
    addAssetDataComponents();
  }

  private void initDBProperties() throws IOException {
    dbp = DBProperties.readQueryFile(DBUtils.QUERY_FILE);
  }

  private String queryOrgClass() {
    String orgClass = null;
    substitutions.put(":agent_name", name);
    try {
      Connection conn = DBUtils.getConnection();
      try {
	Statement stmt = conn.createStatement();	
	String query = DBUtils.getQuery(QUERY_AGENT_ASSET_CLASS, substitutions);
	ResultSet rs = stmt.executeQuery(query);
	while (rs.next()) {
	  orgClass = rs.getString(1);	  
	}
	rs.close();
	stmt.close();

      } finally {
	conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
      throw new RuntimeException("Error" + e);
    }
    return orgClass;
  }

  // Take the ComponentData for an Agent to which
  // we are adding some data.
//    private ComponentData addAssetData(ComponentData data) {
//      AgentAssetData assetData = new AgentAssetData((AgentComponentData)data);

//      assetData.setType(AgentAssetData.ORG);

//      String orgClass = queryOrgClass();
//      assetData.setAssetClass(orgClass);
    
//      // Get list of assemblies for use in query, ignoring CMT assemblies
//      String assemblyMatch = DBUtils.getListMatch(assemblyID, "CMT");

//      if (assemblyMatch != null) {
//        substitutions.put(":assemblyMatch", assemblyMatch);
//        substitutions.put(":agent_name", name);

//        // Add all Relationship data
//        try {
//          Connection conn = DBUtils.getConnection();
//          try {
//            Statement stmt = conn.createStatement();	
//            String query = DBUtils.getQuery(QUERY_AGENT_RELATIONS, substitutions);
	
//            ResultSet rs = stmt.executeQuery(query);
//            while(rs.next()) {
//              RelationshipData rd = new RelationshipData();
//              String supported = rs.getString(1);
//              String role = rs.getString(2);
//              Timestamp startDate = rs.getTimestamp(3);
//              Timestamp endDate = rs.getTimestamp(4);
//  	    // what about rd.setType()? Probably to Supporting for most things?
//  	    // except for some to Superior?
//              rd.setSupported(supported);
//              rd.setRole(role);
//              if (startDate != null) {
//                rd.setStartTime(startDate.getTime());
//              }
//              if (endDate != null) rd.setEndTime(endDate.getTime());
//              assetData.addRelationship(rd);
//            }
//            rs.close();
//            stmt.close();

//          } finally {
//            conn.close();
//          }
//        } catch (Exception e) {
//          if(log.isErrorEnabled()) {
//            log.error("Exception", e);
//          }
//          throw new RuntimeException("Error" + e);
//        }
//      }

//      // FIXME: Add in other property groups!!
    
//      // Note: This really does a SET, so it correctly
//      // replaces the old data, if any, with the new
//      data.addAgentAssetData(assetData);

//      return data;
//    }

  /**
   * Adds all relevent <code>ComponentData</code> for this agent.
   * This method will not perform any modifications, only additions.
   *
   * @see ComponentData
   * @param data Pointer to the global <code>ComponentData</code> object.
   * @return a <code>ComponentData</code> value
   */
  public ComponentData addComponentData(ComponentData data) {
    // The incoming data is an agent
    String name = data.getName();
    int dotPos = name.lastIndexOf('.');
    if (dotPos >= 0) {
      name = name.substring(dotPos + 1);
    }

    String assemblyMatch = DBUtils.getListMatch(assemblyID, "CMT");
    if (assemblyMatch != null) {
      substitutions.put(":assemblyMatch", assemblyMatch);

      substitutions.put(":agent_name", name);

      data.setAlibID((String)propComponentID.getValue());
      // could do data.setChildren(new ComponentData[0]);
      // this ensures all the stuff I add is new, which
      // it should be

      // Get Plugin Names
      try {
        Connection conn = DBUtils.getConnection();
        try {
          Statement stmt = conn.createStatement();	
          String query = dbp.getQuery(QUERY_PLUGIN_NAME, substitutions);
	  
          ResultSet rs = stmt.executeQuery(query);
          while(rs.next()) {
            GenericComponentData plugin = new GenericComponentData();
            String pluginClassName = rs.getString(1);
            String pluginName = rs.getString(4);
            plugin.setType(ComponentData.PLUGIN);
            plugin.setClassName(pluginClassName);
            plugin.setParent(data);
            plugin.setOwner(this);
            plugin.setName(pluginName);	  
            String alibId =rs.getString(2);
            plugin.setAlibID(alibId);
            substitutions.put(":comp_alib_id", alibId);
            substitutions.put(":comp_id", rs.getString(3));
            Statement stmt2 = conn.createStatement();
            String query2 = dbp.getQuery(QUERY_PLUGIN_ARGS, substitutions);
            ResultSet rs2 = stmt2.executeQuery(query2);
            while (rs2.next()) {
              String arg = rs2.getString(1);
              plugin.addParameter(arg);
            }
            rs2.close();
            stmt2.close();

	    // This blindly adds the plugin.
	    // In this case, this is OK, cause the CMTAgents
	    // Only fill in stuff for CMT assembly agents,
	    // and are the first ones to be run
	    // In general however, this should do:
	    //if (data.getChildIndex(plugin) < 0)
	    // then add
	    // else, data.setChild(index, plugin)
//             if(log.isDebugEnabled()) {
//               log.debug("Add Plugin: " + plugin.getName());
//             }
            data.addChild(plugin);
          } // end of loop over plugins to add
          rs.close();
          stmt.close();

        } finally {
          conn.close();
        }
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("Exception", e);
        }
        throw new RuntimeException("Error" + e);
      }
    }
    //    data = addAssetData(data);

    // Process AssetData
    Iterator iter = 
      ((Collection)getDescendentsOfClass(AssetComponent.class)).iterator();
    while(iter.hasNext()) {
      AssetComponent asset = (AssetComponent)iter.next();
      asset.addComponentData(data);
    }

    return data;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    initDBProperties();
    createLogger();
  }

  private void addPlugins() {
    ContainerBase container = new ContainerBase("Plugins");
    container.initProperties();
    addChild(container);

    String assemblyMatch = DBUtils.getListMatch(assemblyID, "CMT");
    if (assemblyMatch != null) {
      substitutions.put(":assemblyMatch", assemblyMatch);
      substitutions.put(":agent_name", name);
    }

    // Get Plugin Names, class names, and parameters
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Statement stmt = conn.createStatement();	
        String query = dbp.getQuery(QUERY_PLUGIN_NAME, substitutions);
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          String pluginClassName = rs.getString(1);
          String pluginName = rs.getString(4);
          PluginBase plugin = new PluginBase(pluginName, pluginClassName);
          plugin.initProperties();
          String alibId =rs.getString(2);
          // does this need to be preserved in the plugin?
          // plugin.setAlibID(alibId);
          substitutions.put(":comp_alib_id", alibId);
          substitutions.put(":comp_id", rs.getString(3));
          Statement stmt2 = conn.createStatement();
          String query2 = dbp.getQuery(QUERY_PLUGIN_ARGS, substitutions);
          ResultSet rs2 = stmt2.executeQuery(query2);
          while (rs2.next()) {
            String arg = rs2.getString(1);
            plugin.addParameter(arg);
          }
          rs2.close();
          stmt2.close();
          container.addChild(plugin);
        } // end of loop over plugins to add
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
      throw new RuntimeException("Error" + e);
    }
  }

  private void addAssetDataComponents() {
    BaseComponent asset = 
      (BaseComponent)new AssetDBComponent(name, assemblyID);
    asset.initProperties();
    addChild(asset);
  }


  /**
   * Tests equality for agents.
   * Agents are equal if they have the same short name.
   * @param o Object to test for equality.
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

}
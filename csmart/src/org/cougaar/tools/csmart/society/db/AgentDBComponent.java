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
import org.cougaar.util.log.Logger;

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
import org.cougaar.tools.csmart.society.ComponentBase;
import org.cougaar.tools.csmart.society.BinderBase;
import org.cougaar.tools.csmart.society.AgentBase;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * A single Agent in a <code>SocietyDBComponent</code>, 
 * i.e. one that was created from the configuration Database.
 * @see org.cougaar.tools.csmart.core.db.CMT
 * @see SocietyDBComponent
 */
public class AgentDBComponent
  extends AgentBase 
  implements AgentComponent, Serializable 
{

  /** Agent Property Name **/
  public static final String PROP_AGENT_NAME = "Agent Name";

  /** Assembly ID Property Name **/
  public static final String PROP_ASSEMBLY_ID = "Assembly ID";

//   /** Component ID Property Name **/
//   public static final String PROP_COMPONENT_ID = "Component ID";
  
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
//   private Property propComponentID;

  private String name;
  private String assemblyID;

  private transient DBProperties dbp;
  private Map substitutions = new HashMap();

  /**
   * Creates a new <code>AgentDBComponent</code> instance.
   *
   */
  public AgentDBComponent() {
    this("Name", null);
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
   * This component is created from the database assembly
   * that is passed in as a parameter.
   *
   * @param name Name of the new component
   * @param assemblyID String Assembly ID for the component
   */
  public AgentDBComponent(String name, String assemblyID) {
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
    super.initProperties();
//     String componentID = name;
//     String componentCategory = "agent";
    propName = addProperty(PROP_AGENT_NAME, name,
			   new ConfigurableComponentPropertyAdapter() {
			     public void PropertyValueChanged(PropertyEvent e) {
			     }
			   });

//     propComponentID = addProperty(PROP_COMPONENT_ID, componentID,
// 			   new ConfigurableComponentPropertyAdapter() {
// 			     public void PropertyValueChanged(PropertyEvent e) {
// 			     }
// 			   });

    try {
      initDBProperties();
    } catch (IOException ioe) {
      if(log.isErrorEnabled()) {
        log.error("Exception", ioe);
      }
    }

    // FIXME: What about the Agent class?

    addBinders();
    addPlugins();
    addComponents();
    
    if(DBUtils.agentHasAssetData(name, assemblyID)) {
      addAssetData();
    }
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

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    initDBProperties();
    createLogger();
  }

  protected void addPlugins() {
    ContainerBase container = new ContainerBase("Plugins");
    container.initProperties();
    addChild(container);

    // Grab all non-CMT assemblies. Why?
    //    String assemblyMatch = DBUtils.getListMatch(assemblyID, "CMT");
    String assemblyMatch = DBUtils.getListMatch(assemblyID);
    if (assemblyMatch != null) {
      substitutions.put(":assemblyMatch", assemblyMatch);
      substitutions.put(":agent_name", name);
    }

    // Get Plugin Names, class names, and parameters
    try {
      Connection conn = DBUtils.getConnection();
      String query = "";
      substitutions.put(":comp_type:", "= '" + ComponentData.PLUGIN + "'");
      try {
        Statement stmt = conn.createStatement();	
        query = dbp.getQuery(QUERY_PLUGIN_NAME, substitutions);
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          String pluginClassName = rs.getString(1);
          String pluginName = rs.getString(4);
          String priority = rs.getString(6).intern();
          PluginBase plugin = new PluginBase(pluginName, pluginClassName, priority);
          plugin.initProperties();
          String alibId = rs.getString(2);
          String libId = rs.getString(3);
          plugin.setAlibID(alibId);
          plugin.setLibID(libId);
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

  protected void addBinders() {
    ContainerBase container = new ContainerBase("Binders");
    container.initProperties();
    addChild(container);

    // Grab all non-CMT assemblies. Why?
    //    String assemblyMatch = DBUtils.getListMatch(assemblyID, "CMT");
    String assemblyMatch = DBUtils.getListMatch(assemblyID);
    if (assemblyMatch != null) {
      substitutions.put(":assemblyMatch", assemblyMatch);
      substitutions.put(":agent_name", name);
    }

    // Get Binder Names, class names, and parameters
    try {
      Connection conn = DBUtils.getConnection();
      String query = "";
      substitutions.put(":comp_type:", "= '" + ComponentData.AGENTBINDER + "'");
      try {
        Statement stmt = conn.createStatement();	
        query = dbp.getQuery(QUERY_PLUGIN_NAME, substitutions);
//         if(log.isDebugEnabled()) {
//           log.debug("Query: " + query);
//         }

        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          String binderClassName = rs.getString(1);
          String binderName = rs.getString(4);
          String priority = rs.getString(6).intern();
          BinderBase binder = new BinderBase(binderName, binderClassName, priority);
          binder.initProperties();
	  //binder.setBinderType(ComponentData.AGENTBINDER);
          String alibId = rs.getString(2);
          String libId = rs.getString(3);
          binder.setAlibID(alibId);
          binder.setLibID(libId);

          substitutions.put(":comp_alib_id", alibId);
          substitutions.put(":comp_id", rs.getString(3));
          Statement stmt2 = conn.createStatement();
          String query2 = dbp.getQuery(QUERY_PLUGIN_ARGS, substitutions);
          ResultSet rs2 = stmt2.executeQuery(query2);
          while (rs2.next()) {
            String arg = rs2.getString(1);
            binder.addParameter(arg);
          }
          rs2.close();
          stmt2.close();
          container.addChild(binder);
        } // end of loop over binders to add
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

  protected void addComponents() {
    ContainerBase container = new ContainerBase("Other Components");
    container.initProperties();
    addChild(container);

    // Grab all non-CMT assemblies. Why?
    //    String assemblyMatch = DBUtils.getListMatch(assemblyID, "CMT");
    String assemblyMatch = DBUtils.getListMatch(assemblyID);
    if (assemblyMatch != null) {
      substitutions.put(":assemblyMatch", assemblyMatch);
      substitutions.put(":agent_name", name);
    }

    // Get Binder Names, class names, and parameters
    try {
      Connection conn = DBUtils.getConnection();
      String query = "";

      // FIXME!!!
      List types = new ArrayList();
      types.add(ComponentData.SOCIETY);
      types.add(ComponentData.NODE);
      types.add(ComponentData.HOST);
      types.add(ComponentData.NODEBINDER);
      types.add(ComponentData.NODEAGENT);
      types.add(ComponentData.AGENTBINDER);
      types.add(ComponentData.AGENT);
      types.add(ComponentData.PLUGIN);
      String ctypematch = "NOT " + DBUtils.getListMatch(types);
      substitutions.put(":comp_type:", ctypematch);

      try {
        Statement stmt = conn.createStatement();	
        query = dbp.getQuery(QUERY_PLUGIN_NAME, substitutions);
//         if(log.isDebugEnabled()) {
//           log.debug("Query: " + query);
//         }

        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          String binderClassName = rs.getString(1);
          String binderName = rs.getString(4);
          String priority = rs.getString(6).intern();
          ComponentBase binder = new ComponentBase(binderName, binderClassName, priority);
          binder.initProperties();
	  binder.setComponentType(rs.getString(5));
          String alibId = rs.getString(2);
          String libId = rs.getString(3);
          binder.setAlibID(alibId);
          binder.setLibID(libId);

          substitutions.put(":comp_alib_id", alibId);
          substitutions.put(":comp_id", rs.getString(3));
          Statement stmt2 = conn.createStatement();
          String query2 = dbp.getQuery(QUERY_PLUGIN_ARGS, substitutions);
          ResultSet rs2 = stmt2.executeQuery(query2);
          while (rs2.next()) {
            String arg = rs2.getString(1);
            binder.addParameter(arg);
          }
          rs2.close();
          stmt2.close();
          container.addChild(binder);
        } // end of loop over binders to add
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

  protected void addAssetData() {
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

/* 
 * <copyright>
 * Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.societies.cmt;

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

import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.societies.database.DBUtils;

public class CMTAgent 
  extends ConfigurableComponent 
  implements AgentComponent, Serializable 
{

  public static final String PROP_AGENT_NAME = "Agent Name";
  public static final String PROP_ASSEMBLY_ID = "Assembly ID";
  public static final String PROP_COMPONENT_ID = "Component ID";
  public static final String PROP_COMPONENT_CATEGORY = "Component Category";
  private static final String QUERY_AGENT_DATA = "queryAgentData";
  private static final String QUERY_PLUGIN_NAME = "queryPluginNames";
  private static final String QUERY_PLUGIN_ARGS = "queryPluginArgs";
  private static final String QUERY_AGENT_RELATIONS = "queryAgentRelationships";
  private static final String QUERY_AGENT_ASSET_CLASS = "queryAgentAssetClass";

  // The tree will not display in the builder
  // if there are no properties so put it's name.
  private Property propName;
  private Property propComponentID;
  private Property propAssemblyID;
  private Property propCategory;

  private String name;
  private List assemblyID;

  private transient DBProperties dbp;
  private Map substitutions = new HashMap();

  public CMTAgent() {
    this("Name", new ArrayList());
  }

  public CMTAgent(String name, List assemblyID) {
    super(name);
    this.name = name;
    this.assemblyID = assemblyID;
  }
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

//     propCategory = addProperty(PROP_COMPONENT_CATEGORY, componentCategory,
// 			   new ConfigurableComponentPropertyAdapter() {
// 			     public void PropertyValueChanged(PropertyEvent e) {
// 			     }
// 			   });

    try {
      initDBProperties();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private void initDBProperties() throws IOException {
    dbp = DBProperties.readQueryFile(DBUtils.DATABASE, DBUtils.QUERY_FILE);
    dbp.setDebug(true);
  }

  private static String getNonNullString(ResultSet rs, int ix, String query)
    throws SQLException
  {
    String result = rs.getString(ix);
    if (result == null)
      throw new RuntimeException("Null in DB ix=" + ix + " query=" + query);
    return result;
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
      e.printStackTrace();
      throw new RuntimeException("Error" + e);
    }
    return orgClass;
  }

  private ComponentData addAssetData(ComponentData data) {
    AgentAssetData assetData = new AgentAssetData((AgentComponentData)data);

    assetData.setType(AgentAssetData.ORG);

    String orgClass = queryOrgClass();
    System.out.println("Org Class: " + orgClass);
    assetData.setAssetClass(orgClass);
    StringBuffer assemblyMatch = new StringBuffer();
    assemblyMatch.append("in (");
    Iterator iter = assemblyID.iterator();
    boolean first = true;
    while (iter.hasNext()) {
      String val = (String)iter.next();
      if (first) {
	first = false;
      } else {
	assemblyMatch.append(", ");
      }
      assemblyMatch.append("'");
      assemblyMatch.append(val);
      assemblyMatch.append("'");
    }
    assemblyMatch.append(")");

    substitutions.put(":assemblyMatch", assemblyMatch.toString());
    substitutions.put(":agent_name", name);

    // Add all Relationship data
    try {
      Connection conn = DBUtils.getConnection();
      try {
	Statement stmt = conn.createStatement();	
	String query = DBUtils.getQuery(QUERY_AGENT_RELATIONS, substitutions);
	System.out.println(query);
	
	ResultSet rs = stmt.executeQuery(query);
	while(rs.next()) {
	  RelationshipData rd = new RelationshipData();
	  String supported = rs.getString(1);
	  String role = rs.getString(2);
	  Timestamp startDate = rs.getTimestamp(3);
	  Timestamp endDate = rs.getTimestamp(4);

	  rd.setSupported(supported);
	  rd.setRole(role);
	  if (startDate != null) {
            rd.setStartTime(startDate.getTime());
            System.out.println("setting start time to " + startDate);
          }
          if (endDate != null) rd.setEndTime(endDate.getTime());
	  assetData.addRelationship(rd);
	}
	rs.close();
	stmt.close();

      } finally {
	conn.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error" + e);
    }

    data.addAgentAssetData(assetData);

    return data;
  }


  public ComponentData addComponentData(ComponentData data) {

    String name = data.getName();
    int dotPos = name.lastIndexOf('.');
    if (dotPos >= 0) {
      name = name.substring(dotPos + 1);
    }

    StringBuffer assemblyMatch = new StringBuffer();
    assemblyMatch.append("in (");
    Iterator iter = assemblyID.iterator();
    boolean first = true;
    while (iter.hasNext()) {
      String val = (String)iter.next();
      if (first) {
	first = false;
      } else {
	assemblyMatch.append(", ");
      }
      assemblyMatch.append("'");
      assemblyMatch.append(val);
      assemblyMatch.append("'");
    }
    assemblyMatch.append(")");

    substitutions.put(":assemblyMatch", assemblyMatch.toString());

    substitutions.put(":agent_name", name);

    data.setAlibID((String)propComponentID.getValue());

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
	  plugin.setType(ComponentData.PLUGIN);
          plugin.setClassName(pluginClassName);
	  plugin.setParent(data);
	  plugin.setOwner(this);
	  plugin.setName(pluginClassName);	  
	  String alibId =rs.getString(2);
	  plugin.setAlibID(alibId);
	  substitutions.put(":comp_alib_id", alibId);
	  substitutions.put(":comp_id", rs.getString(3));
	  Statement stmt2 = conn.createStatement();
	  String query2 = dbp.getQuery(QUERY_PLUGIN_ARGS, substitutions);
	  ResultSet rs2 = stmt.executeQuery(query2);
	  while (rs2.next()) {
	    String arg = rs2.getString(1);
	    plugin.addParameter(arg);
	  }
	  rs2.close();
	  stmt2.close();
	  data.addChild(plugin);
	}
	rs.close();
	stmt.close();

      } finally {
	conn.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error" + e);
    }

    System.out.println("Adding AssetData");    
    data = addAssetData(data);

    return data;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    initDBProperties();
  }
  
} // end of CMTAgent.java

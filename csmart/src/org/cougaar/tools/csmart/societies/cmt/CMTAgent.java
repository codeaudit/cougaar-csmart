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
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

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

  // The tree will not display in the builder
  // if there are no properties so put it's name.
  private Property propName;
  private Property propComponentID;
  private Property propAssemblyID;
  private Property propCategory;

  private String name;
  private String assemblyID;

  private String database;
  private String username;
  private String password;
  private DBProperties dbp;
  private Map substitutions = new HashMap();

  public CMTAgent() {
    this("Name", "AssemblyID");
  }

  public CMTAgent(String name, String assemblyID) {
    super(name);
    this.name = name;
    this.assemblyID = assemblyID;
  }
  public void initProperties() {
    String componentID;
    String componentCategory;

    try {
      dbp = DBProperties.readQueryFile(DBUtils.DATABASE, DBUtils.QUERY_FILE);
      database = dbp.getProperty("database");
      username = dbp.getProperty("username");
      password = dbp.getProperty("password");
      substitutions.put(":assemblyMatch", assemblyID);
      substitutions.put(":component_name", name);
    } catch(IOException e) {
      throw new RuntimeException("Error: " + e);
    }

    try {
      Connection conn = DBConnectionPool.getConnection(database, username, password);
      try {
	Statement stmt = conn.createStatement();
	String query = dbp.getQuery(QUERY_AGENT_DATA, substitutions);
	ResultSet rs = stmt.executeQuery(query);
	rs.next();
	componentID = getNonNullString(rs, 1, query);
	componentCategory = getNonNullString(rs, 2, query);
      } finally {
	conn.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error" + e);
    }

    propName = addProperty(PROP_AGENT_NAME, name,
			   new ConfigurableComponentPropertyAdapter() {
			     public void PropertyValueChanged(PropertyEvent e) {
			     }
			   });

    propAssemblyID = addProperty(PROP_ASSEMBLY_ID, assemblyID,
			   new ConfigurableComponentPropertyAdapter() {
			     public void PropertyValueChanged(PropertyEvent e) {
			     }
			   });

    propComponentID = addProperty(PROP_COMPONENT_ID, componentID,
			   new ConfigurableComponentPropertyAdapter() {
			     public void PropertyValueChanged(PropertyEvent e) {
			     }
			   });

    propCategory = addProperty(PROP_COMPONENT_CATEGORY, componentCategory,
			   new ConfigurableComponentPropertyAdapter() {
			     public void PropertyValueChanged(PropertyEvent e) {
			     }
			   });

  }

  private static String getNonNullString(ResultSet rs, int ix, String query)
    throws SQLException
  {
    String result = rs.getString(ix);
    if (result == null)
      throw new RuntimeException("Null in DB ix=" + ix + " query=" + query);
    return result;
  }

  public ComponentData addComponentData(ComponentData data) {
    return data;
  }
  
} // end of CMTAgent.java

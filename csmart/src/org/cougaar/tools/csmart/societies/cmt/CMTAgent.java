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
    String componentID = new String("");
    String componentCategory = new String("");
    StringBuffer assemblyMatch = null;

    try {
      initDBProperties();
      assemblyMatch = new StringBuffer();
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
      substitutions.put(":component_name", name);
    } catch(IOException e) {
      throw new RuntimeException("Error: " + e);
    }

    try {
      Connection conn = DBUtils.getConnection();
      try {
	Statement stmt = conn.createStatement();
	String query = dbp.getQuery(QUERY_AGENT_DATA, substitutions);
	ResultSet rs = stmt.executeQuery(query);
	while(rs.next()) {
	  componentID = getNonNullString(rs, 1, query);
	  componentCategory = getNonNullString(rs, 2, query);
	}

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

  public ComponentData addComponentData(ComponentData data) {

    //    System.out.println("Agent: " + data.getName());
    StringBuffer assemblyMatch = null;
    
    String name = data.getName();
    int dotPos = name.lastIndexOf('.');
    if (dotPos >= 0) {
      name = name.substring(dotPos + 1);
    }
    substitutions.put(":agent_name", name);

    data.setAlibID((String)propComponentID.getValue());

    // Get Plugin Names
    try {
      Connection conn = DBUtils.getConnection();
      try {
	Statement stmt = conn.createStatement();	
        dbp.setDebug(true);
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
	  plugin.setAlibID(rs.getString(2));
	  data.addChild(plugin);
	}

      } finally {
	conn.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error" + e);
    }
    return data;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    initDBProperties();
  }
  
} // end of CMTAgent.java

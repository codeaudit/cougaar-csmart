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

import java.io.FileFilter;
import java.io.Serializable;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import org.cougaar.util.DBProperties;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.tools.csmart.ui.component.AgentComponent;
import org.cougaar.tools.csmart.ui.component.ComponentData;
import org.cougaar.tools.csmart.ui.component.ModificationListener;
import org.cougaar.tools.csmart.ui.component.ModificationEvent;
import org.cougaar.tools.csmart.ui.component.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.ui.component.PropertyEvent;
import org.cougaar.tools.csmart.ui.component.PropertiesListener;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;
import org.cougaar.tools.csmart.ui.component.*;

import org.cougaar.tools.csmart.ui.viewer.CSMART;

import org.cougaar.tools.csmart.societies.database.DBUtils;

public class CMTSociety 
  extends ModifiableConfigurableComponent 
  implements PropertiesListener, Serializable, SocietyComponent, ModificationListener 
{
  private boolean isRunning = false;
  private boolean editable = true;
  private static final String DESCRIPTION_RESOURCE_NAME = "description.html";
  private static final String BACKUP_DESCRIPTION =
  "CMTSociety description not available";
  private static final String QUERY_AGENT_NAMES = "queryAgentNames";

  private Property testProp;

  private List assemblyID;
  private String database;
  private String username;
  private String password;
  transient private DBProperties dbp;
  private Map substitutions = new HashMap();

  // FIXME - are these right constructors?
  public CMTSociety() {
    this("CMT");
  }

  public CMTSociety(String name) {
    super(name);
    assemblyID = new ArrayList();
    assemblyID.add(name);
  }

  public CMTSociety(List ids) {
    super("Combo");
    assemblyID = ids;
  }

  public static CMTSociety loadCMTSociety(String assemblyID) {
    CMTSociety society = null;
    Map substitutions = new HashMap();
    boolean match = false;

    // load all the appropriate stuff from the db right here:
    // 1) Get a DB Connection
    // 2) Make sure the ASSEMBLY_ID given exists.
    // -- if not, return null
    // 3) Create a new CMT Society with an appropriate name - the ASSEMBLY_ID?
    // 4) Fill in all the other necessary data - see comments in the initProperties() method
    // -- basically, we want the componentdata stuff, or at least the list of Agents with names
    // creating the necessary AgentComponents

//     if(CSMART.inDBMode()) {
//       try {
// 	substitutions.put(":assembly_type", "CMT");
// 	Connection conn = DBUtils.getConnection();
// 	Statement stmt = conn.createStatement();
// 	String query = DBUtils.getQuery(DBUtils.ASSEMBLYID_QUERY, substitutions);
// 	ResultSet rs = stmt.executeQuery(query);
// 	while(rs.next()) {
// 	  String result = rs.getString(1);
// 	  if(result != null && result.equals(assemblyID)) {	    
// 	    match = true;
// 	    break;
// 	  }
// 	}
// 	rs.close();
// 	stmt.close();
// 	conn.close();
// 	if(match) {
// 	  society = new CMTSociety(assemblyID);
// 	}
//       } catch (SQLException se) {}   
//     }

    return society;
  }

  public void initProperties() {
    Map substitutions = new HashMap();
    StringBuffer assemblyMatch = null;

    if (assemblyID.size() != 0) {
      try {
	dbp = DBProperties.readQueryFile(DBUtils.DATABASE, DBUtils.QUERY_FILE);
	database = dbp.getProperty("database");
	username = dbp.getProperty("username");
	password = dbp.getProperty("password");
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
	substitutions.put(":insertion_point", "Node.AgentManager.Agent");
      } catch(IOException e) {
	throw new RuntimeException("Error: " + e);
      }

      try {
	Connection conn = DBConnectionPool.getConnection(database, username, password);
	try {
	  Statement stmt = conn.createStatement();
	  String query = dbp.getQuery(QUERY_AGENT_NAMES, substitutions);
	  ResultSet rs = stmt.executeQuery(query);
	  while (rs.next()) {
	    String agentName = getNonNullString(rs, 1, query);
	    CMTAgent agent = new CMTAgent(agentName, assemblyID);
	    agent.initProperties();
	    addChild(agent);
	  }
	} finally {
	  conn.close();
	}
      } catch (Exception e) {
	e.printStackTrace();
	throw new RuntimeException("Error" + e);
      }
    
    }    
  }

  private static String getNonNullString(ResultSet rs, int ix, String query)
    throws SQLException
  {
    String result = rs.getString(ix);
    if (result == null)
      throw new RuntimeException("Null in DB ix=" + ix + " query=" + query);
    return result;
  }

  public void modified(ModificationEvent e) {
    fireModification();
  }

  public void setName(String newName) {
    super.setName(newName);
    fireModification();
  }

  public String getSocietyName() {
    return getShortName();
  }

  public AgentComponent[] getAgents() {
    Collection agents = getDescendentsOfClass(CMTAgent.class);
    return (AgentComponent[]) agents.toArray(new AgentComponent[agents.size()]);
  }

  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  /**
   * Returns whether or not the society can be edited.
   * @return true if society can be edited and false otherwise
   */
  public boolean isEditable() {
    //    return !isRunning;
    return editable;
  }

  /**
   * Set whether or not the society can be edited.
   * @param editable true if society is editable and false otherwise
   */
  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  /**
   * Set by the experiment controller to indicate that the
   * society is running.
   * The society is running from the moment that any node
   * is successfully created 
   * until all nodes are terminated (aborted, self terminated, or
   * manually terminated).
   * @param flag indicating whether or not the society is running
   */
  public void setRunning(boolean isRunning) {
    this.isRunning = isRunning;
  }

  /**
   * Returns whether or not the society is running, 
   * i.e. can be dynamically monitored.
   * Running societies are not editable, but they can be copied,
   * and the copy can be edited.
   * @return true if society is running and false otherwise
   */
  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Return a file filter which can be used to fetch
   * the metrics files for this experiment.
   * @return file filter to get metrics files for this experiment
   */
  public FileFilter getResultFileFilter() {
    return null;
  }  

  /**
   * Return a file filter which can be used to delete
   * the files generated by this experiment.
   * @return file filter for cleanup
   */
  public FileFilter getCleanupFileFilter() {
    // TODO: return cleanup file filter
    return null;
  }

  /**
   * Returns whether the society is self terminating or must
   * be manually terminated.
   * Self terminating nodes cause a NODE_DESTROYED event
   * to be generated (see org.cougaar.tools.server.NodeEvent).
   * @return true if society is self terminating
   * @see org.cougaar.tools.server.NodeEvent
   */
  public boolean isSelfTerminating() {
    return false;
  }

  public ComponentData addComponentData(ComponentData data) {
    ComponentData[] children = data.getChildren();

    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
      if(child.getType() == ComponentData.AGENT) {

	Iterator iter = ((Collection)getDescendentsOfClass(CMTAgent.class)).iterator();

	while(iter.hasNext()) {
	  CMTAgent agent = (CMTAgent)iter.next();
	  if(child.getName().equals(agent.getShortName().toString())) {
	    child.setOwner(this);
	    agent.addComponentData(child);
	  }
	}		
      } else {
	// Process it's children.
	addComponentData(child);
      }      
    }

    return data;
  }

  public void propertyRemoved(PropertyEvent e) {
  }
  
  public void propertyAdded(PropertyEvent e) {
  }
} // end of CMTSociety.java

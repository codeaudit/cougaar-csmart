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
package org.cougaar.tools.csmart.society.cmt;

import java.io.FileFilter;
import java.io.Serializable;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.ModificationListener;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertiesListener;

import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.io.ObjectInputStream;

/**
 * A Society created from the CFW portion of the CSMART configuration
 * database.
 * @see org.cougaar.tools.csmart.core.db.CMT
 */
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

  private List assemblyID;
  private Map substitutions;
  private transient Logger log;

  public CMTSociety(String name, List assemblyID) {
    super(name);
    this.assemblyID = assemblyID;
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

  public void initProperties() {
    Map substitutions = new HashMap();
    if (assemblyID.size() > 0) {
      substitutions.put(":assemblyMatch", DBUtils.getListMatch(assemblyID));
      substitutions.put(":insertion_point", "Node.AgentManager.Agent");

      try {
	Connection conn = DBUtils.getConnection();
	try {
	  Statement stmt = conn.createStatement();
	  String query = DBUtils.getQuery(QUERY_AGENT_NAMES, substitutions);
	  ResultSet rs = stmt.executeQuery(query);
	  while (rs.next()) {
	    String agentName = DBUtils.getNonNullString(rs, 1, query);
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
//     if(log.isDebugEnabled()) {
//       log.debug("In CMTSociety addComponent");
//     }
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

  public List getAssemblyID() {
    return this.assemblyID;
  }

  public ModifiableComponent copy(String name) {
    List assemblyIDs = getAssemblyID();
    SocietyComponent sc = new CMTSociety(name, assemblyIDs);
    sc.initProperties();
    return sc;
  }

} // end of CMTSociety.java

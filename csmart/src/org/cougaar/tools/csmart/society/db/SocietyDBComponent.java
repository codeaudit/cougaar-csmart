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
package org.cougaar.tools.csmart.society.db;

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
import org.cougaar.tools.csmart.society.SocietyBase;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.ModificationListener;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
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
public class SocietyDBComponent
  extends SocietyBase
  implements PropertiesListener, Serializable, ModificationListener 
{
  private boolean isRunning = false;
  private boolean editable = true;
  private static final String DESCRIPTION_RESOURCE_NAME = "description.html";
  private static final String BACKUP_DESCRIPTION =
  "Society description not available";
  private static final String QUERY_AGENT_NAMES = "queryAgentNames";

  private Map substitutions;
  private transient Logger log;

  public SocietyDBComponent(String name, String assemblyID) {
    super(name);
    super.setAssemblyId(assemblyID);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void initProperties() {
    Map substitutions = new HashMap();
    if (assemblyId != null) {
      substitutions.put(":assemblyMatch", DBUtils.getListMatch(assemblyId));
      substitutions.put(":insertion_point", "Node.AgentManager.Agent");

      // FIXME:
      // It would be really nice to be able to handle Binders and other non-Agent
      // top-level things

      try {
	Connection conn = DBUtils.getConnection();
	try {
	  Statement stmt = conn.createStatement();
	  String query = DBUtils.getQuery(QUERY_AGENT_NAMES, substitutions);
	  ResultSet rs = stmt.executeQuery(query);
	  while (rs.next()) {
	    String agentName = DBUtils.getNonNullString(rs, 1, query);
	    AgentDBComponent agent = 
              new AgentDBComponent(agentName, assemblyId);
	    agent.initProperties();
	    addChild(agent);
	  }
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
  }

  public void modified(ModificationEvent e) {
    fireModification();
  }

  public void setName(String newName) {
    super.setName(newName);
    fireModification();
  }

  /**
   * Returns whether or not the society can be edited.
   * @return true if society can be edited and false otherwise
   */
  public boolean isEditable() {
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
   * Returns whether the society is self terminating or must
   * be manually terminated.
   * Self terminating nodes cause the app-server to send back
   * a "process-destroyed" message when the node terminates.
   * @return true if society is self terminating
   */
  public boolean isSelfTerminating() {
    return false;
  }

  public ModifiableComponent copy(String name) {
    if (log.isDebugEnabled()) {
      log.debug("Copying society " + this.getSocietyName() + " with assembly " + getAssemblyId() + " into new name " + name);
    }
    String assemblyID = getAssemblyId();
    ModifiableComponent sc = new SocietyDBComponent(name, assemblyID);
    sc.initProperties();
    ((SocietyDBComponent)sc).saveToDatabase();
    return sc;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

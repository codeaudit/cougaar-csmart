/**
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
 *  </copyright>
 */
package org.cougaar.tools.csmart.recipe.db;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cougaar.core.agent.ClusterImpl;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.recipe.ComplexRecipeBase;
import org.cougaar.tools.csmart.recipe.RecipeBase;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponentCreator;
import org.cougaar.tools.csmart.society.db.AgentDBComponent;
import org.cougaar.tools.csmart.ui.viewer.GUIUtils;


/**
 * ComplexRecipeBase.java
 *
 * Creates a ComplexRecipe from the database.
 * All property initialization is from the database assembly tables.
 *
 * Created: Thu Jun 20 13:52:13 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class ComplexRecipeDbComponent extends ComplexRecipeBase 
  implements Serializable {

  private static final String QUERY_AGENT_NAMES = "queryAgentNames";

  public ComplexRecipeDbComponent (String name){
    super(name);
  }
  
  public ComplexRecipeDbComponent (String name, String assemblyId){
    super(name);
    this.assemblyId = assemblyId;
  }

  /**
   * Initialize Recipe Component from the database using the specified assemblyId
   *
   */
  public void initProperties() {
    super.initProperties();
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
      // After reading the soc from the DB, it is not modified.
      //      modified = false;
      modified = false;
      fireModification(new ModificationEvent(this, RECIPE_SAVED));
    }    
  }

}// ComplexRecipeDbComponent

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
package org.cougaar.tools.csmart.recipe;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cougaar.core.agent.ClusterImpl;
import org.cougaar.util.DBProperties;

import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.ComponentBase;
import org.cougaar.tools.csmart.society.PluginBase;
import org.cougaar.tools.csmart.society.SocietyComponentCreator;
import org.cougaar.tools.csmart.society.cdata.AgentCDataComponent;
import org.cougaar.tools.csmart.society.cdata.BaseCDataComponent;
import org.cougaar.tools.csmart.society.db.AgentDBComponent;
import org.cougaar.tools.csmart.ui.viewer.GUIUtils;

/**
 * ComplexRecipeBase.java
 *
 * Base component for a ComplexRecipe.  A ComplexRecipe is a recipe that requires
 * storage in both the recipe tables and an assembly in the asb table.
 * The primary key is an assembly Id which is a hidden property of the recipe.
 * This key is stored as the primary argument in the recipe args table.
 * On load, the assembly Id is used to obtain all recipe related data from the
 * assembly tables.
 *
 * Created: Thu Jun 20 13:52:13 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */
public class ComplexRecipeBase extends RecipeBase 
  implements ComplexRecipeComponent, Serializable {

  protected static String DESCRIPTION_RESOURCE_NAME = "";

  /** Identifier String for the hidden Assembly Id Property **/
  public static final String ASSEMBLY_PROP = "Assembly Id";


  private static final String QUERY_AGENT_NAMES = "queryAgentNames";
  private static final String QUERY_PLUGIN_NAME = "queryPluginNames";
  private static final String QUERY_PLUGIN_ARGS = "queryComponentArgs";

  public static final String PROP_TARGET_COMPONENT_QUERY = "Target Component Selection Query";
  public static final String PROP_TARGET_COMPONENT_QUERY_DFLT = "recipeQuerySelectNothing";
  public static final String PROP_TARGET_COMPONENT_QUERY_DESC = 
    "The query name for selecting components to modify";

  protected String recipeId = null;
  protected String assemblyId = null;
  protected String oldAssemblyId = null;
  protected ComponentData cdata = null;
  private Property propAssemblyId;
  private String initName = null;
  private Map substitutions = new HashMap();
  private transient DBProperties dbp;


  public ComplexRecipeBase (String name){
    super(name);
    this.initName = name;
  }
  
  public ComplexRecipeBase (String name, String assemblyId){
    super(name);
    this.assemblyId = assemblyId;
    this.initName = name;
  }

  public ComplexRecipeBase (String name, String assemblyId, String recipeId){
    super(name);
    this.assemblyId = assemblyId;
    this.initName = name;
    this.recipeId = recipeId;
  }

  public ComplexRecipeBase (String name, String assemblyId, String recipeId, String initName){
    super(name);
    this.assemblyId = assemblyId;
    this.initName = initName;
    this.recipeId = recipeId;
  }

  public ComplexRecipeBase (ComponentData cdata, String assemblyId) {
    super(cdata.getName());
    this.cdata = cdata;
    this.assemblyId = assemblyId;
  }

  /**
   * Returns the description of this society
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);

  }

  /**
   * Save the recipe to the database.  This save performs the
   * saving of all data to the assembly tables.  It then calls
   * it's parents save to save the assembly Id and recipe name
   * to the recipe tables.
   *
   * @return a <code>boolean</code> value
   */
  public boolean saveToDatabase() {
    // First, save the new recipe assembly.
    saveInProgress = true;
    if(log.isInfoEnabled()) {
      log.info("saveToDatabase recipe (" + getRecipeName() + ") with asb: " + getAssemblyId() + " and old Assembly: " + oldAssemblyId);
    }

    // TODO:
    // Should I notice when I need to save and only save then?
    // Should I resist creating a new assembly ID, to avoid
    // breaking other experiments? Or always create one?
    
    String oldCMTAsbid = oldAssemblyId;
    String currAssID = getAssemblyId();
    if (currAssID == null || currAssID.trim().equals(""))
      currAssID = null;
    String name = getRecipeName();

    // And what is my current assemblyID?
    // How do I know if it is different?
    // FIXME: Maybe I need a new
    
    // But probably only want to pass it in if it was in fact a CMT assembly, no?
    // Or does it hurt to pass it in?
    PopulateDb pdb = null;
    boolean ret = true;
    try {
      // FIXME: Is there a non-gui conflict handler I should use?
      pdb = new PopulateDb(oldCMTAsbid, name, currAssID, GUIUtils.createSaveToDbConflictHandler(null), true);
      pdb.populateCSA(getComponentData());
      // Set the new CSA assembly ID on the society - get it from the PDB
      //      setAssemblyId(pdb.getCMTAssemblyId());
      assemblyId = pdb.getCMTAssemblyId();
      propAssemblyId.setValue(assemblyId);
      // What about fixAssemblies?
      // is it really populateCSA?
      pdb.close();
    } catch (Exception sqle) {
      if (log.isErrorEnabled()) {
	log.error("Error saving recipe to database: ", sqle);
      }
      ret = false;
    } finally {
      if (pdb != null) {
	try {
	  pdb.close();
	} catch (SQLException e) {}
      }
    }

    propAssemblyId.setVisible(true);
    ret &= super.saveToDatabase();
    propAssemblyId.setVisible(false);
    
    return ret;
  }

  public void addTargetQueryProperty() {
    Property p = addRecipeQueryProperty(PROP_TARGET_COMPONENT_QUERY, PROP_TARGET_COMPONENT_QUERY_DFLT);
    p.setToolTip(PROP_TARGET_COMPONENT_QUERY_DESC);
  }

  protected Property addRecipeQueryProperty(String name, String dflt) {
    Property prop = addProperty(new RecipeQueryProperty(this, name, dflt));
    prop.setPropertyClass(String.class);
    return prop;
  }


  /**
   * Initializes the hidden assembly id property, or CDATA
   *
   */
  public void initProperties() {
    if (cdata != null) {
      initFromCData();
      assemblyId = null;
    } else if (assemblyId != null) {
      initFromDatabase();
    }

    propAssemblyId = addProperty(ASSEMBLY_PROP, ((assemblyId != null) ? assemblyId : ""));
    propAssemblyId.setVisible(false);

    if (initName != getRecipeName()) {
      // A recipe copy - mark it as modified
      oldAssemblyId = assemblyId;
      assemblyId = "";
      propAssemblyId.setValue("");
      modified = true;
    } else {
      // After reading from the DB, it is not modified.
      modified = false;
      fireModification(new ModificationEvent(this, RECIPE_SAVED));
    }
  }

  // Initialize recipe details from the DB, from an RCP assembly
  private void initFromDatabase() {
    if(assemblyId != null) {
      try {
        dbp = DBProperties.readQueryFile(DBUtils.QUERY_FILE);
      } catch (IOException ioe) {
        if(log.isErrorEnabled()) {
          log.error("IO Exception reading Query File", ioe);
        }
      }

      initAgentsFromDb(); // Get the Agents
      initCompsFromDb(); // Get the other components
      initTargetsFromDb(); // Get the target query over-rides for the other comps
    }
  }

  // Get the Agent components to be added from the DB
  private void initAgentsFromDb() {
    Map substitutions = new HashMap();
    if (assemblyId != null) {
      substitutions.put(":assemblyMatch", DBUtils.getListMatch(assemblyId));
      substitutions.put(":insertion_point", "Node.AgentManager.Agent");

      try {
	Connection conn = DBUtils.getConnection();
	try {
	  Statement stmt = conn.createStatement();
	  String query = DBUtils.getQuery(QUERY_AGENT_NAMES, substitutions);
	  ResultSet rs = stmt.executeQuery(query);
	  while (rs.next()) {
	    String agentName = DBUtils.getNonNullString(rs, 1, query);
	    if (log.isDebugEnabled())
	      log.debug("initAgsFromDb adding " + agentName);
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
    }    
  }

  // Initialize non-Agent sub-components from DB
  private void initCompsFromDb() {
    String assemblyMatch = DBUtils.getListMatch(assemblyId);
    if (assemblyMatch != null) {
      substitutions.put(":assemblyMatch", assemblyMatch);
      substitutions.put(":agent_name", initName);
    }

    List types = new ArrayList();
    types.add(ComponentData.SOCIETY);
    types.add(ComponentData.NODE);
    types.add(ComponentData.HOST);
    types.add(ComponentData.NODEAGENT);
    types.add(ComponentData.AGENT);
    types.add(ComponentData.RECIPE);
    String ctypematch = "NOT " + DBUtils.getListMatch(types);
    substitutions.put(":comp_type:", ctypematch);
    
    // Get Plugin Names, class names, and parameters
    try {
      Connection conn = DBUtils.getConnection();
      String query = "";
      try {
        Statement stmt = conn.createStatement();	
        query = dbp.getQuery(QUERY_PLUGIN_NAME, substitutions);

	if (log.isDebugEnabled())
	  log.debug("initCompsFromDb doing query " + query);

        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          String pluginClassName = rs.getString(1);

	  if (log.isDebugEnabled())
	    log.debug("initFromDb loading " + pluginClassName);

          String pluginName = rs.getString(4);
	  String pluginType = rs.getString(5).trim().intern();
          String priority = rs.getString(6).intern();
          ComponentBase plugin = new ComponentBase(pluginName, pluginClassName, priority);
          plugin.initProperties();
          String alibId = rs.getString(2);
          String libId = rs.getString(3);
          plugin.setAlibID(alibId);
          plugin.setLibID(libId);
	  plugin.setComponentType(pluginType);
	  // set the type too
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
          addChild(plugin);
        } // end of loop over plugins to add
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("initCompsFromDb Exception: ", e);
      }
    }
  }

  // Initialize target query over-rides from DB
  private void initTargetsFromDb() {
    if(log.isDebugEnabled()) {
      log.debug("In initTargetsFromDb");
    }

    Map substitutions = new HashMap();
    if (recipeId != null) {
      substitutions.put(":recipe_id", recipeId);

      try {
	Connection conn = DBUtils.getConnection();
	try {
	  Statement stmt = conn.createStatement();
	  String query = DBUtils.getQuery("queryRecipeProperties", substitutions);
          if(log.isDebugEnabled()) {
            log.debug("initTargetsFromDb: Run query: " + query);
          }

	  ResultSet rs = stmt.executeQuery(query);
	  while (rs.next()) {
            String propName = rs.getString(1);
            if(propName.startsWith("$$CP=")) {
              int start = propName.indexOf("=");
              String component = propName.substring(start+1);

              // Find the Component and add the Property
              if(log.isDebugEnabled()) {
                log.debug("Looking for: " + component + " out of " + getChildCount() + " children");
              }
              for(int i=0; i < getChildCount(); i++) {
                ConfigurableComponent cc = (ConfigurableComponent)getChild(i);
//                 ComponentBase cc = (ComponentBase)getChild(i);
		if (log.isDebugEnabled())
		  log.debug("Considering child " + i + ": " + cc.getShortName());

                if (cc instanceof ComponentBase) {
                  String tag = ((ComponentBase)cc).getComponentClassName() + "-" + i;
                  if(log.isDebugEnabled()) {
                    log.debug("Compare with: " + tag);
                  }
                  if(tag.equals(component)) {
                    if(log.isDebugEnabled()) {
                      log.debug("Found Desired Parent: " + tag + " adding parameter");
                    }
                    cc.addProperty(PROP_TARGET_COMPONENT_QUERY, rs.getString(2));
		    break;
                  }
                } else {
		  // What kind of component is this?
		  if (cc instanceof AgentComponent) {
		    log.debug("not right - it's an Agent");
		  }
		}
              } // loop over children
            } else if (! propName.equals(ASSEMBLY_PROP)) {
	      // Non child component property
	      Property prop = getProperty(propName);
	      if (prop != null) {
		if (log.isDebugEnabled())
		  log.debug("setting local prop " + propName + " to " + rs.getString(2));
		prop.setValue(rs.getString(2));
	      } else {
		if (log.isDebugEnabled())
		  log.debug("Adding new local prop " + propName + " with val " + rs.getString(2));
		addProperty(propName, rs.getString(2));
	      }
	    }
	  } // end of loop over props from db
	  rs.close();
	  stmt.close();
	} finally {
	  conn.close();
	}
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("Exception getting target properties for recipe " + getRecipeName(), e);
        }
      }
    }    
  }

  private void initFromCData() {
    // create society properties from cdata
    // create agents from cdata
    ArrayList agentData = new ArrayList();
    ArrayList componentData = new ArrayList();
    ArrayList alldata = new ArrayList();
    if (cdata != null)
      alldata.add(cdata);
    
    // Find all the data.
    for (int i = 0; i < alldata.size(); i++) {
      ComponentData someData = (ComponentData)alldata.get(i);
      String type = someData.getType();
      if (type.equals(ComponentData.AGENT)) {
        agentData.add(someData);
      } else if (type.equals(ComponentData.SOCIETY) || type.equals(ComponentData.HOST) || type.equals(ComponentData.NODE) || type.equals(ComponentData.RECIPE)) {
	// It's not something I know - some sort of container
        ComponentData[] moreData = someData.getChildren();
        for (int j = 0; j < moreData.length; j++) 
          alldata.add(moreData[j]);
      } else {
	// Add it directly
        componentData.add(someData);
      }
    }
    
    // For each agent, create a component and add it as a child
    for (int i = 0; i < agentData.size(); i++) {
      AgentComponent agentComponent = 
        new AgentCDataComponent((ComponentData)agentData.get(i));
      agentComponent.initProperties();
      addChild(agentComponent);
    }
      
    // Add my immediate children
    for (int j=0; j < componentData.size(); j++) {
      BaseComponent baseComponent = 
        new BaseCDataComponent((ComponentData)componentData.get(j));
      baseComponent.initProperties();
      addChild(baseComponent);
    }
  }


  /**
   * Get the assembly id for this Recipe.
   * @return a <code>String</code> which is the assembly id for this Recipe
   */
  public String getAssemblyId() {
    return this.assemblyId;
  }

  protected ComponentData getComponentData() {
    ComponentData cd = new GenericComponentData();
    cd.setType(ComponentData.RECIPE);
    cd.setClassName(RECIPE_CLASS);
    cd.setName(getRecipeName());
    cd.setOwner(null);
    cd.setParent(null);

    AgentComponent[] agents = getAgents();
    for (int i = 0; i < agents.length; i++) {
      generateAgentComponentData(agents[i], cd, null);
    }

    // Now let all components add their data.
    addComponentData(cd);

    modifyComponentData(cd);

    return addComponentData(cd);
  }

  /**
   * Adds all the component data relevant to this recipe.
   *
   * @param data 
   * @return a <code>ComponentData</code> value
   */
  public ComponentData addComponentData(ComponentData data) {
    ComponentData[] children = data.getChildren();
    if (children == null)
      return data;
    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
      // for each child component data, if it's an agent's component data
      if (child.getType() == ComponentData.AGENT) {
        // get all my agent components
	Iterator iter = 
          ((Collection)getDescendentsOfClass(AgentComponent.class)).iterator();
	while(iter.hasNext()) {
	  AgentComponent agent = (AgentComponent)iter.next();
          // if the component data name matches the agent name
	  if (child.getName().equals(agent.getShortName().toString())) {
            // then set me as the owner of the component data
	    child.setOwner(this);
            // and add the component data
	    agent.addComponentData(child);
	  }
	}		
      } else {
	// FIXME!! Will we support other top-level components?
	// Process children of component data
	addComponentData(child);
      }      
    }
    return data;
  }

  private static final void generateAgentComponentData(AgentComponent agent, 
                             ComponentData parent, 
                             ConfigurableComponent owner) {

    AgentComponentData ac = new AgentComponentData();
    ac.setName(agent.getShortName());
    ac.setClassName(ClusterImpl.class.getName());
    ac.addParameter(agent.getShortName().toString()); // Agents have one parameter, the agent name
    ac.setOwner(owner);
    ac.setParent(parent);
    parent.addChild((ComponentData)ac);
  }

  /**
   * Copies a ComplexRecipe
   *
   * @param name 
   * @return a <code>ModifiableComponent</code> value
   */
  public ModifiableComponent copy(String name) {
    
    ComponentData cdata = getComponentData();
    cdata.setName(name);
    RecipeComponent component = new ComplexRecipeBase(cdata, null);
    component.initProperties();

    ((ComplexRecipeBase)component).modified = this.modified;
    ((ComplexRecipeBase)component).oldAssemblyId = getAssemblyId();

    return component;
  }

}// ComplexRecipeBase

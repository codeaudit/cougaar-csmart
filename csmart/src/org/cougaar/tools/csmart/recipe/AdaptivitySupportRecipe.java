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

import java.io.Serializable;
import java.util.ArrayList;
import java.sql.SQLException;
import java.util.Set;
import java.util.Iterator;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import java.net.URL;
import org.cougaar.tools.csmart.core.property.Property;


/**
 * AdaptivitySupportRecipe.java
 *
 *
 * Created: Wed May 22 10:22:54 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class AdaptivitySupportRecipe extends RecipeBase implements Serializable {

  private static final String DESCRIPTION_RESOURCE_NAME = 
    "adaptivity-support-recipe-description.html";
  private static final String BACKUP_DESCRIPTION = 
    "AdaptivitySupportRecipe provides a method for adding Adapitivity pieces to all agents in an experiment";

  private static final String PROP_TARGET_AGENT_QUERY = "Target Agent Selection Query";
  private static final String PROP_TARGET_AGENT_QUERY_DFLT = "recipeQueryAllAgents";
  private static final String PROP_TARGET_AGENT_QUERY_DESC = 
    "The query name for selecting agents to which to add adaptivity support.";

  private static final String PROP_VIEWER_SERVLET = "AEViewerServlet";
  private static final boolean PROP_VIEWER_SERVLET_DFLT = true;
  private static final String PROP_VIEWER_SERVLET_DESC = "Include the AEViewerServlet in the target agent(s)";

  private Property propViewerServlet;
  private Property propTargetAgentQuery;

  public AdaptivitySupportRecipe (){
    this("AdaptivitySupportRecipe");
  }

  public AdaptivitySupportRecipe(String name) {
    super(name);
  }

  public void initProperties() {
    
    propViewerServlet = addBooleanProperty(PROP_VIEWER_SERVLET, PROP_VIEWER_SERVLET_DFLT);
    propViewerServlet.setToolTip(PROP_VIEWER_SERVLET_DESC);

    propTargetAgentQuery =
      addRecipeQueryProperty(PROP_TARGET_AGENT_QUERY,
                             PROP_TARGET_AGENT_QUERY_DFLT);
    propTargetAgentQuery.setToolTip(PROP_TARGET_AGENT_QUERY_DESC);
  }

  private Property addRecipeQueryProperty(String name, String dflt) {
    Property prop = addProperty(new RecipeQueryProperty(this, name, dflt));
    prop.setPropertyClass(String.class);
    return prop;
  }

  /**
   * Gets the name of the html help file for this component.
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  public String getRecipeName() {
    return getShortName();
  }

  public ComponentData modifyComponentData(ComponentData data, PopulateDb pdb) {
    try {
      Set targets = pdb.executeQuery(propTargetAgentQuery.getValue().toString());
      modifyComponentData(data, pdb, targets);
    } catch (SQLException sqle) {
      if (log.isErrorEnabled()) {
	log.error("Cant run agent target query", sqle);
      }
    }
    return data;
  }

  private void modifyComponentData(ComponentData data, PopulateDb pdb, Set targets)
    throws SQLException
  {
    if (targets.contains(pdb.getComponentAlibId(data))) { 
      String agent = pdb.getComponentAlibId(data);

      if (log.isDebugEnabled()) {
	log.debug("Adding Adaptivity Support to " + agent);
      }

      Iterator iter = getPlugins();
      while(iter.hasNext()) {    
        String pluginName = (String)iter.next();
        GenericComponentData plugin = new GenericComponentData();
        plugin.setType(ComponentData.PLUGIN);
        plugin.setClassName(pluginName);
        plugin.setParent(data);
        plugin.setOwner(this);
        if( GenericComponentData.alreadyAdded(data, plugin)) {
          if(log.isDebugEnabled()) {
            log.debug("Not re-adding" + pluginName);
          }
        } else {
          plugin.setName(GenericComponentData.getSubComponentUniqueName(data, plugin));
          data.addChildDefaultLoc(plugin);
        }
      } 

      GenericComponentData plugin = new GenericComponentData();
      plugin.setType(ComponentData.PLUGIN);
      plugin.setClassName("org.cougaar.core.adaptivity.PlaybookManager");
      plugin.setOwner(this);
      plugin.setParent(data);
      plugin.addParameter(agent + "-plays.txt");
      if( GenericComponentData.alreadyAdded(data, plugin)) {
        if(log.isDebugEnabled()) {
          log.debug("Not re-adding PlaybookManager");
        }
      } else {
        plugin.setName(GenericComponentData.getSubComponentUniqueName(data, plugin));
          data.addChildDefaultLoc(plugin);
      }
      
      if(((Boolean)(propViewerServlet.getValue())).booleanValue()) {
        plugin = new GenericComponentData();
        plugin.setType(ComponentData.PLUGIN);
        plugin.setClassName("org.cougaar.core.adaptivity.BlackBoardServletComponent");
        plugin.setOwner(this);
        plugin.setParent(data);
        plugin.addParameter("org.cougaar.core.adaptivity.AEViewerServlet");
        plugin.addParameter("/aeviewer");
        if( GenericComponentData.alreadyAdded(data, plugin)) {
          if(log.isDebugEnabled()) {
            log.debug("Not re-adding AEViewerServlet");
          }
        } else {
          plugin.setName(GenericComponentData.getSubComponentUniqueName(data, plugin));
          data.addChildDefaultLoc(plugin);
        }
      }
    }    

    if (data.childCount() > 0) {
      // for each child, call this same method.
      ComponentData[] children = data.getChildren();
      for (int i = 0; i < children.length; i++) {
	// If the child is a plugins or AgentBinder, no need to look at it
	if (children[i].getType().equals(ComponentData.PLUGIN) || children[i].getType().equals(ComponentData.AGENTBINDER))
	  continue;
	modifyComponentData(children[i], pdb, targets);
      }
    }

  }

  private Iterator getPlugins() {
    ArrayList list = new ArrayList();
    list.add("org.cougaar.core.adaptivity.AdaptivityEngine");
    list.add("org.cougaar.core.adaptivity.ConditionServiceProvider");
    list.add("org.cougaar.core.adaptivity.OperationModeServiceProvider");
    list.add("org.cougaar.core.adaptivity.OperatingModePolicyManager");
    
    return list.iterator();
  }

}// AdaptivitySupportRecipe

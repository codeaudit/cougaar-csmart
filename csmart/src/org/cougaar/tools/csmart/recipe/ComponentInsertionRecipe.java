/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
 * </copyright>
 */
package org.cougaar.tools.csmart.recipe;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import java.util.ArrayList;

import org.cougaar.util.DBProperties;

import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.society.AgentComponent;

/**
 * This recipe performs component insertion into an experiment based
 * on queries and constants described by its parameters.
 *
 * This recipe performs three queries: The first query lists the
 * target components and should return COMPONENT_ALIB_ID values such
 * as appear in the V4_ALIB_COMPONENT.COMPONENT_ALIB_ID column. It is
 * not necessary for the ids to be distinct. The substitutions that
 * are available during this first query are: :assembly_match: has
 * something like "in ('assembly', 'assembly', ...)" and should be
 * used as "... AND R.ASSEMBLY_ID :assembly_match;" which would be
 * true if the assembly_id is in the set of assemblies of the current
 * experiment trial.
 *
 * The second query is used to obtain descriptions of the components
 * that are to be inserted. It is executed once for each target
 * component. A rows should be returned for each component to be
 * inserted into the target component. Each returned row should be a
 * triple consisting of: component name, component type, and class
 * name. The number of components inserted equals the number of rows
 * returned. The substitutions available consist of the following:
 * :component_name:
 * :component_lib_id:
 * :component_alib_id:
 * :component_category:
 * :component_class:
 * :insertion_point:
 * :description:
 * These are all relative to the target component. So for example, if
 * the target is an agent the insertion point would be Node.AgentManager.Agent
 *
 * The third query is performed for each inserted component. Each row
 * returned corresponds to a parameter of the component and consists
 * of two parts: name and value. If both are non-null, the parameter
 * is constructed by concatenating the string together with an "="
 * between them. If either is null, the other is used (without an
 * "="). The same substitutions are available, except they refer to
 * the component being inserted.
 **/
public class ComponentInsertionRecipe extends RecipeBase
    implements Serializable
{
  private static final String DESCRIPTION_RESOURCE_NAME = "component-insertion-recipe-description.html";
  private static final String BACKUP_DESCRIPTION =
    "ComponentInsertionRecipe provides formulaic component insertions into an experiment";

  /** Target Component Property Definitions **/
  public static final String PROP_TARGET_COMPONENT_QUERY = "Target Component Selection Query";
  public static final String PROP_TARGET_COMPONENT_QUERY_DFLT = "recipeQuerySelectNothing";
  public static final String PROP_TARGET_COMPONENT_QUERY_DESC 
    = "The query name for selecting components to modify";

  /** Insertion Component Property Definitions **/
  public static final String PROP_INSERTION_COMPONENT_QUERY 
    = "Insertion Component Specification Query";
  public static final String PROP_INSERTION_COMPONENT_QUERY_DFLT = "recipeQuerySelectNothing";
  public static final String PROP_INSERTION_COMPONENT_QUERY_DESC 
    = "The query name for specifying components to insert";

  /** Insertion Component Property Arg Definitions **/
  public static final String PROP_INSERTION_COMPONENT_ARG_QUERY 
    = "Insertion Component Arg Specification Query";
  public static final String PROP_INSERTION_COMPONENT_ARG_QUERY_DFLT 
    = "recipeQuerySelectNothing";
  public static final String PROP_INSERTION_COMPONENT_ARG_QUERY_DESC 
    = "The query name for specifying args of components to insert";

  // Props for metrics
  private Property propTargetComponentQuery;
  private Property propInsertionComponentQuery;
  private Property propInsertionComponentArgQuery;

  public ComponentInsertionRecipe() {
    super("Component Insertion Recipe");
  }
  
  public ComponentInsertionRecipe(String name) {
    super(name);
  }

  public void initProperties() {
    propTargetComponentQuery =
      addRecipeQueryProperty(PROP_TARGET_COMPONENT_QUERY,
                             PROP_TARGET_COMPONENT_QUERY_DFLT);
    propTargetComponentQuery.setToolTip(PROP_TARGET_COMPONENT_QUERY_DESC);

    propInsertionComponentQuery =
      addRecipeQueryProperty(PROP_INSERTION_COMPONENT_QUERY,
                             PROP_INSERTION_COMPONENT_QUERY_DFLT);
    propInsertionComponentQuery.setToolTip(PROP_INSERTION_COMPONENT_QUERY_DESC);

    propInsertionComponentArgQuery =
      addRecipeQueryProperty(PROP_INSERTION_COMPONENT_ARG_QUERY,
                             PROP_INSERTION_COMPONENT_ARG_QUERY_DFLT);
    propInsertionComponentQuery.setToolTip(PROP_INSERTION_COMPONENT_ARG_QUERY_DESC);
  }

  private Property addRecipeQueryProperty(String name, String dflt) {
    Property prop = addProperty(new RecipeQueryProperty(this, name, dflt));
    prop.setPropertyClass(String.class);
    return prop;
  }

  /**
   * This starts all the work. Find all the target components, then
   * recurse through the component data. When a target is found,
   * modify it.
   **/
  public ComponentData modifyComponentData(ComponentData data, PopulateDb pdb) {
    try {
      Set targets = pdb.executeQuery(propTargetComponentQuery.getValue().toString());
      modifyComponentData(data, pdb, targets);
    } catch (SQLException sqle) {
      sqle.printStackTrace();
    }
    return data;
  }

  private void modifyComponentData(ComponentData data, PopulateDb pdb, Set targets)
    throws SQLException
  {
    if (targets.contains(pdb.getComponentAlibId(data))) {
      // do insertion
      String[][] rowData = pdb.executeQueryForComponent(propInsertionComponentQuery.getValue().toString(), data);
      for (int j = 0; j < rowData.length; j++) {
        String[] vals = rowData[j];
        GenericComponentData comp = new GenericComponentData();
        comp.setName(vals[0]);
        comp.setType(vals[1]);
	if (comp.getType().equals("binder"))
	  comp.setType(ComponentData.AGENTBINDER);
        comp.setClassName(vals[2]);
        rowData = pdb.executeQueryForComponent(propInsertionComponentArgQuery.getValue().toString(), comp);
        for (int i = 0; i < rowData.length; i++) {
          String[] row = rowData[i];
          String param;
          if (row[0] == null) {
            param = row[1];
          } else if (row[1] == null) {
            param = row[0];
          } else {
            param = row[0] + "=" + row[1];
          }
          comp.addParameter(param);
        }
        comp.setParent(data);
        comp.setOwner(this);
	
	// Add this component, replacing any existing copy,
	// and ensuring binders go before others
	data.addChildDefaultLoc(comp);
      }
    }
	
    if (data.childCount() > 0) {
      // for each child, call this same method.
      ComponentData[] children = data.getChildren();
      for (int i = 0; i < children.length; i++) {
	modifyComponentData(children[i], pdb, targets);
      }
    }
  }
}

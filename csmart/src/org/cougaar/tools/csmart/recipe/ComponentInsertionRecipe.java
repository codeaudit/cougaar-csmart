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

import org.cougaar.tools.csmart.ui.component.*;
import java.io.Serializable;
import java.net.URL;
import java.util.Set;
import java.sql.SQLException;

/**
 * This recipe performs component insertion into an experiment based
 * on queries and constants described by its parameters.
 **/
public class ComponentInsertionRecipe extends ModifiableConfigurableComponent
    implements RecipeComponent, PropertiesListener, Serializable
{
  private static final String DESCRIPTION_RESOURCE_NAME = "component-insertion-recipe-description.html";
  private static final String BACKUP_DESCRIPTION =
    "ComponentInsertionRecipe provides formulaic component insertions into an experiment";

  /** Target Component Property Definitions **/
  public static final String PROP_TARGET_COMPONENT_QUERY = "Target Component Selection Query";
  public static final String PROP_TARGET_COMPONENT_QUERY_DFLT = "selectNothing";
  public static final String PROP_TARGET_COMPONENT_QUERY_DESC = "The query name for selecting components to modify";

  /** Insertion Component Property Definitions **/
  public static final String PROP_INSERTION_COMPONENT_QUERY = "Insertion Component Specification Query";
  public static final String PROP_INSERTION_COMPONENT_QUERY_DFLT = "selectNothing";
  public static final String PROP_INSERTION_COMPONENT_QUERY_DESC = "The query name for specifying components to insert";

  /** Insertion Component Property Arg Definitions **/
  public static final String PROP_INSERTION_COMPONENT_ARG_QUERY = "Insertion Component Arg Specification Query";
  public static final String PROP_INSERTION_COMPONENT_ARG_QUERY_DFLT = "selectNothing";
  public static final String PROP_INSERTION_COMPONENT_ARG_QUERY_DESC = "The query name for specifying args of components to insert";

  // Props for metrics
  private Property propTargetComponentQuery;
  private Property propInsertionComponentQuery;
  private Property propInsertionComponentArgQuery;

  private boolean editable = true;

  public ComponentInsertionRecipe() {
    super("Component Insertion Recipe");
  }
  
  public ComponentInsertionRecipe(String name) {
    super(name);
  }

  public void initProperties() {
    propTargetComponentQuery =
      addProperty(PROP_TARGET_COMPONENT_QUERY,
                  PROP_TARGET_COMPONENT_QUERY_DFLT);
    propTargetComponentQuery.setToolTip(PROP_TARGET_COMPONENT_QUERY_DESC);

    propInsertionComponentQuery =
      addProperty(PROP_INSERTION_COMPONENT_QUERY,
                  PROP_INSERTION_COMPONENT_QUERY_DFLT);
    propInsertionComponentQuery.setToolTip(PROP_INSERTION_COMPONENT_QUERY_DESC);

    propInsertionComponentArgQuery =
      addProperty(PROP_INSERTION_COMPONENT_ARG_QUERY,
                  PROP_INSERTION_COMPONENT_ARG_QUERY_DFLT);
    propInsertionComponentQuery.setToolTip(PROP_INSERTION_COMPONENT_ARG_QUERY_DESC);
  }

  public String getRecipeName() {
    return getShortName();
  }

  private void adjustParameterCount() {
  }

  /**
   * Get the agents, both assigned and unassigned.
   * Only return new agents.
   * @return array of agent components
   */
  public AgentComponent[] getAgents() {
    // This recipe adds no new agents
    return null;
  }

  /**
   * Add all components as "modifications"
   **/
  public ComponentData addComponentData(ComponentData data) {
    return data;
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
      System.out.println("Inserting components into " + data.getName());
      String[][] rowData = pdb.executeQueryForComponent(propInsertionComponentQuery.getValue().toString(), data);
      for (int j = 0; j < rowData.length; j++) {
        String[] vals = rowData[j];
        GenericComponentData comp = new GenericComponentData();
        comp.setName(vals[0]);
        comp.setType(vals[1]);
        comp.setClassName(vals[2]);
        rowData = pdb.executeQueryForComponent(propInsertionComponentArgQuery.getValue().toString(), data);
        for (int i = 0; i < rowData.length; i++) {
          String[] row = rowData[i];
          comp.addParameter(row[0] + "=" + row[1]);
        }
        comp.setParent(data);
        comp.setOwner(this);
        data.addChild(comp);
        System.out.println("Inserted " + comp + " into " + data.getName());
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

  ///////////////////////////////////////////
  // Boilerplate stuff added below... Necessary?
  
  // Implement PropertyListener
  /**
   * Called when a new property has been added to the
   * society. 
   *
   * @param PropertyEvent Event for the new property
   */
  public void propertyAdded(PropertyEvent e) {
    Property addedProperty = e.getProperty();
    Property myProperty = getProperty(addedProperty.getName().last().toString());
    if (myProperty != null) {
      setPropertyVisible(addedProperty, true);
    }
  }

  /**
   * Called when a property has been removed from the society
   */
  public void propertyRemoved(PropertyEvent e) {
    // FIXME - do something?
  }
  // end of PropertyListener implementation

  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  /**
   * Returns whether or not the component can be edited.
   * @return true if component can be edited and false otherwise
   */
  public boolean isEditable() {
    //    return !isRunning;
    return editable;
  }

  /**
   * Set whether or not the component can be edited.
   * @param editable true if component is editable and false otherwise
   */
  public void setEditable(boolean editable) {
    this.editable = editable;
  }
}

/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redsitribute it and/or modify
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

import java.io.Serializable;
import java.sql.SQLException;
import java.net.URL;
import java.util.Set;
import java.util.Iterator;

import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;

import org.cougaar.tools.csmart.core.db.PopulateDb;

import org.cougaar.tools.csmart.society.AgentComponent;

public class SpecificInsertionRecipe extends RecipeBase
  implements Serializable
{
  private static final String DESCRIPTION_RESOURCE_NAME = 
    "specific-insertion-recipe-description.html";
  private static final String BACKUP_DESCRIPTION = 
    "SpecificInsertionRecipe provides a method for inserting Components into an experiment";

  private static final String PROP_NAME = "Component Name";
  private static final String PROP_NAME_DFLT = "";
  private static final String PROP_NAME_DESC = 
    "Class name of the component. (usually same as Class Name)";

  private static final String PROP_TYPE = "Type of Insertion";
  private static final String PROP_TYPE_DFLT = "";
  private static final String PROP_TYPE_DESC = "Type of Insertion: plugin, binder, etc.";

  private static final String PROP_CLASS = "Class Name";
  private static final String PROP_CLASS_DFLT = "";
  private static final String PROP_CLASS_DESC = "The complete class of of the Component.";

  // Hack Note:
  // When the properties are read from the database, they are sorted.
  // It is essential that the Number of Arguments property is read
  // from the database before the actual arguments, or it will crash
  // (Because number of args creates the arg props)
  // This is handled now by naming the args as "Value.."

  private static final String PROP_ARGS = "Number of Arguments";
  private static final Integer PROP_ARGS_DFLT = new Integer(0);
  private static final String PROP_ARGS_DESC = "The number of arguments for this insertion.";

  private static final String PROP_TARGET_COMPONENT_QUERY = "Target Component Selection Query";
  private static final String PROP_TARGET_COMPONENT_QUERY_DFLT = "recipeQuerySelectNothing";
  private static final String PROP_TARGET_COMPONENT_QUERY_DESC = 
    "The query name for selecting components to modify";

  private Property propName;
  private Property propType;
  private Property propClass;
  private Property propArgs;
  private Property propTargetComponentQuery;
  private Property[] variableProps = null;

  public SpecificInsertionRecipe() {
    super("Specific Component Recipe");
  }

  public SpecificInsertionRecipe(String name) {
    super(name);
  }

  public void initProperties() {
    propName = addProperty(PROP_NAME, PROP_NAME_DFLT);
    propName.setToolTip(PROP_NAME_DESC);

    propType = addProperty(PROP_TYPE, PROP_TYPE_DFLT);
    propType.setToolTip(PROP_TYPE_DESC);

    propClass = addProperty(PROP_CLASS, PROP_CLASS_DFLT);
    propClass.setToolTip(PROP_CLASS_DESC);

    propArgs = addProperty(PROP_ARGS, PROP_ARGS_DFLT);
    propArgs.addPropertyListener(new ConfigurableComponentPropertyAdapter() {
        public void propertyValueChanged(PropertyEvent e) {
          updatePropCount((Integer)e.getProperty().getValue());
        }
      });
    propArgs.setToolTip(PROP_ARGS_DESC);

    propTargetComponentQuery =
      addRecipeQueryProperty(PROP_TARGET_COMPONENT_QUERY,
                             PROP_TARGET_COMPONENT_QUERY_DFLT);
    propTargetComponentQuery.setToolTip(PROP_TARGET_COMPONENT_QUERY_DESC);

  }

  /**
   * Gets the name of the html help file for this component.
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  private Property addRecipeQueryProperty(String name, String dflt) {
    Property prop = addProperty(new RecipeQueryProperty(this, name, dflt));
    prop.setPropertyClass(String.class);
    return prop;
  }

  private void updatePropCount(Integer newCount) {
    int count = newCount.intValue();

    // For now delete all props and start fresh.
    // Annoying for the user, but it works.
    if( variableProps != null && count != variableProps.length ) {
      for(int i=0; i < variableProps.length; i++) {
        removeProperty(variableProps[i]);
      }
    }

    variableProps = new Property[count];

    // Note: If you feel you want to change the name of these
    // properties, see the "Hack Note" above.
    for(int i=0; i < count; i++) {
      variableProps[i] = addProperty("Value " + (i+1), "");
      ((Property)variableProps[i]).setToolTip("Value for Argument " + (i+1));
    }      
  }

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
      GenericComponentData comp = new GenericComponentData();
      comp.setName(propName.getValue().toString());
      comp.setType(propType.getValue().toString());
      comp.setClassName(propClass.getValue().toString());
      
      // Add args.
      if(variableProps != null) {
        for(int i=0; i < variableProps.length; i++) {
          comp.addParameter(((Property)variableProps[i]).getValue().toString());
        }
      }
      comp.setParent(data);
      comp.setOwner(this);
      data.addChildDefaultLoc(comp);
      //      System.out.println("Inserted " + comp + " into " + data.getName());
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

/* 
 * <copyright>
 *  Copyright 2001,2002 BBNT Solutions, LLC
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
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.range.IntegerRange;
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
  private static final String PROP_TYPE_DFLT = ComponentData.PLUGIN;
  private static final String PROP_TYPE_DESC = "Type of Component being inserted";

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

  private static final String PROP_PRIORITY = "Component Priority";
  private static final String PROP_PRIORITY_DFLT = 
    ComponentDescription.priorityToString(ComponentDescription.PRIORITY_STANDARD);
  public static final String PROP_PRIORITY_DESC = "Load Priority of the component in it's container relative to other child components";

  private Property propName;
  private Property propType;
  private Property propClass;
  private Property propArgs;
  private Property propPriority;
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

    propType = addComponentTypeProperty(PROP_TYPE, PROP_TYPE_DFLT);
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
    propArgs.setAllowedValues(Collections.singleton(new IntegerRange(0, Integer.MAX_VALUE)));
    propPriority = addPriorityProperty(PROP_PRIORITY, PROP_PRIORITY_DFLT);
    propPriority.setToolTip(PROP_PRIORITY_DESC);

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

  private Property addComponentTypeProperty(String name, String dflt) {
    Property prop = addProperty(new ComponentTypeProperty(this, name, dflt));
    return prop;
  }

  private Property addRecipeQueryProperty(String name, String dflt) {
    Property prop = addProperty(new RecipeQueryProperty(this, name, dflt));
    prop.setPropertyClass(String.class);
    return prop;
  }

  private Property addPriorityProperty(String name, String dflt) {
    Property prop = addProperty(new PriorityProperty(this, name, dflt));
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
      if(log.isErrorEnabled()) {
        log.error("Exception", sqle);
      }
    }
    return data;
  }

  private void modifyComponentData(ComponentData data, final PopulateDb pdb, final Set targets)
    throws SQLException
  {
    if (targets.contains(pdb.getComponentAlibId(data))) {
      // do insertion
      GenericComponentData comp = new GenericComponentData();
      comp.setName(propName.getValue().toString());
      comp.setPriority(propPriority.getValue().toString());
      // FIXME: Do an equals with the ComponentData constants here?
      String type = propType.getValue().toString();
      if (type == null) {
	if (log.isWarnEnabled()) {
	  log.warn("Null type for component " + comp);
	}
	comp.setType(null); // FIXME!
      } else if (type.equals(ComponentData.PLUGIN))
	comp.setType(ComponentData.PLUGIN);
      else if (type.equals(ComponentData.AGENTBINDER))
	comp.setType(ComponentData.AGENTBINDER);
      else if (type.equals(ComponentData.NODEBINDER))
	comp.setType(ComponentData.NODEBINDER);
      else
	comp.setType(type);
      comp.setClassName(propClass.getValue().toString());
      
      // Add args.
      if(variableProps != null) {
        for(int i=0; i < variableProps.length; i++) {
          comp.addParameter(((Property)variableProps[i]).getValue().toString());
        }
      }
      comp.setParent(data);
      comp.setOwner(this);

      // Reset the name to ensure uniqueness. This will use the user-specified name if possible,
      // but tack on the first parameter and/or a number if necessary to ensure a unique name

      if (GenericComponentData.alreadyAdded(data, comp)) {
	if (log.isDebugEnabled()) {
	  log.debug("Not re-adding component. " + data.getName() + " already contains " + comp);
	}
      } else {
	comp.setName(GenericComponentData.getSubComponentUniqueName(data, comp));
	
	data.addChildDefaultLoc(comp);
	if(log.isInfoEnabled()) {
	  log.info("Inserted " + comp + " into " + data.getName());
	}
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

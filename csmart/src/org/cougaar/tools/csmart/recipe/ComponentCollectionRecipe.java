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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import org.cougaar.core.agent.ClusterImpl;
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.cdata.PGPropData;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.core.property.range.IntegerRange;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.ComponentBase;
import org.cougaar.tools.csmart.society.PluginComponent;
import org.cougaar.tools.csmart.society.PropGroupComponent;
import org.cougaar.tools.csmart.society.RelationshipComponent;
import org.cougaar.util.log.Logger;

/**
 * Recipe to add a complete Agent to the society.  This agent
 * consists of all required plugins relationships and asset data.
 */
public class ComponentCollectionRecipe extends ComplexRecipeBase
  implements Serializable
{
  private static final String DESCRIPTION_RESOURCE_NAME = 
    "collection-recipe-description.html";

  private Property propTargetComponent;

  /**
   * Creates a new <code>ComponentCollectionRecipe</code> instance.
   *
   */
  public ComponentCollectionRecipe() {
    this("Component Recipe");
  }

  /**
   * Creates a new <code>ComponentCollectionRecipe</code> instance.
   *
   * @param name Name of the Component
   */
  public ComponentCollectionRecipe(String name) {
    super(name);
  }

  public ComponentCollectionRecipe(String name, String assemblyId) {
    super(name, assemblyId);
  }

  public ComponentCollectionRecipe(String name, String assemblyId, String recipeId) {
    super(name, assemblyId, recipeId);
  }

  public ComponentCollectionRecipe(ComponentData cdata, String assemblyId) {
    super(cdata, assemblyId);
  }

  /**
   * Initialize any local Properties
   *
   */
  public void initProperties() {
    super.initProperties();

    propTargetComponent =
      addRecipeQueryProperty(PROP_TARGET_COMPONENT_QUERY,
                             PROP_TARGET_COMPONENT_QUERY_DFLT);
    propTargetComponent.setToolTip(PROP_TARGET_COMPONENT_QUERY_DESC);

    modified = false;
    fireModification(new ModificationEvent(this, RECIPE_SAVED));

  }

  /**
   * Gets the name of the html help file for this component.
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }


  /**
   * Get the name of this Recipe.
   *
   * @return a Recipe Name as a <code>String</code> value
   */
  public String getRecipeName() {
    return getShortName();
  }

  /**
   * Gets an array of all agents created by this recipe.
   *
   * @see AgentComponent
   * @return an <code>AgentComponent[]</code> array of all agents
   */
  public AgentComponent[] getAgents() {
//     initAgents();
    Collection agents = getDescendentsOfClass(AgentComponent.class);    
    return (AgentComponent[]) agents.toArray(new AgentComponent[agents.size()]);
  }

  /**
   * Adds any new data to the global <code>ComponentData</code> tree.
   * No existing data is modified in this method.
   * Warning: Assumes it is handed a ComponentData which contains
   * all Agents below it.
   *
   * @param data Pointer to the Global <code>ComponentData</code> tree
   * @return an updated <code>ComponentData</code> value
   */
  public ComponentData addComponentData(ComponentData data) {
    ComponentData[] children = data.getChildren();
    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
      // for each child component data, if it's an agent's component data
      if (child.getType() == ComponentData.AGENT) {
        // get all my agent components
	Iterator iter = 
          ((Collection)getDescendentsOfClass(AgentComponent.class)).iterator();
	while(iter.hasNext()) {
	  AgentComponent agent = (AgentComponent)iter.next();
          // Do not process Recipe Agents here, only in modify.
          if(!child.getName().equals(RECIPE_AGENT)) {
            // if the component data name matches the agent name
            if (child.getName().equals(agent.getShortName().toString())) {
              // then set me as the owner of the component data
              child.setOwner(this);
              // and add the component data
              agent.addComponentData(child);
            }
          }
	}		
      } else if (child.getType() == ComponentData.PLUGIN || 
                 child.getType() == ComponentData.AGENTBINDER ||
                 child.getType() == ComponentData.NODEBINDER ) {
          // get all top level Plugins
          Iterator iter = ((Collection)getDescendentsOfClass(BaseComponent.class)).iterator();
          while(iter.hasNext()) {
            BaseComponent comp = (BaseComponent)iter.next();
            // if the component data name matches the agent name
            if (child.getName().equals(comp.getShortName().toString())) {
              // then set me as the owner of the component data
              child.setOwner(this);
              // and add the component data
              comp.addComponentData(child);
            }
          }
      } else {
        addComponentData(child);
      }

    }

    return data;
  }

  /**
   * Modifies the global <code>ComponentData</code> tree.
   * This method is free to make any modifications it needs
   * to the global tree, not just to it's own Component.
   * <br>
   * Currently, this component makes no modifications.
   *
   * @param data Pointer to the global <code>ComponentData</code>
   * @param pdb Access to the database via <code>PopulateDb</code> object
   * @return a modified <code>ComponentData</code> value
   */
  public ComponentData modifyComponentData(ComponentData data, PopulateDb pdb) {
    try {
      Set dfltSet = pdb.executeQuery(getProperty(PROP_TARGET_COMPONENT_QUERY).getValue().toString());
      for(int i=0; i < getChildCount(); i++) {
        ConfigurableComponent cc = (ConfigurableComponent)getChild(i);
        if(cc.getProperty(PROP_TARGET_COMPONENT_QUERY) == null) {
          modifyComponentData(data, pdb, dfltSet, cc);
        } else {
          try {
            Set targets = pdb.executeQuery(cc.getProperty(PROP_TARGET_COMPONENT_QUERY).getValue().toString());
            modifyComponentData(data, pdb, targets, cc);
          } catch (SQLException sqle) {
            if(log.isErrorEnabled()) {
              log.error("Caught Exception executing query", sqle);
            }
            return data;
          }
        }
      }
    } catch (SQLException sqle) {
      if(log.isErrorEnabled()) {
        log.error("Caught Exception executing query", sqle);
      }
      return data;
    } 

    return data;
  }

  private void modifyComponentData(ComponentData data, final PopulateDb pdb, final Set targets, ConfigurableComponent cc)
    throws SQLException
  {
    if (targets.contains(pdb.getComponentAlibId(data))) {
      data = cc.modifyComponentData(data);
    }
    if (data.childCount() > 0) {
      // for each child, call this same method.
      ComponentData[] children = data.getChildren();
      for (int i = 0; i < children.length; i++) {
	modifyComponentData(children[i], pdb, targets, cc);
      }
    }
  } 


  public ComponentData getComponentData() {
    ComponentData cd = new GenericComponentData();
    cd.setType(ComponentData.RECIPE);
    cd.setClassName(RECIPE_CLASS);
    cd.setName(getRecipeName());
    cd.setOwner(null);
    cd.setParent(null);

    for(int i=0; i < getChildCount(); i++) {
      ConfigurableComponent cc = (ConfigurableComponent)getChild(i);
      if(cc instanceof AgentComponent) {
        AgentComponentData ac = new AgentComponentData();
        ac.setName(cc.getShortName());
        ac.setClassName(ClusterImpl.class.getName());
        ac.addParameter(cc.getShortName()); // Agents have one parameter, the agent name
        ac.setOwner(cc);
        ac.setParent(cd);
        // Add any new Parameters...
        Iterator iter = getLocalProperties();
        while(iter.hasNext()) {
          Property p = (Property)iter.next();
          ac.addParameter(p);
        }
        
        cd.addChild(cc.addComponentData(ac));

      } else if (cc instanceof ComponentBase) {
        cd = cc.addComponentData(cd);
      } else if (cc instanceof PropGroupComponent) {
        // Need to create a dummy Agent as a container.
        AgentComponentData ac = new AgentComponentData();
        ac.setName(RECIPE_AGENT);
        ac.setClassName(RECIPE_CLASS);
        ac.setParent(cd);
        AgentAssetData assetData = new AgentAssetData(null);
        assetData.addPropertyGroup(((PropGroupComponent)cc).getPropGroupData());
        ac.addAgentAssetData(assetData);
        cd.addChild((ComponentData)ac);
      } else if (cc instanceof RelationshipComponent) {
        System.out.println("Have a RelationshipComponent");
      }

    }
    
    return cd;
  }

  public ModifiableComponent copy(String name) {
    ComponentData cdata = getComponentData();
    cdata.setName(name);
    RecipeComponent component = new ComponentCollectionRecipe(cdata, null);
    component.initProperties();

    ((ComponentCollectionRecipe)component).modified = this.modified;
    ((ComponentCollectionRecipe)component).oldAssemblyId = getAssemblyId();


    for(int i=0; i < getChildCount(); i ++) {
      ConfigurableComponent child = (ConfigurableComponent)getChild(i);
      Property prop = child.getProperty(PROP_TARGET_COMPONENT_QUERY);
      if(prop != null) {
        String propName = prop.getName().toString();
        for(int j=0; j < component.getChildCount(); j++) {
          ConfigurableComponent newChild = (ConfigurableComponent)component.getChild(j);
          if(newChild.getShortName().equals(child.getShortName()))
            newChild.addProperty(propName.substring(name.lastIndexOf(".")+1), prop.getValue());
        }

      }
    }

    return component;
  }

  public boolean saveToDatabase() {

    // The only way to preserve the target overrides in the children is to make them
    // arguments of the parent, save, and then remove them from the parent.

    for(int i=0; i < getChildCount(); i ++) {
      ComponentBase child = (ComponentBase)getChild(i);
      if(child.getProperty(PROP_TARGET_COMPONENT_QUERY) != null) {
        addProperty(("$$CP=" + child.getComponentClassName() + "-" + i), child.getProperty(PROP_TARGET_COMPONENT_QUERY).getValue());
      }
    }

    boolean retVal = super.saveToDatabase();

    // Save is finished, remove the properties from the parent.
    Iterator iter = getProperties();
    while(iter.hasNext()) {
      Property p = (Property)iter.next();
      if(p.getName().last().toString().startsWith("$$CP")) {
        removeProperty(p);
      }
    }

    return retVal;
  }

}

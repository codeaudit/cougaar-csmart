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
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.core.property.range.IntegerRange;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.util.log.Logger;

/**
 * Recipe to add a complete Agent to the society.  This agent
 * consists of all required plugins relationships and asset data.
 */
public class CompleteAgentRecipe extends ComplexRecipeBase
  implements Serializable
{
  private static final String DESCRIPTION_RESOURCE_NAME = 
    "complete-agent-recipe-description.html";

  /**
   * Creates a new <code>CompleteAgentRecipe</code> instance.
   *
   */
  public CompleteAgentRecipe() {
    this("Agent Component Recipe");
  }

  /**
   * Creates a new <code>CompleteAgentRecipe</code> instance.
   *
   * @param name Name of the Component
   */
  public CompleteAgentRecipe(String name) {
    super(name);
  }

  public CompleteAgentRecipe(String name, String assemblyId) {
    super(name, assemblyId);
  }

  public CompleteAgentRecipe(String name, String assemblyId, String recipeId) {
    super(name, assemblyId, recipeId);

    // recipeId is not needed for this recipe, however the method sig is needed
    // so OrganizerHelper can find the class correctly using reflection.
  }

  /**
   * Initialize any local Properties
   *
   */
  public void initProperties() {
    super.initProperties();
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
    return data;
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

  private static final void generateAgentComponentData(AgentComponent agent, 
                             ComponentData parent, 
                             ConfigurableComponent owner) {

    AgentComponentData ac = new AgentComponentData();
    ac.setName(agent.getShortName());
    ac.setClassName(ClusterImpl.class.getName());
    ac.addParameter(agent.getShortName()); // Agents have one parameter, the agent name
    ac.setOwner(owner);
    ac.setParent(parent);
    parent.addChild((ComponentData)ac);
  }

}

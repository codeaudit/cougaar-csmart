/* 
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */ 
package org.cougaar.tools.csmart.recipe;

import org.cougaar.core.agent.SimpleAgent;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.util.log.Logger;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

/**
 * Recipe to add a complete Agent to the society.  This agent
 * consists of all required plugins relationships and asset data.
 */
public class CompleteAgentRecipe extends ComplexRecipeBase
  implements Serializable
{
  protected static String DESCRIPTION_RESOURCE_NAME = 
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

  public CompleteAgentRecipe(String name, String assemblyId, String recipeId, String initName) {
    super(name, assemblyId, recipeId, initName);

    // recipeId is not needed for this recipe, however the method sig is needed
    // so OrganizerHelper can find the class correctly using reflection.
  }

  public CompleteAgentRecipe(ComponentData cdata, String assemblyId) {
    super(cdata, assemblyId);
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
	    if (log.isDebugEnabled())
	      log.debug("addComponentData found my agent " + child.getName() + " which currently says isModified=" + child.isModified());

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

    return modifyComponentData(cd);

    // FIXME: Why call this twice?
    //    return addComponentData(cd);
  }

  private static final void generateAgentComponentData(AgentComponent agent, 
                             ComponentData parent, 
                             ConfigurableComponent owner) {

    AgentComponentData ac = new AgentComponentData();
    ac.setName(agent.getShortName());
    ac.setClassName(SimpleAgent.class.getName());
    ac.addParameter(agent.getShortName()); // Agents have one parameter, the agent name
    ac.setOwner(owner);
    ac.setParent(parent);
    parent.addChild((ComponentData)ac);
  }

}

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
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.ComponentBase;
import org.cougaar.tools.csmart.society.PropGroupComponent;
import org.cougaar.tools.csmart.society.RelationshipComponent;
import org.cougaar.util.log.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Recipe to add a complete Agent to the society.  This agent
 * consists of all required plugins relationships and asset data.
 */
public class ComponentCollectionRecipe extends ComplexRecipeBase
  implements Serializable
{
  protected static final String DESCRIPTION_RESOURCE_NAME = 
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

  public ComponentCollectionRecipe(String name, String assemblyId, String recipeId, String initName) {
    super(name, assemblyId, recipeId, initName);
  }

  public ComponentCollectionRecipe(ComponentData cdata, String assemblyId) {
    super(cdata, assemblyId);
  }

  // Do a normal setName, not silently changing just the recipe name,
  // cause the free-floating components must have their names changed as well
  public void setName(String newName) {
    if (newName == null || newName.equals("") || newName.equals(getRecipeName())) 
      return;

    super.setName(newName);

    // Must mark this recipe modified, cause the sub-component names have changed
    // and must be changed in DB to restore correctly
    // This is bug 2040
    fireModification();
  }

  /**
   * Create the local Parent target query, then call the parent to complete.
   */
  public void initProperties() {
    propTargetComponent =
      addRecipeQueryProperty(PROP_TARGET_COMPONENT_QUERY,
                             PROP_TARGET_COMPONENT_QUERY_DFLT);
    propTargetComponent.setToolTip(PROP_TARGET_COMPONENT_QUERY_DESC);

    super.initProperties();

    // The super will set the parent target query

    // FIXME: Super also deals with this modification stuff
    // And it says that a copy (init from CDATA) is modified
//     modified = false;
//     fireModification(new ModificationEvent(this, RECIPE_SAVED));

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
          // Do not process Recipe Agents here, only in modify.
          if(!child.getName().equals(RECIPE_AGENT)) {
            // if the component data name matches the agent name
            if (child.getName().equals(agent.getShortName().toString())) {
              // then set me as the owner of the component data
              child.setOwner(this);

	      if (log.isDebugEnabled())
		log.debug("addCData about to call it on agent " + child.getName());
              // and add the component data
              agent.addComponentData(child);
            }
          }
	}		
      } else if (child.getType() == ComponentData.SOCIETY || 
                 child.getType() == ComponentData.HOST ||
		 child.getType() == ComponentData.RECIPE ||
                 child.getType() == ComponentData.NODE ) {
	// some container -- recurse down
        addComponentData(child);
      } else {
	// get all top level Components
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
        ac.setClassName(SimpleAgent.class.getName());
        ac.addParameter(cc.getShortName().toString()); // Agents have one parameter, the agent name
        ac.setOwner(cc);
        ac.setParent(cd);
//         // Add any new Parameters...
//         Iterator iter = cc.getLocalProperties();
//         while(iter.hasNext()) {
//           Property p = (Property)iter.next();
// 	  if (p != null)
// 	    ac.addParameter(p.getValue().toString());
//         }
        
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

//     if (log.isDebugEnabled()) {
//       log.debug(this + " produced CDATA: " + cdata);
//       // Maybe look here to see if that param got added OK?
//       ComponentData[] kids = cdata.getChildren();
//       for (int i = 0; i < cdata.childCount(); i++) {
// 	int params = kids[i].parameterCount();
// 	log.debug("cdata.children["+i+"] has params: ");
// 	for (int j = 0; j < params; j++) 
// 	  log.debug("param["+j+"]= " + kids[i].getParameter(j));
//       }
//     }

    RecipeComponent component = new ComponentCollectionRecipe(cdata, null);
    component.initProperties();

    // FIXME: This next line means if the original was not modified
    // then the copy will not be modified, even though the copy
    // will not be in the DB (no RCP assembly)
    //    ((ComponentCollectionRecipe)component).modified = this.modified;
    ((ComponentCollectionRecipe)component).oldAssemblyId = getAssemblyId();


    for(int i=0; i < getChildCount(); i ++) {
      ConfigurableComponent child = (ConfigurableComponent)getChild(i);
      Property prop = child.getProperty(PROP_TARGET_COMPONENT_QUERY);
      if(prop != null) {
        String cname = child.getFullName().get(0).toString();
        for(int j=0; j < component.getChildCount(); j++) {
          ConfigurableComponent newChild = (ConfigurableComponent)component.getChild(j);
          if(newChild.getShortName().equals(child.getShortName()) ||
             newChild.getShortName().equals(cname + "|"+ ((ComponentBase)child).getComponentClassName()))
            newChild.addProperty(prop.getName().last().toString(), prop.getValue());
        }
      }
    }

    return component;
  }

  public boolean saveToDatabase() {

    // The only way to preserve the target overrides in the children is to make them
    // arguments of the parent, save, and then remove them from the parent.

    Map targets = new HashMap();
    for(int i=0; i < getChildCount(); i ++) {
//       ComponentBase child = (ComponentBase)getChild(i);
      ConfigurableComponent child = (ConfigurableComponent)getChild(i);
      if(child.getProperty(PROP_TARGET_COMPONENT_QUERY) != null) {
        targets.put(("$$CP=" + ((ComponentBase)child).getComponentClassName() + "-" + i), child.getProperty(PROP_TARGET_COMPONENT_QUERY).getValue());
      }
    }

    boolean retVal = super.saveToDatabase();

    try {
      PDbBase.saveTargetOverrides(this, targets);
    } catch (SQLException sqle) {
      if(log.isErrorEnabled()) {
        log.error("Error saving target overrides to database", sqle);
      }
      retVal = false;
    } catch (IOException ioe) {
      if(log.isErrorEnabled()) {
        log.error("Error saving target overrides to database", ioe);
      }
      retVal = false;
    }

    return retVal;
  }

}

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
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import org.cougaar.core.agent.ClusterImpl;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponentCreator;
import org.cougaar.tools.csmart.ui.viewer.GUIUtils;
import org.cougaar.tools.csmart.recipe.cdata.ComplexRecipeCDataComponent;

/**
 * ComplexRecipeBase.java
 *
 *
 * Created: Thu Jun 20 13:52:13 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class ComplexRecipeBase extends RecipeBase 
  implements ComplexRecipeComponent, Serializable {

  public static final String ASSEMBLY_PROP = "Assembly Id";

  protected String assemblyId = null;
  protected String oldAssemblyId = null;
  private boolean saveInProgress = false;
  protected boolean modified = true;

  private Property propAssemblyId;

  public static final String RECIPE_CLASS = "##RECIPE_CLASS##";

  public ComplexRecipeBase (String name){
    super(name);
  }
  
  public ComplexRecipeBase (String name, String assemblyId){
    super(name);
    this.assemblyId = assemblyId;
  }

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
    String name = getRecipeName();

    if (currAssID != null && currAssID.startsWith("CMT")) {
      if (log.isDebugEnabled()) {
	log.debug("saveToDB not saving CMT society (" + getRecipeName() + ") with id " + currAssID + " and old ID " + oldCMTAsbid + " into same ID. Will create new one and new name -- like a copy, except done in place");
      }
      oldCMTAsbid = currAssID;
      currAssID = null;
      name = name + " edited";
    }

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

    // Now save to the recipe tables.
    boolean result = false;
    PDbBase pdbb = null;
    try {
      propAssemblyId.setVisible(true);
      pdbb = new PDbBase();
      pdbb.insureLibRecipe(this);
      result = true;
    } catch (Exception sqle) {
      if(log.isErrorEnabled()) {
        log.error("Exception", sqle);
      }
      result = false;
    } finally {
      try {
	if (pdbb != null)
	  pdbb.close();
      } catch (Exception sqle) {
	if(log.isErrorEnabled()) {
	  log.error("Exception", sqle);
	}
      }
    }
    propAssemblyId.setVisible(false);
    modified = false;
    saveInProgress = false;
    // tell listeners society is now saved
    fireModification(new ModificationEvent(this, RECIPE_SAVED));
    return ret;
  }

  public void initProperties() {
    propAssemblyId = addProperty(ASSEMBLY_PROP, ((assemblyId != null) ? assemblyId : ""));
    propAssemblyId.setVisible(false);
    
  }

  /**
   * Get the assembly id for this Recipe.
   * @return a <code>String</code> which is the assembly id for this Recipe
   */
  public String getAssemblyId() {
    return this.assemblyId;
//     return propAssemblyId.getValue().toString();
  }

  private ComponentData getComponentData() {
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

  public ModifiableComponent copy(String name) {
    
    ComponentData cdata = getComponentData();
    cdata.setName(name);
    RecipeComponent component = new ComplexRecipeCDataComponent(cdata, null);
    component.initProperties();

    ((ComplexRecipeBase)component).modified = this.modified;
    ((ComplexRecipeBase)component).oldAssemblyId = getAssemblyId();

    return component;
  }

}// ComplexRecipeBase

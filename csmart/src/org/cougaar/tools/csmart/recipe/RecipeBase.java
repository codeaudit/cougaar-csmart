/**
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.property.*;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.core.property.range.BooleanRange;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Holds basic recipe functionality
 *
 */
public abstract class RecipeBase 
  extends ModifiableConfigurableComponent 
  implements RecipeComponent, PropertiesListener  {

  protected transient Logger log;

  protected boolean modified = true;

  // modification event
  public static final int RECIPE_SAVED = 3;

  // used to block modify notifications while saving
  protected transient boolean saveInProgress = false; 

  public RecipeBase (String name){
    super(name);
    createLogger();
    installListeners();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public String getRecipeName() {
    return getShortName();
  }

  public void setName(String newName) {
    if (newName == null || newName.equals("") || newName.equals(getRecipeName())) 
      return;

    boolean temp = modified;
    String oldname = getRecipeName();
    super.setName(newName);

    // do the DB save that is necessary
    try {
      PDbBase.changeRecipeName(oldname, newName);
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
	log.error("setName exception changing name from " + oldname + " to " + newName, e);
      }
      // On error, mark the recipe as modified
      temp = true;
    }
    
    // It may be that since recipes save via properties
    // and the property names will have changed that I should
    // do an xor or something perhaps, but more likely just
    // mark it true always
    modified = temp;
  }

  // not needed; these are the same as in ModifiableConfigurableComponent
//    public void addModificationListener(ModificationListener l)
//    {
//      getEventListenerList().add(ModificationListener.class, l);
//    }

//    public void removeModificationListener(ModificationListener l)
//    {
//      getEventListenerList().remove(ModificationListener.class, l);
//    }

  public Property addBooleanProperty(String name, boolean dflt) {
    Property tmp = addProperty(name, new Boolean(dflt));
    HashSet booleanSet = new HashSet();
    booleanSet.add(new BooleanRange(false));
    booleanSet.add(new BooleanRange(true));
    tmp.setAllowedValues(booleanSet);
    return tmp;
  }

  /**
   * Called when a new property has been added to the
   * recipe. 
   *
   * @param e Event for the new property
   */
  public void propertyAdded(PropertyEvent e) {
    Property addedProperty = e.getProperty();
    Property myProperty = getProperty(addedProperty.getName().last().toString());
    if (myProperty != null) {
      // set property visible?
      addedProperty.addPropertyListener(myPropertyListener);
      fireModification();
    }
  }

  /**
   * Called when a property has been removed from the recipe.
   */
  public void propertyRemoved(PropertyEvent e) {
    e.getProperty().removePropertyListener(myPropertyListener);
    fireModification();
  }

  /**
   * Most recipes do not contain agents.
   *
   * @return an <code>AgentComponent[]</code> value
   */
  public AgentComponent[] getAgents() {
    ArrayList agents = 
      new ArrayList(getDescendentsOfClass(AgentComponent.class));
    return (AgentComponent[])agents.toArray(new AgentComponent[agents.size()]);
//     return null;
  }

  /**
   * Most recipes do not add ComponentData, they modify it.
   * This is a helper impl.
   *
   * @param data 
   * @return a <code>ComponentData</code> value
   */
  public ComponentData addComponentData(ComponentData data) {
    return data;
  }

  /**
   * Save the recipe to the database.
   * @return boolean true if save was successful
   */
  public boolean saveToDatabase() {
    saveInProgress = true;
    boolean result = false;
    PDbBase pdb = null;
    try {
      pdb = new PDbBase();
      pdb.insureLibRecipe(this);
      result = true;
    } catch (Exception sqle) {
      if(log.isErrorEnabled()) {
        log.error("Exception", sqle);
      }
      result = false;
    } finally {
      try {
	if (pdb != null)
	  pdb.close();
      } catch (Exception sqle) {
	if(log.isErrorEnabled()) {
	  log.error("Exception", sqle);
	}
      }
    }
    saveInProgress = false;
    if (result) {
      modified = false;
      fireModification(new ModificationEvent(this, RECIPE_SAVED));
    }
    return result;
  }

  /**
   * Return true if recipe is different than in the database.
   * @return boolean true if recipe is different than in database
   */
  public boolean isModified() {
    return modified;
  }

  public void fireModification() {
    modified = true;
    super.fireModification();
  }

  // only listen on local properties

  public void installListeners() {
    addPropertiesListener(this);
    for (Iterator i = getLocalPropertyNames(); i.hasNext(); ) {
      Property p = getProperty((CompositeName)i.next());
      if(p != null) 
        p.addPropertyListener(myPropertyListener);
    }
  }

  PropertyListener myPropertyListener =
    new PropertyListener() {
      public void propertyValueChanged(PropertyEvent e) {
	if (e == null || e.getProperty() == null)
	  return;
	if (e.getProperty().getValue() == null) {
	  if (e.getPreviousValue() == null)
	    return;
	  else
	    fireModification();
	} else if (e.getPreviousValue() == null) {
	  fireModification();
	} else if (! e.getProperty().getValue().toString().trim().equals(e.getPreviousValue().toString())) {
	  fireModification();
	}
      }
      
      public void propertyOtherChanged(PropertyEvent e) {
	fireModification();
      }
    };


  ModificationListener myModificationListener = new MyModificationListener();
  
  public int addChild(ComposableComponent c) {
    ((ModifiableConfigurableComponent)c).addModificationListener(myModificationListener);
    fireModification();
    return super.addChild(c);
  }

  public void removeChild(ComposableComponent c) {
    ((ModifiableConfigurableComponent)c).removeModificationListener(myModificationListener);
    fireModification();
    super.removeChild(c);
  }

  class MyModificationListener implements ModificationListener, ConfigurableComponentListener {
    public void modified(ModificationEvent e) {
      // don't propagate modifications when we're saving
      if (!saveInProgress)
        fireModification();
    }
  }

  // Bug 1357: Add in own ModificationListener as per SocietyBase, it check on saveInProgress?

  public ModifiableComponent copy(String name) {
    ModifiableComponent copiedComponent = super.copy(name);
    if (copiedComponent != null)
      ((RecipeBase)copiedComponent).modified = this.modified;
    return copiedComponent;
  }

  /**
   * Indicate that the recipe is up-to-date with respect to the database.
   * Use with caution!  The only reason to reset this flag 
   * is that when a recipe is created from the database, 
   * it appears to be modified.
   */
  public void resetModified() {
    modified = false;
    // tell listeners recipe is now saved
    fireModification(new ModificationEvent(this, RECIPE_SAVED));
  }

  /**
   * Recipes are keyed off of their name in the DB.
   * So two recipes are equal iff their names are equal.
   */
  public boolean equals(Object o) {
    if (o instanceof RecipeComponent) {
      RecipeComponent that = (RecipeComponent)o;
      if (! this.getRecipeName().equals(that.getRecipeName())) {
	return false;
      } 
      return true;
    }
    return false;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
    installListeners();
    saveInProgress = false;
  }

}// RecipeBase

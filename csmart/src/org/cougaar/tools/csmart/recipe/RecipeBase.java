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

import java.io.ObjectInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;

import org.cougaar.util.log.Logger;

import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.core.property.PropertiesListener;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ModificationListener;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.range.BooleanRange;
import org.cougaar.tools.csmart.core.property.range.StringRange;

import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.core.db.PDbBase;

/**
 * Holds basic recipe functionality
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 */
public abstract class RecipeBase 
  extends ModifiableConfigurableComponent 
  implements RecipeComponent, PropertiesListener  {

  protected transient Logger log;

  public RecipeBase (String name){
    super(name);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public String getRecipeName() {
    return getShortName();
  }

  public void addModificationListener(ModificationListener l)
  {
    getEventListenerList().add(ModificationListener.class, l);
  }

  public void removeModificationListener(ModificationListener l)
  {
    getEventListenerList().remove(ModificationListener.class, l);
  }

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
  public void propertyRemoved(PropertyEvent e) {}

  /**
   * Most recipes do not contain agents.
   *
   * @return an <code>AgentComponent[]</code> value
   */
  public AgentComponent[] getAgents() {
    return null;
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

  public boolean saveToDatabase() {
    boolean result = false;
    try {
      PDbBase pdb = new PDbBase();
      pdb.insureLibRecipe(this);
      result = true;
    } catch (Exception sqle) {
      if(log.isErrorEnabled()) {
        log.error("Exception", sqle);
      }
      result = false;
    }
    return result;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}// RecipeBase

/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.core.property;

import javax.swing.event.EventListenerList;

/**
 * Base class for ConfigurableComponents implementing the
 * ModifiableComponent interface
 **/
public abstract class ModifiableConfigurableComponent
  extends ConfigurableComponent
  implements ModifiableComponent
{

  protected boolean editable = false;

  /**
   * Creates a new <code>ModifiableConfigurableComponent</code> instance.
   *
   * @param name 
   */
  protected ModifiableConfigurableComponent(String name) {
    super(name);
  }

  /**
   * Indicates if this component can be edited.
   *
   * @return a <code>boolean</code> value
   */
  public boolean isEditable() {
    return editable;
  }

  /**
   * Sets if this component can be edited.
   *
   * @param editable 
   */
  public void setEditable (boolean editable) {
    this.editable = editable;
  }
  
  /**
   * Adds a new ModificationListener to this Component.
   *
   * @param l ModificationListener for this component.
   */
  public void addModificationListener(ModificationListener l) {
    getEventListenerList().add(ModificationListener.class, l);
  }

  /**
   * Removes a ModificationListener from this component.
   *
   * @param l ModificationListener to remove.
   */
  public void removeModificationListener(ModificationListener l) {
    getEventListenerList().remove(ModificationListener.class, l);
  }

  /**
   * Fires an Event when a Modification has been made to the component.
   *
   */
  protected void fireModification() {
    fireModification(new ModificationEvent(this, ModificationEvent.SOMETHING_CHANGED));
  }

  protected void fireModification(ModificationEvent event) {
    ModificationListener[] ls =
      (ModificationListener[]) getEventListenerList()
      .getListeners(ModificationListener.class);
    for (int i = 0; i < ls.length; i++) {
      try {
        ls[i].modified(event);
      } catch (RuntimeException re) {
        re.printStackTrace();
      }
    }
  }
}

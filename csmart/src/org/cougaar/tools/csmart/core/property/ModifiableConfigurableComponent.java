/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.core.property;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.util.EventListener;

/**
 * Base class for ConfigurableComponents implementing the
 * ModifiableComponent interface
 **/
public abstract class ModifiableConfigurableComponent
  extends ConfigurableComponent
  implements ModifiableComponent
{

  /** Indicates if this Component is editable **/
  protected transient boolean editable = true;
  // FIXME: Should this be marked transient?
  // It is used basically exclusively to block doing other things
  // while a component is in the Configuration Builder 
  // used in CSMART.java and PropertyBuilder.java
  // Also used in ExperimentTree to decide if a DnD action is allowed
  // And in UnboundPropertyBuilder constructor
  // -- if not editable, grey things out, etc

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
    // ensure that we don't add the same event listener twice
    EventListenerList listeners = getEventListenerList();
    EventListener[] modListeners = 
      listeners.getListeners(ModificationListener.class);
    for (int i = 0; i < modListeners.length; i++)
      if (modListeners[i].equals(l))
        return; // it's already listening
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
        if(log.isErrorEnabled()) {
          log.error("Exception telling listener about modification", re);
        }
      }
    }
  }

  private static Class[] constructorArgTypes = {String.class};

  public ModifiableComponent copy(String name) {
    Class cls = this.getClass();
    try {
      Constructor constructor = cls.getConstructor(constructorArgTypes);
      ModifiableComponent component = 
	(ModifiableComponent) constructor.newInstance(new String[] {name});
      component.initProperties();
      component = (ModifiableComponent)this.copy(component);
      return component;
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception copying configurable component", e);
      }
      return null;
    }
  }

  private void readObject(ObjectInputStream stream)
    throws IOException, ClassNotFoundException
  {
    stream.defaultReadObject();
    editable = true;
  }
}

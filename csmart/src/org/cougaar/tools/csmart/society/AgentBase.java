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
package org.cougaar.tools.csmart.society;

import org.cougaar.core.component.ComponentDescription;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.*;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * Abstract base-class for Agents
 * Implementers must take care of ensuring that Plugins, Binders,
 * AssetData (and soon other misc components) are added.
 */
public abstract class AgentBase
  extends ModifiableConfigurableComponent
  implements AgentComponent, PropertiesListener {

  private transient Logger log;
  protected String classname;
  protected boolean modified = true;

  /** Agent Classname Property Definitions **/
  private static final String DEFAULT_CLASS = "org.cougaar.core.agent.SimpleAgent";

  /** Classname Property Definition **/
  public static final String PROP_CLASSNAME = "Classname";

  /** Classname Description Property Definition **/
  public static final String PROP_CLASSNAME_DESC = "Name of the Agent Class";
  
  /**
   * Creates a new <code>AgentBase</code> instance.
   *
   * @param name Name of the new Component
   * @param classname Classname for the agent
   */
  public AgentBase(String name, String classname) {
    super(name);
    this.classname = classname;
    createLogger();
    if(log.isDebugEnabled()) {
      log.debug("Creating Agent: " + name);
    }
    installListeners();
  }

  public AgentBase(String name) {
    this(name, AgentBase.DEFAULT_CLASS);
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Initialize all local properties
   *
   */
  public void initProperties() {
    Property p = addProperty(PROP_CLASSNAME, new String(classname));
    p.setToolTip(PROP_CLASSNAME_DESC);
  }

  /**
   * Adds any relevent <code>ComponentData</code> for this component.
   * This method does not modify any existing <code>ComponentData</code>
   * Warning: This expects to be handed itself
   *
   * @see ComponentData
   * @param data Pointer to the global <code>ComponentData</code>
   * @return an updated <code>ComponentData</code> object
   */
  public ComponentData addComponentData(ComponentData data) {
    if (data.getType() != ComponentData.AGENT) {
      if (log.isErrorEnabled()) {
	log.error("Got a non-Agent to be self: " + data);
      }
      // FIXME: Try to recover
      return data;
    }

    // When this was created we did agent.getShortName().
    // Is name the same thing?
    if (! data.getName().equals(this.getShortName())) {
      if (log.isErrorEnabled()) {
	log.error("Got an agent with wrong name to be self: " + data + ". My name is " + this.getShortName());
      }      
      // FIXME: Try to recover??
      return data;
    }

    data.setClassName(getAgentClassName());

    // I assume the society set the owner & parent
    // And I explicitly wont set any properties

    // Process AssetData
    Iterator iter = 
      ((Collection)getDescendentsOfClass(AssetComponent.class)).iterator();
    while(iter.hasNext()) {
      AssetComponent asset = (AssetComponent)iter.next();
      asset.addComponentData(data);
    }

    // Process all other components.
    iter = 
      ((Collection)getDescendentsOfClass(ContainerBase.class)).iterator();
    while(iter.hasNext()) {
      ContainerBase container = (ContainerBase)iter.next();
      if(container.getShortName().equals("Binders") ||
         container.getShortName().equals("Plugins") ||
         container.getShortName().equals("Other Components")) {
        for(int i=0; i < container.getChildCount(); i++) {
          BaseComponent base = (BaseComponent) container.getChild(i);
	  base.addComponentData(data);
        }
      }
    }
   
    return data;
  }

  /**
   * Gets the classname for this agent
   *
   * @return a <code>String</code> value
   */
  public String getAgentClassName() {
    Property p = getProperty(PROP_CLASSNAME);
    if (p != null) {
      Object o = p.getValue();
      if (o != null)
        return o.toString();
    }
    return DEFAULT_CLASS;
  }

  /**
   * Sets the classname for this agent
   *
   * @param aClass new agent classname
   */
  public void setAgentClassName(String aClass) {
    if (aClass == null)
      return;
    aClass = aClass.trim();
    Property p = getProperty(PROP_CLASSNAME);
    if (p != null) {
      Object o = p.getValue();
      if (o != null && o.equals(aClass))
	return;
      else
	p.setValue(aClass);
    } else {
      addProperty(PROP_CLASSNAME, aClass);
    }
  }

  /**
   * Tests equality for agents.
   * Agents are equal if they have the same short name.
   * @param o Object to test for equality 
   * @return returns true if the object is an AgentComponent with same name
   */
  public boolean equals(Object o) {
//     if (log.isDebugEnabled())
//       log.debug(this + ": AgentBase.equals with other: " + o);

    if (this == o)
      return true;

    if (o instanceof AgentComponent) {
      AgentComponent that = (AgentComponent)o;
      if (!this.getShortName().equals(that.getShortName())  ) {
	return false;
      }     
      return true;
    }
    return false;
  }

  /**
   * Has this agent been modified, such that a save would do something.
   *
   * @return a <code>boolean</code>, false if no save necessary
   */
  public boolean isModified() {
    return modified;
  }

  /**
   * Set the internal modified flag (returned by isModified)
   * and fire a modification event.
   */
  public void fireModification() {
    modified = true;
    super.fireModification();
  }

  // agents listen on all properties, because subcomponents
  // of agents don't have listeners on them

  private void installListeners() {
    addPropertiesListener(this);
    for (Iterator i = getPropertyNames(); i.hasNext(); ) {
      Property p = getProperty((CompositeName)i.next());
      p.addPropertyListener(myPropertyListener);
    }
  }

  public void propertyAdded(PropertyEvent e) {
    Property addedProperty = e.getProperty();
    setPropertyVisible(addedProperty, true);
    addedProperty.addPropertyListener(myPropertyListener);
    fireModification();
  }

  public void propertyRemoved(PropertyEvent e) {
    e.getProperty().removePropertyListener(myPropertyListener);
    fireModification();
  }

  PropertyListener myPropertyListener =
    new PropertyListener() {
        public void propertyValueChanged(PropertyEvent e) {
          fireModification();
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

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
    modified = false;
    installListeners();
  }

  class MyModificationListener implements ModificationListener, ConfigurableComponentListener {
    public void modified(ModificationEvent e) {
        fireModification();
    }
  }

  
} // end of AgentBase

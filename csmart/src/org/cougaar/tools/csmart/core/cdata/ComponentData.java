/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.core.cdata;

import java.io.Serializable;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.core.domain.DomainManager;

/** 
 * Interface that contains all data
 * associated with an UltraLog component.
 */
public interface ComponentData extends Serializable {

  /** Component Types **/
  String SOCIETY = "society";
  String RECIPE = "recipe";
  String ENCLAVE = "enclave";
  String HOST = "host";
  String AGENT = "agent";
  String NODE = "node";
  String NODEAGENT = "node agent";
  String PLUGIN = "plugin";
  String NODEBINDER = "node binder";
  String AGENTBINDER = "agent binder";
  // Shorter type? This is the insertion point
  // It gets added to _Agents_ (and possible NodeAgents)
  String DOMAIN = DomainManager.INSERTION_POINT + ".Domain";
  // Maybe one for arbitrary agent-level component: Node.AgentManager.Agent.Component?
  // Note must augment ComponentTypeProperty, logic in GenericComponentData
  // Also ComponentBase & SpecificInsertionRecipe & PopulateDb
  // And the various Agent*Components too probably
  
  /**
   * Gets the type of this component.  Component Types
   * are defined in this file.
   *
   * @return Component Type
   */
  String getType();

  /**
   * Sets the Type of this component.  
   * Basic component types are defined in the file
   * for convenience.
   *
   * @param type Type of the component.
   */
  void setType(String type);
 
  /**
   * Gets the name of the component.
   *
   * @return Name of this component.
   */
  String getName();
  
  /**
   * Sets the name of this component.
   *
   * @param name of this component.
   */
  void setName(String name);
  
  /** 
   * Gets the class for this component
   * (SimpleAgent for Agent components)
   *
   * @return Class name, including package
   */
  String getClassName();

  /**
   * Sets the class for this component.
   *
   * @param classname, including package.
   */
  void setClassName(String className);

  /**
   * Gets the Priority for this Component
   *
   * @see org.cougaar.core.component.ComponentDescription
   * @return The Components Priority
   */
  String getPriority();

  /**
   * Sets the Priority for this component.
   *
   * @see org.cougaar.core.component.ComponentDescription
   * @param priority Components Priority
   */
  void setPriority(String priority);

  /**
   * Sets the Priority for this component based
   * on onr of the predefined values in <code>ComponentDescription</code>
   *
   * @see org.cougaar.core.component.ComponentDescription
   * @param priority 
   */
  void setPriority(int priority);

  /**
   * Gets all children of this component.
   *
   * @return ComponentData[] of children.
   */
  ComponentData[] getChildren();

  /**
   * Sets all children of this component.
   *
   * @param ComponentData[] Array of all children
   */
  void setChildren(ComponentData[] child);

  /**
   * Adds a child component
   * @param ComponentData child component.
   */
  void addChild(ComponentData child);
  
  /**
   * Adds a child component at the given index.
   * all following children are shifted right.
   * @param int Index to add now child
   * @param ComponentData child component.
   */
  void addChild(int index, ComponentData child);

  /**
   * Sets a child component at the specified index, replacing the current value.
   *
   * @param index position to add new child component.
   * @param ComponentData child component.
   * @throws IndexOutOfBoundsException if the index is out of range
   *            (index &lt; 0 || index &gt; size()).
   */
  void setChild(int index, ComponentData child);

  /**
   * Get the index of the child component (using .equals to find it)
   * Return -1 if not present
   *
   * @param child a <code>ComponentData</code> to find by name
   * @return an <code>int</code> array index, -1 if not present
   */
  int getChildIndex(ComponentData child);
  
  /**
   * Returns count of all children.
   *
   * @return child count
   */
  int childCount();

  /**
   * Add a child to its default location:
   * Binders go before any agents / plugins.
   * Components go after other components of the same type.
   * Also, the given component replaces any existing component
   * of the same name.
   *
   * @param comp a <code>ComponentData</code> to add / update
   */
  void addChildDefaultLoc(ComponentData comp);
  
  /**
   * Gets all parameters associated with this component.
   *
   * @return Object[] Array of all parameters
   */
  Object[] getParameters();

  /**
   * Sets all parameters for this object.
   *
   * @param Object[] Array of parameters
   */
  void setParameters(Object[] params);
  
  /**
   * Adds a single parameter for this object.
   *
   * @param Object parameter for this component.
   */
  void addParameter(Object param);

  /**
   * Sets a Parameter at a specific index, replacing the current value
   *
   * @param index Index to set new parameter value
   * @param param New Parameter for this component.
   * @throws IndexOutOfBoundsException if the index is out of range
   *            (index &lt; 0 || index &gt; size()).
   */
  void setParameter(int index, Object param);

  Object getParameter(int index);

  /**
   * Returns a count of all parameters
   *
   * @return parameter count.
   */
  int parameterCount();

  /**
   * Returns the parent of this component.
   *
   * @return ComponentData Parent of this Component
   */
  ComponentData getParent();

  /**
   * Sets the parent of this component.
   *
   * @param ComponentData Parent of this component.
   */
  void setParent(ComponentData parent);

  /**
   * Gets the ConfigurableComponent associated with this
   * data object.
   *
   * @return ConfigurableComponent owner of this object
   */
  ConfigurableComponent getOwner();

  /**
   * Sets the Configurable Component that is associated with
   * this object.
   *
   * @param ConfigurableComponent Owner
   */
  void setOwner(ConfigurableComponent owner);

  /**
   * Gets all leaf components of this component.
   * A leaf component is any component required by
   * this component to store auxillary data.
   *
   * @return LeafComponentData[] array of all leaf components.
   */
  LeafComponentData[] getLeafComponents();

  /**
   * Sets all leaf components for this component.
   *
   * @param LeafComponentData[] Array of all leaf components
   */
  void setLeafComponents(LeafComponentData[] leaves);

  /**
   * Sets a leaf component at the given index, replacting the current component
   *
   * @param index Index of the leaf component
   * @param LeafComponentData new leaf for this component.
   * @throws IndexOutOfBoundsException if the index is out of range
   *            (index &lt; 0 || index &gt; size()).
   */
  void setLeafComponent(int index, LeafComponentData leaf) 
    throws IndexOutOfBoundsException;

  /**
   * Adds a single leaf component to this component
   * 
   * @param LeafComponentData new leaf component 
   */
  void addLeafComponent(LeafComponentData leaf);
  

  /**
   * Returns a count of all leaf components.
   *
   * @return leaf count
   */
  int leafCount();


  /**
   * Returns all asset data for this agent.
   * Asset data is used to construct the
   * Prototype-ini files.
   *
   * @see org.cougaar.tools.csmart.core.cdata.AgentAssetData
   * @return Asset Data for this agent
   */
  AgentAssetData getAgentAssetData();

  /**
   * Adds all asset data for this agent.
   *
   * @see org.cougaar.tools.csmart.core.cdata.AgentAssetData
   * @param data Full AgentAssetData object.
   */
  void addAgentAssetData(AgentAssetData data);


  // I think everything below this point will be removed.
  // It is now part of the AgentAssetData object.

  /**
   * Returns all TimePhasedData objects known to this
   * component.
   *
   * @return array of all TimePhasedData objects.
   */
  TimePhasedData[] getTimePhasedData();
  
  /**
   * Sets all TimePhasedData objects for this component.
   *
   * @param data array of TimePhasedData objects
   */
  void setTimePhasedData(TimePhasedData[] data);

  /**
   * Sets a TimePhasedData object to this component at
   * the given index, replacing the current value
   *
   * @param index Location in array to replace TimePhased object
   * @param data TimePhased object to add to component
   */
  void setTimePhasedData(int index, TimePhasedData data);

  /**
   * Adds a TimePhasedData object to this component.
   *
   * @param data TimePhased object to add to component
   */
  void addTimePhasedData(TimePhasedData data);

  /**
   * Returns a count of all TimePhasedData objects
   *
   * @return TimePhased Data Count
   */
  int timePhasedCount();

  /**
   * Gets the ALIB_ID for this Component.
   *
   * @return ALIB_ID or null if none exist
   */
  String getAlibID();

  /**
   * Sets the ALIB_ID value for this Component Property
   * This is for Database Experiments, all others
   * should set this to null.
   *
   * @param alibID a <code>String</code> value
   */
  void setAlibID(String alibID);

  /**
   * Gets the LIB_ID for this Component.
   *
   * @return LIB_ID or null if none exist
   */
  String getLibID();

  /**
   * Sets the LIB_ID value for this Component Property
   * This is for Database Experiments, all others
   * should set this to null.
   *
   * @param libID a <code>String</code> value
   */
  void setLibID(String libID);

} // end of ComponentData.java









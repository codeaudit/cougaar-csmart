/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.component;

/** 
 * Interface that contains all data
 * associated with a component.
 */
public interface ComponentData {

  /** Component Types **/
  String SOCIETY = "Society";
  String AGENT = "cluster";
  String NODE = "Node";
  String PLUGIN = "plugin";
  String BINDER = "Binder";
  String SERVICE = "Service";
  
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
   * @param The name of this component.
   */
  void setName(String name);
  
  /** 
   * Gets the class for this component
   * (ClusterImpl for Agent components)
   *
   * @return Class name, including package
   */
  String getClassName();

  /**
   * Sets the class for this component.
   *
   * @param String class name, including package.
   */
  void setClassName(String className);

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
   * Sets a child component at the specified index, replacing the current value.
   *
   * @param index position to add new child component.
   * @param ComponentData child component.
   * @throws IndexOutOfBoundsException if the index is out of range
   *            (index &lt; 0 || index &gt; size()).
   */
  void setChild(int index, ComponentData child);

  /**
   * Returns count of all children.
   *
   * @return child count
   */
  int childCount();

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
  void setLeafComponent(int index, LeafComponentData leaf);

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

} // end of ComponentData.java









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
  public static final String SOCIETY = "Society";
  public static final String AGENT = "Agent";
  public static final String NODE = "Node";
  public static final String PLUGIN = "Plugin";
  public static final String BINDER = "Binder";
  public static final String SERVICE = "Service";
  
  /**
   * Gets the type of this component.  Component Types
   * are defined in this file.
   *
   * @return Component Type
   */
  public String getType();

  /**
   * Sets the Type of this component.  
   * Basic component types are defined in the file
   * for convenience.
   *
   * @param type Type of the component.
   */
  public void setType(String type);
 
  /**
   * Gets the name of the component.
   *
   * @return Name of this component.
   */
  public String getName();
  
  /**
   * Sets the name of this component.
   *
   * @param The name of this component.
   */
  public void setName(String name);
  
  /** Class type of the component **/
  /** ie: ClusterImpl for Agent **/

  /** 
   * Gets the class for this component
   * (ClusterImpl for Agent components)
   *
   * @return Class name, including package
   */
  public String getClassName();

  /**
   * Sets the class for this component.
   *
   * @param String class name, including package.
   */
  public void setClassName(String className);

  /**
   * Gets all children of this component.
   *
   * @return ComponentData[] of children.
   */
  public ComponentData[] getChildren();

  /**
   * Sets all children of this component.
   *
   * @param ComponentData[] Array of all children
   */
  public void setChildren(ComponentData[] child);

  /**
   * Adds a child component
   * @param ComponentData child component.
   */
  public void addChild(ComponentData child);
  
  /**
   * Adds a child component at the specified index.
   *
   * @param index position to add new child component.
   * @param ComponentData child component.
   * @throws IndexOutOfBoundsException if the index is out of range
   *            (index &lt; 0 || index &gt; size()).
   */
  public void addChild(int index, ComponentData child);

  /**
   * Returns count of all children.
   *
   * @return child count
   */
  public int childCount();

  /**
   * Gets all parameters associated with this component.
   *
   * @return Object[] Array of all parameters
   */
  public Object[] getParameters();

  /**
   * Sets all parameters for this object.
   *
   * @param Object[] Array of parameters
   */
  public void setParameters(Object[] params);
  
  /**
   * Adds a single parameter for this object.
   *
   * @param Object parameter for this component.
   */
  public void addParameter(Object param);

  /**
   * Adds a Parameter at a specific index.
   *
   * @param index Index to add new parameter
   * @param param New Parameter for this component.
   * @throws IndexOutOfBoundsException if the index is out of range
   *            (index &lt; 0 || index &gt; size()).
   */
  public void addParameter(int index, Object param);

  /**
   * Returns a count of all parameters
   *
   * @return parameter count.
   */
  public int parameterCount();

  /**
   * Returns the parent of this component.
   *
   * @return ConfigurableComponent Parent of this Component
   */
  public ConfigurableComponent getParent();

  /**
   * Sets the parent of this component.
   *
   * @param ConfigurableComponent Parent of this component.
   */
  public void setParent(ConfigurableComponent parent);

  /**
   * Gets the ConfigurableComponent associated with this
   * data object.
   *
   * @return ConfigurableComponent owner of this object
   */
  public ConfigurableComponent getOwner();

  /**
   * Sets the Configurable Component that is associated with
   * this object.
   *
   * @param ConfigurableComponent Owner
   */
  public void setOwner(ConfigurableComponent owner);

  /**
   * Gets all leaf components of this component.
   * A leaf component is any component required by
   * this component to store auxillary data.
   *
   * @return LeafComponentData[] array of all leaf components.
   */
  public LeafComponentData[] getLeafComponents();

  /**
   * Sets all leaf components for this component.
   *
   * @param LeafComponentData[] Array of all leaf components
   */
  public void setLeafComponents(LeafComponentData[] leaves);

  /**
   * Adds a leaf component at the given index.
   *
   * @param index Index of the leaf component
   * @param LeafComponentData new leaf for this component.
   * @throws IndexOutOfBoundsException if the index is out of range
   *            (index &lt; 0 || index &gt; size()).
   */
  public void addLeafComponent(int index, LeafComponentData leaf);

  /**
   * Adds a single leaf component to this component
   * 
   * @param LeafComponentData new leaf component 
   */
  public void addLeafComponent(LeafComponentData leaf);
  

  /**
   * Returns a count of all leaf components.
   *
   * @return leaf count
   */
  public int leafCount();
}









/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.component;

import java.util.ArrayList;

/**
 * Generic Component Data.
 * All components, except for Agent components can use
 * the generic component data structure.
 *
 * @see ComponentData for docs.
 */
public class GenericComponentData implements ComponentData {
  private String type = null;
  private String name = null;
  private String className = null;
  private ArrayList children = null;
  private ArrayList parameters = null;
  private ConfigurableComponent parent = null;
  private ConfigurableComponent owner = null;
  private ArrayList leafComponents = null;

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getClassName() {
    return this.className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public ComponentData[] getChildren() {
    return (ComponentData[])children.toArray();
  }

  public void setChildren(ComponentData[] child) {
    for(int i=0; i < child.length; i++) {
      this.children.add(child[i]);
    }
  }

  public void addChild(ComponentData child) {
    this.children.add(child);
  }

  public void addChild(int index, ComponentData child) {
    this.children.add(index, child);
  }

  public int childCount() {
    return children.size();
  }

  public Object[] getParameters() {
    return parameters.toArray();
  }

  public void setParameters(Object[] params) {
    for(int i=0; i < params.length; i++) {
      this.parameters.add(params[i]);
    }    
  }

  public void addParameter(Object param) {
    this.parameters.add(param);
  }

  public void addParameter(int index, Object param) {
    this.parameters.add(index, param);
  }

  public int parameterCount() {
    return parameters.size();
  }

  public ConfigurableComponent getParent() {
    return parent;
  }

  public void setParent(ConfigurableComponent parent) {
    this.parent = parent;
  }

  public ConfigurableComponent getOwner() {
    return owner;
  }

  public void setOwner(ConfigurableComponent owner) {
    this.owner = owner;
  }

  public LeafComponentData[] getLeafComponents() {
    return (LeafComponentData[]) leafComponents.toArray();
  }

  public void setLeafComponents(LeafComponentData[] leaves) {
    for(int i = 0 ; i < leaves.length; i++) {
      leafComponents.add(leaves[i]);
    }
  }

  public void addLeafComponent(LeafComponentData leaf) {
    leafComponents.add(leaf);
  }

  public void addLeafComponent(int index, LeafComponentData leaf) {
    leafComponents.add(index, leaf);
  }

  public int leafCount() {
    return leafComponents.size();
  }
}

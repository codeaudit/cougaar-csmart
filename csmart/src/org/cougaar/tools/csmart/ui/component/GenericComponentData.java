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

import java.util.ArrayList;

/**
 * Generic Component Data.
 * All components, except for Agent components can use
 * the generic component data structure.
 *
 * @see ComponentData for docs.
 */
public class GenericComponentData implements ComponentData {
  protected String type = null;
  private String name = null;
  protected String className = null;
  private ArrayList children = null;
  private ArrayList parameters = null;
  private ComponentData parent = null;
  transient private ConfigurableComponent owner = null;
  private ArrayList leafComponents = null;
  private ArrayList timePhasedData = null;
  private AgentAssetData assetData = null;

  public GenericComponentData() {
    children = new ArrayList();
    parameters = new ArrayList();
    leafComponents = new ArrayList();
    timePhasedData = new ArrayList();
  }

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
    return (ComponentData[])children.toArray(new ComponentData[children.size()]);
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

  public void setChild(int index, ComponentData child) {
    this.children.set(index, child);
  }

  public int childCount() {
    return children.size();
  }

  public Object[] getParameters() {
    return parameters.toArray();
  }

  public void setParameters(Object[] params) {
    this.parameters.clear();
    for(int i=0; i < params.length; i++) {
      this.parameters.add(params[i]);
    }    
  }

  public void addParameter(Object param) {
    this.parameters.add(param);
  }

  public void setParameter(int index, Object param) {
    this.parameters.set(index, param);
  }

  public int parameterCount() {
    return parameters.size();
  }

  public ComponentData getParent() {
    return parent;
  }

  public void setParent(ComponentData parent) {
    this.parent = parent;
  }

  public ConfigurableComponent getOwner() {
    return owner;
  }

  public void setOwner(ConfigurableComponent owner) {
    this.owner = owner;
  }

  public LeafComponentData[] getLeafComponents() {
    return (LeafComponentData[]) leafComponents.toArray(new LeafComponentData[leafComponents.size()]);
  }

  public void setLeafComponents(LeafComponentData[] leaves) {
    leafComponents.clear();
    for(int i = 0 ; i < leaves.length; i++) {
      leafComponents.add(leaves[i]);
    }
  }

  public void addLeafComponent(LeafComponentData leaf) {
    leafComponents.add(leaf);
  }

  public void setLeafComponent(int index, LeafComponentData leaf) {
    leafComponents.set(index, leaf);
  }

  public int leafCount() {
    return leafComponents.size();
  }

  public AgentAssetData getAgentAssetData() {
    return assetData;
  }

  public void addAgentAssetData(AgentAssetData data) {
    this.assetData = data;
  }

  // For testing, dump out the tree from here down
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(this.getClass().toString() + ": ");
    buf.append("Name: " + getName());
    buf.append(", Type: " + getType());
    buf.append(", Class: " + getClassName());
    if (owner != null) {
      buf.append(", Owner: " + getOwner());
    }
    buf.append(", LeafCount: " + leafCount());
    buf.append(", ChildCount: " + childCount());
    ComponentData[] children = getChildren();
    for (int i = 0; i < childCount(); i++) {
      buf.append(", Child[" + i + "]: \n" + children[i] + "\n");
    }
    return buf.toString();
  }

  public TimePhasedData[] getTimePhasedData() {
    return (TimePhasedData[]) timePhasedData.toArray(new TimePhasedData[timePhasedData.size()]);
  }

  public void setTimePhasedData(TimePhasedData[] data) {
    timePhasedData.clear();
    for(int i = 0 ; i < data.length; i++) {
      timePhasedData.add(data[i]);
    }
  }

  public void setTimePhasedData(int index, TimePhasedData data) {
    timePhasedData.set(index, data);
  }

  public void addTimePhasedData(TimePhasedData data) {
    timePhasedData.add(data);
  }

  public int timePhasedCount() {
    return timePhasedData.size();
  }

}

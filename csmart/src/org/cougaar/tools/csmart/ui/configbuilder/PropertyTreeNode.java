/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.configbuilder;

import org.cougaar.tools.csmart.core.property.name.CompositeName;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class PropertyTreeNode extends DefaultMutableTreeNode {
  boolean isLeaf = true;
  SortedSet myPropertyNames = new TreeSet();
  CompositeName myName;

  public PropertyTreeNode(CompositeName myName) {
    this.myName = myName;
  }

  public PropertyTreeNode(CompositeName myName, List propertyNames) {
    this(myName);
    myPropertyNames.addAll(propertyNames);
  }

  public int getPropertyCount() {
    return myPropertyNames.size();
  }

  public boolean addPropertyName(CompositeName name) {
    return myPropertyNames.add(name);
  }

  public boolean removePropertyName(CompositeName name) {
    return myPropertyNames.remove(name);
  }

  public Set getPropertyNames() {
    return myPropertyNames;
  }

  public String toString() {
    return myName.last().toString();
  }

  public CompositeName getName() {
    return myName;
  }

  public boolean isLeaf() {
    if (getChildCount() > 0)
      return false;
    return isLeaf; // if no children, the flag overrides
  }

  public void setLeaf(boolean isLeaf) {
    this.isLeaf = isLeaf;
  }

}

/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.builder;

import java.util.*;
import javax.swing.tree.DefaultMutableTreeNode;

import org.cougaar.tools.csmart.ui.component.*;

public class PropertyTreeNode extends DefaultMutableTreeNode {
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
}

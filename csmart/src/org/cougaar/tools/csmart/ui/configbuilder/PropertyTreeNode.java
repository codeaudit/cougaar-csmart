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

/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.tree;

import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ConsoleTreeObject implements Serializable {
  String name;
  private boolean root = false;
  private Class allowedClass = null;
  BaseComponent component = null;
  private transient Logger log;

  public ConsoleTreeObject(BaseComponent component) {
    createLogger();
    name = component.toString();
    this.component = component;
  }

  /**
   * Constructor for root.
   * @param the name for the root node
   * @param allowed class of the children
   */

  public ConsoleTreeObject(String name, String allowedClassName) {
    createLogger();
    this.name = name;
    try {
      allowedClass = Class.forName(allowedClassName);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
    }
    root = true;
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    if (component != null) 
      component.setName(name);
  }
  
  public String toString() {
    if (component == null)
      return name;
    return component.toString();
  }

  public BaseComponent getComponent() {
    return component;
  }

  public boolean isRoot() {
    return root;
  }

  public boolean isHost() {
    return component instanceof HostComponent;
  }

  public boolean isNode() {
    return component instanceof NodeComponent;
  }

  public boolean isAgent() {
    return component instanceof AgentComponent;
  }

  public static boolean flavorEquals(CSMARTDataFlavor flavor1,
                                     CSMARTDataFlavor flavor2)
  {
    if (flavor1.equals(flavor2)) {
      String c1 = flavor1.getUserObjectClassName();
      String c2 = flavor2.getUserObjectClassName();
      if (c1 != null) return c1.equals(c2);
    }
    return false;
  }

  public boolean allowsDataFlavor(DataFlavor flavor) {
    if (flavor instanceof CSMARTDataFlavor) {
      CSMARTDataFlavor cflavor = (CSMARTDataFlavor) flavor;
      // if dropping an agent
      // if the drop site is a Node component or 
      // the drop site is the root of a tree that allows agents
      // then its ok
      if (flavorEquals(cflavor, ConsoleDNDTree.agentFlavor)) {
        return isNode() || isRoot() && allowedClass == AgentComponent.class;
      }
      // same as above, except dropping multiple agents
      if (flavorEquals(cflavor, ConsoleDNDTree.agentArrayFlavor)) {
        return isNode() || isRoot() && allowedClass == AgentComponent.class;
      }
      // if dropping a Node component
      // if the drop site is a Host component or
      // the drop site is the root of a tree that allows node components
      // then its ok
      if (flavorEquals(cflavor, ConsoleDNDTree.nodeFlavor)) {
        return isHost() || isRoot() && allowedClass == NodeComponent.class;
      }
      // same as above, except dropping multiple nodes
      if (flavorEquals(cflavor, ConsoleDNDTree.nodeArrayFlavor)) {
        return isHost() || isRoot() && allowedClass == NodeComponent.class;
      }
      // if dropping a Host component
      // if the drop site is the root of a tree that allows host components
      // then its ok
      if (flavorEquals(cflavor, ConsoleDNDTree.hostFlavor)) {
        return isRoot() && allowedClass == HostComponent.class;
      }
    }
    return false;
  }

  private void writeObject(ObjectOutputStream os) throws IOException {
    throw new IOException("Attempt to serialize ConsoleTreeObject, check DataFlavor");
  }

  private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
    createLogger();
  }
}


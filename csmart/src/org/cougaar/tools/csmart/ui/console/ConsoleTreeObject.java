/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.console;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.cougaar.tools.csmart.ui.component.*;
import java.awt.datatransfer.DataFlavor;

public class ConsoleTreeObject implements Serializable {
  String name;
  private boolean root = false;
  private Class allowedClass = null;
  ComponentProperties component;

  public ConsoleTreeObject(ComponentProperties component) {
    name = component.toString();
    this.component = component;
  }

  /**
   * Constructor for root.
   * @param the name for the root node
   * @param allowed class of the children
   */

  public ConsoleTreeObject(String name, String allowedClassName) {
    this.name = name;
    try {
      allowedClass = Class.forName(allowedClassName);
    } catch (Exception e) {
      e.printStackTrace();
    }
    root = true;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    if (component != null) {
      ((ConfigurableComponent)component).setName(name);
    }
  }
  
  public String toString() {
    if (component == null)
      return name;
    return component.toString();
  }

  public ComponentProperties getComponent() {
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
      if (flavorEquals(cflavor, ConsoleDNDTree.agentFlavor)) {
        return isNode() || isRoot() && allowedClass == AgentComponent.class;
      }
      if (flavorEquals(cflavor, ConsoleDNDTree.agentArrayFlavor)) {
        return isNode() || isRoot() && allowedClass == AgentComponent.class;
      }
      if (flavorEquals(cflavor, ConsoleDNDTree.nodeFlavor)) {
        return isHost() || isRoot() && allowedClass == NodeComponent.class;
      }
      if (flavorEquals(cflavor, ConsoleDNDTree.nodeArrayFlavor)) {
        return isHost() || isRoot() && allowedClass == NodeComponent.class;
      }
      if (flavorEquals(cflavor, ConsoleDNDTree.hostFlavor)) {
        return isRoot() && allowedClass == HostComponent.class;
      }
    }
    return false;
  }

  private void writeObject(ObjectOutputStream os) throws IOException {
    throw new IOException("Attempt to serialize ConsoleTreeObject, check DataFlavor");
  }
}


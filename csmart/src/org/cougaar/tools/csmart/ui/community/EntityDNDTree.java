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

package org.cougaar.tools.csmart.ui.community;

import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.ui.tree.*;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * Provides method definitions for abstract methods in DNDTree.
 * Encapsulates all information specific to the drag-and-drop
 * trees in the Console tool, leaving DNDTree as the generic
 * drag-and-drop tree class.
 */

public class EntityDNDTree extends DNDTree {

  private transient Logger log;

  protected static CSMARTDataFlavor agentFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         AgentComponent.class,
                         EntityDNDTree.class,
                         "CSMART Agent");
  protected static CSMARTDataFlavor hostFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         HostComponent.class,
                         EntityDNDTree.class,
                         "CSMART Host");
  protected static CSMARTDataFlavor agentArrayFlavor =
    new CSMARTDataFlavor(DMTNArray.class,
                         AgentComponent.class,
                         EntityDNDTree.class,
                         "CSMART Agent");
  protected static CSMARTDataFlavor hostArrayFlavor =
    new CSMARTDataFlavor(DMTNArray.class,
                         HostComponent.class,
                         EntityDNDTree.class,
                         "CSMART Host");

  public EntityDNDTree(DefaultTreeModel model) {
    super(model);
    setToolTipText("");
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public String getToolTipText(MouseEvent evt) {
    if (getRowForLocation(evt.getX(), evt.getY()) == -1) return null;    
    TreePath path = getPathForLocation(evt.getX(), evt.getY());
    DefaultMutableTreeNode node =
      (DefaultMutableTreeNode)path.getLastPathComponent();
    Object o = node.getUserObject();
    if (o != null && o instanceof CommunityTreeObject)
      return ((CommunityTreeObject)o).getToolTip();
    else
      return "";
  }

  protected boolean supportsMultiDrag() {
    return true;
  }

  /**
   * Return true if object can be dragged and false otherwise.
   * Anything but roots can be dragged.
   */

  public boolean isDraggable(Object selected) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) selected;
    return (node.getParent() != null);
  }

  /**
   * Make a draggable object from a DefaultMutableTreeNode.
   */

  public Transferable makeDraggableObject(Object o) {
    if (o instanceof DefaultMutableTreeNode) {
      return new EntityTreeObjectTransferable((DefaultMutableTreeNode) o);
    } else if (o instanceof DMTNArray) {
      DMTNArray nodeArray = (DMTNArray)o;
      CommunityTreeObject userObject = 
        (CommunityTreeObject)nodeArray.nodes[0].getUserObject();
      if (userObject == null)
        throw new IllegalArgumentException("null user object");
      CSMARTDataFlavor[] theFlavors = null;
      if (userObject.isAgent())
        theFlavors = new CSMARTDataFlavor[] { agentArrayFlavor };
      else if (userObject.isHost())
        theFlavors = new CSMARTDataFlavor[] { hostArrayFlavor };
      return new CommunityArrayTransferable(nodeArray, theFlavors);
    }
    return null;
  }

  /**
   * You can't drop anything in the host-node-agent trees.
   */

  protected int isDroppable(DataFlavor[] flavors, 
                            DefaultMutableTreeNode target) {
    return DnDConstants.ACTION_NONE;
  }

 /**
   * Adds dropped object to this tree; called by drop method.
   * Object is either a CommunityTreeObject or a DefaultMutableTreeNode
   * depending on whether the user is dragging a leaf or a node.
   * Returns true if successful.
   */
   
  public int addElement(Transferable transferable, 
                        DefaultMutableTreeNode target,
                        DefaultMutableTreeNode before) {
    // this will never be called
    // because you can't drop anything in these trees
    return DnDConstants.ACTION_NONE;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}


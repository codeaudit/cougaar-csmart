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

public class CommunityDNDTree extends DNDTree {

  private transient Logger log;

  protected static CSMARTDataFlavor agentFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         AgentComponent.class,
                         CommunityDNDTree.class,
                         "CSMART Agent");
  protected static CSMARTDataFlavor nodeFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         NodeComponent.class,
                         CommunityDNDTree.class,
                         "CSMART Node");
  protected static CSMARTDataFlavor hostFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         HostComponent.class,
                         CommunityDNDTree.class,
                         "CSMART Host");
  protected static CSMARTDataFlavor communityFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         CommunityTreeObject.class,
                         CommunityDNDTree.class,
                         "CSMART Community");
  protected static CSMARTDataFlavor agentArrayFlavor =
    new CSMARTDataFlavor(DMTNArray.class,
                         AgentComponent.class,
                         CommunityDNDTree.class,
                         "CSMART Agent");
  protected static CSMARTDataFlavor nodeArrayFlavor =
    new CSMARTDataFlavor(DMTNArray.class,
                         NodeComponent.class,
                         CommunityDNDTree.class,
                         "CSMART Node");
  protected static CSMARTDataFlavor hostArrayFlavor =
    new CSMARTDataFlavor(DMTNArray.class,
                         HostComponent.class,
                         CommunityDNDTree.class,
                         "CSMART Host");
  protected static CSMARTDataFlavor communityArrayFlavor =
    new CSMARTDataFlavor(DMTNArray.class,
                         CommunityTreeObject.class,
                         CommunityDNDTree.class,
                         "CSMART Community");

  public CommunityDNDTree(DefaultTreeModel model) {
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

  protected DefaultMutableTreeNode addNode(DefaultMutableTreeNode node,
                                           String name, String type) {
    DefaultTreeModel model = (DefaultTreeModel)getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
    if (node == null) {
      root.removeAllChildren();
      model.nodeStructureChanged(root);
      node = root;
    }
    DefaultMutableTreeNode newNode =
      new DefaultMutableTreeNode(new CommunityTreeObject(name, type),
                                 !type.equals("Agent"));
    model.insertNodeInto(newNode, node, node.getChildCount());
    // expand tree to display new node
    scrollPathToVisible(new TreePath(newNode.getPath()));
    // expand tree
//      Enumeration treeNodes = root.depthFirstEnumeration();
//      while (treeNodes.hasMoreElements()) {
//        DefaultMutableTreeNode tmpNode = 
//  	(DefaultMutableTreeNode)treeNodes.nextElement();
//        expandPath(new TreePath(tmpNode.getPath()));
//      }
    return newNode;
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
      return new CommunityTreeObjectTransferable((DefaultMutableTreeNode) o);
    } else if (o instanceof DMTNArray) {
      DMTNArray nodeArray = (DMTNArray)o;
      CommunityTreeObject userObject = 
        (CommunityTreeObject)nodeArray.nodes[0].getUserObject();
      if (userObject == null)
        throw new IllegalArgumentException("null user object");
      CSMARTDataFlavor[] theFlavors = null;
      if (userObject.isCommunity())
        theFlavors = new CSMARTDataFlavor[] { communityArrayFlavor };
      else if (userObject.isHost())
        theFlavors = new CSMARTDataFlavor[] { hostArrayFlavor };
      else if (userObject.isNode())
        theFlavors = new CSMARTDataFlavor[] { nodeArrayFlavor };
      else if (userObject.isAgent())
        theFlavors = new CSMARTDataFlavor[] { agentArrayFlavor };
      return new CommunityArrayTransferable(nodeArray, theFlavors);
    }
    return null;
  }

  /**
   * Uses CommunityTreeObject class to enforce dropping rules.
   */

  protected int isDroppable(DataFlavor[] flavors, 
                            DefaultMutableTreeNode target) {
    Object userObject = target.getUserObject();
    if (userObject instanceof CommunityTreeObject) {
      CommunityTreeObject cto = (CommunityTreeObject) userObject;
      for (int i = 0; i < flavors.length; i++) {
        if (cto.allowsDataFlavor(flavors[i])) {
          return testFlavors(flavors, (CSMARTDataFlavor)flavors[i]);
        }
      }
    }
    return DnDConstants.ACTION_NONE;
  }

  // if the move is allowed
  // returns ACTION_MOVE if the object is being moved in the same tree
  // and ACTION_COPY if the object is being moved between trees

  private int testFlavors(DataFlavor[] possibleFlavors, 
                          CSMARTDataFlavor testFlavor) {
    for (int i = 0; i < possibleFlavors.length; i++) {
      DataFlavor flavor = possibleFlavors[i];
      if (flavor instanceof CSMARTDataFlavor) {
        CSMARTDataFlavor cflavor = (CSMARTDataFlavor) flavor;
        if (cflavor.equals(testFlavor)) {
          if (getClass().getName().equals(cflavor.getSourceClassName())) {
            return DnDConstants.ACTION_MOVE;
          } else {
            return DnDConstants.ACTION_COPY;
          }
        }
      }
    }
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
                        DefaultMutableTreeNode before)
  {
    DataFlavor[] flavors = transferable.getTransferDataFlavors();
    int action = isDroppable(flavors, target);
    if (action == DnDConstants.ACTION_NONE) {
      return action;
    }
    Object data = null;
    try {
      data = 
        transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
      return DnDConstants.ACTION_NONE;
    }
    if (data instanceof DMTNArray) {
      DMTNArray nodes = (DMTNArray) data;
      for (int i = 0; i < nodes.nodes.length; i++) {
        if (!addElement(nodes.nodes[i], target, before))
          return DnDConstants.ACTION_NONE;
      }
    } else {
      if (!addElement((DefaultMutableTreeNode) data, target, before))
        return DnDConstants.ACTION_NONE;
    }
    return action;
  }

  // add an element by getting the user object out of the source
  // and creating a new tree node for it
  // do the same thing for all the node's descendants
  private boolean addElement(DefaultMutableTreeNode source,
                             DefaultMutableTreeNode target,
                             DefaultMutableTreeNode before) {
    // disallow move if source is an ancestor of target
    if (target.isNodeAncestor(source))
      return false;
    DefaultMutableTreeNode newNode = copyNode(source);
    int ix = target.getChildCount(); // Drop at end by default
    DefaultTreeModel model = (DefaultTreeModel) getModel();
    if (before != null)        // If before specified, put it there.
      ix = model.getIndexOfChild(target, before);
    model.insertNodeInto(newNode, target, ix);
    scrollPathToVisible(new TreePath(newNode.getPath()));
    return true;
  }

  private DefaultMutableTreeNode copyNode(DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode =
      new DefaultMutableTreeNode(node.getUserObject(), 
                                 !((CommunityTreeObject)node.getUserObject()).isAgent());
    DefaultTreeModel model = (DefaultTreeModel)getModel();
    int nChildren = node.getChildCount();
    for (int i=0; i < nChildren; i++) {
      //      newNode.add(copyNode((DefaultMutableTreeNode)node.getChildAt(i)));
      DefaultMutableTreeNode newChildNode =
        copyNode((DefaultMutableTreeNode)node.getChildAt(i));
      model.insertNodeInto(newChildNode, newNode, i);
    }
    return newNode;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}


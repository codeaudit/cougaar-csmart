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
    // expand branch below new node and scroll to display new node
    expandBranch(newNode);
    scrollPathToVisible(new TreePath(newNode.getPath()));
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
      //      else if (userObject.isNode())
      //        theFlavors = new CSMARTDataFlavor[] { nodeArrayFlavor };
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

  /** 
   * Add an element by getting the user object out of the source
   * and creating a new tree node for it.
   */

  private boolean addElement(DefaultMutableTreeNode source,
                             DefaultMutableTreeNode target,
                             DefaultMutableTreeNode before) {
    // disallow move if source is an ancestor of target
    if (target.isNodeAncestor(source)) {
      return false;
    }
    DefaultTreeModel model = (DefaultTreeModel) getModel();
    // drop new node at end by default, unless before target is specified
    int ix = target.getChildCount();
    if (before != null)
      ix = model.getIndexOfChild(target, before);
    CommunityTreeObject cto = (CommunityTreeObject)source.getUserObject();
    DefaultMutableTreeNode newNode =
      new DefaultMutableTreeNode(cto, cto.allowsChildren());
    if (isDuplicate(newNode, target) || isInCommunity(newNode, target)) {
      return false;
    }
    model.insertNodeInto(newNode, target, ix);
    // copy the source node's descendants, recursively
    copyChildren(source, newNode);
    // expand branch below new node and scroll to display new node
    expandBranch(newNode);
    scrollPathToVisible(new TreePath(newNode.getPath()));
    return true;
  }

  /**
   * Add an element by getting the user object out of the source
   * and creating a new tree node for it; 
   * recurse for all the node's descendants;
   * nodes must be added "top-down" in order for the model listener
   * to correctly add them to the database.
   * Reject duplicates.  Sibling nodes and nodes in the same community
   * must be unique.
   */
  private void copyChildren(DefaultMutableTreeNode oldNode,
                            DefaultMutableTreeNode newNode) {
    DefaultTreeModel model = (DefaultTreeModel)getModel();
    int nChildren = oldNode.getChildCount();
    for (int i=0; i < nChildren; i++) {
      DefaultMutableTreeNode oldChildNode = 
        (DefaultMutableTreeNode)oldNode.getChildAt(i);
      CommunityTreeObject cto = 
        (CommunityTreeObject)oldChildNode.getUserObject();
      DefaultMutableTreeNode newChildNode =
        new DefaultMutableTreeNode(cto, cto.allowsChildren());
      if (isDuplicate(newChildNode, newNode) ||
          isInCommunity(newChildNode, newNode))
        continue;
      model.insertNodeInto(newChildNode, newNode, newNode.getChildCount());
      copyChildren(oldChildNode, newChildNode);
    }
  }

  /**
   * Return true if the given node is a duplicate of a node in the parent,
   * i.e. has the same label.
   */
  private boolean isDuplicate(DefaultMutableTreeNode node,
                              DefaultMutableTreeNode parent) {
    String s = node.getUserObject().toString();
    int nChildren = parent.getChildCount();
    for (int i = 0; i < nChildren; i++)
      if (((DefaultMutableTreeNode)parent.getChildAt(i)).getUserObject().toString().equals(s))
        return true;
    return false;
  }

  /**
   * Return true if the given node is already in the community;
   * community is determined by tracing up the tree from the parent
   * until the community is found.
   */
  private boolean isInCommunity(DefaultMutableTreeNode node,
                                DefaultMutableTreeNode parent) {
    // find the community starting with parent
    DefaultMutableTreeNode communityNode = null;
    while (parent != null) {
      CommunityTreeObject cto = (CommunityTreeObject)parent.getUserObject();
      if (cto.isCommunity()) {
        communityNode = parent;
        break;
      }
      parent = (DefaultMutableTreeNode)parent.getParent();
    }
    if (communityNode == null)
      return false;
    String nodeName = ((CommunityTreeObject)node.getUserObject()).toString();
    // get the descendants of the community
    // and make sure they don't include this node
    // TODO: you can't move a node within a community; is this a problem?
    return isNodeNameInCommunity(communityNode, nodeName);
  }

  /**
   * Return true if the node name is in the community starting at node.
   */
  private boolean isNodeNameInCommunity(DefaultMutableTreeNode node, 
                                        String nodeName) {
    int nChildren = node.getChildCount();
    for (int i = 0; i < nChildren; i++) {
      DefaultMutableTreeNode childNode = 
        (DefaultMutableTreeNode)node.getChildAt(i);
      CommunityTreeObject cto = (CommunityTreeObject)childNode.getUserObject();
      if (cto.toString().equals(nodeName)) {
        return true;
      } else {
        if (childNode.getChildCount() != 0 && !cto.isCommunity()) {
          boolean result = isNodeNameInCommunity(childNode, nodeName);
          if (result)
            return true;
        }
      }
    }
    return false;
  }

  // expand the branch from the specified node down
  private void expandBranch(DefaultMutableTreeNode node) {
    Enumeration nodes = node.depthFirstEnumeration();
    while (nodes.hasMoreElements())
      expandPath(new TreePath((DefaultMutableTreeNode)nodes.nextElement()));
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}


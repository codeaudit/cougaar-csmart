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

import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.tree.CSMARTDataFlavor;
import org.cougaar.tools.csmart.ui.tree.DMTNArray;
import org.cougaar.tools.csmart.ui.tree.DNDTree;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Provides method definitions for abstract methods in DNDTree
 * to support Community tree in the Community Panel of
 * the Experiment Builder.
 */

public class CommunityDNDTree extends DNDTree {

  private transient Logger log;

  protected static CSMARTDataFlavor agentFlavor =
    new CSMARTDataFlavor(DefaultMutableTreeNode.class,
                         AgentComponent.class,
                         CommunityDNDTree.class,
                         "CSMART Agent");
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

  /**
   * Add a new node to the tree; used to build trees when
   * user displays a new tree.
   */

  protected DefaultMutableTreeNode addNode(DefaultMutableTreeNode node,
                                           String name, String type,
                                           String communityName) {
    DefaultTreeModel model = (DefaultTreeModel)getModel();
    DefaultMutableTreeNode newNode =
      new DefaultMutableTreeNode(new CommunityTreeObject(name, type, communityName),
				 (type.equals("Root") || 
				  type.equals("Host") || 
				  type.equalsIgnoreCase("Community")));
    model.insertNodeInto(newNode, node, node.getChildCount());
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
   * Add an element by copying the user object in the source
   * and creating a new tree node for it.
   * Hosts are not added, only their children.
   * Nodes (node agents) are added and their children are
   * added at the same level (i.e. the hierarchy is flattened).
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
    // if node is a host, then just add its descendants
    if (cto.isHost()) {
      copyChildren(source, target, ix);
      DefaultMutableTreeNode insertionPointNode =
        (DefaultMutableTreeNode)target.getChildAt(ix);
      scrollPathToVisible(new TreePath(insertionPointNode.getPath()));
      return true;
    }
    CommunityTreeObject newCTO = cto.copy();
    DefaultMutableTreeNode newNode =
      new DefaultMutableTreeNode(newCTO, false);
    if (isDuplicate(newNode, target) || isInCommunity(newNode, target)) {
      return false;
    }
    model.insertNodeInto(newNode, target, ix++);
    // copy the source node's descendants, recursively
    copyChildren(source, target, ix);
    scrollPathToVisible(new TreePath(newNode.getPath()));
    return true;
  }

  /**
   * Add an element by getting the user object out of the source node
   * and creating a new tree node for it,
   * and adding it as a child of the parent node;
   * recurse for all the node's descendants, flattening the hierarchy,
   * i.e. all nodes are added to the parentNode.
   * nodes must be added "top-down" in order for the model listener
   * to correctly add them to the database.
   * Reject duplicates.  Sibling nodes and nodes in the same community
   * must be unique.
   */
  private void copyChildren(DefaultMutableTreeNode sourceNode,
                            DefaultMutableTreeNode parentNode,
                            int insertionIndex) {
    DefaultTreeModel model = (DefaultTreeModel)getModel();
    int nChildren = sourceNode.getChildCount();
    for (int i=0; i < nChildren; i++) {
      DefaultMutableTreeNode oldChildNode = 
        (DefaultMutableTreeNode)sourceNode.getChildAt(i);
      CommunityTreeObject cto = 
        (CommunityTreeObject)oldChildNode.getUserObject();
      CommunityTreeObject newCTO = cto.copy();
      DefaultMutableTreeNode newChildNode =
        new DefaultMutableTreeNode(newCTO, false);
      if (isDuplicate(newChildNode, parentNode) ||
          isInCommunity(newChildNode, parentNode))
        continue;
      model.insertNodeInto(newChildNode, parentNode, insertionIndex++);
      copyChildren(oldChildNode, parentNode, insertionIndex);
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
   * Return true if the given node is already in the community to which
   * it is being dragged;
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

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}


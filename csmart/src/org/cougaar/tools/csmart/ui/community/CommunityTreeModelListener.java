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


package org.cougaar.tools.csmart.ui.community;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Update the database when user modifies the community tree.
 * Note that treeNodesInserted handles both the insertion of a node,
 * and its removal from its previous community, then treeNodesRemoved does
 * nothing.  This approach is taken because the listener can be messaged
 * with treeNodesInserted, before treeNodesRemoved.
 */

public class CommunityTreeModelListener implements TreeModelListener {
  private CommunityTableUtils communityTableUtils;
  private String assemblyId; // The Experiments Community Assembly
  private transient Logger log;

  public CommunityTreeModelListener(CommunityTableUtils communityTableUtils, String assemblyId) {
    this.communityTableUtils = communityTableUtils;
    log = CSMART.createLogger(this.getClass().getName());
    this.assemblyId = assemblyId;
  }

  public void treeNodesChanged(TreeModelEvent e) {
  }

  /**
   * Add info on added nodes to community_entity_attribute table, if needed.
   * This is called, when tree nodes are inserted because:
   * 1) the user is creating a new community (using pop-up menus)
   * the add action (createCommunity method) updates the 
   * community_attribute_table; this updates the community_entity_attribute
   * table if the new community is created within a community (not in root)
   * 2) the user dragged an entity from an experiment into a community
   * create a new entry in the community_entity_attribute table
   * 3) the user dragged an entity or community from one community to another
   * create a new entry in the community_entity_attribute table
   * On input, the user object contains the name of the community
   * that the node was in; this method sets the new community name.
   */
  public void treeNodesInserted(TreeModelEvent e) {
    Object[] addedNodes = e.getChildren();
    // first, take remove action
    for (int i = 0; i < addedNodes.length; i++)
      removeNode((DefaultMutableTreeNode)addedNodes[i]);
    // get parent of added nodes
    DefaultMutableTreeNode node =
      (DefaultMutableTreeNode)e.getTreePath().getLastPathComponent();
    if (node.isRoot())
      return; // don't do anything if adding to root

    // find community this node belongs in and add info on the new entity
    CommunityTreeObject cto = null;
    while (node != null) {
      cto = (CommunityTreeObject)node.getUserObject();
      if (cto.isCommunity())
        break;
      node = (DefaultMutableTreeNode)node.getParent();
    }
    if (cto == null) {
      if(log.isErrorEnabled()) {
        log.error("Attempted to insert node that is not in a community: " +
                  node);
      }
      return;
    }

    String communityName = cto.toString();
    for (int i = 0; i < addedNodes.length; i++) {
      node = (DefaultMutableTreeNode)addedNodes[i];
      CommunityTreeObject addedObject = 
        (CommunityTreeObject)node.getUserObject();
      // set the new community name in the user object
      addedObject.setCommunityName(communityName);
      if (!addedObject.isHost()) { // don't add info about hosts to database
        String entityName = addedObject.toString();
        CommunityDBUtils.insertEntityInfo(communityName, entityName, 
                                          "EntityType", addedObject.getType(), assemblyId);
        CommunityDBUtils.insertEntityInfo(communityName, entityName,
                                          "Role", "Member", assemblyId);
      }
    }
  }

  /**
   * Do nothing; the remove action is taken by the inserted method,
   * because these messages are received backwards (insert before remove).
   */
  public void treeNodesRemoved(TreeModelEvent e) {
  }

  /**
   * Remove the node from the previous community if any.
   * Invoked when inserting a node which may have
   * been moved from some other community, or when deleting nodes.
   */
  public void removeNode(DefaultMutableTreeNode node) {
    // remove from previous community, if any
    CommunityTreeObject deletedObject =
      (CommunityTreeObject)node.getUserObject();
    String entityName = deletedObject.toString();
    String communityName = deletedObject.getCommunityName();
    if (communityName == null)
      return;
    if (!deletedObject.isHost()) 
      CommunityDBUtils.deleteEntityInfo(communityName, entityName, assemblyId);
  }

  /**
   * Remove a node and all its descendants from the community.
   * Invoked when deleting a node.
   * Delete subcommunities only if this is the last reference.
   */
  public void removeBranch(DefaultMutableTreeNode node) {
    int nChildren = node.getChildCount();
    for (int i = 0; i < nChildren; i++) {
      DefaultMutableTreeNode childNode = 
        (DefaultMutableTreeNode)node.getChildAt(i);
      CommunityTreeObject childObject = 
        (CommunityTreeObject)childNode.getUserObject();
      if (childObject.isCommunity()) {
        removeNode(childNode);
        removeCommunity(childObject.toString());
      } else {
        removeBranch(childNode);
        removeNode(childNode);
      }
    }
    removeNode(node);
    CommunityTreeObject deletedObject = 
      (CommunityTreeObject)node.getUserObject();
    if (deletedObject.isCommunity())
      removeCommunity(deletedObject.toString());
  }

  /**
   * Remove this community if it's not in any other community.
   */
  private void removeCommunity(String communityName) {
    if (!CommunityDBUtils.isCommunityInUse(communityName, assemblyId))
      CommunityDBUtils.deleteCommunityInfo(communityName, assemblyId);
  }

  public void treeStructureChanged(TreeModelEvent e) {
  }
}


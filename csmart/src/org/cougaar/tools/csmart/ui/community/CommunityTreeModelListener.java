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

import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Update the database when user modifies the community tree.
 * This maintains a hashtable of names of nodes in the tree
 * and their community (don't use the actual nodes, as they change),
 * so that treeNodesInserted can both handle the insertion of a node,
 * and its removal from its previous community, then treeNodesRemoved does
 * nothing.  This approach is taken because the listener can be messaged
 * with treeNodesInserted, before treeNodesRemoved.
 */

public class CommunityTreeModelListener implements TreeModelListener {
  CommunityTableUtils communityTableUtils;
  Hashtable communities = new Hashtable();

  public CommunityTreeModelListener(CommunityTableUtils communityTableUtils) {
    this.communityTableUtils = communityTableUtils;
  }

  /**
   * Just update the hashtable of nodes and communities.
   */
  public void addNode(DefaultMutableTreeNode node, String communityName) {
    CommunityTreeObject cto = (CommunityTreeObject)node.getUserObject();
    communities.put(cto.toString(), communityName);
  }

  public void treeNodesChanged(TreeModelEvent e) {
    System.out.println("TREE NODES CHANGED");
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
      System.out.println("Inserted node that is not in a community: " +
                         node);
      return;
    }

    String communityName = cto.toString();
    for (int i = 0; i < addedNodes.length; i++) {
      node = (DefaultMutableTreeNode)addedNodes[i];
      CommunityTreeObject addedObject = 
        (CommunityTreeObject)node.getUserObject();
      if (!addedObject.isHost()) { // don't add info about hosts to database
        String entityName = addedObject.toString();
        String query = CommunityFrame.INSERT_ENTITY_INFO_QUERY + 
          communityName + "', '" + entityName +
          "', 'MemberType', '" + addedObject.getType() + "')";
        communityTableUtils.executeQuery(query);
        query = CommunityFrame.INSERT_ENTITY_INFO_QUERY + communityName + 
          "', '" + entityName + "', 'Role', 'Member')";
        communityTableUtils.executeQuery(query);
      }
      addNode(node, communityName);
    }
  }

  /**
   * Do nothing; the remove action is taken by the inserted method,
   * because these messages are received backwards (insert before remove).
   */
  public void treeNodesRemoved(TreeModelEvent e) {
  }

  /**
   * Remove the node from the hashtable of nodes and communities
   * and update the database table.
   * Invoked when inserting a node which may have
   * been moved from some other community, or when deleting nodes.
   */
  public void removeNode(DefaultMutableTreeNode node) {
    // remove from previous community
    CommunityTreeObject deletedObject =
      (CommunityTreeObject)node.getUserObject();
    String entityName = deletedObject.toString();
    String communityName = (String)communities.remove(entityName);
    if (communityName == null)
      return;
    if (!deletedObject.isHost()) {
      String query = CommunityFrame.DELETE_ENTITY_INFO_QUERY + 
        communityName + "' and entity_id = '" + entityName + "'";
      communityTableUtils.executeQuery(query);
    } 
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
    String query = CommunityFrame.IS_COMMUNITY_IN_USE_QUERY +
      communityName + "'";
    System.out.println(query);
    ArrayList results = CommunityDBUtils.getQueryResults(query);
    if (results.size() == 0) {
      query = CommunityFrame.DELETE_COMMUNITY_INFO_QUERY +
        communityName + "'";
      System.out.println(query);
      communityTableUtils.executeQuery(query);
    } else
      System.out.println("Community not deleted");
  }

  public void treeStructureChanged(TreeModelEvent e) {
    System.out.println("TREE STRUCTURE CHANGED");
  }
}


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

import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class CommunityTreeModelListener implements TreeModelListener {
  CommunityTableUtils communityTableUtils;

  public CommunityTreeModelListener(CommunityTableUtils communityTableUtils) {
    this.communityTableUtils = communityTableUtils;
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
    DefaultMutableTreeNode parentNode =
      (DefaultMutableTreeNode)e.getTreePath().getLastPathComponent();
    CommunityTreeObject cto = 
      (CommunityTreeObject)parentNode.getUserObject();
    // if parent is a community, add info on the new entity
    if (cto.isCommunity()) {
      String communityName = cto.toString();
      for (int i = 0; i < addedNodes.length; i++) {
        DefaultMutableTreeNode node = 
          (DefaultMutableTreeNode)addedNodes[i];
        CommunityTreeObject addedObject = 
          (CommunityTreeObject)node.getUserObject();
        String entityName = addedObject.toString();
        String query = CommunityFrame.INSERT_ENTITY_INFO_QUERY + 
          communityName +
          "', '" + entityName +
          "', 'MemberType', '" + addedObject.getType() + "')";
        communityTableUtils.executeQuery(query);
        query = CommunityFrame.INSERT_ENTITY_INFO_QUERY + communityName + 
          "', '" + entityName +
          "', 'Role', 'Member')";
        communityTableUtils.executeQuery(query);
      }
    }
  }

  /**
   * Remove info on deleted nodes from community_entity_attribute table.
   * TODO: if a community is deleted, should all info on it be deleted?
   */
  public void treeNodesRemoved(TreeModelEvent e) {
    Object[] deletedNodes = e.getChildren();
    DefaultMutableTreeNode parentNode =
      (DefaultMutableTreeNode)e.getTreePath().getLastPathComponent();
    CommunityTreeObject cto = 
      (CommunityTreeObject)parentNode.getUserObject();
    // if parent is a community, remove the entity info from the community
    if (cto.isCommunity()) {
      String communityName = cto.toString();
      for (int i = 0; i < deletedNodes.length; i++) {
        DefaultMutableTreeNode node = 
          (DefaultMutableTreeNode)deletedNodes[i];
        CommunityTreeObject deletedObject = 
          (CommunityTreeObject)node.getUserObject();
        String query = CommunityFrame.DELETE_ENTITY_INFO_QUERY + 
          communityName +
          "' and entity_id = '" + deletedObject.toString() + "'";
        communityTableUtils.executeQuery(query);
      }
    }
  }

  public void treeStructureChanged(TreeModelEvent e) {
    System.out.println("TREE STRUCTURE CHANGED");
  }
}


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

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class CommunityTreeSelectionListener implements TreeSelectionListener {
  CommunityTableUtils communityTableUtils;

  public CommunityTreeSelectionListener(CommunityTableUtils communityTableUtils) {
    this.communityTableUtils = communityTableUtils;
  }

  /**
   * When tree selection changes, then display information in table
   * for newly selected node.
   * If the node has no parent, either it's the root node,
   * or the user has deleted the node (in which case,
   * the node selection event is received AFTER the node is deleted);
   * so if the node has no parent, ignore the selection event.
   * If the node is not a community and has children, 
   * display parameters for all the children.
   * @param e the tree selection event
   */

  public void valueChanged(TreeSelectionEvent e) {
    DefaultMutableTreeNode node = 
      (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
    if (node.getParent() == null)
      return;
    CommunityTreeObject cto = (CommunityTreeObject)node.getUserObject();
    String name = cto.toString();
    String query = null;
    if (cto.isRoot())
      return; // ignore selecting root
    // node is a community; display information for a community 
    if (cto.isCommunity()) {
      query = CommunityFrame.GET_COMMUNITY_INFO_QUERY + name + "'";
      communityTableUtils.executeQuery(query);
      return;
    } 
    // get community that node is in
    String communityName = "";
    DefaultMutableTreeNode tmpNode = (DefaultMutableTreeNode)node.getParent();
    while (tmpNode != null) {
      if (((CommunityTreeObject)tmpNode.getUserObject()).isCommunity()) {
        communityName = tmpNode.toString();
        break;
      } else
        tmpNode = (DefaultMutableTreeNode)tmpNode.getParent();
    }
    displayEntityInfo(communityName, node);
  }

  private void displayEntityInfo(String communityName,
                                DefaultMutableTreeNode node) {
    int nChildren = node.getChildCount();
    if (nChildren == 0) {
      String entityName =
        ((CommunityTreeObject)node.getUserObject()).toString();
      String query = CommunityFrame.GET_ENTITY_INFO_QUERY + communityName + 
        "' and community_entity_attribute.entity_id = '" + entityName + "'";
      communityTableUtils.executeQuery(query);
    } else {
      StringBuffer sbuf = new StringBuffer(500);
      sbuf.append(CommunityFrame.GET_ENTITY_INFO_QUERY);
      sbuf.append(communityName);
      sbuf.append("' and (");
      for (int i = 0; i < nChildren; i++) {
        DefaultMutableTreeNode childNode =
          (DefaultMutableTreeNode)node.getChildAt(i);
        String entityName = 
          ((CommunityTreeObject)childNode.getUserObject()).toString();
        sbuf.append("community_entity_attribute.entity_id = '");
        sbuf.append(entityName);
        if (i < (nChildren-1))
          sbuf.append("' or ");
        else
          sbuf.append("')");
      }
      communityTableUtils.executeQuery(sbuf.toString());
    }
  }
                                
}

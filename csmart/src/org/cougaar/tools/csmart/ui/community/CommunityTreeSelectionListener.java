/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
    if (cto.isRoot())
      return; // ignore selecting root
    // node is a community; display information for a community 
    if (cto.isCommunity()) {
      communityTableUtils.getCommunityInfo(name);
      return;
    } 
    // get community that node is in
    String communityName = cto.getCommunityName();
    displayEntityInfo(communityName, node);
  }

  private void displayEntityInfo(String communityName,
                                 DefaultMutableTreeNode node) {
    int nChildren = node.getChildCount();
    if (nChildren == 0) {
      String entityName =
        ((CommunityTreeObject)node.getUserObject()).toString();
      communityTableUtils.getEntityInfo(communityName, entityName);
    } else {
      StringBuffer sbuf = new StringBuffer(500);
      for (int i = 0; i < nChildren; i++) {
        DefaultMutableTreeNode childNode =
          (DefaultMutableTreeNode)node.getChildAt(i);
        String entityName = 
          ((CommunityTreeObject)childNode.getUserObject()).toString();
        sbuf.append(entityName);
        if (i < (nChildren-1))
          sbuf.append("', '");
      }
      communityTableUtils.getChildrenEntityInfo(communityName, sbuf.toString());
    }
  }

}

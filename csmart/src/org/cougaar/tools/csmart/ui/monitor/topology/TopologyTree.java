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

package org.cougaar.tools.csmart.ui.monitor.topology;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 *  A custom JTree that supplies tool tips from the user object.
 */

public class TopologyTree extends JTree {

  public TopologyTree(DefaultTreeModel model) {
    super(model);
    setToolTipText("");
  }

  public String getToolTipText(MouseEvent evt) {
    if (getRowForLocation(evt.getX(), evt.getY()) == -1) return null;    
    TreePath path = getPathForLocation(evt.getX(), evt.getY());
    DefaultMutableTreeNode node =
      (DefaultMutableTreeNode)path.getLastPathComponent();
    Object o = node.getUserObject();
    if (o != null && o instanceof TopologyTreeObject)
      return ((TopologyTreeObject)o).getToolTip();
    else
      return "";
  }

  /**
   * Values are:
   * AGENT, NODE, HOST, SITE, ENCLAVE, INCARNATION, MOVE_ID, IS_NODE, STATUS
   */

  public void setValues(ArrayList values) {
    DefaultTreeModel model = (DefaultTreeModel)getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
    root.removeAllChildren();
    model.nodeStructureChanged(root);
    if (values == null)
      return;
    Hashtable hosts = new Hashtable();
    Hashtable nodes = new Hashtable();
    for (int i = 0; i < values.size(); i++) {
      StringTokenizer st = new StringTokenizer((String)values.get(i), ",");
      if (st.countTokens() < 3) {
        continue;
      }
      String agent = st.nextToken();
      String node = st.nextToken();
      String host = st.nextToken();
      // description is the remainder of the initial string
      String prefix = host + "," + node + "," + agent;
      String s = (String)values.get(i);
      String description = s.substring(prefix.length());
      if (description.startsWith(","))
        description = description.substring(1);
      description = description.trim();
      DefaultMutableTreeNode hostNode = 
        (DefaultMutableTreeNode)hosts.get(host);
      if (hostNode == null) {
        hostNode = 
          new DefaultMutableTreeNode(new TopologyTreeObject(host), true);
        model.insertNodeInto(hostNode, root, root.getChildCount());
        hosts.put(host, hostNode);
      }
      DefaultMutableTreeNode nodeNode = 
        (DefaultMutableTreeNode)nodes.get(node);
      if (nodeNode == null) {
        nodeNode =
          new DefaultMutableTreeNode(new TopologyTreeObject(node));
        model.insertNodeInto(nodeNode, hostNode, hostNode.getChildCount());
        nodes.put(node, nodeNode);
      }
      DefaultMutableTreeNode agentNode = 
        new DefaultMutableTreeNode(new TopologyTreeObject(agent, description));
      model.insertNodeInto(agentNode, nodeNode, nodeNode.getChildCount());
    }
    // expand tree
    Enumeration treeNodes = root.depthFirstEnumeration();
    while (treeNodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)treeNodes.nextElement();
      expandPath(new TreePath(node.getPath()));
    }
  }
}

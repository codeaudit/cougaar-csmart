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

package org.cougaar.tools.csmart.ui.monitor.topology;

import java.awt.event.MouseEvent;
import java.util.*;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

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

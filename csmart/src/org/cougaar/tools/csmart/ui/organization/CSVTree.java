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

package org.cougaar.tools.csmart.ui.organization;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;

/**
 * Custom JTree for information read from CSV files.
 * Supports custom renderer to show deleted/undeleted agents.
 * Supports custom expander to prevent expanding deleted items.
 */
public class CSVTree extends JTree {
  private Model model;

  /**
   * Create tree from root.
   * @param model model that drives this application
   * @param root root of the tree
   */
  public CSVTree(Model model, DefaultMutableTreeNode root) {
    super(root);
    this.model = model;
    setShowsRootHandles(true);
    setCellRenderer(new CSVTreeCellRenderer());
    addTreeWillExpandListener(new CSVTreeWillExpandListener());
    addMouseListener(new CSVMouseListener());
  }

  /**
   * Custom tree cell renderer.
   * Display nodes which will be agents in black and others in gray.
   * Nodes which will be agents are the nodes
   * for which the CSVOrgInfo.deleted flag is false.
   */
  private class CSVTreeCellRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
      Component c = 
        super.getTreeCellRendererComponent(tree, value, sel,
                                           expanded, leaf, row, hasFocus);
      Font f = c.getFont();
      CSVOrgInfo org = 
        (CSVOrgInfo)((DefaultMutableTreeNode)value).getUserObject();
      if (!org.deleted) {
        c.setForeground(Color.black);
        c.setFont(new Font(f.getName(), Font.BOLD, f.getSize()));
      } else {
        c.setForeground(Color.gray);
        c.setFont(new Font(f.getName(), Font.PLAIN, f.getSize()));
      }
      return c;
    }
  }

  /**
   * Tree will expand listener for tree from CSV file;
   * don't allow expanding a deleted node.
   */
  private class CSVTreeWillExpandListener implements TreeWillExpandListener {
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }

    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
      DefaultMutableTreeNode node = 
        (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
      CSVOrgInfo org = (CSVOrgInfo)node.getUserObject();
      if (org.deleted)
        throw new ExpandVetoException(event);
    }
  }

  /**
   * Mouse listener that deletes/undeletes a node and its
   * children on right click.
   * Used for tree built from CSV.
   */
  class CSVMouseListener extends MouseAdapter {

    public void mouseClicked(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON3) {
        JTree tree = (JTree)e.getComponent();
        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) return;
        DefaultMutableTreeNode node =
          (DefaultMutableTreeNode)path.getLastPathComponent();
        DefaultMutableTreeNode root =
          (DefaultMutableTreeNode)tree.getModel().getRoot();
        if (node == root) return; // can't delete root
        CSVOrgInfo org = (CSVOrgInfo)node.getUserObject();
        updateDeleted(node);
        if (org.deleted)
          tree.collapsePath(path);
        tree.getModel().valueForPathChanged(path, org);
      }
    }

    // if the node was not deleted, then delete it and its children
    // if the node was deleted, then undelete it and its children
    private void updateDeleted(DefaultMutableTreeNode node) {
      boolean delete = true;
      if (((CSVOrgInfo)node.getUserObject()).deleted)
        delete = false;
      Enumeration nodes = node.depthFirstEnumeration();
      while (nodes.hasMoreElements()) {
        DefaultMutableTreeNode descendantNode = 
          (DefaultMutableTreeNode)nodes.nextElement();
        CSVOrgInfo orgInfo = (CSVOrgInfo)descendantNode.getUserObject();
        orgInfo.deleted = delete;
      }
      model.updateAgentCount();
    }
  }

}

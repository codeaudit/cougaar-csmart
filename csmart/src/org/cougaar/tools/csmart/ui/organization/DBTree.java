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
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

public class DBTree extends JTree {
  Model model;

  public DBTree(Model model, DefaultMutableTreeNode root) {
    super(root);
    this.model = model;
    setShowsRootHandles(true);
    setCellRenderer(new DBTreeCellRenderer());
    addTreeExpansionListener(new DBTreeExpansionListener());
  }

  /**
   * Custom tree cell renderer.
   * Display nodes which will be agents in black and others in gray.
   * Nodes which will be agents are leaf nodes and non-expanded nodes.
   */
  class DBTreeCellRenderer extends DefaultTreeCellRenderer {

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
      if (leaf || !expanded) {
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
   * Tree expansion listener for tree from database; 
   * update the number of agents when a node is expanded or collapsed.
   */
  class DBTreeExpansionListener implements TreeExpansionListener {
    public void treeCollapsed(TreeExpansionEvent e) {
      model.updateDBAgentCount((JTree)e.getSource());
    }
    public void treeExpanded(TreeExpansionEvent e) {
      model.updateDBAgentCount((JTree)e.getSource());
    }
  }


}

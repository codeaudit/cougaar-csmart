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

package org.cougaar.tools.csmart.ui.viewer;

import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.net.URL;

public class OrganizerTreeCellRenderer extends DefaultTreeCellRenderer {
  Organizer organizer;

  public OrganizerTreeCellRenderer(Organizer organizer) {
    this.organizer = organizer;
  }

  /**
   * If a node represents a component
   * and that component needs to be saved to the database,
   * then draw the component in red.
   * If a node represents a component in an experiment,
   * and that experiment is being edited,
   * then draw the node in gray.
   */
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
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
    Object o = node.getUserObject();
    if (o instanceof Experiment) {
      if (((Experiment)o).isModified()) {
        c.setForeground(Color.red);
      }
      if (c instanceof JLabel) {
	URL image = getClass().getResource("Experiment20t.gif");
	if (image != null) {
	  ImageIcon ii = new ImageIcon(image);
	  if (ii != null)
	    ((JLabel)c).setIcon(ii);
	}
      }
    } else if (o instanceof SocietyComponent) {
      if (((SocietyComponent)o).isModified()) {
        c.setForeground(Color.red);
      }
      if (c instanceof JLabel) {
	URL image = getClass().getResource("Society16t.gif");
	if (image != null) {
	  ImageIcon ii = new ImageIcon(image);
	  if (ii != null)
	    ((JLabel)c).setIcon(ii);
	}
      }
    } else if (o instanceof RecipeComponent) {
      if (((RecipeComponent)o).isModified()) {
        c.setForeground(Color.red);
      }
      if (c instanceof JLabel) {
	URL image = getClass().getResource("Recipe16t.gif");
	if (image != null) {
	  ImageIcon ii = new ImageIcon(image);
	  if (ii != null)
	    ((JLabel)c).setIcon(ii);
	}
      }
    }
    if (organizer.isNodeInUse(node))
      c.setForeground(Color.gray);
    return c;
  }
}

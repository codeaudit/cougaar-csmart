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

/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.tree.CSMARTDataFlavor;
import org.cougaar.tools.csmart.ui.tree.DNDTree;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

public class OrganizerTree extends DNDTree {
  private transient Logger log;

    public static final DataFlavor folderFlavor =
        DataFlavor.stringFlavor;
    public static final CSMARTDataFlavor societyFlavor =
        new CSMARTDataFlavor(SocietyComponent.class,
                             null,
                             OrganizerTree.class,
                             "Society");
    public static final CSMARTDataFlavor recipeFlavor =
      new CSMARTDataFlavor(RecipeComponent.class,
			   //   new CSMARTDataFlavor(Recipe.class,
                             null,
                             OrganizerTree.class,
                             "Recipe");
    public static final CSMARTDataFlavor experimentFlavor =
        new CSMARTDataFlavor(Experiment.class,
                             null,
                             OrganizerTree.class,
                             "Experiment");
    public static final CSMARTDataFlavor componentFlavor =
        new CSMARTDataFlavor(ModifiableComponent.class,
                             null,
                             OrganizerTree.class,
                             "Modifiable Component");

    private DefaultTreeModel model;

    private static class MyTransferable implements Transferable {
        Object theData;
        DataFlavor[] flavors;
        public MyTransferable(DefaultMutableTreeNode aNode) {
            theData = aNode.getUserObject();
            if (theData instanceof String)
                flavors = new DataFlavor[] {folderFlavor};
            else if (theData instanceof SocietyComponent)
                flavors = new DataFlavor[] {societyFlavor};
	    else if (theData instanceof RecipeComponent)
	      //else if (theData instanceof Recipe)
                flavors = new DataFlavor[] {recipeFlavor};
            else if (theData instanceof Experiment)
                flavors = new DataFlavor[] {experimentFlavor};
            else if (theData instanceof ModifiableComponent)
                flavors = new DataFlavor[] {componentFlavor};
            else
                throw new IllegalArgumentException("Unknown node");
        }
        public Object getTransferData(DataFlavor flavor) {
            if (!flavor.equals(flavors[0]))
                throw new IllegalArgumentException("Illegal DataFlavor");
            return theData;
        }
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavors[0].equals(flavor);
        }
    }

    public OrganizerTree(DefaultTreeModel model) {
        super(model);
        this.model = model;
        setExpandsSelectedPaths(true);
        createLogger();
    }

  private void createLogger() {
    log = CSMART.createLogger("org.cougaar.tools.csmart.ui.viewer");
  }


    public void setSelection(TreeNode treeNode) {
        TreeNode[] nodes = model.getPathToRoot(treeNode);
        TreePath path = new TreePath(nodes);
        setSelectionPath(path);
    }

    public Transferable makeDraggableObject(Object o) {
        Transferable tran = null;
        if (o instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
            return new MyTransferable(node);
        }
        throw new IllegalArgumentException("Not a DefaultMutableTreeNode");
    }

    public int isDroppable(DataFlavor[] possibleFlavors, DefaultMutableTreeNode target) {
        Object userObject = target.getUserObject();
        // Can only drop on folders (or the root)
        if (userObject == null || userObject instanceof String) {
            for (int i = 0; i < possibleFlavors.length; i++) {
                DataFlavor flavor = possibleFlavors[i];
                if (flavor.equals(DataFlavor.stringFlavor))
                    return DnDConstants.ACTION_MOVE;
                if (flavor instanceof CSMARTDataFlavor) {
                    CSMARTDataFlavor cflavor = (CSMARTDataFlavor) flavor;
                    if (getClass().getName().equals(cflavor.getSourceClassName())) {
                        if (cflavor.equals(societyFlavor)
                            || cflavor.equals(recipeFlavor)
                            || cflavor.equals(experimentFlavor)
			    || cflavor.equals(componentFlavor))
                            return DnDConstants.ACTION_MOVE;
                    }
                }
            }
        }
        return DnDConstants.ACTION_NONE;
    }

    public boolean isDraggable(Object o) {
        if (o instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
	    if (node == getModel().getRoot()) 
	      return false; // not draggable if it's the root node
	    return true;
        }
        return false;
    }

    public int addElement(Transferable t,
                          DefaultMutableTreeNode target,
                          DefaultMutableTreeNode before) {
        DataFlavor[] flavors = t.getTransferDataFlavors();
        int action = isDroppable(flavors, target);
        if (action != DnDConstants.ACTION_NONE) {
            try {
                Object userData = t.getTransferData(flavors[0]);
                DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode(userData, userData instanceof String);
                int ix = target.getChildCount();
                if (before != null) {
                    ix = model.getIndexOfChild(target, before);
                }
                if(log.isDebugEnabled()) {
                  log.debug("Insert into " + target + " at " + ix + " before " + before);
                }
                model.insertNodeInto(node, target, ix);
                selectNode(node);
                return action;  // Always MOVE
            } catch (Exception e) {
              if(log.isDebugEnabled()) {
                log.error("Caught an exception", e);
                e.printStackTrace();
              }
            }
        }
        return DnDConstants.ACTION_NONE;
    }
}

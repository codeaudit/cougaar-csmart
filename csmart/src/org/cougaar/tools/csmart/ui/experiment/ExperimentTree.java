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

package org.cougaar.tools.csmart.ui.experiment;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.console.*;
import org.cougaar.tools.csmart.ui.tree.CSMARTDataFlavor;
import org.cougaar.tools.csmart.ui.tree.DNDTree;
import org.cougaar.tools.csmart.society.scalability.ScalabilityXSociety;

public class ExperimentTree extends DNDTree {
    public static final String RECIPES = "Recipes";
    public static final String SOCIETIES = "Societies";
    public static final String COMPONENTS = "Components";
    public static final CSMARTDataFlavor societyFlavor =
        new CSMARTDataFlavor(SocietyComponent.class,
                             null,
                             ExperimentTree.class,
                             "Society");
    public static final CSMARTDataFlavor recipeFlavor =
      new CSMARTDataFlavor(RecipeComponent.class,
                             null,
                             ExperimentTree.class,
                             "Recipe");
    public static final CSMARTDataFlavor componentFlavor =
        new CSMARTDataFlavor(ModifiableComponent.class,
                             null,
                             ExperimentTree.class,
                             "Modifiable Component");

    private DefaultTreeModel model;

    private static class MyTransferable implements Transferable {
        Object theData;
        DataFlavor[] flavors;
        public MyTransferable(DefaultMutableTreeNode aNode) {
            theData = aNode.getUserObject();
            if (theData instanceof RecipeComponent)
	      //else if (theData instanceof Recipe)
                flavors = new DataFlavor[] {recipeFlavor};
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

    public ExperimentTree(DefaultTreeModel model) {
        super(model);
        this.model = model;
        setExpandsSelectedPaths(true);
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
            try {
                return new MyTransferable(node);
            } catch (IllegalArgumentException iae) {
                return null;
            }
        }
        throw new IllegalArgumentException("Not a DefaultMutableTreeNode");
    }

    private int testFlavors(DataFlavor[] possibleFlavors, CSMARTDataFlavor testFlavor) {
        for (int i = 0; i < possibleFlavors.length; i++) {
            DataFlavor flavor = possibleFlavors[i];
            if (flavor instanceof CSMARTDataFlavor) {
                CSMARTDataFlavor cflavor = (CSMARTDataFlavor) flavor;
                if (cflavor.equals(testFlavor)) {
                    if (getClass().getName()
                        .equals(cflavor.getSourceClassName())) {
                        return DnDConstants.ACTION_MOVE;
                    } else {
                        return DnDConstants.ACTION_COPY;
                    }
                }
            }
        }
        return DnDConstants.ACTION_NONE;
    }

    public int isDroppable(DataFlavor[] possibleFlavors,
                           DefaultMutableTreeNode target)
    {
        if (!isEditable())
          return DnDConstants.ACTION_NONE;
        Object userObject = target.getUserObject();
        if (userObject instanceof String) {
            if (userObject.equals(SOCIETIES)) {
		// only allow one society per experiment for now
		if (target.getChildCount() > 0)
		    return DnDConstants.ACTION_NONE;
                return testFlavors(possibleFlavors, societyFlavor);
            }
            if (userObject.equals(RECIPES)) {
                return testFlavors(possibleFlavors, recipeFlavor);
            }
        } else if (target == model.getRoot()) {
            return DnDConstants.ACTION_COPY
                & (testFlavors(possibleFlavors, societyFlavor)
                   | testFlavors(possibleFlavors, recipeFlavor));
        }
        return DnDConstants.ACTION_NONE;
    }

  public boolean isDraggable(Object o) {
    if (o instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
      if( node == getModel().getRoot()) {
        return false; // not draggable if it's the root node
      }
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
            DataFlavor flavor = flavors[0];
            try {
                DefaultMutableTreeNode root =
                    (DefaultMutableTreeNode) model.getRoot();
                if (target == root) {
                    for (int i = 0, n = root.getChildCount(); i < n; i++) {
                        DefaultMutableTreeNode node =
                            (DefaultMutableTreeNode) root.getChildAt(i);
                        Object o = node.getUserObject();
                        if (SOCIETIES.equals(o) && flavor == societyFlavor
                            || RECIPES.equals(o) && flavor == recipeFlavor) {
                            target = node;
                        } else {
                            return DnDConstants.ACTION_NONE;
                        }
                    }
                }
                Object userData = t.getTransferData(flavor);
                if (userData instanceof ModifiableComponent) {
                    ModifiableComponent mcc =
                        (ModifiableComponent) userData;
                    if (!mcc.isEditable() && mcc.hasUnboundProperties())
                        return DnDConstants.ACTION_NONE;
                }
                DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode(userData, false);
                int ix = target.getChildCount();
                if (before != null) {
                    ix = model.getIndexOfChild(target, before);
                }
//                  System.out.println("Insert into " + target
//                                     + " at " + ix
//                                     + " before " + before);
                model.insertNodeInto(node, target, ix);
                selectNode(node);
                return action;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return DnDConstants.ACTION_NONE;
    }
}

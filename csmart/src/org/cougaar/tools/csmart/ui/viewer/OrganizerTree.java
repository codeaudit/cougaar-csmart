/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
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
import org.cougaar.tools.csmart.ui.builder.TreeBuilder;
import org.cougaar.tools.csmart.ui.experiment.*;
import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.console.*;
import org.cougaar.tools.csmart.ui.tree.CSMARTDataFlavor;
import org.cougaar.tools.csmart.ui.tree.DNDTree;
import org.cougaar.tools.csmart.scalability.ScalabilityXSociety;

public class OrganizerTree extends DNDTree {
    public static final DataFlavor folderFlavor =
        DataFlavor.stringFlavor;
    public static final CSMARTDataFlavor societyFlavor =
        new CSMARTDataFlavor(SocietyComponent.class,
                             null,
                             OrganizerTree.class,
                             "Society");
    public static final CSMARTDataFlavor impactFlavor =
        new CSMARTDataFlavor(Impact.class,
                             null,
                             OrganizerTree.class,
                             "Impact");
    public static final CSMARTDataFlavor metricFlavor =
        new CSMARTDataFlavor(Metric.class,
                             null,
                             OrganizerTree.class,
                             "Metric");
    public static final CSMARTDataFlavor experimentFlavor =
        new CSMARTDataFlavor(Experiment.class,
                             null,
                             OrganizerTree.class,
                             "Experiment");

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
            else if (theData instanceof Impact)
                flavors = new DataFlavor[] {impactFlavor};
            else if (theData instanceof Metric)
                flavors = new DataFlavor[] {metricFlavor};
            else if (theData instanceof Experiment)
                flavors = new DataFlavor[] {experimentFlavor};
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
                            || cflavor.equals(impactFlavor)
                            || cflavor.equals(metricFlavor)
                            || cflavor.equals(experimentFlavor))
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
            return node != getModel().getRoot();
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
//                  System.out.println("Insert into " + target + " at " + ix + " before " + before);
                model.insertNodeInto(node, target, ix);
                selectNode(node);
                return action;  // Always MOVE
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return DnDConstants.ACTION_NONE;
    }
}

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

package org.cougaar.tools.csmart.ui.experiment;

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
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.console.*;
import org.cougaar.tools.csmart.ui.tree.CSMARTDataFlavor;
import org.cougaar.tools.csmart.ui.tree.DMTNArray;
import org.cougaar.tools.csmart.ui.tree.DNDTree;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.io.ObjectInputStream;
import java.io.IOException;

public class ExperimentTree extends DNDTree {
  private transient Logger log;

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
    private Experiment experiment;

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

    public ExperimentTree(DefaultTreeModel model,
                          Experiment experiment) {
        super(model);
        this.model = model;
        this.experiment = experiment;
        setExpandsSelectedPaths(true);
        createLogger();
    }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
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

  /**
   * Add a dropped element to the tree.
   * Rejects duplicates.  An object is a duplicate
   * if it has the same name as a child of the target, 
   * and is not from the same tree.
   * @param t the transferable object to drop
   * @param target where to drop the object
   * @param before before which node to drop it or null
   * @return int the action as defined in DnDConstants
   */
  public int addElement(Transferable t,
                        DefaultMutableTreeNode target,
                        DefaultMutableTreeNode before) {
    DataFlavor[] flavors = t.getTransferDataFlavors();
    // perhaps isDroppable should more rigorously check all the
    // transferables, as it stands, the next addElement method
    // does checks for individual elements when an array of them is added
    int action = isDroppable(flavors, target);
    if (action == DnDConstants.ACTION_NONE)
      return DnDConstants.ACTION_NONE;
    DataFlavor flavor = flavors[0];
    Object data = null;
    try {
      data = t.getTransferData(flavors[0]);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception adding dropped element:", e);
        return DnDConstants.ACTION_NONE;
      }
    }
    if (data == null) {
      if(log.isErrorEnabled()) {
        log.error("Attempting to add null dropped element");
      }
      return DnDConstants.ACTION_NONE;
    }
    if (data instanceof DMTNArray) {
      DMTNArray nodes = (DMTNArray) data;
      for (int i = 0; i < nodes.nodes.length; i++) 
        addElement(nodes.nodes[i].getUserObject(), flavor, target, before);
    } else 
      addElement(data, flavor, target, before);
    return action;
  }

  /**
   * Add the element; this does some additional checks which may
   * reject the element.  In particular, this checks each element
   * when adding an array of elements.
   */
  private void addElement(Object userData,
                          DataFlavor flavor,
                          DefaultMutableTreeNode target,
                          DefaultMutableTreeNode before) {
    DefaultMutableTreeNode root =
      (DefaultMutableTreeNode) model.getRoot();
    // if target is root, try to find the right place to drop the element
    if (target == root) {
      for (int i = 0, n = root.getChildCount(); i < n; i++) {
        DefaultMutableTreeNode node =
          (DefaultMutableTreeNode) root.getChildAt(i);
        Object o = node.getUserObject();
        if (SOCIETIES.equals(o) && userData instanceof SocietyComponent) {
          target = node;
          before = null;
          break;
        } else if (RECIPES.equals(o) && userData instanceof RecipeComponent) {
          target = node;
          before = null;
          break;
        }
      }
      if (target == root)
        return; // no place to put the new element
    }
    // make sure we don't add a recipe to the societies or vice versa
    if (target.getUserObject().equals(SOCIETIES) && 
        userData instanceof RecipeComponent) {
      return;
    }
    if (target.getUserObject().equals(RECIPES) && 
        userData instanceof SocietyComponent) {
      return;
    }
    // don't add more than one society
    if (target.getUserObject().equals(SOCIETIES) && 
        target.getChildCount() > 0) {
      return;
    }
    // if we're adding a recipe that adds an agent that's
    // in the experiment, then reject the recipe
    if (userData instanceof RecipeComponent) {
      if (!checkAdd((RecipeComponent)userData))
        return;
    }
    if (userData instanceof ModifiableComponent) {
      ModifiableComponent mcc = (ModifiableComponent) userData;
      if (!mcc.isEditable() && mcc.hasUnboundProperties())
        return;
      // check for duplicates, if not dragging from same tree
      String thisClassName = getClass().getName();
      if (flavor instanceof CSMARTDataFlavor &&
          !((CSMARTDataFlavor)flavor).getSourceClassName().equals(thisClassName)) {
        String newName = mcc.getShortName();
        int ix = target.getChildCount();
        for (int i = 0; i < ix; i++) {
          DefaultMutableTreeNode childNode = 
            (DefaultMutableTreeNode)target.getChildAt(i);
          Object targetChildData = childNode.getUserObject();
          if (targetChildData instanceof ModifiableComponent &&
              ((ModifiableComponent)targetChildData).getShortName().equals(newName)) 
            return;
        }
      }
    }
    DefaultMutableTreeNode node = 
      new DefaultMutableTreeNode(userData, false);
    int ix = target.getChildCount();
    if (before != null) {
      ix = model.getIndexOfChild(target, before);
    }
    if(log.isDebugEnabled()) {
      log.debug("Insert into " + target
                + " at " + ix
                + " before " + before);
    }
    model.insertNodeInto(node, target, ix);
    selectNode(node);
  }

  private boolean checkAdd(RecipeComponent rc) {
    AgentComponent[] recAgents = rc.getAgents();
    if (recAgents != null && recAgents.length > 0) {
      for (int j=0; j < recAgents.length; j++) {
        if (!experiment.agentNameUnique(recAgents[j].getShortName())) {
	  final String dupName = recAgents[j].getShortName();
	  // Need to remove recipe and inform user.
	  javax.swing.SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
		JOptionPane.showMessageDialog(null, 
                                          "Experiment cannot contain two agents with the same name: " + dupName,
                                          "Recipe Add Aborted!",
                                          JOptionPane.ERROR_MESSAGE);
	      }
	    });
            return false;
        }
      }
    }
    return true;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

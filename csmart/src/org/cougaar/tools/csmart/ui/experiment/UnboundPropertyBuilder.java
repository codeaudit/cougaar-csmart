/* 
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.EventObject;

/**
 * Properties panel in experiment builder.<br>
 * Use this panel to add a society or recipes to your experiment.
 * This class manages the manipulation of these components.
 */
public class UnboundPropertyBuilder extends JPanel {
  private static final String REMOVE_MENU_ITEM = "Remove";
  private ExperimentBuilder experimentBuilder;
  private DefaultTreeModel model;
  private ExperimentTree tree;
  private Experiment experiment;
  private boolean isEditable;
  private DefaultMutableTreeNode root;
  private DefaultMutableTreeNode societies;
  private DefaultMutableTreeNode recipes;

  // FIXME: right panel is empty. Get rid of it?
  // Do something to make this panel useful?
  // Drop it altogether?
  private JPanel rightPanel = new JPanel(new BorderLayout());
  private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
  private JPopupMenu popupMenu = new JPopupMenu();
  private JPopupMenu societiesMenu = new JPopupMenu();
  private JPopupMenu recipesMenu = new JPopupMenu();

  private transient Logger log;

  /**
   * Define actions for pop-up menus on societies and recipes.
   */
  private Action removeAction = new AbstractAction(REMOVE_MENU_ITEM) {
    public void actionPerformed(ActionEvent e) {
      removeSelectedItems();
    }
  };
  
  private Action[] popupActions = {
    removeAction
  };
  
  private Action[] societiesActions = {
    // FIXME: Why do we allow doing this?
    new AbstractAction(REMOVE_MENU_ITEM + " Society") {
      public void actionPerformed(ActionEvent e) {
        removeAllChildren(societies);
      }
    }
  };
  
  private Action[] recipesActions = {
    new AbstractAction(REMOVE_MENU_ITEM + " All Recipes") {
      public void actionPerformed(ActionEvent e) {
        removeAllChildren(recipes);
      }
    }
  };

  /**
   * Define mouse listener to pop-up menus.
   */
  private MouseListener mouseListener = new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
      if (!isEditable) return;
      if (e.isPopupTrigger()) doPopup(e);
    }
    public void mousePressed(MouseEvent e) {
      if (!isEditable) return;
      if (e.isPopupTrigger()) doPopup(e);
    }
    public void mouseReleased(MouseEvent e) {
      if (!isEditable) return;
      if (e.isPopupTrigger()) doPopup(e);
    }
  };

  /**
   * Define tree model listener to update the societies and recipes
   * in the experiment when the user modifies the tree.
   */

  private TreeModelListener myTreeModelListener = new TreeModelListener() {
    public void treeNodesChanged(TreeModelEvent e) {
      reconcileExperimentNodes();
    }
    public void treeNodesInserted(TreeModelEvent e) {
      reconcileExperimentNodes();
    }
    public void treeNodesRemoved(TreeModelEvent e) {
      reconcileExperimentNodes();
    }
    public void treeStructureChanged(TreeModelEvent e) {
      reconcileExperimentNodes();
    }
  };

  /**
   * Constructs user interface for specifying values of
   * properties of configurable components to use in experiments.
   * @param experiment the experiment to edit
   * @param experimentBuilder the <code>ExperimentBuilder</code> that this user interface was invoked from
   */
  public UnboundPropertyBuilder(Experiment experiment, 
                                ExperimentBuilder experimentBuilder) {
    this.experiment = experiment;
    this.experimentBuilder = experimentBuilder;
    createLogger();
    isEditable = experiment.isEditable();
    root = new DefaultMutableTreeNode();
    model = new DefaultTreeModel(root);
    societies = new DefaultMutableTreeNode(ExperimentTree.SOCIETIES);
    recipes = new DefaultMutableTreeNode(ExperimentTree.RECIPES);
    societies.setAllowsChildren(true);
    recipes.setAllowsChildren(true);
    model.insertNodeInto(societies, root, 0);
    model.insertNodeInto(recipes, root, 1);
    model.setAsksAllowsChildren(true);
    tree = new ExperimentTree(model, experiment);
    // cell editor always returns false so that user can't edit cell names
    // using tree.setCellEditor(null) doesn't work
    DefaultCellEditor myEditor = new DefaultCellEditor(new JTextField()) {
      public boolean isCellEditable(EventObject e) {
	return false;
      }
    };
    tree.setCellEditor(myEditor);

    tree.setExpandsSelectedPaths(true);
    tree.setRootVisible(false);
    tree.expandNode(societies);
    tree.expandNode(recipes);
    tree.addMouseListener(mouseListener);
    //    tree.setPreferredSize(new Dimension(250, 200));
    model.addTreeModelListener(myTreeModelListener);

    splitPane.setRightComponent(rightPanel);
    splitPane.setLeftComponent(new JScrollPane(tree));
    setLayout(new BorderLayout());
    add(splitPane, BorderLayout.CENTER);
    splitPane.setDividerLocation(100);
    for (int i = 0; i < popupActions.length; i++) {
      popupMenu.add(popupActions[i]);
    }
    // Actions on societies - this allows removing all societies
    // from an Experiment. Why do we permit this?
    // This possibility is not documented.
//     for (int i = 0; i < societiesActions.length; i++) {
//       societiesMenu.add(societiesActions[i]);
//     }
    for (int i = 0; i < recipesActions.length; i++) {
      recipesMenu.add(recipesActions[i]);
    }
    initDisplay();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Display information about the experiment.  Called to 
   * re-use this interface.
   * @param newExperiment the new experiment to edit
   */
  public void reinit(Experiment newExperiment) {
    experiment = newExperiment;
    isEditable = newExperiment.isEditable();
    initDisplay();
  }

  private void initDisplay() {
    model.removeTreeModelListener(myTreeModelListener);
    societies.removeAllChildren();
    recipes.removeAllChildren();
    model.nodeStructureChanged(root);
    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    if (!isEditable) {
      renderer.setTextNonSelectionColor(Color.gray);
      renderer.setTextSelectionColor(Color.gray);
    }
    tree.setCellRenderer(renderer);
    tree.setEditable(isEditable);
    SocietyComponent society = experiment.getSocietyComponent();
    if (society != null) {
      addSocietyComponent(society);
    }
    for (int i = 0, n = experiment.getRecipeComponentCount(); i < n; i++) {
      addRecipe(experiment.getRecipeComponent(i));
    }
    tree.expandNode(societies);
    tree.expandNode(recipes);
    model.addTreeModelListener(myTreeModelListener);
  }

  /**
   * Update society and recipes in the experiment when the user
   * modifies the tree.
   */
  private void reconcileExperimentNodes() {
    int nSocieties = societies.getChildCount();
    if (nSocieties > 1) {
      if (log.isErrorEnabled())
        log.error("More than one society in experiment.");
    } else if (nSocieties == 1) {
      SocietyComponent newSociety = (SocietyComponent) ((DefaultMutableTreeNode) societies.getChildAt(0)).getUserObject();
      //      society.setEditable(false); // so society editability tracks experiment editability
      SocietyComponent society = experiment.getSocietyComponent();
      if (society == null)
        experiment.addSocietyComponent(newSociety);
      else if (!society.equals(newSociety))
        if (log.isErrorEnabled())
          log.error("Attempted to add society to experiment that has a society.");
    } else if (nSocieties == 0) {
      experiment.removeSocietyComponent();
    }
    int nRecipes = recipes.getChildCount();
    RecipeComponent[] recipeAry = new RecipeComponent[nRecipes];
    for (int i = 0; i < nRecipes; i++) {
      recipeAry[i] =
        (RecipeComponent) ((DefaultMutableTreeNode) recipes.getChildAt(i)).getUserObject();
    }
    experiment.setRecipeComponents(recipeAry);
  }

  /**
   * Display the correct popup menu for "Societies", "Recipes" or 
   * one or more recipes.
   * If multiple objects are selected, 
   * then, if they're not all recipes, then select 
   * the one the mouse is pointing at.
   */
  private void doPopup(MouseEvent e) {
    TreePath[] selectionPaths = tree.getSelectionPaths();
    if (selectionPaths == null)
      return;
    boolean haveRecipes = false;
    for (int i = 0; i < selectionPaths.length; i++) {
      DefaultMutableTreeNode node =
	(DefaultMutableTreeNode) selectionPaths[i].getLastPathComponent();
      if (node.getUserObject() instanceof RecipeComponent)
        haveRecipes = true;
      else {
        haveRecipes = false;
        break;
      }
    }
    if (!haveRecipes) {
      TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
      // set the selected node to be the node the mouse is pointing at
      tree.setSelectionPath(selPath);
      DefaultMutableTreeNode popupNode =
        (DefaultMutableTreeNode) selPath.getLastPathComponent();
      if (popupNode == societies)
        societiesMenu.show(tree, e.getX(), e.getY());
      else if (popupNode == recipes)
        recipesMenu.show(tree, e.getX(), e.getY());
    } else
      popupMenu.show(tree, e.getX(), e.getY());
  }

  /**
   * Remove all children of the specified parent from the tree.
   */
  private void removeAllChildren(DefaultMutableTreeNode parent) {
    for (int i = 0; i < parent.getChildCount(); i++) {
//       DefaultMutableTreeNode node = 
// 	(DefaultMutableTreeNode)parent.getChildAt(i);
      // make removed society component editable again
//        Object userObject = node.getUserObject();
//        if (userObject != null &&
//  	  userObject instanceof ModifiableComponent)
//  	((ModifiableComponent)userObject).setEditable(true);
    }
    parent.removeAllChildren();
    model.nodeStructureChanged(parent);
  }

  /**
   * Remove selected items from the tree.
   */
  private void removeSelectedItems() {
    TreePath[] selectionPaths = tree.getSelectionPaths();
    if (selectionPaths == null)
      return;
    DefaultMutableTreeNode[] nodes = 
      new DefaultMutableTreeNode[selectionPaths.length];
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] =
	(DefaultMutableTreeNode) selectionPaths[i].getLastPathComponent();
    }
    for (int i = 0; i < nodes.length; i++) {
      model.removeNodeFromParent(nodes[i]);
      // make removed society component editable again
//        Object userObject = nodes[i].getUserObject();
//        if (userObject != null &&
//  	  userObject instanceof ModifiableComponent)
//  	((ModifiableComponent)userObject).setEditable(true);
    }
  }

  /**
   * Add society component to tree.
   */
  private void addSocietyComponent(SocietyComponent sc) {
    if (societies.getChildCount() != 0) {
      // Only 1 society supposed to be allowed
      System.err.println("attempt to add second society to an experiment");
      return;
    }
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(sc, false);
    model.insertNodeInto(node, societies, societies.getChildCount());
  }

  /**
   * Add recipe component to tree.
   */
  private void addRecipe(RecipeComponent recipe) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(recipe, false);
    model.insertNodeInto(node, recipes, recipes.getChildCount());
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

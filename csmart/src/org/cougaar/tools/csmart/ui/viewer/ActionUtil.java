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

package org.cougaar.tools.csmart.ui.viewer;

import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.tree.DefaultMutableTreeNode;

import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;

/**
 * Set of utilities for determining what tools & actions
 * should be enabled, based on what items are being currently used.
 */
public class ActionUtil {

  public static String CONFIGURE_ACTION = "Configure";
  public static String BUILD_ACTION = "Build";
  public static String RUN_ACTION = "Run";
  public static String DUPLICATE_ACTION = "Duplicate";
  public static String DELETE_ACTION = "Delete";
  public static String RENAME_ACTION = "Rename";
  public static String NEW_EXPERIMENT_ACTION = "New Experiment";
  public static String NEW_EXPERIMENT_FROM_DB_ACTION = "From Database";
  public static String NEW_EXPERIMENT_FROM_FILE_ACTION = "From File";
  public static String NEW_EXPERIMENT_FROM_UI_ACTION = "From User";
  public static String NEW_RECIPE_ACTION = "New Recipe";
  public static String NEW_FOLDER_ACTION = "New Folder";
  public static String DELETE_EXPERIMENT_FROM_DATABASE_ACTION = 
    "Delete Experiment From Database";
  public static String DELETE_RECIPE_FROM_DATABASE_ACTION = 
    "Delete Recipe From Database";
  
  /**
   * Enable/disable an action, based on the object selected.
   * @param action action to enable/disable
   * @param organizer used to get the selected object and children
   */

  public static void setActionAllowed(Action action,
                                      Organizer organizer) {
    String s = (String)action.getValue(Action.NAME);
    DefaultMutableTreeNode selectedNode = organizer.getSelectedNode();

    // If nothing is selected, some things are still legitimate
    if (selectedNode == null) {
      if (s.equals(DELETE_EXPERIMENT_FROM_DATABASE_ACTION) ||
          s.equals(DELETE_RECIPE_FROM_DATABASE_ACTION)) {
        action.setEnabled(true);
        return;
      } else {
	action.setEnabled(false);
        return;
      }
    }

    // can't do anything with nodes in an experiment being edited or run
    if (organizer.isNodeInUse(selectedNode)) {
      action.setEnabled(false);
      return;
    }

    // handle if selected object is root of workspace
    if (selectedNode.isRoot()) {
      if (s.equals(NEW_EXPERIMENT_ACTION) ||
          s.equals(NEW_RECIPE_ACTION) ||
          s.equals(NEW_FOLDER_ACTION) ||
          s.equals(RENAME_ACTION) ||
          s.equals(DELETE_EXPERIMENT_FROM_DATABASE_ACTION) ||
          s.equals(DELETE_RECIPE_FROM_DATABASE_ACTION)) {
        action.setEnabled(true);
        return;
      }
      action.setEnabled(false);
      return;
    } // end handling root

    Object selectedObject = selectedNode.getUserObject();

    if (selectedObject instanceof String) 
      action.setEnabled(isActionAllowedOnFolder(s, organizer, 
                                                organizer.getSelectedNode()));
    else if (selectedObject instanceof Experiment)
      action.setEnabled(isActionAllowedOnExperiment(s, organizer,
                                       (Experiment)selectedObject));
    else if (selectedObject instanceof SocietyComponent)
      action.setEnabled(isActionAllowedOnSociety(s, organizer,
                                   (SocietyComponent)selectedObject));
    else if (selectedObject instanceof RecipeComponent)
      action.setEnabled(isActionAllowedOnRecipe(s, organizer,
                            (RecipeComponent)selectedObject));
    else {
      System.out.println("ActionUtil: unhandled case: " + s);
      action.setEnabled(false);
    }
    return;
  }

  /**
   * New and rename actions are always allowed.
   * Delete is allowed on an empty folder.
   * Other actions are allowed on a folder if and only
   * they're allowed on at least one of its children.
   */
  private static boolean isActionAllowedOnFolder(String action,
                                                 Organizer organizer,
                                                 DefaultMutableTreeNode node) {
    if (action.equals(NEW_EXPERIMENT_ACTION) ||
        action.equals(NEW_RECIPE_ACTION) ||
        action.equals(NEW_FOLDER_ACTION) ||
        action.equals(RENAME_ACTION)) 
      return true;

    Enumeration nodes = node.breadthFirstEnumeration();
    nodes.nextElement(); // advance over first element which is the folder
    // can delete empty folder, but not root
    if (!nodes.hasMoreElements() && !node.isRoot()) 
      return action.equals(DELETE_ACTION);
    return false;
  }

  /**
   * Don't allow configuring society, or building,
   * deleting, renaming, or running an experiment,
   * if you're editing one of its components (i.e. a society or recipe).
   * Don't allow running an experiment, unless its runnable.
   * Don't allow deleting, renaming, configuring or building
   * if you're building or running the experiment.
   */
  private static boolean isActionAllowedOnExperiment(String action,
                                                     Organizer organizer,
                                                     Experiment experiment) {
    if (action.equals(DUPLICATE_ACTION))
      return true;
    SocietyComponent society = experiment.getSocietyComponent();
    if (society != null && !society.isEditable()) {
      return false;
    }
    RecipeComponent[] recipes = experiment.getRecipeComponents();
    for (int i = 0; i < recipes.length; i++) {
      if (!recipes[i].isEditable()) {
        return false;
      }
    }
    if (society == null) {
      if (action.equals(RUN_ACTION) ||
          action.equals(CONFIGURE_ACTION))
        return false;
    }
    if (action.equals(RUN_ACTION))
      return experiment.isRunnable();
    if (action.equals(DELETE_ACTION) || 
        action.equals(RENAME_ACTION) ||
        action.equals(CONFIGURE_ACTION) ||
        action.equals(BUILD_ACTION))
      return (!experiment.isEditInProgress() &&
              !experiment.isRunInProgress());
    return false;
  }

  /**
   * Don't allow duplicating, building an experiment, or deleting
   * if the user selected a society in an experiment.
   * Don't allow building, deleting, renaming, or configuring
   * if the society is being configured.
   * Don't allow configuring if the society is in an experiment
   * which is being configured or run.
   */
  private static boolean isActionAllowedOnSociety(String action,
                                                  Organizer organizer,
                                                  SocietyComponent society) {
    if (!society.isEditable())
      return false;
    if (isNodeInExperiment(organizer)) {
      if (action.equals(DUPLICATE_ACTION) ||
          action.equals(BUILD_ACTION) ||
          action.equals(DELETE_ACTION) ||
          action.equals(RUN_ACTION))
        return false;
    }
    if (action.equals(DUPLICATE_ACTION))
      return true;
    if (action.equals(DELETE_ACTION) ||
        action.equals(RENAME_ACTION))
      return (!CSMART.isSocietyInEditor(society));
    if (action.equals(CONFIGURE_ACTION) ||
        action.equals(BUILD_ACTION) ||
        action.equals(RUN_ACTION))
      return !organizer.isComponentInUse(society) &&
        !CSMART.isSocietyInEditor(society);
    return false;
  }

  /**
   * Don't allow duplicating or deleting
   * if the recipe is in an experiment.
   * Don't allow deleting, renaming, or configuring
   * if the recipe is being configured.
   * Don't allow configuring if the recipe is in an experiment
   * which is being configured or run.
   */
  private static boolean isActionAllowedOnRecipe(String action,
                                                 Organizer organizer,
                                                 RecipeComponent recipe) {
    if (!recipe.isEditable())
      return false;
    if (isNodeInExperiment(organizer)) {
      if (action.equals(DUPLICATE_ACTION) ||
          action.equals(DELETE_ACTION))
        return false;
    }
    if (action.equals(DUPLICATE_ACTION))
      return true;
    if (action.equals(DELETE_ACTION) ||
        action.equals(RENAME_ACTION))
      return (!CSMART.isRecipeInEditor(recipe));
    if (action.equals(CONFIGURE_ACTION))
      return !organizer.isComponentInUse(recipe) &&
        !CSMART.isRecipeInEditor(recipe);
    return false;
  }


  // return true if the user selected a node within an experiment
  private static boolean isNodeInExperiment(Organizer organizer) {
    DefaultMutableTreeNode node = organizer.getSelectedNode();
    if (node == null)
      return false;
    DefaultMutableTreeNode parentNode = 
      (DefaultMutableTreeNode)node.getParent();
    if (parentNode == null)
      return false;
    Object o = parentNode.getUserObject();
    if (o == null)
      return false;
    return (o instanceof Experiment);
  }
}

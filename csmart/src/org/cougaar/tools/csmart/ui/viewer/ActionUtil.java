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
import javax.swing.tree.DefaultMutableTreeNode;

import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;

/**
 * Set of utilities for determining what tools & actions
 * should be enabled, based on what items are being currently used.
 */
public class ActionUtil {

  /** Configuration String **/
  public static String CONFIGURE_ACTION = "Configure";

  /** Build String **/
  public static String BUILD_ACTION = "Build";

  /** Run String **/
  public static String RUN_ACTION = "Run";

  /** Duplicate String **/
  public static String DUPLICATE_ACTION = "Duplicate";

  /** Delete String **/
  public static String DELETE_ACTION = "Delete";

  /** Rename String **/
  public static String RENAME_ACTION = "Rename";

  /** New Experiment String **/
  public static String NEW_EXPERIMENT_ACTION = "New Experiment";

  public static String NEW_EXPERIMENT_FROM_DB_ACTION = "From Database";
  public static String NEW_EXPERIMENT_FROM_FILE_ACTION = "From File";
  public static String NEW_EXPERIMENT_FROM_UI_ACTION = "From User";
  //  public static String NEW_SOCIETY_ACTION = "New Society";
  public static String NEW_RECIPE_ACTION = "New Recipe";
  public static String NEW_FOLDER_ACTION = "New Folder";
  public static String DELETE_EXPERIMENT_FROM_DATABASE_ACTION = 
    "Delete Experiment From Database";
  public static String DELETE_RECIPE_FROM_DATABASE_ACTION = 
    "Delete Recipe From Database";
  
  /**
   * Return whether or not an action is allowed, based
   * on the object selected.
   * If the object selected is either root or a folder,
   * then if the caller is setting up a pop-up menu,
   * then the actions are specific to the selected object;
   * if the caller is setting up the top-level menu
   * or the buttons, then the actions are dependent on all the
   * experiments and recipes contained under the root or folder.
   * @param String action, as defined in this class
   * @param organizer used to get the selected object and children
   * @param doPopup true if action will be used in popup menu
   * @return boolean true if action allowed, else false
   */
//    public static boolean isActionAllowed(String action,
//                                          Organizer organizer,
//                                          boolean doPopup) {
//      // If nothing is selected, some things are still legitimate
//      if (organizer.getSelectedNode() == null) {
//        if (action.equals(DELETE_EXPERIMENT_FROM_DATABASE_ACTION) ||
//            action.equals(DELETE_RECIPE_FROM_DATABASE_ACTION)) {
//          return true;
//        } else {
//  	return false;
//        }
//      }

//      if (organizer.isNodeBeingEdited(organizer.getSelectedNode()))
//        return false; // can't do anything with nodes in an exp. being edited

//      if (organizer.getSelectedNode().isRoot()) {
//        if (action.equals(NEW_EXPERIMENT_ACTION) ||
//            action.equals(NEW_RECIPE_ACTION) ||
//            action.equals(NEW_FOLDER_ACTION) ||
//            action.equals(RENAME_ACTION) ||
//            action.equals(DELETE_EXPERIMENT_FROM_DATABASE_ACTION) ||
//            action.equals(DELETE_RECIPE_FROM_DATABASE_ACTION)) {
//          return true;
//        } else if (doPopup) {
//          return false;
//        } else {
//          // TODO: Configure and Build should only be enabled
//          // if they would be enabled for some component;
//          // you can only edit recipes and societies
//          // that are not being edited and are not
//          // in an experiment that is being edited;
//          // you can only build an experiment which is not being built
//          // and whose society and recipes are not being edited;
//          // unfortunately, this requires knowing the node for the component
//          // also, when the action is invoked,
//          // you need to apply the same logic as to which components
//          // are configured or built
//          if (action.equals(CONFIGURE_ACTION)) {
//            RecipeComponent[] recipes = organizer.getSelectedRecipes();
//            if (recipes != null) {
//              for (int i = 0; i < recipes.length; i++) 
//                if (!CSMART.isRecipeInEditor(recipes[i]))
//                  return true;
//            }
//            return false;
//          } else if (action.equals(BUILD_ACTION)) {
//            Experiment[] experiments = organizer.getSelectedExperiments();
//            if (experiments != null) {
//              for (int i = 0; i < experiments.length; i++)
//                if (!CSMART.isExperimentInEditor(experiments[i]))
//                  return true;
//            }
//            return false;
//          } else if (action.equals(RUN_ACTION)) {
//            Experiment[] experiments = organizer.getSelectedExperiments();
//            if (experiments != null) {
//              for (int i = 0; i < experiments.length; i++)
//                if (experiments[i].isRunnable())
//                  return true;
//            }
//            return false;
//          } else
//            return false;
//        }
//      } // end if selected object is root

//      Object selectedObject = organizer.getSelectedObject();

//      if (selectedObject instanceof String) { // folder selected
//        if (action.equals(DELETE_ACTION)) {
//          RecipeComponent[] recipes = organizer.getSelectedRecipes();
//          if (recipes != null) {
//            for (int i = 0; i < recipes.length; i++) 
//              if (CSMART.isRecipeInEditor(recipes[i]))
//                return false;
//          } 
//          Experiment[] experiments = organizer.getSelectedExperiments();
//          if (experiments != null) {
//            for (int i = 0; i < experiments.length; i++)
//              if (CSMART.isExperimentInEditor(experiments[i]) ||
//                  experiments[i].isRunInProgress())
//                return false;
//          }
//          return true;
//        }
//        else if (action.equals(NEW_EXPERIMENT_ACTION) ||
//                 action.equals(NEW_RECIPE_ACTION) ||
//                 action.equals(NEW_FOLDER_ACTION) ||
//                 action.equals(RENAME_ACTION)) {
//          return true;
//        } else {
//          if (action.equals(CONFIGURE_ACTION)) {
//            RecipeComponent[] recipes = organizer.getSelectedRecipes();
//            if (recipes != null) {
//              for (int i = 0; i < recipes.length; i++) 
//                if (!CSMART.isRecipeInEditor(recipes[i]))
//                  return true;
//            }
//            return false;
//          }
//          else if (action.equals(BUILD_ACTION)) {
//            Experiment[] experiments = organizer.getSelectedExperiments();
//            if (experiments != null) {
//              for (int i = 0; i < experiments.length; i++)
//                if (!CSMART.isExperimentInEditor(experiments[i]))
//                  return true;
//            }
//            return false;
//          }
//          else if (action.equals(RUN_ACTION)) {
//            Experiment[] experiments = organizer.getSelectedExperiments();
//            if (experiments != null) {
//              for (int i = 0; i < experiments.length; i++)
//                if (experiments[i].isRunnable())
//                  return true;
//            }
//            return false;
//          }
//          else return false;
//        }
//      } // end if selected object is folder

//      // don't allow configuring society, or building,
//      // deleting, renaming, or running an experiment,
//      // if you're editing one of its components (i.e. a society or recipe)
//      // don't allow running an experiment, unless its runnable
//      // don't allow deleting, renaming, configuring or building
//      // if you're editing or running the experiment
//      if (selectedObject instanceof Experiment) {
//        Experiment experiment = (Experiment)selectedObject;
//        if (action.equals(DUPLICATE_ACTION))
//          return true;
//        SocietyComponent society = experiment.getSocietyComponent();
//        if (CSMART.isSocietyInEditor(society))
//          return false;
//        RecipeComponent[] recipes = experiment.getRecipeComponents();
//        for (int i = 0; i < recipes.length; i++) {
//          if (CSMART.isRecipeInEditor(recipes[i]))
//            return false;
//        }
//        if (action.equals(RUN_ACTION))
//          return experiment.isRunnable();
//        if (action.equals(DELETE_ACTION) || 
//            action.equals(RENAME_ACTION) ||
//            action.equals(CONFIGURE_ACTION) ||
//            action.equals(BUILD_ACTION))
//          return (!experiment.isEditInProgress() &&
//                  !experiment.isRunInProgress());
//        return false;
//      } // end if selected object is experiment

//      // don't allow duplicating, building an experiment, or deleting
//      // if the society is in an experiment
//      // don't allow building, deleting, renaming, or configuring
//      // if the society is being configured
//      if (selectedObject instanceof SocietyComponent) {
//        SocietyComponent society = (SocietyComponent)selectedObject;
//        if (isNodeInExperiment(organizer)) {
//          if (action.equals(DUPLICATE_ACTION) ||
//              action.equals(BUILD_ACTION) ||
//              action.equals(DELETE_ACTION))
//            return false;
//        }
//        if (action.equals(DUPLICATE_ACTION))
//          return true;
//        else if (action.equals(BUILD_ACTION) ||
//                 action.equals(DELETE_ACTION) ||
//                 action.equals(RENAME_ACTION) ||
//                 action.equals(CONFIGURE_ACTION)) 
//          return (!CSMART.isSocietyInEditor(society));
//        else
//          return false;
//      } // end if selected object is society

//      if (selectedObject instanceof RecipeComponent) {
//        RecipeComponent recipe = (RecipeComponent)selectedObject;
//        if (isNodeInExperiment(organizer)) {
//          if (action.equals(DUPLICATE_ACTION) ||
//              action.equals(DELETE_ACTION))
//            return false;
//        }
//        if (action.equals(DUPLICATE_ACTION))
//          return true;
//        else if (action.equals(DELETE_ACTION) ||
//                 action.equals(RENAME_ACTION) ||
//                 action.equals(CONFIGURE_ACTION))
//          return (!CSMART.isRecipeInEditor(recipe));
//        else
//          return false;
//      } // end if selected object is recipe

//      System.out.println("Util: unhandled case: " + action);
//      return false;
//    }

  public static boolean isActionAllowed(String action,
                                        Organizer organizer,
                                        boolean doPopup) {
    // If nothing is selected, some things are still legitimate
    if (organizer.getSelectedNode() == null) {
      if (action.equals(DELETE_EXPERIMENT_FROM_DATABASE_ACTION) ||
          action.equals(DELETE_RECIPE_FROM_DATABASE_ACTION)) {
        return true;
      } else {
	return false;
      }
    }

    // can't do anything with nodes in an experiment being edited
    if (organizer.isNodeBeingEdited(organizer.getSelectedNode()))
      return false; 

    // handle if selected object is root of workspace
    if (organizer.getSelectedNode().isRoot()) {
      if (action.equals(NEW_EXPERIMENT_ACTION) ||
          action.equals(NEW_RECIPE_ACTION) ||
          action.equals(NEW_FOLDER_ACTION) ||
          action.equals(RENAME_ACTION) ||
          action.equals(DELETE_EXPERIMENT_FROM_DATABASE_ACTION) ||
          action.equals(DELETE_RECIPE_FROM_DATABASE_ACTION)) 
        return true;
      //      if (doPopup)
      //        return false;
      return false;
      //      DefaultMutableTreeNode rootNode = 
      //        (DefaultMutableTreeNode)organizer.getSelectedNode();
      //      return isActionAllowedOnFolder(action, organizer, rootNode);
    } // end handling root

    Object selectedObject = organizer.getSelectedObject();

    if (selectedObject instanceof String) 
      return isActionAllowedOnFolder(action, organizer, 
                                     organizer.getSelectedNode());
    if (selectedObject instanceof Experiment)
      return isActionAllowedOnExperiment(action, organizer,
                                         (Experiment)selectedObject);
    if (selectedObject instanceof SocietyComponent)
      return isActionAllowedOnSociety(action, organizer,
                                      (SocietyComponent)selectedObject);
    if (selectedObject instanceof RecipeComponent)
      return isActionAllowedOnRecipe(action, organizer,
                                     (RecipeComponent)selectedObject);
    System.out.println("Util: unhandled case: " + action);
    return false;
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

//      boolean actionAllowed = false;
//      while (nodes.hasMoreElements()) {
//        DefaultMutableTreeNode nextNode = 
//          (DefaultMutableTreeNode)nodes.nextElement();
//        Object o = nextNode.getUserObject();
//        if (o instanceof String) 
//          actionAllowed = 
//            isActionAllowedOnFolder(action, organizer, nextNode);
//        else if (o instanceof Experiment)
//          actionAllowed =
//            isActionAllowedOnExperiment(action, organizer, (Experiment)o);
//        else if (o instanceof SocietyComponent)
//          actionAllowed =
//            isActionAllowedOnSociety(action, organizer, (SocietyComponent)o);
//        else if (o instanceof RecipeComponent)
//          actionAllowed =
//            isActionAllowedOnRecipe(action, organizer, (RecipeComponent)o);
//        if (actionAllowed)
//          return true;
//      }
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
    if (CSMART.isSocietyInEditor(society))
      return false;
    RecipeComponent[] recipes = experiment.getRecipeComponents();
    for (int i = 0; i < recipes.length; i++) {
      if (CSMART.isRecipeInEditor(recipes[i]))
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
   * if the society is in an experiment.
   * Don't allow building, deleting, renaming, or configuring
   * if the society is being configured.
   */
  private static boolean isActionAllowedOnSociety(String action,
                                                  Organizer organizer,
                                                  SocietyComponent society) {
    if (isNodeInExperiment(organizer)) {
      if (action.equals(DUPLICATE_ACTION) ||
          action.equals(BUILD_ACTION) ||
          action.equals(DELETE_ACTION))
        return false;
    }
    if (action.equals(DUPLICATE_ACTION))
      return true;
    if (action.equals(BUILD_ACTION) ||
        action.equals(DELETE_ACTION) ||
        action.equals(RENAME_ACTION) ||
        action.equals(CONFIGURE_ACTION)) 
      return (!CSMART.isSocietyInEditor(society));
    return false;
  }

  /**
   * Don't allow duplicating or deleting
   * if the recipe is in an experiment.
   * Don't allow deleting, renaming, or configuring
   * if the recipe is being configured.
   */
  private static boolean isActionAllowedOnRecipe(String action,
                                                 Organizer organizer,
                                                 RecipeComponent recipe) {
    if (isNodeInExperiment(organizer)) {
      if (action.equals(DUPLICATE_ACTION) ||
          action.equals(DELETE_ACTION))
        return false;
    }
    if (action.equals(DUPLICATE_ACTION))
      return true;
    if (action.equals(DELETE_ACTION) ||
        action.equals(RENAME_ACTION) ||
        action.equals(CONFIGURE_ACTION))
      return (!CSMART.isRecipeInEditor(recipe));
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

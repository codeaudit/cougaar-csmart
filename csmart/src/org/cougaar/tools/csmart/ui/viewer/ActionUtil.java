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
  public static String NEW_SOCIETY_ACTION = "New Society";
  public static String NEW_RECIPE_ACTION = "New Recipe";
  public static String NEW_FOLDER_ACTION = "New Folder";
  public static String DELETE_EXPERIMENT_FROM_DATABASE_ACTION = 
    "Delete Experiment From Database";
  public static String DELETE_RECIPE_FROM_DATABASE_ACTION = 
    "Delete Recipe From Database";
  public static String SAVE_TO_DATABASE_ACTION = "Save To Database";
  
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

    if (organizer.getSelectedNode().isRoot()) {
      if (action.equals(NEW_EXPERIMENT_ACTION) ||
          action.equals(NEW_SOCIETY_ACTION) ||
          action.equals(NEW_RECIPE_ACTION) ||
          action.equals(NEW_FOLDER_ACTION) ||
          action.equals(RENAME_ACTION) ||
          action.equals(DELETE_EXPERIMENT_FROM_DATABASE_ACTION) ||
          action.equals(DELETE_RECIPE_FROM_DATABASE_ACTION)) {
        return true;
      } else if (doPopup) {
        return false;
      } else {
        if (action.equals(CONFIGURE_ACTION)) {
          RecipeComponent[] recipes = organizer.getSelectedRecipes();
          if (recipes != null) {
            for (int i = 0; i < recipes.length; i++) 
              if (!CSMART.isRecipeInEditor(recipes[i]))
                return true;
          }
          return false;
        } else if (action.equals(BUILD_ACTION)) {
          Experiment[] experiments = organizer.getSelectedExperiments();
          if (experiments != null) {
            for (int i = 0; i < experiments.length; i++)
              if (!CSMART.isExperimentInEditor(experiments[i]))
                return true;
          }
          return false;
        } else if (action.equals(RUN_ACTION)) {
          Experiment[] experiments = organizer.getSelectedExperiments();
          if (experiments != null) {
            for (int i = 0; i < experiments.length; i++)
              if (experiments[i].isRunnable())
                return true;
          }
          return false;
        } else
          return false;
      }
    } // end if selected object is root

    Object selectedObject = organizer.getSelectedObject();

    if (selectedObject instanceof String) { // folder selected
      if (action.equals(DELETE_ACTION)) {
        RecipeComponent[] recipes = organizer.getSelectedRecipes();
        if (recipes != null) {
          for (int i = 0; i < recipes.length; i++) 
            if (CSMART.isRecipeInEditor(recipes[i]))
              return false;
        } 
        Experiment[] experiments = organizer.getSelectedExperiments();
        if (experiments != null) {
          for (int i = 0; i < experiments.length; i++)
            if (CSMART.isExperimentInEditor(experiments[i]) ||
                experiments[i].isRunInProgress())
              return false;
        }
        return true;
      }
      else if (action.equals(NEW_EXPERIMENT_ACTION) ||
               action.equals(NEW_SOCIETY_ACTION) ||
               action.equals(NEW_RECIPE_ACTION) ||
               action.equals(NEW_FOLDER_ACTION) ||
               action.equals(RENAME_ACTION)) {
        return true;
      } else {
        if (action.equals(CONFIGURE_ACTION)) {
          RecipeComponent[] recipes = organizer.getSelectedRecipes();
          if (recipes != null) {
            for (int i = 0; i < recipes.length; i++) 
              if (!CSMART.isRecipeInEditor(recipes[i]))
                return true;
          }
          return false;
        }
        else if (action.equals(BUILD_ACTION)) {
          Experiment[] experiments = organizer.getSelectedExperiments();
          if (experiments != null) {
            for (int i = 0; i < experiments.length; i++)
              if (!CSMART.isExperimentInEditor(experiments[i]))
                return true;
          }
          return false;
        }
        else if (action.equals(RUN_ACTION)) {
          Experiment[] experiments = organizer.getSelectedExperiments();
          if (experiments != null) {
            for (int i = 0; i < experiments.length; i++)
              if (experiments[i].isRunnable())
                return true;
          }
          return false;
        }
        else return false;
      }
    } // end if selected object is folder

    // TODO: probably need to make running configuration tool more selective
    // for now, if experiment is selected, allow user to
    // configure the society in it
    if (selectedObject instanceof Experiment) {
      Experiment experiment = (Experiment)selectedObject;
      if (action.equals(DUPLICATE_ACTION) ||
          action.equals(SAVE_TO_DATABASE_ACTION)) {
        return true;
      } else if (action.equals(CONFIGURE_ACTION)) {
        return true;
      } else if (action.equals(BUILD_ACTION)) {
        return !CSMART.isExperimentInEditor(experiment);
      } else if (action.equals(RUN_ACTION)) {
        return experiment.isRunnable();
      } else if (action.equals(DELETE_ACTION) || 
                 action.equals(RENAME_ACTION)) {
        return (!experiment.isEditInProgress() &&
                !experiment.isRunInProgress());
      } else
        return false;
    } // end if selected object is experiment


    if (selectedObject instanceof SocietyComponent) {
      SocietyComponent society = (SocietyComponent)selectedObject;
      if (action.equals(DUPLICATE_ACTION))
        return true;
      if (action.equals(SAVE_TO_DATABASE_ACTION)) {
        return false;
      } else if (action.equals(BUILD_ACTION)) {
        return !CSMART.isSocietyInEditor(society);
      } else if (action.equals(DELETE_ACTION) ||
                 action.equals(RENAME_ACTION) ||
                 action.equals(CONFIGURE_ACTION)) {
        return (!CSMART.isSocietyInEditor(society));
      } else if (action.equals(BUILD_ACTION)) {
 // TODO: should return true only if society not in experiment????
        return true;
      } else
        return false;
    } // end if selected object is society

    if (selectedObject instanceof RecipeComponent) {
      RecipeComponent recipe = (RecipeComponent)selectedObject;
      if (action.equals(DUPLICATE_ACTION) ||
          action.equals(SAVE_TO_DATABASE_ACTION))
        return true;
      else if (action.equals(DELETE_ACTION) ||
               action.equals(RENAME_ACTION) ||
               action.equals(CONFIGURE_ACTION))
        return (!CSMART.isRecipeInEditor(recipe));
      else
        return false;
    } // end if selected object is recipe

    System.out.println("Util: unhandled case: " + action);
    return false;
  }
}

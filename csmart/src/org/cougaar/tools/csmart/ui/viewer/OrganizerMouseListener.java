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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.experiment.Experiment;

public class OrganizerMouseListener extends MouseAdapter {
  private Organizer organizer;
  private OrganizerTree workspace;
  private JPopupMenu societyMenu = new JPopupMenu();
  private JPopupMenu recipeMenu = new JPopupMenu();
  private JPopupMenu experimentMenu = new JPopupMenu();
  private JPopupMenu folderMenu = new JPopupMenu();
  private JPopupMenu rootMenu = new JPopupMenu();
  
  private JMenu newExperimentMenu = new JMenu(ActionUtil.NEW_EXPERIMENT_ACTION);
  private JMenu newExperimentInFolderMenu = new JMenu(ActionUtil.NEW_EXPERIMENT_ACTION);
  private JMenu newRecipeMenu = new JMenu(ActionUtil.NEW_RECIPE_ACTION);
  private JMenu newRecipeInFolderMenu = new JMenu(ActionUtil.NEW_RECIPE_ACTION);

  /**
   * Construct a mouse listener for the workspace tree.
   * @param organizer the organizer for which this is the mouse listener
   * @param workspace the workspace tree for which this is the mouse listener
   */
  public OrganizerMouseListener(Organizer organizer,
                                OrganizerTree workspace) {
    this.organizer = organizer;
    this.workspace = workspace;

    // define pop-up menus
    Object[] rootMenuItems = {
      newExperimentMenu,
      newRecipeMenu,
      organizer.newFolderAction,
      organizer.renameWorkspaceAction,
      organizer.deleteExperimentFromDatabaseAction,
      organizer.deleteRecipeFromDatabaseAction
    };

    Object[] experimentMenuItems = {
      organizer.buildExperimentAction,
      organizer.runExperimentAction,
      organizer.duplicateAction,
      organizer.deleteExperimentAction,
      organizer.renameExperimentAction,
      organizer.configureAction,
      organizer.saveAction
    };

    Object[] societyMenuItems = {
      organizer.configureAction,
      organizer.buildExperimentAction,
      organizer.duplicateAction,
      organizer.deleteSocietyAction,
      organizer.renameSocietyAction,
      organizer.saveAction
    };

    Object[] recipeMenuItems = {
      organizer.configureAction,
      organizer.duplicateAction,
      organizer.deleteRecipeAction,
      organizer.renameRecipeAction,
      organizer.saveAction
    };

    Object[] folderMenuItems = {
      newExperimentInFolderMenu,
      newRecipeInFolderMenu,
      organizer.newFolderAction,
      organizer.deleteFolderAction,
      organizer.renameFolderAction
    };

    // set up experiment submenus
    for (int i = 0; i < organizer.newExperimentActions.length; i++) {
      newExperimentMenu.add(organizer.newExperimentActions[i]);
      newExperimentInFolderMenu.add(organizer.newExperimentActions[i]);
    }
    // set up recipe submenus
    for (int i = 0; i < organizer.newRecipeActions.length; i++) {
      newRecipeMenu.add(organizer.newRecipeActions[i]);
      newRecipeInFolderMenu.add(organizer.newRecipeActions[i]);
    }
    for (int i = 0; i < rootMenuItems.length; i++) {
      if (rootMenuItems[i] instanceof Action)
        rootMenu.add((Action)rootMenuItems[i]);
      else
        rootMenu.add((JMenuItem)rootMenuItems[i]);
    }
    for (int i = 0; i < experimentMenuItems.length; i++) 
      experimentMenu.add((Action)experimentMenuItems[i]);
    for (int i = 0; i < societyMenuItems.length; i++)
      societyMenu.add((Action)societyMenuItems[i]);
    for (int i = 0; i < recipeMenuItems.length; i++)
      recipeMenu.add((Action)recipeMenuItems[i]);
    for (int i = 0; i < folderMenuItems.length; i++) {
      if (folderMenuItems[i] instanceof Action)
        folderMenu.add((Action)folderMenuItems[i]);
      else
        folderMenu.add((JMenuItem)folderMenuItems[i]);
    }
  }

  /**
   * If the event is a popup trigger, then display the appropriate
   * popup menu which depends on the type of object selected in the workspace
   * (i.e. an experiment, society, recipe, etc.)
   * @param e the mouse event
   */
  public void mouseClicked(MouseEvent e) {
    if (e.isPopupTrigger()) doPopup(e);
  }
  
  /**
   * If the event is a popup trigger, then display the appropriate
   * popup menu which depends on the type of object selected in the workspace
   * (i.e. an experiment, society, recipe, etc.)
   * @param e the mouse event
   */
  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger()) doPopup(e);
  }
  
  /**
   * If the event is a popup trigger, then display the appropriate
   * popup menu which depends on the type of object selected in the workspace
   * (i.e. an experiment, society, recipe, etc.)
   * @param e the mouse event
   */
  public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger()) doPopup(e);
  }


  /**
   * Popup appropriate menu, but first disable appropriate commands
   * if component is not editable.
   */
  private void doPopup(MouseEvent e) {
    TreePath selPath = workspace.getPathForLocation(e.getX(), e.getY());
    if (selPath == null) return;
    // set the selected node to be the node the mouse is pointing at
    workspace.setSelectionPath(selPath);
    DefaultMutableTreeNode selectedNode =
      (DefaultMutableTreeNode) selPath.getLastPathComponent();
    Object o = selectedNode.getUserObject();
    if (organizer.isNodeInUse(selectedNode))
      return; // can't do anything with these
    if (selectedNode.isRoot()) {
      configureMenu(rootMenu);
      rootMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof SocietyComponent) {
      configureMenu(societyMenu);
      societyMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof RecipeComponent) {
      configureMenu(recipeMenu);
      recipeMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof Experiment) {
      configureMenu(experimentMenu);
      experimentMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof String) {
      configureMenu(folderMenu);
      folderMenu.show(workspace, e.getX(), e.getY());
    }
  }
  
  private void configureMenu(JPopupMenu menu) {
    int n = menu.getComponentCount();
    for (int i = 0; i < n; i++) {
      Component c = menu.getComponent(i);
      if (c != null && c instanceof JMenuItem) {
        Action action = ((JMenuItem)c).getAction();
        // skip submenus which have null actions
        if (action != null)
          ActionUtil.setActionAllowed(action, organizer);
      }
    }
  }

}

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
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
      if (c != null) {
        if (c instanceof JMenu)
          enableActions((JMenu)c);
        else if (c instanceof JMenuItem) {
          Action action = ((JMenuItem)c).getAction();
          if (action != null)
            ActionUtil.setActionAllowed(action, organizer);
        }
      }
    }
  }

  private boolean enableActions(JMenu menu) {
    boolean haveEnabledActions = false;
    int n = menu.getItemCount();
    for (int i = 0; i < n; i++) {
      JMenuItem menuItem = menu.getItem(i);
      if (menuItem == null)
        continue;
      if (menuItem instanceof JMenu) {
        if (enableActions((JMenu)menuItem)) {
          menuItem.setEnabled(true);
          haveEnabledActions = true;
        } else
          menuItem.setEnabled(false);
      } else {
        Action action = menuItem.getAction();
        if (action != null) {
          ActionUtil.setActionAllowed(action, organizer);
          if (action.isEnabled())
            haveEnabledActions = true;
        }
      }
    }
    return haveEnabledActions;
  }

  private boolean enableActions(JMenuItem c) {
    boolean haveEnabledActions = false;
    if (c instanceof JMenu) {
      if (enableActions(c)) {
        c.setEnabled(true);
        haveEnabledActions = true;
      } else
        c.setEnabled(false);
    } else {
      Action action = ((JMenuItem)c).getAction();
      if (action != null) {
        ActionUtil.setActionAllowed(action, organizer);
        if (action.isEnabled())
          haveEnabledActions = true;
      }
    }
    return haveEnabledActions;
  }
}

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
  
  // Define actions for use on menus

  private Action[] newExperimentActions = {
    new AbstractAction(ActionUtil.NEW_EXPERIMENT_FROM_DB_ACTION) {
        public void actionPerformed(ActionEvent e) {
          organizer.selectExperimentFromDatabase();
        }
      },
    new AbstractAction(ActionUtil.NEW_EXPERIMENT_FROM_FILE_ACTION) {
        public void actionPerformed(ActionEvent e) {
          organizer.createExperimentFromFile();
        }
      },
    new AbstractAction(ActionUtil.NEW_EXPERIMENT_FROM_UI_ACTION) {
        public void actionPerformed(ActionEvent e) {
          organizer.createExperimentFromUI();
        }
      }
  };

  private Action newFolderAction = 
    new AbstractAction(ActionUtil.NEW_FOLDER_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.newFolder();
	}
      };

  private Action renameWorkspaceAction =
    new AbstractAction(ActionUtil.RENAME_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameWorkspace();
	}
      };

  private AbstractAction deleteExperimentFromDatabaseAction =
    new AbstractAction(ActionUtil.DELETE_EXPERIMENT_FROM_DATABASE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        organizer.deleteExperimentFromDatabase();
      }
    };

  private AbstractAction deleteRecipeFromDatabaseAction =
    new AbstractAction(ActionUtil.DELETE_RECIPE_FROM_DATABASE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        organizer.deleteRecipeFromDatabase();
      }
    };

  private AbstractAction buildExperimentAction =
    new AbstractAction(ActionUtil.BUILD_ACTION, 
                       new ImageIcon(getClass().getResource("EB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  organizer.startExperimentBuilder();
	}
      };

  private AbstractAction runExperimentAction =
    new AbstractAction(ActionUtil.RUN_ACTION, 
                       new ImageIcon(getClass().getResource("EC16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  organizer.startConsole();
	}
      };

  private AbstractAction duplicateAction =
    new AbstractAction(ActionUtil.DUPLICATE_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.duplicate();
	}
      };

  private AbstractAction deleteExperimentAction =
    new AbstractAction(ActionUtil.DELETE_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.deleteExperiment();
	}
      };

  private AbstractAction renameExperimentAction =
    new AbstractAction(ActionUtil.RENAME_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameExperiment();
	}
      };

  private AbstractAction configureAction =
    new AbstractAction(ActionUtil.CONFIGURE_ACTION, 
                       new ImageIcon(getClass().getResource("SB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  organizer.startBuilder();
	}
      };

  private AbstractAction deleteRecipeAction =
    new AbstractAction(ActionUtil.DELETE_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.deleteRecipe();
	}
      };

  private AbstractAction renameRecipeAction =
    new AbstractAction(ActionUtil.RENAME_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameRecipe();
	}
      };

  private AbstractAction deleteFolderAction =
    new AbstractAction(ActionUtil.DELETE_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.deleteFolder();
	}
      };

  private AbstractAction renameFolderAction =
    new AbstractAction(ActionUtil.RENAME_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameFolder();
	}
      };

  private AbstractAction deleteSocietyAction =
    new AbstractAction(ActionUtil.DELETE_ACTION) {
        public void actionPerformed(ActionEvent e) {
          organizer.deleteSociety();
        }
      };

  private AbstractAction renameSocietyAction =
    new AbstractAction(ActionUtil.RENAME_ACTION) {
        public void actionPerformed(ActionEvent e) {
          organizer.renameSociety();
        }
      };

  private Action[] newRecipeActions = {
    new AbstractAction("From Database") {
	public void actionPerformed(ActionEvent e) {
          organizer.selectRecipeFromDatabase();
  	}
    },
    new AbstractAction("Built In") {
	public void actionPerformed(ActionEvent e) {
	  organizer.newRecipe();
	}
    }
  };

  private JMenu newExperimentMenu = new JMenu(ActionUtil.NEW_EXPERIMENT_ACTION);
  private JMenu newExperimentInFolderMenu = new JMenu(ActionUtil.NEW_EXPERIMENT_ACTION);
  private JMenu newRecipeMenu = new JMenu(ActionUtil.NEW_RECIPE_ACTION);
  private JMenu newRecipeInFolderMenu = new JMenu(ActionUtil.NEW_RECIPE_ACTION);

  // define pop-up menus
  private Object[] rootMenuItems = {
    newExperimentMenu,
    newRecipeMenu,
    newFolderAction,
    renameWorkspaceAction,
    deleteExperimentFromDatabaseAction,
    deleteRecipeFromDatabaseAction
  };

  private Object[] experimentMenuItems = {
    buildExperimentAction,
    runExperimentAction,
    duplicateAction,
    deleteExperimentAction,
    renameExperimentAction,
  };

  private Object[] societyMenuItems = {
    configureAction,
    buildExperimentAction,
    duplicateAction,
    deleteSocietyAction,
    renameSocietyAction
  };

  private Object[] recipeMenuItems = {
    configureAction,
    duplicateAction,
    deleteRecipeAction,
    renameRecipeAction,
  };

  private Object[] folderMenuItems = {
    newExperimentInFolderMenu,
    newRecipeInFolderMenu,
    newFolderAction,
    deleteFolderAction,
    renameFolderAction
  };

  /**
   * Construct a mouse listener for the workspace tree.
   * @param organizer the organizer for which this is the mouse listener
   * @param workspace the workspace tree for which this is the mouse listener
   */
  public OrganizerMouseListener(Organizer organizer,
                                OrganizerTree workspace) {
    this.organizer = organizer;
    this.workspace = workspace;
    // set up experiment submenus
    for (int i = 0; i < newExperimentActions.length; i++) {
      newExperimentMenu.add(newExperimentActions[i]);
      newExperimentInFolderMenu.add(newExperimentActions[i]);
    }
    // set up recipe submenus
    for (int i = 0; i < newRecipeActions.length; i++) {
      newRecipeMenu.add(newRecipeActions[i]);
      newRecipeInFolderMenu.add(newRecipeActions[i]);
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
        String s = ((JMenuItem)c).getActionCommand();
        ((JMenuItem)c).setEnabled(ActionUtil.isActionAllowed(s, organizer, true));
      }
    }
  }

}

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

package org.cougaar.tools.csmart.ui.viewer;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

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
  private Action newSocietyAction = new AbstractAction("New Society") {
      public void actionPerformed(ActionEvent e) {
        organizer.newSociety();
      }
    };
  private Action[] rootAction = {
//      new AbstractAction("New Society") {
//  	public void actionPerformed(ActionEvent e) {
//  	  organizer.newSociety();
//  	}
//        },
    newSocietyAction,
    new AbstractAction(ActionUtil.NEW_FOLDER_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.newFolder();
	}
      },
    new AbstractAction(ActionUtil.RENAME_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameWorkspace();
	}
      },
    new AbstractAction(ActionUtil.DELETE_EXPERIMENT_FROM_DATABASE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        organizer.deleteExperimentFromDatabase();
      }
    },
    new AbstractAction(ActionUtil.DELETE_RECIPE_FROM_DATABASE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        organizer.deleteRecipeFromDatabase();
      }
    }
  };
  private Action[] newExperimentActions = {
    new AbstractAction("From Database") {
  	public void actionPerformed(ActionEvent e) {
          organizer.selectExperimentFromDatabase();
    	}
    },
    new AbstractAction("Built In") {
	public void actionPerformed(ActionEvent e) {
	  organizer.newExperiment();
	}
    }
  };
  private Action[] experimentAction = {
    new AbstractAction(ActionUtil.BUILD_ACTION, new ImageIcon(getClass().getResource("EB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  organizer.startExperimentBuilder();
	}
      },
    new AbstractAction(ActionUtil.RUN_ACTION, new ImageIcon(getClass().getResource("EC16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  organizer.startConsole();
	}
      },
    new AbstractAction(ActionUtil.DUPLICATE_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.duplicate();
	}
      },
    new AbstractAction(ActionUtil.DELETE_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.deleteExperiment();
	}
      },
    new AbstractAction(ActionUtil.RENAME_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameExperiment();
	}
      }
  };
  private Action[] societyAction = {
    new AbstractAction(ActionUtil.CONFIGURE_ACTION, new ImageIcon(getClass().getResource("SB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  organizer.startBuilder();
	}
      },
    new AbstractAction("Build Experiment",
		       new ImageIcon(getClass().getResource("EB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  organizer.startExperimentBuilder();
	}
      },
    new AbstractAction("Run Experiment", new ImageIcon(getClass().getResource("EC16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  organizer.startConsole();
	}
      },
    new AbstractAction(ActionUtil.DUPLICATE_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.duplicate();
	}
      },
    new AbstractAction(ActionUtil.DELETE_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.deleteSociety();
	}
      },
    new AbstractAction(ActionUtil.RENAME_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameSociety();
	}
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
  private Action[] recipeAction = {
    new AbstractAction(ActionUtil.CONFIGURE_ACTION, new ImageIcon(getClass().getResource("SB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  organizer.startBuilder();
	}
      },
    new AbstractAction(ActionUtil.DUPLICATE_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.duplicate();
	}
      },
    new AbstractAction(ActionUtil.DELETE_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.deleteRecipe();
	}
      },
    new AbstractAction(ActionUtil.RENAME_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameRecipe();
	}
      },
    new AbstractAction(ActionUtil.SAVE_TO_DATABASE_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.saveRecipe();
	}
      }
  };
  private Action[] folderAction = {
    newSocietyAction,
//      new AbstractAction("New Society") {
//  	public void actionPerformed(ActionEvent e) {
//            organizer.newSociety();
//  	}
//        },
//      new AbstractAction("New Recipe") {
//  	public void actionPerformed(ActionEvent e) {
//  	  organizer.newRecipe();
//  	}
//        },
    new AbstractAction(ActionUtil.NEW_FOLDER_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.newFolder();
	}
      },
    new AbstractAction(ActionUtil.DELETE_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.deleteFolder();
	}
      },
    new AbstractAction(ActionUtil.RENAME_ACTION) {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameFolder();
	}
      }
  };

  public OrganizerMouseListener(Organizer organizer,
                                OrganizerTree workspace) {
    this.organizer = organizer;
    this.workspace = workspace;
    newSocietyAction.setEnabled(false); // disable creating builtin societies
    JMenu newExperimentMenu = new JMenu(ActionUtil.NEW_EXPERIMENT_ACTION);
    for (int i = 0; i < newExperimentActions.length; i++) {
      newExperimentMenu.add(newExperimentActions[i]);
    }
    rootMenu.add(newExperimentMenu);
    JMenu newRecipeMenu = new JMenu(ActionUtil.NEW_RECIPE_ACTION);
    for (int i = 0; i < newRecipeActions.length; i++) {
      newRecipeMenu.add(newRecipeActions[i]);
    }
    rootMenu.add(newRecipeMenu);
    for (int i = 0; i < rootAction.length; i++) {
      rootMenu.add(rootAction[i]);
    }
    for (int i = 0; i < societyAction.length; i++) {
      societyMenu.add(societyAction[i]);
    }
    for (int i = 0; i < recipeAction.length; i++) {
      recipeMenu.add(recipeAction[i]);
    }
    for (int i = 0; i < experimentAction.length; i++) {
      experimentMenu.add(experimentAction[i]);
    }
    JMenu newExperimentInFolderMenu = new JMenu(ActionUtil.NEW_EXPERIMENT_ACTION);
    for (int i = 0; i < newExperimentActions.length; i++) {
      newExperimentInFolderMenu.add(newExperimentActions[i]);
    }
    folderMenu.add(newExperimentInFolderMenu);
    JMenu newRecipeInFolderMenu = new JMenu(ActionUtil.NEW_RECIPE_ACTION);
    for (int i = 0; i < newRecipeActions.length; i++) {
      newRecipeInFolderMenu.add(newRecipeActions[i]);
    }
    folderMenu.add(newRecipeInFolderMenu);
    for (int i = 0; i < folderAction.length; i++) {
      folderMenu.add(folderAction[i]);
    }
  }

  public void mouseClicked(MouseEvent e) {
    if (e.isPopupTrigger()) doPopup(e);
  }
  
  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger()) doPopup(e);
  }
  
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
      configureRootMenu();
      rootMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof SocietyComponent) {
      configureSocietyMenu();
      societyMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof RecipeComponent) {
      configureRecipeMenu();
      recipeMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof Experiment) {
      configureExperimentMenu();
      experimentMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof String) {
      configureFolderMenu();
      folderMenu.show(workspace, e.getX(), e.getY());
    }
  }
  
  private void configureRootMenu() {
    for (int i = 0; i < rootAction.length; i++) {
      String s = (String)rootAction[i].getValue(Action.NAME);
      rootAction[i].setEnabled(ActionUtil.isActionAllowed(s, organizer, true));
    }
  }

  private void configureFolderMenu() {
    for (int i = 0; i < folderAction.length; i++) {
      String s = (String)folderAction[i].getValue(Action.NAME);
      folderAction[i].setEnabled(ActionUtil.isActionAllowed(s, organizer, true));
    }
  }

  private void configureExperimentMenu() {
    for (int i = 0; i < experimentAction.length; i++) {
      String s = (String)experimentAction[i].getValue(Action.NAME);
      experimentAction[i].setEnabled(ActionUtil.isActionAllowed(s, organizer, true));
    }
  }

  private void configureSocietyMenu() {
  }

  private void configureRecipeMenu() {
    for (int i = 0; i < recipeAction.length; i++) {
      String s = (String)recipeAction[i].getValue(Action.NAME);
      recipeAction[i].setEnabled(ActionUtil.isActionAllowed(s, organizer, true));
    }
  }

}

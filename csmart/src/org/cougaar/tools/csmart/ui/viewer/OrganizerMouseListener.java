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
  private JPopupMenu treeMenu = new JPopupMenu();
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
    new AbstractAction("New Folder") {
	public void actionPerformed(ActionEvent e) {
	  organizer.newFolder();
	}
      },
    new AbstractAction("Rename") {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameWorkspace();
	}
      },
    new AbstractAction("Delete Experiment From Database") {
      public void actionPerformed(ActionEvent e) {
        organizer.deleteExperimentFromDatabase();
      }
    },
    new AbstractAction("Delete Recipe From Database") {
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
    new AbstractAction("Configure", new ImageIcon(getClass().getResource("EB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  organizer.startExperimentBuilder();
	}
      },
    new AbstractAction("Run", new ImageIcon(getClass().getResource("EC16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  organizer.startConsole();
	}
      },
    new AbstractAction("Duplicate") {
	public void actionPerformed(ActionEvent e) {
	  organizer.duplicate();
	}
      },
    new AbstractAction("Delete") {
	public void actionPerformed(ActionEvent e) {
	  organizer.deleteExperiment();
	}
      },
    new AbstractAction("Rename") {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameExperiment();
	}
      }
  };
  private Action[] societyAction = {
    new AbstractAction("Configure", new ImageIcon(getClass().getResource("SB16.gif"))) {
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
    new AbstractAction("Duplicate") {
	public void actionPerformed(ActionEvent e) {
	  organizer.duplicate();
	}
      },
    new AbstractAction("Delete") {
	public void actionPerformed(ActionEvent e) {
	  organizer.deleteSociety();
	}
      },
    new AbstractAction("Rename") {
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
    new AbstractAction("Configure", new ImageIcon(getClass().getResource("SB16.gif"))) {
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
    new AbstractAction("Duplicate") {
	public void actionPerformed(ActionEvent e) {
	  organizer.duplicate();
	}
      },
    new AbstractAction("Delete") {
	public void actionPerformed(ActionEvent e) {
	  organizer.deleteRecipe();
	}
      },
    new AbstractAction("Rename") {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameRecipe();
	}
      },
    new AbstractAction("Save To Database") {
	public void actionPerformed(ActionEvent e) {
	  organizer.saveRecipe();
	}
      }
  };
  private Action[] treeAction = {
    newSocietyAction,
//      new AbstractAction("New Society") {
//  	public void actionPerformed(ActionEvent e) {
//            organizer.newSociety();
//  	}
//        },
    new AbstractAction("New Recipe") {
	public void actionPerformed(ActionEvent e) {
	  organizer.newRecipe();
	}
      },
    new AbstractAction("New Folder") {
	public void actionPerformed(ActionEvent e) {
	  organizer.newFolder();
	}
      },
    new AbstractAction("Delete") {
	public void actionPerformed(ActionEvent e) {
	  organizer.deleteFolder();
	}
      },
    new AbstractAction("Rename") {
	public void actionPerformed(ActionEvent e) {
	  organizer.renameFolder();
	}
      }
  };

  public OrganizerMouseListener(Organizer organizer,
                                OrganizerTree workspace) {
    this.organizer = organizer;
    this.workspace = workspace;
    newSocietyAction.setEnabled(false); // disable creatin builtin societies
    JMenu newExperimentMenu = new JMenu("New Experiment");
    for (int i = 0; i < newExperimentActions.length; i++) {
      newExperimentMenu.add(newExperimentActions[i]);
    }
    rootMenu.add(newExperimentMenu);
    JMenu newRecipeMenu = new JMenu("New Recipe");
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
    JMenu newExperimentInTreeMenu = new JMenu("New Experiment");
    for (int i = 0; i < newExperimentActions.length; i++) {
      newExperimentInTreeMenu.add(newExperimentActions[i]);
    }
    treeMenu.add(newExperimentInTreeMenu);
    JMenu newRecipeInTreeMenu = new JMenu("New Recipe");
    for (int i = 0; i < newRecipeActions.length; i++) {
      newRecipeInTreeMenu.add(newRecipeActions[i]);
    }
    treeMenu.add(newRecipeInTreeMenu);
    for (int i = 0; i < treeAction.length; i++) {
      treeMenu.add(treeAction[i]);
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
      rootMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof SocietyComponent) {
      configureSocietyMenu(((SocietyComponent)o).isEditable());
      societyMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof RecipeComponent) {
      configureRecipeMenu(((RecipeComponent)o).isEditable());
      recipeMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof Experiment) {
      configureExperimentMenu(((Experiment)o));
      experimentMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof String) {
      treeMenu.show(workspace, e.getX(), e.getY());
    }
  }
  
  private void configureExperimentMenu(Experiment experiment) {
    boolean isEditable = experiment.isEditable();
    if (isEditable) {
      for (int i = 0; i < experimentAction.length; i++)
	experimentAction[i].setEnabled(true);
    } else {
      for (int i = 0; i < experimentAction.length; i++) {
	String s = (String)experimentAction[i].getValue(Action.NAME);
	if (s.equals("Rename")) {
	  experimentAction[i].setEnabled(false);
          break;
        }
      }
    }
    for (int i = 0; i < experimentAction.length; i++) {
      String s = (String)experimentAction[i].getValue(Action.NAME);
      if (s.equals("Run")) {
	if (experiment.getSocietyComponentCount() != 0 &&
	    experiment.isRunnable())
	  experimentAction[i].setEnabled(true);
	else
	  experimentAction[i].setEnabled(false);
	break;
      }
    }
  }

  private void configureSocietyMenu(boolean isEditable) {
    if (isEditable) {
      for (int i = 0; i < societyAction.length; i++) 
	societyAction[i].setEnabled(true);
    } else {
      for (int i = 0; i < societyAction.length; i++) {
	String s = (String)societyAction[i].getValue(Action.NAME);
	if (s.equals("Build Experiment") ||
	    s.equals("Rename"))
	  societyAction[i].setEnabled(false);
      }
    }
  }
  
  private void configureRecipeMenu(boolean isEditable) {
    if (isEditable) {
      for (int i = 0; i < recipeAction.length; i++) 
	recipeAction[i].setEnabled(true);
    } else {
      for (int i = 0; i < recipeAction.length; i++) {
	String s = (String)recipeAction[i].getValue(Action.NAME);
	if (s.equals("Build Experiment") ||
	    s.equals("Rename"))
	  recipeAction[i].setEnabled(false);
      }
    }
  }

}

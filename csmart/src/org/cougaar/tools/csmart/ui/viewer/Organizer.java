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
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.console.ExperimentDB;
import org.cougaar.tools.csmart.ui.experiment.*;
import org.cougaar.tools.csmart.scalability.ScalabilityXSociety;
import org.cougaar.tools.csmart.societies.abcsociety.ABCSociety;
import org.cougaar.tools.csmart.societies.abcsociety.BasicMetric;
import org.cougaar.tools.csmart.societies.cmt.CMTSociety;
import org.cougaar.tools.csmart.societies.database.DBUtils;
import org.cougaar.tools.csmart.recipe.ComponentInsertionRecipe;
import org.cougaar.tools.csmart.recipe.SpecificInsertionRecipe;
import org.cougaar.tools.csmart.recipe.AgentInsertionRecipe;

/**
 * The Organizer holds all the component a user creates
 * and manipulates in CSMART
 */
public class Organizer extends JScrollPane {
  private static final String DEFAULT_FILE_NAME = "Default Workspace.bin";
  
  private static final String FRAME_TITLE = "CSMART Launcher";

  private static final String EXPT_DESC_QUERY = "queryExptDescriptions";
  private static final String EXPT_ASSEM_QUERY = "queryExperiment";
  private static final String EXPT_TRIAL_QUERY = "queryTrials";
  private static final String EXPT_NODE_QUERY = "queryNodes";
  private static final String EXPT_HOST_QUERY = "queryHosts";
  private static final String COMPONENT_ARGS_QUERY = "queryComponentArgs";
  private static final long UPDATE_DELAY = 5000L;

  private boolean updateNeeded = false;
  
  private long nextUpdate = 0L;
  
  private String workspaceFileName;
  private CSMART csmart;
  private DefaultMutableTreeNode root;
  DefaultTreeModel model;
  private OrganizerTree workspace;
  private Organizer organizer;
  private Map dbExptMap = new HashMap();
  private Map dbTrialMap = new HashMap();
  private CMTDialog cmtDialog;

  // The societies which can be created in CSMART
  private Object[] socComboItems = {
    new ComboItem("Scalability", ScalabilityXSociety.class),
    new ComboItem("ABC", ABCSociety.class),
  };

  // The stand-alone recipes that can be created in CSMART
  private Object[] metComboItems = {
    new ComboItem("Basic Metric", BasicMetric.class),
    new ComboItem("Component Insertion", ComponentInsertionRecipe.class),
    new ComboItem("Specific Insertion", SpecificInsertionRecipe.class),
    new ComboItem("Agent Insertion", AgentInsertionRecipe.class),
    new ComboItem("Empty Metric", EmptyMetric.class),
    new ComboItem("ABCImpact", ABCImpact.class),
  };

  // Define Unique Name sets
  private UniqueNameSet societyNames = new UniqueNameSet("Society");
  private UniqueNameSet experimentNames = new UniqueNameSet("Experiment");
  private UniqueNameSet recipeNames = new UniqueNameSet("Recipe");
  private UniqueNameSet componentNames = new UniqueNameSet("Component");
  
  // Define menus
  private DefaultMutableTreeNode popupNode;
  private JPopupMenu societyMenu = new JPopupMenu();
  private JPopupMenu componentMenu = new JPopupMenu();
  private JPopupMenu recipeMenu = new JPopupMenu();
  private JPopupMenu experimentMenu = new JPopupMenu();
  private JPopupMenu treeMenu = new JPopupMenu();
  private JPopupMenu rootMenu = new JPopupMenu();
  
  // Define actions for use on menus
  private Action[] rootAction = {
    new AbstractAction("New Society") {
	public void actionPerformed(ActionEvent e) {
	  newSociety(popupNode);
	}
      },
//      new AbstractAction("New Recipe") {
//  	public void actionPerformed(ActionEvent e) {
//  	  newRecipe(popupNode);
//  	}
//        },
    new AbstractAction("New Folder") {
	public void actionPerformed(ActionEvent e) {
	  newFolder(popupNode);
	}
      },
    new AbstractAction("Rename") {
	public void actionPerformed(ActionEvent e) {
	  renameWorkspace();
	}
      },
    new AbstractAction("Delete Experiment From Database") {
      public void actionPerformed(ActionEvent e) {
        GUIUtils.timeConsumingTaskStart(organizer);
        try {
          new Thread("DeleteExperiment") {
            public void run() {
              deleteExperimentFromDatabase();
              GUIUtils.timeConsumingTaskEnd(organizer);
            }
          }.start();
        } catch (RuntimeException re) {
          System.out.println("Runtime exception deleting experiment: " + re);
          re.printStackTrace();
          GUIUtils.timeConsumingTaskEnd(organizer);
        }
      }
    }
  };
  private Action[] newExperimentActions = {
    new AbstractAction("From Database") {
  	public void actionPerformed(ActionEvent e) {
//            GUIUtils.timeConsumingTaskStart(organizer);
//            try {
//              new Thread("SelectExperiment") {
//                public void run() {
                  selectExperimentFromDatabase(popupNode);
//                  GUIUtils.timeConsumingTaskEnd(organizer);
//                }
//              }.start();
//            } catch (RuntimeException re) {
//              System.out.println("Runtime exception creating experiment: " + re);
//              re.printStackTrace();
//              GUIUtils.timeConsumingTaskEnd(organizer);
//            }
    	}
    },
    new AbstractAction("Built In") {
	public void actionPerformed(ActionEvent e) {
	  newExperiment(popupNode);
	}
    }
  };
  private Action[] experimentAction = {
    new AbstractAction("Configure", new ImageIcon(getClass().getResource("EB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  startExperimentBuilder(popupNode, 
				 e.getActionCommand().equals("Configure"));
	}
      },
    new AbstractAction("Run", new ImageIcon(getClass().getResource("EC16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  startConsole(popupNode);
	}
      },
    new AbstractAction("Duplicate") {
	public void actionPerformed(ActionEvent e) {
	  copyExperimentInNode(popupNode);
	}
      },
    new AbstractAction("Delete") {
	public void actionPerformed(ActionEvent e) {
	  deleteExperiment(popupNode);
	}
      },
    new AbstractAction("Rename") {
	public void actionPerformed(ActionEvent e) {
	  renameExperiment(popupNode);
	}
      }
  };
  private Action[] societyAction = {
    new AbstractAction("Configure", new ImageIcon(getClass().getResource("SB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  startBuilder(popupNode, e.getActionCommand().equals("Configure"));
	}
      },
    new AbstractAction("Build Experiment",
		       new ImageIcon(getClass().getResource("EB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  startExperimentBuilder(popupNode, true);
	}
      },
    new AbstractAction("Run Experiment", new ImageIcon(getClass().getResource("EC16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  startConsole(popupNode);
	}
      },
    new AbstractAction("Duplicate") {
	public void actionPerformed(ActionEvent e) {
	  copySocietyInNode(popupNode);
	}
      },
    new AbstractAction("Delete") {
	public void actionPerformed(ActionEvent e) {
	  deleteSociety(popupNode);
	}
      },
    new AbstractAction("Rename") {
	public void actionPerformed(ActionEvent e) {
	  renameSociety(popupNode);
	}
      }
  };
  private Action[] componentAction = {
    //          new AbstractAction("Edit", new ImageIcon(getClass().getResource("SB16.gif"))) {
    //              public void actionPerformed(ActionEvent e) {
    //                  startBuilder(popupNode);
    //              }
    //          },
    //          new AbstractAction("Run", new ImageIcon(getClass().getResource("EC16.gif"))) {
    //              public void actionPerformed(ActionEvent e) {
    //                  startConsole(popupNode);
    //              }
    //          },
    new AbstractAction("Build Experiment",
		       new ImageIcon(getClass().getResource("EB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  startExperimentBuilder(popupNode, true);
	}
      },
    new AbstractAction("Duplicate") {
	public void actionPerformed(ActionEvent e) {
	  copyComponentInNode(popupNode);
	}
      },
    new AbstractAction("Delete") {
	public void actionPerformed(ActionEvent e) {
	  deleteComponent(popupNode);
	}
      },
    new AbstractAction("Rename") {
	public void actionPerformed(ActionEvent e) {
	  renameComponent(popupNode);
	}
      }
  };
  private Action[] newRecipeActions = {
    new AbstractAction("From Database") {
	public void actionPerformed(ActionEvent e) {
          GUIUtils.timeConsumingTaskStart(organizer);
          try {
            new Thread("SelectRecipe") {
              public void run() {
                selectRecipeFromDatabase(popupNode);
                GUIUtils.timeConsumingTaskEnd(organizer);
              }
            }.start();
          } catch (RuntimeException re) {
            System.out.println("Runtime exception creating recipe: " + re);
            re.printStackTrace();
            GUIUtils.timeConsumingTaskEnd(organizer);
          }
  	}
    },
    new AbstractAction("Built In") {
	public void actionPerformed(ActionEvent e) {
	  newRecipe(popupNode);
	}
    }
  };
  private Action[] recipeAction = {
    new AbstractAction("Configure", new ImageIcon(getClass().getResource("SB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  startBuilder(popupNode, e.getActionCommand().equals("Configure"));
	}
      },
    //          new AbstractAction("Run", new ImageIcon(getClass().getResource("EC16.gif"))) {
    //              public void actionPerformed(ActionEvent e) {
    //                  startConsole(popupNode);
    //              }
    //          },
    new AbstractAction("Build Experiment",
		       new ImageIcon(getClass().getResource("EB16.gif"))) {
	public void actionPerformed(ActionEvent e) {
	  startExperimentBuilder(popupNode, true);
	}
      },
    new AbstractAction("Duplicate") {
	public void actionPerformed(ActionEvent e) {
	  copyRecipeInNode(popupNode);
	}
      },
    new AbstractAction("Delete") {
	public void actionPerformed(ActionEvent e) {
	  deleteRecipe(popupNode);
	}
      },
    new AbstractAction("Rename") {
	public void actionPerformed(ActionEvent e) {
	  renameRecipe(popupNode);
	}
      },
    new AbstractAction("Save To Database") {
	public void actionPerformed(ActionEvent e) {
	  saveRecipe(popupNode);
	}
      }
  };
  private Action[] treeAction = {
    new AbstractAction("New Society") {
	public void actionPerformed(ActionEvent e) {
	  while (! popupNode.getAllowsChildren())
	    popupNode = (DefaultMutableTreeNode)popupNode.getParent();
	  newSociety(popupNode);
	}
      },
    new AbstractAction("New Recipe") {
	public void actionPerformed(ActionEvent e) {
	  while (! popupNode.getAllowsChildren())
	    popupNode = (DefaultMutableTreeNode)popupNode.getParent();
	  newRecipe(popupNode);
	}
      },
//      new AbstractAction("New Experiment") {
//  	public void actionPerformed(ActionEvent e) {
//  	  while (! popupNode.getAllowsChildren())
//  	    popupNode = (DefaultMutableTreeNode)popupNode.getParent();
//  	  newExperiment(popupNode);
//  	}
//        },
//      new AbstractAction("Select Experiment from Database") {
//  	public void actionPerformed(ActionEvent e) {
//  	  while (! popupNode.getAllowsChildren())
//  	    popupNode = (DefaultMutableTreeNode)popupNode.getParent();
//  	  selectExperimentFromDatabase(popupNode);
//  	}
//        },
    new AbstractAction("New Folder") {
	public void actionPerformed(ActionEvent e) {
	  while (! popupNode.getAllowsChildren())
	    popupNode = (DefaultMutableTreeNode)popupNode.getParent();
	  newFolder(popupNode);
	}
      },
    new AbstractAction("Delete") {
	public void actionPerformed(ActionEvent e) {
	  deleteFolder(popupNode);
	}
      },
    new AbstractAction("Rename") {
	public void actionPerformed(ActionEvent e) {
	  renameFolder(popupNode);
	}
      }
  };

  private MouseListener mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
	if (e.isPopupTrigger()) doPopup(e);
      }
      public void mousePressed(MouseEvent e) {
	if (e.isPopupTrigger()) doPopup(e);
      }
      public void mouseReleased(MouseEvent e) {
	if (e.isPopupTrigger()) doPopup(e);
      }
    };

  ////////////////////////////////
  // Constructors
  public Organizer(CSMART csmart) {
    this(csmart, null);
    organizer = this;
  }
  
  public Organizer(CSMART csmart, String workspaceFileName) {
    setPreferredSize(new Dimension(400, 100));
    JPanel panel = new JPanel(new BorderLayout());
    setViewportView(panel);
    this.csmart = csmart;
    if (workspaceFileName == null) {
      this.workspaceFileName = DEFAULT_FILE_NAME;
    } else {
      this.workspaceFileName = workspaceFileName;
    }
    restore(this.workspaceFileName);
    if (root == null) {
      root = new DefaultMutableTreeNode(null, true);
      //            model = new DefaultTreeModel(root);
      model = createModel(this, root);
    }
    // 	System.out.println("Organizer: setting root name to: " +
    // 			   this.workspaceFileName);
    //         String rootName = this.workspaceFileName;
    //         int extpos = this.workspaceFileName.lastIndexOf('.');
    //         if (extpos >= 0) {
    //             rootName = rootName.substring(0, extpos);
    //         }
    //         root.setUserObject(rootName);
    model.setAsksAllowsChildren(true);
    model.addTreeModelListener(myModelListener);
    workspace = new OrganizerTree(model);
    DefaultCellEditor myEditor = new DefaultCellEditor(new JTextField()) {
      public boolean isCellEditable(EventObject e) {
	if (super.isCellEditable(e) && e instanceof MouseEvent) {
	  TreePath path = workspace.getPathForLocation(((MouseEvent)e).getX(),
						       ((MouseEvent)e).getY());
	  if (path == null)
	    return false;
	  Object o = path.getLastPathComponent();
	  Object userObject = ((DefaultMutableTreeNode)o).getUserObject();
	  if (userObject instanceof ModifiableConfigurableComponent)
	    return ((ModifiableConfigurableComponent)userObject).isEditable();
	  else
  	    return true; // renaming workspace or folder is always ok
	}
	return false;
      }
    };
    workspace.setCellEditor(myEditor);
  
    workspace.setExpandsSelectedPaths(true);
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
    for (int i = 0; i < componentAction.length; i++) {
      componentMenu.add(componentAction[i]);
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
    workspace.addTreeSelectionListener(mySelectionListener);
    workspace.addAncestorListener(myAncestorListener);
    workspace.addMouseListener(mouseListener);
    workspace.setSelection(root);
    expandTree(); // fully expand workspace tree
    panel.add(workspace);
    setViewportView(panel);
    updater.start();
  }
  
  ////////////////////////////////////////////////
  // Methods to get the user's selection
  private DefaultMutableTreeNode getSelectedNode() {
    TreePath selPath = workspace.getSelectionPath();
    if (selPath == null) return null;
    return (DefaultMutableTreeNode) selPath.getLastPathComponent();
  }
  
  public SocietyComponent[] getSelectedSocieties() {
    return (SocietyComponent[]) getSelectedLeaves(SocietyComponent.class);
  }
  
  public ModifiableConfigurableComponent[] getSelectedComponents() {
    return (ModifiableConfigurableComponent[]) getSelectedLeaves(ModifiableConfigurableComponent.class);
  }
  
  public RecipeComponent[] getSelectedRecipes() {
    return (RecipeComponent[]) getSelectedLeaves(RecipeComponent.class);
  }
    
  public Experiment[] getSelectedExperiments() {
    return (Experiment[]) getSelectedLeaves(Experiment.class);
  }
  
  private Object[] getSelectedLeaves(Class leafClass) {
    TreePath[] paths = workspace.getSelectionPaths();
    if (paths == null)
      return null;
    return getLeaves(leafClass, paths);
  }
  
  private Object[] getLeaves(Class leafClass, TreePath[] paths) {
    List result = new ArrayList();
    return fill(result, leafClass, paths);
  }
  
  private Object[] getLeaves(Class leafClass, TreeNode node) {
    TreeNode[] nodes = model.getPathToRoot(node);
    return getLeaves(leafClass,
		     new TreePath[] {new TreePath(nodes)});
  }
  
  private ModifiableConfigurableComponent[] getConfigurableComponents(TreeNode node) {
    return (ModifiableConfigurableComponent[]) getLeaves(ModifiableConfigurableComponent.class, node);
  }
  
  private SocietyComponent[] getSocietyComponents(TreeNode node) {
    return (SocietyComponent[]) getLeaves(SocietyComponent.class, node);
  }
  
  private RecipeComponent[] getRecipes(TreeNode node) {
    return (RecipeComponent[]) getLeaves(RecipeComponent.class, node);
  }
 
  private Experiment[] getExperiment(TreeNode node) {
    return (Experiment[]) getLeaves(Experiment.class, node);
  }
  
  private Object[] fill(List result, Class leafClass, TreePath[] paths) {
    for (int i = 0; i < paths.length; i++) {
      DefaultMutableTreeNode selNode =
	(DefaultMutableTreeNode) paths[i].getLastPathComponent();
      Object o = selNode.getUserObject();
      if (leafClass.isInstance(o)) {
	result.add(o);
      } else if (o instanceof String) {
	for (Enumeration e = selNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
	  DefaultMutableTreeNode node =
	    (DefaultMutableTreeNode) e.nextElement();
	  o = node.getUserObject();
	  if (leafClass.isInstance(o)) {
	    result.add(o);
	  }
	}
      }
    }
    return result.toArray((Object[]) Array.newInstance(leafClass, result.size()));
  }
  
  public Object getSelectedObject() {
    DefaultMutableTreeNode selNode = getSelectedNode();
    if (selNode == null) return null;
    return selNode.getUserObject();
  }

  // End of methods to get the User's selection
  ////////////////////////////////////  

  /**
   * Popup appropriate menu, but first disable appropriate commands
   * if component is not editable.
   */
  private void doPopup(MouseEvent e) {
    TreePath selPath = workspace.getPathForLocation(e.getX(), e.getY());
    if (selPath == null) return;
    // set the selected node to be the node the mouse is pointing at
    workspace.setSelectionPath(selPath);
    popupNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
    Object o = popupNode.getUserObject();
    if (popupNode.isRoot()) {
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
    } else if (o instanceof ModifiableConfigurableComponent) {
      configureComponentMenu(((ModifiableConfigurableComponent)o).isEditable());
      componentMenu.show(workspace, e.getX(), e.getY());
    } else if (o instanceof String) {
      treeMenu.show(workspace, e.getX(), e.getY());
    }
  }
  
  private void configureExperimentMenu(Experiment experiment) {
    boolean isEditable = experiment.isEditable();
    if (isEditable) {
      for (int i = 0; i < experimentAction.length; i++) {
	String s = (String)experimentAction[i].getValue(Action.NAME);
	if (s.equals("View"))
	  experimentAction[i].putValue(Action.NAME, "Configure");
	experimentAction[i].setEnabled(true);
      } 
    } else {
      for (int i = 0; i < experimentAction.length; i++) {
	String s = (String)experimentAction[i].getValue(Action.NAME);
	if (s.equals("Rename"))
	  experimentAction[i].setEnabled(false);
	else if (s.equals("Configure"))
	  experimentAction[i].putValue(Action.NAME, "View");
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
      for (int i = 0; i < societyAction.length; i++) {
	String s = (String)societyAction[i].getValue(Action.NAME);
	if (s.equals("View"))
	  societyAction[i].putValue(Action.NAME, "Configure");
	societyAction[i].setEnabled(true);
      } 
    } else {
      for (int i = 0; i < societyAction.length; i++) {
	String s = (String)societyAction[i].getValue(Action.NAME);
	if (s.equals("New Experiment") ||
	    s.equals("Rename"))
	  societyAction[i].setEnabled(false);
	else if (s.equals("Configure"))
	  societyAction[i].putValue(Action.NAME, "View");
      }
    }
  }
  
  private void configureComponentMenu(boolean isEditable) {
    if (isEditable) {
      for (int i = 0; i < componentAction.length; i++) {
	String s = (String)componentAction[i].getValue(Action.NAME);
	if (s.equals("View"))
	  componentAction[i].putValue(Action.NAME, "Configure");
	componentAction[i].setEnabled(true);
      } 
    } else {
      for (int i = 0; i < componentAction.length; i++) {
	String s = (String)componentAction[i].getValue(Action.NAME);
	if (s.equals("New Experiment") ||
	    s.equals("Rename"))
	  componentAction[i].setEnabled(false);
	else if (s.equals("Configure"))
	  componentAction[i].putValue(Action.NAME, "View");
      }
    }
  }
  
  private void configureRecipeMenu(boolean isEditable) {
    if (isEditable) {
      for (int i = 0; i < recipeAction.length; i++) {
	String s = (String)recipeAction[i].getValue(Action.NAME);
	if (s.equals("View"))
	  recipeAction[i].putValue(Action.NAME, "Configure");
	recipeAction[i].setEnabled(true);
      } 
    } else {
      for (int i = 0; i < recipeAction.length; i++) {
	String s = (String)recipeAction[i].getValue(Action.NAME);
	if (s.equals("New Experiment") ||
	    s.equals("Rename"))
	  recipeAction[i].setEnabled(false);
	else if (s.equals("Configure"))
	  recipeAction[i].putValue(Action.NAME, "View");
      }
    }
  }

  private void startBuilder(DefaultMutableTreeNode node,
			    boolean openForEditing) {
    csmart.runBuilder((ModifiableConfigurableComponent) node.getUserObject(), false,
		      openForEditing);
  }
  
  private void startExperimentBuilder(DefaultMutableTreeNode node,
				      boolean openForEditing) {
    Object o = node.getUserObject();
    Experiment experiment;
    if (o instanceof SocietyComponent) {
      SocietyComponent sc = (SocietyComponent) o;
      String name = "Experiment for " + sc.getSocietyName();
      experiment = new Experiment(experimentNames.generateName(name),
				  new SocietyComponent[] {sc},
      				  new RecipeComponent[0]);
    } else if (o instanceof RecipeComponent) {
      RecipeComponent recipe = (RecipeComponent) o;
      //} else if (o instanceof Recipe) {
      //Recipe recipe = (Recipe) o;
      String name = "Experiment for " + recipe.getRecipeName();
      experiment = new Experiment(experimentNames.generateName(name),
				  new SocietyComponent[0],
      				  new RecipeComponent[] {recipe});
    } else if (o instanceof Experiment) {
      experiment = (Experiment) o;
    } else if (o instanceof String) {
      experiment = new Experiment(experimentNames.generateName(),
				  getSocietyComponents(node),
				  getRecipes(node));
    } else {
      return;
    }
    if (o instanceof Experiment) {
      //Experiment already in tree
    } else {
      if (o instanceof String) {
	// Add experiment under the given node
	DefaultMutableTreeNode newNode = 
	  new DefaultMutableTreeNode(experiment);
	model.insertNodeInto(newNode, node, 0);
	// make the new node be the selected node
	workspace.setSelection(newNode);
      } else {
	// Add experiment as sibling of given node
	DefaultMutableTreeNode newNode =
	  new DefaultMutableTreeNode(experiment, false);
	DefaultMutableTreeNode parentNode =
	  (DefaultMutableTreeNode) node.getParent();
	model.insertNodeInto(newNode, parentNode, parentNode.getIndex(node) + 1);
	// make the new node be the selected node
	workspace.setSelection(newNode);
      }
      experimentNames.add(experiment.getExperimentName());
      experiment.addModificationListener(myModificationListener);
    }
    csmart.runExperimentBuilder(experiment, false, openForEditing);
  }
  
  private void startConsole(DefaultMutableTreeNode node) {
    Object o = node.getUserObject();
    Experiment experiment;
    if (o instanceof SocietyComponent) {
      SocietyComponent sc = (SocietyComponent) o;
      String name = "Temp Experiment for " + sc.getSocietyName();
      name = experimentNames.generateName(name);
      experiment = new Experiment(name,
				  new SocietyComponent[] {sc},
				  new RecipeComponent[0]);
      DefaultMutableTreeNode parent = 
	(DefaultMutableTreeNode) node.getParent();
      DefaultMutableTreeNode newNode =
	new DefaultMutableTreeNode(experiment, false);
      model.insertNodeInto(newNode, parent, parent.getChildCount());
      experimentNames.add(name);
      experiment.addModificationListener(myModificationListener);
      workspace.setSelection(newNode);

    } else if (o instanceof Experiment) {
      experiment = (Experiment) o;
    } else {
      return;
    }
    // In addition, its possible there are unbound properties!
    if (experiment.hasUnboundProperties()) {
      // can't run it without setting these
      csmart.runExperimentBuilder(experiment, false, true);
    }
    // At this point the user may not have created a configuration.
    // So they really can't run the console quite yet.
    // We'll create a default configuration for them
    if (! experiment.hasConfiguration())
      experiment.createDefaultConfiguration();

    // Start up the console on the experiment
    csmart.runConsole(experiment);
  }
  
  private int nameCounter = 0;
  
  private static Class[] constructorArgTypes = {String.class};
  
  PropertyListener myPropertyListener = new PropertyListener() {
      public void propertyValueChanged(PropertyEvent e) {
	update();
      }
      
      public void propertyOtherChanged(PropertyEvent e) {
	update();
      }
    };
  
  PropertiesListener myPropertiesListener = new PropertiesListener() {
      public void propertyAdded(PropertyEvent e) {
	e.getProperty().addPropertyListener(myPropertyListener);
	update();
      }
      
      public void propertyRemoved(PropertyEvent e) {
	e.getProperty().removePropertyListener(myPropertyListener);
	update();
      }
    };

  /**
   * Call update if anything changed.
   * Also handle experiment name having been changed:
   *   update the name of the experiment in the tree
   *   update the name of the experiment in the set of experiment names
   *    (currently done by re-initting the experiment names)
   */

  ModificationListener myModificationListener = new ModificationListener() {
      public void modified(ModificationEvent event) {
        if (event.getSource() instanceof Experiment) {
          model.nodeChanged(findNode(event.getSource()));
          try {
            Class[] noTypes = new Class[0];
            experimentNames.clear();
            experimentNames.init(getLeaves(Experiment.class, root),
                Experiment.class.getMethod("getExperimentName", noTypes));
          } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
          } catch (SecurityException se) {
            se.printStackTrace();
          }
        }
	update();
      }
    };
  
  public boolean exitAllowed() {
    synchronized(root) {
      if (updateNeeded) {
	nextUpdate = System.currentTimeMillis();
	root.notify();
	while (updateNeeded) {
	  try {
	    root.wait();
	  } catch (InterruptedException ie) {
	  }
	}
      }
    }
    return true;
  }

  /**
   * Create a new society.
   */

  private DefaultMutableTreeNode newSociety(DefaultMutableTreeNode node) {
    Object answer =
      JOptionPane.showInputDialog(this, "Select Society Type",
				  "Select Society",
				  JOptionPane.QUESTION_MESSAGE,
				  null,
				  socComboItems,
				  "ScalabilityX");
    if (answer == null)
      return null;
    SocietyComponent sc = null;
    if (answer instanceof ComboItem) {
      // create a scalability or abc society
      ComboItem item = (ComboItem) answer;
      String name = societyNames.generateName(item.name);
      while (true) {
	name = (String) JOptionPane.showInputDialog(this, "Enter Society Name",
						    "Name Society",
						    JOptionPane.QUESTION_MESSAGE,
						    null, null,
						    name);
	if (name == null) return null;
	if (!societyNames.contains(name)) break;
	int ok = JOptionPane.showConfirmDialog(this,
					       "Use an unique name",
					       "Society Name Not Unique",
					       JOptionPane.OK_CANCEL_OPTION,
					       JOptionPane.ERROR_MESSAGE);
	if (ok != JOptionPane.OK_OPTION) return null;
      }
      sc = createSoc(name, item.cls);
    }
    if (sc == null)
      return null;
    DefaultMutableTreeNode newNode = addSocietyToWorkspace(sc, node);
    workspace.setSelection(newNode);
    return newNode;
  }

  private SocietyComponent createSoc(String name, Class cls) {
    try {
      Constructor constructor = cls.getConstructor(constructorArgTypes);
      SocietyComponent sc = (SocietyComponent) constructor.newInstance(new String[] {name});
      sc.initProperties();
      return sc;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  private SocietyComponent createSoc(String name, List assemblyIDs, Class cls) {
    try {
      Constructor constructor = cls.getConstructor(new Class[] {String.class, List.class});
      SocietyComponent sc = (SocietyComponent) constructor.newInstance(new Object[] {name, assemblyIDs});
      sc.initProperties();
      return sc;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private DefaultMutableTreeNode addSocietyToWorkspace(SocietyComponent sc,
						       DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode = 
      new DefaultMutableTreeNode(sc, false);
    addNode(node, newNode);
    societyNames.add(sc.getSocietyName());
    installListeners((ModifiableConfigurableComponent)sc);
    return newNode;
  }
  
  private DefaultMutableTreeNode addComponentToWorkspace(ModifiableConfigurableComponent sc,
							 DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode = 
      new DefaultMutableTreeNode(sc, false);
    addNode(node, newNode);
    componentNames.add(sc.getShortName());
    installListeners(sc);
    return newNode;
  }
  
  private void installListeners(ModifiableConfigurableComponent sc) {
    sc.addPropertiesListener(myPropertiesListener);
    for (Iterator i = sc.getPropertyNames(); i.hasNext(); ) {
      Property p = sc.getProperty((CompositeName) i.next());
      PropertyEvent event = new PropertyEvent(p, PropertyEvent.PROPERTY_ADDED);
      myPropertiesListener.propertyAdded(event);
    }
    sc.addModificationListener(myModificationListener);
  }
  
  private void renameWorkspace() {
    String name = JOptionPane.showInputDialog("New workspace name");
    renameWorkspace(name);
  }
  
  private void renameWorkspace(String name) {
    if (name == null || name.equals(""))
      return;
    workspaceFileName = name;
    String rootName = this.workspaceFileName;
    int extpos = this.workspaceFileName.lastIndexOf('.');
    if (extpos >= 0) {
      rootName = rootName.substring(0, extpos);
    }
    root.setUserObject(rootName);
    model.nodeChanged(root);
    update();
  }
  
  private void renameSociety(DefaultMutableTreeNode node) {
    SocietyComponent societyComponent =
      (SocietyComponent) node.getUserObject();
    String name = JOptionPane.showInputDialog("New society name");
    renameSociety(node, name);
  }
  
  private void renameComponent(DefaultMutableTreeNode node) {
    ModifiableConfigurableComponent societyComponent =
      (ModifiableConfigurableComponent) node.getUserObject();
    if (societyComponent instanceof SocietyComponent) {
      renameSociety(node);
    } else if (societyComponent instanceof Experiment) {
      renameExperiment(node);
    } else if (societyComponent instanceof RecipeComponent) {
      renameRecipe(node);
    }
    String name = JOptionPane.showInputDialog("New component name");
    renameComponent(node, name);
  }
  
  private void renameSociety(DefaultMutableTreeNode node, String name) {
    SocietyComponent societyComponent =
      (SocietyComponent) node.getUserObject();
    if (name == null || name.equals(societyComponent.getSocietyName()) || name.equals("")) return;
    while (true) {
      if (!societyNames.contains(name)) break;
      int ok = JOptionPane.showConfirmDialog(this,
					     "Use an unique name",
					     "Society Name Not Unique",
					     JOptionPane.OK_CANCEL_OPTION,
					     JOptionPane.ERROR_MESSAGE);
      if (ok != JOptionPane.OK_OPTION) return;
      name = JOptionPane.showInputDialog("New society name");
      if (name == null || name.equals("")) return;
    }
    societyNames.remove(societyComponent.getSocietyName());
    societyNames.add(name);
    societyComponent.setName(name);
    model.nodeChanged(node);
  }
  
  private void renameComponent(DefaultMutableTreeNode node, String name) {
    ModifiableConfigurableComponent componentComponent =
      (ModifiableConfigurableComponent) node.getUserObject();
    if (name == null || name.equals(componentComponent.getShortName()) || name.equals("")) return;
    while (true) {
      if (!componentNames.contains(name)) break;
      int ok = JOptionPane.showConfirmDialog(this,
					     "Use an unique name",
					     "Component Name Not Unique",
					     JOptionPane.OK_CANCEL_OPTION,
					     JOptionPane.ERROR_MESSAGE);
      if (ok != JOptionPane.OK_OPTION) return;
      name = JOptionPane.showInputDialog("New component name");
      if (name == null || name.equals("")) return;
    }
    componentNames.remove(componentComponent.getShortName());
    componentNames.add(name);
    componentComponent.setName(name);
    model.nodeChanged(node);
  }
  
  /**
   * Note that this simply deletes the society from the workspace;
   * if the society was included in an experiment, then the experiment
   * still retains the society.
   */
  private void deleteSociety(DefaultMutableTreeNode node) {
    SocietyComponent society = (SocietyComponent)node.getUserObject();
    if (!society.isEditable()) {
      int result = JOptionPane.showConfirmDialog(this,
						 "Society has been or is being used; delete anyway?",
						 "Society Not Editable",
						 JOptionPane.YES_NO_OPTION,
						 JOptionPane.WARNING_MESSAGE);
      if (result != JOptionPane.YES_OPTION)
	return;
    }
    model.removeNodeFromParent(node);
    societyNames.remove(society.getSocietyName());
  }
  
  /**
   * Note that this simply deletes the component from the workspace;
   * if the component was included in an experiment, then the experiment
   * still retains the component.
   */
  private void deleteComponent(DefaultMutableTreeNode node) {
    ModifiableConfigurableComponent component = (ModifiableConfigurableComponent)node.getUserObject();
    if (component instanceof SocietyComponent)
      deleteSociety(node);
    else if (component instanceof Experiment)
      deleteExperiment(node);
    else if (component instanceof RecipeComponent)
      deleteRecipe(node);
    else {
      if (!component.isEditable()) {
	int result = JOptionPane.showConfirmDialog(this,
						   "Component has been or is being used; delete anyway?",
						   "Component Not Editable",
						   JOptionPane.YES_NO_OPTION,
						   JOptionPane.WARNING_MESSAGE);
	if (result != JOptionPane.YES_OPTION)
	  return;
      }
      model.removeNodeFromParent(node);
      componentNames.remove(component.getShortName());
    }
  }

  private void newRecipe(DefaultMutableTreeNode node) {
    Object[] values = metComboItems;
    Object answer =
      JOptionPane.showInputDialog(this, "Select Recipe Type",
				  "Select Recipe",
				  JOptionPane.QUESTION_MESSAGE,
				  null,
				  values,
				  "Empty Recipe");
				  //"Mo Recipe");
    if (answer instanceof ComboItem) {
      ComboItem item = (ComboItem) answer;
      String name = recipeNames.generateName(item.name);
      while (true) {
	name = (String) JOptionPane.showInputDialog(this, "Enter Recipe Name",
						    "Name Recipe",
						    JOptionPane.QUESTION_MESSAGE,
						    null, null,
						    name);
	if (name == null) return;
	if (!recipeNames.contains(name)) break;
	int ok = JOptionPane.showConfirmDialog(this,
					       "Use an unique name",
					       "Recipe Name Not Unique",
					       JOptionPane.OK_CANCEL_OPTION,
					       JOptionPane.ERROR_MESSAGE);
	if (ok != JOptionPane.OK_OPTION) return;
      }
      try {
	Constructor constructor = item.cls.getConstructor(constructorArgTypes);
	RecipeComponent recipe =
		  (RecipeComponent) constructor.newInstance(new String[] {name});
	recipe.initProperties();
	DefaultMutableTreeNode newNode =
	  addRecipeToWorkspace(recipe, node);
	workspace.setSelection(newNode);
      } catch (Exception e) {
	e.printStackTrace();
      }
    }
  } // end of newRecipe

  private RecipeComponent createRecipeComponent(String name, Class cls) {
    try {
      Constructor constructor = cls.getConstructor(constructorArgTypes);
      RecipeComponent recipe =
	(RecipeComponent) constructor.newInstance(new String[] {name});
      recipe.initProperties();
      return recipe;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  private DefaultMutableTreeNode addRecipeToWorkspace(RecipeComponent recipe,
 //private DefaultMutableTreeNode addRecipeToWorkspace(Recipe recipe,
						      DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode = 
      new DefaultMutableTreeNode(recipe, false);
    addNode(node, newNode);
    recipeNames.add(recipe.getRecipeName());
    //installListeners(recipe);
    return newNode;
  }
  
  private void renameRecipe(DefaultMutableTreeNode node) {
    String name = JOptionPane.showInputDialog("New recipe name");
    renameRecipe(node, name);
  }
  
  // Separate method for use from model...
  private void renameRecipe(DefaultMutableTreeNode node, String name) {
      RecipeComponent recipe =
        (RecipeComponent) node.getUserObject();
      //    Recipe recipe =
      //(Recipe) node.getUserObject();
    if (name == null || name.equals(recipe.getRecipeName()) || name.equals("")) return;
    while (true) {
      if (!recipeNames.contains(name)) break;
      int ok = JOptionPane.showConfirmDialog(this,
					     "Use an unique name",
					     "Recipe Name Not Unique",
					     JOptionPane.OK_CANCEL_OPTION,
					     JOptionPane.ERROR_MESSAGE);
      if (ok != JOptionPane.OK_OPTION) return;
      name = JOptionPane.showInputDialog("New recipe name");
      if (name == null || name.equals("")) return;
    }
    if (name != null) {
      recipeNames.remove(recipe.getRecipeName());
      recipe.setName(name);
      recipeNames.add(recipe.getRecipeName());
      model.nodeChanged(node); // update the model...
    }
  }
  
  private void deleteRecipe(DefaultMutableTreeNode node) {
    if (node == null) return;
    model.removeNodeFromParent(node);
    RecipeComponent rc = (RecipeComponent) node.getUserObject();
    try {
      PDbBase pdb = new PDbBase();
      try {
        if (pdb.recipeExists(rc) == PDbBase.RECIPE_STATUS_EXISTS) {
          int answer =
            JOptionPane.showConfirmDialog(this,
                                          "Delete recipe from database?",
                                          "Delete Recipe",
                                          JOptionPane.YES_NO_OPTION);
          if (answer == JOptionPane.YES_OPTION) {
            pdb.removeLibRecipe(rc);
          }
        }
      } finally {
        pdb.close();
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
                                    "An exception occurred deleting the recipe from the database",
                                    "Error Writing Database",
                                    JOptionPane.ERROR_MESSAGE);
    }
    recipeNames.remove(rc.getRecipeName());
  }
  
  private void saveRecipe(DefaultMutableTreeNode node) {
    if (node == null) return;
    RecipeComponent rc = (RecipeComponent) node.getUserObject();
    saveRecipe(rc);
  }

  private void saveRecipe(RecipeComponent rc) {
    try {
      PDbBase pdb = new PDbBase();
      switch (pdb.recipeExists(rc)) {
      case PDbBase.RECIPE_STATUS_EXISTS:
        JOptionPane.showMessageDialog(this,
                                      "The recipe is already in the database with the same values.",
                                      "Write Not Needed",
                                      JOptionPane.INFORMATION_MESSAGE);
        return;
      case PDbBase.RECIPE_STATUS_DIFFERS:
        int answer =
          JOptionPane.showConfirmDialog(this,
                                        "Recipe "
                                        + rc.getRecipeName()
                                        + " already in database. Overwrite?",
                                        "Recipe Exists",
                                        JOptionPane.OK_CANCEL_OPTION,
                                        JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        break;
      case PDbBase.RECIPE_STATUS_ABSENT:
        break;                // Just write it
      }
      pdb.replaceLibRecipe(rc);
      JOptionPane.showMessageDialog(this,
                                    "Recipe written successfully.",
                                    "Recipe Written",
                                    JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception sqle) {
      sqle.printStackTrace();
      JOptionPane.showMessageDialog(this,
                                    "An exception occurred writing the recipe to the database",
                                    "Error Writing Database",
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  // CAUTION: this runs in a non-swing thread
  private void selectRecipeFromDatabase(DefaultMutableTreeNode node) {
    Map recipeNamesHT = getRecipeNamesFromDatabase();
    Set dbRecipeNames = recipeNamesHT.keySet();
    if (dbRecipeNames.isEmpty()) {
      JOptionPane.showMessageDialog(this,
                                    "There are no recipes in the database",
                                    "No Database Recipes",
                                    JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    JComboBox cb = new JComboBox(dbRecipeNames.toArray());
    cb.setEditable(false);
    JPanel panel = new JPanel();
    panel.add(new JLabel("Select Recipe:"));
    panel.add(cb);
    int result = 
      JOptionPane.showConfirmDialog(null, panel, "Recipe",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION)
      return;
    String recipeName = (String)cb.getSelectedItem();
    String recipeId = (String) recipeNamesHT.get(recipeName);
    // produce an unique name for CSMART if necessary
    if (recipeNames.contains(recipeName)) {
      JOptionPane.showMessageDialog(null,
                                    "There is already a recipe named "
                                    + recipeName
                                    + " in your workspace.\nDelete or rename it first.",
                                    "Recipe Exists",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }
    DbRecipe dbRecipe = getDatabaseRecipe(recipeId);
    if (dbRecipe == null) return;
    dbRecipe.name = recipeName;
    RecipeComponent mc = createRecipeComponent(dbRecipe.name, dbRecipe.cls);
    setRecipeComponentProperties(dbRecipe, mc);
    addRecipeToWorkspace(mc, node);
  }

  private void selectExperimentFromDatabase(DefaultMutableTreeNode node) {
    Map experimentNamesMap = ExperimentDB.getExperimentNames();
    Set keys = experimentNamesMap.keySet();
    JComboBox cb = new JComboBox(keys.toArray(new String[keys.size()]));
    cb.setEditable(false);
    JPanel panel = new JPanel();
    panel.add(new JLabel("Select Experiment:"));
    panel.add(cb);
    int result = 
      JOptionPane.showConfirmDialog(null, panel, "Experiment",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION)
      return;
    String experimentName = (String)cb.getSelectedItem();
    final String originalExperimentName = experimentName;
    String experimentId = (String)experimentNamesMap.get(experimentName);
    // produce an unique name for CSMART if necessary
    if (experimentNames.contains(experimentName)) {
      experimentName = 
        getUniqueExperimentName(experimentNames.generateName(experimentName));
      if (experimentName == null)
        return;
    }

    // get threads and groups information
    cmtDialog = new CMTDialog(csmart, this, experimentName, experimentId);
    while (!cmtDialog.isULThreadSelected() && !cmtDialog.wasCancelled()) {
      JOptionPane.showMessageDialog(this,
                                    "You must select at least one thread.",
                                    "No Thread Selected",
                                    JOptionPane.ERROR_MESSAGE);
      // TODO: the following doesn't work, so we recreate the dialog each time
      //      cmtDialog.show();
      //      cmtDialog.toFront();
      cmtDialog = new CMTDialog(csmart, this, experimentName, experimentId);
    }
    if (cmtDialog.wasCancelled())
      return;

    final DefaultMutableTreeNode treeNode = node;
    GUIUtils.timeConsumingTaskStart(organizer);
    try {
      new Thread("SelectExperiment") {
        public void run() {
          cmtDialog.processResults(); // a potentially long process
          final String trialId = cmtDialog.getTrialId();
          if (trialId != null)
            createCMTExperiment(treeNode, originalExperimentName,
                                cmtDialog.getExperimentName(),
                                cmtDialog.getExperimentId(), 
                                trialId, cmtDialog.isCloned());
          GUIUtils.timeConsumingTaskEnd(organizer);
        }
      }.start();
    } catch (RuntimeException re) {
      System.out.println("Runtime exception creating experiment: " + re);
      re.printStackTrace();
      GUIUtils.timeConsumingTaskEnd(organizer);
    }
  }

  // CAUTION: this runs in a non-swing thread
  private void deleteExperimentFromDatabase() {
    Map experimentNamesMap = ExperimentDB.getExperimentNames();
    Set keys = experimentNamesMap.keySet();
    JComboBox cb = new JComboBox(keys.toArray(new String[keys.size()]));
    cb.setEditable(false);
    JPanel panel = new JPanel();
    panel.add(new JLabel("Delete Experiment:"));
    panel.add(cb);
    int result = 
      JOptionPane.showConfirmDialog(null, panel, "Delete Experiment",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION)
      return;
    String experimentName = (String)cb.getSelectedItem();
    String experimentId = (String)experimentNamesMap.get(experimentName);
    ExperimentDB.deleteExperiment(experimentId, experimentName);
  }

  /**
   * Add assembly ids for trial.
   */

  private ArrayList getTrialAssemblyIds(String experimentId, String trialId) {
    ArrayList assemblyIds = new ArrayList();
    try {
      Connection conn = DBUtils.getConnection();
      Map substitutions = new HashMap();
      substitutions.put(":expt_id", experimentId);
      substitutions.put(":trial_id", trialId);
      Statement stmt = conn.createStatement();
      String query = DBUtils.getQuery(EXPT_ASSEM_QUERY, substitutions);
      //      System.out.println("Organizer: getTrialAssemblyIds: " + query);
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
        String asmid = rs.getString(1);
	//        System.out.println("Assembly ID: " + asmid);	  
        assemblyIds.add(asmid);
      }
      rs.close();
      stmt.close();
    } catch (SQLException se) {
      System.err.println("Caught SQL exception: " + se);
      se.printStackTrace();
    }
    return assemblyIds;
  }

  private String getAssemblyMatch(ArrayList assemblyIDs) {
    StringBuffer assemblyMatch = new StringBuffer();
    assemblyMatch.append("in (");
    Iterator iter = assemblyIDs.iterator();
    boolean first = true;
    while (iter.hasNext()) {
      String val = (String)iter.next();
      if (first) {
        first = false;
      } else {
        assemblyMatch.append(", ");
      }
      assemblyMatch.append("'");
      assemblyMatch.append(val);
      assemblyMatch.append("'");
    }
    assemblyMatch.append(")");
    return assemblyMatch.toString();
  }

  /**
   * Get nodes for a trial.
   */

  private ArrayList getNodes(String trialId, String assemblyMatch) {
    ArrayList nodes = new ArrayList();
    try {
      Connection conn = DBUtils.getConnection();
      Map substitutions = new HashMap();
      substitutions.put(":trial_id", trialId);
      substitutions.put(":assemblyMatch", assemblyMatch);
      Statement stmt = conn.createStatement();
      String query = DBUtils.getQuery(EXPT_NODE_QUERY, substitutions);
      //      System.out.println("Organizer: Nodes query: " + query);
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
        nodes.add(rs.getString(1));
      }
      rs.close();
      stmt.close();
      conn.close();
    } catch (SQLException se) {      
      System.err.println("Caught SQL exception: " + se);
      se.printStackTrace();
    }
    return nodes;
  }

  // Currently the passed in args are not used however I expect
  // this to change very soon.
  private ArrayList getHosts(String trialId, String assemblyMatch) {
    ArrayList hosts = new ArrayList();
    try {
      Connection conn = DBUtils.getConnection();
      Map substitutions = new HashMap();
      substitutions.put(":assemblyMatch", assemblyMatch);
      Statement stmt = conn.createStatement();
      String query = DBUtils.getQuery(EXPT_HOST_QUERY, substitutions);
      //      System.out.println("Organizer: Get hosts query: " + query);
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
        hosts.add(rs.getString(1));
      }
      rs.close();
      stmt.close();
      conn.close();
    } catch (SQLException se) {      
      System.err.println("Caught SQL exception: " + se);
      se.printStackTrace();
    }
    return hosts;
  }

  private void mapNodesToHosts(Experiment experiment, 
			       String assemblyMatch ) {

    NodeComponent[] nodeComponents = experiment.getNodes();
    HostComponent[] hostComponents = experiment.getHosts();

    try {
      Connection conn = DBUtils.getConnection();
      Map substitutions = new HashMap();
      substitutions.put(":assemblyMatch", assemblyMatch);
      Statement stmt = conn.createStatement();
      String query = DBUtils.getQuery("queryHostNodes", substitutions);
      //      System.out.println(query);
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
	String hostName = rs.getString(1);
	String nodeName = rs.getString(2);	
	for (int i=0; i < hostComponents.length; i++) {
	  HostComponent hc = hostComponents[i];
	  if (hc.getShortName().equals(hostName)) {
	    for (int j=0; j < nodeComponents.length; j++) {
	      NodeComponent nc = nodeComponents[j];
	      if (nc.getShortName().equals(nodeName)) {
		hc.addNode(nc);
		break;
	      }	      
	    }	    
	    break;
	  }	  
	} 
      }
      rs.close();

      stmt.close();
      conn.close();   
    } catch (SQLException se) {
      System.err.println("Caught SQL exception: " + se);
      se.printStackTrace();
    }
  }

  private void addAgents(Experiment experiment,
                         ArrayList nodes,
                         AgentComponent[] agents,
                         String trialId, String assemblyMatch) {
    Iterator iter = nodes.iterator();
    try {
      Connection conn = DBUtils.getConnection();
      Map substitutions = new HashMap();
      substitutions.put(":trial_id", trialId);
      substitutions.put(":assemblyMatch", assemblyMatch);
      substitutions.put(":insertion_point", "Node.AgentManager.Agent");	  
      Statement stmt = conn.createStatement();
      NodeComponent nodeComponent = null;
      while(iter.hasNext()) {
        String query;
        ResultSet rs;
        String nodeName = (String)iter.next();
        nodeComponent = experiment.addNode(nodeName);
        substitutions.put(":parent_name", nodeName);
        substitutions.put(":comp_alib_id", nodeName);
        // Get args of the node
        Properties nodeProps = nodeComponent.getArguments();
        getComponentArguments(stmt, substitutions, nodeProps);
        // Query All Agents for each node.
        // Loop Query for every node.
        query = DBUtils.getQuery("queryComponents", substitutions);
	//        System.out.println("Organizer: Get agents: " + query);
        rs = stmt.executeQuery(query);
        while(rs.next()) {
          // Find AgentComponent.
          String aName = rs.getString(1);
          for (int i=0; i < agents.length; i++) {
            AgentComponent ac = agents[i];
            if (ac.getShortName().equals(aName)) {
              nodeComponent.addAgent(ac);
              //System.out.println("Organizer:  Adding agent named:  " + ac.getShortName());
              break;
            }	    
          } 	  
        }
        rs.close();
      }
      stmt.close();
      conn.close();   
    } catch (SQLException se) {
      System.err.println("Caught SQL exception: " + se);
      se.printStackTrace();
    }
  }

  private void getComponentArguments(Statement stmt,
                                     Map substitutions,
                                     Properties props)
    throws SQLException
  {
    String query = DBUtils.getQuery("queryComponentArgs", substitutions);
    ResultSet rs = stmt.executeQuery(query);
    while (rs.next()) {
      String arg = rs.getString(1);
      if (arg.startsWith("-D")) {
        int equalsIx = arg.indexOf('=', 2);
        String pname = arg.substring(2, equalsIx);
        String value = arg.substring(equalsIx + 1);
        props.setProperty(pname, value);
      }
    }
    rs.close();
  }

  private List checkForRecipes(String trialId, String exptId) {
    List recipeList = new ArrayList();
    try {
      Connection conn = DBUtils.getConnection();
      Map substitutions = new HashMap();
      substitutions.put(":trial_id", trialId);
      substitutions.put(":expt_id", exptId);
      Statement stmt = conn.createStatement();
      String query = DBUtils.getQuery("queryRecipes", substitutions);
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
        try {
          DbRecipe dbRecipe = new DbRecipe(rs.getString(2), Class.forName(rs.getString(3)));
          String recipeId = rs.getString(1);
          substitutions.put(":recipe_id", recipeId);
          getRecipeProperties(dbRecipe, conn, substitutions);
          recipeList.add(dbRecipe);
        } catch (ClassNotFoundException cnfe) {
          System.err.println(cnfe + ": for recipe");
        }
      }
      rs.close();
      stmt.close();
      conn.close();   
    } catch (SQLException se) {
      System.err.println("Caught SQL exception: " + se);
      se.printStackTrace();
    }    
    
    return recipeList;
  }

  private Map getRecipeNamesFromDatabase() {
    Map recipes = new TreeMap();

    try {
      Connection conn = DBUtils.getConnection();
      Statement stmt = conn.createStatement();
      Map substitutions = new HashMap();
      String query = DBUtils.getQuery("queryLibRecipes", substitutions);
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
        recipes.put(rs.getString(2), rs.getString(1));
      }
      rs.close();
      stmt.close();
      conn.close();   
    } catch (SQLException se) {
      System.err.println("Caught SQL exception: " + se);
      se.printStackTrace();
    }    
    
    return recipes;
  }

  private void getRecipeProperties(DbRecipe dbRecipe, Connection conn, Map substitutions)
    throws SQLException
  {
    Statement stmt = conn.createStatement();
    String query = DBUtils.getQuery("queryRecipeProperties", substitutions);
    ResultSet rs = stmt.executeQuery(query);
    while(rs.next()) {
      dbRecipe.props.put(rs.getString(1), rs.getString(2));
    }
    rs.close();
  }

  private DbRecipe getDatabaseRecipe(String recipeId) {
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Map substitutions = new HashMap();
        substitutions.put(":recipe_id", recipeId);
        Statement stmt = conn.createStatement();
        String query = DBUtils.getQuery("queryRecipe", substitutions);
        ResultSet rs = stmt.executeQuery(query);
        if (rs.next()) {
          try {
            DbRecipe dbRecipe = new DbRecipe(rs.getString(2), Class.forName(rs.getString(3)));
            getRecipeProperties(dbRecipe, conn, substitutions);
            return dbRecipe;
          } catch (ClassNotFoundException cnfe) {
            System.err.println(cnfe + ": for recipe");
          }
        }
        System.err.println("Recipe not found: " + recipeId);
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      System.err.println("Caught SQL exception: " + se);
      se.printStackTrace();
    }    
    return null;
  }

  private void setRecipeComponentProperties(DbRecipe dbRecipe, RecipeComponent mc) {
    for (Iterator i = dbRecipe.props.keySet().iterator(); i.hasNext(); ) {
      try {
        String propName = (String) i.next();
        String propValue = (String) dbRecipe.props.get(propName);
        Property prop = mc.getProperty(propName);
        Class propClass = prop.getPropertyClass();
        Constructor constructor = propClass.getConstructor(new Class[] {String.class});
        Object value = constructor.newInstance(new Object[] {propValue});
        prop.setValue(value);
      } catch (Exception e) {
        System.err.println("Organizer: [setRecipeComponentProperties] Caught exception: " + e);
      }
    }
  }

  /**
   * Create a CMT society with the specified assembly ids
   * (which define threads and groups) and
   * the specified nodes.
   * Caution: this runs in a non-Swing thread and updates the Organizer tree
   */

  private void createCMTExperiment(DefaultMutableTreeNode node,
                                   String originalExperimentName,
                                   String experimentName,
                                   String experimentId,
                                   String trialId,
                                   boolean isCloned) {
    //    System.out.println("Creating society");
    // get assembly ids for trial
    ArrayList assemblyIds = getTrialAssemblyIds(experimentId, trialId);
    String assemblyMatch = getAssemblyMatch(assemblyIds);
    // get nodes for trial
    ArrayList nodes = getNodes(trialId, assemblyMatch);
    ArrayList hosts = getHosts(trialId, assemblyMatch);
    CMTSociety soc = null;
    if (assemblyIds.size() != 0) {
      soc = new CMTSociety(assemblyIds);      
      soc.initProperties();
    } else { // We need to create a new trial.
      // Need to have the experiment id, trial id, and multiplicity
      // for the call that will generate the assembly here.
      System.out.println("No assemblies for: " + experimentId + " " + trialId);
      return; // creating an experiment from scratch not implemented yet
    }
    Experiment experiment = new Experiment((String)experimentName, 
                                           experimentId, trialId);
    // The following is ugly
    
    setDefaultNodeArguments(experiment, assemblyMatch,
                            ComponentData.SOCIETY
                            + "|"
                            + originalExperimentName);
    experiment.setCloned(isCloned);

    //to hold all potential agents
    List agents = new ArrayList();  

    List recipes = checkForRecipes(trialId, experimentId);
    if (recipes.size() != 0) {
      Iterator metIter = recipes.iterator();
      while (metIter.hasNext()) {
        DbRecipe dbRecipe = (DbRecipe) metIter.next();
        RecipeComponent mc = createRecipeComponent(dbRecipe.name, dbRecipe.cls);
        setRecipeComponentProperties(dbRecipe, mc);
        AgentComponent[] recagents = mc.getAgents(); 
        if (recagents != null && recagents.length > 0) {
          agents.addAll(Arrays.asList(recagents));
        }      
        experiment.addRecipe(mc);
        if (!recipeNames.contains(mc.getRecipeName())) {
          addRecipeToWorkspace(mc, node);
        }
      }
    }
    AgentComponent[] socagents = soc.getAgents();
    if (socagents!= null && socagents.length > 0) {
      agents.addAll(Arrays.asList(socagents));
    }
    AgentComponent[] allagents = (AgentComponent[])agents.toArray(new AgentComponent[agents.size()]); 

    experiment.addSocietyComponent((SocietyComponent)soc);
    // Add all Nodes.
    addAgents(experiment, nodes, allagents, trialId, assemblyMatch);
    // Add all Hosts.
    Iterator hostIter = hosts.iterator();
    while (hostIter.hasNext()) {
      String hostName = (String) hostIter.next();
      HostComponent hc = experiment.addHost(hostName);
      setComponentProperties((ConfigurableComponent) hc, hostName, assemblyMatch);
    }
    mapNodesToHosts(experiment, assemblyMatch);
    workspace.setSelection(addExperimentToWorkspace(experiment, node));
    // if the experiment hasn't been cloned, then save it so it's runnable
    if (!isCloned)
      experiment.saveToDb();
  }

  private void setComponentProperties(ConfigurableComponent cc,
                                      String comp_alib_id,
                                      String assemblyMatch)
  {
    try {
      Connection conn = DBUtils.getConnection();
      Map substitutions = new HashMap();
      substitutions.put(":assemblyMatch", assemblyMatch);
      substitutions.put(":comp_alib_id", comp_alib_id);
      Statement stmt = conn.createStatement();
      String query = DBUtils.getQuery(COMPONENT_ARGS_QUERY, substitutions);
      //      System.out.println("Organizer " + COMPONENT_ARGS_QUERY + ": "  + query);
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
        String param = rs.getString(1);
        if (param.startsWith(Experiment.PROP_PREFIX)) {
          int ix1 = Experiment.PROP_PREFIX.length();
          int ix2 = param.indexOf('=', ix1);
          String pname = param.substring(ix1, ix2);
          String pvalue = param.substring(ix2 + 1);
          Property prop = cc.getProperty(pname);
          if (prop == null) {
	    //            System.out.println("adding " + pname + "=" + pvalue);
            cc.addProperty(pname, pvalue);
          } else {
	    //            System.out.println("setting " + pname + "=" + pvalue);
            prop.setValue(pvalue);
          }
        }
      }
      rs.close();
      stmt.close();
      conn.close();
    } catch (SQLException se) {      
      System.err.println("Caught SQL exception: " + se);
      se.printStackTrace();
    }
  }

  private void setDefaultNodeArguments(Experiment experiment,
                                       String assemblyMatch,
                                       String socAlibId)
  {
    Properties props = experiment.getDefaultNodeArguments();
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Statement stmt = conn.createStatement();
        Map substitutions = new HashMap();
        substitutions.put(":assemblyMatch", assemblyMatch);
        substitutions.put(":comp_alib_id", socAlibId);
        getComponentArguments(stmt, substitutions, experiment.getDefaultNodeArguments());
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      System.err.println("Caught SQL exception: " + se);
      se.printStackTrace();
    }
  }

  /**
   * Ensure that experiment name is unique in both CSMART and the database.
   */

  public String getUniqueExperimentName(String name) {
    while (true) {
      name = (String) JOptionPane.showInputDialog(this, "Enter Experiment Name",
                                                  "Experiment Name",
                                                  JOptionPane.QUESTION_MESSAGE,
                                                  null, null,
                                                  name);
      if (name == null) return null;
      // if name is unique in CSMART
      if (!experimentNames.contains(name)) {
        // ensure that name is not in the database
        boolean inDatabase = false;
        // if can't reach database, just assume that name is ok
        try {
          inDatabase = ExperimentDB.isExperimentNameInDatabase(name);
        } catch (RuntimeException e) {
          System.err.println(e);
        }
        if (inDatabase) {
          int answer = JOptionPane.showConfirmDialog(this,
                     "This name is in the database; use an unique name",
                                                 "Experiment Name Not Unique",
						 JOptionPane.OK_CANCEL_OPTION,
						 JOptionPane.ERROR_MESSAGE);
          if (answer != JOptionPane.OK_OPTION) return null;
        } else
          break; // have unique name
      } else {
        int answer = JOptionPane.showConfirmDialog(this,
						 "Use an unique name",
						 "Experiment Name Not Unique",
						 JOptionPane.OK_CANCEL_OPTION,
						 JOptionPane.ERROR_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return null;
      }
    }
    return name;
  }

  private DefaultMutableTreeNode newExperiment(DefaultMutableTreeNode node) {
    String name = getUniqueExperimentName(experimentNames.generateName());
    if (name == null) return null;

    try {
      Experiment experiment = new Experiment(name);
      DefaultMutableTreeNode newNode =
	addExperimentToWorkspace(experiment, node);
      // if a single society is selected, add it to the experiment
      DefaultMutableTreeNode selectedNode = getSelectedNode();
      if (selectedNode != null) {
	Object o = selectedNode.getUserObject();
	if (o instanceof SocietyComponent) 
	  experiment.addSocietyComponent((SocietyComponent)o);
      }
      workspace.setSelection(newNode);
      return newNode;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private DefaultMutableTreeNode addExperimentToWorkspace(Experiment experiment, 
							  DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode = 
      new DefaultMutableTreeNode(experiment, false);
    addNode(node, newNode);
    experimentNames.add(experiment.getExperimentName());
    // TODO: if add experiment properties,
    // then need to install listeners
    experiment.addModificationListener(myModificationListener);
    return newNode;
  }
  
  private void renameExperiment(DefaultMutableTreeNode node) {
    String name = JOptionPane.showInputDialog("New experiment name");
    renameExperiment(node, name);
  }
  
  // Separate method that takes a new name, for use by model.valueChanged...
  private void renameExperiment(DefaultMutableTreeNode node, String name) {
    final Experiment experiment =
      (Experiment) node.getUserObject();
    if (name == null || name.equals(experiment.getExperimentName()) || name.equals("")) return;
    while (true) {
      if (!experimentNames.contains(name)) break;
      int ok = JOptionPane.showConfirmDialog(this,
					     "Use an unique name",
					     "Experiment Name Not Unique",
					     JOptionPane.OK_CANCEL_OPTION,
					     JOptionPane.ERROR_MESSAGE);
      if (ok != JOptionPane.OK_OPTION) return;
      name = JOptionPane.showInputDialog("New experiment name");
      if (name == null || name.equals("")) return;
    }
    if (name != null) {
      experimentNames.remove(experiment.getExperimentName());
      experiment.setName(name);
      experiment.setCloned(false);
      GUIUtils.timeConsumingTaskStart(organizer);
      try {
        new Thread("SaveExperiment") {
          public void run() {
            experiment.saveToDb(); // save under new name
            GUIUtils.timeConsumingTaskEnd(organizer);
          }
        }.start();
      } catch (RuntimeException re) {
        System.out.println("Runtime exception saving experiment: " + re);
        re.printStackTrace();
        GUIUtils.timeConsumingTaskEnd(organizer);
      }
      experimentNames.add(name);
      // This next line is the key line, that updates things
      model.nodeChanged(node);
    }
  }
  
  private void deleteExperiment(DefaultMutableTreeNode node) {
    if (node == null) return;
    Experiment experiment = (Experiment)node.getUserObject();
    if (!experiment.isEditable()) {
      String reason = "has been run";
      if (experiment.isRunning())
	reason = "is in use";
      int result = JOptionPane.showConfirmDialog(this,
						 "Experiment " + reason + "; delete anyway?",
						 "Experiment Not Editable",
						 JOptionPane.YES_NO_OPTION,
						 JOptionPane.WARNING_MESSAGE);
      if (result != JOptionPane.YES_OPTION)
	return;
    }
    model.removeNodeFromParent(node);
    experimentNames.remove(experiment.getExperimentName());
    // if experiment was in database, ask if it should be deleted from there
    if (experiment.isInDatabase()) {
      int result = 
        JOptionPane.showConfirmDialog(this,
                                      "Delete experiment from database?",
                                      "Delete Experiment From Database",
                                      JOptionPane.YES_NO_OPTION,
                                      JOptionPane.WARNING_MESSAGE);
      if (result != JOptionPane.YES_OPTION)
        return;

      final String id = experiment.getExperimentID();
      final String name = experiment.getShortName();
      GUIUtils.timeConsumingTaskStart(organizer);
      try {
        new Thread("DeleteExperiment") {
          public void run() {
            ExperimentDB.deleteExperiment(id, name);
            GUIUtils.timeConsumingTaskEnd(organizer);
          }
        }.start();
      } catch (RuntimeException re) {
        System.out.println("Runtime exception deleting experiment: " + re);
        re.printStackTrace();
        GUIUtils.timeConsumingTaskEnd(organizer);
      }
    }
  }
  
  private void newFolder(DefaultMutableTreeNode node) {
    String name = JOptionPane.showInputDialog("New folder name");
    if (name != null) {
      DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name, true);
      addNode(node, newNode);
      workspace.setSelection(newNode);
    }
  }
  
  private void addNode(DefaultMutableTreeNode node, DefaultMutableTreeNode newNode) {
    if (node == null)
      node = root;
    model.insertNodeInto(newNode, node, node.getChildCount());
    workspace.scrollPathToVisible(new TreePath(newNode.getPath()));
    //          TreePath path = new TreePath(newNode.getPath());
    //          workspace.expandPath(path);
    //          workspace.setSelectionPath(path);
    //          System.out.println(path);
  }
  
  private void renameFolder(DefaultMutableTreeNode node) {
    String name = JOptionPane.showInputDialog("New folder name");
    // FIXME Unique?
    renameFolder(node, name);
  }
  
  private void renameFolder(DefaultMutableTreeNode node, String name) {    
    // FIXME Unique?
    if (name != null && ! name.equals("")) {
      node.setUserObject(name);
      model.nodeChanged(node);
    }
  }
  
  private void deleteFolder(DefaultMutableTreeNode node) {
    if (node == root) return;
    if (node == null) return;
    int reply = 
      JOptionPane.showConfirmDialog(this,
                                    "Delete folder and all its contents?",
                                    "Delete Folder",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE);
    if (reply == JOptionPane.OK_OPTION)
      model.removeNodeFromParent(node);
  }

  private void restore(String fileName) {
    PropertyListener l = null;
    try {
      File f = new File(fileName);
      this.workspaceFileName = f.getPath();
      if (f.canRead()) {
	ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
	try {
	  root = (DefaultMutableTreeNode) ois.readObject();
	  for (Enumeration e = root.depthFirstEnumeration();
	       e.hasMoreElements(); ) {
	    DefaultMutableTreeNode node =
	      (DefaultMutableTreeNode) e.nextElement();
	    Object o = node.getUserObject();
	    if (o instanceof Experiment) {
	      ((Experiment)o).addModificationListener(myModificationListener);
	      // This used to be just Societies, but do it for everything...
	    } else if (o instanceof ModifiableConfigurableComponent) {
	      installListeners((ModifiableConfigurableComponent) o);
	    }
	  } // end of for loop
	} catch (Exception e) {
	  System.err.println("Organizer: can't read file: " + f);
	} finally {
	  ois.close();
	} 	  
      }
      if (root == null)
	root = new DefaultMutableTreeNode();
      String rootName = f.getName();
      int extpos = rootName.lastIndexOf('.');
      if (extpos >= 0) 
	rootName = rootName.substring(0, extpos);
      root.setUserObject(rootName);
      //            model = new DefaultTreeModel(root);
      model = createModel(this, root);
      Class[] noTypes = new Class[0];
      try {
	societyNames.init(getLeaves(SocietyComponent.class, root),
			  SocietyComponent.class.getMethod("getSocietyName", noTypes));
	experimentNames.init(getLeaves(Experiment.class, root),
			     Experiment.class.getMethod("getExperimentName", noTypes));
	recipeNames.init(getLeaves(RecipeComponent.class, root),
			 RecipeComponent.class.getMethod("getRecipeName", noTypes));
	componentNames.init(getLeaves(ModifiableConfigurableComponent.class, root),
			    ModifiableConfigurableComponent.class.getMethod("getShortName", noTypes));
	return;
      } catch (NoSuchMethodException nsme) {
	nsme.printStackTrace();
      } catch (SecurityException se) {
	se.printStackTrace();
      }
    } catch (Exception ioe) {
	ioe.printStackTrace();
	return;
    }
  } // end of restore
    
  private void initSocietyNames() {
    // initialize known society names
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      if (node.isLeaf()) {
	Object o = node.getUserObject();
	if (o instanceof SocietyComponent)
	  societyNames.add(((SocietyComponent)o).getSocietyName());
      }
    }
  }
  
  /** 
   * Force an update, which saves the current workspace.
   */
  public void save() {
    update();
  }
  
  private void save(String fileName) {
    //      System.out.println("Saving to: " + fileName);
    if (!fileName.endsWith(".bin"))
      fileName = fileName + ".bin";
    try {
      File f = new File(fileName);
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            try {
	      oos.writeObject(root);
            } finally {
	      oos.close();
            }
    } catch (Exception ioe) {
      ioe.printStackTrace();
    }
  }
  
  private Thread updater = new Thread() {
      public void start() {
	updateNeeded = false;
	super.start();
      }
      public void run() {
	synchronized (root) {
	  while (true) {
	    try {
	      long now = System.currentTimeMillis();
	      if (updateNeeded && now > nextUpdate) {
		save(workspaceFileName);
		updateNeeded = false;
		root.notifyAll(); // In case anyone's waiting for the update to finish
	      } else if (updateNeeded) {
		long delay = nextUpdate - now;
		if (delay > 0) {
		  root.wait(delay);
                            }
	      } else {
		root.wait();
	      }
	    } catch (InterruptedException ie) {
	    }
	  }
	}
      }
    };
  
  private void update() {
    synchronized (root) {
      nextUpdate = System.currentTimeMillis() + UPDATE_DELAY;
      updateNeeded = true;
      root.notify();
    }
  }
  
  private TreeModelListener myModelListener = new TreeModelListener() {
      public void treeNodesChanged(TreeModelEvent e) {
	update();
      }
      public void treeNodesInserted(TreeModelEvent e) {
	update();
      }
      public void treeNodesRemoved(TreeModelEvent e) {
	update();
      }
      public void treeStructureChanged(TreeModelEvent e) {
	update();
      }
    };
  
  private void setFrameTitle() {
    setFrameTitle(workspace.getSelectionPath());
  }
  
  private void setFrameTitle(TreePath newSelection) {
    String doc;
    if (newSelection != null) {
      doc = newSelection.toString();
    } else {
      doc = null;
    }
    setFrameTitleDocument(doc);
  }
  
  private void setFrameTitleDocument(String doc) {
    JFrame frame =
      (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, workspace);
    if (frame == null) return;
    String oldTitle = frame.getTitle();
    if (oldTitle == null) {
      oldTitle = FRAME_TITLE;
    } else {
      int colon = oldTitle.indexOf(':');
      if (colon >= 0) oldTitle = oldTitle.substring(0, colon).trim();
    }
    if (doc == null)
      frame.setTitle(oldTitle);
    else
      frame.setTitle(oldTitle + ":" + doc);
  }
  
  private TreeSelectionListener mySelectionListener =
    new TreeSelectionListener()
      {
        public void valueChanged(TreeSelectionEvent e) {
	  setFrameTitle(e.getNewLeadSelectionPath());
        }
      };
  
  private AncestorListener myAncestorListener =
    new AncestorListener()
      {
        public void ancestorAdded(AncestorEvent e) {
	  setFrameTitle();
        }
        public void ancestorRemoved(AncestorEvent e) {}
        public void ancestorMoved(AncestorEvent e) {}
      };
  
//      public void setRoot(ObjectInputStream init) {
//          root = (DefaultMutableTreeNode) init.readObject();
//      }

  /**
   * Create a new experiment and make it be the selected experiment.
   * Allows tools that need an experiment to create one
   * if it does not exist when the tool is invoked.
   */
  public void addExperiment() {
    DefaultMutableTreeNode newNode = newExperiment(root);
  }

  private DefaultMutableTreeNode findNode(Object userObject) {
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      //      System.out.println("Have Element");
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      if (node.isLeaf() &&
	  node.getUserObject().equals(userObject))
	return node;
    }
    return null;
  }
  
  /**
   * Fully expand the tree; called in initialization
   * so that the initial view of the tree is fully expanded.
   */
  private void expandTree() {
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      workspace.expandPath(new TreePath(node.getPath()));
    }
  }
  
  /**
   * Create a new society and make it be the selected society.
   * Allows tools that need a society to create one,
   * if none exists when the tool is invoked.
   */
  public void addSociety() {
    DefaultMutableTreeNode newNode = newSociety(root);
  }
  
  public void addTreeSelectionListener(TreeSelectionListener listener) {
    workspace.addTreeSelectionListener(listener);
  }
  

  ////////////////////////////////////
  // Stuff to copy items
  
  /**
   * Copy an experiment.
   */
  private void copyExperimentInNode(DefaultMutableTreeNode node) {
    copyExperiment((Experiment)node.getUserObject(), node.getParent());
  }
  
  public Experiment copyExperiment(Experiment experiment, Object context) {
    // context is the tree node of the folder containing the experiment
    // Fixme: if make Experiment hold everything in properties, then can
    // use the Component copy method
    final Experiment experimentCopy = experiment.copy(this, context);
    // if the experiment was from a database, then save copy in the database
    // if the copy isn't modified, this is the only place it's put in the
    // database and hence made runnable
    if (experiment.isInDatabase()) {
      experimentCopy.setCloned(false);
      GUIUtils.timeConsumingTaskStart(organizer);
      try {
        new Thread("DuplicateExperiment") {
            public void run() {
              experimentCopy.saveToDb();
              GUIUtils.timeConsumingTaskEnd(organizer);
            }
          }.start();
      } catch (RuntimeException re) {
        System.out.println("Runtime exception duplicating experiment: " + re);
        re.printStackTrace();
        GUIUtils.timeConsumingTaskEnd(organizer);
      }
    }
    if (context == null)
      // add copy as sibling of original
      workspace.setSelection(addExperimentToWorkspace(experimentCopy,
			       (DefaultMutableTreeNode)findNode(experiment).getParent()));
    else
      workspace.setSelection(addExperimentToWorkspace(experimentCopy, 
			       (DefaultMutableTreeNode)context));
    return experimentCopy;
  }
  
  private void copySocietyInNode(DefaultMutableTreeNode node) {
    copySociety((SocietyComponent)node.getUserObject(), node.getParent());
  }
  
  public SocietyComponent copySociety(SocietyComponent society, 
				      Object context) {
    //    SocietyComponent societyCopy = society.copy(this, context);
    // Create a new society whose name is based on the old one
    SocietyComponent societyCopy = null;

    if(society instanceof CMTSociety) {
      societyCopy = createSoc(generateSocietyName(society.getSocietyName()), 
                              ((CMTSociety)society).getAssemblyID(), society.getClass());
    } else {
      societyCopy = createSoc(generateSocietyName(society.getSocietyName()), society.getClass());
    }
    // use the base copy method in ComponentProperties
    society.copy(societyCopy);
    boolean builtIn = false;
    for (int i = 0; i < socComboItems.length; i++) 
      if (((ComboItem)(socComboItems[i])).cls.isInstance(societyCopy)) {
        builtIn = true;
        break;
      }
    // don't add societies from database experiments to the workspace
    if (!builtIn) {
      societyNames.add(societyCopy.getSocietyName());
      return societyCopy;
    }
    // add society to workspace
    if (context == null)
      workspace.setSelection(addSocietyToWorkspace(societyCopy,
                             (DefaultMutableTreeNode)findNode(society).getParent()));
    else
      workspace.setSelection(addSocietyToWorkspace(societyCopy, (DefaultMutableTreeNode)context));
    return societyCopy;
  }
  
  private void copyComponentInNode(DefaultMutableTreeNode node) {
    copyComponent((ModifiableConfigurableComponent)node.getUserObject(), node.getParent());
  }
  
  public ModifiableConfigurableComponent copyComponent(ModifiableConfigurableComponent comp, 
						       Object context) {
    if (comp instanceof SocietyComponent)
      return (ModifiableConfigurableComponent)copySociety((SocietyComponent)comp, context);
    else if (comp instanceof Experiment)
      return (ModifiableConfigurableComponent)copyExperiment((Experiment)comp, context);
    else if (comp instanceof RecipeComponent)
      return comp; // don't copy recipes
      //      return (ModifiableConfigurableComponent)copyRecipe((RecipeComponent)comp, context);
    //ModifiableConfigurableComponent compCopy = comp.copy(this, context);
    // FIXME!!!
    ModifiableConfigurableComponent compCopy = comp;
    if (context == null)
      // add copy as sibling of original
      workspace.setSelection(addComponentToWorkspace(compCopy, 
			      (DefaultMutableTreeNode)findNode(comp).getParent()));
    else
      workspace.setSelection(addComponentToWorkspace(compCopy, (DefaultMutableTreeNode)context));
    return compCopy;
  }
  
  private void copyRecipeInNode(DefaultMutableTreeNode node) {
    copyRecipe((RecipeComponent)node.getUserObject(), node.getParent());
  }
  
  public RecipeComponent copyRecipe(RecipeComponent recipe, Object context) {
    // FIXME!!!!
    RecipeComponent recipeCopy = createRecipeComponent(generateRecipeName(recipe.getRecipeName()), recipe.getClass());
    recipe.copy(recipeCopy);
    //public Recipe copyRecipe(Recipe recipe, Object context) {
    // Fixme: once only recipes are components,
    // get rid of this & do real component copy
    //Recipe recipeCopy = recipe.copy(this, context);
    if (context == null)
      workspace.setSelection(addRecipeToWorkspace(recipeCopy, (DefaultMutableTreeNode)findNode(recipe).getParent()));
    else 
      workspace.setSelection(addRecipeToWorkspace(recipeCopy, (DefaultMutableTreeNode)context));
    return recipeCopy;
  }
  
  public String generateExperimentName(String name) {
    return experimentNames.generateName(name);
  }
  
  public String generateSocietyName(String name) {
    return societyNames.generateName(name);
  }

  public String generateComponentName(String name) {
    return componentNames.generateName(name);
  }

  public String generateRecipeName(String name) {
    return recipeNames.generateName(name);
  }

  // Override the default tree model
  // This is necessary because when edits happen on Nodes in a
  // DefaultTreeModel, the objects are treated as Strings.
  // The result is that whenever a user edits
  // an entry in the model, it becomes a string.
  // Instead, use the main rename functions from above
  // Note that the key thing those above methods do is call
  // model.nodeChanged(node) if the node in fact changed.
  // This causes the model to tell the tree the new size of the label,
  // for example
  private DefaultTreeModel createModel(final Organizer myorg, DefaultMutableTreeNode node) {
    return new DefaultTreeModel(node) {
	public void valueForPathChanged(TreePath path, Object newValue) {
	  if (newValue == null) return;
	  DefaultMutableTreeNode aNode = (DefaultMutableTreeNode)path.getLastPathComponent();
	  
	  if (aNode.getUserObject() instanceof SocietyComponent) {
	    //	    System.err.println("Resetting node type to SocietyComponent");
	    myorg.renameSociety(aNode, newValue.toString());
	    
	  } else if (aNode.getUserObject() instanceof RecipeComponent) {
	    myorg.renameRecipe(aNode, newValue.toString());
	  } else if (aNode.getUserObject() instanceof Experiment) {
	    myorg.renameExperiment(aNode, newValue.toString());
	  } else if (aNode == myorg.root) {
	    // trying to rename the workspace
	    myorg.renameWorkspace(newValue.toString());
	  } else if (aNode.getUserObject() instanceof ModifiableConfigurableComponent) {
	    myorg.renameComponent(aNode, newValue.toString());
	  } else {
	    // This must be a folder?
	    aNode.setUserObject(newValue);
	    nodeChanged(aNode);
	  }	  
	}
      };
  }

  // to guarantee unique names
  private static class UniqueNameSet extends HashSet {
    private String prefix;
    private int nameCounter = 0;
    public UniqueNameSet(String prefix) {
      this.prefix = prefix;
    }
    public void init(Object[] things, Method getNameMethod) {
      Object[] noArgs = new Object[0];
      for (int i = 0; i < things.length; i++) {
	try {
	  String name = (String) getNameMethod.invoke(things[i], noArgs);
	  add(name);
	} catch (Exception e) {
	  System.err.println("Reading: " + things[i]);
	  e.printStackTrace();
	}
      }
    }
    
    public String generateName() {
      return generateName(prefix);
    }
    public String generateName(String name) {
      if (contains(name)) {
	String base = name;
	do {
	  name = base + ++nameCounter;
	} while (contains(name));
      }
      return name;
    }
  } // end of UniqueNameSet definition

  // Class for holding name/Class pairs in UIs
  private static class ComboItem {
    public String name;
    public Class cls;
    public ComboItem(String name, Class cls) {
      this.cls = cls;
      this.name = name;
    }
    public String toString() {
      return name;
    }
  }
  private static class DbRecipe extends ComboItem {
    public Map props = new TreeMap();
    public DbRecipe(String name, Class cls) {
      super(name, cls);
    }
  }
}

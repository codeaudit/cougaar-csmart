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

import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.db.DBConflictHandler;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.core.property.ModificationListener;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.PropertyListener;
import org.cougaar.tools.csmart.core.property.PropertiesListener;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.recipe.BasicMetric;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.recipe.ComponentInsertionRecipe;
import org.cougaar.tools.csmart.recipe.SpecificInsertionRecipe;
import org.cougaar.tools.csmart.recipe.AgentInsertionRecipe;
import org.cougaar.tools.csmart.recipe.ParameterInsertionRecipe;
import org.cougaar.tools.csmart.recipe.ServletGroupInsertionRecipe;
import org.cougaar.tools.csmart.recipe.ABCImpact;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.abc.ABCSociety;
import org.cougaar.tools.csmart.society.scalability.ScalabilityXSociety;
import org.cougaar.tools.csmart.society.cmt.CMTSociety;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * The Organizer holds all the component a user creates
 * and manipulates in CSMART
 */
public class Organizer extends JScrollPane {
  private static final String DEFAULT_FILE_NAME = "Default Workspace.bin";
  
  private static final String FRAME_TITLE = "CSMART Launcher";

  private static final long UPDATE_DELAY = 5000L;

  private boolean updateNeeded = false;
  
  private long nextUpdate = 0L;
  
  private String workspaceFileName;
  private CSMART csmart;
  private DefaultMutableTreeNode root;
  DefaultTreeModel model;
  private OrganizerTree workspace;
  private Organizer organizer;
  private CMTDialog cmtDialog;

  private transient Logger log;

  private DBConflictHandler saveToDbConflictHandler =
    GUIUtils.createSaveToDbConflictHandler(this);
  
  // The societies which can be created in CSMART
  private NameClassItem[] builtInSocieties = {
    new NameClassItem("Scalability", ScalabilityXSociety.class),
    new NameClassItem("ABC", ABCSociety.class),
  };

  // The stand-alone recipes that can be created in CSMART
  private Object[] metNameClassItems = {
    new NameClassItem("Basic Metric", BasicMetric.class),
    new NameClassItem("Component Insertion", ComponentInsertionRecipe.class),
    new NameClassItem("Specific Insertion", SpecificInsertionRecipe.class),
    new NameClassItem("Agent Insertion", AgentInsertionRecipe.class),
    new NameClassItem("Parameter Insertion", ParameterInsertionRecipe.class),
    new NameClassItem("Servlet Group Insertion", ServletGroupInsertionRecipe.class),
    new NameClassItem("ABCImpact", ABCImpact.class),
  };

  // Define Unique Name sets
  private UniqueNameSet societyNames = new UniqueNameSet("Society");
  private UniqueNameSet experimentNames = new UniqueNameSet("Experiment");
  private UniqueNameSet recipeNames = new UniqueNameSet("Recipe");
  private UniqueNameSet folderNames = new UniqueNameSet("Folder");

  // define helper class
  private OrganizerHelper helper = new OrganizerHelper();
  
  // Constructors
  public Organizer(CSMART csmart) {
    this(csmart, null);
    organizer = this;
  }
  
  public Organizer(CSMART csmart, String workspaceFileName) {
    createLogger();

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
      model = new DefaultTreeModel(root);
    }
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
	  if (userObject instanceof ModifiableComponent)
	    return ((ModifiableComponent)userObject).isEditable();
	  else
  	    return true; // renaming workspace or folder is always ok
	}
	return false;
      }
    };
    workspace.setCellEditor(myEditor);
  
    workspace.setExpandsSelectedPaths(true);
    workspace.addTreeSelectionListener(mySelectionListener);
    workspace.addAncestorListener(myAncestorListener);
    workspace.addMouseListener(new OrganizerMouseListener(this, workspace));
    workspace.setSelection(root);
    expandTree(); // fully expand workspace tree
    panel.add(workspace);
    setViewportView(panel);
    updater.start();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  ///////////////////////////////////////  
  // Methods to get the user's selection
  ///////////////////////////////////////  

  public DefaultMutableTreeNode getSelectedNode() {
    TreePath selPath = workspace.getSelectionPath();
    if (selPath == null) return null;
    return (DefaultMutableTreeNode) selPath.getLastPathComponent();
  }
  
  public SocietyComponent[] getSelectedSocieties() {
    return (SocietyComponent[]) getSelectedLeaves(SocietyComponent.class);
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

  ////////////////////////////////////  
  // Start tools
  ////////////////////////////////////  

  public void startBuilder() {
    csmart.runBuilder((ModifiableComponent) getSelectedNode().getUserObject(), 
                      false);
  }

  /**
   * When called from Organizer, the selected user object is an experiment.
   * When called from CSMART File-Build menu, the
   * selected user object is a recipe or society.
   */

  public void startExperimentBuilder() {
    Experiment experiment = null;
    DefaultMutableTreeNode node = getSelectedNode();
    Object o = node.getUserObject();
    if (o instanceof RecipeComponent || o instanceof SocietyComponent) {
      if (o instanceof SocietyComponent) {
        SocietyComponent sc = (SocietyComponent) o;
        String name = "Experiment for " + sc.getSocietyName();
        experiment = new Experiment(experimentNames.generateName(name),
                                    sc,
                                    new RecipeComponent[0]);
      } else if (o instanceof RecipeComponent) {
        RecipeComponent recipe = (RecipeComponent) o;
        String name = "Experiment for " + recipe.getRecipeName();
        experiment = new Experiment(experimentNames.generateName(name),
                                    null,
                                    new RecipeComponent[] {recipe});
      }
      DefaultMutableTreeNode newNode =
        new DefaultMutableTreeNode(experiment, false);
      DefaultMutableTreeNode parentNode =
        (DefaultMutableTreeNode) node.getParent();
      model.insertNodeInto(newNode, parentNode, parentNode.getIndex(node) + 1);
      // make the new node be the selected node
      workspace.setSelection(newNode);
      experimentNames.add(experiment.getExperimentName());
      experiment.addModificationListener(myModificationListener);
    } else if (o instanceof Experiment) 
      experiment = (Experiment) o;
    if (experiment != null)
      csmart.runExperimentBuilder(experiment, false);
  }

  /**
   * Try to run the console.  This is called in two cases:
   * 1) the user has selected a runnable experiment
   * 2) the user has selected a society and this method creates an experiment
   */

  public void startConsole() {
    DefaultMutableTreeNode node = getSelectedNode();
    Object o = node.getUserObject();
    Experiment experiment;
    if (o instanceof SocietyComponent) {
      SocietyComponent sc = (SocietyComponent) o;
      String name = "Temp Experiment for " + sc.getSocietyName();
      name = experimentNames.generateName(name);
      experiment = new Experiment(name,
				  sc,
				  new RecipeComponent[0]);
      DefaultMutableTreeNode parent = 
	(DefaultMutableTreeNode) node.getParent();
      DefaultMutableTreeNode newNode =
	new DefaultMutableTreeNode(experiment, false);
      model.insertNodeInto(newNode, parent, parent.getChildCount());
      experimentNames.add(name);
      experiment.addModificationListener(myModificationListener);
      workspace.setSelection(newNode);
      // can't run if experiment has unbound properties
      if (experiment.hasUnboundProperties()) {
        csmart.runExperimentBuilder(experiment, false);
        return;
      }
      if (!experiment.hasConfiguration())
        experiment.createDefaultConfiguration();
    } else if (o instanceof Experiment) {
      experiment = (Experiment) o;
    } else {
      return;
    }
    // Start up the console on the experiment
    csmart.runConsole(experiment);
  }
  
  ///////////////////////////////////
  // New Experiments
  //////////////////////////////////

  /**
   * Ensure that experiment name is unique in both CSMART and the database.
   * Optionally allow re-use of existing name.
   */

  public String getUniqueExperimentName(String originalName,
                                        boolean allowExistingName) {
    String name = null;
    while (true) {
      name = (String) JOptionPane.showInputDialog(this, "Enter Experiment Name",
                                                  "Experiment Name",
                                                  JOptionPane.QUESTION_MESSAGE,
                                                  null, null,
                                                  originalName);
      if (name == null) return null;
      if (name.equals(originalName) && allowExistingName)
        return name;
      // if name is unique in CSMART
      if (!experimentNames.contains(name)) {
        // ensure that name is not in the database
        boolean inDatabase = false;
        // if can't reach database, just assume that name is ok
        try {
          inDatabase = ExperimentDB.isExperimentNameInDatabase(name);
        } catch (RuntimeException e) {
          if(log.isErrorEnabled()) {
            log.error("RuntimeException", e);
          }
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

  public DefaultMutableTreeNode newExperiment() {
    String name = getUniqueExperimentName(experimentNames.generateName(),
                                          false);
    if (name == null) return null;

    try {
      Experiment experiment = new Experiment(name);
      DefaultMutableTreeNode newNode =
	addExperimentToWorkspace(experiment, getSelectedNode());
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
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
      return null;
    }
  }

  /**
   * Create a new experiment and make it be the selected experiment.
   * Allows tools that need an experiment to create one
   * if it does not exist when the tool is invoked.
   */
  public void addExperiment() {
    // make root be the selected node
    workspace.setSelectionPath(new TreePath(root.getPath())); 
    DefaultMutableTreeNode newNode = newExperiment();
  }

  private DefaultMutableTreeNode addExperimentToWorkspace(Experiment experiment) {
    return addExperimentToWorkspace(experiment, getSelectedNode());
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

  ///////////////////////////////////
  // New Societies
  //////////////////////////////////

  /**
   * Create a new built-in society.
   */

  public DefaultMutableTreeNode newSociety() {
    Object answer =
      JOptionPane.showInputDialog(this, "Select Society Type",
				  "Select Society",
				  JOptionPane.QUESTION_MESSAGE,
				  null,
                                  builtInSocieties,
				  "ScalabilityX");
    if (answer == null)
      return null;
    SocietyComponent sc = null;
    if (answer instanceof NameClassItem) {
      // create a scalability or abc society
      NameClassItem item = (NameClassItem) answer;
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
      sc = helper.createSociety(name, item.cls);
    }
    if (sc == null)
      return null;
    DefaultMutableTreeNode newNode = 
      addSocietyToWorkspace(sc, getSelectedNode());
    workspace.setSelection(newNode);
    return newNode;
  }
  /**
   * Create a new society and make it be the selected society.
   * Allows tools that need a society to create one,
   * if none exists when the tool is invoked.
   */
  public void addSociety() {
    // make root be the selected node
    workspace.setSelectionPath(new TreePath(root.getPath())); 
    DefaultMutableTreeNode newNode = newSociety();
  }
  
  private DefaultMutableTreeNode addSocietyToWorkspace(SocietyComponent sc,
						       DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode = 
      new DefaultMutableTreeNode(sc, false);
    addNode(node, newNode);
    societyNames.add(sc.getSocietyName());
    installListeners(sc);
    return newNode;
  }

  /////////////////////////////////
  // New Recipes
  /////////////////////////////////  

  public void newRecipe() {
    Object[] values = metNameClassItems;
    Object answer =
      JOptionPane.showInputDialog(this, "Select Recipe Type",
				  "Select Recipe",
				  JOptionPane.QUESTION_MESSAGE,
				  null,
				  values,
				  "Empty Recipe");
    if (answer instanceof NameClassItem) {
      NameClassItem item = (NameClassItem) answer;
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
      RecipeComponent recipe = helper.createRecipe(name, item.cls);
      if (recipe != null)
	workspace.setSelection(addRecipeToWorkspace(recipe));
    }
  } // end of newRecipe

  private DefaultMutableTreeNode addRecipeToWorkspace(RecipeComponent recipe) {
    return addRecipeToWorkspace(recipe, getSelectedNode());
  }

  private DefaultMutableTreeNode addRecipeToWorkspace(RecipeComponent recipe,
						      DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode = 
      new DefaultMutableTreeNode(recipe, false);
    addNode(node, newNode);
    recipeNames.add(recipe.getRecipeName());
    installListeners(recipe);
    return newNode;
  }
  
  public void newFolder() {
    String name = getUniqueName("New folder name",
                                "Folder Name Not Unique",
                                folderNames);
    if (name == null) return;
    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name, true);
    addNode(getSelectedNode(), newNode);
    workspace.setSelection(newNode);
  }
  
  /////////////////////////////////////
  // Rename items
  /////////////////////////////////////

  /**
   * Rename a folder, experiment, society or recipe.
   */

  public void rename() {
    DefaultMutableTreeNode node = getSelectedNode();
    if (node == null)
      return;
    Object o = node.getUserObject();
    if (o == null)
      return;
    if (node.isRoot())
      renameWorkspace();
    else if (o instanceof Experiment)
      renameExperiment();
    else if (o instanceof SocietyComponent)
      renameSociety();
    else if (o instanceof RecipeComponent)
      renameRecipe();
    else if (o instanceof String)
      renameFolder();
  }

  /**
   * Rename the top-level workspace.
   */

  public void renameWorkspace() {
    String name = JOptionPane.showInputDialog("New workspace name");
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

  private String getUniqueName(String prompt, String errorMsgTitle,
                               UniqueNameSet uniqueNames) {
    String name;
    while (true) {
      name = JOptionPane.showInputDialog(prompt);
      if (name == null || name.equals(""))
        return null;
      if (!uniqueNames.contains(name)) break;
      int result = JOptionPane.showConfirmDialog(this,
                                                 "Use an unique name",
                                                 errorMsgTitle,
                                                 JOptionPane.OK_CANCEL_OPTION,
                                                 JOptionPane.ERROR_MESSAGE);
      if (result != JOptionPane.OK_OPTION)
        return null;
    }
    return name;
  }

  /**
   * Rename an experiment.
   */

  public void renameExperiment() {
    DefaultMutableTreeNode node = getSelectedNode();
    final Experiment experiment = (Experiment) node.getUserObject();
    String originalName = experiment.getExperimentName();
    String newName = getUniqueExperimentName(originalName, true);
    if (newName == null)
      return;
    if (newName.equals(originalName))
      return;
    experimentNames.remove(originalName);
    experiment.setName(newName);
    GUIUtils.timeConsumingTaskStart(organizer);
    try {
      new Thread("SaveExperiment") {
          public void run() {
            experiment.saveToDb(saveToDbConflictHandler); // save under new name
            GUIUtils.timeConsumingTaskEnd(organizer);
          }
        }.start();
    } catch (RuntimeException re) {
      if(log.isErrorEnabled()) {
        log.error("Runtime exception saving experiment", re);
      }
      GUIUtils.timeConsumingTaskEnd(organizer);
    }
    experimentNames.add(newName);
    model.nodeChanged(node);
  }

  /**
   * Rename a society.
   */

  public void renameSociety() {
    String newName = getUniqueName("New society name",
                                   "Society Name Not Unique",
                                   societyNames);
    if (newName == null)
      return;
    DefaultMutableTreeNode node = getSelectedNode();
    SocietyComponent societyComponent = 
      (SocietyComponent) node.getUserObject();
    if (newName.equals(societyComponent.getSocietyName()))
      return;
    societyNames.remove(societyComponent.getSocietyName());
    societyNames.add(newName);
    societyComponent.setName(newName);
    model.nodeChanged(node);
  }
  
  /**
   * Rename a recipe.
   */

  public void renameRecipe() {
    String newName = getUniqueName("New recipe name",
                                   "Recipe Name not Unique",
                                   recipeNames);
    if (newName == null)
      return;
    DefaultMutableTreeNode node = getSelectedNode();
    final RecipeComponent recipe = (RecipeComponent) node.getUserObject();
    if (newName.equals(recipe.getRecipeName()))
      return;
    recipeNames.remove(recipe.getRecipeName());
    recipe.setName(newName);
    GUIUtils.timeConsumingTaskStart(organizer);
    try {
      new Thread("SaveRecipe") {
          public void run() {
            recipe.saveToDatabase();
            GUIUtils.timeConsumingTaskEnd(organizer);
          }
        }.start();
    } catch (RuntimeException re) {
      if(log.isErrorEnabled()) {
        log.error("Runtime exception saving recipe", re);
      }
      GUIUtils.timeConsumingTaskEnd(organizer);
    }
    recipeNames.add(newName);
    model.nodeChanged(node); // update the model...
  }

  /**
   * Rename a folder.
   */

  public void renameFolder() {
    String newName = getUniqueName("New folder name",
                                   "Folder Name Not Unique",
                                   folderNames);
    if (newName == null) return;
    DefaultMutableTreeNode node = getSelectedNode();
    if (node.getUserObject().equals(newName))
      return;
    node.setUserObject(newName);
    model.nodeChanged(node);
  }

  ///////////////////////////////
  // Save recipe
  ///////////////////////////////

  public void saveRecipe() {
    DefaultMutableTreeNode node = getSelectedNode();
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
      if(log.isErrorEnabled()) {
        log.error("Exception", sqle);
      }
      JOptionPane.showMessageDialog(this,
                                    "An exception occurred writing the recipe to the database",
                                    "Error Writing Database",
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  ///////////////////////////////////
  // Select items from database
  ///////////////////////////////////

  public void selectExperimentFromDatabase() {
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
    // if the CSMART workspace contains an experiment with this name,
    // then force the user to select a new unique name
    if (experimentNames.contains(experimentName)) {
      experimentName = 
        getUniqueExperimentName(experimentNames.generateName(experimentName),
                                false);
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
      cmtDialog = new CMTDialog(csmart, this, experimentName, experimentId);
    }
    if (cmtDialog.wasCancelled())
      return;

    GUIUtils.timeConsumingTaskStart(organizer);
    try {
      new Thread("SelectExperiment") {
        public void run() {
	  // a potentially long process
          if (cmtDialog.processResults()) {
	    final String trialId = cmtDialog.getTrialId();
	    if (trialId != null) {
	      Experiment experiment =
		helper.createExperiment(originalExperimentName,
					cmtDialog.getExperimentName(),
					cmtDialog.getExperimentId(), 
					trialId);
	      if (experiment != null) {
		DefaultMutableTreeNode newNode =
		  addExperimentToWorkspace(experiment);
		RecipeComponent[] recipes = experiment.getRecipes();
		for (int i = 0; i < recipes.length; i++)
		  if (!recipeNames.contains(recipes[i].getRecipeName()))
		    addRecipeToWorkspace(recipes[i]);
		workspace.setSelection(newNode);
	      }
	    }
	  }
          GUIUtils.timeConsumingTaskEnd(organizer);
        }
      }.start();
    } catch (RuntimeException re) {
      if(log.isErrorEnabled()) {
        log.error("Runtime exception creating experiment", re);
      }
      GUIUtils.timeConsumingTaskEnd(organizer);
    }
  }

  public void selectRecipeFromDatabase() {
    Map recipeNamesHT = helper.getRecipeNamesFromDatabase();
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
    RecipeComponent rc = helper.getDatabaseRecipe(recipeId, recipeName);
    if (rc != null)
      addRecipeToWorkspace(rc);
  }

  ////////////////////////////////////
  // Copy items
  ////////////////////////////////////
  
  /**
   * Copy an experiment, society or recipe.
   */

  public void duplicate() {
    DefaultMutableTreeNode node = getSelectedNode();
    if (node == null)
      return;
    Object o = node.getUserObject();
    if (o == null)
      return;
    if (o instanceof Experiment)
      copyExperiment((Experiment)o);
    else if (o instanceof SocietyComponent)
      copySociety((SocietyComponent)o);
    else if (o instanceof RecipeComponent)
      copyRecipe((RecipeComponent)o);
  }

  public Experiment copyExperiment(Experiment experiment) {
    String newName = generateExperimentName(experiment.getExperimentName());
    final Experiment experimentCopy = (Experiment)experiment.copy(newName);
    // save copy in database
    GUIUtils.timeConsumingTaskStart(organizer);
    try {
      new Thread("DuplicateExperiment") {
          public void run() {
            experimentCopy.saveToDb(saveToDbConflictHandler);
            GUIUtils.timeConsumingTaskEnd(organizer);
          }
        }.start();
    } catch (RuntimeException re) {
      if(log.isErrorEnabled()) {
        log.error("Runtime exception duplicating experiment", re);
      }
      GUIUtils.timeConsumingTaskEnd(organizer);
    }
    DefaultMutableTreeNode node = 
      (DefaultMutableTreeNode)findNode(experiment).getParent();
    // add copy as sibling of original
    workspace.setSelection(addExperimentToWorkspace(experimentCopy, node));
    return experimentCopy;
  }
  
  public SocietyComponent copySociety(SocietyComponent society) {
    String newName = generateSocietyName(society.getSocietyName());
    SocietyComponent societyCopy = (SocietyComponent)society.copy(newName);

    boolean builtIn = false;
    for (int i = 0; i < builtInSocieties.length; i++) 
      if (builtInSocieties[i].cls.isInstance(societyCopy)) {
        builtIn = true;
        break;
      }
    // add society to workspace only if it's builtin
    if (builtIn) {
      DefaultMutableTreeNode node = 
        (DefaultMutableTreeNode)findNode(society).getParent();
      workspace.setSelection(addSocietyToWorkspace(societyCopy, node));
    } else
      societyNames.add(newName);
    return societyCopy;
  }

  /**
   * Copy the recipe component; generate a new unique name for the copy;
   * add the copy to the Workspace as a sibling of the original.
   * @return RecipeComponent the copied recipe
   */

  public RecipeComponent copyRecipe(RecipeComponent recipe) {
    DefaultMutableTreeNode node = 
      (DefaultMutableTreeNode)findNode(recipe).getParent();
    String newName = generateRecipeName(recipe.getRecipeName());
    RecipeComponent recipeCopy = (RecipeComponent)recipe.copy(newName);
    workspace.setSelection(addRecipeToWorkspace(recipeCopy, node));
    return recipeCopy;
  }
  
  ////////////////////////////////////
  // Delete items
  ////////////////////////////////////

  public void delete() {
    DefaultMutableTreeNode node = getSelectedNode();
    if (node == null)
      return;
    Object o = node.getUserObject();
    if (o == null)
      return;
    if (o instanceof Experiment)
      deleteExperiment();
    else if (o instanceof SocietyComponent)
      deleteSociety();
    else if (o instanceof RecipeComponent)
      deleteRecipe();
  }

  public void deleteExperiment() {
    deleteExperimentInNode(getSelectedNode());
  }

  public void deleteExperimentInNode(DefaultMutableTreeNode node) {
    if (node == null) return;
    Experiment experiment = (Experiment)node.getUserObject();
    if (!experiment.isEditable()) {
      int result = 
        JOptionPane.showConfirmDialog(this,
                                      "Experiment " +
                                      experiment.getExperimentName() +
                                      " is not editable; delete anyway?",
                                      "Experiment Not Editable",
                                      JOptionPane.YES_NO_OPTION,
                                      JOptionPane.WARNING_MESSAGE);
      if (result != JOptionPane.YES_OPTION)
	return;
    }
    model.removeNodeFromParent(node);
    String experimentName = experiment.getExperimentName(); 
    experimentNames.remove(experimentName);
    // ask if experiment should be deleted from database
    int result = 
      JOptionPane.showConfirmDialog(this,
                                    "Delete experiment " +
                                    experiment.getExperimentName() + 
                                    " from database?",
                                    "Delete Experiment From Database",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE);
    if (result != JOptionPane.YES_OPTION)
      return;
    ExperimentDB.deleteExperiment(experiment.getExperimentID(),
                                  experimentName);
  }
  
  /**
   * Note that this simply deletes the society from the workspace;
   * if the society was included in an experiment, then the experiment
   * still retains the society.
   */
  public void deleteSociety() {
    DefaultMutableTreeNode node = getSelectedNode();
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

  public void deleteRecipe() {
    deleteRecipeInNode(getSelectedNode());
  }

  private void deleteRecipeInNode(DefaultMutableTreeNode node) {
    if (node == null) return;
    model.removeNodeFromParent(node);
    RecipeComponent rc = (RecipeComponent) node.getUserObject();
    try {
      PDbBase pdb = new PDbBase();
      try {
        int status = pdb.recipeExists(rc);
        if (status == PDbBase.RECIPE_STATUS_EXISTS || status == PDbBase.RECIPE_STATUS_DIFFERS) {
          int answer =
            JOptionPane.showConfirmDialog(this,
                                          "Delete recipe " +
                                          rc.getRecipeName() +
                                          " from database?",
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

  /**
   * Delete folder and recursively delete all its contents.
   */

  public void deleteFolder() {
    DefaultMutableTreeNode node = getSelectedNode();
    if (node == root) return;
    if (node == null) return;
    int reply = 
      JOptionPane.showConfirmDialog(this,
                                    "Delete folder and all its contents?",
                                    "Delete Folder",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE);
    if (reply != JOptionPane.OK_OPTION)
      return;

    // if this folder is empty, just delete it
    if (node.isLeaf()) {
      model.removeNodeFromParent(node);
      return;
    }

    // make a list of nodes to delete
    // because we can't delete nodes while traversing the tree
    ArrayList nodesToDelete = new ArrayList();
    Enumeration nodes = node.breadthFirstEnumeration();
    nodes.nextElement(); // skip the first node which is this node
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode childNode =
        (DefaultMutableTreeNode)nodes.nextElement();
      // if a node is a leaf, add it to the list to delete
      if (childNode.isLeaf())
        nodesToDelete.add(childNode);
    }
    // delete the leaf nodes and user objects
    for (int i = 0; i < nodesToDelete.size(); i++) {
      DefaultMutableTreeNode nodeToDelete =
        (DefaultMutableTreeNode)nodesToDelete.get(i);
      Object o = nodeToDelete.getUserObject();
      if (o instanceof Experiment)
        deleteExperimentInNode(nodeToDelete);
      else if (o instanceof RecipeComponent)
        deleteRecipeInNode(nodeToDelete);
      else if (o instanceof String) // an empty folder
        model.removeNodeFromParent(nodeToDelete); // just delete the tree node
      // TODO: delete societies
    }
    // now we're left with the experiments the user decided not to delete
    // and the folder(s) containing them, but we need to
    // recursively delete the folder(s) that are empty or contain only
    // empty folders
    if (node.isLeaf())
      model.removeNodeFromParent(node);
    else
      deleteEmptyFolders(node);
  }

  /**
   * Recursively delete empty folders or folders containing only
   * empty folders, starting with the given node.
   */

  private void deleteEmptyFolders(DefaultMutableTreeNode node) {
    ArrayList nodesToDelete = new ArrayList();
    Enumeration nodes = node.depthFirstEnumeration();
    nodes.nextElement(); // skip the first node which is the start node
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode childNode =
        (DefaultMutableTreeNode)nodes.nextElement();
      // if a node is a folder and a leaf, add it to the list to delete
      Object o = childNode.getUserObject();
      if (o instanceof String && childNode.isLeaf())
        nodesToDelete.add(childNode);
    }
    for (int i = 0; i < nodesToDelete.size(); i++)
      model.removeNodeFromParent((DefaultMutableTreeNode)nodesToDelete.get(i));
    // if folder is now empty, just delete it
    if (node.isLeaf()) {
      model.removeNodeFromParent(node);
      return;
    }
    // if we deleted any nodes, then scan the tree again
    if (nodesToDelete.size() != 0)
      deleteEmptyFolders(node);
  }

  /**
   * Delete experiment from database and from workspace if it's there.
   */

  public void deleteExperimentFromDatabase() {
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
    // if experiment is in workspace, 
    // select it and delete it from there as well
    // selecting the experiment first notifies listeners appropriately
    DefaultMutableTreeNode node = findNodeNamed(experimentName);
    if (node != null) {
      workspace.setSelection(node);
      model.removeNodeFromParent(node);
      experimentNames.remove(experimentName);
    }
  }

  public void deleteRecipeFromDatabase() {
    Map recipeNamesHT = helper.getRecipeNamesFromDatabase();
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
    panel.add(new JLabel("Delete Recipe:"));
    panel.add(cb);
    int result = 
      JOptionPane.showConfirmDialog(null, panel, "Delete Recipe",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION)
      return;
    String recipeName = (String)cb.getSelectedItem();
    try {
      helper.deleteRecipe(recipeName);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
                                    "An exception occurred deleting the recipe from the database",
                                    "Error Writing Database",
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  ////////////////////////////////
  // Utilities
  ///////////////////////////////

  public String generateExperimentName(String name) {
    return experimentNames.generateName(name);
  }
  
  private String generateSocietyName(String name) {
    return societyNames.generateName(name);
  }

  private String generateRecipeName(String name) {
    return recipeNames.generateName(name);
  }

  private void installListeners(ModifiableComponent component) {
    component.addPropertiesListener(myPropertiesListener);
    for (Iterator i = component.getPropertyNames(); i.hasNext(); ) {
      Property p = component.getProperty((CompositeName) i.next());
      PropertyEvent event = new PropertyEvent(p, PropertyEvent.PROPERTY_ADDED);
      myPropertiesListener.propertyAdded(event);
    }
    component.addModificationListener(myModificationListener);
  }

  private void addNode(DefaultMutableTreeNode node, DefaultMutableTreeNode newNode) {
    if (node == null)
      node = root;
    model.insertNodeInto(newNode, node, node.getChildCount());
    workspace.scrollPathToVisible(new TreePath(newNode.getPath()));
  }
  
  private DefaultMutableTreeNode findNode(Object userObject) {
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      if (node.isLeaf() &&
	  node.getUserObject().equals(userObject))
	return node;
    }
    return null;
  }

  private DefaultMutableTreeNode findNodeNamed(String s) {
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      if (node.isLeaf() && node.toString().equals(s))
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

  ////////////////////////////////
  // Title utilities
  ///////////////////////////////
  
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

  ///////////////////////////////////////////
  // workspace listeners
  ///////////////////////////////////////////  

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
  
  private TreeSelectionListener mySelectionListener =
    new TreeSelectionListener() {
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
  
  public void addTreeSelectionListener(TreeSelectionListener listener) {
    workspace.addTreeSelectionListener(listener);
  }
  
  public void addTreeModelListener(TreeModelListener listener) {
    model.addTreeModelListener(listener);
  }

  ///////////////////////////////////////
  // Restore (read from file) and save workspace.
  ///////////////////////////////////////

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
	    } else if (o instanceof ModifiableComponent) {
	      installListeners((ModifiableComponent) o);
	    }
	  } // end of for loop
//          } catch (ClassNotFoundException cnfe) {
//          } catch (InvalidClassException ice) {
//          } catch (StreamCorruptedException sce) {
//          } catch (OptionalDataException ode) {
//          } catch (IOException ioe) {
	} catch (Exception e) {
          if(log.isErrorEnabled()) {
            log.error("Organizer: can't read file: " + f + " got exception", e);
          }
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
      model = new DefaultTreeModel(root);
      //      model = createModel(this, root);
      Class[] noTypes = new Class[0];
      try {
	societyNames.init(getLeaves(SocietyComponent.class, root),
			  SocietyComponent.class.getMethod("getSocietyName", noTypes));
	experimentNames.init(getLeaves(Experiment.class, root),
			     Experiment.class.getMethod("getExperimentName", noTypes));
	recipeNames.init(getLeaves(RecipeComponent.class, root),
			 RecipeComponent.class.getMethod("getRecipeName", noTypes));
	return;
      } catch (NoSuchMethodException nsme) {
	if (log.isErrorEnabled()) {
	  log.error("Organizer error restoring workspace", nsme);
	}
      } catch (SecurityException se) {
	if (log.isErrorEnabled()) {
	  log.error("Organizer error restoring workspace", se);
	}
      }
    } catch (Exception ioe) {
      if (log.isErrorEnabled()) {
	log.error("Organizer error restoring workspace", ioe);
      }
      return;
    }
  } // end of restore

  // This is to support auto-saving
  
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
	    if (log.isErrorEnabled()) {
	      log.error("Organizer error finding experiment name", nsme);
	    }
          } catch (SecurityException se) {
	    if (log.isErrorEnabled()) {
	      log.error("Organizer error finding experiment name", se);
	    }
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
   * Force an update, which saves the current workspace.
   */
  public void save() {
    update();
  }
  
  private void save(String fileName) {
    if(log.isDebugEnabled()) {
      log.debug("Saving to: " + fileName);
    }
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
      if (log.isErrorEnabled()) {
	log.error("Organizer error saving workspace", ioe);
      }
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

  ////////////////////////////////////////////
  
  // Class for holding name/Class pairs in UIs
  private static class NameClassItem {
    public String name;
    public Class cls;
    public NameClassItem(String name, Class cls) {
      this.cls = cls;
      this.name = name;
    }
    public String toString() {
      return name;
    }
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

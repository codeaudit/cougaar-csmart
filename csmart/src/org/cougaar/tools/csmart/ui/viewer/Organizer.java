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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.cougaar.util.log.Logger;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.db.PopulateDb;
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
import org.cougaar.tools.csmart.recipe.RecipeList;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.file.SocietyFileComponent;
import org.cougaar.tools.csmart.society.ui.SocietyUIComponent;
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
  private Object lockObject = new Object();
  DefaultTreeModel model;
  private OrganizerTree workspace;
  private Organizer organizer;
  private CMTDialog cmtDialog;
  private ArrayList editingNodes = new ArrayList();
  private transient Logger log;

  private DBConflictHandler saveToDbConflictHandler =
    GUIUtils.createSaveToDbConflictHandler(this);
  
  // The stand-alone recipes that can be created in CSMART
  private Object[] metNameClassItems = null;

  // Define Unique Name sets
  private UniqueNameSet societyNames = new UniqueNameSet("Society");
  private UniqueNameSet experimentNames = new UniqueNameSet("Experiment");
  private UniqueNameSet recipeNames = new UniqueNameSet("Recipe");
  private UniqueNameSet folderNames = new UniqueNameSet("Folder");

  // define helper class
  private OrganizerHelper helper = new OrganizerHelper();
  
  /**
   * Construct a workspace, i.e. an user interface for a tree
   * of components (experiments, societies, recipes).
   * Reads the previous workspace from "Default Workspace.bin"
   * @param csmart The <code>CSMART</code> user interface containing this.
   */
  public Organizer(CSMART csmart) {
    this(csmart, null);
    organizer = this;
  }
  
  /**
   * Construct a workspace, i.e. an user interface for a tree
   * of components (experiments, societies, recipes).
   * If the workspace file name parameter is null,
   * reads the previous workspace from "Default Workspace.bin"
   * @param csmart The <code>CSMART</code> user interface containing this.
   * @param workspaceFileName file from which to read previous workspace
   */
  public Organizer(CSMART csmart, String workspaceFileName) {
    createLogger();
    
    initRecipes();

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
	  if (userObject instanceof ModifiableComponent) {
	    //return ((ModifiableComponent)userObject).isEditable();
	    // Disable edits, which just change the name, cause
	    // we would need to notice the name change
	    // and do the right thing...
	    return false;
	  } else
  	    return true; // renaming workspace or folder is always ok
	}
	return false;
      }
    };
    workspace.setCellEditor(myEditor);

    // if a node represents an experiment
    // and that experiment needs to be saved to the database, 
    // then draw the experiment in red
    // if a node represents a component in an experiment,
    // and that experiment is being edited,
    // then draw the node in gray
    DefaultTreeCellRenderer myRenderer = new DefaultTreeCellRenderer() {
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
          Component c = 
            super.getTreeCellRendererComponent(tree, value, sel,
                                               expanded, leaf, row, hasFocus);
          DefaultMutableTreeNode node = 
            (DefaultMutableTreeNode)value;
          Object o = node.getUserObject();
          if (o instanceof Experiment) {
            if (((Experiment)o).isModified()) {
              c.setForeground(Color.red);
            }
          } else if (editingNodes.contains(node)) {
            c.setForeground(Color.gray);
	  }
	  // mark a society or recipe modified
	  // by turning it red...
	  else if (o instanceof SocietyComponent) {
	    if (((SocietyComponent)o).isModified()) {
	      c.setForeground(Color.red);
	    }
	  } else if (o instanceof RecipeComponent) {
	    if (((RecipeComponent)o).isModified()) {
	      c.setForeground(Color.red);
	    }
	  }
          return c;
        }
      };
    workspace.setCellRenderer(myRenderer);
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

  private void initRecipes() {
    metNameClassItems = new Object[RecipeList.getRecipeCount()];
    for(int i=0; i < RecipeList.getRecipeCount(); i++) {
      metNameClassItems[i] = new NameClassItem(RecipeList.getRecipeName(i), 
                                            RecipeList.getRecipeClass(i));
    }
  }

  ///////////////////////////////////////  
  // Methods to get the user's selection
  ///////////////////////////////////////  

  protected DefaultMutableTreeNode getSelectedNode() {
    TreePath selPath = workspace.getSelectionPath();
    if (selPath == null) return null;
    return (DefaultMutableTreeNode) selPath.getLastPathComponent();
  }
  
  protected SocietyComponent[] getSelectedSocieties() {
    return (SocietyComponent[]) getSelectedLeaves(SocietyComponent.class);
  }
  
  protected RecipeComponent[] getSelectedRecipes() {
    return (RecipeComponent[]) getSelectedLeaves(RecipeComponent.class);
  }
    
  protected Experiment[] getSelectedExperiments() {
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
  
  protected Object getSelectedObject() {
    DefaultMutableTreeNode selNode = getSelectedNode();
    if (selNode == null) return null;
    return selNode.getUserObject();
  }

  ////////////////////////////////////  
  // Start tools; used to start tools from menus
  ////////////////////////////////////  

  protected void startBuilder() {
    csmart.runBuilder((ModifiableComponent) getSelectedNode().getUserObject(), 
                      false);
  }

  /**
   * When called from Organizer, the selected user object is an experiment.
   * When called from CSMART File-Build menu, the
   * selected user object is a recipe or society.
   */

  protected void startExperimentBuilder() {
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
      DefaultMutableTreeNode parentNode =
        (DefaultMutableTreeNode) node.getParent();
      addExperimentAndComponentsToWorkspace(experiment, parentNode);
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

  protected void startConsole() {
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
      DefaultMutableTreeNode parentNode = 
	(DefaultMutableTreeNode) node.getParent();
      addExperimentToWorkspace(experiment, parentNode);
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
   * Get a name for an experiment
   * that is unique in the workspace and in the database.
   * @param originalName the current name of the object
   * @param allowExistingName true to allow existing name (true if renaming)
   * @return String new unique name
   */
  protected String getUniqueExperimentName(String originalName,
                                        boolean allowExistingName) {
    return getUniqueName("Experiment", experimentNames, originalName,
                         allowExistingName, "isExperimentNameInDatabase");
  }

  /**
   * Create an experiment for a society read from a file.
   */

  protected void createExperimentFromFile() {
    // query user for INI file and create society
    SocietyComponent society = newSocietyComponent();
    if (society == null)
      return;
    Experiment experiment = 
      new Experiment("Experiment for " + society.getSocietyName(),
                     society, null);
    DefaultMutableTreeNode experimentNode =
      addExperimentToWorkspace(experiment, getSelectedNode());
    addSocietyToWorkspace(society, experimentNode);
  }

  /**
   * Create an experiment and a society that will be specified
   * from the user interface.
   */

  protected void createExperimentFromUI() {
    String name = getUniqueExperimentName("", false);
    if (name == null)
      return;
    SocietyComponent society = new SocietyUIComponent("Society for " + name);
    Experiment experiment = new Experiment(name, society, null);
    DefaultMutableTreeNode experimentNode =
      addExperimentToWorkspace(experiment, getSelectedNode());
    addSocietyToWorkspace(society, experimentNode);
  }

  ///////////////////////////////////
  // New Societies
  //////////////////////////////////

  /**
   * Create a new built-in society.
   */

  private SocietyComponent newSocietyComponent() {
    // display file chooser to allow user to select file that defines society
    JFileChooser chooser = 
      new JFileChooser(SocietyFinder.getInstance().getPath());
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    File file = null;
    SocietyComponent sc = null;
    while (file == null) {
      int result = chooser.showDialog(this, "OK");
      if (result != JFileChooser.APPROVE_OPTION)
	return null;
      file = chooser.getSelectedFile();
    }
    // create society from agent files or single node file
    String name = "";

    name = file.getName();
    if (name.endsWith(".ini"))
      name = name.substring(0, name.length()-4);
    
    // if name is not unique, then get an unique name for the society
    while (societyNames.contains(name)) {
      name = societyNames.generateName(name);
      name = 
	(String)JOptionPane.showInputDialog(this, "Enter Society Name",
					    "Society Name Not Unique",
					    JOptionPane.QUESTION_MESSAGE,
					    null, null, name);
    }
    if (name == null) return null; 

    if (file.isDirectory()) {
      String[] filenames =
        SocietyFinder.getInstance().getAgentFilenames(file);

      if (filenames == null || filenames.length == 0) {
	// Found no Agents
	if (log.isWarnEnabled()) {
	  log.warn("Found no agent in dir " + file.getPath());
	}
	return null;
      }

      sc = helper.createSociety(name, filenames, SocietyFileComponent.class);
    } else {
      sc = helper.createSociety(file.getPath(), name, SocietyFileComponent.class);
    }
    if (sc == null)
      return null;
    societyNames.add(name);
    sc.setName(name); 
    return sc;
  }

  protected void addSociety(SocietyComponent sc) {
    String name = sc.getSocietyName();
    // if name is not unique, then get an unique name for the society
    while (societyNames.contains(name)) {
      name = societyNames.generateName(name);
      name = 
        (String)JOptionPane.showInputDialog(this, "Enter Society Name",
                                            "Society Name Not Unique",
                                            JOptionPane.QUESTION_MESSAGE,
                                            null, null, name);
      if (name == null) return; // TODO: delete society to clean up
    }
    sc.setName(name); 
    // add society as sibling of experiment
    DefaultMutableTreeNode parentNode = null;
    DefaultMutableTreeNode node = getSelectedNode();
    if (node != null)
      parentNode = (DefaultMutableTreeNode)node.getParent();
    DefaultMutableTreeNode newNode = 
      addSocietyToWorkspace(sc, (DefaultMutableTreeNode)parentNode);
  }
  
  /////////////////////////////////
  // New Recipes
  /////////////////////////////////  

  /**
   * Ensure that recipe name is unique in both CSMART and the database.
   * Optionally allow re-use of existing name.
   */

  protected String getUniqueRecipeName(String originalName,
                                    boolean allowExistingName) {
    return getUniqueName("Recipe", recipeNames, originalName,
                         allowExistingName, "isRecipeNameInDatabase");
  }

  protected void newRecipe() {
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
	addRecipeToWorkspace(recipe, getSelectedNode());
    }
  } // end of newRecipe

  protected void newFolder() {
    String name = getUniqueFolderName("", false);
    if (name == null) return;
    addFolderToWorkspace(name, getSelectedNode());
  }

  /////////////////////////////////////
  // Rename items
  /////////////////////////////////////

  /**
   * Rename a folder, experiment, society or recipe.
   */

  protected void rename() {
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

  protected void renameWorkspace() {
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

  /**
   * Rename an experiment.
   */

  protected void renameExperiment() {
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
    if (experiment.isModified()) {
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
    } // end of block to see if expt was modified.
    experimentNames.add(newName);
    model.nodeChanged(node);
  }

  protected String getUniqueSocietyName(String originalName,
                                     boolean allowExistingName) {
    return getUniqueName("Society", societyNames, originalName,
                         allowExistingName, "isSocietyNameInDatabase");
  }
  /**
   * Rename a society.
   * If the society is in an experiment,
   * copy the society, rename it, and  save it to the database.
   * If it's not in an experiment, then warn the user that they
   * need to save any experiments that contain it.
   */
  protected void renameSociety() {
    DefaultMutableTreeNode node = getSelectedNode();
    DefaultMutableTreeNode parentNode = 
      (DefaultMutableTreeNode)node.getParent();
    int index = parentNode.getIndex(node);
    SocietyComponent society = (SocietyComponent) node.getUserObject();
    String originalName = society.getSocietyName();
    String newName = getUniqueSocietyName(originalName, true);
    if (newName == null)
      return;
    if (newName.equals(originalName))
      return;
    societyNames.remove(originalName);
    Object o = parentNode.getUserObject();
    if (o instanceof Experiment) {
      Experiment experiment = (Experiment)o;
      model.removeNodeFromParent(node);
      experiment.removeSocietyComponent();
      SocietyComponent societyCopy = (SocietyComponent)society.copyAndSave(newName);
      experiment.addSocietyComponent(societyCopy);

      addSocietyToWorkspace(societyCopy, parentNode);
    } else {
      societyNames.add(newName);
      // Make this method in SocietyBase do the DB update
      // so we dont have to do a separate save
      society.setName(newName);
      //society.saveToDatabase();
      Enumeration nodes = root.depthFirstEnumeration();
      while (nodes.hasMoreElements()) {
        node = (DefaultMutableTreeNode)nodes.nextElement();
        if (node.getUserObject().equals(society))
          model.nodeChanged(node);
      }
      displayExperiments(society);
    }
  }
  
  /**
   * Rename a recipe.
   * If the recipe is in an experiment,
   * copy the recipe, rename it, and  save it to the database.
   * If it's not in an experiment, then warn the user that they
   * need to save any experiments that contain it.
   */

  protected void renameRecipe() {
    DefaultMutableTreeNode node = getSelectedNode();
    DefaultMutableTreeNode parentNode = 
      (DefaultMutableTreeNode)node.getParent();
    int index = parentNode.getIndex(node);
    final RecipeComponent recipe = (RecipeComponent) node.getUserObject();
    String originalName = recipe.getRecipeName();
    String newName = getUniqueRecipeName(originalName, true);
    if (newName == null)
      return;
    if (newName.equals(originalName))
      return;
    recipeNames.remove(originalName);
    Object o = parentNode.getUserObject();
    if (o instanceof Experiment) {
      Experiment experiment = (Experiment)o;
      model.removeNodeFromParent(node);
      experiment.removeRecipeComponent(recipe);
      RecipeComponent recipeCopy = (RecipeComponent)recipe.copy(newName);
      experiment.addRecipeComponent(recipeCopy);
      recipeCopy.saveToDatabase();
      addRecipeToWorkspace(recipeCopy, parentNode);
    } else {
      recipeNames.add(newName);
      recipe.setName(newName);
      recipe.saveToDatabase();
      Enumeration nodes = root.depthFirstEnumeration();
      while (nodes.hasMoreElements()) {
        node = (DefaultMutableTreeNode)nodes.nextElement();
        if (node.getUserObject().equals(recipe))
          model.nodeChanged(node);
      }
      displayExperiments(recipe);
    }
  }

  /**
   * Rename a folder.
   */

  private String getUniqueFolderName(String originalName,
                                     boolean allowExistingName) {
    return getUniqueName("Folder", folderNames, originalName,
                         allowExistingName, null);
  }

  protected void renameFolder() {
    String originalName = (String)(getSelectedNode().getUserObject());
    String newName = getUniqueFolderName(originalName, true);
    if (newName == null) return;
    DefaultMutableTreeNode node = getSelectedNode();
    if (node.getUserObject().equals(newName))
      return;
    node.setUserObject(newName);
    model.nodeChanged(node);
    folderNames.remove(originalName);
    folderNames.add(newName);
  }

  ///////////////////////////////////
  // Select items from database
  ///////////////////////////////////

  protected void selectExperimentFromDatabase() {
    boolean haveCMTAssembly = false;
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

    // Does this experiment use a CMT assembly for configuration?
    // If so, use the CMTDialog
    // Otherwise, just do the load (which is now done through the CMTDialog)
    // Note: CMTDialog creates a CMTSociety -- should it be creating
    // a SocietyDBComponent?


    if(log.isDebugEnabled()) {
      log.debug("Experiment Id: " + experimentId);
    }

    // Does the experiment have a CMT configuration assembly
    if(DBUtils.containsCMTAssembly(experimentId)) {
      haveCMTAssembly = true;
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
    }
    if(haveCMTAssembly) {
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
                if (experiment != null)
                  addExperimentAndComponentsToWorkspace(experiment,
                                                        getSelectedNode());
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
    } else {
      final String trialId = ExperimentDB.getTrialId(experimentId);
      final String expName = experimentName;
      final String expId = experimentId;
      if (trialId != null) {
	GUIUtils.timeConsumingTaskStart(organizer);
	try {
	  new Thread("SelectExperiment") {
	    public void run() {
	      // a potentially long process
	      Experiment experiment =
		helper.createExperiment(originalExperimentName,
					expName,
					expId, 
					trialId);
	      if (experiment != null)
		addExperimentAndComponentsToWorkspace(experiment,
						      getSelectedNode());
	      GUIUtils.timeConsumingTaskEnd(organizer);
	    }
	  }.start();
	} catch (RuntimeException re) {
	  if(log.isErrorEnabled()) {
	    log.error("Runtime exception creating experiment", re);
	  }
	  GUIUtils.timeConsumingTaskEnd(organizer);
	}
      } // end of if block
    } // end of else block
  } // end of selectExpFromDB

  private void addExperimentAndComponentsToWorkspace(Experiment experiment,
                                       DefaultMutableTreeNode node) {
    DefaultMutableTreeNode expNode = 
      addExperimentToWorkspace(experiment, node);
    SocietyComponent societyComponent = experiment.getSocietyComponent();
    String societyName = societyComponent.getSocietyName();
    // add editable societies and recipes as siblings of experiment
    // only if these aren't already in the workspace
    if (!societyNames.contains(societyName)) {
      societyNames.add(societyName);
      addSocietyToWorkspace(societyComponent, node);
    }
    RecipeComponent[] recipes = experiment.getRecipeComponents();
    for (int i = 0; i < recipes.length; i++)
      if (!recipeNames.contains(recipes[i].getRecipeName()))
        addRecipeToWorkspace(recipes[i], node);
    // add non-editable societies and recipes
    // as children of experiment
    addSocietyToWorkspace(societyComponent, expNode);
    for (int i = 0; i < recipes.length; i++)
      addRecipeToWorkspace(recipes[i], expNode);
    // select new experiment in workspace
    workspace.setSelection(expNode);
  }

  protected void selectRecipeFromDatabase() {
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
      recipeName =
        getUniqueRecipeName(recipeNames.generateName(recipeName), false);
      if (recipeName == null)
        return;
    }
    RecipeComponent rc = helper.getDatabaseRecipe(recipeId, recipeName);
    if (rc != null)
      addRecipeToWorkspace(rc, getSelectedNode());
  }

  ////////////////////////////////////
  // Copy items
  ////////////////////////////////////
  
  /**
   * Copy an experiment, society or recipe.
   */

  protected void duplicate() {
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

  protected Experiment copyExperiment(Experiment experiment) {
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
    if (node == null) {
      if (log.isErrorEnabled()) {
        log.error("Experiment not found in workspace: " + 
                  experiment.getExperimentName());
      }
    }
    // add copy as sibling of original
    addExperimentAndComponentsToWorkspace(experimentCopy, node);
    return experimentCopy;
  }
  
  /**
   * Copy society; save the society to the database;
   * put the society in the workspace.
   * If the society is in an experiment, put the society in the workspace
   * as a sibling of the experiment, else put it in the workspace
   * as a sibling of the society.
   * @param society the society to copy
   * @return SocietyComponent the copied society
   */
  protected SocietyComponent copySociety(SocietyComponent society) {
    String newName = generateSocietyName(society.getSocietyName());
    final SocietyComponent societyCopy = 
      (SocietyComponent)society.copy(newName);
    // save society copy
    GUIUtils.timeConsumingTaskStart(organizer);
    try {
      new Thread("DuplicateSociety") {
        public void run() {
	    // Check the return value?
            societyCopy.saveToDatabase();
            GUIUtils.timeConsumingTaskEnd(organizer);
        }
      }.start();
    } catch (RuntimeException re) {
      if(log.isErrorEnabled()) {
        log.error("Runtime exception duplicating society", re);
      }
      GUIUtils.timeConsumingTaskEnd(organizer);
    }
    DefaultMutableTreeNode parentNode = 
      (DefaultMutableTreeNode)getSelectedNode().getParent();
    if (isSelectedNodeInExperiment()) 
      addSocietyToWorkspace(societyCopy, 
                            (DefaultMutableTreeNode)parentNode.getParent());
    else
      addSocietyToWorkspace(societyCopy, parentNode);
    return societyCopy;
  }

  /**
   * Copy the recipe component; generate a new unique name for the copy;
   * add the copy to the Workspace as a sibling of the original.
   * If the recipe is in an experiment, put the recipe in the workspace
   * as a sibling of the experiment, else put it in the workspace
   * as a sibling of the recipe.
   * @param society the society to copy
   * @return RecipeComponent the copied recipe
   */

  protected RecipeComponent copyRecipe(RecipeComponent recipe) {
    String newName = generateRecipeName(recipe.getRecipeName());
    final RecipeComponent recipeCopy = (RecipeComponent)recipe.copy(newName);
    GUIUtils.timeConsumingTaskStart(organizer);
    try {
      new Thread("DuplicateRecipe") {
        public void run() {
            recipeCopy.saveToDatabase();
            GUIUtils.timeConsumingTaskEnd(organizer);
        }
      }.start();
    } catch (RuntimeException re) {
      if(log.isErrorEnabled()) {
        log.error("Runtime exception duplicating recipe", re);
      }
      GUIUtils.timeConsumingTaskEnd(organizer);
    }
    DefaultMutableTreeNode parentNode = 
      (DefaultMutableTreeNode)getSelectedNode().getParent();
    if (isSelectedNodeInExperiment()) 
      addRecipeToWorkspace(recipeCopy, 
                           (DefaultMutableTreeNode)parentNode.getParent());
    else
      addRecipeToWorkspace(recipeCopy, parentNode);
    recipeNames.add(newName);
    return recipeCopy;
  }
  
  ////////////////////////////////////
  // Delete items
  ////////////////////////////////////

  protected void delete() {
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

  protected void deleteExperiment() {
    deleteExperimentInNode(getSelectedNode());
  }

  protected void deleteExperimentInNode(DefaultMutableTreeNode node) {
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
  protected void deleteSociety() {
    deleteSocietyInNode(getSelectedNode());
  }

  protected void deleteSocietyInNode(DefaultMutableTreeNode societyNode) {
    SocietyComponent society = (SocietyComponent)societyNode.getUserObject();
    if (!society.isEditable()) {
      int result = JOptionPane.showConfirmDialog(this,
						 "Society has been or is being used; delete anyway?",
						 "Society Not Editable",
						 JOptionPane.YES_NO_OPTION,
						 JOptionPane.WARNING_MESSAGE);
      if (result != JOptionPane.YES_OPTION)
	return;
    }
    model.removeNodeFromParent(societyNode);
//      int result =
//        JOptionPane.showConfirmDialog(this,
//                                      "Delete society " +
//                                      society.getSocietyName() +
//                                      " from all experiments?",
//                                      "Delete Society",
//                                      JOptionPane.YES_NO_OPTION,
//                                      JOptionPane.WARNING_MESSAGE);
//      if (result == JOptionPane.NO_OPTION) {
//        return; // just delete this node
//      }

//      // delete society from all experiments in the workspace
//      // and warn user to resave them
//      displayExperimentsInWorkspace(society);
//      ArrayList nodesToDelete = new ArrayList();
//      Enumeration nodes = root.depthFirstEnumeration(); 
//      while (nodes.hasMoreElements()) {
//        DefaultMutableTreeNode node = 
//          (DefaultMutableTreeNode)nodes.nextElement();
//        Object o = node.getUserObject();
//        if (o instanceof Experiment) {
//          Experiment exp = (Experiment)o;
//          if (exp.getSocietyComponent().equals(society))
//            exp.removeSocietyComponent();
//        } else if (society.equals(o)) {
//          nodesToDelete.add(node);
//        }
//      }
//      // remove nodes containing this society
//      for (int i = 0; i < nodesToDelete.size(); i++)
//        model.removeNodeFromParent((DefaultMutableTreeNode)nodesToDelete.get(i));

    // if deleted last reference to society in the workspace
    // ask if society should be deleted from database and delete it
    if (findNodeNamed(society.getSocietyName()) != null)
        return;
    int answer =
      JOptionPane.showConfirmDialog(this,
                                    "Delete society " +
                                    society.getSocietyName() +
                                    " from database?",
                                    "Delete Society",
                                    JOptionPane.YES_NO_OPTION);
    if (answer == JOptionPane.YES_OPTION) {
      try {
	// Will only actually do the delete
	// if no-one uses it
	PopulateDb.deleteSociety(society.getAssemblyId());
      } catch (Exception e) {
	if (log.isErrorEnabled()) {
	  log.error("Exception deleting society " + society.getSocietyName() + " from db", e);
	}
      }
    }
    societyNames.remove(society.getSocietyName());
  }

  protected void deleteRecipe() {
    deleteRecipeInNode(getSelectedNode());
  }
  
  /**
   * Ask user if recipe should be deleted everywhere;
   * if deleting everywhere, remove recipe from all experiments
   * in workspace and warn user to re-save them,
   * then ask user if recipe should be deleted from 
   * the database.
   */
  private void deleteRecipeInNode(DefaultMutableTreeNode recipeNode) {
    RecipeComponent recipe = (RecipeComponent) recipeNode.getUserObject();
    model.removeNodeFromParent(recipeNode);
//      int result =
//        JOptionPane.showConfirmDialog(this,
//                                      "Delete recipe " +
//                                      recipe.getRecipeName() +
//                                      " from all experiments?",
//                                      "Delete Recipe",
//                                      JOptionPane.YES_NO_OPTION,
//                                      JOptionPane.WARNING_MESSAGE);
//      if (result == JOptionPane.NO_OPTION) {
//        return; // just delete this node
//      }

//      // delete recipe from all experiments in the workspace
//      // and warn user to resave them
//      displayExperimentsInWorkspace(recipe);
//      ArrayList nodesToDelete = new ArrayList();
//      Enumeration nodes = root.depthFirstEnumeration(); 
//      while (nodes.hasMoreElements()) {
//        DefaultMutableTreeNode node = 
//          (DefaultMutableTreeNode)nodes.nextElement();
//        Object o = node.getUserObject();
//        if (o instanceof Experiment) {
//          Experiment exp = (Experiment)o;
//          RecipeComponent[] recipes = exp.getRecipeComponents();
//          for (int j = 0; j < recipes.length; j++) {
//            if (recipes[j].equals(recipe)) {
//              exp.removeRecipeComponent(recipe);
//              break;
//            }
//          }
//        } else if (recipe.equals(o)) {
//          nodesToDelete.add(node);
//        }
//      }
//      // remove nodes containing this recipe
//      for (int i = 0; i < nodesToDelete.size(); i++)
//        model.removeNodeFromParent((DefaultMutableTreeNode)nodesToDelete.get(i));

    // if deleted last reference to recipe in the workspace
    // ask if recipe should be deleted from database and delete it
    if (findNodeNamed(recipe.getRecipeName()) != null)
      return;
    try {
      PDbBase pdb = new PDbBase();
      try {
        int status = pdb.recipeExists(recipe);
        if (status == PDbBase.RECIPE_STATUS_EXISTS ||
            status == PDbBase.RECIPE_STATUS_DIFFERS) {
          int answer =
            JOptionPane.showConfirmDialog(this,
                                          "Delete recipe " +
                                          recipe.getRecipeName() +
                                          " from database?",
                                          "Delete Recipe",
                                          JOptionPane.YES_NO_OPTION);
          if (answer == JOptionPane.YES_OPTION) {
	    // Even if the user wanted it removed, only remove
	    // it if it is not in use, including any experiments
	    // currently in the workspace
	    if (! pdb.isRecipeUsed(recipe))
	      pdb.removeLibRecipe(recipe);
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
      if (log.isErrorEnabled()) {
	log.error("Error removing recipe from DB", e);
      }
    }
    recipeNames.remove(recipe.getRecipeName());
  }

  /**
   * Delete folder and recursively delete all its contents.
   */

  protected void deleteFolder() {
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
      nodesToDelete.add(childNode);
    }
    // delete the leaf nodes and user objects
    for (int i = 0; i < nodesToDelete.size(); i++) {
      DefaultMutableTreeNode nodeToDelete =
        (DefaultMutableTreeNode)nodesToDelete.get(i);
      Object o = nodeToDelete.getUserObject();
      if (o instanceof Experiment)
        deleteExperimentInNode(nodeToDelete);
      else if (o instanceof SocietyComponent)
        deleteSocietyInNode(nodeToDelete);
      else if (o instanceof RecipeComponent)
        deleteRecipeInNode(nodeToDelete);
      else if (o instanceof String) // an empty folder
        model.removeNodeFromParent(nodeToDelete); // just delete the tree node
    }
    // now we're left with the components the user decided not to delete
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
    // if original folder is now empty, just delete it
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
   * Don't allow deleting an experiment that's in the console,
   * because that would make it unrunnable.
   */

  protected void deleteExperimentFromDatabase() {
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
    if (findNodeNamed(experimentName) != null) {
      JOptionPane.showMessageDialog(this,
                                    "You cannot delete an experiment that is in the workspace; delete it from the workspace first.",
                                    "Cannot Delete Experiment",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }
//      if (CSMART.isExperimentInConsole(experimentName)) {
//        JOptionPane.showMessageDialog(this,
//                                      "You cannot delete an experiment that is in the Experiment Controller",
//                                      "Cannot Delete Experiment",
//                                      JOptionPane.ERROR_MESSAGE);
//        return;
    //    }
    String experimentId = (String)experimentNamesMap.get(experimentName);
    ExperimentDB.deleteExperiment(experimentId, experimentName);
    // if experiment is in workspace, then mark it as needing to be resaved
//      DefaultMutableTreeNode node = findNodeNamed(experimentName);
//      if (node != null) {
//        if (node.getUserObject() instanceof Experiment) {
//          Experiment experiment = (Experiment)node.getUserObject();
//          experiment.fireModification();
//        }
//      }
  }

  /**
   * Delete recipe from database and from workspace if it's there.
   */
  protected void deleteRecipeFromDatabase() {
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
    if (findNodeNamed(recipeName) != null) {
      JOptionPane.showMessageDialog(this,
                                    "You cannot delete a recipe that is in the workspace; delete it from the workspace first.",
                                    "Cannot Delete Recipe",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }
    try {
      helper.deleteRecipe(recipeName);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
                                    "An exception occurred deleting the recipe from the database",
                                    "Error Writing Database",
                                    JOptionPane.ERROR_MESSAGE);
    }
    // if recipe is in workspace, 
    // select it and delete it from there as well
    // selecting the recipe first notifies listeners appropriately
//      DefaultMutableTreeNode node = findNodeNamed(recipeName);
//      if (node != null) {
//        workspace.setSelection(node);
//        model.removeNodeFromParent(node);
//        recipeNames.remove(recipeName);
//      }
  }


  ////////////////////////////////
  // Replace Component in Experiment
  ///////////////////////////////

  public void replaceComponent(Experiment experiment,
                               ModifiableComponent component,
                               ModifiableComponent newComponent) {
    // find experiment node
    DefaultMutableTreeNode expNode = findNode(experiment);
    if (expNode == null) {
      if (log.isErrorEnabled()) {
        log.error("Experiment is not in tree: " + 
                  experiment.getExperimentName());
      }
      return;
    }
    // find component node in experiment
    DefaultMutableTreeNode componentNode = null;
    Enumeration nodes = expNode.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      if (node.getUserObject().equals(component)) {
        componentNode = node;
        break;
      }
    }
    if (componentNode == null) {
      if (log.isErrorEnabled()) {
        log.error("Component is not in experiment: " + 
                  component.getShortName());
      }
      return;
    }
    model.removeNodeFromParent(componentNode);
    if (component instanceof SocietyComponent) {
      societyNames.remove(component.getShortName());
      addSocietyToWorkspace((SocietyComponent)newComponent, expNode);
    } else if (component instanceof RecipeComponent) {
      recipeNames.remove(component.getShortName());
      addRecipeToWorkspace((RecipeComponent)newComponent, expNode);
    }
  }

  ////////////////////////////////
  // Add and remove nodes for children of an experiment
  ////////////////////////////////

  /**
   * Add tree nodes for the children (societies and recipes)
   * of an experiment.  First removes any current children nodes.
   * @param experiment the experiment for which to add children
   */
  public void addChildren(Experiment experiment) {
    DefaultMutableTreeNode expNode = findNode(experiment);
    // first remove old nodes
    int n = expNode.getChildCount();
    for (int i = 0; i < n; i++) {
      DefaultMutableTreeNode node = 
        (DefaultMutableTreeNode)expNode.getChildAt(0);
      model.removeNodeFromParent(node);
      editingNodes.remove(node);
    }
    SocietyComponent society = experiment.getSocietyComponent();
    if (society != null)
      addSocietyToWorkspace(society, expNode);
    RecipeComponent[] recipes = experiment.getRecipeComponents();
    for (int i = 0; i < recipes.length; i++)
      addRecipeToWorkspace(recipes[i], expNode);
    workspace.setSelection(expNode);
  }

  /**
   * Display tree nodes for the children (societies and recipes)
   * of an experiment in gray and don't let them be edited.
   * Used when editing the experiment.
   * @param experiment the experiment
   */
  protected void removeChildren(Experiment experiment) {
    DefaultMutableTreeNode expNode = findNode(experiment);
    int n = expNode.getChildCount();
    // display these nodes in gray and don't let them be edited
    for (int i = 0; i < n; i++) {
      DefaultMutableTreeNode node = 
        (DefaultMutableTreeNode)expNode.getChildAt(i);
      editingNodes.add(node);
      model.nodeChanged(node);
    }
  }

  protected boolean isNodeBeingEdited(DefaultMutableTreeNode node) {
    return editingNodes.contains(node);
  }

  ////////////////////////////////
  // Display names of experiments containing societies or recipes
  ///////////////////////////////

  /**
   * Display names of experiments in workspace or database
   * that contain the indicated society.
   */
  public void displayExperiments(SocietyComponent society) {
    Set experimentNames = 
      DBUtils.dbGetExperimentsWithSociety(society.getSocietyName());
    experimentNames.addAll(getExperimentNamesInWorkspace(society));
    if (experimentNames.size() == 0)
      return; // no experiments were affected
    displayExperimentList(experimentNames);
  }

  private void displayExperimentsInWorkspace(SocietyComponent society) {
    ArrayList experimentNames = getExperimentNamesInWorkspace(society);
    if (experimentNames.size() == 0)
      return; // no experiments were affected
    displayExperimentList(experimentNames);
  }

  /**
   * Display names of experiments in workspace or database
   * that contain the indicated recipe.
   * @param recipe the recipe component
   */
  public void displayExperiments(RecipeComponent recipe) {
    Set experimentNames = 
      DBUtils.dbGetExperimentsWithRecipe(recipe.getRecipeName());
    experimentNames.addAll(getExperimentNamesInWorkspace(recipe));
    if (experimentNames.size() == 0)
      return; // no experiments were affected
    displayExperimentList(experimentNames);
  }

  private void displayExperimentsInWorkspace(RecipeComponent recipe) {
    ArrayList experimentNames = getExperimentNamesInWorkspace(recipe);
    if (experimentNames.size() == 0)
      return; // no experiments were affected
    displayExperimentList(experimentNames);
  }    
  /**
   * Get experiments from workspace.
   */
  private ArrayList getExperiments() {
    ArrayList experiments = new ArrayList();
    Enumeration nodes = root.depthFirstEnumeration(); 
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
        (DefaultMutableTreeNode)nodes.nextElement();
      Object o = node.getUserObject();
      if (o instanceof Experiment) 
        experiments.add(o);
    }
    return experiments;
  }

  /**
   * Get names of experiments in workspace that contain the society.
   * Mark the experiments as modified.
   */
  private ArrayList getExperimentNamesInWorkspace(SocietyComponent society) {
    ArrayList results = new ArrayList();
    ArrayList experiments = getExperiments();
    for (int i = 0; i < experiments.size(); i++) {
      Experiment experiment = (Experiment)experiments.get(i);
      SocietyComponent sc = experiment.getSocietyComponent();
      if (sc != null && sc.equals(society))
        results.add(experiment.getExperimentName());
    }
    return results;
  }

  /**
   * Get names of experiments in workspace that contain the recipe.
   * Mark the experiments as modified.
   */
  private ArrayList getExperimentNamesInWorkspace(RecipeComponent recipe) {
    ArrayList results = new ArrayList();
    ArrayList experiments = getExperiments();
    for (int i = 0; i < experiments.size(); i++) {
      Experiment experiment = (Experiment)experiments.get(i);
      RecipeComponent[] recipes = experiment.getRecipeComponents();
      for (int j = 0; j < recipes.length; j++) {
        if (recipes[j].equals(recipe)) {
          results.add(experiment.getExperimentName());
          break;
        }
      }
    }
    return results;
  }

  /**
   * Display the names of experiments affected by a change
   * to their society or recipes.
   */
  private void displayExperimentList(Collection names) {
    Frame frame = null;
    try {
      frame = JOptionPane.getFrameForComponent(this);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Could not get frame for Organizer: " + e);
      }
    }
    final JDialog dialog = 
      new JDialog(frame, "Experiments That Must Be Saved", true);
    dialog.getContentPane().setLayout(new BorderLayout());
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    int x = 0;
    int y = 0;
    JTextArea msg = new JTextArea("The following experiments must be saved to the database in order to be updated:", 3, 100);
    msg.setLineWrap(true);
    msg.setWrapStyleWord(true);
    msg.setBackground(panel.getBackground());
    panel.add(msg,
              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.CENTER,
                                     GridBagConstraints.NONE,
                                     new Insets(10, 5, 5, 5),
                                     0, 0));
    Vector sortedNames = new Vector(names);
    Collections.sort(sortedNames);
    JList namesList = new JList(sortedNames);
    namesList.setBackground(panel.getBackground());
    JScrollPane jsp = new JScrollPane(namesList);
    jsp.setMinimumSize(new Dimension(100, 50));
    panel.add(jsp,
              new GridBagConstraints(x, y++, 1, 1, 1.0, 1.0,
                                     GridBagConstraints.CENTER,
                                     GridBagConstraints.BOTH,
                                     new Insets(0, 5, 0, 5),
                                     0, 0));
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dialog.dispose();
        }
      });
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(okButton);
    dialog.getContentPane().add(panel, BorderLayout.CENTER);
    dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    dialog.setSize(400, 300);
    dialog.setVisible(true);
  }


  ////////////////////////////////
  // Utilities
  ///////////////////////////////

  protected String generateExperimentName(String name) {
    return experimentNames.generateName(name);
  }
  
  protected String generateSocietyName(String name) {
    return societyNames.generateName(name);
  }

  protected String generateRecipeName(String name) {
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

  /**
   * The following methods:
   * create a new tree node for the component,
   * add the new node to the workspace as the last child of the specified node,
   * add the component name to the appropriate list of unique names,
   * add the listeners for the component, and
   * set the workspace selection to be the new node.
   */
  private DefaultMutableTreeNode addExperimentToWorkspace(Experiment experiment, 
                                                          DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode = 
      new DefaultMutableTreeNode(experiment, true);
    addNode(node, newNode);
    experimentNames.add(experiment.getExperimentName());
    workspace.setSelection(newNode);
    installListeners(experiment);
    return newNode;
  }

  private DefaultMutableTreeNode addSocietyToWorkspace(SocietyComponent sc,
						       DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode = 
      new DefaultMutableTreeNode(sc, false);
    addNode(node, newNode);
    societyNames.add(sc.getSocietyName());
    workspace.setSelection(newNode);
    installListeners(sc);
    return newNode;
  }

  private DefaultMutableTreeNode addRecipeToWorkspace(RecipeComponent recipe,
						      DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode = 
      new DefaultMutableTreeNode(recipe, false);
    addNode(node, newNode);
    recipeNames.add(recipe.getRecipeName());
    workspace.setSelection(newNode);
    installListeners(recipe);
    return newNode;
  }
  
  private DefaultMutableTreeNode addFolderToWorkspace(String name,
                                                      DefaultMutableTreeNode node) {
    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name, true);
    addNode(node, newNode);
    folderNames.add(name);
    workspace.setSelection(newNode);
    return newNode;
  }
  
  /**
   * Add newNode as last child of node.
   */
  private void addNode(DefaultMutableTreeNode node, DefaultMutableTreeNode newNode) {
    if (node == null)
      node = root;
    model.insertNodeInto(newNode, node, node.getChildCount());
    workspace.scrollPathToVisible(new TreePath(newNode.getPath()));
  }

  /**
   * Find a node containing the user object.
   * WARNING: societies and recipes may be in more than one node,
   * this just returns the first node found
   */
  private DefaultMutableTreeNode findNode(Object userObject) {
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      if (node.getUserObject().equals(userObject))
	return node;
    }
    return null;
  }

  private DefaultMutableTreeNode findNodeNamed(String s) {
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      if (node.toString().equals(s))
	return node;
    }
    return null;
  }

  /**
   * If the selected node is in an experiment, return true.
   */
  private boolean isSelectedNodeInExperiment() {
    DefaultMutableTreeNode parentNode = 
      (DefaultMutableTreeNode)getSelectedNode().getParent();
    if (parentNode.getUserObject() != null &&
        parentNode.getUserObject() instanceof Experiment)
      return true;
    return false;
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
   * Get a name for a folder, experiment, society, recipe, etc.
   * that is unique in the workspace and in the database.
   * The databaseCheck method is assumed to be the name of a method
   * in ExperimentDB that takes the component name as an argument
   * and returns a boolean indicating if that name is in the database.
   * @param prompt a single word describing the object to be named
   * @param names  the unique name set for this object
   * @param originalName the current name of the object
   * @param allowExistingName true to allow existing name (true if renaming)
   * @param databaseCheck method name to check for name in database or null
   * @return String new unique name
   */
  private String getUniqueName(String prompt,
                               UniqueNameSet names,
                               String originalName,
                               boolean allowExistingName,
                               String databaseCheck) {
    Method dbCheckMethod = null;
    String name = null;
    while (true) {
      // get new name from user
      String inputPrompt = "Enter " + prompt + " Name";
      String title = prompt + " Name";
      name = 
        (String) JOptionPane.showInputDialog(this, inputPrompt, title,
                                             JOptionPane.QUESTION_MESSAGE,
                                             null, null, originalName);
      if (name == null) return null;
      if (name.equals(originalName) && allowExistingName)
        return name;

      // if name is not unique in CSMART, tell user to try again
      if (names.contains(name)) {
        title = prompt + " Name Not Unique";
        int answer = 
          JOptionPane.showConfirmDialog(this,
                                        "Use an unique name",
                                        title,
                                        JOptionPane.OK_CANCEL_OPTION,
                                        JOptionPane.ERROR_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return null;
      } else {
        // if name is unique in CSMART and not checking database, return name
        if (databaseCheck == null)
          return name;
        // get the method to check for the name in the database
        if (dbCheckMethod == null) {
          Class[] paramClasses = { String.class };
          try {
            dbCheckMethod =
              ExperimentDB.class.getMethod(databaseCheck, paramClasses);
          } catch (NoSuchMethodException e) {
            if(log.isErrorEnabled()) {
              log.error("No such method", e);
            }
          }
          if (dbCheckMethod == null)
            return name; // can't check database, assume name is ok
        }
        // ensure that name is not in the database
        // if can't reach database, just assume that name is ok
        boolean inDatabase = false;
        Object[] args = new Object[1];
        args[0] = name;
        try {
          Object result = dbCheckMethod.invoke(null, args);
          if (result instanceof Boolean)
            inDatabase = ((Boolean)result).booleanValue();
          else
            if(log.isErrorEnabled()) {
              log.error("In Database Check returned unexpected result: " + 
                        inDatabase);
            }
        } catch (Exception e) {
          if(log.isErrorEnabled()) {
            log.error("Exception checking for unique name in database", e);
          }
        }
        if (inDatabase) {
          title = prompt + " Name Not Unique";
          int answer = 
            JOptionPane.showConfirmDialog(this,
                                          "This name is in the database; use an unique name",
                                          title,
                                          JOptionPane.OK_CANCEL_OPTION,
                                          JOptionPane.ERROR_MESSAGE);
          if (answer != JOptionPane.OK_OPTION) return null;
        } else
          return name; // have unique name
      } // end if unique in CSMART
    } // end while loop
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
  
  protected void addTreeSelectionListener(TreeSelectionListener listener) {
    workspace.addTreeSelectionListener(listener);
  }
  
  protected void addTreeModelListener(TreeModelListener listener) {
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
	    // Don't dump stack on this -- too verbose
            log.error("Organizer: can't read file: " + f + " got exception ", e);
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
        folderNames.init(getLeaves(String.class, root),
                         String.class.getMethod("toString", noTypes));
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
        DefaultMutableTreeNode changedNode = findNode(event.getSource());
        if (changedNode != null)
          model.nodeChanged(changedNode);
        if (event.getSource() instanceof Experiment) {
          //          model.nodeChanged(findNode(event.getSource()));
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
  
  protected boolean exitAllowed() {
    //    synchronized(root) {
    synchronized(lockObject) {
      if (updateNeeded) {
	nextUpdate = System.currentTimeMillis();
        //	root.notify();
        lockObject.notify();
	while (updateNeeded) {
	  try {
            lockObject.wait();
            //	    root.wait();
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
  protected void save() {
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
        //	synchronized (root) {
	synchronized (lockObject) {
	  while (true) {
	    try {
	      long now = System.currentTimeMillis();
	      if (updateNeeded && now > nextUpdate) {
		save(workspaceFileName);
		updateNeeded = false;
                //		root.notifyAll(); // In case anyone's waiting for the update to finish
                lockObject.notifyAll(); // In case anyone's waiting for the update to finish
	      } else if (updateNeeded) {
		long delay = nextUpdate - now;
		if (delay > 0) {
                  //		  root.wait(delay);
                  lockObject.wait(delay);
                            }
	      } else {
                //		root.wait();
                lockObject.wait();
	      }
	    } catch (InterruptedException ie) {
	    }
	  }
	}
      }
    };
  
  private void update() {
    //    synchronized (root) {
    synchronized (lockObject) {
      nextUpdate = System.currentTimeMillis() + UPDATE_DELAY;
      updateNeeded = true;
      //      root.notify();
      lockObject.notify();
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

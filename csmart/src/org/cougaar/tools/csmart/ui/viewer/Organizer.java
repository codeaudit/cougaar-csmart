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

import org.cougaar.tools.csmart.core.db.CommunityDbUtils;
import org.cougaar.tools.csmart.core.db.DBConflictHandler;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.core.property.ModificationListener;
import org.cougaar.tools.csmart.experiment.DBExperiment;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.recipe.RecipeList;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.ui.SocietyUIComponent;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * The Organizer holds all the component a user creates
 * and manipulates in CSMART
 * @property org.cougaar.tools.csmart.doWorkspace if false do NOT read / write from the
 * workspace file, nor set listeners to support doing so.
 */
public class Organizer extends JScrollPane {
  private static final String DEFAULT_FILE_NAME = "Default Workspace.xml";
  private static final String FRAME_TITLE = "CSMART Launcher";
  private static final long UPDATE_DELAY = 5000L;
  private boolean updateNeeded = false;
  private long nextUpdate = 0L;

  // If you set this give property to false, then no Workspace file is
  // read or written
  private static boolean doWorkspace = true;
  static {
    String s = System.getProperty("org.cougaar.tools.csmart.doWorkspace");
    if ("false".equalsIgnoreCase(s)) {
      doWorkspace = false;
    }
  }

  private String workspaceFileName;
  protected CSMART csmart;
  protected DefaultMutableTreeNode root;
  private Object lockObject = new Object();
  protected DefaultTreeModel model;
  protected OrganizerTree workspace;
  private Organizer organizer;
  private CMTDialog cmtDialog;
  private transient Logger log;

  private DBConflictHandler saveToDbConflictHandler =
    GUIUtils.createSaveToDbConflictHandler(this);

  // The stand-alone recipes that can be created in CSMART
  private Object[] recipeNameClassItems = null;

  // Define Unique Name sets
  private OrganizerNameSet experimentNames =
    new OrganizerNameSet("Experiment", "Experiment",
                         "isExperimentNameInDatabase");
  private OrganizerNameSet societyNames =
    new OrganizerNameSet("Society", "Society",
                         "isSocietyNameInDatabase");
  private OrganizerNameSet recipeNames =
    new OrganizerNameSet("Recipe", "Recipe",
			 "isRecipeNameInDatabase");
  private OrganizerNameSet folderNames =
    new OrganizerNameSet("Folder", "Folder", null);

  // define helper class
  protected OrganizerHelper helper;

  private ArrayList recipes = new ArrayList();

  // Define actions for use on menus

  protected Action newExperimentFromDBAction =
    new AbstractAction(ActionUtil.NEW_EXPERIMENT_FROM_DB_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.selectExperimentFromDatabase();
      }
    };

  protected Action newExperimentFromFileAction =
    new AbstractAction(ActionUtil.NEW_EXPERIMENT_FROM_FILE_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.createExperimentFromFile();
      }
    };

  protected Action newExperimentFromUIAction =
    new AbstractAction(ActionUtil.NEW_EXPERIMENT_FROM_UI_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.createExperimentFromUI();
      }
    };

  protected Action[] newExperimentActions = {
    newExperimentFromDBAction,
    newExperimentFromFileAction,
    newExperimentFromUIAction
  };

  protected Action newFolderAction =
    new AbstractAction(ActionUtil.NEW_FOLDER_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.newFolder();
      }
    };

  protected Action renameWorkspaceAction =
    new AbstractAction(ActionUtil.RENAME_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.renameWorkspace();
      }
    };

  protected AbstractAction deleteExperimentFromDatabaseAction =
    new AbstractAction(ActionUtil.DELETE_EXPERIMENT_FROM_DATABASE_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.deleteExperimentFromDatabase();
      }
    };

  protected AbstractAction deleteRecipeFromDatabaseAction =
    new AbstractAction(ActionUtil.DELETE_RECIPE_FROM_DATABASE_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.deleteRecipeFromDatabase();
      }
    };

  protected AbstractAction buildExperimentAction =
    new AbstractAction(ActionUtil.BUILD_ACTION,
		       new ImageIcon(getClass().getResource("Experiment16t.gif"))) {
      public void actionPerformed(ActionEvent e) {
	organizer.startExperimentBuilder();
      }
    };

  protected AbstractAction runExperimentAction =
    new AbstractAction(ActionUtil.RUN_ACTION,
		       new ImageIcon(getClass().getResource("EC16.gif"))) {
      public void actionPerformed(ActionEvent e) {
	organizer.startConsole();
      }
    };

  protected AbstractAction duplicateAction =
    new AbstractAction(ActionUtil.DUPLICATE_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.duplicate();
      }
    };

  protected AbstractAction deleteExperimentAction =
    new AbstractAction(ActionUtil.DELETE_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.deleteExperiment();
      }
    };

  protected AbstractAction deleteAction =
    new AbstractAction(ActionUtil.DELETE_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.delete();
      }
    };

  protected AbstractAction renameExperimentAction =
    new AbstractAction(ActionUtil.RENAME_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.renameExperiment();
      }
    };

  protected AbstractAction renameAction =
    new AbstractAction(ActionUtil.RENAME_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.rename();
      }
    };

  protected AbstractAction configureAction =
    new AbstractAction(ActionUtil.CONFIGURE_ACTION,
		       new ImageIcon(getClass().getResource("SB16.gif"))) {
      public void actionPerformed(ActionEvent e) {
	organizer.startBuilder();
      }
    };

  protected AbstractAction deleteRecipeAction =
    new AbstractAction(ActionUtil.DELETE_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.deleteRecipe();
      }
    };

  protected AbstractAction renameRecipeAction =
    new AbstractAction(ActionUtil.RENAME_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.renameRecipe();
      }
    };

  protected AbstractAction deleteFolderAction =
    new AbstractAction(ActionUtil.DELETE_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.deleteFolder();
      }
    };

  protected AbstractAction renameFolderAction =
    new AbstractAction(ActionUtil.RENAME_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.renameFolder();
      }
    };

  protected AbstractAction deleteSocietyAction =
    new AbstractAction(ActionUtil.DELETE_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.deleteSociety();
      }
    };

  protected AbstractAction renameSocietyAction =
    new AbstractAction(ActionUtil.RENAME_ACTION) {
      public void actionPerformed(ActionEvent e) {
	organizer.renameSociety();
      }
    };

  protected AbstractAction newRecipeFromDatabaseAction =
    new AbstractAction("From Database") {
      public void actionPerformed(ActionEvent e) {
	organizer.selectRecipeFromDatabase();
      }
    };

  protected AbstractAction newRecipeBuiltInAction =
    new AbstractAction("From Template") {
      public void actionPerformed(ActionEvent e) {
	organizer.newRecipe();
      }
    };

  protected Action[] newRecipeActions = {
    newRecipeFromDatabaseAction,
    newRecipeBuiltInAction
  };

  protected AbstractAction saveAction =
    new AbstractAction("Save") {
      public void actionPerformed(ActionEvent e) {
	organizer.saveInDatabase();
      }
    };

  /**
   * Construct a workspace, i.e. an user interface for a tree
   * of components (experiments, societies, recipes).
   * Reads the previous workspace from "Default Workspace.xml"
   * @param csmart The <code>CSMART</code> user interface containing this.
   */
  public Organizer(CSMART csmart) {
    this(csmart, null);
  }

  /**
   * Construct a workspace, i.e. an user interface for a tree
   * of components (experiments, societies, recipes).
   * If the workspace file name parameter is null,
   * reads the previous workspace from "Default Workspace.xml"
   * @param csmart The <code>CSMART</code> user interface containing this.
   * @param workspaceFileName file from which to read previous workspace
   */
  public Organizer(CSMART csmart, String workspaceFileName) {
    createLogger();
    organizer = this;
    helper = new OrganizerHelper(this);
    initRecipes();

    setPreferredSize(new Dimension(400, 400));
    JPanel panel = new JPanel(new BorderLayout());
    setViewportView(panel);
    this.csmart = csmart;
    if (workspaceFileName == null) {
      this.workspaceFileName = DEFAULT_FILE_NAME;
    } else {
      this.workspaceFileName = workspaceFileName;
    }

    restore(this.workspaceFileName);

    if (! doWorkspace) {
      if (log.isInfoEnabled()) {
	log.info("Not using workspace file");
      }
    }

    if (root == null) {
      root = new DefaultMutableTreeNode(null, true);
      model = new DefaultTreeModel(root);
    }
    model.setAsksAllowsChildren(true);

    // This listener is only used to call update
    if (doWorkspace)
      model.addTreeModelListener(myModelListener);

    // When restoring workspace file, this should have been done already
    if (workspace == null)
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
	      // don't allow edits which change the name,
	      // use Rename menu action instead
	      return false;
	    } else
	      return true; // renaming workspace or folder is always ok
	  }
	  return false;
	}
      };
    workspace.setCellEditor(myEditor);
    workspace.setCellRenderer(new OrganizerTreeCellRenderer(this));
    workspace.setExpandsSelectedPaths(true);
    workspace.addTreeSelectionListener(mySelectionListener);
    workspace.addAncestorListener(myAncestorListener);
    workspace.setSelection(root);
    workspace.addMouseListener(new OrganizerMouseListener(this, workspace));
    expandTree(); // fully expand workspace tree
    panel.add(workspace);
    setViewportView(panel);
    if (doWorkspace)
      updater.start();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  private void initRecipes() {
    recipeNameClassItems = new Object[RecipeList.getRecipeCount()];
    for(int i=0; i < RecipeList.getRecipeCount(); i++) {
      recipeNameClassItems[i] = new NameClassItem(RecipeList.getRecipeName(i),
						  RecipeList.getRecipeClass(i));
    }
  }

  /**
   * Get the society component with the given name from the workspace;
   * this ensures that there is exactly one object per component
   * in the workspace.
   */

  public SocietyComponent getSociety(String name) {
    return (SocietyComponent)societyNames.getObjectInTree(SocietyComponent.class, root, name);
  }

  public RecipeComponent getRecipe(String name) {
    return (RecipeComponent)recipeNames.getObjectInTree(RecipeComponent.class, root, name);
  }

  ///////////////////////////////////////
  // Methods to get the user's selection
  ///////////////////////////////////////

  protected DefaultMutableTreeNode getSelectedNode() {
    TreePath selPath = workspace.getSelectionPath();
    if (selPath == null) return null;
    return (DefaultMutableTreeNode) selPath.getLastPathComponent();
  }

  ////////////////////////////////////
  // Start tools; used to start tools from menus
  ////////////////////////////////////

  protected void startBuilder() {
    csmart.runBuilder((ModifiableComponent) getSelectedNode().getUserObject(),
		      false);
  }

  private DBExperiment createExperiment(SocietyComponent society) {
    String name = generateExperimentName("Experiment for " +
					 society.getSocietyName(), false);
    if (name == null)
      return null;
    return new DBExperiment(name,
			  society,
			  new RecipeComponent[0]);
  }

  private DBExperiment createExperiment(RecipeComponent recipe) {
    String name = generateExperimentName("Experiment for " +
					 recipe.getRecipeName(), false);
    if (name == null)
      return null;
    return new DBExperiment(name,
			  null,
			  new RecipeComponent[] {recipe});
  }

  /**
   * When called from Organizer, the selected user object is an experiment.
   * When called from CSMART File-Build menu, the
   * selected user object is a recipe or society.
   */
  protected void startExperimentBuilder() {
    DBExperiment experiment = null;
    DefaultMutableTreeNode node = getSelectedNode();
    Object o = node.getUserObject();
    if (o instanceof SocietyComponent) {
      experiment = createExperiment((SocietyComponent)o);
      if (experiment != null)
	addExperimentAndComponentsToWorkspace(experiment,
					      (DefaultMutableTreeNode) node.getParent());
    } else if (o instanceof RecipeComponent) {
      experiment = createExperiment((RecipeComponent)o);
      if (experiment != null)
	addExperimentAndComponentsToWorkspace(experiment,
					      (DefaultMutableTreeNode) node.getParent());
    } else if (o instanceof Experiment)
      experiment = (DBExperiment) o;
    if (experiment != null)
      csmart.runExperimentBuilder(experiment, false);
  }

  /**
   * Try to run the console.  This is called in three cases:
   * 1) the user has selected a runnable experiment
   * 2) the user has selected a society and this method creates an experiment
   * 3) the user has not selected an experiment and plans to attach to running nodes
   */
  protected void startConsole() {
    DefaultMutableTreeNode node = getSelectedNode();
    if (node == null) {
      csmart.runConsole(null);
      return;
    }
    Object o = node.getUserObject();
    DBExperiment experiment = null;
    if (o instanceof SocietyComponent) {
      experiment = createExperiment((SocietyComponent)o);
      if (experiment == null)
	return;
      addExperimentAndComponentsToWorkspace(experiment,
					    (DefaultMutableTreeNode) node.getParent());
      if (!experiment.hasConfiguration())
	experiment.createDefaultConfiguration();
      // TODO: this generates an error -- null trial id in PdbBase.put
      experiment.save(saveToDbConflictHandler);
    } else if (o instanceof Experiment) {
      experiment = (DBExperiment)o;
    }
    // Start up the console on the experiment or with no experiment
    csmart.runConsole(experiment);
  }


  ///////////////////////////////////
  // Unique name support
  //////////////////////////////////

  /**
   * Get a unique name, optionally allowing the existing name.
   * Prompts the user for a name based on the original name,
   * and then checks the user entered value for uniqueness in
   * the CSMART workspace and in the database.
   */

  public String getUniqueExperimentName(String originalName,
					boolean allowExistingName) {
    return experimentNames.getUniqueName(originalName, allowExistingName);
  }

  protected String getUniqueSocietyName(String originalName,
					boolean allowExistingName) {
    return societyNames.getUniqueName(originalName, allowExistingName);
  }

  protected String getUniqueRecipeName(String originalName,
				       boolean allowExistingName) {
    return recipeNames.getUniqueName(originalName, allowExistingName);
  }

  private String getUniqueFolderName(String originalName,
				     boolean allowExistingName) {
    return folderNames.getUniqueName(originalName, allowExistingName);
  }

  protected boolean isUniqueSocietyName(String name) {
    return societyNames.isUniqueName(name);
  }

  /**
   * Generate unique names.  Only prompts user if a generated
   * name conflicts in the database.
   */

  protected String generateExperimentName(String name,
					  boolean allowExistingName) {
    return experimentNames.generateUniqueName(name, allowExistingName);
  }

  protected String generateSocietyName(String name,
				       boolean allowExistingName) {
    return societyNames.generateUniqueName(name, allowExistingName);
  }

  protected String generateRecipeName(String name,
				      boolean allowExistingName) {
    return recipeNames.generateUniqueName(name, allowExistingName);
  }

  /**
   * Generate an unique name with no user input.
   */

  protected String generateExperimentName(String name) {
    return experimentNames.generateName(name);
  }

  protected String generateSocietyName(String name) {
    return societyNames.generateName(name);
  }

  ///////////////////////////////////
  // New Experiments
  //////////////////////////////////

  protected void createExperimentFromFile() {
    SocietyComponent society = helper.createSocietyFromFile();
    if (society == null)
      return;
    DBExperiment experiment = createExperiment(society);

    // Add in community info for this society
    JFileChooser chooser =
      new JFileChooser(System.getProperty("org.cougaar.install.path"));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setDialogTitle("Select the communities.xml file for this Society, if any");
    // Allow selection of XML files and directories
    chooser.addChoosableFileFilter(new FileFilter() {
	public boolean accept(File f) {
	  if (f == null)
	    return false;
	  if (f.isDirectory())
	    return true;
	  if (! f.canRead())
	    return false;
	  return f.getName().endsWith(".xml");
	}

	public String getDescription() {
	  return "XML Files";
	}

      });

    File xmlFile = null;
    while (xmlFile == null) {
      int result = chooser.showDialog(organizer, "OK");
      if (result != JFileChooser.APPROVE_OPTION)
	break;
      xmlFile = chooser.getSelectedFile();
      if (xmlFile != null) {
	if (! xmlFile.canRead())
	  xmlFile = null;
      }
    }

    if (xmlFile != null && !CommunityDbUtils.importCommunityXML(xmlFile, experiment.getCommAsbID())) {
      // There may have been none, so don't complain too loudly.
      if (log.isInfoEnabled()) {
	log.info("crtExpFromFile got no Community XML data out of " + xmlFile.getAbsolutePath());
      }
    } else if (log.isDebugEnabled()) {
      log.debug("crtExpFromFile had xmlFile: " + xmlFile + " and or return from CommDbUtils not false");
    }

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
    DBExperiment experiment = new DBExperiment(name, society, null);
    DefaultMutableTreeNode experimentNode =
      addExperimentToWorkspace(experiment, getSelectedNode());
    addSocietyToWorkspace(society, experimentNode);
  }

  ///////////////////////////////////
  // New Societies
  //////////////////////////////////

  protected void addSociety(SocietyComponent sc) {
    // add society as sibling of experiment
    DefaultMutableTreeNode parentNode =
      (DefaultMutableTreeNode)(getSelectedNode().getParent());
    addSocietyToWorkspace(sc, parentNode);
  }

  /////////////////////////////////
  // New Recipes
  /////////////////////////////////

  /**
   * Ensure that recipe name is unique in both CSMART and the database.
   * Optionally allow re-use of existing name.
   */
  protected void newRecipe() {
    Object[] values = recipeNameClassItems;
    Object answer =
      JOptionPane.showInputDialog(this, "Select Recipe Type",
				  "Select Recipe",
				  JOptionPane.QUESTION_MESSAGE,
				  null,
				  values,
				  "Empty Recipe");
    if (answer instanceof NameClassItem) {
      NameClassItem item = (NameClassItem) answer;
      //      String name = generateRecipeName(item.name, false);
      String name = recipeNames.getUniqueName(item.name, false);
      if (name == null)
	return;
      RecipeComponent recipe = helper.createRecipe(name, item.cls);
      if (recipe != null) {
	recipe.saveToDatabase();
	addRecipeToWorkspace(recipe, getSelectedNode());
      }
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
    if (doWorkspace)
      update();
  }

  /**
   * Rename an experiment.
   */

  protected void renameExperiment() {
    DefaultMutableTreeNode node = getSelectedNode();
    final DBExperiment experiment = (DBExperiment) node.getUserObject();
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
	    experiment.save(saveToDbConflictHandler); // save under new name
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
    //    int index = parentNode.getIndex(node);
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
    //    int index = parentNode.getIndex(node);
    final RecipeComponent recipe = (RecipeComponent) node.getUserObject();
    String originalName = recipe.getRecipeName();
    String newName = getUniqueRecipeName(originalName, true);
    if (newName == null)
      return;
    if (newName.equals(originalName))
      return;

    Object o = parentNode.getUserObject();

    // If its local, create a copy with the new name
    if (o instanceof Experiment) {
      Experiment experiment = (Experiment)o;
      model.removeNodeFromParent(node);
      experiment.removeRecipeComponent(recipe);
      RecipeComponent recipeCopy = (RecipeComponent)recipe.copy(newName);
      experiment.addRecipeComponent(recipeCopy);
      recipeCopy.saveToDatabase();
      addRecipeToWorkspace(recipeCopy, parentNode);

      // If there are no other Nodes in the model with the originalName
      // then and only then we want to remove that name from the list
      // of recipeNames
      boolean nameStillUsed = false;
      Enumeration nodes = root.depthFirstEnumeration();
      while (nodes.hasMoreElements()) {
	node = (DefaultMutableTreeNode)nodes.nextElement();
	if (node.getUserObject().equals(recipe)) {
	  // Original recipe still in the model someplace
	  // so don't remove the name
	  nameStillUsed = true;
	  break;
	}
      }
      if (! nameStillUsed)
	recipeNames.remove(originalName);
    } else {
      // Only really want to remove
      // the original name if there are no other nodes with this name
      // If its global (as in this block) we're going to change
      // the recipe name throughout so this is correct
      recipeNames.remove(originalName);

      recipeNames.add(newName);

      // This method now updates the DB as well
      recipe.setName(newName);

      // So only save it to the DB if necessary
      // Note however that
      // since the property names may have changed,
      // it may be I always want to re-save
      if (recipe.isModified())
	recipe.saveToDatabase();

      Enumeration nodes = root.depthFirstEnumeration();
      while (nodes.hasMoreElements()) {
	node = (DefaultMutableTreeNode)nodes.nextElement();
	if (node.getUserObject().equals(recipe))
	  model.nodeChanged(node);
      }

      // Now that changing a recipe name
      // doesn't change the recipe ID, there is no need to
      // resave all these experiments
      // when you change a recipe name
      //      displayExperiments(recipe);
    } // end of block on global change
  }

  /**
   * Rename a folder.
   */

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

  /**
   * Prompt the user for an experiment to load from the database.
   * Return an array with 2 string: experiment name and ID.
   * Note that the return is null if the user cancels the operation in any way.
   **/
  protected String[] selectExperimentFromDBToLoad() {
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
      return null;
    String experimentName = (String)cb.getSelectedItem();
    // if the CSMART workspace contains an experiment with this name,
    // then force the user to select a new unique name
    if (experimentNames.contains(experimentName)) {
      experimentName = generateExperimentName(experimentName, false);
      if (experimentName == null)
	return null;
    }

    String[] res = new String[2];
    res[0] = experimentName;
    res[1] = (String)experimentNamesMap.get(experimentName);
    return res;
  }

  /**
   * Prompt the user for an experiment to load, then create it from the database,
   * and put it in the workspace.
   **/
  protected void selectExperimentFromDatabase() {
    String[] res = selectExperimentFromDBToLoad();
    if (res == null)
      return;
    selectGivenExperimentFromDatabase(res[0], res[1]);
  }

  /**
   * Take the given experiment name and ID, and use them to try to load
   * an experiment from the database and add them to the workspace.
   **/
  protected void selectGivenExperimentFromDatabase(String experimentName, String experimentId) {
    selectGivenExperimentFromDatabase(experimentName, experimentId, true);
  }

  /**
   * Take the given experiment name and ID, and use them to try to load
   * an experiment from the database and add them to the workspace.
   * However, the final argument indicates whether to actually display
   * the CMTDialog window. If not, then whatever threads were already
   * selected will be used to load the experiment.
   **/
  protected void selectGivenExperimentFromDatabase(String experimentName, String experimentId, boolean withPrompt) {
    boolean haveCMTAssembly = false;
    final String originalExperimentName = experimentName;
    // Does this experiment use a CMT assembly for configuration?
    // If so, use the CMTDialog
    // Otherwise, just do the load (which is now done through the CMTDialog)
    // Note: CMTDialog creates a CMTSociety -- should it be creating
    // a SocietyDBComponent?


    if(log.isDebugEnabled()) {
      log.debug("selectExperimentFromDB: Experiment Id: " + experimentId + " name: " + originalExperimentName + " will now get name " + experimentName);
    }

    // Does the experiment have a CMT configuration assembly
    if(DBUtils.containsCMTAssembly(experimentId)) {
      haveCMTAssembly = true;
      // get threads and groups information
      cmtDialog = new CMTDialog(csmart, this, experimentName, experimentId, ! withPrompt);
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
      if (log.isDebugEnabled()) {
	log.debug("selectExptFromDB about to do cmtDialog.processResults");
      }
      GUIUtils.timeConsumingTaskStart(organizer);
      try {
	new Thread("SelectExperiment") {
	  public void run() {
	    // a potentially long process
	    if (cmtDialog.processResults()) {
	      final String trialId = cmtDialog.getTrialId();
	      if (log.isDebugEnabled()) {
		log.debug("selectExptFromDB after cmtDialog.processResults with new name/id: " + cmtDialog.getExperimentName() + "/" + cmtDialog.getExperimentId() + " trialID: " + trialId + " and orig name: " + originalExperimentName);
	      }
	      if (trialId != null) {
		DBExperiment experiment =
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
      if (log.isDebugEnabled()) {
	log.debug("selectExptFromDB got nonCMT expt to load. About to call helper to do load with origName: " + originalExperimentName + ", new name: " + expName + ", new ID: " + expId + ", and trialID: " + trialId);
      }
      if (trialId != null) {
	GUIUtils.timeConsumingTaskStart(organizer);
	try {
	  new Thread("SelectExperiment") {
	    public void run() {
	      // a potentially long process
	      DBExperiment experiment =
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

  // return true if component is in workspace
  // and is not the child of an experiment
  protected boolean isInWorkspace(ModifiableComponent mc) {
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node =
	(DefaultMutableTreeNode)nodes.nextElement();
      if (node.getUserObject().equals(mc)) {
        DefaultMutableTreeNode parentNode =
          (DefaultMutableTreeNode)node.getParent();
        Object parentObject = parentNode.getUserObject();
        if (parentObject != null && !(parentObject instanceof Experiment))
          return true;
      }
    }
    return false;
  }

  // TODO: determine how to handle naming of components that
  // are children of the experiment and also at the "top level"
  // these names are currently added twice and not deleted
  // when the experiment is deleted
  protected void addExperimentAndComponentsToWorkspace(DBExperiment experiment,
						       DefaultMutableTreeNode node) {
    DefaultMutableTreeNode expNode =
      addExperimentToWorkspace(experiment, node);
    SocietyComponent societyComponent = experiment.getSocietyComponent();

    if (!isInWorkspace(societyComponent))
      addSocietyToWorkspace(societyComponent, node);
    RecipeComponent[] recipes = experiment.getRecipeComponents();
    for (int i = 0; i < recipes.length; i++)
      if (!isInWorkspace(recipes[i]))
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
    Object[] selectedRecipes =
      Util.getObjectsFromList(this, new ArrayList(dbRecipeNames),
                              "Recipes", "Select Recipes");
    if (selectedRecipes == null)
      return;
    // where to put the new recipes
    DefaultMutableTreeNode parentNode = getSelectedNode();
    if (parentNode == null)
      parentNode = root;
    for (int i = 0; i < selectedRecipes.length; i++) {
      String recipeName = (String)selectedRecipes[i];
      String recipeId = (String) recipeNamesHT.get(recipeName);
      if (recipeNames.contains(recipeName))
        recipeName = recipeNames.getUniqueName(recipeName, false);
      if (recipeName == null)
        return;
      RecipeComponent rc = helper.getDatabaseRecipe(recipeId, recipeName);
      if (rc != null)
        addRecipeToWorkspace(rc, parentNode);
    }
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
      copyExperiment((Experiment)o, true);
    else if (o instanceof SocietyComponent)
      copySociety((SocietyComponent)o);
    else if (o instanceof RecipeComponent)
      copyRecipe((RecipeComponent)o);
  }

  protected Experiment copyExperiment(Experiment experiment,
                                      boolean save) {
    String newName =
      generateExperimentName(experiment.getExperimentName(), false);
    if (newName == null)
      return null;
    final DBExperiment experimentCopy = (DBExperiment)experiment.copy(newName);
    if (save) {
      // save copy in database
      GUIUtils.timeConsumingTaskStart(organizer);
      try {
        new Thread("DuplicateExperiment") {
	  public void run() {
	    experimentCopy.save(saveToDbConflictHandler);
	    GUIUtils.timeConsumingTaskEnd(organizer);
	  }
	}.start();
      } catch (RuntimeException re) {
        if(log.isErrorEnabled()) {
          log.error("Runtime exception duplicating experiment", re);
        }
        GUIUtils.timeConsumingTaskEnd(organizer);
      }
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
    String newName =
      generateSocietyName(society.getSocietyName(), false);
    if (newName == null)
      return null;
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
   * @param recipe the recipe to copy
   * @return RecipeComponent the copied recipe
   */
  protected RecipeComponent copyRecipe(RecipeComponent recipe) {
    String newName =
      generateRecipeName(recipe.getRecipeName(), false);
    if (newName == null)
      return null;
    final RecipeComponent recipeCopy = (RecipeComponent)recipe.copy(newName);

    // If we got an error copying the recipe, don't try to save it
    if (recipeCopy == null)
      return null;

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
    DBExperiment experiment = (DBExperiment)node.getUserObject();
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
    SocietyComponent society = experiment.getSocietyComponent();
    if (society != null)
      societyNames.remove(society.getSocietyName());
    int n = experiment.getRecipeComponentCount();
    for (int i = 0; i < n; i++)
      recipeNames.remove(experiment.getRecipeComponent(i).getRecipeName());
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
    model.removeNodeFromParent(societyNode);
    // if deleted last reference to society in the workspace
    // ask if society should be deleted from database and delete it
    if (findNodeNamed(society.getSocietyName()) != null)
      return;
    //    removeSocietyFromList(society);
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
	    // Bug 2032 -- not completely deleting complex recipes
 	    if (! pdb.isRecipeUsed(recipe))
	      helper.deleteRecipe(recipe.getRecipeName());
	    // 	      pdb.removeLibRecipe(recipe);
          }
        }
      } finally {
	if (pdb != null)
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
      folderNames.remove(node.getUserObject());
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
    if (node.isLeaf()) {
      folderNames.remove(node.getUserObject());
      model.removeNodeFromParent(node);
    } else
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
      folderNames.remove(node.getUserObject());
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
    String experimentId = (String)experimentNamesMap.get(experimentName);
    ExperimentDB.deleteExperiment(experimentId, experimentName);
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
  }


  ////////////////////////////////
  // Save Experiment or Society in Database
  ///////////////////////////////

  private void saveInDatabase() {
    DefaultMutableTreeNode node = getSelectedNode();
    if (node == null)
      return;
    Object o = node.getUserObject();
    if (o == null)
      return;
    if (o instanceof Experiment)
      saveExperiment((DBExperiment)o);
    else if (o instanceof SocietyComponent)
      saveSociety((SocietyComponent)o);
    else if (o instanceof RecipeComponent)
      saveRecipe((RecipeComponent)o);
  }

  private void saveExperiment(DBExperiment experiment) {
    final DBExperiment exp = experiment;
    GUIUtils.timeConsumingTaskStart(organizer);
    try {
      new Thread("Save") {
	public void run() {
	  exp.save(saveToDbConflictHandler);
	  GUIUtils.timeConsumingTaskEnd(organizer);
	}
      }.start();
    } catch (RuntimeException re) {
      if(log.isErrorEnabled()) {
        log.error("Error saving experiment: ", re);
      }
      GUIUtils.timeConsumingTaskEnd(organizer);
    }
  }

  private void saveSociety(SocietyComponent society) {
    final SocietyComponent sc = society;
    GUIUtils.timeConsumingTaskStart(organizer);
    try {
      new Thread("SaveSociety") {
	public void run() {
	  boolean success = sc.saveToDatabase();
	  GUIUtils.timeConsumingTaskEnd(organizer);
	  if (!success && organizer.log.isWarnEnabled()) {
	    organizer.log.warn("Failed to save society " +
			       sc.getSocietyName());
	  } else if (organizer.log.isDebugEnabled()) {
	    organizer.log.debug("Saved society " +
				sc.getSocietyName());
	  }
	}
      }.start();
    } catch (RuntimeException re) {
      if(log.isErrorEnabled()) {
        log.error("Runtime exception saving society", re);
      }
      GUIUtils.timeConsumingTaskEnd(organizer);
    }
    GUIUtils.timeConsumingTaskEnd(organizer);
  }

  private void saveRecipe(RecipeComponent recipe) {
    recipe.saveToDatabase();
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
      model.nodeChanged(node);
    }
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

  private void installListeners(ModifiableComponent component) {
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
  private DefaultMutableTreeNode addExperimentToWorkspace(DBExperiment experiment,
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

  protected DefaultMutableTreeNode addRecipeToWorkspace(RecipeComponent recipe,
							DefaultMutableTreeNode node) {
    if (recipe == null)
      return null;
    DefaultMutableTreeNode newNode =
      new DefaultMutableTreeNode(recipe, false);
    addNode(node, newNode);
    recipeNames.add(recipe.getRecipeName());
    workspace.setSelection(newNode);
    installListeners(recipe);
    return newNode;
  }

  protected DefaultMutableTreeNode addFolderToWorkspace(String name,
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
   * Returns true if the object is
   * a component (a society or recipe) in an experiment
   * that is being built or run.
   */
  protected boolean isComponentInUse(ModifiableComponent mc) {
    Enumeration nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node =
	(DefaultMutableTreeNode)nodes.nextElement();
      Object o = node.getUserObject();
      if (o instanceof Experiment) {
        Experiment exp = (Experiment)o;
        if (CSMART.isExperimentInConsole(exp) ||
            CSMART.isExperimentInEditor(exp))
          if (exp.getComponents().contains(mc))
            return true;
      }
    }
    return false;
  }

  /**
   * Returns true if the node is in an experiment node
   * and the experiment is being built or run.
   */
  protected boolean isNodeInUse(DefaultMutableTreeNode node) {
    DefaultMutableTreeNode parentNode =
      (DefaultMutableTreeNode)node.getParent();
    if (parentNode == null)
      return false;
    Object parentObject = parentNode.getUserObject();
    if (parentObject instanceof Experiment) {
      Experiment exp = (Experiment)parentObject;
      if (CSMART.isExperimentInConsole(exp) ||
          CSMART.isExperimentInEditor(exp))
        return true;
    }
    return false;
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
        if (doWorkspace)
          update();
      }
      public void treeNodesInserted(TreeModelEvent e) {
        if (doWorkspace)
          update();
      }
      public void treeNodesRemoved(TreeModelEvent e) {
        if (doWorkspace)
          update();
      }
      public void treeStructureChanged(TreeModelEvent e) {
        if (doWorkspace)
          update();
      }
    };

  /**
   * Called when selection in organizer tree is changed.
   */
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
    try {
      if (! fileName.endsWith(".xml"))
	fileName = fileName + ".xml";
      this.workspaceFileName = fileName;

      root = new DefaultMutableTreeNode();
      String label = workspaceFileName;
      label = label.substring(0, label.lastIndexOf('.'));
      root.setUserObject(label);

      // Must set up the model, workspace before actually doing the
      // load since the helper methods need this infrastructure
      model = new DefaultTreeModel(root);

      if (doWorkspace) {
	// set the selected node to be the root node
	workspace = new OrganizerTree(model);
	workspace.setSelection(root);
      }

      try {
        experimentNames.init(root, DBExperiment.class, "getExperimentName");
        societyNames.init(root, SocietyComponent.class, "getSocietyName");
        recipeNames.init(root, RecipeComponent.class, "getRecipeName");
        folderNames.init(root, String.class, "toString");
      } catch (SecurityException se) {
	if (log.isErrorEnabled()) {
	  log.error("Organizer error restoring workspace", se);
	}
      }

      if (doWorkspace) {
	OrganizerXML oxml = new OrganizerXML();
	oxml.populateWorkspace(fileName, this);
      } else {
	if (log.isInfoEnabled()) {
	  log.info("Not reading workspace file " + fileName);
	}
      }

    } catch (Exception ioe) {
      if (log.isErrorEnabled()) {
	log.error("Organizer error restoring workspace", ioe);
      }
      return;
    }
  } // end of restore

  /**
   * Call update if anything changed.
   */
  private transient ModificationListener myModificationListener = new ModificationListener() {
      public void modified(ModificationEvent event) {
        DefaultMutableTreeNode changedNode = findNode(event.getSource());
        if (changedNode != null) {
          model.nodeChanged(changedNode);
          // this could be an experiment saved, which would make it runnable
          // which should change which tools are enabled
          csmart.enableCSMARTTools();
        }
	if (doWorkspace)
	  update();
      }
    };

  protected boolean exitAllowed() {
    if (doWorkspace) {
      synchronized(lockObject) {
	if (updateNeeded) {
	  nextUpdate = System.currentTimeMillis();
	  lockObject.notify();
	  while (updateNeeded) {
	    try {
	      lockObject.wait();
	    } catch (InterruptedException ie) {
	    }
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
    if (doWorkspace)
      update();
  }

  private void save(String fileName) {
    if (! doWorkspace)
      return;

    if(log.isDebugEnabled()) {
      log.debug("Saving to: " + fileName);
    }

    if (!fileName.endsWith(".xml"))
      fileName = fileName + ".xml";

    try {
      // Write out the workspace as an XML file
      OrganizerXML oxml = new OrganizerXML();
      try {
	oxml.writeXMLFile(new File("."), oxml.createXMLDocument(root, csmart.getResultDir().getPath()), fileName);
      } catch (IOException ioe) {
	if(log.isErrorEnabled()) {
	  log.error("Caught an Exception trying to write Organizer XML File", ioe);
	}
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
	if (doWorkspace)
	  super.start();
      }
      public void run() {
	if (! doWorkspace)
	  return;
	synchronized (lockObject) {
	  while (true) {
	    try {
	      long now = System.currentTimeMillis();
	      if (updateNeeded && now > nextUpdate) {
		save(workspaceFileName);
		updateNeeded = false;
                lockObject.notifyAll(); // In case anyone's waiting for the update to finish
	      } else if (updateNeeded) {
		long delay = nextUpdate - now;
		if (delay > 0) {
                  lockObject.wait(delay);
                }
	      } else {
                lockObject.wait();
	      }
	    } catch (InterruptedException ie) {
	    }
	  }
	}
      }
    };

  private void update() {
    if (! doWorkspace)
      return;
    synchronized (lockObject) {
      nextUpdate = System.currentTimeMillis() + UPDATE_DELAY;
      updateNeeded = true;
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

} // end of Organizer class

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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JToolBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.cougaar.Version;
import org.cougaar.bootstrap.Bootstrapper;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.LoggerFactory;

// tools created by this user interface

import org.cougaar.tools.csmart.ui.analyzer.Analyzer;
import org.cougaar.tools.csmart.ui.configbuilder.PropertyBuilder;
import org.cougaar.tools.csmart.ui.console.CSMARTConsole;
import org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL;
import org.cougaar.tools.csmart.ui.experiment.ExperimentBuilder;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.cdata.SocietyCDataComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.monitor.generic.ExtensionFileFilter;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Util;

/**
 * Top level CSMART user interface.
 * Allows user to:
 * build, test, control, monitor and analyze
 * a society.
 */
public class CSMART extends JFrame implements ActionListener, Observer, TreeSelectionListener, TreeModelListener {
  private static Organizer organizer;
  private static JFileChooser workspaceFileChooser;
  private static ArrayList runningExperiments = new ArrayList();
  private static Hashtable titleToMenuItem = new Hashtable();
  private static JToolBar toolBar;
  private static JMenu windowMenu;
  private static CSMARTConsole console;
  private static File resultDir;
  // the entries in the file menu; conditionally enabled
  // based on selection in the workspace
  private static JMenu fileMenu;
  private static JMenu newRecipeMenu;
  private static JMenu newExperimentMenu;
  private static JMenuItem newSocietyMenuItem;
  private static JMenuItem newFolderMenuItem;
  private static JMenuItem configureMenuItem;
  private static JMenuItem buildMenuItem;
  private static JMenuItem runMenuItem;
  private static JMenuItem duplicateMenuItem;
  private static JMenuItem deleteMenuItem;
  private static JMenuItem deleteExperimentFromDatabaseMenuItem;
  private static JMenuItem deleteRecipeFromDatabaseMenuItem;
  private static JMenuItem renameMenuItem;
  private static JMenuItem saveToDatabaseMenuItem;

  // define strings here so we can easily change them
  private static final String FILE_MENU = "File";
  private static final String NEW_MENU_ITEM = "Open Workspace...";
  private static final String NEW_RESULTS_MENU_ITEM = "Save New Results In...";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String WINDOW_MENU = "Window";
  private static final String HELP_MENU = "Help";

  protected static final String HELP_DOC = "help.html";
  protected static final String VERSION_ITEM = "Show CSMART Version";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "About Launcher";

  private transient Logger log;

  private String[] helpMenuItems = {
    HELP_MENU_ITEM, VERSION_ITEM, ABOUT_CSMART_ITEM
  };
  
  private static final String PRE="<html><center><b><font face=\"sans-serif\">";
  private static final String POST="</font></b></center></html>";
  // tool names
  private static final String CONFIGURATION_BUILDER = "Configuration Builder";
  private static final String EXPERIMENT_BUILDER = "Experiment Builder";
  private static final String EXPERIMENT_CONTROLLER = "Experiment Controller";
  private static final String SOCIETY_MONITOR = "Society Monitor";
  private static final String PERFORMANCE_ANALYZER = "Performance Analyzer";
  private static final String[] views = {
    CONFIGURATION_BUILDER,
    EXPERIMENT_BUILDER,
    EXPERIMENT_CONTROLLER,
    SOCIETY_MONITOR,
    PERFORMANCE_ANALYZER
  };

  private static final String[] tooltips = {
    "Specify properties of a society or other configurable component.",
    "Configure an experiment.",
    "Start, stop and abort experiments.",
    "Monitor a running society.",
    "Analyze results of running an experiment."
  };

  private static final String[] iconFilenames = {
    "SB.gif",
    "EB.gif",
    "EC.gif",
    "SM.gif",
    "PA.gif"
  };

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

  /**
   * Constructor for top level class in CSMART.
   */
  public CSMART() {
    setTitle("CSMART");

    createLog();

    // Write initial CSMART info to the log file
    if (log.isShoutEnabled()) {
      log.shout(writeDebug());
    }

    resultDir = initResultDir();

    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    // set-up file menu which includes entries based on workspace selection
    fileMenu = new JMenu(FILE_MENU);
    fileMenu.setToolTipText("Create new workspace or quit.");
    fileMenu.addMenuListener(myMenuListener);
    JMenuItem newMenuItem = new JMenuItem(NEW_MENU_ITEM);
    newMenuItem.addActionListener(this);
    newMenuItem.setToolTipText("Create a new workspace.");
    fileMenu.add(newMenuItem);
    JMenuItem newResultsMenuItem = new JMenuItem(NEW_RESULTS_MENU_ITEM);
    newResultsMenuItem.addActionListener(this);
    newResultsMenuItem.setToolTipText("Select a directory for saving results");
    fileMenu.add(newResultsMenuItem);

    newExperimentMenu = new JMenu(ActionUtil.NEW_EXPERIMENT_ACTION);
    for (int i = 0; i < newExperimentActions.length; i++)
      newExperimentMenu.add(newExperimentActions[i]);
    fileMenu.add(newExperimentMenu);

    newRecipeMenu = new JMenu(ActionUtil.NEW_RECIPE_ACTION);
    for (int i = 0; i < newRecipeActions.length; i++)
      newRecipeMenu.add(newRecipeActions[i]);
    fileMenu.add(newRecipeMenu);

    newFolderMenuItem = new JMenuItem(ActionUtil.NEW_FOLDER_ACTION);
    newFolderMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          organizer.newFolder();
        }
      });
    fileMenu.add(newFolderMenuItem);
    fileMenu.addSeparator();
    configureMenuItem = new JMenuItem(ActionUtil.CONFIGURE_ACTION);
    configureMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          organizer.startBuilder();
        }
      });
    fileMenu.add(configureMenuItem);
    buildMenuItem = new JMenuItem(ActionUtil.BUILD_ACTION);
    buildMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          organizer.startExperimentBuilder();
        }
      });
    fileMenu.add(buildMenuItem);
    runMenuItem = new JMenuItem(ActionUtil.RUN_ACTION);
    runMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          organizer.startConsole();
        }
      });
    fileMenu.add(runMenuItem);
    duplicateMenuItem = new JMenuItem(ActionUtil.DUPLICATE_ACTION);
    duplicateMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          organizer.duplicate();
        }
      });
    fileMenu.add(duplicateMenuItem);
    deleteMenuItem = new JMenuItem(ActionUtil.DELETE_ACTION);
    deleteMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          organizer.delete();
        }
      });
    fileMenu.add(deleteMenuItem);
    deleteExperimentFromDatabaseMenuItem = 
      new JMenuItem(ActionUtil.DELETE_EXPERIMENT_FROM_DATABASE_ACTION);
    deleteExperimentFromDatabaseMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          organizer.deleteExperimentFromDatabase();
        }
      });
    fileMenu.add(deleteExperimentFromDatabaseMenuItem);
    deleteRecipeFromDatabaseMenuItem = 
      new JMenuItem(ActionUtil.DELETE_RECIPE_FROM_DATABASE_ACTION);
    deleteRecipeFromDatabaseMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          organizer.deleteRecipeFromDatabase();
        }
      });
    fileMenu.add(deleteRecipeFromDatabaseMenuItem);
    renameMenuItem = new JMenuItem(ActionUtil.RENAME_ACTION);
    renameMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          organizer.rename();
        }
      });
    fileMenu.add(renameMenuItem);
    fileMenu.addSeparator();
    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.addActionListener(this);
    exitMenuItem.setToolTipText("Exit");
    fileMenu.add(exitMenuItem);

    windowMenu = new JMenu(WINDOW_MENU);
    windowMenu.setToolTipText("Display selected window.");

    JMenu helpMenu = new JMenu(HELP_MENU);
    for (int i = 0; i < helpMenuItems.length; i++) {
      JMenuItem mItem = new JMenuItem(helpMenuItems[i]);
      mItem.addActionListener(this);
      helpMenu.add(mItem);
    }

    menuBar.add(fileMenu);
    menuBar.add(windowMenu);
    menuBar.add(helpMenu);

    toolBar = new JToolBar();
    toolBar.setLayout(new GridLayout(1, 5, 2, 2));
    getContentPane().add("North", toolBar);
    organizer = new Organizer(this);
    organizer.addTreeSelectionListener(this);
    organizer.addTreeModelListener(this);
    getContentPane().add("Center", organizer);

    for (int i = 0; i < views.length; i++) {
      JButton button = makeButton(views[i], iconFilenames[i]);
      button.setHorizontalTextPosition(JButton.CENTER);
      button.setVerticalTextPosition(JButton.BOTTOM);
      button.setActionCommand(views[i]);
      button.addActionListener(this);
      button.setToolTipText(tooltips[i]);
      toolBar.add(button);
    }
    enableCSMARTTools();

    // if user closes this window, quit
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	exit();
      }
    });

    NamedFrame.getNamedFrame().addObserver(this);

    pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int w = getWidth();
    int h = getHeight();
    setSize(w, h);
    setLocation((screenSize.width - w)/2, (screenSize.height - h)/2);
    setVisible(true);
  }

  /**
   * Get the <code>Organizer</code> object which manages the tree
   * of experiments, societies, recipes, etc.
   * @return the organizer
   */
  public static Organizer getOrganizer() {
    return organizer;
  }

  /**
   * Enable/disable entries in the File menu dependent on what
   * is selected in the organizer.
   */

  private MenuListener myMenuListener =
    new MenuListener() {
        public void menuCanceled(MenuEvent e) {
        }
        public void menuDeselected(MenuEvent e) {
        }
        public void menuSelected(MenuEvent e) {
          int n = fileMenu.getItemCount();
          // skip Open Workspace, Save New Results, and Exit 
          // which are always enabled
          for (int i = 2; i < n-1; i++) {
            JMenuItem menuItem = fileMenu.getItem(i);
            if (menuItem != null) // skip separators
              menuItem.setEnabled(ActionUtil.isActionAllowed(menuItem.getActionCommand(), organizer, false));
          }
        }
      }; // end of listener

  /**
   * Add an experiment to the list of running experiments.
   * @param experiment the experiment to add
   */
  public void addRunningExperiment(Experiment experiment) {
    runningExperiments.add(experiment);
  }

  /**
   * Remove an experiment from the list of running experiments.
   * @param experiment the experiment to remove
   */
  public void removeRunningExperiment(Experiment experiment) {
    runningExperiments.remove(experiment);
  }

  /**
   * Get the list of running experiments.
   * @return an array of <code>Experiment</code>
   */
  public static Experiment[] getRunningExperiments() {
    return (Experiment[])runningExperiments.toArray(new Experiment[runningExperiments.size()]);
  }

  private void enableConfigurationTool(boolean enable) {
    ((JButton)toolBar.getComponentAtIndex(0)).setEnabled(enable);
  }

  // When built-in societies are supported,
  // then the experiment tool should always be enabled,
  // because you could create a new experiment and add a society to it.
  // When built-in societies are not supported,
  // creating your own experiment does not make sense,
  // so the experiment tool is only enabled if an experiment is selected.

  private void enableExperimentTool(boolean enable) {
    ((JButton)toolBar.getComponentAtIndex(1)).setEnabled(enable);
  }

  private void enableConsoleTool(boolean enable) {
    ((JButton)toolBar.getComponentAtIndex(2)).setEnabled(enable);
  }

  /**
   * The selection in the workspace tree changed.
   * This is defined in the TreeSelectionListener interface.
   * Listen on organizer tree to determine what is selected
   * and enable appropriate tools.
   * @param e the tree selection event
   */

  public void valueChanged(TreeSelectionEvent e) {
    enableCSMARTTools();
  }

  /**
   * Tree nodes were inserted.
   * This is defined in the TreeModelListener interface.
   * Enable the appropriate tools when new nodes are inserted in the
   * workspace tree.
   * @param e event that describes the nodes inserted
   */
  public void treeNodesInserted(TreeModelEvent e) {
    enableCSMARTTools();
  }

  /**
   * Tree nodes were removed.
   * This is defined in the TreeModelListener interface.
   * Enable the appropriate tools when nodes are removed from the
   * workspace tree.
   * @param e event that describes the nodes removed
   */
  public void treeNodesRemoved(TreeModelEvent e) {
    enableCSMARTTools();
  }

  /**
   * Tree structure was changed.
   * This is defined in the TreeModelListener interface.
   * Enable the appropriate tools when the workspace tree structure
   * is changed.
   * @param e event that describes the change
   */
  public void treeStructureChanged(TreeModelEvent e) {
    enableCSMARTTools();
  }

  /**
   * Tree nodes were changed.
   * This is defined in the TreeModelListener interface.
   * Does nothing.
   * @param e event that describes the change
   */
  public void treeNodesChanged(TreeModelEvent e) {
    // don't care about these
  }

  /**
   * Called on initialization, when a selection changes in the Organizer,
   * when nodes are added or removed in the Organizer tree, or
   * when the configuration builder, experiment builder, or console
   * start or stop.
   * If an experiment is selected, then enable the 
   * experiment builder, unless it's already being edited.
   * If a runnable experiment is selected, then enable the console.
   * If a society or recipe is selected, then enable the 
   * configuration builder tool, if it's not already being edited.
   * Uses the action utilities to figure out what's allowed,
   * so that it matches the File menu and pop-up menus.
   */

  private void enableCSMARTTools() {
    enableConfigurationTool(ActionUtil.isActionAllowed(ActionUtil.CONFIGURE_ACTION, organizer, false));
    enableExperimentTool(ActionUtil.isActionAllowed(ActionUtil.BUILD_ACTION, organizer, false));
    enableConsoleTool(ActionUtil.isActionAllowed(ActionUtil.RUN_ACTION, organizer, false));
  }

  private void exit() {
    if (organizer.exitAllowed()) {
      if (console != null)
	console.stopExperiments();
      System.exit(0);
    }
  }

  private JButton makeButton(String label, String iconFilename) {
    // replace spaces in label with <br> so that labels fit on buttons
    int index = label.indexOf(' ');
    while (index != -1) {
      label = label.substring(0, index) + "<br>" +
        label.substring(index+1);
      index = label.indexOf(' ');
    }
    label = PRE + label + POST; // formatting
    URL iconURL = getClass().getResource(iconFilename);
    if (iconURL == null)
      return new JButton(label);
    ImageIcon icon = new ImageIcon(iconURL);
    return new JButton(label, icon);
  }

  /**
   * Window management for windows launched by CSMART.
   * Updates which tools are enabled and updates the Window menu.
   * @param o the <code>NamedFrame</code> that was added, removed or changed
   * @param arg an event describing the change
   */
  public void update(Observable o, Object arg) {
    if (o instanceof NamedFrame) {
      NamedFrame namedFrame = (NamedFrame) o;
      NamedFrame.Event event = (NamedFrame.Event) arg;
      if (event.eventType == NamedFrame.Event.ADDED) {
        JMenuItem menuItem = new JMenuItem(event.title);
        titleToMenuItem.put(event.title, menuItem);
        menuItem.addActionListener(this);
        windowMenu.add(menuItem);
        // update experiment Controls, as the experiment's runnability
        // may have changed
        if ((event.title.indexOf(CONFIGURATION_BUILDER) != -1) ||
            (event.title.indexOf(EXPERIMENT_BUILDER) != -1) ||
            (event.title.indexOf(EXPERIMENT_CONTROLLER) != -1))
          enableCSMARTTools();
      } else if (event.eventType == NamedFrame.Event.REMOVED) {
        JMenuItem menuItem = (JMenuItem) titleToMenuItem.get(event.title);
        if (menuItem == null) {
          if(log.isWarnEnabled()) {
            log.warn("CSMART: No window menu item for " + event.title);
          }
        } else {
          windowMenu.remove(menuItem);
          titleToMenuItem.remove(event.title);
        }
        // update experiment Controls, as the experiment's runnability
        // may have changed
        if ((event.title.indexOf(CONFIGURATION_BUILDER) != -1) ||
            (event.title.indexOf(EXPERIMENT_BUILDER) != -1) ||
            (event.title.indexOf(EXPERIMENT_CONTROLLER) != -1))
          enableCSMARTTools();
      } else if (event.eventType == NamedFrame.Event.CHANGED) {
	JMenuItem menuItem = (JMenuItem)titleToMenuItem.get(event.prevTitle);
        if (menuItem == null) {
          if(log.isWarnEnabled()) {
            log.warn("CSMART: No window menu item for " + event.title);
          }
        } else {
          windowMenu.remove(menuItem);
          titleToMenuItem.remove(event.prevTitle);
	  JMenuItem newMenuItem = new JMenuItem(event.title);
	  titleToMenuItem.put(event.title, newMenuItem);
	  newMenuItem.addActionListener(this);
	  windowMenu.add(newMenuItem);
	}
      }
    }
  }

  /**
   * Run configuration builder.
   * If this is called on an experiment, copy the experiment,
   * create a new society (SocietyCDataComponent), 
   * remove the society and any recipes from the experiment copy,
   * and put the new society in the experiment.
   * @param cc the component to configure
   * @param alwaysNew if true create a new configuration builder, otherwise re-use existing one
   */
  protected void runBuilder(ModifiableComponent cc, 
                         boolean alwaysNew) {
    // note that cc is guaranteed non-null when this is called
    ModifiableComponent originalComponent = null;
    Experiment experiment = null;
    if (cc instanceof Experiment) {
      experiment = (Experiment)cc;
      // copy the original experiment and put the copy in the workspace
      Experiment experimentCopy = organizer.copyExperiment(experiment);
      // remove all the components from the copy
      SocietyComponent sc = experimentCopy.getSocietyComponent();
      if (sc != null)
        experimentCopy.removeComponent(sc);
      RecipeComponent[] recipes = experimentCopy.getRecipeComponents();
      for (int i = 0; i < recipes.length; i++)
        experimentCopy.removeRecipeComponent(recipes[i]);
      // remove components from the workspace
      organizer.removeChildren(experimentCopy);
      // create a new society based on the society component data
      // in the original experiment
      ComponentData cdata = experiment.getSocietyComponentData();
      cdata.setName(cdata.getName() + " Society");
      cc = new SocietyCDataComponent(cdata,
         ((SocietyComponent)experiment.getSocietyComponent()).getAssemblyId());
      cc.initProperties();
      // Save this new society:
      ((SocietyComponent)cc).saveToDatabase();
      // put the new society in the copy of the experiment
      experimentCopy.addSocietyComponent((SocietyComponent)cc);
      // also put the new society in the workspace
      organizer.addSociety((SocietyComponent)cc);
      // and add the society to the workspace as a child of the experiment
      organizer.addChildren(experimentCopy);
      // edit the society within the newly created experiment
      experiment = experimentCopy;
    } else {
      // if configuring a component in an experiment, get the experiment
      DefaultMutableTreeNode selectedNode = organizer.getSelectedNode();
      if (selectedNode.getUserObject() != null &&
          selectedNode.getUserObject().equals(cc)) {
        DefaultMutableTreeNode parentNode = 
          (DefaultMutableTreeNode)selectedNode.getParent();
        if (parentNode.getUserObject() != null &&
            parentNode.getUserObject() instanceof Experiment) {
          experiment = (Experiment)parentNode.getUserObject();
          // copy the component, so it's modified only in the experiment
          if (cc instanceof SocietyComponent) {
            SocietyComponent society = (SocietyComponent)cc;
            String newName =
              organizer.generateSocietyName(society.getSocietyName());
            SocietyComponent societyCopy = (SocietyComponent)society.copy(newName);
            originalComponent = cc;
            cc = societyCopy;
//             if (societyCopy.isModified())
//               System.out.println("CSMART: society is modified");
          } else if (cc instanceof RecipeComponent) {
            RecipeComponent recipe = (RecipeComponent)cc;
            String newName =
              organizer.generateRecipeName(recipe.getRecipeName());
            RecipeComponent recipeCopy = (RecipeComponent)recipe.copy(newName);
            originalComponent = cc;
            cc = recipeCopy;
          }
        }
      }
    }
    JFrame tool = 
      (JFrame)new PropertyBuilder(this, cc, originalComponent, experiment);
    addTool(CONFIGURATION_BUILDER, cc.getShortName(), tool);
  }

  /**
   * Determine if an experiment is being edited in the ExperimentBuilder.
   * @param experiment the experiment
   * @return true if experiment is in the ExperimentBuilder
   */
  protected static boolean isExperimentInEditor(Experiment experiment) {
    String s = EXPERIMENT_BUILDER + ": " + experiment.getExperimentName();
    if (NamedFrame.getNamedFrame().getFrame(s) != null)
      return true;
    else
      return false;
  }
    
  /**
   * Determine if an experiment is in the Console.
   * @param String the experiment name
   * @return true if experiment is in the Console
   */
  protected static boolean isExperimentInConsole(Experiment experiment) {
    String s = EXPERIMENT_CONTROLLER + ": " + experiment.getExperimentName();
    if (NamedFrame.getNamedFrame().getFrame(s) != null)
      return true;
    else
      return false;
  }
    
  /**
   * Determine if a recipe is being edited in the ConfigurationBuilder.
   * @param recipe the recipe
   * @return true if recipe is in the ConfigurationBuilder
   */
  protected static boolean isRecipeInEditor(RecipeComponent recipe) {
    String s = CONFIGURATION_BUILDER + ": " + recipe.getRecipeName();
    if (NamedFrame.getNamedFrame().getFrame(s) != null)
      return true;
    else
      return false;
  }
    
  /**
   * Determine if a society is being edited in the ConfigurationBuilder.
   * @param society the society
   * @return true if society is in the ConfigurationBuilder
   */
  protected static boolean isSocietyInEditor(SocietyComponent society) {
    String s = CONFIGURATION_BUILDER + ": " + society.getShortName();
    if (NamedFrame.getNamedFrame().getFrame(s) != null)
      return true;
    else
      return false;
  }

  /**
   * Run the experiment builder to edit an experiment.
   * @param experiment the experiment to edit
   * @param alwaysNew true to create a new <code>ExperimentBuilder</code>; false to re-use an existing one
   */
  protected void runExperimentBuilder(Experiment experiment, 
                                   boolean alwaysNew) {
    // if this experiment is being edited, then don't edit again
    if (isExperimentInEditor(experiment))
      return;
    if (!experiment.isEditable()) {
      // otherwise editability can be overwritten
      Object[] options = { "Edit", "View", "Copy", "Cancel" };
      experiment = queryUser(experiment, options);
    }
    // if it's editable, then just edit it
    if (experiment != null)
      runExperimentBuilderWorker(experiment, alwaysNew);
  }

  private Experiment queryUser(Experiment experiment,
                               Object[] options) {
    int result = 
        JOptionPane.showOptionDialog(this,
                                     experiment.getShortName() + 
                                     " is not editable",
                                     "Experiment Not Editable",
                                     JOptionPane.DEFAULT_OPTION,
                                     JOptionPane.WARNING_MESSAGE,
                                     null,
                                     options,
                                     options[0]);
    if (options[result].equals("Edit"))
      // edit it anyway
      experiment.setEditable(true);
    else if (options[result].equals("Copy"))
      // copy it
      experiment = organizer.copyExperiment(experiment);
    else if (options[result].equals("Cancel"))
      // user cancelled
      return null;
    return experiment;
  }
   
  private void runExperimentBuilderWorker(Experiment experiment,
                                          boolean alwaysNew) {
    // gray out the children of the experiment in the tree
    // to avoid confusion while editing the experiment
    organizer.removeChildren(experiment);
    JFrame tool =
      (JFrame)new ExperimentBuilder(this, experiment);
    addTool(EXPERIMENT_BUILDER, experiment.getExperimentName(), tool);
  }

  /**
   * Run the specified experiment.  The experiment must be runnable.
   * @param experiment the experiment to run
   */
  protected void runConsole(Experiment experiment) {
    // TODO: we get here if the user edits an experiment containing
    // societies and removes all the societies, and then invokes the console
    if (experiment.getSocietyComponentCount() == 0) {
      // don't run console and disable it's button
      enableConsoleTool(false);
      return;
    }
    // TODO: we get here if the user is editing an experiment
    // the isRunnable flag is off, but we don't detect it
    if (!experiment.isRunnable()) {
      if (log.isWarnEnabled())
        log.warn("CSMART: WARNING: experiment is not runnable");
      enableConsoleTool(false);
      return;
    }
    JFrame tool =
      (JFrame)new CSMARTConsole(this, experiment);
    addTool(EXPERIMENT_CONTROLLER, experiment.getExperimentName(), tool);
  }

  /**
   * Run the Society Monitor tool.
   */
  protected void runMonitor() {
    Experiment runningExperiment = null;
    String name = "";
    if (runningExperiments.size() > 0) {
      runningExperiment = (Experiment)runningExperiments.get(0);
      name = runningExperiment.getExperimentName();
    }
    JFrame tool = (JFrame)new CSMARTUL(this, runningExperiment);
    addTool(SOCIETY_MONITOR, name, tool);
  }

  /**
   * Run the Performance Analyzer tool.
   * @param experiment the experiment to analyze
   */
  protected void runAnalyzer(Experiment experiment) {
    JFrame tool = (JFrame)new Analyzer(this, experiment);
    addTool(PERFORMANCE_ANALYZER, experiment.getExperimentName(), tool);
  }

  private void noSocietySelected() {
    JOptionPane.showMessageDialog(this, "Select a society first",
                                  "No Society Selected", JOptionPane.ERROR_MESSAGE);
  }

  private void noComponentSelected() {
    JOptionPane.showMessageDialog(this, "Select a component first",
                                  "No Component Selected", JOptionPane.ERROR_MESSAGE);
  }

  private void noExperimentSelected() {
    JOptionPane.showMessageDialog(this, "Select an experiment first",
                                  "No Experiment Selected", JOptionPane.ERROR_MESSAGE);
  }

  private void newWorkspace() {
    if (workspaceFileChooser == null) {
      workspaceFileChooser = 
	new JFileChooser(System.getProperty("org.cougaar.install.path"));
      String[] filters = { "bin" };
      ExtensionFileFilter filter = 
	new ExtensionFileFilter(filters, "workspace file");
      workspaceFileChooser.addChoosableFileFilter(filter);
    }
    if (workspaceFileChooser.showOpenDialog(this) == 
	JFileChooser.CANCEL_OPTION)
      return;
    File file = workspaceFileChooser.getSelectedFile();
    if (file == null)
      return;
    organizer.exitAllowed();
    getContentPane().remove(organizer);
    organizer = new Organizer(this, file.getPath());
    getContentPane().add("Center", organizer);
    validate();
  }

  private String getResultDirName() {
    String resultDirName = ".";
    try {
      resultDirName = System.getProperty("org.cougaar.install.path");
    } catch (RuntimeException e) {
      // just use current directory
      resultDirName = ".";
    }
    return resultDirName;
  }

  private File initResultDir() {
    return new File(getResultDirName() + File.separatorChar + "results");
  }

  /**
   * Set the directory in which to store the metrics file.
   * Displays a file chooser, initted
   * to the cougaar install path, for the user to choose a directory.
   */

  private void setResultDir() {
    JFileChooser chooser = new JFileChooser(getResultDirName());
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
	public boolean accept (File f) {
	  return f.isDirectory();
	}
	public String getDescription() {return "All Directories";}
      });
    int result = chooser.showDialog(this, "Select Results Directory");
    if (result == JFileChooser.APPROVE_OPTION)
      resultDir = chooser.getSelectedFile();
  }

  /**
   * Return directory in which to store results.
   * @return the directory
   */
  public File getResultDir() {
    return resultDir;
  }

  /**
   * ActionListener interface.
   * @param e an event describing the action
   */
  public void actionPerformed(ActionEvent e) {
    String s = ((AbstractButton)e.getSource()).getActionCommand();
    if (s.equals(NEW_MENU_ITEM)) {
      newWorkspace();
    } else if (s.equals(NEW_RESULTS_MENU_ITEM)) {
      setResultDir();
    } else if (s.equals(EXIT_MENU_ITEM)) {
      exit();
    } else if (s.equals(HELP_MENU_ITEM)) {
      URL help = (URL)getClass().getResource(HELP_DOC);
      if (help != null)
	Browser.setPage(help);
    } else if (s.equals(VERSION_ITEM)) {
      String txt = writeDebug();
      if (txt != null)
	Browser.setPage(txt);
    } else if (s.equals(ABOUT_CSMART_ITEM)) {
      URL about = (URL)getClass().getResource(ABOUT_DOC);
      if (about != null)
	Browser.setPage(about);
    } else if (s.indexOf(CONFIGURATION_BUILDER) != -1) {
      Object userObject = organizer.getSelectedObject();
      if (userObject instanceof Experiment ||
          userObject instanceof SocietyComponent ||
          userObject instanceof RecipeComponent)
        runBuilder((ModifiableComponent)userObject, false);
    } else if (s.indexOf(EXPERIMENT_BUILDER) != -1) {
      Object userObject = organizer.getSelectedObject();
      if (userObject instanceof Experiment)
        runExperimentBuilder((Experiment)userObject, false);
    } else if (s.indexOf(EXPERIMENT_CONTROLLER) != -1) {
      Object userObject = organizer.getSelectedObject();
      if (userObject instanceof Experiment &&
          ((Experiment)userObject).isRunnable())
        runConsole((Experiment)userObject);
    } else if (s.indexOf(SOCIETY_MONITOR) != -1) {
      // only run one society monitor
      // if user selects "Society Monitor" and its running
      // bring the frame to the front
      JFrame societyMonitorFrame = 
        NamedFrame.getNamedFrame().getToolFrame(SOCIETY_MONITOR);
      if (societyMonitorFrame == null)
        runMonitor();
      else
        societyMonitorFrame.toFront();
    } else if (s.indexOf(PERFORMANCE_ANALYZER) != -1) {
      Object userObject = organizer.getSelectedObject();
      if (userObject instanceof Experiment)
        runAnalyzer((Experiment)userObject);
    } else { // a frame selected from the window menu
      JFrame f = NamedFrame.getNamedFrame().getFrame(s);
      if (f != null) {
	f.toFront();
	f.setState(Frame.NORMAL);
      }
    }
  }

  /**
   * Add the tool to the menu of windows CSMART maintains,
   * and set up a listener to update the menu when the tool is exited.
   */
  private void addTool(String toolName, String docName, JFrame tool) {
    NamedFrame.getNamedFrame().addFrame(toolName + ": " + docName, tool);
    final JFrame frameArg = tool;
    tool.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	NamedFrame.getNamedFrame().removeFrame(frameArg);
	frameArg.dispose();
      }
    });
  }

  /**
   * Start up CSMART main UI. <br>
   * If <code>org.cougaar.useBootstrapper</code> is set false, use CLASSPATH to find classes as normal.<br>
   * Otherwise, use the Cougaar Bootstrapper to search the Classpath + CIP/lib, /plugins, /sys, etc.
   **/
  public static void main(String[] args) {
    // Use the Cougaar Bootstrapper to make CLASSPATH issues a little easier.
    if ("true".equals(System.getProperty("org.cougaar.useBootstrapper", "true"))) {
      //System.err.println("Using Bootstrapper");
      Bootstrapper.launch(CSMART.class.getName(), args);
    } else {
      launch(args);
    }
  }

  public String getUniqueExperimentName(String name, 
                                        boolean allowExistingName) {
    return organizer.getUniqueExperimentName(name, allowExistingName);
  }
  
  public static void launch(String[] args) {
    new CSMART();
  }

  /**
   * Write out basic info about this run of CSMART
   * to help with debugging.
   *
   * @return a <code>String</code> debug string for logging
   */
  public String writeDebug() {
    // Cougaar version, build info
    StringBuffer result = new StringBuffer();
    String version = null;
    long buildtime = 0;
    try {
      Class vc = Class.forName("org.cougaar.Version");
      Field vf = vc.getField("version");
      Field bf = vc.getField("buildTime");
      version = (String) vf.get(null);
      buildtime = bf.getLong(null);
    } catch (Exception e) {}

    result.append("CSMART ");
    if (version == null) {
      result.append("(unknown version)\n");
    } else {
      result.append(version+" built on "+(new Date(buildtime)) + "\n");
    }
    
    String vminfo = System.getProperty("java.vm.info");
    String vmv = System.getProperty("java.vm.version");
    result.append("VM: JDK "+vmv+" ("+vminfo+")\n");
    String os = System.getProperty("os.name");
    String osv = System.getProperty("os.version");
    result.append("OS: "+os+" ("+osv+")\n");

    // Some properties valus: install.path, config.path,
    // dbMode, isMySQL
    // What tools are open
    // whats loaded in the workspace
    // results directory setting
    // but probably not the cougaar.rc contents

    return result.toString();
  }
  
  // Logging Methods

  private static LoggerFactory lf;
  static {
    lf = LoggerFactory.getInstance();

    Properties defaults = new Properties();
    defaults.setProperty("log4j.rootCategory", "WARN, A1");
    defaults.setProperty("log4j.appender.A1",
                         "org.apache.log4j.ConsoleAppender");
    defaults.setProperty("log4j.appender.A1.Target", "System.out");
    defaults.setProperty("log4j.appender.A1.layout", 
                         "org.apache.log4j.PatternLayout");
    defaults.setProperty("log4j.appender.A1.layout.ConversionPattern", 
                         "%d{ABSOLUTE} %-5p [ %t] - %m%n");

    Properties props = new Properties(defaults);
    // Get the debug file.
    ConfigFinder cf = ConfigFinder.getInstance();
    try {
      props.load(new FileInputStream(cf.locateFile("debug.properties")));
    } catch(Exception e) {
      System.err.println("Could not read debug properties file, using defaults");
    }

    lf.configure(props);
  }

  /**
   * Used to grab an instance of the Logger
   *
   * @param requestor 
   * @return a <code>Logger</code> value
   */
  public static Logger createLogger(String name) {
    return lf.createLogger(name);
  }

  private void createLog() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLog();
  }

}

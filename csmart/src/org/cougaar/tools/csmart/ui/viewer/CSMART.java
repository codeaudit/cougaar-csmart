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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.cougaar.Version;
import org.cougaar.core.node.Bootstrapper;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.LoggerFactory;

// tools created by this user interface

import org.cougaar.tools.csmart.ui.analyzer.Analyzer;
import org.cougaar.tools.csmart.ui.configbuilder.PropertyBuilder;
import org.cougaar.tools.csmart.ui.console.CSMARTConsole;
import org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL;
import org.cougaar.tools.csmart.ui.experiment.ExperimentBuilder;

import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.monitor.generic.ExtensionFileFilter;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Util;

import org.cougaar.tools.csmart.society.scalability.ScalabilityXSociety;
import java.io.ObjectInputStream;

/**
 * Top level CSMART user interface.
 * Allows user to:
 * build, test, control, monitor and analyze
 * a society.
 */
public class CSMART extends JFrame implements ActionListener, Observer, TreeSelectionListener {
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
  private static JMenu newExperimentMenu;
  private static JMenu newRecipeMenu;
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

  // Constructor

  public CSMART() {
    setTitle("CSMART");

    createLog();

    // Write initial CSMART info to the log file
    if (log.isFatalEnabled()) {
      log.fatal(writeDebug());
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
    newSocietyMenuItem = new JMenuItem("New Society");
    newSocietyMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          organizer.newSociety();
        }
      });
    newSocietyMenuItem.setEnabled(false); // disable creating built-in societies
    fileMenu.add(newSocietyMenuItem);
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
    saveToDatabaseMenuItem = 
      new JMenuItem(ActionUtil.SAVE_TO_DATABASE_ACTION);
    saveToDatabaseMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          organizer.saveRecipe();
        }
      });
    fileMenu.add(saveToDatabaseMenuItem);
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

  public void addRunningExperiment(Experiment experiment) {
    runningExperiments.add(experiment);
  }

  public void removeRunningExperiment(Experiment experiment) {
    runningExperiments.remove(experiment);
  }

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
   * TreeSelectionListener interface.
   * Listen on organizer tree to determine what is selected
   * and enable appropriate tools.
   */

  public void valueChanged(TreeSelectionEvent e) {
    enableCSMARTTools();
  }

  /**
   * Called on initialization, when a selection changes in the Organizer,
   * or when the configuration builder, experiment builder, or console
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
   * Updates which tools are enabled.
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

  // the rest of this is methods to launch the various tools
  // within CSMART
  public void runBuilder(ModifiableComponent cc, 
                         boolean alwaysNew) {
    // components are always editable
//      if (!cc.isEditable()) {
//        Object[] options = { "Edit", "View", "Copy", "Cancel" };
//        int result = 
//          JOptionPane.showOptionDialog(this,
//                                       cc.getShortName() + " is not editable",
//                                       "Not Editable",
//                                       JOptionPane.DEFAULT_OPTION,
//                                       JOptionPane.WARNING_MESSAGE,
//                                       null,
//                                       options,
//                                       options[0]);
//        if (result == 0) {
//          // edit it anyway
//          cc.setEditable(true);
//        } else if (result == 2) {
//          // copy it
//          if (cc instanceof RecipeComponent)
//            cc = organizer.copyRecipe((RecipeComponent)cc);
//          else if (cc instanceof SocietyComponent)
//            cc = organizer.copySociety((SocietyComponent)cc);
//        } else if (result != 1)
//          // user cancelled
//          return;
//      }
    // note that cc is guaranteed non-null when this is called
    Class[] paramClasses = { ModifiableComponent.class };
    Object[] params = new Object[1];
    params[0] = cc;
    createTool(CONFIGURATION_BUILDER, PropertyBuilder.class, 
	       alwaysNew, cc.getShortName(), (ModifiableComponent)cc,
	       paramClasses, params);
  }

  private void runMultipleBuilders(ModifiableComponent[] comps) {
    for (int i = 0; i < comps.length; i++) {
      String s = CONFIGURATION_BUILDER + ": " + comps[i].getShortName();
      if (NamedFrame.getNamedFrame().getFrame(s) == null) 
  	runBuilder(comps[i], true);
    }
  }

  public static boolean isExperimentInEditor(Experiment experiment) {
    String s = EXPERIMENT_BUILDER + ": " + experiment.getExperimentName();
    if (NamedFrame.getNamedFrame().getFrame(s) != null)
      return true;
    else
      return false;
  }
    
  public static boolean isRecipeInEditor(RecipeComponent recipe) {
    String s = CONFIGURATION_BUILDER + ": " + recipe.getRecipeName();
    if (NamedFrame.getNamedFrame().getFrame(s) != null)
      return true;
    else
      return false;
  }
    
  public void runExperimentBuilder(Experiment experiment, 
                                   boolean alwaysNew) {
    // if this experiment is being edited, then don't edit again
    if (isExperimentInEditor(experiment))
      return;
    // if the experiment is being run, it can't be edited
    //    if (experiment.isRunInProgress()) {
    //      Object[] options = { "View", "Copy", "Cancel" };
    //      experiment = queryUser(experiment, options);
    //    } else if (!experiment.isEditable()) {
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
    Class[] paramClasses = { CSMART.class, Experiment.class };
    Object[] params = new Object[2];
    params[0] = this;
    params[1] = experiment;
    createTool(EXPERIMENT_BUILDER, ExperimentBuilder.class, 
	       alwaysNew, experiment.getExperimentName(), experiment,
	       paramClasses, params);
  }

  /**
   * If an experiment builder is not editing this experiment,
   * then start a new experiment builder to edit this experiment.
   */

  private void runMultipleExperimentBuilders(Experiment[] experiments) {
    for (int i = 0; i < experiments.length; i++) 
      runExperimentBuilder(experiments[i], true);
  }

  /**
   * Run the specified experiment.  The experiment must be runnable.
   */

  public void runConsole(Experiment experiment) {
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
    Class[] paramClasses = { CSMART.class, Experiment.class };
    Object[] params = new Object[2];
    params[0] = this;
    params[1] = experiment;
    console = (CSMARTConsole)createNewTool(EXPERIMENT_CONTROLLER,
					   CSMARTConsole.class, 
					   experiment.getExperimentName(),
					   experiment, 
                                           paramClasses, params);
  }

  public void runMonitor() {
    Experiment runningExperiment = null;
    String name = "";
    if (runningExperiments.size() > 0) {
      runningExperiment = (Experiment)runningExperiments.get(0);
      name = runningExperiment.getExperimentName();
    }
    Class[] paramClasses = { CSMART.class, Experiment.class };
    Object[] params = new Object[2];
    params[0] = this;
    params[1] = runningExperiment;
    createTool(SOCIETY_MONITOR, CSMARTUL.class, name, null,
               paramClasses, params);
  }

  public void runAnalyzer(Experiment experiment) {
    Class[] paramClasses = { CSMART.class, Experiment.class };
    Object[] params = new Object[2];
    params[0] = this;
    params[1] = experiment;
    createTool(PERFORMANCE_ANALYZER, Analyzer.class, 
	       experiment.getExperimentName(), experiment, 
               paramClasses, params);
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
   */

  public File getResultDir() {
    return resultDir;
  }

  /**
   * ActionListener interface.
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
      // Configuration Builder is used to edit both societies and recipes
      ArrayList components = new ArrayList();
      RecipeComponent[] recipes = organizer.getSelectedRecipes();
      if (recipes != null) 
        for (int i = 0; i < recipes.length; i++)
          components.add(recipes[i]);
      SocietyComponent[] societies = organizer.getSelectedSocieties();
      if (societies != null)
        for (int i = 0; i < societies.length; i++)
          components.add(societies[i]);
      // if no recipes or societies, then try to create a society
      if (components.size() == 0) {
	organizer.addSociety();
	societies = organizer.getSelectedSocieties();
        if (societies != null)
          for (int i = 0; i < societies.length; i++)
            components.add(societies[i]);
      }
      if (components.size() == 1)
	runBuilder((ModifiableComponent)components.get(0), false);
      else if (components.size() > 1) {
	runMultipleBuilders((ModifiableComponent[])components.toArray(new ModifiableComponent[components.size()]));
      }
    } else if (s.indexOf(EXPERIMENT_BUILDER) != -1) {
      Experiment[] experiments = organizer.getSelectedExperiments();
      if (experiments == null || experiments.length == 0) {
	organizer.addExperiment();
	experiments = organizer.getSelectedExperiments();
      }
      if (experiments != null && experiments.length == 1)
	runExperimentBuilder(experiments[0], false);
      else if (experiments != null && experiments.length > 1)
	runMultipleExperimentBuilders(experiments);
    } else if (s.indexOf(EXPERIMENT_CONTROLLER) != -1) {
      Experiment[] experiments = organizer.getSelectedExperiments();
      if (experiments != null) {
        for (int i = 0; i < experiments.length; i++) 
          if (experiments[i].isRunnable()) {
            runConsole(experiments[i]);
            break;
          }
      }
    } else if (s.indexOf(SOCIETY_MONITOR) != -1) {
      if (NamedFrame.getNamedFrame().getToolFrame(SOCIETY_MONITOR) == null)
        runMonitor();
    } else if (s.indexOf(PERFORMANCE_ANALYZER) != -1) {
      Experiment[] experiments = organizer.getSelectedExperiments();
      if (experiments != null && experiments.length > 0)
        runAnalyzer(experiments[0]);
    } else { // a frame selected from the window menu
      JFrame f = NamedFrame.getNamedFrame().getFrame(s);
      if (f != null) {
	f.toFront();
	f.setState(Frame.NORMAL);
      }
    }
  }

  /**
   * Create a tool such as a builder or monitor; handles
   * setting the tool window title and adding it to our Window menu.
   * Assumes that tools extend JFrame and have a constructor
   * that accepts a single CSMART argument.
   */

  private JFrame createNewTool(String toolName, Class toolClass, 
			       String docName, Object argument,
			       Class[] paramClasses, Object[] params) {
    return createTool(toolName, toolClass, true, docName, argument, 
		      paramClasses, params);
  }

  private JFrame createTool(String toolName, Class toolClass, 
			    String docName, Object argument,
			    Class[] paramClasses, Object[] params) {
    return createTool(toolName, toolClass, false, docName, argument,
		      paramClasses, params);
  }

  private JFrame createTool(String toolName, Class toolClass, 
			    boolean alwaysNew, String docName,
			    Object argument, Class[] paramClasses, Object[] params) {
    JFrame tool = null;

    // try to reuse tool
    if (!alwaysNew) {
      // first try to get tool displaying the same document
      tool = NamedFrame.getNamedFrame().getFrame(toolName + ": " + docName);
      // if have tool displaying another document, reuse the tool
      if (tool == null) {
	tool = NamedFrame.getNamedFrame().getToolFrame(toolName, docName);
	if (tool != null) {
	  // set item to edit when reuse tools
	  if (tool instanceof ExperimentBuilder)
	    ((ExperimentBuilder)tool).reinit((Experiment)argument);
	  else if (tool instanceof PropertyBuilder)
	    ((PropertyBuilder)tool).reinit((ModifiableComponent)argument);
	  else if (tool instanceof Analyzer)
	    ((Analyzer)tool).reinit((Experiment)argument);
          enableCSMARTTools(); // update tools
	}
      }
      // if have tool window, just bring it to the front and return
      if (tool != null) {
	tool.toFront();
	tool.setState(Frame.NORMAL);
	return tool;
      }
    }

    // create a new tool
    try {
      if (paramClasses == null) {
	paramClasses = new Class[1];
	paramClasses[0] = CSMART.class;
	params = new Object[1];
	params[0] = this;
      }
      Constructor constructor = toolClass.getConstructor(paramClasses);
      tool = (JFrame) constructor.newInstance(params);
    } catch (Exception exc) {
      if(log.isErrorEnabled()) {
        log.error("CSMART: " + exc, exc);
      }
      return null;
    }
    if (docName != null)
      toolName = toolName + ": " + docName;
    NamedFrame.getNamedFrame().addFrame(toolName, tool);
    final JFrame frameArg = tool;
    tool.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	NamedFrame.getNamedFrame().removeFrame(frameArg);
	frameArg.dispose();
      }
    });
    return tool;
  }

  /**
   * Start up CSMART main UI.<br>
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

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

// tools created by this user interface
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import org.cougaar.Version;
import org.cougaar.bootstrap.Bootstrapper;
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
import org.cougaar.tools.csmart.ui.analyzer.Analyzer;
import org.cougaar.tools.csmart.ui.configbuilder.PropertyBuilder;
import org.cougaar.tools.csmart.ui.console.CSMARTConsole;
import org.cougaar.tools.csmart.ui.experiment.ExperimentBuilder;
import org.cougaar.tools.csmart.ui.monitor.generic.ExtensionFileFilter;
import org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.LoggerFactory;

/**
 * Top level CSMART user interface.
 * Allows user to: build, test, control, monitor and analyze a society. For installation and
 * basic usage, see csmart/doc/InstallAndTest.html.<br>
 * @property org.cougaar.tools.csmart.doWorkspace if false do NOT read/write from Default Worksapce file.
 * @property org.cougaar.configuration.database should be specified in cougaar.rc as the URL to find the CSMART database. See CSMART InstallAndTest.html
 * @property csmart.PopulateDb.log.enable if true enables the logging
 * of some executed database queries to a file named PopulateDb<datetime>.log.
 * @property org.cougaar.install.path Used to default results directory, find workspace file, saved graphs, etc
 * @property org.cougaar.config.path has standard meaning, and is used to find some CSMART UI properties among other things.
 * @property org.cougaar.useBootstrapper has meaning specified by Bootstrapper
 *
 */
public class CSMART extends JFrame {
  private static Organizer organizer;
  private static JFileChooser workspaceFileChooser;
  private static ArrayList runningExperiments = new ArrayList();
  private static JToolBar toolBar = null;
  private static JMenu windowMenu;
  private static CSMARTConsole console;
  private static File resultDir;
  private static JMenu fileMenu;
  private static JMenu editMenu;
  private static JButton configureButton;
  private static JButton buildButton;
  private static JButton runButton;
  private static Hashtable titleToMenuItem = new Hashtable();

  // define strings here so we can easily change them
  private static final String FILE_MENU = "File";
  private static final String NEW_MENU_ITEM = "Open Workspace...";
  private static final String NEW_RESULTS_MENU_ITEM = "Save New Results In...";
  private static final String EXIT_MENU_ITEM = "Exit";

  private static final String EDIT_MENU = "Edit";

  private static final String WINDOW_MENU = "Window";

  private static final String HELP_MENU = "Help";
  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";
  private static final String VERSION_ACTION = "Show CSMART Version";
  private static final String ABOUT_CSMART_ACTION = "About CSMART";
  private static final String HELP_ACTION = "About Launcher";
  private static final String CSMART_LISTENER = "CSMART-";
  private static String listenerId;
  private transient Logger log;

  private Action[] helpActions = {
    new AbstractAction(HELP_ACTION) {
        public void actionPerformed(ActionEvent e) {
          CSMART.displayURL(HELP_DOC);
        }
      },
    new AbstractAction(VERSION_ACTION) {
        public void actionPerformed(ActionEvent e) {
	  Browser.setPage(writeDebug());
          //CSMART.displayURL(ABOUT_DOC);
        }
      },
    new AbstractAction(ABOUT_CSMART_ACTION) {
        public void actionPerformed(ActionEvent e) {
          CSMART.displayURL(ABOUT_DOC);
        }
      }
  };

  private static final String PRE="<html><center><b><font face=\"sans-serif\">";
  private static final String POST="</font></b></center></html>";
  // tool names
  public static final String CONFIGURATION_BUILDER = "Configuration Builder";
  public static final String EXPERIMENT_BUILDER = "Experiment Builder";
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
    "Experiment48t.gif",
    "EC.gif",
    "SM.gif",
    "PA.gif"
  };

  private CSMART csmart;

  private Experiment experimentToEdit;

  /**
   * Constructor for top level class in CSMART.
   */
  public CSMART() {
    setTitle("CSMART");
    csmart = this;

    createLog();

    // Write initial CSMART info to the log file
    if (log.isShoutEnabled()) {
      log.shout(writeDebug());
    }

    String hostName = "";
    try {
      InetAddress myAddr = InetAddress.getLocalHost();
      hostName = myAddr.getHostName();
    } catch (Exception e) {
    }
    listenerId = CSMART_LISTENER + hostName + "-" +
      new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    resultDir = initResultDir();

    organizer = new Organizer(this);
    initDisplay();
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    // if user closes this window, quit
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if(!CSMART.this.getGlassPane().isVisible()) {
          exit();
        }
      }
    });

    NamedFrame.getNamedFrame().addObserver(myFrameObserver);

    pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int w = getWidth();
    int h = getHeight();
    setSize(w, h);
    setLocation((screenSize.width - w)/2, (screenSize.height - h)/2);
    setVisible(true);
  }

  public static String getNodeListenerId() {
    return listenerId;
  }

  // must be called whenever there's a new organizer (i.e. new workspace)
  private void initDisplay() {
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    // set-up file menu which includes entries based on workspace selection
    fileMenu = new JMenu(FILE_MENU);
    fileMenu.setToolTipText("Create new workspace or quit.");
    fileMenu.addMenuListener(myMenuListener);
    JMenuItem newMenuItem = new JMenuItem(NEW_MENU_ITEM);
    newMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          newWorkspace();
        }
      });
    newMenuItem.setToolTipText("Create a new workspace.");
    fileMenu.add(newMenuItem);
    JMenuItem newResultsMenuItem = new JMenuItem(NEW_RESULTS_MENU_ITEM);
    newResultsMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setResultDir();
        }
      });
    newResultsMenuItem.setToolTipText("Select a directory for saving results");
    fileMenu.add(newResultsMenuItem);

    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          exit();
        }
      });
    exitMenuItem.setToolTipText("Exit");
    fileMenu.add(exitMenuItem);

    // End of file menu
    editMenu = new JMenu(EDIT_MENU);
    editMenu.setToolTipText("Load, edit, run, or delete experiments");
    editMenu.addMenuListener(myMenuListener);

    JMenu newExperimentMenu = new JMenu(ActionUtil.NEW_EXPERIMENT_ACTION);
    for (int i = 0; i < organizer.newExperimentActions.length; i++)
      newExperimentMenu.add(organizer.newExperimentActions[i]);
    editMenu.add(newExperimentMenu);
    newExperimentMenu.setToolTipText("Load or Create an Experiment");

    JMenu newRecipeMenu = new JMenu(ActionUtil.NEW_RECIPE_ACTION);
    for (int i = 0; i < organizer.newRecipeActions.length; i++)
      newRecipeMenu.add(organizer.newRecipeActions[i]);
    editMenu.add(newRecipeMenu);
    newRecipeMenu.setToolTipText("Load or Create a Recipe");

    JMenuItem newMenu = null;
    newMenu = new JMenuItem(organizer.deleteExperimentFromDatabaseAction);
    newMenu.setToolTipText("Delete an Experiment from the Database");
    editMenu.add(newMenu);

    newMenu = new JMenuItem(organizer.deleteRecipeFromDatabaseAction);
    newMenu.setToolTipText("Delete a recipe from the Database");
    editMenu.add(newMenu);

    newMenu = new JMenuItem(organizer.newFolderAction);
    newMenu.setToolTipText("Create new folder in Organizer");
    editMenu.add(newMenu);

    editMenu.addSeparator();

    newMenu = new JMenuItem(organizer.configureAction);
    newMenu.setToolTipText("Edit a Society or Recipe");
    editMenu.add(newMenu);

    newMenu = new JMenuItem(organizer.buildExperimentAction);
    newMenu.setToolTipText("Set up Experiment Configuration");
    editMenu.add(newMenu);

    newMenu = new JMenuItem(organizer.runExperimentAction);
    newMenu.setToolTipText("Run a configured Experiment, or attach to a running Experiment");
    editMenu.add(newMenu);

    newMenu = new JMenuItem(organizer.duplicateAction);
    newMenu.setToolTipText("Copy this item");
    editMenu.add(newMenu);

    newMenu = new JMenuItem(organizer.renameAction);
    newMenu.setToolTipText("Rename this item (possibly workspace)");
    editMenu.add(newMenu);

    newMenu = new JMenuItem(organizer.saveAction);
    newMenu.setToolTipText("Save a modified item");
    editMenu.add(newMenu);

    newMenu = new JMenuItem(organizer.deleteAction);
    newMenu.setToolTipText("Delete this item");
    editMenu.add(newMenu);

    // Done with Edit menu

    windowMenu = new JMenu(WINDOW_MENU);
    windowMenu.setToolTipText("Display selected window.");

    JMenu helpMenu = new JMenu(HELP_MENU);
    for (int i = 0; i < helpActions.length; i++) 
      helpMenu.add(new JMenuItem(helpActions[i]));

    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(windowMenu);
    menuBar.add(helpMenu);
    
    if (toolBar != null)
      getContentPane().remove(toolBar); // get rid of old tool bar if any
    toolBar = new JToolBar();
    toolBar.setLayout(new GridLayout(1, 5, 2, 2));
    getContentPane().add("North", toolBar);
    organizer.addTreeSelectionListener(new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
          enableCSMARTTools();
        }
      });

    getContentPane().add("Center", organizer);

    Action societyAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          JFrame societyMonitorFrame = 
            NamedFrame.getNamedFrame().getToolFrame(SOCIETY_MONITOR);
          if (societyMonitorFrame == null)
            runMonitor();
          else
            societyMonitorFrame.toFront();
        }
      };

    Action analyzerAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          DefaultMutableTreeNode node = organizer.getSelectedNode();
          Object userObject = node.getUserObject();
          if (userObject instanceof Experiment)
            runAnalyzer((Experiment)userObject);
          else
            runAnalyzer();
        }
      };

    Action[] actions = {
      organizer.configureAction,
      organizer.buildExperimentAction,
      organizer.runExperimentAction,
      societyAction,
      analyzerAction
    };

    for (int i = 0; i < views.length; i++) {
      JButton button = makeButton(views[i], iconFilenames[i], 
                                  actions[i]);
      button.setHorizontalTextPosition(JButton.CENTER);
      button.setVerticalTextPosition(JButton.BOTTOM);
      button.setActionCommand(views[i]);
      button.setToolTipText(tooltips[i]);
      toolBar.add(button);
    }
    configureButton = (JButton)toolBar.getComponentAtIndex(0);
    buildButton = (JButton)toolBar.getComponentAtIndex(1);
    runButton = (JButton)toolBar.getComponentAtIndex(2);
    enableCSMARTTools();
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
          enableActions((JMenu)e.getSource());
        }
        // set whether actions in menu are enabled
        // and return true if any are enabled
        private boolean enableActions(JMenu menu) {
          boolean haveEnabledActions = false;
          int n = menu.getItemCount();
          for (int i = 0; i < n; i++) {
            JMenuItem menuItem = menu.getItem(i);
            if (menuItem == null)
              continue;
            String s = menuItem.getText();
            if (s.equals(NEW_MENU_ITEM) ||
                s.equals(NEW_RESULTS_MENU_ITEM) ||
                s.equals(EXIT_MENU_ITEM)) {
              haveEnabledActions = true;
              continue;
            }
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
      }; // end of listener

  // TODO: runningExperiments is maintained solely so
  // that the SocietyMonitor can be started with a reference
  // to the running experiment if there is one
  // is there a better way to do this, i.e. have the console(s)
  // implement a method to get the running experiment if any?
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

  /**
   * Called on initialization, when a selection changes in the Organizer, or
   * when the configuration builder, experiment builder, or console
   * start or stop.
   * Uses the action utilities to figure out what's allowed,
   * so that it matches the File menu and pop-up menus.
   */

  protected void enableCSMARTTools() {
    ActionUtil.setActionAllowed(configureButton.getAction(), organizer);
    ActionUtil.setActionAllowed(buildButton.getAction(), organizer);
    ActionUtil.setActionAllowed(runButton.getAction(), organizer);
  }

  private void exit() {
    if (organizer.exitAllowed()) {
      // don't stop experiments when exiting CSMART
      //      if (console != null)
      //	console.stopExperiments();
      System.exit(0);
    }
  }

  private JButton makeButton(String label, String iconFilename, 
                             Action action) {
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
    JButton button = new JButton(action);
    button.setText(label);
    button.setIcon(icon);
    return button;
    //    return new JButton(label, icon);
  }

  Observer myFrameObserver = new Observer() {
      ActionListener myActionListener = new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
	    String s = 
	      ((AbstractButton)e.getSource()).getActionCommand();
	    JFrame f = NamedFrame.getNamedFrame().getFrame(s);
	    if (f != null) {
	      f.toFront();
	      f.setState(Frame.NORMAL);
	    }
	  }
        };
      
      /**
       * Window management for windows launched by CSMART.
       * Updates which tools are enabled and updates the Window menu.
       * @param o the <code>NamedFrame</code> that was added, removed or changed
       * @param arg an event describing the change
       */
      public void update(Observable o, Object arg) {
        if (o instanceof NamedFrame) {
	  //          NamedFrame namedFrame = (NamedFrame) o;
          NamedFrame.Event event = (NamedFrame.Event) arg;
          if (event.eventType == NamedFrame.Event.ADDED) {
            JMenuItem menuItem = new JMenuItem(event.title);
            titleToMenuItem.put(event.title, menuItem);
            menuItem.addActionListener(myActionListener);
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
              newMenuItem.addActionListener(myActionListener);
              windowMenu.add(newMenuItem);
            }
          }
        }
      }
    };

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
    if (cc instanceof Experiment) {
      runBuilderOnExperiment((Experiment)cc);
      return;
    }

    ModifiableComponent originalComponent = null;
    Experiment experiment = null;
    DefaultMutableTreeNode selectedNode = organizer.getSelectedNode();
    DefaultMutableTreeNode parentNode = 
      (DefaultMutableTreeNode)selectedNode.getParent();
    if (parentNode.getUserObject() != null &&
        parentNode.getUserObject() instanceof Experiment) {
      experiment = (Experiment)parentNode.getUserObject();
      // if configuring a component in an experiment, 
      // copy the component, so it's modified only in the experiment
      if (cc instanceof SocietyComponent) {
        SocietyComponent society = (SocietyComponent)cc;
        String newName =
          organizer.generateSocietyName(society.getSocietyName(), false);
        if (newName == null)
          return;
        SocietyComponent societyCopy = (SocietyComponent)society.copy(newName);
        originalComponent = cc;
        cc = societyCopy;
      } else if (cc instanceof RecipeComponent) {
        RecipeComponent recipe = (RecipeComponent)cc;
        String newName =
          organizer.generateRecipeName(recipe.getRecipeName(), false);
        if (newName == null)
          return;
        RecipeComponent recipeCopy = (RecipeComponent)recipe.copy(newName);
        originalComponent = cc;
        cc = recipeCopy;
      } else
        return; // unknown component in experiment
    }
    cc.setEditable(false);
    if (originalComponent != null)
      originalComponent.setEditable(false);
    JFrame tool = 
      (JFrame)new PropertyBuilder(this, cc, originalComponent, experiment);
    addTool(CONFIGURATION_BUILDER, cc.getShortName(), tool);
  }

  private void runBuilderOnExperiment(Experiment experiment) {
    Thread configureThread = null;
    experimentToEdit = null;
    configureThread = 
      new ConfigureThread("ConfigureExperiment", experiment);
    // TODO: this isn't blocking mouse events on the toolbar; it's deferring them
    GUIUtils.timeConsumingTaskStart(csmart);
    try {
      configureThread.start();
    } catch (RuntimeException re) {
      if (log.isErrorEnabled()) {
        log.error("Runtime exception creating experiment", re);
      }
    }
    // wait for the above to finish, and then edit the new society
    if (configureThread != null) {
      try {
        configureThread.join();
      } catch (InterruptedException ie) {
        if (log.isErrorEnabled()) {
          log.error("Interrupted exception creating experiment", ie);
        }
      }
      if (experimentToEdit == null)
        return;
      organizer.addExperimentAndComponentsToWorkspace(experimentToEdit,
             (DefaultMutableTreeNode)organizer.getSelectedNode().getParent());
      SocietyComponent originalSociety = 
        experimentToEdit.getSocietyComponent();
      String copyName = 
        organizer.generateSocietyName(originalSociety.getSocietyName(), false);
      if (copyName == null) {
        GUIUtils.timeConsumingTaskEnd(csmart);
        return;
      }
      SocietyComponent societyCopy = 
        (SocietyComponent)originalSociety.copy(copyName);
      societyCopy.setEditable(false);
      originalSociety.setEditable(false);
      JFrame tool = 
        (JFrame)new PropertyBuilder(csmart, societyCopy, originalSociety,
                                    experimentToEdit);
      addTool(CONFIGURATION_BUILDER, societyCopy.getShortName(), tool);
      GUIUtils.timeConsumingTaskEnd(csmart);
    }
  }

  /**
   * Run the experiment builder to edit an experiment.
   * @param experiment the experiment to edit
   * @param alwaysNew true to create a new <code>ExperimentBuilder</code>; false to re-use an existing one
   */
  protected void runExperimentBuilder(Experiment experiment, 
                                      boolean alwaysNew) {
//      // if this experiment is being edited, then don't edit again
//      if (isExperimentInEditor(experiment))
//        return;
//      if (!experiment.isEditable()) {
//        // otherwise editability can be overwritten
//        Object[] options = { "Edit", "View", "Copy", "Cancel" };
//        experiment = queryUser(experiment, options);
//        if (experiment == null)
//          return;
//      }
    // gray out the children of the experiment in the tree
    // to avoid confusion while editing the experiment
    organizer.removeChildren(experiment);
    JFrame tool =
      (JFrame)new ExperimentBuilder(this, experiment);
    addTool(EXPERIMENT_BUILDER, experiment.getExperimentName(), tool);
  }

//    private Experiment queryUser(Experiment experiment,
//                                 Object[] options) {
//      int result = 
//          JOptionPane.showOptionDialog(this,
//                                       experiment.getShortName() + 
//                                       " is not editable",
//                                       "Experiment Not Editable",
//                                       JOptionPane.DEFAULT_OPTION,
//                                       JOptionPane.WARNING_MESSAGE,
//                                       null,
//                                       options,
//                                       options[0]);
//      if (options[result].equals("Edit"))
//        // edit it anyway
//        experiment.setEditable(true);
//      else if (options[result].equals("Copy"))
//        // copy it
//        experiment = organizer.copyExperiment(experiment, true);
//      else if (options[result].equals("Cancel"))
//        // user cancelled
//        return null;
//      return experiment;
//    }
   
  /**
   * Run the specified experiment.  The experiment must be runnable.
   * @param experiment the experiment to run
   */
  protected void runConsole(Experiment experiment) {
    JFrame tool =
      (JFrame)new CSMARTConsole(this, experiment);
    String s = "";
    if (experiment != null)
      s = experiment.getExperimentName();
    addTool(EXPERIMENT_CONTROLLER, s, tool);
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

  protected void runAnalyzer() {
    JFrame tool = (JFrame)new Analyzer(this);
    addTool(PERFORMANCE_ANALYZER, "", tool);
  }

  /**
   * Add the tool to the menu of windows CSMART maintains,
   * and set up a listener to update the menu when the tool is exited.
   */
  private void addTool(String toolName, String docName, JFrame tool) {
    NamedFrame.getNamedFrame().addFrame(toolName + ": " + docName, tool);
    final JFrame frameArg = tool;
    final boolean isConsole = (toolName == EXPERIMENT_CONTROLLER);
    tool.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    tool.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if(!frameArg.getGlassPane().isVisible()) {

	  // If user hit cancel in exit dialog in console,
	  // then don't close the window
	  if (isConsole && ((CSMARTConsole)frameArg).dontClose)
	    return;

          NamedFrame.getNamedFrame().removeFrame(frameArg);
	  // Next line can cause NPE in Container.removeNotify(line 1878)
	  // Is there some test we can/should do on the JFrame before calling?
	  try {
	    frameArg.dispose();
	  } catch (NullPointerException npe) {
	    if (log.isWarnEnabled())
	      log.warn("Bug 1439: Was a drop-down list open? Using: " + writeDebug(), npe);
	  }
        }
      }
    });
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
    initDisplay();
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

  public static void displayURL(String s) {
    URL help = (URL)CSMART.class.getResource(s);
    if (help != null)
      Browser.setPage(help);
  }

  /**
   * Start up CSMART main UI. <br>
   * If <code>org.cougaar.useBootstrapper</code> is set false, 
   * use CLASSPATH to find classes as normal.<br>
   * Otherwise, use the Cougaar Bootstrapper to search the 
   * Classpath + CIP/lib, /plugins, /sys, etc.
   **/
  public static void main(String[] args) {
    if ("true".equals(System.getProperty("org.cougaar.useBootstrapper", "true"))) {
      Bootstrapper.launch(CSMART.class.getName(), args);
    } else {
      launch(args);
    }
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
  public static String writeDebug() {
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
    result.append("org.cougaar.tools.csmart.doWorkspace=" + System.getProperty("org.cougaar.tools.csmart.doWorkspace") + "\n");
    result.append("org.cougaar.config.path=" + System.getProperty("org.cougaar.config.path") + "\n");
    result.append("org.cougaar.install.path=" + System.getProperty("org.cougaar.install.path") + "\n");
    //    result.append("CSMART configuration DB (org.cougaar.configuration.database)=" + System.getProperty("org.cougaar.configuration.database") + "\n");
    // dbMode, isMySQL
    // What tools are open
    // whats loaded in the workspace
//     if (experimentToEdit != null) {
//       result.append("Working with experiment " + experimentToEdit.getFullName().toString() + " (ID: " + experimentToEdit.getExperimentID() + ", trial: " + experimentToEdit.getTrialID() + ") which says to isEditable: " + experimentToEdit.isEditable() + " and to isEditInProgress: " + experimentToEdit.isEditInProgress() + " and to isModified: " + experimentToEdit.isModified() + " and to isRunInProgress: " + experimentToEdit.isRunInProgress() + " and to isRunnable: " + experimentToEdit.isRunnable() + "\n");
//       SocietyComponent soc = experimentToEdit.getSocietyComponent();
//       if (soc != null) {
// 	result.append("  made of society " + soc.getFullName() + " (ID: " + soc.getAssemblyId() + ") whic says to isModified: " + soc.isModified() + " and to isRunning: " + soc.isRunning() + " and to isEditable: " + soc.isEditable() + "\n");
//       }
//       result.append("   and which also has " + experimentToEdit.getRecipeComponentCount() + " recipes\n");
//     }
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
      System.err.println("CSMART.java Could not read debug.properties file, using defaults.");
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

  // Configure an experiment by combining its societies and recipes
  // into a single society.
  class ConfigureThread extends Thread {
    Experiment experiment;

    ConfigureThread(String name, Experiment experiment) {
      this.experiment = experiment;
    }

    public void run() {
      // copy the original experiment and put the copy in the workspace
      String newName = 
        organizer.generateExperimentName(experiment.getExperimentName());
      experimentToEdit = (Experiment)experiment.copy(newName);
      if (experimentToEdit == null) {
        return;
      }
      // remove all the components from the copy
      SocietyComponent sc = experimentToEdit.getSocietyComponent();
      if (sc != null)
        experimentToEdit.removeComponent(sc);
      RecipeComponent[] recipes = experimentToEdit.getRecipeComponents();
      for (int i = 0; i < recipes.length; i++)
        experimentToEdit.removeRecipeComponent(recipes[i]);
      // create a new society based on the society component data
      // in the original experiment
      ComponentData cdata = experiment.getSocietyComponentData();
      newName = organizer.generateSocietyName(cdata.getName());
      cdata.setName(newName);
      SocietyComponent newSociety = 
        new SocietyCDataComponent(cdata,
         ((SocietyComponent)experiment.getSocietyComponent()).getAssemblyId());
      newSociety.initProperties();
      // Save this new society
      newSociety.saveToDatabase();
      // put the new society in the copy of the experiment
      experimentToEdit.addSocietyComponent(newSociety);
    }
  }

}

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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.cougaar.core.society.Bootstrapper;
import org.cougaar.util.Parameters;

import org.cougaar.tools.csmart.societies.database.DBUtils;

// tools created by this user interface

import org.cougaar.tools.csmart.ui.analyzer.Analyzer;
import org.cougaar.tools.csmart.ui.builder.PropertyBuilder;
import org.cougaar.tools.csmart.ui.console.CSMARTConsole;
import org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL;
import org.cougaar.tools.csmart.ui.experiment.*;

import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.component.ConfigurableComponent;
import org.cougaar.tools.csmart.ui.component.ComponentProperties;
import org.cougaar.tools.csmart.ui.component.HostComponent;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;
import org.cougaar.tools.csmart.ui.component.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.ui.monitor.generic.ExtensionFileFilter;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Util;

import org.cougaar.tools.csmart.scalability.ScalabilityXSociety;
import org.cougaar.tools.csmart.societies.database.DBUtils;

/**
 * Top level CSMART user interface.
 * Allows user to:
 * build, test, control, monitor and analyze
 * a society.
 */
public class CSMART extends JFrame implements ActionListener, Observer, TreeSelectionListener {
  public static String MONITOR = "Society Monitor";

  private static Organizer organizer;
  private static JFileChooser workspaceFileChooser;
  private static Hashtable titleToMenuItem = new Hashtable();
  private static JToolBar toolBar;
  private static JMenu windowMenu;
  // the running experiment; set by the console
  private static Experiment runningExperiment;
  private static CSMARTConsole console;
  // define strings here so we can easily change them
  private static final String FILE_MENU = "File";
  private static final String NEW_MENU_ITEM = "Open Workspace...";
  private static final String DBCONFIG_MENU_ITEM = "Configure Database";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String WINDOW_MENU = "Window";
  private static final String HELP_MENU = "Help";

  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  //  protected static final String ABOUT_DOC = "../help/about-csmart.html";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "About Launcher";

  //  private static boolean dbMode = false;

  private String[] helpMenuItems = {
    HELP_MENU_ITEM, ABOUT_CSMART_ITEM
  };

  private static final String PRE="<html><center><b><font face=\"sans-serif\">";
  private static final String POST="</font></b></center></html>";
  private static final String[] views = {
    PRE + "Configuration<br>Builder" + POST,
    PRE + "Configuration<br>Helper" + POST,
    PRE + "Experiment<br>Builder" + POST,
    PRE + "Experiment<br>Controller" + POST,
    PRE + "Society<br>Monitor" + POST,
    PRE + "Performance<br>Analyzer" + POST
  };

  private static final String[] tooltips = {
    "Specify properties of a society or other configurable component.",
    "", 
    "Configure an experiment.",
    "Start, stop and abort experiments.",
    "Monitor a running society.",
    "Analyze results of running an experiment."
  };

  private static final String[] iconFilenames = {
    "SB.gif",
    "CC.gif",
    "EB.gif",
    "EC.gif",
    "SM.gif",
    "PA.gif"
  };

  // used for database
  static JDialog dbConfigDialog = null;
  static JTextField dbConfigField;
  static JTextField dbNameField;
  static JPasswordField dbPasswordField;
  static String dbConfig;
  static String dbName;
  static String dbPassword;

  public CSMART() {
    setTitle("CSMART");

    // Setup the database parameters.
    dbConfig = Parameters.findParameter(DBUtils.DATABASE);
    dbName = Parameters.findParameter(DBUtils.USER);
    dbPassword = Parameters.findParameter(DBUtils.PASSWORD);
      
    //    CSMART.inDBMode(true);
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.setToolTipText("Create new workspace or quit.");
    JMenuItem newMenuItem = new JMenuItem(NEW_MENU_ITEM);
    newMenuItem.addActionListener(this);
    newMenuItem.setToolTipText("Create a new workspace.");
    fileMenu.add(newMenuItem);
    JMenuItem dbConfigMenuItem = new JMenuItem(DBCONFIG_MENU_ITEM);
    dbConfigMenuItem.setToolTipText("Configure the database.");
    dbConfigMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	dbConfigMenuItem_actionPerformed();
      }
    });
    // Don't allow database configuration for now
    //    fileMenu.add(dbConfigMenuItem);
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
      button.addActionListener(this);
      button.setToolTipText(tooltips[i]);
      // disable experiment checker
      if (i == 1)
	button.setEnabled(false);
      toolBar.add(button);
    }
    enableMonitorTool(false);
    enableExperimentTools();

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
   * Display dialog that allows user to configure the database.
   */

  private void dbConfigMenuItem_actionPerformed() {
    if (dbConfigDialog != null) {
      dbConfigDialog.setVisible(true);
      return;
    }
    JPanel panel = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    dbConfigField = new JTextField(dbConfig);
    dbNameField = new JTextField(dbName);
    dbPasswordField = new JPasswordField(dbPassword);
    panel.add(new JLabel("Database:"),
              new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(10, 0, 5, 5),
                                     0, 0));
    panel.add(dbConfigField,
              new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.HORIZONTAL,
                                     new Insets(10, 0, 5, 0),
                                     0, 0));
    x = 0;
    panel.add(new JLabel("User:"),
              new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(10, 0, 5, 5),
                                     0, 0));
    panel.add(dbNameField,
              new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.HORIZONTAL,
                                     new Insets(10, 0, 5, 0),
                                     0, 0));
    x = 0;
    panel.add(new JLabel("Password:"),
              new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(10, 0, 5, 5),
                                     0, 0));
    panel.add(dbPasswordField,
              new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.HORIZONTAL,
                                     new Insets(10, 0, 5, 0),
                                     0, 0));
    
    dbConfigDialog = new JDialog(this, "Database Configuration", true);
    dbConfigDialog.getContentPane().setLayout(new BorderLayout());
    dbConfigDialog.getContentPane().add(panel, BorderLayout.CENTER);
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dbConfig = dbConfigField.getText();
        dbName = dbNameField.getText();
        dbPassword = new String(dbPasswordField.getPassword());
        dbConfigDialog.setVisible(false);
      }
    });
    buttonPanel.add(okButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dbConfigDialog.setVisible(false);
      }
    });
    buttonPanel.add(cancelButton);
    dbConfigDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    dbConfigDialog.setSize(400, 200);
    dbConfigDialog.setVisible(true);
  }

  public static String getDatabaseConfiguration() {
    return dbConfig;
  }

  public static String getDatabaseUserName() {
    return dbName;
  }

  public static String getDatabaseUserPassword() {
    return dbPassword;
  }

  /**
   * Check to see if CSMART is running in the CMT-DB connected mode
   * @return a <code>boolean</code> whether have a valid CMT database connection
   */
  //  public static boolean inDBMode() {
  //    return CSMART.inDBMode(false);
  //  }
  
  /**
   * Check to see if CSMART is running in the CMT-DB connected mode, rechecking
   * the database connection optionally.
   *
   * @param checkConnection a <code>boolean</code> indicating whether to re-check the database connection
   * @return a <code>boolean</code>, true if there is a valid CMT DB connection
   */
//    public static boolean inDBMode(boolean checkConnection) {
//      if (checkConnection) {
//        CSMART.dbMode = DBUtils.isValidDBConnection();
//        System.out.println("CSMART DB MODE: " + CSMART.dbMode);
//      }
//      return CSMART.dbMode;
//    }
  
  public void saveWorkspace() {
    organizer.save();
  }

  public Experiment getExperiment() {
    Experiment[] exp = organizer.getSelectedExperiments();
    if (exp == null || exp.length == 0)
      return null;
    return exp[0];
  }

  public SocietyComponent getSociety() {
    SocietyComponent[] societies = organizer.getSelectedSocieties();
    if (societies == null || societies.length == 0)
      return null;
    return societies[0];
  }

  public ModifiableConfigurableComponent getComponent() {
    ModifiableConfigurableComponent[] comps = organizer.getSelectedComponents();
    if (comps == null || comps.length == 0)
      return null;
    return comps[0];
  }

  /**
   * Set which experiment is running.  Called with null when experiment
   * terminates or is terminated.
   * TODO: allow monitor tool to run on saved files when no experiment running?
   */

  public void setRunningExperiment(Experiment experiment) {
    runningExperiment = experiment;
    enableMonitorTool(runningExperiment != null);
  }

  /**
   * Get the running experiment.  Used by tools that monitor experiments.
   */

  public Experiment getRunningExperiment() {
    return runningExperiment;
  }

  private void enableConsoleTool(boolean enable) {
    ((JButton)toolBar.getComponentAtIndex(3)).setEnabled(enable);
  }

  private void enableMonitorTool(boolean enable) {
    ((JButton)toolBar.getComponentAtIndex(4)).setEnabled(enable);
  }

  private void enableAnalyzerTool(boolean enable) {
    ((JButton)toolBar.getComponentAtIndex(5)).setEnabled(enable);
  }

  /**
   * Listen on organizer tree to determine when an experiment
   * is selected.  Disable console and analyzer
   * when no experiment is selected.
   */

  public void valueChanged(TreeSelectionEvent e) {
    enableExperimentTools();
  }
  
  // TODO: experiment tool buttons should also be enabled/disabled
  // as tools are run, but there are no listeners (on tool completion)
  // to support this
  private void enableExperimentTools() {
    Experiment exp[] = organizer.getSelectedExperiments();
    if (exp == null || exp.length == 0) {
      enableConsoleTool(false);
      enableAnalyzerTool(false);
    } else {
      enableAnalyzerTool(true);
      // TODO: if at least one selected experiment has a society
      // then enable the console tool
      // for now, check that the first selected experiment
      // has a society, because the first experiment will be the one run
      //      for (int i = 0; i < exp.length; i++)
      //	if (exp[i].getSocietyComponentCount() != 0) {
      //	  enableConsoleTool(true);
      //	  return;
      //	}
      if (exp.length > 0 && 
	  exp[0].getSocietyComponentCount() != 0 &&
	  exp[0].isRunnable())
	enableConsoleTool(true);
      else
	enableConsoleTool(false);
    }
  }

  private void exit() {
    if (organizer.exitAllowed()) {
      if (console != null)
	console.stopExperiments();
      System.exit(0);
    }
  }

  private JButton makeButton(String label, String iconFilename) {
    URL iconURL = getClass().getResource(iconFilename);
    if (iconURL == null)
      return new JButton(label);
    ImageIcon icon = new ImageIcon(iconURL);
    return new JButton(label, icon);
  }

  //  public static NamedFrame getNamedFrame() {
  //    return NamedFrame.getNamedFrame();
  //  }

  public void update(Observable o, Object arg) {
    if (o instanceof NamedFrame) {
      NamedFrame namedFrame = (NamedFrame) o;
      NamedFrame.Event event = (NamedFrame.Event) arg;
      if (event.eventType == NamedFrame.Event.ADDED) {
        JMenuItem menuItem = new JMenuItem(event.title);
        titleToMenuItem.put(event.title, menuItem);
        menuItem.addActionListener(this);
        windowMenu.add(menuItem);
      } else if (event.eventType == NamedFrame.Event.REMOVED) {
        JMenuItem menuItem = (JMenuItem) titleToMenuItem.get(event.title);
        if (menuItem == null) {
          System.err.println("CSMART: No window menu item for " + event.title);
        } else {
          windowMenu.remove(menuItem);
          titleToMenuItem.remove(event.title);
        }
	// If the window was the Exp.Builder or the Console, update
	// the experiment Controls
	if (event.title.startsWith("Experiment"))
	    enableExperimentTools();
      } else if (event.eventType == NamedFrame.Event.CHANGED) {
	JMenuItem menuItem = (JMenuItem)titleToMenuItem.get(event.prevTitle);
        if (menuItem == null) {
          System.err.println("CSMART: No window menu item for " + event.title);
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
   * ActionListener interface.
   */
  public void runBuilder(ModifiableConfigurableComponent cc, boolean alwaysNew,
			 boolean openForEditing) {
    if (!cc.isEditable() && openForEditing) {
      int result = JOptionPane.showConfirmDialog(this,
				    "Component is not editable; create copy?",
				    "Component Not Editable",
				    JOptionPane.YES_NO_OPTION,
				    JOptionPane.WARNING_MESSAGE);
      if (result != JOptionPane.YES_OPTION) {
	openForEditing = false;
	//	return;
      } else {
	cc = organizer.copyComponent(cc, null);
      }
    }
    // note that cc is guaranteed non-null when this is called
    Class[] paramClasses = { ModifiableConfigurableComponent.class };
    Object[] params = new Object[1];
    params[0] = cc;
    createTool("Configuration Builder", PropertyBuilder.class, 
	       alwaysNew, cc.getShortName(), (ModifiableConfigurableComponent)cc,
	       paramClasses, params);
  }

  /**
   * If an tree builder is not editing this society,
   * then start a new tree builder to edit this society.
   */
  private void runMultipleBuilders(ComponentProperties[] comps) {
    for (int i = 0; i < comps.length; i++) {
      if (! (comps[i] instanceof ModifiableConfigurableComponent))
	continue;
      String s = "Configuration Builder: " + comps[i].getShortName();
      if (NamedFrame.getNamedFrame().getFrame(s) == null) 
	runBuilder((ModifiableConfigurableComponent)comps[i], true, true);
    }
  }

  public void runExperimentBuilder(Experiment experiment, boolean alwaysNew,
				   boolean openForEditing) {
    if (!experiment.isEditable() && openForEditing) {
      int result = JOptionPane.showConfirmDialog(this,
				    "Experiment is not editable; create copy?",
				    "Experiment Not Editable",
				    JOptionPane.YES_NO_OPTION,
				    JOptionPane.WARNING_MESSAGE);
      if (result != JOptionPane.YES_OPTION) {
	openForEditing = false;
	//	return;
      } else {
	experiment = organizer.copyExperiment(experiment, null);
      }
    }
    Class[] paramClasses = { CSMART.class, Experiment.class };
    Object[] params = new Object[2];
    params[0] = this;
    params[1] = experiment;
    createTool("Experiment Builder", ExperimentBuilder.class, 
	       alwaysNew, experiment.getExperimentName(), experiment,
	       paramClasses, params);
  }

  /**
   * If an experiment builder is not editing this experiment,
   * then start a new experiment builder to edit this experiment.
   */

  private void runMultipleExperimentBuilders(Experiment[] experiments) {
    for (int i = 0; i < experiments.length; i++) {
      String s = "Experiment Builder: " + experiments[i].getExperimentName();
      if (NamedFrame.getNamedFrame().getFrame(s) == null) 
	runExperimentBuilder(experiments[i], true, true);
    }
  }

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
      enableConsoleTool(false);
      return;
    }
    console = (CSMARTConsole)createNewTool("Experiment Controller",
					   CSMARTConsole.class, 
					   experiment.getExperimentName(),
					   experiment, null, null);
  }

  public void runMonitor() {
    HostComponent[] hosts = getRunningExperiment().getHosts();
    String name = hosts[0].getShortName();
    createTool(MONITOR, CSMARTUL.class, name, null, null, null);
  }

  public void runAnalyzer(Experiment experiment) {
    createTool("Performance Analyzer", Analyzer.class, 
	       experiment.getExperimentName(), experiment, null, null);
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

  public void actionPerformed(ActionEvent e) {
    String s = ((AbstractButton)e.getSource()).getActionCommand();
    if (s.equals(NEW_MENU_ITEM)) {
      newWorkspace();
    } else if (s.equals(EXIT_MENU_ITEM)) {
      exit();
    } else if (s.equals(HELP_MENU_ITEM)) {
      URL help = (URL)getClass().getResource(HELP_DOC);
      if (help != null)
	Browser.setPage(help);
    } else if (s.equals(ABOUT_CSMART_ITEM)) {
      URL about = (URL)getClass().getResource(ABOUT_DOC);
      System.out.println("URL is: " + about);
      if (about != null)
	Browser.setPage(about);
    } else if (s.equals(views[0])) {
      SocietyComponent[] societies = organizer.getSelectedSocieties();
      // try to create a society if none exists
      if (societies == null || societies.length == 0) {
	organizer.addSociety();
	societies = organizer.getSelectedSocieties();
      }
      if (societies.length == 1)
	runBuilder((ModifiableConfigurableComponent)societies[0], false, true);
      else if (societies.length > 1) {
	runMultipleBuilders((ComponentProperties [])societies);
      }
    } else if (s.equals(views[1])) {
    } else if (s.equals(views[2])) {
      Experiment[] experiments = organizer.getSelectedExperiments();
      if (experiments == null || experiments.length == 0) {
	organizer.addExperiment();
	experiments = organizer.getSelectedExperiments();
      }
      if (experiments.length == 1)
	runExperimentBuilder(experiments[0], false, true);
      else if (experiments.length > 1)
	runMultipleExperimentBuilders(experiments);
    } else if (s.equals(views[3])) {
      Experiment[] experiments = organizer.getSelectedExperiments();
      if (experiments.length > 0)
        runConsole(experiments[0]);
    } else if (s.equals(views[4])) {
      runMonitor();
    } else if (s.equals(views[5])) {
      Experiment[] experiments = organizer.getSelectedExperiments();
      if (experiments.length > 0)
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
	    ((PropertyBuilder)tool).reinit((ModifiableConfigurableComponent)argument);
	  else if (tool instanceof Analyzer)
	    ((Analyzer)tool).reinit((Experiment)argument);
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
//       Class[] paramClasses = { CSMART.class };
//       Object[] params = { this };
//       if (argument != null) {
// 	paramClasses = new Class[2];
// 	paramClasses[0] = CSMART.class;
      // this doesn't work if the argument is a SocietyComponent (it picks up the class, not the interface)
// 	paramClasses[1] = argument.getClass(); 
// 	params = new Object[2];
// 	params[0] = this;
// 	params[1] = argument;
//       }
      Constructor constructor = toolClass.getConstructor(paramClasses);
      tool = (JFrame) constructor.newInstance(params);
    } catch (Exception exc) {
      System.out.println("CSMART: " + exc);
      exc.printStackTrace();
      return null;
    }
    if (docName != null)
      toolName = toolName + ": " + docName;
    NamedFrame.getNamedFrame().addFrame(toolName, tool);
    final JFrame frameArg = tool;
    // TODO: do we need to be a window listener
    // we're registerd with NamedFrames to get updates on windows closing
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

  public String getUniqueExperimentName(String name) {
    return organizer.getUniqueExperimentName(name);
  }
  
  public static void launch(String[] args) {
    new CSMART();
  }

}

/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.viewer;

import java.awt.Frame;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.GridLayout;
import java.net.URL;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

// tools created by this user interface

import org.cougaar.tools.csmart.ui.analyzer.Analyzer;
import org.cougaar.tools.csmart.ui.builder.TreeBuilder;
import org.cougaar.tools.csmart.ui.console.CSMARTConsole;
import org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL;
import org.cougaar.tools.csmart.ui.experiment.*;

import org.cougaar.tools.csmart.scalability.ScalabilityXSociety;
import org.cougaar.tools.csmart.ui.component.ConfigurableComponent;
import org.cougaar.tools.csmart.ui.component.HostComponent;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;
import org.cougaar.tools.csmart.ui.monitor.generic.ExtensionFileFilter;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.core.society.Bootstrapper;

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
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String WINDOW_MENU = "Window";
  private static final String HELP_MENU = "Help";

  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "../help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";

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
    "Specify properties of a society.",
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

  public CSMART() {
    setTitle("CSMART");
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.setToolTipText("Create new workspace or quit.");
    JMenuItem newMenuItem = new JMenuItem(NEW_MENU_ITEM);
    newMenuItem.addActionListener(this);
    newMenuItem.setToolTipText("Create a new workspace.");

    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.addActionListener(this);
    exitMenuItem.setToolTipText("Exit");
    fileMenu.add(newMenuItem);
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
      if (exp.length > 0 && exp[0].getSocietyComponentCount() != 0)
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

  public void runBuilder(SocietyComponent cc, boolean alwaysNew) {
    // note that cc is guaranteed non-null when this is called
    // set the society as the selected society because
    // the builder will ask this object for the selected society
    Class[] paramClasses = { CSMART.class, SocietyComponent.class };
    Object[] params = new Object[2];
    params[0] = this;
    params[1] = cc;
    createTool("Configuration Builder", TreeBuilder.class, 
	       alwaysNew, cc.getSocietyName(), cc,
	       paramClasses, params);
  }

  /**
   * If an tree builder is not editing this society,
   * then start a new tree builder to edit this society.
   */

  private void runMultipleBuilders(SocietyComponent[] societies) {
    for (int i = 0; i < societies.length; i++) {
      String s = "Configuration Builder: " + societies[i].getSocietyName();
      if (NamedFrame.getNamedFrame().getFrame(s) == null) 
	runBuilder(societies[i], true);
    }
  }

  public void runExperimentBuilder(Experiment experiment, boolean alwaysNew) {
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
	runExperimentBuilder(experiments[i], true);
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
	runBuilder(societies[0], false);
      else if (societies.length > 1)
	runMultipleBuilders(societies);
    } else if (s.equals(views[1])) {
    } else if (s.equals(views[2])) {
      Experiment[] experiments = organizer.getSelectedExperiments();
      if (experiments == null || experiments.length == 0) {
	organizer.addExperiment();
	experiments = organizer.getSelectedExperiments();
      }
      if (experiments.length == 1)
	runExperimentBuilder(experiments[0], false);
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
	  else if (tool instanceof TreeBuilder)
	    ((TreeBuilder)tool).reinit((SocietyComponent)argument);
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
  
  public static void launch(String[] args) {
    new CSMART();
  }

}

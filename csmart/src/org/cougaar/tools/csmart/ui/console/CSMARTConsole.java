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

package org.cougaar.tools.csmart.ui.console;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.cougaar.tools.csmart.scalability.ScalabilityXSociety;
import org.cougaar.tools.csmart.ui.builder.PropertyEditorPanel;
import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.configuration.ConsoleTreeObject;
import org.cougaar.tools.csmart.ui.experiment.Experiment;
import org.cougaar.tools.csmart.ui.experiment.HostConfigurationBuilder;
import org.cougaar.tools.csmart.ui.experiment.Trial;
import org.cougaar.tools.csmart.ui.experiment.TrialResult;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.viewer.GUIUtils;
import org.cougaar.tools.server.*;
import org.cougaar.tools.server.rmi.ClientCommunityController;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.util.Parameters;

public class CSMARTConsole extends JFrame {
  // org.cougaar.control.port; port for contacting applications server
  public static final int APP_SERVER_DEFAULT_PORT = 8484;
  public static final String NAME_SERVER_PORTS = "8888:5555";
  private static final String[] emptyStringArray = {};
  // number of characters displayed in the node output window
  private static final int DEFAULT_VIEW_SIZE = 50000;
  CSMART csmart; // top level viewer, gives access to save method, etc.
  HostConfigurationBuilder hostConfiguration;
  CommunityServesClient communitySupport;
  String nameServerHostName;
  SocietyComponent societyComponent;
  Experiment experiment;
  boolean isEditable;
  int currentTrial; // index of currently running trial in experiment
  long startTrialTime; // in msecs
  long startExperimentTime;
  DecimalFormat myNumberFormat;
  javax.swing.Timer trialTimer;
  javax.swing.Timer experimentTimer;
  boolean userStoppedTrials = false;
  boolean stopping = false;
  //  boolean aborting = false;
  Hashtable runningNodes; // maps NodeComponents to NodeServesClient
  ArrayList oldNodes; // node components to destroy before running again
  NodeComponent[] nodesToRun; // node components that contain agents to run
  String[] hostsToRunOn;      // hosts that are assigned nodes to run
  ArrayList hostsToUse; // Hosts that are actually having stuff run on them
  Hashtable nodeListeners; // ConsoleNodeListener referenced by node name
  Hashtable nodePanes;     // ConsoleTextPane referenced by node name
  String notifyCondition; // if this appears in node stdout, notify user
  boolean notifyOnStandardError = false; // if stderr appears, notify user
  int viewSize = DEFAULT_VIEW_SIZE; // number of characters in node view
  ConsoleNodeOutputFilter displayFilter;

  // gui controls
  ButtonGroup statusButtons;
  JToggleButton runButton;
  JToggleButton stopButton;
  JToggleButton abortButton;
  JLabel trialNameLabel;
  JProgressBar trialProgressBar; // indicates how many trials have been run
  JPanel buttonPanel; // contains status buttons
  JPopupMenu nodeMenu; // pop-up menu on node status button
  private static Dimension HGAP10 = new Dimension(10,1);
  private static Dimension HGAP5 = new Dimension(5,1);
  private static Dimension VGAP30 = new Dimension(1,30);

  // top level menus and menu items
  private static final String FILE_MENU = "File";
  private static final String EXIT_MENU_ITEM = "Close";
  private static final String VIEW_MENU = "View";
  private static final String SHOW_LOG_MENU_ITEM = "Show Entire Log";
  private static final String SET_VIEW_SIZE_MENU_ITEM = "Set View Size...";
  private static final String FILTER_MENU_ITEM = "Filter...";
  private static final String FORMAT_MENU_ITEM = "Format...";
  private static final String NOTIFY_MENU = "Notify";
  private static final String SET_NOTIFY_MENU_ITEM = "Set Notification...";
  private static final String VIEW_NOTIFY_MENU_ITEM = "View Notification";
  private static final String REMOVE_NOTIFY_MENU_ITEM = "Remove All Notifications";
  private static final String RESET_NOTIFY_MENU_ITEM = "Reset All Notifications";

  private static final String HELP_MENU = "Help";
  private static final String ABOUT_CONSOLE_ITEM = "About Experiment Controller";
  private static final String ABOUT_CSMART_ITEM = "About CSMART";
  private static final String LEGEND_MENU_ITEM = "Legend";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";
  private static final String HELP_DOC = "help.html";


  // for pop-up menu on node status buttons
  private static final String ABOUT_ACTION = "Info";
  private static final String RESET_ACTION = "Reset Notification";

  // used for log file name
  private static DateFormat fileDateFormat =
                  new SimpleDateFormat("yyyyMMddHHmmss");
  private static DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

  private static final int MSECS_PER_SECOND = 1000;
  private static final int MSECS_PER_MINUTE = 60000;
  private static final int MSECS_PER_HOUR = 3600000;
  private Date runStart = null;
  private ConsoleDesktop desktop;
  // node whose status lamp is selected
  // set only when pop-up menu is displayed
  private String selectedNodeName;

  Legend legend; // the node status lamp legend

  /**
   * Create and show console GUI.
   */
  public CSMARTConsole(CSMART csmart) {
    this.csmart = csmart;
    experiment = csmart.getExperiment();
    isEditable = experiment.isEditable();
    experiment.setEditable(false); // not editable while we have it
    currentTrial = -1;
    societyComponent = experiment.getSocietyComponent(0);
    desktop = new ConsoleDesktop();
    setSocietyComponent(societyComponent);
  }

  /**
   * Set the society component for which to display information and run.
   */
  private void setSocietyComponent(SocietyComponent cc) {
    communitySupport = new ClientCommunityController();
    runningNodes = new Hashtable();
    oldNodes = new ArrayList();
    nodeListeners = new Hashtable();
    nodePanes = new Hashtable();

    // top level menus
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.setToolTipText("Exit this tool.");
    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.setToolTipText("Exit this tool.");
    exitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	exitMenuItem_actionPerformed(e);
      }
    });
    fileMenu.add(exitMenuItem);

    JMenu viewMenu = new JMenu(VIEW_MENU);
    JMenuItem viewMenuItem = new JMenuItem(SHOW_LOG_MENU_ITEM);
    viewMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      }
    });
    viewMenuItem.setEnabled(false); // disabled cause of Swing error
    viewMenu.add(viewMenuItem);
    JMenuItem viewSizeMenuItem = new JMenuItem(SET_VIEW_SIZE_MENU_ITEM);
    viewSizeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        viewSizeMenuItem_actionPerformed();
      }
    });
    viewMenu.add(viewSizeMenuItem);
    viewMenu.addSeparator();
    JMenuItem filterMenuItem = new JMenuItem(FILTER_MENU_ITEM);
    filterMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        filterMenuItem_actionPerformed();
      }
    });
    viewMenu.add(filterMenuItem);
    JMenuItem formatMenuItem = new JMenuItem(FORMAT_MENU_ITEM);
    formatMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        formatMenuItem_actionPerformed();
      }
    });
    formatMenuItem.setEnabled(false);
    viewMenu.add(formatMenuItem);

    JMenu notifyMenu = new JMenu(NOTIFY_MENU);
    JMenuItem setNotifyMenuItem = new JMenuItem(SET_NOTIFY_MENU_ITEM);
    setNotifyMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setNotifyMenuItem_actionPerformed();
      }
    });
    notifyMenu.add(setNotifyMenuItem);
    JMenuItem viewNotifyMenuItem = new JMenuItem(VIEW_NOTIFY_MENU_ITEM);
    viewNotifyMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        viewNotifyMenuItem_actionPerformed();
      }
    });
    notifyMenu.add(viewNotifyMenuItem);
    JMenuItem removeNotifyMenuItem = new JMenuItem(REMOVE_NOTIFY_MENU_ITEM);
    removeNotifyMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeNotifyMenuItem_actionPerformed();
      }
    });
    notifyMenu.add(removeNotifyMenuItem);
    notifyMenu.addSeparator();
    JMenuItem resetNotifyMenuItem = new JMenuItem(RESET_NOTIFY_MENU_ITEM);
    resetNotifyMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetNotifyMenuItem_actionPerformed();
      }
    });
    notifyMenu.add(resetNotifyMenuItem);

    JMenu helpMenu = new JMenu(HELP_MENU);
    JMenuItem helpMenuItem = new JMenuItem(ABOUT_CONSOLE_ITEM);
    helpMenuItem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  URL help = (URL)this.getClass().getResource(HELP_DOC);
	  if (help != null)
	    Browser.setPage(help);
	}
      });
    helpMenu.add(helpMenuItem);
    JMenuItem aboutMenuItem = new JMenuItem(ABOUT_CSMART_ITEM);
    aboutMenuItem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  URL about = (URL)this.getClass().getResource(ABOUT_DOC);
	  if (about != null)
	    Browser.setPage(about);
	}
      });
    helpMenu.add(aboutMenuItem);
    legend = new Legend();
    JMenuItem legendMenuItem = new JMenuItem(LEGEND_MENU_ITEM);
    legendMenuItem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
          legend.setVisible(true);
	}
      });
    helpMenu.add(legendMenuItem);
    
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(viewMenu);
    menuBar.add(notifyMenu);
    menuBar.add(helpMenu);
    getRootPane().setJMenuBar(menuBar);

    // create panel which contains
    // description panel, status button panel, and tabbed panes
    JPanel panel = new JPanel(new GridBagLayout());

    // descriptionPanel contains society name, trial name, control buttons
    JPanel descriptionPanel = createHorizontalPanel(true);
    descriptionPanel.add(Box.createRigidArea(VGAP30));
    descriptionPanel.add(Box.createRigidArea(HGAP10));
    descriptionPanel.add(new JLabel(cc.getSocietyName()));
    descriptionPanel.add(Box.createRigidArea(HGAP5));
    descriptionPanel.add(new JLabel("Trial: "));
    descriptionPanel.add(Box.createRigidArea(HGAP5));
    trialNameLabel = new JLabel("");
    descriptionPanel.add(trialNameLabel);
    descriptionPanel.add(Box.createRigidArea(HGAP10));
    
    runButton = new JToggleButton("Run");
    runButton.setToolTipText("Start running experiment");
    runButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	runButton_actionPerformed();
      }
    });
    runButton.setFocusPainted(false);
    descriptionPanel.add(runButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));
    
    stopButton = new JToggleButton("Stop");
    stopButton.setToolTipText("Halt experiment at end of current");
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	stopButton_actionPerformed(e);
      }
    });
    stopButton.setFocusPainted(false);
    descriptionPanel.add(stopButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));

    abortButton = new JToggleButton("Abort");
    abortButton.setToolTipText("Interrupt experiment");
    abortButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	abortButton_actionPerformed();
      }
    });
    abortButton.setFocusPainted(false);
    descriptionPanel.add(abortButton);

    // create progress panel for progress bar and trial and experiment times
    trialProgressBar = new JProgressBar(0, experiment.getTrialCount() + 1);
    trialProgressBar.setValue(0);
    trialProgressBar.setStringPainted(true);
    JPanel progressPanel = new JPanel(new GridBagLayout());
    final JLabel trialTimeLabel = new JLabel("Trial: 00:00:00");
    final JLabel experimentTimeLabel = new JLabel("Experiment: 00:00:00");
    myNumberFormat = new DecimalFormat("00");

    // create trial progress panel for time labels
    JPanel trialProgressPanel = createHorizontalPanel(false);
    if (!experiment.isInDatabase()) {
      trialProgressPanel.add(trialTimeLabel);
      trialProgressPanel.add(Box.createRigidArea(HGAP10));
      progressPanel.add(trialProgressBar,
		      new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
					     GridBagConstraints.WEST,
					     GridBagConstraints.HORIZONTAL,
                                             new Insets(5, 0, 0, 0),
					     0, 0));
    }
    trialProgressPanel.add(experimentTimeLabel);
    progressPanel.add(trialProgressPanel,
		      new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
					     GridBagConstraints.WEST,
					     GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0),
					     0, 0));
    
    descriptionPanel.add(Box.createRigidArea(HGAP10));
    descriptionPanel.add(progressPanel);
    descriptionPanel.add(Box.createRigidArea(HGAP10));
    // add description panel to top panel
    panel.add(descriptionPanel, 
	      new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
				     GridBagConstraints.WEST,
				     GridBagConstraints.HORIZONTAL,
                                     new Insets(0, 10, 2, 10),
				     0, 0));

    // set up trial and experiment timers
    trialTimer = 
      new javax.swing.Timer(1000, new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  trialTimeLabel.setText(getElapsedTimeLabel("Trial: ",
						     startTrialTime));
	}
      });
    experimentTimer = 
      new javax.swing.Timer(1000, new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  experimentTimeLabel.setText(getElapsedTimeLabel("Experiment: ", 
						   startExperimentTime));
	}
      });
    // create status button panel, initially with no buttons
    buttonPanel = createHorizontalPanel(true);
    buttonPanel.add(Box.createRigidArea(HGAP10));
    buttonPanel.add(new JLabel("Node Status"));
    buttonPanel.add(Box.createRigidArea(HGAP10));
    buttonPanel.add(Box.createRigidArea(VGAP30));
    JScrollPane jsp = new JScrollPane(buttonPanel);
    // ensure the layout leaves space for the scrollbar
    jsp.setMinimumSize(new Dimension(100, 50));
    panel.add(jsp,
	      new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
				     GridBagConstraints.WEST,
				     GridBagConstraints.HORIZONTAL,
                                     new Insets(0, 10, 0, 10),
				     0, 0));

    statusButtons = new ButtonGroup();
    nodeMenu = new JPopupMenu();
    Action aboutAction = new AbstractAction(ABOUT_ACTION) {
      public void actionPerformed(ActionEvent e) {
        displayAboutNode();
      }
    };
    nodeMenu.add(aboutAction);
    Action resetAction = new AbstractAction(RESET_ACTION) {
      public void actionPerformed(ActionEvent e) {
        resetNodeStatus();
      }
    };
    Action legendAction = new AbstractAction(LEGEND_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        legend.setVisible(true);
      }
    };
    nodeMenu.add(resetAction);
    nodeMenu.addSeparator();
    nodeMenu.add(legendAction);

    // create tabbed panes for configuration information (not editable)
    hostConfiguration = new HostConfigurationBuilder(experiment, null);
    hostConfiguration.update(); // set host configuration to display 1st trial
    hostConfiguration.addHostTreeSelectionListener(myTreeListener);
    JInternalFrame jif = new JInternalFrame("Configuration",
                                            true, false, true, true);
    jif.getContentPane().add(hostConfiguration);
    jif.setSize(300, 300);
    jif.setLocation(0, 0);
    jif.setVisible(true);
    desktop.add(jif, JLayeredPane.DEFAULT_LAYER);
    if (!experiment.isInDatabase()) {
      PropertyEditorPanel trialViewer = 
        new PropertyEditorPanel(experiment.getComponentsAsArray());
      jif = new JInternalFrame("Trial Values", true, false, true, true);
      jif.getContentPane().add(trialViewer);
      jif.setSize(300, 300);
      jif.setLocation(310, 0);
      jif.setVisible(true);
      desktop.add(jif, JLayeredPane.DEFAULT_LAYER);
    }
    panel.add(desktop,
	      new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
				     GridBagConstraints.WEST,
				     GridBagConstraints.BOTH,
                                     new Insets(0, 0, 0, 0),
				     0, 0));

    getContentPane().add(panel);

    // enable run button if have experiment with at least one host, node,
    // and agent
    initRunButton();
    stopButton.setEnabled(false);
    abortButton.setEnabled(false);

    // add a WindowListener: Do an exit to kill the Nodes
    // If this window is closing
    // Note that the main CSMART UI handles actually disposing
    // this frame
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	updateExperimentEditability();
	exitMenuItem_actionPerformed(e);
      }
    });

    pack();
    setSize(700, 600);
    setVisible(true);
  }

  /**
   * If experiment has trial results it's no longer editable.
   */
  private void updateExperimentEditability() {
    if (isEditable) { // only need to update if was editable
      Trial[] trials = experiment.getTrials();
      for (int i = 0; i < trials.length; i++) {
	TrialResult[] results = trials[i].getTrialResults();
	if (results.length != 0) {
	  isEditable = false;
	  break;
	}
      }
      experiment.setEditable(isEditable);
    }
  }

  /**
   * Create a panel whose components are layed out horizontally.
   */
  private JPanel createHorizontalPanel(boolean makeBorder) {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.setAlignmentY(TOP_ALIGNMENT);
    p.setAlignmentX(LEFT_ALIGNMENT);
    if (makeBorder)
      p.setBorder(LineBorder.createGrayLineBorder());
    return p;
  }


  /**
   * Create a button representing a node.
   */
  private NodeStatusButton createStatusButton(String nodeName, String hostName) {
    NodeStatusButton button =
      new NodeStatusButton(new ColoredCircle(NodeStatusButton.unknownStatus, 20, null));
    button.setSelectedIcon(new SelectedColoredCircle(NodeStatusButton.unknownStatus, 20, null));
    button.setToolTipText("Node " + nodeName + " (" + hostName + "), unknown");
    button.setActionCommand(nodeName);
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setMargin(new Insets(2,2,2,2));
    return button;
  }

  /**
   * Add status button to status button display.
   */
  private void addStatusButton(JRadioButton button) {
    statusButtons.add(button);
    button.addMouseListener(myMouseListener);
    buttonPanel.add(button);
  }

  /**
   * Display pop-up menu with "about" menu item, which provides
   * the same functionality as the "about" menu item in the node window,
   * but from the node status lamp.
   */

  private void doPopup(MouseEvent e) {
    selectedNodeName = ((JRadioButton)e.getSource()).getActionCommand();
    nodeMenu.show((Component)e.getSource(), e.getX(), e.getY());
  }

  private void displayAboutNode() {
    desktop.getNodeFrame(selectedNodeName).displayAbout();
  }

  private void displayNodeFrame(String nodeName) {
    JInternalFrame frame = desktop.getNodeFrame(nodeName);
    if (frame == null)
      return; // frame not created yet
    try {
      frame.setIcon(false);
      frame.setSelected(true);
    } catch (PropertyVetoException exc) {
    }
  }

  private NodeStatusButton getNodeStatusButton(String nodeName) {
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      NodeStatusButton button = (NodeStatusButton)buttons.nextElement();
      if (button.getActionCommand().equals(nodeName))
        return button;
    }
    return null;
  }

  /**
   * Clears the error in the console node listener so it 
   * updates the status button again.
   * Clears the notify position in the console text pane.
   */

  private void resetNodeStatus() {
    ConsoleTextPane consoleTextPane =
      (ConsoleTextPane)nodePanes.get(selectedNodeName);
    consoleTextPane.clearNotify();
    NodeStatusButton button = getNodeStatusButton(selectedNodeName);
    if (button != null)
      button.clearError();
  }

  /**
   * Listener on the node status buttons.
   * Right click pops-up a menu with the "About" node menu item.
   * Left click opens the node standard out frame,
   * and highlights the node in the configuration tree.
   */

  private MouseListener myMouseListener = new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
      if (e.isPopupTrigger()) 
        doPopup(e);
      else {
        String nodeName = ((JRadioButton)e.getSource()).getActionCommand();
        displayNodeFrame(nodeName);
        selectNodeInHostTree(nodeName);
      }
    }
    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }
  };

  /**
   * Enable run button if experiment has at least one host that has at least
   * one node to run which has at least one agent to run.
   */
  private void initRunButton() {
    HostComponent[] hosts = experiment.getHosts();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      if (nodes != null && nodes.length > 0) {
	for (int j = 0; j < nodes.length; j++) {
	  AgentComponent[] agents = nodes[j].getAgents();
	  if (agents != null && agents.length > 0) {
	    runButton.setEnabled(true);
	    return;
	  }
	}
      }
    }
    runButton.setEnabled(false);
  }

  /**
   * User selected "Run" button.
   * Set the next trial values.
   * For each host which is assigned a node,
   * and each node that is assigned an agent,
   * start the node on that host, 
   * and create a status button and
   * tabbed pane for it.
   */

  private void runButton_actionPerformed() {
    destroyOldNodes(); // Get rid of any old stuff before creating the new
    userStoppedTrials = false;
    ArrayList nodesToUse = new ArrayList();
    hostsToUse = new ArrayList(); // hosts that nodes are on
    nameServerHostName = null;
    HostComponent[] hosts = experiment.getHosts();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
	AgentComponent[] agents = nodes[j].getAgents();
	// skip nodes that have no agents
	if (agents == null || agents.length == 0)
	  continue;
	nodesToUse.add(nodes[j]);
	// record host for each node
	hostsToUse.add(hosts[i].getShortName());
      }
    }
    nodesToRun =
      (NodeComponent[])nodesToUse.toArray(new NodeComponent[nodesToUse.size()]);
    hostsToRunOn = 
      (String[])hostsToUse.toArray(new String[hostsToUse.size()]);
    if (hostsToRunOn.length > 0) 
      nameServerHostName = hostsToRunOn[0];

    // this state should be detected earlier and the run button disabled
    if (!haveMoreTrials()) {
      System.out.println("CSMARTConsole: WARNING: no more trials to run");
      runButton.setSelected(false);
      runButton.setEnabled(false);
      return; // nothing to run
    }

    // query user for results directory before running the first time
    getResultDir(); 
    if (nodesToRun.length != 0) 
      runTrial();
  }

  private void runTrial() {
    setTrialValues();
    final CSMARTConsole console = this;
    GUIUtils.timeConsumingTaskStart(this);
    try {
      new Thread("CreateNodes") {
        public void run() {
          createNodes();
          GUIUtils.timeConsumingTaskEnd(console);
        }
      }.start();
    } catch (RuntimeException re) {
      re.printStackTrace();
      GUIUtils.timeConsumingTaskEnd(this);
    }
  }

  /**
   * Create nodes; if no nodes can be run, reset the run button,
   * unset the trial values, decrement the trial counter, and return.
   */

  private void createNodes() {
    // first set configuration file name in all node components
    for (int i = 0; i < nodesToRun.length; i++) {
      NodeComponent nodeComponent = nodesToRun[i];
      ((ConfigurableComponent)nodeComponent).addProperty("ConfigurationFileName",
                                 nodeComponent.getShortName() + currentTrial);
    }
    String tmp =
      Parameters.findParameter("org.cougaar.tools.csmart.startdelay");
    int delay = 1;
    if (tmp != null) {
      try {
        delay = Integer.parseInt(tmp);
      } catch (NumberFormatException nfe) {
      }
    }
    boolean haveRunningNode = false;
    runStart = new Date();
    for (int i = 0; i < nodesToRun.length; i++) {
      try {
        Thread.sleep(delay);
      } catch (Exception e) {
      }
      NodeComponent nodeComponent = nodesToRun[i];
      if (createNode(nodeComponent, hostsToRunOn[i]))
	haveRunningNode = true;
    }
    if (!haveRunningNode) {
      runButton.setSelected(false);
      runButton.setEnabled(true);
      unsetTrialValues();
      currentTrial--;
    }
  }

  private boolean haveMoreTrials() {
    return currentTrial < (experiment.getTrialCount()-1);
  }

  /**
   * Set the trial values in the corresponding properties,
   * and update the trial guis.
   * Check if there are any new unassigned agents and assign them.
   * Remove any agents that no longer exist.
   * Update the configuration view.
   */

  private void setTrialValues() {
    if (currentTrial >= 0)
      unsetTrialValues(); // unset previous trial values
    currentTrial++;  // starts with 0
    Trial trial = experiment.getTrials()[currentTrial];
    trialNameLabel.setText(trial.getShortName());
    trialProgressBar.setValue(currentTrial+1);
    Property[] properties = trial.getParameters();
    Object[] values = trial.getValues();
    for (int i = 0; i < properties.length; i++) 
      properties[i].setValue(values[i]);
    // assign any unassigned agents 
    assignUnassignedAgents();
    // update host-node-agent panel
    hostConfiguration.update();
  }

  /**
   * Called when trial is finished; unset the property values
   * that were used in the trial by setting their value to null.
   */

  private void unsetTrialValues() {
    if (currentTrial < 0)
      return;
    Trial trial = experiment.getTrials()[currentTrial];
    Property[] properties = trial.getParameters();
    for (int i = 0; i < properties.length; i++) 
      properties[i].setValue(null);
  }

  /**
   * Assign any unassigned agents before each trial.
   */

  private void assignUnassignedAgents() {
    // get the nodes in the experiment
    // note that this reconciles node agents to society agents,
    // removing any agents that no longer exist
    NodeComponent[] nodes = experiment.getNodes();
    if (nodes.length == 0)
      return; // no nodes to use
    // get all assigned agents
    ArrayList assignedAgents = new ArrayList();
    HostComponent[] hosts = experiment.getHosts();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodesInHost = hosts[i].getNodes();
      for (int j = 0; j < nodesInHost.length; j++) { 
	assignedAgents.addAll(Arrays.asList(nodesInHost[j].getAgents()));
      }
    }
    // assign all unassigned agents to nodes    
    int nextNode = 0;
    int nNodes = nodes.length;
    AgentComponent[] agents = experiment.getAgents();
    for (int i = 0; i < agents.length; i++) {
      if (!assignedAgents.contains(agents[i])) {
	nodes[nextNode++].addAgent(agents[i]);
	if (nextNode == nNodes)
	  nextNode = 0;
      }
    }
  }

  /**
   * Stop all nodes.
   * If all societies in the experiment are self terminating, 
   * just stop after the current trial (don't start next trial).
   * If any society is not self terminating, determine if the experiment
   * is being monitored, and if so, ask the user to confirm the stop. 
   */
  private void stopButton_actionPerformed(ActionEvent e) {
    stopButton.setSelected(true); // indicate stopping
    stopButton.setEnabled(false); // disable until experiment stops
    int nSocieties = experiment.getSocietyComponentCount();
    boolean isSelfTerminating = true;
    for (int i = 0; i < nSocieties; i++) {
      SocietyComponent society = experiment.getSocietyComponent(i);
      if (!society.isSelfTerminating()) {
	isSelfTerminating = false;
	break;
      }
    }
    // societies in experiment will self terminate
    if (isSelfTerminating) {
      userStoppedTrials = true;
      return;
    }
    // need to manually stop some societies
    // if societies are being monitored, ask user to confirm stop
    if (experiment.isMonitored()) {
      int response = 
	JOptionPane.showConfirmDialog(this,
				      "Experiment is being monitored; stop anyway (society monitor displays will be destroyed)?",
				      "Confirm Stop",
				      JOptionPane.YES_NO_OPTION);
      if (response == JOptionPane.YES_OPTION) 
	stopAllNodes();
    } else
      stopAllNodes();
  }

  /**
   * Abort all nodes, (using NodeServesClient interface which is
   * returned by createNode).
   */
  private void abortButton_actionPerformed() {
    //    aborting = true;
    userStoppedTrials = true;
    stopAllNodes();
  }

  /**
   * Stop all experiments.  Called before exiting CSMART.
   */
  public void stopExperiments() {
    stopAllNodes(); // stop the nodes
    destroyOldNodes(); // kill all their outputs
    unsetTrialValues(); // unset values from last trial
  }

  /**
   * Stop the nodes, but don't kill the tabbed panes.
   * Used to stop trials in societies that aren't self terminating
   * and used to abort trials.
   */
  private void stopAllNodes() {
    // set a flag indicating that we're stopping the trial
    stopping = true;
    Enumeration nodeComponents = runningNodes.keys();
    while (nodeComponents.hasMoreElements()) {
      NodeComponent nodeComponent = 
	(NodeComponent)nodeComponents.nextElement();
      NodeServesClient nsc = (NodeServesClient)runningNodes.get(nodeComponent);
      String nodeName = nodeComponent.getShortName();
      if (nsc == null) {
        System.err.println("Unknown node name: " + nodeName);
	continue;
      }
      try {
        nsc.flushNodeEvents();
        nsc.destroy();
      } catch (Exception ex) {
        System.err.println("Unable to destroy node, assuming it's dead: " + ex);
        nodeStopped(nodeComponent);
        getNodeStatusButton(nodeComponent.getShortName()).setStatus(NodeStatusButton.STATUS_NO_ANSWER);
      }
    }
  }

  // Kill any existing output frames or history charts
  private void destroyOldNodes() {
    Iterator nodeComponents = oldNodes.iterator();
    while (nodeComponents.hasNext()) {
      NodeComponent nodeComponent = (NodeComponent)nodeComponents.next();
      String nodeName = nodeComponent.getShortName();
      removeStatusButton(nodeName);
    }
    oldNodes.clear();
    nodeListeners.clear();
    nodePanes.clear();
    JInternalFrame[] frames = desktop.getAllFrames();
    for (int i = 0; i < frames.length; i++) {
      String s = frames[i].getTitle();
      if (!s.equals("Configuration") && !s.equals("Trial Values"))
        frames[i].dispose();
    }
  }
    
  private void updateExperimentControls(Experiment experiment,
					boolean isRunning) {
    if (!isRunning) {
      experiment.experimentStopped();
      csmart.setRunningExperiment(null);
    } else
      csmart.setRunningExperiment(experiment);
    // update societies
    int nSocieties = experiment.getSocietyComponentCount();
    for (int i = 0; i < nSocieties; i++)
      experiment.getSocietyComponent(i).setRunning(isRunning);

    // if aborting, disable and unselect all controls
//      if (aborting) {
//        runButton.setEnabled(false);
//        runButton.setSelected(false);
//        stopButton.setEnabled(false);
//        stopButton.setSelected(false);
//        abortButton.setEnabled(false);
//        abortButton.setSelected(false);
//        return;
//      }

    // if not running, enable the run button, and don't select it
    // if running, disable the run button and select it
    runButton.setEnabled(!isRunning && haveMoreTrials());
    runButton.setSelected(isRunning);

    // if running, enable the stop button and don't select it
    // if not running, disable the stop button and don't select it
    stopButton.setEnabled(isRunning);
    stopButton.setSelected(false);
    abortButton.setEnabled(isRunning);
    abortButton.setSelected(false);
  }

  /**
   * Called by ConsoleNodeListener when node has stopped;
   * called within the Swing thread.
   * If all nodes are stopped, then trial is stopped.
   * Wait for this method to be called on each node before 
   * starting the next trial
   * Run the next trial.
   * Update the gui controls.
   */
  public void nodeStopped(NodeComponent nodeComponent) {
    if (runningNodes.get(nodeComponent) != null) {
      oldNodes.add(nodeComponent);
      runningNodes.remove(nodeComponent);
    }
    // enable restart command on node output window
    desktop.getNodeFrame(nodeComponent.getShortName()).enableRestart();
    // if any node has stopped, then don't kill the rest of the nodes
    // when any node has stopped, kill the rest of the nodes
    // unless we're already killing the other nodes
    //    if (!stopping) 
    //      stopAllNodes();

    // when all nodes have stopped, save results
    // run the next trial and update the gui controls
    if (runningNodes.isEmpty()) {
      stopping = false;
      trialFinished();
      //      if (aborting) {
        //	experimentFinished();
      //	aborting = false;
      //      } else if (haveMoreTrials()) {
      if (haveMoreTrials()) {
	if (!userStoppedTrials) {
	  destroyOldNodes(); // destroy old guis before starting new ones
	  runTrial(); // run next trial
	} else { // user stopped trials, allow them to run the next one
	  runButton.setSelected(false);
	}
      } else { // no more trials, experiment is done
	experimentFinished();
      }
    }
  }

  /**
   * The trial is finished; stop the timer, save the results,
   * unset the property values used, and update the gui.
   */
  private void trialFinished() {
    trialTimer.stop();
    Collection c = nodeListeners.values();
    for (Iterator i = c.iterator(); i.hasNext(); )
      ((ConsoleNodeListener)i.next()).closeLogFile();
    saveResults();
    //    nodeListeners.clear();
    //    nodePanes.clear();
    updateExperimentControls(experiment, false);
  }

  /**
   * The experiment is finished; disable and deselect the run button;
   * stop the timers; and
   * unset the property values used in the experiment.
   */
  private void experimentFinished() {
    trialProgressBar.setValue(experiment.getTrialCount() + 1);
    updateExperimentControls(experiment, false);
    //    runButton.setEnabled(false);
    runButton.setSelected(false);
    // allow user to run experiment again
    runButton.setEnabled(true);
    // reset trial counter
    currentTrial = -1;
    experimentTimer.stop();
  }


  /**
   * Select status button for node.
   */

  private void selectStatusButton(String nodeName) {
    NodeStatusButton button = getNodeStatusButton(nodeName);
    if (button != null)
      button.setSelected(true);
  }

  /**
   * Remove status button for node if it exists.
   */

  private void removeStatusButton(String nodeName) {
    NodeStatusButton button = getNodeStatusButton(nodeName);
    if (button != null) {
      statusButtons.remove(button);
      buttonPanel.remove(button);
    }
  }

  public static int getAppServerPort(Properties properties) {
    int port = APP_SERVER_DEFAULT_PORT;
    if (properties == null)
      return port;
    try {
      String tmp = properties.getProperty("org.cougaar.control.port");
      if (tmp != null)
        port = Integer.parseInt(tmp);
    } catch (NumberFormatException nfe) {
      // use default port
    }
    return port;
  }

  private Properties getNodeMinusD(NodeComponent nc) {
    Properties result = new Properties();
    Properties props = nc.getArguments();
    for (Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
      String pname = (String) e.nextElement();
      if (pname.equals(HostConfigurationBuilder.COMMAND_ARGUMENTS)) continue;
      result.put(pname, props.getProperty(pname));
    }
    return result;
  }

  private String[] getNodeArguments(NodeComponent nc) {
    Properties props = nc.getArguments();
    String commandArguments =
      props.getProperty(HostConfigurationBuilder.COMMAND_ARGUMENTS);
    if (commandArguments == null) return emptyStringArray;
    StringTokenizer tokens = new StringTokenizer(commandArguments.trim(), "\n");
    String[] result = new String[tokens.countTokens()];
    for (int i = 0; i < result.length; i++) {
      result[i] = tokens.nextToken();
      System.out.println("arg=" + result[i]);
    }
    return result;
  }

  /**
   * Create a node and add a tab and status button for it.
   * Create a node event listener and pass it the status button
   * so that it can update it.
   * Returns true if successful and false otherwise.
   */
  private boolean createNode(NodeComponent nodeComponent, String hostName) {
    String nodeName = nodeComponent.getShortName();
    // create an unique node name to circumvent server problems
    String uniqueNodeName = nodeName;

    // get arguments from NodeComponent and pass them to ApplicationServer
    // note that these properties augment any properties that
    // are passed to the server in a properties file on startup
    Properties properties = getNodeMinusD(nodeComponent);
    String[] args = getNodeArguments(nodeComponent);

    if (!experiment.isInDatabase()) {
      // Can't change the name of the Nodes when running
      // from the database, cause it needs to load those names
      uniqueNodeName = nodeName + currentTrial;
      properties.setProperty("org.cougaar.node.name", uniqueNodeName);
      properties.setProperty("org.cougaar.filename", uniqueNodeName + ".ini");
    } else
      properties.setProperty("org.cougaar.experiment.id", 
                             experiment.getTrialID());
    // update properties on node
    nodeComponent.setArguments(properties); 
    // set configuration file name as node component property; why?
    //    ((ConfigurableComponent)nodeComponent).addProperty("ConfigurationFileName",
    //                              nodeName + currentTrial);
    
    // create a status button
    NodeStatusButton statusButton = createStatusButton(nodeName, hostName);

    ConsoleStyledDocument doc = new ConsoleStyledDocument();
    ConsoleTextPane textPane = new ConsoleTextPane(doc, statusButton);
    JScrollPane scrollPane = new JScrollPane(textPane);

    // create a node event listener to get events from the node
    NodeEventListener listener = null;
    String logFileName = getLogFileName(nodeName);
    try {
      listener = new ConsoleNodeListener(this,
					 nodeComponent,
                                         logFileName,
					 statusButton,
                                         doc);
    } catch (Exception e) {
      System.err.println("Unable to create output for: " + nodeName);
      e.printStackTrace();
      return false;
    }
    if (notifyCondition != null)
      textPane.setNotifyCondition(notifyCondition);
    ((ConsoleStyledDocument)textPane.getStyledDocument()).setBufferSize(viewSize);
    if (notifyOnStandardError)
      statusButton.setNotifyOnStandardError(true);
    if (displayFilter != null)
      ((ConsoleNodeListener)listener).setFilter(displayFilter);
    nodeListeners.put(nodeName, listener);
    nodePanes.put(nodeName, textPane);

    NodeEventFilter filter = new NodeEventFilter(10);
    ConfigurationWriter configWriter = 
      experiment.getConfigurationWriter(nodesToRun);

    // create the node
    NodeServesClient nsc = null;
    try {
      HostServesClient hsc = 
        communitySupport.getHost(hostName, getAppServerPort(properties));
      //      System.out.println("Properties:");
      //      properties.list(System.out);
      nsc = hsc.createNode(uniqueNodeName, properties, args,
                           listener, filter, configWriter);
      if (nsc != null)
	runningNodes.put(nodeComponent, nsc);
    } catch (Exception e) {
       System.out.println("CSMARTConsole: cannot create node: " + nodeName);
       JOptionPane.showMessageDialog(this,
				     "Cannot create node on: " + hostName +
				     "; check that server is running");
       e.printStackTrace();
       return false;
    }

    // only add gui controls if successfully created node
    addStatusButton(statusButton);
    desktop.addNodeFrame(nodeComponent, 
                         (ConsoleNodeListener)listener, 
                         new NodeFrameListener(),
                         scrollPane,
                         statusButton,
                         logFileName,
                         nsc,
                         this);
    updateExperimentControls(experiment, true);
    startTimers();
    return true;
  }

  /** 
   * Called from ConsoleInternalFrame (i.e. from menu on the node's output
   * window) to stop a node.
   * If this is the only node, then it is handled the same
   * as selecting the Abort button on the console.
   */

  public void stopNode(NodeComponent node) {
    if (runningNodes.size() == 1) {
      abortButton_actionPerformed();
      return;
    }
    NodeServesClient nsc = (NodeServesClient)runningNodes.get(node);
    if (nsc == null)
      return;
    try {
      nsc.flushNodeEvents();
      nsc.destroy();
    } catch (Exception ex) {
      System.err.println("Unable to destroy node, assuming it's dead: " + ex);
      // call the method that would have been called when the 
      // ConsoleNodeListener received the node destroyed confirmation
      nodeStopped(node); 
      getNodeStatusButton(node.getShortName()).setStatus(NodeStatusButton.STATUS_NO_ANSWER);
    }
  }

  /**
   * Called from ConsoleInternalFrame (i.e. from menu on the node's output
   * window) to restart the node.
   * If this is the only node, then it is handled the same
   * as selecting the Run button on the console.
   */

  public NodeServesClient restartNode(NodeComponent nodeComponent) {
    if (oldNodes.size() + runningNodes.size() == 1) {
      runButton_actionPerformed();
      return null; // it doesn't matter what we return, the caller is going away
    }
    CommunityServesClient communitySupport = new ClientCommunityController();
    HostServesClient hostServer = null;
    Properties properties = getNodeMinusD(nodeComponent);
    String[] args = getNodeArguments(nodeComponent);
    // get host component by searching hosts for one with this node.
    String hostName = null;
    HostComponent[] hosts = experiment.getHosts();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
        if (nodes[j].equals(nodeComponent)) {
          hostName = hosts[i].getShortName();
          break;
        }
      }
    }
    try {
      hostServer = 
        communitySupport.getHost(hostName, getAppServerPort(properties));
    } catch (Exception e) {
      System.out.println(e);
      return null;
    }
    // close the log file and remove the old node event listener
    String nodeName = nodeComponent.getShortName();
    ConsoleNodeListener listener = 
      (ConsoleNodeListener)nodeListeners.remove(nodeName);
    listener.closeLogFile();
    // reuse old document
    ConsoleStyledDocument doc = listener.getDocument();
    // create a node event listener to get events from the node
    String logFileName = getLogFileName(nodeName);
    try {
      listener = new ConsoleNodeListener(this,
					 nodeComponent,
                                         logFileName,
					 getNodeStatusButton(nodeName),
                                         doc);
    } catch (Exception e) {
      System.err.println("Unable to create output for: " + nodeName);
      e.printStackTrace();
      return null;
    }
    if (displayFilter != null)
      listener.setFilter(displayFilter);
    nodeListeners.put(nodeName, listener);

    NodeServesClient nsc = null;
    try {
      nsc = hostServer.createNode(nodeName, properties, args,
                                  listener, new NodeEventFilter(10), null);
      if (nsc != null)
        runningNodes.put(nodeComponent, nsc);
    } catch (Exception e) {
      System.out.println(e);
    }
    return nsc;
  }

  private void startTimers() {
    startTrialTime = new Date().getTime();
    trialTimer.start();
    if (currentTrial == 0) {
      startExperimentTime = new Date().getTime();
      experimentTimer.start();
    }
  }

  /**
   * This checks all the societies in the experiment to determine if
   * any of them generated this metrics file.
   * Creating a new File from the filename works because acceptFile
   * just looks at the filename.
   */
  private boolean isResultFile(String filename) {
    File thisFile = new java.io.File(filename);
    // FIXME!!! Add stuff to check metrics too!!!
    int n = experiment.getSocietyComponentCount();
    for (int i = 0; i < n; i++) {
      SocietyComponent societyComponent = experiment.getSocietyComponent(i);
      java.io.FileFilter fileFilter = societyComponent.getResultFileFilter();
      if (fileFilter == null)
	continue;
      if(fileFilter.accept(thisFile))
	return true;
    }
    int nrecipes = experiment.getRecipeCount();
    for (int i = 0; i < nrecipes; i++) {
      RecipeComponent recipeComponent = experiment.getRecipe(i);
      if (recipeComponent instanceof MetricComponent) {
        MetricComponent metricComponent = (MetricComponent) recipeComponent;
        java.io.FileFilter fileFilter = metricComponent.getResultFileFilter();
        if (fileFilter != null && fileFilter.accept(thisFile))
          return true;
      }
    }
    return false;
  }

  /**
   * Read remote files and copy to directory specified by experiment.
   */
  private void copyResultFiles(HostServesClient hostInfo,
				String dirname) {
    char[] cbuf = new char[1000];
    try {
      String[] filenames = hostInfo.list("./");
      for (int i = 0; i < filenames.length; i++) {
	if (!isResultFile(filenames[i]))
	  continue;
	File newResultFile = 
	  new File(dirname + File.separator + filenames[i]);
	InputStream is = hostInfo.open(filenames[i]);
	BufferedReader reader = 
	  new BufferedReader(new InputStreamReader(is), 1000);
	BufferedWriter writer =
	  new BufferedWriter(new FileWriter(newResultFile));
	int len = 0;
	while ((len = reader.read(cbuf, 0, 1000)) != -1) {
	  writer.write(cbuf, 0, len);
	}
	reader.close();
	writer.close();
      }
    } catch (Exception e) {
      System.out.println("Analyzer: copyResultFiles: " + e);
      e.printStackTrace();
    }
  }

  /**
   * Get the directory in which to store the metrics file.
   * If no directory is set, then display a file chooser, initted
   * to the cougaar install path, for the user to choose a directory.
   */
  private File getResultDir() {
    File resultDir = experiment.getResultDirectory();
    if (resultDir != null)
      return resultDir;
    String resultDirName = ".";
    try {
      resultDirName = System.getProperty("org.cougaar.install.path");
    } catch (RuntimeException e) {
      // just use default
    }
    if (resultDirName == null)
      resultDirName = ".";
    JFileChooser chooser = new JFileChooser(resultDirName);
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
	public boolean accept (File f) {
	  return f.isDirectory();
	}
	public String getDescription() {return "All Directories";}
      });
    int result = chooser.showDialog(this, "Select Results (Metrics) Storage Directory");
    if (result != JFileChooser.APPROVE_OPTION)
      return null;
    resultDir = chooser.getSelectedFile();
    experiment.setResultDirectory(resultDir);
    return resultDir;
  }

  /**
   * Create a file for the results of this trial.
   * Results file structure is:
   * <ExperimentName>
   *    <TrialName>
   *       Results-<Timestamp>.results
   */
  private void saveResults() {
    if (currentTrial < 0)
      return; // nothing to save
    File resultDir = experiment.getResultDirectory();
    if (resultDir == null)
      return; // can't save, user didn't specify metrics directory
    Trial trial = experiment.getTrials()[currentTrial];
    if (runStart == null)
      runStart = new Date();
    String dirname = resultDir.getAbsolutePath() + File.separatorChar + 
      experiment.getExperimentName() + File.separatorChar +
      trial.getShortName() + File.separatorChar +
      "Results-" + fileDateFormat.format(runStart);
    try {
      File f = new File(dirname);
      // guarantee that directories exist
      if (!f.exists() && !f.mkdirs() && !f.exists()) {
	System.out.println("CSMARTConsole: Could not save results in: " +
			   dirname);
	return;
      }
      String myHostName = InetAddress.getLocalHost().getHostName();
      URL url = new URL("file", myHostName, dirname);
      trial.addTrialResult(new TrialResult(runStart, url));
    } catch (Exception e) {
      System.out.println("Exception creating trial results URL: " + e);
      e.printStackTrace();
    }
    HostComponent[] hosts = experiment.getHosts();
    for (int i = 0; i < hosts.length; i++) {
      String hostName = hosts[i].getShortName();

      // Skip hosts that have no node
      if (! hostsToUse.contains(hostName))
	continue;

      // get host port from first node on this host
      // TODO: note that if the user specified different server ports for
      // different nodes on the same host, this won't work
      Properties properties = null;
      NodeComponent[] nodes = hosts[i].getNodes();
      if (nodes.length != 0) 
        properties = getNodeMinusD(nodes[0]);
      HostServesClient hostInfo = null;
      try {
	hostInfo = 
          communitySupport.getHost(hostName, getAppServerPort(properties));
      } catch (java.rmi.UnknownHostException uhe) {
	JOptionPane.showMessageDialog(this,
				      "Unknown host: " + hostName,
				      "Unknown Host",
				      JOptionPane.WARNING_MESSAGE);
	continue;
      } catch (Exception e) {
	// This happens if you listed random hosts which you don't
	// really intend to talk to
	JOptionPane.showMessageDialog(this,
				      "Cannot save results.  No response from: " + hostName +
				      "; check that server is running.",
				      "No Response From Server",
				      JOptionPane.WARNING_MESSAGE);
	continue;
      }
      copyResultFiles(hostInfo, dirname);
    }
  }
  
  /**
   * Create a log file name which is of the form:
   * agent name + date + .log
   * Create it in the results directory if possible.
   */
  private String getLogFileName(String agentName) {
    String filename = agentName + fileDateFormat.format(runStart) + ".log";
    File resultDir = experiment.getResultDirectory();
    // if user didn't specify results directory, save in local directory
    if (resultDir == null)
      return filename; 
    Trial trial = experiment.getTrials()[currentTrial];
    String dirname = resultDir.getAbsolutePath() + File.separatorChar + 
      experiment.getExperimentName() + File.separatorChar +
      trial.getShortName() + File.separatorChar +
      "Results-" + fileDateFormat.format(runStart);
    try {
      File f = new File(dirname);
      // guarantee that directories exist
      if (!f.exists() && !f.mkdirs() && !f.exists()) 
	return filename;
    } catch (Exception e) {
      System.out.println("Couldn't create results directory: " + e);
      return filename;
    }
    return dirname + File.separatorChar + filename;
  }

  /**
   * Action listeners for top level menus.
   */

  private void exitMenuItem_actionPerformed(AWTEvent e) {
    stopExperiments();
    updateExperimentControls(experiment, false);
    updateExperimentEditability();

    // If this was this frame's exit menu item, we have to remove
    // the window from the list
    // if it was a WindowClose, the parent notices this as well
    if (e instanceof ActionEvent)
      NamedFrame.getNamedFrame().removeFrame(this);
    dispose();
  }

  /**
   * Display dialog to set size of screen buffer for node output.
   * Return user's response.  An value of -1 means display all.
   */

  static int displayViewSizeDialog(int currentViewSize) {
    JPanel bufferEventsPanel = new JPanel();
    JRadioButton allButton = new JRadioButton("All");
    JRadioButton sizeButton = new JRadioButton("Buffer Size");
    JTextField sizeTF = new JTextField(8);
    if (currentViewSize == -1) {
      allButton.setSelected(true);
      sizeButton.setSelected(false);
      sizeTF.setText(String.valueOf(DEFAULT_VIEW_SIZE));
    } else {
      allButton.setSelected(false);
      sizeButton.setSelected(true);
      sizeTF.setText(String.valueOf(currentViewSize));
    }
    ButtonGroup bufferButtonGroup = new ButtonGroup();
    bufferButtonGroup.add(allButton);
    bufferButtonGroup.add(sizeButton);
    bufferEventsPanel.add(allButton);
    bufferEventsPanel.add(sizeButton);
    bufferEventsPanel.add(sizeTF);

    int result = JOptionPane.showConfirmDialog(null,
                                    bufferEventsPanel,
                                    "Node View",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null);
    if (result == JOptionPane.CANCEL_OPTION)
      return currentViewSize; // no change
    int newViewSize = 0;
    if (allButton.isSelected()) {
      newViewSize = -1;
    } else {
      try {
        newViewSize = Integer.parseInt(sizeTF.getText());
      } catch (NumberFormatException e) {
        System.out.println(e);
        return currentViewSize;
      }
    }
    return newViewSize;
  }

  private void viewSizeMenuItem_actionPerformed() {
    viewSize = displayViewSizeDialog(viewSize);
    Enumeration textPanes = nodePanes.elements();
    while (textPanes.hasMoreElements()) {
      JTextPane textPane = (JTextPane)textPanes.nextElement();
      ((ConsoleStyledDocument)textPane.getStyledDocument()).setBufferSize(viewSize);
    }
  }

  /**
   * Set filtering (what node output is displayed) for all nodes.
   * TODO: overwrites any filters on individual node panes; is this
   * what we want (i.e. the last filter set either here or in the node
   * output frame is the filter used)
   */

  private void filterMenuItem_actionPerformed() {
    if (displayFilter == null)
      displayFilter = 
        new ConsoleNodeOutputFilter(this, null, true);
    else
      displayFilter =
        new ConsoleNodeOutputFilter(this, displayFilter.getValues(),
                                    displayFilter.isAllSelected());
    Enumeration listeners = nodeListeners.elements();
    while (listeners.hasMoreElements()) {
      ConsoleNodeListener listener = 
        (ConsoleNodeListener)listeners.nextElement();
      listener.setFilter(displayFilter);
    }
  }

  /**
   * Set formatting (font size, style, color) for all nodes.
   */

  private void formatMenuItem_actionPerformed() {
    ConsoleFontChooser cfc = new ConsoleFontChooser();
    cfc.setVisible(true);
    System.out.println("Font Chooser not implemented yet");
  }

  /**
   * Notify (by coloring status button) when the specified
   * output is received on any node.
   */

  private void setNotifyMenuItem_actionPerformed() {
//      String s = 
//        (String)JOptionPane.showInputDialog(this,
//                                            "Search string:",
//                                            "Notification",
//                                            JOptionPane.QUESTION_MESSAGE,
//                                            null, null, notifyCondition);
//      if (s == null || s.length() == 0)
//        notifyCondition = null;
//      else
//        notifyCondition = s;
    JPanel notifyPanel = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    notifyPanel.add(new JLabel("Search string:"),
                    new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(10, 0, 5, 5),
                                           0, 0));
    JTextField notifyField = 
      new JTextField(20);
    notifyField.setText(notifyCondition);
    notifyPanel.add(notifyField,
                    new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(10, 0, 5, 0),
                                           0, 0));
    x = 0;
    JCheckBox stdErrorCB = 
      new JCheckBox("Notify on Standard Error", notifyOnStandardError);
    notifyPanel.add(stdErrorCB,
                    new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(10, 0, 5, 5),
                                           0, 0));
    int result = JOptionPane.showConfirmDialog(this, notifyPanel, 
                                               "Notification",
                                               JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.CANCEL_OPTION)
      return;
    String s = notifyField.getText();
    if (s == null || s.length() == 0)
      notifyCondition = null;
    else
      notifyCondition = s;
    setNotification();
    notifyOnStandardError = stdErrorCB.isSelected();
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      NodeStatusButton button = (NodeStatusButton)buttons.nextElement();
      button.setNotifyOnStandardError(notifyOnStandardError);
    }
  }

  /**
   * Set notification in all text panes.
   * Called to set or reset notification.
   */

  private void setNotification() {
    Enumeration textPanes = nodePanes.elements();
    while (textPanes.hasMoreElements()) {
      ConsoleTextPane textPane = (ConsoleTextPane)textPanes.nextElement();
      textPane.setNotifyCondition(notifyCondition);
    }
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      NodeStatusButton button = (NodeStatusButton)buttons.nextElement();
        button.clearError();
    }
  }

  /**
   * Display notification string.
   */

  private void viewNotifyMenuItem_actionPerformed() {
    if (notifyCondition == null)
      JOptionPane.showConfirmDialog(this,
                                    "No notification set.",
                                    "Notification",
                                    JOptionPane.PLAIN_MESSAGE,
                                    JOptionPane.OK_OPTION);
    else 
      JOptionPane.showConfirmDialog(this,
              "Notify if any node writes: " + notifyCondition,
                                    "Notification",
                                    JOptionPane.PLAIN_MESSAGE,
                                    JOptionPane.OK_OPTION);
  }

  /**
   * Remove all notifications.
   */

  private void removeNotifyMenuItem_actionPerformed() {
    notifyCondition = null;
    setNotification();
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      NodeStatusButton button = (NodeStatusButton)buttons.nextElement();
        button.clearError();
    }
  }

  /**
   * Reset status for all nodes. Resets the "notify" position
   * in the text pane and resets the error flag of the node status button.
   */
  private void resetNotifyMenuItem_actionPerformed() {
    Enumeration textPanes = nodePanes.elements();
    while (textPanes.hasMoreElements()) {
      ConsoleTextPane textPane = (ConsoleTextPane)textPanes.nextElement();
      textPane.clearNotify();
    }
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      NodeStatusButton button = (NodeStatusButton)buttons.nextElement();
        button.clearError();
    }
  }

  private String getElapsedTimeLabel(String prefix, long startTime) {
    long now = new Date().getTime();
    long timeElapsed = now - startTime;
    long hours = timeElapsed / MSECS_PER_HOUR;
    timeElapsed = timeElapsed - (hours * MSECS_PER_HOUR);
    long minutes = timeElapsed / MSECS_PER_MINUTE;
    timeElapsed = timeElapsed - (minutes * MSECS_PER_MINUTE);
    long seconds = timeElapsed / MSECS_PER_SECOND;
    StringBuffer sb = new StringBuffer(20);
    sb.append(prefix);
    sb.append(myNumberFormat.format(hours));
    sb.append(":");
    sb.append(myNumberFormat.format(minutes));
    sb.append(":");
    sb.append(myNumberFormat.format(seconds));
    return sb.toString();
  }

  /**
   * TreeSelectionListener interface.
   * If user selects an agent in the "Hosts" tree,
   * then pop the pane for that node to the foreground, and
   * select the node status button.
   */
  
  private TreeSelectionListener myTreeListener = new TreeSelectionListener() {
    public void valueChanged(TreeSelectionEvent event) {
      TreePath path = event.getPath();
      if (path == null) return;
      DefaultMutableTreeNode treeNode =
        (DefaultMutableTreeNode)path.getLastPathComponent();
      ConsoleTreeObject cto = (ConsoleTreeObject)treeNode.getUserObject();
      if (!cto.isNode())
        return; // ignore selecting if it's not a node
      String nodeName = 
      ((ConsoleTreeObject)treeNode.getUserObject()).getName();
      displayNodeFrame(nodeName);
      selectStatusButton(nodeName);
    }
  };

  /**
   * Select node in host tree; called when user selects corresponding
   * pane or status button.
   */

  private void selectNodeInHostTree(String nodeName) {
    hostConfiguration.removeHostTreeSelectionListener(myTreeListener);
    hostConfiguration.selectNodeInHostTree(nodeName);
    hostConfiguration.addHostTreeSelectionListener(myTreeListener);
  }

  public static void main(String[] args) {
    CSMARTConsole console = new CSMARTConsole(null);
    // for debugging, create our own society
    SocietyComponent sc = (SocietyComponent)new org.cougaar.tools.csmart.scalability.ScalabilityXSociety();
    console.setSocietyComponent(sc);
  }

  class NodeFrameListener implements InternalFrameListener {
    public void internalFrameClosed(InternalFrameEvent e) {
    }
    public void internalFrameClosing(InternalFrameEvent e) {
    }
    public void internalFrameDeactivated(InternalFrameEvent e) {
    }
    public void internalFrameIconified(InternalFrameEvent e) {
    }
    public void internalFrameOpened(InternalFrameEvent e) {
    }
    public void internalFrameActivated(InternalFrameEvent e) {
      frameSelected(e);
    }
    public void internalFrameDeiconified(InternalFrameEvent e) {
      frameSelected(e);
    }
    /**
     * When frame is selected,
     * select status button and node in configuration tree.
     */
    private void frameSelected(InternalFrameEvent e) {
      ConsoleInternalFrame frame = (ConsoleInternalFrame)e.getInternalFrame();
      NodeComponent node = frame.getNodeComponent();
      String nodeName = node.getShortName();
      selectStatusButton(nodeName);
      selectNodeInHostTree(nodeName);
    }
  }

}

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

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.DefaultStyledDocument;

import com.klg.jclass.chart.JCChart;

import org.cougaar.tools.csmart.scalability.ScalabilityXSociety;
import org.cougaar.tools.csmart.ui.builder.PropertyEditorPanel;
import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.experiment.Experiment;
import org.cougaar.tools.csmart.ui.experiment.HostConfigurationBuilder;
import org.cougaar.tools.csmart.ui.experiment.Trial;
import org.cougaar.tools.csmart.ui.experiment.TrialResult;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.server.*;
import org.cougaar.tools.server.rmi.ClientCommunityController;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.component.ModifiableConfigurableComponent;

public class CSMARTConsole extends JFrame implements ChangeListener {
  // must match port used in org.cougaar.tools.server package
  private static final int DEFAULT_PORT = 8484;

  CSMART csmart; // top level viewer, gives access to save method, etc.
  // Name of the remote registry that contains the runtime information.
  public static final String DEFAULT_SERVER_NAME = "ServerHook";

  JFrame consoleFrame;
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
  boolean aborting = false;
  Hashtable runningNodes; // maps NodeComponents to NodeServesClient
  Hashtable oldNodes; // store old charts till next experiment is started
  Hashtable charts; // maps node name to idle time chart
  Hashtable chartFrames; // maps node name to chart frame
  NodeComponent[] nodesToRun; // node components that contain agents to run
  String[] hostsToRunOn;      // hosts that are assigned nodes to run
  ArrayList hostsToUse; // Hosts that are actually having stuff run on them

  // gui controls
  JTabbedPane tabbedPane;
  ButtonGroup statusButtons;
  JToggleButton runButton;
  JToggleButton stopButton;
  JToggleButton abortButton;
  JLabel trialNameLabel;
  JProgressBar trialProgressBar; // indicates how many trials have been run
  JMenuItem historyMenuItem; // enabled only if experiment is running
  JPanel buttonPanel; // contains status buttons
  public static Dimension HGAP10 = new Dimension(10,1);
  public static Dimension HGAP5 = new Dimension(5,1);
  public static Dimension VGAP30 = new Dimension(1,30);
  Border loweredBorder = new CompoundBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED), new EmptyBorder(5,5,5,5));

  // top level menus and menu items
  private static final String FILE_MENU = "File";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String NODE_MENU = "Node";
  private static final String HISTORY_MENU_ITEM = "Utilization History";
  private static final String STATUS_MENU_ITEM = "Status";
  private static final String LOAD_MENU_ITEM = "Load";
  private static final String DESCRIBE_MENU_ITEM = "Describe";
  private static final String HELP_MENU = "Help";

  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "../help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";

  // status button colors
  public static Color busyStatus = new Color(230, 255, 230); // shades of green
  public static Color highBusyStatus = new Color(175, 255, 175);
  public static Color mediumHighBusyStatus = new Color(0, 255, 0);
  public static Color mediumBusyStatus = new Color(0, 235, 0);
  public static Color mediumLowBusyStatus = new Color(0, 215, 0);
  public static Color lowBusyStatus = new Color(0, 195, 0);
  public static Color idleStatus = new Color(0, 175, 0);
  public static Color errorStatus = new Color(215, 0, 0); // red
  public static Color noAnswerStatus = new Color(245, 245, 0); // yellow
  public static Color unknownStatus = new Color(180, 180, 180); //gray

  // used for log file name
  private static DateFormat fileDateFormat =
                  new SimpleDateFormat("yyyyMMddHHmmss");
  private static DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

  private static final int MSECS_PER_SECOND = 1000;
  private static final int MSECS_PER_MINUTE = 60000;
  private static final int MSECS_PER_HOUR = 3600000;

  private Date runStart = null;
  
  /**
   * Create and show console GUI.
   */
  public CSMARTConsole(CSMART csmart) {
    this.csmart = csmart;
    experiment = csmart.getExperiment();
    isEditable = experiment.isEditable();
    experiment.setEditable(false); // not editable while we have it
    currentTrial = -1;
    // TODO: support experiments with multiple societies
    societyComponent = experiment.getSocietyComponent(0);
    setSocietyComponent(societyComponent);
  }

  /**
   * Set the society component for which to display information and run.
   */
  private void setSocietyComponent(SocietyComponent cc) {
    communitySupport = new ClientCommunityController();
    runningNodes = new Hashtable();
    oldNodes = new Hashtable();
    charts = new Hashtable();
    chartFrames = new Hashtable();

    // top level menus
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.setToolTipText("Save configuration or exit.");
    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.setToolTipText("Exit this tool.");
    exitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	exitMenuItem_actionPerformed(e);
      }
    });
    fileMenu.add(exitMenuItem);

    JMenu nodeMenu = new JMenu(NODE_MENU);
    nodeMenu.setToolTipText("Display information about a node.");
    historyMenuItem = new JMenuItem(HISTORY_MENU_ITEM);
    historyMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	historyMenuItem_actionPerformed(e);
      }
    });
    historyMenuItem.setEnabled(false); // enable when node is run
    historyMenuItem.setToolTipText("Display load at a node vs. time.");
    nodeMenu.add(historyMenuItem);
    JMenuItem statusMenuItem = new JMenuItem(STATUS_MENU_ITEM);
    statusMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	statusMenuItem_actionPerformed(e);
      }
    });
    statusMenuItem.setEnabled(false);
    nodeMenu.add(statusMenuItem);
    JMenuItem loadMenuItem = new JMenuItem(LOAD_MENU_ITEM);
    loadMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	loadMenuItem_actionPerformed(e);
      }
    });
    loadMenuItem.setEnabled(false);
    nodeMenu.add(loadMenuItem);
    JMenuItem describeMenuItem = new JMenuItem(DESCRIBE_MENU_ITEM);
    describeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	describeMenuItem_actionPerformed(e);
      }
    });
    describeMenuItem.setEnabled(false);
    nodeMenu.add(describeMenuItem);

    JMenu helpMenu = new JMenu(HELP_MENU);
    JMenuItem helpMenuItem = new JMenuItem(HELP_MENU_ITEM);
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
    

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(nodeMenu);
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
    runButton.setToolTipText("Start running experiments.");
    runButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	runButton_actionPerformed(e);
      }
    });
    runButton.setFocusPainted(false);
    descriptionPanel.add(runButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));
    
    stopButton = new JToggleButton("Stop");
    stopButton.setToolTipText("Stop running experiments when the current experiment completes.");
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	stopButton_actionPerformed(e);
      }
    });
    stopButton.setFocusPainted(false);
    descriptionPanel.add(stopButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));

    abortButton = new JToggleButton("Abort");
    abortButton.setToolTipText("Stop experiment now and discard results.");
    abortButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	abortButton_actionPerformed(e);
      }
    });
    abortButton.setFocusPainted(false);
    descriptionPanel.add(abortButton);

    // create progress panel for progress bar and trial and experiment times
    trialProgressBar = new JProgressBar(0, experiment.getTrialCount() + 1);
    trialProgressBar.setValue(0);
    trialProgressBar.setStringPainted(true);
    JPanel progressPanel = new JPanel(new GridBagLayout());
    progressPanel.add(trialProgressBar,
		      new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
					     GridBagConstraints.WEST,
					     GridBagConstraints.HORIZONTAL,
					     new Insets(10, 0, 5, 0),
					     0, 0));
    final JLabel trialTimeLabel = new JLabel("Trial: 00:00:00");
    final JLabel experimentTimeLabel = new JLabel("Experiment: 00:00:00");
    myNumberFormat = new DecimalFormat("00");

    // create trial progress panel for time labels
    JPanel trialProgressPanel = createHorizontalPanel(false);
    trialProgressPanel.add(trialTimeLabel);
    trialProgressPanel.add(Box.createRigidArea(HGAP5));
    trialProgressPanel.add(experimentTimeLabel);
    progressPanel.add(trialProgressPanel,
		      new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
					     GridBagConstraints.WEST,
					     GridBagConstraints.HORIZONTAL,
					     new Insets(10, 0, 10, 0),
					     0, 0));
    
    descriptionPanel.add(Box.createRigidArea(HGAP10));
    descriptionPanel.add(progressPanel);
    descriptionPanel.add(Box.createRigidArea(HGAP10));
    // add description panel to top panel
    panel.add(descriptionPanel, 
	      new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
				     GridBagConstraints.WEST,
				     GridBagConstraints.HORIZONTAL,
				     new Insets(10, 10, 10, 10),
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
    panel.add(buttonPanel,
	      new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
				     GridBagConstraints.WEST,
				     GridBagConstraints.HORIZONTAL,
				     new Insets(10, 10, 10, 10),
				     0, 0));

    statusButtons = new ButtonGroup();

    // create tabbed panes for configuration information (not editable)
    JTabbedPane configTabbedPane = new JTabbedPane();
    hostConfiguration = new HostConfigurationBuilder(experiment);
    configTabbedPane.add("Configuration", hostConfiguration);
    configTabbedPane.add("Trial Values", 
			 new PropertyEditorPanel(experiment.getComponentsAsArray()));

    // create tabbed panes for running nodes, tabs are added dynamically
    tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener(this);

    // create split pane to hold config panes on left and output panes on right
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(configTabbedPane);
    splitPane.setRightComponent(tabbedPane);

    panel.add(splitPane,
	      new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
				     GridBagConstraints.WEST,
				     GridBagConstraints.BOTH,
				     new Insets(10, 10, 10, 10),
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
    splitPane.setDividerLocation(400);
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
   * Create a panel whose components are layed out vertically.
   */
  private JPanel createVerticalPanel(boolean threeD) {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setAlignmentY(TOP_ALIGNMENT);
    p.setAlignmentX(LEFT_ALIGNMENT);
    if(threeD) {
      p.setBorder(loweredBorder);
    }
    return p;
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
  private JRadioButton createStatusButton(String nodeName) {
    // use unknown color
    JRadioButton button = 
      new JRadioButton(new ColoredCircle(unknownStatus, 20));
    button.setSelectedIcon(new SelectedColoredCircle(unknownStatus, 20));
    button.setToolTipText(nodeName);
    button.setActionCommand(nodeName);
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setMargin(new Insets(2,2,2,2));
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	statusButton_actionPerformed(e);
      }
    });
    return button;
  }

  /**
   * Add status button to status button display.
   */
  private void addStatusButton(JRadioButton button) {
    statusButtons.add(button);
    buttonPanel.add(button);
  }

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

  public void runButton_actionPerformed(ActionEvent e) {
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
    createNodes();
    // select tabbed pane and status button for first node
    String firstNodeName = nodesToRun[0].getShortName();
    selectTabbedPane(firstNodeName);
    selectStatusButton(firstNodeName);
  }

  /**
   * Create nodes; if no nodes can be run, reset the run button,
   * unset the trial values, decrement the trial counter, and return.
   */

  private void createNodes() {
    boolean haveRunningNode = false;
    runStart = new Date();
    for (int i = 0; i < nodesToRun.length; i++) {
      NodeComponent nodeComponent = nodesToRun[i];
      if (createNode(nodeComponent, nodeComponent.getShortName(), 
		     hostsToRunOn[i]))
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
  public void stopButton_actionPerformed(ActionEvent e) {
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
  public void abortButton_actionPerformed(ActionEvent e) {
    aborting = true;
    stopAllNodes();
  }

  /**
   * Stop all experiments.  Called before exiting CSMART.
   */
  public void stopExperiments() {
    // TODO: kill the Console Node Listeners?
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
	System.out.println("NODE DESTROYED: " + nodeName);
      } catch (Exception ex) {
        System.err.println("Unable to destroy node: " + ex);
	continue;
      }
    }
  }

  // Kill any existing tabbed panes or history charts
  private void destroyOldNodes() {
    Enumeration nodeComponents = oldNodes.keys();
    while (nodeComponents.hasMoreElements()) {
      NodeComponent nodeComponent = 
	(NodeComponent)nodeComponents.nextElement();
      String nodeName = nodeComponent.getShortName();
      removeTabbedPane(nodeName);
      removeStatusButton(nodeName);
      oldNodes.remove(nodeComponent);
    }
    // dispose of all chart frames
    Collection frames = chartFrames.values();
    Iterator iter = frames.iterator();
    while (iter.hasNext()) {
      JFrame frame = (JFrame)iter.next();
      NamedFrame.getNamedFrame().removeFrame(frame);
      frame.dispose();
    }
    chartFrames.clear();
    charts.clear();
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
    if (aborting) {
      runButton.setEnabled(false);
      runButton.setSelected(false);
      stopButton.setEnabled(false);
      stopButton.setSelected(false);
      abortButton.setEnabled(false);
      abortButton.setSelected(false);
      historyMenuItem.setEnabled(false);
      return;
    }

    // if not running, enable the run button, and don't select it
    // if running, disable the run button and select it
    runButton.setEnabled(!isRunning && haveMoreTrials());
    runButton.setSelected(isRunning);

    // if running, enable the stop button and don't select it
    // if not running, disable the stop button and don't select it
    stopButton.setEnabled(isRunning);
    stopButton.setSelected(false);
    abortButton.setEnabled(isRunning);
    historyMenuItem.setEnabled(isRunning);
  }

  /**
   * Called by ConsoleNodeListener when node has stopped.
   * If all nodes are stopped, then trial is stopped.
   * Wait for this method to be called on each node before 
   * starting the next trial
   * Run the next trial.
   * Update the gui controls.
   */
  public void nodeStopped(NodeComponent nodeComponent) {
    oldNodes.put(nodeComponent, runningNodes.get(nodeComponent));
    runningNodes.remove(nodeComponent);
    System.out.println("NODE STOPPED: " + nodeComponent.getShortName());

    // when any node has stopped, kill the rest of the nodes
    // unless we're already killing the other nodes
    if (!stopping) 
      stopAllNodes();
    // when all nodes have stopped, save results
    // run the next trial and update the gui controls
    if (runningNodes.isEmpty()) {
      stopping = false;
      trialFinished();
      if (aborting) {
	experimentFinished();
	aborting = false;
      } else if (haveMoreTrials()) {
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
    saveResults();
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
    runButton.setEnabled(false);
    runButton.setSelected(false);
    experimentTimer.stop();
  }

  /**
   * TreeSelectionListener interface.
   * If user selects an agent in the "Hosts" tree,
   * then pop the tabbed pane for that node to the foreground, and
   * select the node status button.
   */

//   public void valueChanged(TreeSelectionEvent event) {
//     TreePath path = event.getPath();
//     if (path == null) return;
//     DefaultMutableTreeNode treeNode =
//       (DefaultMutableTreeNode)path.getLastPathComponent();
//     ConsoleTreeObject cto = (ConsoleTreeObject)treeNode.getUserObject();
//     if (!cto.isNode())
//       return; // ignore selecting if it's not a node
//     String nodeName = 
//       ((ConsoleTreeObject)treeNode.getUserObject()).getName();
//     // select tabbed pane if one exists
//     selectTabbedPane(nodeName);
//     selectStatusButton(nodeName);
//   }

  /**
   * If user selects a status button, then pop the tabbed pane
   * for that node to the foreground, and select the node in the host
   * tree.
   */

  public void statusButton_actionPerformed(ActionEvent e) {
    String nodeName = ((JRadioButton)e.getSource()).getActionCommand();
    //    selectNodeInHostTree(nodeName);
    selectTabbedPane(nodeName);
    displayStripChart(nodeName);
  }

  /**
   * Display strip chart for node name.
   */

  public void displayStripChart(String nodeName) {
    JFrame f = (JFrame)chartFrames.get(nodeName);
    if (f != null) {
      f.toFront();
      f.setState(Frame.NORMAL);
      return;
    }
    JCChart chart = (JCChart)charts.get(nodeName);
    if (chart == null)
      return;
    final Frame newChartFrame = new StripChartFrame(chart);
    newChartFrame.setTitle(NamedFrame.getNamedFrame().addFrame(nodeName, (JFrame)newChartFrame));
    newChartFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	chartFrames.remove(newChartFrame.getTitle());
	NamedFrame.getNamedFrame().removeFrame((JFrame)newChartFrame);
	newChartFrame.dispose();
      }
    });
    chartFrames.put(nodeName, newChartFrame);
  }

  /**
   * ChangeListener interface (for tabbed panes).
   * If user selects a tab, then select the node in the "Nodes" tree,
   * and select the node's button in the status buttons.
   */

  public void stateChanged(ChangeEvent e) {
    JTabbedPane tabbedPane = (JTabbedPane)e.getSource();
    String nodeName = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
    selectStatusButton(nodeName);
    //    selectNodeInHostTree(nodeName);
    JFrame f = (JFrame)chartFrames.get(nodeName);
    if (f != null) {
      f.toFront();
      f.setState(Frame.NORMAL);
    }
  }

  /**
   * Select node in host tree.
   */

//   private void selectNodeInHostTree(String nodeName) {
//     DefaultTreeModel model = (DefaultTreeModel)hostTree.getModel();
//     DefaultMutableTreeNode root = 
//       (DefaultMutableTreeNode)model.getRoot();
//     TreePath path = null;
//     Enumeration nodes = root.breadthFirstEnumeration();
//     while (nodes.hasMoreElements()) {
//       DefaultMutableTreeNode node = 
// 	(DefaultMutableTreeNode)nodes.nextElement();
//       if (node.getUserObject() instanceof ConsoleTreeObject) {
// 	ConsoleTreeObject cto = (ConsoleTreeObject)node.getUserObject();
// 	if (cto.isNode()) {
// 	  if (cto.getName().equals(nodeName)) {
// 	    path = new TreePath(node.getPath());
// 	    break;
// 	  }
// 	}
//       }
//     }
//     if (path != null) {
//       hostTree.removeTreeSelectionListener(this);
//       hostTree.setSelectionPath(path);
//       hostTree.addTreeSelectionListener(this);
//     }
//   }

  /**
   * Select tabbed pane for node if it exists.
   */

  private void selectTabbedPane(String nodeName) {
    int i = tabbedPane.indexOfTab(nodeName);
    if (i != -1) 
      tabbedPane.setSelectedIndex(i);
  }

  /**
   * Remove tabbed pane for node if it exists.
   * Remove tabbed pane change listener or stateChanged method gets called.
   */

  private void removeTabbedPane(String nodeName) {
    int i = tabbedPane.indexOfTab(nodeName);
    if (i != -1) {
      tabbedPane.removeChangeListener(this);
      tabbedPane.remove(i);
      tabbedPane.addChangeListener(this);
    }
  }

  /**
   * Select status button for node.
   */

  private void selectStatusButton(String nodeName) {
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      JRadioButton button = (JRadioButton)buttons.nextElement();
      if (button.getActionCommand().equals(nodeName)) {
	button.setSelected(true);
	break;
      }
    }
  }

  /**
   * Remove status button for node if it exists.
   */

  private void removeStatusButton(String nodeName) {
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      JRadioButton button = (JRadioButton)buttons.nextElement();
      if (button.getActionCommand().equals(nodeName)) {
	statusButtons.remove(button);
	buttonPanel.remove(button);
	break;
      }
    }
  }

  /**
   * Create a node and add a tab and status button for it.
   * Create a node event listener and pass it the status button
   * so that it can update it.
   * Returns true if successful and false otherwise.
   */
  private boolean createNode(NodeComponent nodeComponent,
			     String nodeName, String hostName) {
    // create an unique node name to circumvent server problems
    String uniqueNodeName = nodeName + currentTrial;
    //    DefaultStyledDocument doc = new DefaultStyledDocument();
    RollingStyledDocument doc = new RollingStyledDocument();
    JTextPane pane = new JTextPane(doc);
    JScrollPane stdoutPane = new JScrollPane(pane);

    // create a status button
    JRadioButton statusButton = createStatusButton(nodeName);

    // create a node event listener to get events from the node
    NodeEventListener listener = null;
    try {
      listener = new ConsoleNodeListener(this,
					 nodeComponent,
					 getLogFileName(nodeName), 
					 doc,
					 statusButton);
    } catch (Exception e) {
      System.err.println("Unable to create output for: " + nodeName);
      e.printStackTrace();
      return false;
    }

    // set up idle chart
    JCChart chart = new StripChart();
    StripChartSource chartDataModel = new StripChartSource(chart);
    ((StripChart)chart).init(chartDataModel);
    ((ConsoleNodeListener)listener).setIdleChart(chart, chartDataModel);
    charts.put(nodeName, chart);

    // create node event filter with no buffering
    // so that idle display is "smooth"
    NodeEventFilter filter = new NodeEventFilter();
    Properties properties = new Properties();
    properties.put("org.cougaar.node.name", uniqueNodeName);
    String nameServerPorts = "8888:5555";
    properties.put("org.cougaar.tools.server.nameserver.ports", nameServerPorts);
    properties.put("org.cougaar.name.server", 
		   nameServerHostName + ":" + nameServerPorts);
    String regName = DEFAULT_SERVER_NAME;
    properties.put("org.cougaar.tools.server.name", DEFAULT_SERVER_NAME);
    int port = 8484;
    String[] args = new String[4];
    args[0] = "-f";
    args[1] = uniqueNodeName + ".ini";
    args[2] = "-controlPort";
    args[3] = Integer.toString(port);
//     System.out.println("CSMARTConsole: creating node with:" + 
// 		       " Host: " + hostName + 
// 		       " Port: " + port +
// 		       " Registry: " + regName +
// 		       " Node: " + nodeName +
// 		       " Args: " + args[0] +
// 		       " Args: " + args[1] +
// 		       " Args: " + args[2] +
// 		       " Args: " + args[3] +
// 		       " Listener: " + listener +
// 		       " Filter: " + filter +
// 		       " NodeComponent: " + nodeComponent);
//     System.out.println(" Properties: ");
//     properties.list(System.out);

    // set configuration file names in nodesToRun
    for (int i = 0; i < nodesToRun.length; i++) 
      ((ConfigurableComponent)nodesToRun[i]).addProperty("ConfigurationFileName", 
				nodesToRun[i].getShortName() + currentTrial);
    // write the configuration files
    ConfigurationWriter configWriter = 
      experiment.getConfigurationWriter(nodesToRun);
    try {
      configWriter.writeConfigFiles(new File("."));
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
				    "Cannot write configuration on: " + 
				    hostName +
				    "; check that server is running");
      e.printStackTrace();
      return false;
    }

    // create the node
    try {
      HostServesClient hsc = communitySupport.getHost(hostName, port);
      NodeServesClient nsc = hsc.createNode(uniqueNodeName, properties, args, 
      					    listener, filter, configWriter);
      if (nsc != null)
	runningNodes.put(nodeComponent, nsc);
      System.out.println("CREATED NODE: " + nodeName);
    } catch (Exception e) {
       System.out.println("CSMARTConsole: cannot create node: " + nodeName);
       JOptionPane.showMessageDialog(this,
				     "Cannot create node on: " + hostName +
				     "; check that server is running");
       e.printStackTrace();
       return false;
    }
    
    // only add gui controls if successfully created node
    tabbedPane.add(nodeName, stdoutPane);
    addStatusButton(statusButton);
    updateExperimentControls(experiment, true);
    startTimers();
    return true;
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
//      int n = experiment.getMetricCount();
//      for (int i = 0; i < n; i++) {
//        MetricComponent metricComponent = experiment.getMetric(i);
//        java.io.FileFilter fileFilter = metricComponent.getResultFileFilter();
//        if (fileFilter == null)
//  	continue;
//        if(fileFilter.accept(thisFile))
//  	return true;
//      }
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
	//	System.out.println("CSMARTConsole: saving results in: " +
	//			   dirname + File.separatorChar + 
	//			   filenames[i].substring(2));
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
      
      HostServesClient hostInfo = null;
      try {
	hostInfo = communitySupport.getHost(hostName, DEFAULT_PORT);
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
				      "No response from host: " + hostName +
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
   */
  private String getLogFileName(String agentName) {
    return agentName + fileDateFormat.format(runStart) + ".log";
  }

  /**
   * Action listeners for top level menus.
   */
  public void exitMenuItem_actionPerformed(AWTEvent e) {
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

  public void historyMenuItem_actionPerformed(ActionEvent e) {
    // get selected node from tab
    // tree, tabs, and status buttons should be in sync
    int index = tabbedPane.getSelectedIndex();
    if (index == -1)
      return;
    String nodeName = tabbedPane.getTitleAt(index);
    displayStripChart(nodeName);
  }

  public void statusMenuItem_actionPerformed(ActionEvent e) {
  }

  public void loadMenuItem_actionPerformed(ActionEvent e) {
  }

  public void describeMenuItem_actionPerformed(ActionEvent e) {
  }

  public String getElapsedTimeLabel(String prefix, long startTime) {
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

  public static void main(String[] args) {
    CSMARTConsole console = new CSMARTConsole(null);
    // for debugging, create our own society
    SocietyComponent sc = (SocietyComponent)new org.cougaar.tools.csmart.scalability.ScalabilityXSociety();
    console.setSocietyComponent(sc);
  }

}

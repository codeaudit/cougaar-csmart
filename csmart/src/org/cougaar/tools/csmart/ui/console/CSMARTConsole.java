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

import org.cougaar.core.component.ComponentDescription;
import org.cougaar.mlm.ui.glsinit.GLSClient;
import org.cougaar.tools.server.*;
import org.cougaar.util.Parameters;
import org.cougaar.util.log.Logger;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.experiment.Trial;
import org.cougaar.tools.csmart.experiment.TrialResult;
import org.cougaar.tools.csmart.recipe.MetricComponent;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.configbuilder.PropertyEditorPanel;
import org.cougaar.tools.csmart.ui.experiment.HostConfigurationBuilder;
import org.cougaar.tools.csmart.ui.tree.ConsoleTreeObject;
import org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.viewer.GUIUtils;
import org.cougaar.tools.csmart.ui.util.ClientServletUtil;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.util.ResultsFileFilter;

/**
 * The Console or Experiment Controller is the tool for 
 * starting, stopping, and monitoring
 * a running Cougaar society.
 **/
public class CSMARTConsole extends JFrame {
  private static final String DEFAULT_BOOTSTRAP_CLASS = "org.cougaar.bootstrap.BootStrapper";
  private static final String DEFAULT_NODE_CLASS = "org.cougaar.core.node.Node";
  public static final String COMMAND_ARGUMENTS = "Command$Arguments";
  private static final String[] emptyStringArray = {};

  // number of characters displayed in the node output window
  private static final int DEFAULT_VIEW_SIZE = 300000; // 60 pages of text or 300K

  private String GLS_PROTOCOL = "http"; // default protocol for GLSClient
  private String GLS_SECURE_PROTOCOL = "https";

  // Servlet to look for in initializing GLS window
  private static final String GLS_SERVLET = "org.cougaar.mlm.plugin.organization.GLSInitServlet";
  private CSMART csmart;
  private HostConfigurationBuilder hostConfiguration = null;
  private RemoteHostRegistry remoteHostRegistry;
  private SocietyComponent societyComponent;
  private Experiment experiment;
  private int currentTrial; // index of currently running trial in experiment
  private long startTrialTime; // in msecs
  private long startExperimentTime;
  private DecimalFormat myNumberFormat;
  private javax.swing.Timer trialTimer;
  private javax.swing.Timer experimentTimer;
  private boolean userStoppedTrials = false;
  private boolean stopping = false; // user is stopping the experiment
  private Hashtable runningNodes; // maps node names to RemoteProcesses
  private Object runningNodesLock = new Object();
  private ArrayList oldNodes; // names of nodes to destroy before running again
  private Hashtable nodeListeners; // map node name to ConsoleNodeListener
  private Hashtable nodePanes;     // map node name to ConsoleTextPane 
  private Hashtable nodeToNodeInfo; // map node name to NodeInfo
  private String notifyCondition = "exception"; 
  private boolean notifyOnStandardError = false; // if stderr appears, notify user
  private int viewSize = DEFAULT_VIEW_SIZE; // number of characters in node view
  private ConsoleNodeOutputFilter displayFilter;
  private Date runStart = null;
  private ConsoleDesktop desktop;
  private String selectedNodeName; // node whose status lamp is selected
  private Legend legend; // the node status lamp legend
  private CSMARTConsole console;
  private GLSClient glsClient = null;
  private AppServerList appServers; // array of AppServerDescription
  private transient Logger log;

  // gui controls
  ButtonGroup statusButtons;
  JToggleButton attachButton;
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
  private static final String FIND_MENU = "Find";
  private static final String FIND_HOST_MENU_ITEM = "Find Host...";
  private static final String FIND_NODE_MENU_ITEM = "Find Node...";
  private static final String FIND_AGENT_MENU_ITEM = "Find Agent...";
  private static final String APP_SERVER_MENU = "Application Server";
  private static final String VIEW_APP_SERVER_ITEM = "View";
  private static final String ADD_APP_SERVER_ITEM = "Add...";
  private static final String DELETE_APP_SERVER_ITEM = "Delete...";
  private static final String REFRESH_APP_SERVER_ITEM = "Refresh";
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

  /**
   * Create and show console GUI.
   * Experiment may be null.
   * @param csmart the CSMART viewer
   * @param experiment experiment to run, may be null
   */
  public CSMARTConsole(CSMART csmart, Experiment experiment) {
    this.csmart = csmart;
    this.experiment = experiment;
    createLogger();
    console = this;
    currentTrial = -1;
    remoteHostRegistry = RemoteHostRegistry.getInstance();
    runningNodes = new Hashtable();
    oldNodes = new ArrayList();
    nodeListeners = new Hashtable();
    nodePanes = new Hashtable();
    nodeToNodeInfo = new Hashtable();
    appServers = new AppServerList();
    if (experiment != null)
      getAppServersFromExperiment();
    initGui();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  private void initGui() {
    String description = "";
    if (experiment != null) {
      experiment.setRunInProgress(true);
      experiment.setResultDirectory(csmart.getResultDir());
      description = experiment.getSocietyComponent().getSocietyName();
    }
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

    JMenu findMenu = new JMenu(FIND_MENU);
    JMenuItem findHostMenuItem = new JMenuItem(FIND_HOST_MENU_ITEM);
    findHostMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hostConfiguration.findHost();
      }
    });
    findMenu.add(findHostMenuItem);
    JMenuItem findNodeMenuItem = new JMenuItem(FIND_NODE_MENU_ITEM);
    findNodeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hostConfiguration.findNode();
      }
    });
    findMenu.add(findNodeMenuItem);
    JMenuItem findAgentMenuItem = new JMenuItem(FIND_AGENT_MENU_ITEM);
    findAgentMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hostConfiguration.findAgent();
      }
    });
    findMenu.add(findAgentMenuItem);
    if (experiment == null) {
      findHostMenuItem.setEnabled(false);
      findNodeMenuItem.setEnabled(false);
      findAgentMenuItem.setEnabled(false);
    }

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

    JMenu appServerMenu = new JMenu(APP_SERVER_MENU);
    appServerMenu.setToolTipText("Display, add, and delete list of Application Servers.");
    JMenuItem displayMenuItem = new JMenuItem(VIEW_APP_SERVER_ITEM);
    displayMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          displayAppServers();
        }
      });
    displayMenuItem.setToolTipText("Display list of Application Servers.");
    appServerMenu.add(displayMenuItem);
    JMenuItem addMenuItem = new JMenuItem(ADD_APP_SERVER_ITEM);
    addMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addAppServer();
        }
      });
    addMenuItem.setToolTipText("Add an Application Server.");
    appServerMenu.add(addMenuItem);
    JMenuItem deleteMenuItem = new JMenuItem(DELETE_APP_SERVER_ITEM);
    deleteMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          deleteAppServers();
        }
      });
    deleteMenuItem.setToolTipText("Ignore Application Servers.");
    appServerMenu.add(deleteMenuItem);
    JMenuItem refreshMenuItem = new JMenuItem(REFRESH_APP_SERVER_ITEM);
    refreshMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          refreshAppServers();
        }
      });
    refreshMenuItem.setToolTipText("Refresh list of Application Servers");
    appServerMenu.add(refreshMenuItem);

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
    menuBar.add(findMenu);
    menuBar.add(notifyMenu);
    menuBar.add(appServerMenu);
    menuBar.add(helpMenu);
    getRootPane().setJMenuBar(menuBar);

    // create panel which contains
    // description panel, status button panel, and tabbed panes
    JPanel panel = new JPanel(new GridBagLayout());

    // descriptionPanel contains society name, trial name, control buttons
    JPanel descriptionPanel = createHorizontalPanel(true);
    descriptionPanel.add(Box.createRigidArea(VGAP30));
    descriptionPanel.add(Box.createRigidArea(HGAP10));
    descriptionPanel.add(new JLabel(description));
    descriptionPanel.add(Box.createRigidArea(HGAP5));
    trialNameLabel = new JLabel("");
    //    descriptionPanel.add(new JLabel("Trial: "));
    //    descriptionPanel.add(Box.createRigidArea(HGAP5));
    //    descriptionPanel.add(trialNameLabel);
    //    descriptionPanel.add(Box.createRigidArea(HGAP10));

    attachButton = new JToggleButton("Attach");
    attachButton.setToolTipText("Attach to running nodes");
    attachButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	attachButton_actionPerformed();
      }
    });
    attachButton.setFocusPainted(false);
    // TODO: enable this when attaching is implemented
    attachButton.setEnabled(false);
    descriptionPanel.add(attachButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));
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

    // create trial progress panel for time labels
    // these are referenced elsewhere, so are created even if not displayed
    JPanel trialProgressPanel = createHorizontalPanel(false);
    final JLabel experimentTimeLabel = new JLabel("Experiment: 00:00:00");
    final JLabel trialTimeLabel = new JLabel("Trial: 00:00:00");
    myNumberFormat = new DecimalFormat("00");
    int n = 1;
    if (experiment != null)
      n = experiment.getTrialCount()+1;
    trialProgressBar = new JProgressBar(0, n);

    // only when not in database
    // create progress panel for progress bar and trial and experiment times
    //    trialProgressBar.setValue(0);
    //    trialProgressBar.setStringPainted(true);
    //    trialProgressPanel.add(trialTimeLabel);
    //    trialProgressPanel.add(Box.createRigidArea(HGAP10));
    //    progressPanel.add(trialProgressBar,
    //        new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
    //  			     GridBagConstraints.WEST,
    //  			     GridBagConstraints.HORIZONTAL,
    //                               new Insets(5, 0, 0, 0), 0, 0));
    JPanel progressPanel = new JPanel(new GridBagLayout());
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

    desktop = new ConsoleDesktop();
    // create tabbed panes for configuration information (not editable)
    if (experiment != null) {
      hostConfiguration = new HostConfigurationBuilder(experiment, null);
      hostConfiguration.update(); // display first trial
      hostConfiguration.addHostTreeSelectionListener(myTreeListener);
      JInternalFrame jif = new JInternalFrame("Configuration",
                                              true, false, true, true);
      jif.getContentPane().add(hostConfiguration);
      jif.setSize(660, 400);
      jif.setLocation(0, 0);
      jif.setVisible(true);
      desktop.add(jif, JLayeredPane.DEFAULT_LAYER);
    }
    // only when not in database
//        PropertyEditorPanel trialViewer = 
//          new PropertyEditorPanel(experiment.getComponentsAsArray(), false);
//        jif = new JInternalFrame("Trial Values", true, false, true, true);
//        jif.getContentPane().add(trialViewer);
//        jif.setSize(300, 300);
//        jif.setLocation(310, 0);
//        jif.setVisible(true);
//        desktop.add(jif, JLayeredPane.DEFAULT_LAYER);
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
	exitMenuItem_actionPerformed(e);
      }
    });

    pack();
    setSize(700, 600);
    setVisible(true);
  } // end initGui

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
    ConsoleInternalFrame frame = desktop.getNodeFrame(selectedNodeName);
    if (frame != null)
      frame.displayAbout();
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
    if (consoleTextPane == null)
      return;
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
   * Called from initialization.
   */
  private void initRunButton() {
    if (experiment != null) {
      HostComponent[] hosts = experiment.getHostComponents();
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
    }
    runButton.setEnabled(false);
  }

  /**
   * User selected "Run" button.
   * Set the next trial values.
   * Start each node
   * and create a status button and
   * tabbed pane for it.
   */

  private void runButton_actionPerformed() {
    if (log.isDebugEnabled()) {
      log.debug("Hit run button");
    }
    destroyOldNodes(); // Get rid of any old stuff before creating the new
    userStoppedTrials = false;
    if (experiment != null)
      initNodesFromExperiment();
    runTrial();
  }

  /**
   * If the console was invoked with a non-null experiment
   * then extract NodeInfo from its nodes and save it
   * in the nodeToNodeInfo hashtable which is used to run the nodes.
   */
  private void initNodesFromExperiment() {
    HostComponent[] hostsToRunOn = experiment.getHostComponents();
    for (int i = 0; i < hostsToRunOn.length; i++) {
      String hostName = hostsToRunOn[i].getShortName();
      NodeComponent[] nodesToRun = hostsToRunOn[i].getNodes();
      for (int j = 0; j < nodesToRun.length; j++) {
        NodeComponent nodeComponent = nodesToRun[j];
        String nodeName = nodeComponent.getShortName();

        // get arguments from NodeComponent and pass them to ApplicationServer
        // note that these properties augment any properties that
        // are passed to the server in a properties file on startup
        Properties properties = getNodeMinusD(nodeComponent, hostName);
        java.util.List args = getNodeArguments(nodeComponent);

	if (experiment.getTrialID() != null)
	  properties.setProperty(Experiment.EXPERIMENT_ID, 
                                 experiment.getTrialID());
	else {
	  log.error("Null trial ID for experiment!");
	}
        int remotePort = getAppServerPort(properties);
        RemoteHost appServer = getAppServer(hostName, remotePort);
        if (appServer == null)
          continue;
        nodeToNodeInfo.put(nodeName, 
                           new NodeInfo(appServer,
                                        nodeName, hostName, properties, args));
        // if not running from database, need to write config files
        // writeConfigFiles()
      }
    }
  }

  // for experiments that are not run from a database
  // needs to be defined to be called from initNodesFromExperiment
//    private void writeConfigFiles() {
//      Iterator fileIter = experiment.getConfigFiles(nodesToRun);
//      RemoteFileSystem remoteFS;
//      try {
//        remoteFS = remoteAppServer.getRemoteFileSystem();
//      } catch (Exception e) {
//        if(log.isErrorEnabled()) {
//          log.error("CSMARTConsole: unable to access app-server file system on " + 
//                    hostName + " : " + appServerPort, e);
//        }
//        JOptionPane.showMessageDialog(this,
//                                      "Unable to access app-server file system on " + 
//                                      hostName + " : " + appServerPort +
//                                      "; check that server is running");
//        continue;
//      }

//      while(fileIter.hasNext()) {
//        String filename = (String)fileIter.next();
//        OutputStream out = null;
//        try {
//          out = remoteFS.write(filename);
//          experiment.writeContents(filename, out);
//        } catch(Exception e) {
//          if(log.isErrorEnabled()) {
//            log.error("Caught an Exception writing leaf on " +
//                      hostName + " : " + appServerPort, e);
//          }
//        } finally {
//          try {
//            out.close();
//          } catch(Exception e) {
//            if(log.isErrorEnabled()) {
//              log.error("Caught exception closing stream", e);
//            }
//          }
//        }
//      }
//    }

  private Thread nodeCreator;
  private volatile boolean stopNodeCreation;
  private volatile boolean starting; // true while we're creating nodes
  private ArrayList nodeCreationInfoList;

  /**
   * Run nodes using info in nodeToNodeInfo hashtable,
   * which maps node names to NodeInfo objects.
   */
  private void runTrial() {
    if (log.isDebugEnabled()) {
      log.debug("runTrial about to setTrialValues");
    }
    runStart = new Date(); // set now, cause it's used in logfilename
    if (experiment != null)
      setTrialValues();
    nodeCreationInfoList = new ArrayList();
    Collection values = nodeToNodeInfo.values();
    for (Iterator i = values.iterator(); i.hasNext(); ) {
      NodeInfo ni = (NodeInfo)i.next();
      prepareToCreateNode(ni.appServer, ni.nodeName, ni.hostName,
                          ni.properties, ni.args);
    }
    stopNodeCreation = false;
    startTimers();
    if (log.isDebugEnabled()) {
      log.debug("runTrial about to start createNodes thread");
    }

    nodeCreator = new Thread("CreateNodes") {
      public void run() {
        createNodes();
        starting = false;
        // reset controls if no nodes started successfully
        boolean reset = false;
        synchronized (runningNodesLock) {
          if (runningNodes.isEmpty()) 
            reset = true;
        } // end synchronized
        if (reset) {
          SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                runButton.setSelected(false);
                runButton.setEnabled(true);
                unsetTrialValues();
                currentTrial--;
              }
            });
        } else {
          // create and display iconified GLSClient
          // if its servlet exists
	  final String glsAgent = findServlet();
          if(glsAgent != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  JInternalFrame jif = 
                    new JInternalFrame("GLS", true, false, true, true);
                  glsClient = new GLSClient(getOPlanAgentURL(glsAgent));
                  jif.getContentPane().add(glsClient);
                  jif.setSize(350, 350);
                  jif.setLocation(0, 0);
                  jif.setVisible(true);
                  desktop.add(jif, JLayeredPane.DEFAULT_LAYER);
                  try {
                    jif.setIcon(true);
                  } catch (PropertyVetoException e) {
                  }
                }
              });
          }
	}
      }
    }; // end node creating thread


    starting = true;
    if (log.isDebugEnabled()) {
      log.debug("runTrial about to create Nodes");
    }
    try {
      nodeCreator.start();
    } catch (RuntimeException re) {
      if(log.isErrorEnabled()) {
        log.error("Exception", re);
      }
    }
  }

  /**
   * Use experiment component data tree to find plugins which are servlets.
   * TODO: shouldn't have to hard code servlet class names;
   * should be able to get servlet class to use from client
   */

  private String findServlet() {
    if (experiment == null)
      return null;
    ArrayList componentData = new ArrayList();
    ComponentData societyData = experiment.getSocietyComponentData();
    ComponentData[] cdata = societyData.getChildren();
    for (int i = 0; i < cdata.length; i++) 
      componentData.add(cdata[i]);
    for (int i = 0; i < componentData.size(); i++) {
      Object o = componentData.get(i);
      if (o instanceof AgentComponentData) {
	AgentComponentData acd = (AgentComponentData)o;
        if (hasServlet(acd)) {
	  String name = acd.getName().substring(acd.getName().lastIndexOf('.') + 1);
	  if (log.isDebugEnabled()) {
	    log.debug("Found GLSServlet in agent " + name);
	  }
          return name;
	}
      } else {
        ComponentData[] tmp = ((ComponentData)o).getChildren();
        for (int j = 0; j < tmp.length; j++) 
          componentData.add(tmp[j]);
      }
    }
    return null;
  }

  /**
   * Find the GLS servlet.
   */

  private boolean hasServlet(AgentComponentData cdata) {
    String[] names = cdata.getPluginNames();
    for (int i = 0; i < names.length; i++) {
      // Could the name ever add the OPLAN? In which case, we should
      // do an indexOf != -1
      if (names[i].endsWith(GLS_SERVLET))
        return true;
    }
    return false;
  }

  /**
   * Get the agent URL for the agent named "NCA" in the experiment.
   */
  private String getOPlanAgentURL(String agent) {
    if (agent == null || agent.equals(""))
      agent = "NCA";
    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
        AgentComponent[] agents = nodes[j].getAgents();
        for (int k = 0; k < agents.length; k++) {
          if (agents[k].getShortName().equals(agent))
            return getURL(hosts[i].getShortName(), agent, 
                          nodes[j].getArguments());
        }
      }
    }
    return null;
  }

  /**
   * Look for a definition of the https port, and if it exists, 
   * use https and the port number;
   * else look for a definition of http port, and if it exists,
   * use http and the port number;
   * else use http and the default port number.
   */
  private String getURL(String hostName, String agentName,
                        Properties arguments) {
    String defaultURL = 
      GLS_PROTOCOL + "://" + hostName + ":" + CSMARTUL.agentPort + "/$" + agentName;
    if (arguments == null)
      return defaultURL;
    String s = arguments.getProperty(CSMARTUL.AGENT_HTTPS_PORT);
    int port = 0;
    if (s != null) {
      try {
        port = Integer.parseInt(s);
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception parsing " + CSMARTUL.AGENT_HTTPS_PORT 
                    + " : ", e);
        }
        return defaultURL;
      }
      return GLS_SECURE_PROTOCOL + "://" + hostName + ":" + port + "/$" + agentName;
    }
    s = arguments.getProperty(CSMARTUL.AGENT_HTTP_PORT);
    if (s != null) {
      try {
        port = Integer.parseInt(s);
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception parsing " + CSMARTUL.AGENT_HTTP_PORT);
        }
        return defaultURL;
      }
      return GLS_PROTOCOL + "://" + hostName + ":" + port + "/$" + agentName;
    }
    return defaultURL;
  }

  private boolean haveMoreTrials() {
    if (experiment == null)
      return false;
    else
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
    if (log.isDebugEnabled()) {
      log.debug("setTrialValues about to loop through trial properties to set");
    }
    for (int i = 0; i < properties.length; i++) {
      if (properties[i].getValue() == null || ! properties[i].getValue().equals(values[i]))
	properties[i].setValue(values[i]);
    }
  }

  /**
   * Called when trial is finished; unset the property values
   * that were used in the trial by setting their value to null.
   */
  private void unsetTrialValues() {
    if (experiment == null)
      return;
    if (currentTrial < 0)
      return;
    Trial trial = experiment.getTrials()[currentTrial];
    Property[] properties = trial.getParameters();
    for (int i = 0; i < properties.length; i++) 
      properties[i].setValue(null);
  }

  /**
   * Stop all nodes.
   * If the society in the experiment is self terminating, 
   * just stop after the current trial (don't start next trial),
   * otherwise stop immediately (same as abort).
   */
  private void stopButton_actionPerformed(ActionEvent e) {
    stopButton.setSelected(true); // indicate stopping
    stopButton.setEnabled(false); // disable until experiment stops
    userStoppedTrials = true; // if self terminating, don't start next trial
    stopAllNodes(); // all nodes must be stopped manually for now
//      SocietyComponent society = experiment.getSocietyComponent();
//      if (society != null && !society.isSelfTerminating())
//        stopAllNodes(); // manually stop society immediately
  }

  /**
   * Abort all nodes, (using the RemoteProcesses interface which is
   * returned by the app-server's "createRemoteProcess(..)").
   */
  private void abortButton_actionPerformed() {
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
   * Stop the nodes, but don't kill the node frames.
   * Dispose of the GLSClient frame if it exists.
   * Used to stop trials in societies that aren't self terminating
   * and used to abort trials.
   */
  private void stopAllNodes() {
    // if there's a thread still creating nodes, stop it
    stopNodeCreation = true;
    // wait for the creating thread to stop
    if (nodeCreator != null) {
      try {
        nodeCreator.join();
      } catch (InterruptedException ie) {
        if(log.isErrorEnabled()) {
          log.error("Exception waiting for node creation thread to die: ", ie);
        }
      }
      nodeCreator = null;
    }
    // at this point all the nodes have been created, but
    // the gui controls for the last node created may not have
    // been created (as they're created via a swing-thread-invoke-later)
    // so we can no longer assume that a gui (node frame, status button)
    // exists for every node

    // set a flag indicating that we're stopping the trial
    stopping = true;
    Enumeration nodeNames;
    synchronized (runningNodesLock) {
      nodeNames = runningNodes.keys();
    } // end synchronized
    // before destroying nodes, stop the GLSClient so
    // we don't get error messages
    if (glsClient != null) 
      glsClient.stop();

    // destroy the nodes by creating a thread to destroy each one
    // and then waiting for all those threads to finish
    ArrayList destroyerThreads = new ArrayList();
    while (nodeNames.hasMoreElements()) {
      final String nodeName = (String)nodeNames.nextElement();
      Thread nodeDestroyer = new Thread("DestroyNode " + nodeName) {
        public void run() {
          RemoteProcess remoteNode;
          synchronized (runningNodesLock) {
            remoteNode = (RemoteProcess)runningNodes.get(nodeName);
          } // end synchronized
          if (remoteNode == null) {
            if(log.isErrorEnabled()) {
              log.error("Unknown node name: " + nodeName);
            }
            return;
          }
          try {
            remoteNode.getRemoteListenable().flushOutput();
            remoteNode.destroy();
          } catch (Exception ex) {
            if(log.isErrorEnabled()) {
              log.error("Unable to destroy node, assuming it's dead: ", ex);
            }
          }
        }
      }; // end nodeDestroyer thread
      destroyerThreads.add(nodeDestroyer);
      nodeDestroyer.start();
    }
    for (int i = 0; i < destroyerThreads.size(); i++) {
      try {
        Thread destroyer = (Thread)destroyerThreads.get(i);
        destroyer.join(); // wait for node destruction to complete
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("Exception", e);
        }
      }
    }
    // update the gui
    while (nodeNames.hasMoreElements()) {
      String nodeName = (String)nodeNames.nextElement();
      nodeStopped(nodeName);
      getNodeStatusButton(nodeName).setStatus(NodeStatusButton.STATUS_NO_ANSWER);
    }
  }

  // Kill any existing output frames or history charts
  private void destroyOldNodes() {
    Iterator nodeNames = oldNodes.iterator();
    while (nodeNames.hasNext()) {
      String nodeName = (String)nodeNames.next();
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
    
  private void updateControls(boolean isRunning) {
    if (experiment != null) {
      if (!isRunning) {
        experiment.experimentStopped();
        csmart.removeRunningExperiment(experiment);
      } else
        csmart.addRunningExperiment(experiment);
      // update society
      experiment.getSocietyComponent().setRunning(isRunning);
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
    abortButton.setSelected(false);

    // if not running, don't allow the user to restart individual nodes
    JInternalFrame[] frames = desktop.getAllFrames();
    for (int i = 0; i < frames.length; i++) {
      String s = frames[i].getTitle();
      if (!s.equals("Configuration") && !s.equals("Trial Values") &&
          !s.equals("GLS"))
        ((ConsoleInternalFrame)frames[i]).enableRestart(false);
    }
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

  public void nodeStopped(String nodeName) {
    synchronized (runningNodesLock) {
      if (runningNodes.get(nodeName) != null) {
        oldNodes.add(nodeName);
        runningNodes.remove(nodeName);
      }
    } // end synchronized

    // enable restart command on node output window, only if we're not stopping
    ConsoleInternalFrame frame = 
      desktop.getNodeFrame(nodeName);
    if (frame != null && !stopping)
      frame.enableRestart(true);

    // ignore condition in which we temporarily have 
    // no running nodes while starting
    if (starting) 
      return;

    // when all nodes have stopped, save results
    // run the next trial and update the gui controls
    boolean finishedTrial = false;
    synchronized (runningNodesLock) {
      if (runningNodes.isEmpty())
        finishedTrial = true;
    } // end synchronized
    if (finishedTrial) {
      stopping = false;
      trialFinished();
      if (haveMoreTrials()) {
	if (!userStoppedTrials) {
          // if the trial stopped by itself, then start the next trial
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
    updateControls(false);
  }

  /**
   * The experiment is finished; disable and deselect the run button;
   * disable the restart menus in the node output frames;
   * stop the timers; and
   * unset the property values used in the experiment.
   */
  private void experimentFinished() {
    if (experiment != null)
      trialProgressBar.setValue(experiment.getTrialCount() + 1);
    updateControls(false);
    runButton.setSelected(false);
    runButton.setEnabled(true);
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
    int port = Experiment.APP_SERVER_DEFAULT_PORT;
    if (properties == null)
      return port;
    try {
      String tmp = properties.getProperty(Experiment.CONTROL_PORT);
      if (tmp != null)
        port = Integer.parseInt(tmp);
    } catch (NumberFormatException nfe) {
      // use default port
    }
    return port;
  }

  /**
   * Get node -d arguments.
   * Substitute host name for $HOST value if it occurs.
   * @param node for which to get the -d arguments
   * @return properties the -d arguments
   */
  public Properties getNodeMinusD(NodeComponent nc, String hostName) {
    Properties result = new Properties();
    Properties props = nc.getArguments();
    boolean foundclass = false;
    for (Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
      String pname = (String) e.nextElement();
      if (pname.equals(COMMAND_ARGUMENTS)) continue;
      if (pname.equals(Experiment.BOOTSTRAP_CLASS)) foundclass = true;
      String value = props.getProperty(pname);
      int index = value.indexOf("$HOST");
      if (index != -1)
        value = value.substring(0, index) + hostName + 
          value.substring(index+5);
      result.put(pname, value);
      //      result.put(pname, props.getProperty(pname));
    }
    // make sure that the classname is "Node"
    //
    // this can be removed once the CMT and all "node.props"
    // are sure to have this property.

    // FIXME! We must allow users to specify the bootstrapper class!!
    if (foundclass = false) 
      result.put(
		 Experiment.BOOTSTRAP_CLASS, 
		 DEFAULT_BOOTSTRAP_CLASS);
    return result;
  }

  private java.util.List getNodeArguments(NodeComponent nc) {
    Properties props = nc.getArguments();
    String commandArguments =
      props.getProperty(COMMAND_ARGUMENTS);
    if (commandArguments == null || commandArguments.trim().equals("")) {
      // Warning: If you are running the bootstrapper and supply
      // nothing here, nothing will run!
      //  So if were using the default bootstrapper and have no arguments
      // Give it an argument with the default node class
      if (props.getProperty(Experiment.BOOTSTRAP_CLASS) == null || props.getProperty(Experiment.BOOTSTRAP_CLASS).equals(DEFAULT_BOOTSTRAP_CLASS)) 
	return Collections.singletonList(DEFAULT_NODE_CLASS);
      return Collections.EMPTY_LIST;
    }
    StringTokenizer tokens = 
      new StringTokenizer(commandArguments.trim(), "\n");
    String[] result = new String[tokens.countTokens()];
    for (int i = 0; i < result.length; i++) {
      result[i] = tokens.nextToken();
    }

    java.util.List l = Arrays.asList(result);
    return l;
  }

  private void prepareToCreateNode(RemoteHost appServer,
                                   String nodeName,
                                   String hostName,
                                   Properties properties,
                                   java.util.List args) {
    NodeStatusButton statusButton = createStatusButton(nodeName, hostName);
    ConsoleStyledDocument doc = new ConsoleStyledDocument();
    ConsoleTextPane textPane = new ConsoleTextPane(doc, statusButton);
    JScrollPane scrollPane = new JScrollPane(textPane);

    // create a node event listener to get events from the node
    OutputListener listener;
    String logFileName = getLogFileName(nodeName);
    try {
      listener = new ConsoleNodeListener(this,
                                         nodeName,
                                         logFileName,
                                         statusButton,
                                         doc);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Unable to create output for: " + nodeName, e);
      }
      return;
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

    OutputPolicy outputPolicy = new OutputPolicy(10);
    NodeCreationInfo nci =
      new NodeCreationInfo(appServer, 
                           properties, args,
                           listener, outputPolicy, 
                           nodeName, hostName,
                           scrollPane, statusButton,
                           logFileName);
    nodeCreationInfoList.add(nci);
  }

  // runs in a separate thread
  private void createNodes() {
    int delay = 1;
    // get inter-node start delay
    String tmp =
      Parameters.findParameter("org.cougaar.tools.csmart.startdelay");
    if (tmp != null) {
      try {
        delay = Integer.parseInt(tmp);
      } catch (NumberFormatException nfe) {
      }
    }
    final int interNodeStartDelay = delay;

    for (int i = 0; i < nodeCreationInfoList.size(); i++) {
      try {
        Thread.sleep(interNodeStartDelay);
      } catch (Exception e) {
      }
      if (stopNodeCreation)
        break;
      final NodeCreationInfo nci = 
        (NodeCreationInfo)nodeCreationInfoList.get(i);
      final RemoteProcess remoteNode;
      try {
        String experimentName = "";
        if (experiment != null)
          experimentName = experiment.getExperimentName();
        String procName = experimentName + "-" + nci.nodeName;
        String groupName = "csmart";
        ProcessDescription desc =
          new ProcessDescription(procName,
                                 groupName,
                                 nci.properties,
                                 nci.args);
        RemoteListenableConfig conf =
          new RemoteListenableConfig(nci.listener, 
                                     nci.outputPolicy);
        remoteNode = 
          nci.remoteAppServer.createRemoteProcess(desc, conf);
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("CSMARTConsole: cannot create node: " + 
                    nci.nodeName, e);
        }
        JOptionPane.showMessageDialog(this,
                                      "Cannot create node on: " + 
                                      nci.hostName +
                                      "; check that server is running");
        continue;
      }
      synchronized (runningNodesLock) {
        runningNodes.put(nci.nodeName, remoteNode);
      } // end synchronized
      SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            // don't create gui controls if node creation has been stopped
            if (nodeCreator == null) return;
            addStatusButton(nci.statusButton);
            ConsoleInternalFrame frame = 
              new ConsoleInternalFrame(nci.nodeName,
                                       nci.hostName,
                                       nci.properties,
                                       nci.args,
                                       (ConsoleNodeListener)nci.listener,
                                       nci.scrollPane,
                                       nci.statusButton,
                                       nci.logFileName,
                                       remoteNode,
                                       console);
            frame.addInternalFrameListener(new NodeFrameListener());
            desktop.addNodeFrame(frame, nci.nodeName);
            updateControls(true);
          }
        });
    }
  }

  /** 
   * Called from ConsoleInternalFrame (i.e. from menu on the node's output
   * window) to stop a node. 
   * If this is the only node, then it is handled the same
   * as selecting the Abort button on the console.
   */
  public void stopNode(String nodeName) {
    boolean doAbort = false;
    synchronized (runningNodesLock) {
      if (runningNodes.size() == 1)
        doAbort = true;
    } // end synchronized
    if (doAbort) {
      abortButton_actionPerformed();
      return;
    }
    RemoteProcess remoteNode;
    synchronized (runningNodesLock) {
      remoteNode = (RemoteProcess)runningNodes.get(nodeName);
    } // end synchronized
    if (remoteNode == null)
      return;
    try {
      remoteNode.getRemoteListenable().flushOutput();
      remoteNode.destroy();
    } catch (Exception ex) {
      if(log.isErrorEnabled()) {
        log.error("Unable to destroy node, assuming it's dead: ", ex);
      }
      // call the method that would have been called when the 
      // ConsoleNodeListener received the node destroyed confirmation
      nodeStopped(nodeName);
      getNodeStatusButton(nodeName).setStatus(NodeStatusButton.STATUS_NO_ANSWER);
    }
  }

  /**
   * Called from ConsoleInternalFrame (i.e. from menu on the node's output
   * window) to restart the node. 
   * If this is the only node, then it is handled the same
   * as selecting the Run button on the console.
   */

  public RemoteProcess restartNode(String nodeName) {
    NodeInfo nodeInfo = (NodeInfo)nodeToNodeInfo.get(nodeName);
    String hostName = nodeInfo.hostName;
    Properties properties = nodeInfo.properties;
    java.util.List args = nodeInfo.args;
    boolean doRun = false;
    synchronized (runningNodes) {
      if (oldNodes.size() + runningNodes.size() == 1)
        doRun = true;
    } // end synchronized
    if (doRun) {
      runButton_actionPerformed();
      return null; // return null, caller (ConsoleInternalFrame) is going away
    }
    properties.remove(Experiment.PERSIST_CLEAR);
    int remotePort = getAppServerPort(properties);
    RemoteHost remoteAppServer = getAppServer(hostName, remotePort);
    if (remoteAppServer == null)
      return null;
    // close the log file and remove the old node event listener
    ConsoleNodeListener listener = 
      (ConsoleNodeListener)nodeListeners.remove(nodeName);
    listener.closeLogFile();
    // reuse old document
    ConsoleStyledDocument doc = listener.getDocument();
    // create a node event listener to get events from the node
    String logFileName = getLogFileName(nodeName);
    try {
      listener = new ConsoleNodeListener(this,
                                         nodeName,
                                         logFileName,
                                         getNodeStatusButton(nodeName),
                                         doc);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Unable to create output for: " + nodeName, e);
      }
      return null;
    }
    if (displayFilter != null)
      listener.setFilter(displayFilter);
    nodeListeners.put(nodeName, listener);

    RemoteProcess remoteNode = null;
    try {
      String experimentName = "";
      if (experiment != null)
        experimentName = experiment.getExperimentName();
      String procName = experimentName + "-" + nodeName;
      String groupName = "csmart";
      ProcessDescription desc =
        new ProcessDescription(procName,
                               groupName,
                               properties,
                               args);
      RemoteListenableConfig conf =
        new RemoteListenableConfig(listener, 
                                   new OutputPolicy(10));
      remoteNode = 
        remoteAppServer.createRemoteProcess(desc, conf);
      if (remoteNode != null) {
        synchronized (runningNodesLock) {
          runningNodes.put(nodeName, remoteNode);
        } // end synchronized
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
    }
    return remoteNode;
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
   * This checks the society and recipes in the experiment to determine if
   * any of them generated this metrics file.
   * Creating a new File from the filename works because acceptFile
   * just looks at the filename.
   */
  private boolean isResultFile(String filename) {
    File thisFile = new java.io.File(filename);
    // if no experiment, use default filter
    if (experiment == null) 
      return new ResultsFileFilter().accept(thisFile);
    SocietyComponent societyComponent = experiment.getSocietyComponent();
    if (societyComponent != null) {
      java.io.FileFilter fileFilter = societyComponent.getResultFileFilter();
      if(fileFilter != null && fileFilter.accept(thisFile))
        return true;
    }
    int nrecipes = experiment.getRecipeComponentCount();
    for (int i = 0; i < nrecipes; i++) {
      RecipeComponent recipeComponent = experiment.getRecipeComponent(i);
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
  private void copyResultFiles(RemoteFileSystem remoteFS,
                               String dirname) {
    char[] cbuf = new char[1000];
    try {
      String[] filenames = remoteFS.list("./");
      for (int i = 0; i < filenames.length; i++) {
        if (!isResultFile(filenames[i]))
          continue;
        File newResultFile = 
          new File(dirname + File.separator + filenames[i]);
        InputStream is = remoteFS.read(filenames[i]);
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
      if(log.isErrorEnabled()) {
        log.error("Analyzer: copyResultFiles: ", e);
      }
    }
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
    String dirname = makeResultDirectory();
    if (experiment != null) {
      try {
        String myHostName = InetAddress.getLocalHost().getHostName();
        URL url = new URL("file", myHostName, dirname);
        Trial trial = experiment.getTrials()[currentTrial];
        trial.addTrialResult(new TrialResult(runStart, url));
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("Exception creating trial results URL: ", e);
        }
      }
    }
    Collection values = nodeToNodeInfo.values();
    for (Iterator i = values.iterator(); i.hasNext(); ) {
      NodeInfo ni = (NodeInfo)i.next();
      RemoteHost remoteAppServer = ni.appServer;
      RemoteFileSystem remoteFS = null;
      try {
        remoteFS = remoteAppServer.getRemoteFileSystem();
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Cannot save results.  Unable to access filesystem for " + 
            ni.hostName + ".",
            "Unable to access file system",
            JOptionPane.WARNING_MESSAGE);
        continue;
      }
      copyResultFiles(remoteFS, dirname);
    }
  }
  
  /**
   * Create a log file name which is of the form:
   * node name + date + .log
   * Create it in the results directory if possible.
   */
  private String getLogFileName(String nodeName) {
    String filename = nodeName + fileDateFormat.format(runStart) + ".log";
    String dirname = makeResultDirectory();
    if (dirname == null)
      return filename;
    else
      return dirname + File.separatorChar + filename;
  }

  /**
   * Create a directory for the results of this trial.
   * Results file structure is:
   * <ExperimentName>
   *    <TrialName>
   *       Results-<Timestamp>.results
   */
  private String makeResultDirectory() {
    // defaults, if we don't have an experiment
    File resultDir = csmart.getResultDir();
    String experimentName = "Experiment";
    String trialName = "Trial 1";
    if (experiment != null) {
      resultDir = experiment.getResultDirectory();
      experimentName = experiment.getExperimentName();
      trialName = experiment.getTrials()[currentTrial].getShortName();
    }
    // if user didn't specify results directory, save in local directory
    if (resultDir == null)
      return null;
    String dirname = resultDir.getAbsolutePath() + File.separatorChar + 
      experimentName + File.separatorChar +
      trialName + File.separatorChar +
      "Results-" + fileDateFormat.format(runStart);
    try {
      File f = new File(dirname);
      // guarantee that directories exist
      if (!f.exists() && !f.mkdirs() && !f.exists()) 
        return null;
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Couldn't create results directory: ", e);
      }
      return null;
    }
    return dirname;
  }
  
  /**
   * Action listeners for top level menus.
   */
  
  private void exitMenuItem_actionPerformed(AWTEvent e) {
    stopExperiments();
    updateControls(false);
    if (experiment != null)
      experiment.setRunInProgress(false);
    
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
   * A value of -2 means that the user cancelled the dialog.
   */
  
  static int displayViewSizeDialog(int currentViewSize) {
    JPanel bufferEventsPanel = new JPanel();
    JRadioButton allButton = new JRadioButton("All");
    JRadioButton sizeButton = new JRadioButton("Buffer Size");
    JTextField sizeTF = new JTextField(8);
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.ui.CSMARTConsole");
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
      return -2; // user cancelled
    int newViewSize = 0;
    if (allButton.isSelected()) {
      newViewSize = -1;
    } else {
      try {
        newViewSize = Integer.parseInt(sizeTF.getText());
      } catch (NumberFormatException e) {
        if(log.isErrorEnabled()) {
          log.error("Exception setting view size", e);
        }
        return currentViewSize;
      }
    }
    return newViewSize;
  }
  
  private void viewSizeMenuItem_actionPerformed() {
    int newViewSize = displayViewSizeDialog(viewSize);
    if (newViewSize == -2)
      return; // ignore, user cancelled
    viewSize = newViewSize;
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
    if(log.isErrorEnabled()) {
      log.error("Font Chooser not implemented yet");
    }
  }
  
  /**
   * Notify (by coloring status button) when the specified
   * output is received on any node.
   */
  
  private void setNotifyMenuItem_actionPerformed() {
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
      JOptionPane.showMessageDialog(this,
                                    "No notification set.",
                                    "Notification",
                                    JOptionPane.PLAIN_MESSAGE);
    else 
      JOptionPane.showMessageDialog(this,
                                    "Notify if any node writes: " + notifyCondition,
                                    "Notification",
                                    JOptionPane.PLAIN_MESSAGE);
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
    if (hostConfiguration == null) return;
    hostConfiguration.removeHostTreeSelectionListener(myTreeListener);
    hostConfiguration.selectNodeInHostTree(nodeName);
    hostConfiguration.addHostTreeSelectionListener(myTreeListener);
  }

  private RemoteHost getAppServer(String hostName, int port) {
    RemoteHost remoteAppServer = null;
    try {
      remoteAppServer = 
        remoteHostRegistry.lookupRemoteHost(hostName, port, true);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("CSMARTConsole: unable to contact app-server on " + 
                  hostName + " : " + port, e);
      }
      JOptionPane.showMessageDialog(this,
                                    "Unable to contact app-server on " +
                                    hostName + " : " + port +
                                    "; check that server is running");
      return null;
    }
    return remoteAppServer;
  }

  /**
   * Called from menu to display known app servers.
   */
  private void displayAppServers() {
    Util.showObjectsInList(this, appServers, "Application Servers", 
                           "Application Servers");
  }

  /**
   * Called from menu to recontact known app servers;
   * app servers that don't respond are automatically removed from the list.
   */
  private void refreshAppServers() {
    for (int i = 0; i < appServers.size(); i++) {
      AppServerDescription asd = (AppServerDescription)appServers.get(i);
      RemoteHost rh = getAppServer(asd.hostName, asd.remotePort);
      if (rh == null) {
        appServers.remove(asd);
        i--;
      }
    }
  }

  /**
   * Called from menu to delete app servers from the list of known servers.
   */
  private void deleteAppServers() {
    Object[] appServersToDelete =
      Util.getObjectsFromList(this, appServers, "Application Servers",
                              "Select Application Servers To Ignore:");
    if (appServersToDelete == null) return;
    for (int i = 0; i < appServersToDelete.length; i++)
      appServers.remove(appServersToDelete[i]);
  }

  /**
   * Called from menu to query user for a hostname and port
   * for a new app server.
   * If create AppServer successfully, then add it to the list of appServers.
   */
  private void addAppServer() {
    JTextField tf = new JTextField("", 20);
    JPanel panel = new JPanel();
    panel.add(new JLabel("Enter HostName:Port:"));
    panel.add(tf);
    int result = 
      JOptionPane.showOptionDialog(null, panel, "Add Application Server",
				   JOptionPane.OK_CANCEL_OPTION,
				   JOptionPane.PLAIN_MESSAGE,
				   null, null, null);
    if (result != JOptionPane.OK_OPTION)
      return;
    String s = tf.getText();
    int index = s.indexOf(':');
    if (index == -1)
      return;
    String hostName = s.substring(0, index);
    hostName = hostName.trim();
    String port = s.substring(index+1);
    port = port.trim();
    int remotePort = 0;
    try {
      remotePort = Integer.parseInt(port);
    } catch (Exception e) {
      return;
    }
    RemoteHost appServer = getAppServer(hostName, remotePort);
    if (appServer != null)
      appServers.add(new AppServerDescription(appServer, hostName, remotePort));
  }

  private void getAppServersFromExperiment() {
    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      String hostName = hosts[i].getShortName();
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
        NodeComponent nodeComponent = nodes[j];
        Properties properties = getNodeMinusD(nodeComponent, hostName);
        int remotePort = getAppServerPort(properties);
        RemoteHost appServer = getAppServer(hostName, remotePort);
        if (appServer != null)
          appServers.add(new AppServerDescription(appServer, 
                                                  hostName, remotePort));
      }
    }
  }

  /**
   * Return map of ProcessDescription to RemoteHost (appserver).
   */
  private Hashtable getProcessDescriptions() {
    Hashtable nodeToAppServer = new Hashtable();
    for (int i = 0; i < appServers.size(); i++) {
      AppServerDescription desc = (AppServerDescription)appServers.get(i);
      RemoteHost appServer = desc.appServer;
      java.util.List someNodes = null;
      try {
        someNodes = appServer.listProcessDescriptions();
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error("Exception getting info from app server: " + e);
        }
      }
      if (someNodes != null)
        for (Iterator j = someNodes.iterator(); j.hasNext(); )
          nodeToAppServer.put(j.next(), appServer);
    }
    return nodeToAppServer;
  }

  /**
   * Display all process descriptions from app servers and return the process
   * descriptions that the user selects.
   */

  private ProcessDescription[] getNodesToAttach(ArrayList nodes) {
    Object[] selected = 
      Util.getObjectsFromList(this, nodes, "Attach to Nodes", "Select Nodes:");
    if (selected != null) {
      ArrayList sel = new ArrayList(selected.length);
      for (int i = 0; i < selected.length; i++)
        sel.add(selected[i]);
      return (ProcessDescription[])sel.toArray(new ProcessDescription[selected.length]);
    } else
      return null;
  }

  /**
   * Get all known app servers from the experiment or from the user.
   * Display a list of the nodes that the app servers know about.
   * Allow the user to select the nodes to which to attach (or all).
   * Add the nodes to which the user attaches to the runningNodes list.
   */
  private void attachButton_actionPerformed() {
    attachButton.setSelected(false);
    if (appServers.size() == 0)
      return;
    Hashtable nodeToAppServer = getProcessDescriptions();
    Set pds = nodeToAppServer.keySet();
    if (pds.size() == 0)
      return;
    ProcessDescription[] attachToNodes = getNodesToAttach(new ArrayList(pds));
    //    printInfoFromAppServer(attachToNodes);
    nodeToNodeInfo.clear(); // clear all previous info on nodes
    for (int i = 0; i < attachToNodes.length; i++) {
      ProcessDescription pd = attachToNodes[i];
      Map map = pd.getJavaProperties();
      // workaround to immutable map problem
      Properties properties = new Properties();
      Set keys = map.keySet();
      for (Iterator k = keys.iterator(); k.hasNext(); ) {
        Object key = k.next();
        Object value = map.get(key);
        properties.put(key, value);
      }
      // end workaround
      String nodeName = (String)properties.get("org.cougaar.node.name");
      String hostName = (String)properties.get("org.cougaar.name.server");
      java.util.List args = pd.getCommandLineArguments();
      RemoteHost appServer = (RemoteHost)nodeToAppServer.get(pd);
      nodeToNodeInfo.put(nodeName, 
                         new NodeInfo(appServer, nodeName, hostName, 
                                      properties, args));
 
    }
    // TODO: finish this when the attach functionality is working
    // FOR DEBUGGING: because we can't attach to running nodes yet
    // abort and then run the new nodes; note this doesn't clean up
    // the gui properly, but will be ok when attach does the right thing
    abortButton_actionPerformed();
    experiment = null; // pretend we don't know about any experiment
    runButton_actionPerformed();
  }

  /**
   * Used by ConsoleInternalFrame to get values from experiment
   * if it exists.
   */
  protected Object getHostPropertyValue(String hostName, String propertyName) {
    if (experiment == null) return null;
    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      String s = hosts[i].getShortName();
      if (s.equals(hostName))
        return getPropertyValue(hosts[i], propertyName);
    }
    return null;
  }

  /**
   * Used by ConsoleInternalFrame to get values from experiment
   * if it exists.
   */
  protected Object getNodePropertyValue(String nodeName, String propertyName) {
    if (experiment == null) return null;
    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
        String s = nodes[j].getShortName();
        if (s.equals(nodeName))
          return getPropertyValue(nodes[j], propertyName);
      }
    }
    return null;
  }

  private Object getPropertyValue(BaseComponent component, String name) {
    Property prop = component.getProperty(name);
    if (prop == null)
      return null;
    return prop.getValue();
  }

 /**
   * Used by ConsoleInternalFrame to get values from experiment
   * if it exists.
   */
  protected ArrayList getAgentComponentDescriptions(String nodeName,
                                                    String agentName) {
    if (experiment == null)
      return null;
    ComponentData societyComponentData = experiment.getSocietyComponentData();
    if (societyComponentData == null) {
      if(log.isWarnEnabled()) {
        log.warn("CSMARTConsole: Need to save experiment");
      }
      return null;
    }
    ComponentData[] children = societyComponentData.getChildren();
    ComponentData nodeComponentData = null;
    for (int i = 0; i < children.length; i++) {
      if (children[i].getType().equals(ComponentData.HOST)) {
        ComponentData[] nodes = children[i].getChildren();
        for (int j = 0; j < nodes.length; j++) {
          if (nodes[j].getName().equals(nodeName)) {
            nodeComponentData = nodes[j];
            break;
          }
        }
      }
    }

    //  If couldn't find the node in the ComponentData, give up
    if (nodeComponentData == null)
      return null;

    ComponentData agentComponentData = null;

    // The "agent" might be a NodeAgent, in which case this is the right spot.
    if (agentName.equals(nodeComponentData.getName())) {
      agentComponentData = nodeComponentData;
    } else {
      // OK. Find the sub-Agent with the right name
      ComponentData[] agents = nodeComponentData.getChildren();
      for (int i = 0; i < agents.length; i++) {
	if (agents[i] instanceof AgentComponentData &&
	    agents[i].getName().equals(agentName)) {
	  agentComponentData = agents[i];
	  break;
	}
      }
    }
    
    // If couldn't find the Agent in the ComponentData for the node, give up
    if (agentComponentData == null)
      return null;

    // Loop through the children
    ComponentData[] agentChildren = agentComponentData.getChildren();
    ArrayList entries = new ArrayList(agentChildren.length);
    for (int i = 0; i < agentChildren.length; i++) {

      // If this Agent is a NodeAgent, ignore its Agent children.
      if (agentChildren[i].getType().equals(ComponentData.AGENT))
	continue;

      // FIXME: This should use same code as ExperimentINIWriter if possible
      StringBuffer sb = new StringBuffer();
      if (agentChildren[i].getType().equals(ComponentData.AGENTBINDER)) {
	sb.append("Node.AgentManager.Agent.PluginManager.Binder");
      } else if (agentChildren[i].getType().equals(ComponentData.NODEBINDER)) {
	sb.append("Node.AgentManager.Binder");
      } else {
	sb.append(agentChildren[i].getType());
      }
      if(ComponentDescription.parsePriority(agentChildren[i].getPriority()) != 
	 ComponentDescription.PRIORITY_COMPONENT) {
	sb.append("(" + agentChildren[i].getPriority() + ")");
      }
      sb.append(" = ");
      sb.append(agentChildren[i].getClassName());
      if (agentChildren[i].parameterCount() != 0) {
        sb.append("(");
        Object[] params = agentChildren[i].getParameters();
        sb.append(params[0].toString());
        for (int j = 1; j < agentChildren[i].parameterCount(); j++) {
          sb.append(",");
          sb.append(params[j].toString());
        }
        sb.append(")");
      }
      entries.add(sb.toString());
    }
    return entries;
  }

  // for debugging
  private void printInfoFromAppServer(ProcessDescription[] attachToNodes) {
    for (int i = 0; i < attachToNodes.length; i++) {
      System.out.println("Name: " + attachToNodes[i].getName());
      System.out.println("Group: " + attachToNodes[i].getGroup());
      Map properties = attachToNodes[i].getJavaProperties();
      Set keys = properties.keySet();
      for (Iterator j = keys.iterator(); j.hasNext(); ) {
        Object key = j.next();
        System.out.println("Property: " + key + ", " + properties.get(key));
      }
      java.util.List args = attachToNodes[i].getCommandLineArguments();
      for (Iterator j = args.iterator(); j.hasNext(); ) {
        System.out.println("Arg: " + j.next());
      }
    }
  }

  public static void main(String[] args) {
    CSMARTConsole console = new CSMARTConsole(null, null);
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
      String nodeName = frame.getNodeName();
      selectStatusButton(nodeName);
      selectNodeInHostTree(nodeName);
    }
  }
  
  /**
   * This contains all the information needed to create a node
   * and display its output.  It's passed to a separate thread
   * that creates the node.
   */
  
  class NodeCreationInfo {
    RemoteHost remoteAppServer;
    Properties properties;
    java.util.List args;
    OutputListener listener;
    OutputPolicy outputPolicy;
    String nodeName;
    String hostName;
    NodeComponent nodeComponent;
    JScrollPane scrollPane;
    NodeStatusButton statusButton;
    String logFileName;
    
    public NodeCreationInfo(RemoteHost remoteAppServer,
                            Properties properties,
                            java.util.List args,
                            OutputListener listener,
                            OutputPolicy outputPolicy,
                            String nodeName,
                            String hostName,
                            JScrollPane scrollPane,
                            NodeStatusButton statusButton,
                            String logFileName) {
      this.remoteAppServer = remoteAppServer;
      this.properties = properties;
      this.args = args;
      this.listener = listener;
      this.outputPolicy = outputPolicy;
      this.nodeName = nodeName;
      this.hostName = hostName;
      this.scrollPane = scrollPane;
      this.statusButton = statusButton;
      this.logFileName = logFileName;
    }
  }

  /**
   * This contains all the information needed to start or restart a node.
   */

  class NodeInfo {
    RemoteHost appServer;
    String nodeName;
    String hostName;
    Properties properties;
    java.util.List args;

    public NodeInfo(RemoteHost appServer,
                    String nodeName, String hostName,
                    Properties properties, java.util.List args) {
      this.appServer = appServer;
      this.nodeName = nodeName;
      this.hostName = hostName;
      this.properties = properties;
      this.args = args;
    }
  }

  /**
   * This contains the information about an application server,
   * needed to "display app servers" for the user.
   */

  class AppServerDescription {
    RemoteHost appServer;
    String hostName;
    int remotePort;

    public AppServerDescription(RemoteHost appServer, String hostName,
                                int remotePort) {
      this.appServer = appServer;
      this.hostName = hostName;
      this.remotePort = remotePort;
    }

    public String toString() {
      return hostName + ":" + remotePort;
    }
  }

  /**
   * This ignores app servers found on the same host and port
   * as an existing app server.
   * TODO: need to worry about app server(s) that are restarted?
   */
  class AppServerList extends ArrayList {
    public void add(AppServerDescription desc) {
      if (desc == null) return;
      for (int i = 0; i < size(); i++) {
        AppServerDescription asd = (AppServerDescription)get(i);
        if (desc.hostName.equals(asd.hostName) &&
            desc.remotePort == asd.remotePort) {
          return;
        }
      }
      super.add(desc);
    }
  }
  
  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }
  
}


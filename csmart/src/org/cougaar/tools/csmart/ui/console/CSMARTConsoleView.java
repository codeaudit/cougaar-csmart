/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.XMLExperiment;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.experiment.HostConfigurationBuilder;
import org.cougaar.tools.csmart.ui.tree.ConsoleTreeObject;
import org.cougaar.tools.csmart.ui.util.ChooserUtils;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

/**
 * org.cougaar.tools.csmart.ui.console
 *
 */
public class CSMARTConsoleView extends JFrame implements Observer {
  private HostConfigurationBuilder hostConfiguration = null;
  private Component hostConfigComponent = null;
  private transient Logger log;
  private ConsoleDesktop desktop;
  private CSMARTConsoleModel model;
  public boolean dontClose = false; // flag for use when running under CSMART

  // gui controls
  private ButtonGroup statusButtons;
  private JToggleButton attachButton;
  private JMenuItem attachMenuItem; // same as attachButton
  private JMenuItem addGLSMenuItem; // call addGLSWindow
  private JMenuItem findHostMenuItem;
  private JMenuItem findNodeMenuItem;
  private JMenuItem findAgentMenuItem;
  private JToggleButton runButton;
  private JToggleButton stopButton;
  private JPanel buttonPanel; // contains status buttons
  private JPopupMenu nodeMenu; // pop-up menu on node status button
  private Legend legend;

  private boolean haveGLS = false;

  private JMenuItem deleteMenuItem;
  private JMenuItem displayMenuItem;
  private JMenuItem killAllMenuItem;
  private JMenuItem refreshMenuItem;
  private ConsoleNodeOutputFilter displayFilter = null;
  private javax.swing.Timer experimentTimer;
  private JLabel experimentTimeLabel;
  private static Dimension HGAP10 = new Dimension(10, 1);
  private static Dimension HGAP5 = new Dimension(5, 1);
  private static Dimension VGAP30 = new Dimension(1, 30);
  private long startExperimentTime;
  private DecimalFormat myNumberFormat;

  // top level menus and menu items
  private static final String FILE_MENU = "File";
  private static final String OPEN_MENU_ITEM = "Load Society XML";
  private static final String EXIT_MENU_ITEM = "Close";
  private static final String VIEW_MENU = "View";
  private static final String SET_VIEW_SIZE_MENU_ITEM = "Set View Size...";
  private static final String FILTER_MENU_ITEM = "Filter...";
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
  private static final String ATTACH_AS_ITEM = "Attach...";
  private static final String KILL_ALL_PROCS_ITEM = "Kill Any Nodes";
  private static final String REFRESH_APP_SERVER_ITEM = "Refresh";
  private static final String SET_POLL_INTERVAL_ITEM = "Set Poll Interval";
  private static final String ADD_GLS_ITEM = "Add GLS Client";
  private static final String HELP_MENU = "Help";
  private static final String ABOUT_CONSOLE_ITEM = "About Experiment Controller";
  private static final String ABOUT_CSMART_ITEM = "About CSMART";
  private static final String LEGEND_MENU_ITEM = "Node Status Legend";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";
  private static final String HELP_DOC = "help.html";

  // for pop-up menu on node status buttons
  private static final String ABOUT_ACTION = "Info";
  private static final String RESET_ACTION = "Reset Notification";

  private static final int MSECS_PER_SECOND = 1000;
  private static final int MSECS_PER_MINUTE = 60000;
  private static final int MSECS_PER_HOUR = 3600000;


  /**
   * Create and show console GUI.
   */
  public CSMARTConsoleView() {
    this(null, null);
  }

  public CSMARTConsoleView(CSMART csmart) {
    this(csmart, null);
  }

  /**
   * Create and show console GUI.
   * @param csmart the CSMART viewer
   */
  public CSMARTConsoleView(CSMART csmart, Experiment experiment) {
    this.model = new CSMARTConsoleModel(experiment, csmart);
    createLogger();
    initGUI();
  }

  private void initGUI() {
    String description = "Experiment Controller";

    desktop = new ConsoleDesktop();

    setTitle(description);
    getRootPane().setJMenuBar(createMenu());
    getContentPane().add(createNodePanel(description));

    model.addObserver(this);
    initButtons();
    updateASControls();

    experimentTimer =
        new javax.swing.Timer(1000, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            experimentTimeLabel.setText(getElapsedTimeLabel("Experiment: ", startExperimentTime));
          }
        });

    // do nothing on the close; the windowClosing method handles the close
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        exitMenuItem_actionPerformed(e);
      }
    });

    pack();
    setSize(700, 600);
    setVisible(true);
  } // end initGUI

  private JPanel createNodePanel(String description) {
    // create panel which contains description panel, status button panel, and tabbed panes
    JPanel panel = new JPanel(new GridBagLayout());

    // descriptionPanel contains society name, control buttons
    JPanel descriptionPanel = createHorizontalPanel(true);
    descriptionPanel.add(Box.createRigidArea(VGAP30));
    descriptionPanel.add(Box.createRigidArea(HGAP10));
    descriptionPanel.add(new JLabel(description));
    descriptionPanel.add(Box.createRigidArea(HGAP5));

    attachButton = new JToggleButton("Attach");
    attachButton.setToolTipText("Attach to running nodes");
    attachButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        attachStateChanged();
      }
    });
    attachButton.setFocusPainted(false);
    descriptionPanel.add(attachButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));
    runButton = new JToggleButton("Run");
    runButton.setToolTipText("Start running experiment");
    runButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        model.setRunning(true);
      }
    });
    runButton.setFocusPainted(false);
    descriptionPanel.add(runButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));

    stopButton = new JToggleButton("Stop");
    stopButton.setToolTipText("Halt experiment at end of current");
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        model.setRunning(false);
      }
    });
    stopButton.setFocusPainted(false);
    descriptionPanel.add(stopButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));

    // create progress panel for time labels
    // these are referenced elsewhere, so are created even if not displayed
    JPanel runProgressPanel = createHorizontalPanel(false);
    myNumberFormat = new DecimalFormat("00");
    experimentTimeLabel = new JLabel("Experiment: 00:00:00");
    JPanel progressPanel = new JPanel(new GridBagLayout());
    runProgressPanel.add(experimentTimeLabel);
    progressPanel.add(runProgressPanel,
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

    // if have experiment,
    // create internal frame for host/node/agent configuration
    createHostConfiguration();
    panel.add(desktop,
              new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.BOTH,
                                     new Insets(0, 0, 0, 0),
                                     0, 0));
    return panel;
  }

  private void createHostConfiguration() {
    Experiment experiment = model.getExperiment();
    if (experiment != null) {
      if(hostConfigComponent != null) {
        desktop.remove(hostConfigComponent);
      }
      hostConfiguration = new HostConfigurationBuilder(experiment, null);
      hostConfiguration.update(); // display configuration
      hostConfiguration.addHostTreeSelectionListener(myTreeListener);
      JInternalFrame jif = new JInternalFrame("Configuration",
                                              true, false, true, true);
      jif.getContentPane().add(hostConfiguration);
      jif.setSize(660, 400);
      jif.setLocation(0, 0);
      jif.setVisible(true);
      hostConfigComponent = jif;
      desktop.add(jif, JLayeredPane.DEFAULT_LAYER);
    }
  }

  private JMenuBar createMenu() {
    // top level menus
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.setToolTipText("Console Operations");

    JMenuItem openMenuItem = new JMenuItem(OPEN_MENU_ITEM);
    openMenuItem.setToolTipText("Load in an XML Society to run.");
    openMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openMenuItem_actionPerformed(e);
      }
    });
    fileMenu.add(openMenuItem);

    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.setToolTipText("Exit this tool.");
    exitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        exitMenuItem_actionPerformed(e);
      }
    });
    fileMenu.add(exitMenuItem);

    JMenu viewMenu = new JMenu(VIEW_MENU);
    JMenuItem viewSizeMenuItem = new JMenuItem(SET_VIEW_SIZE_MENU_ITEM);
    viewSizeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        viewSizeMenuItem_actionPerformed();
      }
    });
    viewMenu.add(viewSizeMenuItem);
    JMenuItem filterMenuItem = new JMenuItem(FILTER_MENU_ITEM);
    filterMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        filterMenuItem_actionPerformed();
      }
    });
    viewMenu.add(filterMenuItem);

    JMenu findMenu = new JMenu(FIND_MENU);
    findHostMenuItem = new JMenuItem(FIND_HOST_MENU_ITEM);
    findHostMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hostConfiguration.findHost();
      }
    });
    findMenu.add(findHostMenuItem);
    findNodeMenuItem = new JMenuItem(FIND_NODE_MENU_ITEM);
    findNodeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hostConfiguration.findNode();
      }
    });
    findMenu.add(findNodeMenuItem);
    findAgentMenuItem = new JMenuItem(FIND_AGENT_MENU_ITEM);
    findAgentMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hostConfiguration.findAgent();
      }
    });
    findMenu.add(findAgentMenuItem);
    enableFindOptions();

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

    displayMenuItem = new JMenuItem(VIEW_APP_SERVER_ITEM);
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

    deleteMenuItem = new JMenuItem(DELETE_APP_SERVER_ITEM);
    deleteMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteAppServers();
      }
    });
    deleteMenuItem.setToolTipText("Ignore Application Servers.");
    appServerMenu.add(deleteMenuItem);

    attachMenuItem = new JMenuItem(ATTACH_AS_ITEM);
    attachMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        attachStateChanged();
      }
    });
    attachMenuItem.setToolTipText("Attach to any new Running Nodes.");
    appServerMenu.add(attachMenuItem);

    killAllMenuItem = new JMenuItem(KILL_ALL_PROCS_ITEM);
    killAllMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        killAll_actionPerformed();
      }
    });
    killAllMenuItem.setToolTipText("Kill Any Nodes on known App Servers.");
    appServerMenu.add(killAllMenuItem);

    refreshMenuItem = new JMenuItem(REFRESH_APP_SERVER_ITEM);
    refreshMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshAppServers();
      }
    });
    refreshMenuItem.setToolTipText("Refresh list of Application Servers");
    appServerMenu.add(refreshMenuItem);

    JMenuItem pollIntervalMenuItem = new JMenuItem(SET_POLL_INTERVAL_ITEM);
    pollIntervalMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Put up a dialog with the current interval.
        // If the user changes the interval, cancel the current timer
        // and create a new one
        int interval = getNewASPollInterval();
        if (interval != 0)
          model.resetASPoller(interval);
      }
    });
    pollIntervalMenuItem.setToolTipText("Change Delay Between Checking for New Application Servers");
    appServerMenu.add(pollIntervalMenuItem);

    // Menu item for popping up a new GLS Client
    addGLSMenuItem = new JMenuItem(ADD_GLS_ITEM);
    addGLSMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        model.addGLSWindow();
      }
    });
    addGLSMenuItem.setToolTipText("Add new GLS Client for sending GLS Init");
    appServerMenu.add(addGLSMenuItem);

    JMenu helpMenu = new JMenu(HELP_MENU);
    JMenuItem helpMenuItem = new JMenuItem(ABOUT_CONSOLE_ITEM);
    helpMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        URL help = this.getClass().getResource(HELP_DOC);
        if (help != null)
          Browser.setPage(help);
      }
    });
    helpMenu.add(helpMenuItem);

    legend = new Legend();
    JMenuItem legendMenuItem = new JMenuItem(LEGEND_MENU_ITEM);
    legendMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        legend.setVisible(true);
      }
    });
    helpMenu.add(legendMenuItem);

    JMenuItem aboutMenuItem = new JMenuItem(ABOUT_CSMART_ITEM);
    aboutMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        URL about = this.getClass().getResource(ABOUT_DOC);
        if (about != null)
          Browser.setPage(about);
      }
    });
    helpMenu.add(aboutMenuItem);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(viewMenu);
    menuBar.add(findMenu);
    menuBar.add(notifyMenu);
    menuBar.add(appServerMenu);
    menuBar.add(helpMenu);
    return menuBar;
  }

  private void enableFindOptions() {
    if (model.getExperiment() == null) {
      findHostMenuItem.setEnabled(false);
      findNodeMenuItem.setEnabled(false);
      findAgentMenuItem.setEnabled(false);
    } else {
      findHostMenuItem.setEnabled(true);
      findNodeMenuItem.setEnabled(true);
      findAgentMenuItem.setEnabled(true);
    }
  }

  private void openMenuItem_actionPerformed(ActionEvent e) {
      final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          return new CreateExperiment();
        }
//        public void finished() {
//          model.setExperiment(exp);
//        }
      };
      worker.start();

//      XMLExperiment exp = new XMLExperiment(chooser.getSelectedFile(), this);
//      exp.doParse();
//      model.setXMLFile(chooser.getSelectedFile());
//      model.setExperiment(exp);
//
//      model.setXMLFile(file);

//      Document doc = ComponentDataXML.createXMLDocument(exp.getExperiment());
//      try {
//        XMLUtils.writeXMLFile(new File("/tmp/"), doc, "dump.xml");
//      } catch (IOException e1) {
//        e1.printStackTrace();
//      }
  }

  /**
   * Get new App Server polling interval from user.
   * A value of 0 means the user entered an invalid value or cancelled.
   */
  private int getNewASPollInterval() {
    if (log.isDebugEnabled()) {
      log.debug("Getting new ASPoll Interval");
    }
    JPanel pollPanel = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    pollPanel.add(new JLabel("Interval in milliseconds between polls for live AppServers (0 to not poll):"),
                  new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                         GridBagConstraints.WEST,
                                         GridBagConstraints.NONE,
                                         new Insets(10, 0, 5, 5),
                                         0, 0));
    JTextField pollField =
        new JTextField(7);
    pollField.setText(String.valueOf(model.getASPollInterval()));
    pollPanel.add(pollField,
                  new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                         GridBagConstraints.WEST,
                                         GridBagConstraints.HORIZONTAL,
                                         new Insets(10, 0, 5, 0),
                                         0, 0));
    int result = JOptionPane.showConfirmDialog(null, pollPanel,
                                               "Polling Interval",
                                               JOptionPane.OK_CANCEL_OPTION);
    if (result != JOptionPane.OK_OPTION)
      return 0;
    String s = pollField.getText().trim();
    if (s == null || s.length() == 0) {
      return 0;
    } else {
      int res = 0;
      try {
        res = Integer.parseInt(s);
      } catch (NumberFormatException e) {
      }
      if (res < 0)
        return 0;
      return res;
    }
  }

  /**
   * If there are AppServers,
   * then enable the delete and refresh app server controls.
   * If there are AppServers with unattached nodes,
   * then enable the attach controls.
   * If there are AppServers with attached nodes,
   * then enable the "kill" menu.
   */

  private void updateASControls() {
    if (model.getAppServers().size() == 0) {
      displayMenuItem.setEnabled(false);
      deleteMenuItem.setEnabled(false);
      refreshMenuItem.setEnabled(false);
    } else {
      displayMenuItem.setEnabled(true);
      deleteMenuItem.setEnabled(true);
      refreshMenuItem.setEnabled(true);
    }
    if (model.getUnattachedNodes().size() == 0) {
      if (!attachButton.isSelected())
        attachButton.setEnabled(false);
      attachMenuItem.setEnabled(false);
    } else {
      attachButton.setEnabled(true);
      attachMenuItem.setEnabled(true);
    }
    if (model.getAttachedNodes().size() == 0) {
      killAllMenuItem.setEnabled(false);
    } else {
      killAllMenuItem.setEnabled(true);
    }
  }

  private void initButtons() {
    runButton.setEnabled(model.isRunnable());
    stopButton.setEnabled(false);
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

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  //   Listeners start here.

  /**
   * Display information about the selected node.
   */
  private void displayAboutNode() {
    String name = model.getSelectedNodeName();
    if (name != null) {
      NodeView nodeView = desktop.getNodeFrame(name);
      if (nodeView != null)
        nodeView.displayAbout();
    }
  }

  /**
   * Reset the notify string and status button for the selected node.
   */
  private void resetNodeStatus() {
    String name = model.getSelectedNodeName();
    if (name != null) {
      NodeView nodeView = desktop.getNodeFrame(name);
      if (nodeView != null)
        nodeView.resetNotify();
    }
  }

  /**
   * Set the view size for all the node views.
   */
  private void viewSizeMenuItem_actionPerformed() {
    int newViewSize = displayViewSizeDialog(model.getViewSize());
    if (newViewSize == -2)
      return; // ignore, user cancelled
    model.setViewSize(newViewSize);
  }

  /**
   * Set the filter for all the node views.
   */
  private void filterMenuItem_actionPerformed() {
    if (displayFilter == null)
      displayFilter = new ConsoleNodeOutputFilter(null, true);
    else {
       displayFilter =
         new ConsoleNodeOutputFilter(displayFilter.getValues(),
                                     displayFilter.isAllSelected());
    }
    // this must set the filter values for all the nodes
    model.setFilter(displayFilter);
  }

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
    notifyField.setText(model.getNotifyCondition());
    notifyPanel.add(notifyField,
                    new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(10, 0, 5, 0),
                                           0, 0));
    x = 0;
    JCheckBox stdErrorCB =
      new JCheckBox("Notify on Standard Error",
                    model.getNotifyOnStandardError());
    notifyPanel.add(stdErrorCB,
                    new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(10, 0, 5, 5),
                                           0, 0));
    int result = JOptionPane.showConfirmDialog(this, notifyPanel,
                                               "Notification",
                                               JOptionPane.OK_CANCEL_OPTION);
    if (result != JOptionPane.OK_OPTION)
      return;
    String s = notifyField.getText();
    if (s.length() == 0)
      s = null;
    model.setNotification(s, stdErrorCB.isSelected());
  }

  private void viewNotifyMenuItem_actionPerformed() {
    String notifyCondition = model.getNotifyCondition();
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
    model.setNotification(null, false);
  }

  /**
   * Reset status for all nodes. Resets the "notify" position
   * in the text pane and resets the error flag of the node status button.
   */
  private void resetNotifyMenuItem_actionPerformed() {
    model.resetNotifyStatus();
  }

  private void killAll_actionPerformed() {
    int result =
      JOptionPane.showConfirmDialog(this,
                   "Really kill all running Nodes on all known AppServers?",
                                               "Kill All Nodes",
                                               JOptionPane.OK_CANCEL_OPTION);
    if (result != JOptionPane.OK_OPTION)
      return;

    if (log.isDebugEnabled())
      log.debug("Killing all Nodes!");

    model.killAllProcesses();
  }

  /**
   * Invoked from File-Exit menu command or from the window
   * close icon.
   * Displays a dialog that gives the user the option to
   * detach from the nodes, kill the nodes, or cancel the exit.
   * If the user cancels the exit, set the dontClose flag,
   * which the main CSMART viewer uses in its windowClosing
   * method to determine whether or not to ignore the close.
   */
  private void exitMenuItem_actionPerformed(AWTEvent e) {
    dontClose = false;
    if (log.isDebugEnabled()) {
      log.debug("In exitMenuItem_actionPerformed");
    }
    if (model.haveAttached()) {
      ExitConsole ec = new ExitConsole();
      if (ec.reallyExit()) {
        if (ec.getResult().equals(ec.SOCIETY_DETACH)) {
          model.detachFromSociety();
        } else if (ec.getResult().equals(ec.KILL_NODES)) {
          model.stopNodes();
        }
      } else {
        dontClose = true;  // tells the main CSMART viewer to ignore close
        return; // user cancelled the close, do nothing
      }
    }

    // when Console is a part of CSMART
    // this removes the window from the list that CSMART maintains
    if(!model.isCSMARTNull()) {
      if (e instanceof ActionEvent) {
        NamedFrame.getNamedFrame().removeFrame(this);
      }
      dispose();
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Not doing a removeFrame: event was " + e);
      }
      dispose(); // dispose of this instance (should clean up all instances in console)
      System.exit(0);
    }
  }

  /**
   * Get a list of unattached nodes and display these to the user.
   * Message the model to attach to each node.
   */
  private void attachStateChanged() {
    ArrayList nodes = model.getUnattachedNodes();
    Object[] selected =
        Util.getObjectsFromList(null, nodes,
                                "Attach to Nodes", "Select Nodes:");
    if (selected != null) {
      for (int i = 0; i < selected.length; i++)
        model.attachToNode((String) selected[i]);
    }
  }

  // End Listeners

  public static void main(String[] args) {
    CSMARTConsoleView ccv = new CSMARTConsoleView(null);
  }

  /**
   * This is an observer on the CSMARTConsoleModel;
   * it handles the following events:
   * If an AppServer was added or deleted or a node added,
   * it updates the Attach button and the AppServer menu.
   * If a new NodeView was created,
   * it adds it and its status button to the display.
   * If a NodeView is removed, it updates the display.
   * If a GLS window is added, it updates the display.
   * If the experiment timer should be started or stopped, it does that.
   * If the state of a node changed,
   * it updates the Run and Stop buttons.
   */
  public void update(Observable o, Object arg) {
    if (arg.equals(CSMARTConsoleModel.APP_SERVER_ADDED) ||
        arg.equals(CSMARTConsoleModel.APP_SERVER_DELETED) ||
        arg.equals(CSMARTConsoleModel.NODE_ADDED)) {
      updateASControls();
    } else if (arg instanceof NodeView) {
      desktop.addNodeFrame((NodeView)arg, ((NodeView) arg).getNodeName());
      ((NodeView)arg).addInternalFrameListener(new NodeFrameListener());
      NodeModel nm = model.getNodeModel(((NodeView)arg).getNodeName());
      statusButtons.add(nm.getStatusButton());
      nm.getStatusButton().addMouseListener(myMouseListener);
      buttonPanel.add(nm.getStatusButton());
    } else if (arg instanceof NodeChange) {
      NodeChange nc = (NodeChange)arg;
      if (nc.action.equals(CSMARTConsoleModel.NODE_REMOVED))
        removeNodeView(nc.nodeName);
    } else if (arg.equals(CSMARTConsoleModel.ADD_GLS_WINDOW)) {
      if(!haveGLS) {
        JInternalFrame gls = GLSClientView.getGLSClientView(model.getGLSContactInfo());
        desktop.add(gls, JLayeredPane.DEFAULT_LAYER);
        try {
          gls.setIcon(true);
        } catch (PropertyVetoException e) {
          // Ignore exception, the window will not be set as icon.
        }

        haveGLS = true;
      }
    } else if (arg.equals(CSMARTConsoleModel.START_EXPERIMENT_TIMER)) {
      startExperimentTime = new Date().getTime();
      experimentTimer.start();
    } else if (arg.equals(CSMARTConsoleModel.STOP_EXPERIMENT_TIMER)) {
      experimentTimer.stop();
    } else if (arg instanceof String &&
               ((String)arg).startsWith("NODE_STATE")) {
      updateButtons();
    } else if (arg.equals(CSMARTConsoleModel.NEW_EXPERIMENT)) {
      initButtons();
      createHostConfiguration();
      updateASControls();
      enableFindOptions();
    }
  }

  /**
   * If any node is in the running state, enable Stop.
   * If any node is in the initted or stopped states, enable Run.
   * If any node is in the starting or running states, select Run.
   * If any node is in the stopping or stopped states, select Stop.
   * Selected buttons are also enabled.
   */
//  private void updateButtons() {
//    boolean runSelected = false;
//    boolean stopSelected = false;
//    ArrayList nodeModels = model.getNodeModels();
//    for (int i = 0; i < nodeModels.size(); i++) {
//      NodeModel nodeModel = (NodeModel)nodeModels.get(i);
//      String state = nodeModel.getState();
//      if (state.equals(NodeModel.STATE_STARTING) ||
//          state.equals(NodeModel.STATE_RUNNING))
//        runSelected = true;
//      else if (state.equals(NodeModel.STATE_STOPPING) ||
//               state.equals(NodeModel.STATE_STOPPED))
//        stopSelected = true;
//    }
//    runButton.setSelected(runSelected);
//    stopButton.setSelected(stopSelected);
//    boolean enableRun = false;
//    boolean enableStop = false;
//    for (int i = 0; i < nodeModels.size(); i++) {
//      NodeModel nodeModel = (NodeModel)nodeModels.get(i);
//      String state = nodeModel.getState();
//      if (state.equals(NodeModel.STATE_RUNNING)) {
//        enableStop = true;
//      } else if (state.equals(NodeModel.STATE_INITTED) ||
//                 state.equals(NodeModel.STATE_STOPPED)) {
//        enableRun = true;
//      }
//    }
//    runButton.setEnabled(enableRun || runSelected);
//    stopButton.setEnabled(enableStop || stopSelected);
//  }

  /**
   * Called when any model changes state.
   * AttachButton:
   *  Enable when there are 1 or more unattached nodes (handled in updateASControls)
   *  Disable wen there are 0 unattached nodes and its not selected
   *  Deselect when there are 0 nodes in the Running state.
   * RunButton:
   *  Enable when 1 or more nodes are in the Initted or Stopped states.
   *  Disable when 0 nodes are in the Initted or Stopped states and it's not selected.
   *  Select when 1 or more nodes are in the Running state.
   *  Deselect when 0 nodes are in the Running state.
   * StopButton:
   *  Enable when 1 or more nodes are in the Running state.
   *  Disable when 0 nodes are in the Running state and it's not selected.
   *  Select when 1 or more nodes are in the Stopping or Stopped states.
   *  Deselect when 0 nodes are in the Stopping or Stopped states.
   */
  private void updateButtons() {
    boolean haveInitted = false;
    boolean haveRunning = false;
    boolean haveStopping = false;
    boolean haveStopped = false;

    ArrayList nodeModels = model.getNodeModels();
    for (int i = 0; i < nodeModels.size(); i++) {
      NodeModel nodeModel = (NodeModel)nodeModels.get(i);
      String state = nodeModel.getState();
      if (state.equals(NodeModel.STATE_RUNNING))
        haveRunning = true;
      else if (state.equals(NodeModel.STATE_INITTED))
        haveInitted = true;
      else if (state.equals(NodeModel.STATE_STOPPING))
        haveStopping = true;
      else if (state.equals(NodeModel.STATE_STOPPED))
        haveStopped = true;
    }
    if (haveInitted || haveStopped)
      runButton.setEnabled(true);
    if (!haveInitted && !haveStopped && !runButton.isSelected())
      runButton.setEnabled(false);
    if (haveRunning) {
      runButton.setSelected(true);
      // if you select a button, you have to enable it
      runButton.setEnabled(true);
      stopButton.setEnabled(true);
    } else {
      attachButton.setSelected(false);
      if (model.getUnattachedNodes().size() == 0)
        attachButton.setEnabled(false);
      runButton.setSelected(false);
      if (!stopButton.isSelected())
        stopButton.setEnabled(false);
    }
    if (haveStopping || haveStopped) {
      stopButton.setSelected(true);
      // if you select a button, you have to enable it
      stopButton.setEnabled(true);
    } else
      stopButton.setSelected(false);
  }

  /**
   * Remove node view and node status button.
   */
  public void removeNodeView(String nodeName) {
    NodeStatusButton button = model.getNodeModel(nodeName).getStatusButton();
    if (button != null) {
      statusButtons.remove(button);
      buttonPanel.remove(button);
    }
    NodeView view = desktop.getNodeFrame(nodeName);
    if (view != null)
      desktop.removeFrame(view);
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
    Logger log = CSMART.createLogger(CSMARTConsoleView.class.getName());
    if (currentViewSize == -1) {
      allButton.setSelected(true);
      sizeButton.setSelected(false);
      sizeTF.setText(String.valueOf(CSMARTConsoleModel.DEFAULT_VIEW_SIZE));
    } else {
      allButton.setSelected(false);
      sizeButton.setSelected(true);
      sizeTF.setText(String.valueOf(currentViewSize));
    }
    String oldTFVal = sizeTF.getText();
    ButtonGroup bufferButtonGroup = new ButtonGroup();
    bufferButtonGroup.add(allButton);
    bufferButtonGroup.add(sizeButton);
    bufferEventsPanel.add(allButton);
    bufferEventsPanel.add(sizeButton);
    bufferEventsPanel.add(sizeTF);

    int newViewSize = 0;
    while (true) {
      int result = JOptionPane.showConfirmDialog(null,
                                                 bufferEventsPanel,
                                                 "Node View",
                                                 JOptionPane.OK_CANCEL_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null);
      if (result != JOptionPane.OK_OPTION)
        return -2; // user cancelled
      newViewSize = 0;
      if (allButton.isSelected()) {
        return -1;
      } else {
        try {
          newViewSize = Integer.parseInt(sizeTF.getText());
        } catch (NumberFormatException e) {
          if (log.isErrorEnabled()) {
            log.error("Bad new view size: " + sizeTF.getText());
          }
        }
        if (newViewSize < 1) {
          // Invalid size. Show error message.
          JOptionPane.showMessageDialog(null,
                                        "Invalid buffer size. Must be a whole number, at least 1.",
                                        "Invalid entry",
                                        JOptionPane.WARNING_MESSAGE);
          sizeTF.setText(oldTFVal);
        } else {
          // Legitimate result
          break;
        }
      }
    } // end of while loop
    return newViewSize;
  }

  /**
   * Select status button for node.
   */
  private void selectStatusButton(String nodeName) {
    NodeStatusButton button = model.getNodeModel(nodeName).getStatusButton();
    if (button != null)
      button.setSelected(true);
  }

  /**
   * Listener on the node status buttons.
   * Right click pops-up a menu with the "About" node menu item.
   * Left click opens the node standard out frame,
   * and highlights the node in the configuration tree.
   */
  private MouseListener myMouseListener = new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
      if (e.isPopupTrigger()) {
        doPopup(e);
      } else {
        String nodeName = ((JRadioButton) e.getSource()).getActionCommand();
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
   * Display pop-up menu with "about" menu item, which provides
   * the same functionality as the "about" menu item in the node window,
   * but from the node status lamp.
   */

  private void doPopup(MouseEvent e) {
    model.setSelectedNodeName(((JRadioButton)e.getSource()).getActionCommand());
    nodeMenu.show((Component) e.getSource(), e.getX(), e.getY());
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

  /**
   * Query the user for the host and port of the remote app server.
   * Message the model to try to add this app server.
   */
  private void addAppServer() {
    int port = Experiment.APP_SERVER_DEFAULT_PORT;
    JTextField tf = new JTextField("localhost:" + port, 20);
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
    String s = tf.getText().trim();
    int index = s.indexOf(':');
    String hostName = s;

    if (index != -1) {
      // Then hostname is the part before
      hostName = s.substring(0, index);
      hostName = hostName.trim();
      // But if there's nothing before the colon, use localhost
      if (hostName.equals(""))
        hostName = "localhost";
      // Port is the part after the colon
      String portString = s.substring(index + 1);
      portString = portString.trim();
      if (!portString.equals("")) {
        try {
          port = Integer.parseInt(portString);
        } catch (Exception e) {
          return;
        }
        if (port < 1)
          return;
      }
    }
    // check for duplicates
    ArrayList appServers = model.getAppServers();
    for (int i = 0; i < appServers.size(); i++) {
      AppServerDesc desc = (AppServerDesc) appServers.get(i);
      if (desc.hostName.equals(hostName) &&
          desc.port == port) {
        JOptionPane.showMessageDialog(null,
                                      "This is a known application server; use Refresh to update information from it.",
                                      "Known Application Server",
                                      JOptionPane.INFORMATION_MESSAGE);
        return;
      }
    }
    model.requestAppServerAdd(hostName, port);
  }

  /**
   * Display a list of known app servers to the user.
   * Message the model to delete the app servers that the user selects.
   */
  private void deleteAppServers() {
    ArrayList appServers = model.getAppServers();
    Object[] appServersSelected =
        Util.getObjectsFromList(null, appServers, "Application Servers",
                                "Select Application Servers To Ignore:");
    if (appServersSelected == null) return;
    for (int i = 0; i < appServersSelected.length; i++) {
      AppServerDesc appServerDesc = (AppServerDesc) appServersSelected[i];
      model.appServerDelete(appServerDesc);
    }
  }

  private void refreshAppServers() {
    model.refreshAppServers();
  }

  private void displayAppServers() {
    Util.showObjectsInList(this,
                           model.getAppServers(),
                           "Application Servers",
                           "Application Servers");
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
          (DefaultMutableTreeNode) path.getLastPathComponent();
      ConsoleTreeObject cto = (ConsoleTreeObject) treeNode.getUserObject();
      if (!cto.isNode())
        return; // ignore selecting if it's not a node
      String nodeName =
          ((ConsoleTreeObject) treeNode.getUserObject()).getName();
      if(model.getNodeModel(nodeName) != null) {
        displayNodeFrame(nodeName);
        selectStatusButton(nodeName);
      }
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

  /**
   * Listener on node frame. When frame is selected,
   * select status button and node in configuration tree.
   */
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

    private void frameSelected(InternalFrameEvent e) {
      NodeView nodeView = (NodeView)e.getInternalFrame();
      String nodeName = nodeView.getNodeName();
      selectStatusButton(nodeName);
      selectNodeInHostTree(nodeName);
    }
  } // end NodeFrameListener

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

  class CreateExperiment {

    boolean isValidating = false;
    JCheckBox validate = null;
    CreateExperiment() {
      JFileChooser chooser = new JFileChooser();
      validate = new JCheckBox("Validate with Schema");
      validate.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          toggleValidate();
        }
      });

      ChooserUtils utils = new ChooserUtils();
      chooser.setAccessory(validate);
      chooser.addChoosableFileFilter(utils.getFileFilter("xml", "Configuration files (*.xml)"));
      int option = chooser.showOpenDialog(CSMARTConsoleView.this);
      if (option == JFileChooser.APPROVE_OPTION) {
        XMLExperiment exp = new XMLExperiment(chooser.getSelectedFile(), CSMARTConsoleView.this);

        try {
          exp.doParse(isValidating);
          model.setXMLFile(chooser.getSelectedFile());
          if(isValidating) {
            // Since we cannot modify XML, if it has been validated once, don't validate it again.
            NodeComponent nodes[] = exp.getNodeComponents();
            for (int i = 0; i < nodes.length; i++) {
              NodeComponent node = nodes[i];
              node.addArgument("org.cougaar.core.node.validate", "false");
            }
          }
          model.setExperiment(exp);

        } catch(Exception e) {
          JOptionPane.showMessageDialog(CSMARTConsoleView.this, "Parse Failed, or cancel was pushed. " +
              "See logs for details", "Failure", JOptionPane.ERROR_MESSAGE);
        }
      }
    }

    private void toggleValidate() {
      isValidating = validate.isSelected();
    }
  }
}


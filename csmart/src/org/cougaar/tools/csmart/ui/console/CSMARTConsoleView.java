package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * org.cougaar.tools.csmart.ui.console
 *
 */
public class CSMARTConsoleView extends JFrame implements Observer {

  private transient Logger log;
  private ConsoleDesktop desktop;
  private CSMARTConsoleModel model;

  // gui controls
  private ButtonGroup statusButtons;
  private JToggleButton attachButton;
  private JMenuItem attachMenuItem; // same as attachButton
  private JMenuItem addGLSMenuItem; // call addGLSWindow
  private JToggleButton runButton;
  private JToggleButton stopButton;
  //  private JToggleButton invisButton;  // Used to turn off both run and stop button.
  //  private ButtonGroup runningState;
  private JPanel buttonPanel; // contains status buttons
  private JPopupMenu nodeMenu; // pop-up menu on node status button
  private Legend legend;

  private JMenuItem deleteMenuItem;
  private JMenuItem displayMenuItem;
  private JMenuItem killAllMenuItem;

  private static Dimension HGAP10 = new Dimension(10, 1);
  private static Dimension HGAP5 = new Dimension(5, 1);
  private static Dimension VGAP30 = new Dimension(1, 30);

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

    // enable run button if have experiment with at least one host, node, and agent
    initRunButton();
    updateASControls();

    // add a WindowListener: Do an exit to kill the Nodes if this window is closing
    // Note that the main CSMART UI handles actually disposing this frame
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        exit(e);
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
    attachButton.setEnabled(true);
    descriptionPanel.add(attachButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));
    runButton = new JToggleButton("Run");
    runButton.setToolTipText("Start running experiment");
    runButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        runStateChanged();
      }
    });
    runButton.setFocusPainted(false);
    descriptionPanel.add(runButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));

    //    invisButton = new JToggleButton("Invisible");
    stopButton = new JToggleButton("Stop");
    stopButton.setToolTipText("Halt experiment at end of current");
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        runStateChanged();
      }
    });
    stopButton.setFocusPainted(false);
    descriptionPanel.add(stopButton);
    descriptionPanel.add(Box.createRigidArea(HGAP5));

    // Sets up a ButtonGroup to handle toggling between buttons.
    //    runningState = new ButtonGroup();
    //    runningState.add(runButton);
    //    runningState.add(stopButton);
    //    runningState.add(invisButton);

    // create progress panel for time labels
    // these are referenced elsewhere, so are created even if not displayed
    JPanel runProgressPanel = createHorizontalPanel(false);
    final JLabel experimentTimeLabel = new JLabel("Experiment: 00:00:00");
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

    // create tabbed panes for configuration information (not editable)
//    if (experiment != null) {
//      hostConfiguration = new HostConfigurationBuilder(experiment, null);
//      hostConfiguration.update(); // display configuration
//      hostConfiguration.addHostTreeSelectionListener(myTreeListener);
//      JInternalFrame jif = new JInternalFrame(configWindowTitle,
//                                              true, false, true, true);
//      jif.getContentPane().add(hostConfiguration);
//      jif.setSize(660, 400);
//      jif.setLocation(0, 0);
//      jif.setVisible(true);
//      desktop.add(jif, JLayeredPane.DEFAULT_LAYER);
//    }
    panel.add(desktop,
              new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.BOTH,
                                     new Insets(0, 0, 0, 0),
                                     0, 0));
    return panel;
  }


  private JMenuBar createMenu() {
    // top level menus
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.setToolTipText("Exit this tool.");
    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.setToolTipText("Exit this tool.");
    exitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        exit(e);
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
//        hostConfiguration.findHost();
      }
    });
    findMenu.add(findHostMenuItem);
    JMenuItem findNodeMenuItem = new JMenuItem(FIND_NODE_MENU_ITEM);
    findNodeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//        hostConfiguration.findNode();
      }
    });
    findMenu.add(findNodeMenuItem);
    JMenuItem findAgentMenuItem = new JMenuItem(FIND_AGENT_MENU_ITEM);
    findAgentMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//        hostConfiguration.findAgent();
      }
    });
    findMenu.add(findAgentMenuItem);
//    if (experiment == null) {
//      findHostMenuItem.setEnabled(false);
//      findNodeMenuItem.setEnabled(false);
//      findAgentMenuItem.setEnabled(false);
//    }

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
        //        model.getAppServerSupport().addAppServer();
        //        updateASControls();
        addAppServer();
      }
    });
    addMenuItem.setToolTipText("Add an Application Server.");
    appServerMenu.add(addMenuItem);

    deleteMenuItem = new JMenuItem(DELETE_APP_SERVER_ITEM);
    deleteMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //        model.getAppServerSupport().deleteAppServers();
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

    JMenuItem refreshMenuItem = new JMenuItem(REFRESH_APP_SERVER_ITEM);
    refreshMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //        model.getAppServerSupport().refreshAppServers();
        //        updateASControls();
        // test the AS
        //        noticeIfServerDead();
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
//        addGLSWindow(findServlet());
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
    } else {
      displayMenuItem.setEnabled(true);
      deleteMenuItem.setEnabled(true);
    }
    if (model.getUnattachedNodes().size() == 0) {
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
    // TODO: determine when to enable/disable GLSMenuItem
//        if (glsClient == null)
//          addGLSMenuItem.setEnabled(true);
//        else {
//          // Bug 2258 workaround
//          //	  addGLSMenuItem.setEnabled(false);
//          if (log.isInfoEnabled())
//            log.info("Bug 2258 workaround. Have Valid AppServers & running nodes. But glsClient is non-null. Enabling menu item anyhow.");
//          addGLSMenuItem.setEnabled(true);
//        }

  }

  // TODO: Make this initRun listener based.
  /**
   * Enable run button if experiment has at least one host that has at least
   * one node to run which has at least one agent to run.
   * Called from initialization.
   */
  private void initRunButton() {
//    if (experiment != null) {
//      HostComponent[] hosts = experiment.getHostComponents();
//      for (int i = 0; i < hosts.length; i++) {
//        NodeComponent[] nodes = hosts[i].getNodes();
//        if (nodes != null && nodes.length > 0) {
//	  // Bug 1763: Perhaps allow running a society with just Nodes, no Agents?
//          for (int j = 0; j < nodes.length; j++) {
//            AgentComponent[] agents = nodes[j].getAgents();
//            if (agents != null && agents.length > 0) {
//              runButton.setEnabled(true);
//              return;
//            }
//          }
//        }
//      }
//    }

    // Don't enable the stop button until a run is started.
    stopButton.getModel().setEnabled(false);

    // FIXME runButton.setEnabled(false);
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


  private void displayAboutNode() {
    if (log.isDebugEnabled()) {
      log.debug("In dsiplayAboutNode");
    }


  }

  private void resetNodeStatus() {
    if (log.isDebugEnabled()) {
      log.debug("In resetNodeStatus");
    }


  }

  private void viewSizeMenuItem_actionPerformed() {
    if (log.isDebugEnabled()) {
      log.debug("In viewSizeMenuItem_actionPerformed");
    }


  }

  private void filterMenuItem_actionPerformed() {
    if (log.isDebugEnabled()) {
      log.debug("In filterMenuItem_actionPerformed");
    }

  }

  private void formatMenuItem_actionPerformed() {
    if (log.isDebugEnabled()) {
      log.debug("In formatMenuItem_actionPerformed");
    }


  }

  private void setNotifyMenuItem_actionPerformed() {
    if (log.isDebugEnabled()) {
      log.debug("In setNotifyMenuItem_actionPerformed");
    }


  }

  private void viewNotifyMenuItem_actionPerformed() {
    if (log.isDebugEnabled()) {
      log.debug("In viewNotifyMenuItem_actionPerformed");
    }


  }

  private void removeNotifyMenuItem_actionPerformed() {
    if (log.isDebugEnabled()) {
      log.debug("In removeNotifyMenuItem_actionPerformed");
    }


  }

  private void resetNotifyMenuItem_actionPerformed() {
    if (log.isDebugEnabled()) {
      log.debug("In resetNotifyMenuItem_actionPerformed");
    }

  }

  private void killAll_actionPerformed() {
    if (log.isDebugEnabled()) {
      log.debug("In killAll_actionPerformed");
    }

  }

  // When user hits Exit
  private void exit(AWTEvent e) {
    if (log.isDebugEnabled()) {
      log.debug("In exitMenuItem_actionPerformed");
    }
    if (model.haveAttached()) {
      ExitConsole ec = new ExitConsole();
      if (ec.reallyExit()) {
        if (ec.getResult().equals(ec.SOCIETY_DETACH)) {
          model.detachFromSociety();
        } else if (ec.getResult().equals(ec.KILL_NODES)) {
          model.killAllNodes();
        }
      } else {
        return;
      }
    }

    // If this was this frame's exit menu item, we have to remove
    // the window from the list
    // if it was a WindowClose, the parent notices this as well
    if (e instanceof ActionEvent) {
      NamedFrame.getNamedFrame().removeFrame(this);
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Not doing a removeFrame: event was " + e);
      }
    }
//
//    // remove listeners from this window
//    WindowListener[] lists = getWindowListeners();
//    for (int i = 0; i < lists.length; i++)
//      removeWindowListener(lists[i]);

//    dispose();
//    System.exit(0);

  }

  /**
   * Get a list of unattached nodes and display these to the user.
   * Message the model to attach to each node.
   */
  private void attachStateChanged() {
    //    model.setAttached(attachButton.getModel().isSelected());
    ArrayList nodes = model.getUnattachedNodes();
    Object[] selected =
        Util.getObjectsFromList(null, nodes,
                                "Attach to Nodes", "Select Nodes:");
    if (selected != null) {
      ArrayList sel = new ArrayList(selected.length);
      for (int i = 0; i < selected.length; i++)
        model.attachToNode((String) sel.get(i));
    }
  }

  //  private void attachStateChanged() {
  //    model.setAttached(attachButton.getModel().isSelected());
  //  }

  private void runStateChanged() {
    model.setRunning(runButton.getModel().isSelected());
  }

  // End Listeners

  public static void main(String[] args) {
    CSMARTConsoleView ccv = new CSMARTConsoleView(null);
  }


  public void update(Observable o, Object arg) {
    if (arg.equals(CSMARTConsoleModel.TOGGLE_RUNNING_STATE)) {
      //      if(!model.isRunning()) {
      //        invisButton.getModel().setPressed(true);
      //      }
      if (runButton.getModel().isEnabled() &&
          !stopButton.getModel().isEnabled()) {
        stopButton.getModel().setEnabled(true);
      }
    }

    if (arg.equals(CSMARTConsoleModel.RUN_FAILED)) {
      // this should "pop the button back up" indicating its selectable
      runButton.getModel().setPressed(false);
    }

    if (arg.equals(CSMARTConsoleModel.TOGGLE_ATTACHED_STATE)) {

    }

    if (arg.equals(CSMARTConsoleModel.APP_SERVERS_CHANGED) ||
        arg.equals(CSMARTConsoleModel.APP_SERVER_ADDED) ||
        arg.equals(CSMARTConsoleModel.APP_SERVER_DELETED) ||
        arg.equals(CSMARTConsoleModel.NODE_ADDED)) {
      updateASControls();
    }

    if (arg instanceof NodeView) {
      // TODO: second argument should be node name
      desktop.addNodeFrame((NodeView)arg, "");
      return;
    }
  }

  /**
   * Query the user for the host and port of the remote app server.
   * Message the model to try to add this app server.
   */
  private void addAppServer() {
    int port = CSMARTConsoleModel.APP_SERVER_DEFAULT_PORT;
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


}

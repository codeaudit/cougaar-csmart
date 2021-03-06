/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.console;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.tools.server.system.ProcessStatus;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Vector;

/**
 * org.cougaar.tools.csmart.ui.console
 *
 */
// This is essentially ConsoleInternalFrame, but
// created from the new CSMARTConsoleModel code

public class NodeView extends JInternalFrame implements Observer {
  private static final String NODE_MENU = "Node";
  private static final String INFO_ACTION = "Info";
  private static final String CPU_USAGE_ACTION = "Machine CPU Usage";
  private static final String NODE_CPU_USAGE_ACTION = "Node CPU Usage";

  private static final String EDIT_MENU = "Edit";
  private static final String CUT_ACTION = "Cut";
  private static final String COPY_ACTION = "Copy";
  private static final String PASTE_ACTION = "Paste";
  private static final String SELECT_ALL_ACTION = "Select All";
  private static final String FIND_ACTION = "Find...";
  private static final String FIND_NEXT_ACTION = "Find Next";

  private static final String VIEW_MENU = "View";
  private static final String SET_VIEW_SIZE_ACTION = "Set View Size...";
  private static final String FILTER_ACTION = "Filter...";

  private static final String CONTROL_MENU = "Control";
  private static final String START_ACTION = "Restart";
  private static final String STOP_ACTION = "Kill";
  private static final String STACK_TRACE_ACTION = "Stack Trace";

  private static final String NOTIFY_MENU = "Notify";
  private static final String SET_NOTIFY_ACTION = "Set Notification...";
  private static final String REMOVE_NOTIFY_ACTION = "Remove Notification";
  private static final String NOTIFY_NEXT_ACTION = "Find Next Notification";
  private static final String RESET_NOTIFY_ACTION = "Reset Notification";

  private ConsoleTextPane consoleTextPane;
  private Action findAction;
  private Action findNextAction;
  private Action notifyAction;
  private Action notifyNextAction;
  private Action startAction;
  private Action stopAction;
  private Action traceAction;

  private transient Logger log;

  private NodeModel model;
  private JScrollPane scrollPane;

  private String nodeName;
  private String hostName;
  private Properties properties;
  private java.util.List args;
  private ConsoleNodeListener listener;
  private NodeStatusButton statusButton;
  private String logFileName;
  private RemoteProcess remoteNode;

  public NodeView(NodeModel model) {
    super("", // title
          true, //resizable
          false, //not closable, because they can't be recreated
          true, //maximizable
          true);//iconifiable

    this.model = model;
    initGui();
  }

  private void initGui() {
    scrollPane = new JScrollPane(model.getTextPane());
    nodeName = model.getNodeName();
    NodeInfo info = model.getInfo();
    hostName = info.getHostName();
    properties = info.getProperties();
    args = info.getArgs();
    listener = model.getListener();
    statusButton = model.getStatusButton();
    logFileName = model.getLogFileName();
    remoteNode = null;
    try {
      remoteNode = info.getAppServer().getRemoteProcess(nodeName);
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Trying to get remote process:", e);
      }
    }
    createLogger();
    consoleTextPane = (ConsoleTextPane) scrollPane.getViewport().getView();
    setTitle("Node " + nodeName + " (" + hostName + ")");
    JMenuBar menuBar = new JMenuBar();

    // Node menu
    JMenu nodeMenu = new JMenu(NODE_MENU);
    Action infoAction = new AbstractAction(INFO_ACTION) {
      public void actionPerformed(ActionEvent e) {
        displayAbout();
      }
    };
    nodeMenu.add(infoAction);
    Action cpuUsageAction = new AbstractAction(CPU_USAGE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        totalCPUUsage_actionPerformed();
      }
    };
    nodeMenu.add(cpuUsageAction);
    Action nodeCPUUsageAction = new AbstractAction(NODE_CPU_USAGE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        nodeCPUUsage_actionPerformed();
      }
    };
    nodeMenu.add(nodeCPUUsageAction);
    // Edit menu
    JMenu editMenu = new JMenu(EDIT_MENU);
    Action cutAction = new AbstractAction(CUT_ACTION) {
      public void actionPerformed(ActionEvent e) {
        cut_actionPerformed();
      }
    };
    JMenuItem mi = editMenu.add(cutAction);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                                             Event.CTRL_MASK));
    Action copyAction = new AbstractAction(COPY_ACTION) {
      public void actionPerformed(ActionEvent e) {
        copy_actionPerformed();
      }
    };
    mi = editMenu.add(copyAction);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                             Event.CTRL_MASK));
    Action pasteAction = new AbstractAction(PASTE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        paste_actionPerformed();
      }
    };
    mi = editMenu.add(pasteAction);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                             Event.CTRL_MASK));
    editMenu.addSeparator();
    Action selectAllAction = new AbstractAction(SELECT_ALL_ACTION) {
      public void actionPerformed(ActionEvent e) {
        selectAll_actionPerformed();
      }
    };
    mi = editMenu.add(selectAllAction);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                                             Event.CTRL_MASK));
    editMenu.addSeparator();
    findAction = new AbstractAction(FIND_ACTION) {
      public void actionPerformed(ActionEvent e) {
        find_actionPerformed();
      }
    };
    mi = editMenu.add(findAction);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                             Event.CTRL_MASK));
    findNextAction = new AbstractAction(FIND_NEXT_ACTION) {
      public void actionPerformed(ActionEvent e) {
        findNext_actionPerformed();
      }
    };
    mi = editMenu.add(findNextAction);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
                                             Event.CTRL_MASK));
    // view menu
    JMenu viewMenu = new JMenu(VIEW_MENU);
    Action setViewSizeAction = new AbstractAction(SET_VIEW_SIZE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        setViewSize_actionPerformed();
      }
    };
    viewMenu.add(setViewSizeAction);
    Action filterAction = new AbstractAction(FILTER_ACTION) {
      public void actionPerformed(ActionEvent e) {
        filter_actionPerformed();
      }
    };
    viewMenu.add(filterAction);

    // control menu
    JMenu controlMenu = new JMenu(CONTROL_MENU);
    startAction = new AbstractAction(START_ACTION) {
      public void actionPerformed(ActionEvent e) {
        model.restart();
      }
    };
    startAction.setEnabled(false);
    controlMenu.add(startAction);

    stopAction = new AbstractAction(STOP_ACTION) {
      public void actionPerformed(ActionEvent e) {
        model.stop();
      }
    };
    stopAction.setEnabled(false);
    controlMenu.add(stopAction);

    traceAction = new AbstractAction(STACK_TRACE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        trace_actionPerformed();
      }
    };
    traceAction.setEnabled(false);
    controlMenu.add(traceAction);

    // notify menu
    JMenu notifyMenu = new JMenu(NOTIFY_MENU);
    notifyAction = new AbstractAction(SET_NOTIFY_ACTION) {
      public void actionPerformed(ActionEvent e) {
        notify_actionPerformed();
      }
    };
    notifyMenu.add(notifyAction);
    Action removeNotifyAction = new AbstractAction(REMOVE_NOTIFY_ACTION) {
      public void actionPerformed(ActionEvent e) {
        removeNotify_actionPerformed();
      }
    };
    notifyMenu.add(removeNotifyAction);
    notifyNextAction = new AbstractAction(NOTIFY_NEXT_ACTION) {
      public void actionPerformed(ActionEvent e) {
        notifyNext_actionPerformed();
      }
    };
    mi = notifyMenu.add(notifyNextAction);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                             Event.CTRL_MASK));
    Action resetNotifyAction = new AbstractAction(RESET_NOTIFY_ACTION) {
      public void actionPerformed(ActionEvent e) {
        resetNotify();
      }
    };
    notifyMenu.add(resetNotifyAction);

    menuBar.add(nodeMenu);
    menuBar.add(editMenu);
    menuBar.add(viewMenu);
    menuBar.add(controlMenu);
    menuBar.add(notifyMenu);
    getRootPane().setJMenuBar(menuBar);
    initKeyMap(consoleTextPane);
    getContentPane().add(scrollPane);
    setSize(300, 300);
    updateMenuItems(model.getState()); // get the menu items initted
    model.addObserver(this);
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Set up a keymap:
   * ctrl-f find  (find)
   * ctrl-g find next
   * ctrl-n notify
   * ctrl-o notify next
   */
  private void initKeyMap(ConsoleTextPane pane) {
    InputMap im = pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap am = pane.getActionMap();
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK),
           FIND_ACTION);
    am.put(FIND_ACTION, findAction);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK),
           FIND_NEXT_ACTION);
    am.put(FIND_NEXT_ACTION, findNextAction);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK),
           NOTIFY_NEXT_ACTION);
    am.put(NOTIFY_NEXT_ACTION, notifyNextAction);
  }

  public String getNodeName() {
    return nodeName;
  }

  /**
   * Display information about node in pop-up dialog.
   * Colloquially the "Node Info" window.
   */
  public void displayAbout() {
    ArrayList agentNames =
        (ArrayList) model.getNodePropertyValue(nodeName, "AgentNames");

    // If got no Agent Names, that probably means
    // we have no configuration info for the Node,
    // so we won't display that portion of
    // the info window
    if (agentNames != null) {
      // clone the agent names so we don't modify them when we add the NodeAgent
      agentNames = (ArrayList) agentNames.clone();
      agentNames.add(0, nodeName);
    }

    JPanel aboutPanel = new JPanel();
    aboutPanel.setLayout(new GridBagLayout());
    int x = 0;
    int y = 0;

    // insets are top, left, bottom, right
    aboutPanel.add(new JLabel("Status:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(10, 0, 5, 5),
                                          0, 0));
    String status = (statusButton).getMyModel().getStatusDescription();
    aboutPanel.add(new JLabel(status),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(10, 0, 5, 0),
                                          0, 0));
    x = 0;
    aboutPanel.add(new JLabel("Notifications:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    int notifyCount = consoleTextPane.getNotifyCount();
    aboutPanel.add(new JLabel(Integer.toString(notifyCount)),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;
    aboutPanel.add(new JLabel("Log File:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    aboutPanel.add(new JTextField(logFileName),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;
    aboutPanel.add(new JLabel("Host Name:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    aboutPanel.add(new JLabel(hostName),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;
    aboutPanel.add(new JLabel("Host Address:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    String hostAddress = "";
    try {
      InetAddress host = InetAddress.getByName(hostName);
      hostAddress = host.toString();
    } catch (UnknownHostException uhe) {
    }
    aboutPanel.add(new JLabel(hostAddress),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;
    aboutPanel.add(new JLabel("Host Type:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    aboutPanel.add(new JLabel((String) model.getHostPropertyValue(hostName,
                                                                  "MachineType")),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;
    aboutPanel.add(new JLabel("Host Location:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    aboutPanel.add(new JLabel((String) model.getHostPropertyValue(hostName,
                                                                  "Location")),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;
    aboutPanel.add(new JLabel("Host Description:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    aboutPanel.add(new JLabel((String) model.getHostPropertyValue(hostName,
                                                                  "Description")),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;
    aboutPanel.add(new JLabel("Node Description: "),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    aboutPanel.add(new JLabel((String) model.getNodePropertyValue(nodeName,
                                                                  "Description")),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;

    // display -D options
    aboutPanel.add(new JLabel("-D Options (in CSMART):"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    Vector data = new Vector();
    Enumeration propertyNames = properties.propertyNames();
    while (propertyNames.hasMoreElements()) {
      String name = (String) propertyNames.nextElement();
      Vector row = new Vector(2);
      row.add(name);
      row.add(properties.getProperty(name));
      data.add(row);
    }
    Vector columnNames = new Vector(2);
    columnNames.add("Name");
    columnNames.add("Value");
    // Bug 1874: Disable editing here
    JTable argTable = new JTable(new DefaultTableModel(data, columnNames)) {
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    argTable.getTableHeader().setReorderingAllowed(false);
    argTable.setColumnSelectionAllowed(false);
    argTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    argTable.setPreferredScrollableViewportSize(new Dimension(50, 100));
    JScrollPane jspArgTable = new JScrollPane(argTable);
    // ensure the layout leaves space for the scrollbar
    jspArgTable.setMinimumSize(new Dimension(50, 50));
    aboutPanel.add(jspArgTable,
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 1.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.BOTH,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;
    aboutPanel.add(new JLabel("Command Line Arguments:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    JScrollPane jspArgs = new JScrollPane(new JList(args.toArray()));
    jspArgs.setMinimumSize(new Dimension(50, 50));
    aboutPanel.add(jspArgs,
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 1.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.BOTH,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));

    x = 0;

    // Only show initial configuration info if we had some
    if (agentNames != null) {
      // Now the contents of the Node
      aboutPanel.add(new JLabel("Agents (initial configuration):"),
                     new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 0, 5, 5),
                                            0, 0));
      aboutPanel.add(new JLabel("(select for more information)"),
                     new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(0, 0, 5, 5),
                                            0, 0));

      // Put together the list of Agents in the Node
      JList agentsList = null;
      agentsList = new JList(agentNames.toArray());
      agentsList.setBackground(aboutPanel.getBackground());

      // Allow user to select an Agent in the list and get the detailed
      // contents of that Agent in a pop-up window
      agentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      agentsList.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting())
            return;
          JList myagentsList = (JList) e.getSource();
          String agentName = (String) myagentsList.getSelectedValue();
          if (agentName != null)
            displayPlugins(agentName);
        }
      });

      JScrollPane jspAgents = new JScrollPane(agentsList);
      jspAgents.setMinimumSize(new Dimension(50, 50));
      aboutPanel.add(jspAgents,
                     new GridBagConstraints(x, y++, 1, 1, 1.0, 1.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.BOTH,
                                            new Insets(0, 0, 5, 0),
                                            0, 0));
    } // end of block to show configuration info

    JOptionPane pane = new JOptionPane(aboutPanel, JOptionPane.PLAIN_MESSAGE);
    JInternalFrame infoFrame = pane.createInternalFrame(this, "Information: "  + nodeName + " (" + hostName + ")");
    infoFrame.setSize(new Dimension(500, 500));
    infoFrame.setResizable(true);
    infoFrame.show();
  }

  // Implementations for the action items from the menu

  private void totalCPUUsage_actionPerformed() {
    try {
      displayProcessInfo(remoteNode.listProcesses(true));
    } catch (Exception e) {
      JOptionPane.showConfirmDialog(this,
                                    "This information is not available for this node.",
                                    "Information Not Available",
                                    JOptionPane.PLAIN_MESSAGE);
    }
  }

  private void nodeCPUUsage_actionPerformed() {
    try {
      displayProcessInfo(remoteNode.listProcesses(false));
    } catch (Exception e) {
      JOptionPane.showConfirmDialog(this,
                                    "This information is not available for this node.",
                                    "Information Not Available",
                                    JOptionPane.PLAIN_MESSAGE);
    }
  }

  private void displayProcessInfo(ProcessStatus[] ps) {
    String[][] psInfo = new String[ps.length][];
    for (int i = 0; i < ps.length; i++) {
      String[] singlePSInfo = new String[5];
      singlePSInfo[0] = String.valueOf(ps[i].getProcessIdentifier());
      singlePSInfo[1] = String.valueOf(ps[i].getStartTime());
      singlePSInfo[2] = String.valueOf(ps[i].getParentProcessIdentifier());
      singlePSInfo[3] = ps[i].getUserName();
      singlePSInfo[4] = ps[i].getCommand();
      psInfo[i] = singlePSInfo;
    }
    String[] columnNames = {"PID", "Start Time", "Parent PID",
                            "User Name", "Command"};
    JTable table = new JTable(psInfo, columnNames);
    JScrollPane jsp = new JScrollPane(table);
    table.setPreferredScrollableViewportSize(new Dimension(400, 100));
    JOptionPane.showConfirmDialog(this, jsp, "Process Status",
                                  JOptionPane.PLAIN_MESSAGE);
  }

  /**
   * Query user for search string and search for it.
   */

  private void find_actionPerformed() {
    // get string to search for
    String s = (String) JOptionPane.showInputDialog(this,
                                                    "Search string:",
                                                    "Search",
                                                    JOptionPane.QUESTION_MESSAGE,
                                                    null, null,
                                                    consoleTextPane.getSearchString());
    if (s == null || s.length() == 0) {
      return;
    }
    // find and highlight
    boolean found = consoleTextPane.search(s);
    findNextAction.setEnabled(found);
  }

  /**
   * Search for next instance of string.
   */

  private void findNext_actionPerformed() {
    // search and highlight
    boolean found = consoleTextPane.searchNext();
    findNextAction.setEnabled(found);
    consoleTextPane.revalidate();
    consoleTextPane.repaint();
  }

  private void cut_actionPerformed() {
    consoleTextPane.requestFocus();
    consoleTextPane.cut();
  }

  private void copy_actionPerformed() {
    consoleTextPane.requestFocus();
    consoleTextPane.copy();
  }

  private void paste_actionPerformed() {
    consoleTextPane.requestFocus();
    consoleTextPane.paste();
  }

  /**
   * Select everything in the node's output pane.
   * Note that the focus must be requested before the select action will work.
   */

  private void selectAll_actionPerformed() {
    consoleTextPane.requestFocus();
    consoleTextPane.selectAll();
  }

  private void setViewSize_actionPerformed() {
    ConsoleStyledDocument doc =
        (ConsoleStyledDocument) consoleTextPane.getStyledDocument();
    int viewSize =
        CSMARTConsoleView.displayViewSizeDialog(doc.getBufferSize());
    if (viewSize == -2)
      return; // ignore, user cancelled
    setViewSize(viewSize);
  }

  public void setViewSize(int size) {
    ConsoleStyledDocument doc =
        (ConsoleStyledDocument) consoleTextPane.getStyledDocument();
    doc.setBufferSize(size);
  }

  /**
   * Get the values from any existing filter and use those to
   * initialize the new filter.
   */
  private void filter_actionPerformed() {
    boolean[] filterValues = null;
    boolean isAllSelected = true;
    ConsoleNodeOutputFilter currentFilter = listener.getFilter();
    if (currentFilter != null) {
      filterValues = currentFilter.getValues();
      isAllSelected = currentFilter.isAllSelected();
    }
    ConsoleNodeOutputFilter filter =
        new ConsoleNodeOutputFilter(filterValues, isAllSelected);
    model.setFilter(filter);
  }

  private void trace_actionPerformed() {
    try {
      remoteNode.dumpThreads();
    } catch (Exception e) {
      JOptionPane.showConfirmDialog(this,
                                    "This operation is not available for this node.",
                                    "Operation Not Available",
                                    JOptionPane.PLAIN_MESSAGE);

    }
  }

  /**
   * Notify (by coloring status button) when the specified
   * output is received on this node.
   */

  private void notify_actionPerformed() {
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
    notifyField.setText(consoleTextPane.getNotifyCondition());
    notifyPanel.add(notifyField,
                    new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.HORIZONTAL,
                                           new Insets(10, 0, 5, 0),
                                           0, 0));
    x = 0;
    JCheckBox stdErrorCB =
        new JCheckBox("Notify on Standard Error",
                      (statusButton).getMyModel().getNotifyOnStandardError());
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
    setNotification(notifyField.getText());
    (statusButton).getMyModel().setNotifyOnStandardError(stdErrorCB.isSelected());
  }

  private void setNotification(String s) {
    String notifyCondition = null;
    if (s != null && s.length() != 0)
      notifyCondition = s;
    consoleTextPane.setNotifyCondition(notifyCondition);
    (statusButton).getMyModel().clearError();
  }

  private void removeNotify_actionPerformed() {
    setNotification(null);
    (statusButton).getMyModel().setNotifyOnStandardError(false);
    (statusButton).getMyModel().clearError();
  }

  private void notifyNext_actionPerformed() {
    boolean found = consoleTextPane.notifyNext();
    notifyNextAction.setEnabled(found);
    consoleTextPane.revalidate();
    consoleTextPane.repaint();
  }

  public void resetNotify() {
    consoleTextPane.clearNotify();
    (statusButton).getMyModel().clearError();
  }

  // Display pop-up with the INI style contents of an Agent listed
  // invoked from window displayed by displayAbout method
  private void displayPlugins(String agentName) {
    ArrayList entries =
        model.getAgentComponentDescriptions(nodeName, agentName);
    if (entries == null)
      return;
    JList plugInsList = new JList(entries.toArray());
    JScrollPane jsp =
        new JScrollPane(plugInsList,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    jsp.setPreferredSize(new Dimension(550, 200));
    JPanel agentInfoPanel = new JPanel();
    agentInfoPanel.setLayout(new GridBagLayout());
    plugInsList.setBackground(agentInfoPanel.getBackground());
    int x = 0;
    int y = 0;
    agentInfoPanel.add(new JLabel("SubComponents:"),
                       new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(10, 0, 5, 5),
                                              0, 0));
    agentInfoPanel.add(jsp,
                       new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 5, 0),
                                              0, 0));
    JOptionPane.showMessageDialog(this, agentInfoPanel,
                                  "Information: " + agentName,
                                  JOptionPane.PLAIN_MESSAGE);

  }

  /**
   * Over-ride standard dispose to ensure everything cleaned up.
   * In particular, clean up the ConsoleNodeListener,
   * and recurse to the TextPane and from there to the document
   **/
  // TODO: Determine if this is being called and handled correctly
  public void dispose() {
    // FIXME: Do this first? Will it complain if called twice?
    super.dispose();
    // Clean up the listener (and log file)
    // before the text pane (which handles the document)
    if (listener != null) {
      // FIXME: Do this?
//       if (log.isDebugEnabled())
// 	log.debug("CInternal.dipose had non-null listener to cleanUp");
      listener.cleanUp();
      listener = null;
    }
    if (consoleTextPane != null) {
//       if (log.isDebugEnabled())
// 	log.debug("CInternal had non-null pane to dispose");
      // recurse to the text pane
      // this also recurses to the document
      // which means the ConsoleNodeListener should be gone too
      // but rely on someone else to do this?
      consoleTextPane.cleanUp();
      consoleTextPane = null;
    }
    remoteNode = null;

    // Remove listeners
    InternalFrameListener[] lists = getInternalFrameListeners();
    for (int i = 0; i < lists.length; i++)
      removeInternalFrameListener(lists[i]);

    // FIXME
    // Ancestor listener? ContainerListener? FocusListener? ComponentListener?

    removeAll();
//     if (log.isDebugEnabled()) {
//       log.debug("In CInternal.dispose");
//     }
  }

  private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    createLogger();
  }

  public void update(Observable o, Object arg) {
    if (arg instanceof String && ((String) arg).startsWith("NODE_STATE"))
      updateMenuItems((String) arg);
  }

  private void updateMenuItems(String state) {
    if (state.startsWith("NODE_STATE")) {
      if (state.equals(model.STATE_RUNNING)) {
        stopAction.setEnabled(true);
        traceAction.setEnabled(true);
      } else {
        stopAction.setEnabled(false);
      }
      if (state.equals(NodeModel.STATE_STOPPED)) {
        startAction.setEnabled(true);
        traceAction.setEnabled(false);
      } else {
        startAction.setEnabled(false);
      }
    }
  }


}


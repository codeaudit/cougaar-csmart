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

import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultEditorKit;

import com.klg.jclass.chart.JCChart;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.Property;

import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.ui.experiment.HostConfigurationBuilder;

import org.cougaar.tools.server.CommunityServesClient;
import org.cougaar.tools.server.rmi.ClientCommunityController;
import org.cougaar.tools.server.ConfigurationWriter;
import org.cougaar.tools.server.HostServesClient;
import org.cougaar.tools.server.NodeEventFilter;
import org.cougaar.tools.server.NodeServesClient;
import org.cougaar.tools.server.system.ProcessStatus;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.io.ObjectInputStream;
import java.io.IOException;

public class ConsoleInternalFrame extends JInternalFrame {
  private static final String NODE_MENU = "Node";
  private static final String INFO_ACTION = "Info";
  private static final String CPU_USAGE_ACTION = "Machine CPU Usage";
  private static final String NODE_CPU_USAGE_ACTION = "Node CPU Usage";
  private static final String MEMORY_USAGE_ACTION = "Memory Usage";
  private static final String HISTORY_ACTION = "Utilization History";

  private static final String EDIT_MENU = "Edit";
  private static final String CUT_ACTION = "Cut";
  private static final String COPY_ACTION = "Copy";
  private static final String PASTE_ACTION = "Paste";
  private static final String SELECT_ALL_ACTION = "Select All";
  private static final String FIND_ACTION = "Find...";
  private static final String FIND_NEXT_ACTION = "Find Next";

  private static final String VIEW_MENU = "View";
  private static final String DISPLAY_LOG_ACTION = "Show Entire Log";
  private static final String SET_VIEW_SIZE_ACTION = "Set View Size...";
  private static final String FILTER_ACTION = "Filter...";

  private static final String CONTROL_MENU = "Control";
  private static final String START_ACTION = "Restart";
  private static final String MOVE_ACTION = "Move";
  private static final String STOP_ACTION = "Kill";
  private static final String STACK_TRACE_ACTION = "Stack Trace";

  private static final String NOTIFY_MENU = "Notify";
  private static final String SET_NOTIFY_ACTION = "Set Notification...";
  private static final String REMOVE_NOTIFY_ACTION = "Remove Notification";
  private static final String NOTIFY_NEXT_ACTION = "Find Next Notification";
  private static final String RESET_NOTIFY_ACTION = "Reset Notification";

  private NodeComponent node;
  private ConsoleNodeListener listener;
  private ConsoleTextPane consoleTextPane;
  private Action findAction;
  private Action findNextAction;
  private Action notifyAction;
  private Action notifyNextAction;
  private Action startAction;
  private Action stopAction;
  private HostComponent host;
  private String hostName;
  private JRadioButton statusButton;
  private String logFileName;
  private ConsoleNodeOutputFilter filter;
  private NodeServesClient nodeServer;
  private CSMARTConsole console;
  private Experiment experiment;

  private transient Logger log;

  public ConsoleInternalFrame(NodeComponent node, 
                              ConsoleNodeListener listener,
                              JScrollPane pane,
                              JRadioButton statusButton,
                              String logFileName,
                              NodeServesClient nodeServer,
                              CSMARTConsole console) {
    super("",   // title
          true, //resizable
          false, //not closable, because they can't be recreated
          true, //maximizable
          true);//iconifiable
    this.node = node;
    this.listener = listener;
    this.statusButton = statusButton;
    this.logFileName = logFileName;
    this.nodeServer = nodeServer;
    this.console = console;

    createLogger();

    consoleTextPane = (ConsoleTextPane)pane.getViewport().getView();
    // get host component by getting the experiment and 
    // searching its hosts for one with this node.
    experiment = 
      (Experiment)getPropertyValue(node, "Experiment");
    HostComponent[] hosts = experiment.getHosts();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
        if (nodes[j].equals(node)) {
          host = hosts[i];
          hostName = host.getShortName();
          break;
        }
      }
    }
    // title is "Node name (host name)"
    setTitle("Node " + node.getShortName() + " (" + hostName + ")");
    // init menubar
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
    Action memoryUsageAction = new AbstractAction(MEMORY_USAGE_ACTION) {
      public void actionPerformed(ActionEvent e) {
      }
    };
    memoryUsageAction.setEnabled(false);
    nodeMenu.add(memoryUsageAction);
    Action historyAction = new AbstractAction(HISTORY_ACTION) {
      public void actionPerformed(ActionEvent e) {
	history_actionPerformed();
      }
    };
    nodeMenu.add(historyAction);

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
    Action displayLogAction = new AbstractAction(DISPLAY_LOG_ACTION) {
      public void actionPerformed(ActionEvent e) {
        displayLog_actionPerformed();
      }
    };
    displayLogAction.setEnabled(false);
    viewMenu.add(displayLogAction);
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
        restart_actionPerformed();
      }
    };
    startAction.setEnabled(false);
    controlMenu.add(startAction);
    Action moveAction = new AbstractAction(MOVE_ACTION) {
      public void actionPerformed(ActionEvent e) {
      }
    };
    moveAction.setEnabled(false);
    controlMenu.add(moveAction);
    stopAction = new AbstractAction(STOP_ACTION) {
      public void actionPerformed(ActionEvent e) {
        stop_actionPerformed();
      }
    };
    controlMenu.add(stopAction);
    Action traceAction = new AbstractAction(STACK_TRACE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        trace_actionPerformed();
      }
    };
    controlMenu.add(traceAction);

    // notify menu
    JMenu notifyMenu = new JMenu(NOTIFY_MENU);
    notifyAction = new AbstractAction(SET_NOTIFY_ACTION) {
      public void actionPerformed(ActionEvent e) {
	notify_actionPerformed();
      }
    };
    mi = notifyMenu.add(notifyAction);
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
	resetNotifyAction_actionPerformed();
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
    getContentPane().add(pane);
    setSize(300,300);
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

  public NodeComponent getNodeComponent() {
    return node;
  }

  /**
   * Display information about node in pop-up dialog.
   */

  public void displayAbout() {
    final ArrayList agentNames = 
      (ArrayList)getPropertyValue(node, "AgentNames");
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
    String status = ((NodeStatusButton)statusButton).getStatusDescription();
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
    aboutPanel.add(new JLabel((String)getPropertyValue(host, "MachineType")),
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
    aboutPanel.add(new JLabel((String)getPropertyValue(host, "Location")),
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
    aboutPanel.add(new JLabel((String)getPropertyValue(host, "Description")),
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
    aboutPanel.add(new JLabel((String)getPropertyValue(node, "Description")),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;
    // display -D options
    aboutPanel.add(new JLabel("-D Options:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    Properties properties = console.getNodeMinusD(node);
    Vector data = new Vector();
    //    Properties properties = node.getArguments();
    Enumeration propertyNames = properties.propertyNames();
    while (propertyNames.hasMoreElements()) {
      String name = (String)propertyNames.nextElement();
      Vector row = new Vector(2);
      row.add(name);
      row.add(properties.getProperty(name));
      data.add(row);
    }
    Vector columnNames = new Vector(2);
    columnNames.add("Name");
    columnNames.add("Value");
    JTable argTable = new JTable(new DefaultTableModel(data, columnNames));
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
    JTextArea args = new JTextArea(5, 40);
    Properties props = node.getArguments();
    String commandArguments =
      props.getProperty(CSMARTConsole.COMMAND_ARGUMENTS);
    if (commandArguments == null)
      commandArguments = "";
    args.setText(commandArguments);
    JScrollPane jspArgs = new JScrollPane(args);
    jspArgs.setMinimumSize(new Dimension(50, 50));
    aboutPanel.add(jspArgs,
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 1.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.BOTH,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;
    aboutPanel.add(new JLabel("Agents:"),
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
    JList agentsList = new JList(agentNames.toArray());
    agentsList.setBackground(aboutPanel.getBackground());
    agentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    agentsList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
          return;
        String agentName = (String)agentNames.get(e.getLastIndex());
        displayPlugIns(agentName);
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
    JOptionPane.showMessageDialog(this, aboutPanel, 
                                  "Information: " + node.getShortName() + 
                                  " (" + hostName + ")",
                                  JOptionPane.PLAIN_MESSAGE);
  }

  private Object getPropertyValue(BaseComponent component, String name) {
    Property prop = component.getProperty(name);
    if (prop == null)
      return null;
    return prop.getValue();
  }

  // Implementations for the action items from the menu

  private void totalCPUUsage_actionPerformed() {
    try {
      displayProcessInfo(nodeServer.listProcesses(true));
    } catch (Exception e) {
      JOptionPane.showConfirmDialog(this, 
                         "This information is not available for this node.", 
                                    "Information Not Available",
                                    JOptionPane.PLAIN_MESSAGE);
    }
  }

  private void nodeCPUUsage_actionPerformed() {
    try {
      displayProcessInfo(nodeServer.listProcesses(false));
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
    String[] columnNames = { "PID", "Start Time", "Parent PID",
                             "User Name", "Command" };
    JTable table = new JTable(psInfo, columnNames);
    JScrollPane jsp = new JScrollPane(table);
    table.setPreferredScrollableViewportSize(new Dimension(400, 100));
    JOptionPane.showConfirmDialog(this, jsp, "Process Status", 
                                  JOptionPane.PLAIN_MESSAGE);
  }

  /**
   * Display strip chart for node.
   */

  private void history_actionPerformed() {
    JCChart chart = new StripChart();
    StripChartSource chartDataModel = new StripChartSource(chart);
    ((StripChart)chart).init(chartDataModel);
    listener.setIdleChart(chart, chartDataModel);
    JInternalFrame chartFrame = new StripChartFrame(chart, title);
    ConsoleDesktop desktop = (ConsoleDesktop)getDesktopPane();
    desktop.addFrame(chartFrame, false);
  }

  /**
   * Query user for search string and search for it.
   */

  private void find_actionPerformed() {
    // get string to search for
    String s = (String)JOptionPane.showInputDialog(this,
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

  private void displayLog_actionPerformed() {
    listener.fillFromLogFile();
  }

  private void setViewSize_actionPerformed() {
    ConsoleStyledDocument doc = 
      (ConsoleStyledDocument)consoleTextPane.getStyledDocument();
    int viewSize = CSMARTConsole.displayViewSizeDialog(doc.getBufferSize());
    doc.setBufferSize(viewSize);
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
      new ConsoleNodeOutputFilter(console, filterValues, isAllSelected);
    listener.setFilter(filter);
  }

  /**
   * Enable restart command after nodeStopped message has been received
   * by the node listener.
   */

  public void enableRestart(boolean enable) {
    startAction.setEnabled(enable);
    stopAction.setEnabled(!enable);
  }

  private void restart_actionPerformed() {
    startAction.setEnabled(false);
    NodeServesClient newNodeServer = console.restartNode(node);
    if (newNodeServer != null) {
      nodeServer = newNodeServer;
      stopAction.setEnabled(true);
    }
  }

  private void stop_actionPerformed() {
    stopAction.setEnabled(false);
    console.stopNode(node);
  }

  private void trace_actionPerformed() {
    try {
      nodeServer.dumpThreads();
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
             ((NodeStatusButton)statusButton).getNotifyOnStandardError());
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
    setNotification(notifyField.getText());
    ((NodeStatusButton)statusButton).setNotifyOnStandardError(stdErrorCB.isSelected());
  }

  private void setNotification(String s) {
    String notifyCondition = null;
    if (s != null && s.length() != 0)
      notifyCondition = s;
    consoleTextPane.setNotifyCondition(notifyCondition);
    ((NodeStatusButton)statusButton).clearError();
  }

  private void removeNotify_actionPerformed() {
    setNotification(null);
    ((NodeStatusButton)statusButton).setNotifyOnStandardError(false);
    ((NodeStatusButton)statusButton).clearError();
  }

  private void notifyNext_actionPerformed() {
    boolean found = consoleTextPane.notifyNext();
    //    notifyNextAction.setEnabled(found);
    consoleTextPane.revalidate();
    consoleTextPane.repaint();
  }

  private void resetNotifyAction_actionPerformed() {
    consoleTextPane.clearNotify();
    ((NodeStatusButton)statusButton).clearError();
  }

  private void displayPlugIns(String agentName) {
    ComponentData societyComponentData = experiment.getSocietyComponentData();
    if (societyComponentData == null) {
      if(log.isWarnEnabled()) {
        log.warn("ConsoleInternalFrame: Need to save experiment");
      }
      return;
    }
    ComponentData[] children = societyComponentData.getChildren();
    ComponentData nodeComponentData = null;
    for (int i = 0; i < children.length; i++) {
      if (children[i].getType().equals(ComponentData.HOST)) {
        ComponentData[] nodes = children[i].getChildren();
        for (int j = 0; j < nodes.length; j++) {
          if (nodes[j].getName().equals(node.getShortName())) {
            nodeComponentData = nodes[j];
            break;
          }
        }
      }
    }
    if (nodeComponentData == null)
      return;
    ComponentData[] agents = nodeComponentData.getChildren();
    ComponentData agentComponentData = null;
    for (int i = 0; i < agents.length; i++) {
      if (agents[i] instanceof AgentComponentData &&
          agents[i].getName().equals(agentName)) {
        agentComponentData = agents[i];
        break;
      }
    }
    if (agentComponentData == null)
      return;
    ComponentData[] agentChildren = agentComponentData.getChildren();
    ArrayList entries = new ArrayList(agentChildren.length);
    for (int i = 0; i < agentChildren.length; i++) {
      if (!agentChildren[i].getType().equals(ComponentData.PLUGIN))
        continue;
      StringBuffer sb = new StringBuffer();
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
    JList plugInsList = new JList(entries.toArray());
    JScrollPane jsp = new JScrollPane(plugInsList);
    jsp.setMinimumSize(new Dimension(50, 50));
    JPanel agentInfoPanel = new JPanel();
    agentInfoPanel.setLayout(new GridBagLayout());
    plugInsList.setBackground(agentInfoPanel.getBackground());
    int x = 0;
    int y = 0;
    agentInfoPanel.add(new JLabel("PlugIns:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(10, 0, 5, 5),
                                          0, 0));
    agentInfoPanel.add(jsp,
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    JOptionPane.showMessageDialog(this, agentInfoPanel, 
                                  "Information: " + agentName,
                                  JOptionPane.PLAIN_MESSAGE);

  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}





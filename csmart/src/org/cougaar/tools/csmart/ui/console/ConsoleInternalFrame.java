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

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

import com.klg.jclass.chart.JCChart;

import org.cougaar.tools.csmart.ui.component.ComponentName;
import org.cougaar.tools.csmart.ui.component.ConfigurableComponent;
import org.cougaar.tools.csmart.ui.component.HostComponent;
import org.cougaar.tools.csmart.ui.component.NodeComponent;
import org.cougaar.tools.csmart.ui.component.Property;
import org.cougaar.tools.csmart.ui.experiment.Experiment;

public class ConsoleInternalFrame extends JInternalFrame {
  private static final String ABOUT_MENU = "About";
  private static final String ABOUT_ACTION = "About";
  private static final String CPU_USAGE_ACTION = "CPU Usage";
  private static final String MEMORY_USAGE_ACTION = "Memory Usage";
  private static final String CONTROL_MENU = "Control";
  private static final String START_ACTION = "Restart";
  private static final String STOP_ACTION = "Kill";
  private static final String MOVE_ACTION = "Move";
  private static final String TRACE_ACTION = "Stack Trace";
  private static final String SEARCH_MENU = "Search";
  private static final String SEARCH_ACTION = "Search...";
  private static final String SEARCH_NEXT_ACTION = "Search Next";
  private static final String STATUS_MENU = "Status";
  private static final String HISTORY_ACTION = "Utilization History";
  private static final String DISPLAY_MENU = "Display";
  private static final String DISPLAY_LOG_ACTION = "Display Log";
  private static final String SELECT_ALL_ACTION = "Select All";
  private static final String FILTER_ACTION = "Filter...";
  private static final String NOTIFY_MENU = "Notify";
  private static final String NOTIFY_ACTION = "Notify When...";
  private static final String NOTIFY_NEXT_ACTION = "Notify Next";
  private static int openFrameCount = 0;
  private static final int xOffset = 30, yOffset = 30;
  private NodeComponent node;
  private ConsoleNodeListener listener;
  private ConsoleTextPane consoleTextPane;
  private Action searchAction;
  private Action searchNextAction;
  private Action notifyAction;
  private Action notifyNextAction;
  private HostComponent host;
  private String hostName;
  private String notifyCondition;
  private JRadioButton statusButton;
  private String logFileName;
  private ConsoleNodeOutputFilter filter;

  public ConsoleInternalFrame(NodeComponent node, 
                              ConsoleNodeListener listener,
                              JScrollPane pane,
                              JRadioButton statusButton,
                              String logFileName) {
    super("",   // title
          true, //resizable
          false, //not closable, because they can't be recreated
          true, //maximizable
          true);//iconifiable
    this.node = node;
    this.listener = listener;
    this.statusButton = statusButton;
    this.logFileName = logFileName;
    consoleTextPane = (ConsoleTextPane)pane.getViewport().getView();
    //    filter = new ConsoleNodeOutputFilter();
    //    listener.setFilter(filter);
    // get host component by getting the experiment and 
    // searching its hosts for one with this node.
    Experiment experiment = 
      (Experiment)getPropertyValue((ConfigurableComponent)node, "Experiment");
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
    // title is "node name:host name"
    setTitle(node.getShortName() + ":" + hostName);
    // init menu
    JMenuBar menuBar = new JMenuBar();
    JMenu aboutMenu = new JMenu(ABOUT_MENU);
    Action aboutAction = new AbstractAction(ABOUT_ACTION) {
      public void actionPerformed(ActionEvent e) {
        displayAbout();
      }
    };
    aboutMenu.add(aboutAction);
    Action cpuUsageAction = new AbstractAction(CPU_USAGE_ACTION) {
      public void actionPerformed(ActionEvent e) {
      }
    };
    aboutMenu.add(cpuUsageAction);
    Action memoryUsageAction = new AbstractAction(MEMORY_USAGE_ACTION) {
      public void actionPerformed(ActionEvent e) {
      }
    };
    aboutMenu.add(memoryUsageAction);
    JMenu controlMenu = new JMenu(CONTROL_MENU);
    Action startAction = new AbstractAction(START_ACTION) {
      public void actionPerformed(ActionEvent e) {
      }
    };
    startAction.setEnabled(false);
    controlMenu.add(startAction);
    Action stopAction = new AbstractAction(STOP_ACTION) {
      public void actionPerformed(ActionEvent e) {
      }
    };
    stopAction.setEnabled(false);
    controlMenu.add(stopAction);
    Action moveAction = new AbstractAction(MOVE_ACTION) {
      public void actionPerformed(ActionEvent e) {
      }
    };
    moveAction.setEnabled(false);
    controlMenu.add(moveAction);
    Action traceAction = new AbstractAction(TRACE_ACTION) {
      public void actionPerformed(ActionEvent e) {
      }
    };
    traceAction.setEnabled(false);
    controlMenu.add(traceAction);
    JMenu displayMenu = new JMenu(DISPLAY_MENU);
    Action displayLogAction = new AbstractAction(DISPLAY_LOG_ACTION) {
      public void actionPerformed(ActionEvent e) {
        displayLog_actionPerformed();
      }
    };
    displayMenu.add(displayLogAction);
    Action selectAllAction = new AbstractAction(SELECT_ALL_ACTION) {
      public void actionPerformed(ActionEvent e) {
        selectAll_actionPerformed();
      }
    };
    displayMenu.add(selectAllAction);
    Action filterAction = new AbstractAction(FILTER_ACTION) {
      public void actionPerformed(ActionEvent e) {
        filter_actionPerformed();
      }
    };
    displayMenu.add(filterAction);
    filterAction.setEnabled(false);
    JMenu searchMenu = new JMenu(SEARCH_MENU);
    searchAction = new AbstractAction(SEARCH_ACTION) {
      public void actionPerformed(ActionEvent e) {
        search_actionPerformed();
      }
    };
    searchMenu.add(searchAction);
    searchNextAction = new AbstractAction(SEARCH_NEXT_ACTION) {
      public void actionPerformed(ActionEvent e) {
        searchNext_actionPerformed();
      }
    };
    searchMenu.add(searchNextAction);
    JMenu notifyMenu = new JMenu(NOTIFY_MENU);
    notifyAction = new AbstractAction(NOTIFY_ACTION) {
      public void actionPerformed(ActionEvent e) {
	notify_actionPerformed();
      }
    };
    notifyMenu.add(notifyAction);
    JMenu notifyNextMenu = new JMenu(NOTIFY_MENU);
    notifyNextAction = new AbstractAction(NOTIFY_NEXT_ACTION) {
      public void actionPerformed(ActionEvent e) {
	notifyNext_actionPerformed();
      }
    };
    notifyMenu.add(notifyNextAction);
    JMenu statusMenu = new JMenu(STATUS_MENU);
    Action historyAction = new AbstractAction(HISTORY_ACTION) {
      public void actionPerformed(ActionEvent e) {
	history_actionPerformed();
      }
    };
    statusMenu.add(historyAction);
    menuBar.add(aboutMenu);
    menuBar.add(controlMenu);
    menuBar.add(displayMenu);
    menuBar.add(statusMenu);
    menuBar.add(searchMenu);
    menuBar.add(notifyMenu);
    getRootPane().setJMenuBar(menuBar);
    initKeyMap(consoleTextPane);
    openFrameCount++;
    getContentPane().add(pane);
    setSize(300,300);
    //Set the window's location.
    setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
  }

  /**
   * Set up a keymap:
   * ctrl-s search 
   * ctrl-t search next
   * ctrl-n notify
   * ctrl-o notify next
   */

  private void initKeyMap(ConsoleTextPane pane) {
    InputMap im = pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap am = pane.getActionMap();
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK), 
           SEARCH_ACTION);
    am.put(SEARCH_ACTION, searchAction);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, Event.CTRL_MASK), 
           SEARCH_NEXT_ACTION);
    am.put(SEARCH_NEXT_ACTION, searchNextAction);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK), 
           NOTIFY_ACTION);
    am.put(NOTIFY_ACTION, notifyAction);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK), 
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
    ArrayList agentNames = 
      (ArrayList)getPropertyValue((ConfigurableComponent)node, "AgentNames");
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
    aboutPanel.add(new JLabel("Notify Conditions Found:"),
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
    aboutPanel.add(new JLabel("Log File Name:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    aboutPanel.add(new JLabel(logFileName),
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
    aboutPanel.add(new JLabel("Host Description:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    aboutPanel.add(new JLabel((String)getPropertyValue((ConfigurableComponent)host, "Description")),
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
    aboutPanel.add(new JLabel((String)getPropertyValue((ConfigurableComponent)host, "MachineType")),
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
    aboutPanel.add(new JLabel((String)getPropertyValue((ConfigurableComponent)host, "Location")),
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
    aboutPanel.add(new JLabel((String)getPropertyValue((ConfigurableComponent)node, "Description")),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;
    aboutPanel.add(new JLabel("Command Line Arguments:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    aboutPanel.add(new JLabel((String)getPropertyValue((ConfigurableComponent)node, "CmdLineArgs")),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    x = 0;
    aboutPanel.add(new JLabel("Agents:"),
                   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 5, 5),
                                          0, 0));
    JList agentsList = new JList(agentNames.toArray());
    agentsList.setBackground(aboutPanel.getBackground());
    aboutPanel.add(new JScrollPane(agentsList),
                   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 5, 0),
                                          0, 0));
    JOptionPane.showMessageDialog(this, aboutPanel, 
                                  "About " + node.getShortName(), 
                                  JOptionPane.PLAIN_MESSAGE);
  }

  // helper method for about panel
  private Object getPropertyValue(ConfigurableComponent component, String name) {
    Property prop = component.getProperty(new ComponentName(component, name));
    if (prop == null)
      return null;
    return prop.getValue();
  }

  private void search_actionPerformed() {
    // get string to search for
    String s = JOptionPane.showInputDialog("Search string:");
    if (s == null || s.length() == 0) {
      return;
    }
    // search and highlight
    boolean found = consoleTextPane.search(s);
    searchNextAction.setEnabled(found);
  }

  private void searchNext_actionPerformed() {
    // search and highlight
    boolean found = consoleTextPane.searchNext();
    searchNextAction.setEnabled(found);
    consoleTextPane.revalidate();
    consoleTextPane.repaint();
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
   * Select everything in the node's output pane.
   * Note that the focus must be requested before the select action will work.
   */

  private void selectAll_actionPerformed() {
    consoleTextPane.requestFocus();
    consoleTextPane.selectAll();
  }

  private void filter_actionPerformed() {
    ConsoleStyledDocument doc = 
      (ConsoleStyledDocument)consoleTextPane.getStyledDocument();
    filter.setBufferSize(doc.getBufferSize());
    filter.setVisible(true);
    int n = filter.getBufferSize();
    if (n != 0)
      doc.setBufferSize(n);
  }

  private void displayLog_actionPerformed() {
    listener.fillFromLogFile();
  }

  /**
   * Notify (by coloring status button) when the specified
   * output is received on this node.
   */

  private void notify_actionPerformed() {
    String s = 
      (String)JOptionPane.showInputDialog(this,
                                          "Notify if node writes:",
                                          "Notification",
                                          JOptionPane.QUESTION_MESSAGE,
                                          null, null, notifyCondition);
    if (s == null || s.length() == 0)
      notifyCondition = null;
    else
      notifyCondition = s;
    consoleTextPane.setNotifyCondition(notifyCondition);
    notifyNextAction.setEnabled(true);
  }

  private void notifyNext_actionPerformed() {
    boolean found = consoleTextPane.notifyNext();
    notifyNextAction.setEnabled(found);
    consoleTextPane.revalidate();
    consoleTextPane.repaint();
  }

}




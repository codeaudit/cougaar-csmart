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
  private static final String NODE_MENU = "Node";
  private static final String INFO_ACTION = "Info";
  private static final String CPU_USAGE_ACTION = "CPU Usage";
  private static final String MEMORY_USAGE_ACTION = "Memory Usage";
  private static final String HISTORY_ACTION = "Utilization History";

  private static final String EDIT_MENU = "Edit";
  private static final String CUT_ACTION = "Cut";
  private static final String COPY_ACTION = "Copy";
  private static final String PASTE_ACTION = "Paste";
  private static final String CLEAR_ACTION = "Clear";
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

  private static int openFrameCount = 0;
  private static final int xOffset = 30, yOffset = 30;
  private NodeComponent node;
  private ConsoleNodeListener listener;
  private ConsoleTextPane consoleTextPane;
  private Action findAction;
  private Action findNextAction;
  private Action notifyAction;
  private Action notifyNextAction;
  private HostComponent host;
  private String hostName;
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
      }
    };
    cpuUsageAction.setEnabled(false);
    nodeMenu.add(cpuUsageAction);
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
    cutAction.setEnabled(false);
    JMenuItem mi = editMenu.add(cutAction);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                                             Event.CTRL_MASK));
    Action copyAction = new AbstractAction(COPY_ACTION) {
      public void actionPerformed(ActionEvent e) {
        copy_actionPerformed();
      }
    };
    copyAction.setEnabled(false);
    mi = editMenu.add(copyAction);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                             Event.CTRL_MASK));
    Action pasteAction = new AbstractAction(PASTE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        paste_actionPerformed();
      }
    };
    pasteAction.setEnabled(false);
    mi = editMenu.add(pasteAction);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                             Event.CTRL_MASK));
    Action clearAction = new AbstractAction(CLEAR_ACTION) {
      public void actionPerformed(ActionEvent e) {
        clear_actionPerformed();
      }
    };
    clearAction.setEnabled(false);
    editMenu.add(clearAction);
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
    Action startAction = new AbstractAction(START_ACTION) {
      public void actionPerformed(ActionEvent e) {
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
    Action stopAction = new AbstractAction(STOP_ACTION) {
      public void actionPerformed(ActionEvent e) {
      }
    };
    stopAction.setEnabled(false);
    controlMenu.add(stopAction);
    Action traceAction = new AbstractAction(STACK_TRACE_ACTION) {
      public void actionPerformed(ActionEvent e) {
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
    mi = notifyMenu.add(notifyAction);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                             Event.CTRL_MASK));
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
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
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
    openFrameCount++;
    getContentPane().add(pane);
    setSize(300,300);
    //Set the window's location.
    setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
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
           SET_NOTIFY_ACTION);
    am.put(SET_NOTIFY_ACTION, notifyAction);
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

  // Implementations for the action items from the menu


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
    String s = JOptionPane.showInputDialog("Find string:");
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
  }

  private void copy_actionPerformed() {
  }

  private void paste_actionPerformed() {
  }

  private void clear_actionPerformed() {
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
      new ConsoleNodeOutputFilter(filterValues, isAllSelected);
    listener.setFilter(filter);
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
                                          null, null, 
                                          consoleTextPane.getNotifyCondition());
    setNotification(s);
  }

  private void setNotification(String s) {
    String notifyCondition = null;
    //    if (s == null || s.length() == 0) {
      //      notifyNextAction.setEnabled(false);
    //    } else {
    //      notifyCondition = s;
      //      notifyNextAction.setEnabled(true);
    //    }
    if (s != null && s.length() != 0)
      notifyCondition = s;
    consoleTextPane.setNotifyCondition(notifyCondition);
    ((NodeStatusButton)statusButton).clearError();
  }

  private void removeNotify_actionPerformed() {
    setNotification(null);
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

}





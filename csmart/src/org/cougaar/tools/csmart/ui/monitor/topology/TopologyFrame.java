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

package org.cougaar.tools.csmart.ui.monitor.topology;

import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.util.NamedFrame;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class TopologyFrame extends JFrame {
  private static final String FILE_MENU = "File";
  private static final String CLOSE_ACTION = "Close";

  private static final String VIEW_MENU = "View";
  private static final String REFRESH_ACTION = "Refresh";
  private static final String SET_REFRESH_INTERVAL_ACTION = "Set Refresh Interval";

  private static final String HELP_MENU = "Help";
  private static final String HELP_DOC = "help.html";
  private static final String HELP_ACTION = "Help";
  private static final String ABOUT_ACTION = "About CSMART";
  private static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";

  private final JFrame myFrame;
  private String title;
  private TopologyTree topologyTree;
  private TopologyTableModel model;
  private TableSorter sorter;
  private Timer timer = null; // timer to auto-refresh data
  private int refreshInterval = 0; // default, no auto-refresh
  private TopologyService topologyService;

  private Action closeAction = new AbstractAction(CLOSE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    };

  private Action refreshAction = new AbstractAction(REFRESH_ACTION) {
      public void actionPerformed(ActionEvent e) {
        ArrayList values = TopologyFrame.this.getAgentLocations();
        TopologyFrame.this.refresh(values);
      }
    };

  private Action setRefreshIntervalAction = 
    new AbstractAction(SET_REFRESH_INTERVAL_ACTION) {
      public void actionPerformed(ActionEvent e) {

	// As long as we don't have a valid interval,
	// keep asking
	int newVal = -1;
	while (newVal < 0) {
	  String s = 
	    JOptionPane.showInputDialog("Set Auto Refresh Interval (in seconds), 0 to cancel: ", Integer.toString(refreshInterval));
	  // But if the user types in nothing, treat it as a cancel
	  if (s == null || s.trim().equals("") || s.trim().equals(Integer.toString(refreshInterval)))
	    return;
	  try {
	    newVal = Integer.parseInt(s);
	  } catch (NumberFormatException nfe) {
	    JOptionPane.showMessageDialog(null,
					  "Refresh interval must be specified as a number of seconds.",
					  "Illegal Number",
					  JOptionPane.ERROR_MESSAGE);
	  }
	}
	
	// If user made no change, we're done.
	if (newVal == refreshInterval)
	  return;
	
	refreshInterval = newVal;
	
        // cancel any existing timer
        if (timer != null)
          timer.cancel();

	// If refreshInterval set to 0, no timer needed
	if (refreshInterval > 0) {
	  // refresh now and every refreshInterval
	  timer = new Timer();
	  timer.schedule(new RefreshTask(), 0, (long)(refreshInterval * 1000));
	}
      }
    };

  private Action helpAction = new AbstractAction(HELP_ACTION) {
      public void actionPerformed(ActionEvent e) {
        URL help = (URL)TopologyFrame.class.getResource(HELP_DOC);
        if(help != null)
          Browser.setPage(help);
      }
    };

  private Action aboutAction = new AbstractAction(ABOUT_ACTION) {
      public void actionPerformed(ActionEvent e) {
        URL help = (URL)TopologyFrame.class.getResource(ABOUT_DOC);
        if(help != null)
          Browser.setPage(help);
      }
    };

  public TopologyFrame(String title, String host, int port, String agent) {
    super(title);
    this.title = NamedFrame.getNamedFrame().addFrame(title, this);
    setTitle(this.title);
    topologyService = new TopologyService();
    myFrame = this;
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.add(new JMenuItem(closeAction));
    menuBar.add(fileMenu);
    JMenu viewMenu = new JMenu(VIEW_MENU);
    viewMenu.add(new JMenuItem(refreshAction));
    viewMenu.add(new JMenuItem(setRefreshIntervalAction));
    menuBar.add(viewMenu);
    JMenu helpMenu = new JMenu(HELP_MENU);
    helpMenu.add(new JMenuItem(helpAction));
    helpMenu.add(new JMenuItem(aboutAction));
    menuBar.add(helpMenu);
    getRootPane().setJMenuBar(menuBar);

    ArrayList values = getAgentLocations();
    if (values == null || values.isEmpty()) 
      JOptionPane.showMessageDialog(null,
                                    "No information received from society");
    JTable table = createTable(values);
    JTree tree = createTree(values);
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    splitPane.setLeftComponent(new JScrollPane(table));
    splitPane.setRightComponent(new JScrollPane(tree));
    splitPane.validate();
    getContentPane().add("Center", splitPane);
    JPanel topPanel = new JPanel();
    topPanel.add(new JLabel("Dynamic Configuration", SwingConstants.CENTER));
    getContentPane().add("North", topPanel);
    JPanel bottomPanel = new JPanel();
    JButton updateButton = new JButton(refreshAction);
    bottomPanel.add(updateButton);
    getContentPane().add("South", bottomPanel);
    pack();
    setVisible(true);
  }

  private void close() {
    if (timer != null)
      timer.cancel();
    NamedFrame.getNamedFrame().removeFrame(myFrame);
    myFrame.dispose();
  }

  private synchronized ArrayList getAgentLocations() {
    return topologyService.getAgentLocations();
  }

  // update with the latest info
  private void refresh(ArrayList newValues) {
    model.updateValues(newValues);
    sorter.updateModel();
    topologyTree.setValues(newValues);
  }

  // Create the tree representation of topology info
  private JTree createTree(ArrayList values) {
    DefaultMutableTreeNode root = 
      new DefaultMutableTreeNode(new TopologyTreeObject("Hosts"), true);
    DefaultTreeModel model = new DefaultTreeModel(root);
    model.setAsksAllowsChildren(true);
    topologyTree = new TopologyTree(model);
    topologyTree.setValues(values);
    return topologyTree;
  }

  // Create the table of topology info
  private JTable createTable(ArrayList values) {
    model = new TopologyTableModel(values);
    sorter = new TableSorter(model);
    JTable table = new JTable(sorter);
    sorter.addMouseListenerToHeaderInTable(table);
    return table;
  }

  // Testing purposes, run standalone
  public static void main(String[] args) {
    new TopologyFrame("Topology", "victoria.bbn.com", 8800, "NCA");
  }

  /** Thread to refresh the display **/
  class RefreshTask extends TimerTask {
    public void run() {
      final ArrayList values = TopologyFrame.this.getAgentLocations();
      Runnable updater = new Runnable() {
          public void run() {
            TopologyFrame.this.refresh(values);
          }
        };
      SwingUtilities.invokeLater(updater);
    }
  }
}


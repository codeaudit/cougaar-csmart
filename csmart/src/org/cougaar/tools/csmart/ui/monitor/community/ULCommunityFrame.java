/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.community;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;
import att.grappa.*;

import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTFrame;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTGraph;
import org.cougaar.tools.csmart.ui.monitor.generic.LegendComboBoxModel;
import org.cougaar.tools.csmart.ui.monitor.generic.LegendRenderer;
import org.cougaar.tools.csmart.ui.util.NamedFrame;

/**
 * A frame to display communities.
 * The nodes in the graph are the communities in the society.
 */

public class ULCommunityFrame extends CSMARTFrame {
  static final String LEGEND_MENU_ITEM = "Legend";
  JDialog legendDialog;

  /**
   * Create a frame for the specified graph.
   * @param title  title for the frame
   * @param graph  graph to display in the frame
   */

  public ULCommunityFrame(String title, CSMARTGraph graph) {
    super(title, graph);
  }

  /**
   * Add menu items specific to Event objects and add this
   * as the handler.
   */

  public void customize() {
    insertMenuItem(CSMARTFrame.VIEW_MENU, LEGEND_MENU_ITEM, 2, this);
  }

  /**
   * Enable/disable menu items that are used when the event graph
   * has selected nodes.
   * Can be overridden to enable/disable additional customized menu items.
   * @param enable true to enable menu items; false to disable
   */

  public void enableSelectedMenus(boolean enable) {
    super.enableSelectedMenus(enable);
    showMenu.setEnabled(false); // never need this
  }

  /**
   * Get table model to display the attributes of this node.
   * @param node the node for which to return a table model
   * @return     table model to use to display attributes for this node
   */

  public TableModel getAttributeTableModel(Node node) {
    return new ULCommunityTableModel(node);
  }

  /**
   * Process menu commands that are specific to events;
   * pass other menu commands to CSMARTFrame.
   * @param evt the event received
   */

  public void actionPerformed(ActionEvent evt) {
    String command = ((JMenuItem)evt.getSource()).getText();

    if (command.equals(NEW_WITH_SELECTION_MENU_ITEM)) {
      CSMARTGraph newGraph = graph.newGraphFromSelection();
      new ULCommunityFrame(NamedFrame.COMMUNITY, newGraph);
      return;
    }

    if (command.equals(LEGEND_MENU_ITEM)) {
      createLegend();
      return;
    }

    super.actionPerformed(evt);
  }

  /**
   * Create a legend; which is a non-modal dialog.
   * Called in response to selecting "display legend" from a menu.
   */

  private void createLegend() {
    if (legendDialog != null) {
      legendDialog.setVisible(true);
      return;
    }
    legendDialog = new JDialog(this, "Legend", false);

    JPanel clusterPanel = new JPanel();
    clusterPanel.setBorder(new TitledBorder("Communities"));
    clusterPanel.setLayout(new BoxLayout(clusterPanel, BoxLayout.Y_AXIS));
    int x = 0;
    int y = 0;
    JComboBox cb = 
      new JComboBox(new LegendComboBoxModel(graph.getNodeColors()));
    cb.setRenderer(new LegendRenderer());
    cb.setSelectedIndex(0);
    clusterPanel.add(cb);

    JPanel buttonPanel = new JPanel();
    JButton OKButton = new JButton("OK");
    OKButton.setFocusPainted(false);
    OKButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	legendDialog.setVisible(false);
      }
    });
    buttonPanel.add(OKButton);
    legendDialog.getContentPane().setLayout(new BorderLayout());
    legendDialog.getContentPane().add(clusterPanel, BorderLayout.CENTER);
    legendDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    legendDialog.pack();
    legendDialog.setSize(legendDialog.getWidth(),legendDialog.getHeight()+100);
    legendDialog.show();
  }


}











/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.experiment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;

import org.cougaar.tools.csmart.experiment.Experiment;

public class TrialBuilder extends JPanel {
  private static final String DELETE_MENU_ITEM = "Delete Trial";
  JTable trialTable;
  TrialTableModel trialTableModel;
  JPopupMenu trialMenu;
  int deleteRowIndex; // row to delete
  Experiment experiment;
  boolean isEditable;
  boolean needUpdate = true; // need to update display

  public TrialBuilder(Experiment experiment) {
    this.experiment = experiment;
    isEditable = experiment.isEditable();
    trialTable = new JTable();
    // don't allow user to reorder columns
    trialTable.getTableHeader().setReorderingAllowed(false);
    trialTable.setColumnSelectionAllowed(false);
    trialTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    JScrollPane scrollPane = new JScrollPane(trialTable);
    trialMenu = new JPopupMenu();
    JMenuItem deleteMenuItem = new JMenuItem(DELETE_MENU_ITEM);
    deleteMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	deleteTrial();
      }
    });
    trialMenu.add(deleteMenuItem);
    setLayout(new BorderLayout());
    add(scrollPane, BorderLayout.CENTER);
    needUpdate = true;
  }

  /**
   * Set experiment to edit; used to re-use a running editor
   * to edit a different experiment.  
   */

  public void reinit(Experiment newExperiment) {
    if (this.experiment != null &&
	this.experiment.equals(newExperiment))
      return; // no change
    experiment = newExperiment;
    isEditable = newExperiment.isEditable();
    if (isShowing())
      updateDisplay();
    else
      needUpdate = true;
  }

  private void updateDisplay() {
    if (isEditable) {
      trialTable.addMouseListener(mouseListener);
      trialTable.setForeground(Color.black);
    } else
      trialTable.setForeground(Color.gray);
    if (!experiment.hasValidTrials())
      experiment.getTrials();
    trialTableModel = new TrialTableModel(experiment);
    trialTable.setModel(trialTableModel);
    needUpdate = false;
  }

  /**
   * Update the trials if either there's a new experiment
   * or the experiment trials have changed.
   */

  public void setVisible(boolean visible) {
    if (visible && (needUpdate || !experiment.hasValidTrials()))
      updateDisplay();
    super.setVisible(visible);
  }

  private MouseListener mouseListener = new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }
    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }
  };

  /**
   * On right mouse click, pop-up menu with delete menu item.
   * Remember what row the user clicked on, because if they select delete,
   * then that's the row to delete.
   */

  private void doPopup(MouseEvent e) {
    Point pt = e.getPoint();
    int rowIndex = trialTable.rowAtPoint(pt);
    int columnIndex = trialTable.columnAtPoint(pt);
    if (rowIndex == -1 || columnIndex == -1)
      return; // ignore, clicked outside of table
    else {
      // If there is only one trial left, dont let the user remove it
      // FIXME: Should really search and make sure were disabling
      // The delete menu item, but I happen to know its the only option
      if (trialTableModel.getRowCount() < 2) 
	trialMenu.getComponent(0).setEnabled(false);
      trialMenu.show(e.getComponent(), e.getX(), e.getY());
      deleteRowIndex = rowIndex;
    }
  }

  /**
   * Called when user selects delete from the pop-up menu;
   * tell the model to delete the row.
   */

  private void deleteTrial() {
    trialTableModel.deleteRow(deleteRowIndex);
  }
}

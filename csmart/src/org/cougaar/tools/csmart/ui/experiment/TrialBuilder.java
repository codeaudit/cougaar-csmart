/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.experiment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;

public class TrialBuilder extends JPanel {
  private static final String DELETE_MENU_ITEM = "Delete Trial";
  JTable trialTable;
  TrialTableModel trialTableModel;
  JPopupMenu trialMenu;
  int deleteRowIndex; // row to delete
  Experiment experiment;
  boolean isEditable;
  boolean isRunnable;
  boolean needUpdate = true; // need to update display

  public TrialBuilder(Experiment experiment) {
    this.experiment = experiment;
    isEditable = experiment.isEditable();
    isRunnable = experiment.isRunnable();
    trialTable = new JTable();
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

    // restore editable flag on previous experiment
    if (isEditable) 
      experiment.setEditable(isEditable);
    if (isRunnable)
      experiment.setRunnable(isRunnable);
    experiment = newExperiment;
    isEditable = newExperiment.isEditable();
    isRunnable = newExperiment.isRunnable();
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

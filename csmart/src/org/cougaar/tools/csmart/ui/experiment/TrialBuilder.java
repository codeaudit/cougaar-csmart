/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       � Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.experiment;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;

public class TrialBuilder extends JPanel {
  private static final String DELETE_MENU_ITEM = "Delete Trial";
  PropertyBuilder propertyBuilder;
  JTable trialTable;
  TrialTableModel trialTableModel;
  JPopupMenu trialMenu;
  int deleteRowIndex; // row to delete
  Experiment experiment;
  boolean needUpdate = true; // need to update display

  public TrialBuilder(Experiment experiment, PropertyBuilder propertyBuilder) {
    this.propertyBuilder = propertyBuilder;
    trialTable = new JTable();
    trialTable.addMouseListener(mouseListener);
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
    setExperiment(experiment);
  }

  /**
   * Set experiment to edit; used to re-use a running editor
   * to edit a different experiment.  
   */

  public void setExperiment(Experiment experiment) {
    if (this.experiment != null &&
	this.experiment.equals(experiment))
      return; // no change

    this.experiment = experiment;
    if (isShowing())
      updateDisplay();
    else
      needUpdate = true;
  }

  private void updateDisplay() {
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

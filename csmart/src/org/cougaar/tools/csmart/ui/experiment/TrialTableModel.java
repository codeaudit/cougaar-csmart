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

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.lang.reflect.Array;
import org.cougaar.tools.csmart.ui.component.*;

/**
 * Creates a table of trials;
 * first column is trial number
 * succeeding columns are the properties being varied
 */

public class TrialTableModel extends AbstractTableModel {
  private Experiment experiment;

  /**
   * Just save the experiment.
   */

  public TrialTableModel(Experiment experiment) {
    this.experiment = experiment;
  }

  public int getRowCount() {
    return experiment.getTrialCount();
  }

  /**
   * The columns are the parameters in the trials.
   * Preumably every trial has the same number of parameters, so
   * just get the number of parameters in the first trial, and add one
   * for the trial name.
   */

  public int getColumnCount() {
    Trial[] trials = experiment.getTrials();
    if (trials.length == 0)
      return 1;
    return trials[0].getNumberOfParameters() + 1;
  }

  public String getColumnName(int column) {
    if (column >= getColumnCount())
      return null;
    if (column == 0)
      return "Trial Number";
    else {
      Trial[] trials = experiment.getTrials();
      if (trials.length == 0)
	return "";
      Property[] parameters = trials[0].getParameters();
      return parameters[column-1].getName().toString();
    }
  }

  public Object getValueAt(int row, int column) {
    if (row < 0 || row >= getRowCount() ||
	column < 0 || column >= getColumnCount())
      return null; // index out of range
    Trial trial = experiment.getTrials()[row];
    if (column == 0)
      return trial.getShortName();
    Object[] values = trial.getValues();
    return values[column-1];   
  }

  /**
   * Delete a trial.
   * Note that if the user subsequently changes property values or
   * the variation scheme, that the trial set is regenerated,
   * and all "custom" deletions are lost.
   */

  public void deleteRow(int row) {
    experiment.removeTrial(row);
    fireTableRowsDeleted(row, row);
  }


}


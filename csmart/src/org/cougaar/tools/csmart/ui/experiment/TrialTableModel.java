/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.lang.reflect.Array;

import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.Trial;

import org.cougaar.tools.csmart.core.property.Property;

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

  /**
   * Cells aren't editable, maybe they should be.
   */

  public boolean isCellEditable(int row, int col) {
    return false;
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
    Object value = values[column-1];
    if (value.getClass().isArray()) {
      StringBuffer buf = new StringBuffer();
      buf.append('{');
      for (int i = 0, n = Array.getLength(value); i < n; i++) {
        if (i > 0) buf.append(",");
        buf.append(Array.get(value, i));
      }
      buf.append("}");
      return buf.toString();
    } else
      return value;
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


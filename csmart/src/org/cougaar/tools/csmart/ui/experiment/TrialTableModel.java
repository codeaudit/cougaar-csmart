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
  private ArrayList propertyNames; // array of CompositeNames
  private ArrayList propertyValues; // array of Object[]
  // one entry per row, array of Object[], one value per column
  private ArrayList trialValues;

  /**
   * Just save the experiment.
   */

  public TrialTableModel(Experiment experiment) {
    this.experiment = experiment;
  }

  /**
   * Update table based on latest information in experiment.
   * Called when table is displayed?
   * Compute trials based on variation scheme and experimental values
   * of properties.
   */

  public void update(int variationScheme) {
    getExperimentProperties();
    if (propertyNames.size() == 0) {
      // no properties to vary
      trialValues = new ArrayList(0);
      return;
    }
    if (variationScheme == PropertyBuilder.VARY_ONE_DIMENSION) {
      int numberOfTrials = 
	PropertyBuilder.getTrialCount(experiment, variationScheme);
      trialValues = new ArrayList(numberOfTrials);
      int nProperties = propertyNames.size();
      // trial values includes trial name
      int propToVaryIndex = 0; // index of property being varied
      int k = 0; // index into values of property being varied
      for (int i = 0; i < numberOfTrials; i++) {
	Object[] thisTrialValues = new Object[nProperties+1];
	int m = 0;
	thisTrialValues[m++] = "Trial Number " + (i+1); // default trial name
	for (int j = 0; j < nProperties; j++) {
	  if (j == propToVaryIndex) 
	    thisTrialValues[m++] = ((Object [])propertyValues.get(j))[k++];
	  else
	    thisTrialValues[m++] = ((Object [])propertyValues.get(j))[0];
	}
	trialValues.add(thisTrialValues);
	// if we've stepped through all the values of the property being varied
	// then vary the next property
	if (((Object [])propertyValues.get(propToVaryIndex)).length == k) {
	  k = 1; // after first pass, skip nominal values
	  propToVaryIndex++;
	}
      }
    } else
      System.out.println("Variation scheme not implemented yet.");
    fireTableStructureChanged();
  }

  // for debugging
  private void printTrialValues(Object[] thisTrialValues) {
    System.out.println("Trial values are: ");
    for (int m = 0; m < thisTrialValues.length; m++)
      System.out.println(thisTrialValues[m]);
  }

  private void getExperimentProperties() {
    propertyNames = new ArrayList();
    propertyValues = new ArrayList();
    int n = experiment.getSocietyComponentCount();
    for (int i = 0; i < n; i++) {
      SocietyComponent society = experiment.getSocietyComponent(i);
      Iterator names = society.getPropertyNames();
      while (names.hasNext()) {
	Property property = society.getProperty((CompositeName)names.next());
	List values = property.getExperimentValues();
	if (values != null) {
	  propertyNames.add(property.getName());
	  propertyValues.add(values.toArray());
	}
      }
    }
  }

  public int getRowCount() {
    return trialValues.size();
  }

  public int getColumnCount() {
    return propertyNames.size() + 1;
  }

  public String getColumnName(int column) {
    if (column >= getColumnCount())
      return null;
    if (column == 0)
      return "Trial Number";
    else
      return ((CompositeName)propertyNames.get(column-1)).toString();
  }

  public Object getValueAt(int row, int column) {
    if (row < 0 || row >= getRowCount() ||
	column < 0 || column >= getColumnCount())
      return null; // index out of range
    Object[] values = (Object [])trialValues.get(row);
    return values[column];   
  }

  /**
   * Delete a trial.
   * Delete the values from the trialValues array; 
   * note that if the user subsequently changes property values or
   * the variation scheme, that the trial set is regenerated,
   * and all "custom" deletions are lost.
   */

  public void deleteRow(int row) {
    trialValues.remove(row);
    fireTableRowsDeleted(row, row);
  }

  // for testing
  public static void main(String[] args) {
    // have to skip setting properties from experiment
    // and explicitly set number of properties for this to work
//     TrialTableModel model = 
//       new TrialTableModel((Experiment)null);
//     model.propertyNames = new ArrayList(2);
//     model.propertyNames.add("Bytes Per Message");
//     model.propertyNames.add("Task Verb");
//     model.propertyValues = new ArrayList(2);
//     Integer[] values = new Integer[2];
//     values[0] = new Integer(5);
//     values[1] = new Integer(3);
//     model.propertyValues.add(values);
//     String[] stringValues = { "Transport", "Supply", "Feed" };
//     model.propertyValues.add(stringValues);
//     model.update(PropertyBuilder.VARY_ONE_DIMENSION);
//     JTable trialTable = new JTable(model);
//     JScrollPane scrollPane = new JScrollPane(trialTable);
//     JFrame frame = new JFrame();
//     frame.getContentPane().add(scrollPane);
//     frame.pack();
//     frame.show();
  }

}


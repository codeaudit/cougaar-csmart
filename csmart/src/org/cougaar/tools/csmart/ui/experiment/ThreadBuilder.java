/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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

import java.awt.*;
import java.util.*;
import javax.swing.*;

import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.experiment.Experiment;

public class ThreadBuilder extends JPanel {
  // thread names displayed for user
  private static final String[] ULThreads = {
    "Subsistence (Class 1)",
    "Fuel (Class 3)",
    "Construction Material (Class 4)",
    "Ammunition (Class 5)",
    "Spare Parts (Class 9)"
  };
  // thread names passed to database
  private static final String[] ULDBThreads = {
    "CLASS-1", "CLASS-3", "CLASS-4", "CLASS-5", "CLASS-9"
  };
  private String[] groupNames;
  private ArrayList groupCheckBoxes = new ArrayList();
  private ArrayList ULThreadCheckBoxes = new ArrayList();
  private ArrayList multiplierFields = new ArrayList();
  private ArrayList originalThreadSelected = new ArrayList();
  private ArrayList originalGroupSelected = new ArrayList();
  private ArrayList originalGroupMultiplier = new ArrayList();
  private Experiment experiment;
  private boolean needUpdate = true; // need to update display

  public ThreadBuilder(Experiment experiment) {
    this.experiment = experiment;
    updateDisplay();
    needUpdate = false;
  }

  /**
   * Called when this panel is first displayed after having been
   * created or re-initialized to display a new experiment.
   * Initially, this panel is used only for viewing, so all the controls
   * are disabled; eventually this can be used for configuring the experiment.
   */

  private void updateDisplay() {
    removeAll(); // remove previous components
    if (experiment == null || !DBUtils.dbMode ||
        !ExperimentDB.isExperimentNameInDatabase(experiment.getExperimentName())) {
      setLayout(new BorderLayout());
      add(new JLabel("Experiment is not in database or otherwise has no thread information available.", SwingConstants.CENTER), BorderLayout.CENTER);
        return;
    }
    String experimentId = experiment.getExperimentID();
    String trialId = experiment.getTrialID();
    setLayout(new GridBagLayout());
    int x = 0;
    int y = 0;
    int leftIndent = 0;
    add(new JLabel("Threads:"),
        new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                               GridBagConstraints.WEST,
                               GridBagConstraints.NONE,
                               new Insets(0, leftIndent, 5, 0),
                               0, 0));
    leftIndent = leftIndent + 5;
    for (int i = 0; i < ULThreads.length; i++) {
      JCheckBox cb = new JCheckBox(ULThreads[i]);
      boolean sel = 
        ExperimentDB.isULThreadSelected(trialId, ULDBThreads[i]);
      ULThreadCheckBoxes.add(cb);
      cb.setSelected(sel);
      originalThreadSelected.add(new Boolean(sel));
      cb.setEnabled(false);
      add(cb,
          new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                 GridBagConstraints.WEST,
                                 GridBagConstraints.NONE,
                                 new Insets(0, leftIndent, 5, 0),
                                 0, 0));
    }
    leftIndent = leftIndent - 5;
    add(new JLabel("Organization Groups:"),
        new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                               GridBagConstraints.WEST,
                               GridBagConstraints.NONE,
                               new Insets(0, leftIndent, 0, 0),
                               0, 0));
    leftIndent = leftIndent + 5;
    Map groupNameToId = ExperimentDB.getOrganizationGroups(experimentId);
    Set groups = groupNameToId.keySet();
    groupNames = (String[])groups.toArray(new String[groups.size()]);
    int nGroupNames = groupNames.length;
    for (int i = 0; i < nGroupNames; i++) {
      String groupName = (String)groupNames[i];
      JCheckBox groupCB = new JCheckBox(groupName);
      boolean sel = ExperimentDB.isGroupSelected(trialId, groupName);
      groupCheckBoxes.add(groupCB);
      originalGroupSelected.add(new Boolean(sel));
      groupCB.setSelected(sel);
      groupCB.setEnabled(false);
      add(groupCB,
          new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                 GridBagConstraints.WEST,
                                 GridBagConstraints.NONE,
                                 new Insets(0, leftIndent, 5, 0),
                                 0, 0));
      add(new JLabel("Number of Copies:"),
          new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                 GridBagConstraints.WEST,
                                 GridBagConstraints.NONE,
                                 new Insets(0, 0, 5, 5),
                                 0, 0));
      JTextField multiplierField = new JTextField(4);
      multiplierFields.add(multiplierField);
      int multiplier = ExperimentDB.getMultiplier(trialId, groupName);
      multiplierField.setText(String.valueOf(multiplier));
      multiplierField.setEnabled(false);
      originalGroupMultiplier.add(new Integer(multiplier));
      add(multiplierField,
          new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                 GridBagConstraints.WEST,
                                 GridBagConstraints.NONE,
                                 new Insets(0, 0, 5, 5),
                                 0, 0));
      add(new JLabel("Members:"),
          new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                 GridBagConstraints.WEST,
                                 GridBagConstraints.NONE,
                                 new Insets(0, 0, 5, 5),
                                 0, 0));
      Set members = 
        ExperimentDB.getOrganizationsInGroup(experimentId, 
                                         (String)groupNameToId.get(groupName));
      JList membersList = new JList(members.toArray());
      membersList.setVisibleRowCount(4);
      membersList.setEnabled(false);
      add(new JScrollPane(membersList),
          new GridBagConstraints(x++, y++, 1, 1, 0.0, 0.0,
                                 GridBagConstraints.WEST,
                                 GridBagConstraints.NONE,
                                 new Insets(0, 0, 5, 5),
                                 0, 0));
    }
  }

  /**
   * Set experiment to display.  Display is actually updated
   * when this panel is made visible.
   * TODO: why does this have to reset the editable and runnable
   * flags -- this is done by the caller (ExperimentBuilder)
   */

  public void reinit(Experiment newExperiment) {
    if (this.experiment != null && this.experiment.equals(newExperiment))
      return; // no change
    experiment = newExperiment;
    if (isShowing())
      updateDisplay();
    else
      needUpdate = true;
  }

  /**
   * Ensure that display is up-to-date before showing it.
   */

  public void setVisible(boolean visible) {
    if (visible && needUpdate)
      updateDisplay();
    super.setVisible(visible);
  }

}

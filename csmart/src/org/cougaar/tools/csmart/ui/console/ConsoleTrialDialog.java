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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class ConsoleTrialDialog extends JDialog {
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
  private String experimentId;
  private String trialId;
  private String[] groupNames;
  private ArrayList groupCheckBoxes = new ArrayList();
  private ArrayList ULThreadCheckBoxes = new ArrayList();
  private ArrayList multiplierFields = new ArrayList();
  private ArrayList originalThreadSelected = new ArrayList();
  private ArrayList originalGroupSelected = new ArrayList();
  private ArrayList originalGroupMultiplier = new ArrayList();

  public ConsoleTrialDialog(JFrame parent, String experimentId) {
    super(parent, "Threads and Groups", true); // modal dialog
    this.experimentId = experimentId;
    this.trialId = ExperimentDB.getTrialId(experimentId);
    JPanel panel = new JPanel(new BorderLayout());
    JPanel bottomPanel = new JPanel(new GridBagLayout());
    bottomPanel.setBorder(LineBorder.createGrayLineBorder());
    int x = 0;
    int y = 0;
    int leftIndent = 0;
    bottomPanel.add(new JLabel("Select Threads:"),
              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(0, leftIndent, 5, 0),
                                     0, 0));
    leftIndent = leftIndent + 5;
    for (int i = 0; i < ULThreads.length; i++) {
      JCheckBox cb = new JCheckBox(ULThreads[i]);
      ULThreadCheckBoxes.add(cb);
      boolean sel = ExperimentDB.isULThreadSelected(trialId, ULDBThreads[i]);
      cb.setSelected(sel);
      originalThreadSelected.add(new Boolean(sel));
      if (i == (ULThreads.length-1))
        cb.setEnabled(false); // spare parts
      bottomPanel.add(cb,
                      new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.NONE,
                                             new Insets(0, leftIndent, 5, 0),
                                             0, 0));
    }
    leftIndent = leftIndent - 5;
    bottomPanel.add(new JLabel("Select Organization Groups:"),
              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(0, leftIndent, 0, 0),
                                     0, 0));
    leftIndent = leftIndent + 5;
    Hashtable h = ExperimentDB.getOrganizationGroups(experimentId);
    Set groups = h.keySet();
    groupNames = (String[])groups.toArray(new String[groups.size()]);
    int nGroupNames = groupNames.length;
    for (int i = 0; i < nGroupNames; i++) {
      String groupName = (String)groupNames[i];
      JCheckBox groupCB = new JCheckBox(groupName);
      boolean sel = ExperimentDB.isGroupSelected(trialId, groupName);
      groupCheckBoxes.add(groupCB);
      originalGroupSelected.add(new Boolean(sel));
      groupCB.setSelected(false);
      bottomPanel.add(groupCB,
                new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, leftIndent, 5, 0),
                                       0, 0));
      bottomPanel.add(new JLabel("Number of Copies:"),
                new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, 0, 5, 5),
                                       0, 0));
      JTextField multiplierField = new JTextField(4);
      multiplierFields.add(multiplierField);
      int multiplier = ExperimentDB.getMultiplier(trialId, groupName);
      multiplierField.setText(String.valueOf(multiplier));
      originalGroupMultiplier.add(new Integer(multiplier));
      bottomPanel.add(multiplierField,
                new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, 0, 5, 5),
                                       0, 0));
      bottomPanel.add(new JLabel("Members:"),
                new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, 0, 5, 5),
                                       0, 0));
      String[] members = {
        "Agent1", "Agent2", "Agent3", "Agent4", "Agent5",
        "Agent6", "Agent7", "Agent8", "Agent9", "Agent10" 
      };
      JComboBox membersCB = new JComboBox(members);
      bottomPanel.add(membersCB,
                new GridBagConstraints(x++, y++, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, 0, 5, 5),
                                       0, 0));
      x = 0;
    }
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ok_actionPerformed();
      }
    });
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancel_actionPerformed();
      }
    });
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    panel.add(bottomPanel, BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.SOUTH);
    getContentPane().add(panel);
    pack();
  }

  /**
   * If threads, groups or multipliers have been modified, then
   * create a new trial.
   */

  private void ok_actionPerformed() {
    setVisible(false);
    boolean modified = false;
    int n = ULThreads.length;
    for (int i = 0; i < n; i++) {
      JCheckBox cb = (JCheckBox)ULThreadCheckBoxes.get(i);
      Boolean b = (Boolean)originalThreadSelected.get(i);
      if (b.booleanValue() == cb.isSelected())
        continue;
      ExperimentDB.setULThreadSelected(trialId, ULDBThreads[i], 
                                       cb.isSelected());
      modified = true;
    }
    n = groupCheckBoxes.size();
    for (int i = 0; i < n; i++) {
      String groupName = groupNames[i];
      JCheckBox cb = (JCheckBox)groupCheckBoxes.get(i);
      Boolean b = (Boolean)originalGroupSelected.get(i);
      if (b.booleanValue() != cb.isSelected()) {
        ExperimentDB.setGroupSelected(trialId, groupName, cb.isSelected());
        modified = true;
      }
      JTextField multiplierField = (JTextField)multiplierFields.get(i);
      int multiplier = ((Integer)originalGroupMultiplier.get(i)).intValue();
      try {
        int newMultiplier = Integer.parseInt(multiplierField.getText());
        if (multiplier == newMultiplier)
          continue;
        ExperimentDB.setMultiplier(trialId, groupName, newMultiplier);
        modified = true;
      } catch (NumberFormatException e) {
      }
    }
    if (modified) {
      System.out.println("MODIFIED");
      String name = 
        (String) JOptionPane.showInputDialog(this, "Enter Experiment Name",
                                             "Experiment Name",
                                             JOptionPane.QUESTION_MESSAGE,
                                             null, null, "");
      if (name == null)
        return;
      experimentId = ExperimentDB.cloneExperiment(experimentId, name);
      ExperimentDB.updateCMTAssembly(experimentId);
      trialId = ExperimentDB.getTrialId(experimentId);
    }
  }

  private void cancel_actionPerformed() {
    setVisible(false);
  }

  public String getExperimentId() {
    return experimentId;
  }

  public String getTrialId() {
    return trialId;
  }

  public static void main(String[] args) {
    ArrayList trialNames = new ArrayList();
    ConsoleTrialDialog d = 
      new ConsoleTrialDialog(null, "ExperimentID");
    d.setVisible(true);
  }
}

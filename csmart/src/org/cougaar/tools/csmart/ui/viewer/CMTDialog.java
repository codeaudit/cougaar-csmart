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

package org.cougaar.tools.csmart.ui.viewer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

import org.cougaar.tools.csmart.ui.console.ExperimentDB;
import org.cougaar.tools.csmart.societies.database.DBUtils;

public class CMTDialog extends JDialog {
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
  private String experimentName;
  private boolean cloned = false;
  private String trialId;
  private String[] groupNames;
  private JCheckBox forceRecomputeBox;
  private ArrayList groupCheckBoxes = new ArrayList();
  private ArrayList ULThreadCheckBoxes = new ArrayList();
  private ArrayList multiplierFields = new ArrayList();
  private ArrayList originalThreadSelected = new ArrayList();
  private ArrayList originalGroupSelected = new ArrayList();
  private ArrayList originalGroupMultiplier = new ArrayList();
  private Organizer organizer;
  private boolean cancelled = false;

  public CMTDialog(JFrame parent, Organizer organizer,
                   String experimentName,
                   String experimentId) {
    super(parent, "Threads and Groups", true); // modal dialog
    this.organizer = organizer; // to get unique names
    this.experimentName = experimentName;
    this.experimentId = experimentId;
    this.trialId = ExperimentDB.getTrialId(experimentId);
    JPanel panel = new JPanel(new BorderLayout());

    // Panel for threads
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
      
//        if (DBUtils.isMySQL())
//  	cb.setEnabled(false); // MySQL DB
      
      ULThreadCheckBoxes.add(cb);
      boolean sel = ExperimentDB.isULThreadSelected(trialId, ULDBThreads[i]);
      cb.setSelected(sel);
      originalThreadSelected.add(new Boolean(sel));      
      //if (i == (ULThreads.length-1))
      //  cb.setEnabled(false); // spare parts
      bottomPanel.add(cb,
                      new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                             GridBagConstraints.WEST,
                                             GridBagConstraints.NONE,
                                             new Insets(0, leftIndent, 5, 0),
                                             0, 0));
    }
    leftIndent = leftIndent - 5;
//     bottomPanel.add(new JLabel("Select Organization Groups:"),
//               new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
//                                      GridBagConstraints.WEST,
//                                      GridBagConstraints.NONE,
//                                      new Insets(0, leftIndent, 0, 0),
//                                      0, 0));
    leftIndent = leftIndent + 5;
    Map groupNameToId = ExperimentDB.getOrganizationGroups(experimentId);
    Set groups = groupNameToId.keySet();
    groupNames = (String[])groups.toArray(new String[groups.size()]);
    int nGroupNames = groupNames.length;
    for (int i = 0; i < nGroupNames; i++) {
      String groupName = (String)groupNames[i];
      JCheckBox groupCB = new JCheckBox(groupName);
      
//        if (DBUtils.isMySQL())
//  	groupCB.setEnabled(false); // MySQL DB
      
      boolean sel = ExperimentDB.isGroupSelected(trialId, groupName);
      groupCheckBoxes.add(groupCB);
      originalGroupSelected.add(new Boolean(sel));
      groupCB.setSelected(sel);
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
      Set members = 
        ExperimentDB.getOrganizationsInGroup(experimentId, 
                                         (String)groupNameToId.get(groupName));
      JList membersList = new JList(members.toArray());
      membersList.setVisibleRowCount(4);
      bottomPanel.add(new JScrollPane(membersList),
                new GridBagConstraints(x++, y++, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, 0, 5, 5),
                                       0, 0));
      x = 0;
    }

    leftIndent = leftIndent - 5;
    bottomPanel.add(new JLabel("Society:"),
              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(0, leftIndent, 0, 0),
                                     0, 0));
    leftIndent = leftIndent + 5;

    forceRecomputeBox = new JCheckBox("Force Recompute");
    bottomPanel.add(forceRecomputeBox,
                new GridBagConstraints(x, y+1, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, leftIndent, 5, 5),
                                       0, 0));
   
    panel.add(bottomPanel, BorderLayout.CENTER);

    // Buttons panel
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
    panel.add(buttonPanel, BorderLayout.SOUTH);
    getContentPane().add(panel);
    pack();
    // make dialog display over the middle of the caller's frame
    Point p = parent.getLocation();
    Dimension d = parent.getSize();
    int centerX = p.x + d.width/2;
    int centerY = p.y + d.height/2;
    Dimension myD = getSize();
    setLocation(new Point(centerX - myD.width/2, centerY - myD.height/2));
    show();
  }


  private void ok_actionPerformed() {
    cancelled = false;
    hide();
  }

  /**
   * Returns true if any UltraLog thread was selected and false otherwise.
   */

  public boolean isULThreadSelected() {
    int n = ULThreads.length;
    for (int i = 0; i < n; i++) {
      JCheckBox cb = (JCheckBox)ULThreadCheckBoxes.get(i);
      if (cb.isSelected())
        return true;
    }
    return false;
  }


  /**
   * Returns true if the force recompute society box was selected, false otherwise.
   */
  public boolean isForceRecomputeSelected() {
    return forceRecomputeBox.isSelected();
  }

  /**
   * If threads, groups or multipliers have been modified, then
   * create a new trial.
   */

  public void processResults() {
    boolean modified = false;
    int n = ULThreads.length;
    for (int i = 0; i < n; i++) {
      JCheckBox cb = (JCheckBox)ULThreadCheckBoxes.get(i);
      Boolean b = (Boolean)originalThreadSelected.get(i);
      if (b.booleanValue() == cb.isSelected())
        continue;
      if (!modified) {
        if (!cloneExperiment())
          return; // user cancelled modifications
        modified = true;
      }
      ExperimentDB.setULThreadSelected(trialId, ULDBThreads[i], 
                                       cb.isSelected());
    }
    n = groupCheckBoxes.size();
    for (int i = 0; i < n; i++) {
      String groupName = groupNames[i];
      JCheckBox cb = (JCheckBox)groupCheckBoxes.get(i);
      Boolean b = (Boolean)originalGroupSelected.get(i);
      if (b.booleanValue() != cb.isSelected()) {
        if (!modified) {
          if (!cloneExperiment())
            return; // user cancelled modifications
          modified = true;
        }
        ExperimentDB.setGroupSelected(trialId, groupName, cb.isSelected());
      }
      JTextField multiplierField = (JTextField)multiplierFields.get(i);
      int multiplier = ((Integer)originalGroupMultiplier.get(i)).intValue();
      try {
        int newMultiplier = Integer.parseInt(multiplierField.getText());
        if (multiplier == newMultiplier)
          continue;
        if (!modified) {
          if (!cloneExperiment())
            return; // user cancelled modifications
          modified = true;
        }
        ExperimentDB.setMultiplier(trialId, groupName, newMultiplier);
      } catch (NumberFormatException e) {
      }
    }
    if(forceRecomputeBox.isSelected()) {
      System.out.println("Force Recompute Checked");
      ExperimentDB.deleteCMTAssembly(experimentId);
      modified = true;
    }

    if (modified) {
      ExperimentDB.updateCMTAssembly(experimentId);
      trialId = ExperimentDB.getTrialId(experimentId);
    }
  }

  private boolean cloneExperiment() {
    // ask user to confirm cloning experiment
//      int result = JOptionPane.showConfirmDialog(this,
//                                                 "Modify Experiment",
//                                                 "Modify Experiment?",
//                                                 JOptionPane.YES_NO_OPTION,
//                                                 JOptionPane.QUESTION_MESSAGE);
//      if (result == JOptionPane.NO_OPTION)
//        return false;
    // if experiment name is in the database, then ask user for new name
    // or allow user to re-use existing name
    if (ExperimentDB.isExperimentNameInDatabase(experimentName)) {
      String name = organizer.getUniqueExperimentName(experimentName,
                                                      true);
      if (name == null)
        return false;
      experimentName = name;
    }
    //System.out.println("Cloning experiment: " + experimentName);
    experimentId = ExperimentDB.cloneExperiment(experimentId, 
                                                experimentName);
    trialId = ExperimentDB.getTrialId(experimentId);
    cloned = true;
    return true;
  }


  private void cancel_actionPerformed() {
    cancelled = true;
    hide();
    trialId = null;
  }

  public String getExperimentId() {
    return experimentId;
  }

  public String getExperimentName() {
    return experimentName;
  }

  public String getTrialId() {
    return trialId;
  }

  public boolean isCloned() {
    return cloned;
  }

  public boolean wasCancelled() {
    return cancelled;
  }

  public static void main(String[] args) {
    ArrayList trialNames = new ArrayList();
    CMTDialog d = new CMTDialog(null, null, "ExperimentName", "ExperimentID");
    d.show();
  }
}

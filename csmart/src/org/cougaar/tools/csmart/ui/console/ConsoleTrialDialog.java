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
import javax.swing.*;
import javax.swing.border.LineBorder;

public class ConsoleTrialDialog extends JDialog {
  private static final String[] ULThreads = {
    "Subsistence (Class 1)",
    "Fuel (Class 3)",
    "Construction Material (Class 4)",
    "Ammunition (Class 5)",
    "Spare Parts (Class 9)"
  };
  private ArrayList groupNames = new ArrayList();
  private ArrayList groupCheckBoxes = new ArrayList();
  private ArrayList ULThreadCheckBoxes = new ArrayList();
  private ArrayList multiplierFields = new ArrayList();
  private JRadioButton trialSelectionButton;
  private JRadioButton trialCreationButton;
  private JComboBox trialComboBox;

  public ConsoleTrialDialog() {
    super((java.awt.Frame)null, "Trial", true); // modal dialog
    JPanel panel = new JPanel(new BorderLayout());
    JPanel topPanel = new JPanel(new GridBagLayout());
    topPanel.setBorder(LineBorder.createGrayLineBorder());
    JPanel bottomPanel = new JPanel(new GridBagLayout());
    bottomPanel.setBorder(LineBorder.createGrayLineBorder());
    int x = 0;
    int y = 0;
    int leftIndent = 5;
    // insets are top, left, bottom, right
    ButtonGroup buttonGroup = new ButtonGroup();
    trialSelectionButton = new JRadioButton("Select Trial");
    buttonGroup.add(trialSelectionButton);
    trialSelectionButton.setSelected(true);
    topPanel.add(trialSelectionButton,
              new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(10, leftIndent, 5, 5),
                                     0, 0));
    String[] trialNames = getTrialNames();
    trialComboBox = new JComboBox(trialNames);
    topPanel.add(trialComboBox,
              new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.HORIZONTAL,
                                          new Insets(10, 0, 5, 5),
                                          0, 0));
    x = 0;
    trialCreationButton = new JRadioButton("Create Trial");
    //    trialCreationButton.setSelected(false);
    buttonGroup.add(trialCreationButton);
    bottomPanel.add(trialCreationButton,
              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(10, leftIndent, 5, 0),
                                     0, 0));
    leftIndent = leftIndent + 5;
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
      cb.setSelected(isULThreadSelected(ULThreads[i]));
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
    groupNames = getOrganizationGroups();
    int nGroupNames = groupNames.size();
    for (int i = 0; i < nGroupNames; i++) {
      String groupName = (String)groupNames.get(i);
      JCheckBox groupCB = new JCheckBox(groupName);
      groupCheckBoxes.add(groupCB);
      groupCB.setSelected(isGroupSelected(groupName));
      bottomPanel.add(groupCB,
                new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, leftIndent, 5, 0),
                                       0, 0));
      bottomPanel.add(new JLabel("Multiplier:"),
                new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                       GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, 0, 5, 5),
                                       0, 0));
      JTextField multiplierField = new JTextField(4);
      multiplierFields.add(multiplierField);
      multiplierField.setText(Integer.toString(getMultiplier(groupName)));
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
    panel.add(topPanel, BorderLayout.NORTH);
    panel.add(bottomPanel, BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.SOUTH);
    getContentPane().add(panel);
    pack();
  }

  private void ok_actionPerformed() {
    setVisible(false);
    if (trialSelectionButton.isSelected()) {
      System.out.println("Trial Selected: " + 
                         trialComboBox.getSelectedItem());
      return;
    }
    int n = ULThreads.length;
    for (int i = 0; i < n; i++) {
      JCheckBox cb = (JCheckBox)ULThreadCheckBoxes.get(i);
      String threadName = ULThreads[i];
      setULThreadSelected(threadName, cb.isSelected());
    }
    n = groupCheckBoxes.size();
    for (int i = 0; i < n; i++) {
      String groupName = (String)groupNames.get(i);
      JCheckBox cb = (JCheckBox)groupCheckBoxes.get(i);
      setGroupSelected(groupName, cb.isSelected());
      JTextField multiplierField = (JTextField)multiplierFields.get(i);
      try {
        setMultiplier(groupName, Integer.parseInt(multiplierField.getText()));
      } catch (NumberFormatException e) {
      }
    }
  }

  private void cancel_actionPerformed() {
    setVisible(false);
  }

  // for testing

  public String[] getTrialNames() {
    String[] trialNames = { "Trial Name 1", "Trial Name 2", "Trial Name 3" };
    return trialNames;
  }

  public ArrayList getOrganizationGroups() {
    ArrayList names = new ArrayList();
    names.add("Third Infantry Division");
    names.add("2nd Brigade");
    return names;
  }

  // interface to database

  private int getMultiplier(String groupName) {
    return 1;
  }

  private void setMultiplier(String groupName, int value) {
    System.out.println("Group: " + groupName + " value: " + value);
  }

  private boolean isULThreadSelected(String threadName) {
    return false;
  }

  private void setULThreadSelected(String threadName, boolean selected) {
    System.out.println("Thread: " + threadName +
                       " selected: " + selected);
  }

  private boolean isGroupSelected(String groupName) {
    return false;
  }

  private void setGroupSelected(String groupName, boolean selected) {
    System.out.println("Group: " + groupName +
                       " selected: " + selected);
  }
    
  public static void main(String[] args) {
    ConsoleTrialDialog d = new ConsoleTrialDialog();
    d.setVisible(true);
  }
}

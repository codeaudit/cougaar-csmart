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

package org.cougaar.tools.csmart.ui.experiment;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;

public class NodeArgumentDialog extends JDialog {
  Properties props;
  JTable argTable;
  NodeArgumentTableModel model;
  JTextArea args;
  int returnValue;

  public NodeArgumentDialog(String title, Properties props, boolean isLocal) {
    super((Frame)null, title, true); // display modal dialog
    Box nodeArgPanel = Box.createVerticalBox();
    JPanel argumentPanel = new JPanel();
    // ok and cancel buttons panel
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton("OK");
    // if user confirms the dialog and hasn't terminated editing a table cell
    // then get the value from that cell editor and set it in the cell
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Component c = argTable.getEditorComponent();
        if (c != null)
          if (c instanceof JTextField) {
            int rowIndex = argTable.getEditingRow();
            int colIndex = argTable.getEditingColumn();
            if (rowIndex != -1 && colIndex != -1)
              model.setValueAt(((JTextField)c).getText(), rowIndex, colIndex);
          } else
            System.out.println("Unexpected editor class: " + c.getClass());
        returnValue = JOptionPane.OK_OPTION;
        setVisible(false);
      }
    });
    buttonPanel.add(okButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        returnValue = JOptionPane.CANCEL_OPTION;
        setVisible(false);
      }
    });
    buttonPanel.add(cancelButton);

    // add JTable
    argTable = new JTable();
    // don't allow user to reorder columns
    argTable.getTableHeader().setReorderingAllowed(false);
    argTable.setColumnSelectionAllowed(false);
    argTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = new JScrollPane(argTable);
    JPanel tablePanel = new JPanel(new BorderLayout());
    tablePanel.setBorder(BorderFactory.createTitledBorder("-D Options"));
    tablePanel.add(scrollPane);

    // add buttons to add/delete properties
    JPanel tableButtonPanel = new JPanel();
    JButton addButton = new JButton("Add Property");
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        model.addRow("", "");
      }
    });
    tableButtonPanel.add(addButton);
    JButton deleteButton = new JButton("Delete Property");
    deleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int i = argTable.getSelectedRow();
        if (i != -1)
          model.removeRow(i);
      }
    });
    tableButtonPanel.add(deleteButton);
    JButton fileButton = new JButton("Read From File");
    fileButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        readPropertiesFromFile();
      }
    });
    tableButtonPanel.add(fileButton);
    tablePanel.add(tableButtonPanel, BorderLayout.SOUTH);

    argumentPanel.setBorder(BorderFactory.createTitledBorder("Arguments"));
    args = new JTextArea(10, 40);
    args.setToolTipText("Enter command line args one per line");
    argumentPanel.add(args);

    nodeArgPanel.add(tablePanel);
    nodeArgPanel.add(argumentPanel);
    nodeArgPanel.add(buttonPanel);
    getContentPane().add(nodeArgPanel);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        returnValue = JOptionPane.CLOSED_OPTION;
        NodeArgumentDialog.this.setVisible(false);
      }
    });

    // initialize text area and table from properties
    this.props = props;
    String value = props.getProperty(CSMARTConsole.COMMAND_ARGUMENTS);
    setArguments(value);
    model = new NodeArgumentTableModel(props, isLocal);
    argTable.setModel(model);

    pack();
  }

  /**
   * Return a value indicating how the dialog was dismissed: one of
   * JOptionPane.OK_OPTION, JOptionPane.CANCEL_OPTION, 
   * JOptionPane.CLOSED_OPTION
   */

  public int getValue() {
    return returnValue;
  }

  /**
   * Update the properties from the table.
   * @return true if any modifications, false otherwise
   */

  public boolean updateProperties() {
    boolean isModified = model.updateProperties();
    String s = getArguments();
    if (s != null && 
        !s.equals(props.getProperty(CSMARTConsole.COMMAND_ARGUMENTS))) {
      props.setProperty(CSMARTConsole.COMMAND_ARGUMENTS, s);
      isModified = true;
    }
    return isModified;
  }

  /**
   * Set the arguments displayed in the dialog.
   */

  private void setArguments(String arguments) {
    args.setText(arguments);
  }

  /**
   * Get the arguments displayed in the dialog.
   */

  private String getArguments() {
    return args.getText();
  }

  private void readPropertiesFromFile() {
    String dirName = ".";
    try {
      dirName = System.getProperty("org.cougaar.install.path");
    } catch (RuntimeException e) {
      // just use default
    }
    if (dirName == null)
      dirName = ".";
    JFileChooser chooser = new JFileChooser(dirName);
    int result = chooser.showOpenDialog(this);
    if (result != JFileChooser.APPROVE_OPTION)
      return;
    FileInputStream in = null;
    Properties properties = new Properties();
    try {
      in = new FileInputStream(chooser.getSelectedFile());
      properties.load(in);
    } catch (Exception e) {
      System.err.println("Exception reading properties file: " + e);
      return;
    }
    // add all properties from the file, overwriting any existing property
    Enumeration keys = properties.propertyNames();
    while (keys.hasMoreElements()) {
      String key = (String)keys.nextElement();
      if (key.equals(CSMARTConsole.COMMAND_ARGUMENTS))
        setArguments(properties.getProperty(key));
      else
        model.addRow(key, properties.getProperty(key));
    }
  }

  public static void main(String[] args) {
    Properties props = new Properties();
    props.setProperty("Color", "Red");
    props.setProperty("Number", "33");
    props.setProperty("Day", "Friday");
    NodeArgumentDialog nad = new NodeArgumentDialog("Test", props, true);
    nad.setVisible(true);
    if (nad.getValue() != JOptionPane.OK_OPTION)
      return;
    nad.updateProperties();
    props.list(System.out);
  }

}

    


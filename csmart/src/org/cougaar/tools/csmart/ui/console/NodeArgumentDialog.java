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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

public class NodeArgumentDialog extends JDialog {
  JTable argTable;
  DefaultTableModel nodeArgTableModel;
  JTextArea args;

  public NodeArgumentDialog(String title) {
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
              argTable.getModel().setValueAt(((JTextField)c).getText(),
                                             rowIndex, colIndex);
          } else
            System.out.println("Unexpected editor class: " + c.getClass());
        setVisible(false);
      }
    });
    buttonPanel.add(okButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
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
        Vector newData = new Vector(2);
        newData.add("");
        newData.add("");
        ((DefaultTableModel)argTable.getModel()).addRow(newData);
      }
    });
    tableButtonPanel.add(addButton);
    JButton deleteButton = new JButton("Delete Property");
    deleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int i = argTable.getSelectedRow();
        if (i != -1)
          ((DefaultTableModel)argTable.getModel()).removeRow(i);
      }
    });
    tableButtonPanel.add(deleteButton);
    tablePanel.add(tableButtonPanel, BorderLayout.SOUTH);

    argumentPanel.setBorder(BorderFactory.createTitledBorder("Arguments"));
    args = new JTextArea(10, 40);
    args.setToolTipText("Enter command line args one per line");
    argumentPanel.add(args);

    nodeArgPanel.add(tablePanel);
    nodeArgPanel.add(argumentPanel);
    nodeArgPanel.add(buttonPanel);
    getContentPane().add(nodeArgPanel);
    pack();
  }

  /**
   * Set the data to display in the table.
   * The data argument is a vector of vectors; data.size() is the
   * number of rows.
   */

  public void setData(Vector data, int nColumns) {
    Vector columnNames = new Vector(nColumns);
    columnNames.add("Name");
    columnNames.add("Value");
    if (nColumns > 2) columnNames.add("Global");
    nodeArgTableModel = new DefaultTableModel(data, columnNames);
    argTable.setModel(nodeArgTableModel);
  }

  public void setArguments(String arguments) {
    args.setText(arguments);
  }

  /**
   * Return the data from the table.
   * The return value is a vector of vectors; the size is the
   * number of rows.
   */

  public Vector getData() {
    return nodeArgTableModel.getDataVector();
  }

  public String getArguments() {
    return args.getText();
  }

  public static void main(String[] args) {
    NodeArgumentDialog nad = new NodeArgumentDialog("Test");
    Vector data = new Vector();
    Vector row = new Vector();
    row.add("Color");
    row.add("Red");
    data.add(row);
    row = new Vector();
    row.add("Number");
    row.add("33");
    data.add(row);
    row = new Vector();
    row.add("Day");
    row.add("Friday");
    data.add(row);
    nad.setData(data, 2);
    nad.setVisible(true);
    Vector newData = nad.getData();
    for (int i = 0; i < newData.size(); i++) {
      row = (Vector)newData.get(i);
      System.out.println(row.get(0) + ":" + row.get(1));
    }
  }

}

    


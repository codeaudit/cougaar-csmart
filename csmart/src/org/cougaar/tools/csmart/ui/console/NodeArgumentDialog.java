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

// TODO: add listeners to the table so that we can detect a change
// without the user having to enter a carriage-return

public class NodeArgumentDialog extends JDialog {
  JTable argTable;
  DefaultTableModel nodeArgTableModel;

  public NodeArgumentDialog() {
    super((Frame)null, true); // display modal dialog
    JPanel nodeArgPanel = new JPanel(new BorderLayout());
    // ok and cancel buttons panel
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
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

    // panel for table
    JPanel tablePanel = new JPanel();

    // add JTable
    argTable = new JTable();
    // don't allow user to reorder columns
    argTable.getTableHeader().setReorderingAllowed(false);
    argTable.setColumnSelectionAllowed(false);
    argTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = new JScrollPane(argTable);
    tablePanel.setLayout(new BorderLayout());
    tablePanel.add(scrollPane, BorderLayout.CENTER);

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

    nodeArgPanel.add(tablePanel, BorderLayout.CENTER);
    nodeArgPanel.add(buttonPanel, BorderLayout.SOUTH);
    getContentPane().add(nodeArgPanel);
    pack();
  }

  /**
   * Set the data to display in the table.
   * The data argument is a vector of vectors; data.size() is the
   * number of rows.
   */

  public void setData(Vector data) {
    Vector columnNames = new Vector(2);
    columnNames.add("Name");
    columnNames.add("Value");
    nodeArgTableModel = new DefaultTableModel(data, columnNames);
    argTable.setModel(nodeArgTableModel);
  }

  /**
   * Return the data from the table.
   * The return value is a vector of vectors; the size is the
   * number of rows.
   */

  public Vector getData() {
    return nodeArgTableModel.getDataVector();
  }

  public static void main(String[] args) {
    NodeArgumentDialog nad = new NodeArgumentDialog();
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
    nad.setData(data);
    nad.setVisible(true);
    Vector newData = nad.getData();
    for (int i = 0; i < newData.size(); i++) {
      row = (Vector)newData.get(i);
      System.out.println(row.get(0) + ":" + row.get(1));
    }
  }

}

    


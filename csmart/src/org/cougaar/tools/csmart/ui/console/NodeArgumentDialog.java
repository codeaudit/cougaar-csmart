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
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Vector;

public class NodeArgumentDialog extends JDialog {
  JTable argTable;
  NodeArgumentTableModel nodeArgTableModel;
  ArrayList names;
  ArrayList values;

  public NodeArgumentDialog() {

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
    argTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    JScrollPane scrollPane = new JScrollPane(argTable);
    tablePanel.setLayout(new BorderLayout());
    tablePanel.add(scrollPane, BorderLayout.CENTER);

    nodeArgPanel.add(tablePanel, BorderLayout.CENTER);
    nodeArgPanel.add(buttonPanel, BorderLayout.SOUTH);
    getContentPane().add(nodeArgPanel);
    pack();
  }

  public void setArguments(ArrayList names, ArrayList values) {
    this.names = names;
    this.values =  values;
    argTable.setForeground(Color.black);
    nodeArgTableModel = new NodeArgumentTableModel(names, values);
    argTable.setModel(nodeArgTableModel);
  }
    
  public static void main(String[] args) {
    NodeArgumentDialog nad = new NodeArgumentDialog();
    nad.setVisible(true);
    ArrayList names = new ArrayList();
    ArrayList values = new ArrayList();
    names.add("Me");
    names.add("you");
    names.add("them");
    values.add("one");
    values.add("two");
    values.add("three");
    nad.setArguments(names, values);
  }

}

    


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

import org.cougaar.tools.csmart.util.ReadOnlyProperties;
import org.cougaar.tools.csmart.ui.console.CSMARTConsole;

/**
 * Creates a table of Node Arguments;
 * first column is Argument name
 * second column is Argument Value
 */

public class NodeArgumentTableModel extends AbstractTableModel {
  Properties props;
  ArrayList names = new ArrayList();
  ArrayList values = new ArrayList();
  ArrayList globalFlags;
  boolean isLocal;

  public NodeArgumentTableModel(Properties props, boolean isLocal) {
    this.props = props;
    this.isLocal = isLocal;
    if (isLocal)
      globalFlags = new ArrayList();
    Enumeration keys = props.propertyNames();
    while (keys.hasMoreElements()) {
      String key = (String)keys.nextElement();
      if (!key.equals(CSMARTConsole.COMMAND_ARGUMENTS)) {
        names.add(key);
        values.add(props.getProperty(key));
        if (isLocal) {
          // if local value is null, then display *
          // to indicate value is from global properties
          if (props.get(key) == null)
            globalFlags.add("*");
          else
            globalFlags.add("");
        }
      }
    }
  }

  /**
   * Check for editability of read only property values.
   */

  public boolean isCellEditable(int row, int col) {
    if ((col == 0 || col == 1)) {
      if (props instanceof ReadOnlyProperties) {
        String name = (String)names.get(row);
        if (props.getProperty(name) == null)
          return true;
        else
          return !((ReadOnlyProperties)props).isReadOnly(name);
      } else
        return true;
    } else
      return false;
  }

  public int getRowCount() {
    return names.size();
  }

  public int getColumnCount() {
    if (isLocal)
      return 3;
    else
      return 2;
  }
  
  public Object getValueAt(int row, int column) {
    if (row < 0 || row >= getRowCount() ||
	column < 0 || column >= getColumnCount())
      return null; // index out of range
    if (column == 0) 
      return names.get(row);
    else if (column == 1)
      return values.get(row);
    else if (column == 2)
      return globalFlags.get(row);
    else 
      return null;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= getRowCount() ||
	columnIndex < 0 || columnIndex >= getColumnCount())
      return; // index out of range
    if (columnIndex == 0) { // it's a name
      names.set(rowIndex, aValue);
    }
    if (columnIndex == 1) { // it's a value
      values.set(rowIndex, aValue);
    } else if (columnIndex == 2)
      return; // global flag is not editable
    fireTableCellUpdated(rowIndex, columnIndex);
  }

  public String getColumnName(int column) {
    if (column >= getColumnCount())
      return null;
    if (column == 0)
      return "Name";
    else if (column == 1)
      return "Value";
    else if (column == 2)
      return "Global";
    else
      return null; // invalid column index
  }

  /**
   * If name exists, set value to new value, else add new row.
   */

  public void addRow(String name, String value) {
    int index = names.indexOf(name);
    if (index == -1) {
      names.add(name);
      values.add(value);
      if (isLocal)
        globalFlags.add("");
      fireTableRowsInserted(index, index);
    } else {
      if (isCellEditable(index, 0)) {
        values.set(index, value);
        if (isLocal)
          globalFlags.set(index, "");
        fireTableRowsUpdated(index, index);
      }
    }
  }

  public void removeRow(int row) {
    if (isCellEditable(row, 0)) {
      names.remove(row);
      values.remove(row);
      if (isLocal)
        globalFlags.remove(row);
      fireTableRowsDeleted(row, row);
    }
  }

  /**
   * Update properties based on table values.
   * @return true if any modifications, false otherwise
   */

  public boolean updateProperties() {
    boolean isModified = false;
    // add new or modified properties
    for (int i = 0; i < names.size(); i++) {
      String name = (String)names.get(i);
      if (name.length() == 0)
        continue; // ignore blank names
      String value = props.getProperty(name);
      if (value == null || !value.equals(values.get(i))) {
        props.setProperty(name, (String)values.get(i));
        isModified = true;
      }
    }
    // remove properties
    Enumeration keys = props.propertyNames();
    while (keys.hasMoreElements()) {
      String key = (String)keys.nextElement();
      if (key.equals(CSMARTConsole.COMMAND_ARGUMENTS))
        continue;
      if (!names.contains(key)) {
        props.remove(key);
        isModified = true;
      }
    }
    return isModified;
  }
}


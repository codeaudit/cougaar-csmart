/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.experiment;

import org.cougaar.tools.csmart.ui.console.CSMARTConsoleModel;
import org.cougaar.tools.csmart.util.ReadOnlyProperties;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

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
      String key = (String) keys.nextElement();
      if (!key.equals(CSMARTConsoleModel.COMMAND_ARGUMENTS)) {
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
        String name = (String) names.get(row);
        if (props.getProperty(name) == null)
          return true;
        else
          return !((ReadOnlyProperties) props).isReadOnly(name);
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

    // trim whitespace off of the value.
    if (aValue instanceof String) {
      aValue = ((String) aValue).trim();

      // AppServer cant handle values containing whitespace
      if (((String) aValue).indexOf(' ') != -1) {
        // Now what? Reject edit? Tell user?
        return;
      }
    }

    // See if the value really changed. If not, return
    if ((aValue == null && getValueAt(rowIndex, columnIndex) == null) || (aValue != null && aValue.equals(getValueAt(rowIndex, columnIndex)))) {
      return;
    }

    // changing the name or value makes it local
    if (columnIndex == 0) { // it's a name
      // What if aValue is now null or empty? Is that allowed? I think not.
      if (aValue == null || aValue.toString().equals("")) {
        return;
      }
      names.set(rowIndex, aValue);
    } else if (columnIndex == 1) { // it's a value
      values.set(rowIndex, aValue);
    } else if (columnIndex == 2)
      return; // global flag is not editable

    fireTableCellUpdated(rowIndex, columnIndex);
    if (isLocal) {
      globalFlags.set(rowIndex, "");
      fireTableCellUpdated(rowIndex, 2);
    }
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
    if (name == null || value == null)
      return;

    // Trim excess whitespace off of value and name
    if (value != null)
      value = value.trim();
    if (name != null)
      name = name.trim();

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
      String name = (String) names.get(i);
      if (name.length() == 0)
        continue; // ignore blank names
      String value = props.getProperty(name);
      if (value == null || !value.equals(values.get(i))) {
        props.setProperty(name, (String) values.get(i));
        isModified = true;
      }
    }
    // remove properties
    Enumeration keys = props.propertyNames();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      if (key.equals(CSMARTConsoleModel.COMMAND_ARGUMENTS))
        continue;
      if (!names.contains(key)) {
        props.remove(key);
        isModified = true;
      }
    }
    return isModified;
  }
}


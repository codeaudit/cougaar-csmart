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
package org.cougaar.tools.csmart.ui.console;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.lang.reflect.Array;

/**
 * Creates a table of Node Arguments;
 * first column is Argument name
 * second column is Argument Value
 */

public class NodeArgumentTableModel extends AbstractTableModel {
  ArrayList names;
  ArrayList values;

  public NodeArgumentTableModel(ArrayList names, ArrayList values) {
    this.names = names;
    this.values = values;
  }

  /**
   * Cells aren't editable, maybe they should be.
   */

  public boolean isCellEditable(int row, int col) {
    return false;
  }

  public int getRowCount() {
    return names.size();
  }

  public int getColumnCount() {
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
    else 
      return null;
  }

  public String getColumnName(int column) {
    if (column == 0)
      return "Name";
    else if (column == 1)
      return "Value";
    else
      return null; // invalidate column index
  }
}


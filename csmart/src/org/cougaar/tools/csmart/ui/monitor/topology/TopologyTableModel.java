/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.topology;

import java.util.*;
import javax.swing.table.AbstractTableModel;

/**
 * Supports a table in which the columns are:
 * AGENT, NODE, HOST, SITE, ENCLAVE, INCARNATION, MOVE_ID, IS_NODE, STATUS
 */

public class TopologyTableModel extends AbstractTableModel {
  ArrayList rows;
  int columnCount;
  int rowCount;

  public TopologyTableModel(ArrayList values) {
    updateValues(values);
  }

  public void updateValues(ArrayList values) {
    if (values == null) {
      columnCount = 0;
      rowCount = 0;
      return;
    }
    rows = new ArrayList();
    rowCount = values.size();
    columnCount = 0;
    for (int i = 0; i < rowCount; i++) {
      ArrayList row = new ArrayList();
      StringTokenizer st = new StringTokenizer((String)values.get(i), ",");
      int index = 0;
      while (st.hasMoreTokens()) {
        String s = st.nextToken();
        s = s.trim();
        Class c = getColumnClass(index);
        if (c.equals(String.class))
          row.add(s);
        else if (c.equals(Integer.class)) {
          try {
            row.add(new Integer(s));
          } catch (NumberFormatException nfe) {
            row.add("");
          }
        }
        index++;
      }
      columnCount = Math.max(columnCount, row.size());
      rows.add(row);
    }
  }

  public int getColumnCount() {
    return columnCount;
  }

  public int getRowCount() { 
    return rowCount;
  }
  
  public Object getValueAt(int row, int col) { 
    return ((ArrayList)(rows.get(row))).get(col);
  }

  public String getColumnName(int index) {
    if (index == 0)
      return "Agent";
    else if (index == 1)
      return "Node";
    else if (index == 2)
      return "Host";
    else if (index == 3)
      return "Site";
    else if (index == 4)
      return "Enclave";
    else if (index == 5)
      return "Incarnation";
    else if (index == 6)
      return "Move Id";
    else if (index == 7)
      return "Type";
    else if (index == 8)
      return "Status";
    else
      return "";
  }

  public Class getColumnClass(int index) {
    return String.class; // default
  }

}
 

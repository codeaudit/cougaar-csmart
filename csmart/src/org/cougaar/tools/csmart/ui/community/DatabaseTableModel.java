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

// Adapted from Java Swing TableExample demo

package org.cougaar.tools.csmart.ui.community;

import java.util.ArrayList;
import java.util.Collections;
import java.sql.*;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import org.cougaar.tools.csmart.core.db.DBUtils;

public class DatabaseTableModel extends AbstractTableModel {
  String[]            columnNames = {};
  ArrayList rows = new ArrayList();
  ResultSetMetaData   metaData;

  /**
   * Execute query and use results to fill table.
   * @param query SQL query
   */
  public void executeQuery(String query) {
    Connection conn = null;
    try {
      conn = DBUtils.getConnection();
      if (conn == null) {
        System.err.println("Could not get connection to database");
        return;
      }
      Statement statement = conn.createStatement();
      ResultSet resultSet = statement.executeQuery(query);
      metaData = resultSet.getMetaData();

      // get column names
      int numberOfColumns =  metaData.getColumnCount();
      columnNames = new String[numberOfColumns];
      for(int column = 0; column < numberOfColumns; column++) {
        columnNames[column] = metaData.getColumnLabel(column+1);
      }

      // if there is data (i.e. column count non-zero), get all rows
      if (numberOfColumns != 0) {
        rows = new ArrayList();
        while (resultSet.next()) {
          ArrayList newRow = new ArrayList();
          for (int i = 1; i <= getColumnCount(); i++) {
            newRow.add(resultSet.getString(i));
          }
          rows.add(newRow);
        }
      }
      fireTableChanged(null); // Tell the listeners a new table has arrived.
      resultSet.close();
      statement.close();
    }
    catch (SQLException ex) {
      System.err.println(ex + " in query: " + query);
    } finally {
      try {
        if (conn != null)
          conn.close();
      } catch (SQLException e) {
      }
    }
  }

  /**
   * Return all values in the specified column; removes duplicates.
   * @param column index of column
   * @return unique values in that column
   */
  public ArrayList getKnownValues(int columnIndex) {
    ArrayList values = new ArrayList();
    int nRows = getRowCount();
    for (int i = 0; i < nRows; i++) {
      Object o = getValueAt(i, columnIndex);
      if (!values.contains(o))
        values.add(o);
    }
    return values;
  }

  /**
   * Add a row to the table.  Adds with empty strings.
   */
  public void addRow() {
    int n = getColumnCount();
    if (n == 0)
      return; // can't do anything if we don't have a table defined
    int newRowIndex = rows.size();
    ArrayList newRow = new ArrayList(1);
    for (int i = 0; i < n; i++) {
      newRow.add("");
    }
    rows.add(newRow);
    fireTableRowsInserted(newRowIndex, newRowIndex);
  }

  /**
   * Delete the specified row and update the database.
   * @param rowIndex the index of the row to delete
   */
  public void deleteRow(int rowIndex) {
    if (rowIndex >= rows.size()) 
      return;
    try {
      String tableName = metaData.getTableName(1);
      String query = "delete from "+ tableName + " where ";
      int n = getColumnCount();
      for (int col = 0; col < n; col++) {
        String colName = getColumnName(col);
        if (colName.equals(""))
          continue;
        if (col != 0)
          query = query + " and ";
        query = query + colName +" = "+
          dbRepresentation(col, getValueAt(rowIndex, col));
      }
      System.out.println(query);
      executeQuery(query);
    } catch (SQLException e) {
      System.err.println("Update failed");
    }
    rows.remove(rowIndex);
    fireTableRowsDeleted(rowIndex, rowIndex);
  }

  /**
   * Make the table empty.
   */
  public void clear() {
    rows = new ArrayList();
    columnNames = new String[0];
    fireTableChanged(null);
  }

  //////////////////////////////////////////////////////////////////////////
  //
  //             Implementation of the TableModel Interface
  //             All methods here need corresponding wrapper
  //             methods in TableSorter
  //
  //////////////////////////////////////////////////////////////////////////

  // MetaData

  public String getColumnName(int column) {
    if (columnNames[column] != null) {
      return columnNames[column];
    } else {
      return "";
    }
  }

  public Class getColumnClass(int column) {
    int type;
    String typeName;
    try {
      type = metaData.getColumnType(column+1);
      typeName = metaData.getColumnTypeName(column+1);
    }
    catch (SQLException e) {
      return super.getColumnClass(column);
    }
    switch(type) {
    case Types.CHAR:
    case Types.VARCHAR:
    case Types.LONGVARCHAR:
      return String.class;

    case Types.BIT:
      return Boolean.class;

    case Types.TINYINT:
    case Types.SMALLINT:
    case Types.INTEGER:
      return Integer.class;

    case Types.BIGINT:
      return Long.class;

    case Types.FLOAT:
    case Types.DOUBLE:
      return Double.class;

    case Types.DATE:
      return java.sql.Date.class;

    default:
      return Object.class;
    }
  }

  public boolean isCellEditable(int row, int column) {
    try {
      return metaData.isWritable(column+1);
    }
    catch (SQLException e) {
      return false;
    }
  }

  public int getColumnCount() {
    return columnNames.length;
  }

  // Data methods

  public int getRowCount() {
    return rows.size();
  }

  public Object getValueAt(int aRow, int aColumn) {
    ArrayList row = (ArrayList)rows.get(aRow);
    return row.get(aColumn);
  }

  private String dbRepresentation(int column, Object value) {
    int type;

    if (value == null) {
      return "null";
    }

    try {
      type = metaData.getColumnType(column+1);
    }
    catch (SQLException e) {
      return value.toString();
    }

    switch(type) {
    case Types.INTEGER:
    case Types.DOUBLE:
    case Types.FLOAT:
      return value.toString();
    case Types.BIT:
      return ((Boolean)value).booleanValue() ? "1" : "0";
    case Types.DATE:
      return value.toString(); // This will need some conversion.
    default:
      return "\""+value.toString()+"\"";
    }

  }

  public void setValueAt(Object value, int row, int column) {
    try {
      String tableName = metaData.getTableName(column+1);
      String columnName = getColumnName(column);
      String query =
        "update "+tableName+
        " set "+columnName+" = "+dbRepresentation(column, value)+
        " where ";
      // We don't have a model of the schema so we don't know the
      // primary keys or which columns to lock on. To demonstrate
      // that editing is possible, we'll just lock on everything.
      for(int col = 0; col<getColumnCount(); col++) {
        String colName = getColumnName(col);
        if (colName.equals("")) {
          continue;
        }
        if (col != 0) {
          query = query + " and ";
        }
        query = query + colName +" = "+
          dbRepresentation(col, getValueAt(row, col));
      }
      System.out.println(query);
      executeQuery(query);
    }
    catch (SQLException e) {
      System.err.println("Update failed");
    }
    ArrayList dataRow = (ArrayList)rows.get(row);
    dataRow.set(column, value);
  }

}

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

import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.table.AbstractTableModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseTableModel extends AbstractTableModel {
  private static final String GET_ALL_COMMUNITY_INFO_QUERY = "queryAllCommunityInfo";
  private static final String GET_COMMUNITY_INFO_QUERY = "queryCommunityInfo";
  private static final String GET_ENTITY_INFO_QUERY = "queryEntityInfo";
  private static final String GET_CHILDREN_ENTITY_INFO_QUERY = 
    "queryChildrenEntityInfo";

  private String[] columnNames = {};
  private ArrayList rows = new ArrayList();
  private ResultSetMetaData metaData;
  private String communityName;

  private String assemblyId = null;

  private transient Logger log;

  private HashMap substitutions = new HashMap();

  public DatabaseTableModel() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public DatabaseTableModel(String assemblyId) {
    log = CSMART.createLogger(this.getClass().getName());
    setAssemblyId(assemblyId);
  }

  public void setAssemblyId(String assemblyId) {
    this.assemblyId = assemblyId;
    substitutions = getSubstitutions(assemblyId);
  }

  public static HashMap getSubstitutions(String assemblyId) {
    HashMap subs = new HashMap();
    subs.put(":assembly_id:", assemblyId);

    // FIXME: This says if assembly_id is null, use none at all
    // Maybe instead use all?
    if (assemblyId != null)
      subs.put(":assembly_match:", "IN ('" + assemblyId + "')");
    else 
      subs.put(":assembly_match:", "IS NULL");

    return subs;
  }

  /**
   * Methods for getting information from the database and updating the table model.
   */
  public void getAllCommunityInfo(String communityId) {
    substitutions.put(":community_id", communityId);
    String query =
      DBUtils.getQuery(GET_ALL_COMMUNITY_INFO_QUERY, substitutions);
    executeQuery(query);
  }

  public void getCommunityInfo(String communityId) {
    substitutions.put(":community_id", communityId);
    String query =
      DBUtils.getQuery(GET_COMMUNITY_INFO_QUERY, substitutions);
    executeQuery(query);
  }

  public void getEntityInfo(String communityId, String entityId) {
    substitutions.put(":community_id", communityId);
    substitutions.put(":entity_id", entityId);
    String query =
      DBUtils.getQuery(GET_ENTITY_INFO_QUERY, substitutions);
    executeQuery(query);
    communityName = communityId; // used if user adds parameters to this entity
  }

  public void getChildrenEntityInfo(String communityId, String childrenEntityIds) {
    substitutions.put(":community_id", communityId);
    substitutions.put(":children_entity_ids", childrenEntityIds);
    String query =
      DBUtils.getQuery(GET_CHILDREN_ENTITY_INFO_QUERY, substitutions);
    executeQuery(query);
  }

  private void executeQuery(String query) {
    Connection conn = null;
    try {
      conn = DBUtils.getConnection();
      if (conn == null) {
        if(log.isErrorEnabled()) {
          log.error("Could not get connection to database");
        }
        return;
      }
      Statement statement = conn.createStatement();
      ResultSet resultSet = statement.executeQuery(query);

      // if it's an update or insert don't change rows, columns or metadata
      if (!query.startsWith("update") && !query.startsWith("UPDATE") && !query.startsWith("INSERT") && 
          !query.startsWith("insert")) {
        metaData = resultSet.getMetaData();
        // get column names
        int numberOfColumns =  metaData.getColumnCount();
        columnNames = new String[numberOfColumns];
        for (int column = 0; column < numberOfColumns; column++)
          columnNames[column] = metaData.getColumnLabel(column+1);
        // if there is data (i.e. column count non-zero), get all rows
        if (numberOfColumns != 0) {
          rows = new ArrayList();
          while (resultSet.next()) {
            ArrayList newRow = new ArrayList();
            for (int i = 1; i <= getColumnCount(); i++)
              newRow.add(resultSet.getString(i));
            rows.add(newRow);
          }
        }
      }
      fireTableChanged(null); // Tell the listeners a new table has arrived.
      resultSet.close();
      statement.close();
    }
    catch (SQLException ex) {
      if(log.isErrorEnabled()) {
        log.error("Exception in query: " + query, ex);
      }
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
   * Delete the specified row and update the database.
   * @param rowIndex the index of the row to delete
   */
  public void deleteRow(int row) {
    if (row >= rows.size()) 
      return;
    String tableName = "";
    try {
      tableName = metaData.getTableName(1);
    } catch (SQLException e) {
      if(log.isErrorEnabled()) {
        log.error("Exception getting table name: ", e);
      }
      return;
    }
    if (tableName.equalsIgnoreCase("community_attribute") || tableName.equalsIgnoreCase("ca"))
      CommunityDBUtils.deleteCommunityAttribute( 
                               dbRepresentation(0, getValueAt(row, 0)),
                               dbRepresentation(1, getValueAt(row, 1)),
                               dbRepresentation(2, getValueAt(row, 2)),
			       assemblyId);
    else if (tableName.equalsIgnoreCase("community_entity_attribute") || tableName.equalsIgnoreCase("cea"))
      CommunityDBUtils.deleteEntityAttribute(communityName,
                               dbRepresentation(0, getValueAt(row, 0)),
                               dbRepresentation(1, getValueAt(row, 1)),
                               dbRepresentation(2, getValueAt(row, 2)),
					     assemblyId);
    else {
      if(log.isErrorEnabled()) {
        log.error("Attempting to delete from unknown table: " + tableName);
      }
    }
    rows.remove(row);
    fireTableRowsDeleted(row, row);
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
    String columnName = getColumnName(column);
    if (columnName != null) {
      if (columnName.equalsIgnoreCase("ENTITY_ID") || columnName.equalsIgnoreCase("COMMUNITY_ID") || columnName.equalsIgnoreCase("Entity") || columnName.equalsIgnoreCase("Entity ID") || columnName.equalsIgnoreCase("Name") || columnName.equalsIgnoreCase("Community ID") || columnName.equalsIgnoreCase("Community")) {
	if (log.isDebugEnabled()) {
	  log.debug("isCellEditable: Column " + column + "(" + columnName + ") not editable by name.");
	}
	return false;
      }
    }
    
    try {
      return metaData.isWritable(column+1);
    }
    catch (SQLException e) {
      if(log.isErrorEnabled()) {
        log.error("Exception on is cell editable: " +
                  row + "," + column, e);
      }
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
      return value.toString();
    }

  }

  /**
   * Update the database table with the new information.
   * @param value new value
   * @param row index of row in which to set value
   * @param column index of column in which to set value
   */
  public void setValueAt(Object value, int row, int column) {
    if ((value != null && value.equals(getValueAt(row, column))) || value == null && getValueAt(row, column) == null) {
      if (log.isDebugEnabled()) {
	log.debug("setValueAt (" + row + ", " + column + ") not changing unchanged value " + value);
      }
      return;
    }
    String tableName = "";
    try {
      tableName = metaData.getTableName(column+1);
    } catch (SQLException e) {
      if(log.isErrorEnabled()) {
        log.error("Exception getting column name: ", e);
      }
      return;
    }
    if (tableName.equalsIgnoreCase("community_attribute") || tableName.equalsIgnoreCase("ca"))
      updateCommunityAttributeTable(value, row, column);
    else if (tableName.equalsIgnoreCase("community_entity_attribute") || tableName.equalsIgnoreCase("cea"))
      updateCommunityEntityAttributeTable(value, row, column);
    else 
      if(log.isErrorEnabled()) {
        log.error("Attempting to set value for unknown table: " + tableName);
      }
  }

  private void updateCommunityAttributeTable(Object value, int row, int column) {
    String columnName = getColumnName(column);
    String communityId = (String)getValueAt(row, 0);
    if (communityId == null) {
      log.error("updateCommAtt on column " + columnName + " has null commId. CommName=" + communityName + " in row " + row);
    }
    if (columnName == null) {
      log.error("updateCommAtt has null columnName for community " + communityId + " and column " + column + " in row: " + row);
    }
    String dbValue = dbRepresentation(column, value);
    if (dbValue == null) {
      log.error("updateCommAtt has null dbValue on column " + columnName + ", Comm: " + communityName + ", commId: " + communityId + " in row " + row);
    }

    if (columnName.equalsIgnoreCase("ATTRIBUTE_ID") || columnName.equalsIgnoreCase("COMMUNITY_ATTRIBUTE_ID")) {
      // Bug 1905: Disallow empty values here.
      dbValue = dbValue.trim();
      if (dbValue.equals("") || dbValue.equals("null")) {
	if (log.isInfoEnabled()) {
	  log.info("updateCommAttribute: Empty value for Attribute_ID not allowed. Ignoring change.");
	}
	return;
      }

      // This throws an exception if it looks like a duplicate
      try {
	CommunityDBUtils.setCommunityAttributeId(communityId, dbValue,
						 dbRepresentation(1, getValueAt(row, 1)),
						 dbRepresentation(2, getValueAt(row, 2)),
						 assemblyId);
      } catch (IllegalArgumentException iae) {
	if (log.isWarnEnabled()) {
	  log.warn("updateCommAtt failed to set the Att ID for comm " + communityId + ": " + iae.toString());
	}
	// Couldnt make the edit, so dont update the UI
	return;
      }
    } else if (columnName.equalsIgnoreCase("ATTRIBUTE_VALUE") || columnName.equalsIgnoreCase("COMMUNITY_ATTRIBUTE_VALUE")) {
      // This throws an exception if it looks like a duplicate
      try {
	CommunityDBUtils.setCommunityAttributeValue(communityId, dbValue,
                               dbRepresentation(1, getValueAt(row, 1)),
                               dbRepresentation(2, getValueAt(row, 2)),
						  assemblyId);
      } catch (IllegalArgumentException iae) {
	if (log.isWarnEnabled()) {
	  log.warn("updateCmmAtt failed to set the Att Value for com " + communityId + ": " + iae.toString());
	}
	// Couldnt make the edit, so dont update the UI
	return;
      }
    } else if (columnName.equalsIgnoreCase("COMMUNITY_ID")) {
      // Can't edit the community ID here
      if (log.isInfoEnabled()) {
	log.info("Cannot edit the " + columnName + " column here.");
      }
      return;
    } else {
      if (log.isErrorEnabled()) {
        log.error("Attempting to set value for unknown column name: " + 
                  columnName);
      }
      return;
    }
    ArrayList dataRow = (ArrayList)rows.get(row);
    dataRow.set(column, value);
  }

  private void updateCommunityEntityAttributeTable(Object value, int row, int column) {
    String columnName = getColumnName(column);
    String entityId = (String)getValueAt(row, 0);
    if (entityId == null) {
      log.error("updateCommEntityAtt on column " + columnName + " has null entityId. CommName=" + communityName + " in row " + row);
    }
    if (communityName == null) {
      log.error("updateCommEntityAtt has null commName for entity " + entityId + " and column " + columnName + " in row: " + row);
    }
    String dbValue = dbRepresentation(column, value);
    if (dbValue == null) {
      log.error("updateCommEntityAtt has null dbValue on column " + columnName + ", Comm: " + communityName + ", entity: " + entityId + " in row " + row);
    }

    if (columnName.equalsIgnoreCase("ATTRIBUTE_ID") || columnName.equalsIgnoreCase("ENTITY_ATTRIBUTE_ID")) {
      // Bug 1905: Disallow empty values here.
      dbValue = dbValue.trim();
      if (dbValue.equals("") || dbValue.equals("null")) {
	if (log.isInfoEnabled()) {
	  log.info("updateCommEntityAttribute: Empty value for Attribute_ID not allowed. Ignoring change.");
	}
	return;
      }

      try {
      // This throws an exception if it looks like a duplicate
	CommunityDBUtils.setEntityAttributeId(communityName, entityId, dbValue,
                               dbRepresentation(1, getValueAt(row, 1)),
                               dbRepresentation(2, getValueAt(row, 2)),
					    assemblyId);
      } catch (IllegalArgumentException iae) {
	if (log.isWarnEnabled()) {
	  log.warn("updateCommEntAtt failed to set the Att ID for comm " + communityName + ", entity " + entityId + ": " + iae.toString());
	}
	// Couldnt make the edit, so dont update the UI
	return;
      }
    } else if (columnName.equalsIgnoreCase("ATTRIBUTE_VALUE") || columnName.equalsIgnoreCase("ENTITY_ATTRIBUTE_VALUE")) {
      // This throws an exception if it looks like a duplicate
      try {
	CommunityDBUtils.setEntityAttributeValue(communityName, entityId, dbValue,
                               dbRepresentation(1, getValueAt(row, 1)),
                               dbRepresentation(2, getValueAt(row, 2)),
					       assemblyId);
      } catch (IllegalArgumentException iae) {
	if (log.isWarnEnabled()) {
	  log.warn("updateCommEntAtt failed to set the Att value for comm " + communityName + ", entity " + entityId + ": " + iae.toString());
	}
	// Couldnt make the edit, so dont update the UI
	return;
      }
    } else if (columnName.equalsIgnoreCase("ENTITY_ID")) {
      // Can't edit the community ID here
      if (log.isInfoEnabled()) {
	log.info("Cannot edit the " + columnName + " column here.");
      }
      return;
    } else {
      if (log.isErrorEnabled()) {
        log.error("Attempting to set value for unknown column name: " + 
                  columnName);
      }
      return;
    }
    ArrayList dataRow = (ArrayList)rows.get(row);
    dataRow.set(column, value);
  }
}

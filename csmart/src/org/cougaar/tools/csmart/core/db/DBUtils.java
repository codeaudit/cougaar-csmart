/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.core.db;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.io.IOException;
import org.cougaar.util.Parameters;
import org.cougaar.util.DBProperties;
import org.cougaar.util.DBConnectionPool;

/**
 * Static methods for accessing the CSMART configuration database
 * and manipulating the results.
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 */
public class DBUtils {

  // The following three variables are all implied by QUERY_FILE and not be needed
  public static final String DATABASE = "org.cougaar.configuration.database";
  public static final String USER = "org.cougaar.configuration.user";
  public static final String PASSWORD = "org.cougaar.configuration.password";

  public static final String QUERY_FILE = "CSMART.q";
  public static final String ASSEMBLYID_QUERY = "queryAssemblyID";

  public DBUtils() {
  }


  /**
   * Determines if a valid RC file exists.  It
   * first checks for <code>$HOME/.cougaarrc</code>
   * it then searches the config path for <code>cougaar.rc</code>.
   * Once a file is found, it is parsed to ensure that
   * it is a valid file.  
   * A valid file requires the following parameters in the rc file:
   * <ul>
   *   <li>org.cougaar.configuration.database</li>
   *   <li>org.cougaar.configuration.user</li>
   *   <li>org.cougaar.configuration.password<.li>
   * </ul>
   *
   * @return boolean indicating a valid rc file.
   */
  public static boolean isValidRCFile() {

    boolean valid = false;

    valid = (Parameters.findParameter(DBUtils.DATABASE) == null) ? false : true;
    valid &= (Parameters.findParameter(DBUtils.USER) == null) ? false : true;
    valid &= (Parameters.findParameter(DBUtils.PASSWORD) == null) ? false : true;
    return valid;
  }

  /**
   * Tests database connection to ensure it is valid.
   * This is done by running a simple query on the database.
   *
   * This test requires a <code>CSMART.q</code> and
   * the querySocieties Statment within that file.
   *
   * The Query run is:
   * <code> 
   *   SELECT ASSEMBLY_ID, ASSEMBLY_TYPE, DESCRIPTION
   *   FROM V3_ASB_ASSEMBLY WHERE ASSEMBLY_TYPE = 'CMT'
   * </code>
   * 
   * @return boolean indicating valid connection
   */
  public static boolean isValidDBConnection() {
    DBProperties dbProps;
    String database;
    String username;
    String password;

    boolean valid = false;
    Map substitutions = new HashMap();

    if(isValidRCFile()) {
      try {
 	dbProps = DBProperties.readQueryFile(QUERY_FILE);
	substitutions.put(":assembly_type", "CMT");
	try {
	  String dbtype = dbProps.getDBType();
	  String driverParam = "driver." + dbtype;
	  String driverClass = Parameters.findParameter(driverParam);
	  if (driverClass != null) {

	    Class.forName(driverClass);
	    Connection conn = DBUtils.getConnection();
	    try {
	      Statement stmt = conn.createStatement();
	      String query = dbProps.getQuery(ASSEMBLYID_QUERY, substitutions);
	      ResultSet rs = stmt.executeQuery(query);
	      String assemblyId;
	      String description;
	      if (rs.next()) {
		valid = true;
	      }
	      rs.close();
	      stmt.close();
	    } finally {
	      conn.close();
	    }
	  }
	} catch (ClassNotFoundException e) {
          System.err.println("Class not found: " + e);
	} catch (SQLException se) {
          System.err.println("Sql exception: " + se);
        }
      } catch(IOException e) {
        System.err.println("IO exception: " + e);
      }
    }
    return valid;
  }

  /**
   * Gets a connection from the Connection Pool.
   * Connection is established to the database
   * defined in the <code>CSMART.q</code> file.
   *
   * @return valid db connection
   */
  public static Connection getConnection() throws SQLException {
    return getConnection(QUERY_FILE);
  }

  /**
   * Gets a connection from the Connection Pool.
   * Connection is established to the database
   * defined in the <code>CSMART.q</code> file.
   *
   * @param queryFile to get the database connection information from
   * @return valid db connection
   */
  public static Connection getConnection(String queryFile) throws SQLException {
    DBProperties dbProps;
    String database;
    String username;
    String password;
    Connection conn = null;

    if(isValidRCFile()) {
      try {	
	dbProps = DBProperties.readQueryFile(queryFile);
	database = dbProps.getProperty("database");
	username = dbProps.getProperty("username");
	password = dbProps.getProperty("password");
//  	System.out.println("getConnection: DATABASE="+DATABASE+", QUERY_FILE="+ QUERY_FILE);
//  	System.out.println("getConnection: database="+database+", username="+ username);
	String dbtype = dbProps.getDBType();
	String driverParam = "driver." + dbtype;
	String driverClass = Parameters.findParameter(driverParam);
	if(driverClass == null) 
	  throw new SQLException("Unknown driver " + driverParam);
	Class.forName(driverClass);
	conn = DBConnectionPool.getConnection(database, username, password);
      } catch(IOException e) {
	throw new SQLException(e.toString());
      } catch(ClassNotFoundException ce) {
	throw new SQLException(ce.toString());
      }
    } else {
      throw new SQLException("Invalid cougaar rc file");
    }
    return conn;
  }

  /**
   * Retrive the named query from the default query file
   *
   * @param query a <code>String</code> query name to retrieve
   * @param substitutions a <code>Map</code> of substitutions to make in the query
   * @return a <code>String</code> for the resultant complete query
   */
  public static String getQuery(String query, Map substitutions) {
    return DBUtils.getQuery(query, substitutions, QUERY_FILE);
  }

  /**
   * Retrive the named query from the named query file
   *
   * @param query a <code>String</code> query name to retrieve
   * @param substitutions a <code>Map</code> of substitutions to make in the query
   * @param qFile a <code>String</code> query file name to look in
   * @return a <code>String</code> for the resultant complete query
   */
  public static String getQuery(String query, Map substitutions, String qFile) {
    DBProperties dbProps;
    String result = null;

    if(isValidRCFile()) {
      try {
	dbProps = DBProperties.readQueryFile(qFile);
	result = dbProps.getQuery(query, substitutions);
      } catch(IOException e) {}      
    }
      
    return result;
  }

  public static final boolean isMySQL() {
    DBProperties dbProps;

    if(isValidRCFile()) {
      try {	
	dbProps = DBProperties.readQueryFile(QUERY_FILE);
	String dbtype = dbProps.getDBType();
	return (dbtype != null && dbtype.equals("mysql"));
      } catch(IOException e) {}      
    }
      
    return false;
  }

  /**
   * Global flag indicating if CSMART is working with a valid
   * CSMART configuration database.
   */
  public static boolean dbMode = DBUtils.isValidDBConnection();

  /**
   * Build up a string for substituting in queries
   * to match a list of items. IE given a list of Assembly IDs, this
   * constructs the string <code>in ('id1','id2')</code> or <code>in ('id1')</code> or <code>is null</code>.<br>
   * Optionally ignore items in the list that start with the given pattern.
   *
   * @param listItems a <code>List</code> of items to match
   * @param badStartPattern a <code>String</code> start to entries to ignore. IE ignore entries that start with 'CMT'
   * @return a <code>String</code> to substitue in a query
   */
  public static String getListMatch(List listItems, String badStartPattern) {
    StringBuffer assemblyMatch = null;
    
    if (listItems != null && listItems.size() != 0) {
      assemblyMatch = new StringBuffer();
      assemblyMatch.append("in (");
      Iterator iter = listItems.iterator();
      boolean first = true;
      while (iter.hasNext()) {
	String val = (String)iter.next();
	// Ignore entries that start with the given pattern
	if (badStartPattern != null && ! val.startsWith(badStartPattern))
	  continue;
	if (first) {
	  first = false;
	} else {
	  assemblyMatch.append(", ");
	}
	assemblyMatch.append("'");
	assemblyMatch.append(val);
	assemblyMatch.append("'");
      }
      assemblyMatch.append(")");
      return assemblyMatch.toString();
    } else {
      return "is null";
    }
  }

  /**
   * Build up a string for substituting in queries
   * to match a list of items. IE given a list of Assembly IDs, this
   * constructs the string <code>in ('id1','id2')</code> or <code>in ('id1')</code> or <code>is null</code>
   *
   * @param listItems a <code>List</code> of items to match
   * @return a <code>String</code> to substitue in a query
   */
  public static String getListMatch(List listItems) {
    return DBUtils.getListMatch(listItems, null);
  }
  
  public static String getAssemblyMatch(ArrayList assemblyIDs) {
    return DBUtils.getListMatch(assemblyIDs);
  }

  /**
   * Ensure the returned column in the result is not null. Throw a RuntimeException if it is null.
   *
   * @param rs a <code>ResultSet</code> to get the data from
   * @param ix an <code>int</code> column index to retrieve from
   * @param query a <code>String</code> for the query that was executed
   * @return a <code>String</code> result string, never null
   * @exception SQLException if an error occurs
   */
  public static String getNonNullString(ResultSet rs, int ix, String query)
    throws SQLException
  {
    String result = rs.getString(ix);
    if (result == null)
      throw new RuntimeException("Null in DB ix=" + ix + " query=" + query);
    return result;
  }

}


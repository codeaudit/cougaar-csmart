/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.StringTokenizer;
import java.util.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import org.cougaar.util.Parameters;
import org.cougaar.util.DBProperties;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

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
  private static final String EXPERIMENT_QUERY = "queryExptsWithRecipe";
  private static final String EXPERIMENT_SOCIETY_QUERY = "queryExptsWithSociety";
  private static final String PLUGIN_QUERY = "queryGetPluginClasses";
  private static final String BINDER_QUERY = "queryGetBinderClasses";

  // Flag whether to actually go to DB on queries
  public static boolean execute = true;
  public static boolean traceQueries = false; // set to true for more debugging
  
  public DBUtils() {
  }

  /**
   * Set whether to output debugging output on queries
   *
   * @param b a <code>Boolean</code> 
   */
  public static void setTraceQueries(Boolean b){
    traceQueries = b.booleanValue();
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
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
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
	} catch (Exception e) {
          // Should just do nothing here and assume file mode.
          if(log.isInfoEnabled()) {
            log.info("Class not found", e);
          }
        }
      } catch(IOException e) {
          // Should just do nothing here and assume file mode.
        if(log.isInfoEnabled()) {
          log.info("IO Exception", e);
        }
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
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");

    if(isValidRCFile()) {
      try {	
	dbProps = DBProperties.readQueryFile(queryFile);
	database = dbProps.getProperty("database");
	username = dbProps.getProperty("username");
	password = dbProps.getProperty("password");
        if(log.isDebugEnabled()) {
          log.debug("DBUtils.getConnection(): \n DATABASE=" + DATABASE + 
                    "\n QUERY_FILE=" + QUERY_FILE + 
                    "\n database=" + database + 
                    "\n username=" + username);
        }
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
  
  public static String getSocietyName(String assemblyId) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    Map substitutions = new HashMap();
    String result = null;
    substitutions.put(":assembly_id:", assemblyId);

    if( !substitutions.isEmpty() ) {
      String query = null;
      Connection conn = null;
      try {
	conn = getConnection();
        Statement stmt = conn.createStatement();
        query = getQuery("querySocietyName", substitutions);
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          result = rs.getString(1);
        }
        
        rs.close();
        stmt.close();
      } catch (SQLException se) {
        if(log.isErrorEnabled()) {
          log.error("Caught SQL exception getting Society Name " + query, se);
        }
      } finally {
	try {
	  if (conn != null)
	    conn.close();
	} catch (SQLException e) {}
      }
    }

    return result;
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

  /**
   * Does the current cougaarrc file indicate a MySQL database
   *
   * @return a <code>boolean</code>, true if the CSMART DB is MySQL
   */
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
	if (val == null || val.equals(""))
	  continue;
	if (badStartPattern != null && val.startsWith(badStartPattern))
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
      if (first) {
	return "is null";
      } else {
	return assemblyMatch.toString();
      }
    } else {
      return "is null";
    }
  }

  public static String getListMatch(String oneitem) {
    List ol = new ArrayList(1);
    ol.add(oneitem);
    return DBUtils.getListMatch(ol, null);
  }

  public static String getListMatch(String oneitem, String badStartPattern) {
    List ol = new ArrayList(1);
    ol.add(oneitem);
    return DBUtils.getListMatch(ol, badStartPattern);
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
  
  public static String getAssemblyMatch(List assemblyIDs) {
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

  // Stuff from CMT.java
  /**
   * Describe <code>makeDeleteQuery</code> method here.
   *
   * @param table a <code>String</code> value
   * @param column a <code>String</code> value
   * @param val a <code>String</code> value
   * @return a <code>String</code> value
   */
  static String makeDeleteQuery(String table, String column, String val) {
    return "delete from "+ table +" where "+column+"="+val;
  }
  
  /**
   * Do a bunch of updates, using the values in the given set for the given
   * substitution in the given operation 
   *
   * @param op a <code>String</code> update to perform
   * @param subs a <code>String</code> substitution name
   * @param items a <code>Set</code> of values for the above substitution
   * @param qFile a <code>String</code> query file name
   */
  static void iterateUpdate(String op, String subs, Set items, String qFile){
    Iterator i = items.iterator();
    Map m = new HashMap();
    while (i.hasNext()) {
      dbUpdate(op,addSubs(m,subs,(String)i.next()), qFile);
    }
  }
  
  /**
   * Execute a list of queries (updates, etc).
   *
   * @param queries an <code>ArrayList</code> of statements to execute
   * @param qFile a <code>String</code> query file name
   */
  static void executeQueries(ArrayList queries, String qFile) {
    if (!execute)
      return;
    String dbQuery = "";
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    Logger qLog = CSMART.createLogger("queries");

    try {
      Connection conn = DBUtils.getConnection(qFile);
      try {
	Statement stmt = conn.createStatement();	
	for (int i = 0; i < queries.size(); i++) {
	  dbQuery = (String)queries.get(i);
          if(qLog.isDebugEnabled()) {
            qLog.debug("Query: " + dbQuery);
          }
	  int count = stmt.executeUpdate(dbQuery);
          if(log.isDebugEnabled()) {
            log.debug("Deleted "+count+" items from the database");
          }
	}
	stmt.close();
      } finally {
	conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("dbDelete: "+dbQuery, e);
      }
      throw new RuntimeException("Error" + e);
    }
  } 

  public static boolean isSocietyNameInDatabase(String societyName) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    Logger qLog = CSMART.createLogger("queries");
    Map substitutions = new HashMap();
    String result = null;
    String query = "";

    substitutions.put(":society_name:", societyName);

    try {
      Connection conn = DBUtils.getConnection();
      try {
	Statement stmt = conn.createStatement();	
        query = getQuery("querySocietyByName", substitutions);
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          result = rs.getString(1);
          if(result != null) { break; }
        }
        rs.close();
	stmt.close();
      } finally {
	conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("isSocietyNameInDatabase: "+query, e);
      }
      throw new RuntimeException("Error" + e);
    }

    return (result != null) ? true : false;
  }

  public static boolean isRecipeNameInDatabase(String recipeName) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    Logger qLog = CSMART.createLogger("queries");
    Map substitutions = new HashMap();
    String result = null;
    String query = "";
    substitutions.put(":recipe_name:", recipeName);

    try {
      Connection conn = DBUtils.getConnection();
      try {
	Statement stmt = conn.createStatement();	
        query = getQuery("queryRecipeByName", substitutions);
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          result = rs.getString(1);
          if(result != null) { break; }
        }
        rs.close();
	stmt.close();
      } finally {
	conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("isRecipeNameInDatabase: "+query, e);
      }
      throw new RuntimeException("Error" + e);
    }

    return (result != null) ? true : false;
  }
  


  /**
   * Determines if an agent has asset data.
   *
   * @return a <code>boolean</code> value
   */
  public static boolean agentHasAssetData(String name, String assemblyId) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    Logger qLog = CSMART.createLogger("queries");
    Map substitutions = new HashMap();
    String result = null;
    String query = "";
    substitutions.put(":agent:", name);
    substitutions.put(":assembly_id", assemblyId);

    try {
      Connection conn = DBUtils.getConnection();
      try {
	Statement stmt = conn.createStatement();	
        query = getQuery("queryAgentAssetData", substitutions);
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          result = rs.getString(1);
          if(result != null) { break; }
        }
        rs.close();
	stmt.close();
      } finally {
	conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("agentHasAssetData: "+query, e);
      }
      throw new RuntimeException("Error" + e);
    }

    return (result != null) ? true : false;
  }

  /**
   * Delete rows from the given table where the given column has the given value
   *
   * @param table a <code>String</code> table name to delete from
   * @param column a <code>String</code> column name to match on
   * @param val a <code>String</code> value to look for in that column
   * @param qFile a <code>String</code> query file name
   * @return an <code>int</code> number of rows removed
   */
  static int deleteItems(String table, String column, String val, String qFile){
    String dbQuery = "delete from "+ table +" where "+column+"="+val;
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    Logger qLog = CSMART.createLogger("queries");

    int count=0;
    try {
      Connection conn = DBUtils.getConnection(qFile);
      try {
        if(execute){
          Statement stmt = conn.createStatement();	
          if(qLog.isDebugEnabled()) {
            qLog.debug("Query: " + dbQuery);
          }
          count = stmt.executeUpdate(dbQuery);
          if(log.isInfoEnabled()) {
            log.info("Deleted "+count+" items from the database");
          }
          stmt.close();
        }
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("dbDelete: "+dbQuery, e);
      }
      throw new RuntimeException("Error" + e);
    }
    return count;
  }
  
  /**
   * Execute a query that returns multiple rows of 1 String each
   *
   * @param query a <code>String</code> query to execute
   * @param substitutions a <code>Map</code> of replacements
   * @param qFile a <code>String</code> query file name
   * @return a <code>Set</code> of the results
   */
  static Set querySet(String query, Map substitutions, String qFile){
    String dbQuery = DBUtils.getQuery(query, substitutions, qFile);
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    Set s = new HashSet();
    try {
      Connection conn = DBUtils.getConnection(qFile);
      try {
	Statement stmt = conn.createStatement();	
	ResultSet rs = stmt.executeQuery(dbQuery);
	while (rs.next()) {s.add(rs.getString(1));}
	rs.close();
	stmt.close();
      } finally {
	conn.close();
      }
      
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("querySet: "+dbQuery, e);
      }
      throw new RuntimeException("Error" + e);
    }
    return s;
  }
  
  /**
   * Execute a query whose return is a set of rows of 2 values, first
   * value is a key, second is a value, and these are put into
   * a Map and returned.
   *
   * @param query a <code>String</code> query to execute
   * @param substitutions a <code>Map</code> of replacements to do
   * @param qFile a <code>String</code> query file name with DB info
   * @return a <code>SortedMap</code> of the results
   */
  static SortedMap queryHT(String query, Map substitutions, String qFile){
    String dbQuery = DBUtils.getQuery(query, substitutions, qFile);
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    Connection conn = null;
    Map ht = new TreeMap();
    try {
      conn = DBUtils.getConnection(qFile);
      if (conn == null) {
        if(log.isInfoEnabled()) {
          log.info("DBUtils:queryHT: Got no connection");
        }
	return (SortedMap)ht;
      }
      try {
	Statement stmt = conn.createStatement();	
	ResultSet rs = stmt.executeQuery(dbQuery);
	while (rs.next()) {
	  // Null first string, (like expt name), kills stuff
	  if (rs.getString(1) != null)
	    ht.put(rs.getString(1),rs.getString(2));
	}
	rs.close();
	stmt.close();
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("queryHT: "+dbQuery, e);
      }
      throw new RuntimeException("Error" + e);
    }

    return (SortedMap)ht;
  }
  
  /**
   * Execute the given query which returns one String
   *
   * @param query a <code>String</code> query that returns one String
   * @param substitutions a <code>Map</code> of substitutions to make in the query
   * @param qFile a <code>String</code> query file name with the DB info
   * @return a <code>String</code> return value from the query
   */
  static String query1String(String query, Map substitutions, String qFile){
    String dbQuery = DBUtils.getQuery(query, substitutions, qFile);
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");

    String res = null;
    try {
      Connection conn = DBUtils.getConnection(qFile);
      try {
	Statement stmt = conn.createStatement();	
	ResultSet rs = stmt.executeQuery(dbQuery);
	if (rs.next()) {
	  
	  res=rs.getString(1);
	}
	rs.close();
	stmt.close();
      } finally {
	conn.close();
      }
      
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("query1String: "+dbQuery, e);
      }
      throw new RuntimeException("Error" + e);
    }
    return res;
  }
  
  /**
   * Execute the given query which returns one Integer
   *
   * @param query a <code>String</code> query that returns one Integer
   * @param substitutions a <code>Map</code> of substitutions to make in the query
   * @param qFile a <code>String</code> query file name with the DB info
   * @return an <code>Integer</code> result of the query
   */
  static Integer query1Int(String query, Map substitutions, String qFile){
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    String dbQuery = DBUtils.getQuery(query, substitutions, qFile);
    Integer res = null;
    try {
      Connection conn = DBUtils.getConnection(qFile);
      try {
	Statement stmt = conn.createStatement();	
	ResultSet rs = stmt.executeQuery(dbQuery);
	if (rs.next()) {
	  res=new Integer(rs.getInt(1));
	}
	rs.close();
	stmt.close();
      } finally {
	conn.close();
      }
      
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
      throw new RuntimeException("Error" + e);
    }
    return res;
  }
  
  /**
   * Add the given subsititution to the given set of subsitutions.
   * Useful when nesting the substitution addition in a larger command,
   * because the return is the updated substitution map.
   *
   * @param subs a <code>Map</code> of substitutions
   * @param subName a <code>String</code> subsitution key in query files
   * @param subItem an <code>Object</code> value to substitute for the key
   * @return a <code>Map</code> of substitutions, updated
   */
  static Map addSubs(Map subs, String subName, Object subItem){
    subs.put(subName,subItem);
    return subs;
  }
  
  /**
   * Execute a set of queries whose name starts with the given base.<br>
   * Used, for example, where MySQL takes multiple queries to do what
   * Oracle does in one.
   *
   * @param queryBase a <code>String</code> query name root
   * @param substitutions a <code>Map</code> of subsitutions to make in the query
   * @param qFile a <code>String</code> query file name
   */
  static void executeQuerySet(String queryBase, Map substitutions, String qFile){
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    if (traceQueries){
      System.out.println("\nexecuteQuerySet "+queryBase);
    }
    String qs = null;
    try{
      qs = DBUtils.getQuery(queryBase+"Queries", substitutions, qFile);
    } catch (IllegalArgumentException e)
      {
	// this is expected if there is no "Queries" item
      }
    String dbQuery=null;
    if (qs==null){
      dbQuery = DBUtils.getQuery(queryBase, substitutions, qFile);
      if (dbQuery != null) {
	dbExecute(dbQuery, "Execute", qFile);
      } else {
	System.out.println("Can't find query - " + queryBase);
      }
    } else {
      StringTokenizer queries = new StringTokenizer(qs);
      Connection conn = null;
      try {
	conn = DBUtils.getConnection(qFile);
	while (queries.hasMoreTokens()) {
	  String queryName = queries.nextToken();
	  dbQuery = DBUtils.getQuery(queryName, substitutions, qFile);
	  if (dbQuery != null) {
	    dbExecute(dbQuery, "Execute", qFile, conn);
	  } else {
	    System.out.println("Can't find query - " + queryBase);
	  }
	}
      } catch (SQLException e) {
	// Got an error opening the exception
	if (log.isErrorEnabled()) {
	  log.error("dbExecuteQuerySet error opening connection to file " + qFile, e);
	}
      } finally {
	if (conn != null) {
	  try {
	    conn.close();
	  } catch (SQLException e) {
	  }
	}
      }
    }
  }

  /**
   * Execute the given update, optionally timing it. Use the given connection,
   * leaving it open for the caller to close.
   *
   * @param dbQuery a <code>String</code> query to execute (update/delete/insert...)
   * @param type a <code>String</code> type of update
   * @param qFile a <code>String</code> query file name, indicating the DB to use
   * @param conn a <code>Connection</code> to the DB to use, to be left open
   * @return an <code>int</code>, the number of lines affected
   */
  public static int dbExecute(String dbQuery, String type, String qFile, Connection conn) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");

    int count=0;
    boolean gotConn = false;

    try {
      if (conn != null && ! conn.isClosed())
	gotConn = true;
      else
	conn = DBUtils.getConnection(qFile);
      if (traceQueries){
	System.out.println("\nStarting dbExecute at "+new Date()+"\n\n"+dbQuery);
      }
      try {
        if (execute){
          Statement stmt = conn.createStatement();	
          count = stmt.executeUpdate(dbQuery);
          //  		    System.out.println("db"+type+" "+type+"d "+count+" items in the database"); 
          stmt.close();
        }
      } finally {
	if (! gotConn)
	  conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("db"+type+": "+dbQuery, e);
      }
      throw new RuntimeException("Error" + e);
    }

    if (traceQueries){
      System.out.println("\nexiting dbExecute with "+count+" at "+new Date());
    }
    return count;
  }
  
  
  /**
   * Execute the given update, optionally timing it
   *
   * @param dbQuery a <code>String</code> query to execute (update/delete/insert...)
   * @param type a <code>String</code> type of update
   * @param qFile a <code>String</code> query file name, indicating the DB to use
   * @return an <code>int</code>, the number of lines affected
   */
  public static int dbExecute(String dbQuery, String type, String qFile){
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");

    int count=0;
    try {
      Connection conn = DBUtils.getConnection(qFile);
      if (traceQueries){
	System.out.println("\nStarting dbExecute at "+new Date()+"\n\n"+dbQuery);
      }
      try {
        if (execute){
          Statement stmt = conn.createStatement();	
          count = stmt.executeUpdate(dbQuery);
          //  		    System.out.println("db"+type+" "+type+"d "+count+" items in the database"); 
          stmt.close();
        }
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("db"+type+": "+dbQuery, e);
      }
      throw new RuntimeException("Error" + e);
    }

    if (traceQueries){
      System.out.println("\nexiting dbExecute with "+count+" at "+new Date());
    }
    return count;
  }
  
  /**
   * Do a db Insertion
   *
   * @param query a <code>String</code> query to execute (insert)
   * @param substitutions a <code>Map</code> of substitutions to make in the query
   * @param qFile a <code>String</code> query file name, indicating the db to use
   * @return an <code>int</code>, the number of lines inserted
   */
  public static int dbInsert(String query, Map substitutions, String qFile){
    String dbQuery = DBUtils.getQuery(query, substitutions, qFile);
    return dbExecute(dbQuery, "Insert", qFile);
  }
  
  /**
   * Do a db Update
   *
   * @param query a <code>String</code> query (update) to execute
   * @param substitutions a <code>Map</code> of substitutions to make in the query
   * @param qFile a <code>String</code> query file name, indicating the db to use
   * @return an <code>int</code>, the number of lines updated
   */
  public static int dbUpdate(String query, Map substitutions, String qFile){
    String dbQuery = DBUtils.getQuery(query, substitutions, qFile);
    return dbExecute(dbQuery, "Update", qFile);
  }
  
  /**
   * Do a delete from the database
   *
   * @param query a <code>String</code> query (deletion) to execute
   * @param substitutions a <code>Map</code> of substitutions to make in the query
   * @param qFile a <code>String</code> query file name, indicating the db to use
   * @return an <code>int</code>, the number of rows deleted
   */
  public static int dbDelete(String query, Map substitutions, String qFile){
    String dbQuery = DBUtils.getQuery(query, substitutions, qFile);
    return dbExecute(dbQuery, "Delete", qFile);
  }
  
  /**
   * Quote any quote marks in the string for use with SQL
   *
   * @param s a <code>String</code> to include in a query
   * @return a <code>String</code> with single-quotes escaped
   */
  public static String sqlQuote(String s) {
    if (s == null) return "null";
    int quoteIndex = s.indexOf('\'');
    while (quoteIndex >= 0) {
      s = s.substring(0, quoteIndex) + "''" + s.substring(quoteIndex + 1);
      quoteIndex = s.indexOf('\'', quoteIndex + 2);
    }
    return "'" + s + "'";
  }  

  /**
   * Get a set of experiment names that say they have the 
   * named recipe.
   *
   * @param recipeName a <code>String</code> recipe name to look for
   * @return a <code>Set</code> of experiment names that include that recipe
   */
  public static Set dbGetExperimentsWithRecipe(String recipeName) {
    Map substitutions = new HashMap();
    substitutions.put(":recipeName", recipeName);
    String dbQuery = DBUtils.getQuery(EXPERIMENT_QUERY, substitutions);
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    Set s = new HashSet();
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Statement stmt = conn.createStatement();	
        ResultSet rs = stmt.executeQuery(dbQuery);
        while (rs.next()) {s.add(rs.getString(1));}
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("querySet: "+dbQuery, e);
      }
      throw new RuntimeException("Error" + e);
    } 
    return s;
  }

  /**
   * Get a set of experiment names that say they have the 
   * named society.
   *
   * @param societyName a <code>String</code> society name to look for
   * @return a <code>Set</code> of experiment names that include that society
   */
  public static Set dbGetExperimentsWithSociety(String societyName) {
    Map substitutions = new HashMap();
    substitutions.put(":societyName", societyName);
    String dbQuery = DBUtils.getQuery(EXPERIMENT_QUERY, substitutions);
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    Set s = new HashSet();
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Statement stmt = conn.createStatement();	
        ResultSet rs = stmt.executeQuery(dbQuery);
        while (rs.next()) {s.add(rs.getString(1));}
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("querySet: "+dbQuery, e);
      }
      throw new RuntimeException("Error" + e);
    }
    return s;
  }

  /**
   * Does the given experiment have a CMT assembly
   * as part of its configuration definition. Used to decide
   * whether to allow the user to modify the selection when loading
   * from the database, and when deciding whether to display the 
   * Threads pane in the Experiment builder.
   *
   * @param experimentId a <code>String</code> experiment ID in the database
   * @return a <code>boolean</code>, true if the experiment uses a CMT society
   */
  public static final boolean containsCMTAssembly(String experimentId) {
    if (experimentId == null || experimentId.equals(""))
      return false;
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    boolean result = false;
    Map substitutions = new HashMap();
    String query = "";
    substitutions.put(":expt_id:", experimentId);
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Statement stmt = conn.createStatement();	
        query = getQuery("queryCMTAssembly", substitutions);
        ResultSet rs = stmt.executeQuery(query);
        if(rs.next())
          result = true;
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("querySet: "+query, e);
      }
      throw new RuntimeException("Error" + e);
    }
    return result;
  }

  /**
   * Get a set of plugin class names.
   *
   * @return a <code>Set</code> of all plugin class names in the database
   */
  public static Set dbGetPluginClasses() {
    String dbQuery = DBUtils.getQuery(PLUGIN_QUERY, new HashMap());
    return dbGetPluginOrBinderClasses(dbQuery);
  }

  /**
   * Get a set of binder class names.
   *
   * @return a <code>Set</code> of all binder class names in the database
   */
  public static Set dbGetBinderClasses() {
    String dbQuery = DBUtils.getQuery(BINDER_QUERY, new HashMap());
    return dbGetPluginOrBinderClasses(dbQuery);
  }

  private static Set dbGetPluginOrBinderClasses(String dbQuery) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.DBUtils");
    Set s = new HashSet();
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Statement stmt = conn.createStatement();	
        ResultSet rs = stmt.executeQuery(dbQuery);
        while (rs.next()) {s.add(rs.getString(1));}
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("query: "+dbQuery, e);
      }
      throw new RuntimeException("Error" + e);
    }
    return s;
  }

} // end of DBUtils.java

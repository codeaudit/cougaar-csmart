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

package org.cougaar.tools.csmart.societies.database;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.io.IOException;
import org.cougaar.util.Parameters;
import org.cougaar.util.DBProperties;
import org.cougaar.util.DBConnectionPool;

public class DBUtils {

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
 	dbProps = DBProperties.readQueryFile(DATABASE, QUERY_FILE);
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
    DBProperties dbProps;
    String database;
    String username;
    String password;
    Connection conn = null;

    if(isValidRCFile()) {
      try {	
	dbProps = DBProperties.readQueryFile(DATABASE, QUERY_FILE);
	database = dbProps.getProperty("database");
	username = dbProps.getProperty("username");
	password = dbProps.getProperty("password");
	System.out.println("getConnection: DATABASE="+DATABASE+", QUERY_FILE="+ QUERY_FILE);
	System.out.println("getConnection: database="+database+", username="+ username);
	String dbtype = dbProps.getDBType();
	String driverParam = "driver." + dbtype;
	String driverClass = Parameters.findParameter(driverParam);
	if(driverClass == null) 
	  throw new SQLException("Unknown driver " + driverParam);
	Class.forName(driverClass);
	conn = DBConnectionPool.getConnection(database, username, password);
      } catch(IOException e) {
	// Need to log something here.
      } catch(ClassNotFoundException ce) {
	// Need to log something here.
      }
    }    

    return conn;
  }

  public static String getQuery(String query, Map substitutions) {
    DBProperties dbProps;
    String result = null;

    if(isValidRCFile()) {
      try {
	dbProps = DBProperties.readQueryFile(DATABASE, QUERY_FILE);
	result = dbProps.getQuery(query, substitutions);
      } catch(IOException e) {}      
    }
      
    return result;
  }

  public static String getAssemblyMatch(ArrayList assemblyIDs) {
    StringBuffer assemblyMatch = new StringBuffer();
    assemblyMatch.append("in (");
    Iterator iter = assemblyIDs.iterator();
    boolean first = true;
    while (iter.hasNext()) {
      String val = (String)iter.next();
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
  }

}

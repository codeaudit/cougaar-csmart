/* 
 * <copyright>
 * Copyright 2002 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).

 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.tools.csmart.util;

import org.cougaar.bootstrap.Bootstrapper;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.DBProperties;
import org.cougaar.util.Parameters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for use by csmart/bin/exportExperiment script
 * to ensure that ALIB IDs are correct for complex recipes.
 * invoke with name of experiment being exported.
 * DB tempcopy must exist.
 * Uses CSMART.q & cougaar.rc to get DB connection info.
 **/
public class ExportExperimentHelper {

  // get the alib_ids that need to be updated - one per
  // complex recipe
  private static String getAlibQuery = "SELECT DISTINCT " + 
    "AA.COMPONENT_ALIB_ID " + 
"   FROM " +
"     alib_component AA, " + 
"     asb_component_hierarchy AH, " + 
"     expt_trial_mod_recipe EA, " + 
"     lib_mod_recipe_arg MA, " +
"     expt_trial ET, " + 
"     expt_experiment E " + 
"   WHERE " +
"     E.NAME = ':exptName' " +
"     AND E.EXPT_ID = ET.EXPT_ID " + 
"     AND ET.TRIAL_ID = EA.TRIAL_ID " + 
"     AND EA.MOD_RECIPE_LIB_ID = MA.MOD_RECIPE_LIB_ID " + 
"     AND MA.ARG_NAME = 'Assembly Id' " + 
"     AND MA.ARG_VALUE = AH.ASSEMBLY_ID " + 
"     AND (AH.COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID OR AH.PARENT_COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID) " + 
"     AND AA.COMPONENT_TYPE = 'recipe'";

  // Queries to update the alib_ids to match the exported
  // ID names
  private static String[] updateAlibQueries = {
"UPDATE tempcopy.alib_component SET COMPONENT_ALIB_ID = CONCAT(':old_alib_id', '-cpy') WHERE COMPONENT_ALIB_ID = ':old_alib_id'",
"UPDATE tempcopy.alib_component SET COMPONENT_NAME = CONCAT(':old_alib_id', '-cpy') WHERE COMPONENT_NAME = ':old_alib_id'",
"UPDATE tempcopy.asb_component_arg SET COMPONENT_ALIB_ID = CONCAT(':old_alib_id', '-cpy') WHERE COMPONENT_ALIB_ID = ':old_alib_id'",
"UPDATE tempcopy.asb_component_hierarchy SET COMPONENT_ALIB_ID = CONCAT(':old_alib_id', '-cpy') WHERE COMPONENT_ALIB_ID = ':old_alib_id'",
"UPDATE tempcopy.asb_component_hierarchy SET PARENT_COMPONENT_ALIB_ID = CONCAT(':old_alib_id', '-cpy') WHERE PARENT_COMPONENT_ALIB_ID = ':old_alib_id'"
};

  public static void main(String[] args) {
    if ("true".equals(System.getProperty("org.cougaar.useBootstrapper", "true"))) {
      Bootstrapper.launch(ExportExperimentHelper.class.getName(), args);
    } else {
      launch(args);
    }
  }

  public static void launch (String[] args) {
    boolean debug = false;
    if (args.length < 1) {
      System.out.println("ExportExperimentHelper error. Expected first argument of a valid experiment name, second optional argument to turn on debug messages.");
      System.exit(-1);
    } else if (args.length == 2)
      debug = true;

    String eName = args[0];

    if (debug)
      System.out.println("Using experiment name " + eName);

    // Get the corrected query for ALIB IDs, using the experiment name
    String getAQuery = ExportExperimentHelper.parseQuery(":exptName", eName, ExportExperimentHelper.getAlibQuery);
    if (getAQuery == null) {
      System.out.println("ExportExperimentHelper couldnt parse out query to get ALIB IDs, using experiment name " + eName + ". Check that you supplied a valid experiment name.");
      System.exit(-1);
    } else {
      if (debug)
	System.out.println("Parsed ALIB ID query = " + getAQuery);
    }

    // Now the real work
    Connection conn = null;
    try {
      conn = ExportExperimentHelper.getConnection();
      try {
	Statement stmt = conn.createStatement();
	ResultSet rs = stmt.executeQuery(getAQuery);
	String updQuery = null;
	Statement stmt2 = conn.createStatement();
	ResultSet rs2 = null;

	// For each ALIB ID that needs updating
	while (rs.next()) {
	  // For each update query
	  for (int i = 0; i < ExportExperimentHelper.updateAlibQueries.length; i++) {
	    // Get a corrected query
	    updQuery = ExportExperimentHelper.parseQuery(":old_alib_id", rs.getString(1), ExportExperimentHelper.updateAlibQueries[i]);
	    if (updQuery != null) {
	      if (debug)
		System.out.println("Doing update query = " + updQuery);
	      // Do the update
	      try {
		rs2 = stmt2.executeQuery(updQuery);
	      } finally {
		rs2.close();
	      }
	    }
	  } // loop over update queries
	} // loop over ALIB IDs to update
	rs.close();
	stmt2.close();
	stmt.close();
      } finally {
	conn.close();
      }
    } catch (SQLException sqe) {
    }
    
  } // done with launch

  /**
   * Cribbed from DBUtils - get a connection to the db
   * specified by CSMART.q, without using the logger.
   **/
  public static Connection getConnection() {
    DBProperties dbProps;
    String database;
    String username;
    String password;
    Connection conn = null;
    try {	
      dbProps = DBProperties.readQueryFile("CSMART.q");
      database = dbProps.getProperty("database");
      username = dbProps.getProperty("username");
      password = dbProps.getProperty("password");
      String dbtype = dbProps.getDBType();
      String driverParam = "driver." + dbtype;
      String driverClass = Parameters.findParameter(driverParam);
      if(driverClass == null) {
	System.err.println("ExportExperimentHelper found bad driver specified in cougaar.rc and CSMART.q. Check your instalation. Driver was " + driverParam);
	System.exit(-1);
      }
      Class.forName(driverClass);
	conn = DBConnectionPool.getConnection(database, username, password);
      } catch (IOException e) {
	e.printStackTrace();
	System.exit(-1);
      } catch (ClassNotFoundException ce) {
	ce.printStackTrace();
	System.exit(-1);
      } catch (SQLException sqe) {
	sqe.printStackTrace();
	System.exit(-1);
      }
    return conn;
  }

  /**
   * Replace all instances of key with subst in the given
   * String query, returning the result.
   **/
  public static String parseQuery(String key, String subst, String query) {
    StringBuffer tmp = new StringBuffer(query);
    if (subst == null || key == null || key.equals(""))
      return query;
    int ix = 0;
    while ((ix = tmp.indexOf(key, ix)) >= 0) {
      tmp.replace(ix, ix+key.length(), subst);
      ix = ix + key.length();
    }
    return tmp.substring(0);
  }
      
}

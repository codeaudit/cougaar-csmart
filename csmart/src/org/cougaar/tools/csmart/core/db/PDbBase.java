/* 
 * <copyright>
 * Copyright 2001-2002 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.core.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import org.cougaar.util.DBProperties;
import org.cougaar.util.Parameters;
import org.cougaar.util.DBConnectionPool;

import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.io.ObjectInputStream;
/**
 * This class takes a structure of ComponentData objects and populates
 * the configuration database with some or all of the components
 * described by the data. The selection of applicable components is
 * still an issue.
 * @property csmart.PopulateDb.log.enable if true enables the logging
 * of executed queries to a file named PopulateDb<datetime>.log.
 **/
public class PDbBase {
    public static final int RECIPE_STATUS_ABSENT = 0;
    public static final int RECIPE_STATUS_EXISTS = 1;
    public static final int RECIPE_STATUS_DIFFERS = 2;

    public static final String QUERY_FILE = "PopulateDb.q";

    private static final String PROP_LOG_QUERIES = "csmart.PopulateDb.log.enable";
    private static final String DFLT_LOG_QUERIES = "false";
    private static boolean logQueries =
        System.getProperty(PROP_LOG_QUERIES, DFLT_LOG_QUERIES).equals("true");

  private transient Logger log;

    protected Map substitutions = new HashMap() {
        public Object put(Object key, Object val) {
            if (val == null) throw new IllegalArgumentException("Null value for " + key);
            return super.put(key, val);
        }
    };
    protected DBProperties dbp;

    protected Connection dbConnection;
    protected Statement stmt;
    protected Statement updateStmt;
    protected boolean debug = false;
    protected PrintWriter pwlog;

    /**
     * Constructor
     **/
    public PDbBase()
        throws SQLException, IOException
    {

      createLogger();
        if (logQueries)
            pwlog = new PrintWriter(new FileWriter(getLogName()));
        dbp = DBProperties.readQueryFile(QUERY_FILE);
        try {
            dbp.addQueryFile(RecipeComponent.RECIPE_QUERY_FILE);
        } catch (FileNotFoundException e) {
            // This is normal if a user has no separate recipe query file.
        }
        //        dbp.setDebug(true);
        String database = dbp.getProperty("database");
        String username = dbp.getProperty("username");
        String password = dbp.getProperty("password");
        String dbtype = dbp.getDBType();
        String driverParam = "driver." + dbtype;
        String driverClass = Parameters.findParameter(driverParam);
        if (driverClass == null)
            throw new SQLException("Unknown driver " + driverParam);
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException cnfe) {
            throw new SQLException("Driver class not found: " + driverClass);
        }
        dbConnection = DBConnectionPool.getConnection(database, username, password);
        dbConnection.setAutoCommit(false);
        stmt = dbConnection.createStatement();
        updateStmt = dbConnection.createStatement();
    }

    DateFormat logDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private String getLogName() {
        return "PopulateDb" + logDateFormat.format(new Date()) + ".log";
    }

    /**
     * Check the status of a recipe in the database.
     * @param rc the RecipeComponent to check
     * @return RECIPE_STATUS_ABSENT -- Recipe not in database<br>
     * RECIPE_STATUS_EXISTS -- Recipe already in database with same
     * value<br> RECIPE_STATUS_DIFFERS -- Recipe already in database
     * with different value
     **/

  public int recipeExists(RecipeComponent rc) {
    String[] recipeIdAndClass = getRecipeIdAndClass(rc.getRecipeName());
    if (recipeIdAndClass == null)
      return RECIPE_STATUS_ABSENT;
    else {
      if (isRecipeEqual(recipeIdAndClass, rc))
        return RECIPE_STATUS_EXISTS;
      else
        return RECIPE_STATUS_DIFFERS;
    }
  }

  /**
   * Checks if the given recipe matches the recipe in the database
   * specified by the id and class.
   * @param String[] id and class from recipe in database
   * @param rc the recipe component to compare against
   * @return true if recipe class, properties, and values are the same
   **/

  private boolean isRecipeEqual(String[] recipeIdAndClass, 
                                RecipeComponent rc) {
    if (!recipeIdAndClass[1].equals(rc.getClass().getName()))
      return false; // different class

    Map newProps = new HashMap();
    for (Iterator j = rc.getPropertyNames(); j.hasNext(); ) {
      CompositeName pname = (CompositeName) j.next();
      Property prop = rc.getProperty(pname);
      Object val = prop.getValue();
      if (val == null) continue; // Don't write null values
      String sval = val.toString();
      if (sval.equals("")) continue; // Don't write empty values
      String name = pname.last().toString();
      newProps.put(name, sval);
    }

    Map oldProps = new HashMap();
    substitutions.put(":recipe_id:", recipeIdAndClass[0]);
    ResultSet rs = null;
    try {
      rs = executeQuery(stmt, 
                        dbp.getQuery("queryLibRecipeProps", substitutions));
      while (rs.next()) {
        oldProps.put(rs.getString(1), rs.getString(2));
      }
    } catch (SQLException sqle) {
      if(log.isErrorEnabled()) {
        log.error("SQL Exception: ", sqle);
      }
      return false;
    } finally {
      try {
        if (rs != null)
          rs.close();
      } catch (SQLException sqle2) {
        if(log.isErrorEnabled()) {
          log.error("SQL Exception: ", sqle2);
        }
        return false;
      }
    }
    return oldProps.equals(newProps);
  }

  /**
   * Insures that the given recipe is in the database.
   * If the recipe is not in the database, it writes it to the database
   * with a new id.  If the recipe is in the database, it updates
   * the database entries if necessary.
   * @param rc the recipe to save in the database
   * @return the id of the recipe in the database
   */

    public String insureLibRecipe(RecipeComponent rc) throws SQLException {
      String[] recipeIdAndClass = getRecipeIdAndClass(rc.getRecipeName());
      if (recipeIdAndClass != null) {
        if (isRecipeEqual(recipeIdAndClass, rc))
          return recipeIdAndClass[0];
        else
          return insertLibRecipe(rc, recipeIdAndClass[0]);
      } else
        return insertLibRecipe(rc, null);
    }

    public void removeLibRecipe(RecipeComponent rc) throws SQLException {
        String[] recipeIdAndClass = getRecipeIdAndClass(rc.getRecipeName());
        if (recipeIdAndClass != null) {
            substitutions.put(":recipe_id:", recipeIdAndClass[0]);
            executeUpdate(dbp.getQuery("deleteLibRecipeArgs", substitutions));
            executeUpdate(dbp.getQuery("deleteLibRecipe", substitutions));
        }
    }

    public void removeLibRecipeNamed(String recipeName) throws SQLException {
        String[] recipeIdAndClass = getRecipeIdAndClass(recipeName);
        if (recipeIdAndClass != null) {
            substitutions.put(":recipe_id:", recipeIdAndClass[0]);
            executeUpdate(dbp.getQuery("deleteLibRecipeArgs", substitutions));
            executeUpdate(dbp.getQuery("deleteLibRecipe", substitutions));
        }
    }

  /**
   * Inserts the specified recipe into the database.
   * If the given recipe id already exists in the database,
   * then just update the database recipe (by removing it
   * and inserting the new recipe using the same id).
   **/
    private String insertLibRecipe(RecipeComponent rc,
                                   String recipeId) throws SQLException {
      try {
        if (recipeId != null)
          removeLibRecipeNamed(rc.getRecipeName());
        else
          recipeId = getNextId("queryMaxRecipeId", "RECIPE-");
        substitutions.put(":recipe_id:", recipeId);
        substitutions.put(":java_class:", rc.getClass().getName());
        substitutions.put(":description:", "No description available");
        executeUpdate(dbp.getQuery("insertLibRecipe", substitutions));
        int order = 0;
        for (Iterator j = rc.getLocalPropertyNames(); j.hasNext(); ) {
            CompositeName pname = (CompositeName) j.next();
            Property prop = rc.getProperty(pname);
            Object val = prop.getValue();
            if (val == null) continue; // Don't write null values
            String sval = val.toString();
            if (sval.equals("")) continue; // Don't write empty values
            String name = pname.last().toString();
            substitutions.put(":arg_name:", name);
            substitutions.put(":arg_value:", sval);
            substitutions.put(":arg_order:", String.valueOf(order++));
            executeUpdate(dbp.getQuery("insertLibRecipeProp", substitutions));
        }
        return recipeId;
      } catch (SQLException sqle) {
        System.out.println("Exception in insertLibRecipe: " + sqle);
        Thread.dumpStack();
        return null;
      }
    }

    private String[] getRecipeIdAndClass(String recipeName) {
      substitutions.put(":recipe_name:", recipeName);
      ResultSet rs = null;
      try {
        rs = executeQuery(stmt, 
                          dbp.getQuery("queryLibRecipeByName", substitutions));
        if (rs.next()) {
          return new String[] {rs.getString(1), rs.getString(2)};
        } else {
          return null;
        }
      } catch (SQLException sqle) {
        if(log.isErrorEnabled()) {
          log.error("SQL Exception: ", sqle);
        }
        return null;
      } finally {
        try {
          if (rs != null)
            rs.close();
        } catch (SQLException sqle2) {
          if (log.isErrorEnabled()) {
            log.error("SQL Exception: ", sqle2);
          }
          return null;
        }
      }
    }

    protected String getNextId(String queryName, String prefix) {
        DecimalFormat format = new DecimalFormat("0000");
        format.setPositivePrefix(prefix);
        substitutions.put(":max_id_pattern:", prefix + "____");
        String id = format.format(1); // Default
        try {
            Statement stmt = dbConnection.createStatement();
            try {
                String query = dbp.getQuery(queryName, substitutions);
                ResultSet rs = executeQuery(stmt, query);
                try {
                    if (rs.next()) {
                        String maxId = rs.getString(1);
                        if (maxId != null) {
                            int n = format.parse(maxId).intValue();
                            id = format.format(n + 1);
                        }
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } catch (Exception e) {
          if(log.isErrorEnabled()) {
            log.error("Exception: ", e);
          }
            // Ignore exceptions and use default
        }
        return id;
    }

    /**
     * Utility method to perform an executeUpdate statement. Also
     * additional code to be added for each executeUpdate for
     * debugging purposes.
     **/
    protected int executeUpdate(String query) throws SQLException {
        if (query == null) throw new IllegalArgumentException("executeUpdate: null query");
        try {
            long startTime = 0;
            if (pwlog != null)
                startTime = System.currentTimeMillis();
            int result = updateStmt.executeUpdate(query);
            if (pwlog != null) {
                long endTime = System.currentTimeMillis();
                pwlog.println((endTime - startTime) + " " + query);
            }
            return result;
        } catch (SQLException sqle) {
          if(log.isErrorEnabled()) {
            log.error("SQLException query: " + query);
          }
          if (pwlog != null) {
            pwlog.println("SQLException query: " + query);
            pwlog.flush();
          }
          throw sqle;
        }
    }

    /**
     * Utility method to perform an executeUpdate statement. Also
     * additional code to be added for each executeUpdate for
     * debugging purposes.
     **/
    protected ResultSet executeQuery(PreparedStatement pstmt, String query) throws SQLException {
        if (query == null) throw new IllegalArgumentException("executeUpdate: null query");
        try {
            long startTime = 0;
            if (pwlog != null)
                startTime = System.currentTimeMillis();
            ResultSet result = pstmt.executeQuery();
            if (pwlog != null) {
                long endTime = System.currentTimeMillis();
                pwlog.println((endTime - startTime) + " " + query);
            }
            return result;
        } catch (SQLException sqle) {
          if(log.isErrorEnabled()) {
            log.error("SQLException query: " + query);
          }
            if (pwlog != null) {
                pwlog.println("SQLException query: " + query);
                pwlog.flush();
            }
            throw sqle;
        }
    }

    /**
     * Utility method to perform an executeUpdate statement. Also
     * additional code to be added for each executeUpdate for
     * debugging purposes.
     **/
    protected int executeUpdate(PreparedStatement pstmt, String query) throws SQLException {
        if (query == null) throw new IllegalArgumentException("executeUpdate: null query");
        try {
            long startTime = 0;
            if (pwlog != null)
                startTime = System.currentTimeMillis();
            int result = pstmt.executeUpdate();
            if (pwlog != null) {
                long endTime = System.currentTimeMillis();
                pwlog.println((endTime - startTime) + " " + query);
            }
            return result;
        } catch (SQLException sqle) {
          if(log.isErrorEnabled()) {
            log.error("SQLException query: " + query, sqle);
          }
            if (pwlog != null) {
                pwlog.println("SQLException query: " + query);
                pwlog.flush();
            }
            throw sqle;
        }
    }

    /**
     * Utility method to perform an executeQuery statement. Also
     * additional code to be added for each executeQuery for
     * debugging purposes.
     **/
    protected ResultSet executeQuery(Statement stmt, String query) throws SQLException {
        if (query == null) throw new IllegalArgumentException("executeQuery: null query");
        try {
            long startTime = 0;
            if (pwlog != null)
                startTime = System.currentTimeMillis();
            ResultSet rs = stmt.executeQuery(query);
            if (pwlog != null) {
                long endTime = System.currentTimeMillis();
                pwlog.println((endTime - startTime) + " " + query);
            }
            return rs;
        } catch (SQLException sqle) {
          if(log.isErrorEnabled()) {
            log.error("SQLException query: " + query, sqle);
          }
            if (pwlog != null) {
                pwlog.println("SQLException query: " + query);
                pwlog.flush();
            }
            throw sqle;
        }
    }

    /**
     * Enables debugging
     **/
    public void setDebug(boolean newDebug) {
        debug = newDebug;
        dbp.setDebug(newDebug);
    }

    /**
     * Indicates that this is no longer needed. Closes the database
     * connection. Well-behaved users of this class will close when
     * done. Otherwise, the finalizer will close it.
     **/
    public synchronized void close() throws SQLException {
        if (pwlog != null) {
            pwlog.flush();
            pwlog.close();
            pwlog = null;
        }
        if (dbConnection != null) {
            if (!dbConnection.getAutoCommit()) dbConnection.commit();
            dbConnection.close();
            dbConnection = null;
        }
    }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

    protected void finalize() {
        try {
            if (dbConnection != null) close();
        } catch (SQLException sqle) {
          if(log.isErrorEnabled()) {
            log.error("Exception", sqle);
          }
        }
    }

    /**
     * Quote a string for SQL. We don't double quotes that appear in
     * strings because we have no cases where such quotes occur.
     **/
    protected static String sqlQuote(String s) {
        if (s == null) return "null";
        int quoteIndex = s.indexOf('\'');
        while (quoteIndex >= 0) {
            s = s.substring(0, quoteIndex) + "''" + s.substring(quoteIndex + 1);
            quoteIndex = s.indexOf('\'', quoteIndex + 2);
        }
        return "'" + s + "'";
    }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

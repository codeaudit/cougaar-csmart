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

// methods for interfacing to database and supporting community displays

package org.cougaar.tools.csmart.ui.community;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import org.cougaar.tools.csmart.core.db.DBUtils;

public class CommunityDBUtils {

  // execute a query and return the results
  protected static ArrayList getQueryResults(String query) {
    ArrayList values = new ArrayList();
    Connection conn = null;
    try {
      conn = DBUtils.getConnection();
      if (conn == null) {
        System.err.println("Could not get connection to database");
        return null;
      }
      Statement statement = conn.createStatement();
      ResultSet resultSet = statement.executeQuery(query);
      while (resultSet.next()) {
        String s = resultSet.getString(1);
        if (!values.contains(s))
          values.add(s);
      }
      resultSet.close();
      statement.close();
    }
    catch (SQLException ex) {
      System.err.println(ex);
    } finally {
      try {
        if (conn != null)
          conn.close();
      } catch (SQLException e) {
      }
    }
    Collections.sort(values);
    return values;
  }
}

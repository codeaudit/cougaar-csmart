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
package org.cougaar.tools.csmart.recipe;

import java.io.IOException;
import java.util.*;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import org.cougaar.tools.csmart.ui.component.ConfigurableComponent;
import org.cougaar.tools.csmart.ui.component.ConfigurableComponentProperty;
import org.cougaar.tools.csmart.ui.component.PDbBase;
import org.cougaar.tools.csmart.ui.component.StringRange;
import org.cougaar.util.DBProperties;
import org.cougaar.tools.csmart.societies.database.DBUtils;

/**
 * Extends ConfigurableComponentProperty in order to override the
 * getAllowedValues method with values obtained from the set of
 * available queries. Also overrides the setValue to insure a good
 * value.
 **/
public class AgentQueryProperty extends ConfigurableComponentProperty {
    public AgentQueryProperty(ConfigurableComponent c, String name, Object value) {
        super(c, name, value);
    }

    public Set getAllowedValues() {
        return getAvailableQueries();
    }

//      public void setValue(Object newValue) {
//          if (!getAllowedValues().contains(newValue))
//              throw new IllegalArgumentException("Unknown query: " + newValue);
//          super.setValue(newValue);
//      }

    private static Set availableQueries = null;

  private static Set getAvailableQueries() {
    String query = null;

    if (availableQueries == null) {
      availableQueries = new HashSet();
      Map substitutions = new HashMap();

      substitutions.put(":insertion_point", "Node.AgentManager.Agent");

      try {
        Connection conn = DBUtils.getConnection();
        try {
          Statement stmt = conn.createStatement();
          query = DBUtils.getQuery("queryAllAgentNames", substitutions);
          ResultSet rs = stmt.executeQuery(query);
          while (rs.next()) {
            availableQueries.add(new StringRange(rs.getString(1)));
          }
          stmt.close();
        } finally {
          conn.close();
        }
      } catch (Exception e) {
        System.err.println("Query: " + query);
        e.printStackTrace();
        throw new RuntimeException("Error" + e);
      }
    }   
    return availableQueries;
  }
}
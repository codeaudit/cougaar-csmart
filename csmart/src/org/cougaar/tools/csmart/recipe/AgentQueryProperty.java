/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentProperty;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.util.DBProperties;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Extends ConfigurableComponentProperty in order to override the
 * getAllowedValues method with values obtained from the set of
 * available queries. Also overrides the setValue to insure a good
 * value.
 **/
public class AgentQueryProperty extends ConfigurableComponentProperty {

    private static Set availableQueries = null;

  /**
   * Creates a new <code>AgentQueryProperty</code> instance.
   *
   * @param c <code>ConfigurableComponent</code> the Property belongs to
   * @param name Name of the <code>ConfigurableComponentProperty</code>
   * @param value Value for the Property
   */
  public AgentQueryProperty(ConfigurableComponent c, String name, Object value) {
        super(c, name, value);
	setPropertyClass(StringRange.class);
    }

  /**
   * Gets all AllowedValues for this ComponentProperty
   *
   * @return null
   */
  public Set getAllowedValues() {
    return null;
  }
  
  /**
   * Sets the value of this Property
   *
   * @param newValue The new property value
   */
  public void setValue(Object newValue) {
    super.setValue(newValue);
  }
  
  private static Set getAvailableQueries() {
    String query = null;
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.recipe.AgentQueryProperty");

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
        if(log.isErrorEnabled()) {
          log.error("Query: " + query, e);
        }
        throw new RuntimeException("Error" + e);
      }
    }   
    return availableQueries;
  }
}

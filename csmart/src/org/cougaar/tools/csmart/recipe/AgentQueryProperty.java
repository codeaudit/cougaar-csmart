/* 
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package org.cougaar.tools.csmart.recipe;

import org.cougaar.core.agent.Agent;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentProperty;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.DBProperties;
import org.cougaar.util.log.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

      substitutions.put(":insertion_point", Agent.INSERTION_POINT);

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

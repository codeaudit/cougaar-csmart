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
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentProperty;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.util.DBProperties;

/**
 * Extends ConfigurableComponentProperty in order to override the
 * getAllowedValues method with values obtained from the set of
 * available queries. Also overrides the setValue to insure a good
 * value.
 **/
public class RecipeQueryProperty extends ConfigurableComponentProperty {
  public RecipeQueryProperty(ConfigurableComponent c, String name, Object value) {
    super(c, name, value);
  }

  public Set getAllowedValues() {
    return getAvailableQueries();
  }

  private static Set availableQueries = null;

  private static Set getAvailableQueries() {
    if (availableQueries == null) {
      try {
        availableQueries = new HashSet();
        // Get the base queries
        for (Iterator i = DBProperties.readQueryFile(PDbBase.QUERY_FILE).keySet().iterator();
             i.hasNext(); ) {
          String s = i.next().toString();
          if (s.startsWith("recipeQuery"))
            availableQueries.add(new StringRange(s));
        }

        // Get the user defined queries from recipeQueries.q
        try {
          for (Iterator i = DBProperties.readQueryFile(RecipeComponent.RECIPE_QUERY_FILE).keySet().iterator();
               i.hasNext(); ) {
            String s = i.next().toString();
            if (s.startsWith("recipeQuery"))
              availableQueries.add(new StringRange(s));
          }
        } catch (FileNotFoundException e) {
          // this is normal if a user has no separate recipe query file.
        }
                
      } catch (IOException ioe) {
        ioe.printStackTrace();
        availableQueries = Collections.EMPTY_SET;
      }
    }
    return availableQueries;
  }
}

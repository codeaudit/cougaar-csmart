/* 
 * <copyright>
 *  Copyright 2001,2002 BBNT Solutions, LLC
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
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentProperty;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

import org.cougaar.util.ConfigFinder;
import org.cougaar.util.DBProperties;
import org.cougaar.util.log.Logger;

/**
 * Extends ConfigurableComponentProperty in order to override the
 * getAllowedValues method with values obtained from the set of
 * available queries. 
 **/
public class RecipeQueryProperty extends ConfigurableComponentProperty {
  public RecipeQueryProperty(ConfigurableComponent c, String name, Object value) {
    super(c, name, value);
    setPropertyClass(StringRange.class);
  }

  public Set getAllowedValues() {
    return getAvailableQueries();
  }

  private static Set availableQueries = null;

  // When was the recipeQueries.q file last modified
  private static long rQFileLastMod = 0l;

  private static Set getAvailableQueries() {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.recipe.RecipeQueryProperty");
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
	File rqfile = ConfigFinder.getInstance().locateFile(RecipeComponent.RECIPE_QUERY_FILE);
	if (rqfile != null) {
	  try {
	    rQFileLastMod = rqfile.lastModified();
	  } catch (SecurityException se) {
	  }
	}

        try {
	  // Must reRead -- calling addQueryFile effectively does that.
	  // Alternatively, have a reRead method in DBProperties
	  DBProperties dbp = DBProperties.readQueryFile(RecipeComponent.RECIPE_QUERY_FILE);
	  dbp.addQueryFile(RecipeComponent.RECIPE_QUERY_FILE);
	  for (Iterator i = dbp.keySet().iterator();
               i.hasNext(); ) {
            String s = i.next().toString();
            if (s.startsWith("recipeQuery"))
              availableQueries.add(new StringRange(s));
          }
        } catch (FileNotFoundException e) {
          // this is normal if a user has no separate recipe query file.
	  if (log.isDebugEnabled()) {
	    log.debug("No " + RecipeComponent.RECIPE_QUERY_FILE + " to parse.");
	  }
        }
                
      } catch (IOException ioe) {
        if(log.isErrorEnabled()) {
          log.error("Exception", ioe);
        }
        availableQueries = Collections.EMPTY_SET;
      }
    } else {
      if (log.isDebugEnabled()) {
	log.debug("Not re-reading " + PDbBase.QUERY_FILE);
      }

      // Get the last modified date of the recipeQueries file.
      // If it's the same now as it was before, dont re-read
      File rqfile = ConfigFinder.getInstance().locateFile(RecipeComponent.RECIPE_QUERY_FILE);
      if (rqfile != null) {
	long newMod = 0l;
	try {
	  newMod = rqfile.lastModified();
	} catch (SecurityException se) {
	}
	// If file wasnt modified, use what we have
	if (newMod == rQFileLastMod) {
	  if (log.isDebugEnabled()) {
	    log.debug("Not re-reading unmodified" + RecipeComponent.RECIPE_QUERY_FILE);
	  }
	  return availableQueries;
	}
      }

      // Re-read recipeQueries.q, allowing user to edit the query names there
      try {
        // Get the user defined queries from recipeQueries.q
        try {
	  // Must reRead -- calling addQueryFile effectively does that.
	  // Alternatively, have a reRead method in DBProperties
	  DBProperties dbp = DBProperties.readQueryFile(RecipeComponent.RECIPE_QUERY_FILE);
	  dbp.addQueryFile(RecipeComponent.RECIPE_QUERY_FILE);
	  for (Iterator i = dbp.keySet().iterator();
               i.hasNext(); ) {
            String s = i.next().toString();
            if (s.startsWith("recipeQuery")) {
	      // Add only if not already present
	      if (! availableQueries.contains(new StringRange(s)))
		availableQueries.add(new StringRange(s));
	    }
          }
        } catch (FileNotFoundException e) {
          // this is normal if a user has no separate recipe query file.
	  if (log.isDebugEnabled()) {
	    log.debug("No " + RecipeComponent.RECIPE_QUERY_FILE + " to parse.");
	  }
        }
                
      } catch (IOException ioe) {
        if(log.isErrorEnabled()) {
          log.error("Exception", ioe);
        }
      }
    }
    return availableQueries;
  }
}

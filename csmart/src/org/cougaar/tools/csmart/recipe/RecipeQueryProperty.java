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

  // Cached list of available recipe queries
  private static Set availQueries = null;

  // When was the recipeQueries.q file last modified
  private static long rQFileLastMod = 0l;

  // Get the recipe queries available, updating the local cache
  private static Set getAvailableQueries() {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.recipe.RecipeQueryProperty");

    // Get the last modified date of the recipeQueries file.
    // If it's the same now as it was before, dont re-read
    File rqfile = ConfigFinder.getInstance().locateFile(RecipeComponent.RECIPE_QUERY_FILE);
    long newMod = 0l;
    if (rqfile != null) {
      try {
	newMod = rqfile.lastModified();
      } catch (SecurityException se) {
      }
    }

    // If we have never read the available queries, or the recipeQueries.q file was modified,
    // must re-collect the available queries
    if (availQueries == null || newMod != rQFileLastMod) {
      availQueries = new HashSet();
      rQFileLastMod = 0l;
      DBProperties dbp = null;
      
      // First grab the queries from PopulateDb.q
      // Note that this is the cached DBProperties - which may already
      // have the contents of recipeQueries.q in it
      try {
	dbp = DBProperties.readQueryFile(PDbBase.QUERY_FILE);
      } catch (IOException ioe) {
	if (log.isDebugEnabled()) {
	  log.debug("Couldn't read " + PDbBase.QUERY_FILE);
	}
      }

      // Add in the contents of recipeQueries.q - possibly just over-writing
      // the values previously added
      try {
	// Add to the basic PopDb.q the queries in recipeQueries.q
	dbp.addQueryFile(RecipeComponent.RECIPE_QUERY_FILE);
	rQFileLastMod = newMod;
      } catch (IOException e) {
	if (log.isDebugEnabled()) {
	  log.debug("No " + RecipeComponent.RECIPE_QUERY_FILE + " file found.");
	}
      }
      
      // Now collect the query names from the combo .q file
      for (Iterator i = dbp.keySet().iterator();
	   i.hasNext(); ) {
	String s = i.next().toString();
	if (s.startsWith("recipeQuery")) {
	  StringRange sr = new StringRange(s);
	  // Since on save only the last query of a given name is available,
	  // only list queries with a duplicate name once
	  if (! availQueries.contains(sr)) 
	    availQueries.add(sr);
	}
      } // loop over query names
    } // end of block to (re-)read the queries

    return availQueries;
  }
}

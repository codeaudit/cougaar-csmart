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

  // Cached list of available recipe queries from PopDb
  private static Set popDBQueries = null;
  private static Set recipeQQueries = null; // from recipeQueries.q

  // When was the recipeQueries.q file last modified
  private static long rQFileLastMod = 0l;

  // Get the recipe queries available, updating the local cache
  private static Set getAvailableQueries() {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.recipe.RecipeQueryProperty");
    Set returnQ = new HashSet(); // list to return

    // Read PopDB for recipe queries exactly once
    if (popDBQueries == null) {
      try {
        popDBQueries = new HashSet();
        // Get the base queries
        for (Iterator i = DBProperties.readQueryFile(PDbBase.QUERY_FILE).keySet().iterator();
             i.hasNext(); ) {
          String s = i.next().toString();
          if (s.startsWith("recipeQuery"))
            popDBQueries.add(new StringRange(s));
        }
      } catch (IOException ioe) {
        if(log.isErrorEnabled()) {
          log.error("Exception", ioe);
        }
        popDBQueries = Collections.EMPTY_SET;
      }
    }

    // The returned list includes at least those from popDB
    returnQ.addAll(popDBQueries);

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

    // If file wasnt modified, use what we have
    if (newMod == rQFileLastMod) {
      if (log.isDebugEnabled()) {
	log.debug("Not re-reading unmodified " + RecipeComponent.RECIPE_QUERY_FILE);
      }
      if (recipeQQueries != null)
	returnQ.addAll(recipeQQueries);
      return returnQ;
    }

    // If we get here, we're going to re-parse the recipeQueries.q file
    if (recipeQQueries == null)
      recipeQQueries = new HashSet();
    else
      recipeQQueries.clear();

    // (Re-)read recipeQueries.q, allowing user to edit the query names there
    try {

      // FIXME: Note that cause DBProperties is caching,
      // Old query names will remain,
      // until we have a real reRead method in DBProperties
      // Must reRead -- calling addQueryFile effectively handles adding additional
      // queries, but not removing (renaming) queries
      // Alternatively, have a reRead method in DBProperties
      DBProperties dbp = DBProperties.readQueryFile(RecipeComponent.RECIPE_QUERY_FILE);
      dbp.addQueryFile(RecipeComponent.RECIPE_QUERY_FILE);
      
      // Only reset lastmod timestamp after succesfully reparsing
      rQFileLastMod = newMod;
      
      for (Iterator i = dbp.keySet().iterator();
	   i.hasNext(); ) {
	String s = i.next().toString();
	if (s.startsWith("recipeQuery")) {
	  // Add only if not already present
	  if (! recipeQQueries.contains(new StringRange(s)))
	    recipeQQueries.add(new StringRange(s));
	}
      }
    } catch (FileNotFoundException e) {
      // this is normal if a user has no separate recipe query file.
      if (log.isDebugEnabled()) {
	log.debug("No " + RecipeComponent.RECIPE_QUERY_FILE + " to parse.");
      }
    } catch (IOException ioe) {
      if(log.isErrorEnabled()) {
	log.error("Exception", ioe);
      }
    }
    returnQ.addAll(recipeQQueries);
    return returnQ;
  }
}

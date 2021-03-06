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

import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentProperty;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.DBProperties;
import org.cougaar.util.log.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

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
    File rqfile = ConfigFinder.getInstance("csmart").locateFile(RecipeComponent.RECIPE_QUERY_FILE);
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
      availQueries = new TreeSet(); // keep queries sorted by name
      DBProperties dbp = null;
      
      // First grab the queries from PopulateDb.q
      // Note that this is the cached DBProperties - which may already
      // have the contents of recipeQueries.q in it
      try {
	// If have read the file before but still got indication of a change,
	// then re-read the query file to throw out any query name changes
	if (rQFileLastMod != 0l) {
	  if (log.isDebugEnabled()) {
	    log.debug("Re-reading query files.");
	  }
	  dbp = DBProperties.reReadQueryFile(PDbBase.QUERY_FILE, "csmart").unlock();
	} else
	  dbp = DBProperties.readQueryFile(PDbBase.QUERY_FILE, "csmart").unlock();
      } catch (IOException ioe) {
	if (log.isDebugEnabled()) {
	  log.debug("Couldn't read " + PDbBase.QUERY_FILE);
	}
      }

      rQFileLastMod = 0l;

      // Add in the contents of recipeQueries.q - possibly just over-writing
      // the values previously added
      try {
	// Add to the basic PopDb.q the queries in recipeQueries.q
	dbp.addQueryFile(RecipeComponent.RECIPE_QUERY_FILE, "csmart");
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

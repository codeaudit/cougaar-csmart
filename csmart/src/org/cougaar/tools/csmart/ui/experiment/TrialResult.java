/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.experiment;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

/**
 * Time that a trial was run and location of the results.
 */

public class TrialResult implements Serializable {
  private Date timestamp;
  private URL results;

  /**
   * Create a new trial result.
   * @param timestamp time that trial was run
   * @param URL where the trial results are stored
   */

  public TrialResult(Date timestamp, URL results) {
    this.timestamp = timestamp;
    this.results = results;
  }
    
  /**
   * Set the time that a trial was run.
   * @param timestamp time that trial was run
   */

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Get the time that a trial was run.
   * @return time that trial was run
   */

  public Date getTimestamp() {
    return timestamp;
  }

  /**
   * Set a reference to where the trial results are stored.
   * @param URL where the trial results are stored
   */

  public void setResults(URL results) {
    this.results = results;
  }

  /**
   * Get a reference to where the trial results are stored.
   * @return URL where the trial results are stored
   */

  public URL getResults() {
    return results;
  }

}

  

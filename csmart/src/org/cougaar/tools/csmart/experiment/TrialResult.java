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

package org.cougaar.tools.csmart.experiment;

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

  

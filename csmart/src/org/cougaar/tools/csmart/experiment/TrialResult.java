/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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
   * @param results URL where the trial results are stored
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
   * @param results URL where the trial results are stored
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

  

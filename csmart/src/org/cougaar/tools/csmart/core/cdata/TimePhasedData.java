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

package org.cougaar.tools.csmart.core.cdata;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Data Structure for Time Phased Data.
 * The time-phased data string is in the format:
 *
 * mm/dd/yyyy hh:mm am
 *
 */
public class TimePhasedData implements Serializable {

  private String startTime = null;
  private String stopTime = null;
  private SimpleDateFormat sdf;

  /**
   * Creates a new <code>TimePhasedData</code> instance.
   *
   */
  public TimePhasedData() {
    sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
  }

  /**
   * Sets the start time for the time-phased object
   * Data is in the format of: 03/25/1959 12:00 am
   * or an empty String.  An empty String implies
   * "ignore start time"
   *
   * @param start 
   * @exception ParseException if an error occurs
   */
  public void setStartTime(String start) throws ParseException {
    if(!start.equals("")) {
      sdf.parse(start);
    }
    this.startTime = start;
  }

  /**
   * Sets the Stop time for the time-phased object.
   * Data is in the format of: 03/25/1959 12:00 am
   * or an empty String. An empty String implies
   * "ignore stop time"  
   *
   * @param stop 
   * @exception ParseException if an error occurs
   */
  public void setStopTime(String stop) throws ParseException {
    if(!stop.equals("")) {
      sdf.parse(stop);
    }
    this.stopTime = stop;
  }

  /**
   * Gets the Start Time for the time
   * phased object
   *
   * @return start time.
   */
  public String getStartTime() {
    return startTime;
  }

  /**
   * Gets the Stop time for the time 
   * phased object. 
   *
   * @return stop time
   */
  public String getStopTime() {
    return stopTime;
  }
}

/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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

/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.component;

/**
 * Data Structure for Time Phased Data.
 * The time-phased data string is in the format:
 *
 * mm/dd/yyyy hh:mm am
 *
 */
public class TimePhasedData {

  /** Start time for the time phased object **/
  String startTime = null;

  /** Stop time for the time phased object **/
  String stopTime = null;

  public TimePhasedData() {
  }

  /**
   * Sets the start time for the time-phased object
   * Data is in the format of: 03/25/1959 12:00 am
   *
   * @param String start time.
   */
  public void setStartTime(String start) {
    this.startTime = start;
  }

  /**
   * Sets the Stop time for the time-phased object.
   * Data is in the format of: 03/25/1959 12:00 am
   *
   * @param String stop time.
   */
  public void setStopTime(String stop) {
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

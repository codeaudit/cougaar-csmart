/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.binder;

/**
 * A chart of capacity (0.0 to 1.0) over time.
 */
public interface CapacityChart {

  /**
   * Get the start time for this chart.
   */
  long getTime();

  /**
   * Get the capacity at this point in time.
   *
   * @param time millisecond time that is &gt;= to <tt>getTime()</tt>
   */
  double getCapacity(long time);

  /**
   * Get the area over the specified time interval.
   *
   * @param start millisecond time that is &gt;= to <tt>getTime()</tt>
   * @param stop millisecond time that is &gt; than <tt>start</tt>
   */
  double getArea(long start, long stop);

  /**
   * Degrade the chart over the specified time interval.
   * <p>
   * @param factor a double that is &gt;= 0.0 and &lt; 1.0
   *
   * @return another CapacityChart that should be used for future calls
   */
  CapacityChart create(double factor, long start, long stop);

}

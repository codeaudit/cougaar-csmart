/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
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
  public long getTime();

  /**
   * Get the capacity at this point in time.
   *
   * @param time millisecond time that is &gt;= to <tt>getTime()</tt>
   */
  public double getCapacity(long time);

  /**
   * Get the area over the specified time interval.
   *
   * @param start millisecond time that is &gt;= to <tt>getTime()</tt>
   * @param stop millisecond time that is &gt; than <tt>start</tt>
   */
  public double getArea(long start, long stop);

  /**
   * Degrade the chart over the specified time interval.
   * <p>
   * @param factor a double that is &gt;= 0.0 and &lt; 1.0
   *
   * @return another CapacityChart that should be used for future calls
   */
  public CapacityChart create(double factor, long start, long stop);

}

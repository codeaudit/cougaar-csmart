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

import junit.framework.*;
import java.util.Iterator;

public class TimePhasedDataTest extends TestCase {

  private TimePhasedData data = null;

  public TimePhasedDataTest(String name) {
    super(name);
  }

  protected void setUp() {
    data = new TimePhasedData();
  }

  public void testStartTime() {
    String start = "03/25/1959 12:00 am";
    data.setStartTime(start);
    assertEquals("Test start time", start, data.getStartTime());
  }

  public void testStopTime() {
    String stop = "03/25/1959 12:00 pm";
    data.setStopTime(stop);
    assertEquals("Test stop time", stop, data.getStopTime());
  }

  public static Test suite() {
    return new TestSuite(TimePhasedDataTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}

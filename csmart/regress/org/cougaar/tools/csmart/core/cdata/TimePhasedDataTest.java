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

package org.cougaar.tools.csmart.core.cdata;

import junit.framework.*;
import java.util.Iterator;
import java.text.ParseException;

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
    try {
      data.setStartTime(start);
      assertEquals("Test start time", start, data.getStartTime());
    } catch(ParseException pe) {
      fail("Caught unexpected exception");
    }

    try {
      data.setStartTime("Monday, 6 pm");
      fail("Expected ParseException");
    } catch(ParseException pe) {}

    try {
      data.setStartTime("");
      assertEquals("Test empty start time", "", data.getStartTime());
    } catch(ParseException pe) {
      fail("Caught unexpected exception");
    }
  }

  public void testStopTime() {
    String stop = "03/25/1959 12:00 pm";
    try {
      data.setStopTime(stop);
      assertEquals("Test stop time", stop, data.getStopTime());
    } catch(ParseException pe) {
      fail("Caught unexpected exception");
    }

    try {
      data.setStopTime("Monday, 6 pm");
      fail("Expected ParseException");
    } catch(ParseException pe) {}

    try {
      data.setStopTime("");
      assertEquals("Test empty stop time", "", data.getStopTime());
    } catch(ParseException pe) {
      fail("Caught unexpected exception");
    }
  }

  public static Test suite() {
    return new TestSuite(TimePhasedDataTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}

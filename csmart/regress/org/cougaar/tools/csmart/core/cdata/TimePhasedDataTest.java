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

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

public class CommunityTimePhasedDataTest extends TestCase {

  private CommunityTimePhasedData ctpd = null;
  private String[] comms = {"Comm1", "Commm2", "Comm3", "Comm4"};

  public CommunityTimePhasedDataTest(String name) {
    super(name);
  }

  protected void setUp() {

    ctpd = new CommunityTimePhasedData();
    ctpd.setCommunities(comms);
  }

  public void testGetCommunityIterator() {
    assertEquals("Test Size", 4, ctpd.size());
    Iterator iter = ctpd.getCommunityIterator();
    for(int i=0; iter.hasNext(); i++) {
      String nc = (String)iter.next();
      assertEquals("Test getCommunityIterator()", comms[i], nc);
    }
  }

  public void testGetCommunities() {
    String[] ca = ctpd.getCommunities();
    for(int i=0; i < ctpd.size(); i++) {
      assertEquals("Test getCommunities()", comms[i], ca[i]);
      assertEquals("Test getCommunities()", comms[i], ca[i]);
    }
  }
   
  public void testAddSetAndGetCommunity() {
    ctpd = new CommunityTimePhasedData();
    ctpd.addCommunity("Test1");
    assertEquals("Test Add Community", "Test1", ctpd.getCommunity(0));
    ctpd.setCommunity(0, "NewTest");
    assertEquals("Test setCommunity(int, String)", "NewTest", ctpd.getCommunity(0));

    try {
      ctpd.setCommunity(-1, "Should Fail");
      fail("IndexOutOfBoundsException expected.");
    } catch(IndexOutOfBoundsException e) {}

    try {
      ctpd.setCommunity(4, "Should Fail");
      fail("IndexOutOfBoundsException expected.");
    } catch(IndexOutOfBoundsException e) {}

  }

  public static Test suite() {
    return new TestSuite(CommunityTimePhasedDataTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}

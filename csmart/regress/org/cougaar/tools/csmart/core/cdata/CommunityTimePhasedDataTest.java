/* 
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

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

public class CommunityTimePhasedDataTest extends TestCase {

  private CommunityTimePhasedData ctpd = null;

  public CommunityTimePhasedDataTest(String name) {
    super(name);
  }

  protected void setUp() {
    ctpd = new CommunityTimePhasedData();
  }

  public void testCommunities() {
    String[] comms = {"Comm1", "Commm2", "Comm3", "Comm4"};

    ctpd.setCommunities(comms);
    assertEquals("Test Size", 4, ctpd.size());
    Iterator iter = ctpd.getCommunityIterator();
    String nc = (String)iter.next();
    assertEquals("Test getCommunityIterator()", "Comm1", nc);
    String[] ca = ctpd.getCommunities();
    assertEquals("Test getCommunities()", comms[0], ca[0]);
    assertEquals("Test getCommunities()", comms[3], ca[3]);

    ctpd = new CommunityTimePhasedData();
    ctpd.addCommunity("Test1");
    assertEquals("Test Add Community", "Test1", ctpd.getCommunity(0));
    ctpd.addCommunity(0, "NewTest");
    assertEquals("Test addCommunity(int, String)", "NewTest", ctpd.getCommunity(0));
  }

  public static Test suite() {
    return new TestSuite(CommunityTimePhasedDataTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}

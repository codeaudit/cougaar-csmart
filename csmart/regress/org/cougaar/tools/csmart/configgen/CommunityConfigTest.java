/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.configgen;

/**
 * CommunityConfigTest.java
 *
 *
 * Created: Tue Feb 20 13:59:06 2001
 *
 * @author Brian Krisler
 * @version 1.0
 */

import junit.framework.*;
import java.util.*;
import org.w3c.dom.Element;

public class CommunityConfigTest extends TestCase {
  
  private CommunityConfig comm = new CommunityConfig();

  public CommunityConfigTest(String name) {
    super(name);
  }

  public void setUp() {
  }

  public void testName() {
    comm.setName("Name");

    try {
      comm.setName("Name");
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertEquals("Name", comm.getName());
  }

  public void testTemplate() {
    comm.setTemplate("Template");
    
    try {
      comm.setTemplate("Template");
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}
    
    assertEquals("Template", comm.getTemplate());
  }

  public void testDemand() {
    comm.setDemand(10);

    try {
      comm.setDemand(10);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertEquals(10, comm.getDemand());
  }

  public void testProduction() {
    comm.setProduction(5);
    
    try {
      comm.setProduction(5);
      fail("IllegalArgumentException Expected");
    } catch(IllegalArgumentException e) {}

    assertEquals(5, comm.getProduction());
  }

  public void testSuppliers() {
    ArrayList aList = new ArrayList(1);

    aList.add("Supply One");
    comm.setSuppliers(aList);

    try {
      comm.setSuppliers(aList);
      fail("IllegalArgumentException Expected");
    } catch( IllegalArgumentException e) {}

    assertNotNull(comm.getSuppliers());
  }

  public void testLatitude() {
    comm.setLatitude(34.0);
    
    try {
      comm.setLatitude(34.0);
      fail("IllegalArgumentException Expected");
    } catch( IllegalArgumentException e) {}

    assertEquals(34.0, comm.getLatitude(), 0.01);
  }

  public void testLongitude() {
    comm.setLongitude(34.0);

    try {
      comm.setLongitude(34.0);
      fail("IllegalArgumentExcpetion Expected");
    } catch( IllegalArgumentException e) {}

    assertEquals(34.0, comm.getLongitude(), 0.01);
  }

  public void testParse() {
    XMLParser xp = new XMLParser("xmlParserTest.xml");
    
    CommunityConfig lcom = new CommunityConfig();
    Element elem = (Element)xp.getFirstNode();
    lcom.parse(elem);
    
    assertEquals("Comm 1", lcom.getName());
    assertEquals("Four Agent", lcom.getTemplate());
    assertEquals(10, lcom.getDemand());
    assertEquals(5, lcom.getProduction());
    Iterator i = lcom.getSuppliers();
    assertEquals("Comm 2", i.next());
    assertEquals(25.0, lcom.getLatitude(), 0.01);
    assertEquals(52.0, lcom.getLongitude(), 0.01);
  }

  public static Test suite() {
    return new TestSuite(CommunityConfigTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
} // CommunityConfigTest

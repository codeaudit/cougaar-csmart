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

package org.cougaar.tools.csmart.configgen;

import junit.framework.*;
import org.w3c.dom.*;
import java.io.*;

public class XMLParserTest extends TestCase {
  
  XMLParser xp = null;
  File testFile = null;
  private static final String testFileName = "xmlParserTest.xml";
  public XMLParserTest(String name) {
    super(name);
  }
  
  protected void setUp() {
    try {
      testFile = new File(testFileName);
    } catch (NullPointerException e) {
    }
    try {
      PrintWriter writer = new PrintWriter(new FileOutputStream(testFile));
      writer.println("<CommunityConfig>");
      writer.println("  <Community name=\"Comm 1\" template=\"Four Agent\">");
      writer.println("    <Demand>10</Demand>");
      writer.println("    <Production>5</Production>");
      writer.println("    <Supports/>");
      writer.println("    <Geography>");
      writer.println("      <Latitude>25</Latitude>");
      writer.println("      <Longitude>52</Longitude>");
      writer.println("    </Geography>");
      writer.println("  </Community>");
      writer.println("  <Community name=\"Comm 2\" template=\"Four Agent\">");
      writer.println("    <Demand>23</Demand>");
      writer.println("    <Production>13</Production>");
      writer.println("    <Supports>");
      writer.println("      <SupportedCommunity>Comm 1</SupportedCommunity>");
      writer.println("    </Supports>");
      writer.println("    <Geography>");
      writer.println("      <Latitude>45</Latitude>");
      writer.println("      <Longitude>75</Longitude>");
      writer.println("    </Geography>");
      writer.println("  </Community>");
      writer.println("  <Community name=\"Comm 3\" template=\"Four Agent\">");
      writer.println("    <Demand>3</Demand>");
      writer.println("    <Production>43</Production>");
      writer.println("    <Supports>");
      writer.println("      <SupportedCommunity>Comm 2</SupportedCommunity>");
      writer.println("    </Supports>");
      writer.println("    <Geography>");
      writer.println("      <Latitude>145</Latitude>");
      writer.println("      <Longitude>175</Longitude>");
      writer.println("    </Geography>");
      writer.println("  </Community>");
      writer.println("</CommunityConfig>");
      writer.close();
    } catch (SecurityException e) {
      fail("Got exception writing test file: " + e);
    } catch (FileNotFoundException e) {
      fail("Got exception writing test file: " + e);
    }

    xp = new XMLParser(testFileName);
  }

  protected void tearDown() {
    if (testFile != null) {
      try {
	testFile.delete();
      } catch (SecurityException e) {}
    }
  }

  public void testGetFirstChild() {
    Node n = xp.getFirstNode();
    assertEquals("Community", n.getNodeName());
  }

  public void testGetAllNodes() {
    NodeList nl = xp.getAllNodes();

    for(int i=0; i < nl.getLength(); i++) {
      Node sub = nl.item(i);
      if(sub.getNodeType() == Node.ELEMENT_NODE) {
	assertEquals("Community", sub.getNodeName());
      }
    } 
  }

  public static Test suite() {
    return new TestSuite(XMLParserTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
} // XMLParserTest

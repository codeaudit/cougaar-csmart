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

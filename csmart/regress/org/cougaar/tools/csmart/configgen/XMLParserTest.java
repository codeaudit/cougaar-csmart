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
 * XMLParserTest.java
 *
 *
 * Created: Tue Feb 20 14:15:00 2001
 *
 * @author Brian Krisler
 * @version 1.0
 */

import junit.framework.*;
import org.w3c.dom.*;

public class XMLParserTest extends TestCase {
  
  XMLParser xp = new XMLParser("xmlParserTest.xml");

  public XMLParserTest(String name) {
    super(name);
  }
  
  protected void setUp() {
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

/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.configgen;

import java.io.*;

import org.w3c.dom.*;
import org.apache.xerces.parsers.DOMParser;

/**
 * A simple XML Parser Utility that can be used by the Society Builders.
 *
 * This is completely optional -- the configuration doesn't need to be
 * from an XML file.
 */
public class XMLParser  {

  /** Parsed Document **/
  private Document doc;

  /** Root Element of Parsed Document **/
  private Element root;

  /** 
   * Constructor.  
   * <br>
   * @param xmlFileString name of the XML file to parse.
   */
  public XMLParser(String xmlFileString) {    
    parse(xmlFileString);
  }

  /**
   * Parses the XML file and creates a document
   * and a Root Element as private members for
   * future access.
   * <br>
   * @param xmlFileString name of the XML file to parse.
   */

  protected void parse(String xmlFileString) {
    DOMParser parser = new DOMParser();

    try {
      parser.parse(xmlFileString);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } catch (org.xml.sax.SAXException sae) {
      sae.printStackTrace();
    }

    doc = parser.getDocument();
    root = doc.getDocumentElement();
  }

  /**
   * Returns the first node in the XML file.
   * <br>
   * @return An XML Node containing the first Node entry
   * @see org.w3c.dom.Node
   */
  public Node getFirstNode() {
    NodeList nList = root.getChildNodes();
    Node n = null;

    for (int i=0; i < nList.getLength(); i++) {
      n = nList.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        return n;
      }
    }
    return null;
  }

  /**
   * Returns a NodeList of all Nodes in the XML file.
   * <br>
   * @return Nodelist of all nodes
   * @see org.w3c.dom.NodeList
   */
  public NodeList getAllNodes() {
    return root.getChildNodes();
  }

} // XMLParser

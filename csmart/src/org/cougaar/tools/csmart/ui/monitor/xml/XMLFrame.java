/*
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.xml;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import att.grappa.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTFrame;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTGraph;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.util.XMLUtils;
import org.cougaar.util.PropertyTree;

/**
 * A frame to display xml files.
 * The nodes in the graph are the elements of the xml file.
 * TODO: Nodes are too small for labels; font problem?
 */

public class XMLFrame extends CSMARTFrame {
  public static final String ROLLUP_DOWN_MENU_ITEM = "RollUp/Down";
  public static final String MARK_MENU_ITEM = "Mark";
  private static int index = 0; // generate unique names in attributes table
  private static XMLMarker markerDialog = null;

  /**
   * Create a frame for the specified graph.
   * @param title  title for the frame
   * @param graph  graph to display in the frame
   */

  public XMLFrame(String title, CSMARTGraph graph) {
    super(title, graph);
  }

  /**
   * Read an XML file and create and display a graph.
   */

  public static CSMARTGraph createGraph(File f) {
     XMLUtils xmlUtils = new XMLUtils();
     Document doc = xmlUtils.loadXMLFile(f);
     if(doc == null) return null;
     Vector nodeObjects = new Vector();
     createNode(nodeObjects, doc.getDocumentElement(),(XMLNode)null);
     CSMARTGraph graph = new CSMARTGraph(nodeObjects,
                                         CSMARTGraph.GRAPH_TYPE_XML);
     return graph;
  }

  public static CSMARTGraph createGraphWithSAXParser(File f) {
    XMLHandler xmlHandler = new XMLHandler();
    XMLReader parser = null;
    try {
      parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
    } catch (Exception e) {
      System.out.println("Exception parsing file: " + e);
      return null;
    }
    parser.setContentHandler(xmlHandler);
    parser.setErrorHandler(xmlHandler);
    try {
      parser.parse(f.getAbsolutePath());
    } catch (SAXParseException e) {
      // ignore
    } catch (Exception e) {
      System.err.println("error: Parse error occurred - "+e.getMessage());
      Exception se = e;
      if (e instanceof SAXException) {
        se = ((SAXException)e).getException();
      }
      if (se != null)
        se.printStackTrace(System.err);
      else
        e.printStackTrace(System.err);
    }
    Vector nodeObjects = xmlHandler.getNodeObjects();
    return new CSMARTGraph(nodeObjects, CSMARTGraph.GRAPH_TYPE_XML);
  }

  /**
   * Create XMLNodes with Nodes returned by the DOM parser.
   */
  private static void createNode(Vector nodeObjects, org.w3c.dom.Node node,
                                 XMLNode parent) {
// this code creates a XMLNode for each node returned by the parser
//     if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
//       String s = node.getNodeValue();
//       if (s == null)
//         return;
//       s = s.trim();
//       if (s.length() == 0)
//         return;
//     } else if (node.getNodeType() == org.w3c.dom.Node.COMMENT_NODE)
//       return;

    // only create nodes for society, hosts, nodes, and agents
    // for agents, add the child node properties to the agent's properties
    // TODO: add child node properties to society, host and node?
    XMLNode newNode = null;
    if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
      String name = node.getNodeName();
      if (name.equals("society") ||
          name.equals("host")) {
        newNode = new XMLNode(node);
      } else if (name.equals("node")) {
        String nodeName = 
          ((node.getAttributes()).getNamedItem("name")).getNodeValue();
        newNode = new XMLNode(node);
        if (parent != null)
          parent.addOutgoingLink(newNode.getUID());
        nodeObjects.add(newNode);
        parent = newNode;
        NodeList children = node.getChildNodes();
        // child nodes that are not agents are added as properties
        index = 1;
        for (int i = 0; i < children.getLength(); i++) {
          org.w3c.dom.Node child = children.item(i);
          if ((child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) &&
              child.getNodeName().trim().equals("agent")) {
            createNode(nodeObjects, (org.w3c.dom.Node)child, parent);
          } else {
            PropertyTree properties = new PropertyTree();
            getChildProperties(child, properties);
            newNode.addProperties(properties);
          }
        }
        return; // done handling "node" node
      } else if (name.equals("agent")) {
        String agentName = ((node.getAttributes()).getNamedItem("name")).getNodeValue();
        PropertyTree properties = new PropertyTree();
        index = 1;
        getChildProperties(node, properties);
        newNode = new XMLNode(agentName, properties);
      }
      if (newNode != null) {
        if (parent != null)
          parent.addOutgoingLink(newNode.getUID());
        nodeObjects.add(newNode);
        parent = newNode;
      }
    }
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      org.w3c.dom.Node child = children.item(i);
      createNode(nodeObjects, (org.w3c.dom.Node)child, parent);
    }
  }

  /**
   * If node type is Text,
   * then if the node value is non-null and the trimmed node value is non-empty,
   * then add it as an attribute with a default name.
   * If node type is Element,
   * then add the node name and all the attributes.
   * If node type is anything else, ignore it.
   */
  private static void getChildProperties(org.w3c.dom.Node node, PropertyTree properties) {
    //    System.out.println("Index: " + index);
    //    System.out.println("Node name: " + node.getNodeName());
    //    System.out.println("Node type: " + getNodeType(node));
    //    System.out.println("Node value: " + node.getNodeValue());
    int nodeType = node.getNodeType();
    if (nodeType == Node.TEXT_NODE) {
      String s = node.getNodeValue();
      if (s != null) {
        s = s.trim();
        if (s.length() != 0) {
          index++;
          properties.put(String.valueOf(index) + "-" + "text", s);
        }
      }
    } else if (nodeType == Node.ELEMENT_NODE) {
      NamedNodeMap map = node.getAttributes();
      if (map != null) {
        if (map.getLength() > 0)
          index++;
        for (int i = 0; i < map.getLength(); i++) {
          Node attributeNode = map.item(i);
          //          System.out.println(attributeNode.getNodeName() + " " +
          //                             attributeNode.getNodeValue());
          properties.put(String.valueOf(index) + "-" +
                         attributeNode.getNodeName(), 
                         attributeNode.getNodeValue());
        }
      }
    }
    //    System.out.println();
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      getChildProperties((org.w3c.dom.Node)children.item(i), properties);
    }
  }

  private static String getNodeType(org.w3c.dom.Node node) {
    switch (node.getNodeType()) {
    case Node.ATTRIBUTE_NODE:
      return "Attribute";
    case Node.CDATA_SECTION_NODE:
      return "CData";
    case Node.COMMENT_NODE:
      return "Comment";
    case Node.DOCUMENT_FRAGMENT_NODE:
      return "Document Fragment";
    case Node.DOCUMENT_NODE:
      return "Document";
    case Node.DOCUMENT_TYPE_NODE:
      return "Document Type";
    case Node.ELEMENT_NODE:
      return "Element";
    case Node.ENTITY_NODE:
      return "Entity";
    case Node.ENTITY_REFERENCE_NODE:
      return "Entity Reference";
    case Node.NOTATION_NODE:
      return "Notation";
    case Node.PROCESSING_INSTRUCTION_NODE:
      return "Processing Instruction";
    case Node.TEXT_NODE:
      return "Text";
    }
    return "Unknown";
  }

  /**
   * Add menu items specific to this type of graph.
   */

  public void customize() {
    insertMenuItem(CSMARTFrame.VIEW_MENU, ROLLUP_DOWN_MENU_ITEM, 7, this);
    insertMenuItem(CSMARTFrame.SHOW_MENU, MARK_MENU_ITEM, 0, this);
  }

  /**
   * Enable/disable menu items that are specific to this graph.
   * @param enable true to enable menu items; false to disable
   */

  public void enableSelectedMenus(boolean enable) {
    super.enableSelectedMenus(enable);
    showMenu.setEnabled(true); // always enable this for marking nodes
  }

  /**
   * Get table model to display the attributes of this node.
   * @param node the node for which to return a table model
   * @return     table model to use to display attributes for this node
   */

  public TableModel getAttributeTableModel(att.grappa.Node node) {
    return new XMLTableModel(node);
  }

  /**
   * Process menu commands that are specific to events;
   * pass other menu commands to CSMARTFrame.
   * @param evt the event received
   */

  public void actionPerformed(ActionEvent evt) {
    String command = ((JMenuItem)evt.getSource()).getText();

    if (command.equals(NEW_WITH_SELECTION_MENU_ITEM)) {
      CSMARTGraph newGraph = graph.newGraphFromSelection();
      new XMLFrame(NamedFrame.XML, newGraph);
      return;
    } else if (command.equals(ROLLUP_DOWN_MENU_ITEM)) {
      rollUpDown();
      return;
    } else if (command.equals(MARK_MENU_ITEM)) {
      mark();
      return;
    }

    super.actionPerformed(evt);
  }

  /**
   * Get selected node and if it has child nodes, make them
   * visible/invisible.
   */

  private void rollUpDown() {
    att.grappa.Node selectedNode = null;
    Vector elements = graph.getSelectedElements();
    for (int i = 0; i < elements.size(); i++) {
      Element element = (Element)elements.elementAt(i);
      if (element.getType() == GrappaConstants.NODE) {
        selectedNode = (att.grappa.Node)element;
        break;
      }
    }
    if (selectedNode == null)
      return;

    Vector children = graph.getHeadElements(selectedNode, new Vector());
    if (children.size() == 0)
      return;
    boolean makeVisible = true;
    String invAttrValue = "false";
    Element child = (Element)children.elementAt(0);
    if (child.visible) {
      makeVisible = false;
      invAttrValue = "true";
    }
    for (int i = 0; i < children.size(); i++) {
      child = (Element)children.elementAt(i);
      child.setAttribute(CSMARTGraph.INVISIBLE_ATTRIBUTE, invAttrValue);
      child.visible = makeVisible;
    }
    graph.updateGraph();
  }

  /**
   * Allow user to select color for nodes with specified
   * names and attributes.
   */

  private void mark() {
    if (markerDialog == null)
      markerDialog = new XMLMarker(graph);
    else
      markerDialog.show();
  }

}











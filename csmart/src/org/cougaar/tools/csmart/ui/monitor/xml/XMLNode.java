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

import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import org.cougaar.core.util.UID;
import org.cougaar.tools.csmart.ui.monitor.generic.NodeObject;
import org.cougaar.util.PropertyTree;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 * Creates a NodeObject, i.e. an object that can be graphed, from
 * an XML element.
 */

public class XMLNode implements NodeObject {
  boolean visible = true;
  String UID;
  String label;
  String toolTip = "";
  String color = "black";
  String shape = "ellipse";
  String sides = "0";
  String distortion = "0";
  String orientation = "0";
  Vector outgoingLinks = new Vector();
  PropertyTree properties = new PropertyTree();
  static int count = 0;

  /**
   * Create a NodeObject from a label and properties which were extracted from
   * an XML document by the caller.
   * @param label label for the node
   * @param properties name/value pairs to display in a table associated with this node
   */
  public XMLNode(String label, PropertyTree properties) {
    this.label = label;
    this.properties = properties;
    nodeCreator(label);
  }

  /**
   * Create a NodeObject from a XML Element.
   * The label is taken from the value of a text node, or the name
   * of an element node, or the value of a name attribute if it exists.
   * @param node XML node from which to create a grappa node
   */
  public XMLNode(Node node) {
    // set label
    int nodeType = node.getNodeType();
    if (nodeType == Node.TEXT_NODE)
      label = node.getNodeValue();
    else if (nodeType == Node.ELEMENT_NODE)
      label = node.getNodeName();
    else
      System.out.println("Bad node type: " + nodeType + " " + node.getNodeName() + " " + node.getNodeValue());

    // create properties from attributes, override label with name if specified
    NamedNodeMap map = node.getAttributes();
    if (map != null) {
      for (int i = 0; i < map.getLength(); i++) {
        Node attributeNode = map.item(i);
        if (attributeNode.getNodeName().equals("name"))
          label = attributeNode.getNodeValue();
        else
          properties.put(attributeNode.getNodeName(),
                         attributeNode.getNodeValue());
      }
    }

    nodeCreator(label);
  }

  /**
   * Create a NodeObject from a XML Element.
   * Called by the XMLHandler when using a SAX parser.
   * The label is taken from the value of a text node, or the name
   * of an element node, or the value of a name attribute if it exists.
   * @param label the label for the grappa node
   * @param attrs the attributes for the grappa node
   */

  public XMLNode(String label, Attributes attrs) {
    int n = attrs.getLength();
    for (int i = 0; i < n; i++)
      properties.put(attrs.getLocalName(i), attrs.getValue(i));
    nodeCreator(label);
  }

  /**
   * Common node creation code.
   * Replace newlines in labels with spaces; only display last component of class names;
   * limit labels to 30 characters; use the label to determine the node shape.
   * @param the node label
   */
  private void nodeCreator(String s) {
    // replace newlines in label with spaces
    label = s.replace('\n', ' ');

    // assume that any label that contains a dot is a classname
    // and only include the last component
    int i = label.lastIndexOf('.');
    if (i != -1)
      label = label.substring(i+1);

    // limit a label to 30 characters
    if (label.length() > 30)
      label = label.substring(0, 29);

    // UID is Noden
    UID = "Node " + String.valueOf(count++);

    // use label to determine shape
    if (label.equals("agent") || label.equals("agents")) {
      shape = "diamond";
    } else if (label.equals("community") || label.equals("communities")) {
      shape = "doublecircle";
    } else if (label.equals("component")) {
      //      shape = "circle";
      shape = "box";
    } else if (label.equals("description")) {
      //      shape = "roundedbox";
      shape = "box";
    } else if (label.equals("experiment")) {
      shape = "invtrapezium";
    } else if (label.equals("host") || label.equals("hosts")) {
      shape = "hexagon";
    } else if (label.equals("node") || label.equals("nodes")) {
      shape = "octagon";
    } else if (label.equals("parameter") || label.equals("parameters")) {
      shape = "parallelogram";
    } else if (label.equals("property") || label.equals("properties")) {
      //      shape = "pentagon";
      shape = "invtriangle";
    } else if (label.equals("propertygroup")) {
      shape = "egg";
    } else if (label.equals("type")) {
      shape = "house";
    }
  }

  public void addProperties(PropertyTree newProperties) {
    properties.putAll(newProperties);
  }

  public String getUID() {
    return UID;
  }

  public String getLabel() {
    return label;
  }
    
  public String getToolTip() {
    return toolTip;
  }

  public String getColor() {
    return color;
  }

  public String getBorderColor() {
    return null;
  }

  public String getFontStyle() {
    return "normal";
  }

  public String getShape() {
    return shape;
  }

  public String getSides() {
    return sides;
  }

  public String getDistortion() {
    return distortion;
  }

  public String getOrientation() {
    return orientation;
  }

  public PropertyTree getProperties() {
    return properties;
  }

  public Vector getIncomingLinks() {
    return null;
  }

  public Vector getOutgoingLinks() {
    return outgoingLinks;
  }

  public Vector getBidirectionalLinks() {
    return null;
  }

  private void setNodeShapeParameters() {
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public void addOutgoingLink(String UID) {
    outgoingLinks.add(UID);
  }

  public String getWidth() {
    return "0"; // default, ignored
  }

  public String getHeight() {
    return "0"; // default, ignored
  }
}





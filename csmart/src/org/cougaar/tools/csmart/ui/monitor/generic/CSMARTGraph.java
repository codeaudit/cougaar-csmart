/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.generic;

import att.grappa.*;
import java.awt.Color;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.rmi.server.UID;
import java.util.*;
import javax.swing.*;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.util.PropertyTree;

/**
 * Display graph representing arbitrary objects.
 */

public class CSMARTGraph extends Graph
{
  public static final String GRAPH_TYPE = "Graph_Type";
  public static final String GRAPH_TYPE_COMMUNITY = "Community";
  public static final String GRAPH_TYPE_SOCIETY = "Society";
  public static final String GRAPH_TYPE_AGENT = "Agents";
  public static final String GRAPH_TYPE_PLAN = "Plan";
  public static final String GRAPH_TYPE_EVENTS = "Events";
  public static final String GRAPH_TYPE_THREAD = "Thread";
  private static final String COLOR_SELECT = "colorSelect";
  private static final String RANK_ATTRIBUTE = "rank";
  private static final String RANK_SAME = "same";
  private static final int NODE_VALUE_LESS_THAN = 1;
  private static final int NODE_VALUE_EQUAL_TO = 2;
  private static final int NODE_VALUE_GREATER_THAN = 3;
  public static final String COLOR_DETERMINER = "colorDeterminer";
  // attribute assigned to transitive edges created when nodes are hidden
  private static final String PSEUDO_EDGE = "pseudo_edge";
  public static final String INVISIBLE_ATTRIBUTE = "invisible";
  private static final String COLOR_TABLE = "ColorTable";

  // Name of the .dot file generated from data retrieved from society
  private static final String dotFileName = "csmart.dot";

  // Full path to dot executable
  private String dotExecutable;
  
  // file from which graph was read, or to which it was saved
  private File file; 
  private Hashtable UIDToNode = new Hashtable();
  private Hashtable UIDToEdge = new Hashtable();
  private Hashtable UIDToIncoming = new Hashtable();
  private Hashtable UIDToOutgoing = new Hashtable();
  private Hashtable UIDToBidirectional = new Hashtable();
  private Vector markedElements;

  private static UIProperties properties;

  /**
   * Create a graph and nodes and edges in that graph.
   * This constructor is used to create a graph and later
   * add objects to it.
   */
  public CSMARTGraph() {
    super("CSMART", true, true);
    markedElements = new Vector();
    customizeGraph();
    setColors();
    setupDotPath();
  }

  /**
   * Create a graph from the specified collection.
   * GraphType is used to determine the type of graph to create
   * when a saved graph is read from a file.
   * @param objectsToGraph objects that implement NodeObject interface
   * @param graphType one of the GRAPH_TYPE constants defined in this class
   */
  public CSMARTGraph(Collection objectsToGraph, String graphType) {
    super("CSMART", true, true);
    markedElements = new Vector();
    setColors();
    if (objectsToGraph == null)
      return;
    for (Iterator i = objectsToGraph.iterator(); i.hasNext(); )
      makeNode((NodeObject)i.next());
    // after making all the nodes, for invisible nodes, hide the edges
    // and make new transitive edges for the hidden nodes
    Vector allNodes = vectorOfElements(GrappaConstants.NODE);
    for (int i = 0; i < allNodes.size(); i++) {
      Node node = (Node)allNodes.elementAt(i);
      if (!node.visible)
	hideEdges(node);
    }
    if (graphType != null)
      setAttribute(GRAPH_TYPE, graphType);
    customizeGraph();
    setupDotPath();
    doLayout();
  }


  /**
   * Figure out the path to the dot executable
   * We look for it in CIP/csmart/bin, CIP/sys, CIP/bin, CIP/csmart/data and CIP/csmart/lib
   * in that order.<br>
   * We expect the file to be dot.exe on Windows, else dot-l386.<br>
   * That is what we look for first. However, if we don't find it,
   * we look for dot-L386 or dot as well.<br>
   * If we cant recognize the OS, or can't find the expected dot
   * executable, exit (rather drastic!).
   */
  private void setupDotPath() {
    if (dotExecutable != null)
      return;
    // Win: x86, Linux: i386, Solaris: sparc
    String arch = (String)System.getProperty("os.arch");
    // Win: Windows NT, Linux: Linux, Solaris: SunOS
    String os = (String)System.getProperty("os.name");
    String[] dotNames = {"dot.exe", "dot-l386", "dot-L386", "dot"};
    String[] dotPaths = new String[5];
    dotPaths[0] = System.getProperty("org.cougaar.install.path") + File.separatorChar + "csmart" + File.separatorChar + "bin" + File.separatorChar;
    dotPaths[1] = System.getProperty("org.cougaar.install.path") + File.separatorChar + "sys" + File.separatorChar;
    dotPaths[2] = System.getProperty("org.cougaar.install.path") + File.separatorChar + "bin" + File.separatorChar;
    dotPaths[3] = System.getProperty("org.cougaar.install.path") + File.separatorChar + "csmart" + File.separatorChar + "data" + File.separatorChar;
    dotPaths[4] = System.getProperty("org.cougaar.install.path") + File.separatorChar + "csmart" + File.separatorChar + "lib" + File.separatorChar;

    String dotName = "dot.exe";
    if (os.equals("Linux") || os.equals("SunOS")) {
      dotName = "dot-l386";
      dotNames[1] = dotNames[0];
      dotNames[0] = dotName;
    } else if (! os.startsWith("Windows")) {
      // What is this OS?
      System.err.println("Unknown OS, " + os + ", not recognized. If this is a Windows or Linux variant, please report it on the Cougaar bug-tracking site. \n\nCannot lay out graphs without recognizing your system as either Windows or Linux.");
      System.exit(-1);
    }

    File dotFile = null;
    for (int j = 0; j < dotNames.length; j++) {
      for (int i = 0; i < dotPaths.length; i++) {
	dotExecutable = dotPaths[i] + dotNames[j];
	try {
	  System.out.println("Looking for " + dotExecutable);
	  dotFile = new File(dotExecutable);
	} catch (NullPointerException e) {
	  //System.out.println("CSMARTGraph: Could not open file handle for " + dotExecutable");
	  dotFile = null;
	}
	if (dotFile != null && dotFile.exists())
	  break;
      } // end of dotPaths loop
      if (dotFile != null && dotFile.exists())
	break;
    } // end of dotNames loop	

    if (dotFile == null || ! dotFile.exists()) {
      System.err.println("CSMARTGraph: Could not find dot executable: " + dotName + " (the version used for your OS, " + os + ").\n Make sure it is in CIP/csmart/bin, CIP/sys, CIP/bin, CIP/csmart/data or CIP/csmart/lib.");
      System.exit(-1);
    }
  } // end of setupDotPath

  /**
   * Create a graph from a dot file.
   */
  public static CSMARTGraph createGraphFromDotFile(File f) {
    InputStream input = null;
    try {
      input = new FileInputStream(f);
    } catch(FileNotFoundException fnf) {
      System.err.println(fnf.toString());
      return null;
    }
    CSMARTGraph graph = new CSMARTGraph();
    graph.file = f;
    Parser program = new Parser(input, System.err, graph);
    try {
      program.parse();
    } catch(Exception ex) {
      System.out.println("CSMARTGraph: parser exception: " + ex);
      return null;
    }
    stripAttributeQuotes(graph);
    Attribute colorTableAttr = graph.getAttribute(COLOR_TABLE);
    // if no color table, just get values for COLOR_DETERMINER colorName
    // from properties, and put these in the Grappa color table
    // and in COLOR_ATTR and STYLE_ATTR
    // note: this does not preserve colors for files that have no color table
    if (colorTableAttr ==  null) {
      GraphEnumeration ge = graph.elements(GrappaConstants.NODE);

      while (ge.hasMoreElements()) {
	Node node = (Node)ge.nextGraphElement();
	Attribute a = node.getAttribute(COLOR_DETERMINER);
	if (a == null)
	  continue;
	String colorName = a.getStringValue();
	Color color = properties.getClusterColor(colorName);
	GrappaColor.addColor(colorName, color);
	node.setAttribute(COLOR_ATTR, colorName);
	node.setAttribute(GrappaConstants.STYLE_ATTR,
			  new GrappaStyle(GrappaConstants.NODE, 
			    "filled(true), line_color(" + colorName + ")"));
      }
    } else {
      // set uiproperties and GrappaColor from color table
      String colorTable = colorTableAttr.getStringValue();
      // parse color table, return hashtable of colorname to color
      Hashtable colors = readColorTable(colorTable);
      Enumeration keys = colors.keys();
      // enter colorname to color mappings in uiproperties and GrappaColor
      while (keys.hasMoreElements()) {
	String colorName = (String)keys.nextElement();
	Color color = (Color)colors.get(colorName);
	properties.setClusterColor(colorName, color);
	GrappaColor.addColor(colorName, color);
      }
      // define COLOR_ATTR and STYLE_ATTR to use COLOR_DETERMINER value
      GraphEnumeration ge = graph.elements(GrappaConstants.NODE);
      while (ge.hasMoreElements()) {
	Node node = (Node)ge.nextGraphElement();
	Attribute a = node.getAttribute(COLOR_DETERMINER);
	if (a == null)
	  continue;
	String colorName = a.getStringValue();
	node.setAttribute(COLOR_ATTR, colorName);
	node.setAttribute(GrappaConstants.STYLE_ATTR,
			  new GrappaStyle(GrappaConstants.NODE, 
                            "filled(true), line_color(" + colorName + ")"));
      }
    }
    graph.buildShapes();
    return graph;
  }

  /**
   * This setter is called by CSMARTUL before creating any graphs,
   * so that all the graphs share the same properties (which determine
   * colors).
   */

  public static void setProperties(UIProperties p) {
    properties = p;
  }

  /**
   * Used by specialized graphs (i.e. event graphs) to read properties.
   */

  public static UIProperties getProperties() {
    return properties;
  }

  /**
   * Read the color table from a dot file.
   */

  private static Hashtable readColorTable(String s) {
    Hashtable ht = new Hashtable();
    StringTokenizer st = new StringTokenizer(s, ";");
    while (st.hasMoreTokens()) {
      String entry = st.nextToken();
      int index = entry.indexOf('+');
      String colorName = entry.substring(0, index);
      Color color = stringToColor(entry.substring(index+1));
      ht.put(colorName, color);
    }
    return ht;
  }

  /**
   * Create a color from a string of comma separated RGB values.
   */

  private static Color stringToColor(String s) {
    StringTokenizer st = new StringTokenizer(s, ",");
    if (st.countTokens() < 3) {
      System.out.println("CSMARTGraph: WARNING: Can't parse color: " + s);
      return Color.green; // bad color string, return default color
    }
    int[] rgb = new int[3];
    try {
      for (int i = 0; i < 3; i++)
	rgb[i] = Integer.parseInt(st.nextToken());
    } catch (Exception e) {
      System.out.println("Exception parsing color string: " + e);
      return Color.green;
    }
    return new Color(rgb[0], rgb[1], rgb[2]);
  }


  /**
   * Copy all nodes and edges in the specified graph and all their
   * attributes (including position) and create a new
   * graph with the new elements and with the specified name.
   * @param graphToCopy the graph to copy
   * @param name        name for the new graph
   */

  public CSMARTGraph(CSMARTGraph graphToCopy, String name) {
    super(name, true, true);
    setupDotPath();
    markedElements = new Vector();
    GraphEnumeration ge = graphToCopy.elements(GrappaConstants.NODE);
    Hashtable newNodes = new Hashtable();
    // create copies of nodes
    while (ge.hasMoreElements()) {
      Node thisNode = (Node)ge.nextGraphElement();
      Node node = new Node(this, thisNode.getName());
      newNodes.put(node.getName(), node);
      UIDToNode.put(thisNode.getName(), node); // used to match lay out info
      Enumeration attributes = thisNode.getAttributePairs();
      while (attributes.hasMoreElements()) {
	Attribute thisAttribute = (Attribute)attributes.nextElement();
	node.setAttribute(thisAttribute.getName(),
			  thisAttribute.getValue());
      }
    }
    // create copies of edges, between the new nodes
    // and copy their attributes
    ge = graphToCopy.elements(GrappaConstants.EDGE);
    while (ge.hasMoreElements()) {
      Edge thisEdge = (Edge)ge.nextGraphElement();
      Node tailNode = (Node)newNodes.get(thisEdge.getTail().getName());
      Node headNode = (Node)newNodes.get(thisEdge.getHead().getName());
      if (tailNode == null || headNode == null)
	System.out.println("WARNING: null node");
      Edge edge = new Edge(this, tailNode, headNode);
      Enumeration attributes = thisEdge.getAttributePairs();
      while (attributes.hasMoreElements()) {
	Attribute thisAttribute = (Attribute)attributes.nextElement();
	edge.setAttribute(thisAttribute.getName(),
			  thisAttribute.getValue());
      }
    }
    // preserve graph type
    Attribute a = graphToCopy.getAttribute(GRAPH_TYPE);
    if (a != null)
      setAttribute(GRAPH_TYPE, a.getStringValue());
    customizeGraph(); // set direction, font, etc.
  }

  /**
   * Copy only selected nodes and edges in the specified graph and all their
   * attributes (including position) and create a new
   * graph with the new elements and with the specified name.
   * @param graphToCopy the graph to copy
   * @param name        name for the new graph
   * @param selectedElementsOnly true to copy only selected elements
   */

  public CSMARTGraph(CSMARTGraph graphToCopy, String name, 
		     boolean selectedElementsOnly) {
    super(name, true, true);
    markedElements = new Vector();
    Vector selectedElements = null;
    if (selectedElementsOnly)
      selectedElements = graphToCopy.getSelectedElements();
    else
      selectedElements = graphToCopy.vectorOfElements(GrappaConstants.NODE+
						      GrappaConstants.EDGE);
    Hashtable newNodes = new Hashtable();
    //    System.out.println("Graph to copy: " + graphToCopy);
    // create copies of nodes
    for (int i = 0; i < selectedElements.size(); i++) {
      Element e = (Element)selectedElements.elementAt(i);
      if (!(e instanceof Node))
	continue;
      Node thisNode = (Node)e;
      //      System.out.println("Adding node: " + thisNode.getName() +
      //			 " to: " + this);
      Node node = new Node(this, thisNode.getName());
      newNodes.put(node.getName(), node);
      UIDToNode.put(thisNode.getName(), node); // used to match lay out info
      Enumeration attributes = thisNode.getAttributePairs();
      while (attributes.hasMoreElements()) {
	Attribute thisAttribute = (Attribute)attributes.nextElement();
	node.setAttribute(thisAttribute.getName(),
			  thisAttribute.getValue());
      }
    }
    // create copies of edges, between the new nodes
    // and copy their attributes
    // this makes separate passes at creating nodes and edges
    // to ensure that the new nodes exist before creating the new edges
    for (int i = 0; i < selectedElements.size(); i++) {
      Element e = (Element)selectedElements.elementAt(i);
      if (!(e instanceof Edge))
	continue;
      Edge thisEdge = (Edge)e;
      Node tailNode = (Node)newNodes.get(thisEdge.getTail().getName());
      Node headNode = (Node)newNodes.get(thisEdge.getHead().getName());
      if (tailNode == null || headNode == null)
	System.out.println("WARNING: null node");
      Edge edge = new Edge(this, tailNode, headNode);
      Enumeration attributes = thisEdge.getAttributePairs();
      while (attributes.hasMoreElements()) {
	Attribute thisAttribute = (Attribute)attributes.nextElement();
	edge.setAttribute(thisAttribute.getName(),
			  thisAttribute.getValue());
      }
    }
    // preserve graph type
    Attribute a = graphToCopy.getAttribute(GRAPH_TYPE);
    if (a != null)
      setAttribute(GRAPH_TYPE, a.getStringValue());
    customizeGraph(); // set direction, font, etc.
  }

  /**
   * Create a new graph containing the selected nodes and ALL their edges.
   * @return   the new graph
   */

  public CSMARTGraph newGraphFromSelection() {
    // get the selected nodes and edges from this graph
    Vector elements = getSelectedElements();
    if (elements == null) {
      //      System.out.println("No elements selected");
      return null;
    }
    // create a copy of this graph, both nodes and edges
    CSMARTGraph newGraph = new CSMARTGraph(this, "Selected Nodes");
    // clear selection in new graph
    newGraph.clearSelection();
    // get the names of the selected nodes
    Vector nodeNames = new Vector(elements.size());
    for (int i = 0; i < elements.size(); i++) {
      Element element = (Element)elements.elementAt(i);
      if (element instanceof Node)
	nodeNames.addElement(((Node)element).getName());
    }
    // keep only the selected nodes
    newGraph.keepNamedNodes(nodeNames);
    // layout new graph
    newGraph.doLayout();
    return newGraph;
  }

  /**
   * Create a new graph containing the selected nodes and
   * only the selected edges.
   * @return   the new graph
   */

  public CSMARTGraph newGraphWithSelectedElementsOnly() {
    // get the selected nodes and edges from this graph
    Vector elements = getSelectedElements();
    if (elements == null) {
      //      System.out.println("No elements selected");
      return null;
    }
    // create a copy of this graph, both nodes and edges
    CSMARTGraph newGraph = new CSMARTGraph(this, "Selected Nodes And Edges",
					   true);
    // clear selection in new graph
    newGraph.clearSelection();
    // layout new graph
    newGraph.doLayout();
    return newGraph;
  }

  /**
   * Set color used for selection.
   * TODO: determine why this doesn't work in grappa
   */

  private void setColors() {
    GrappaColor.addColor(COLOR_SELECT, properties.getColorSelect());
  }

  /**
   * Make a node in the graph from a NodeObject.
   * This calls methods on the NodeObject
   * which provide the information needed to create a node.
   * @param obj the object for which to make a node
   */

  private Node makeNode(NodeObject obj) {
    String UID = obj.getUID();
    if (UIDToNode.get(UID) != null)
      return null; // don't make duplicates
    Node node = new Node(this, UID);
    UIDToNode.put(UID, node);
    if (!obj.isVisible()) {
      node.setAttribute(INVISIBLE_ATTRIBUTE, "true");
      node.visible = false;
    }
    node.setAttribute(PropertyNames.UID_ATTR, UID);
    node.setAttribute(GrappaConstants.LABEL_ATTR, obj.getLabel());
    node.setAttribute(GrappaConstants.TIP_ATTR, obj.getToolTip());
    node.setAttribute(GrappaConstants.SHAPE_ATTR, obj.getShape());
    // only set these if they're not the default value
    String sides = obj.getSides();
    if (!sides.equals("0"))
      node.setAttribute(GrappaConstants.SIDES_ATTR, sides);
    String distortion = obj.getDistortion();
    if (!distortion.equals("0"))
      node.setAttribute(GrappaConstants.DISTORTION_ATTR, distortion);
    String orientation = obj.getOrientation();
    if (!orientation.equals("0"))
      node.setAttribute(GrappaConstants.ORIENTATION_ATTR, orientation);
    String width = obj.getWidth();
    if (!width.equals("0"))
      node.setAttribute(GrappaConstants.WIDTH_ATTR, width);
    String height = obj.getHeight();
    if (!height.equals("0"))
      node.setAttribute(GrappaConstants.HEIGHT_ATTR, height);
    // get a string that determines the color
    String colorName = obj.getColor();
    // add mapping from this string to a real Color in grappa
    GrappaColor.addColor(colorName, properties.getClusterColor(colorName));
    node.setAttribute(GrappaConstants.COLOR_ATTR, colorName);
    GrappaStyle gs = new GrappaStyle(GrappaConstants.NODE, 
	   "filled(true), line_color(" + colorName + ")");
    node.setAttribute(GrappaConstants.STYLE_ATTR, gs);
    String fontStyle = obj.getFontStyle();
    if (!fontStyle.equals("normal"))
      node.setAttribute(GrappaConstants.FONTSTYLE_ATTR, fontStyle);
    // set attribute for what string determines color
    // used to reconstitute color mapping from saved nodes
    node.setAttribute(COLOR_DETERMINER, colorName);
    // get attributes for the object
    PropertyTree properties = obj.getProperties();
    Set names = properties.keySet();
    Iterator namesIter = names.iterator();
    while (namesIter.hasNext()) {
      String name = (String)namesIter.next();
      Object o = properties.get(name);
      if (o instanceof String)
	node.setAttribute(name, (String)o);
      else if (o instanceof Vector) {
	Vector values = (Vector)o;
	for (int i = 0; i < values.size(); i++)
	  node.setAttribute(name + "_" + i, (String)values.elementAt(i));
      } else if (o instanceof ArrayList) {
	ArrayList values = (ArrayList)o;
	for (int i = 0; i < values.size(); i++)
	  node.setAttribute(name + "_" + i, (String)values.get(i));
      }
      // for debugging
      //      else
      //	System.out.println("Property: " + name + " of class: " + o.getClass() + " not copied to grappa node");
    }
    // get UIDs at the incoming (tail) end of a link
    Vector incomingLinks = obj.getIncomingLinks();
    if (incomingLinks != null) {
      for (int i = 0; i < incomingLinks.size(); i++) {
	String tailUID = (String)incomingLinks.elementAt(i);
	// get the node for each incoming link
	Node tailNode = (Node)UIDToNode.get(tailUID);
	// if no node exists for the tail UID
	if (tailNode == null) {
	  // get or create the set of outgoing links for the tail UID
	  Set outgoing = (Set)UIDToOutgoing.get(tailUID);
	  if (outgoing == null)
	    outgoing = new HashSet();
	  // add the current node to the set of outgoing links to create
	  outgoing.add(node);
	  UIDToOutgoing.put(tailUID, outgoing);
	} else {
	  Edge edge = null;
	  try {
	    edge = new Edge(this, tailNode, node);
	  } catch (RuntimeException e) {
	    System.out.println("Couldn't create edge from: " +
			       tailNode.getName() + " " +
			       node.getName() + " " + e);
	    continue;
	  }
	  edge.setAttribute(GrappaConstants.TIP_ATTR, 
			    (tailNode.getAttributeValue(PropertyNames.UID_ATTR)).toString() +
			    " " +
			    (node.getAttributeValue(PropertyNames.UID_ATTR)).toString());
	}
      }
    }
    // handle the case where the other end of the link was created first
    Set outgoing = (Set)UIDToOutgoing.get(UID);
    if (outgoing != null)
      for (Iterator i = outgoing.iterator(); i.hasNext(); ) {
	Node headNode = (Node)i.next();
	Edge edge = null;
	try {
	  edge = new Edge(this, node, headNode);
	} catch (RuntimeException e) {
	  System.out.println("Couldn't create edge from: " +
			     node.getName() + " " +
			     headNode.getName() + " " + e);
	  continue;
	}
	edge.setAttribute(GrappaConstants.TIP_ATTR, 
			  (node.getAttributeValue(PropertyNames.UID_ATTR)).toString() +
			  " " +
			  (headNode.getAttributeValue(PropertyNames.UID_ATTR)).toString());
      }

    // get UIDs at the outgoing (head) end of a link
    Vector outgoingLinks = obj.getOutgoingLinks();
    if (outgoingLinks != null) {
      for (int i = 0; i < outgoingLinks.size(); i++) {
	String headUID = (String)outgoingLinks.elementAt(i);
	Node headNode = (Node)UIDToNode.get(headUID);
	if (headNode == null) {
	  Set incoming = (Set)UIDToIncoming.get(headUID);
	  if (incoming == null)
	    incoming = new HashSet();
	  incoming.add(node);
	  UIDToIncoming.put(headUID, incoming);
	} else {
	  Edge edge = null;
	  try {
	    edge = new Edge(this, node, headNode);
	  } catch (RuntimeException e) {
	    System.out.println("Couldn't create edge from: " +
			       node.getName() + " " +
			       headNode.getName() + " " + e);
	    continue;
	  }
	  edge.setAttribute(GrappaConstants.TIP_ATTR, 
			    (node.getAttributeValue(PropertyNames.UID_ATTR)).toString() +
			    " " +
			    (headNode.getAttributeValue(PropertyNames.UID_ATTR)).toString());
	}
      }
    }
    // handle case where the other end of the link was created first
    Set incoming = (Set)UIDToIncoming.get(UID);
    if (incoming != null) {
      for (Iterator i = incoming.iterator(); i.hasNext(); ) {
	Node tailNode = (Node)i.next();
	Edge edge = null;
	try {
	  edge = new Edge(this, tailNode, node);
	} catch (RuntimeException e) {
	  System.out.println("Couldn't create edge from: " +
			     tailNode.getName() + " " +
			     node.getName() + " " + e);
	  continue;
	}
	edge.setAttribute(GrappaConstants.TIP_ATTR, 
			  (tailNode.getAttributeValue(PropertyNames.UID_ATTR)).toString() +
			  " " +
			  (node.getAttributeValue(PropertyNames.UID_ATTR)).toString());
      }
    }

    // create bidirectional links
    Vector bidirectionalLinks = obj.getBidirectionalLinks();
    if (bidirectionalLinks != null) {
      for (int i = 0; i < bidirectionalLinks.size(); i++) {
	String linkedToUID = (String)bidirectionalLinks.elementAt(i);
	Node linkedToNode = (Node)UIDToNode.get(linkedToUID);
	if (linkedToNode != null) {
	  try {
	    Edge edge = new Edge(this, node, linkedToNode);
	    edge.setAttribute(GrappaConstants.DIR_ATTR, "none");
	  } catch (Exception e) {
	    // we'll probably get exceptions for duplicate edges
	    //	    System.out.println("Couldn't create edge from: " +
	    //			       node.getName() + " " +
	    //			       linkedToNode.getName() + " " + e);
	    continue;
	  }
	} else {
	  Set bidirectional = (Set)UIDToBidirectional.get(linkedToUID);
	  if (bidirectional == null)
	    bidirectional = new HashSet();
	  bidirectional.add(node);
	  UIDToBidirectional.put(linkedToUID, bidirectional);
	}
      }
    }

    // bidirectional links are not necessarily referenced in both nodes
    // so handle the case where only one node has the link
    Set bidirectional = (Set)UIDToBidirectional.get(UID);
    if (bidirectional != null) {
      for (Iterator i = bidirectional.iterator(); i.hasNext(); ) {
	Node linkedToNode = (Node)i.next();
	try {
	  Edge edge = new Edge(this, node, linkedToNode);
	  edge.setAttribute(GrappaConstants.DIR_ATTR, "none");
	} catch (Exception e) {
	  //	  System.out.println("Couldn't create edge from: " +
	  //			     node.getName() + " " +
	  //			     linkedToNode.getName() + " " + e);
	  continue;
	}
      }
    }

    return node;
  }

  /**
   * Add nodes for the specified objects to this graph.
   * @param objectsToAdd objects to graph; must implement NodeObject interface
   */

  public void addObjects(Collection objectsToAdd) {
    if (objectsToAdd == null)
      return;
    // after making all the nodes, hide the edges
    // and make new transitive edges for the hidden nodes
    Vector allNodes = vectorOfElements(GrappaConstants.NODE);
    for (int i = 0; i < allNodes.size(); i++) {
      Node node = (Node)allNodes.elementAt(i);
      if (!node.visible) 
	hideEdges(node);
    }
    doLayout();
    repaint();
  }

  /**
   * Customize graph: specify font, allow menus, disallow editing.
   * Layout left-to-right, except for GRAPH_TYPE_SOCIETY which is
   * top-to-bottom.
   */

  private void customizeGraph() {
    String graphType = (String)getAttributeValue(GRAPH_TYPE);
    if (graphType != null && graphType.equals(GRAPH_TYPE_SOCIETY))
      setAttribute(GrappaConstants.RANKDIR_ATTR, "TB");
    else
      setAttribute(GrappaConstants.RANKDIR_ATTR, "LR");
    setNodeAttribute(GrappaConstants.FONTSIZE_ATTR, "12");
    setNodeAttribute(GrappaConstants.FONTNAME_ATTR, "Helvetica");
    // TODO: determine how to set selection color as this doesn't work
    //    setGrappaAttribute(GrappaConstants.GRAPPA_SELECTION_STYLE_ATTR, 
    //		       "lineColor(" + COLOR_SELECT + ")");
    setEditable(false); // don't allow creation of new nodes and edges
    setMenuable(true);
    // is selectable by default
    setErrorWriter(new PrintWriter(System.err,true));
  }

  /**
   * Print graph attributes; for debugging.
   */

  private void printGraphAttributes() {
    Enumeration e = getAttributePairs();
    while (e.hasMoreElements()) {
      Attribute a = (Attribute)e.nextElement();
      System.out.println("Graph attribute: " + 
			 a.getName() + " " + a.getValue());
    }
  }

  /**
   * Get vector of selected nodes and edges.
   * Always returns a clone of "currentSelection"; never the original
   * value, so that callers can compare selection snapshots
   * before and after other events.
   * @return vector of selected nodes and edges (vector of att.grappa.Element)
   */

  public Vector getSelectedElements() {
    if (currentSelection == null)
      return null;
    if (currentSelection instanceof Element) {
      Vector results = new Vector(1);
      results.addElement(currentSelection);
      return results;
    }
    return (Vector)((Vector)currentSelection).clone();
  }

  /**
   * Mark (color) the incoming edges and the nodes at the
   * tails of those edges; recurses until it reaches the start of the graph.
   * Note that the color parameter must have been added to the
   * grappa color table (using GrappaColor.addColor).
   * @param node     starting Node
   * @param color    string which describes color
   */

  public void markTails(Node node, String color) {
    Vector elements = getTailElements(node, new Vector());
    mark(elements, color);
  }

  /**
   * Get the incoming edges and the nodes at the tails of those edges;
   * recurses until it reaches the start of the graph.
   * @param       node     starting node
   * @param       results  initially an empty vector
   * @return               vector of att.grappa.Element
   */

  private Vector getTailElements(Node node, Vector results) {
    Enumeration edges = node.inEdgeElements();
    while (edges.hasMoreElements()) {
      Edge edge = (Edge)edges.nextElement();
      Node tailNode = edge.getTail();
      if (tailNode != null) {
	results.addElement(edge);
	results.addElement(tailNode);
	results = getTailElements(tailNode, results);
      }
    }
    return results;
  }

  /**
   * Mark (color) the outgoing edges and the nodes at the
   * heads of those edges; recurses until it reaches the end of the graph.
   * Note that the color parameter must have been added to the
   * grappa color table (using GrappaColor.addColor).
   * @param node     starting Node
   * @param color    string which describes color
   */

  public void markHeads(Node node, String color) {
    Vector elements = getHeadElements(node, new Vector());
    mark(elements, color);
  }

  /**
   * Get the outgoing edges and the nodes at the heads of those edges;
   * recurses until it reaches the end of the graph.
   * @param       node     starting node
   * @param       results  initially an empty vector
   * @return               vector of att.grappa.Element
   */

  private Vector getHeadElements(Node node, Vector results) {
    Enumeration edges = node.outEdgeElements();
    while (edges.hasMoreElements()) {
      Edge edge = (Edge)edges.nextElement();
      Node headNode = edge.getHead();
      if (headNode != null) {
	results.addElement(edge);
	results.addElement(headNode);
	results = getHeadElements(headNode, results);
      }
    }
    return results;
  }

  /**
   * Get vector of nodes with the specified attribute name/value.
   * @param attributeName   name of the node attribute
   * @param attributeValue  value of the node attribute
   * @return                matching nodes; vector of att.grappa.Node
   */

  public Vector getNodesWithAttribute(String attributeName,
				      String attributeValue) {
     Vector allNodes = vectorOfElements(GrappaConstants.NODE);
     Vector selectedNodes = new Vector(allNodes.size());
     for (int i = 0; i < allNodes.size(); i++) {
       Node node = (Node)allNodes.elementAt(i);
       Attribute a = node.getAttribute(attributeName);
       if (a != null) 
	 if (a.getStringValue().equals(attributeValue))
	   selectedNodes.addElement(node);
     }
     return selectedNodes;
  }

  /**
   * Get vector of alternating node color determiner values 
   * (value of COLOR_DETERMINER attribute), and color
   * attribute values (value of GrappaConstants.COLOR_ATTR attribute).
   * @return     vector of String
   */

  public Vector getNodeColors() {
    Vector nodeColors = new Vector();
    Vector allNodes = vectorOfElements(GrappaConstants.NODE);
    for (int i = 0; i < allNodes.size(); i++) {
      Node node = (Node)allNodes.elementAt(i);
      Attribute attribute = node.getAttribute(COLOR_DETERMINER);
      if (attribute == null) {
	System.out.println("CSMARTGraph: No Color Determiner Attribute for: " +
			   node.getName());
      } else {
	String colorDeterminer = attribute.getStringValue();
	if (colorDeterminer == null)
	  System.out.println("CSMARTGraph: Color Determiner Attribute is null for: " +
			     node.getName());
	else {
	  // strip off uid, if it exists
	  int index = colorDeterminer.indexOf('=');
	  if (index != -1)
	    colorDeterminer = colorDeterminer.substring(index+1);
	  if (!nodeColors.contains(colorDeterminer)) {
	    nodeColors.addElement(colorDeterminer);
	    String colorName = 
	      node.getAttribute(GrappaConstants.COLOR_ATTR).getStringValue();
	    nodeColors.addElement(GrappaColor.getColor(colorName, null));
	  }
	}
      }
    }
    return nodeColors;
  }

  /**
   * Get values of specified attribute; returns vector of unique values,
   * which will be empty if there are no nodes with the attribute.
   * @param attributeName  name of attribute for which to get values
   * @return               values of the attribute; vector of String
   */

  public Vector getValuesOfAttribute(String attributeName) {
    Vector values = new Vector();
    Vector allNodes = vectorOfElements(GrappaConstants.NODE);
    for (int i = 0; i < allNodes.size(); i++) {
      Node node = (Node)allNodes.elementAt(i);
      Attribute attribute = node.getAttribute(attributeName);
      if (attribute != null) {
	String value = attribute.getStringValue();
	if (value != null) 
	  if (!values.contains(value))
	    values.add(value);
      }
    }
    return values;
  }

  /**
   * Get names of all nodes.  
   * @return     the names of all the nodes; vector of String
   */

  public Vector getNames() {
    Vector nodes = vectorOfElements(GrappaConstants.NODE);
    Vector names = new Vector(nodes.size());
    for (int i = 0; i < nodes.size(); i++)
      names.addElement(((Node)nodes.elementAt(i)).getName());
    return names;
  }

  /**
   * Remove specified nodes and collapse remaining edges.
   * Nodes must be specified by name, so that this method works
   * both on the original graph and on filtered copies of the graph.
   * @param nodeNamesToRemove names of nodes to remove; vector of String
   */

  public void removeNamedNodes(Vector nodeNamesToRemove) {
    removeNodesByNameWorker(nodeNamesToRemove, true);
  }

  /**
   * Retain specified nodes by removing all the unspecified nodes
   * and collapsing the remaining edges.
   * Nodes must be specified by name, so that this method works
   * both on the original graph and on filtered copies of the graph.
   * @param nodeNamesToKeep names of nodes to keep; vector of String
   */

  public void keepNamedNodes(Vector nodeNamesToKeep) {
    removeNodesByNameWorker(nodeNamesToKeep, false); 
  }

  /**
   * Remove specified nodes and collapse remaining edges.
   * If "remove" is false, then removes unspecified nodes (inverse operation).
   * For each node being removed, add edges from
   * the tail of all incoming edges to the head of all
   * outgoing edges, then delete the node and all its edges.
   * Nodes must be specified by name, so that this method works
   * both on the original graph and on filtered copies of the graph.
   * @param nodes       nodes to remove or keep; vector of att.grappa.Node
   * @param remove      flag to indicate whether to remove or keep nodes
   */

  private void removeNodesWorker(Vector nodes, boolean remove) {
    Vector allNodes = vectorOfElements(GrappaConstants.NODE);
    for (int i = 0; i < allNodes.size(); i++) {
      Node node = (Node)allNodes.elementAt(i);
      // if this node is specified and we're keeping nodes, skip it
      // if this node is not specified and we're removing nodes, skip it
      if (nodes.contains(node)) {
	if (!remove)
	  continue;
      }	else {
	if (remove)
	  continue;
      }
      deleteEdges(node);
      node.delete();
    }
  }

  /**
   * Remove specified nodes and collapse remaining edges.
   * If "remove" is false, then removes unspecified nodes (inverse operation).
   * For each node being removed, add edges from
   * the tail of all incoming edges to the head of all
   * outgoing edges, then delete the node and all its edges.
   * @param nodes       names of nodes to remove or keep; vector of String
   * @param remove      flag to indicate whether to remove or keep nodes
   */

  private void removeNodesByNameWorker(Vector nodeNames, boolean remove) {
    Vector allNodes = vectorOfElements(GrappaConstants.NODE);
    for (int i = 0; i < allNodes.size(); i++) {
      Node node = (Node)allNodes.elementAt(i);
      String nodeName = node.getName();
      // if this node is specified and we're keeping nodes, skip it
      // if this node is not specified and we're removing nodes, skip it
      if (nodeNames.contains(nodeName)) {
	if (!remove)
	  continue;
      }	else {
	if (remove)
	  continue;
      }
      deleteEdges(node);
      node.delete();
    }
  }

  /**
   * Remove specified nodes and their links.
   * Does NOT reconnect remaining nodes.
   * @param nodes       names of nodes to remove; vector of String
   */

  public void removeNamedNodesAndLinks(Vector nodeNames) {
    Vector allNodes = vectorOfElements(GrappaConstants.NODE);
    for (int i = 0; i < allNodes.size(); i++) {
      Node node = (Node)allNodes.elementAt(i);
      String nodeName = node.getName();
      if (nodeNames.contains(nodeName)) {
	Enumeration inEdgeElements = node.inEdgeElements();
	while (inEdgeElements.hasMoreElements())
	  ((Edge)inEdgeElements.nextElement()).delete();
	Enumeration outEdgeElements = node.outEdgeElements();
	while (outEdgeElements.hasMoreElements())
	  ((Edge)outEdgeElements.nextElement()).delete();
	node.delete();
      }
    }
  }

  /**
   * Show named nodes and their edges.
   * Set INVISIBLE_ATTRIBUTE to false.
   * @param nodeNames names of nodes to show
   */

  public void showNamedNodes(Vector nodeNames) {
    for (int i = 0; i < nodeNames.size(); i++) {
      Node node = findNodeByName((String)nodeNames.elementAt(i));
      node.setAttribute(INVISIBLE_ATTRIBUTE, "false");
      node.visible = true;
      Enumeration inEdgeElements = node.inEdgeElements();
      while (inEdgeElements.hasMoreElements()) {
	Edge edge = (Edge)inEdgeElements.nextElement();
	edge.setAttribute(INVISIBLE_ATTRIBUTE, "false");
	edge.visible = true;
      }
      Enumeration outEdgeElements = node.outEdgeElements();
      while (outEdgeElements.hasMoreElements()) {
	Edge edge = (Edge)outEdgeElements.nextElement();
	edge.setAttribute(INVISIBLE_ATTRIBUTE, "false");
	edge.visible = true;
      }
    }
  }

  /**
   * Hide named nodes and their edges by setting element.visible to false.
   * Create links to reconnect remaining nodes.
   * @param nodeNames names of nodes to hide
   */

  public void hideNamedNodes(Vector nodeNames) {
    for (int i = 0; i < nodeNames.size(); i++) {
      Node node = findNodeByName((String)nodeNames.elementAt(i));
      node.setAttribute(INVISIBLE_ATTRIBUTE, "true");
      node.visible = false;
      hideEdges(node);
    }
  }

  /**
   * Hide edges and draw links to reconnect remaining nodes.
   */

  private void hideEdges(Node node) {
    Enumeration inEdgeElements;
    Enumeration outEdgeElements;
    inEdgeElements = node.inEdgeElements();
    // first, connect in-edges to out-edges using wider lines
    // but only add a new edge if both ends are visible
    GrappaStyle gs = new GrappaStyle(GrappaConstants.EDGE, "line_width(3)");
    while (inEdgeElements.hasMoreElements()) {
      Edge inEdge = (Edge)inEdgeElements.nextElement();
      outEdgeElements = node.outEdgeElements();
      while (outEdgeElements.hasMoreElements()) {
	Edge outEdge = (Edge)outEdgeElements.nextElement();
	Node tailNode = inEdge.getTail();
	Node headNode = outEdge.getHead();
	if (!hasEdge(tailNode, headNode) && tailNode.visible &&
	    headNode.visible) {
	  Edge edge = null;
	  try {
	    edge = new Edge(this, inEdge.getTail(), outEdge.getHead());
	  } catch (Exception e) {
	    System.out.println("Couldn't create edge from: " +
			       inEdge.getTail() + " " +
			       outEdge.getHead() + " " + e);
	    continue;
	  }
	  edge.setAttribute(GrappaConstants.STYLE_ATTR, gs);
	  // mark these edges as special
	  edge.setAttribute(PSEUDO_EDGE, "true");
	}
      }
    }
    // now hide original out edges
    outEdgeElements = node.outEdgeElements();
    while (outEdgeElements.hasMoreElements()) {
      Edge edge = (Edge)outEdgeElements.nextElement();
      edge.setAttribute(INVISIBLE_ATTRIBUTE, "true");
      edge.visible = false;
    }
    // and hide original in edges
    inEdgeElements = node.inEdgeElements();
    while (inEdgeElements.hasMoreElements()) {
      Edge edge = (Edge)inEdgeElements.nextElement();
      edge.setAttribute(INVISIBLE_ATTRIBUTE, "true");
      edge.visible = false;
    }
  }

  /**
   * Delete the edges that were made when nodes were hidden.
   */

  public void deletePseudoEdges() {
    Vector allEdges = vectorOfElements(GrappaConstants.EDGE);
    for (int i = 0; i < allEdges.size(); i++) {
      Edge edge = (Edge)allEdges.elementAt(i);
      Attribute a = edge.getAttribute(PSEUDO_EDGE);
      // note that this attribute only exists if it's a pseudo edge
      // so we don't have to check its value
      if (a != null)
	edge.delete();
    }
  }


  /**
   * Get a node label.
   * @param node the node
   * @return     the label
   */

  public String getLabel(Node node) {
    return (String)node.getAttribute(GrappaConstants.LABEL_ATTR).getValue();
  }

  /**
   * Delete the incoming and outgoing edges of the specified node,
   * and reconnect the remaining edges.
   * For each edge being removed, add edges from
   * the tail of all incoming edges to the head of all
   * outgoing edges, then delete the edge.
   * @param node the node for which to delete the edges.
   */

  private void deleteEdges(Node node) {
    Enumeration inEdgeElements;
    Enumeration outEdgeElements;
    inEdgeElements = node.inEdgeElements();
    // first, connect in-edges to out-edges
    while (inEdgeElements.hasMoreElements()) {
      Edge inEdge = (Edge)inEdgeElements.nextElement();
      outEdgeElements = node.outEdgeElements();
      GrappaStyle gs = new GrappaStyle(GrappaConstants.EDGE, "line_width(3)");
      while (outEdgeElements.hasMoreElements()) {
	Edge outEdge = (Edge)outEdgeElements.nextElement();
	if (!hasEdge(inEdge.getTail(), outEdge.getHead())) {
	  Edge edge = null;
	  try {
	    edge = new Edge(this, inEdge.getTail(), outEdge.getHead());
	  } catch (Exception e) {
	    System.out.println("Couldn't create edge from: " +
			       inEdge.getTail() + " " +
			       outEdge.getHead() + " " + e);
	    continue;
	  }
	  edge.setAttribute(GrappaConstants.STYLE_ATTR, gs);
	}
      }
    }
    // now delete original out edges
    outEdgeElements = node.outEdgeElements();
    while (outEdgeElements.hasMoreElements()) 
      ((Edge)outEdgeElements.nextElement()).delete();
    // and delete original in edges
    inEdgeElements = node.inEdgeElements();
    while (inEdgeElements.hasMoreElements()) 
      ((Edge)inEdgeElements.nextElement()).delete();
  }

  /**
   * Returns true if there is an edge from the tail node to the head node.
   * @param tail the tail node
   * @param head the head node
   * @return     true if there's an edge from tail to head; else false
   */

  private boolean hasEdge(Node tail, Node head) {
    GraphEnumeration ge = elements(GrappaConstants.EDGE);
    while (ge.hasMoreElements()) {
      Edge edge = (Edge)ge.nextGraphElement();
      if (edge.getTail().equals(tail) && edge.getHead().equals(head))
	return true;
    }
    return false;
  }

  /**
   * Get nodes for which the value of the named attribute is
   * less than, equal to, or greater than the given value.
   * For example, if the comparison is NODE_VALUE_LESS_THAN
   * then the comparison that is done is: nodeValue < value
   * @param attributeName the attribute to test
   * @param value         the value to test against
   * @param comparison    NODE_VALUE_EQUAL_TO, LESS_THAN or GREATER_THAN
   * @return              matching nodes; vector of att.grappa.Node
   */

  private Vector getNodesByComparison(String attributeName, long value,
				      int comparison) {
    Vector nodes = new Vector();
    try {
      GraphEnumeration ge = elements(GrappaConstants.NODE);
      while (ge.hasMoreElements()) {
	Element e = ge.nextGraphElement();
	String s = (String)e.getAttributeValue(attributeName);
	if (s == null)
	  continue;
	long nodeValue = Long.parseLong(s);
	if (comparison == NODE_VALUE_EQUAL_TO && nodeValue == value)
	  nodes.addElement(e);
	else if (comparison == NODE_VALUE_LESS_THAN && nodeValue < value)
	  nodes.addElement(e);
	else if (comparison == NODE_VALUE_GREATER_THAN && nodeValue > value)
	  nodes.addElement(e);
      }
    } catch (NumberFormatException e) {
      System.out.println("CSMARTGraph: " + e);
    }
    return nodes;
  }

  /** 
   * Mark nodes for which the attribute value is
   * less than the given value.  Use the specified color to mark.
   * Note that the color parameter must have been added to the
   * grappa color table (using GrappaColor.addColor).
   * @param attributeName the attribute to test
   * @param value         the value to compare against
   * @param color         string describing the color
   */

  public void markNodesLessThan(String attributeName, long value, 
				String color) {
    Vector nodes = getNodesLessThan(attributeName, value);
    mark(nodes, color);
  }

  /** 
   * Mark nodes for which the attribute value is
   * greater than the given value.  Use the specified color to mark.
   * Note that the color parameter must have been added to the
   * grappa color table (using GrappaColor.addColor).
   * @param attributeName the attribute to test
   * @param value         the value to compare against
   * @param color         string describing the color
   */

  public void markNodesGreaterThan(String attributeName, long value,
				   String color) {
    Vector nodes = getNodesGreaterThan(attributeName, value);
    mark(nodes, color);
  }

  /** 
   * Mark nodes for which the attribute value is
   * equal to the given value.  Use the specified color to mark.
   * Note that the color parameter must have been added to the
   * grappa color table (using GrappaColor.addColor).
   * @param attributeName the attribute to test
   * @param value         the value to compare against
   * @param color         string describing the color
   */

  public void markNodesEqualTo(String attributeName, long value,
			       String color) {
    Vector nodes = getNodesEqualTo(attributeName, value);
    mark(nodes, color);
  }

  /** 
   * Return nodes for which the attribute value is
   * less than the given value.
   * @param attributeName the attribute to test
   * @param value         the value to compare against
   * @return              matching nodes; vector of att.grappa.Node
   */

  public Vector getNodesLessThan(String attributeName, long value) {
    return getNodesByComparison(attributeName, value, NODE_VALUE_LESS_THAN);
  }

  /** 
   * Return nodes for which the attribute value is
   * greater than the given value.
   * @param attributeName the attribute to test
   * @param value         the value to compare against
   * @return              matching nodes; vector of att.grappa.Node
   */

  public Vector getNodesGreaterThan(String attributeName, long value) {
    return getNodesByComparison(attributeName, value, NODE_VALUE_GREATER_THAN);
  }

  /** 
   * Return nodes for which the attribute value is
   * equal to the given value.
   * @param attributeName the attribute to test
   * @param value         the value to compare against
   * @return              matching nodes; vector of att.grappa.Node
   */

  public Vector getNodesEqualTo(String attributeName, long value) {
    return getNodesByComparison(attributeName, value, NODE_VALUE_EQUAL_TO);
  }

  /**
   * Display the given set of elements with the given color.
   * Colors edges and the outlines of nodes.
   * Note that the color parameter must have been added to the
   * grappa color table (using GrappaColor.addColor).
   * @param elements  nodes and edges to color; vector of att.grappa.Element
   * @param color     string which describes color
   */

  public void mark(Vector elements, String color) {
    for (int i = 0; i < elements.size(); i++) {
      Element e = (Element)elements.elementAt(i);
      if (e instanceof Edge)
	e.setAttribute(GrappaConstants.COLOR_ATTR, color);
      else {
	Node node = (Node)e;
	GrappaStyle gs =
	  new GrappaStyle(GrappaConstants.NODE, 
			  "filled(true),line_width(3),line_color(" +
			  color + ")");
	node.setAttribute(GrappaConstants.STYLE_ATTR, gs);
      }
    }
    markedElements.addAll(elements);
    resync();
    repaint();
  }

  /**
   * Clear currentSelection (the vector of selected nodes)
   * and clear the highlighting of selected nodes.
   */

  public void clearSelection() {
    GraphEnumeration ge = elements();
    while (ge.hasMoreElements()) {
      Element e = ge.nextGraphElement();
      GrappaSupport.setHighlight(e, GrappaSupport.SELECTION_MASK,
				 GrappaSupport.HIGHLIGHT_OFF);
    }
    currentSelection = null;
    repaint();
  }

  /**
   * Select (highlight) the given set of elements and de-select 
   * all others.
   * @param elements the elements to select; vector of att.grappa.Element
   */

  public void select(Vector elements) {
    clearSelection(); 
    resetColors();
    currentSelection = new Vector();
    for (int i = 0; i < elements.size(); i++) {
      Element e = (Element)elements.elementAt(i);
      GrappaSupport.setHighlight(e, GrappaSupport.SELECTION_MASK,
				 GrappaSupport.HIGHLIGHT_ON);
      ((Vector)currentSelection).addElement(e);
    }
  }

  /**
   * Select (highlight) the given element(node or edge) and de-select others.
   * @param element the element to select
   */

  public void select(Element e) {
    clearSelection(); 
    resetColors();
    currentSelection = new Vector();
    GrappaSupport.setHighlight(e, GrappaSupport.SELECTION_MASK,
			       GrappaSupport.HIGHLIGHT_ON);
    ((Vector)currentSelection).addElement(e);
  }    

  /**
   * Select all nodes and edges.
   */

  public void selectAll() {
    select(vectorOfElements(GrappaConstants.NODE +
			    GrappaConstants.EDGE));
  }

  /**
   * Resets colors used by "mark" methods;
   * does not affect highlighting (selecting).
   * Resets edges to black; resets node fill and outline color
   * to the original color, as defined by the COLOR_ATTR attribute.
   */

  public void resetColors() {
    for (int i = 0; i < markedElements.size(); i++) {
      Element element = (Element)markedElements.elementAt(i);
      if (element instanceof Edge) {
	element.setAttribute(GrappaConstants.COLOR_ATTR, "black");
      } else if (element instanceof Node) {
	Node node = (Node)element;
	if (!node.visible)
	  continue;
	Attribute a = node.getAttribute(COLOR_ATTR);
	if (a == null) {
	  System.out.println("CSMARTGraph: No default color for node");
	  continue;
	}
	GrappaStyle gs = 
	  new GrappaStyle(GrappaConstants.NODE, "filled(true),line_color(" +
			  a.getStringValue() + ")");
	node.setAttribute(GrappaConstants.STYLE_ATTR, gs);
      }
    }
    markedElements.removeAllElements();
    resync();
    repaint();
  }

  /**
   * Create a label for a node from a UID string.
   * Creates a label of the form: <clustername><newline><UID-tail>
   * where clustername is the UIDString before the first backslash
   * and UID-tail is the last 5 characters of the UIDString
   * @param UIDString UID
   * @return          label
   */

  private String UIDToLabel(String UIDString) {
    String clusterId = UIDString.substring(0, UIDString.indexOf('/'));
    String tail = UIDString.substring(UIDString.length()-5,
				      UIDString.length());
    return clusterId + "\\n" + tail;
  }

  /**
   * Delete all nodes and edges with the INVISIBLE_ATTRIBUTE.
   */

  public void removeInvisibleElements() {
    Vector elements = 
      vectorOfElements(GrappaConstants.NODE + GrappaConstants.EDGE);
    for (int i = 0; i < elements.size(); i++) {
      Element element = (Element)elements.elementAt(i);
      String s = (String)element.getAttributeValue(INVISIBLE_ATTRIBUTE);
      if (s != null)
	if (s.equals("true"))
	  element.delete();
    }
  }

  /**
   * Move all the event nodes and edges from subgraphs
   * to this graph, and delete the subgraphs.
   */

  public void removeSubgraphs() {
    Vector subgraphs = vectorOfElements(GrappaConstants.SUBGRAPH);
    for (int i = 0; i < subgraphs.size(); i++) {
      Subgraph subgraph = (Subgraph)subgraphs.elementAt(i);
      Vector nodes = vectorOfElements(GrappaConstants.NODE);
      for (int j = 0; j < nodes.size(); j++) {
	Node node = (Node)nodes.elementAt(j);
	subgraph.removeNode(node.getName());
	addNode(node);
      }
      Vector edges = vectorOfElements(GrappaConstants.EDGE);
      for (int j = 0; j < edges.size(); j++) {
	Edge edge = (Edge)edges.elementAt(j);
	subgraph.removeEdge(edge.getName());
	addEdge(edge);
      }
      subgraph.delete();
    }
  }

  /**
   * Create the specified number of subgraphs.
   * Creates an invisible node in each subgraph in order to enforce ranking.
   * @param numberofSubgraphs number of subgraphs to create
   * @return                  an array of the subgraphs created
   */

  public Subgraph[] createRankedSubgraphs(int numberOfSubgraphs) {
    Node lastNode = null;
    Subgraph[] subgraphs = new Subgraph[numberOfSubgraphs];
    for (int i = 0; i < numberOfSubgraphs; i++) {
      subgraphs[i] = new Subgraph(this);
      subgraphs[i].setAttribute(RANK_ATTRIBUTE, RANK_SAME);
      Node node = new Node(subgraphs[i], String.valueOf(i));
      node.setAttribute(INVISIBLE_ATTRIBUTE, "true");
      node.visible = false;
      if (lastNode != null) {
	Edge edge = null;
	try {
	  edge = new Edge(this, lastNode, node);
	} catch (RuntimeException e) {
	    System.out.println("Couldn't create edge from: " +
			       lastNode + " " +
			       node + " " + e);
	    continue;
	}
 	edge.setAttribute(INVISIBLE_ATTRIBUTE, "true");
	edge.visible = false;
      }
      lastNode = node;
    }
    return subgraphs;
  }

  /**
   * Remove node from this graph and add it to the specified subgraph.
   * After placing nodes in subgraphs, call updateGraph to re-layout and
   * redraw the graph.
   * @param node     the node to remove
   * @param subgraph the subgraph to which to add the node
   */

  public void addNodeToSubgraph(Node node, Subgraph subgraph) {
    removeNode(node.getName());
    subgraph.addNode(node);
  }

  /**
   * Re-layout and repaint the graph.
   */

  public void updateGraph() {
    doLayout();
    repaint();
  }

  /**
   * Layout the graph (i.e. determine the positions of nodes and edges).
   * Invoke graph.printGraph to print an ascii representation of the graph,
   * which is saved in the dot file (specified by dotFileName)
   * in the directory specified by org.cougaar.install.
   * Invoke the dot tool (dot.exe)
   * on the ascii representation to
   * produce another ascii representation with node and edge positions.
   * Parse the results of the dot tool, which produces a new graph
   * with positions added.
   */

  public void doLayout() {
    //    System.out.println("CSMARTGraph: Starting layout...");

    // if we have a mapping of UIDs to nodes then we can minimize the
    // information sent to the layout engine
    boolean minimizeGraph = true;
    if (UIDToNode.size() == 0)
      minimizeGraph = false;

    // write the graph to a dot file
    String dotPathname = System.getProperty("org.cougaar.install.path");

    // use static dotFileName
    if (dotPathname != null)
      dotPathname = dotPathname + File.separatorChar + dotFileName;
    else
      dotPathname = dotFileName;

    // Create a file handle for the dot file
    // This will be used later to get the size of the file
    File dotFile = null;
    try {
      dotFile = new File(dotPathname);
    } catch (NullPointerException e) {
      System.out.println("CSMARTGraph: Could not open file handle for " + dotPathname);
    }
    
    // before writing the graph, save the edges in a hash table
    // so that we can merge the results of the layout engine
    // note that the nodes were saved in a hash table at creation
    if (minimizeGraph) {
      UIDToEdge = new Hashtable();
      Vector edges = vectorOfElements(GrappaConstants.EDGE);
      for (int i = 0; i < edges.size(); i++) {
	Edge edge = (Edge)edges.elementAt(i);
	UIDToEdge.put(edge.getTail().getName() + edge.getHead().getName(), edge);
      }
    }
    FileOutputStream fos = null;
    //    System.out.println("CSMARTGraph: Writing: " + dotPathname + 
    //		       " at: " + System.currentTimeMillis()/1000);
    try {
      fos = new FileOutputStream(dotFile); //AMH - use the File handle
      PrintWriter pw = new PrintWriter(fos);
      // only write attributes required for layout
      if (minimizeGraph)
	Grappa.elementPrintLayoutAttributesOnly = true;
      printGraph(pw);
      if (minimizeGraph)
	Grappa.elementPrintLayoutAttributesOnly = false;
      pw.close(); // AMH - close it to flush the data, garbage collect
    } catch (FileNotFoundException e) {
      System.out.println("CSMARTGraph: Could not write to file:" + dotPathname + " file not found exception: " + e);
    } catch (SecurityException se) {
      System.out.println("CSMARTGraph: Could not write to file:" + dotPathname + " security exception: " + se);
    } catch (IOException ie) {
      System.out.println("CSMARTGraph: Could not write to file:" + dotPathname + " I/O exception: " + ie);
    }
    //    System.out.println("Finished writing: " + dotPathname + 
    //		       " at: " + System.currentTimeMillis()/1000);

    // Now run dot.exe on the dot file
    // Take the output of dot.exe, and run it through the parser
    // Then build the resulting graph, with buildShapes();
    layoutDotFile(dotPathname, minimizeGraph);
    
    // hide nodes and edges with the INVISIBLE_ATTRIBUTE set to true
    hideInvisibleElements();
  } // end of doLayout()

  /**
   * Element.visible is NOT preserved across the "doLayout" method
   * hence, visibility is indicated by the INVISIBLE attribute and doLayout
   * calls this method after layout;
   * if the INVISIBLE attribute is present and set to "true",
   * then element.visible is set false
   */

  private void hideInvisibleElements() {
    Vector elements = 
      vectorOfElements(GrappaConstants.NODE + GrappaConstants.EDGE);
    for (int i = 0; i < elements.size(); i++) {
      Element element = (Element)elements.elementAt(i);
      String s = (String)element.getAttributeValue(INVISIBLE_ATTRIBUTE);
      if (s != null)
	if (s.equals("true"))
	  element.visible = false;
    }
  }


  /**
   * Specialized Reader to read the output from the Grappa dot.exe process
   * Add a <code>Grappa.NEW_LINE</code> at the end of every line,
   * except the last line.
   *
   * @see BufferedReader
   */
  private class GrappaDotReader extends BufferedReader {
    boolean firstChar = true;

    public GrappaDotReader(Reader in) {
      super(in);
    }
    public GrappaDotReader(Reader in, int sz) throws IllegalArgumentException {
      super(in, sz);
    }

    public int read() throws IOException {
      int c = super.read();
      if (firstChar) {
	firstChar = false;
	//	System.out.println("GrappaDotReader read first char at: " +
	//			   System.currentTimeMillis()/1000);
      }
      //      if (c == -1)
      //	System.out.println("GrappaDotReader read last char at: " +
      //			   System.currentTimeMillis()/1000);
      return c;
    }

    public String readLine() throws IOException {
      //      System.out.println("CSMARTGraph: GrappDotReader: readLine()");
      String ans = super.readLine();
      if (ans == "}" || ans == "}\r") {
	return ans;
      } else {
	return ans + Grappa.NEW_LINE;
      }
    } // end of readLine()
  } // end of GrappaDotReader
    

  /**
   * Save the graph in a dot file.
   * Saves the color table mapping, which is COLOR_DETERMINER to Java Color,
   * as defined in the UIProperties object.
   * Note that if the graph is generated live (not read from a file), 
   * then COLOR_DETERMINER is simply the name of an agent or community.
   * This "simple" naming scheme is used so that metric charts
   * can share the same color mapping.
   * When the graph is saved, the COLOR_DETERMINERs are made "unique".
   * (However, subsequently saves of the graph or subsets of the graph
   * will use the same COLOR_DETERMINERs and color table; this is ok,
   * because the colors are the same.)
   * The graph ColorTable attribute only exists if the graph is read
   * from a file or has been previously saved, and in these cases the
   * COLOR_DETERMINERs are already unique, and are not modified.
   * Thus, the color scheme used in the graph is saved with the file.
   * @param outputFile the file in which to save the graph
   */

  private void saveGraph(File outputFile) {
    if (outputFile == null)
      return;
    StringBuffer colorTable = new StringBuffer(500);
    // first, save colors as real colors
    Attribute ct = getAttribute(COLOR_TABLE);
    // if there's no color table, then give the COLOR_DETERMINER attributes
    // unique ids before storing them in a color table
    String graphUID = null;
    if (ct == null) {
      InetAddress localHost = null;
      try {
	localHost = InetAddress.getLocalHost();
      } catch (Exception e) {
	System.out.println("CSMARTGraph: Can't get local host: " + e);
      }
      graphUID = localHost.toString() + "/" + (new UID()).toString();
    }
    Hashtable colorsWritten = new Hashtable();
    GraphEnumeration ge = elements(GrappaConstants.NODE);
    while (ge.hasMoreElements()) {
      Node node = (Node)ge.nextGraphElement();
      // save mapping of COLOR_DETERMINER to Color from ui.properties
      Attribute a = node.getAttribute(COLOR_DETERMINER);
      if (a == null)
	continue;
      String colorName = a.getStringValue();
      Color c = properties.getClusterColor(colorName);
      //      System.out.println("CSMARTGraph: color for: " + colorName + " is: " + c);
      if (graphUID != null) {
	colorName = graphUID + "=" + colorName;
	node.setAttribute(COLOR_DETERMINER, colorName);
      }
      if (colorsWritten.get(colorName) != null)
	continue; // write each mapping only once
      colorsWritten.put(colorName, c);
      colorTable.append(colorName);
      colorTable.append("+");
      colorTable.append(Integer.toString(c.getRed()));
      colorTable.append(",");
      colorTable.append(Integer.toString(c.getGreen()));
      colorTable.append(",");
      colorTable.append(Integer.toString(c.getBlue()));
      colorTable.append(";");
    }
    // save color table
    setAttribute(COLOR_TABLE, colorTable.toString());
    // next, save all edges as black
    ge = elements(GrappaConstants.EDGE);
    while (ge.hasMoreElements()) {
      Element e = ge.nextGraphElement();
      e.setAttribute(GrappaConstants.COLOR_ATTR, "black");
    }
    String pathname = outputFile.getPath();
    String extension = "";
    int i = pathname.lastIndexOf('.');
    if (i > 0 && i < pathname.length()-1)
      extension = pathname.substring(i+1).toLowerCase();

    if (extension.length() == 0 || !(extension.equals("dot"))) {
      pathname = pathname + ".dot";
      outputFile = new File(pathname);
      extension = "dot";
    }
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(outputFile);
      PrintWriter pw = new PrintWriter(fos);
      printGraph(pw);
      fos.flush();
      fos.close();
    } catch (Exception e) {
      System.out.println("CSMARTGraph: Could not write to file:" + e);
    }
  }

  /**
   * Override grappa printGraph method to quote all attributes
   * whose attribute type is String.
   */

  public void printGraph(PrintWriter pw) {
    GraphEnumeration ge = elements();
    while (ge.hasMoreElements()) {
      Element element = (Element)ge.nextGraphElement();
      Enumeration attributes = element.getAttributePairs();
      while (attributes.hasMoreElements()) {
	Attribute attr = (Attribute)attributes.nextElement();
	if (attr.getAttributeType() == GrappaConstants.STRING_TYPE) {
	  String value = (String)attr.getValue();
	  if (value != null && value.length() > 0 && 
	      Character.isDigit(value.charAt(0))) 
	    attr.setValue("\"" + value + "\"");
	}
      }
    }
    super.printGraph(pw); // now print the graph
  }

  /**
   * When read graph strip leading and trailing quotes from
   * attributes whose attribute type is String.
   */

  private static void stripAttributeQuotes(Graph graph) {
    GraphEnumeration ge = graph.elements();
    while (ge.hasMoreElements()) {
      Element element = (Element)ge.nextGraphElement();
      Enumeration attributes = element.getAttributePairs();
      while (attributes.hasMoreElements()) {
	Attribute attr = (Attribute)attributes.nextElement();
	if (attr.getAttributeType() == GrappaConstants.STRING_TYPE) {
	  String value = (String)attr.getValue();
	  if (value != null && value.length() > 0 && 
	      value.charAt(0) == '\"' && 
	      value.charAt(value.length()-1) == '\"') 
	    attr.setValue(value.substring(1, value.length()-1));
	}
      }
    }
  }

  /**
   * Save in dot file from which graph was read
   * or query user for file to save in.
   */

  public void saveGraph() {
    if (file != null)
      saveGraph(file);
    else 
      saveAsGraph();
  }

  /**
   * Returns true if the graph has been saved to a file.
   * Can be used to enable/disable "Save As" menus.
   * @return       returns true if graph has an output file associated with it
   */

  public boolean hasOutputFile() {
    return file != null;
  }

  /**
   * Query user for file in which to save graph and save it.
   */

  public void saveAsGraph() {
    JFileChooser jfc = new JFileChooser(System.getProperty("org.cougaar.install.path"));
    ExtensionFileFilter filter;
    String[] filters = { "dot" };
    filter = new ExtensionFileFilter(filters, "dot files");
    jfc.addChoosableFileFilter(filter);
    if (jfc.showSaveDialog(null) == JFileChooser.CANCEL_OPTION)
      return;
    File file = jfc.getSelectedFile();
    if (file == null)
      System.out.println("CSMARTGraph: file is null");
    saveGraph(file);
  }

  /**
   * Return true if a node is selected and false otherwise.
   * @return true if at least one node is selected
   */

  public boolean isNodeSelected() {
    Vector elements = getSelectedElements();
    for (int i = 0; i < elements.size(); i++) {
      Element element = (Element)elements.elementAt(i);
      if (element.getType() == GrappaConstants.NODE)
	  return true;
    }
    return false;
  }


  /**
   * Create a graph from a dot file.
   * Merges results of layout with current graph.
   * Invokes dot.exe on the file.
   * The hashtable is used to merge the results of the layout
   * into the current nodes.
   * @param dotPathname path name of dot file
   * @param the hashtable mapping UID to node in the current graph
   */
  private void layoutDotFile(String dotPathname, boolean minimizeGraph) {
    // Create a file handle for the dot file
    // This will be used later to get the size of the file
    File dotFile = null;
    try {
      dotFile = new File(dotPathname);
    } catch (NullPointerException e) {
      System.out.println("CSMARTGraph: Could not open file handle for " + dotPathname);
    }
    
    // This is the size of the dot file, use this
    // for initializing the Buffer for reading the dot.exe process output
    long dotFileSize = dotFile.length();

    // done with dotFile File handle

    //    System.out.println("CSMARTGraph: Got dot file of size " + dotFileSize +
    //		       " at: " + System.currentTimeMillis()/1000);
    
    // run the dot file through the dot utility to produce a directed graph
    //    String command = "dot.exe " + dotPathname;
    if (dotExecutable == null)
      setupDotPath();
    String command = dotExecutable + ' ' + dotPathname;
    
    //    System.out.println("Starting: " + command + " at: " + 
    //		       System.currentTimeMillis()/1000);
    Process proc = null;
    try {
      proc = Runtime.getRuntime().exec(command);
    } catch(Exception ex) {
      System.out.println("Exception while executing: " + 
			 command + " " + ex.getMessage());
      ex.printStackTrace(System.err);
      proc = null;
    }

    // get the input stream of the dot process from which 
    // we read the dot-processed file
    InputStream fromFilterRaw = null;
    try {
      if (proc == null) {
	throw new Exception("proc is null");
      } else {
	fromFilterRaw = proc.getInputStream();
      }
    } catch(Exception e) {
      System.out.println("CSMARTGraph: Couldn't get from filter: " + e);
    }

    // Use a special BufferedReader which does the newlines
    // thing for us.  By using this Reader, we can avoid
    // creating the large StringBuffer altogether
    // We'll just pass this reader directly into the Parser
    BufferedReader fromFilter = 
      new GrappaDotReader(new InputStreamReader(fromFilterRaw));

    // now invoke the parser on the dot-processed file
    // AMH - pass in the original BufferedReader that reads
    // directly from the dot.exe process
    // create a new graph to get the results of the layout
    Parser program = null;
    Graph newGraph = null;
    if (minimizeGraph) {
      newGraph = new Graph("Layout", true, true);
      program = new Parser(fromFilter, getErrorWriter(), newGraph);
    } else
      program = new Parser(fromFilter, getErrorWriter(), this);

    // garbage collect before parsing
    Runtime runtime = Runtime.getRuntime();
    //    System.out.println("Free memory: " + runtime.freeMemory());
    runtime.gc();
    //    System.out.println("After gc; Free memory: " + runtime.freeMemory());
    //    System.out.println("Invoking parser at: " + System.currentTimeMillis()/1000 + "...");
    try {
      program.parse();
    } catch(Exception ex) {
      System.out.println("CSMARTGraph: parser exception: " + ex.getMessage());
      ex.printStackTrace();
    }
    //    System.out.println("Parser finished at: " + System.currentTimeMillis()/1000);
    // AMH: Close our special Buffered Reader
    try {
      fromFilter.close();
    } catch (IOException io) {
      System.out.println("Error closing special reader");
    }
    stripAttributeQuotes(this);
    if (!minimizeGraph) {
      buildShapes();
      return;
    }


    // copy the results of the layout into this graph
    // get position and size information for nodes
    Vector newNodes = newGraph.vectorOfElements(GrappaConstants.NODE);
    for (int i = 0; i < newNodes.size(); i++) {
      Node newNode = (Node)newNodes.elementAt(i);
      Node node = (Node)UIDToNode.get(newNode.getName());
      if (node == null) {
	System.out.println("CSMARTGraph: WARNING: node not found: " + newNode.getName());
      } else {
	node.setAttribute(GrappaConstants.POS_ATTR,
			  newNode.getAttributeValue(GrappaConstants.POS_ATTR));
	node.setAttribute(GrappaConstants.WIDTH_ATTR,
			  newNode.getAttributeValue(GrappaConstants.WIDTH_ATTR));
	node.setAttribute(GrappaConstants.HEIGHT_ATTR,
			  newNode.getAttributeValue(GrappaConstants.HEIGHT_ATTR));
      }
    }
    // get position information for edges
    Vector newEdges = newGraph.vectorOfElements(GrappaConstants.EDGE);
    for (int i = 0; i < newEdges.size(); i++) {
      Edge newEdge = (Edge)newEdges.elementAt(i);
      String name = newEdge.getTail().getName() + newEdge.getHead().getName();
      Edge edge = (Edge)UIDToEdge.get(name);
      if (edge == null)
	System.out.println("CSMARTGraph: WARNING: edge not found: " + name);
      else
	edge.setAttribute(GrappaConstants.POS_ATTR,
			  newEdge.getAttributeValue(GrappaConstants.POS_ATTR));
    }
    // and build the resultant graph
    buildShapes();
    //    System.out.println("Finished building grappa nodes at: " + System.currentTimeMillis()/1000);
  } // end of layoutDotFile

} // end of CSMARTGraph.java

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

package org.cougaar.tools.csmart.ui.monitor.society;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.TableModel;
import att.grappa.*;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTFrame;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTGraph;
import org.cougaar.tools.csmart.ui.monitor.generic.LegendComboBoxModel;
import org.cougaar.tools.csmart.ui.monitor.generic.LegendRenderer;
import org.cougaar.tools.csmart.ui.monitor.generic.UIProperties;
import org.cougaar.tools.csmart.ui.util.NamedFrame;

/**
 * A frame to display agents.
 * The nodes in the graph are agents.
 */

public class ULSocietyFrame extends CSMARTFrame {
  static final String LEGEND_MENU_ITEM = "Legend";
  static final String RELATIONSHIPS_MENU = "Relationships";
  static final String COLOR_SUPERIOR = "colorSuperior";
  static final String COLOR_SUBORDINATE = "colorSubordinate";
  static final String COLOR_PROVIDER = "colorProvider";
  static final String COLOR_CUSTOMER = "colorCustomer";
  // standard relationships
  static final String SUPERIOR = "Superior";
  static final String SUBORDINATE = "Subordinate";
  static final String PROVIDER = "Provider";
  static final String CUSTOMER = "Customer";

  JDialog legendDialog;
  private static UIProperties properties = null;

  /**
   * Create a frame for the specified graph.
   * @param title  title for the frame
   * @param graph  graph to display in the frame
   */

  public ULSocietyFrame(String title, CSMARTGraph graph) {
    super(title, graph);
    setProperties();
  }

  /**
   * Add menu items specific to Event objects and add this
   * as the handler.
   */

  public void customize() {
    insertMenuItem(CSMARTFrame.VIEW_MENU, LEGEND_MENU_ITEM, 2, this);
    Vector names = getRelationshipNames();
    Collections.sort(names);
    // show single relationship, superior,subordinate,customer,provider, etc.
    int j = 0;
    Vector dualRelationships = new Vector();
    for (int i = 0; i < names.size(); i++) {
      String name = (String)names.elementAt(i);
      insertMenuItem(CSMARTFrame.SHOW_MENU, name, j++, this);
      if (name.endsWith(SUPERIOR))
	name = name + "/" + SUBORDINATE;
      else if (name.endsWith(SUBORDINATE))
	name = name.substring(0, name.indexOf(SUBORDINATE)) +
	  SUPERIOR + "/" + SUBORDINATE;
      else if (name.endsWith(CUSTOMER))
	name = name + "/" + PROVIDER;
      else if (name.endsWith(PROVIDER))
	name = name.substring(0, name.indexOf(PROVIDER)) +
	  CUSTOMER + "/" + PROVIDER;
      if (!dualRelationships.contains(name))
	dualRelationships.add(name);
    }
    Collections.sort(dualRelationships);
    JMenu relationshipsMenu = new JMenu(RELATIONSHIPS_MENU);
    for (int i = 0; i < dualRelationships.size(); i++) {
      JMenuItem menuItem = 
	new JMenuItem((String)dualRelationships.elementAt(i));
      menuItem.addActionListener(this);
      relationshipsMenu.insert(menuItem, i);
    }
    insertMenu(CSMARTFrame.FILE_MENU, relationshipsMenu, 1);
  }

  private void setProperties() {
    if (properties != null)
      return; 
    properties = CSMARTGraph.getProperties();
    GrappaColor.addColor(COLOR_SUPERIOR, properties.getColorCauses());
    GrappaColor.addColor(COLOR_SUBORDINATE, properties.getColorEffects());
    GrappaColor.addColor(COLOR_PROVIDER, properties.getColorCauses());
    GrappaColor.addColor(COLOR_CUSTOMER, properties.getColorEffects());
  }

  /**
   * Enable/disable menu items that are used when the event graph
   * has selected nodes.
   * Can be overridden to enable/disable additional customized menu items.
   * @param enable true to enable menu items; false to disable
   */

  public void enableSelectedMenus(boolean enable) {
    super.enableSelectedMenus(enable);
  }

  /**
   * Get table model to display the attributes of this node.
   * @param node the node for which to return a table model
   * @return     table model to use to display attributes for this node
   */

  public TableModel getAttributeTableModel(Node node) {
    return new ULSocietyTableModel(node);
  }

  /**
   * Get relationship names from node attributes; strip off leading
   * unique string of the form: Related_to_n_
   */

  private Vector getRelationshipNames() {
    Vector results = new Vector();
    Vector nodes = graph.vectorOfElements(GrappaConstants.NODE);
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      Enumeration attributes = node.getAttributePairs();
      while (attributes.hasMoreElements()) {
	Attribute attr = (Attribute)attributes.nextElement();
	String name = attr.getName();
	if (name.startsWith(PropertyNames.ORGANIZATION_RELATED_TO)) {
	  name = name.substring(name.lastIndexOf('_')+1);
	  if (!results.contains(name))
	    results.add(name);
	}
      }
    }
    return results;
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
      new ULSocietyFrame(NamedFrame.SOCIETY, newGraph);
      return;
    }

    if (command.equals(LEGEND_MENU_ITEM)) {
      createLegend();
      return;
    }

    // show superior/subordinate relationships only in a new graph
    // have to search for these before single relationship commands
    if (command.endsWith("/" + SUBORDINATE)) {
      String rel1 = command.substring(0, command.indexOf("/" + SUBORDINATE));
      String rel2 = command.substring(0, command.indexOf(SUPERIOR + "/" +
					      SUBORDINATE)) + SUBORDINATE;
      showDualRelationship(rel1, rel2);
      return;
    }

    // show customer/provider relationships only in a new graph
    // have to search for these before single relationship commands
    if (command.endsWith("/" + PROVIDER)) {
      String rel1 = command.substring(0, command.indexOf("/" + PROVIDER));
      String rel2 = command.substring(0, command.indexOf(CUSTOMER + "/" +
					      PROVIDER)) + PROVIDER;
      showDualRelationship(rel1, rel2);
      return;
    }

    // mark superior/subordinate/provider/customer relationships 
    if (command.endsWith(SUPERIOR)) {
      markRelatedElements(command, COLOR_SUPERIOR);
      return;
    }

    if (command.endsWith(SUBORDINATE)) {
      markRelatedElements(command, COLOR_SUBORDINATE);
      return;
    }

    if (command.endsWith(PROVIDER)) {
      markRelatedElements(command, COLOR_PROVIDER);
      return;
    }

    if (command.endsWith(CUSTOMER)) {
      markRelatedElements(command, COLOR_CUSTOMER);
      return;
    }

    super.actionPerformed(evt);
  }

  /**
   * Mark (color) nodes and edges that are related to 
   * each selected node by the specified relationship.
   */

  private void markRelatedElements(String relationship, String color) {
    Vector selectedElements = graph.getSelectedElements();
    if (selectedElements == null)
      return;
    graph.resetColors();
    Vector relatedElements = getRelatedElements(selectedElements,
						relationship);
    graph.mark(relatedElements, color);
  }

  /**
   * Get dual relationship, i.e. superior/subordinate or customer/provider
   * for selected elements and create new graph.
   */

  private void showDualRelationship(String rel1, String rel2) {
    Vector selectedElements = graph.getSelectedElements();
    if (selectedElements == null)
      return;
    Vector relatedElements = getRelatedElements(selectedElements, rel1);
    Vector addl = getRelatedElements(selectedElements, rel2);
    for (int i = 0; i < addl.size(); i++) 
      if (!relatedElements.contains(addl.elementAt(i)))
	relatedElements.add(addl.elementAt(i));
    if (relatedElements.size() == 0)
      return;
    graph.select(relatedElements);
    CSMARTGraph newGraph = graph.newGraphWithSelectedElementsOnly();
    new ULSocietyFrame(rel1 + "/" + rel2, newGraph);
  }

  /**
   * Recursively get nodes and edges related to the specified
   * nodes by relationship.  Adds in the new nodes and edges to the
   * vector of elements, and returns this vector.
   * Note that this functionality depends on relationship values ending in:
   * Superior, Subordinate, Customer, or Provider.
   */

  private Vector getRelatedElements(Vector elements, String relationship) {
    for (int i = 0; i < elements.size(); i++) {
      Element element = (Element)elements.elementAt(i);
      // remove any subgraphs (in particular the whole graph)
      if (element instanceof Subgraph) {
	elements.remove(i--);
	continue;
      }
      // remove edges, as we only want the ones that match relationship
      if (element instanceof Edge) {
	elements.remove(i--);
	continue;
      }
    }
    for (int i = 0; i < elements.size(); i++) {
      Element element = (Element)elements.elementAt(i);
      if (!(element instanceof Node))
	continue;
      Node node = (Node)element;
      Vector otherNodeNames = 
	getAttributeValuesWithSuffix(node, relationship);
      Enumeration edges = node.edgeElements();
      while (edges.hasMoreElements()) {
	Edge edge = (Edge)edges.nextElement();
	if (edge.getHead().equals(node)) {
	  if (otherNodeNames.contains(getOrganizationName(edge.getTail()))) {
	    if (!elements.contains(edge))
	      elements.add(edge);
	    Node tail = edge.getTail();
	    if (!elements.contains(tail))
	      elements.add(tail);
	  }
	} else if (edge.getTail().equals(node)) {
	  if (otherNodeNames.contains(getOrganizationName(edge.getHead()))) {
	    if (!elements.contains(edge)) 
	      elements.add(edge);
	    Node head = edge.getHead();
	    if (!elements.contains(head)) 
	      elements.add(head);
	  }
	}
      }
    }
    return elements;
  }

  /**
   * Get organization name of a node.  This is compared to the
   * value of the relationship attribute to "find" the other node
   * in a specified relationship.
   */

  private String getOrganizationName(Node node) {
    return (String)node.getAttributeValue(PropertyNames.ORGANIZATION_KEY_NAME);
  }

  /**
   * Get values of the node's attributes whose names
   * end with the given suffix.
   * Returns vector of Objects.
   */

  private Vector getAttributeValuesWithSuffix(Node node, String suffix) {
    Enumeration attributes = node.getAttributePairs();
    Vector results = new Vector();
    while (attributes.hasMoreElements()) {
      Attribute attr = (Attribute)attributes.nextElement();
      if (attr.getName().endsWith(suffix))
	results.add(attr.getValue());
    }
    return results;
  }

  /**
   * Create a legend; which is a non-modal dialog.
   * Called in response to selecting "display legend" from a menu.
   */

  private void createLegend() {
    if (legendDialog != null) {
      legendDialog.setVisible(true);
      return;
    }
    legendDialog = new JDialog(this, "Legend", false);

    JPanel clusterPanel = new JPanel();
    clusterPanel.setBorder(new TitledBorder("Society"));
    clusterPanel.setLayout(new BoxLayout(clusterPanel, BoxLayout.Y_AXIS));
    int x = 0;
    int y = 0;
    JComboBox cb = 
      new JComboBox(new LegendComboBoxModel(graph.getNodeColors()));
    cb.setRenderer(new LegendRenderer());
    cb.setSelectedIndex(0);
    clusterPanel.add(cb);

    JPanel buttonPanel = new JPanel();
    JButton OKButton = new JButton("OK");
    OKButton.setFocusPainted(false);
    OKButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	legendDialog.setVisible(false);
      }
    });
    buttonPanel.add(OKButton);
    legendDialog.getContentPane().setLayout(new BorderLayout());
    legendDialog.getContentPane().add(clusterPanel, BorderLayout.CENTER);
    legendDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    legendDialog.pack();
    legendDialog.setSize(legendDialog.getWidth(),legendDialog.getHeight()+100);
    legendDialog.show();
  }

}











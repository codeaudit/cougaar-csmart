/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.society;

import att.grappa.*;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTFrame;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTGraph;
import org.cougaar.tools.csmart.ui.monitor.generic.LegendComboBoxModel;
import org.cougaar.tools.csmart.ui.monitor.generic.LegendRenderer;
import org.cougaar.tools.csmart.ui.monitor.generic.UIProperties;
import org.cougaar.tools.csmart.ui.util.NamedFrame;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

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
    // get all nodes related to the selected nodes
    // recursively by the first relationship
    Vector relatedElements = getRelatedElements(selectedElements, rel1);
    // get all nodes related to the selected nodes
    // recursively by the second relationship
    Vector addl = getRelatedElements(selectedElements, rel2);
    for (int i = 0; i < addl.size(); i++) 
      if (!relatedElements.contains(addl.elementAt(i)))
	relatedElements.add(addl.elementAt(i));
    // remove all nodes with no relationships
    for (int i = 0; i < relatedElements.size(); i++) {
      Element element = (Element)relatedElements.elementAt(i);
      if (element instanceof Node) {
        Enumeration edges = ((Node)element).edgeElements();
        boolean hasEdges = false;
        while (edges.hasMoreElements()) {
          if (relatedElements.contains(edges.nextElement())) {
            hasEdges = true;
            break;
          }
        }
        if (!hasEdges)
          relatedElements.remove(i--);
      }
    }
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
    Vector myElements = (Vector)elements.clone();
    for (int i = 0; i < myElements.size(); i++) {
      Element element = (Element)myElements.elementAt(i);
      // remove any subgraphs (in particular the whole graph)
      if (element instanceof Subgraph) {
	myElements.remove(i--);
	continue;
      }
      // remove edges, as we only want the ones that match relationship
      if (element instanceof Edge) {
	myElements.remove(i--);
	continue;
      }
    }
    for (int i = 0; i < myElements.size(); i++) {
      Element element = (Element)myElements.elementAt(i);
      if (!(element instanceof Node))
	continue;
      Node node = (Node)element;
      // for each node
      // get the names of the nodes that have the specified relationship
      Vector otherNodeNames = 
	getAttributeValuesWithSuffix(node, relationship);
      // get all the original node's edges
      Enumeration edges = node.edgeElements();
      // the relationship determines whether we should look
      // for the other nodes at the heads or tails of edges
      boolean getTails = false;
      if (relationship.endsWith("Superior") ||
          relationship.endsWith("Customer"))
        getTails = true;
      while (edges.hasMoreElements()) {
	Edge edge = (Edge)edges.nextElement();
	if (edge.getHead().equals(node)) {
          // if the original node was at the head
          // if the tail node is in the related node names
          // and we're getting tails
          // then add the tail node and the edge to it
	  if (otherNodeNames.contains(getOrganizationName(edge.getTail())) &&
              getTails) {
	    if (!myElements.contains(edge)) 
	      myElements.add(edge);
	    Node tail = edge.getTail();
	    if (!myElements.contains(tail)) 
	      myElements.add(tail);
	  }
	} else if (edge.getTail().equals(node) && !getTails) {
          // if the original node was at the tail
          // and we're getting heads
          // if the head node is in the related node names
          // then add the head node and the edge to it
	  if (otherNodeNames.contains(getOrganizationName(edge.getHead()))) {
	    if (!myElements.contains(edge)) 
	      myElements.add(edge);
	    Node head = edge.getHead();
	    if (!myElements.contains(head))
	      myElements.add(head);
	  }
	}
      }
    }
    return myElements;
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

    JPanel agentPanel = new JPanel();
    agentPanel.setBorder(new TitledBorder("Society"));
    agentPanel.setLayout(new BoxLayout(agentPanel, BoxLayout.Y_AXIS));

    JComboBox cb = 
      new JComboBox(new LegendComboBoxModel(graph.getNodeColors()));
    cb.setRenderer(new LegendRenderer());
    cb.setSelectedIndex(0);
    agentPanel.add(cb);

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
    legendDialog.getContentPane().add(agentPanel, BorderLayout.CENTER);
    legendDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    legendDialog.pack();
    legendDialog.setSize(legendDialog.getWidth(),legendDialog.getHeight()+100);
    legendDialog.show();
  }

}











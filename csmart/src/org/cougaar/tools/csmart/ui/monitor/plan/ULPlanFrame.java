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

package org.cougaar.tools.csmart.ui.monitor.plan;

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
import org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL;
import org.cougaar.tools.csmart.ui.util.NamedFrame;

import att.grappa.Node;

public class ULPlanFrame extends CSMARTFrame {
  private static final String THREAD_UP_MENU_ITEM = "Ancestor Thread";
  private static final String THREAD_DOWN_MENU_ITEM = "Descendant Thread";
  private static final String LEGEND_MENU_ITEM = "Legend";
  private static final String PARENTS_MENU_ITEM = "Parents";
  private static final String CHILDREN_MENU_ITEM = "Children";
  private static final String COLOR_PARENTS = "colorParents";
  private static final String COLOR_CHILDREN = "colorChildren";
  // MEK
  private static final String FIND_PLAN_OBJECTS_MENU_ITEM = "Find Plan Objects";
  private Hashtable communityToAgents;
  private ULPlanFinder finder;
  // MEK
  private JDialog legendDialog;
  private ULPlanFilter filter;
  private static UIProperties properties = null;
  private JMenuItem threadUpMenuItem;
  private JMenuItem threadDownMenuItem;

  public ULPlanFrame(String title, CSMARTGraph graph, ULPlanFilter filter, Hashtable communityToAgents) {
    super(title, graph);
    setProperties();
    this.filter = filter;
    this.communityToAgents = communityToAgents;
    this.graph = graph;

  }

  public void customize() {
    insertMenuItem(CSMARTFrame.VIEW_MENU, LEGEND_MENU_ITEM, 2, this);
    threadUpMenuItem = 
      insertMenuItem(CSMARTFrame.VIEW_MENU, THREAD_UP_MENU_ITEM, 3, this);
    threadUpMenuItem.setEnabled(false);
    threadDownMenuItem =
      insertMenuItem(CSMARTFrame.VIEW_MENU, THREAD_DOWN_MENU_ITEM, 4, this);
    threadDownMenuItem.setEnabled(false);
    insertMenuItem(CSMARTFrame.SHOW_MENU, PARENTS_MENU_ITEM, 0, this);
    insertMenuItem(CSMARTFrame.SHOW_MENU, CHILDREN_MENU_ITEM, 1, this);
    insertMenuItem(CSMARTFrame.SELECT_MENU, FIND_PLAN_OBJECTS_MENU_ITEM, 1, this);
  }

  private void setProperties() {
    if (properties != null)
      return; // only need to do this once
    properties = CSMARTGraph.getProperties();
    GrappaColor.addColor(COLOR_PARENTS, properties.getColorCauses());
    GrappaColor.addColor(COLOR_CHILDREN, properties.getColorEffects());
  }

  /**
   * Enable/disable menu items that are used when the event graph
   * has selected nodes.
   * Overrides CSMARTFrame to enable the show menu only
   * when a task node is selected.
   * @param enable true to enable menu items; false to disable
   */

  protected void enableSelectedMenus(boolean enable) {
    super.enableSelectedMenus(enable);
    boolean haveSelectedNode = false;
    if (enable) {
      Vector elements = graph.getSelectedElements();
      if (elements == null)
	return;
      for (int i = 0; i < elements.size(); i++) {
	Element element = (Element)elements.elementAt(i);
	if (element instanceof Node) {
	  haveSelectedNode = true;
	  String objectType =
	    (String)element.getAttributeValue(PropertyNames.OBJECT_TYPE);
	  if (objectType != null && 
	      objectType.equals(PropertyNames.TASK_OBJECT)) {
	    threadUpMenuItem.setEnabled(true);
	    threadDownMenuItem.setEnabled(true);
	    return; // a task element was selected, leave show menu enabled
	  }
	}
      }
      if (threadUpMenuItem != null) { // may not have been initted
	threadUpMenuItem.setEnabled(haveSelectedNode);
	threadDownMenuItem.setEnabled(haveSelectedNode);
      }
      showMenu.setEnabled(false);
    } else {
      // disabling menu items
      if (threadUpMenuItem != null) { // may not have been initted
	threadUpMenuItem.setEnabled(false);
	threadDownMenuItem.setEnabled(false);
      }
    }
  }

  public TableModel getAttributeTableModel(Node node) {
    return new ULPlanTableModel(node);
  }

  /**
   * Process menu commands that are specific to plan objects,
   * pass other menu commands to CSMARTFrame.
   * @param evt the event received
   */

  public void actionPerformed(ActionEvent evt) {
    String command = ((JMenuItem)evt.getSource()).getText();

    if (command.equals(NEW_WITH_SELECTION_MENU_ITEM)) {
      // check that at least one node is selected, else do nothing
      if (!graph.isNodeSelected())
	return;
      CSMARTGraph newGraph = graph.newGraphFromSelection();
      // TODO: can't copy filter here; filter is set with
      // values for the NEXT graph to be created, not with
      // the values used to create the current graph, which is what you need
      //
      new ULPlanFrame(NamedFrame.PLAN, newGraph, filter.copy(), communityToAgents);
      return;
    }

    if (command.equals(LEGEND_MENU_ITEM)) {
      createLegend();
      return;
    }

    if (command.equals(THREAD_UP_MENU_ITEM) ||
	 command.equals(THREAD_DOWN_MENU_ITEM)) {
      Vector selected = graph.getSelectedElements();
      if (selected != null && selected.size() != 0) {
 	  Element element = (Element)selected.elementAt(0);
	  if (element.isNode()) {
	    String UID = element.getName();
	    String agentName = 
	      (String)element.getAttributeValue(PropertyNames.AGENT_ATTR);
	    if (UID != null && agentName != null)
	      if (command.equals(THREAD_UP_MENU_ITEM))
		CSMARTUL.makeThreadGraph(UID, agentName, false);
	      else
		CSMARTUL.makeThreadGraph(UID, agentName, true);
	  }
      }
      return;
    }

    // filter menu item is declared in base CSMARTFrame class
    if (command.equals(FILTER_MENU_ITEM)) {
      filter.postFilter(this, graph);
      return;
    }

    if (command.equals(PARENTS_MENU_ITEM)) {
      Vector elements = graph.getSelectedElements();
      if (elements == null)
	return;
      graph.resetColors();
      for (int i = 0; i < elements.size(); i++) {
	Element element = (Element)elements.elementAt(i);
	if (element instanceof Node)
	  graph.markTails((Node)element, COLOR_PARENTS);
      }
      return;
    }

    if (command.equals(CHILDREN_MENU_ITEM)) {
      Vector elements = graph.getSelectedElements();
      if (elements == null)
	return;
      graph.resetColors();
      for (int i = 0; i < elements.size(); i++) {
	Element element = (Element)elements.elementAt(i);
	if (element instanceof Node)
	  graph.markHeads((Node)element, COLOR_CHILDREN);
      }
      return;
    }
    if (command.equals(FIND_PLAN_OBJECTS_MENU_ITEM)) {
      finder = new ULPlanFinder(this, graph, communityToAgents);
      finder.displayFinder();
      return;
    }

    super.actionPerformed(evt); // let CSMARTFrame handle the rest
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
    JPanel legendPanel = new JPanel(new BorderLayout());
    JPanel clusterPanel = new JPanel();
    clusterPanel.setBorder(new TitledBorder("Agents"));
    clusterPanel.setLayout(new BoxLayout(clusterPanel, BoxLayout.Y_AXIS));
    int x = 0;
    int y = 0;
    JComboBox cb = 
      new JComboBox(new LegendComboBoxModel(graph.getNodeColors()));
    cb.setRenderer(new LegendRenderer());
    cb.setSelectedIndex(0);
    clusterPanel.add(cb);

    JPanel planPanel = new JPanel(new BorderLayout());
    planPanel.setBorder(new TitledBorder("Plan Objects"));
    planPanel.add(getNodeShapeLegend(), BorderLayout.CENTER);
    legendPanel.add(clusterPanel, BorderLayout.NORTH);
    legendPanel.add(planPanel, BorderLayout.CENTER);
    legendDialog = new JDialog(this, "Legend", false);
    legendDialog.getContentPane().setLayout(new BorderLayout());
    legendDialog.getContentPane().add(legendPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    JButton OKButton = new JButton("OK");
    OKButton.setFocusPainted(false);
    OKButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	legendDialog.setVisible(false);
      }
    });
    buttonPanel.add(OKButton);
    legendDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    //    legendDialog.setSize(400, 700);
    legendDialog.setSize(300, 400);
    legendDialog.show();
  }

  /**
   * Get node shapes for plan objects for legend.
   */

  private JPanel getNodeShapeLegend() {
    CSMARTGraph legendGraph = new CSMARTGraph(); // just to make nodes
    legendGraph.setAttribute(GrappaConstants.RANKDIR_ATTR, "TB");
    legendGraph.setNodeAttribute(GrappaConstants.COLOR_ATTR, "goldenrod2");
    GrappaStyle gs = 
      new GrappaStyle(GrappaConstants.NODE,
		      "filled(true),line_color(goldenrod2)");
    legendGraph.setNodeAttribute(GrappaConstants.STYLE_ATTR, gs);
    Subgraph subg1 = new Subgraph(legendGraph);
    Subgraph subg2 = new Subgraph(legendGraph);

    GrappaPanel legendPanel = new GrappaPanel(legendGraph);
    legendPanel.setToolTipText("");
    GrappaColor.addColor("myGray", legendPanel.getBackground());
    legendPanel.setMinimumSize(new Dimension(370, 520));
    legendPanel.setMaximumSize(new Dimension(370, 520));
    legendPanel.setPreferredSize(new Dimension(370, 520));
    legendPanel.setScaleToFit(true);

    // column 1
    Node taskNode = new Node(legendGraph);
    taskNode.setAttribute(GrappaConstants.LABEL_ATTR, "Task");
    taskNode.setAttribute(GrappaConstants.SHAPE_ATTR, "ellipse");
    taskNode.setAttribute(GrappaConstants.WIDTH_ATTR, "1.0");

    Node allocationNode = new Node(legendGraph);
    allocationNode.setAttribute(GrappaConstants.LABEL_ATTR, "Allocation");
    allocationNode.setAttribute(GrappaConstants.SHAPE_ATTR, "triangle");
    allocationNode.setAttribute(GrappaConstants.ORIENTATION_ATTR, "-90");
    Edge edge = new Edge(legendGraph, taskNode, allocationNode);
    edge.setAttribute(CSMARTGraph.INVISIBLE_ATTRIBUTE, "true");

    Node expansionNode = new Node(legendGraph);
    expansionNode.setAttribute(GrappaConstants.LABEL_ATTR, 
			       "Expansion");
    expansionNode.setAttribute(GrappaConstants.SHAPE_ATTR, "trapezium");
    expansionNode.setAttribute(GrappaConstants.ORIENTATION_ATTR, "90");
    edge = new Edge(legendGraph, allocationNode, expansionNode);
    edge.setAttribute(CSMARTGraph.INVISIBLE_ATTRIBUTE, "true");

    Node aggregationNode = new Node(legendGraph);
    aggregationNode.setAttribute(GrappaConstants.LABEL_ATTR, "Aggregation");
    aggregationNode.setAttribute(GrappaConstants.SHAPE_ATTR, "trapezium");
    aggregationNode.setAttribute(GrappaConstants.ORIENTATION_ATTR, "-90");
    edge = new Edge(legendGraph, expansionNode, aggregationNode);
    edge.setAttribute(CSMARTGraph.INVISIBLE_ATTRIBUTE, "true");

    // column 2
    Node assetNode = new Node(legendGraph);
    assetNode.setAttribute(GrappaConstants.SHAPE_ATTR, "box");
    assetNode.setAttribute(GrappaConstants.LABEL_ATTR, "Asset");

    Node assetTransferNode = new Node(legendGraph);
    assetTransferNode.setAttribute(GrappaConstants.LABEL_ATTR, 
				   "AssetTransfer");
    assetTransferNode.setAttribute(GrappaConstants.SHAPE_ATTR, "hexagon");
    edge = new Edge(legendGraph, assetNode, assetTransferNode);
    edge.setAttribute(CSMARTGraph.INVISIBLE_ATTRIBUTE, "true");

    Node workflowNode = new Node(legendGraph);
    workflowNode.setAttribute(GrappaConstants.LABEL_ATTR, "Workflow");
    workflowNode.setAttribute(GrappaConstants.SHAPE_ATTR, "triangle");
    edge = new Edge(legendGraph, assetTransferNode, workflowNode);
    edge.setAttribute(CSMARTGraph.INVISIBLE_ATTRIBUTE, "true");

    // create two columns
    legendGraph.addNodeToSubgraph(taskNode, subg1);
    legendGraph.addNodeToSubgraph(allocationNode, subg1);
    legendGraph.addNodeToSubgraph(expansionNode, subg1);
    legendGraph.addNodeToSubgraph(aggregationNode, subg1);

    legendGraph.addNodeToSubgraph(assetNode, subg2);
    legendGraph.addNodeToSubgraph(assetTransferNode, subg2);
    legendGraph.addNodeToSubgraph(workflowNode, subg2);

    legendGraph.doLayout();
    // TODO: setting background doesn't work
    legendGraph.setGrappaAttribute(GrappaConstants.GRAPPA_BACKGROUND_COLOR_ATTR,
				   "lightgray");
    return legendPanel;
  }


}



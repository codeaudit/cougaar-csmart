/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.plan;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import att.grappa.*;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames;

/**
 * Find plan object from a dialog in which the user can specify:
 * community name, agent name, plan object type, and for some
 * objects, additional object-specific information.
 */

public class ULPlanFinder extends JDialog {
  private static final String ALL = "all";
  //  private CSMARTGraph graph;
  //  private CSMARTFrame frame;
  private JPanel findPanel;
  private JComboBox communityCB;
  private JComboBox agentCB;
  private JComboBox planObjectTypeCB;
  private int selectedIndex;
  private int maxSelectedIndex;
  private JButton findNextButton;
  private Vector selectedObjects;
  private Hashtable communityToAgents;

  public ULPlanFinder(String[] communityNames,
		      Hashtable communityToAgents,
		      String[] planObjectTypes) {
    this.communityToAgents = communityToAgents;
    findPanel = new JPanel(new BorderLayout());
    Box box = new Box(BoxLayout.Y_AXIS);

    // community
    JPanel communityPanel = new JPanel();
    communityPanel.setLayout(new GridBagLayout());
    TitledBorder communityTitledBorder = new TitledBorder("Community");
    Font font = communityTitledBorder.getTitleFont();
    Font titleFont = font.deriveFont(Font.ITALIC);
    communityTitledBorder.setTitleFont(titleFont);
    communityPanel.setBorder(communityTitledBorder);
    JLabel communityLabel = new JLabel("Community: ");
    ArrayList names = new ArrayList(communityNames.length);
    names.add("Select One");
    for (int i = 0; i < communityNames.length; i++)
      names.add(communityNames[i]);
    communityCB = new JComboBox((String[])names.toArray(new String[names.size()]));
    communityCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	communityActionPerformed(e);
      }
    });
    communityPanel.add(communityLabel,
		   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    communityPanel.add(communityCB,
		   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    box.add(communityPanel);

    // agent
    JPanel agentPanel = new JPanel();
    agentPanel.setLayout(new GridBagLayout());
    TitledBorder agentTitledBorder = new TitledBorder("Agent");
    agentTitledBorder.setTitleFont(titleFont);
    agentPanel.setBorder(agentTitledBorder);
    JLabel agentLabel = new JLabel("Agent: ");
    agentCB = new JComboBox();
    agentPanel.add(agentLabel,
		   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    agentPanel.add(agentCB,
		   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    box.add(agentPanel);

    // planObject types
    JPanel planObjectPanel = new JPanel();
    planObjectPanel.setLayout(new GridBagLayout());
    TitledBorder planObjectTitledBorder = new TitledBorder("PlanObject Type");
    planObjectTitledBorder.setTitleFont(titleFont);
    planObjectPanel.setBorder(planObjectTitledBorder);
    JLabel planObjectTypeLabel = new JLabel("PlanObject Types: ");
    planObjectTypeCB = new JComboBox();
    planObjectTypeCB.addItem(ALL);
    for (int i = 0; i < planObjectTypes.length; i++)
      planObjectTypeCB.addItem(planObjectTypes[i]);
    planObjectTypeCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	planObjectTypeActionPerformed(e);
      }
    });

    planObjectPanel.add(planObjectTypeLabel,
		   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    planObjectPanel.add(planObjectTypeCB,
		   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    box.add(planObjectPanel);

    // if only one community, select it and fill in the agent list
    //    if (communityNames.size() == 1)
    //      communityCB.setSelectedIndex(0);

    // select the "Select One" item
    //    communityCB.setSelectedIndex(communityNames.size()-1);

    findPanel.add(box, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    final JButton findButton = new JButton("Find");
    findButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	find();
	findButton.setEnabled(false);
      }
    });
    buttonPanel.add(findButton);
    findNextButton = new JButton("Find Next");
    // find next is disabled until user selects find
    findNextButton.setEnabled(false);
    findNextButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	findNext();
      }
    });
    buttonPanel.add(findNextButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	cancel();
      }
    });
    buttonPanel.add(cancelButton);
    findPanel.add(buttonPanel, BorderLayout.SOUTH);
    getContentPane().add(findPanel);
    pack();

    // find next is disabled and find is enabled
    // when any other dialog change is made
    // so user must select "find" to process the values in the dialog
    // and determine the new set of selected objects
    ActionListener disableFindNext = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	findNextButton.setEnabled(false);
	findButton.setEnabled(true);
      }
    };
    communityCB.addActionListener(disableFindNext);
    agentCB.addActionListener(disableFindNext);
    planObjectTypeCB.addActionListener(disableFindNext);
  }

  /**
   * User selected find button. Scroll to first selected object.
   */

  private void find() {
    selectedObjects = getSelectedObjects();
    if (selectedObjects.size() == 0)
      return;
    selectedIndex = 0;
    maxSelectedIndex = selectedObjects.size();
    findNext();
    findNextButton.setEnabled(true);
  }

  /**
   * User selected find next button. Scroll to next selected object.
   * If at last selected object, cycle around to the first one.
   */

  private void findNext() {
    //    graph.select((Element)selectedObjects.elementAt(selectedIndex++));
    //    if (selectedIndex == maxSelectedIndex)
    //      selectedIndex = 0;
    //    frame.scrollToSelected();
  }

  /**
   * User selected community; set agents in that community in the
   * agent combo box.
   */

  private void communityActionPerformed(ActionEvent e) {
    JComboBox cb = (JComboBox)e.getSource();
    if (agentCB.getItemCount() != 0)
      agentCB.removeAllItems();
    Object selection = cb.getSelectedItem();
    if (((String)selection).equals("Select One"))
      return; // do nothing
    String[] agentNames = 
      (String[])communityToAgents.get(cb.getSelectedItem());
    agentCB.addItem(ALL);
    for (int i = 0; i < agentNames.length; i++)
      agentCB.addItem(agentNames[i]);
  }

  /**
   * User plan object type; enable plan object type specific panels.
   */

  private void planObjectTypeActionPerformed(ActionEvent e) {
    JComboBox cb = (JComboBox)e.getSource();
    String s = (String)cb.getSelectedItem();
  }

  /**
   * User selected cancel button; hide dialog.
   */

  private void cancel() {
    setVisible(false);
  }

  /**
   * Display dialog.  
   */

  public void displayFinder() {
    setVisible(true);
  }

  /**
   * Determine what objects were selected from the dialog.
   * Returns vector of grappa Node.
   */

  private Vector getSelectedObjects() {
    String communitySelected = (String)communityCB.getSelectedItem();
    String agentSelected = (String)agentCB.getSelectedItem();
    String planObjectTypeSelected = (String)planObjectTypeCB.getSelectedItem();
    // Vector selectedNodes = graph.vectorOfElements(GrappaConstants.NODE);
    // use getNodesWithStringAttribute to select subset of nodes
    return null; // should return selected nodes from graph
  }

  /**
   * Get value from a text field.
   */

  private float getFloatValue(String s, String errorMessage, 
			      float defaultValue) {
    float value = defaultValue;
    try {
      value = Float.parseFloat(s);
    } catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(null, errorMessage);
    }
    return value;
  }

  private long getLongValue(String s, String errorMessage, long defaultValue) {
    long value = defaultValue;
    try {
      value = Long.parseLong(s);
    } catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(null, errorMessage);
    }
    return value;
  }

  private Vector getNodesInFloatRange(Vector nodes,
				      String attributeName, 
				      float startTime, float endTime) {
    Vector selectedNodes = new Vector(nodes.size());
    float nodeValue;
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      String s = (String)node.getAttributeValue(attributeName);
      if (s == null)
	continue;
      try {
	nodeValue = Float.parseFloat(s);
      } catch (NumberFormatException e) {
	System.out.println("ULPlanFinder: " + e);
	continue;
      }
      if (nodeValue >= startTime && nodeValue <= endTime)
	selectedNodes.addElement(node);
    }
    return selectedNodes;
  }

  private Vector getNodesInLongRange(Vector nodes,
				     String attributeName, 
				     long startTime, long endTime) {
    Vector selectedNodes = new Vector(nodes.size());
    long nodeValue;
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      String s = (String)node.getAttributeValue(attributeName);
      if (s == null)
	continue;
      try {
	nodeValue = Long.parseLong(s);
      } catch (NumberFormatException e) {
	System.out.println("ULPlanFinder: " + e);
	continue;
      }
      if (nodeValue >= startTime && nodeValue <= endTime)
	selectedNodes.addElement(node);
    }
    return selectedNodes;
  }

  private Vector getNodesWithFloatAttribute(Vector nodes, 
					    String attributeName,
					    float value) {
    Vector selectedNodes = new Vector(nodes.size());
    float nodeValue;
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      String s = (String)node.getAttributeValue(attributeName);
      if (s == null)
	continue;
      try {
	nodeValue = Float.parseFloat(s);
      } catch (NumberFormatException e) {
	System.out.println("ULPlanFinder: " + e);
	continue;
      }
      if (nodeValue == value)
	selectedNodes.addElement(node);
    }
    return selectedNodes;
  }

  private Vector getNodesWithLongAttribute(Vector nodes, 
					   String attributeName,
					   long value) {
    Vector selectedNodes = new Vector(nodes.size());
    long nodeValue;
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      String s = (String)node.getAttributeValue(attributeName);
      if (s == null)
	continue;
      try {
	nodeValue = Long.parseLong(s);
      } catch (NumberFormatException e) {
	System.out.println("ULPlanFinder: " + e);
	continue;
      }
      if (nodeValue == value)
	selectedNodes.addElement(node);
    }
    return selectedNodes;
  }


  private Vector getNodesWithStringAttribute(Vector nodes,
					     String attributeName,
					     String attributeValue) {
    Vector selectedNodes = new Vector(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      Attribute a = node.getAttribute(attributeName);
      if (a != null) 
	if (a.getStringValue().equals(attributeValue))
	  selectedNodes.addElement(node);
    }
    return selectedNodes;
  }

  /**
   * Return subset of nodes that are in the specified community.
   */

  private Vector getNodesInChoiceCommunity(Vector nodes,
					   String commName) {
    Vector selectedNodes = new Vector(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      // If source is selected community, OK
      Attribute oComm = 
	node.getAttribute(PropertyNames.PLAN_OBJECT_COMMUNITY_NAME);
      if (oComm != null) 
	if (oComm.getStringValue().equals(commName))
	  selectedNodes.addElement(node);
    }
    return selectedNodes;
  }

  // Return subset of Nodes for which EITHER of the given
  // attributes has the value specified
  private Vector getNodesWithEitherAttribute(Vector nodes,
					     String attributeName,
					     String otherAttName,
					     String attributeValue) {
    Vector selectedNodes = new Vector(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      Attribute a = node.getAttribute(attributeName);
      if (a != null) 
	if (a.getStringValue().equals(attributeValue)) 
	  selectedNodes.addElement(node);
      a = node.getAttribute(otherAttName);
      if (a != null) {
	//
//System.out.println("NodesWEA looking for " + attributeValue + " and have " + a.getStringValue());
	if (a.getStringValue().equals(attributeValue)) {
	  //System.out.println("NodesWEA found match for atValue " + attributeValue + " on attribute " + otherAttName);
	  selectedNodes.addElement(node);
	}
      }
    }
    return selectedNodes;
  }

  public static void main(String[] args) {
    Hashtable communityToAgents = new Hashtable();
    String[] communityNames = { "Community One", "Community Two", "Community Three" };
    String[] agentNames = { "Customer", "Provider" };
    for (int i = 0; i < communityNames.length; i++) {
      String[] myAgents = new String[agentNames.length];
      for (int j = 0; j < agentNames.length; j++) 
	myAgents[j] = agentNames[j] + i;
      communityToAgents.put(communityNames[i], myAgents);
    }
    String[] planObjectTypes = { "Allocation", "Aggregation", "Disposition", "Expansion", "Asset", "Task", "Workflow" };
    ULPlanFinder finder =
      new ULPlanFinder(communityNames, communityToAgents, planObjectTypes);
    finder.displayFinder();
  }
  
  /**
   * Used to order selected nodes according to position.
   */

  class NodeComparator implements Comparator {

    /** Return negative, zero or positive as o1's position is less than,
     * equal to, or greater than o2, where position is considered
     * top-to-bottom and left-to-right.
     */

    public int compare(Object o1, Object o2) {
      Node n1 = (Node)o1;
      Node n2 = (Node)o2;
      GrappaPoint gp1 =
	(GrappaPoint)n1.getAttributeValue(GrappaConstants.POS_ATTR);
      GrappaPoint gp2 = 
	(GrappaPoint)n2.getAttributeValue(GrappaConstants.POS_ATTR);
      if (gp1 == null || gp2 == null)
	return 0; // treat them as equal
      double y1 = gp1.getY();
      double y2 = gp2.getY();
      if (y1 < y2)
	return -1;
      else if (y1 == y2) {
	double x1 = gp1.getX();
	double x2 = gp2.getX();
	if (x1 < x2)
	  return -1;
	else if (x1 > x2)
	  return 1;
	else 
	  return 0;
      } else 
	return 1;
    }

    public boolean equals(Object o) {
      return this.equals(o);
    }

  }

}

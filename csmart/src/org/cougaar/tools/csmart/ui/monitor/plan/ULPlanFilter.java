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
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTFrame;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTGraph;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Util;

public class ULPlanFilter {
  // maps community names to names of agents in community
  Hashtable communityToAgents; 
  // ask user if should fetch more objects than this
  private static int DEFAULT_OBJECT_LIMIT = 2000;
  // order these as they should appear in the filter
  private static final String[] PLAN_OBJECT_TYPES = { 
    PropertyNames.PLAN_ELEMENT_OBJECT,
    PropertyNames.DIRECT_OBJECT,
    PropertyNames.WORKFLOW_OBJECT,
  };

  // filter dialog controls referenced in set-up and processing dialog
  private JPanel filterPanel;
  private Vector buttonGroups;
  private Vector buttonLabels;
  private JList communityList;
  private Vector planObjectComponents;
  private JRadioButton allPlanObjectsButton;
  private JTextField limitField;

  // results of displaying filter
  private Vector agentsSelected = null;
  private boolean allPlanObjectsSelected;
  private Vector showObjectTypes;
  private Vector hideObjectTypes;
  private Vector ignoreObjectTypes;

  // filtering before or after fetching data
  private boolean preFilter = true;

  /**
   * TODO: in PSP or in CSMARTUL, add agent name and community name
   * to properties on an object
   * There are three cases in which filters are created:
   * 1) the user is filtering the data before fetching the data (prefilter);
   *    the community->agents mapping is specified;
   *    the show/hide/ignore object types are empty
   * 2) the user is filtering the data in an existing graph (postfilter);
   *    the community->agents mapping, and show/hide/ignore object
   *    types are passed from the existing filter to a new filter
   * 3) the user is filtering a graph that was saved in a file (postfilter);
   *    the agent->community mapping is derived from the information
   *    saved with each node; 
   *    the show/hide/ignore object types are undefined (TODO: save these?)
   * Note that this filter is not re-used unless the user cancels the
   * filter dialog; i.e. when the user "filters" and selects "ok",
   * a new frame with a new filter is created.
   *
   * Prefilter.
   */

  public ULPlanFilter(Hashtable communityToAgents) {
    this.communityToAgents = communityToAgents;
    showObjectTypes = new Vector();
    hideObjectTypes = new Vector();
    ignoreObjectTypes = new Vector();
    initFilter();
  }

  /**
   * Postfilter.
   */

  public ULPlanFilter(Hashtable communityToAgents,
		      Vector showObjectTypes,
		      Vector hideObjectTypes,
		      Vector ignoreObjectTypes) {
    this.communityToAgents = communityToAgents;
    this.showObjectTypes = showObjectTypes;
    this.hideObjectTypes = hideObjectTypes;
    this.ignoreObjectTypes = ignoreObjectTypes;
    initFilter();
  }

  /**
   * Filter graph read from file.
   */

  public ULPlanFilter(CSMARTGraph graph) {
    communityToAgents = getCommunityToAgents(graph);
    showObjectTypes = new Vector();
    hideObjectTypes = new Vector();
    ignoreObjectTypes = new Vector();
    initFilter();
  }

  /**
   * Create a copy of this filter to use with a new graph 
   * constructed from the graph associated with this filter.
   */

  public ULPlanFilter copy() {
    return new ULPlanFilter(communityToAgents, showObjectTypes,
			    hideObjectTypes, ignoreObjectTypes);
  }

  /**
   * Get community to agents mapping from a saved graph.
   */

  private Hashtable getCommunityToAgents(CSMARTGraph graph) {
    Hashtable communityToAgents = new Hashtable();
    Vector nodes = graph.vectorOfElements(GrappaConstants.NODE);
    for (int i = 0; i < nodes.size(); i++) {
      Node node =(Node)nodes.elementAt(i);
      String agentName = 
	(String)node.getAttributeValue(PropertyNames.PLAN_OBJECT_AGENT_NAME);
      if (agentName == null)
	continue;
      String communityName = 
	(String)node.getAttributeValue(PropertyNames.PLAN_OBJECT_COMMUNITY_NAME);
      if (communityName == null)
	continue;
      Vector agents = (Vector)communityToAgents.get(communityName);
      if (agents == null) {
	agents = new Vector();
	agents.addElement(agentName);
	communityToAgents.put(communityName, agents);
      } else
	agents.addElement(agentName);
    }
    return communityToAgents;
  }

  /**
   * Get names of agents to contact for prefiltering,
   * i.e. only fetch data from specified agents.
   * @return Vector of Strings, names of agents to contact
   */

  public Vector getAgentsSelected() {
    //    displayFilter();
    return agentsSelected;
  }

  /**
   * Get filter to pass to agents for prefiltering.
   * Creates a filter, which is a comma separated list of ignoreObjectTypes
   * @return filter to pass to agent PSP or null (if user cancelled dialog)
   */

  public String getIgnoreObjectTypes() {
    if (ignoreObjectTypes == null)
      return null;
    if (ignoreObjectTypes.size() == 0)
      return null;
    StringBuffer sb = new StringBuffer(100);
    for (int i = 0; i < ignoreObjectTypes.size(); i++) {
      sb.append(ignoreObjectTypes.elementAt(i));
      sb.append(",");
    }
    return sb.substring(0, sb.length()-1);
  }

  /**
   * Create the filter dialog.
   */

  private void initFilter() {
    filterPanel = new JPanel(new BorderLayout());
    Box box = new Box(BoxLayout.Y_AXIS);

    // community panel; list of communities
    JPanel communityPanel = new JPanel();
    communityPanel.setLayout(new GridBagLayout());
    TitledBorder communityTitledBorder = new TitledBorder("Communities");
    Font font = communityTitledBorder.getTitleFont();
    Font titleFont = font.deriveFont(Font.ITALIC);
    communityTitledBorder.setTitleFont(titleFont);
    communityPanel.setBorder(communityTitledBorder);
    Vector allCommunities = new Vector(communityToAgents.keySet());
    communityList = new JList(allCommunities);
    JScrollPane scrollPane = new JScrollPane(communityList);
    communityList.setVisibleRowCount(4);
    // if just one community, then select it
    if (allCommunities.size() == 1)
      communityList.setSelectedIndex(0);
    int x = 0;
    int y = 0;
    communityPanel.add(scrollPane,
		       new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
					      GridBagConstraints.WEST,
					      GridBagConstraints.HORIZONTAL,
					      new Insets(0, 0, 0, 0), 0, 0));
    box.add(communityPanel);

    // the label, show, hide and ignore buttons which are enabled/disabled
    // when the user selects the "All" or "Selected Plan Objects" buttons
    planObjectComponents = new Vector(PLAN_OBJECT_TYPES.length*4);
    JPanel planObjectPanel = new JPanel();
    planObjectPanel.setLayout(new GridBagLayout());
    TitledBorder border = new TitledBorder("Plan Objects");
    border.setTitleFont(titleFont);
    planObjectPanel.setBorder(border);
    // button to show all plan objects
    allPlanObjectsButton = new JRadioButton("All");
    allPlanObjectsButton.setFocusPainted(false);
    allPlanObjectsButton.setSelected(true);
    allPlanObjectsSelected = true;
    allPlanObjectsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	if (allPlanObjectsSelected)
	  return; // ignore redundant button press
	allPlanObjectsSelected = true;
	for (int i = 0; i < planObjectComponents.size(); i++)
	  ((JComponent)planObjectComponents.elementAt(i)).setEnabled(false);
      }
    });
    // button to show specific plan objects
    JRadioButton specificPlanObjectsButton =
      new JRadioButton("Selected Plan Objects");
    specificPlanObjectsButton.setFocusPainted(false);
    specificPlanObjectsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	if (!allPlanObjectsSelected)
	  return; // ignore redundant button press
	allPlanObjectsSelected = false;
	for (int i = 0; i < planObjectComponents.size(); i++)
	  ((JComponent)planObjectComponents.elementAt(i)).setEnabled(true);
      }
    });
    ButtonGroup bg = new ButtonGroup();
    bg.add(allPlanObjectsButton);
    bg.add(specificPlanObjectsButton);
    planObjectPanel.add(allPlanObjectsButton,
		   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));

    planObjectPanel.add(specificPlanObjectsButton,
		   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    // button groups to show/hide/ignore specific plan object types
    buttonGroups = new Vector(PLAN_OBJECT_TYPES.length);
    buttonLabels = new Vector(PLAN_OBJECT_TYPES.length);
    for (int i = 0; i < PLAN_OBJECT_TYPES.length; i++) {
      x = 0;
      String s = PLAN_OBJECT_TYPES[i];
      // names are sent to PSPs, so must not have spaces
      // make pretty names for display
      JLabel label = new JLabel(s.replace('_', ' '));
      planObjectComponents.add(label);
      label.setEnabled(false);
      buttonLabels.addElement(s);
      planObjectPanel.add(label, 
	  new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
				 GridBagConstraints.WEST,
				 GridBagConstraints.HORIZONTAL,
				 new Insets(0, 20, 0, 0), 0, 0));

      ButtonGroup buttonGroup = new ButtonGroup();
      buttonGroups.addElement(buttonGroup);
      JRadioButton showButton = new JRadioButton("Show");
      planObjectComponents.add(showButton);
      showButton.setFocusPainted(false);
      showButton.setSelected(true);
      showButton.setEnabled(false); // initially set to all plan objects
      buttonGroup.add(showButton);
      planObjectPanel.add(showButton,
	  new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
				 GridBagConstraints.WEST,
				 GridBagConstraints.HORIZONTAL,
				 new Insets(0, 0, 0, 0), 0, 0));
      JRadioButton hideButton = new JRadioButton("Hide");
      planObjectComponents.add(hideButton);
      hideButton.setFocusPainted(false);
      hideButton.setEnabled(false); // initially set to all plan objects
      buttonGroup.add(hideButton);
      planObjectPanel.add(hideButton,
	  new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
				 GridBagConstraints.WEST,
				 GridBagConstraints.HORIZONTAL,
				 new Insets(0, 0, 0, 0), 0, 0));
      JRadioButton ignoreButton = new JRadioButton("Ignore");
      planObjectComponents.add(ignoreButton);
      ignoreButton.setFocusPainted(false);
      ignoreButton.setEnabled(false); // initially set to all plan objects
      buttonGroup.add(ignoreButton);
      planObjectPanel.add(ignoreButton,
	  new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0,
				 GridBagConstraints.WEST,
				 GridBagConstraints.HORIZONTAL,
				 new Insets(0, 0, 0, 0), 0, 0));
      y++; // next row
    }
    box.add(planObjectPanel);

    JPanel limitPanel = new JPanel();
    limitPanel.setLayout(new GridBagLayout());
    TitledBorder limitTitledBorder = 
      new TitledBorder("Number of Plan Objects");
    limitTitledBorder.setTitleFont(titleFont);
    limitPanel.setBorder(limitTitledBorder);
    JLabel limitLabel = new JLabel("Limit number of plan objects to: ");
    limitField = new JTextField(String.valueOf(DEFAULT_OBJECT_LIMIT));
    limitPanel.add(limitLabel,
		   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    limitPanel.add(limitField,
		   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    box.add(limitPanel);

    filterPanel.add(box, BorderLayout.CENTER);

    // set filter controls to reflect objects shown/hidden/ignored
    // before this filter was created
    if (showObjectTypes.size() == 0 &&
	hideObjectTypes.size() == 0 &&
	ignoreObjectTypes.size() == 0)
      return;

    // if any objects were ignored, then disable "all plan objects" option
    allPlanObjectsButton.setSelected(false);
    allPlanObjectsButton.setEnabled(false);
    specificPlanObjectsButton.setSelected(true);
    allPlanObjectsSelected = false;
    for (int i = 0; i < planObjectComponents.size(); i++)
      ((JComponent)planObjectComponents.elementAt(i)).setEnabled(true);

    // set show, hide, ignore buttons to reflect objects shown/hidden/ignored
    for (int i = 0; i < PLAN_OBJECT_TYPES.length; i++) {
      if (showObjectTypes.contains(PLAN_OBJECT_TYPES[i])) {
	JRadioButton component = 
	  (JRadioButton)planObjectComponents.elementAt(i*4+1);
	component.setSelected(true);
      } else if (hideObjectTypes.contains(PLAN_OBJECT_TYPES[i])) {
	JRadioButton component =
	  (JRadioButton)planObjectComponents.elementAt(i*4+2);
	component.setSelected(true);
      } else if (ignoreObjectTypes.contains(PLAN_OBJECT_TYPES[i])) {
	JRadioButton component =
	  (JRadioButton)planObjectComponents.elementAt(i*4+3);
	component.setSelected(true);
	// if an object type was ignored, 
	// it can't be unignored (it no longer exists)
	// so disable all the components for that object type
	for (int j = i*4; j < i*4+4; j++) {
	  JComponent c = (JComponent)planObjectComponents.elementAt(j);
	  c.setEnabled(false);
	}
      }
    }
  }

  /**
   * Display the filter before fetching the data.
   */

  public boolean preFilter() {
    preFilter = true;
    return displayFilter();
  }

  /**
   * Returns number of objects that the user requested.
   * @return number of objects user requested
   */

  public int getNumberOfObjects() {
    String s = limitField.getText();
    int limit = DEFAULT_OBJECT_LIMIT;
    try {
      limit = Integer.parseInt(s);
    } catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(null,
				    "Invalid limit on number of objects: " + 
				    s + " using " + DEFAULT_OBJECT_LIMIT + ".");
    }
    return limit;
  }

  /**
   * Display the filter; return true if user selects "OK" and
   * false if user cancels.  
   * Sets agentsSelected vector and vectors of plan object names that user is
   * showing/hiding/ignoring.
   */

  private boolean displayFilter() {
    int result = JOptionPane.showOptionDialog(null, filterPanel, 
					      "Filter Plan Elements",
					      JOptionPane.OK_CANCEL_OPTION,
					      JOptionPane.PLAIN_MESSAGE,
					      null, null, null);
    if (result == JOptionPane.CANCEL_OPTION)
      return false; // user cancelled

    // get selected agent names
    Object names[] = communityList.getSelectedValues();
    if (names.length == 0) {
      JOptionPane.showMessageDialog(null,
				    "No communities selected.");
      return false;
    }
    agentsSelected = new Vector();
    for (int i = 0; i < names.length; i++) 
      agentsSelected.addAll((Vector)communityToAgents.get(names[i]));

    // get types of plan objects to show/hide/ignore
    if (!allPlanObjectsSelected) {
      showObjectTypes.removeAllElements();
      hideObjectTypes.removeAllElements();
      ignoreObjectTypes.removeAllElements();
      for (int i = 0; i < buttonLabels.size(); i++) {
	ButtonGroup buttonGroup = (ButtonGroup)buttonGroups.elementAt(i);
	Enumeration buttons = buttonGroup.getElements();
	String objectType = (String)buttonLabels.elementAt(i);
	while (buttons.hasMoreElements()) {
	  AbstractButton button = (AbstractButton)buttons.nextElement();
	  if (button.isSelected()) {
	    if (button.getText().equals("Show"))
	      showObjectTypes.add(objectType);
	    else if (button.getText().equals("Hide"))
	      hideObjectTypes.add(objectType);
	    else if (button.getText().equals("Ignore"))
	      ignoreObjectTypes.add(objectType);
	  }
	}
      }
    }
    // if pre-filtering, disable controls for ignored types
    // because user can no longer ask for information on those
    if (preFilter && ignoreObjectTypes.size() != 0) {
      allPlanObjectsButton.setEnabled(false);
      for (int i = 0; i < PLAN_OBJECT_TYPES.length; i++) {
	if (ignoreObjectTypes.contains(PLAN_OBJECT_TYPES[i])) {
	  for (int j = i*4; j < i*4+4; j++) {
	    JComponent c = (JComponent)planObjectComponents.elementAt(j);
	    c.setEnabled(false);
	  }
	}
      }
    }
    return true;
  }

  /**
   * Display filter and create new graph from results.
   */

  public void postFilter(CSMARTFrame frame, CSMARTGraph graph) {
    preFilter = false;
    if (!displayFilter())
      return; // user cancelled, do nothing

    // get names of nodes to show, hide, and ignore
    Vector nodes = graph.vectorOfElements(GrappaConstants.NODE);
    Vector showNames = new Vector();
    Vector hideNames = new Vector();
    Vector ignoreNames = new Vector();
    for (int i = 0; i < nodes.size(); i++) {
      Node node = (Node)nodes.elementAt(i);
      String objectType =
	(String)node.getAttributeValue(PropertyNames.OBJECT_TYPE);
      if (objectType == null)
	continue;
      // special case direct objects
      // TODO: if an asset is a direct object and also
      // the object of an allocation, then treating it as simply
      // a direct object is incorrect
      if (objectType.equals(PropertyNames.ASSET_OBJECT)) {
	String isDirectObject =
	  (String)node.getAttributeValue(PropertyNames.ASSET_IS_DIRECT_OBJECT);
	if (isDirectObject == null)
	  continue;
	if (isDirectObject.equals("true"))
	  objectType = PropertyNames.DIRECT_OBJECT;
      }
      String nodeName = node.getName();
      if (ignoreObjectTypes.contains(objectType))
	ignoreNames.addElement(nodeName);
      else if (hideObjectTypes.contains(objectType))
	hideNames.addElement(nodeName);
      else {
	String agentName =
	  (String)node.getAttributeValue(PropertyNames.PLAN_OBJECT_AGENT_NAME);
	if (agentName == null)
	  continue;
	if (allPlanObjectsSelected || showObjectTypes.contains(objectType)) 
	  if (agentsSelected.contains(agentName))
	    showNames.addElement(nodeName);
	  else
	    hideNames.addElement(nodeName);
      }
    } // end filtering nodes      

    // create a new graph from the selected nodes
    // first copy the current graph
    CSMARTGraph newGraph = new CSMARTGraph(graph, "Filtered Graph");

    // remove any edges previously created when nodes were hidden
    newGraph.deletePseudoEdges();

    // ensure that the shown nodes and their edges are visible
    for (int i = 0; i < showNames.size(); i++)
      newGraph.showNamedNodes(showNames);

    // hide the nodes, patch together their in/out links, and
    // hide the original edges
    newGraph.hideNamedNodes(hideNames);

    // ignore the specified nodes
    // delete the nodes and DELETE their in/out links
    newGraph.removeNamedNodesAndLinks(ignoreNames);

    // redo the layout now that we have the correct nodes
    newGraph.doLayout();
    
    // create a new frame for the new graph
    // and create a new filter with the values from this filter
    // i.e. the new filter will display the object types
    // shown/ignored/hidden in the accompanying frame
    new ULPlanFrame(NamedFrame.PLAN, newGraph, 
		    new ULPlanFilter(communityToAgents, 
				     showObjectTypes, hideObjectTypes,
				     ignoreObjectTypes));

  }

}

/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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

// Adapted from Java Swing TableExample demo

package org.cougaar.tools.csmart.ui.community;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.cougaar.util.log.Logger;

import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

public class CommunityPanel extends JPanel {
  private static final String VIEW_COMMUNITY_ACTION = "Display Community...";
  private static final String NEW_COMMUNITY_ACTION = "New Community...";
  private static final String VIEW_COMMUNITY_INFO_ACTION = "Show All Parameters";
  private static final String ADD_PARAMETER_ACTION = "Add Parameter";
  private static final String DELETE_ACTION = "Delete";

  private Experiment experiment;
  private JSplitPane splitPane;
  private JSplitPane treePane;
  private CommunityDNDTree communityTree;
  private JTree hostTree;
  private JTree nodeTree;
  private JTree agentTree;
  private CommunityTreeModelListener treeModelListener;
  private JTable communityTable;
  private CommunityTableUtils communityTableUtils;
  private JPopupMenu rootMenu;
  private JPopupMenu communityMenu;
  private JPopupMenu parentEntityMenu;
  private JPopupMenu entityMenu;

  private transient Logger log;

  // actions on menus and pop-up menus
  public Action viewCommunityAction = new AbstractAction(VIEW_COMMUNITY_ACTION) {
      public void actionPerformed(ActionEvent e) {
        displayCommunityInformation();
      }
    };

  public AbstractAction newCommunityAction = new AbstractAction(NEW_COMMUNITY_ACTION) {
      public void actionPerformed(ActionEvent e) {
        createCommunity();
      }
    };

  private AbstractAction viewCommunityInfoAction = new AbstractAction(VIEW_COMMUNITY_INFO_ACTION) {
      public void actionPerformed(ActionEvent e) {
        viewCommunityInfo();
      }
    };

  private AbstractAction addParameterAction = new AbstractAction(ADD_PARAMETER_ACTION) {
      public void actionPerformed(ActionEvent e) {
        addParameter();
      }
    };

  private AbstractAction deleteAction = new AbstractAction(DELETE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        delete();
      }
    };

  public CommunityPanel(Experiment experiment) {
    log = CSMART.createLogger(this.getClass().getName());
    this.experiment = experiment;
    communityTable = createTable();
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(new JScrollPane(communityTable));
    treePane = createTreeDisplay();
    splitPane.setRightComponent(treePane);
    splitPane.setDividerLocation(400);
    splitPane.validate();
    setLayout(new BorderLayout());
    add("Center", splitPane);
    JPanel topPanel = new JPanel();
    topPanel.add(new JLabel("Community Editor", SwingConstants.CENTER));
    add("North", topPanel);
  }

  public void reinit(Experiment experiment) {
    this.experiment = experiment;
    splitPane.remove(treePane);
    if (log.isDebugEnabled()) {
      if (experiment != null) 
	log.debug("reinit using Comm ASB " + experiment.getCommAsbID());
    }
    if (experiment != null)
      ((CommunityTable)communityTable).setAssemblyId(experiment.getCommAsbID());
    else
      ((CommunityTable)communityTable).setAssemblyId(null);
    treePane = createTreeDisplay();
    splitPane.setRightComponent(treePane);
  }

  private JTable createTable() {
    CommunityTable communityTable =  new CommunityTable();
    if (log.isDebugEnabled()) {
      if (experiment != null) 
	log.debug("createTable using Comm ASB " + experiment.getCommAsbID());
    }
    if (experiment != null)
      communityTable.setAssemblyId(experiment.getCommAsbID());
    communityTableUtils = (CommunityTableUtils)communityTable.getModel();
    return communityTable;
  }

  private JSplitPane createTreeDisplay() {
    DefaultMutableTreeNode root = 
      new DefaultMutableTreeNode(new CommunityTreeObject("Community"));
    DefaultTreeModel treeModel = new DefaultTreeModel(root, true) {
      public void valueForPathChanged(TreePath path, Object newValue) {
        if (newValue == null || newValue.toString().equals("")) return;
        DefaultMutableTreeNode node = 
          (DefaultMutableTreeNode)path.getLastPathComponent();
        CommunityTreeObject cto = 
          (CommunityTreeObject)node.getUserObject();
        cto.setLabel(newValue.toString());
        nodeChanged(node);
      }
    };
    communityTree = new CommunityDNDTree(treeModel);
    treeModelListener = new CommunityTreeModelListener(communityTableUtils, experiment.getCommAsbID());
    treeModel.addTreeModelListener(treeModelListener);
    communityTree.setExpandsSelectedPaths(true);
    communityTree.setCellEditor(treeCellEditor);
    communityTree.addTreeSelectionListener(new CommunityTreeSelectionListener(communityTableUtils));
    JScrollPane communityScrollPane = new JScrollPane(communityTree);

    // attach a mouse listener to the community tree to display menu 
    MouseListener communityTreeMouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) displayCommunityTreeMenu(e);
      }
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) displayCommunityTreeMenu(e);
      }
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) displayCommunityTreeMenu(e);
      }
    };
    communityTree.addMouseListener(communityTreeMouseListener);
    rootMenu = new JPopupMenu();
    rootMenu.add(new JMenuItem(newCommunityAction));
    communityMenu = new JPopupMenu();
    communityMenu.add(new JMenuItem(newCommunityAction));
    communityMenu.add(new JMenuItem(viewCommunityInfoAction));
    communityMenu.add(new JMenuItem(deleteAction));
    communityMenu.add(new JMenuItem(addParameterAction));
    parentEntityMenu = new JPopupMenu();
    parentEntityMenu.add(new JMenuItem(deleteAction));
    entityMenu = new JPopupMenu();
    entityMenu.add(new JMenuItem(addParameterAction));
    entityMenu.add(new JMenuItem(deleteAction));
    DefaultMutableTreeNode hostRoot = 
      new DefaultMutableTreeNode(new CommunityTreeObject("Hosts"));
    hostTree = new EntityDNDTree(new DefaultTreeModel(hostRoot, true));
    hostTree.setExpandsSelectedPaths(true);
    hostTree.setCellEditor(noEdit);
    JScrollPane hostScrollPane = new JScrollPane(hostTree);
    DefaultMutableTreeNode nodeRoot = 
      new DefaultMutableTreeNode(new CommunityTreeObject("Nodes (unassigned)"));    
    nodeTree = new EntityDNDTree(new DefaultTreeModel(nodeRoot, true));
    nodeTree.setExpandsSelectedPaths(true);
    nodeTree.setCellEditor(noEdit);
    JScrollPane nodeScrollPane = new JScrollPane(nodeTree);
    DefaultMutableTreeNode agentRoot = 
      new DefaultMutableTreeNode(new CommunityTreeObject("Agents (unassigned)"));
    agentTree = new EntityDNDTree(new DefaultTreeModel(agentRoot, true));
    agentTree.setExpandsSelectedPaths(true);
    agentTree.setCellEditor(noEdit);
    JScrollPane agentScrollPane = new JScrollPane(agentTree);
    JSplitPane paneOne = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JSplitPane paneTwo = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JSplitPane paneThree = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    paneThree.setTopComponent(nodeScrollPane);
    paneThree.setBottomComponent(agentScrollPane);
    paneTwo.setTopComponent(hostScrollPane);
    paneTwo.setBottomComponent(paneThree);
    paneOne.setTopComponent(communityScrollPane);
    paneOne.setBottomComponent(paneTwo);
    paneOne.setDividerLocation(100);
    paneTwo.setDividerLocation(100);
    paneThree.setDividerLocation(100);
    addHostsFromExperiment();
    addUnassignedNodesFromExperiment();
    addUnassignedAgentsFromExperiment();
    return paneOne;
  }

  /**
   * Ensure that display is up-to-date before showing it.
   */

  public void setVisible(boolean visible) {
    if (visible)
      update();
    super.setVisible(visible);
  }

  /**
   * Bring the display up-to-date by re-reading host/node/agent information
   * from the experiment.
   */
  public void update() {
    removeAllChildren(hostTree);
    removeAllChildren(nodeTree);
    removeAllChildren(agentTree);
    // Necessary? Must remove listener first?
    communityTree.getModel().removeTreeModelListener(treeModelListener);
    removeAllChildren(communityTree);
    communityTree.getModel().addTreeModelListener(treeModelListener);
    // get hosts, agents and nodes from experiment
    addHostsFromExperiment();
    putAllCommunitiesInTree();
    // add unassigned nodes to nodes tree
    addUnassignedNodesFromExperiment();
    // add unassigned agents to agents tree
    addUnassignedAgentsFromExperiment();
  }

  /**
   * Remove all children from a tree; called in update.
   */

  private void removeAllChildren(JTree tree) {
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
    root.removeAllChildren();
    model.nodeStructureChanged(root);
  }

  DefaultCellEditor noEdit = new DefaultCellEditor(new JTextField()) {
      public boolean isCellEditable(EventObject e) {
        return false;
      }
    };

  DefaultCellEditor treeCellEditor = new DefaultCellEditor(new JTextField()) {
      public boolean isCellEditable(EventObject e) {
        if (super.isCellEditable(e) && e instanceof MouseEvent) {
          TreePath path = 
            communityTree.getPathForLocation(((MouseEvent)e).getX(),
                                             ((MouseEvent)e).getY());
          if (path == null)
            return false;
	  Object o = path.getLastPathComponent();
          DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)o;
          return ((CommunityTreeObject)treeNode.getUserObject()).isCommunity();
        }
        return super.isCellEditable(e);
      }

      public boolean stopCellEditing() {
        TreePath path = communityTree.getEditingPath();
        DefaultMutableTreeNode node =
          (DefaultMutableTreeNode)path.getLastPathComponent();
        if (node.getUserObject() instanceof CommunityTreeObject) {
          CommunityTreeObject cto = (CommunityTreeObject)node.getUserObject();
          // TODO: check if name is unique
          String newName = (String)getCellEditorValue();
          return super.stopCellEditing();
        } 
        return false;
      }
    };


  private void displayCommunityTreeMenu(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();
    TreePath path = communityTree.getPathForLocation(x, y);
    if (path == null)
      return;
    // select and act on the node that the mouse is pointing at
    TreePath[] selectedPaths = communityTree.getSelectionPaths();
    if (!communityTree.isPathSelected(path) || selectedPaths.length > 1)
      communityTree.setSelectionPath(path);
    DefaultMutableTreeNode node = 
      (DefaultMutableTreeNode)path.getLastPathComponent();
    CommunityTreeObject cto = (CommunityTreeObject)node.getUserObject();
    if (cto.isRoot())
      rootMenu.show(communityTree, x, y);
    else if (cto.isCommunity())
      communityMenu.show(communityTree, x, y);
    else if (node.getChildCount() != 0)
      parentEntityMenu.show(communityTree, x, y);
    else
      entityMenu.show(communityTree, x, y);
  }

  private void createCommunity() {
    String communityName = JOptionPane.showInputDialog("New community name: ");
    if (communityName == null || communityName.length() == 0)
      return;
    // TODO: check that name is unique
    String[] communityTypes = new String[] { "Domain", "Robustness", "Security", "Restart" };
    JComboBox cb = new JComboBox(communityTypes);
    cb.setEditable(true);
    JPanel panel = new JPanel();
    panel.add(new JLabel("Community Type:"));
    panel.add(cb);
    int result = 
      JOptionPane.showConfirmDialog(null, panel, "Community Type",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION)
      return;
    String communityType = (String)cb.getSelectedItem();
    DefaultTreeModel model = (DefaultTreeModel)communityTree.getModel();
    CommunityTreeObject cto = 
      new CommunityTreeObject(communityName, "Community");
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(cto, true);
    TreePath selectedPath = communityTree.getSelectionPath();
    // if nothing in community tree selected, add new community to root
    if (selectedPath == null) {
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
      model.insertNodeInto(node, root, root.getChildCount());
    } else {
      DefaultMutableTreeNode selectedNode = 
        (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
      CommunityTreeObject selectedObject =
        (CommunityTreeObject)selectedNode.getUserObject();
      // if community is selected, add new community as its child
      if (selectedObject.isCommunity())
        model.insertNodeInto(node, selectedNode, selectedNode.getChildCount());
      else {
        // if non-community is selected, add new community to root
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        model.insertNodeInto(node, root, root.getChildCount());
      }
    }
    communityTree.scrollPathToVisible(new TreePath(node.getPath()));
    // add community info to database
    CommunityDBUtils.insertCommunityInfo(communityName, communityType, experiment.getCommAsbID());
    // populate table by retrieving new info from database
    communityTableUtils.getCommunityInfo(communityName);
  }

  /**
   * View all the information for the selected community.
   */
  private void viewCommunityInfo() {
    TreePath selectedPath = communityTree.getSelectionPath();
    if (selectedPath == null)
      return;
    DefaultMutableTreeNode selectedNode =
      (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
    CommunityTreeObject userObject =
      (CommunityTreeObject)selectedNode.getUserObject();
    String communityName = userObject.toString();
    communityTableUtils.getAllCommunityInfo(communityName);
  }

  /**
   * Delete the community or entity selected in the tree.
   * Tell the model listener to update it's hashtable and the database.
   */
  private void delete() {
    TreePath selectedPath = communityTree.getSelectionPath();
    if (selectedPath == null)
      return;
    DefaultMutableTreeNode selectedNode =
      (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
    ((DefaultTreeModel)communityTree.getModel()).removeNodeFromParent(selectedNode);
    treeModelListener.removeBranch(selectedNode);
  }

  /**
   * Add parameter to community_attribute for community
   * or to community_entity_attribute for entity.
   */
  private void addParameter() {
    TreePath selectedPath = communityTree.getSelectionPath();
    if (selectedPath == null)
      return;
    DefaultMutableTreeNode selectedNode =
      (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
    CommunityTreeObject cto = 
      (CommunityTreeObject)selectedNode.getUserObject();
    if (cto.isCommunity()) {
      String communityName = cto.toString();
      CommunityDBUtils.insertCommunityAttribute(communityName, experiment.getCommAsbID());
      // populate table by retrieving new info from database
      communityTableUtils.getCommunityInfo(communityName);
    } else {
      String communityName = cto.getCommunityName();
      String entityName = cto.toString();
      if (communityName == null)
        return;
      CommunityDBUtils.insertEntityAttribute(communityName, entityName, experiment.getCommAsbID());
      // populate table by retrieving new info from database
      communityTableUtils.getEntityInfo(communityName, entityName);
    }
  }

  /**
   * Add hosts and their nodes and agents from an experiment.
   */

  private void addHostsFromExperiment() {
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)hostTree.getModel().getRoot();
    DefaultTreeModel model = (DefaultTreeModel)hostTree.getModel();
    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      HostComponent hostComponent = hosts[i];
      CommunityTreeObject cto = new CommunityTreeObject(hostComponent);
      DefaultMutableTreeNode hostNode = new DefaultMutableTreeNode(cto, true);
      model.insertNodeInto(hostNode, root, root.getChildCount());
      NodeComponent[] nodes = hostComponent.getNodes();
      for (int j = 0; j < nodes.length; j++) {
	NodeComponent nodeComponent = nodes[j];
	cto = new CommunityTreeObject(nodeComponent);
	DefaultMutableTreeNode nodeTreeNode = 
	  new DefaultMutableTreeNode(cto, true);
	model.insertNodeInto(nodeTreeNode, hostNode, hostNode.getChildCount());
	AgentComponent[] agents = nodeComponent.getAgents();
	for (int k = 0; k < agents.length; k++) {
	  AgentComponent agentComponent = agents[k];
	  cto = new CommunityTreeObject(agentComponent);
	  DefaultMutableTreeNode agentNode = 
	    new DefaultMutableTreeNode(cto, false);
	  model.insertNodeInto(agentNode, nodeTreeNode,
			       nodeTreeNode.getChildCount());
	}
      }
    }
  }

  /**
   * Add unassigned nodes from experiment to unassigned nodes tree.
   */
  private void addUnassignedNodesFromExperiment() {
    Set unassignedNodes;
    unassignedNodes = new TreeSet(dbBaseComponentComparator);
    HostComponent[] hosts = experiment.getHostComponents();
    NodeComponent[] nodes = experiment.getNodeComponents();
    unassignedNodes.addAll(Arrays.asList(nodes));
    for (int i = 0; i < hosts.length; i++)
      unassignedNodes.removeAll(Arrays.asList(hosts[i].getNodes()));
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)nodeTree.getModel().getRoot();
    DefaultTreeModel model = (DefaultTreeModel)nodeTree.getModel();
    Iterator iter = unassignedNodes.iterator();
    while (iter.hasNext()) {
      NodeComponent nodeComponent = (NodeComponent)iter.next();
      CommunityTreeObject cto = new CommunityTreeObject(nodeComponent);
      DefaultMutableTreeNode newNodeTreeNode = 
	new DefaultMutableTreeNode(cto, true);
      model.insertNodeInto(newNodeTreeNode, root, root.getChildCount());
      AgentComponent[] agents = nodeComponent.getAgents();
      for (int j = 0; j < agents.length; j++) {
	AgentComponent agentComponent = agents[j];
	cto = new CommunityTreeObject(agentComponent);
	DefaultMutableTreeNode newAgentNode = 
	  new DefaultMutableTreeNode(cto, false);
	model.insertNodeInto(newAgentNode, newNodeTreeNode, 
			     newNodeTreeNode.getChildCount());
      }
    }
  }

  // In general, agent names from built in societies are complex
  // and those from the db are short
  // The complex ones should be compared in full,
  // while the DB ones should only be compared in short versions
  // And Nodes are also short only
  // Failing to compare only the short names when using DB societies
  // results in agents erroneously appearing 2x, once unassigned

  private static Comparator dbBaseComponentComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      BaseComponent c1 = (BaseComponent) o1;
      BaseComponent c2 = (BaseComponent) o2;
      return c1.getShortName().compareTo(c2.getShortName());
    }
  };

  /**
   * Add unassigned agents to unassigned agents tree.
   */

  private void addUnassignedAgentsFromExperiment() {
    Set unassignedAgents;
    unassignedAgents = new TreeSet(dbBaseComponentComparator);
    AgentComponent[] agents = experiment.getAgents();
    NodeComponent[] nodes = experiment.getNodeComponents();
    unassignedAgents.addAll(Arrays.asList(agents));
    for (int i = 0; i < nodes.length; i++) {
      List assignedAgents = Arrays.asList(nodes[i].getAgents());
      unassignedAgents.removeAll(assignedAgents);
    }
    DefaultTreeModel model = (DefaultTreeModel)agentTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
    Iterator iter = unassignedAgents.iterator();
    while (iter.hasNext()) {
      AgentComponent agentComponent = (AgentComponent)iter.next();
      CommunityTreeObject cto = new CommunityTreeObject(agentComponent);
      DefaultMutableTreeNode newNode =
	new DefaultMutableTreeNode(cto, false);
      model.insertNodeInto(newNode, root, root.getChildCount());
    }
  }

  private String selectCommunityToDisplay() {
    // This version shows all communities anywhere
    //    ArrayList communityNames = CommunityDBUtils.getCommunities();
    // But I think I only want those in this experiment
    ArrayList communityNames = CommunityDBUtils.getCommunitiesForExperiment(experiment.getCommAsbID());
    if (communityNames == null || communityNames.size() == 0) {
      if (log.isDebugEnabled()) {
	log.debug("selectComms got none");
      }
      // Show a message to that effect
      JOptionPane.showMessageDialog(null, "No Communities defined for this Experiment.");
      return null;
    }

    // Add special community name meaning show all communities...
    communityNames.add(0, "All");

    JComboBox cb = 
      new JComboBox(communityNames.toArray(new String[communityNames.size()]));
    cb.setEditable(false);
    JPanel panel = new JPanel();
    panel.add(new JLabel("Select Community:"));
    panel.add(cb);
    int result =
      JOptionPane.showConfirmDialog(null, panel, "Community",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION)
      return null;
    String communityName = (String)cb.getSelectedItem();
    communityName = communityName.trim();
    if (communityName.length() == 0)
      return null;
    return communityName;
  }

  // method invoked from view-community menu item
  // put the community in the tree and display info about it
  // disable tree model listener so it doesn't try to update the database
  private void displayCommunityInformation() {
//     if (log.isDebugEnabled()) {
//       log.debug("displayCommunityInformation called");
//     }
    String communityName = selectCommunityToDisplay();
    if (communityName == null)
      return;

    if (communityName.equals("All")) {
      putAllCommunitiesInTree();
      return;
    }

    // display community information in table
    communityTableUtils.getAllCommunityInfo(communityName);
    communityTree.getModel().removeTreeModelListener(treeModelListener);

    // Note that this clears out any communities currently being displayed!
    addToTree(communityTree.addNode(null, communityName, "Community", null),
              communityName);
    communityTree.getModel().addTreeModelListener(treeModelListener);
  }

  private void putAllCommunitiesInTree() {
    ArrayList communityNames = CommunityDBUtils.getCommunitiesForExperiment(experiment.getCommAsbID());
    if (communityNames == null || communityNames.size() == 0)
      return;
    communityTree.getModel().removeTreeModelListener(treeModelListener);
    for (Iterator iter = communityNames.iterator(); iter.hasNext(); ) {
      String communityName = (String)iter.next();
      communityName = communityName.trim();
      if (communityName.length() > 0) {
// 	if (log.isDebugEnabled()) {
// 	  log.debug("putAllComms adding " + communityName);
// 	}
	// This first argument must point to the tree root
	addToTree(communityTree.addNode((DefaultMutableTreeNode)communityTree.getModel().getRoot(), communityName, "Community", null),
		  communityName);
      }
    }
    communityTree.getModel().addTreeModelListener(treeModelListener);
  }

  private void addToTree(DefaultMutableTreeNode node, String communityName) {
    ArrayList entityNames = CommunityDBUtils.getEntities(communityName, experiment.getCommAsbID());
    if (entityNames == null || entityNames.size() == 0)
      return;
    for (int i = 0; i < entityNames.size(); i++) {
      String entityName = (String)entityNames.get(i);
      String entityType = CommunityDBUtils.getEntityType(entityName, experiment.getCommAsbID());
      DefaultMutableTreeNode newNode = 
        communityTree.addNode(node, entityName, entityType, communityName);
      if (entityType.equals("Community"))
        addToTree(newNode, entityName);
    }
  }

}

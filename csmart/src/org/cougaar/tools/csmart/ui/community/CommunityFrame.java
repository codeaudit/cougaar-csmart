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
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.util.NamedFrame;

public class CommunityFrame extends JFrame {
  // database queries
  private static final String GET_COMMUNITIES_QUERY = "Select community_id from community_entity_attribute";
  private static final String GET_ENTITIES_QUERY = "Select entity_id from community_entity_attribute where community_entity_attribute.community_id ='";
  private static final String GET_MEMBER_TYPE_QUERY = "Select attribute_value from community_entity_attribute where community_entity_attribute.entity_id = '";
  private static final String GET_ALL_COMMUNITY_INFO_QUERY = "Select community_entity_attribute.community_id, community_entity_attribute.entity_id, community_entity_attribute.attribute_id, community_entity_attribute.attribute_value, community_attribute.attribute_id, community_attribute.attribute_value from community_entity_attribute, community_attribute where community_attribute.community_id=community_entity_attribute.community_id and community_attribute.community_id='";
  public static final String GET_COMMUNITY_INFO_QUERY = "Select * from community_attribute where community_attribute.community_id='";
  public static final String GET_ENTITY_INFO_QUERY = "Select entity_id, attribute_id, attribute_value from community_entity_attribute where community_entity_attribute.community_id = '";
  public static final String INSERT_COMMUNITY_INFO_QUERY = "Insert into community_attribute values ('";
  public static final String INSERT_ENTITY_INFO_QUERY = "Insert into community_entity_attribute values ('";
  public static final String DELETE_ENTITY_INFO_QUERY = "Delete from community_entity_attribute where community_id = '";
  private static final String FILE_MENU = "File";
  private static final String CLOSE_ACTION = "Close";
  private static final String VIEW_MENU = "View";
  private static final String VIEW_COMMUNITY_ACTION = "Community";
  private static final String HELP_MENU = "Help";
  private static final String HELP_DOC = "help.html";
  private static final String HELP_ACTION = "Help";
  private static final String ABOUT_ACTION = "About CSMART";
  private static final String ABOUT_DOC = "../../help/about-csmart.html";
  private static final String NEW_COMMUNITY_ACTION = "New Community";
  private static final String VIEW_COMMUNITY_INFO_ACTION = "Show All Parameters";
  private static final String ADD_PARAMETER_ACTION = "Add Parameter";
  private static final String DELETE_ACTION = "Delete";

  private CommunityDNDTree communityTree;
  private CommunityTreeModelListener treeModelListener;
  private JTable communityTable;
  private CommunityTableUtils communityTableUtils;
  private JPopupMenu rootMenu;
  private JPopupMenu communityMenu;
  private JPopupMenu entityMenu;

  private Action closeAction = new AbstractAction(CLOSE_ACTION) {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    };

  private Action viewCommunityAction = new AbstractAction(VIEW_COMMUNITY_ACTION) {
      public void actionPerformed(ActionEvent e) {
        displayCommunityInformation();
      }
    };

  private Action helpAction = new AbstractAction(HELP_ACTION) {
      public void actionPerformed(ActionEvent e) {
        URL help = (URL)CommunityFrame.class.getResource(HELP_DOC);
        if(help != null)
          Browser.setPage(help);
      }
    };

  private Action aboutAction = new AbstractAction(ABOUT_ACTION) {
      public void actionPerformed(ActionEvent e) {
        URL help = (URL)CommunityFrame.class.getResource(ABOUT_DOC);
        if(help != null)
          Browser.setPage(help);
      }
    };

  // actions on pop-up menus
  private AbstractAction newCommunityAction = new AbstractAction(NEW_COMMUNITY_ACTION) {
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

  public CommunityFrame(Experiment experiment) {
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.add(new JMenuItem(closeAction));
    menuBar.add(fileMenu);
    JMenu viewMenu = new JMenu(VIEW_MENU);
    viewMenu.add(new JMenuItem(viewCommunityAction));
    menuBar.add(viewMenu);
    JMenu helpMenu = new JMenu(HELP_MENU);
    helpMenu.add(new JMenuItem(helpAction));
    helpMenu.add(new JMenuItem(aboutAction));
    menuBar.add(helpMenu);
    getRootPane().setJMenuBar(menuBar);

    communityTable = createTable();
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(new JScrollPane(communityTable));
    JSplitPane treePane = createTreeDisplay(experiment);
    splitPane.setRightComponent(treePane);
    splitPane.setDividerLocation(300);
    splitPane.validate();
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add("Center", splitPane);
    JPanel topPanel = new JPanel();
    topPanel.add(new JLabel("Community Editor", SwingConstants.CENTER));
    getContentPane().add("North", topPanel);
    pack();
    setVisible(true);
    // for debugging
    //    displayQueryFrame();
  }

  private void close() {
    NamedFrame.getNamedFrame().removeFrame(CommunityFrame.this);
    dispose();
  }

  private JTable createTable() {
    CommunityTable communityTable =  new CommunityTable();
    communityTableUtils = (CommunityTableUtils)communityTable.getModel();
    return communityTable;
  }

  private JSplitPane createTreeDisplay(Experiment experiment) {
    DefaultMutableTreeNode root = 
      new DefaultMutableTreeNode(new CommunityTreeObject("Community", 
                                 CommunityTreeObject.class), true);
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
    treeModelListener = new CommunityTreeModelListener(communityTableUtils);
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
    entityMenu = new JPopupMenu();
    entityMenu.add(new JMenuItem(addParameterAction));
    entityMenu.add(new JMenuItem(deleteAction));
    DefaultMutableTreeNode hostRoot = 
      new DefaultMutableTreeNode(new CommunityTreeObject("Hosts", 
                    org.cougaar.tools.csmart.experiment.HostComponent.class),
                                 true);
    JTree hostTree = 
      new EntityDNDTree(new DefaultTreeModel(hostRoot, true));
    hostTree.setExpandsSelectedPaths(true);
    hostTree.setCellEditor(noEdit);
    JScrollPane hostScrollPane = new JScrollPane(hostTree);
    DefaultMutableTreeNode nodeRoot = 
      new DefaultMutableTreeNode(new CommunityTreeObject("Nodes (unassigned)", 
                    org.cougaar.tools.csmart.experiment.NodeComponent.class),
                                 true);
    JTree nodeTree = 
      new EntityDNDTree(new DefaultTreeModel(nodeRoot, true));
    nodeTree.setExpandsSelectedPaths(true);
    nodeTree.setCellEditor(noEdit);
    JScrollPane nodeScrollPane = new JScrollPane(nodeTree);
    DefaultMutableTreeNode agentRoot = 
      new DefaultMutableTreeNode(new CommunityTreeObject("Agents (unassigned)", 
                    org.cougaar.tools.csmart.society.AgentComponent.class),
                                 true);
    JTree agentTree = 
      new EntityDNDTree(new DefaultTreeModel(agentRoot, true));
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
    paneOne.setDividerLocation(150);
    paneTwo.setDividerLocation(150);
    paneThree.setDividerLocation(150);
    addHostsFromExperiment(hostTree, experiment);
    addUnassignedNodesFromExperiment(nodeTree, experiment);
    addUnassignedAgentsFromExperiment(agentTree, experiment);
    return paneOne;
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
    else
      entityMenu.show(communityTree, x, y);
  }

  private void createCommunity() {
    String communityName = JOptionPane.showInputDialog("New community name: ");
    if (communityName == null || communityName.length() == 0)
      return;
    // TODO: check that name is unique
    String[] communityTypes = new String[] { "Domain", "Robustness" };
    JComboBox cb = new JComboBox(communityTypes);
    cb.setEditable(false);
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
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(cto);
    TreePath selectedPath = communityTree.getSelectionPath();
    if (selectedPath == null) {
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
      model.insertNodeInto(node, root, root.getChildCount());
    } else {
      DefaultMutableTreeNode selectedNode = 
        (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
      model.insertNodeInto(node, selectedNode, selectedNode.getChildCount());
    }
    communityTree.scrollPathToVisible(new TreePath(node.getPath()));
    // add community info to database
    String query = INSERT_COMMUNITY_INFO_QUERY + 
                   communityName + "'," +
                   " 'CommunityType', " +
                   " '" +  communityType + "')";
    communityTableUtils.executeQuery(query);
    query = CommunityFrame.GET_COMMUNITY_INFO_QUERY + communityName + "'";
    // populate table by retrieving new info from database
    communityTableUtils.executeQuery(query);
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
    String query = GET_ALL_COMMUNITY_INFO_QUERY + communityName + "'";
    communityTableUtils.executeQuery(query);
  }

  /**
   * Delete the community or entity selected in the tree.
   * Just removes it from the model, the model listener updates the database.
   */
  private void delete() {
    TreePath selectedPath = communityTree.getSelectionPath();
    if (selectedPath == null)
      return;
    DefaultMutableTreeNode selectedNode =
      (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
    ((DefaultTreeModel)communityTree.getModel()).removeNodeFromParent(selectedNode);
  }

  /**
   * Add new empty row to table.
   */
  private void addParameter() {
    communityTableUtils.addRow();
  }

  /**
   * Add hosts and their nodes and agents from an experiment.
   */

  private void addHostsFromExperiment(JTree hostTree, Experiment experiment) {
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
  private void addUnassignedNodesFromExperiment(JTree nodeTree, 
                                                Experiment experiment) {
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

  private void addUnassignedAgentsFromExperiment(JTree agentTree,
                                                 Experiment experiment) {
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
    ArrayList communityNames = 
      CommunityDBUtils.getQueryResults(GET_COMMUNITIES_QUERY);
    if (communityNames == null || communityNames.size() == 0)
      return null;
    Collections.sort(communityNames);
    JComboBox cb = new JComboBox(communityNames.toArray(new String[communityNames.size()]));
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
    String communityName = selectCommunityToDisplay();
    if (communityName == null)
      return;
    // display community information in table
    String query = GET_ALL_COMMUNITY_INFO_QUERY + communityName + "'";
    communityTableUtils.executeQuery(query);
    communityTree.getModel().removeTreeModelListener(treeModelListener);
    addToTree(communityTree.addNode(null, communityName, "Community"),
              communityName);
    communityTree.getModel().addTreeModelListener(treeModelListener);
  }

  private void addToTree(DefaultMutableTreeNode node, String communityName) {
    ArrayList entityNames =
      CommunityDBUtils.getQueryResults(GET_ENTITIES_QUERY + 
                                       communityName + "'");
    if (entityNames == null || entityNames.size() == 0)
      return;
    for (int i = 0; i < entityNames.size(); i++) {
      String entityName = (String)entityNames.get(i);
      ArrayList results =
        CommunityDBUtils.getQueryResults(GET_MEMBER_TYPE_QUERY + entityName +
           "' and community_entity_attribute.attribute_id = 'MemberType'");
      String entityType = "Entity";
      if (results.size() != 0)
        entityType = (String)results.get(0);
      DefaultMutableTreeNode newNode = 
        communityTree.addNode(node, entityName, entityType);
      if (entityType.equals("Community"))
        addToTree(newNode, entityName);
    }
  }

  // for debugging, displays a frame in which the user can enter
  // a sql query

  private void displayQueryFrame() {
    JPanel queryPanel = new JPanel(new BorderLayout());
    // Create the query text area and label.
    final JTextArea queryTextArea = 
      new JTextArea("SELECT * FROM community_entity_attribute", 25, 25);
    JScrollPane queryPane = new JScrollPane(queryTextArea);
    queryPanel.add("Center", queryPane);
    JButton fetchButton = new JButton("Fetch");
    fetchButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          communityTableUtils.executeQuery(queryTextArea.getText());
        }
      });
    queryPanel.add("South", fetchButton);

    // Create a Frame and put the panel in it.
    JFrame frame = new JFrame("SQL Query");
    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}});
    frame.getContentPane().add("Center", queryPanel);
    frame.pack();
    frame.setVisible(true);
    frame.setBounds(200, 200, 640, 480);
  }

}

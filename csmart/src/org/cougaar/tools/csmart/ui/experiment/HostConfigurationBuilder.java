/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.experiment;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import javax.swing.tree.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.cougaar.tools.csmart.ui.component.AgentComponent;
import org.cougaar.tools.csmart.ui.component.ConfigurableComponent;
import org.cougaar.tools.csmart.ui.component.HostComponent;
import org.cougaar.tools.csmart.ui.component.NodeComponent;
import org.cougaar.tools.csmart.ui.component.SocietyComponent;
import org.cougaar.tools.csmart.ui.configuration.ConsoleDNDTree;
import org.cougaar.tools.csmart.ui.configuration.ConsoleTreeObject;
import org.cougaar.tools.csmart.ui.experiment.Experiment;
import org.cougaar.tools.csmart.ui.tree.DNDTree;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

public class HostConfigurationBuilder extends JPanel implements TreeModelListener {
  Experiment experiment;
  SocietyComponent societyComponent;
  JPopupMenu hostRootMenu;
  JPopupMenu hostHostMenu;
  JPopupMenu hostNodeMenu;
  JPopupMenu nodeRootMenu;
  JPopupMenu nodeNodeMenu;
  DNDTree hostTree;
  DNDTree nodeTree;
  DNDTree agentTree;
  // menu items for popup menu in hostTree
  private static final String NEW_HOST_MENU_ITEM = "New Host";
  private static final String NEW_NODE_MENU_ITEM = "New Node";
  private static final String DELETE_MENU_ITEM = "Remove";
  //  private static final String DESCRIBE_MENU_ITEM = "Describe";

  public HostConfigurationBuilder(Experiment experiment) {
    initDisplay();
    setExperiment(experiment);
  }

  private void initDisplay() {
    // host split pane contains host tree and 
    // the bottom split pane which contains the node and agent trees
    JSplitPane hostPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    // tree of hosts and assigned nodes and agents
    DefaultMutableTreeNode root = 
      new DefaultMutableTreeNode(new ConsoleTreeObject("Hosts", 
		       "org.cougaar.tools.csmart.ui.component.HostComponent"));
    // setting "askAllowsChildren" forces empty nodes that can have
    // children to be displayed as "folders" rather than leaf nodes
    DefaultTreeModel model = createModel(experiment, root, true);
    hostTree = new ConsoleDNDTree(model);
    hostTree.setExpandsSelectedPaths(true);

    // get hosts, agents and nodes from experiment
    //    addHostsFromExperiment();
    // create new host components for hosts named in config file
    //    addHostsFromFile();

    JScrollPane hostTreeScrollPane = new JScrollPane(hostTree);
    hostTreeScrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    hostPane.setTopComponent(hostTreeScrollPane);
    //    hostTree.getModel().addTreeModelListener(this);

    // create popup menus for host tree
    hostRootMenu = new JPopupMenu();
    hostHostMenu = new JPopupMenu();
    hostNodeMenu = new JPopupMenu();
    JMenuItem newHostMenuItem = new JMenuItem(NEW_HOST_MENU_ITEM);
    newHostMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	newHostMenuItem_actionPerformed(e);
      }
    });
    JMenuItem deleteHostMenuItem = new JMenuItem(DELETE_MENU_ITEM);
    deleteHostMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	deleteHostMenuItem_actionPerformed(e);
      }
    });
    JMenuItem newNodeInHostMenuItem = new JMenuItem(NEW_NODE_MENU_ITEM);
    newNodeInHostMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	newNodeInHostMenuItem_actionPerformed(e);
      }
    });
    JMenuItem deleteNodeInHostMenuItem = new JMenuItem(DELETE_MENU_ITEM);
    deleteNodeInHostMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	deleteNodeInHostMenuItem_actionPerformed(e);
      }
    });
    // init pop-up menus
    // TODO: handle describe menu items
    hostRootMenu.add(newHostMenuItem);

    hostHostMenu.add(newNodeInHostMenuItem);
    hostHostMenu.add(deleteHostMenuItem);

    hostNodeMenu.add(deleteNodeInHostMenuItem);
    
    // attach a mouse listener to the host tree to display menu 
    MouseListener hostTreeMouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
	if (!hostTree.isEditable()) return;
	if (e.isPopupTrigger()) displayHostTreeMenu(e);
      }
      public void mousePressed(MouseEvent e) {
	if (!hostTree.isEditable()) return;
	if (e.isPopupTrigger()) displayHostTreeMenu(e);
      }
      public void mouseReleased(MouseEvent e) {
	if (!hostTree.isEditable()) return;
	if (e.isPopupTrigger()) displayHostTreeMenu(e);
      }
    };
    hostTree.addMouseListener(hostTreeMouseListener);

    // bottom split pane contains the node and agent trees
    JSplitPane bottomPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    // tree of unassigned nodes
    ConsoleTreeObject cto = new ConsoleTreeObject("Unassigned Nodes", 
                 "org.cougaar.tools.csmart.ui.component.NodeComponent");
    root = new DefaultMutableTreeNode(cto, true);
    model = createModel(experiment, root, true);
    nodeTree = new ConsoleDNDTree(model);
    nodeTree.setExpandsSelectedPaths(true);
    //    addUnassignedNodesFromExperiment();
    //    nodeTree.getModel().addTreeModelListener(this);
    JScrollPane nodeTreeScrollPane = new JScrollPane(nodeTree);
    nodeTreeScrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    bottomPane.setTopComponent(nodeTreeScrollPane);
    // popup menu for creating and deleting nodes
    nodeRootMenu = new JPopupMenu();
    nodeNodeMenu = new JPopupMenu();
    JMenuItem newNodeMenuItem = new JMenuItem(NEW_NODE_MENU_ITEM);
    newNodeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	newNodeMenuItem_actionPerformed(e);
      }
    });
    nodeRootMenu.add(newNodeMenuItem);
    JMenuItem deleteNodeMenuItem = new JMenuItem(DELETE_MENU_ITEM);
    deleteNodeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	deleteNodeMenuItem_actionPerformed(e);
      }
    });
    nodeNodeMenu.add(deleteNodeMenuItem);

    // attach a mouse listener to the node tree to display menu 
    MouseListener nodeTreeMouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
	if (!nodeTree.isEditable()) return;
	if (e.isPopupTrigger()) displayNodeTreeMenu(e);
      }
      public void mousePressed(MouseEvent e) {
	if (!nodeTree.isEditable()) return;
	if (e.isPopupTrigger()) displayNodeTreeMenu(e);
      }
      public void mouseReleased(MouseEvent e) {
	if (!nodeTree.isEditable()) return;
	if (e.isPopupTrigger()) displayNodeTreeMenu(e);
      }
    };
    nodeTree.addMouseListener(nodeTreeMouseListener);

    // tree of unassigned agents
    cto = new ConsoleTreeObject("Unassigned Agents", 
		"org.cougaar.tools.csmart.ui.component.AgentComponent");
    root = new DefaultMutableTreeNode(cto, true);
    model = createModel(experiment, root, true);
    agentTree = new ConsoleDNDTree(model);
    agentTree.setExpandsSelectedPaths(true);
    //    addUnassignedAgentsFromExperiment();
    JScrollPane agentTreeScrollPane = new JScrollPane(agentTree);
    agentTreeScrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    bottomPane.setBottomComponent(agentTreeScrollPane);
    hostPane.setBottomComponent(bottomPane);

    hostTree.setEditable(true);
    nodeTree.setEditable(true);
    agentTree.setEditable(true);

    // fully expand trees
    //    expandTree(hostTree);
    //    expandTree(nodeTree);
    //    expandTree(agentTree);

    setLayout(new BorderLayout());
    add(hostPane, BorderLayout.CENTER);
    hostPane.setDividerLocation(100);
    bottomPane.setDividerLocation(100);
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
   * Set display to show a new experiment.
   */

  public void setExperiment(Experiment experiment) {
    this.experiment = experiment;
  }

  private void update() {
    if (experiment.getSocietyComponentCount() != 0)
      societyComponent = experiment.getSocietyComponent(0);
    else
      societyComponent = null;
    hostTree.getModel().removeTreeModelListener(this);
    nodeTree.getModel().removeTreeModelListener(this);
    removeAllChildren(hostTree);
    removeAllChildren(nodeTree);
    removeAllChildren(agentTree);
    if (societyComponent != null) {
      // get hosts, agents and nodes from experiment
      addHostsFromExperiment();
      // create new host components for hosts named in config file
      addHostsFromFile();
      // add unassigned nodes to nodes tree
      addUnassignedNodesFromExperiment();
      // add unassigned agents to agents tree
      addUnassignedAgentsFromExperiment();
      // fully expand trees
      expandTree(hostTree);
      expandTree(nodeTree);
      expandTree(agentTree);
    }
    hostTree.getModel().addTreeModelListener(this);
    nodeTree.getModel().addTreeModelListener(this);
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

  /**
   * Fully expand the tree; called in initialization
   * so that the initial view of the tree is fully expanded.
   */

  private void expandTree(JTree tree) {
    Enumeration nodes = 
      ((DefaultMutableTreeNode)tree.getModel().getRoot()).depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      tree.expandPath(new TreePath(node.getPath()));
    }
  }

  /**
   * Add hosts and their nodes and agents from an experiment.
   */

  private void addHostsFromExperiment() {
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)hostTree.getModel().getRoot();
    DefaultTreeModel model = (DefaultTreeModel)hostTree.getModel();
    HostComponent[] hosts = experiment.getHosts();
    for (int i = 0; i < hosts.length; i++) {
      HostComponent hostComponent = hosts[i];
      ConsoleTreeObject cto = new ConsoleTreeObject(hostComponent);
      DefaultMutableTreeNode hostNode = new DefaultMutableTreeNode(cto, true);
      model.insertNodeInto(hostNode, root, root.getChildCount());
      NodeComponent[] nodes = hostComponent.getNodes();
      for (int j = 0; j < nodes.length; j++) {
	NodeComponent nodeComponent = nodes[j];
	cto = new ConsoleTreeObject(nodeComponent);
	DefaultMutableTreeNode nodeTreeNode = 
	  new DefaultMutableTreeNode(cto, true);
	model.insertNodeInto(nodeTreeNode, hostNode, hostNode.getChildCount());
	AgentComponent[] agents = nodeComponent.getAgents();
	for (int k = 0; k < agents.length; k++) {
	  AgentComponent agentComponent = agents[k];
	  cto = new ConsoleTreeObject(agentComponent);
	  DefaultMutableTreeNode agentNode = 
	    new DefaultMutableTreeNode(cto, false);
	  model.insertNodeInto(agentNode, nodeTreeNode,
			       nodeTreeNode.getChildCount());
	}
      }
    }
  }

  /**
   * Create host components for hosts read from a text file.
   */

  private void addHostsFromFile() {
    String pathName = Util.getPath("hosts.txt");
    if (pathName == null)
      return;

    java.util.List hosts = new ArrayList();
    RandomAccessFile hostFile = null;
    // read hosts, one per line
    try { 
      hostFile = new RandomAccessFile(pathName, "r");      
      while (true) {
	String ihost = hostFile.readLine(); // get their name       
	if (ihost == null) {
	  break;
	}
	hosts.add(ihost);
      }
      hostFile.close();
    } catch (IOException e) {
      System.err.println("Error during read/open from file: " + pathName + 
			 " " + e.toString());
      return;
    } 
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)hostTree.getModel().getRoot();
    DefaultTreeModel model = (DefaultTreeModel)hostTree.getModel();
    DefaultMutableTreeNode hostNode = null;
    HostComponent[] hostsInExperiment = experiment.getHosts();
    Vector hostNames = new Vector(hostsInExperiment.length);
    for (int i = 0; i < hostsInExperiment.length; i++) 
      hostNames.add(hostsInExperiment[i].toString());
    for (int i = 0; i < hosts.size(); i++) {
      if (hostNames.contains((String)hosts.get(i)))
	continue; // this host already exists in the experiment
      HostComponent hostComponent = 
	experiment.addHost((String)hosts.get(i));
      ConsoleTreeObject cto = new ConsoleTreeObject(hostComponent);
      hostNode = new DefaultMutableTreeNode(cto, true);
      model.insertNodeInto(hostNode, root, root.getChildCount());
    }
    if (hostNode != null)
      hostTree.scrollPathToVisible(new TreePath(hostNode.getPath()));
  }
  
  /**
   * Add unassigned nodes from experiment to unassigned nodes tree.
   */

  private void addUnassignedNodesFromExperiment() {
    Set unassignedNodes = new TreeSet(configurableComponentComparator);
    HostComponent[] hosts = experiment.getHosts();
    NodeComponent[] nodes = experiment.getNodes();
    unassignedNodes.addAll(Arrays.asList(nodes));
    for (int i = 0; i < hosts.length; i++)
      unassignedNodes.removeAll(Arrays.asList(hosts[i].getNodes()));
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)nodeTree.getModel().getRoot();
    DefaultTreeModel model = (DefaultTreeModel)nodeTree.getModel();
    Iterator iter = unassignedNodes.iterator();
    while (iter.hasNext()) {
      NodeComponent node = (NodeComponent)iter.next();
      ConsoleTreeObject cto = new ConsoleTreeObject(node);
      DefaultMutableTreeNode newNodeTreeNode = 
	new DefaultMutableTreeNode(cto, true);
      model.insertNodeInto(newNodeTreeNode, root, root.getChildCount());
      AgentComponent[] agents = node.getAgents();
      for (int j = 0; j < agents.length; j++) {
	AgentComponent agentComponent = agents[j];
	cto = new ConsoleTreeObject(agentComponent);
	DefaultMutableTreeNode newAgentNode = 
	  new DefaultMutableTreeNode(cto, false);
	model.insertNodeInto(newAgentNode, newNodeTreeNode, 
			     newNodeTreeNode.getChildCount());
      }
    }
  }

  /**
   * Add unassigned agents to unassigned agents tree.
   */

  private static Comparator configurableComponentComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      ConfigurableComponent c1 = (ConfigurableComponent) o1;
      ConfigurableComponent c2 = (ConfigurableComponent) o2;
      return c1.getFullName().compareTo(c2.getFullName());
    }
  };

  private void addUnassignedAgentsFromExperiment() {
    Set unassignedAgents = new TreeSet(configurableComponentComparator);
    AgentComponent[] agents = experiment.getAgents();
    NodeComponent[] nodes = experiment.getNodes();
    unassignedAgents.addAll(Arrays.asList(agents));
    for (int i = 0; i < nodes.length; i++)
      unassignedAgents.removeAll(Arrays.asList(nodes[i].getAgents()));
    DefaultTreeModel model = (DefaultTreeModel)agentTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
    Iterator iter = unassignedAgents.iterator();
    while (iter.hasNext()) {
      AgentComponent agentComponent = (AgentComponent)iter.next();
      ConsoleTreeObject cto = new ConsoleTreeObject(agentComponent);
      DefaultMutableTreeNode newNode =
	new DefaultMutableTreeNode(cto, false);
      model.insertNodeInto(newNode, root, root.getChildCount());
    }
  }

  /**
   * Display the popup menu for the host tree.
   * If pointing to root, displays "New Host"
   * If pointing to host, displays "New Node", "Delete Host"
   * If pointing to node, displays "Delete Node"
   */

  public void displayHostTreeMenu(MouseEvent e) {
    TreePath selPath = hostTree.getPathForLocation(e.getX(), e.getY());
    if (selPath == null)
      return;
    // set the selected node to be the node the mouse is pointing at
    hostTree.setSelectionPath(selPath);
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)selPath.getLastPathComponent();
    ConsoleTreeObject selected =
      (ConsoleTreeObject)selNode.getUserObject();
    // display popup menu 
    if (selected.isRoot())
      hostRootMenu.show(hostTree, e.getX(), e.getY());
    else if (selected.isHost())
      hostHostMenu.show(hostTree, e.getX(), e.getY());
    else if (selected.isNode())
      hostNodeMenu.show(hostTree, e.getX(), e.getY());
  } 

  /**
   * Listener for adding new hosts to host tree.
   */

  public void newHostMenuItem_actionPerformed(ActionEvent e) {
    String hostName = JOptionPane.showInputDialog("New host name: ");
    if (hostName == null || hostName.length() == 0)
      return;
    DefaultTreeModel model = (DefaultTreeModel)hostTree.getModel();
    DefaultMutableTreeNode hostTreeRoot = 
      (DefaultMutableTreeNode)model.getRoot();
    HostComponent hostComponent = experiment.addHost(hostName);
    ConsoleTreeObject cto = new ConsoleTreeObject(hostComponent);
    DefaultMutableTreeNode hostNode = new DefaultMutableTreeNode(cto);
    model.insertNodeInto(hostNode,
			 hostTreeRoot,
			 hostTreeRoot.getChildCount());
    hostTree.scrollPathToVisible(new TreePath(hostNode.getPath()));
  }

  /**
   * Display the popup menus for the node tree.
   * If pointing to root, displays "New Node"
   * If pointing to node, displays "Delete Node"
   */

  public void displayNodeTreeMenu(MouseEvent e) {
    TreePath selPath = nodeTree.getPathForLocation(e.getX(), e.getY());
    if (selPath == null)
      return;
    // set the selected node to be the node the mouse is pointing at
    nodeTree.setSelectionPath(selPath);
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)selPath.getLastPathComponent();
    ConsoleTreeObject selected =
      (ConsoleTreeObject)selNode.getUserObject();
    // display popup menu
    if (selected.isRoot())
      nodeRootMenu.show(nodeTree, e.getX(), e.getY());
    else if (selected.isNode())
      nodeNodeMenu.show(nodeTree, e.getX(), e.getY());
  }

  /**
   * Listener for adding new nodes to host tree.
   */

  public void newNodeInHostMenuItem_actionPerformed(ActionEvent e) {
    newNodeInTree(hostTree);
    //    setRunButtonEnabled();
  }

  private void newNodeInTree(JTree tree) {
    TreePath path = tree.getSelectionPath();
    if (path == null) {
      System.out.println("CSMARTConsole newNodeInTree called with null path; ignoring");
      return;
    }
    DefaultMutableTreeNode selectedNode = 
      (DefaultMutableTreeNode)path.getLastPathComponent();
    String nodeName = JOptionPane.showInputDialog("New node name: ");
    if (nodeName == null || nodeName.length() == 0)
      return;
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
    NodeComponent nodeComponent = experiment.addNode(nodeName);
    DefaultMutableTreeNode newNode =
      new DefaultMutableTreeNode(new ConsoleTreeObject(nodeComponent));
    model.insertNodeInto(newNode,
			 selectedNode,
			 selectedNode.getChildCount());
    tree.scrollPathToVisible(new TreePath(newNode.getPath()));
  }

  /**
   * Listener for deleting nodes from host tree.
   */

  public void deleteNodeInHostMenuItem_actionPerformed(ActionEvent e) {
    deleteNodeFromTree(hostTree);
    //    setRunButtonEnabled();
  }

  private void deleteNodeFromTree(JTree tree) {
    TreePath path = tree.getSelectionPath();
    if (path == null) {
      System.out.println("CSMARTConsole deleteNodeMenuItem called with null path; ignoring");
      return;
    }
    DefaultMutableTreeNode selectedNode = 
      (DefaultMutableTreeNode)path.getLastPathComponent();
    ConsoleTreeObject nodeCTO = 
      (ConsoleTreeObject)selectedNode.getUserObject();
    // get any agents that are descendants of the node being deleted
    // and return them to the agent tree
    DefaultTreeModel agentModel = (DefaultTreeModel)agentTree.getModel();
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)agentModel.getRoot();
    int n = selectedNode.getChildCount();
    for (int i = 0; i < n; i++) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)selectedNode.getChildAt(0);
      ConsoleTreeObject cto = (ConsoleTreeObject)node.getUserObject();
      if (cto.isAgent()) {
 	agentModel.insertNodeInto(node, root, root.getChildCount());
 	agentTree.scrollPathToVisible(new TreePath(node.getPath()));
      }
    }
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
    model.removeNodeFromParent(selectedNode);
    experiment.removeNode((NodeComponent)nodeCTO.getComponent());
  }

  /**
   * Listener for adding new nodes to node tree.
   */

  public void newNodeMenuItem_actionPerformed(ActionEvent e) {
    newNodeInTree(nodeTree);
  }

  /**
   * TreeModelListener interface.
   */

  /**
   * Called when user drags nodes on to the host or node tree;
   * dispatches to a tree specific method.
   */

  public void treeNodesInserted(TreeModelEvent e) {
    //    System.out.println("CSMARTConsole: treeNodesInserted");
    Object source = e.getSource();
    if (hostTree.getModel().equals(source))
      treeNodesInsertedInHostTree(e);
    else if (nodeTree.getModel().equals(source))
      treeNodesInsertedInNodeTree(e);
  }

  /**
   * Called when user drags nodes on to the host tree (as opposed to
   * creating new nodes from the pop-up menu) or when user drags
   * agents on to a node in the host tree.
   * Notify society component if nodes are added to the host tree.
   * Notify node component if agents are added to a node in the host tree.
   * Note that if nodes are dragged on to a host,
   * then this gets called on both the host tree node and the node tree node.
   * TODO: if you drag a node between two hosts in the tree,
   * then this is called once for each agent in the dragged node with 
   * e.getTreePath identifying the dragged node 
   * (see workaround nodeComponentHasAgent)
   */

  private void treeNodesInsertedInHostTree(TreeModelEvent e) {
    TreePath path = e.getTreePath(); // parent of the new node
    DefaultMutableTreeNode changedNode = 
      (DefaultMutableTreeNode)path.getLastPathComponent();
    ConsoleTreeObject cto = (ConsoleTreeObject)(changedNode.getUserObject());
    //    System.out.println("CSMARTConsole: treeNodesInsertedInHostTree: " + cto.toString());
    // agents were dragged on to a node
    // tell the node that agents were added 
    if (cto.isNode()) {
      addAgentsToNode((NodeComponent)cto.getComponent(), e.getChildren());
    } else if (cto.isHost()) {
      // nodes were dragged on to a host
      // tell the host that nodes were added
      HostComponent hostComponent = (HostComponent)cto.getComponent();
      Object[] newChildren = e.getChildren();
      for (int i = 0; i < newChildren.length; i++) {
	DefaultMutableTreeNode treeNode =
	  (DefaultMutableTreeNode)newChildren[i];
	cto = (ConsoleTreeObject)treeNode.getUserObject();
	hostComponent.addNode((NodeComponent)cto.getComponent());
      }
    }
    //    setRunButtonEnabled();
  }

  /**
   * Do nothing when agent nodes are inserted into a node tree,
   * because the treeNodesInserted method gets called both on
   * host tree nodes and node tree nodes when a node is dragged
   * on to the host tree.
   */

  private void treeNodesInsertedInNodeTree(TreeModelEvent e) {
//     TreePath path = e.getTreePath();
//     DefaultMutableTreeNode changedNode = 
//       (DefaultMutableTreeNode)path.getLastPathComponent();
//     ConsoleTreeObject cto = (ConsoleTreeObject)(changedNode.getUserObject());
//     // agents were dragged on to a node
//     // tell the node that agents were added 
//     if (!cto.isRoot()) {
//       System.out.println("Agents were added to node in node tree: " +
// 			 ((NodeComponent)cto.getComponent()).toString());
//       addAgentsToNode((NodeComponent)cto.getComponent(), e.getChildren());
//     }
  }

  /**
   * Tell node component to add agent components.
   */

  private void addAgentsToNode(NodeComponent nodeComponent,
			       Object[] newChildren) {
    for (int i = 0; i < newChildren.length; i++) {
      DefaultMutableTreeNode treeNode =
	(DefaultMutableTreeNode)newChildren[i];
      ConsoleTreeObject cto = (ConsoleTreeObject)treeNode.getUserObject();
      AgentComponent agentComponent = (AgentComponent)cto.getComponent();
      if (!nodeComponentHasAgent(nodeComponent, agentComponent))
	nodeComponent.addAgent(agentComponent);
    }
  }

  /**
   * TODO: remove this workaround
   * Check if a node has an agent before telling it about a new one.
   * Workaround for bug that causes treeNodesInsertedInHostTree to
   * be called when agents are moved within the tree.
   */

  private boolean nodeComponentHasAgent(NodeComponent node,
					AgentComponent agent) {
    AgentComponent[] agents = node.getAgents();
    for (int i = 0; i < agents.length; i++) {
      if (agents[i].equals(agent))
	return true;
    }
    return false;
  }

  /**
   * Called when user drags nodes off the host or node tree;
   * dispatches to a tree specific method.
   */

  public void treeNodesRemoved(TreeModelEvent e) {
    Object source = e.getSource();
    if (hostTree.getModel().equals(source))
      treeNodesRemovedFromHostTree(e);
    else if (nodeTree.getModel().equals(source))
      treeNodesRemovedFromNodeTree(e);
  }


  /**
   * Notify society component if nodes are removed from the host tree.
   * Notify node component if agents are removed from a node in the host tree.
   * Note that this is not symmetric with adding nodes to a tree;
   * i.e. if a node tree node is removed from the hosts tree,
   * then this is called only on the host tree node.
   */

  public void treeNodesRemovedFromHostTree(TreeModelEvent e) {
    TreePath path = e.getTreePath();
    DefaultMutableTreeNode changedNode = 
      (DefaultMutableTreeNode)path.getLastPathComponent();
    ConsoleTreeObject cto = (ConsoleTreeObject)(changedNode.getUserObject());
    //    System.out.println("CSMARTConsole: treeNodesRemovedFromHostTree: " + cto.toString());
    // tell the node that agents were removed
    if (cto.isNode()) {
      removeAgentsFromNode((NodeComponent)cto.getComponent(), e.getChildren());
    } else if (cto.isHost()) {
      // tell the host that nodes were removed
      HostComponent hostComponent = (HostComponent)cto.getComponent();
      Object[] removedChildren = e.getChildren();
      for (int i = 0; i < removedChildren.length; i++) {
	DefaultMutableTreeNode treeNode =
	  (DefaultMutableTreeNode)removedChildren[i];
	cto = (ConsoleTreeObject)treeNode.getUserObject();
	NodeComponent nodeComponent = (NodeComponent)cto.getComponent();
	hostComponent.removeNode(nodeComponent);
      }
    }
    //    setRunButtonEnabled();
  }

  /**
   * Notify nodes if agents were removed from a node in the
   * unassigned nodes tree.
   */

  public void treeNodesRemovedFromNodeTree(TreeModelEvent e) {
    TreePath path = e.getTreePath();
    DefaultMutableTreeNode changedNode = 
      (DefaultMutableTreeNode)path.getLastPathComponent();
    ConsoleTreeObject cto = (ConsoleTreeObject)(changedNode.getUserObject());
    // tell the node that agents were removed
    if (!cto.isRoot())
      removeAgentsFromNode((NodeComponent)cto.getComponent(), e.getChildren());
  }

  /**
   * Tell node component to remove agents from node.
   */

  private void removeAgentsFromNode(NodeComponent nodeComponent,
				    Object[] removedChildren) {
    for (int i = 0; i < removedChildren.length; i++) {
      DefaultMutableTreeNode treeNode =
	(DefaultMutableTreeNode)removedChildren[i];
      ConsoleTreeObject cto = (ConsoleTreeObject)treeNode.getUserObject();
      AgentComponent agentComponent = (AgentComponent)cto.getComponent();
      nodeComponent.removeAgent(agentComponent);
    }
  }

  /**
   * TreeModelListener interface -- unused.
   */

  public void treeNodesChanged(TreeModelEvent e) {
  }

  /**
   * TreeModelListener interface -- unused.
   */

  public void treeStructureChanged(TreeModelEvent e) {
  }
  
  /**
   * Listener for deleting items from host tree.
   * Called when user selects "Delete" from popup menu.
   * This just takes care of the tree; the treeNodesRemoved method
   * updates the Society and Node components.
   */

  public void deleteHostMenuItem_actionPerformed(ActionEvent e) {
    TreePath path = hostTree.getSelectionPath();
    if (path == null) {
      System.out.println("CSMARTConsole deleteHostMenuItem called with null path; ignoring");
      return;
    }
    DefaultMutableTreeNode selectedNode = 
      (DefaultMutableTreeNode)path.getLastPathComponent();
    ConsoleTreeObject hostCTO = 
      (ConsoleTreeObject)selectedNode.getUserObject();
    // get any nodes that are descendants of the host being deleted
    // and return them to the unassigned nodes tree
    DefaultTreeModel nodeModel = (DefaultTreeModel)nodeTree.getModel();
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)nodeModel.getRoot();
    int n = selectedNode.getChildCount();
    for (int i = 0; i < n; i++) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)selectedNode.getChildAt(0);
      ConsoleTreeObject cto = (ConsoleTreeObject)node.getUserObject();
      if (cto.isNode()) {
	nodeModel.insertNodeInto(node, root, root.getChildCount());
	nodeTree.scrollPathToVisible(new TreePath(node.getPath()));
      }
    }
    DefaultTreeModel hostModel = (DefaultTreeModel)hostTree.getModel();
    hostModel.removeNodeFromParent(selectedNode);
    experiment.removeHost((HostComponent)hostCTO.getComponent());
    //    setRunButtonEnabled();
  }

  /**
   * Listener for deleting items from node tree.
   * Called when user selects "Delete" from popup menu.
   * This just takes care of the tree; the treeNodesRemoved method
   * updates the Society and Node components.
   */

  public void deleteNodeMenuItem_actionPerformed(ActionEvent e) {
    deleteNodeFromTree(nodeTree);
  }

  private DefaultTreeModel createModel(final Experiment experiment, DefaultMutableTreeNode node, boolean askKids) {
    return new DefaultTreeModel(node, askKids) {
	public void valueForPathChanged(TreePath path, Object newValue) {
	  if (newValue == null || newValue.toString().equals("")) return;
	  // Allow renaming hosts or Nodes only
	  DefaultMutableTreeNode aNode = (DefaultMutableTreeNode)path.getLastPathComponent();
	  ConsoleTreeObject cto = (ConsoleTreeObject)aNode.getUserObject();
	    // FIXME: Do check to avoid duplicate
	    // host or node names?
	  String name = newValue.toString();
	  if (cto.isHost()) {
	    experiment.renameHost((HostComponent)cto.getComponent(), name);
	    cto.setName(name);
	    nodeChanged(aNode);
	  } else if (cto.isNode()) {
	    experiment.renameNode((NodeComponent)cto.getComponent(), name);
	    cto.setName(name);
	    nodeChanged(aNode);
	  }
	}
      };
  }
}




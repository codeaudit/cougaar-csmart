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

package org.cougaar.tools.csmart.ui.experiment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import javax.swing.tree.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.name.ComponentName;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.name.SimpleName;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.ExperimentNode;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.console.CSMARTConsole;
import org.cougaar.tools.csmart.ui.tree.ConsoleDNDTree;
import org.cougaar.tools.csmart.ui.tree.ConsoleTreeObject;
import org.cougaar.tools.csmart.ui.tree.DNDTree;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

public class HostConfigurationBuilder extends JPanel implements TreeModelListener {
  Experiment experiment;
  ExperimentBuilder experimentBuilder;
  boolean isEditable;
  boolean isRunnable;
  SocietyComponent societyComponent;
  JPopupMenu hostRootMenu;
  JPopupMenu hostHostMenu;
  JPopupMenu hostNodeMenu;
  JPopupMenu nodeRootMenu;
  JPopupMenu nodeNodeMenu;
  JMenuItem cmdLineNodeMenuItem;
  JMenuItem cmdLineNodeInHostMenuItem;
  JMenuItem newNodeInHostMenuItem;
  DNDTree hostTree;
  DNDTree nodeTree;
  DNDTree agentTree;

  // menu items for popup menu in hostTree and for 
  // File menu in ExperimentBuilder
  public static final String NEW_HOST_MENU_ITEM = "New Host";
  public static final String NEW_NODE_MENU_ITEM = "New Node";
  public static final String DELETE_MENU_ITEM = "Delete";
  public static final String DELETE_HOST_MENU_ITEM = "Delete Host";
  public static final String DELETE_NODE_MENU_ITEM = "Delete Node";
  public static final String DESCRIBE_MENU_ITEM = "Describe";
  public static final String DESCRIBE_HOST_MENU_ITEM = "Describe Host";
  public static final String DESCRIBE_NODE_MENU_ITEM = "Describe Node";
  public static final String NODE_COMMAND_LINE_MENU_ITEM = "Command Line Arguments";
  public static final String GLOBAL_COMMAND_LINE_MENU_ITEM =
    "Global Command Line Arguments";
  public static final String HOST_TYPE_MENU_ITEM = "Type";
  public static final String HOST_LOCATION_MENU_ITEM = "Location";
  private JPanel hostConfigurationBuilder;
  // map agent component to node component
  private Hashtable agentToNode = new Hashtable();
  // map node component to host component
  private Hashtable nodeToHost = new Hashtable();

  public HostConfigurationBuilder(Experiment experiment, 
                                  ExperimentBuilder experimentBuilder) {
    this.experiment = experiment;
    this.experimentBuilder = experimentBuilder;
    hostConfigurationBuilder = this; // for inner class dialogs
    isEditable = experiment.isEditable();
    isRunnable = experiment.isRunnable();
    initDisplay();
  }

  private void initDisplay() {
    // host split pane contains host tree and 
    // the bottom split pane which contains the node and agent trees
    JSplitPane hostPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    // tree of hosts and assigned nodes and agents
    DefaultMutableTreeNode root = 
      new DefaultMutableTreeNode(new ConsoleTreeObject("Hosts", 
		       "org.cougaar.tools.csmart.experiment.HostComponent"));
    // setting "askAllowsChildren" forces empty nodes that can have
    // children to be displayed as "folders" rather than leaf nodes
    DefaultTreeModel model = createModel(experiment, root, true);
    hostTree = new ConsoleDNDTree(model);
    hostTree.setExpandsSelectedPaths(true);
    // cell editor returns false if user tries to edit root node
    DefaultCellEditor myEditor = new DefaultCellEditor(new JTextField()) {
      public boolean isCellEditable(EventObject e) {
	if (super.isCellEditable(e) && e instanceof MouseEvent) {
	  TreePath path = hostTree.getPathForLocation(((MouseEvent)e).getX(),
						      ((MouseEvent)e).getY());
	  if (path == null)
	    return false;
	  Object o = path.getLastPathComponent();
          DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)o;
	  if (treeNode.isRoot() ||
              ((ConsoleTreeObject)treeNode.getUserObject()).isAgent())
	    return false;
	}
	return super.isCellEditable(e);
      }
      public boolean stopCellEditing() {
        TreePath path = hostTree.getEditingPath();
        DefaultMutableTreeNode node =
          (DefaultMutableTreeNode)path.getLastPathComponent();
        int result = 0;
        if (node.getUserObject() instanceof ConsoleTreeObject) {
          ConsoleTreeObject cto = (ConsoleTreeObject)node.getUserObject();
          if (cto.isHost()) {
            // stop cell editing, accept new unique value
            if (isHostNameUnique((String)getCellEditorValue()))
              return super.stopCellEditing();
            // tell user that value isn't unique
            result = JOptionPane.showConfirmDialog(hostConfigurationBuilder,
                                                   "Use an unique name",
                                                   "Host Name Not Unique",
                                                   JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.ERROR_MESSAGE);
          } else if (cto.isNode()) {
            // stop cell editing, accept new unique value
            if (isNodeNameUnique((String)getCellEditorValue()))
              return super.stopCellEditing();
            // tell user that value isn't unique
            result = JOptionPane.showConfirmDialog(hostConfigurationBuilder,
                                                   "Use an unique name",
                                                   "Node Name Not Unique",
                                                   JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.ERROR_MESSAGE);
          }
        } else
          return super.stopCellEditing();
        // user cancelled the message dialog, so cancel the editing
        if (result != JOptionPane.OK_OPTION) {
          cancelCellEditing();
          return true;
        }
        // user entered non-unique value, not done editing
        return false;
      }
    };
    hostTree.setCellEditor(myEditor);

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
        createHost();
      }
    });

    JMenuItem deleteHostMenuItem = new JMenuItem(DELETE_MENU_ITEM);
    deleteHostMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteHost();
      }
    });

    newNodeInHostMenuItem = new JMenuItem(NEW_NODE_MENU_ITEM);
    newNodeInHostMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        createAssignedNode();
      }
    });

    JMenuItem hostDescriptionMenuItem = 
      new JMenuItem(DESCRIBE_MENU_ITEM);
    hostDescriptionMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setHostDescription();
      }
    });

    JMenuItem hostLocationMenuItem = new JMenuItem(HOST_LOCATION_MENU_ITEM);
    hostLocationMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setHostLocation();
      }
    });

    JMenuItem hostTypeMenuItem = new JMenuItem(HOST_TYPE_MENU_ITEM);
    hostTypeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setHostType();
      }
    });

    JMenuItem describeNodeInHostMenuItem = 
      new JMenuItem(DESCRIBE_MENU_ITEM);
    describeNodeInHostMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setNodeDescription(hostTree);
      }
    });

    cmdLineNodeInHostMenuItem = 
      new JMenuItem(NODE_COMMAND_LINE_MENU_ITEM);
    cmdLineNodeInHostMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setNodeCommandLine((DefaultMutableTreeNode)hostTree.getLastSelectedPathComponent());
      }
    });
    cmdLineNodeInHostMenuItem.setEnabled(true);

    Action globalCmdLineAction = new AbstractAction(GLOBAL_COMMAND_LINE_MENU_ITEM) {
      public void actionPerformed(ActionEvent e) {
        setGlobalCommandLine();
      }
    };

    JMenuItem deleteNodeInHostMenuItem = new JMenuItem(DELETE_MENU_ITEM);
    deleteNodeInHostMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	deleteNodesFromTree(hostTree);
      }
    });

    // init pop-up menus
    hostRootMenu.add(newHostMenuItem);
    hostRootMenu.add(globalCmdLineAction);

    hostHostMenu.add(hostDescriptionMenuItem);
    hostHostMenu.add(hostTypeMenuItem);
    hostHostMenu.add(hostLocationMenuItem);
    hostHostMenu.add(newNodeInHostMenuItem);
    hostHostMenu.add(globalCmdLineAction);
    hostHostMenu.add(deleteHostMenuItem);

    hostNodeMenu.add(describeNodeInHostMenuItem);
    hostNodeMenu.add(cmdLineNodeInHostMenuItem);
    hostNodeMenu.add(globalCmdLineAction);
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
    ConsoleTreeObject cto = new ConsoleTreeObject("Nodes (unassigned)", 
                 "org.cougaar.tools.csmart.experiment.NodeComponent");
    root = new DefaultMutableTreeNode(cto, true);
    model = createModel(experiment, root, true);
    nodeTree = new ConsoleDNDTree(model);
    // cell editor returns false if try to edit agent names or root name
    DefaultCellEditor nodeEditor = new DefaultCellEditor(new JTextField()) {
      public boolean isCellEditable(EventObject e) {
	if (super.isCellEditable(e) && e instanceof MouseEvent) {
	  TreePath path = hostTree.getPathForLocation(((MouseEvent)e).getX(),
						      ((MouseEvent)e).getY());
	  if (path == null)
	    return false;
	  Object o = path.getLastPathComponent();
	  DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)o;
	  if (treeNode.isRoot() ||
	      ((ConsoleTreeObject)treeNode.getUserObject()).isAgent())
	    return false;
	}
	return super.isCellEditable(e);
      }
      public boolean stopCellEditing() {
        // stop cell editing, accept new unique value
        if (isNodeNameUnique((String)getCellEditorValue()))
          return super.stopCellEditing();
        // tell user that value isn't unique
        int ok = JOptionPane.showConfirmDialog(hostConfigurationBuilder,
                                               "Use an unique name",
                                               "Node Name Not Unique",
                                               JOptionPane.OK_CANCEL_OPTION,
                                               JOptionPane.ERROR_MESSAGE);
        // user cancelled the message dialog, so cancel the editing
        if (ok != JOptionPane.OK_OPTION) {
          cancelCellEditing();
          return true;
        }
        // user entered non-unique value, not done editing
        return false;
      }
    };
    nodeTree.setCellEditor(nodeEditor);
    nodeTree.setExpandsSelectedPaths(true);
    JScrollPane nodeTreeScrollPane = new JScrollPane(nodeTree);
    nodeTreeScrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    bottomPane.setTopComponent(nodeTreeScrollPane);
    // popup menu for creating and deleting nodes
    nodeRootMenu = new JPopupMenu();
    nodeNodeMenu = new JPopupMenu();
    JMenuItem newNodeMenuItem = new JMenuItem(NEW_NODE_MENU_ITEM);
    newNodeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	createUnassignedNode();
      }
    });
    nodeRootMenu.add(newNodeMenuItem);
    nodeRootMenu.add(globalCmdLineAction);
    JMenuItem describeNodeMenuItem = new JMenuItem(DESCRIBE_MENU_ITEM);
    describeNodeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	setNodeDescription(nodeTree);
      }
    });
    cmdLineNodeMenuItem = new JMenuItem(NODE_COMMAND_LINE_MENU_ITEM);
    cmdLineNodeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	setNodeCommandLine((DefaultMutableTreeNode)nodeTree.getLastSelectedPathComponent());
      }
    });
    cmdLineNodeMenuItem.setEnabled(true);
    JMenuItem globalCmdLineMenuItem = 
      new JMenuItem(GLOBAL_COMMAND_LINE_MENU_ITEM);
    globalCmdLineMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setGlobalCommandLine();
      }
    });
    globalCmdLineMenuItem.setEnabled(true);
    JMenuItem deleteNodeMenuItem = new JMenuItem(DELETE_MENU_ITEM);
    deleteNodeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	deleteNodesFromTree(nodeTree);
      }
    });
    nodeNodeMenu.add(describeNodeMenuItem);
    nodeNodeMenu.add(cmdLineNodeMenuItem);
    nodeNodeMenu.add(globalCmdLineAction);
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
    cto = new ConsoleTreeObject("Agents (unassigned)", 
		"org.cougaar.tools.csmart.society.AgentComponent");
    root = new DefaultMutableTreeNode(cto, true);
    model = createModel(experiment, root, true);
    agentTree = new ConsoleDNDTree(model);
    // cell editor returns false; can't edit agent names or root name
    DefaultCellEditor agentEditor = new DefaultCellEditor(new JTextField()) {
      public boolean isCellEditable(EventObject e) {
	return false;
      }
    };
    agentTree.setCellEditor(agentEditor);
    agentTree.setExpandsSelectedPaths(true);
    JScrollPane agentTreeScrollPane = new JScrollPane(agentTree);
    agentTreeScrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    bottomPane.setBottomComponent(agentTreeScrollPane);
    hostPane.setBottomComponent(bottomPane);
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

  public void reinit(Experiment newExperiment) {
    // restore editable flag on previous experiment
    if (isEditable)
      experiment.setEditable(isEditable);
    if (isRunnable)
      experiment.setRunnable(isRunnable);
    experiment = newExperiment;
    isEditable = newExperiment.isEditable();
    isRunnable = newExperiment.isRunnable();
    // if this pane is being displayed, then bring it up-to-date
    if (isShowing())
      update(); 
  }

  /**
   * Bring the display up-to-date by re-reading host/node/agent information
   * from the experiment.
   */

  public void update() {
    if (experiment.getSocietyComponentCount() != 0)
      societyComponent = experiment.getSocietyComponent(0);
    else
      societyComponent = null;
    hostTree.getModel().removeTreeModelListener(this);
    nodeTree.getModel().removeTreeModelListener(this);
    agentTree.getModel().removeTreeModelListener(this);
    removeAllChildren(hostTree);
    removeAllChildren(nodeTree);
    removeAllChildren(agentTree);
    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    if (!isEditable) {
      renderer.setTextNonSelectionColor(Color.gray);
      renderer.setTextSelectionColor(Color.gray);
    }
    hostTree.setCellRenderer(renderer);
    nodeTree.setCellRenderer(renderer);
    agentTree.setCellRenderer(renderer);
    hostTree.setEditable(isEditable);
    nodeTree.setEditable(isEditable);
    agentTree.setEditable(isEditable);
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
    hostTree.getModel().addTreeModelListener(this);
    nodeTree.getModel().addTreeModelListener(this);
    agentTree.getModel().addTreeModelListener(this);
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
        nodeToHost.put(nodeComponent, hostComponent);
	AgentComponent[] agents = nodeComponent.getAgents();
	for (int k = 0; k < agents.length; k++) {
	  AgentComponent agentComponent = agents[k];
	  cto = new ConsoleTreeObject(agentComponent);
	  DefaultMutableTreeNode agentNode = 
	    new DefaultMutableTreeNode(cto, false);
	  model.insertNodeInto(agentNode, nodeTreeNode,
			       nodeTreeNode.getChildCount());
          agentToNode.put(agentComponent, nodeComponent);
	}
      }
    }
  }

  /**
   * Create host components for hosts read from a text file.
   */

  private void addHostsFromFile() {
    // this may silently fail, but that's ok, cause the file is optional
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
	ihost = ihost.trim();
	// Do other checking for reasonable host names here...
	if (! ihost.equals("") && ihost != null)
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
    Set unassignedNodes;
    if (experiment.isInDatabase())
      unassignedNodes = new TreeSet(dbBaseComponentComparator);
    else
      unassignedNodes = new TreeSet(builtInBaseComponentComparator);
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
      NodeComponent nodeComponent = (NodeComponent)iter.next();
      ConsoleTreeObject cto = new ConsoleTreeObject(nodeComponent);
      DefaultMutableTreeNode newNodeTreeNode = 
	new DefaultMutableTreeNode(cto, true);
      model.insertNodeInto(newNodeTreeNode, root, root.getChildCount());
      AgentComponent[] agents = nodeComponent.getAgents();
      for (int j = 0; j < agents.length; j++) {
	AgentComponent agentComponent = agents[j];
	cto = new ConsoleTreeObject(agentComponent);
	DefaultMutableTreeNode newAgentNode = 
	  new DefaultMutableTreeNode(cto, false);
	model.insertNodeInto(newAgentNode, newNodeTreeNode, 
			     newNodeTreeNode.getChildCount());
        agentToNode.put(agentComponent, nodeComponent);
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
      
  private static Comparator builtInBaseComponentComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      BaseComponent c1 = (BaseComponent) o1;
      BaseComponent c2 = (BaseComponent) o2;

      if (c1 instanceof NodeComponent || c2 instanceof NodeComponent)
	return c1.getShortName().compareTo(c2.getShortName());
      else // agent name comparison
	return c1.getFullName().compareTo(c2.getFullName());
    }
  };

  /**
   * Add unassigned agents to unassigned agents tree.
   */
  private void addUnassignedAgentsFromExperiment() {
    Set unassignedAgents;
    if (experiment.isInDatabase())
      unassignedAgents = new TreeSet(dbBaseComponentComparator);
    else
      unassignedAgents = new TreeSet(builtInBaseComponentComparator);
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
   * Displays different menus if pointing to root, host or node.
   * If pointing to different types of nodes, does not display a menu.
   */

  private void displayHostTreeMenu(MouseEvent e) {
    // the path to the node the mouse is pointing at
    TreePath selPath = hostTree.getPathForLocation(e.getX(), e.getY());
    if (selPath == null)
      return;
    // if the mouse is pointing at a selected node
    // and all selected nodes are of the same type, then act on all of them
    TreePath[] selectedPaths = hostTree.getSelectionPaths();
    if (hostTree.isPathSelected(selPath) && selectedPaths.length > 1) {
      boolean haveHosts = false;
      boolean haveNodes = false;
      for (int i = 0; i < selectedPaths.length; i++) {
        DefaultMutableTreeNode selNode =
          (DefaultMutableTreeNode)selectedPaths[i].getLastPathComponent();
        ConsoleTreeObject selected =
          (ConsoleTreeObject)selNode.getUserObject();
        if (selected.isHost()) {
          if (!haveHosts && !haveNodes)
            haveHosts = true;
          else if (haveNodes) {
            haveNodes = false;
            break;
          }
        } else if (selected.isNode()) {
          if (!haveHosts && !haveNodes)
            haveNodes = true;
          else if (haveHosts) {
            haveHosts = false;
            break;
          }
        } else {
          haveHosts = false;
          haveNodes = false;
          break;
        }
      }
      if (haveHosts) {
        newNodeInHostMenuItem.setEnabled(false);
        hostHostMenu.show(hostTree, e.getX(), e.getY());
        return;
      } else if (haveNodes) {
        cmdLineNodeInHostMenuItem.setEnabled(false);
        hostNodeMenu.show(hostTree, e.getX(), e.getY());
        return;
      }
    } else {
      // else set the selected node to be the node the mouse is pointing at
      hostTree.setSelectionPath(selPath);
      DefaultMutableTreeNode selNode =
        (DefaultMutableTreeNode)selPath.getLastPathComponent();
      ConsoleTreeObject selected =
        (ConsoleTreeObject)selNode.getUserObject();
      // display popup menu 
      if (selected.isRoot())
        hostRootMenu.show(hostTree, e.getX(), e.getY());
      else if (selected.isHost()) {
        newNodeInHostMenuItem.setEnabled(true);
        hostHostMenu.show(hostTree, e.getX(), e.getY());
      } else if (selected.isNode()) {
        cmdLineNodeInHostMenuItem.setEnabled(true);
        hostNodeMenu.show(hostTree, e.getX(), e.getY());
      } 
    }
  } 

  /**
   * Add new host to host tree.
   */

  public void createHost() {
    String hostName = null;
    while (true) {
      hostName = JOptionPane.showInputDialog("New host name: ");
      if (hostName == null || hostName.length() == 0)
        return;
      HostComponent[] hc = experiment.getHosts();
      boolean isUnique = true;
      for (int i = 0; i < hc.length; i++) 
        if (hostName.equals(hc[i].getShortName())) {
          isUnique = false;
          break;
        }
      if (isUnique)
        break;
      int ok = JOptionPane.showConfirmDialog(this,
                                             "Use an unique name",
                                             "Host Name Not Unique",
                                             JOptionPane.OK_CANCEL_OPTION,
                                             JOptionPane.ERROR_MESSAGE);
      if (ok != JOptionPane.OK_OPTION) return;
    }
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

  private boolean isHostNameUnique(String name) {
    HostComponent[] hc = experiment.getHosts();
    boolean isUnique = true;
    for (int i = 0; i < hc.length; i++) 
      if (name.equals(hc[i].getShortName()))
        return false;
    return true;
  }

  /**
   * Display the popup menus for the node tree, either the node menu
   * or the root menu.
   */

  private void displayNodeTreeMenu(MouseEvent e) {
    // the path to the node the mouse is pointing at
    TreePath selPath = nodeTree.getPathForLocation(e.getX(), e.getY());
    if (selPath == null)
      return;
    // if the mouse is pointing at a selected node
    // and all selected nodes are of the same type, then act on all of them
    TreePath[] selectedPaths = nodeTree.getSelectionPaths();
    if (nodeTree.isPathSelected(selPath) && selectedPaths.length > 1) {
      boolean haveNodes = false;
      for (int i = 0; i < selectedPaths.length; i++) {
        DefaultMutableTreeNode selNode =
          (DefaultMutableTreeNode)selectedPaths[i].getLastPathComponent();
        ConsoleTreeObject selected =
          (ConsoleTreeObject)selNode.getUserObject();
        if (selected.isNode())
          haveNodes = true;
        else {
          haveNodes = false;
          break;
        }
      }
      // handle multiple selected nodes
      // disable menu command to set individual node command line arguments
      if (haveNodes) {
        cmdLineNodeMenuItem.setEnabled(false);
        nodeNodeMenu.show(nodeTree, e.getX(), e.getY());
        return;
      }
    } else {
      // set the selected node to be the node the mouse is pointing at
      nodeTree.setSelectionPath(selPath);
      DefaultMutableTreeNode selNode =
        (DefaultMutableTreeNode)selPath.getLastPathComponent();
      ConsoleTreeObject selected =
        (ConsoleTreeObject)selNode.getUserObject();
      // display popup menu
      if (selected.isRoot())
        nodeRootMenu.show(nodeTree, e.getX(), e.getY());
      else if (selected.isNode()) {
        cmdLineNodeMenuItem.setEnabled(true);
        nodeNodeMenu.show(nodeTree, e.getX(), e.getY());
      }
    }
  }

  /**
   * Listener for adding new nodes to host tree.
   */

  public void createAssignedNode() {
    newNodeInTree(hostTree);
    //    setRunButtonEnabled();
  }

  public void createUnassignedNode() {
    newNodeInTree(nodeTree);
  }

  // create a new node component in either the host or nodes tree
  private void newNodeInTree(JTree tree) {
    TreePath path = tree.getSelectionPath();
    if (path == null) {
      System.out.println("HostConfigurationBuilder newNodeInTree called with null path; ignoring");
      return;
    }
    DefaultMutableTreeNode selectedNode = 
      (DefaultMutableTreeNode)path.getLastPathComponent();
    String nodeName = null;
    while (true) {
      nodeName = JOptionPane.showInputDialog("New node name: ");
      if (nodeName == null || nodeName.length() == 0)
        return;
      // don't allow node names that are the same as node or agent names
      if (isNodeNameUnique(nodeName))
        break;
      int ok = JOptionPane.showConfirmDialog(this,
                                             "Use an unique name",
                                             "Node Name Not Unique",
                                             JOptionPane.OK_CANCEL_OPTION,
                                             JOptionPane.ERROR_MESSAGE);
      if (ok != JOptionPane.OK_OPTION) return;
    }
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
    NodeComponent nodeComponent = experiment.addNode(nodeName);
    DefaultMutableTreeNode newNode =
      new DefaultMutableTreeNode(new ConsoleTreeObject(nodeComponent));
    model.insertNodeInto(newNode,
			 selectedNode,
			 selectedNode.getChildCount());
    if (tree.equals(hostTree)) { // added node to host
      nodeToHost.put(nodeComponent, selectedNode.getUserObject());
    }
    tree.scrollPathToVisible(new TreePath(newNode.getPath()));
  }

  // check all trees and return false if there's a node or agent
  // with the same name

  private boolean isNodeNameUnique(String name) {
    if (isNodeNameUniqueInTree(hostTree, name) &&
        isNodeNameUniqueInTree(nodeTree, name) &&
        isNodeNameUniqueInTree(agentTree, name))
      return true;
    else
      return false;
  }

  private boolean isNodeNameUniqueInTree(JTree tree, String name) {
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
    Enumeration nodes = root.breadthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
 	(DefaultMutableTreeNode)nodes.nextElement();
        if (node.getUserObject() instanceof ConsoleTreeObject) {
          ConsoleTreeObject cto = (ConsoleTreeObject)node.getUserObject();
          if ((cto.isNode() || cto.isAgent()) &&
              cto.getName().equals(name)) 
            return false;
        }
    }
    return true;
  }

  public void deleteNode() {
    if (getSelectedNodesInHostTree() != null)
      deleteNodesFromTree(hostTree);
    if (getSelectedNodesInNodeTree() != null)
      deleteNodesFromTree(nodeTree);
  }

  /**
   * Listener for deleting nodes from host tree.
   */

  private void deleteNodesFromTree(JTree tree) {
    TreePath[] selectedPaths = tree.getSelectionPaths();
    for (int i = 0; i < selectedPaths.length; i++) 
      deleteNodeFromTree(tree, selectedPaths[i]);
    //    setRunButtonEnabled();
  }

  /**
   * Delete node component from either host or unassigned nodes tree.
   */

  private void deleteNodeFromTree(JTree tree, TreePath path) {
    DefaultMutableTreeNode selectedNode = 
      (DefaultMutableTreeNode)path.getLastPathComponent();
    ConsoleTreeObject nodeCTO = 
      (ConsoleTreeObject)selectedNode.getUserObject();
    NodeComponent nodeComponent = (NodeComponent)nodeCTO.getComponent();
    BaseComponent parentComponent =
      getComponentFromTreeNode((DefaultMutableTreeNode)selectedNode.getParent());
    // get any agents that are descendants of the node being deleted
    // and return them to the unassigned agents tree
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
        agentToNode.remove(cto.getComponent());
      }
    }
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
    model.removeNodeFromParent(selectedNode);
    experiment.removeNode(nodeComponent);
    if (parentComponent instanceof HostComponent) {
      nodeToHost.remove(nodeComponent);
      ((HostComponent)parentComponent).removeNode(nodeComponent);
    }
  }

  /**
   * TreeModelListener interface.
   */

  /**
   * Called when user drags nodes on to the host or node tree;
   * dispatches to a tree specific method.
   */

  public void treeNodesInserted(TreeModelEvent e) {
    Object source = e.getSource();
    if (hostTree.getModel().equals(source)) {
      treeNodesInsertedInHostTree(e.getTreePath(), e.getChildren());
    } else if (nodeTree.getModel().equals(source)) {
      treeNodesInsertedInNodeTree(e.getTreePath(), e.getChildren());
    } else if (agentTree.getModel().equals(source)) {
      treeNodesInsertedInAgentTree(e.getChildren());
    }
    experimentBuilder.setModified(true);
  }

  /**
   * Called when user drags nodes on to the host tree (as opposed to
   * creating new nodes from the pop-up menu) or when user drags
   * agents on to a node in the host tree.
   * Notify host component if nodes are added to the host tree.
   * Notify node component if agents are added to a node in the host tree.
   * This also removes the component from its previous parent,
   * i.e. both the add and remove of the configurable component
   * are done on the "inserted" message and the "removed message" is ignored.
   */

  private void treeNodesInsertedInHostTree(TreePath path, Object[] children) {
    BaseComponent component = getComponentFromPath(path);
    if (component instanceof NodeComponent) 
      addAgentsToNode((NodeComponent)component, children);
    else if (component instanceof HostComponent)
      addNodesToHost((HostComponent)component, children);
  }

  /**
   * Update agent component to node component mapping
   * when agents are dragged onto the Unassigned Nodes tree.
   * Remove node component from host component 
   * when nodes are dragged onto the Unassigned Nodes tree.
   */

  private void treeNodesInsertedInNodeTree(TreePath path, Object[] children) {
    BaseComponent component = getComponentFromPath(path);
    if (component == null) { // add nodes to Unassigned Nodes tree
      for (int i = 0; i < children.length; i++) {
        NodeComponent nodeComponent = 
          (NodeComponent)getComponentFromTreeNode((DefaultMutableTreeNode)children[i]);
        HostComponent previousParent = 
          (HostComponent)nodeToHost.get(nodeComponent);
        if (previousParent != null)
          previousParent.removeNode(nodeComponent);
        nodeToHost.remove(nodeComponent);
      }
    } else
      addAgentsToNode((NodeComponent)component, children);
  }

  /**
   * Update agent component to node component mapping
   * when agents are dragged onto the Unassigned Agents tree.
   * Just remove agents from previous node component.
   */

  private void treeNodesInsertedInAgentTree(Object[] children) {
    for (int i = 0; i < children.length; i++) {
      AgentComponent agent = 
        (AgentComponent)getComponentFromTreeNode((DefaultMutableTreeNode)children[i]);
      NodeComponent previousParent = (NodeComponent)agentToNode.get(agent);
      if (previousParent != null) 
        previousParent.removeAgent(agent);
      agentToNode.remove(agent); // update mapping
    }
  }

  /**
   * Tell host component to add node components.
   * Remove node components from previous host component.
   */

  private void addNodesToHost(HostComponent hostComponent,
                              Object[] newChildren) {
    for (int i = 0; i < newChildren.length; i++) {
      NodeComponent nodeComponent = 
        (NodeComponent)getComponentFromTreeNode((DefaultMutableTreeNode)newChildren[i]);
      HostComponent previousParent = 
        (HostComponent)nodeToHost.get(nodeComponent);
      // ignore moving nodes within the same host
      if (previousParent != null && previousParent.equals(hostComponent))
        return;
      hostComponent.addNode(nodeComponent);
      if (previousParent != null)
        previousParent.removeNode(nodeComponent);
      nodeToHost.put(nodeComponent, hostComponent);
    }
  }

  /**
   * Tell node component to add agent components.
   * Remove agent components from previous node component.
   */

  private void addAgentsToNode(NodeComponent nodeComponent,
			       Object[] newChildren) {
    for (int i = 0; i < newChildren.length; i++) {
      AgentComponent agentComponent = 
        (AgentComponent)getComponentFromTreeNode((DefaultMutableTreeNode)newChildren[i]);
      NodeComponent previousParent = 
        (NodeComponent)agentToNode.get(agentComponent);
      // ignore moving agents within the same node
      if (previousParent != null && previousParent.equals(nodeComponent))
        return;
      nodeComponent.addAgent(agentComponent);
      if (previousParent != null) 
        previousParent.removeAgent(agentComponent);
      agentToNode.put(agentComponent, nodeComponent); // update mapping
    }
  }

  /**
   * Called when user drags nodes off the host or node tree;
   * does nothing as updating the components is done at insertion.
   */

  public void treeNodesRemoved(TreeModelEvent e) {
  }

  /**
   * Called if user edits the name of a host or a node.
   */

  public void treeNodesChanged(TreeModelEvent e) {
    Object source = e.getSource();
    // handle user editing the name of a host in the host tree
    if (hostTree.getModel().equals(source)) {
      DefaultMutableTreeNode parent = 
        (DefaultMutableTreeNode)e.getTreePath().getLastPathComponent();
      if (((ConsoleTreeObject)parent.getUserObject()).isRoot()) {
        experimentBuilder.setModified(true);
        return;
      }
    }
    // handle user editing the name of a node in the host or node tree
    if (hostTree.getModel().equals(source) || 
        nodeTree.getModel().equals(source)) {
      Object[] children = e.getChildren();
      if (children != null && children.length > 0) {
        DefaultMutableTreeNode firstChild = 
          (DefaultMutableTreeNode)children[0];
        ConsoleTreeObject changedNode =
          (ConsoleTreeObject)firstChild.getUserObject();
        if (changedNode.isNode()) {
          ((ExperimentNode)changedNode.getComponent()).rename(changedNode.getName());
          experimentBuilder.setModified(true);
          return;
        }
      }
    }
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

  public void deleteHost() {
    TreePath[] selectedPaths = hostTree.getSelectionPaths();
    for (int i = 0; i < selectedPaths.length; i++) {
      DefaultMutableTreeNode selectedNode = 
        (DefaultMutableTreeNode)selectedPaths[i].getLastPathComponent();
      ConsoleTreeObject hostCTO = 
        (ConsoleTreeObject)selectedNode.getUserObject();
      // get any nodes that are descendants of the host being deleted
      // and return them to the unassigned nodes tree
      DefaultTreeModel nodeModel = (DefaultTreeModel)nodeTree.getModel();
      DefaultMutableTreeNode root = 
        (DefaultMutableTreeNode)nodeModel.getRoot();
      int n = selectedNode.getChildCount();
      for (int j = 0; j < n; j++) {
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
  }

  /**
   * Helper method to get value of property of selected node in specified tree.
   */

  private String getPropertyOfNode(JTree tree, String name) {
    DefaultMutableTreeNode selectedNode =
      (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    ConsoleTreeObject cto = (ConsoleTreeObject)selectedNode.getUserObject();
    BaseComponent component = cto.getComponent();
    Property prop = component.getProperty(new ComponentName(component, name));
    if (prop == null)
      return null;
    return (String)prop.getValue();
  }

  /**
   * Helper method to set value of property of selected nodes 
   * in specified tree.
   */

  private void setPropertyOfNode(JTree tree, String name, String value) {
    TreePath[] selectedPaths = tree.getSelectionPaths();
    for (int i = 0; i < selectedPaths.length; i++) {
      DefaultMutableTreeNode selectedNode =
        (DefaultMutableTreeNode)selectedPaths[i].getLastPathComponent();
      ConsoleTreeObject cto = (ConsoleTreeObject)selectedNode.getUserObject();
      BaseComponent component = cto.getComponent();
      component.addProperty(name, value);
    }
  }

    
  public void setHostDescription() {
    String description = getPropertyOfNode(hostTree, "Description");
    String s = (String)JOptionPane.showInputDialog(this,
                                           "Enter Host Description",
                                           "Host Description",
                                           JOptionPane.QUESTION_MESSAGE,
                                           null, null, description);
    if (s != null && s.length() != 0) 
      setPropertyOfNode(hostTree, "Description", s);
  }

  public void setHostType() {
    String machineType = getPropertyOfNode(hostTree, "MachineType");
    String[] machineTypes = { "Linux", "Solaris", "Windows" };
    String s = (String)JOptionPane.showInputDialog(this,
                                           "Enter Host Machine Type",
                                           "Host Machine Type",
                                           JOptionPane.QUESTION_MESSAGE,
                                           null, machineTypes, machineType);
    if (s != null && s.length() != 0) 
      setPropertyOfNode(hostTree, "MachineType", s);
  }

  public void setHostLocation() {
    String location = getPropertyOfNode(hostTree, "Location");
    String s = (String)JOptionPane.showInputDialog(this,
                                           "Enter Host Location",
                                           "Host Location",
                                           JOptionPane.QUESTION_MESSAGE,
                                           null, null, location);
    if (s != null && s.length() != 0) 
      setPropertyOfNode(hostTree, "Location", s);
  }

  /**
   * Get description of nodes from user and set in all nodes
   * selected in the host or node trees.
   */

  public void setNodeDescription() {
    boolean askedUser = false;
    String description = "";
    if (getSelectedNodesInHostTree() != null) {
      description = getPropertyOfNode(hostTree, "Description");
      description =
        (String)JOptionPane.showInputDialog(this,
                                            "Enter Node Description",
                                            "Node Description",
                                            JOptionPane.QUESTION_MESSAGE,
                                            null, null, description);
      askedUser = true;
      if (description != null && description.length() != 0)
        setPropertyOfNode(hostTree, "Description", description);
    }
    if (getSelectedNodesInNodeTree() != null) {
      if (!askedUser) {
        description = getPropertyOfNode(nodeTree, "Description");
        description =
          (String)JOptionPane.showInputDialog(this,
                                              "Enter Node Description",
                                              "Node Description",
                                              JOptionPane.QUESTION_MESSAGE,
                                              null, null, description);
      }
      if (description != null && description.length() != 0)
        setPropertyOfNode(nodeTree, "Description", description);
    }
  }

  /**
   * Pop-up input dialog to get node description from user.
   * Called with the tree from which this menu item was invoked.
   */

  private void setNodeDescription(JTree tree) {
    String description = getPropertyOfNode(tree, "Description");
    String s = (String)JOptionPane.showInputDialog(this,
                                           "Enter Node Description",
                                           "Node Description",
                                           JOptionPane.QUESTION_MESSAGE,
                                           null, null, description);
    if (s != null && s.length() != 0)
      setPropertyOfNode(tree, "Description", s);
  }

  /**
   * Pop-up input dialog to get node command line arguments from user.
   * Called with the tree from which this menu item was invoked.
   */

  public void setNodeCommandLine() {
    DefaultMutableTreeNode[] nodes = getSelectedNodesInHostTree();
    if (nodes != null) {
      setNodeCommandLine(nodes[0]);
      return;
    }
    nodes = getSelectedNodesInNodeTree();
    if (nodes != null) 
      setNodeCommandLine(nodes[0]);
  }

  private void setNodeCommandLine(DefaultMutableTreeNode selectedNode) {
    experiment.updateNameServerHostName(); // Be sure this is up-do-date
    ConsoleTreeObject cto = (ConsoleTreeObject)selectedNode.getUserObject();
    NodeComponent nodeComponent = (NodeComponent)cto.getComponent();
    Vector data = new Vector();
    // node component level properties
    NodeArgumentDialog dialog =
      new NodeArgumentDialog("Node " + nodeComponent.getShortName()
                             + " Command Line",
                             nodeComponent.getArguments(), true);
    dialog.setVisible(true);
    if (dialog.getValue() != JOptionPane.OK_OPTION)
      return; // user cancelled
    experimentBuilder.setModified(dialog.updateProperties());
  }

  /**
   * Pop-up input dialog to get global command line arguments from user.
   */

  public void setGlobalCommandLine() {
    experiment.updateNameServerHostName(); // Be sure this is up-do-date
    NodeArgumentDialog dialog = 
      new NodeArgumentDialog("Global Command Line",
                             experiment.getDefaultNodeArguments(), false);
    dialog.setVisible(true);
    if (dialog.getValue() != JOptionPane.OK_OPTION)
      return; // user cancelled
    experimentBuilder.setModified(dialog.updateProperties());
  }

  /**
   * Select a node in the host tree.
   */

  public void selectNodeInHostTree(String nodeName) {
    selectNodeInTree(hostTree, HostComponent.class, nodeName);
  }

  private boolean selectNodeInTree(JTree tree, Class componentClass,
                                   String name) {
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
    TreePath path = null;
    Enumeration nodes = root.breadthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
 	(DefaultMutableTreeNode)nodes.nextElement();
        if (node.getUserObject() instanceof ConsoleTreeObject) {
          ConsoleTreeObject cto = (ConsoleTreeObject)node.getUserObject();
          if (cto.getComponent() != null &&
              cto.getComponent().getClass().equals(componentClass) &&
              cto.getName().equals(name)) {
            path = new TreePath(node.getPath());
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
            return true;
          } 
        }
    }
    return false;
  }

  public void addHostTreeSelectionListener(TreeSelectionListener listener) {
    hostTree.addTreeSelectionListener(listener);
  }

  public void removeHostTreeSelectionListener(TreeSelectionListener listener) {
    hostTree.removeTreeSelectionListener(listener);
  }

  private DefaultTreeModel createModel(final Experiment experiment, DefaultMutableTreeNode node, boolean askKids) {
    return new DefaultTreeModel(node, askKids) {
	public void valueForPathChanged(TreePath path, Object newValue) {
	  if (newValue == null || newValue.toString().equals("")) return;
	  // Allow renaming hosts or Nodes only
	  DefaultMutableTreeNode aNode = (DefaultMutableTreeNode)path.getLastPathComponent();
	  ConsoleTreeObject cto = (ConsoleTreeObject)aNode.getUserObject();
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

  /**
   * Display dialog of component names and ask user to select one.
   * Search the trees for the component the user selected and select it.
   * @param trees trees to search (i.e. hostTree, nodeTree, agentTree)
   * @param components list of components from which user should select
   * @param label to use in dialog boxes (i.e. Host, Node, Agent)
   */

  private void findWorker(JTree[] trees, BaseComponent[] components,
                          String label) {
    if (components.length == 0) {
      JOptionPane.showMessageDialog(this, "No " + label + "s.");
      return;
    }
    Vector names = new Vector(components.length);
    for (int i = 0; i < components.length; i++)
      names.add(components[i].getShortName());
    Collections.sort(names);
    String[] choices = (String[])names.toArray(new String[names.size()]);
    Object answer = 
      JOptionPane.showInputDialog(this, "Select " + label,
                                  "Find " + label,
                                  JOptionPane.QUESTION_MESSAGE,
                                  null,
                                  choices,
                                  null);
    if (answer == null)
      return;
    for (int i = 0; i < trees.length; i++)
      if (selectNodeInTree(trees[i], components[0].getClass(), (String)answer))
        return;
  }

  public void findHost() {
    JTree[] trees = new JTree[1];
    trees[0] = hostTree;
    findWorker(trees, experiment.getHosts(), "Host");
  }

  public void findNode() {
    JTree[] trees = new JTree[2];
    trees[0] = hostTree;
    trees[1] = nodeTree;
    findWorker(trees, experiment.getNodes(), "Node");
  }

  public void findAgent() {
    JTree[] trees = new JTree[3];
    trees[0] = hostTree;
    trees[1] = nodeTree;
    trees[2] = agentTree;
    findWorker(trees, experiment.getAgents(), "Agent");
  }

  private boolean isOnlyRootSelected(JTree tree) {
    TreePath[] selectedPaths = tree.getSelectionPaths();
    if (selectedPaths == null || selectedPaths.length > 1)
      return false;
    DefaultMutableTreeNode selNode =
      (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
    return selNode.isRoot();
  }

  /**
   * Returns true if the host tree root is selected and nothing
   * else in the host tree is selected.
   * @return true if the Host Tree root and only that is selected
   */

  public boolean isHostTreeRootSelected() {
    return isOnlyRootSelected(hostTree);
  }

  /**
   * Returns true if the unassigned Nodes tree root is selected and nothing
   * else in that tree is selected.
   * @return true if the Node Tree root and only that is selected
   */

  public boolean isNodeTreeRootSelected() {
    return isOnlyRootSelected(nodeTree);
  }

  private DefaultMutableTreeNode[] getSelectedItemsInTree(JTree tree,
                                                          Class desiredClass) {
    ArrayList nodes = new ArrayList();
    TreePath[] selectedPaths = tree.getSelectionPaths();
    if (selectedPaths == null)
      return null;
    for (int i = 0; i < selectedPaths.length; i++) {
      DefaultMutableTreeNode selNode =
        (DefaultMutableTreeNode)selectedPaths[i].getLastPathComponent();
      ConsoleTreeObject selected =
        (ConsoleTreeObject)selNode.getUserObject();
      if (desiredClass.isInstance(selected.getComponent()))
        nodes.add(selNode);
      else
        return null;
    }
    if (nodes.size() == 0)
      return null;
    return (DefaultMutableTreeNode[])nodes.toArray(new DefaultMutableTreeNode[nodes.size()]);
  }

  /**
   * Returns an array of selected Hosts in the Host tree, if and only
   * if at least one Host, and only Hosts are selected, else returns null.
   * @return array of tree nodes representing Hosts
   */

  public DefaultMutableTreeNode[] getSelectedHostsInHostTree() {
    return getSelectedItemsInTree(hostTree, HostComponent.class);
  }

  /**
   * Returns an array of selected Nodes in the Host tree, if and only
   * if at least one Node, and only Nodes are selected, else returns null.
   * @return array of tree nodes representing Nodes
   */

  public DefaultMutableTreeNode[] getSelectedNodesInHostTree() {
    return getSelectedItemsInTree(hostTree, NodeComponent.class);
  }

  /**
   * Returns an array of selected Nodes in the Node tree, if and only
   * if at least one Node, and only Nodes are selected, else returns null.
   * @return array of tree nodes representing Nodes
   */

  public DefaultMutableTreeNode[] getSelectedNodesInNodeTree() {
    return getSelectedItemsInTree(nodeTree, NodeComponent.class);
  }

  private BaseComponent getComponentFromPath(TreePath path) {
    DefaultMutableTreeNode node =
      (DefaultMutableTreeNode)path.getLastPathComponent();
    ConsoleTreeObject cto = (ConsoleTreeObject)(node.getUserObject());
    return cto.getComponent();
  }

  private BaseComponent getComponentFromTreeNode(DefaultMutableTreeNode treeNode) {
    ConsoleTreeObject cto = (ConsoleTreeObject)treeNode.getUserObject();
    return cto.getComponent();
  }
}




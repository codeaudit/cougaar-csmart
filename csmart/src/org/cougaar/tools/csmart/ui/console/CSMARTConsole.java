/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.console;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.tree.*;

import com.klg.jclass.chart.JCChart;

import org.cougaar.tools.csmart.scalability.ScalabilityXSociety;
import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.experiment.Experiment;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.server.*;
import org.cougaar.tools.server.rmi.ClientCommunityController;
import java.net.URL;
import org.cougaar.tools.csmart.ui.Browser;

public class CSMARTConsole extends JFrame implements TreeSelectionListener, TreeModelListener, ChangeListener {
  CSMART csmart; // top level viewer, gives access to save method, etc.
  /** 
   * Name of the remote registry that contains the runtime information.
   */
  public static final String DEFAULT_SERVER_NAME = "ServerHook";

  JFrame consoleFrame;
  CommunityServesClient communitySupport;
  String nameServerHostName;
  SocietyComponent societyComponent;
  Experiment experiment;
  Hashtable runningNodes; // maps NodeComponents to NodeServesClient
  Hashtable oldNodes; // store old charts till next experiment is started
  Hashtable charts; // maps node name to idle time chart
  Hashtable chartFrames; // maps node name to chart frame

  // gui controls
  JTabbedPane tabbedPane;
  JPopupMenu hostRootMenu;
  JPopupMenu hostHostMenu;
  JPopupMenu hostNodeMenu;
  JPopupMenu nodeRootMenu;
  JPopupMenu nodeNodeMenu;
  DNDTree hostTree;
  DNDTree nodeTree;
  DNDTree agentTree;
  ButtonGroup statusButtons;
  JButton runButton;
  JButton stopButton;
  JButton abortButton;
  JMenuItem historyMenuItem; // enabled only if experiment is running
  JPanel buttonPanel; // contains status buttons
  public static Dimension HGAP10 = new Dimension(10,1);
  public static Dimension VGAP30 = new Dimension(1,30);
  Border loweredBorder = new CompoundBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED), new EmptyBorder(5,5,5,5));

  // top level menus and menu items
  private static final String FILE_MENU = "File";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String NODE_MENU = "Node";
  private static final String HISTORY_MENU_ITEM = "Utilization History";
  private static final String STATUS_MENU_ITEM = "Status";
  private static final String LOAD_MENU_ITEM = "Load";
  private static final String DESCRIBE_MENU_ITEM = "Describe";
  private static final String HELP_MENU = "Help";

  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "../help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";

  // menu items for popup menu in hostTree
  private static final String NEW_HOST_MENU_ITEM = "New Host";
  private static final String NEW_NODE_MENU_ITEM = "New Node";
  private static final String DELETE_MENU_ITEM = "Remove";
  //  private static final String DESCRIBE_MENU_ITEM = "Describe";

  // status button colors
  public static Color busyStatus = new Color(230, 255, 230); // shades of green
  public static Color highBusyStatus = new Color(175, 255, 175);
  public static Color mediumHighBusyStatus = new Color(0, 255, 0);
  public static Color mediumBusyStatus = new Color(0, 235, 0);
  public static Color mediumLowBusyStatus = new Color(0, 215, 0);
  public static Color lowBusyStatus = new Color(0, 195, 0);
  public static Color idleStatus = new Color(0, 175, 0);
  public static Color errorStatus = new Color(215, 0, 0); // red
  public static Color noAnswerStatus = new Color(245, 245, 0); // yellow
  public static Color unknownStatus = new Color(180, 180, 180); //gray

  // used for log file name
  private static DateFormat fileDateFormat =
                  new SimpleDateFormat("yyyyMMddHHmmss");

  /**
   * Create and show console GUI.
   */

  public CSMARTConsole(CSMART csmart) {
    this.csmart = csmart;
    experiment = csmart.getExperiment();
    // TODO: support experiments with multiple societies
    societyComponent = experiment.getSocietyComponent(0);
    setSocietyComponent(societyComponent);
  }

  /**
   * Set the society component for which to display information and run.
   */

  private void setSocietyComponent(SocietyComponent cc) {
    communitySupport = new ClientCommunityController();
    runningNodes = new Hashtable();
    oldNodes = new Hashtable();
    charts = new Hashtable();
    chartFrames = new Hashtable();

    // top level menus
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.setToolTipText("Save configuration or exit.");
    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.setToolTipText("Exit this tool.");
    exitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	exitMenuItem_actionPerformed(e);
      }
    });
    fileMenu.add(exitMenuItem);

    JMenu nodeMenu = new JMenu(NODE_MENU);
    nodeMenu.setToolTipText("Display information about a node.");
    historyMenuItem = new JMenuItem(HISTORY_MENU_ITEM);
    historyMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	historyMenuItem_actionPerformed(e);
      }
    });
    historyMenuItem.setEnabled(false); // enable when node is run
    historyMenuItem.setToolTipText("Display load at a node vs. time.");
    nodeMenu.add(historyMenuItem);
    JMenuItem statusMenuItem = new JMenuItem(STATUS_MENU_ITEM);
    statusMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	statusMenuItem_actionPerformed(e);
      }
    });
    statusMenuItem.setEnabled(false);
    nodeMenu.add(statusMenuItem);
    JMenuItem loadMenuItem = new JMenuItem(LOAD_MENU_ITEM);
    loadMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	loadMenuItem_actionPerformed(e);
      }
    });
    loadMenuItem.setEnabled(false);
    nodeMenu.add(loadMenuItem);
    JMenuItem describeMenuItem = new JMenuItem(DESCRIBE_MENU_ITEM);
    describeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	describeMenuItem_actionPerformed(e);
      }
    });
    describeMenuItem.setEnabled(false);
    nodeMenu.add(describeMenuItem);

    JMenu helpMenu = new JMenu(HELP_MENU);
    JMenuItem helpMenuItem = new JMenuItem(HELP_MENU_ITEM);
    helpMenuItem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  URL help = (URL)this.getClass().getResource(HELP_DOC);
	  if (help != null)
	    Browser.setPage(help);
	}
      });
    helpMenu.add(helpMenuItem);
    JMenuItem aboutMenuItem = new JMenuItem(ABOUT_CSMART_ITEM);
    aboutMenuItem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  URL about = (URL)this.getClass().getResource(ABOUT_DOC);
	  if (about != null)
	    Browser.setPage(about);
	}
      });
    helpMenu.add(aboutMenuItem);
    

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(nodeMenu);
    menuBar.add(helpMenu);
    getRootPane().setJMenuBar(menuBar);

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
    addHostsFromExperiment();
    // create new host components for hosts named in config file
    addHostsFromFile();
    JScrollPane hostTreeScrollPane = new JScrollPane(hostTree);
    hostTreeScrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    hostPane.setTopComponent(hostTreeScrollPane);
    // add listeners last so that they don't fire during initialization
    hostTree.addTreeSelectionListener(this);
    hostTree.getModel().addTreeModelListener(this);

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
    // TODO: handle describe host, node and agent
    // TODO: add Agent menu with one item -- describe
    hostRootMenu.add(newHostMenuItem);

    hostHostMenu.add(newNodeInHostMenuItem);
    hostHostMenu.add(deleteHostMenuItem);
    describeMenuItem = new JMenuItem(DESCRIBE_MENU_ITEM);
    describeMenuItem.setEnabled(false);
    hostHostMenu.add(describeMenuItem);

    hostNodeMenu.add(deleteNodeInHostMenuItem);
    describeMenuItem = new JMenuItem(DESCRIBE_MENU_ITEM);
    describeMenuItem.setEnabled(false);
    hostNodeMenu.add(describeMenuItem);
    
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
    addUnassignedNodesFromExperiment();
    nodeTree.getModel().addTreeModelListener(this);
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
    // TODO: implement describe menu item
    describeMenuItem = new JMenuItem(DESCRIBE_MENU_ITEM);
    describeMenuItem.setEnabled(false);
    nodeNodeMenu.add(describeMenuItem);
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
    addUnassignedAgentsFromExperiment();
    JScrollPane agentTreeScrollPane = new JScrollPane(agentTree);
    agentTreeScrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    bottomPane.setBottomComponent(agentTreeScrollPane);

    hostPane.setBottomComponent(bottomPane);

    // create right top panel which contains
    // description and start/stop buttons
    // and status buttons
    JPanel rightPanel = createVerticalPanel(true);
    JPanel descriptionPanel = createHorizontalPanel(false);
    // placeholder for description
    descriptionPanel.add(new JLabel(cc.getSocietyName()));
    runButton = new JButton("Run");
    runButton.setToolTipText("Start running experiments.");
    runButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	runButton_actionPerformed(e);
      }
    });
    descriptionPanel.add(Box.createRigidArea(HGAP10));
    descriptionPanel.add(Box.createRigidArea(VGAP30));
    descriptionPanel.add(runButton);
    
    stopButton = new JButton("Stop");
    stopButton.setToolTipText("Stop running experiments when the current experiment completes.");
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	stopButton_actionPerformed(e);
      }
    });
    descriptionPanel.add(stopButton);

    abortButton = new JButton("Abort");
    abortButton.setToolTipText("Stop experiment now and discard results.");
    abortButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	abortButton_actionPerformed(e);
      }
    });
    descriptionPanel.add(abortButton);
    descriptionPanel.add(Box.createRigidArea(HGAP10));
    rightPanel.add(descriptionPanel);
    // create status button panel, initially empty
    buttonPanel = createHorizontalPanel(false);
    buttonPanel.add(new JLabel("Node Status"));
    buttonPanel.add(Box.createRigidArea(HGAP10));
    buttonPanel.add(Box.createRigidArea(VGAP30));
    rightPanel.add(buttonPanel);
    statusButtons = new ButtonGroup();

    // create task completion progress bar
//     JPanel progressPanel = createHorizontalPanel(false);
//     progressPanel.add(new JLabel("Task Completion Progress"));
//     progressPanel.add(Box.createRigidArea(HGAP10));
//     progressPanel.add(new JProgressBar());
//     progressPanel.add(Box.createRigidArea(VGAP30));
//     rightPanel.add(progressPanel);

    // create tabbed panes, tabs are added dynamically
    tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener(this);
    rightPanel.add(tabbedPane);

    JSplitPane topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    topPane.setLeftComponent(hostPane);
    topPane.setRightComponent(rightPanel);
    getContentPane().add(topPane);

    // enable run button if have experiment with at least one host, node,
    // and agent
    setRunButtonEnabled();
    stopButton.setEnabled(false);
    abortButton.setEnabled(false);
    hostTree.setEditable(true);
    nodeTree.setEditable(true);
    agentTree.setEditable(true);

    // fully expand trees
    expandTree(hostTree);
    expandTree(nodeTree);
    expandTree(agentTree);

    // add a WindowListener: Do an exit to kill the Nodes
    // If this window is closing
    // Note that the main CSMART UI handles actually disposing
    // this frame
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	exitMenuItem_actionPerformed(e);
      }
    });

    pack();
    setSize(600, 600);
    setVisible(true);
    hostPane.setDividerLocation(200);
    bottomPane.setDividerLocation(200);
    topPane.setDividerLocation(200); // sets width of tree area
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
   * Create a panel whose components are layed out vertically.
   */

  private JPanel createVerticalPanel(boolean threeD) {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setAlignmentY(TOP_ALIGNMENT);
    p.setAlignmentX(LEFT_ALIGNMENT);
    if(threeD) {
      p.setBorder(loweredBorder);
    }
    return p;
  }

  /**
   * Create a panel whose components are layed out horizontally.
   */

  private JPanel createHorizontalPanel(boolean threeD) {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.setAlignmentY(TOP_ALIGNMENT);
    p.setAlignmentX(LEFT_ALIGNMENT);
    if(threeD) {
      p.setBorder(loweredBorder);
    }
    return p;
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
      return c1.getName().compareTo(c2.getName());
    }
  };

  private void addUnassignedAgentsFromExperiment() {
    Set unassignedAgents = new TreeSet(configurableComponentComparator);
    AgentComponent[] agents = experiment.getAgents();
    NodeComponent[] nodes = experiment.getNodes();
    unassignedAgents.addAll(Arrays.asList(agents));
    for (int i = 0; i < nodes.length; i++)
      unassignedAgents.removeAll(Arrays.asList(nodes[i].getAgents()));
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)agentTree.getModel().getRoot();
    DefaultTreeModel model = (DefaultTreeModel)agentTree.getModel();
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
   * Create a button representing a node.
   */

  private JRadioButton createStatusButton(String nodeName) {
    // use unknown color
    JRadioButton button = 
      new JRadioButton(new ColoredCircle(unknownStatus, 20));
    button.setSelectedIcon(new SelectedColoredCircle(unknownStatus, 20));
    button.setToolTipText(nodeName);
    button.setActionCommand(nodeName);
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setMargin(new Insets(2,2,2,2));
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	statusButton_actionPerformed(e);
      }
    });
    return button;
  }

  /**
   * Add status button to status button display.
   */

  private void addStatusButton(JRadioButton button) {
    statusButtons.add(button);
    buttonPanel.add(button);
  }

  /**
   * Enables run button if experiment has at least one host that has at least
   * one node to run.
   * Called after any change in host tree.
   */

  private void setRunButtonEnabled() {
    ArrayList nodes = new ArrayList();
    DefaultTreeModel model = (DefaultTreeModel)hostTree.getModel();
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)model.getRoot();
    int nHosts = root.getChildCount();
    for (int i = 0; i < nHosts; i++) {
      DefaultMutableTreeNode hostNode = 
	(DefaultMutableTreeNode)root.getChildAt(i);
      int nNodes = hostNode.getChildCount();
      if (nNodes != 0) {
	runButton.setEnabled(true);
	return;
      }
    }
    runButton.setEnabled(false);
  }

  private boolean nodeHasAgents(DefaultMutableTreeNode node) {
    if (node.getChildCount() > 0)
      return true;
    else
      return false;
  }

  private boolean hostHasNodeWithAgents(DefaultMutableTreeNode hostNode) {
    for (int i = 0; i < hostNode.getChildCount(); i++) {
      DefaultMutableTreeNode nodeNode = (DefaultMutableTreeNode)hostNode.getChildAt(i);
      if (nodeNode.getChildCount() > 0)
	return true;
    }
    return false;
  }
  
  /**
   * User selected "Run" button.
   * For each host which is assigned a node,
   * start the node on that host, and create a status button and
   * tabbed pane for it.
   * Disable editing the trees until the experiment is stopped or aborted.
   */
  public void runButton_actionPerformed(ActionEvent e) {
    destroyOldNodes(); // Get rid of any old stuff before creating the new
    ArrayList nodes = new ArrayList();
    ArrayList hostNames = new ArrayList();
    DefaultTreeModel model = (DefaultTreeModel)hostTree.getModel();
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)model.getRoot();
    String firstNodeName = null;
    nameServerHostName = null;
    int nHosts = root.getChildCount();
    for (int i = 0; i < nHosts; i++) {
      DefaultMutableTreeNode hostNode = 
	(DefaultMutableTreeNode)root.getChildAt(i);
      ConsoleTreeObject cto = (ConsoleTreeObject)hostNode.getUserObject();
      String hostName = cto.getName();
      int nNodes = hostNode.getChildCount();
      // Should also skip those Nodes which have no Agents assigned
      // Next if all Nodes have no Agents
      if (! hostHasNodeWithAgents(hostNode))
        continue;
      if (nNodes != 0 && nameServerHostName == null) {
	nameServerHostName = hostName; // use first host as name server
	DefaultMutableTreeNode firstNode = 
	  (DefaultMutableTreeNode)hostNode.getChildAt(0);
	cto = (ConsoleTreeObject)firstNode.getUserObject();
	firstNodeName = cto.getName();
      }
      for (int j = 0; j < nNodes; j++) {
	DefaultMutableTreeNode node = 
	  (DefaultMutableTreeNode)hostNode.getChildAt(j);
	// If this Node has no agents, don't include it
	if (! nodeHasAgents(node))
	  continue;
	cto = (ConsoleTreeObject)node.getUserObject();
	String nodeName = cto.getName();
	// Skip to next Node if this Node has no Agents
	nodes.add(cto.getComponent());
	hostNames.add(hostName);
      } // end of loop over Nodes on Host
    } // end of loop over hosts in tree
    ConfigurationWriter configWriter =
      experiment.getConfigurationWriter((NodeComponent[])nodes.toArray(new NodeComponent[nodes.size()]));
    for (int i = 0; i < nodes.size(); i++) {
      NodeComponent nodeComponent = (NodeComponent)nodes.get(i);
      createNode(nodeComponent, nodeComponent.toString(), 
		 (String)hostNames.get(i),
		 configWriter);
    }
    // select first node
    if (firstNodeName != null) {
      selectTabbedPane(firstNodeName);
      selectStatusButton(firstNodeName);
      selectNodeInHostTree(firstNodeName);
    }
  }

  /**
   * Stop all nodes.
   * If all societies in the experiment are self terminating, 
   * just tell the experiment to stop after the current trial.
   * If any society is not self terminating, determine if the experiment
   * is being monitored, and if so, ask the user to confirm the stop. 
   */

  public void stopButton_actionPerformed(ActionEvent e) {
    // determine if societies in an experiment are self terminating
    Experiment experiment = csmart.getExperiment();
    if (experiment == null) {
      System.err.println("Console lost the experiment!!!");
      return;
    }
    int nSocieties = experiment.getSocietyComponentCount();
    boolean isSelfTerminating = true;
    for (int i = 0; i < nSocieties; i++) {
      SocietyComponent society = experiment.getSocietyComponent(i);
      if (!society.isSelfTerminating()) {
	isSelfTerminating = false;
	break;
      }
    }
    // just tell the experiment to stop after the current trial
    // societies in experiment will self terminate
    if (isSelfTerminating) {
      experiment.stop();
      return;
    }
    // need to manually stop some societies
    // if societies are being monitored, ask user to confirm stop
    if (experiment.isMonitored()) {
      int response = 
	JOptionPane.showConfirmDialog(this,
				      "Experiment is being monitored; stop anyway (society monitor displays will be destroyed)?",
				      "Confirm Stop",
				      JOptionPane.YES_NO_OPTION);
      if (response == JOptionPane.YES_OPTION) 
	stopAllNodes();
    } else
      stopAllNodes();
  }

  /**
   * Abort all nodes, (using NodeServesClient interface which is
   * returned by createNode).
   */
  public void abortButton_actionPerformed(ActionEvent e) {
    stopAllNodes();
  }

  /**
   * Stop all experiments.  Called before exiting CSMART.
   */
  public void stopExperiments() {
    stopAllNodes(); // stop the nodes
    destroyOldNodes(); // kill all their outputs
  }

  // Stop the nodes, but don't kill the tabbed panes
  private void stopAllNodes() {
    Enumeration nodeComponents = runningNodes.keys();
    while (nodeComponents.hasMoreElements()) {
      NodeComponent nodeComponent = 
	(NodeComponent)nodeComponents.nextElement();
      NodeServesClient nsc = (NodeServesClient)runningNodes.get(nodeComponent);
      String nodeName = nodeComponent.toString();
      if (nsc == null) {
        System.err.println("Unknown node name: " + nodeName);
	continue;
      }
      try {
        nsc.flushNodeEvents();
        nsc.destroy();
      } catch (Exception ex) {
        System.err.println("Unable to destroy node: " + ex);
	continue;
      }
      // Don't get rid of the old tabbed panes - good for debugging
      oldNodes.put(nodeComponent, nsc);
      runningNodes.remove(nodeComponent);
    }
    updateExperimentControls(experiment, false);
  }

  // Kill any existing tabbed panes or history charts
  private void destroyOldNodes() {
    Enumeration nodeComponents = oldNodes.keys();
    while (nodeComponents.hasMoreElements()) {
      NodeComponent nodeComponent = 
	(NodeComponent)nodeComponents.nextElement();
      String nodeName = nodeComponent.toString();
      removeTabbedPane(nodeName);
      removeStatusButton(nodeName);
      oldNodes.remove(nodeComponent);
    }
    // dispose of all chart frames
    Collection frames = chartFrames.values();
    Iterator iter = frames.iterator();
    while (iter.hasNext()) {
      JFrame frame = (JFrame)iter.next();
      NamedFrame.getNamedFrame().removeFrame(frame);
      frame.dispose();
    }
    chartFrames.clear();
    charts.clear();
  }
    
  private void updateExperimentControls(Experiment experiment,
					boolean isRunning) {
    if (!isRunning) {
      experiment.experimentStopped();
      csmart.setRunningExperiment(null);
    } else
      csmart.setRunningExperiment(experiment);
    // update societies
    int nSocieties = experiment.getSocietyComponentCount();
    for (int i = 0; i < nSocieties; i++)
      experiment.getSocietyComponent(i).setRunning(isRunning);
    // update trees, buttons, menu items
    hostTree.setEditable(!isRunning);
    nodeTree.setEditable(!isRunning);
    agentTree.setEditable(!isRunning);
    runButton.setEnabled(!isRunning);
    stopButton.setEnabled(isRunning);
    abortButton.setEnabled(isRunning);
    historyMenuItem.setEnabled(isRunning);
  }

  /**
   * Called by ConsoleNodeListener when node has stopped.
   * If all nodes are stopped, then experiment is stopped;
   * update the gui controls.
   */

  public void nodeStopped(NodeComponent nodeComponent) {
    oldNodes.put(nodeComponent, runningNodes.get(nodeComponent));
    runningNodes.remove(nodeComponent);
    String nodeName = nodeComponent.toString();
    //removeTabbedPane(nodeName);
    //removeStatusButton(nodeName);
    updateExperimentControls(experiment, false);
  }

  /**
   * TreeSelectionListener interface.
   * If user selects an agent in the "Hosts" tree,
   * then pop the tabbed pane for that node to the foreground, and
   * select the node status button.
   */

  public void valueChanged(TreeSelectionEvent event) {
    TreePath path = event.getPath();
    if (path == null) return;
    DefaultMutableTreeNode treeNode =
      (DefaultMutableTreeNode)path.getLastPathComponent();
    ConsoleTreeObject cto = (ConsoleTreeObject)treeNode.getUserObject();
    if (!cto.isNode())
      return; // ignore selecting if it's not a node
    String nodeName = 
      ((ConsoleTreeObject)treeNode.getUserObject()).getName();
    // select tabbed pane if one exists
    selectTabbedPane(nodeName);
    selectStatusButton(nodeName);
  }

  /**
   * If user selects a status button, then pop the tabbed pane
   * for that node to the foreground, and select the node in the host
   * tree.
   */

  public void statusButton_actionPerformed(ActionEvent e) {
    String nodeName = ((JRadioButton)e.getSource()).getActionCommand();
    selectNodeInHostTree(nodeName);
    selectTabbedPane(nodeName);
    displayStripChart(nodeName);
  }

  /**
   * Display strip chart for node name.
   */

  public void displayStripChart(String nodeName) {
    JFrame f = (JFrame)chartFrames.get(nodeName);
    if (f != null) {
      f.toFront();
      f.setState(Frame.NORMAL);
      return;
    }
    JCChart chart = (JCChart)charts.get(nodeName);
    if (chart == null)
      return;
    final Frame newChartFrame = new StripChartFrame(chart);
    newChartFrame.setTitle(NamedFrame.getNamedFrame().addFrame(nodeName, (JFrame)newChartFrame));
    newChartFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	chartFrames.remove(newChartFrame.getTitle());
	NamedFrame.getNamedFrame().removeFrame((JFrame)newChartFrame);
	newChartFrame.dispose();
      }
    });
    chartFrames.put(nodeName, newChartFrame);
  }

  /**
   * ChangeListener interface (for tabbed panes).
   * If user selects a tab, then select the node in the "Nodes" tree,
   * and select the node's button in the status buttons.
   */

  public void stateChanged(ChangeEvent e) {
    JTabbedPane tabbedPane = (JTabbedPane)e.getSource();
    String nodeName = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
    selectStatusButton(nodeName);
    selectNodeInHostTree(nodeName);
    JFrame f = (JFrame)chartFrames.get(nodeName);
    if (f != null) {
      f.toFront();
      f.setState(Frame.NORMAL);
    }
  }

  /**
   * Select node in host tree.
   */

  private void selectNodeInHostTree(String nodeName) {
    DefaultTreeModel model = (DefaultTreeModel)hostTree.getModel();
    DefaultMutableTreeNode root = 
      (DefaultMutableTreeNode)model.getRoot();
    TreePath path = null;
    Enumeration nodes = root.breadthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = 
	(DefaultMutableTreeNode)nodes.nextElement();
      if (node.getUserObject() instanceof ConsoleTreeObject) {
	ConsoleTreeObject cto = (ConsoleTreeObject)node.getUserObject();
	if (cto.isNode()) {
	  if (cto.getName().equals(nodeName)) {
	    path = new TreePath(node.getPath());
	    break;
	  }
	}
      }
    }
    if (path != null) {
      hostTree.removeTreeSelectionListener(this);
      hostTree.setSelectionPath(path);
      hostTree.addTreeSelectionListener(this);
    }
  }

  /**
   * Select tabbed pane for node if it exists.
   */

  private void selectTabbedPane(String nodeName) {
    int i = tabbedPane.indexOfTab(nodeName);
    if (i != -1) 
      tabbedPane.setSelectedIndex(i);
  }

  /**
   * Remove tabbed pane for node if it exists.
   * Remove tabbed pane change listener or stateChanged method gets called.
   */

  private void removeTabbedPane(String nodeName) {
    int i = tabbedPane.indexOfTab(nodeName);
    if (i != -1) {
      tabbedPane.removeChangeListener(this);
      tabbedPane.remove(i);
      tabbedPane.addChangeListener(this);
    }
  }

  /**
   * Select status button for node.
   */

  private void selectStatusButton(String nodeName) {
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      JRadioButton button = (JRadioButton)buttons.nextElement();
      if (button.getActionCommand().equals(nodeName)) {
	button.setSelected(true);
	break;
      }
    }
  }

  /**
   * Remove status button for node if it exists.
   */

  private void removeStatusButton(String nodeName) {
    Enumeration buttons = statusButtons.getElements();
    while (buttons.hasMoreElements()) {
      JRadioButton button = (JRadioButton)buttons.nextElement();
      if (button.getActionCommand().equals(nodeName)) {
	statusButtons.remove(button);
	buttonPanel.remove(button);
	break;
      }
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
    setRunButtonEnabled();
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
    setRunButtonEnabled();
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
    setRunButtonEnabled();
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
    setRunButtonEnabled();
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
    setRunButtonEnabled();
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

  /**
   * Create a node and add a tab and status button for it.
   * Create a node event listener and pass it the status button
   * so that it can update it.
   */

  private void createNode(NodeComponent nodeComponent,
			  String nodeName, String hostName,
			  ConfigurationWriter configWriter) {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    JTextPane pane = new JTextPane(doc);
    JScrollPane stdoutPane = new JScrollPane(pane);

    // create a status button
    JRadioButton statusButton = createStatusButton(nodeName);

    // create a node event listener to get events from the node
    NodeEventListener listener = null;
    try {
      listener = new ConsoleNodeListener(this,
					 nodeComponent,
					 getLogFileName(nodeName), 
					 doc,
					 statusButton);
    } catch (Exception e) {
      System.err.println("Unable to create output for: " + nodeName);
      e.printStackTrace();
      return;
    }

    // set up idle chart
    JCChart chart = new StripChart();
    StripChartSource chartDataModel = new StripChartSource(chart);
    ((StripChart)chart).init(chartDataModel);
    ((ConsoleNodeListener)listener).setIdleChart(chart, chartDataModel);
    charts.put(nodeName, chart);

    // create node event filter with no buffering
    // so that idle display is "smooth"
    NodeEventFilter filter = new NodeEventFilter();
    Properties properties = new Properties();
    properties.put("org.cougaar.node.name", nodeName);
    String nameServerPorts = "8888:5555";
    properties.put("org.cougaar.tools.server.nameserver.ports", nameServerPorts);
    properties.put("org.cougaar.name.server", 
		   nameServerHostName + ":" + nameServerPorts);
    String regName = DEFAULT_SERVER_NAME;
    properties.put("org.cougaar.tools.server.name", DEFAULT_SERVER_NAME);
    int port = 8484;
    String[] args = new String[4];
    args[0] = "-f";
    args[1] = nodeName + ".ini";
    args[2] = "-controlPort";
    args[3] = Integer.toString(port);
//     System.out.println("CSMARTConsole: creating node with:" + 
// 		       " Host: " + hostName + 
// 		       " Port: " + port +
// 		       " Registry: " + regName +
// 		       " Node: " + nodeName +
// 		       " Args: " + args[0] +
// 		       " Args: " + args[1] +
// 		       " Args: " + args[2] +
// 		       " Args: " + args[3] +
// 		       " Listener: " + listener +
// 		       " Filter: " + filter +
// 		       " NodeComponent: " + nodeComponent);
//     System.out.println(" Properties: ");
//     properties.list(System.out);

    // write the node component properties
    try {
      configWriter.writeConfigFiles(new File("."));
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
				    "Cannot write configuration on: " + 
				    hostName +
				    "; check that server is running");
      e.printStackTrace();
      return;
    }

    // create the node
    try {
      NodeServesClient nsc = 
 	communitySupport.createNode(hostName, port, regName,
				    nodeName, properties,
				    args, listener, filter, 
				    configWriter);
      if (nsc != null)
	runningNodes.put(nodeComponent, nsc);
    } catch (Exception e) {
       System.out.println("CSMARTConsole: cannot create node: " + nodeName);
       JOptionPane.showMessageDialog(this,
				     "Cannot create node on: " + hostName +
				     "; check that server is running");
       e.printStackTrace();
       return;
    }

    // only add gui controls if successfully created node
    tabbedPane.add(nodeName, stdoutPane);
    addStatusButton(statusButton);
    updateExperimentControls(experiment, true);
  }

  /**
   * Create a log file name which is of the form:
   * agent name + date + .log
   */
  private String getLogFileName(String agentName) {
    return agentName + fileDateFormat.format(new Date()) + ".log";
  }

  /**
   * Action listeners for top level menus.
   */
  public void exitMenuItem_actionPerformed(AWTEvent e) {
    stopExperiments();
    // If this was this frame's exit menu item, we have to remove
    // the window from the list
    // if it was a WindowClose, the parent notices this as well
    if (e instanceof ActionEvent)
      NamedFrame.getNamedFrame().removeFrame(this);
    dispose();
  }

  public void historyMenuItem_actionPerformed(ActionEvent e) {
    // get selected node from tab
    // tree, tabs, and status buttons should be in sync
    int index = tabbedPane.getSelectedIndex();
    if (index == -1)
      return;
    String nodeName = tabbedPane.getTitleAt(index);
    displayStripChart(nodeName);
  }

  public void statusMenuItem_actionPerformed(ActionEvent e) {
  }

  public void loadMenuItem_actionPerformed(ActionEvent e) {
  }

  public void describeMenuItem_actionPerformed(ActionEvent e) {
  }

  public static void main(String[] args) {
    CSMARTConsole console = new CSMARTConsole(null);
    // for debugging, create our own society
    SocietyComponent sc = (SocietyComponent)new org.cougaar.tools.csmart.scalability.ScalabilityXSociety();
    console.setSocietyComponent(sc);
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

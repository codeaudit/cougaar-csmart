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

import com.klg.jclass.chart.JCChart;

import org.cougaar.tools.csmart.scalability.ScalabilityXSociety;
import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.experiment.Experiment;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.server.*;
import org.cougaar.tools.server.rmi.ClientCommunityController;
import java.net.URL;
import org.cougaar.tools.csmart.ui.Browser;

public class CSMARTConsole extends JFrame implements ChangeListener {
  CSMART csmart; // top level viewer, gives access to save method, etc.
  // Name of the remote registry that contains the runtime information.
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

    // create right top panel which contains
    // description and start/stop buttons
    // and status buttons
    JPanel panel = createVerticalPanel(true);
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
    panel.add(descriptionPanel);
    // create status button panel, initially empty
    buttonPanel = createHorizontalPanel(false);
    buttonPanel.add(new JLabel("Node Status"));
    buttonPanel.add(Box.createRigidArea(HGAP10));
    buttonPanel.add(Box.createRigidArea(VGAP30));
    panel.add(buttonPanel);
    statusButtons = new ButtonGroup();

    // create tabbed panes, tabs are added dynamically
    tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener(this);
    panel.add(tabbedPane);

    getContentPane().add(panel);

    // enable run button if have experiment with at least one host, node,
    // and agent
    setRunButtonEnabled();
    stopButton.setEnabled(false);
    abortButton.setEnabled(false);

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
   * Enable run button if experiment has at least one host that has at least
   * one node to run.
   */

  private void setRunButtonEnabled() {
    HostComponent[] hosts = experiment.getHosts();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      if (nodes != null && nodes.length > 0) {
	runButton.setEnabled(true);
	return;
      }
    }
    runButton.setEnabled(false);
  }

  /**
   * User selected "Run" button.
   * For each host which is assigned a node,
   * start the node on that host, and create a status button and
   * tabbed pane for it.
   */

  public void runButton_actionPerformed(ActionEvent e) {
    destroyOldNodes(); // Get rid of any old stuff before creating the new
    ArrayList nodesToRun = new ArrayList();
    ArrayList hostsToUse = new ArrayList(); // hosts that nodes are on
    nameServerHostName = null;
    String firstNodeName = null;
    HostComponent[] hosts = experiment.getHosts();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
	AgentComponent[] agents = nodes[j].getAgents();
	// skip nodes that have no agents
	if (agents == null || agents.length == 0)
	  continue;
	if (firstNodeName == null)
	  firstNodeName = nodes[j].toString();
	nodesToRun.add(nodes[j]);
	String hostName = hosts[i].toString();
	if (nameServerHostName == null)
	  nameServerHostName = hostName;
	// record host for each node
	hostsToUse.add(hostName);
      }
    }
    NodeComponent[] nc =
      (NodeComponent[])nodesToRun.toArray(new NodeComponent[nodesToRun.size()]);
    ConfigurationWriter configWriter = experiment.getConfigurationWriter(nc);
    for (int i = 0; i < nc.length; i++) {
      NodeComponent nodeComponent = nc[i];
      createNode(nodeComponent, nodeComponent.toString(), 
		 (String)hostsToUse.get(i),
		 configWriter);
    }
    // select first node
    if (firstNodeName != null) {
      selectTabbedPane(firstNodeName);
      selectStatusButton(firstNodeName);
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
    //    hostTree.setEditable(!isRunning);
    //    nodeTree.setEditable(!isRunning);
    //    agentTree.setEditable(!isRunning);
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

//   public void valueChanged(TreeSelectionEvent event) {
//     TreePath path = event.getPath();
//     if (path == null) return;
//     DefaultMutableTreeNode treeNode =
//       (DefaultMutableTreeNode)path.getLastPathComponent();
//     ConsoleTreeObject cto = (ConsoleTreeObject)treeNode.getUserObject();
//     if (!cto.isNode())
//       return; // ignore selecting if it's not a node
//     String nodeName = 
//       ((ConsoleTreeObject)treeNode.getUserObject()).getName();
//     // select tabbed pane if one exists
//     selectTabbedPane(nodeName);
//     selectStatusButton(nodeName);
//   }

  /**
   * If user selects a status button, then pop the tabbed pane
   * for that node to the foreground, and select the node in the host
   * tree.
   */

  public void statusButton_actionPerformed(ActionEvent e) {
    String nodeName = ((JRadioButton)e.getSource()).getActionCommand();
    //    selectNodeInHostTree(nodeName);
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
    //    selectNodeInHostTree(nodeName);
    JFrame f = (JFrame)chartFrames.get(nodeName);
    if (f != null) {
      f.toFront();
      f.setState(Frame.NORMAL);
    }
  }

  /**
   * Select node in host tree.
   */

//   private void selectNodeInHostTree(String nodeName) {
//     DefaultTreeModel model = (DefaultTreeModel)hostTree.getModel();
//     DefaultMutableTreeNode root = 
//       (DefaultMutableTreeNode)model.getRoot();
//     TreePath path = null;
//     Enumeration nodes = root.breadthFirstEnumeration();
//     while (nodes.hasMoreElements()) {
//       DefaultMutableTreeNode node = 
// 	(DefaultMutableTreeNode)nodes.nextElement();
//       if (node.getUserObject() instanceof ConsoleTreeObject) {
// 	ConsoleTreeObject cto = (ConsoleTreeObject)node.getUserObject();
// 	if (cto.isNode()) {
// 	  if (cto.getName().equals(nodeName)) {
// 	    path = new TreePath(node.getPath());
// 	    break;
// 	  }
// 	}
//       }
//     }
//     if (path != null) {
//       hostTree.removeTreeSelectionListener(this);
//       hostTree.setSelectionPath(path);
//       hostTree.addTreeSelectionListener(this);
//     }
//   }

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
  
}

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

package org.cougaar.tools.csmart.ui.monitor.viewer;

//import att.grappa.GrappaConstants;

import org.cougaar.tools.csmart.ui.Browser;

import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.AgentComponent;

import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.ExperimentListener;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.monitor.community.ULCommunityFrame;
import org.cougaar.tools.csmart.ui.monitor.community.ULCommunityNode;
import org.cougaar.tools.csmart.ui.monitor.plan.ULPlanFilter;
import org.cougaar.tools.csmart.ui.monitor.plan.ULPlanFrame;
import org.cougaar.tools.csmart.ui.monitor.plan.ULPlanNode;
import org.cougaar.tools.csmart.ui.monitor.society.ULSocietyFrame;
import org.cougaar.tools.csmart.ui.monitor.society.ULSocietyNode;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTFrame;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTGraph;
import org.cougaar.tools.csmart.ui.monitor.generic.ExtensionFileFilter;
import org.cougaar.tools.csmart.ui.monitor.generic.UIProperties;
import org.cougaar.tools.csmart.ui.monitor.metrics.CSMARTMetrics;
import org.cougaar.tools.csmart.ui.monitor.metrics.CSMARTMetricsFrame;
//import org.cougaar.tools.csmart.ui.psp.ThreadUtils;

import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

import org.cougaar.util.PropertyTree;
import org.cougaar.util.ConfigFinder;
import org.cougaar.core.util.UID;
import org.cougaar.core.node.Bootstrapper;

import java.awt.event.*;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.File;
import java.net.URL;
import java.util.*;
import javax.swing.*;

/**
 * The user interface for the CSMART Society.
 * Displays a tool panel with buttons for invoking various graph and
 * chart views.
 */

public class CSMARTUL extends JFrame implements ActionListener, Observer {
  // names of PSPs used by this client
  //  private static final String PSP_COMMUNITY = "PSP_CommunityProvider.PSP";
  //  private static final String PSP_CLUSTER = "PSP_ClusterInfo.PSP";
  //  private static final String PSP_PLAN = "PSP_Plan.PSP";
  //  private static final String PSP_METRICS = "PSP_Metrics.PSP";

  // names of servlets used by this client
  private static final String PSP_COMMUNITY = 
    "CSMART_CommunityProviderServlet";
  private static final String PSP_CLUSTER = 
    "CSMART_ClusterInfoServlet";
  private static final String PSP_PLAN = "CSMART_PlanServlet";
  private static final String PSP_METRICS = "CSMART_MetricsServlet";

  private static final String FILE_MENU = "File";
  private static final String NEW_MENU_ITEM = "New";
  private static final String OPEN_MENU_ITEM = "Open";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String WINDOW_MENU = "Window";
  private static final String OPEN_GRAPH_MENU_ITEM = "Open Graph...";
  private static final String OPEN_METRIC_MENU_ITEM = "Open Metric...";
  private static final String SEPARATOR = "Separator";

  protected static final String HELP_MENU = "Help";
  protected static final String HELP_DOC = "help.html";
  protected static final String ABOUT_CSMART_ITEM = "About CSMART";
  protected static final String ABOUT_DOC = "/org/cougaar/tools/csmart/ui/help/about-csmart.html";
  protected static final String HELP_MENU_ITEM = "Help";

  private String[] helpMenuItems = {
    HELP_MENU_ITEM, ABOUT_CSMART_ITEM
  };

  private CSMART csmart; // top level viewer, gives access to save method, etc.
  private JMenu windowMenu;
  private static String agentURL = null; // agent to contact initially
  private static String agentHost = "localhost";
  //  private static int agentPort = 5555;
  private static int agentPort = 8800;
  private static String agentPortString = "8800";
  // maps host to port, but port used is currently hardcoded
  // TODO: how should this work?
  private static CSMARTAgentMap agentMap;
  // these mappings are determined once,
  private static Hashtable communityToAgents = null;
  private static Hashtable agentToCommunity = null;
  private static Hashtable agentToURL = null;
  private static Hashtable titleToMenuItem = new Hashtable();
  private static UIProperties properties = new UIProperties();
  private static ArrayList myWindows = new ArrayList();
  private static Experiment experiment;
  private static MyExperimentListener listener;

  // configure what views to support
  private static final String[] views = {
     NamedFrame.COMMUNITY,
     NamedFrame.AGENT,
     NamedFrame.PLAN,
     NamedFrame.THREAD,
     NamedFrame.METRICS,
  };

  private static final String[] tooltips = {
    "Display communities.",
    "Display agents and their relationships.",
    "Display plan objects.",
    "Display plan objects related to a specified plan object.",
    "Display metrics.",
  };

  private static final String[] iconFilenames = {
    "community.gif",
    "society.gif",
    "event.gif",
    "thread.gif",
    "metric.gif"
  };

  /**
   * Create and display user interface.
   */

  private void refreshAgents() {
    if (csmart != null) {
      if (experiment == csmart.getRunningExperiment()) {
	// have the experiment already.
	// could things have changed though?
	return;
      }
      if (listener != null && experiment != null)
	experiment.removeExperimentListener(listener);
      // If you dont clear these out, then monitoring
      // two experiments from the same CSMART run
      // means the second run cant view the society
      if (agentURL != null) {
	agentURL = null; // agent to contact initially
	CSMARTAgentMap agentMap = null;
	communityToAgents = null;
	agentToCommunity = null;
	agentToURL = null;
      }
      experiment = csmart.getRunningExperiment();
      setHostToMonitor(experiment);
      if (listener == null)
	listener = new MyExperimentListener(this);
      experiment.addExperimentListener(listener);
      return;
    }
    // Otherwise, were running monitor standalone.
    // We still need to be able to refresh things, if there is
    // a new society running. How? FIXME!!
  }
  
  public CSMARTUL(CSMART csmart) {
    this.csmart = csmart;
    refreshAgents();
//      if (csmart != null) {
//        experiment = csmart.getRunningExperiment();
//        setHostToMonitor(experiment);
//        listener = new MyExperimentListener(this);
//        experiment.addExperimentListener(listener);
//      }

    // create one version of properties for all objects
    // and set them in CSMARTGraph because it can't address CSMARTUL
    // TODO: better handling of properties
    CSMARTGraph.setProperties(properties);
    JMenuBar menuBar = new JMenuBar();
    getRootPane().setJMenuBar(menuBar); 
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.setToolTipText("Create new views, open saved views, or exit.");
    JMenu newMenu = new JMenu(NEW_MENU_ITEM);
    newMenu.setToolTipText("Create new views.");
    fileMenu.add(newMenu);

    JMenu openMenu = new JMenu(OPEN_MENU_ITEM);
    openMenu.setToolTipText("Open a saved view.");
    JMenuItem openGraphMenuItem = new JMenuItem(OPEN_GRAPH_MENU_ITEM);
    openGraphMenuItem.setToolTipText("Open a saved graph view.");
    openGraphMenuItem.addActionListener(this);
    openMenu.add(openGraphMenuItem);
    JMenuItem openMetricMenuItem = new JMenuItem(OPEN_METRIC_MENU_ITEM);
    openMetricMenuItem.setToolTipText("Open a saved metrics view.");
    openMetricMenuItem.addActionListener(this);
    //openMetricMenuItem.setEnabled(false);
    openMenu.add(openMetricMenuItem);
    fileMenu.add(openMenu);

    JMenuItem exitMenuItem = new JMenuItem(EXIT_MENU_ITEM);
    exitMenuItem.setToolTipText("Exit this tool.");
    exitMenuItem.addActionListener(this);
    fileMenu.add(exitMenuItem);

    windowMenu = new JMenu(WINDOW_MENU);
    windowMenu.setToolTipText("Display selected window.");


    menuBar.add(fileMenu);
    
    // if running stand alone, then add a windows menu
    // to keep track of windows created by this tool
    // TODO: replicated NamedWindow capability at this level, but under csmart
    if (csmart == null) {
      menuBar.add(windowMenu);
      setTitle("Society Monitor");
    }

    // Add a Help menu item
    JMenu helpmenu = new JMenu(HELP_MENU);
    for (int i = 0; i < helpMenuItems.length; i++) {
      JMenuItem mItem = new JMenuItem(helpMenuItems[i]);
      mItem.addActionListener(this);
      helpmenu.add(mItem);
    }
    menuBar.add(helpmenu);

    JToolBar toolBar = new JToolBar();
    toolBar.setLayout(new GridLayout(1, 5, 2, 2));
    getContentPane().add("North", toolBar);

    for (int i = 0; i < views.length; i++) {
      JButton button = makeButton(views[i], iconFilenames[i]);
      button.setToolTipText(tooltips[i]);
      button.setHorizontalTextPosition(JButton.CENTER);
      button.setVerticalTextPosition(JButton.BOTTOM);
      button.addActionListener(this);
      toolBar.add(button);
      JMenuItem menuItem = new JMenuItem(views[i]);
      menuItem.addActionListener(this);
      newMenu.add(menuItem);
    }

    if (csmart == null) {
      addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
	  System.exit(0);
	}
      });
      // if running standalone, then manage the windows created by this tool
      NamedFrame.getNamedFrame().addObserver(this);
    } else {
      // if running under csmart, close windows we created when we're closed
      addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
	  closeSubWindows();
	  experiment.removeExperimentListener(listener);
	}
      });
    }

    pack();
    setVisible(true);
  }

  /**
   * Sets monitor host to first host that has a node and agent
   * in the running experiment; defaults to localhost.
   */

  private void setHostToMonitor(Experiment experiment) {
    HostComponent[] hosts = experiment.getHosts();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      if (nodes == null || nodes.length == 0)
        continue;
      for (int j = 0; j < nodes.length; j++) {
	AgentComponent[] agents = nodes[j].getAgents();
	// skip nodes that have no agents
	if (agents == null || agents.length == 0)
	  continue;
        // found a host to use
        agentHost = hosts[i].getShortName();
        break;
      }
    }
    agentURL = "http://" + agentHost + ":" + agentPortString + "/"; 
    agentMap = new CSMARTAgentMap(agentHost, agentPort);
  }

  private JButton makeButton(String label, String iconFilename) {
    URL iconURL = getClass().getResource(iconFilename);
    if (iconURL == null)
      return new JButton(label);
    ImageIcon icon = new ImageIcon(iconURL);
    return new JButton(label, icon);
  }

  /**
   * The Observer interface.
   * Notified by NamedFrame addFrame method.
   * Argument is vector of frame title (String) and the frame (JFrame).
   * Add title to window menu, and when user selects title,
   * pop frame to front.
   * Used only if this tool is run standalone (outside of CSMART).
   * @see NamedFrame#addFrame
   */

  public void update(Observable o, Object arg) {
    if (o instanceof NamedFrame) {
      NamedFrame namedFrame = (NamedFrame) o;
      NamedFrame.Event event = (NamedFrame.Event) arg;
      if (event.eventType == NamedFrame.Event.ADDED) {
        JMenuItem menuItem = new JMenuItem(event.title);
        titleToMenuItem.put(event.title, menuItem);
        menuItem.addActionListener(this);
        windowMenu.add(menuItem);
      } else if (event.eventType == NamedFrame.Event.REMOVED) {
        JMenuItem menuItem = (JMenuItem) titleToMenuItem.get(event.title);
        if (menuItem == null) {
          System.err.println("CSMARTUL: No window menu item for " + event.title);
        } else {
          windowMenu.remove(menuItem);
          titleToMenuItem.remove(event.title);
        }
      } else if (event.eventType == NamedFrame.Event.CHANGED) {
	JMenuItem menuItem = (JMenuItem)titleToMenuItem.get(event.prevTitle);
        if (menuItem == null) {
          System.err.println("CSMARTUL: No window menu item for " + event.title);
        } else {
          windowMenu.remove(menuItem);
          titleToMenuItem.remove(event.prevTitle);
	  JMenuItem newMenuItem = new JMenuItem(event.title);
	  titleToMenuItem.put(event.title, newMenuItem);
	  newMenuItem.addActionListener(this);
	  windowMenu.add(newMenuItem);
	}
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    String s = ((AbstractButton)e.getSource()).getActionCommand();
    if (s.equals(NamedFrame.COMMUNITY)) {
      makeCommunityGraph();
    } else if (s.equals(NamedFrame.AGENT)) {
      makeSocietyGraph(); // only the name changes!
    } else if (s.equals(NamedFrame.PLAN)) {
      makePlanGraph();
    } else if (s.equals(NamedFrame.THREAD)) {
      makeThreadGraph();
    } else if (s.equals(NamedFrame.METRICS)) {
      makeMetricsGraph();
    } else if (s.equals(OPEN_GRAPH_MENU_ITEM)) {
      openGraph();
    } else if (s.equals(OPEN_METRIC_MENU_ITEM)) {
      openMetrics();
    } else if (s.equals(EXIT_MENU_ITEM)) {
      if (csmart == null)
	System.exit(0); // if running standalone, exit
      else {
	closeSubWindows();
	experiment.removeExperimentListener(listener);
	NamedFrame.getNamedFrame().removeFrame(this);
	dispose();
      }
    } else if (s.equals(HELP_MENU_ITEM)) {
      URL help = (URL)getClass().getResource(HELP_DOC);
      if (help != null)
	Browser.setPage(help);
    } else if (s.equals(ABOUT_CSMART_ITEM)) {
      URL about = (URL)getClass().getResource(ABOUT_DOC);
      if (about != null)
	Browser.setPage(about);
    } else {
      JFrame f = NamedFrame.getNamedFrame().getFrame(s);
      if (f != null) {
	f.toFront();
	f.setState(Frame.NORMAL);
      }  
    }
  }

  /**
   * Close windows created by this tool by calling their window listeners;
   * those window listeners close their associated frames
   * such as attribute tables and overview windows.
   */

  private void closeSubWindows() {
    for (int i = 0; i < myWindows.size(); i++) {
      Window w = (Window)myWindows.get(i);
      WindowListener[] wls = 
	(WindowListener[])(w.getListeners(WindowListener.class));
      for (int j = 0; j < wls.length; j++) 
	wls[j].windowClosing(new WindowEvent(w, WindowEvent.WINDOW_CLOSED));
    }
    myWindows.clear();
  }

  private void openMetrics() {
    File file = readFile("mtr", "Metrics files");
    if (file != null) {
      Window w = 
	(Window)new CSMARTMetricsFrame(NamedFrame.METRICS, file);
      myWindows.add(w);      
    }
  }

  private void openGraph() {
    File file = readFile("dot", "graph files");
    if (file != null) {
      CSMARTGraph graph = readGraphFromFile(file);
      if (graph == null)
	return;
      String fileName = file.getName();
      String graphType = 
	(String)graph.getAttributeValue(CSMARTGraph.GRAPH_TYPE);
      if (graphType == null)
	return;
      if (graphType.equals(CSMARTGraph.GRAPH_TYPE_COMMUNITY))
	new ULCommunityFrame(NamedFrame.COMMUNITY + ": <" + fileName + ">", graph);
      else if (graphType.equals(CSMARTGraph.GRAPH_TYPE_SOCIETY))
	new ULSocietyFrame(NamedFrame.AGENT + ": <" + fileName + ">", graph);
      else if (graphType.equals(CSMARTGraph.GRAPH_TYPE_THREAD))
	new ULPlanFrame(NamedFrame.THREAD + ": <" + fileName + ">", graph, null);
      else if (graphType.equals(CSMARTGraph.GRAPH_TYPE_PLAN))
	new ULPlanFrame(NamedFrame.PLAN + ": <" + fileName + ">", graph,
			new ULPlanFilter(graph));
      else
	JOptionPane.showMessageDialog(this, "Unknown graph type: " + graphType);
    }
  }

  private File readFile(String extension, String description) {
    String[] filters = { extension };
    ExtensionFileFilter filter =
      new ExtensionFileFilter(filters, description);
    JFileChooser jfc = 
      new JFileChooser(System.getProperty("org.cougaar.install.path"));
    jfc.addChoosableFileFilter(filter);
    if (jfc.showOpenDialog(this) == JFileChooser.CANCEL_OPTION)
      return null;
    File file = jfc.getSelectedFile();
    if (file == null)
      return null;
    String ext = filter.getExtension(file);
    if (ext == null || !ext.equals(extension)) {
      JOptionPane.showMessageDialog(this,
				    "File must have extension: " +
				    extension + " " + file.getName());
      return null;
    }
    return file;
  }

  private CSMARTGraph readGraphFromFile(File file) {
    CSMARTGraph graph = CSMARTGraph.createGraphFromDotFile(file);
    if (graph == null)
      JOptionPane.showMessageDialog(this,
				    "Cannot read graph from file: " +
				    file.getName());
    return graph;
  }


  /**
   * Query user for agent URL. Only used if not running under CSMART.
   * If running under CSMART, CSMART picks the first host that has
   * nodes and agents and builds a URL from that and the default agentPort.
   */

  private static void getAgentURL() {
    if (agentURL != null)
      return; // only ask user for agent locations once
    JTextField tf = new JTextField("http://localhost:" +
                                   agentPortString + "/");
    JPanel panel = new JPanel();
    panel.add(new JLabel("Agent URL:"));
    panel.add(tf);
    int result = 
      JOptionPane.showOptionDialog(null, panel, "Agent URL",
				   JOptionPane.OK_CANCEL_OPTION,
				   JOptionPane.PLAIN_MESSAGE,
				   null, null, null);
    if (result != JOptionPane.OK_OPTION)
      return;
    agentURL = tf.getText();
    int startIndex;
    int endIndex;
    if (agentURL.startsWith("http://")) {
      startIndex = 7;
      endIndex = agentURL.indexOf(":", startIndex);
      if (endIndex != -1)
	agentHost = agentURL.substring(startIndex, endIndex);
    }
    startIndex = agentURL.lastIndexOf(':');
    if (startIndex != -1) {
      endIndex = agentURL.lastIndexOf('/');
      if (endIndex != -1) {
	String s = agentURL.substring(startIndex+1, endIndex);
	try {
	  agentPort = Integer.parseInt(s);
	} catch (Exception e) {
	  System.out.println("CSMARTUL: " + e);
	}
      }
    }
    agentMap = new CSMARTAgentMap(agentHost, agentPort);
  }

  /**
   * Query user for agent URL and get names of agents from that URL.
   * The user is only queried once, unless we fail to contact the agent.
   */

  private static Vector getAgents() {
    getAgentURL();
    if (agentURL == null)
      return null;
    Vector agents = Util.getClusters(agentURL);
    // if failed to contact agents, reset agentURL to null, 
    // so user is queried again
    if (agents == null) 
      agentURL = null; 
    return agents;
  }


  /**
   * Query user for agent URL and get objects from the
   * specified PSP.
   */

  public static Collection getObjectsFromPSP(String PSPName) {
    Vector agents = getAgents();
    if (agents == null)
      return null;
    //    System.out.println("CSMARTUL: getting objects from: " + PSPName);
    Collection objectsFromPSP = 
      Util.getCollectionFromClusters(agents, PSPName, null);
    return objectsFromPSP;
  }

  /**
   * Query PSP_CommunityProvider.PSP to get community <-> agent mapping.
   */

  private static void getCommunities() {
    if (communityToAgents != null)
      return; // only do this once
    // get agent and community names
    Collection objectsFromPSP = getObjectsFromPSP(PSP_COMMUNITY);
    if (objectsFromPSP == null) 
      return;
    if (objectsFromPSP.size() == 0)
      JOptionPane.showMessageDialog(null,
 				    "No information received from agents.");
    processOrganizationAssets(objectsFromPSP);
  }

  /**
   * Create community<->agents mapping.
   */

  private static void processOrganizationAssets(Collection objectsFromPSP) {
    communityToAgents = new Hashtable();
    agentToCommunity = new Hashtable();
    agentToURL = new Hashtable();
    for (Iterator i = objectsFromPSP.iterator(); i.hasNext(); ) {
      Object o = i.next();
      if (!(o instanceof PropertyTree)) {
	System.out.println("CSMARTUL: expected PropertyTree, got: " + 
			   o.getClass());
	continue;
      }
      PropertyTree properties = (PropertyTree)o;
      String agentName = 
	(String)properties.get(PropertyNames.AGENT_NAME);
      String communityName = 
	(String)properties.get(PropertyNames.AGENT_COMMUNITY_NAME);
      agentToCommunity.put(agentName, communityName);
      Vector agents = (Vector)communityToAgents.get(communityName);
      if (agents == null) {
	agents = new Vector();
	agents.addElement(agentName);
        //        System.out.println("Adding agent name: " + agentName);
	communityToAgents.put(communityName, agents);
      } else
	agents.addElement(agentName);
      agentToURL.put(agentName, properties.get(PropertyNames.AGENT_URL));
    }
  }

  /**
   * Make community graph.
   */

  private void makeCommunityGraph() {
    // get agent and community names
    Collection objectsFromPSP = getObjectsFromPSP(PSP_COMMUNITY);
    if (objectsFromPSP == null) 
      return;
    int n = objectsFromPSP.size();
    if (n == 0)
      JOptionPane.showMessageDialog(null,
 				    "No information received from agents.");
    // set up agent<->community mappings in hashtables
    processOrganizationAssets(objectsFromPSP);
    Vector nodeObjects = new Vector(n);
    Hashtable nameToNodeObject = new Hashtable(n);
    for (Iterator i = objectsFromPSP.iterator(); i.hasNext(); ) {
      PropertyTree properties = (PropertyTree)i.next();
      String communityName = 
	(String)properties.get(PropertyNames.AGENT_COMMUNITY_NAME);
      if (nameToNodeObject.get(communityName) == null) { // filter duplicates
	nameToNodeObject.put(communityName, new ULCommunityNode(properties));
	nodeObjects.add(new ULCommunityNode(properties));
      }
    }
    // add member names to community nodes
    Enumeration communityNames = communityToAgents.keys();
    while (communityNames.hasMoreElements()) {
      String name = (String)communityNames.nextElement();
      ULCommunityNode node = (ULCommunityNode)nameToNodeObject.get(name);
      node.addMembers((Vector)communityToAgents.get(name));
    }
    if (nodeObjects.size() != 0) {
      Window w = 
	(Window)new ULCommunityFrame(NamedFrame.COMMUNITY,
				     new CSMARTGraph(nodeObjects,
						     CSMARTGraph.GRAPH_TYPE_COMMUNITY));
      myWindows.add(w);
    }
  }

  /**
   * Display a plan composed of tasks, plan elements, workflows and assets.
   */

  private void makePlanGraph() {
    // get community<->agent mapping
    getCommunities(); 

    if (communityToAgents == null || communityToAgents.size() == 0)
      return; // nothing to graph

    // get filter
    ULPlanFilter filter = new ULPlanFilter(communityToAgents);
    if (!filter.preFilter())
      return; // user cancelled filter

    // get the list of agents
    Vector agentsToContact = filter.getAgentsSelected();
    if (agentsToContact == null || agentsToContact.size() == 0)
      return;

    Vector agentURLs = new Vector();
    for (int i = 0; i < agentsToContact.size(); i++) {
      String URL = 
  	(String)agentToURL.get((String)agentsToContact.elementAt(i));
      if (URL != null)
  	agentURLs.add(URL);
    }

    String PSPId = PSP_PLAN;
    String filterValue = filter.getIgnoreObjectTypes();
    int filterLimit  = filter.getNumberOfObjects();
    String baseURL =
        PSPId + "?" +
        PropertyNames.PLAN_OBJECTS_TO_IGNORE +
        "=" +
        filterValue;

    // query
    Collection objectsFromPSP =
      Util.getCollectionFromClusters(agentURLs, 
				     baseURL,
				     null,
				     filterLimit);
    int nObjs = 
      ((objectsFromPSP != null) ? 
       objectsFromPSP.size() : 
       (0));
    //    System.out.println("Received plan objects: "+nObjs);

    if (nObjs <= 0) {
      // no response?
      return;
    }

    if (nObjs > filterLimit) {
      // exceeded limit by one.
      //
      // Util.getCol.. did the popup warning
    }

    // a Vector of ULPlanNodes
    Vector nodeObjects = new Vector();

    int nDuplicates = 0;
    Set UIDs = new HashSet();
    Iterator iter = objectsFromPSP.iterator();
    for (int i = 0; i < nObjs; i++) {
      PropertyTree properties = (PropertyTree)iter.next();
      // get the UID
      String UID = (String)properties.get(PropertyNames.UID_ATTR);
      // filter duplicates by UID
      if (!(UIDs.contains(UID))) {
	String agentName = (String)properties.get(PropertyNames.AGENT_ATTR);
	String communityName =
	  ((agentName != null) ?
	   ((String)agentToCommunity.get(agentName)) :
           "");
	nodeObjects.add(new ULPlanNode(properties, communityName));
	UIDs.add(UID);
      } else {
	nDuplicates++;
      }
      // for debugging
      //      Set keys = properties.keySet();
      //      System.out.println("Property names/values........");
      //      for (Iterator j = keys.iterator(); j.hasNext(); ) {
      //	String s = (String)j.next();
      //	System.out.println(s + "," + properties.get(s));
      //      }
      // end for debugging

    }
    // only create popup if objects were found
    if (nodeObjects.size() != 0) {
      Window w = 
	(Window)new ULPlanFrame(NamedFrame.PLAN, 
				new CSMARTGraph(nodeObjects, 
						CSMARTGraph.GRAPH_TYPE_PLAN),
				filter);
      myWindows.add(w);
    }
  }

  /**
   * Called to make a thread graph directly from the launcher window.
   */

  private void makeThreadGraph() {
    // will gather these parameters:
    String UID;
    String agentName;
    int limit;
    boolean isDown;

    // FIXME: should have a single popup:
    //    UID=<default to empty>
    //    AgentName=<default to empty>
    //    Limit=<default to 200, add "no-limit" checkbox + grey-out>
    //    Trace=<option box of "children"/"parents", default to children> 

    // awkward popups for now:
    UID = JOptionPane.showInputDialog("Plan Object UID: "); 
    if (UID == null) {
      return;
    }
    agentName = JOptionPane.showInputDialog("Agent name: ");
    if (agentName == null) {
      return;
    }
    String sLimit = JOptionPane.showInputDialog("Limit (e.g. 200): ");
    if (sLimit == null) {
      return;
    }
    try {
      limit = Integer.parseInt(sLimit);
    } catch (NumberFormatException nfe) {
      System.err.println("Illegal number: "+sLimit);
      return;
    }
    String sIsDown = JOptionPane.showInputDialog("TraceChildren (true/false): ");
    if (sIsDown == null) {
      return;
    }
    isDown = (!("false".equalsIgnoreCase(sIsDown)));

    // create the graph
    makeThreadGraph(UID, agentName, isDown, limit);
  }

  /**
   * Make a thread graph using the plan object threads psp.
   * Called to make a thread graph when the user has selected
   * an object in the plan graph.
   */

  public static void makeThreadGraph(String UID, String agentName,
				     boolean isDown, int limit) {
    getCommunities();
    List objectsFromPSP = ThreadUtils.getFullThread(agentMap, isDown, limit, 
						    agentName, UID);
    if (objectsFromPSP == null || objectsFromPSP.size() == 0) {
      JOptionPane.showMessageDialog(null,
	    "Cannot obtain thread information for specified plan object.");
      return;
    }
    if ((limit >= 0) &&
        (objectsFromPSP.size() > limit)) {
      JOptionPane.showMessageDialog(null,
	    "Exceeded limit of "+limit+" objects; creating a trimmed graph.");
    }
    Vector nodeObjects = new Vector(objectsFromPSP.size());
    Vector assetPropertyTrees = new Vector();
    for (Iterator i = objectsFromPSP.iterator(); i.hasNext(); ) {
      PropertyTree properties = (PropertyTree)i.next();
      // pick off the assets and process after we have them all
      String objectType = (String)properties.get(PropertyNames.OBJECT_TYPE);
      if ((objectType != null) &&
	  (objectType.equals(PropertyNames.ASSET_OBJECT))) {
	assetPropertyTrees.add(properties);
	continue;
      }
      String agent = (String)properties.get(PropertyNames.AGENT_ATTR);
      String community = "";
      if (agent != null)
	community = (String)agentToCommunity.get(agent);
      nodeObjects.add(new ULPlanNode(properties, community));
    }
    // now pick up the correct assets
    Collection c = getAssets(assetPropertyTrees);
    for (Iterator i = c.iterator(); i.hasNext(); ) {
      PropertyTree properties = (PropertyTree)i.next();
      String agent = (String)properties.get(PropertyNames.AGENT_ATTR);
      String community = "";
      if (agent != null)
	community = (String)agentToCommunity.get(agent);
      nodeObjects.add(new ULPlanNode(properties, community));
    }
    if (nodeObjects.size() != 0) {
      Window w = 
	(Window)new ULPlanFrame(NamedFrame.PLAN, 
				new CSMARTGraph(nodeObjects, 
						CSMARTGraph.GRAPH_TYPE_PLAN),
				new ULPlanFilter(communityToAgents));
      myWindows.add(w);
    }
  }

  /**
   * Ignore asset property trees for agents that are not from
   * their own agent, iff we have an asset property tree from its own agent.
   * Helper for makeThreadGraph.
   */

  private static Collection getAssets(Vector assetPTs) {
    Hashtable assets = new Hashtable();
    for (int i = 0; i < assetPTs.size(); i++) {
      PropertyTree pt = (PropertyTree)assetPTs.get(i);
      String assetKeyName = (String)pt.get(PropertyNames.ASSET_KEY);
      // if we have no info on this asset, then save this info
      if (!assets.containsKey(assetKeyName)) {
	assets.put(assetKeyName, pt);
      } else {
	// if this info is from the asset's agent, then save it instead
	String assetClusterName = (String)pt.get(PropertyNames.ASSET_CLUSTER);
	String assetAgentName = (String)pt.get(PropertyNames.AGENT_ATTR);
	if (assetAgentName.equals(assetClusterName)) {
	  assets.put(assetKeyName, pt);
	}
      }
    }
    return assets.values();
  }

  /**
   * Make a graph of the society.
   */

  private void makeSocietyGraph() {
    Collection objectsFromPSP = getObjectsFromPSP(PSP_CLUSTER);
    if (objectsFromPSP == null)
      return;
    Vector nodeObjects = new Vector(objectsFromPSP.size());
    //    System.out.println("Received plan objects: " + objectsFromPSP.size());
    Hashtable nameToUID = new Hashtable();
    for (Iterator i = objectsFromPSP.iterator(); i.hasNext(); ) {
      Object o = i.next();
      if (!(o instanceof PropertyTree)) {
	System.out.println("CSMARTUL: expected PropertyTree, got: " + 
			   o.getClass());
	continue;
      }
      PropertyTree properties = (PropertyTree)o;
      ULSocietyNode node = new ULSocietyNode(properties);
      nodeObjects.add(node);
      String name = (String)properties.get(PropertyNames.ORGANIZATION_KEY_NAME);
      String UID = node.getUID();
      nameToUID.put(name, UID);
    }
    // organizations refer to each other by name, 
    // hence after creating nodes for all organizations, 
    // we create links from the name-to-UID mapping
    for (int i = 0; i < nodeObjects.size(); i++) {
      ULSocietyNode node = (ULSocietyNode)nodeObjects.elementAt(i);
      PropertyTree properties = node.getProperties();
      Set keys = properties.keySet();
      for (Iterator keyIter = keys.iterator(); keyIter.hasNext(); ) {
	String key = (String)keyIter.next();
	if (key.startsWith(PropertyNames.ORGANIZATION_RELATED_TO)) {
          String relationship = key.substring(key.lastIndexOf('_')+1);
	  String relatedToName = (String)properties.get(key);
	  String relatedToUID = (String)nameToUID.get(relatedToName);
          // this creates duplicate links but grappa catches them; see csmartgraph
	  if (relatedToUID != null) {
            if (relationship.endsWith("Customer") || 
                relationship.endsWith("Superior"))
              node.addIncomingLink(relatedToUID);
            else if (!relationship.endsWith("Provider") &&
                     !relationship.endsWith("Subordinate"))
              // do nothing, assume that all relationships are dual
              // so that graphing "one side" of the relationship is ok
              //              node.addOutgoingLink(relatedToUID);
              //            else
              System.out.println("Unknown relationship: " + relatedToName);
          }
        }
      }
    }
    if (nodeObjects.size() != 0) {
      Window w =
	(Window)new ULSocietyFrame(NamedFrame.AGENT,
            new CSMARTGraph(nodeObjects, CSMARTGraph.GRAPH_TYPE_SOCIETY));
      myWindows.add(w);
    }
  }

  public void makeMetricsGraph() {
    Collection objectsFromPSP = getObjectsFromPSP(PSP_METRICS);
    if (objectsFromPSP == null)
      return;
    System.out.println("Received metrics: " + objectsFromPSP.size());

    ArrayList names = new ArrayList();
    ArrayList data = new ArrayList();

    Iterator iter = objectsFromPSP.iterator();
    while(iter.hasNext()) {
      Object obj = iter.next();
      if(obj instanceof String) {
	names.add(obj);
      }	else if (obj instanceof Integer[]) { 
	data.add(obj);
      }
    }
      Window w = 
	(Window)new CSMARTMetricsFrame(NamedFrame.METRICS, names, data);
      myWindows.add(w);
  }

  /**
   * Start up the CSMART Society Monitor stand-alone.<br>
   * If <code>org.cougaar.useBootstrapper</code> is set false, use CLASSPATH to find classes as normal.<br>
   * Otherwise, use the Cougaar Bootstrapper to search the Classpath + CIP/lib, /plugins, /sys, etc.
   **/
  public static void main(String[] args) {
    // Note that because of the static ref to UIProperties, xerces must be in the CLASSPATH proper
    // and you can't rely on the Bootstrapper
    if ("true".equals(System.getProperty("org.cougaar.useBootstrapper", "true"))) {
      //System.err.println("Using Bootstrapper");
      Bootstrapper.launch(CSMARTUL.class.getName(), args);
    } else {
      launch(args);
    }
  }

  public static void launch(String[] args) {    
    new CSMARTUL(null);
  }

  class MyExperimentListener implements ExperimentListener {
    CSMARTUL monitor;

    MyExperimentListener(CSMARTUL monitor) {
      this.monitor = monitor;
    }

    public boolean isMonitoring() {
      // return true if any monitoring processes are running
      //      return (monitor.myWindows.length != 0);
      // there's no good way to re-init the monitor now
      // so always say it's running, so it's killed and re-initted
      // with the next experiment
      // TODO: only kill off windows that are monitoring the experiment?
      return true;
    }

    public void experimentTerminated() {
      monitor.closeSubWindows();
      experiment.removeExperimentListener(this);
      NamedFrame.getNamedFrame().removeFrame(monitor);
      monitor.dispose();
    }
  }

}

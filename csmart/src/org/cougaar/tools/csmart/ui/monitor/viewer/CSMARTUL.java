/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

import org.cougaar.bootstrap.Bootstrapper;
import org.cougaar.core.util.UID;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.monitor.community.ULCommunityFrame;
import org.cougaar.tools.csmart.ui.monitor.community.ULCommunityNode;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTGraph;
import org.cougaar.tools.csmart.ui.monitor.generic.ExtensionFileFilter;
import org.cougaar.tools.csmart.ui.monitor.generic.UIProperties;
import org.cougaar.tools.csmart.ui.monitor.metrics.CSMARTMetricsFrame;
import org.cougaar.tools.csmart.ui.monitor.plan.ULPlanFilter;
import org.cougaar.tools.csmart.ui.monitor.plan.ULPlanFrame;
import org.cougaar.tools.csmart.ui.monitor.plan.ULPlanNode;
import org.cougaar.tools.csmart.ui.monitor.society.ULSocietyFrame;
import org.cougaar.tools.csmart.ui.monitor.society.ULSocietyNode;
import org.cougaar.tools.csmart.ui.monitor.xml.XMLFrame;
//import org.cougaar.tools.csmart.ui.monitor.topology.TopologyFrame;
import org.cougaar.tools.csmart.ui.util.ClientServletUtil;
import org.cougaar.tools.csmart.ui.util.NamedFrame;
import org.cougaar.tools.csmart.ui.util.ServletResponse;
import org.cougaar.tools.csmart.ui.util.ServletResult;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.PropertyTree;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * The user interface for the CSMART Society.
 * Displays a tool panel with buttons for invoking various graph and
 * chart views.
 * @property org.cougaar.install.path Used to find saved graphs to read
 * @property org.cougaar.useBootstrapper See the Bootstrapper for usage
 */
public class CSMARTUL extends JFrame implements ActionListener, Observer {
  private static final String FILE_MENU = "File";
  private static final String NEW_MENU_ITEM = "New";
  private static final String OPEN_MENU_ITEM = "Open";
  private static final String MONITOR_MENU_ITEM = "Monitor...";
  private static final String EXIT_MENU_ITEM = "Exit";
  private static final String WINDOW_MENU = "Window";
  private static final String OPEN_GRAPH_MENU_ITEM = "Open Graph...";
  private static final String OPEN_XML_MENU_ITEM = "Open XML File...";
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

  private static CSMART csmart; // top level viewer
  private JMenu windowMenu;
  private static String agentURL = null; // agent to contact initially
  private static String agentProtocol = "http";
  private static String agentHost = "localhost";
  // default agent port
  public static int agentPort = 8800;
  // node component argument that specifies alternate port
  public static String AGENT_HTTP_PORT = "org.cougaar.lib.web.http.port";
  public static String AGENT_HTTPS_PORT = "org.cougaar.lib.web.https.port";

  // maps host to port
  private static CSMARTAgentMap agentMap;
  // these mappings are determined once per experiment
  private static Hashtable communityToAgents = null;
  private static Hashtable agentToCommunity = null;
  private static Hashtable agentToURL = null;
  private static Hashtable titleToMenuItem = new Hashtable();
  private static UIProperties properties = new UIProperties();
  private static ArrayList myWindows = new ArrayList();
  private static Experiment experiment;

  private transient Logger log;

  // configure what views to support
  private static final String[] views = {
     NamedFrame.COMMUNITY,
     NamedFrame.AGENT,
     NamedFrame.PLAN,
     NamedFrame.THREAD,
     NamedFrame.METRICS,
     //     NamedFrame.TOPOLOGY,
  };

  private static final String[] tooltips = {
    "Display communities.",
    "Display agents and their relationships.",
    "Display plan objects.",
    "Display plan objects related to a specified plan object.",
    "Display metrics.",
    //    "Display topology."
  };

  // TODO: need new gif for topology
  private static final String[] iconFilenames = {
    "community.gif",
    "society.gif",
    "event.gif",
    "thread.gif",
    "metric.gif",
    //    "community.gif"
  };

  /**
   * Start the society monitor.  If there is a single running
   * experiment in CSMART, then monitor it; otherwise, ask the 
   * user which host and port to contact to monitor an experiment.
   */ 

  public CSMARTUL(CSMART csmart, Experiment experimentToMonitor) {
    this.csmart = csmart;
    experiment = experimentToMonitor;
    createLogger();

    // find host and port to contact
    if (experiment != null)
      setHostToMonitor();
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
    JMenuItem openXMLMenuItem = new JMenuItem(OPEN_XML_MENU_ITEM);
    openXMLMenuItem.setToolTipText("Open an xml file.");
    openXMLMenuItem.addActionListener(this);
    openMenu.add(openXMLMenuItem);
    JMenuItem openMetricMenuItem = new JMenuItem(OPEN_METRIC_MENU_ITEM);
    openMetricMenuItem.setToolTipText("Open a saved metrics view.");
    openMetricMenuItem.addActionListener(this);
    //openMetricMenuItem.setEnabled(false);
    openMenu.add(openMetricMenuItem);
    fileMenu.add(openMenu);

    JMenuItem monitorMenuItem = new JMenuItem(MONITOR_MENU_ITEM);
    monitorMenuItem.setToolTipText("Monitor an experiment or other URL.");
    monitorMenuItem.addActionListener(this);
    fileMenu.add(monitorMenuItem);

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
          }
        });
    }

    pack();
    setVisible(true);
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Find a URL from the current running experiment.
   * From the list of hosts which have nodes which have agents,
   * finds a node on which org.cougaar.lib.web.http.port is defined
   * and uses the corresponding host, OR
   * finds a node which has agents, and uses the corresponding host
   * and the default port.
   */
  private static void setHostToMonitor() {
    Logger log = CSMART.createLogger(CSMARTUL.class.getName());
    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      // skip hosts that have no nodes
      if (nodes == null || nodes.length == 0)
        continue;
      for (int j = 0; j < nodes.length; j++) {
 	AgentComponent[] agents = nodes[j].getAgents();
 	// skip nodes that have no agents
 	if (agents == null || agents.length == 0)
 	  continue;
        // potential hosts and nodes
        agentHost = hosts[i].getShortName();
        Properties arguments = nodes[j].getArguments();
        if (arguments == null)
          continue;
        String port = arguments.getProperty(AGENT_HTTP_PORT);
        if (port != null) {
          try {
            agentPort = Integer.parseInt(port);
            break; // have host and specific port
          } catch (Exception e) {
            if (log.isErrorEnabled())
              log.error("Exception parsing " + AGENT_HTTP_PORT + " : ", e);
          }
        }
      }
    }
    // FIXME: Use secure mode here if necessary! Perhaps the experiment / host knows?
    agentURL = ClientServletUtil.makeURL(agentHost, agentPort);
    reset(agentHost, agentPort);
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
      //      NamedFrame namedFrame = (NamedFrame) o;
      NamedFrame.Event event = (NamedFrame.Event) arg;
      if (event.eventType == NamedFrame.Event.ADDED) {
        JMenuItem menuItem = new JMenuItem(event.title);
        titleToMenuItem.put(event.title, menuItem);
        menuItem.addActionListener(this);
        windowMenu.add(menuItem);
      } else if (event.eventType == NamedFrame.Event.REMOVED) {
        JMenuItem menuItem = (JMenuItem) titleToMenuItem.get(event.title);
        if (menuItem == null) {
          if(log.isErrorEnabled()) {
            log.error("CSMARTUL: No window menu item for " + event.title);
          }
        } else {
          windowMenu.remove(menuItem);
          titleToMenuItem.remove(event.title);
        }
      } else if (event.eventType == NamedFrame.Event.CHANGED) {
	JMenuItem menuItem = (JMenuItem)titleToMenuItem.get(event.prevTitle);
        if (menuItem == null) {
          if(log.isErrorEnabled()) {
            log.error("CSMARTUL: No window menu item for " + event.title);
          }
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
//     } else if (s.equals(NamedFrame.TOPOLOGY)) {
//       makeTopologyGraph();
    } else if (s.equals(OPEN_GRAPH_MENU_ITEM)) {
      openGraph();
    } else if (s.equals(OPEN_XML_MENU_ITEM)) {
      openXMLFile();
    } else if (s.equals(OPEN_METRIC_MENU_ITEM)) {
      openMetrics();
    } else if (s.equals(MONITOR_MENU_ITEM)) {
      getAgentURL();
    } else if (s.equals(EXIT_MENU_ITEM)) {
      if (csmart == null)
	System.exit(0); // if running standalone, exit
      else {
	closeSubWindows();
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
    if (file != null && file.canRead()) {
      Window w = 
	(Window)new CSMARTMetricsFrame(NamedFrame.METRICS, file);
      myWindows.add(w);      
    } else {
      if (log.isInfoEnabled())
	log.info("openMetrics: no (readable) file selected");
    }
  }

  private void openXMLFile() {
    File file = readFile("xml", "xml files");
    if (file != null && file.canRead()) {
      CSMARTGraph graph = XMLFrame.createGraph(file);
      if (graph != null)
        new XMLFrame("Test", graph);
    }
  }

  private void openGraph() {
    File file = readFile("dot", "graph files");
    if (file != null && file.canRead()) {
      CSMARTGraph graph = readGraphFromFile(file);
      if (graph == null)
	return;
      String fileName = file.getName();
      String graphType = 
	(String)graph.getAttributeValue(CSMARTGraph.GRAPH_TYPE);
      if (graphType.equals(CSMARTGraph.GRAPH_TYPE_XML))
        new XMLFrame("Test", graph);
      else if (graphType.equals(CSMARTGraph.GRAPH_TYPE_COMMUNITY))
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
    } else {
      if (log.isInfoEnabled())
	log.info("openGraph: no (readable) file selected");
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
				    "File (" + file.getName() + ") must have extension: '" +
				    extension + "'.");
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
   * Query user for agent URL if running multiple experiments
   * or not running under CSMART.
   */
  private static void getAgentURL() {
    //    Logger log = CSMART.createLogger(CSMARTUL.class.getName());

    if (csmart != null) 
      setExperimentToMonitor();
    else
      setURLToMonitor();
  }

  /**
   * If running under csmart, ask user for experiment to monitor.
   */
  private static void setExperimentToMonitor() {
    // ask user to select experiment from running experiments
    Experiment[] exp = CSMART.getRunningExperiments();
    Vector experimentNames = new Vector(exp.length + 1);
    for (int i = 0; i < exp.length; i++) {
      if (! experimentNames.contains(exp[i].getExperimentName()))
 	experimentNames.add(exp[i].getExperimentName());
    }
    experimentNames.add("No experiment -- custom URL");
    JComboBox cb = new JComboBox(experimentNames);
    JPanel panel = new JPanel();
    panel.add(new JLabel("Experiment to Monitor:"));
    panel.add(cb);
    panel.add(new JLabel("Select last item to enter custom URL"));
    int result = 
      JOptionPane.showConfirmDialog(null, panel, "Experiment To Monitor",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION)
      return;
    if (cb.getSelectedIndex() == exp.length) {
      experiment = null;
      setURLToMonitor();
    } else {
      experiment = exp[cb.getSelectedIndex()];
      setHostToMonitor();
    }
  }

  /**
   * If not running under csmart, ask user for url (host and port)
   * to monitor.
   */
  private static void setURLToMonitor() {
    Logger log = CSMART.createLogger(CSMARTUL.class.getName());
    // FIXME: Use secure mode if necessary here!
    // How do I decide?
    // Must I use a different port as well?
    JTextField tf = 
      new JTextField(ClientServletUtil.makeURL("localhost", agentPort));
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
    int startIndex = 0;
    int endIndex = -1;

    if (agentURL.indexOf("://") != -1) {
      agentProtocol = agentURL.substring(0, agentURL.indexOf("://"));
      startIndex = agentURL.indexOf("://") + 3;
    }
   

    endIndex = agentURL.indexOf(":", startIndex);
    if (endIndex != -1)
      agentHost = agentURL.substring(startIndex, endIndex);
    else 
      agentHost = agentURL.substring(startIndex);

    startIndex = endIndex;
    if (startIndex != -1) {
      String s = agentURL.substring(startIndex+1);
      try {
        agentPort = Integer.parseInt(s);
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("setURLToMonitor got bad agentPort: " + s);
        }
	agentURL = agentURL.substring(0, startIndex + 1) + agentPort;
      }
    } else {
      if (log.isDebugEnabled())
	log.debug("setURLToMonitor got not port, using protocol defaults");
      // no port given
      if (agentProtocol.equalsIgnoreCase("http"))
	agentPort = 80;
      else if (agentProtocol.equalsIgnoreCase("https"))
	agentPort = 443;
    }

    if (agentURL.indexOf("://") == -1)
      agentURL = agentProtocol + "://" + agentURL;

    //    log.debug("protocol: " + agentProtocol + ", host: " + agentHost + ", port: " + agentPort);
//      if (startIndex != -1) {
//        endIndex = agentURL.lastIndexOf('/');
//        if (endIndex != -1) {
//  	String s = agentURL.substring(startIndex+1, endIndex);
//  	try {
//  	  agentPort = Integer.parseInt(s);
//  	} catch (Exception e) {
//            if(log.isDebugEnabled()) {
//              log.error("CSMARTUL: " + e);
//            }
//  	}
//        }
//      }
    reset(agentHost, agentPort);
  }

  private static void reset(String agentHost, int agentPort) {
    agentMap = new CSMARTAgentMap(agentHost, agentPort);
    communityToAgents = null;
    agentToCommunity = null;
    agentToURL = null;
  }

  /**
   * Query user for agent URL and get names of agents from that URL.
   * The user is only queried once, unless we fail to contact the agent.
   */
  public static Vector getAgentURLs() {
    if (agentURL == null)
      getAgentURL();
    if (agentURL == null)
      return null;
    Vector agentURLs = null;
    try {
      agentURLs = ClientServletUtil.getAgentURLs(agentURL);
      if (agentURLs == null)
	JOptionPane.showMessageDialog(null,
                              agentURL + "/" +
                              ClientServletUtil.AGENT_PROVIDER_SERVLET +
			      " returned null; no information to graph.");
      else if (agentURLs.size() == 0)
	JOptionPane.showMessageDialog(null,
                                      agentURL + "/" +
                                   ClientServletUtil.AGENT_PROVIDER_SERVLET +
                           " returned no agents; no information to graph.");

      // FIXME: Ignore NodeAgents here?
    } catch (Exception e) {
       JOptionPane.showMessageDialog(null,
                                     "Failed to contact: " + 
                                     agentURL + "/" +
                                     ClientServletUtil.AGENT_PROVIDER_SERVLET +
                                     "; no information to graph.");
    }

    // if failed to contact agents, reset agentURL to null, 
    // so user is queried again
    if (agentURLs == null) 
      agentURL = null; 
    return agentURLs;
  }


  /**
   * Query user for an agent URL in the society,
   * get the URLs for all agents in that society,
   * and get objects from the specified servlet at all agents.
   */

  public static Collection getObjectsFromServlet(String servletName) {
    Vector agentURLs = getAgentURLs();
    if (agentURLs == null)
      return null;
    return getObjectsFromServlet(agentURLs, servletName, null, null, -1);
  }

  /**
   * Invoke specified servlet with parameter names, values, and limit,
   * at specified agents, and return results.
   */

  private static Collection getObjectsFromServlet(Vector agentURLs,
                                                  String servletName,
                                                  ArrayList parameterNames,
                                                  ArrayList parameterValues,
                                                  int limit) {
    return getObjectsFromServletWorker(agentURLs, servletName, 
                                       parameterNames, parameterValues, limit);
  }

  private static Collection getObjectsFromServletWorker(Vector agentURLs,
                                            String servletName,
                                            ArrayList parameterNames,
                                            ArrayList parameterValues,
                                            int limit) {

    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL");

    ServletResult servletResult =
      ClientServletUtil.getCollectionFromAgents(agentURLs, servletName,
                                                parameterNames, 
                                                parameterValues, limit);
    int n = servletResult.getNumberOfResponses();
    StringBuffer buf = new StringBuffer(100);
    Collection objectsFromServlet = null;
    int nAgents = 0;
    for (int i = 0; i < n; i++) {
      ServletResponse response = servletResult.getServletResponse(i);
      String s = response.getErrorMessage();
      if (s != null) {
        buf.append("Contacting: ");
        buf.append(response.getURL());
        buf.append(" ");
        buf.append(response.getErrorMessage());
        buf.append("\n");
      } else {
        Collection c = response.getCollection();
        if (c != null) {
          nAgents++;
          if (objectsFromServlet == null)
            objectsFromServlet = c;
          else {
            try {
              objectsFromServlet.addAll(c);
            } catch (Exception e) {
              if(log.isErrorEnabled()) {
                log.error("CSMARTUL can't add results to collection: ", e);
              }
            }
          }
        }
      }
    }
    if (servletResult.isLimitExceeded())
      JOptionPane.showMessageDialog(null,
           "Exceeded limit, producing a trimmed graph from " + nAgents +
                                      " agents.");
    else if (buf.length() != 0) 
      JOptionPane.showMessageDialog(null, buf.toString());
    return objectsFromServlet;
  }

  /**
   * Query servlet to get community <-> agent mapping.
   */

  private static void getCommunities() {
    if (communityToAgents != null)
      return; // only do this once
    // get agent and community names
    Collection orgAssets = 
      getObjectsFromServlet(ClientServletUtil.COMMUNITY_SERVLET);
    if (orgAssets == null) 
      return;
    if (orgAssets.size() == 0)
      JOptionPane.showMessageDialog(null,
 				    "No information received from agents.");
    processOrganizationAssets(orgAssets);
  }

  /**
   * Create community<->agents mapping.
   */

  private static void processOrganizationAssets(Collection orgAssets) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL");

    communityToAgents = new Hashtable();
    agentToCommunity = new Hashtable();
    agentToURL = new Hashtable();
    for (Iterator i = orgAssets.iterator(); i.hasNext(); ) {
      Object o = i.next();
      if (!(o instanceof PropertyTree)) {
        if(log.isWarnEnabled()) {
          log.warn("CSMARTUL: expected PropertyTree, got: " + 
                   o.getClass());
        }
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
        if(log.isDebugEnabled()) {
          log.debug("Adding agent name: " + agentName);
        }
	communityToAgents.put(communityName, agents);
      } else
	agents.addElement(agentName);
      String url = (String)properties.get(PropertyNames.AGENT_URL);
      url = url.substring(0, url.indexOf(ClientServletUtil.COMMUNITY_SERVLET));
      agentToURL.put(agentName, url);
    }
  }

  /**
   * Make community graph.
   */

  private void makeCommunityGraph() {
    // get agent and community names
    Collection objectsFromServlet = 
      getObjectsFromServlet(ClientServletUtil.COMMUNITY_SERVLET);
    if (objectsFromServlet == null) 
      return;
    int n = objectsFromServlet.size();
    if (n == 0)
      JOptionPane.showMessageDialog(null,
 				    "No information received from agents.");
    // set up agent<->community mappings in hashtables
    processOrganizationAssets(objectsFromServlet);
    Vector nodeObjects = new Vector(n);
    Hashtable nameToNodeObject = new Hashtable(n);
    for (Iterator i = objectsFromServlet.iterator(); i.hasNext(); ) {
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

    String servletId = ClientServletUtil.PLAN_SERVLET;
    int limit  = filter.getNumberOfObjects();
    ArrayList parameterNames = new ArrayList(1);
    ArrayList parameterValues = new ArrayList(1);
    // for example, planObjectsToIgnore=Plan_Element,Workflow,Asset
    String ignoreTypes = filter.getIgnoreObjectTypes();
    if (ignoreTypes != null) {
      parameterNames.add(PropertyNames.PLAN_OBJECTS_TO_IGNORE);
      parameterValues.add(ignoreTypes);
    }
    Collection objectsFromServlets = 
      getObjectsFromServletWorker(agentURLs, servletId,
                                  parameterNames, parameterValues, limit);

    if (objectsFromServlets == null)
      return;

    int nObjs = objectsFromServlets.size();
    if (nObjs == 0)
      return;

    // a Vector of ULPlanNodes
    Vector nodeObjects = new Vector();

    int nDuplicates = 0;
    Set UIDs = new HashSet();
    Iterator iter = objectsFromServlets.iterator();
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
//       if(log.isDebugEnabled()) {
//         Set keys = properties.keySet();
//         log.debug("Property names/values........");
//         for (Iterator j = keys.iterator(); j.hasNext(); ) {
//           String s = (String)j.next();
//           log.debug(s + "," + properties.get(s));
//         }
//       }
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
    String sLimit = JOptionPane.showInputDialog("Limit (e.g. 200, negative for all): ", "200");
    if (sLimit == null || sLimit.trim().equals("")) {
      return;
    }
    try {
      limit = Integer.parseInt(sLimit);
    } catch (NumberFormatException nfe) {
      if(log.isErrorEnabled()) {
        log.error("Illegal number: "+sLimit);
      }
      return;
    }

    if (limit == 0)
      return;

    String sIsDown = JOptionPane.showInputDialog("TraceChildren (true/false): ");
    if (sIsDown == null) {
      return;
    }
    isDown = (!("false".equalsIgnoreCase(sIsDown)));

    // create the graph
    makeThreadGraph(UID, agentName, isDown, limit);
  }

  /**
   * Make a thread graph using the plan object threads servlet.
   * Called to make a thread graph when the user has selected
   * an object in the plan graph.
   */

  public static void makeThreadGraph(String UID, String agentName,
				     boolean isDown, int limit) {
    getCommunities();
    List objectsFromServlet = ThreadUtils.getFullThread(agentMap, isDown, limit, 
						    agentName, UID);
    if (objectsFromServlet == null || objectsFromServlet.size() == 0) {
      JOptionPane.showMessageDialog(null,
	    "Cannot obtain thread information for specified plan object.");
      return;
    }
    if ((limit >= 0) &&
        (objectsFromServlet.size() > limit)) {
      JOptionPane.showMessageDialog(null,
	    "Exceeded limit of "+limit+" objects; creating a trimmed graph.");
    }
    Vector nodeObjects = new Vector(objectsFromServlet.size());
    Vector assetPropertyTrees = new Vector();
    for (Iterator i = objectsFromServlet.iterator(); i.hasNext(); ) {
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
	String assetAgentName = (String)pt.get(PropertyNames.ASSET_AGENT);
	String assetAgentAttrName = (String)pt.get(PropertyNames.AGENT_ATTR);
	if (assetAgentAttrName.equals(assetAgentName)) {
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
    Collection objectsFromServlet = 
      getObjectsFromServlet(ClientServletUtil.AGENT_INFO_SERVLET);
    if (objectsFromServlet == null)
      return;
    Vector nodeObjects = new Vector(objectsFromServlet.size());
    Hashtable nameToUID = new Hashtable();
    for (Iterator i = objectsFromServlet.iterator(); i.hasNext(); ) {
      Object o = i.next();
      if (!(o instanceof PropertyTree)) {
        if(log.isWarnEnabled()) {
          log.warn("CSMARTUL: expected PropertyTree, got: " + 
                             o.getClass());
        }
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
              if(log.isInfoEnabled()) {
                log.info("Unknown relationship: " + relatedToName);
              }
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
    Collection objectsFromServlet = 
      getObjectsFromServlet(ClientServletUtil.METRICS_SERVLET);
    if (objectsFromServlet == null)
      return;
    if(log.isInfoEnabled()) {
      log.info("Received metrics: " + objectsFromServlet.size());
    }

    ArrayList names = new ArrayList();
    ArrayList data = new ArrayList();

    Iterator iter = objectsFromServlet.iterator();
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

//   private void makeTopologyGraph() {
//     Window w =
//       new TopologyFrame("Topology", "localhost", 8800, "NCA");
//     myWindows.add(w);
//   }

  /**
   * Start up the CSMART Society Monitor stand-alone. <br>
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
    new CSMARTUL(null, null);
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

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

package org.cougaar.tools.csmart.ui.console;

import org.cougaar.core.agent.AgentManager;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.core.plugin.PluginManager;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.MetricComponent;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.util.FileUtilities;
import org.cougaar.tools.csmart.util.ResultsFileFilter;
import org.cougaar.tools.server.OutputPolicy;
import org.cougaar.tools.server.RemoteFileSystem;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Properties;

/**
 * ClassName:  NodeModel
 * Description:  The class contains all required data structures to
 * represents a Node within CSMART.  Modifications to key data can be
 * monitored via a listener.
 *
 *
 */
public class NodeModel extends Observable {
  private NodeStatusButton statusButton;
  private ConsoleStyledDocument doc;
  private ConsoleTextPane textPane;
  private ConsoleNodeListener listener;
  private String logFileName;
  private String nodeName;
  private Logger log;
  private NodeInfo info;
  private CSMARTConsoleModel cmodel;
  private String notifyCondition;
  private boolean notifyOnStandardError = false; // if stderr appears, notify user
  private ConsoleNodeOutputFilter displayFilter;
  private OutputPolicy outputPolicy;
  private CreateNodeThread thread;
  // used for log file name
  private static DateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
  private String state;
  public final static String STATE_INITTED = "NODE_STATE_INITTED";
  public final static String STATE_STARTING = "NODE_STATE_STARTING";
  public final static String STATE_RUNNING = "NODE_STATE_RUNNING";
  public final static String STATE_STOPPING = "NODE_STATE_STOPPING";
  public final static String STATE_STOPPED = "NODE_STATE_STOPPED";
  public final static String STATE_ATTACHING = "NODE_STATE_ATTACHING";
  public final static String STATE_UNATTACHED = "NODE_STATE_UNATTACHED";

  private NodeModel() {
    // We don't want a generic constructor, so make it private.
  }

  /**
   * Constructs a new NodeModel object based on the informtion in <code>NodeInfo</code>.
   *
   * @param info A <code>NodeInfo</code> object containing required data for this node.
   * @param cmodel <code>CSMARTConsoleModel</code> A pointer to the Console Model Object.
   */
  public NodeModel(NodeInfo info, CSMARTConsoleModel cmodel) {
    this.info = info;
    this.cmodel = cmodel;
    this.nodeName = info.getNodeName();
    createLogger();
    statusButton = createStatusButton(info.getNodeName(), info.getHostName());
    doc = new ConsoleStyledDocument();
    textPane = new ConsoleTextPane(doc, statusButton);
    logFileName = FileUtilities.getLogFileName(info.getNodeName(), new Date());
    this.outputPolicy = new OutputPolicy(10);

    createFilters();
    setState(STATE_INITTED);
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void setState(String state) {
    this.state = state;
    setChanged();
    notifyObservers(state);
  }

  public String getState() {
    return state;
  }

  // create a node event listener to get events from the node
  private void createListener() {
    listener = new ConsoleNodeListener(this);
  }

  // Set up Node filters & notifications
  private void createFilters() {
    this.notifyCondition = cmodel.getNotifyCondition();
    if (notifyCondition != null) {
      textPane.setNotifyCondition(notifyCondition);
    }
    ((ConsoleStyledDocument) textPane.getStyledDocument()).setBufferSize(cmodel.getViewSize());
    if (notifyOnStandardError) {
      statusButton.getMyModel().setNotifyOnStandardError(true);
    }
    if (displayFilter != null) {
      listener.setFilter(displayFilter);
    }
  }

  /**
   * Sets a filter on the Console Node Output.
   * @param filter The new <code>ConsoleNodeOutputFilter</code>
   */
  public void setFilter(ConsoleNodeOutputFilter filter) {
    displayFilter = filter;
    listener.setFilter(filter);
  }

  /**
   * Create a button representing a node.
   */
  private NodeStatusButton createStatusButton(String nodeName, String hostName) {
    NodeStatusButton button = new NodeStatusButton(new ColoredCircle(NodeStatusButton.unknownStatus, 20, null));
    button.setSelectedIcon(new SelectedColoredCircle(NodeStatusButton.unknownStatus, 20, null));
    button.setToolTipText("Node " + nodeName + " (" + hostName + "), unknown");
    button.setActionCommand(nodeName);
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setMargin(new Insets(2, 2, 2, 2));
    return button;
  }

  /**
   * Returns a handle to the NodeStatusButton for this Node.
   *
   * @return The <code>NodeStatusButton</code> for this Node
   */
  public NodeStatusButton getStatusButton() {
    return this.statusButton;
  }

  /**
   *
   * @return The Name of this Node
   */
  public String getNodeName() {
    return nodeName;
  }

  /**
   *
   * @return The Logfile for this node.
   */
  public String getLogFileName() {
    return logFileName;
  }

  /**
   *
   * @return A handle to the <code>ConsoleStyledDocument</code> for this Node.
   */
  public ConsoleStyledDocument getDoc() {
    return doc;
  }

  public NodeInfo getInfo() {
    return info;
  }

  public ConsoleTextPane getTextPane() {
    return textPane;
  }

  public ConsoleNodeListener getListener() {
    return listener;
  }

  public OutputPolicy getOutputPolicy() {
    return outputPolicy;
  }

  /**
   * The CreateNodeThread must be created each time that we start
   * or attach; threads can only be run once; they're not reusable.
   */
  public void start() {
    setState(STATE_STARTING);
    thread = new CreateNodeThread(cmodel, this);
    createListener();
    if (displayFilter != null)
      listener.setFilter(displayFilter);
    thread.start();
  }

  public void attach() {
    setState(STATE_ATTACHING);
    thread = new CreateNodeThread(cmodel, this);
    thread.setAttach(true);
    createListener();
    if (displayFilter != null)
      listener.setFilter(displayFilter);
    thread.start();
  }

  /**
   * Tell the node to stop.
   */
  public void stop() {
    setState(STATE_STOPPING);
    saveResults();
    thread.interrupt();
    thread.killNode();
  }

  /**
   * The node has stopped.  Called by ConsoleNodeListener.
   */
  public void stopped() {
    setState(STATE_STOPPED);
  }

  /**
   * Create a file for the results of this run.
   * Results file structure is:
   * <ExperimentName>
   *       Results-<Timestamp>.results
   */
  private void saveResults() {
    String dirname = makeResultDirectory();
    // Must check for null return here!?
    if (dirname == null) {
      // User didn't specify a directory or couldn't create one or something?
      if (log.isInfoEnabled())
        log.info("saveResults got no good result directory from makeResult: Using pwd.");
      // Is . really the right choice here?
      dirname = ".";
    }

    RemoteHost appServer = getInfo().getAppServer();
    RemoteFileSystem remoteFS = null;
    try {
      remoteFS = appServer.getRemoteFileSystem();
    } catch (Exception e) {
      if (log.isErrorEnabled())
        log.error("saveResults failed to get filesystem on " +
                  getInfo().getHostName() + ": ", e);
      remoteFS = null;
    }
    if (remoteFS == null) {
      final String host = getInfo().getHostName();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(null,
                                        "Cannot save results.  Unable to access filesystem for " +
                                        host + ".",
                                        "Unable to access file system",
                                        JOptionPane.WARNING_MESSAGE);
        }
      });
    } else {
      copyResultFiles(remoteFS, dirname);
    }
  }

  /**
   * Read remote files and copy to directory specified by experiment.
   */
  private void copyResultFiles(RemoteFileSystem remoteFS, String dirname) {
    char[] cbuf = new char[1000];
    try {
      // FIXME: This reads just from the current directory,
      // but should read from wherever the BasicMetric told it to read,
      // or in general, wherever the Component says to read
      // But does the AppServer support calling list on arbitrary paths?
      // See bug 1668
      // Maybe to generalize, let this traverse sub-directories?
      String[] filenames = remoteFS.list("./");
      for (int i = 0; i < filenames.length; i++) {
        if (!isResultFile(filenames[i]))
          continue;
        File newResultFile = new File(dirname + File.separator + filenames[i]);
        InputStream is = remoteFS.read(filenames[i]);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is), 1000);
        BufferedWriter writer = new BufferedWriter(new FileWriter(newResultFile));
        int len = 0;
        while ((len = reader.read(cbuf, 0, 1000)) != -1) {
          writer.write(cbuf, 0, len);
        }
        reader.close();
        writer.close();
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("NodeModel: copyResultFiles failed: ", e);
      }
    }
  }

  /**
   * This checks the society and recipes in the experiment to determine if
   * any of them generated this metrics file.
   * Creating a new File from the filename works because acceptFile
   * just looks at the filename.
   */
  private boolean isResultFile(String filename) {
    File thisFile = new java.io.File(filename);
    // if no experiment, use default filter
    if (cmodel.getExperiment() == null) {
      return new ResultsFileFilter().accept(thisFile);
    }
    SocietyComponent societyComponent = cmodel.getExperiment().getSocietyComponent();
    if (societyComponent != null) {
      java.io.FileFilter fileFilter = societyComponent.getResultFileFilter();
      if (fileFilter != null && fileFilter.accept(thisFile)) {
        return true;
      }
    }
    int nrecipes = cmodel.getExperiment().getRecipeComponentCount();
    for (int i = 0; i < nrecipes; i++) {
      RecipeComponent recipeComponent = cmodel.getExperiment().getRecipeComponent(i);
      if (recipeComponent instanceof MetricComponent) {
        MetricComponent metricComponent = (MetricComponent) recipeComponent;
        java.io.FileFilter fileFilter = metricComponent.getResultFileFilter();
        if (fileFilter != null && fileFilter.accept(thisFile)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Create a directory for the results of this run.
   * Results file structure is:
   * <ExperimentName>
   *   Results-<Timestamp>.results
   */
  private String makeResultDirectory() {
    // defaults, if we don't have an experiment
    File resultDir = CSMART.getResultDir();
    String experimentName = "Experiment";
    if (cmodel.getExperiment() != null) {
      resultDir = cmodel.getExperiment().getResultDirectory();
      experimentName = cmodel.getExperiment().getExperimentName();
    }
    // if user didn't specify results directory, save in local directory
    if (resultDir == null) {
      if (log.isInfoEnabled())
        log.info("No result directory specified. Should use a local dir. Returning null (in makeResultDirectory).");
      return null;
    }
    String dirname = resultDir.getAbsolutePath() + File.separatorChar +
        experimentName + File.separatorChar +
        "Results-" + fileDateFormat.format(cmodel.getRunStart());
    try {
      File f = new File(dirname);
      // guarantee that directories exist
      if (!f.exists() && !f.mkdirs() && !f.exists()) {
        if (log.isWarnEnabled())
          log.warn("Unabled to create directory " + dirname + ". Should default to local directory - returning null (in makeResultDirectory)");
        return null;
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Couldn't create results directory " + dirname + ": ", e);
      }
      return null;
    }
    return dirname;
  }


 /**
  * Get host properties from experiment if it exists.
  */
 public Object getHostPropertyValue(String hostName, String propertyName) {
    Experiment experiment = cmodel.getExperiment();
    if (experiment == null) return null;
    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      String s = hosts[i].getShortName();
      if (s.equalsIgnoreCase(hostName))
        return getPropertyValue(hosts[i], propertyName);
    }
    return null;
  }

  /**
   * Get node properties from experiment if it exists.
   */
  protected Object getNodePropertyValue(String nodeName, String propertyName) {
    Experiment experiment = cmodel.getExperiment();
    if (experiment == null) return null;
    HostComponent[] hosts = experiment.getHostComponents();
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
        String s = nodes[j].getShortName();
        if (s.equals(nodeName))
          return getPropertyValue(nodes[j], propertyName);
      }
    }
    return null;
  }

  private Object getPropertyValue(BaseComponent component, String name) {
    Property prop = component.getProperty(name);
    if (prop == null)
      return null;
    return prop.getValue();
  }



  /**
   * Get description of agents.
   * Returns an array of strings.
   */
  public ArrayList getAgentComponentDescriptions(String nodeName,
                                                 String agentName) {
    Experiment experiment = cmodel.getExperiment();
    if (experiment == null) return null;
    ComponentData societyComponentData = experiment.getSocietyComponentData();
    if (societyComponentData == null) {
      if (log.isWarnEnabled()) {
        log.warn("NodeModel: Need to save experiment");
      }
      return null;
    }
    ComponentData[] children = societyComponentData.getChildren();
    ComponentData nodeComponentData = null;
    for (int i = 0; i < children.length; i++) {
      if (children[i].getType().equals(ComponentData.HOST)) {
        ComponentData[] nodes = children[i].getChildren();
        for (int j = 0; j < nodes.length; j++) {
          if (nodes[j].getName().equals(nodeName)) {
            nodeComponentData = nodes[j];
            break;
          }
        }
      }
    }

    //  If couldn't find the node in the ComponentData, give up
    if (nodeComponentData == null)
      return null;

    ComponentData agentComponentData = null;

    // The "agent" might be a NodeAgent, in which case this is the right spot.
    if (agentName.equals(nodeComponentData.getName())) {
      agentComponentData = nodeComponentData;
    } else {
      // OK. Find the sub-Agent with the right name
      ComponentData[] agents = nodeComponentData.getChildren();
      for (int i = 0; i < agents.length; i++) {
        if (agents[i] instanceof AgentComponentData &&
            agents[i].getName().equals(agentName)) {
          agentComponentData = agents[i];
          break;
        }
      }
    }

    // If couldn't find the Agent in the ComponentData for the node, give up
    if (agentComponentData == null)
      return null;

    // Loop through the children
    ComponentData[] agentChildren = agentComponentData.getChildren();
    ArrayList entries = new ArrayList(agentChildren.length);
    for (int i = 0; i < agentChildren.length; i++) {

      // If this Agent is a NodeAgent, ignore its Agent children.
      if (agentChildren[i].getType().equals(ComponentData.AGENT))
        continue;

      // FIXME: This should use same code as ExperimentINIWriter if possible
      StringBuffer sb = new StringBuffer();
      if (agentChildren[i].getType().equals(ComponentData.AGENTBINDER)) {
        sb.append(PluginManager.INSERTION_POINT + ".Binder");
      } else if (agentChildren[i].getType().equals(ComponentData.NODEBINDER)) {
        sb.append(AgentManager.INSERTION_POINT + ".Binder");
      } else {
        sb.append(agentChildren[i].getType());
      }
      if (ComponentDescription.parsePriority(agentChildren[i].getPriority()) !=
          ComponentDescription.PRIORITY_COMPONENT) {
        sb.append("(" + agentChildren[i].getPriority() + ")");
      }
      sb.append(" = ");
      sb.append(agentChildren[i].getClassName());
      if (agentChildren[i].parameterCount() != 0) {
        sb.append("(");
        Object[] params = agentChildren[i].getParameters();
        sb.append(params[0].toString());
        for (int j = 1; j < agentChildren[i].parameterCount(); j++) {
          sb.append(",");
          sb.append(params[j].toString());
        }
        sb.append(")");
      }
      entries.add(sb.toString());
    }
    return entries;
  }

  /**
   * Restart is the same as start, except we first clear the
   * persistent data.
   */
  public void restart() {
    // remove the persistent data
    Properties properties = info.getProperties();
    properties.remove(Experiment.PERSIST_CLEAR);
    start();
  }

  public void setNotification(String notifyCondition, boolean notifyOnStdErr) {
    textPane.setNotifyCondition(notifyCondition);
    statusButton.getMyModel().clearError();
    statusButton.getMyModel().setNotifyOnStandardError(notifyOnStdErr);
  }

}

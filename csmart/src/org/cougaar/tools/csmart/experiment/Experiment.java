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

package org.cougaar.tools.csmart.experiment;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.*;
import javax.swing.JOptionPane;

import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.db.DBConflictHandler;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ModificationListener;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.name.ComponentName;
import org.cougaar.tools.csmart.core.property.name.CompositeName;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;

import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.recipe.RecipeComponent;

import org.cougaar.tools.csmart.ui.viewer.CSMART;

import org.cougaar.tools.csmart.util.ReadOnlyProperties;

import org.cougaar.tools.server.ConfigurationWriter;
import org.cougaar.core.node.Node;
import org.cougaar.core.agent.ClusterImpl;
import org.cougaar.util.Parameters;
import org.cougaar.util.log.Logger;

/**
 * A CSMART Experiment. Holds the components being run, and the configuration of host/node/agents.<br>
 * Computes the trials, and the configuration data for running a trial.
 */
public class Experiment extends ModifiableConfigurableComponent implements ModificationListener, java.io.Serializable {
  // org.cougaar.control.port; port for contacting applications server
  public static final int APP_SERVER_DEFAULT_PORT = 8484;
  public static final String NAME_SERVER_PORTS = "8888:5555";
  private static final String DESCRIPTION_RESOURCE_NAME = "description.html";
  public static final String PROP_PREFIX = "PROP$";
  private SocietyComponent society = null;
  private List hosts = new ArrayList();
  private List nodes = new ArrayList();
  private List recipes = new ArrayList();
  private ReadOnlyProperties defaultNodeArguments;
  private transient List listeners = null;
  private File resultDirectory; // where to store results
  private int numberOfTrials = 1;
  private List trials = new ArrayList();
  private boolean hasValidTrials = false;
  // schemes for varying experimental data
  public static final String VARY_ONE_DIMENSION = "Univariate";
  public static final String VARY_TWO_DIMENSION = "Bivariate";
  public static final String VARY_SEQUENTIAL = "Multivariate";
  public static final String VARY_RANDOM = "Random";
  private static final String[] variationSchemes = {
    VARY_ONE_DIMENSION, VARY_TWO_DIMENSION, VARY_SEQUENTIAL, VARY_RANDOM };
  private String variationScheme = variationSchemes[0];
  private boolean overrideEditable = false;
  //  private boolean cloned = false;
  private transient boolean editInProgress = false;
  private transient boolean runInProgress = false;

  private String expID = null; // An Experiment has a single ExpID
  private String trialID = null;
  // Soon Experiment's Trials will have TrialIDs

  // An Experiment has multiple Configuration pieces, potentially:
  // Host/Node, Node/Agent would be 2, for example
  private List configAssemblyIDs = new ArrayList();

  private ComponentData theWholeSoc = null;
  private transient LeafOnlyConfigWriter configWriter = null;

  private transient Logger log;

  public Experiment(String name, SocietyComponent societyComponent,
		    RecipeComponent[] recipes)
  {
    this(name);
    createLogger();
    setSocietyComponent(societyComponent);
    setRecipes(recipes);
    setDefaultNodeArguments();
  }


  public Experiment(String name) {
    super(name);
    createLogger();
    setDefaultNodeArguments();
  }

  public Experiment(String name, String expID, String trialID) {
    super(name);
    createLogger();
    this.expID = expID;
    this.trialID = trialID;
    setDefaultNodeArguments();
    if(log.isDebugEnabled()) {
      log.debug("Experiment: " + expID + " Trial: " + trialID);
    }
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  private void setDefaultNodeArguments() {
    defaultNodeArguments = new ReadOnlyProperties(Collections.singleton("org.cougaar.experiment.id"));
    defaultNodeArguments.put("org.cougaar.core.persistence.enable", "false");
    defaultNodeArguments.put("user.timezone", "GMT");
    defaultNodeArguments.put("org.cougaar.core.agent.startTime", "08/10/2005");
    defaultNodeArguments.put("org.cougaar.planning.ldm.lps.ComplainingLP.level",
                             "0");
    defaultNodeArguments.put("org.cougaar.message.transport.aspects",
                             "org.cougaar.core.mts.StatisticsAspect");
    defaultNodeArguments.put("org.cougaar.tools.server.nameserver.ports", 
                             NAME_SERVER_PORTS);
    defaultNodeArguments.put("org.cougaar.control.port", 
                             Integer.toString(APP_SERVER_DEFAULT_PORT));
    if (DBUtils.dbMode) {
      defaultNodeArguments.put("org.cougaar.configuration.database", 
			       Parameters.findParameter(DBUtils.DATABASE));
      defaultNodeArguments.put("org.cougaar.configuration.user", 
			       Parameters.findParameter(DBUtils.USER));
      defaultNodeArguments.put("org.cougaar.configuration.password", 
			       Parameters.findParameter(DBUtils.PASSWORD));
      defaultNodeArguments.setReadOnlyProperty("org.cougaar.experiment.id", getTrialID());
    }
    try {
      defaultNodeArguments
        .put("env.DISPLAY", InetAddress.getLocalHost().getHostName() +
             ":0.0");
    } catch (UnknownHostException uhe) {
      System.out.println(uhe);
    }
  }

  public Properties getDefaultNodeArguments() {
    return defaultNodeArguments;
  }

  public void setExperimentID(String expID) {
    this.expID = expID;
  }

  public void setTrialID(String trialID) {
    this.trialID = trialID;
    defaultNodeArguments.setReadOnlyProperty("org.cougaar.experiment.id", trialID);
  }

  public void setName(String newName) {
    super.setName(newName);
    fireModification();
  }
  public String getExperimentName() {
    return getShortName();
  }
  public void setSocietyComponent(SocietyComponent soc) {
    society = soc;
  }
  public void setRecipes(RecipeComponent[] ary) {
    recipes.clear();
    recipes.addAll(Arrays.asList(ary));
  }
  public void addSocietyComponent(SocietyComponent sc) {
    if (society == null) {
      setSocietyComponent (sc);
    } else {
      throw new IllegalArgumentException("Already have a society in experiment " + this);
    }
  }
  public void addRecipe(RecipeComponent recipe) {
    if (!recipes.contains(recipe)) recipes.add(recipe);
  }
  
  public void addComponent(ModifiableComponent comp) {
    if (comp instanceof SocietyComponent)
      addSocietyComponent((SocietyComponent)comp);
    else if (comp instanceof RecipeComponent)
      addRecipe((RecipeComponent)comp);
  }
  public void removeSociety(SocietyComponent sc) {
    society = null;
  }
  public void removeRecipe(RecipeComponent recipe) {
    recipes.remove(recipe);
  }
  public void removeComponent(ModifiableComponent comp) {
    if (comp instanceof SocietyComponent)
      removeSociety((SocietyComponent)comp);
    if (comp instanceof RecipeComponent)
      removeRecipe((RecipeComponent)comp);
    // Must handle random components!!!
  }
  public int getSocietyComponentCount() {
    return (society == null ? 0 : 1);
  }
  public int getComponentCount() {
    return getSocietyComponentCount() + recipes.size();
  }
  public int getRecipeCount() {
    return recipes.size();
  }

  public SocietyComponent getSocietyComponent(int i) {
    if (i != 0) {
      throw new IllegalArgumentException("Asked for society " + i + " when only 1, index 0, allowed");
    }
    return society;
  }

  public SocietyComponent getSocietyComponent() {
    return society;
  }

  public RecipeComponent getRecipe(int i) {
    return (RecipeComponent) recipes.get(i);
  }
  public RecipeComponent[] getRecipes() {
    return (RecipeComponent[])recipes.toArray(new RecipeComponent[recipes.size()]);
  }
  private ModifiableComponent getComponent(int i) {
    // What a hack!!!
    if (i < getSocietyComponentCount())
      return (ModifiableComponent)getSocietyComponent();
    else if (i < getComponentCount()) 
      return (ModifiableComponent)(getRecipe(i - (getSocietyComponentCount())));
    else
      return null;
  }
      
  public String toString() {
    return getShortName();
  }

  /**
   * Set edit in progress.  Used by UI tools to indicate that an
   * experiment is being edited.  Note that this is distinct from
   * setting the editability flag, which indicates whether the experiment
   * can ever be edited.
   */
  public void setEditInProgress(boolean newEditInProgress) {
    editInProgress = newEditInProgress;
  }

  /**
   * Set run in progress.  Used by UI tools to indicate that an
   * experiment is being run.  Note that this is distinct from
   * setting the runnability flag, which indicates whether the experiment
   * can ever be run.
   */
  public void setRunInProgress(boolean newRunInProgress) {
    runInProgress = newRunInProgress;
  }

  /**
   * Return whether or not experiment is being edited.
   * Note that the experiment may be viewed (but not edited) in an editor,
   * even if this flag is not set.
   * @return boolean whether or not experiment is being edited
   */
  public boolean isEditInProgress() {
    return editInProgress;
  }

  /**
   * Return whether or not experiment is being run.
   * @return boolean whether or not experiment is being run
   */
  public boolean isRunInProgress() {
    return runInProgress;
  }

  /**
   * Returns true if user could override editability.
   */

  /**
   * Returns true if experiment is editable: 
   * it has no trial results, and
   * it's not being edited or run
   * The first condition can be overridden using the setEditable method.
   * @return true if experiment is editable, false otherwise
   */
  public boolean isEditable() {
    //    if (editInProgress || runInProgress)
    //      return false;
    // allow user to edit a running experiment
    if (editInProgress)
      return false;
    if (overrideEditable)
      return true;
    else
      return !hasResults();
  }

  private boolean hasResults() {
    Trial[] trials = getTrials();
    for (int i = 0; i < trials.length; i++) {
      TrialResult[] results = trials[i].getTrialResults();
      if (results.length != 0) 
        return true;
    }
    return false;
  }

  /**
   * Make an experiment editable even if
   * it has experiment results.
   */
  public void setEditable(boolean editable) {
    if (editable == overrideEditable) return; // no change
    overrideEditable = editable;
    if(log.isDebugEnabled()) {
      log.debug("new editable " + editable);
    }
    //    for (int i = 0; i < getComponentCount(); i++)
    //      getComponent(i).setEditable(editable);
    fireModification();
  }

  /**
   * Return whether or not experiment is runnable.  An experiment is runnable:
   * if it has a host-node-agent mapping, and
   * it has no unbound properties, and
   * it's not being edited or run
   * @return whether or not an experiment is runnable
   */
  public boolean isRunnable() {
    if (!hasConfiguration() || hasUnboundProperties())
      return false;
    //    return !editInProgress && !runInProgress;
    return !runInProgress; // allow user to run experiment they're editing
  }

  /**
   * Stop after the current trial.
   * This invokes experimentStopped.
   */
  public void stop() {
    // TODO: stop the experiment after the current trial
    experimentStopped();
  }

  /**
   * Notify listeners that experiment was terminated.
   */
  public void experimentStopped() {
    // Tell the Societies in the experiment they are no longer running?
    if (society != null)
      society.setRunning(false);
  }

  /**
   * Return a deep copy of the experiment.
   * Called when an experiment is selected in the organizer.
   * Add experiments and society to workspace.
   * @return the copy of the experiment.
   */
  public ModifiableComponent copy(String uniqueName) {
    Experiment experimentCopy = null;
    if (DBUtils.dbMode) 
      experimentCopy = new Experiment(uniqueName, expID, trialID);
    else
      experimentCopy = new Experiment(uniqueName);
    Properties newProps = experimentCopy.getDefaultNodeArguments();
    newProps.clear();
    newProps.putAll(getDefaultNodeArguments());

    // this copies the society and the recipes
    for (int i = 0; i < getComponentCount(); i++) {
      ModifiableComponent mc = getComponent(i);
      ModifiableComponent copiedComponent = null;
      if (mc instanceof SocietyComponent) {
        copiedComponent = mc.copy("Society for " + uniqueName);
      } else if (mc instanceof RecipeComponent) {
        copiedComponent = mc.copy("Recipe for " + uniqueName);
      }
      if (copiedComponent != null)
        experimentCopy.addComponent(copiedComponent);
    }
    // copy hosts
    HostComponent[] hosts = getHosts();
    HostComponent[] nhosts = new HostComponent[hosts.length];
    for (int i = 0; i < hosts.length; i++) {
      nhosts[i] = experimentCopy.addHost(hosts[i].getShortName().toString());
      nhosts[i].setServerPort(hosts[i].getServerPort());
      nhosts[i].setMonitoringPort(hosts[i].getMonitoringPort());
    }
    // copy nodes
    NodeComponent[] nodes = getNodes();
    NodeComponent[] nnodes = new NodeComponent[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      nnodes[i] = ((ExperimentNode)nodes[i]).copy(experimentCopy);
      experimentCopy.addNodeComponent((ExperimentNode)nnodes[i]);
    }
    // reconcile hosts-nodes-agents
    AgentComponent[] nagents = experimentCopy.getAgents();
    NodeComponent nnode = null;
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] onodes = hosts[i].getNodes();
      // for each of the old nodes, find the corresponding new node
      for (int j = 0; j < onodes.length; j++) {
        for (int k = 0; k < nnodes.length; k++) {
          if (nnodes[k].getShortName().equals(onodes[j].getShortName())) {
              nnode = nnodes[k];
              // add new node to new host
              nhosts[i].addNode(nnode);
              break;
          }
        }
        // add new agents to new node
        AgentComponent[] oagents = onodes[j].getAgents();
        for (int x = 0; x < oagents.length; x++) 
          for (int y = 0; y < nagents.length; y++) 
            if (nagents[y].getShortName().equals(oagents[x].getShortName()))
              nnode.addAgent(nagents[y]);
      }
    }
    // copy the results directory; results from the copied experiment
    // will be stored in this directory under the copied experiment name
    experimentCopy.setResultDirectory(getResultDirectory());
    
    return experimentCopy;
  }

  /**
   * Get the URL of the file that describes this experiment.
   * @param helpFile the URL of the file
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  public void initProperties() {
    // put all the properties in here
    // the list of components are children
    // the editability & runability are properties
    // the hosts, trials, etc should all be properties
    // then we wouldn't need the special copy mechanism
    // (if all the sub-pieces were also components)
  }

  public void modified(ModificationEvent e) {
    fireModification();
  }

  protected void fireModification() {
    super.fireModification();
  }

  public HostComponent addHost(String name) {
    // FIXME! This allows 2 Hosts with the same name!
    ExperimentHost result = new ExperimentHost(name);
    hosts.add(result);
    result.addModificationListener(this);
    fireModification();
    return result;
  }

  public void removeHost(HostComponent hostComponent) {
    ExperimentHost sc = (ExperimentHost) hostComponent;
    hosts.remove(hostComponent);
    sc.dispose();           // Let the host disassociate itself from nodes
    sc.removeModificationListener(this);
    fireModification();
  }

  public void renameHost(HostComponent hostComponent, String name) {
    // FIXME! This allows 2 Hosts with the same name!
    hosts.remove(hostComponent);
    hostComponent.setName(name);
    hosts.add(hostComponent);
    fireModification();
  }

  /**
   * Insure that we have a valid nameserver specification. There are
   * several possibilities: If the default node nameserver argument
   * has been set, try to make that be the name server. Parse out the
   * host name part. Then scan all the hosts of nodes having agents
   * and check to see if any such host matches that specified by the
   * default node nameserver argument. If it does, keep that
   * nameserver. If it doesn't (or if there was no default node
   * nameserver argument) then use the first host as the nameserver.
   **/
  public void updateNameServerHostName() {
    Properties defaultNodeArgs = getDefaultNodeArguments();
    String oldNameServer = defaultNodeArgs.getProperty("org.cougaar.name.server");
    String newNameServer = null;
    String dfltNameServer = null;
    String nameServerHost = null;
    if (oldNameServer != null) {
      int colon = oldNameServer.indexOf(':');
      if (colon >= 0) {
        nameServerHost = oldNameServer.substring(0, colon);
      } else {
        nameServerHost = oldNameServer;
      }
    }

    HostComponent[] hosts = getHosts();
  hostLoop:
    for (int i = 0; i < hosts.length; i++) {
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
	AgentComponent[] agents = nodes[j].getAgents();
	// skip nodes that have no agents
	if (agents == null || agents.length == 0)
	  continue;
        String thisHost = hosts[i].getShortName();
        if (thisHost.equals(nameServerHost)) {
          newNameServer = oldNameServer;
          if(log.isDebugEnabled()) {
            log.debug("Keeping Name Server: " + nameServerHost);
          }
          break hostLoop;       // Use existing nameserver definition
        }
        if (dfltNameServer == null) { // First host is default
          dfltNameServer = thisHost + ":" + NAME_SERVER_PORTS;
          if (oldNameServer == null) {
            break hostLoop;     // Use dfltNameServer
          }
        }
      }
    }
    if (newNameServer == null) newNameServer = dfltNameServer;

    // set new server in all nodes
    NodeComponent[] nodes = getNodesInner();
    for (int i = 0; i < nodes.length; i++) {
      NodeComponent node = nodes[i];
      Properties arguments = node.getArguments();
      // Insure no per-node override exists
      arguments.remove("org.cougaar.name.server");
    }
    // Now install experiment-wide setting.
    if (newNameServer != null) {
      defaultNodeArgs.setProperty("org.cougaar.name.server", newNameServer);
    }
  }

  private void addNodeComponent(ExperimentNode node) {
    nodes.add(node);
    node.addModificationListener(this);
    fireModification();
  }

  public NodeComponent addNode(String name) {
    // FIXME! This allows 2 Nodes with the same name. Of itself, that's
    // OK, but on the same Host, that would be bad, and in general
    // its confusing.
    ExperimentNode result = new ExperimentNode(name, this);
    addNodeComponent(result);
    return result;
  }

  public void removeNode(NodeComponent nc) {
    System.out.println("Removed node: " + nc.getShortName());
    ExperimentNode sc = (ExperimentNode) nc;
    nodes.remove(nc);
    sc.dispose();           // Let the node disassociate itself from agents
    sc.removeModificationListener(this);
    fireModification();
  }

  public void renameNode(NodeComponent nc, String name) {
    // FIXME! This allows 2 Nodes with the same name!
    nodes.remove(nc);
    nc.setName(name);
    //fireModification();
    nodes.add(nc);
    fireModification();
  }

  // get all the Societies, Recipes
  private List getComponents() {
    List comps = new ArrayList();
    for (int i = 0; i < getComponentCount(); i++) {
      comps.add(getComponent(i));
    }
    return comps;
  }

  /**
   * @return a <code>ModifiableConfigurableComponent[]</code> array of all the components in the experiment
   */
  public ModifiableComponent[] getComponentsAsArray() {
    List comps = getComponents();
    ModifiableComponent[] compArray = (ModifiableComponent[])comps.toArray(new ModifiableComponent[comps.size()]);
    return compArray;
  }
  
  /**
   * Get the agents from the society in the experiment.
   */
  private List getAgentsInSociety() {
    List agents = new ArrayList();
    if (society != null) {
      AgentComponent[] sags = society.getAgents();
      if (sags != null && sags.length > 0)
	agents.addAll(Arrays.asList(sags));
    }
    return agents;
  }

  private List getAgentsInComponents() {
    List agents = new ArrayList();
    List nags = getAgentsInSociety();
    if (nags != null && (! nags.isEmpty()))
      agents.addAll(nags);
    nags = getAgentsInRecipes();
    if (nags != null && (! nags.isEmpty()))
      agents.addAll(nags);
    return agents;
  }

  private List getAgentsInRecipes() {
    List agents = new ArrayList();
    for (int i = 0; i < recipes.size(); i++) {
      RecipeComponent recipe = (RecipeComponent)recipes.get(i);
      AgentComponent[] impagents = recipe.getAgents();
      if (impagents != null && impagents.length > 0)
	agents.addAll(Arrays.asList(impagents));
    }
    return agents;
  }

  public List getAgentsList() {
    List agents = getAgentsInSociety();
    if (agents == null)
      agents = new ArrayList();
    List other = getAgentsInRecipes();
    if (other != null && (! other.isEmpty()))
      agents.addAll(other);
    return agents;
  }
  
  public AgentComponent[] getAgents() {
    List agents = getAgentsList();
    return (AgentComponent[])agents.toArray(new AgentComponent[agents.size()]);
  }

  /**
   * Reconcile agents in nodes with agents in society,
   * so that if a society has been reconfigured such that
   * it no longer contains some agents, those agents are also
   * removed from the nodes.
   */
  public NodeComponent[] getNodes() {
    NodeComponent[] result = getNodesInner();
    updateNameServerHostName(); // Be sure this is update-to-date
    return result;
  }

  private NodeComponent[] getNodesInner() {
    List agents = getAgentsList();
    for (Iterator i = nodes.iterator(); i.hasNext(); ) {
      NodeComponent nc = (NodeComponent) i.next();
      AgentComponent[] nodeAgent = nc.getAgents();
      for (int j = 0; j < nodeAgent.length; j++) {
	if (!agents.contains(nodeAgent[j]))
	  nc.removeAgent(nodeAgent[j]);
      }
    }
    return (NodeComponent[]) nodes.toArray(new NodeComponent[nodes.size()]);
  }

  public HostComponent[] getHosts() {
    return (HostComponent[]) hosts.toArray(new HostComponent[hosts.size()]);
  }

  public File getResultDirectory() {
    return resultDirectory;
  }

  public void setResultDirectory(File resultDirectory) {
    this.resultDirectory = resultDirectory;
    fireModification();
  }

//    public boolean isCloned() {
//      return cloned;
//    }

//    public void setCloned(boolean newCloned) {
//      cloned = newCloned;
//    }

  public String getExperimentID() {
    if (DBUtils.dbMode) {
      return expID;
    } else {
      return null;
    }
  }

  public String getTrialID() {
    if (DBUtils.dbMode) {
      return trialID;
    } else {
      return null;
    }
  }

  /**
   * An Experiment now has a ConfigurationWriter -
   * one which lets all the components write themselves out
   *
   * @param nodes a <code>NodeComponent[]</code> list of the nodes in the run
   * @return a <code>ConfigurationWriter</code> to write out the config data
   */

//   public ConfigurationWriter getConfigurationWriter(NodeComponent[] nodes) {
//     // The given set of nodes is potentially fewer than the full set in the society
//     // Note the ugly this parameter...
//     if (inDatabase) {
//       // Send a config writer that only writes LeafComponentData
//       try {
//         return new LeafOnlyConfigWriter(getComponents(), nodes, this);
//       } catch (Exception e) {
//         e.printStackTrace();
//         return null;
//       }
//     } else {
//       return new ExperimentINIWriter(getComponents(), nodes, this);
//     }
//   }

  private void createConfigWriter() {
    configWriter = new LeafOnlyConfigWriter(getSocietyComponentData());
  }

  public Iterator getConfigFiles(NodeComponent[] nodes) {
    if (DBUtils.dbMode) {
      // Send a config writer that only writes LeafComponentData
      try {
        //        configWriter = new LeafOnlyConfigWriter(getComponents(), nodes, this);
        createConfigWriter();
        return configWriter.getFileNames();
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("Exception, creating config writer", e);
        }
        return null;
      }
    } else {
      //      return new ExperimentINIWriter(getComponents(), nodes, this);
      return null;
    }    
  }

  public void writeContents(String filename, OutputStream out) throws Exception {
    configWriter.writeFile(filename, out);
  }

  public void saveToDb(DBConflictHandler ch) {
    try {
      updateNameServerHostName(); // Be sure this is up-to-date
      Set writtenNodes = new HashSet();
      List components = getComponents();
      NodeComponent[] nodesToWrite = getNodes();
      ComponentData theSoc = new GenericComponentData();
      theSoc.setType(ComponentData.SOCIETY);
      theSoc.setName(getExperimentName()); // this should be experiment: trial FIXME
      theSoc.setClassName("java.lang.Object"); // Must not be null
      theSoc.setOwner(this); // the experiment
      theSoc.setParent(null);
      addDefaultNodeArguments(theSoc);
      //      PopulateDb pdb =
      //        new PopulateDb("CMT", "CSHNA", "CSMI", getExperimentName(),
      //                       getExperimentID(), trialID, !isCloned(), ch);
      // TODO: isCloned argument to PopulateDB is going away
      PopulateDb pdb =
        new PopulateDb("CMT", "CSHNA", "CSMI", getExperimentName(),
                       getExperimentID(), trialID, true, ch);
      setExperimentID(pdb.getExperimentId());
      //      trialID = pdb.getTrialId();
      setTrialID(pdb.getTrialId()); // sets trial id and -D argument

      // For each node, create a GenericComponentData, and add it to the society
      for (Iterator i = hosts.iterator(); i.hasNext(); ) {
        ExperimentHost host = (ExperimentHost) i.next();
        ComponentData hc = new GenericComponentData();
        hc.setType(ComponentData.HOST);
        hc.setName(host.getShortName());
        hc.setClassName("");
        hc.setOwner(this);
        hc.setParent(theSoc);
        addPropertiesAsParameters(hc, host);
        theSoc.addChild(hc);
        NodeComponent[] nodes = host.getNodes();
        for (int j = 0; j < nodes.length; j++) {
          saveNodeToDb(nodes[j], hc);
          writtenNodes.add(nodes[j]);
        }
      }
      for (int i = 0; i < nodesToWrite.length; i++) {
        if (writtenNodes.contains(nodesToWrite[i])) continue;
        saveNodeToDb(nodesToWrite[i], theSoc);
        writtenNodes.add(nodesToWrite[i]);
      }

      // Some components will want access to the complete set of Nodes
      // in the society, etc. To get that, they must get back to the
      // root soc object, and do a getOwner and go from there. Ugly.

      if(log.isDebugEnabled()) {
        log.debug("Adding All Components");
      }

      // Now ask each component in turn to add its stuff
      boolean componentWasRemoved = false;
      for (int i = 0, n = components.size(); i < n; i++) {
        BaseComponent soc = (BaseComponent) components.get(i);
        if(log.isDebugEnabled()) {
          log.debug(soc + ".addComponentData");
        }
        soc.addComponentData(theSoc);
        componentWasRemoved |= soc.componentWasRemoved();
      }
      if (componentWasRemoved) pdb.repopulateCMT(theSoc);
      pdb.populateHNA(theSoc);
      //      if (pdb.populateHNA(theSoc)) setCloned(true);

      // then give everyone a chance to modify what they've collectively produced
      for (int i = 0, n = components.size(); i < n; i++) {
        BaseComponent soc = (BaseComponent) components.get(i);
        soc.modifyComponentData(theSoc, pdb);
        if (soc.componentWasRemoved()) {
          pdb.repopulateCMT(theSoc);
        }
        pdb.populateCSMI(theSoc);
        //        if (pdb.populateCSMI(theSoc)) setCloned(true);
      }
      pdb.setModRecipes(recipes);
      pdb.close();
      theWholeSoc = theSoc;
    } catch (Exception sqle) {
      if (log.isErrorEnabled())
	log.error(sqle.toString());
    }
  }

  /** 
   * Dump out the INI files for the first trial to
   * the local results directory for that trial.
   * This saves the experiment to the database.
   * The caller is responsible for not unnecessarily
   * saving the experiment to the database.
   */

  public void dumpINIFiles() {
    ExperimentINIWriter cw = null;
    theWholeSoc = null;
    if (DBUtils.dbMode) {
      if (theWholeSoc == null) {
	// write it to the db
	saveToDb(new DBConflictHandler() {
	    public int handleConflict(Object msg, Object[] choices, 
                            Object defaultChoice) {
	      return JOptionPane.showOptionDialog(null,
						  msg,
						  "Database Conflict",
						  JOptionPane.WARNING_MESSAGE,
						  JOptionPane.DEFAULT_OPTION,
						  null,
						  choices,
						  defaultChoice);
	    }
	  });
      }
      if (theWholeSoc != null)
	cw = new ExperimentINIWriter(theWholeSoc);
    } else {
      cw = new ExperimentINIWriter(getComponents(), getNodes(), this);
    }
    
    File resultDir = getResultDirectory();
    // if user didn't specify results directory, save in local directory
    if (resultDir == null) {
      resultDir = new File(".");
    }
    Trial trial = getTrials()[0];
    String dirname = resultDir.getAbsolutePath() + File.separatorChar + 
      getExperimentName() + File.separatorChar +
      trial.getShortName() + File.separatorChar +
      "INIFiles-dump";
    File f = null;
    try {
      f = new File(dirname);
      // guarantee that directories exist
      if (!f.exists() && !f.mkdirs() && !f.exists()) 
	f = new File(".");
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Couldn't create results directory: " + e, "Can't create directory", JOptionPane.ERROR_MESSAGE);
      if(log.isErrorEnabled()) {
        log.error("Couldn't create results directory: ", e);
      }
    }
    if (f != null) {
      try {
	JOptionPane.showMessageDialog(null, "Writing ini file to " + f.getAbsolutePath() + "...");
	//	System.out.println("Writing ini files to " + f.getAbsolutePath() + "...");
	cw.writeConfigFiles(f);
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("Couldn't write ini files: ", e);
        }
      }
    }
  }
  
  private void addDefaultNodeArguments(ComponentData theSoc) {
    Properties props = getDefaultNodeArguments();
    for (Iterator i = props.entrySet().iterator(); i.hasNext(); ) {
      Map.Entry entry = (Map.Entry) i.next();
      theSoc.addParameter("-D" + entry.getKey() + "=" + entry.getValue());
    }
  }

  private void saveNodeToDb(NodeComponent node, ComponentData parent) {
    ComponentData nc = new GenericComponentData();
    nc.setType(ComponentData.NODE);
    nc.setName(node.getShortName());
    nc.setClassName(Node.class.getName()); // leave this out?? FIXME
    nc.setOwner(this); // the experiment? FIXME
    nc.setParent(parent);
    ComponentName name = 
      new ComponentName((ConfigurableComponent) node, "ConfigurationFileName");
    nc.addParameter(node.getShortName());
    Properties props = node.getArguments();
    for (Iterator i = props.entrySet().iterator(); i.hasNext(); ) {
      Map.Entry entry = (Map.Entry) i.next();
      nc.addParameter("-D" + entry.getKey() + "=" + entry.getValue());
    }
    addPropertiesAsParameters(nc, node);
    parent.addChild(nc);
    AgentComponent[] agents = node.getAgents();
    if (agents != null && agents.length > 0) {
      for (int j = 0; j < agents.length; j++) {
        AgentComponentData ac = new AgentComponentData();
        ac.setName(agents[j].getShortName());
        ac.setClassName(ClusterImpl.class.getName());
        ac.addParameter(agents[j].getShortName()); // Agents have one parameter, the agent name
        if(log.isDebugEnabled()) {
          log.debug("Adding agent: " + 
                    agents[j].getShortName() +
                    " to: " + node.getShortName());
        }
        // FIXME!!
        ac.setOwner(null); // the society that contains this agent FIXME!!!
        ac.setParent(nc);
        addPropertiesAsParameters(ac, agents[j]);
        nc.addChild((ComponentData)ac);
      }
    }
  }

  private void addPropertiesAsParameters(ComponentData cd, BaseComponent cp)
  {
    for (Iterator it = cp.getPropertyNames(); it.hasNext(); ) {
      ComponentName pname = (ComponentName) it.next();
      Property prop = cp.getProperty(pname);
      if (prop != null) {
        Object pvalue = prop.getValue();
        if (pvalue instanceof String)
          cd.addParameter(PROP_PREFIX + pname.last() + "=" + pvalue);
      }
    }
  }

  /**
   * Get possible experiment variation schemes, i.e. 
   * univariate, bivariate, multivariate, random.
   * @return the variation schemes which can being used in this experiment
   */

  public static String[] getVariationSchemes() {
    return variationSchemes;
  }

  /**
   * Return variation scheme being used in this experiment:
   * univariate, bivariate, multivariate, or random.
   * @return the variation scheme being used in this experiment
   */

  public String getVariationScheme() {
    return variationScheme;
  }

  /**
   * Set variation scheme to use in this experiment:
   * univariate, bivariate, multivariate, or random.
   * @param the variation scheme to use in this experiment
   */

  public void setVariationScheme(String variationScheme) {
    if (this.variationScheme != null &&
	!(this.variationScheme.equals(variationScheme)))
      hasValidTrials = false; // new variation scheme, invalidates trials
    this.variationScheme = variationScheme;
  }

  /**
   * Return whether or not the experiment has a valid set of trials.
   * The trials may be invalid because something about the experiment
   * has changed (i.e. the set of configurable components, the
   * unbound variables of a component, the variation scheme)
   * and the trials have not been recomputed.  This supports recomputing
   * trials only as needed.
   * @return boolean indicating whether or not the trials are valid
   */

  public boolean hasValidTrials() {
    return hasValidTrials;
  }

  /**
   * Indicate that the experiment does not have valid trials; should
   * be called when some aspect of the experiment or its components
   * changes so as to invalidate trials.
   */

  public void invalidateTrials() {
    hasValidTrials = false;
  }

  /**
   * If the experiment has still unbound properties,
   * then we can't run it yet.<br>
   * For each property in the experiment, if it is not set,
   * and we don't have a set of experimental values, return true;
   *
   * @return a <code>boolean</code> value
   */
  public boolean hasUnboundProperties() {
    int n = getComponentCount();
    for (int i = 0; i < n; i++) {
      ModifiableComponent comp = getComponent(i);
      List propertyNames = comp.getPropertyNamesList();
      for (Iterator j = propertyNames.iterator(); j.hasNext(); ) {
	Property property = comp.getProperty((CompositeName)j.next());
	if (! property.isValueSet()) {
	  List values = property.getExperimentValues();
	  if (values == null || values.size() == 0) {
	    return true;
	  }
	}
      } // end of loop over this components properties
    } // end of loop over all components in experiment
    return false;
  }

  /**
   * If the experiment has at least one host with at least one node
   * with at least one agent, that is a configuration.
   *
   * @return a <code>boolean</code> true if it has a configured agent
   */
  public boolean hasConfiguration() {
    if (hosts.isEmpty() || nodes.isEmpty() || getAgents() == null || getAgents().length == 0) {
      return false;
    }
    HostComponent[] hosts = getHosts();
    for (int i = 0; i < hosts.length; i++) {
      if (hosts[i] == null)
	continue;
      NodeComponent[] nodes = hosts[i].getNodes();
      for (int j = 0; j < nodes.length; j++) {
	if (nodes[j] == null)
	  continue;
	AgentComponent[] agents = nodes[j].getAgents();
	if (agents.length > 0)
	  return true;
      } // loop over nodes in host
    } // loop over hosts in experiment
    return false;
  }
  
  /**
   * Blindly assume the experiment has no hosts or nodes yet.
   * Create a host for the local host, a Node named Node0, and put
   * all the agents on that node on that host.
   */
  public void createDefaultConfiguration() {
    // Check if it already has a node?
    // create one Node
    NodeComponent node = null;

    // BIG HACK IN HERE!!!!!!
    SocietyComponent soc = getSocietyComponent();
    node = addNode("Node0");
    if (expID == null) {
      if (soc != null) 
	expID = soc.getShortName() + "-basic-expt-id";
      else
	expID = "Basic-expt-id";
    }
      
    // Put all the agents in this Node
    // Skip agents already assigned to Nodes?
    AgentComponent[] agents = getAgents();
    for (int i = 0; i < agents.length; i++) {
      node.addAgent(agents[i]);
    }
      
    // Create one host
    // get this machine's name and use it
    String localhost = "localhost";
    try {
      localhost = java.net.InetAddress.getLocalHost().getHostName();
    } catch (java.net.UnknownHostException e) {}
    HostComponent host = addHost(localhost);

    // put the one node on that host
    host.addNode(node);
  }
  
  /**
   * Return the trials defined for this experiment.  Computes trials
   * if necessary.
   * @return array of Trials
   */
  public Trial[] getTrials() {
    if (hasValidTrials)
      return (Trial[])trials.toArray(new Trial[trials.size()]);

    getTrialCount(); // update trial count

    // get lists of unbound properties and their experimental values
    List properties = new ArrayList();
    List experimentValues = new ArrayList();
    int n = getComponentCount();
    for (int i = 0; i < n; i++) {
      ModifiableComponent comp = getComponent(i);
      List propertyNames = comp.getPropertyNamesList();
      for (Iterator j = propertyNames.iterator(); j.hasNext(); ) {
	Property property = comp.getProperty((CompositeName)j.next());
	List values = property.getExperimentValues();
	if (values != null && values.size() != 0) {
	  properties.add(property);
	  experimentValues.add(values);
	}
      }
    }

    // handle simple case of no properties to vary
    int nProperties = properties.size();
    if (nProperties == 0) {
      hasValidTrials = true;
      numberOfTrials = 1;
      Trial trial = new Trial("Trial 1");
      trial.initProperties();
      trials = new ArrayList(1);
      trials.add(trial);
      variationScheme = VARY_ONE_DIMENSION;
      return (Trial[])trials.toArray(new Trial[trials.size()]);
    }

    // create trials according to variation scheme
    if (variationScheme.equals(VARY_ONE_DIMENSION)) 
      return getUnivariateTrials(properties, experimentValues);
    else if (variationScheme.equals(VARY_TWO_DIMENSION)) 
      return getBivariateTrials(properties, experimentValues);
    else if (variationScheme.equals(VARY_SEQUENTIAL) ||
	     variationScheme.equals(VARY_RANDOM))
      return getMultivariateTrials(properties, experimentValues);
    else {
      System.out.println("Variation scheme not implemented: " + 
			 variationScheme);
      return null;
    }
  }

  private Trial[] getUnivariateTrials(List properties, List experimentValues) {
    trials = new ArrayList(numberOfTrials);
    int propToVaryIndex = 0; // index of property being varied
    int k = 0; // index into values of property being varied
    int nProperties = properties.size();
    for (int i = 0; i < numberOfTrials; i++) {
      Trial trial = new Trial("Trial " + (i+1));
      trial.initProperties();
      for (int j = 0; j < nProperties; j++) {
	List parameterValues = (List)experimentValues.get(j);
	Object value;
	if (j == propToVaryIndex)
	  value = parameterValues.get(k++);
	else
	  value = parameterValues.get(0);
	trial.addTrialParameter((Property)properties.get(j), value);
      }
      // if we've stepped through all the values of the property being varied
      // then vary the next property
      if (((List)experimentValues.get(propToVaryIndex)).size() == k) {
	k = 1; // after first pass, skip nominal values
	propToVaryIndex++;
      }
      trials.add(trial);
    }
    hasValidTrials = true;
    return (Trial[])trials.toArray(new Trial[trials.size()]);
  }

  private Trial[] getBivariateTrials(List properties, List experimentValues) {
    hasValidTrials = true;
    return null;
  }

  public void addTrial(Trial trial) {
    trials.add(trial);
    numberOfTrials++;
  }

  public void removeTrial(int trialIndex) {
    trials.remove(trialIndex);
    numberOfTrials--;
    if (numberOfTrials == 0)
      numberOfTrials = 1;
  }

  /**
   * Get the number of trials. Recomputes the number of trials if necessary.
   * @return number of trials
   */
  public int getTrialCount() {
    if (hasValidTrials) 
      return numberOfTrials;
    ArrayList experimentValueCounts = new ArrayList(100);
    int n = getComponentCount();
    for (int i = 0; i < n; i++) {
      ModifiableComponent comp = getComponent(i);
      Iterator names = comp.getPropertyNames();
      while (names.hasNext()) {
	Property property = comp.getProperty((CompositeName)names.next());
	List values = property.getExperimentValues();
	if (values != null)
	  experimentValueCounts.add(new Integer(values.size()));
      }
    }
    // one dimension: sum the counts of experiment values
    // but only count nominal value the first time
    if (variationScheme.equals(VARY_ONE_DIMENSION)) {
      numberOfTrials = 0;
      for (int i = 0; i < experimentValueCounts.size(); i++)
	numberOfTrials = numberOfTrials + 
	  ((Integer)experimentValueCounts.get(i)).intValue() - 1;
      numberOfTrials++; // add one to use nominal value for first time
    }
    // sequential or random: (all combinations): multiply counts
    else if (variationScheme.equals(VARY_RANDOM) ||
	     variationScheme.equals(VARY_SEQUENTIAL)) {
      numberOfTrials = 1;
      for (int i = 0; i < experimentValueCounts.size(); i++)
	numberOfTrials = numberOfTrials *
	  ((Integer)experimentValueCounts.get(i)).intValue();
    }
    // two dimension: ???
    if (numberOfTrials == 0)
      numberOfTrials = 1;  // always assume at least one trial
    return numberOfTrials;
  }

  private void addParameterValues(List trials,
				  Property property, 
				  List values, int nCopies) {
    int x = 0;
    int j = 0;
    int nValues = values.size();
    for (int i = 0; i < numberOfTrials; i++) {
      ((Trial)trials.get(i)).addTrialParameter(property, values.get(j));
      x++;
      if (x >= nCopies) {
	x = 0;
	j++;
	if (j >= nValues)
	  j = 0;
      }
    }
  }

  private Trial[] getMultivariateTrials(List properties, 
					List experimentValues) {
    trials = new ArrayList(numberOfTrials);
    for (int i = 0; i < numberOfTrials; i++) {
      Trial trial = new Trial("Trial " + (i+1));
      trial.initProperties();
      trials.add(trial);
    }
    int nProperties = properties.size();
    // how many times to copy each value in the experiment values
    // to the trials
    int nCopies = 1;
    for (int i = 0; i < nProperties; i++) {
      addParameterValues(trials,
			 (Property)properties.get(i),
			 (List)experimentValues.get(i),
			 nCopies);
      nCopies = nCopies * ((List)(experimentValues.get(i))).size();
    }
    hasValidTrials = true;
    return (Trial[])trials.toArray(new Trial[trials.size()]);
  }

  /**
   * Get component data for the society in the experiment.
   * If the experiment is not in the database, then it
   * constructs the society component data here.
   * @return ComponentData the component data for the society
   */

  // TODO: make creating the society component data be the
  // same for both standalone and indatabase experiments
  // and consolidate the code in one place
  // parts of this are currently replicated in two configuration
  // writers and in the saveToDb method above
  // saveToDb creates host component data; the config writers and this do not
  public ComponentData getSocietyComponentData() {
    if (DBUtils.dbMode && theWholeSoc != null)
      return theWholeSoc;
    theWholeSoc = new GenericComponentData();
    theWholeSoc.setType(ComponentData.SOCIETY);
    theWholeSoc.setName(getExperimentName()); // this should be experiment: trial FIXME
    theWholeSoc.setClassName("java.lang.Object"); // leave this out? FIXME
    theWholeSoc.setOwner(this); // the experiment
    theWholeSoc.setParent(null);
    // For each node, create a GenericComponentData, and add it to the society
    addNodes();

    // Some components will want access to the complete set of Nodes in the society, etc.
    // To get that, they must get back to the root soc object,
    // and do a getOwner and go from there. Ugly.
    
    // Now ask each component in turn to add its stuff
    List components = getComponents();
    for (int i = 0; i < components.size(); i++) {
      BaseComponent soc = (BaseComponent) components.get(i);
      soc.addComponentData(theWholeSoc);
    }
    // Then give everyone a chance to modify what they've collectively produced
    for (int i = 0; i < components.size(); i++) {
      BaseComponent soc = (BaseComponent) components.get(i);
      soc.modifyComponentData(theWholeSoc);
    }    
    return theWholeSoc;
  }
  
  private void addNodes() {
    NodeComponent[] nodesToWrite = getNodes();
    for (int i = 0; i < nodesToWrite.length; i++) {
      ComponentData nc = new GenericComponentData();
      nc.setType(ComponentData.NODE);
      nc.setName(nodesToWrite[i].getShortName());
      nc.setClassName("org.cougaar.core.node.Node"); // leave this out?? FIXME
      nc.setOwner(this);
      nc.setParent(theWholeSoc);
      ComponentName name = 
	  new ComponentName((ConfigurableComponent)nodesToWrite[i], "ConfigurationFileName");
      try {
	nc.addParameter(((BaseComponent)nodesToWrite[i]).getProperty(name).getValue().toString());
      } catch (NullPointerException e) {
	nc.addParameter(nc.getName() + ".ini");
      }
      theWholeSoc.addChild(nc);
      addAgents(nodesToWrite[i], nc);
    }
  }

  private void addAgents(NodeComponent node, ComponentData nc) {
    AgentComponent[] agents = node.getAgents();
    if (agents == null || agents.length == 0)
      return;
    for (int i = 0; i < agents.length; i++) {
      AgentComponentData ac = new AgentComponentData();
      ac.setName(agents[i].getFullName().toString());
      // FIXME!!
      ac.setOwner(null); // the society that contains this agent FIXME!!!
      ac.setParent(nc);
      nc.addChild((ComponentData)ac);
    }
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
    createConfigWriter();
  }

} // end of Experiment.java







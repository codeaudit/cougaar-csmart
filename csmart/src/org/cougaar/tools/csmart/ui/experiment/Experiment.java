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

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.viewer.Organizer;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.server.ConfigurationWriter;
import org.cougaar.core.society.Node;
import org.cougaar.core.cluster.ClusterImpl;

/**
 * A CSMART Experiment. Holds the components being run, and the configuration of host/node/agents.<br>
 * Computes the trials, and the configuration data for running a trial.
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 */
public class Experiment extends ModifiableConfigurableComponent implements ModificationListener, java.io.Serializable {
  private static final String DESCRIPTION_RESOURCE_NAME = "description.html";
  private List societies = new ArrayList();
  private List hosts = new ArrayList();
  private List nodes = new ArrayList();
  private List recipes = new ArrayList();
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
  private boolean editable = true;
  private boolean runnable = true;
  private boolean cloned = false;

  private String expID = null; // An Experiment has a single ExpID
  private String trialID = null;
  // Soon Experiment's Trials will have TrialIDs

  // An Experiment has multiple Configuration pieces, potentially:
  // Host/Node, Node/Agent would be 2, for example
  private List configAssemblyIDs = new ArrayList();

  private boolean inDatabase = false;

  public Experiment(String name, SocietyComponent[] societyComponents,
		    RecipeComponent[] recipes)
  {
    this(name);
    setSocietyComponents(societyComponents);
    setRecipes(recipes);
  }

  public Experiment(String name) {
    super(name);
  }

  public Experiment(String name, String expID, String trialID) {
    super(name);
    this.expID = expID;
    this.trialID = trialID;
    inDatabase = true;
    //    System.out.println("Experiment: " + expID + " Trial: " + trialID);
  }

  public void setExperimentID(String expID) {
    this.expID = expID;
  }

  // This method should get called by Organizer
  // When user tries to load a previously saved Experiment
  public static Experiment loadCMTExperiment (String expID) {
    Experiment ret = new Experiment(expID);
    ret.setExperimentID(expID);
    
    // FIXME:
    // Retrieve the experiment with this ID from the database:
    // Get the Assembly's that are societies, and add them to the Experiment
    // Get the Assembly's that are configurations and fill in the H/N/A stuff,
    // but store where we got this info
    return ret;
  }
  
  public void setName(String newName) {
    super.setName(newName);
    fireModification();
  }
  public String getExperimentName() {
    return getShortName();
  }
  public void setSocietyComponents(SocietyComponent[] ary) {
    societies.clear();
    societies.addAll(Arrays.asList(ary));
  }
  public void setRecipes(RecipeComponent[] ary) {
    recipes.clear();
    recipes.addAll(Arrays.asList(ary));
  }
  public void addSocietyComponent(SocietyComponent sc) {
    if (!societies.contains(sc)) societies.add(sc);
  }
  public void addRecipe(RecipeComponent recipe) {
    if (!recipes.contains(recipe)) recipes.add(recipe);
  }
  
  public void addComponent(ModifiableConfigurableComponent comp) {
    if (comp instanceof SocietyComponent)
      addSocietyComponent((SocietyComponent)comp);
    else if (comp instanceof RecipeComponent)
      addRecipe((RecipeComponent)comp);
    // handle others!!!
  }
  public void removeSociety(SocietyComponent sc) {
    societies.remove(sc);
  }
  public void removeRecipe(RecipeComponent recipe) {
    recipes.remove(recipe);
  }
  public void removeComponent(ModifiableConfigurableComponent comp) {
    if (comp instanceof SocietyComponent)
      removeSociety((SocietyComponent)comp);
    if (comp instanceof RecipeComponent)
      removeRecipe((RecipeComponent)comp);
    // Must handle random components!!!
  }
  public int getSocietyComponentCount() {
    return societies.size();
  }
  public int getComponentCount() {
    return societies.size() + recipes.size();
  }
  public int getRecipeCount() {
    return recipes.size();
  }
  public SocietyComponent getSocietyComponent(int i) {
    return (SocietyComponent) societies.get(i);
  }
  public RecipeComponent getRecipe(int i) {
    return (RecipeComponent) recipes.get(i);
  }
  
  public ModifiableConfigurableComponent getComponent(int i) {
    // What a hack!!!
    if (i < getSocietyComponentCount())
      return (ModifiableConfigurableComponent)getSocietyComponent(i);
    else if (i < getComponentCount()) 
      return (ModifiableConfigurableComponent)(getRecipe(i - (getSocietyComponentCount())));
    else
      return null;
  }
      
  public String toString() {
    return getShortName();
  }

  /**
   * Returns true if experiment is editable: 
   * it is not being edited, 
   * it is not being controlled by the console (regardless of whether
   * or not it has started running),
   * it has no saved data,
   * and all of its societies are editable.
   * @return true if experiement is editable, false otherwise
   */
  public boolean isEditable() {
    if (editable) {
      // make sure that societies are editable too
      // FIXME: Really? What if one component is never editable?
//        for (int i = 0; i < getComponentCount(); i++)
//  	if (!((ModifiableConfigurableComponent)getComponent(i)).isEditable())
//  	  return false;
      return true;
    }
    return false;
  }

  /**
   * Set whether or not the experiment can be edited.
   * @param editable true if experiment is editable and false otherwise
   */
  public void setEditable(boolean editable) {
    this.editable = editable;
    for (int i = 0; i < getComponentCount(); i++)
      getComponent(i).setEditable(editable);
  }

  /**
   * Get whether or not experiment is runnable.  An experiment is 
   * runnable whenever it's not being edited, if it is fully defined.<br>
   * @return whether or not an experiment is runnable
   */
  public boolean isRunnable() {
    if (! hasConfiguration() || hasUnboundProperties()) {
      //System.err.println("Experiment runnable: " + runnable);
      //if (! hasConfiguration())
	//System.err.println("Experiment does not have a complete config");
      //if (hasUnboundProperties())
	//System.err.println("Experiment has unbound props");
      return false;
    }
    //System.err.println("Experiment.isRunnable returning: " + runnable);
    return runnable;
  }

  /**
   * Set whether or not experiment is runnable.  An experiment is 
   * runnable whenever it's not being edited.
   * @param runnable whether or not an experiment is runnable
   */
  public void setRunnable(boolean runnable) {
    this.runnable = runnable;
  }

  /**
   * Returns true if any society in the experiment is running, false otherwise.
   * Running experiments are not editable, but they can be copied,
   * and the copy can be edited.
   * @return true if experiment is running, false otherwise
   */
  public boolean isRunning() {
    for (int i = 0; i < societies.size(); i++)
      if (((SocietyComponent)societies.get(i)).isRunning())
	return true;
    return false;
  }

  /**
   * Returns true if this experiment is being monitored, false otherwise.
   * @return true if experiment is being monitored
   */
  public boolean isMonitored() {
    if (listeners == null) 
      return false;
    for (int i = 0; i < listeners.size(); i++) {
      ExperimentListener listener = (ExperimentListener)listeners.get(i);
      if (listener.isMonitoring())
	return true;
    }
    return false;
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
    if (listeners != null) {
      for (int i = 0; i < listeners.size(); i++) {
	ExperimentListener listener = (ExperimentListener)listeners.get(i);
	listener.experimentTerminated();
      }
    }
    // Tell the Societies in the experiment they are no longer running?
    for (int i = 0; i < societies.size(); i++) {
      ((SocietyComponent)societies.get(i)).setRunning(false);
    }
  }

  /**
   * Return a deep copy of the experiment.
   * Called when an experiment is selected in the organizer.
   * Add experiments and societies to workspace.
   * @return the copy of the experiment.
   */
  public Experiment copy(Organizer organizer, Object context) {
    String uniqueName = organizer.generateExperimentName(getExperimentName());
    Experiment experimentCopy = null;
    if (inDatabase) 
      experimentCopy = new Experiment(uniqueName, expID, trialID);
    else
      experimentCopy = new Experiment(uniqueName);
    for (int i = 0; i < getComponentCount(); i++) {
      ModifiableConfigurableComponent sc = getComponent(i);
      ModifiableConfigurableComponent copiedSC = organizer.copyComponent(sc, context);
      experimentCopy.addComponent(copiedSC);
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
    for (int i = 0; i < nodes.length; i++) 
      nnodes[i] = experimentCopy.addNode(nodes[i].getShortName().toString());
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
   * Add a listener that is queried
   * when the experiment is about to be manually terminated, and
   * notified when an experiment is terminated.
   * @param listener the listener
   */
  public void addExperimentListener(ExperimentListener listener) {
    if (listeners == null) 
      listeners = new ArrayList();
    listeners.add(listener);
  }

  public void removeExperimentListener(ExperimentListener listener) {
    if (listeners == null) 
      return;
    int i = listeners.indexOf(listener);
    if (i != -1)
      listeners.remove(i);
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

  public HostComponent addHost(String name) {
    // FIXME! This allows 2 Hosts with the same name!
    ExperimentHost result = new ExperimentHost(name);
    hosts.add(result);
    result.addModificationListener(this);
    fireModification();
    return result;
  }

  public void removeHost(HostComponent nc) {
    ExperimentHost sc = (ExperimentHost) nc;
    hosts.remove(nc);
    sc.dispose();           // Let the host disassociate itself from nodes
    sc.removeModificationListener(this);
    fireModification();
  }

  public void renameHost(HostComponent nc, String name) {
    // FIXME! This allows 2 Hosts with the same name!
    hosts.remove(nc);
    ((ConfigurableComponent)nc).setName(name);
    //fireModification();
    hosts.add(nc);
    fireModification();
  }

  public NodeComponent addNode(String name) {
    // FIXME! This allows 2 Nodes with the same name. Of itself, that's
    // OK, but on the same Host, that would be bad, and in general
    // its confusing.
    ExperimentNode result = new ExperimentNode(name, this);
    nodes.add(result);
    result.addModificationListener(this);
    fireModification();
    return result;
  }

  public void removeNode(NodeComponent nc) {
    ExperimentNode sc = (ExperimentNode) nc;
    nodes.remove(nc);
    sc.dispose();           // Let the node disassociate itself from agents
    sc.removeModificationListener(this);
    fireModification();
  }

  public void renameNode(NodeComponent nc, String name) {
    // FIXME! This allows 2 Hosts with the same name!
    nodes.remove(nc);
    ((ConfigurableComponent)nc).setName(name);
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
  public ModifiableConfigurableComponent[] getComponentsAsArray() {
    List comps = getComponents();
    ModifiableConfigurableComponent[] compArray = (ModifiableConfigurableComponent[])comps.toArray(new ModifiableConfigurableComponent[comps.size()]);
    return compArray;
  }
  
  /**
   * Get the agents from the societies in the experiment.
   */
  private List getAgentsInSocieties() {
    List agents = new ArrayList();
    for (int i = 0; i < societies.size(); i++) {
      SocietyComponent society = (SocietyComponent)societies.get(i);
      AgentComponent[] sags = society.getAgents();
      if (sags != null && sags.length > 0)
	agents.addAll(Arrays.asList(sags));
    }
    return agents;
  }

  private List getAgentsInComponents() {
    List agents = new ArrayList();
    List nags = getAgentsInSocieties();
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
    List agents = getAgentsInSocieties();
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
   * Reconcile agents in nodes with agents in societies,
   * so that if a society has been reconfigured such that
   * it no longer contains some agents, those agents are also
   * removed from the nodes.
   */
  public NodeComponent[] getNodes() {
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

  public boolean isCloned() {
    return cloned;
  }

  public void setCloned(boolean newCloned) {
    cloned = newCloned;
  }

  public String getExperimentID() {
    if (inDatabase) {
      return expID;
    } else {
      return null;
    }
  }

  public String getTrialID() {
    if (inDatabase) {
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
  public ConfigurationWriter getConfigurationWriter(NodeComponent[] nodes) {
    // The given set of nodes is potentially fewer than the full set in the society
    // Note the ugly this parameter...
    if (inDatabase) {
      // Send a config writer that only writes LeafComponentData
      try {
        return new LeafOnlyConfigWriter(getComponents(), nodes, this);
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    } else {
      return new ExpConfigWriterNew(getComponents(), nodes, this);
    }
  }

  public void saveToDb() {
    try {
      Set writtenNodes = new HashSet();
      List components = getComponents();
      NodeComponent[] nodesToWrite = getNodes();
      ComponentData theSoc = new GenericComponentData();
      theSoc.setType(ComponentData.SOCIETY);
      theSoc.setName(getExperimentName()); // this should be experiment: trial FIXME
      theSoc.setClassName(""); // leave this out? FIXME
      theSoc.setOwner(this); // the experiment
      theSoc.setParent(null);
      PopulateDb pdb =
        new PopulateDb("CMT", "CSHNA", "CSMI", getExperimentName(),
                       getExperimentID(), trialID, !isCloned());
      setExperimentID(pdb.getExperimentId());
      trialID = pdb.getTrialId();

      // For each node, create a GenericComponentData, and add it to the society
      for (Iterator i = hosts.iterator(); i.hasNext(); ) {
        ExperimentHost host = (ExperimentHost) i.next();
        ComponentData hc = new GenericComponentData();
        hc.setType(ComponentData.HOST);
        hc.setName(host.getShortName());
        hc.setClassName("");
        hc.setOwner(this);
        hc.setParent(theSoc);
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

      // Some components will want access to the complete set of Nodes in the society, etc.
      // To get that, they must get back to the root soc object,
      // and do a getOwner and go from there. Ugly.
    
      // Now ask each component in turn to add its stuff
      for (int i = 0; i < components.size(); i++) {
        ComponentProperties soc = (ComponentProperties) components.get(i);
//          System.out.println(soc + ".addComponentData");
        soc.addComponentData(theSoc);
      }
      pdb.setPreexistingItems(theSoc); // Record these items so additions can be detected

      // then give everyone a chance to modify what they've collectively produced
      for (int i = components.size() - 1; i >= 0; i--) {
        ComponentProperties soc = (ComponentProperties) components.get(i);
        soc.modifyComponentData(theSoc, pdb);
      }
      if (pdb.populate(theSoc, 1)) {
        setCloned(true);        // If not cloned before, we certainly are now
      }
      pdb.setModRecipes(recipes);
      pdb.close();
    } catch (Exception sqle) {
      sqle.printStackTrace();
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
    parent.addChild(nc);
    AgentComponent[] agents = node.getAgents();
    if (agents != null && agents.length > 0) {
      for (int j = 0; j < agents.length; j++) {
        AgentComponentData ac = new AgentComponentData();
        ac.setName(agents[j].getShortName());
        ac.setClassName(ClusterImpl.class.getName());
        // FIXME!!
        ac.setOwner(null); // the society that contains this agent FIXME!!!
        ac.setParent(nc);
        nc.addChild((ComponentData)ac);
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
      ModifiableConfigurableComponent comp = getComponent(i);
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
   * FIXME: This method is still buggy
   *
   * @return a <code>boolean</code> true if it has a configured agent
   */
  public boolean hasConfiguration() {
    if (hosts.isEmpty() || nodes.isEmpty() || getAgents() == null || getAgents().length == 0) {
      return false;
    }
    // An experiment has a configuration if it has at least one host
    // which has at least one Node
    // which has at least one Agent
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
    SocietyComponent soc = getSocietyComponent(0);
    if (soc != null && soc.getSocietyName().equals("3ID-135-CMT")) {
      node = addNode("SMALL-135-TRANS-NODE");
      expID = "SMALL-135-TRANS";
    } else
      node = addNode("Node0");
    
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
      ModifiableConfigurableComponent comp = getComponent(i);
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
      ModifiableConfigurableComponent comp = getComponent(i);
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
   * Indicates whether or not the experiment is in the database (or should
   * be written to the database for new experiments).
   * @param db true if experiment is, or should be, in database
   */

  public void setInDatabase(boolean db) {
    inDatabase = db;
  }

  /**
   * Indicates whether or not the experiment is in the database (or should
   * be written to the database for new experiments).
   * @return boolean true if experiment is, or should be, in database
   */
  
  public boolean isInDatabase() {
    return inDatabase;
  }

} // end of Experiment.java







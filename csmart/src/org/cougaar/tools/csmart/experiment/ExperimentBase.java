/* 
 * <copyright>
 *  
 *  Copyright 2003-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package org.cougaar.tools.csmart.experiment;

import org.cougaar.core.agent.SimpleAgent;
import org.cougaar.core.node.Node;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentListener;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.core.property.ModificationListener;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.db.DBConflictHandler;
import org.cougaar.tools.csmart.recipe.RecipeBase;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.AgentBase;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyBase;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.util.ReadOnlyProperties;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.awt.*;

/**
 * org.cougaar.tools.csmart.experiment
 *
 */
public abstract class ExperimentBase extends ModifiableConfigurableComponent implements Experiment {
  private static final String DESCRIPTION_RESOURCE_NAME = "description.html";
  // Member Variables
  private SocietyComponent societyComponent = null;
  private final List hosts = new ArrayList();
  private final List nodes = new ArrayList();
  final List recipes = new ArrayList();
  ReadOnlyProperties defaultNodeArguments;
  private File resultDirectory; // where to store results
  private transient boolean editInProgress = false;
  private transient boolean runInProgress = false;
  ComponentData completeSociety = null;
  private String expID = null; // An Experiment has a single ExpID
  private String trialID = null;

  // modification event
  private static final int EXPERIMENT_SAVED = 1;
  transient LeafOnlyConfigWriter configWriter = null;
  transient Logger log;
  // Mark whether the experiment has been modified
  // and should be saved
  boolean modified = true;
  // add an observer to my arguments
  // if these arguments are modified, then
  // notify listeners on the experiment that it's modified
  private transient Observer myObserver = null;
  /**
   * Create a modification listener, which is registered on
   * all the experiment components. If any components
   * are modified, then mark this experiment as modified,
   * and fire a modification event to the experiment's
   * modification listeners.
   * Checks for modification event being EXPERIMENT_SAVED
   * in which case, it does not mark the experiment modified.
   */
  private final ModificationListener myModificationListener = new MyModificationListener();

  // The assembly holding community info for this Experiment.
  private String commAsb = null;

  public ExperimentBase(String name) {
    super(name);
  }

  public ExperimentBase(String name, String expID, String trialID) {
    super(name);
    this.expID = expID;
    this.trialID = trialID;
  }

  final void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  final void createObserver() {
    if (myObserver == null) {
      myObserver = new Observer() {
        public void update(final Observable o, final Object arg) {
          fireModification();
        }
      };
      defaultNodeArguments.addObserver(myObserver);
    }
  }

  /**
   * Adds a <code>societyComponent</code> to this Experiment.
   *
   * @param sc
   * @exception java.lang.IllegalArgumentException if an error occurs
   */
  public final void addSocietyComponent(SocietyComponent sc) throws IllegalArgumentException {
    if (this.societyComponent == null) {
      setSocietyComponent(sc);
    } else {
      throw new IllegalArgumentException("Already have a societyComponent in experiment " + this);
    }
  }

  /**
   * Removes the <code>SocietyComponent</code> from this experiment
   *
   */
  public final void removeSocietyComponent() {
    if (this.societyComponent == null)
      return;
    removeListeners((ModifiableConfigurableComponent) societyComponent);
    this.societyComponent = null;
    fireModification();
  }

  /**
   * Returns the total number of Societies in this experiment.
   * Since experiments can only have 1 society, this returns
   * a 0 or 1 value.
   *
   * @return an <code>int</code> value
   */
  public final int getSocietyComponentCount() {
    return (this.societyComponent == null ? 0 : 1);
  }

  /**
   * Return the <code>SocietyComponent</code>
   *
   * @return a <code>SocietyComponent</code> value
   */
  public final SocietyComponent getSocietyComponent() {
    return this.societyComponent;
  }

  final void setSocietyComponent(SocietyComponent society) {
    if (this.societyComponent != null)
      removeSocietyComponent();
    this.societyComponent = society;
    installListeners((ModifiableConfigurableComponent) society);
    fireModification();
  }

  private void installListeners(ModifiableConfigurableComponent component) {
    component.addModificationListener(myModificationListener);
  }

  private void removeListeners(ModifiableConfigurableComponent component) {
    component.removeModificationListener(myModificationListener);
  }

  /**
   * Adds an array of <code>RecipeComponent</code>s to this
   * experiment.
   *
   * @param newRecipes RecipeComponents
   */
  public final void setRecipeComponents(RecipeComponent[] newRecipes) {
    for (int i = 0; i < recipes.size(); i++)
      removeListeners((ModifiableConfigurableComponent) recipes.get(i));
    recipes.clear();
    // FIXME: Remove duplicates?
    for (int i = 0; i < newRecipes.length; i++)
      installListeners((ModifiableConfigurableComponent) newRecipes[i]);
    recipes.addAll(Arrays.asList(newRecipes));
    fireModification();
  }

  /**
   * Adds a <code>RecipeComponent</code> to this Experiment
   *
   * @param recipe - <code>RecipeComponent</code> to add to Experiment
   */
  public final void addRecipeComponent(RecipeComponent recipe) throws IllegalArgumentException {
    if (!recipes.contains(recipe)) {
      recipes.add(recipe);
      installListeners((ModifiableConfigurableComponent) recipe);
      fireModification();
    } else {
      throw new IllegalArgumentException("Recipe already exists in experiment");
    }
  }

  /**
   * Removes the specified <code>RecipeComponent</code>.
   *
   * @param recipe
   */
  public final void removeRecipeComponent(RecipeComponent recipe) {
    if (!this.recipes.contains(recipe))
      return;
    this.recipes.remove(recipe);
    removeListeners((ModifiableConfigurableComponent) recipe);
    fireModification();
  }

  /**
   * Returns the total number of recipes in this experiment.
   *
   * @return an <code>int</code> value
   */
  public final int getRecipeComponentCount() {
    return this.recipes.size();
  }

  /**
   * Gets a specific recipe based on Index.
   *
   * @param i
   * @return a <code>RecipeComponent</code> value
   * @exception java.lang.IndexOutOfBoundsException if an error occurs
   */
  public final RecipeComponent getRecipeComponent(int i) throws IndexOutOfBoundsException {
    return (RecipeComponent) recipes.get(i);
  }

  /**
   * Gets all <code>RecipeComponents</code> for this experiment.
   *
   * @return a <code>RecipeComponent[]</code> value
   */
  public final RecipeComponent[] getRecipeComponents() {
    return (RecipeComponent[]) recipes.toArray(new RecipeComponent[recipes.size()]);
  }

  /**
   * Adds a new component to this experiment.
   * Currently only two Component Types are accepted:
   * <br><code>SocietyComponent</code>
   * <br><code>RecipeComponent</code>
   *
   * @param comp
   * @exception java.lang.IllegalArgumentException if an error occurs
   */
  public final void addComponent(final ModifiableComponent comp) throws IllegalArgumentException {
    if (comp instanceof SocietyComponent) {
      try {
        addSocietyComponent((SocietyComponent) comp);
      } catch (IllegalArgumentException e) {
        if (log.isErrorEnabled()) {
          log.error("SocietyComponent already set");
        }
      }
    } else if (comp instanceof RecipeComponent) {
      addRecipeComponent((RecipeComponent) comp);
    } else {
      throw new IllegalArgumentException("Unsupported Component Type" + this);
    }
  }

  /**
   * Removes the given component from the Experiment.
   * If the component is not supported, an exception
   * is throw.
   *
   * @param comp
   * @exception java.lang.IllegalArgumentException if an error occurs
   */
  public final void removeComponent(ModifiableComponent comp) throws IllegalArgumentException {
    if (comp instanceof SocietyComponent) {
      removeSocietyComponent();
    } else if (comp instanceof RecipeComponent) {
      removeRecipeComponent((RecipeComponent) comp);
    } else {
      throw new IllegalArgumentException("Unsupported Component Type");
    }
  }

  /**
   * Returns the total number of Components
   * (Recipe and Society) in this experiment.
   *
   * @return an <code>int</code> value
   */
  public final int getComponentCount() {
    return getSocietyComponentCount() + recipes.size();
  }

  /**
   * @return a <code>ModifiableConfigurableComponent[]</code> array of
   * all the components in the experiment
   */
  public final ModifiableComponent[] getComponentsAsArray() {
    List comps = getComponents();
    ModifiableComponent[] compArray =
        (ModifiableComponent[]) comps.toArray(new ModifiableComponent[comps.size()]);
    return compArray;
  }

  /**
   * Get all the recipes and the society.
   * @return List list of all the components in the experiment
   */
  public final List getComponents() {
    List comps = new ArrayList();
    for (int i = 0; i < getRecipeComponentCount(); i++) {
      comps.add(getRecipeComponent(i));
    }
    if (getSocietyComponent() != null)
      comps.add(getSocietyComponent());
    return comps;
  }

  /**
   * Set run in progress.  Used by UI tools to indicate that an
   * experiment is being run.  Note that this is distinct from
   * setting the runnability flag, which indicates whether the experiment
   * can ever be run.
   * @param newRunInProgress
   */
  public final void setRunInProgress(boolean newRunInProgress) {
    runInProgress = newRunInProgress;
  }

  /**
   * Return whether or not experiment is being run.
   * @return boolean whether or not experiment is being run
   */
  public final boolean isRunInProgress() {
    return runInProgress;
  }

  /**
   * Return whether or not experiment is runnable.  An experiment is runnable:
   * if it has a host-node-agent mapping, and
   * it has no unbound properties, and
   * it's not being edited or run, and
   * it's been saved in the database (i.e. the modified flag is false)
   * @return whether or not an experiment is runnable
   */
  public final boolean isRunnable() {
    if (!hasConfiguration() || modified)
      return false;
    return !runInProgress; // allow user to run experiment they're editing
  }

  /**
   * Set edit in progress.  Used by UI tools to indicate that an
   * experiment is being edited.  Note that this is distinct from
   * setting the editability flag, which indicates whether the experiment
   * can ever be edited.
   * @param newEditInProgress
   */
  public final void setEditInProgress(boolean newEditInProgress) {
    editInProgress = newEditInProgress;
  }

  /**
   * Return whether or not experiment is being edited.
   * Note that the experiment may be viewed (but not edited) in an editor,
   * even if this flag is not set.
   * @return boolean whether or not experiment is being edited
   */
  public final boolean isEditInProgress() {
    return editInProgress;
  }

  /**
   * Stop after the current trial.
   * This invokes experimentStopped.
   */
  public final void stop() {
    // TODO: stop the experiment after the current trial
    experimentStopped();
  }

  /**
   * Notify listeners that experiment was terminated.
   */
  public final void experimentStopped() {
    // Tell the Societies in the experiment they are no longer running?
    if (societyComponent != null)
      societyComponent.setRunning(false);
  }

  /**
   * Returns the Default Node Arguments
   *
   * @return a <code>Properties</code> object containing all default Node Args.
   */
  public final Properties getDefaultNodeArguments() {
    return defaultNodeArguments;
  }

  public final File getResultDirectory() {
    return resultDirectory;
  }

  public final void setResultDirectory(File resultDirectory) {
    this.resultDirectory = resultDirectory;
  }

  /**
   * Returns the Name of this Experiment
   *
   * @return a <code>String</code> value
   */
  public final String getExperimentName() {
    return getShortName();
  }

  /**
   * Returns the name of this component
   *
   * @return a <code>String</code> value
   */
  public final String toString() {
    return this.getShortName();
  }

  /**
   * Get the URL of the file that describes this experiment.
   */
  public final URL getDescription() {
    return getClass().getResource(ExperimentBase.DESCRIPTION_RESOURCE_NAME);
  }

  public final void initProperties() {
    // put all the properties in here
    // the list of components are children
    // the editability & runability are properties
    // the hosts, trial, etc should all be properties
    // then we wouldn't need the special copy mechanism
    // (if all the sub-pieces were also components)
  }

  /**
   * If the experiment was saved, let the listeners know,
   * but don't mark our modified flag.
   * If the society or any recipe was changed, tell the listeners that the
   * experiment changed.
   * If the society or recipe was saved, ignore this,
   * as the experiment must still be saved.
   */
  private void notifyExperimentListeners(ModificationEvent e) {
    if (e.getWhatChanged() == ExperimentBase.EXPERIMENT_SAVED) {
      super.fireModification();
      return;
    }
    if (e.getWhatChanged() == SocietyBase.SOCIETY_SAVED ||
        e.getWhatChanged() == RecipeBase.RECIPE_SAVED)
      return;
    // an experiment, society, or recipe was changed,
    // mark this experiment as modified, and notify the listeners
    fireModification();
  }

  public final void fireModification() {
    modified = true;
    super.fireModification();
  }

  private void addDefaultNodeArguments(ComponentData theSoc) {
    Properties props = getDefaultNodeArguments();
    for (Iterator i = props.entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      String entVal = entry.getKey().toString();
      if(entVal.startsWith("-X")) {
        if(entry.getValue().equals("") || entry.getValue() == null) {
          theSoc.addParameter(entry.getKey());
        } else {
          theSoc.addParameter(entry.getKey() + "=" + entry.getValue());
        }
      } else if (entVal.startsWith("X:")) {
        theSoc.addParameter("-X" + entry.getKey() + "=" + entry.getValue());
      } else {
        theSoc.addParameter("-D" + entry.getKey() + "=" + entry.getValue());
      }
    }
  }

  private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    boolean temp = modified;
    createLogger();
    createObserver();
    modified = temp;
    editInProgress = false;
    runInProgress = false;
  }

  // Put a bunch of Prop$ as parameters to the given component.
  // even if the component itself doesnt think it wants it.
  // This may cause problems for Agents, I think....
  private void addPropertiesAsParameters(ComponentData cd, BaseComponent cp) {
    for (Iterator it = cp.getProperties(); it.hasNext();) {
      Property prop = (Property) it.next();
      if (prop != null) {
        Object pvalue = prop.getValue();
        if (pvalue instanceof String)
        // FIXME: by doing getName.last(), it flattens out any
        // internal hierarchy. Surely that makes
        // it impossible for these to be much use?
        // FIXME??
          cd.addParameter(PROP_PREFIX + prop.getName().last() + "=" + pvalue);
      }
    }
  }

  /**
   * Adds the Given Host to the Experiment
   * If the given host already exists, an exception is thrown.
   *
   * @param name - Name of the new Host
   * @return a <code>HostComponent</code> for this host.
   * @exception java.lang.IllegalArgumentException if an error occurs
   */
  public final HostComponent addHost(String name) throws IllegalArgumentException {
    // FIXME: Maybe forbid localhost?
    if (hostExists(name)) {
      throw new IllegalArgumentException("Host Already Exists in Society");
    } else {
      ExperimentHost eh = new ExperimentHost(name);
      eh.initProperties();
      return addHostComponent(eh);
    }
  }

  /**
   * Get the Hosts in this experiment.
   * As a side effect, reconcile the Node/Agent mapping, if the experiment
   * is marked as modified.
   **/
  public final HostComponent[] getHostComponents() {
    if (modified) {
      reconcileNodeAgentMapping();
    }
    return (HostComponent[]) hosts.toArray(new HostComponent[hosts.size()]);
  }

  public final HostComponent[] getHostComponentsNoReconcile() {
    return (HostComponent[]) hosts.toArray(new HostComponent[hosts.size()]);
  }

  /**
   * Removes the given host from the experiment.
   *
   * @param hostComponent - <code>HostComponent</code> to remove.
   */
  public final void removeHost(HostComponent hostComponent) {
    if (!hosts.contains(hostComponent))
      return;
    hosts.remove(hostComponent);
    // Let the host disassociate itself from nodes
    ((ExperimentHost) hostComponent).dispose();
    removeListeners((ExperimentHost) hostComponent);
    fireModification();
  }

  /**
   * Renames a host in the Experiment.
   * If a host with the given name already exists, an exception
   * is thrown.
   *
   * @param hostComponent - <code>HostComponent</code> of the host to be renamed.
   * @param name - New Host Name
   * @exception java.lang.IllegalArgumentException if an error occurs
   */
  public final void renameHost(HostComponent hostComponent, String name)
      throws IllegalArgumentException {
    // Fixme: Maybe forbid localhost?
    ExperimentHost testHost = new ExperimentHost(name);
    if (hosts.contains(testHost)) {
      throw new IllegalArgumentException("Host Already Exists in Society");
    } else {
      if (hostComponent.getFullName().equals(name))
        return;
      hosts.remove(hostComponent);
      hostComponent.setName(name);
      hosts.add(hostComponent);
      fireModification();
    }
  }

  /**
   * Insure that we have a valid nameserver specification. There are
   * several possibilities: If the default node nameserver argument
   * has been set, try to make that be the name server. Parse out the
   * host name part. Then scan all the hosts that have nodes
   * and check to see if any host matches that specified by the
   * default node nameserver argument. If it does, keep that
   * nameserver. If it doesn't (or if there was no default node
   * nameserver argument) then use the first host as the nameserver.
   **/
  public final void updateNameServerHostName() {
    Properties defaultNodeArgs = getDefaultNodeArguments();
    String oldNameServer = defaultNodeArgs.getProperty(Experiment.NAME_SERVER);
    String newNameServer = null;
    String dfltNameServer = null;
    String nameServerHost = null;
    if (oldNameServer != null) {
      dfltNameServer = oldNameServer;
      int colon = oldNameServer.indexOf(':');
      if (colon >= 0) {
        nameServerHost = oldNameServer.substring(0, colon);
      } else {
        nameServerHost = oldNameServer;
      }
    }

    // This does the NodeAgent reconcilation, if the experiment is modified
    HostComponent[] hosts = getHostComponents();

    hostLoop:
      for (int i = 0; i < hosts.length; i++) {
        NodeComponent[] nodes = hosts[i].getNodes();
        if (nodes.length == 0)
          continue;
        String thisHost = hosts[i].getShortName();
        if (thisHost.equals(nameServerHost)) {
          newNameServer = oldNameServer;
          break hostLoop;       // Use existing nameserver definition
        }
        if (dfltNameServer == null) { // First host is default
          dfltNameServer = thisHost + ":" + Experiment.NAME_SERVER_PORTS;
          if (oldNameServer == null) {
            break hostLoop;     // Use dfltNameServer
          }
        }
      }
    if (newNameServer == null) newNameServer = dfltNameServer;

    // set new server in all nodes
    // Do not, again, reconcile NodeAgent mapping
    // This method doesnt care, and if the experiment is modified, getHostComponents
    // above already does it
    NodeComponent[] nodes = getNodesInner(false);
    for (int i = 0; i < nodes.length; i++) {
      NodeComponent node = nodes[i];
      Properties arguments = node.getArguments();
      // Insure no per-node override exists
      arguments.remove(Experiment.NAME_SERVER);
    }
    // Now install experiment-wide setting.
    if (newNameServer != null) {
      defaultNodeArgs.setProperty(Experiment.NAME_SERVER, newNameServer);
    }
  }

  private boolean hostExists(String name) {
    return (hosts.contains(new ExperimentHost(name))) ? true : false;
  }

  private ExperimentHost addHostComponent(ExperimentHost host) {
    if (hosts.contains(host))
      return host;
    hosts.add(host);
    installListeners(host);
    fireModification();
    return host;
  }

  /**
   * Adds a new node to this experiment.  If the node
   * currently exists, an exception is thrown.
   *
   * @param name - Name of new Node
   * @return a <code>NodeComponent</code> value
   * @exception java.lang.IllegalArgumentException if an error occurs
   */
  public final NodeComponent addNode(String name) throws IllegalArgumentException {
    // if have an agent of this name, complain
    if (!agentNameUnique(name)) {
      throw new IllegalArgumentException("Name already in use (by a Node or Agent)");
    } else if (!nodeExists(name)) {
      ExperimentNode result = new ExperimentNode(name, this);
      result.initProperties();
      addNodeComponent(result);
      return result;
    } else {
      throw new IllegalArgumentException("Node already exists");
    }
  }

  /**
   * Removes the specified node from the Experiment
   *
   * @param nc <code>NodeComponent</code> of the Node to remove
   */
  public final void removeNode(NodeComponent nc) {
    if (!nodes.contains(nc))
      return;
    ExperimentNode expNode = (ExperimentNode) nc;
    nodes.remove(nc);
    expNode.dispose();         // Let the node disassociate itself from agents
    removeListeners(expNode);
    fireModification();
  }

  /**
   * Renames a currently existing Node in this Experiment.
   * If a node with the new name already exists, an exception is thrown
   *
   * @param nc - Node Component of the Node to change name
   * @param name - New Name
   * @exception java.lang.IllegalArgumentException if an error occurs
   */
  public final void renameNode(NodeComponent nc, String name) throws IllegalArgumentException {
    if (nodeExists(name)) {
      throw new IllegalArgumentException("Node name already exists!");
    } else if (!agentNameUnique(name)) {
      throw new IllegalArgumentException("Name already in use");
    } else {
      if (nc.getFullName().equals(name))
        return;
      nodes.remove(nc);
      nc.setName(name);
      nodes.add(nc);
      fireModification();
    }
  }

  /**
   * Get the Nodes in this Experiment.
   * As a side-effect, ensure that the NameServer Host property
   * is set correctly, and ensure that the Node/Agent mapping
   * is up-to-date.
   */
  public final NodeComponent[] getNodeComponents() {
    // Note that as a side-effect, this will also
    // reconcile the node/agent mapping, if the experiment is modified
    updateNameServerHostName(); // Be sure this is update-to-date

    // Since we just did the reconciliation if the experiment is modified,
    // only do it here if the experiment is _not_ modified
    // -- I know, shouldnt need to, but this preserves current functionality closer
    return getNodesInner(!isModified());
  }

  private boolean nodeExists(String name) {
    return (nodes.contains(new ExperimentNode(name, this))) ? true : false;
  }

  public final boolean agentNameUnique(String name) {
    if (name == null || name.equals(""))
      return false;

    // is this agent / node name combination unique
    for (Iterator i = nodes.iterator(); i.hasNext();) {
      NodeComponent nc = (NodeComponent) i.next();
      if (nc.getShortName().equals(name))
        return false;
    }
    for (Iterator i = getAgentsList().iterator(); i.hasNext();) {
      AgentComponent nc = (AgentComponent) i.next();
      if (nc.getShortName().equals(name))
        return false;
    }
    return true;
  }

  final void addNodeComponent(ExperimentNode node) {
    if (nodes.contains(node))
      return;
    nodes.add(node);
    installListeners(node);
    fireModification();
  }

  private NodeComponent[] getNodesInner(boolean check) {
//     if (log.isDebugEnabled())
//       log.debug("getNodesInner(" + check + ")");

    if (check)
      reconcileNodeAgentMapping();

    return (NodeComponent[]) nodes.toArray(new NodeComponent[nodes.size()]);
  }

  // Ensure only Agents in the Experiment are assigned
  // to Nodes, and that all Agents assigned to Nodes are the objects
  // given by the society & recipes
  // Note that this takes a little time
  private void reconcileNodeAgentMapping() {
    //     if (log.isDebugEnabled())
    //       log.debug("reconcileNodeAgentMapping", new Throwable());
    if (log.isDebugEnabled())
      log.debug("reconcileNodeAgentMapping");

    List agents = getAgentsList();
    if (nodes == null)
      return;
    for (Iterator i = nodes.iterator(); i.hasNext();) {
      NodeComponent nc = (NodeComponent) i.next();
      AgentComponent[] nodeAgent = nc.getAgents();
      for (int j = 0; j < nodeAgent.length; j++) {
        int index = agents.indexOf(nodeAgent[j]);
        if (index < 0) {
          //	if (!agents.contains(nodeAgent[j]))
          nc.removeAgent(nodeAgent[j]);
        } else {
          // Find the agent in the agents list which is .equals

          // We're never going to find a second Agent with this
          // name on a Node. So no need to keep it in the list
          // of Agents in the Experiment we compare against

          AgentComponent ag = (AgentComponent) agents.remove(index);
          // if its not ==, remove it from the Node, and re-add it
          // to ensure only the correct object in use
          if (ag != null && ag != nodeAgent[j]) {
            nc.removeAgent(nodeAgent[j]);
            nc.addAgent(ag);
          }
        }
      }
    }
  }

  // returns a collection of all agents written.
  private Collection generateNodeComponent(NodeComponent node, ComponentData parent) {
    Set writtenAgents = new HashSet();

    ComponentData nc = new GenericComponentData();
    nc.setType(ComponentData.NODE);
    nc.setName(node.getShortName());
    nc.setClassName(Node.class.getName()); // leave this out?? FIXME
    nc.setOwner(this); // the experiment? FIXME
    nc.setParent(parent);
//     ComponentName name =
//       new ComponentName((ConfigurableComponent) node, "ConfigurationFileName");
    nc.addParameter(node.getShortName());
    Properties props = node.getArguments();
    for (Iterator i = props.entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      String entVal = entry.getKey().toString();
      if(entVal.startsWith("-X")) {
        if(entry.getValue().equals("") || entry.getValue() == null) {
          nc.addParameter(entry.getKey());
        } else {
          nc.addParameter(entry.getKey() + "=" + entry.getValue());
        }
      } else if (entVal.startsWith("X:")) {
        nc.addParameter("-X" + entry.getKey() + "=" + entry.getValue());
      } else {
        nc.addParameter("-D" + entry.getKey() + "=" + entry.getValue());
      }

     // nc.addParameter("-D" + entry.getKey() + "=" + entry.getValue());
    }
    addPropertiesAsParameters(nc, node);
    if (log.isDebugEnabled()) {
      log.debug("Adding: " + nc.getName() + " to " + parent.getName());
    }
    parent.addChild(nc);

    // Now get the Agents on this Node
    AgentComponent[] agents = node.getAgents();
    if (agents != null && agents.length > 0) {
      for (int j = 0; j < agents.length; j++) {
        generateAgentComponent(agents[j], nc, parent.getOwner());
//          if(log.isDebugEnabled()) {
//            log.debug("Remember Agent: " + agents[j].getFullName().toString());
//          }
        writtenAgents.add(agents[j]);
      }
    }
    return writtenAgents;
  }

  public final List getAgentsList() {
    List agents = getAgentsInSociety();
    if (agents == null)
      agents = new ArrayList();
    List other = getAgentsInRecipes();
    if (other != null && (!other.isEmpty()))
      agents.addAll(other);
    return agents;
  }

  public final AgentComponent[] getAgents() {
    List agents = getAgentsList();
    return (AgentComponent[]) agents.toArray(new AgentComponent[agents.size()]);
  }

  /**
   * Get the agents from the society in the experiment.
   */
  private List getAgentsInSociety() {
    List agents = new ArrayList();
    if (societyComponent != null) {
      AgentComponent[] sags = societyComponent.getAgents();
      if (sags != null && sags.length > 0)
        agents.addAll(Arrays.asList(sags));
    }
    return agents;
  }

  private List getAgentsInRecipes() {
    List agents = new ArrayList();
    for (int i = 0; i < recipes.size(); i++) {
      RecipeComponent recipe = (RecipeComponent) recipes.get(i);
      AgentComponent[] impagents = recipe.getAgents();
      if (impagents != null && impagents.length > 0)
        agents.addAll(Arrays.asList(impagents));
    }
    return agents;
  }

  private void generateAgentComponent(AgentComponent agent,
                                      ComponentData parent,
                                      ConfigurableComponent owner) {
    if (log.isDebugEnabled()) {
      log.debug("Adding Agent: " + agent.getFullName().toString() + " To " + parent.getName());
    }

    AgentComponentData ac = new AgentComponentData();
    ac.setName(agent.getShortName());

    // Change AgentComponent to have a getClass?
    if (agent instanceof AgentBase)
      ac.setClassName(((AgentBase) agent).getAgentClassName());
    else
      ac.setClassName(SimpleAgent.class.getName());

    ac.addParameter(agent.getShortName()); // Agents have one parameter, the agent name
    ac.setOwner(owner);
    ac.setParent(parent);

    // FIXME: This is the lines that forces all the $Prop things
    // on the agents to be stored!!!
    //addPropertiesAsParameters(ac, agent);
    parent.addChild(ac);
  }

  // Generate a host-node-agent ComponentData tree in the global
  // completeSociety variable
  // This saves all assignments of agents to Nodes, including
  // those nodes not assigned to Hosts.
  // It also gathers the command-line arguments
  final void generateHNACDATA() {
    Set savedNodes = new HashSet();
    Set savedAgents = new HashSet();

    // getNodeComponents calls getNodesInner which should
    // reconcile old Agents & new Agents
    updateNameServerHostName();
    // Force a reconciliation, once, even if the Experiment says it is not
    // modified -- just to be safe
    NodeComponent[] nodesToWrite = getNodesInner(!isModified());
    AgentComponent[] agentsToWrite = getAgents();

    if (log.isDebugEnabled())
      log.debug("genHNA has " + agentsToWrite.length + " Agents to write");

    completeSociety = new GenericComponentData();

    completeSociety.setType(ComponentData.SOCIETY);
    completeSociety.setName(getExperimentName()); // this should be experiment: trial FIXME
    completeSociety.setClassName("java.lang.Object"); // Must not be null
    completeSociety.setOwner(this); // the experiment
    completeSociety.setParent(null);
    addDefaultNodeArguments(completeSociety);

    // For each host, add it to the society, and recurse for each of its nodes
    for (Iterator iter = hosts.iterator(); iter.hasNext();) {
      ExperimentHost host = (ExperimentHost) iter.next();
      ComponentData hc = new GenericComponentData();
      if (log.isDebugEnabled()) {
        log.debug("Processing Host: " + host.getShortName());
      }
      hc.setType(ComponentData.HOST);
      hc.setName(host.getShortName());
      hc.setClassName("");
      hc.setOwner(this);
      hc.setParent(completeSociety);
      addPropertiesAsParameters(hc, host);
      completeSociety.addChild(hc);
      NodeComponent[] nodes = host.getNodes();
      for (int j = 0; j < nodes.length; j++) {
        savedAgents.addAll(generateNodeComponent(nodes[j], hc));
        savedNodes.add(nodes[j]);
      }
    }

//     if (log.isDebugEnabled())
//       log.debug("genHNA: After adding assigned nodes, # saved Agents: " + savedAgents.size());

    // For each un-assigned Node, add it, and recurse for the Agents
    for (int i = 0; i < nodesToWrite.length; i++) {
      if (savedNodes.contains(nodesToWrite[i])) continue;
      savedAgents.addAll(generateNodeComponent(nodesToWrite[i], completeSociety));
      savedNodes.add(nodesToWrite[i]);
    }

//     if (log.isDebugEnabled())
//       log.debug("genHNA: After adding UN-assigned nodes, # saved Agents: " + savedAgents.size() + ", agentsToWrite is now: " + agentsToWrite.length);

    // For each unassigned Agent, add it
    for (int i = 0; i < agentsToWrite.length; i++) {
      if (savedAgents.contains(agentsToWrite[i])) {
        if (log.isDebugEnabled()) {
          log.debug("genHNA Already wrote Agent: " + agentsToWrite[i].getShortName());
        }
        continue;
      } else {

        // print out the FullName().toString() of all the savedAgents.

        if (log.isDebugEnabled()) {
// 	  log.debug("genHNA: Apparently haven't yet saved " + agentsToWrite[i]);
// 	  int j = 0;
// 	  for (Iterator ags = savedAgents.iterator(); ags.hasNext(); j++) {
// 	    AgentComponent ag = (AgentComponent)ags.next();
// 	    log.debug("savedAgent[" + j + "]: " + ag.getFullName().toString());
// 	  }

          log.debug("genHNA Writing Agent[" + i + "]: " + agentsToWrite[i].getFullName().toString());
        }
        generateAgentComponent(agentsToWrite[i], completeSociety, this);
        savedAgents.add(agentsToWrite[i]);
      }
    } // end of loop over AgentsToWrite, to catch unassigned Agents

    // Uncomment for very verbose output setting up HNA mapping
//     if (log.isDebugEnabled())
//       checkHNA(completeSociety, getAgents());
  }

  private void checkHNA(ComponentData data, AgentComponent[] agents) {
    // Look for each of the Agents in the Experiment && make
    // sure I can find them, and print out their parent.
    if (data == null) {
      log.debug("checkHNA got null root");
      return;
    }
    ComponentData[] kids = data.getChildren();
    for (int i = 0; i < kids.length; i++) {
      if (kids[i].getType().equals(ComponentData.AGENT)) {
        boolean foundkid = false;
        log.debug("checkHNA found Agent child " + kids[i] + " with parent " + data.getName());
        for (int j = 0; j < agents.length; j++) {
          if (agents[j] == null) {
            log.debug("checkHNA: someone already matched the " + j + " agent");
            continue;
          }
          log.debug("checkHNA looking to see if we found agent " + agents[j].getShortName());
          if (agents[j].getShortName().equals(kids[i].getName())) {
            log.debug("checkHNA!!! found a match for AgentComp " + agents[j].getShortName() + " with full name " + agents[j].getFullName());
            agents[j] = null;
            foundkid = true;
            break;
          }
        } // loop over agents

        // Make sure I found a real Agent for this ComponentData
        if (!foundkid) {
          log.debug("checkHNA!!!! didnt find a real AgentComp to match AgentData " + kids[i] + " in parent " + data.getName());
        }
      } else
        checkHNA(kids[i], agents);
    }

    // See if I found all of the Agents in the Experiment
    if (data == completeSociety)
      for (int i = 0; i < agents.length; i++) {
        if (agents[i] != null) {
          log.debug("checkHNA!!!! Never found componentData for Agent: " + agents[i].getShortName() + " with full name " + agents[i].getFullName());
        }
      }
  }

  // Use the previously inited host-node-agent ComponentData tree
  // in the variable 'completeSociety'
  // And loop through all components in the Experiment,
  // asking them to add their ComponentData
  // return the or'ed value of componentWasRemoved()
  final boolean askComponentsToAddCDATA() {
    // Now ask each component in turn to add its stuff
    boolean componentWasRemoved = false;

    BaseComponent theSoc = getSocietyComponent();
    if (log.isDebugEnabled()) {
      log.debug(theSoc.getFullName().toString() + ".addComponentData");
    }
    theSoc.addComponentData(completeSociety);
    componentWasRemoved |= theSoc.componentWasRemoved();

    for (int i = 0, n = recipes.size(); i < n; i++) {
      BaseComponent soc = (BaseComponent) recipes.get(i);

      if (log.isDebugEnabled()) {
        log.debug(soc.getFullName().toString() + ".addComponentData");
      }
      // Warning: This is a no-op in general
      // for recipes that use RecipeBase. AgentInsertion and ABCImpact
      // use it though. Basically, you can only use
      // it if you don't need a DB query to know what to do.
      soc.addComponentData(completeSociety);

//       if (log.isDebugEnabled())
// 	log.debug("askToAdd: now complete is: " + completeSociety);


      // Components can notice here if they had to remove
      // a component (or, I suppose, modify)
      componentWasRemoved |= soc.componentWasRemoved();
      // Note that no current component returns true here
    }
    return componentWasRemoved;
  }

  public final void writeContents(String filename, OutputStream out) {
    createConfigWriter();
    try {
      configWriter.writeFile(filename, out);
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Exception: ", e);
      }
    }
  }

  /**
   * An Experiment now has a configuration writer that
   * lets all the components write themselves out
   */
  final void createConfigWriter() {
    if (configWriter == null) {
      configWriter = new LeafOnlyConfigWriter(getSocietyComponentData());
    }
  }

  /**
   * If the experiment has at least one host with at least one node
   * with at least one agent, that is a configuration.
   *
   * @return a <code>boolean</code> true if it has a configured agent
   */
  public final boolean hasConfiguration() {
    if (hosts.isEmpty() || nodes.isEmpty() || getAgents() == null || getAgents().length == 0) {
      return false;
    }
    HostComponent[] hosts = getHostComponents();
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
  public final void createDefaultConfiguration() {
    // Check if it already has a node?
    // create one Node
    NodeComponent node = null;

    // BIG HACK IN HERE!!!!!!
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
    } catch (java.net.UnknownHostException e) {
    }
    HostComponent host = addHost(localhost);

    // put the one node on that host
    host.addNode(node);
    fireModification();
  }

  /**
   * Returns whether or not this experiment has been modified
   * since it was saved to the database.
   * @return true if experiment has been modified
   */
  public final boolean isModified() {
    return modified;
  }

  /**
   * Indicate that the experiment is up-to-date with respect to the database.
   * Use with caution!  The only reason to reset this flag
   * is that when an experiment is created from the database, its components
   * are built-up from the database information, and thus the experiment
   * appears to be modified.
   */
  public final void resetModified() {
    modified = false;
    // tell listeners experiment is now saved
    fireModification(new ModificationEvent(this, ExperimentBase.EXPERIMENT_SAVED));
  }

  /**
   * Sets the current Trial ID.
   *
   * @param trialID - New Trial ID
   */
  final void setTrialID(String trialID) {
    if (this.trialID == trialID)
      return;
    this.trialID = trialID;
    defaultNodeArguments.setReadOnlyProperty(EXPERIMENT_ID, trialID);
    fireModification();
  }

  public final String getTrialID() {
    if (DBUtils.dbMode) {
      return trialID;
    } else {
      return null;
    }
  }

  /**
   * Sets the Experiment ID for this Experiment.
   *
   * @param expID
   */
  public final void setExperimentID(String expID) {
    if (this.expID == expID)
      return;
    this.expID = expID;
    fireModification();
  }

  public final String getExperimentID() {
    if (DBUtils.dbMode) {
      return expID;
    } else {
      return null;
    }
  }

  /**
   * get the local variable idea of the community assembly for this
   * experiment, possibly null
   */
  public final String getCommAsbID() {
    return commAsb;
  }

  /**
   * (Re) set the local variables idea of the community assembly for
   * this experiment, possibly to null
   */
  public final void setCommAsbID(String id) {
    this.commAsb = id;
  }

  /**
   * Imports a HNA XML file.  The file is imported into
   * a <code>ComponentData</code> structure which is then
   * traversed and applied to the current experiment.
   *
   * @param parent - Parent GUI Component
   */
  public final void importHNA(Component parent) {
    File resultDir = getResultDirectory();
    String path = ".";
    if (resultDir != null)
      path = resultDir.getAbsolutePath();
    JFileChooser chooser = new JFileChooser(path);
    chooser.setDialogTitle("Select HNA Export file to apply");
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
      public boolean accept(final File f) {
        return (f.isDirectory() || (f.isFile()) && f.canRead() &&
            (f.getName().endsWith("xml") || f.getName().endsWith("XML")));
      }

      public String getDescription() {
        return "HNA XML Files";
      }
    });
    File file = null;
    while (file == null) {
      int result = chooser.showDialog(parent, "OK");
      if (result != JFileChooser.APPROVE_OPTION)
        return;
      file = chooser.getSelectedFile();
      if (file != null && (!file.canRead() || file.isDirectory()))
        file = null;
    }

    if (log.isInfoEnabled()) {
      log.info("importing HNA file " + file.getAbsolutePath());
    }

    // FIXME: Put up a dialog indicating the file being imported?
    // Bug 1765

    ComponentData mapping = null;
    ExperimentXML parser = new ExperimentXML();
    mapping = parser.parseExperimentFile(file);
    if (mapping == null) {
      if (log.isErrorEnabled()) {
        log.error("Error parsing file: " + file.getName());
      }
      // Display an error dialog here!
      return;
    }

    if (mapping.getType().equals(ComponentData.SOCIETY)) {
      addChildren(mapping.getChildren());
    } else {
      if (log.isErrorEnabled()) {
        log.error("Didn't start with a society!");
      }
    }
  }

  /**
   * Adds new children to the experiment components
   * based on the NHA mappings loaded in from a file.
   *
   * @param children - Child Component to re-map.
   */
  private void addChildren(final ComponentData[] children) {
    if (children == null)
      return;
    for (int i = 0; i < children.length; i++) {
      final ComponentData child = children[i];
      if (child == null)
        continue;
      if (child.getType().equals(ComponentData.HOST)) {
        // Add host.
        HostComponent host = null;
        try {
          host = addHost(child.getName());
        } catch (IllegalArgumentException e) {
          // Host already exists.
          final HostComponent[] allHosts = getHostComponents();
          if (allHosts == null)
            continue;
          for (int x = 0; x < allHosts.length; x++) {
            if (allHosts[x].getShortName().equals(child.getName())) {
              host = allHosts[x];
              break;
            }
          }
          if (log.isDebugEnabled()) {
            log.debug("Host already exists: " + child.getName());
          }
        }
        if (host != null)
          addHostChildren(child.getChildren(), host);
      } else if (child.getType().equals(ComponentData.NODE)) {
        // Add node.
        NodeComponent node = null;
        try {
          node = addNode(child.getName());
        } catch (IllegalArgumentException e) {
          // Node already exists.
          final NodeComponent[] allNodes = getNodeComponents();
          if (allNodes == null)
            continue;
          for (int x = 0; x < allNodes.length; x++) {
            if (allNodes[x].getShortName().equals(child.getName())) {
              node = allNodes[x];
              break;
            }
          }
          if (log.isDebugEnabled()) {
            log.debug("Node already exists: " + child.getName());
          }
        }
        if (node != null)
          addNodeChildren(child.getChildren(), node);
      } else if (child.getType().equals(ComponentData.AGENT)) {
        // Ignore.
      } else {
        if (log.isWarnEnabled()) {
          log.warn("Unknown Type: " + child.getType());
        }
      }
    }
  }

  /**
   * Adds all new children to the HostComponent
   *
   * @param children All children of the new HostComponent.
   * @param host - HostComponent to add children to.
   */
  private void addHostChildren(ComponentData[] children, HostComponent host) {
    if (children == null)
      return;
    if (host == null)
      return;
    for (int i = 0; i < children.length; i++) {
      boolean foundInHost = false;
      boolean foundInSociety = false;
      ComponentData child = children[i];
      if (child == null)
        return;
      NodeComponent expNode = null;
      NodeComponent[] allNodes = getNodeComponents();
      if (child.getType().equals(ComponentData.NODE)) {
        if (allNodes != null) {
          for (int k = 0; k < allNodes.length; k++) {
            if (allNodes[k] == null)
              continue;
            if (child.getName().equals(allNodes[k].getShortName())) {
              foundInSociety = true;
              expNode = allNodes[k];
              break;
            }
          }
        }
        NodeComponent[] crntNodes = host.getNodes();
        if (crntNodes != null) {
          for (int j = 0; j < crntNodes.length; j++) {
            if (crntNodes[j] == null)
              continue;
            if (child.getName().equals(crntNodes[j].getShortName())) {
              foundInHost = true;
              break;
            }
          }
        }
        if (!foundInHost) {
          // See if in Exp.
          if (foundInSociety) {
            expNode = host.addNode(expNode);
          } else {
            expNode = host.addNode(addNode(child.getName()));
          }
        }
        if (expNode != null)
          addNodeChildren(child.getChildren(), expNode);
      } else {
        if (log.isWarnEnabled()) {
          log.warn("Unknown child of host: " + child.getName());
        }
      }
    }
  }

  /**
   * Adds all children to the Node.
   *
   * @param children All new children for the node.
   * @param node Node to add children to.
   */
  private void addNodeChildren(ComponentData[] children, NodeComponent node) {
    if (node == null)
      return;
    if (children == null)
      return;

    for (int i = 0; i < children.length; i++) {
      boolean foundInSociety = false;
      boolean foundInNode = false;
      ComponentData child = children[i];
      if (child == null)
        continue;
      AgentComponent[] allAgents = getAgents();
      AgentComponent addAgent = null;
      if (child.getType().equals(ComponentData.AGENT)) {
        if (allAgents != null) {
          for (int k = 0; k < allAgents.length; k++) {
            if (allAgents[k] == null)
              continue;
            if (child.getName().equals(allAgents[k].getShortName())) {
              foundInSociety = true;
              addAgent = allAgents[k];
              break;
            }
          }
        }

        if (foundInSociety) {
          AgentComponent[] crntAgents = node.getAgents();
          if (crntAgents != null) {
            for (int j = 0; j < crntAgents.length; j++) {
              if (crntAgents[j] == null)
                continue;
              if (child.getName().equals(crntAgents[j].getShortName())) {
                foundInNode = true;
                break;
              }
            }
          }
          if (!foundInNode) {
            if (addAgent == null) {
              // this shouldnt happen
            } else {
              // FIXME: This test not quite adequate - must
              // check that agent name not same as any
              // other Node or Agent name
              if (node.getShortName().equalsIgnoreCase(addAgent.getShortName())) {
                if (log.isWarnEnabled()) {
                  log.warn("Agent name same as node, cannot perform addition");
                }
                JOptionPane.showMessageDialog(null, "Cannot Map Agent to Node of Same name, ignoring agent: " + addAgent.getShortName());
              } else {
                node.addAgent(addAgent);
              }
            }
          }
        } else {
          JOptionPane.showMessageDialog(null, "Cannot Map Unknown Agent: " + child.getName());
          if (log.isWarnEnabled()) {
            log.warn("Agent: " + child.getName() + " not known.  Add aborted");
          }
        }
      } else {
        if (log.isWarnEnabled()) {
          log.warn("Unknown child of node: " + child.getName());
        }
      }
    }
  }

  public abstract void save(DBConflictHandler ch);

  protected abstract void setDefaultNodeArguments();

  final class MyModificationListener implements ModificationListener, ConfigurableComponentListener {
    // tell listeners on experiment that experiment was modified
    public void modified(ModificationEvent e) {
      notifyExperimentListeners(e);
    }
  }
}

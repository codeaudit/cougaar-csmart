/* 
 * <copyright>
 * Copyright 2003 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).

 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.tools.csmart.experiment;

import org.cougaar.core.node.Node;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.db.DBConflictHandler;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.awt.*;

/**
 * An Experiment, is a complete Host, Node, Agent, Component mapping
 * containing all required runtime variables and components.
 * A completed Experiment can be handed off to a COUGAAR node and
 * run, with no additional parameters required.
 *
 */
public interface Experiment extends ModifiableComponent {
  String EXPERIMENT_ID = "org.cougaar.experiment.id";
  String PERSISTENCE_ENABLE = "org.cougaar.core.persistence.enable";
  String PERSIST_CLEAR = "org.cougaar.core.persistence.clear";
  String TIMEZONE = "user.timezone";
  String AGENT_STARTTIME = "org.cougaar.core.agent.startTime";
  String COMPLAININGLP_LEVEL = "org.cougaar.planning.ldm.lps.ComplainingLP.level";
  // Used by AppServer
  String CONTROL_PORT = "org.cougaar.control.port";

  String ENV_DISPLAY = "env.DISPLAY";
  String BOOTSTRAP_CLASS = "java.class.name";
  String NAME_SERVER = "org.cougaar.name.server";
  String NODE_NAME = "org.cougaar.node.name";
  /** When true, if CSMART dies, the society does not die **/
  String AS_SWALLOW_ERRORS = "org.cougaar.tools.server.swallowOutputConnectionException";
  /** Which initialization method should we use. DB means the CSMART DB **/
  String INITIALIZER_PROP = Node.INITIALIZER_PROP;

  // Default values:
  String PERSISTENCE_DFLT = "false";
  String PERSIST_CLEAR_DFLT = "true";
  String TIMEZONE_DFLT = "GMT";
  String AGENT_STARTTIME_DFLT = "08/10/2005 00:05:00";
  String COMPLAININGLP_LEVEL_DFLT = "0";

  /** org.cougaar.control.port; port for contacting applications server **/
  int APP_SERVER_DEFAULT_PORT = 8484;
  String NAME_SERVER_PORTS = "8888:5555";
  String DEFAULT_BOOTSTRAP_CLASS = "org.cougaar.bootstrap.Bootstrapper";
  String DEFAULT_NODE_CLASS = "org.cougaar.core.node.Node";
  String AS_SWALLOW_ERRORS_DFLT = "true";
  String PROP_PREFIX = "PROP$";

  /**
   * Adds a society, as a <code>SocietyComponent</code> to the experiment.  A society contains
   * a complete Node/Agent/Component mapping.
   * Each experiment can only contain one Society.  If a society already exists, an exception is
   * thrown.
   * @param sc The new society
   * @throws IllegalArgumentException Exception is thrown if the experiment already contains a society.
   */
  void addSocietyComponent(SocietyComponent sc) throws IllegalArgumentException;

  /**
   * Removes the society component from the experiment.
   */
  void removeSocietyComponent();

  /**
   * Returns a count of all societies in the experiment.  Currently, the
   * experiment can only have 1 society.
   * @return count of societies within the experiment.
   */
  int getSocietyComponentCount();

  /**
   * Gets the society component as a <code>SocietyCompoent</code> for this experiment.
   * @return society component from this experiment.  Or null, if one does not exist.
   */
  SocietyComponent getSocietyComponent();

  /**
   * Sets the recipe components for this experiment.
   * @param newRecipes array of <code>RecipeComponents</code> to apply to this experiment.
   */
  void setRecipeComponents(RecipeComponent[] newRecipes);

  /**
   * Adds a specific recipe component to this experiment.  If the recipe already exists in
   * the experiment, an exception is thrown
   * @param recipe New recipe component to add to the experiment.
   * @throws IllegalArgumentException Exception is thrown if the experiment already contains this recipe.
   */
  void addRecipeComponent(RecipeComponent recipe) throws IllegalArgumentException;

  /**
   * Removed a specific recipe from the experiment.
   * @param recipe The recipe to remove from the experiment.
   */
  void removeRecipeComponent(RecipeComponent recipe);

  /**
   * Returns a count of the number of recipe components within this experiment.
   * @return count of all recipe components.
   */
  int getRecipeComponentCount();

  /**
   * Gets a specific <code>RecipeComponent</code> specified by the index it was stored.
   * @param i - index of the recipe to retrieve.
   * @return A <code>RecipeComponent</code> of the recipe found at the specified index.
   * @throws IndexOutOfBoundsException Exception thrown if index out of bounds occurs.
   */
  RecipeComponent getRecipeComponent(int i) throws IndexOutOfBoundsException;

  /**
   * Gets an array of all <code>RecipeComponents</code> in this experiment.
   * @return array of all recipe components.
   */
  RecipeComponent[] getRecipeComponents();

  /**
   * Adds a component to the experiment.  If the component is a society, and
   * the experiment already contains a society, an exception is thrown.  If the
   * component is not a <code>SocietyComponent</code> or a <code>RecipeComponent</code>
   * an exception is thrown.
   * @param comp Component to add to the experiment.
   * @throws IllegalArgumentException Expception if component known, or already exists.
   */
  void addComponent(ModifiableComponent comp) throws IllegalArgumentException;

  /**
   * Removes a specific component, specified as a <code>ModifiableComponent</code>.
   * An exception is thrown if the Component to be removed is not a <code>SocietyComponent</code>
   * or a <code>RecipeComponent</code>
   * @param comp Component to remove from the experiment.
   * @throws IllegalArgumentException Exception if the component is of unknown type.
   */
  void removeComponent(ModifiableComponent comp) throws IllegalArgumentException;

  /**
   * Returns a count of all components in the experiment.
   * @return count of all components.
   */
  int getComponentCount();

  /**
   * Returns all components on the experiment as a <code>ModifiableComponent</code> array.
   * @return array of all components.
   */
  ModifiableComponent[] getComponentsAsArray();

  /**
   * Returns a <code>List</code> of all components in the experiment.
   * @return All components
   */
  List getComponents();

  /**
   * Set the experiment running flag to true.
   * @param newRunInProgress  true if experiment is currently running, else false
   */
  void setRunInProgress(boolean newRunInProgress);

  /**
   * Indicates if the current experiment is running.
   * @return true if the current experiment is running, else false.
   */
  boolean isRunInProgress();

  /**
   * Indicates that this experiment contains everything it needs to run.
   * @return true if the experiment is ready to run, else false
   */
  boolean isRunnable();

  /**
   * Set the experiment currently being edited flag.
   * @param newEditInProgress true if the experiment is currently being edited, else false
   */
  void setEditInProgress(boolean newEditInProgress);

  /**
   * Indicates if the experiment is currently being edited.
   * @return true if the experiment is being edited, else false
   */
  boolean isEditInProgress();

  /**
   * Stops the currently running experiment.
   */
  void stop();

  /**
   * Stops the currently running experiment.
   */
  void experimentStopped();

  /**
   * Gets all default node arguments.
   * @return <code>Properties of all default node arguments.</code>
   */
  Properties getDefaultNodeArguments();

  /**
   * Returns the results directory used by this experiment to store all collected results.
   * @return <code>File</code> location to store results.
   */
  File getResultDirectory();

  /**
   * Sets the directory to store all experiment results.
   * @param resultDirectory <code>File</code> location to store results.
   */
  void setResultDirectory(File resultDirectory);

  /**
   * Gets the current experiment name.
   * @return Experiment name as a <code>String</code>
   */
  String getExperimentName();


  String toString();

  /**
   * Copies this component, and returns a copy with the name specified.
   * @param uniqueName name of the new Component.
   * @return new <code>ModifiableComponent</code> copy of this component.
   */
  ModifiableComponent copy(String uniqueName);

  /**
   * Gets the <code>URL</code> location of the description of this component.
   * @return <code>URL</code> of description file.
   */
  URL getDescription();

  /**
   * Initializes the properties of this component.
   */
  void initProperties();

  /**
   * Gets the complete society component data specified as a <code>ComponentData</code> object.
   * @return <code>ComponentData</code> of the complete society.
   */
  ComponentData getSocietyComponentData();

  /**
   * Adds a new host to this experiment.  An exception is thrown
   * if the experiment already contains a host of the same name.
   * @param name Name of the new host.
   * @return <code>HostComponent</code> of the new host.
   * @throws IllegalArgumentException Exception if the host is already defined in the experiment.
   */
  HostComponent addHost(String name) throws IllegalArgumentException;

  /**
   * Returns an array of <code>HostComponent</code>s that are part of this experiment.
   * @return Array of all hosts.
   */
  HostComponent[] getHostComponents();

  HostComponent[] getHostComponentsNoReconcile();

  /**
   * Removes the specified host from the experiment.
   * @param hostComponent Host to remove from the experiment.
   */
  void removeHost(HostComponent hostComponent);

  void renameHost(HostComponent hostComponent, String name)
      throws IllegalArgumentException;

  void updateNameServerHostName();

  /**
   * Adds a node to the experiment.  If the experiment already contains a node
   * of the same name, an exception is thrown.
   * @param name Name of the new node.
   * @return <code>NodeComponent</code> for the new node.
   * @throws IllegalArgumentException Exception if the experiment already contains the node.
   */
  NodeComponent addNode(String name) throws IllegalArgumentException;

  /**
   * Removes the specified node from the experiment.
   * @param nc Node to remove from the experiment.
   */
  void removeNode(NodeComponent nc);

  /**
   * Renames a node, in the <code>NodeComponent</code> to the new given name
   * @param nc <code>NodeComponent</code> to be renamed.
   * @param name New name of the node.
   * @throws IllegalArgumentException
   */
  void renameNode(NodeComponent nc, String name) throws IllegalArgumentException;

  /**
   * Gets an <code>NodeComponent</code> array of all nodes in this experiment.
   * @return An array of all nodes in the experiment.
   */
  NodeComponent[] getNodeComponents();

  /**
   * Determines if the given agent name is unique within this experiment.
   * @param name Name of agent to check for uniqueness
   * @return true if the agent is unique, else false.
   */
  boolean agentNameUnique(String name);

  /**
   * Returns a <code>List</code> of all the agents in this experiment.
   * @return a <code>List</code> of all agents
   */
  List getAgentsList();

  /**
   * Gets an <code>AgentComponent</code> array of all agents in this experiment.
   * @return An array of all agents in the experiment.
   */
  AgentComponent[] getAgents();

  /**
   * Dumps INI files for the current experiment.
   */
  void dumpINIFiles();

  /**
   * Dumps an HNA mapping.
   */
  void dumpHNA();

  boolean hasConfiguration();

  void createDefaultConfiguration();

  /**
   * Indicates if the current experiment has been modified.
   * @return true if the current experiment has been modified, else false
   */
  boolean isModified();

  /**
   * Resets the modified flag.
   */
  void resetModified();

  /**
   * Gets the trial id for this experiment, if one exists.
   *
   * @return current trial Id, or null if one doesn't exist.
   */
  public String getTrialID();

  /**
   * get the local variable idea of the community assembly for this
   * experiment, possibly null
   *
   * @return
   */
  public String getCommAsbID();

  /**
   * (Re) set the local variables idea of the community assembly for
   * this experiment, possibly to null
   */
  public void setCommAsbID(String id);

  /**
   * Sets the name of this Experiment.
   * @param name Name of this experiment
   */
  void setName(String name);

  /**
   * Sets the Experiment Id for this experiment.
   *
   * @param expID current experiment id
   */
  public void setExperimentID(String expID);

  /**
   * Experiment ID is used by database societies only.
   *
   * @return The ExperimentID, if one exists, else null.
   */
  public String getExperimentID();

  /**
   * Imports an HNA mapping into the experiment.  This
   * method is only relevant for database experiments.  Experiments
   * from XML already contain their own HNA mapping.
   *
   * @param parent Component containing the HNA mapping.
   */
  public void importHNA(Component parent);

  /**
   * Saves the current Experiment.  If the Experiment Instance is
   * a database instance <code>DBExperiment</code>.  The experiment is
   * saved to the database.
   * Currently, Saving of an XMLExperiment is not implemented.
   * @param handler Database conflict handler.  This handler is only
   * used for <code>DBExperiment</code> instances.  All others
   * ignore it.
   */
  public void save(DBConflictHandler handler);

}

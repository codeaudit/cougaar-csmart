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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.IndexOutOfBoundsException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.cougaar.core.agent.ClusterImpl;
import org.cougaar.core.node.Node;
import org.cougaar.util.Parameters;
import org.cougaar.util.log.Logger;

import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.db.DBConflictHandler;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.core.db.CMT;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentListener;
import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.core.property.ModificationListener;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.name.ComponentName;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.experiment.ExperimentHost;
import org.cougaar.tools.csmart.recipe.RecipeBase;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyBase;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.file.SocietyFileComponent;
import org.cougaar.tools.csmart.society.ui.SocietyUIComponent;
import org.cougaar.tools.csmart.ui.console.CSMARTConsole;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.viewer.SocietyFinder;
import org.cougaar.tools.csmart.util.ReadOnlyProperties;

/**
 * A CSMART Experiment. Holds the components being run, and the configuration of host/node/agents.<br>
 * Computes the trials, and the configuration data for running a trial.
 */
public class Experiment extends ModifiableConfigurableComponent implements java.io.Serializable {

  // Define some Node Args
  public static final String EXPERIMENT_ID = "org.cougaar.experiment.id";
  public static final String PERSISTENCE_ENABLE = "org.cougaar.core.persistence.enable";
  public static final String PERSIST_CLEAR = "org.cougaar.core.pesistence.clear";
  public static final String TIMEZONE = "user.timezone";
  public static final String AGENT_STARTTIME = "org.cougaar.agent.startTime";
  public static final String COMPLAININGLP_LEVEL = 
    "org.cougaar.planning.ldm.lps.ComplainingLP.level";  
  public static final String TRANSPORT_ASPECTS = "org.cougaar.message.transport.aspects";
  public static final String CONTROL_PORT = "org.cougaar.control.port";
  public static final String CONFIG_DATABASE = "org.cougaar.configuration.database";
  public static final String CONFIG_USER = "org.cougaar.configuration.user";
  public static final String CONFIG_PASSWD = "org.cougaar.configuration.password";
  public static final String ENV_DISPLAY = "env.DISPLAY";

  public static final String BOOTSTRAP_CLASS = "java.class.name";
  public static final String NAME_SERVER = "org.cougaar.name.server";
  public static final String NODE_NAME = "org.cougaar.node.name";

  
  // Define some Defaults
  public static final String PERSISTENCE_DFLT = "false";
  public static final String PERSIST_CLEAR_DFLT = "true";
  public static final String TIMEZONE_DFLT = "GMT";
  public static final String AGENT_STARTTIME_DFLT = "08/10/2005";
  public static final String COMPLAININGLP_LEVEL_DFLT = "0";
  public static final String TRANSPORT_ASPECTS_DFLT = 
    "org.cougaar.core.mts.StatisticsAspect";
  // org.cougaar.control.port; port for contacting applications server
  public static final int APP_SERVER_DEFAULT_PORT = 8484;
  public static final String NAME_SERVER_PORTS = "8888:5555";
  public static final String DEFAULT_BOOTSTRAP_CLASS = "org.cougaar.bootstrap.Bootstrapper";
  public static final String DEFAULT_NODE_CLASS = "org.cougaar.core.node.Node";

  private static final String DESCRIPTION_RESOURCE_NAME = "description.html";
  public static final String PROP_PREFIX = "PROP$";

  // Member Variables
  private SocietyComponent societyComponent = null;
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
  //  private boolean overrideEditable = false;
  //  private boolean cloned = false;
  private transient boolean editInProgress = false;
  private transient boolean runInProgress = false;

  // The assembly holding community info for this Experiment.
  private String commAsb = null;

  private String expID = null; // An Experiment has a single ExpID
  private String trialID = null;
  // Soon Experiment's Trials will have TrialIDs

  private ComponentData completeSociety = null;

  // modification event
  private static final int EXPERIMENT_SAVED = 1;

  private transient LeafOnlyConfigWriter configWriter = null;

  private transient Logger log;
  // Mark whether the experiment has been modified
  // and should be saved
  private boolean modified = true;

  ////////////////////////////////////////////
  // Constructors
  ////////////////////////////////////////////

  // Organizer uses this to create a new experiment based on a society/recipe
  // or from the UI
  public Experiment(String name, SocietyComponent societyComponent,
		    RecipeComponent[] recipes)
  {
    this(name);
    init();
    setSocietyComponent(societyComponent);
    if (recipes != null)
      setRecipeComponents(recipes);

    // Give it a new empty Comm ASB:
    PopulateDb pdb = null;
    try {
      pdb = new PopulateDb(null, null);
      commAsb = pdb.getNewCommAssembly();
    } catch (SQLException sqle) {
      log.error("constructor failed to get new comm ASB", sqle);
    } catch (IOException ie) {
      log.error("constructor failed to get new comm ASB", ie);
    } finally {
      try {
	if (pdb != null)
	  pdb.close();
      } catch (SQLException se) {}
    }
  }

  // Used in copy when not in DB mode
  public Experiment(String name) {
    super(name);
    init();
  }

  // Used in copy when in DB mode or on load from DB
  public Experiment(String name, String expID, String trialID) {
    super(name);
    this.expID = expID;
    this.trialID = trialID;
    init();
  }

  ////////////////////////////////////////////
  // Private Operations.
  ////////////////////////////////////////////
  
  private void init() {
    createLogger();
    setDefaultNodeArguments();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  // add an observer to my arguments
  // if these arguments are modified, then
  // notify listeners on the experiment that it's modified
  private transient Observer myObserver = null;

  private void createObserver() {
    if (myObserver == null) {
      myObserver = new Observer() {
        public void update(Observable o, Object arg) {
          Experiment.this.fireModification();
        }
      };
      defaultNodeArguments.addObserver(myObserver);
    }
  }

  private void setDefaultNodeArguments() {
    defaultNodeArguments = 
      new ReadOnlyProperties(Collections.singleton(EXPERIMENT_ID));
    createObserver();
    defaultNodeArguments.put(PERSISTENCE_ENABLE, PERSISTENCE_DFLT);
    // By default we clear any existing persistence deltas when we
    // start a run.
    defaultNodeArguments.put(PERSIST_CLEAR, PERSIST_CLEAR_DFLT);
    defaultNodeArguments.put(TIMEZONE, TIMEZONE_DFLT);
    defaultNodeArguments.put(AGENT_STARTTIME, AGENT_STARTTIME_DFLT);
    defaultNodeArguments.put(COMPLAININGLP_LEVEL, COMPLAININGLP_LEVEL_DFLT);
    defaultNodeArguments.put(TRANSPORT_ASPECTS, TRANSPORT_ASPECTS_DFLT);
    defaultNodeArguments.put(CONTROL_PORT, Integer.toString(APP_SERVER_DEFAULT_PORT));
    // Class of Node to run. This is the first argument to the BOOTSTRAP_CLASS
    // below.
    defaultNodeArguments.put(CSMARTConsole.COMMAND_ARGUMENTS, DEFAULT_NODE_CLASS);

    // Class of bootstrapper to use. The actual class being executed
    defaultNodeArguments.put(BOOTSTRAP_CLASS, DEFAULT_BOOTSTRAP_CLASS);

    if (DBUtils.dbMode) {
      defaultNodeArguments.put(CONFIG_DATABASE, 
			       Parameters.findParameter(DBUtils.DATABASE));
      defaultNodeArguments.put(CONFIG_USER, 
			       Parameters.findParameter(DBUtils.USER));
      defaultNodeArguments.put(CONFIG_PASSWD, 
			       Parameters.findParameter(DBUtils.PASSWORD));
      if (getTrialID() != null)
        defaultNodeArguments.setReadOnlyProperty(EXPERIMENT_ID, getTrialID());
    }
    try {
      defaultNodeArguments.put(ENV_DISPLAY, 
                               InetAddress.getLocalHost().getHostName() + ":0.0");
    } catch (UnknownHostException uhe) {
      if(log.isErrorEnabled()) {
        log.error("UnknownHost Exception", uhe);
      }
    }
  }

  /**
   * Determines if any results exist for this Experiment/Trial
   *
   * @return a <code>boolean</code> value
   */
  private boolean hasResults() {
    Trial[] trials = getTrials();
    for (int i = 0; i < trials.length; i++) {
      TrialResult[] results = trials[i].getTrialResults();
      if (results.length != 0) 
        return true;
    }
    return false;
  }


  ////////////////////////////////////////////
  // Society Component Operations.
  ////////////////////////////////////////////

  // Public Methods

  /**
   * Adds a <code>societyComponent</code> to this Experiment.
   *
   * @param sc 
   * @exception IllegalArgumentException if an error occurs
   */
  public void addSocietyComponent(SocietyComponent sc) throws IllegalArgumentException {
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
  public void removeSocietyComponent() {
    if (this.societyComponent == null)
      return;
    removeListeners((ModifiableConfigurableComponent)societyComponent);
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
  public int getSocietyComponentCount() {
    return (this.societyComponent == null ? 0 : 1);
  }

  /**
   * Return the <code>SocietyComponent</code>
   *
   * @return a <code>SocietyComponent</code> value
   */
  public SocietyComponent getSocietyComponent() {
    return this.societyComponent;
  }

  // Private Methods

  private void setSocietyComponent(SocietyComponent society) {
    if (this.societyComponent != null)
      removeSocietyComponent();
    this.societyComponent = society;
    installListeners((ModifiableConfigurableComponent)society);
    fireModification();
  }

  private void installListeners(ModifiableConfigurableComponent component) {
    component.addModificationListener(myModificationListener);
  }

  private void removeListeners(ModifiableConfigurableComponent component) {
    component.removeModificationListener(myModificationListener);
  }

  ////////////////////////////////////////////
  // Recipe Component Operations.
  ////////////////////////////////////////////

  /**
   * Adds an array of <code>RecipeComponent</code>s to this
   * experiment.
   *
   * @param ary RecipeComponents
   */
  public void setRecipeComponents(RecipeComponent[] newRecipes) {
    for (int i = 0; i < recipes.size(); i++)
      removeListeners((ModifiableConfigurableComponent)recipes.get(i));
    recipes.clear();
    // FIXME: Remove duplicates?
    for (int i = 0; i < newRecipes.length; i++)
      installListeners((ModifiableConfigurableComponent)newRecipes[i]);
    recipes.addAll(Arrays.asList(newRecipes));
    fireModification();
  }


  /**
   * Adds a <code>RecipeComponent</code> to this Experiment
   *
   * @param recipe - <code>RecipeComponent</code> to add to Experiment
   */
  public void addRecipeComponent(RecipeComponent recipe) throws IllegalArgumentException {
    if (!recipes.contains(recipe)) {
      recipes.add(recipe);
      installListeners((ModifiableConfigurableComponent)recipe);
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
  public void removeRecipeComponent(RecipeComponent recipe) {
    if (! this.recipes.contains(recipe))
      return;
    this.recipes.remove(recipe);
    removeListeners((ModifiableConfigurableComponent)recipe);
    fireModification();
  }

  /**
   * Returns the total number of recipes in this experiment.
   *
   * @return an <code>int</code> value
   */
  public int getRecipeComponentCount() {
    return this.recipes.size();
  }

  /**
   * Gets a specific recipe based on Index.
   *
   * @param i 
   * @return a <code>RecipeComponent</code> value
   * @exception IndexOutOfBoundsException if an error occurs
   */
  public RecipeComponent getRecipeComponent(int i) throws IndexOutOfBoundsException {
    return (RecipeComponent) recipes.get(i);
  }

  /**
   * Gets all <code>RecipeComponents</code> for this experiment.
   *
   * @return a <code>RecipeComponent[]</code> value
   */
  public RecipeComponent[] getRecipeComponents() {
    return (RecipeComponent[])recipes.toArray(new RecipeComponent[recipes.size()]);
  }

  ////////////////////////////////////////////
  // Generic Component Operations.
  ////////////////////////////////////////////

  // Public Methods

  /**
   * Adds a new component to this experiment.
   * Currently only two Component Types are accepted:
   * <br><code>SocietyComponent</code>
   * <br><code>RecipeComponent</code>
   *
   * @param comp
   * @exception IllegalArgumentException if an error occurs
   */
  public void addComponent(ModifiableComponent comp) throws IllegalArgumentException {
    if (comp instanceof SocietyComponent) {
      try {
        addSocietyComponent((SocietyComponent)comp);
      } catch (IllegalArgumentException e) {
        if(log.isErrorEnabled()) {
          log.error("SocietyComponent already set");
        }
      }
    } else if (comp instanceof RecipeComponent) {
      addRecipeComponent((RecipeComponent)comp);
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
   * @exception IllegalArgumentException if an error occurs
   */
  public void removeComponent(ModifiableComponent comp) throws IllegalArgumentException {
    if (comp instanceof SocietyComponent) {
      removeSocietyComponent();
    } else if (comp instanceof RecipeComponent) {
      removeRecipeComponent((RecipeComponent)comp);
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
  public int getComponentCount() {
    return getSocietyComponentCount() + recipes.size();
  }

  /**
   * @return a <code>ModifiableConfigurableComponent[]</code> array of 
   * all the components in the experiment
   */
  public ModifiableComponent[] getComponentsAsArray() {
    List comps = getComponents();
    ModifiableComponent[] compArray = 
      (ModifiableComponent[])comps.toArray(new ModifiableComponent[comps.size()]);
    return compArray;
  }


  /**
   * Get all the recipes and the society.
   * @return List list of all the components in the experiment
   */
  public List getComponents() {
    List comps = new ArrayList();
    for (int i = 0; i < getRecipeComponentCount(); i++) {
      comps.add(getRecipeComponent(i));
    }
    if (getSocietyComponent() != null)
      comps.add(getSocietyComponent());
    return comps;
  }

  ////////////////////////////////////////////
  // Component Runnable Operations.
  ////////////////////////////////////////////

  /**
   * Set run in progress.  Used by UI tools to indicate that an
   * experiment is being run.  Note that this is distinct from
   * setting the runnability flag, which indicates whether the experiment
   * can ever be run.
   * @param newRunInProgress 
   */
  public void setRunInProgress(boolean newRunInProgress) {
    runInProgress = newRunInProgress;
  }

  /**
   * Return whether or not experiment is being run.
   * @return boolean whether or not experiment is being run
   */
  public boolean isRunInProgress() {
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
  public boolean isRunnable() {
    if (!hasConfiguration() || hasUnboundProperties() || modified)
      return false;
    return !runInProgress; // allow user to run experiment they're editing
  }


  ////////////////////////////////////////////
  // Component Editable Operations.
  ////////////////////////////////////////////

  /**
   * Set edit in progress.  Used by UI tools to indicate that an
   * experiment is being edited.  Note that this is distinct from
   * setting the editability flag, which indicates whether the experiment
   * can ever be edited.
   * @param newEditInProgress 
   */
  public void setEditInProgress(boolean newEditInProgress) {
    editInProgress = newEditInProgress;
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
   * Returns true if experiment is editable: 
   * it has no trial results, and
   * it's not being edited or run
   * The first condition can be overridden using the setEditable method.
   * @return true if experiment is editable, false otherwise
   */
//    public boolean isEditable() {
//      if (editInProgress)
//        return false;
//      if (overrideEditable)
//        return true;
//      else
//        return !hasResults();
//    }

  /**
   * Make an experiment editable even if
   * it has experiment results.
   */
//    public void setEditable(boolean editable) {
//      if (editable == overrideEditable) return; // no change
//      overrideEditable = editable;
//      fireModification();
//    }


  ////////////////////////////////////////////
  //  Trial Operations.
  ////////////////////////////////////////////

  /**
   * Sets the current Trial ID.
   *
   * @param trialID - New Trial ID
   */
  public void setTrialID(String trialID) {
    if (this.trialID == trialID)
      return;
    this.trialID = trialID;
    defaultNodeArguments.setReadOnlyProperty(EXPERIMENT_ID, trialID);
    fireModification();
  }

  public String getTrialID() {
    if (DBUtils.dbMode) {
      return trialID;
    } else {
      return null;
    }
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
    // Get Society Components.
    ModifiableComponent comp = getSocietyComponent();
    List propertyNames = null;
    if (comp != null) {
       for (Iterator j = comp.getProperties(); j.hasNext(); ) {
        Property property = (Property)j.next();
        List values = property.getExperimentValues();
        if (values != null && values.size() != 0) {
          properties.add(property);
          experimentValues.add(values);
        }
      }
    }

    // Get Recipe Components.
    int n = getRecipeComponentCount();
    for (int i = 0; i < n; i++) {
      comp = getRecipeComponent(i);
       for (Iterator j = comp.getProperties(); j.hasNext(); ) {
	Property property = (Property)j.next();
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

    // Get Society Components
    ModifiableComponent comp = getSocietyComponent();
    if (comp == null)
      return 1; // always assume one trial
    Iterator props = comp.getProperties();
    while (props.hasNext()) {
      Property property = (Property)props.next();
      List values = property.getExperimentValues();
      if (values != null)
        experimentValueCounts.add(new Integer(values.size()));
    }

    // Get Recipe Components
    int n = getRecipeComponentCount();
    for (int i = 0; i < n; i++) {
      comp = getRecipeComponent(i);
      props = comp.getProperties();
      while (props.hasNext()) {
        Property property = (Property)props.next();
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

  // Private Methods

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


  ////////////////////////////////////////////
  // Basic Operations.
  ////////////////////////////////////////////

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
    if (societyComponent != null)
      societyComponent.setRunning(false);
  }

  /**
   * Returns the Default Node Arguments
   * 
   * @return a <code>Properties</code> object containing all default Node Args.
   */
  public Properties getDefaultNodeArguments() {
    return defaultNodeArguments;
  }

  /**
   * Sets the Experiment ID for this Experiment.
   *
   * @param expID 
   */
  public void setExperimentID(String expID) {
    if (this.expID == expID)
      return;
    this.expID = expID;
    fireModification();
  }

  public String getExperimentID() {
    if (DBUtils.dbMode) {
      return expID;
    } else {
      return null;
    }
  }

  public File getResultDirectory() {
    return resultDirectory;
  }

  public void setResultDirectory(File resultDirectory) {
    this.resultDirectory = resultDirectory;
  }

  /**
   * Sets the Name of this Component.
   *
   * @param newName - New Component Name
   */
  public void setName(String newName) {
    // If name is same, don't fire modification
    if(getExperimentName().equals(newName) || newName == null || newName.equals(""))
      return;

    if (log.isDebugEnabled()) {
      log.debug("Change expt name from " + getExperimentName() + " to " + newName);
    }
    super.setName(newName);

    // If the experiment isnt otherwise modified, and we can save
    // to the DB, then do so, and dont mark the experiment
    // as modified.
    if (! modified && expID != null) {
      try {
	PopulateDb.changeExptName(expID, newName);
      } catch (Exception e) {
	if (log.isErrorEnabled()) {
	  log.error("setName: error setting name in DB", e);
	}
      } finally {
	return;
      }
    }

    fireModification();
  }

  /**
   * Returns the Name of this Experiment
   *
   * @return a <code>String</code> value
   */
  public String getExperimentName() {
    return getShortName();
  }

  /**
   * get the local variable idea of the community assembly for this
   * experiment, possibly null
   */
  public String getCommAsbID() {
    return commAsb;
  }

  /**
   * (Re) set the local variables idea of the community assembly for 
   * this experiment, possibly to null
   */
  public void setCommAsbID(String id) {
    this.commAsb = id;
  }

  /**
   * Returns the name of this component
   *
   * @return a <code>String</code> value
   */
  public String toString() {
    return this.getShortName();
  }

  /**
   * Return a deep copy of the experiment.
   * Called when an experiment is selected in the organizer.
   * Add experiments and society to workspace.
   * @return the copy of the experiment.
   */
  public ModifiableComponent copy(String uniqueName) {
    if (log.isDebugEnabled()) {
      log.debug("Experiment copying " + getExperimentName() + " into new name " + uniqueName);
    }
    Experiment experimentCopy = null;
    if (DBUtils.dbMode) 
      experimentCopy = new Experiment(uniqueName, expID, trialID);
    else
      experimentCopy = new Experiment(uniqueName);

    // FIXME: Unset the experiment and trial IDs? I think so...
    experimentCopy.expID = null;
    experimentCopy.trialID = null;

    Properties newProps = experimentCopy.getDefaultNodeArguments();
    newProps.clear();
    newProps.putAll(getDefaultNodeArguments());

//     // Copy the society component
//     ModifiableComponent society = getSocietyComponent();
//     if (society != null) {
//       ModifiableComponent copiedSocietyComponent = null;

//       // FIXME: USe a name based on the original society!!
//       //      copiedSocietyComponent = society.copy("Society for " + uniqueName);
//       copiedSocietyComponent = society.copy(society.getShortName() + " copy");
//       if (copiedSocietyComponent != null) {
//         // FIXME: Must save the copied society to the DB? I think the 
// 	// society does it for me...
//         experimentCopy.addComponent(copiedSocietyComponent);
//       }
//     }

    // Don't copy the society.
    experimentCopy.addComponent(getSocietyComponent());
    
    // Copy the recipe components
    for (int i = 0; i < getRecipeComponentCount(); i++) {
      ModifiableComponent mc = getRecipeComponent(i);
//       ModifiableComponent copiedComponent = null;
//       // FIXME: USe a name based on the original recipe!!
//       //      copiedComponent = mc.copy("Recipe for " + uniqueName);
//       copiedComponent = mc.copy(mc.getShortName() + " copy");
//       if (copiedComponent != null) {
//         experimentCopy.addComponent(copiedComponent);
//       }

      // Don't copy recipes either
      experimentCopy.addComponent(mc);
    }

    // copy hosts
    HostComponent[] hosts = getHostComponents();
    HostComponent[] nhosts = new HostComponent[hosts.length];
    for (int i = 0; i < hosts.length; i++) {
      // FIXME: instead to nhosts[i] = hosts[i].copy();
      // and then don't need the following 2 set calls hopefully
      nhosts[i] = experimentCopy.addHost(hosts[i].getShortName().toString());
      nhosts[i].setServerPort(hosts[i].getServerPort());
      nhosts[i].setMonitoringPort(hosts[i].getMonitoringPort());
      //      hosts[i].copy(nhosts[i]);
      // FIXME: What about other Host fields? OS, etc?
      // -- answer - see above fixme
    }
    // copy nodes
    NodeComponent[] nodes = getNodeComponents();
    NodeComponent[] nnodes = new NodeComponent[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      nnodes[i] = ((ExperimentNode)nodes[i]).copy(experimentCopy);
      experimentCopy.addNodeComponent((ExperimentNode)nnodes[i]);

      // FIXME: What about other Node fields?
      // answer -- see comment in ExperimentNode.copy
    }

    // reconcile hosts-nodes-agents
    AgentComponent[] nagents = experimentCopy.getAgents();
    NodeComponent nnode = null;
    ArrayList nNodesCopied = new ArrayList();
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

	// Record which Nodes we've (potentially) copied the Agents for
	nNodesCopied.add(nnode);
      } // loop over old old nodes on original Host
    } // loop over original hosts

    // FIXME: What about if orig Experiment had Agents assigned to a Node
    // that was not assigned to a host - is that mapping lost?
    // I think so. So we need to maybe keep track of which Nodes we updated just now
    for (int i = 0; i < nnodes.length; i++) {
      if (! nNodesCopied.contains(nnodes[i])) {
	// We may need to copy the old node-agent mapping onto this new node
	AgentComponent[] oagents = nodes[i].getAgents();
// 	if (log.isDebugEnabled() && oagents.length > 0) {
// 	  log.debug("copy: Found node whose Agent mapping may not have been copied: " + nnodes[i] + " and old Node " + nodes[i]);
// 	}
	for (int j = 0; j < oagents.length; j++) {
// 	  if (log.isDebugEnabled()) {
// 	    log.debug("Looking at Agent " + oagents[j].getShortName() + " on old Node");
// 	  }
	  for (int k = 0; k < nagents.length; k++) {
	    if (nagents[k].getShortName().equals(oagents[j].getShortName())) {
	      if (log.isDebugEnabled()) {
		log.debug("copy: Putting new Agent " + nagents[k].getShortName() + " on new Node " + nnodes[i].getShortName());
	      }
	      nnodes[i].addAgent(nagents[k]);
	    } // Found Agent match
	  } // loop over new agents
	} // loop over old agents on old Node
      } // block to deal with uncopied Node
    } // loop over new nodes
    // copy the results directory; results from the copied experiment
    // will be stored in this directory under the copied experiment name
    experimentCopy.setResultDirectory(getResultDirectory());
    

    // Fixme: Grab comm info from orig expt & resave under new asbID
    // and attach that new ID to this Expt
    PopulateDb pdb = null;
    String newAsbid = null;
    try {
      pdb = new PopulateDb(expID, trialID);
      newAsbid = pdb.getNewCommAsbFromExpt(expID, trialID);
    } catch (SQLException sqle) {
      log.error("Expt Copy error copying community info", sqle);
    } catch (IOException ioe) {
      log.error("Expt Copy error copying community info", ioe);
    } finally {
      try {
	if (pdb != null)
	  pdb.close();
      } catch (SQLException se) {}
    }
    if (log.isDebugEnabled()) {
      log.debug("copy: new Expt gets Comm ASB " + newAsbid);
    }
    experimentCopy.setCommAsbID(newAsbid);

    // FIXME: Must duplicate the expt_trial_thread information
    // Else must recognize & fix problem on load in Organizer / CMTDialog

    // FIXME: Only do this sometimes maybe?
    experimentCopy.storeSelectedThreads(getSelectedThreads());

    return experimentCopy;
  }

  // Retrieve from the DB the threads selected in this experiment.
  // Do not store on the local experiment, but instead return a list
  // which will be stored on the copy
  private List getSelectedThreads() {
    if (trialID == null || getSocietyComponent() == null)
      return null;
    List threads = new ArrayList();

    for (int i = 0; i < CMT.ULDBThreads.length; i++) {
      if (ExperimentDB.isULThreadSelected(trialID, CMT.ULDBThreads[i]))
	threads.add(CMT.ULDBThreads[i]);
    }
    if (threads.isEmpty())
      return null;
    else 
      return threads;
  }

  // List of the CMT style threads selected for this Experiment
  // This should be non-null only when we copied the experiment
  // and have not yet saved it.
  private List selThreads = null;

  // Stash away the list of CMT style threads seleced in this experiment
  public void storeSelectedThreads(List threads) {
    this.selThreads = threads;
  }
  
  // If there are any locally storead Threads, save them in DB
  // if we can.
  private void saveSelectedThreads() {
    if (selThreads == null || selThreads.isEmpty() || expID == null || expID.equals("") || trialID == null || trialID.equals(""))
      return;
    
    for (int i = 0; i < CMT.ULDBThreads.length; i++)
      ExperimentDB.setULThreadSelected(trialID, CMT.ULDBThreads[i], selThreads.contains(CMT.ULDBThreads[i]));
    selThreads = null;
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

  /**
   * Get component data for the society in the experiment.
   * If the experiment is not in the database, then it
   * constructs the society component data here.
   * @return ComponentData the component data for the society
   */
  public ComponentData getSocietyComponentData() {
    if (modified)
      saveToDb(); // if modified, update component data and save to database
    
    // If the exp/trialID is null and we have recipes, we must do a save
    // Otherwise recipes wont correctly add their data
    // do this within generateCompleteSociety

    if (completeSociety == null)
      generateCompleteSociety();
    
    return completeSociety;
  }

  /**
   * Create a modification listener, which is registered on
   * all the experiment components. If any components
   * are modified, then mark this experiment as modified,
   * and fire a modification event to the experiment's
   * modification listeners.
   * Checks for modification event being EXPERIMENT_SAVED
   * in which case, it does not mark the experiment modified.
   */
//    ModificationListener myModificationListener = 
//      new ModificationListener() {
//          public void modified(ModificationEvent e) {
//            // tell listeners on experiment that experiment was modified
//            notifyExperimentListeners(e);
//          }
//        };

  ModificationListener myModificationListener = new MyModificationListener();

  /**
   * If the experiment was saved, let the listeners know,
   * but don't mark our modified flag.
   * If the society or any recipe was changed, tell the listeners that the
   * experiment changed.
   * If the society or recipe was saved, ignore this,
   * as the experiment must still be saved.
   */
  private void notifyExperimentListeners(ModificationEvent e) {
    if (e.getWhatChanged() == EXPERIMENT_SAVED) {
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

  public void fireModification() {
    modified = true;
    super.fireModification();
  }

  // Private Methods

  private void addDefaultNodeArguments(ComponentData theSoc) {
    Properties props = getDefaultNodeArguments();
    for (Iterator i = props.entrySet().iterator(); i.hasNext(); ) {
      Map.Entry entry = (Map.Entry) i.next();
      theSoc.addParameter("-D" + entry.getKey() + "=" + entry.getValue());
    }
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    boolean temp = modified;
    createLogger();
    createObserver();
    // these are reinstalled in ConfigurableComponent
    // reinstall listeners
//      if (societyComponent != null)
//        installListeners((ModifiableConfigurableComponent)societyComponent);
//      RecipeComponent[] recipes = getRecipeComponents();
//      for (int i = 0; i < recipes.length; i++)
//        installListeners((ModifiableConfigurableComponent)recipes[i]);
//      HostComponent[] hostComponents = getHostComponents();
//      for (int i = 0; i < hostComponents.length; i++)
//        installListeners((ModifiableConfigurableComponent)hostComponents[i]);
//      NodeComponent[] nodeComponents = getNodeComponents();
//      for (int i = 0; i < nodeComponents.length; i++)
//        installListeners((ModifiableConfigurableComponent)nodeComponents[i]);
    modified = temp;
    editInProgress = false;
    runInProgress = false;
  }

  // Put a bunch of Prop$ as parameters to the given component.
  // even if the component itself doesnt think it wants it.
  // This may cause problems for Agents, I think....
  private void addPropertiesAsParameters(ComponentData cd, BaseComponent cp)
  {
    for (Iterator it = cp.getProperties(); it.hasNext(); ) {
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

  ////////////////////////////////////////////
  // Host Component Operations.
  ////////////////////////////////////////////

  // Public Methods

  /**
   * Adds the Given Host to the Experiment
   * If the given host already exists, an exception is thrown.
   *
   * @param name - Name of the new Host
   * @return a <code>HostComponent</code> for this host.
   * @exception IllegalArgumentException if an error occurs
   */
  public HostComponent addHost(String name) throws IllegalArgumentException {
    // FIXME: Maybe forbid localhost?
    if(hostExists(name)) {
      throw new IllegalArgumentException("Host Already Exists in Society");
    } else {
      ExperimentHost eh = new ExperimentHost(name);
      eh.initProperties();
      return addHostComponent(eh);
    }
  }

  public HostComponent[] getHostComponents() {
    if(modified) {
      getNodesInner();
    }
    return (HostComponent[]) hosts.toArray(new HostComponent[hosts.size()]);
  }

  /**
   * Removes the given host from the experiment.
   *
   * @param hostComponent - <code>HostComponent</code> to remove.
   */
  public void removeHost(HostComponent hostComponent) {
    if (! hosts.contains(hostComponent))
      return;
    hosts.remove(hostComponent);
    // Let the host disassociate itself from nodes
    ((ExperimentHost)hostComponent).dispose();           
    removeListeners((ExperimentHost)hostComponent);
    fireModification();
  }

  /**
   * Renames a host in the Experiment.
   * If a host with the given name already exists, an exception
   * is thrown.
   *
   * @param hostComponent - <code>HostComponent</code> of the host to be renamed.
   * @param name - New Host Name
   * @exception IllegalArgumentException if an error occurs
   */
  public void renameHost(HostComponent hostComponent, String name) 
    throws IllegalArgumentException {
    // Fixme: Maybe forbid localhost?
    ExperimentHost testHost = new ExperimentHost(name);
    if(hosts.contains(testHost)) {
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
  public void updateNameServerHostName() {
    Properties defaultNodeArgs = getDefaultNodeArguments();
    String oldNameServer = defaultNodeArgs.getProperty(NAME_SERVER);
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
        dfltNameServer = thisHost + ":" + NAME_SERVER_PORTS;
        if (oldNameServer == null) {
          break hostLoop;     // Use dfltNameServer
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
      arguments.remove(NAME_SERVER);
    }
    // Now install experiment-wide setting.
    if (newNameServer != null) {
      defaultNodeArgs.setProperty(NAME_SERVER, newNameServer);
    }
  }


  // Private Methods

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

  ////////////////////////////////////////////
  // Node Component Operations.
  ////////////////////////////////////////////

  // Public 

  /**
   * Adds a new node to this experiment.  If the node
   * currently exists, an exception is thrown.
   *
   * @param name - Name of new Node
   * @return a <code>NodeComponent</code> value
   * @exception IllegalArgumentException if an error occurs
   */
  public NodeComponent addNode(String name) throws IllegalArgumentException {
    // if have an agent of this name, complain
    if (! agentNameUnique(name)) {
      throw new IllegalArgumentException("Name already in use (by a Node or Agent)");
    } else if(!nodeExists(name)) {
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
  public void removeNode(NodeComponent nc) {
    if (! nodes.contains(nc))
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
   * @exception IllegalArgumentException if an error occurs
   */
  public void renameNode(NodeComponent nc, String name) throws IllegalArgumentException {
    if(nodeExists(name)) {
      throw new IllegalArgumentException("Node name already exists!");
    } else if (! agentNameUnique(name)) {
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
   * Reconcile agents in nodes with agents in society,
   * so that if a society has been reconfigured such that
   * it no longer contains some agents, those agents are also
   * removed from the nodes.
   */
  public NodeComponent[] getNodeComponents() {
    NodeComponent[] result = getNodesInner();
    updateNameServerHostName(); // Be sure this is update-to-date
    return result;
  }

  private boolean nodeExists(String name) {
    return (nodes.contains(new ExperimentNode(name, this))) ? true : false;
  }

  public boolean agentNameUnique(String name) {
    if (name == null || name.equals(""))
      return false;

    // is this agent / node name combination unique
    for (Iterator i = nodes.iterator(); i.hasNext(); ) {
      NodeComponent nc = (NodeComponent) i.next();
      if (nc.getShortName().equals(name))
	return false;
    }
    for (Iterator i = getAgentsList().iterator(); i.hasNext(); ) {
      AgentComponent nc = (AgentComponent) i.next();
      if (nc.getShortName().equals(name))
	return false;
    }
    return true;
  }

  private void addNodeComponent(ExperimentNode node) {
    if (nodes.contains(node))
      return;
    nodes.add(node);
    installListeners(node);
    fireModification();
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

  // returns a collection of all agents written.
  private Collection generateNodeComponent(NodeComponent node, ComponentData parent) {
    Set writtenAgents = new HashSet();

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
    if(log.isDebugEnabled()) {
      log.debug("Adding: " + nc.getName() + " to " + parent.getName());
    }
    parent.addChild(nc);
    AgentComponent[] agents = node.getAgents();
    if (agents != null && agents.length > 0) {
      for (int j = 0; j < agents.length; j++) {
        generateAgentComponent(agents[j], nc, parent.getOwner());
//         if(log.isDebugEnabled()) {
//           log.debug("Remember Agent: " + agents[j].getShortName());
//         }
        writtenAgents.add(agents[j]);
      }
    }
    return writtenAgents;
  }
  

  ////////////////////////////////////////////
  // Agent Component Operations.
  ////////////////////////////////////////////

  // Public

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

  // Private 

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

  private void generateAgentComponent(AgentComponent agent, 
                             ComponentData parent, 
                             ConfigurableComponent owner) {
    if(log.isDebugEnabled()) {
      log.debug("Adding Agent: " + agent.getShortName() + " To " + parent.getName());
    }

    AgentComponentData ac = new AgentComponentData();
    ac.setName(agent.getShortName());
    // Change AgentComponent to have a getClass?
    ac.setClassName(ClusterImpl.class.getName());
    ac.addParameter(agent.getShortName()); // Agents have one parameter, the agent name
    ac.setOwner(owner);
    ac.setParent(parent);

    // FIXME: This is the lines that forces all the $Prop things
    // on the agents to be stored!!!
    //addPropertiesAsParameters(ac, agent);
    parent.addChild((ComponentData)ac);
  }


  ////////////////////////////////////////////
  // Database Specific Operations
  ////////////////////////////////////////////
  
  /**
   * Save the experiment to the database, regardless of
   * whether or not it's been modified.
   * To avoid useless saves, the caller should check the modified
   * flag with isModified.  This method specifically does not check
   * the modified flag to allow the user to force saving the experiment.
   * If the experiment has been modified, save it to the database.
   * This creates the full ComponentData tree and then uses
   * <code>PopulateDb</code> to save it to the database
   *
   * @param ch a <code>DBConflictHandler</code> to graphically handle conflicts
   * @see org.cougaar.tools.csmart.core.db.PopulateDb
   */
  public void saveToDb(DBConflictHandler ch) {
    PopulateDb pdb = null;

    // Make sure we have the Community Assembly stored locally
    if (commAsb == null) {
      if (log.isDebugEnabled()) {
	log.debug("saveToDb had no CommAsb for expt " + expID);
      }
      try {
	// retrieve from db
	pdb = new PopulateDb(expID, trialID);
	// set it locally
	commAsb = pdb.getCommAsbForExpt(expID, trialID);
	if (commAsb == null) {
	  if (log.isDebugEnabled()) {
	    log.debug("saveToDb found no comm asb in DB either");
	  }
	  commAsb = pdb.getNewCommAssembly();
	}
      } catch (SQLException sqle) {
	log.error("saveToDb error getting Comm assembly", sqle);
      } catch (IOException ioe) {
	log.error("saveToDb error getting Comm assembly", ioe);
      } finally {
	try {
	  if (pdb != null)
	    pdb.close();
	} catch (SQLException se) {}
	pdb = null;
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("saveToDb has commAsb " + commAsb + " for expt " + expID);
    }

    try {
      if (log.isInfoEnabled()) {
	log.info("Saving experiment " + getExperimentName() + " to database");
      }
      updateNameServerHostName(); // Be sure this is up-to-date
      List components = getComponents();

      boolean componentWasRemoved = false;

      SocietyComponent sc = getSocietyComponent();
      if (sc == null) {
        if (log.isErrorEnabled()) 
          log.error("Experiment has no society; cannot save it to database.");
        return;
      }

      if (sc.getAssemblyId() == null) {
	// This should never happen!
	if (log.isErrorEnabled()) {
	  log.error("Saving experiment " + getExperimentName() + " with exptID " + getExperimentID() + " containing society " + sc.getSocietyName() + " but society has now Assembly! Caller trace: ", new Throwable());
	}

	// Try to save the society that somehow
	// was not previously saved
	if (! sc.saveToDatabase()) {
	  if (log.isErrorEnabled()) {
	    log.error("Failed to save society " + sc.getSocietyName() + " so going to give up saving the experiment.");
	  }
	  return;
	}
      } else if (sc.isModified()) {
	// Save the society before trying to save the Experiment
	if (! sc.saveToDatabase()) {
	  if (log.isErrorEnabled()) {
	    log.error("Failed to save society " + sc.getSocietyName() + " so going to give up saving the experiment.");
	  }
	  return;
	}
      }	

      pdb =
	new PopulateDb("CMT", "CSHNA", "CSMI", getExperimentName(),
		       getExperimentID(), trialID, ch, sc.getAssemblyId());

      setExperimentID(pdb.getExperimentId());
      setTrialID(pdb.getTrialId()); // sets trial id and -D argument

      // Save back to DB the selected threads, if necc
      saveSelectedThreads();

      // store in the DB the community assembly ID being used
      pdb.setCommAssemblyId(commAsb);

      // Some components will want access to the complete set of Nodes
      // in the society, etc. To get that, they must get back to the
      // root soc object, and do a getOwner and go from there. Ugly.


      // So far we've just created the pdb (which
      // sets up the Experimet & Trial IDs, and puts the CMT assembly
      // in the right place if necc, and clears out the other 
      // assemblies (CSMI, CSHNA)

      // Gather the HostNodeAgent stuff
      generateHNACDATA();
      if(log.isErrorEnabled() && completeSociety == null) {
        log.error("save: Society Data is null!");
      }

      // Problem: Societies from files store _tons_ of properties
      // on the Agents. This is most of the society definition, in fact.
      // It appears that these end up changing (somehow),
      // between saves. So this can throw that IllegalArgumentException
      // FIXME!!!!
      // Save what we have so far in the CSHNA assembly
      if (log.isDebugEnabled()) {
	log.debug("About to save HNA data");
      }
      pdb.populateHNA(completeSociety);

      // Now let components add in their pieces
      componentWasRemoved = askComponentsToAddCDATA();

      // If any component removed something, we need
      // to recreate the CMT assembly (under the original ID)
      // So this, currently destroys the orig CMT
      // Note also it does not effect Agent asset data
      // or relationships or OPLAN stuff
      if (componentWasRemoved) {
	if (log.isDebugEnabled()) {
	  log.debug("save: After adding CDATA, got a removed.");
	}
	pdb.populateCSA(completeSociety);
      } else {
	if (log.isDebugEnabled()) {
	  log.debug("save: Done adding CDATA, ready to modify.");
	}
      }

      // then give everyone a chance to modify what they've collectively produced
      for (int i = 0, n = components.size(); i < n; i++) {
        BaseComponent soc = (BaseComponent) components.get(i);
        soc.modifyComponentData(completeSociety, pdb);
        if (soc.componentWasRemoved()) {
	  if (log.isDebugEnabled()) {
	    log.debug("save: After modifying CDATA by comp " + soc.getShortName() + ", got a removed.");
	  }
          pdb.populateCSA(completeSociety);
        }

	try {
	  // Incrementally save
	  // so that later recipes have the benefit
	  // of the earlier modifications
	  if (log.isDebugEnabled()) {
	    log.debug("About to save CSMI data, having just done mod in " + soc.getShortName());
	  }
	  pdb.populateCSMI(completeSociety);
	} catch (IllegalArgumentException iae) {
	  if (log.isInfoEnabled()) {
	    log.info("Caught iae " + iae.toString() + " while saving modifications. Must redo as a CSA");
	  }
	  pdb.populateCSA(completeSociety);
	}
      } // end of loop over components to do mod
      pdb.setModRecipes(recipes);

      // Now make sure the runtime/ config time assemblies
      // are all correct
      if (log.isDebugEnabled()) {
	log.debug("About to do fix assemblies");
      }
      if (! pdb.fixAssemblies()) {
	// Some sort of error trying to ensure the assemblies are all correct.
	if (log.isErrorEnabled()) {
	  log.error("Failed to ensure correct assemblies saved.");
	}
      }

      resetModified();
    } catch (Exception sqle) {
      if (log.isErrorEnabled())
	log.error("Error saving experiment to database: ", sqle);
      return;
    } finally {
      if (pdb != null) {
	try {
	  pdb.close();
	} catch (SQLException e) {}
      }
    }
  }

  private void saveToDb() {
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

  // Generate a host-node-agent ComponentData tree in the global
  // completeSociety variable
  // This saves all assignments of agents to Nodes, including
  // those nodes not assigned to Hosts.
  // It also gathers the command-line arguments
  private void generateHNACDATA() {
    Set savedNodes = new HashSet();
    Set savedAgents = new HashSet();
    NodeComponent[] nodesToWrite = getNodeComponents();
    AgentComponent[] agentsToWrite = getAgents();

    completeSociety = new GenericComponentData();

    completeSociety.setType(ComponentData.SOCIETY);
    completeSociety.setName(getExperimentName()); // this should be experiment: trial FIXME
    completeSociety.setClassName("java.lang.Object"); // Must not be null
    completeSociety.setOwner(this); // the experiment
    completeSociety.setParent(null);
    addDefaultNodeArguments(completeSociety);

    // For each node, create a GenericComponentData, and add it to the society
    for (Iterator i = hosts.iterator(); i.hasNext(); ) {
      ExperimentHost host = (ExperimentHost) i.next();
      ComponentData hc = new GenericComponentData();
      if(log.isDebugEnabled()) {
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
    for (int i = 0; i < nodesToWrite.length; i++) {
      if (savedNodes.contains(nodesToWrite[i])) continue;
      savedAgents.addAll(generateNodeComponent(nodesToWrite[i], completeSociety));
      savedNodes.add(nodesToWrite[i]);
    }
    for (int i = 0; i < agentsToWrite.length; i++) {
      if (savedAgents.contains(agentsToWrite[i])) {
	if (log.isDebugEnabled()) {
	  log.debug("getHNA Already wrote Agent: " + agentsToWrite[i].getShortName());
	}
        continue;
      }
      if(log.isDebugEnabled()) {
        log.debug("getHNA Writing Agent: " + agentsToWrite[i].getShortName());
      }
      generateAgentComponent(agentsToWrite[i], completeSociety, this);
      savedAgents.add(agentsToWrite[i]);
    }
    
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
      if(kids[i].getType().equals(ComponentData.AGENT)) {
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
	if (! foundkid) {
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
  private boolean askComponentsToAddCDATA() {
    List components = getComponents();

    // Now ask each component in turn to add its stuff
    boolean componentWasRemoved = false;
    for (int i = 0, n = components.size(); i < n; i++) {
      BaseComponent soc = (BaseComponent) components.get(i);
      if(log.isDebugEnabled()) {
        log.debug(soc.getFullName().toString() + ".addComponentData");
      }
      // Warning: This is a no-op in general
      // for recipes that use RecipeBase. AgentInsertion and ABCImpact
      // use it though. Basically, you can only use
      // it if you don't need a DB query to know what to do.
      soc.addComponentData(completeSociety);
      // Components can notice here if they had to remove
      // a component (or, I suppose, modify)
      componentWasRemoved |= soc.componentWasRemoved();
      // Note that no current component returns true here
    }
    return componentWasRemoved;
  }

  /**
   * Generate a complete ComponentData tree for the experiment
   *
   * @return a <code>boolean</code>, true if any component was removed
   */
  public boolean generateCompleteSociety() {
    // If the exp/trialID is null and we have recipes, we must do a save
    // Otherwise the pdb recipes get when doing modifyComponentData
    // wont let them do the DB queries they want to do
    if ((getTrialID() == null || getExperimentID() == null) && getRecipeComponentCount() > 0)
      saveToDb();

    if (! modified && completeSociety != null)
      return false;
    generateHNACDATA();

    boolean mods = askComponentsToAddCDATA();
    return mods |= allowModifyCData();
  }

  private boolean allowModifyCData() {
    // then give everyone a chance to modify what they've collectively produced
    boolean componentModified = false;
    List components = getComponents();
    PopulateDb pdb = null;
    try {
      pdb = new PopulateDb(getExperimentID(), trialID);
      for (int i = 0, n = components.size(); i < n; i++) {
	BaseComponent soc = (BaseComponent) components.get(i);
	if (log.isDebugEnabled()) {
	  log.debug("allowModify letting comps modify. comp: " + soc.getShortName());
	}
	// Recipes typically need to do DB queries
	// in order to do these insertions correctly,
	// so need the version of modify that takes a pdb called.
	soc.modifyComponentData(completeSociety, pdb);
	componentModified |= soc.componentWasRemoved();
      }
      
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
	log.error("allowModifyCData error with pdb", e);
      }
    } finally {
      if (pdb != null) {
	try {
	  pdb.close();
	} catch (SQLException e) {}
      }
    }
    return componentModified;
  }

  ////////////////////////////////////////////
  // Configuration Writer Specific Operations
  ////////////////////////////////////////////

  /** 
   * Dump out the INI files for the first trial to
   * the local results directory for that trial.
   * This saves the experiment to the database.
   * The caller is responsible for not unnecessarily
   * saving the experiment to the database.
   */
  public void dumpINIFiles() {
    // FIXME:
    // If this Exp's society's assembly has entries in the oplan 
    // tables, then must save out what the assembly ID is such 
    // that on load of the INI files we can preserve the OPLAN INFO

    // Generate the complete Society
    generateCompleteSociety();

    ExperimentINIWriter cw = new ExperimentINIWriter(completeSociety); 
    cw.setTrialID(getTrialID());

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
	cw.writeConfigFiles(f);
	// FIXME: Also, write out the community XML file
	// if (! CommWriter.dumpCommunityXML(f.getFullPath() + "communities.xml", getCommAsbID())) 
	// log.error("Couldn't write communities.xml file");
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("Couldn't write ini files: ", e);
        }
      }
    }
  }
  
  public Iterator getConfigFiles(NodeComponent[] nodes) {
    if (DBUtils.dbMode) {
      // Send a config writer that only writes LeafComponentData
      try {
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
    createConfigWriter();
    configWriter.writeFile(filename, out);
  }

  // Private Methods

  /**
   * An Experiment now has a configuration writer that
   * lets all the components write themselves out
   */
  private void createConfigWriter() {
    if(configWriter == null) {
      configWriter = new LeafOnlyConfigWriter(getSocietyComponentData());
    }
  }

  /**
   * Dumps a Host / Node / Agent mapping to an XML file.
   * The stored file can then be imported at a later date
   * to re-create all mappings in the current experiment.
   *
   */
  public void dumpHNA() {
    generateCompleteSociety();

    ExperimentXML xmlWriter = new ExperimentXML();

    File resultDir = getResultDirectory();
    // if user didn't specify results directory, save in defult results directory
    // FIXME: This should use the CSMART global setting
    // CSMART.getResultDir() would do it if the experiment had access
    // to this...
    if (resultDir == null) {
      String resultDirName = ".";
      try {
	resultDirName = System.getProperty("org.cougaar.install.path");
      } catch (RuntimeException e) {
	// just use current directory
	resultDirName = ".";
      }
      resultDir = new File(resultDirName + File.separatorChar + "results");
    }

    Trial trial = getTrials()[0];
    String dirname = resultDir.getAbsolutePath() + File.separatorChar + 
      getExperimentName() + File.separatorChar +
      trial.getShortName() + File.separatorChar +
      "HNA-XML";
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
	JOptionPane.showMessageDialog(null, "Writing xml file to " + f.getAbsolutePath() + "...");

        xmlWriter.createExperimentFile(completeSociety, f);
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("dumpHNA: Couldn't write XML file: ", e);
        }
      }
    }
  }


  /**
   * Imports a HNA XML file.  The file is imported into
   * a <code>ComponentData</code> structure which is then 
   * traversed and applied to the current experiment.
   *
   * @param parent - Parent GUI Component
   */
  public void importHNA(Component parent) {
    JFileChooser chooser = new JFileChooser(SocietyFinder.getInstance().getPath());
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
	public boolean accept (File f) {
	  return (f.isDirectory() || (f.isFile()) && 
            (f.getName().endsWith("xml") || f.getName().endsWith("XML")));
	}
	public String getDescription() {return "HNA XML Files";}
      });
    File file = null;
    while(file == null) {
      int result = chooser.showDialog(parent, "OK");
      if(result != JFileChooser.APPROVE_OPTION)
        return;
      file = chooser.getSelectedFile();
    }

    ComponentData mapping = null;
    if(file != null) {
      ExperimentXML parser = new ExperimentXML();
      mapping = parser.parseExperimentFile(file);
      if(mapping == null) {
        if(log.isErrorEnabled()) {
          log.error("Error parsing file: " + file.getName());
        }
        // Display an error dialog here!
        return;
      }
      
      if(mapping.getType().equals(ComponentData.SOCIETY)) {
        addChildren(mapping.getChildren());
      } else {
        if(log.isErrorEnabled()) {
          log.error("Didn't start with a society!");
        }
      }
    }
  }

  /**
   * Adds new children to the experiment components
   * based on the NHA mappings loaded in from a file.
   *
   * @param children - Child Component to re-map.
   */
  public void addChildren(ComponentData[] children) {
    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
        if(child.getType().equals(ComponentData.HOST)) {
          // Add host.
          HostComponent host = null;
          try {
            host = addHost(child.getName());
          } catch(IllegalArgumentException e) {
            // Host already exists.
            HostComponent[] allHosts = getHostComponents();
            for(int x=0; x < allHosts.length; x++) {
              if(allHosts[x].getShortName().equals(child.getName())) {
                host = allHosts[x];
                break;
              }
            }
            if(log.isDebugEnabled()) {
              log.debug("Host already exists: " + child.getName());
            }
          }
          addHostChildren(child.getChildren(), host);
        } else if(child.getType().equals(ComponentData.NODE)) {
          // Add node.
          NodeComponent node = null;
          try {
            node = addNode(child.getName());
          } catch(IllegalArgumentException e) {
            // Node already exists.
            NodeComponent[] allNodes = getNodeComponents();
            for(int x=0; x < allNodes.length; x++) {
              if(allNodes[x].getShortName().equals(child.getName())) {
                node = allNodes[x];
                break;
              }
            }
            if(log.isDebugEnabled()) {
              log.debug("Node already exists: " + child.getName());
            }
          }
          addNodeChildren(child.getChildren(), node);
        } else if(child.getType().equals(ComponentData.AGENT)) {
          // Ignore.
        } else {
          if(log.isWarnEnabled()) {
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
  public void addHostChildren(ComponentData[] children, HostComponent host) {
    for(int i=0; i < children.length; i++) {
      boolean foundInHost = false;
      boolean foundInSociety = false;
      ComponentData child = children[i];
      NodeComponent expNode = null;
      NodeComponent[] allNodes = getNodeComponents();
      if(child.getType().equals(ComponentData.NODE)) {
        for(int k=0; k < allNodes.length; k++) {
          if(child.getName().equals(allNodes[k].getShortName())) {
            foundInSociety = true;
            expNode = allNodes[k];
            break;
          }
        }
        NodeComponent[] crntNodes = host.getNodes();
        for(int j=0; j < crntNodes.length; j++) {
          if(child.getName().equals(crntNodes[j].getShortName())) {
            foundInHost = true;
            break;
          }
        }
        if(!foundInHost) {
          // See if in Exp.
          if(foundInSociety) {
            expNode = host.addNode(expNode);
          } else {
            expNode = host.addNode(addNode(child.getName()));
          }
        }
	addNodeChildren(child.getChildren(), expNode);
      } else {
        if(log.isWarnEnabled()) {
          log.warn("Unknown child of host: " + child.getName());
        }
      }
    }
  }

  /**
   * Adds all children to the Node.
   *
   *
   * @param children All new children for the node.
   * @param node Node to add children to.
   */
  public void addNodeChildren(ComponentData[] children, NodeComponent node) {
    for(int i=0; i < children.length; i++) {
      boolean foundInSociety = false;
      boolean foundInNode = false;
      ComponentData child = children[i];
      AgentComponent[] allAgents = getAgents();
      AgentComponent addAgent = null;
      if(child.getType().equals(ComponentData.AGENT)) {
        for(int k=0; k < allAgents.length; k++) {
          if(child.getName().equals(allAgents[k].getShortName())) {
            foundInSociety = true;
            addAgent = allAgents[k];
            break;
          }
        }

        if(foundInSociety) {
          AgentComponent[] crntAgents = node.getAgents();
          for(int j=0; j < crntAgents.length; j++) {
            if(child.getName().equals(crntAgents[j].getShortName())) {
              foundInNode = true;
              break;
            }
          }
          if(!foundInNode) {
            if(node.getShortName().equalsIgnoreCase(addAgent.getShortName())) {
              if(log.isWarnEnabled()) {
                log.warn("Agent name same as node, cannot perform addition");
              }
              JOptionPane.showMessageDialog(null, "Cannot Map Agent to Node of Same name, ignoring agent: " + addAgent.getShortName());
            } else {
              node.addAgent(addAgent);
            }
          }
        } else {
          JOptionPane.showMessageDialog(null, "Cannot Map Unknown Agent: " + addAgent.getShortName());
          if(log.isWarnEnabled()) {
            log.warn("Agent: " + child.getName() + " not known.  Add aborted");
          }
        }
      } else {
        if(log.isWarnEnabled()) {
          log.warn("Unknown child of node: " + child.getName());
        }
      }
    }
  }

  ////////////////////////////////////////////
  // Variation Specific Operations
  ////////////////////////////////////////////

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
   * If the experiment has still unbound properties,
   * then we can't run it yet. <br>
   * For each property in the experiment, if it is not set,
   * and we don't have a set of experimental values, return true;
   *
   * @return a <code>boolean</code> value
   */
  public boolean hasUnboundProperties() {
    // First handle Societies
    ModifiableComponent comp = getSocietyComponent();
    List propertyNames = null;
    if (comp != null) {
       for (Iterator j = comp.getProperties(); j.hasNext(); ) {
        Property property = (Property)j.next();
        if (! property.isValueSet()) {
          List values = property.getExperimentValues();
          if (values == null || values.size() == 0) {
            return true;
          }
        }
      }
    }

    // Now handle Recipes
    int n = getRecipeComponentCount();
    for (int i = 0; i < n; i++) {
      comp = getRecipeComponent(i);
      for (Iterator j = comp.getProperties(); j.hasNext(); ) {
	Property property = (Property)j.next();
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


  ////////////////////////////////////////////
  // Configuration Specific Operations
  ////////////////////////////////////////////

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
  public void createDefaultConfiguration() {
    // Check if it already has a node?
    // create one Node
    NodeComponent node = null;

    // BIG HACK IN HERE!!!!!!
    SocietyComponent soc = getSocietyComponent();
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
    fireModification();
  }

  /**
   * Returns whether or not this experiment has been modified
   * since it was saved to the database.
   * @return true if experiment has been modified
   */
  public boolean isModified() {
    return modified;
  }

  /**
   * Indicate that the experiment is up-to-date with respect to the database.
   * Use with caution!  The only reason to reset this flag 
   * is that when an experiment is created from the database, its components
   * are built-up from the database information, and thus the experiment
   * appears to be modified.
   */
  public void resetModified() {
    modified = false;
    // tell listeners experiment is now saved
    fireModification(new ModificationEvent(this, EXPERIMENT_SAVED));
  }

  class MyModificationListener implements ModificationListener, ConfigurableComponentListener {
    // tell listeners on experiment that experiment was modified
    public void modified(ModificationEvent e) {
      notifyExperimentListeners(e);
    }
  }

} // end of Experiment.java







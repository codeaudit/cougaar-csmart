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
import java.util.*;
import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.viewer.Organizer;
import org.cougaar.tools.server.ConfigurationWriter;

public class Experiment extends ModifiableConfigurableComponent implements ModificationListener, java.io.Serializable {
  private static final String DESCRIPTION_RESOURCE_NAME = "description.html";
  private List societies = new ArrayList();
  private List hosts = new ArrayList();
  private List nodes = new ArrayList();
  private List impacts = new ArrayList();
  private List metrics = new ArrayList();
  private transient List listeners = null;
  private File metricsDirectory; // where to store metrics
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
  private String variationScheme = null;
  private boolean editable = true;
  private boolean runnable = true;

  public Experiment(String name, SocietyComponent[] societyComponents,
		    ImpactComponent[] impacts, Metric[] metrics)
  {
    this(name);
    setSocietyComponents(societyComponents);
    setImpacts(impacts);
    setMetrics(metrics);
  }

  public Experiment(String name) {
    super(name);
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
  public void setImpacts(ImpactComponent[] ary) {
    impacts.clear();
    impacts.addAll(Arrays.asList(ary));
  }
  public void setMetrics(Metric[] ary) {
    metrics.clear();
    metrics.addAll(Arrays.asList(ary));
  }
  public void addSocietyComponent(SocietyComponent sc) {
    if (!societies.contains(sc)) societies.add(sc);
  }
  public void addImpact(ImpactComponent impact) {
    if (!impacts.contains(impact)) impacts.add(impact);
  }
  public void addMetric(Metric metric) {
    if (!metrics.contains(metric)) metrics.add(metric);
  }
  public void removeSociety(SocietyComponent sc) {
    societies.remove(sc);
  }
  public void removeImpact(ImpactComponent impact) {
    impacts.remove(impact);
  }
  public void removeMetric(Metric metric) {
    metrics.remove(metric);
  }
  public void removeComponent(ModifiableConfigurableComponent comp) {
    if (comp instanceof SocietyComponent)
      removeSociety((SocietyComponent)comp);
    if (comp instanceof ImpactComponent)
      removeImpact((ImpactComponent)comp);
  }
  public int getSocietyComponentCount() {
    return societies.size();
  }
  public int getComponentCount() {
    //    return societies.size() + impacts.size() + metrics.size();
    return societies.size() + impacts.size();
  }
  public int getImpactCount() {
    return impacts.size();
  }
  public int getMetricCount() {
    return metrics.size();
  }
  public SocietyComponent getSocietyComponent(int i) {
    return (SocietyComponent) societies.get(i);
  }
  public ImpactComponent getImpact(int i) {
    return (ImpactComponent) impacts.get(i);
  }
  public Metric getMetric(int i) {
    return (Metric) metrics.get(i);
  }
  
  public ModifiableConfigurableComponent getComponent(int i) {
    if (i < getSocietyComponentCount())
      return (ModifiableConfigurableComponent)getSocietyComponent(i);
    else if (i < getImpactCount() + getSocietyComponentCount())
      return (ModifiableConfigurableComponent)getImpact(i - getSocietyComponentCount());
//      else
//        return (ModifiableConfigurableComponent)(getMetric(i = getSocietyComponentCount() + getImpactCount()));
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
      for (int i = 0; i < getComponentCount(); i++)
	if (!((ModifiableConfigurableComponent)getComponent(i)).isEditable())
	  return false;
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
   * runnable whenever it's not being edited.
   * @return whether or not an experiment is runnable
   */
  public boolean isRunnable() {
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
    if (listeners == null)
      return;
    for (int i = 0; i < listeners.size(); i++) {
      ExperimentListener listener = (ExperimentListener)listeners.get(i);
      listener.experimentTerminated();
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
    Experiment experimentCopy = new Experiment(uniqueName);
    for (int i = 0; i < societies.size(); i++) {
      SocietyComponent sc = (SocietyComponent)societies.get(i);
      SocietyComponent copiedSC = organizer.copySociety(sc, context);
      experimentCopy.addSocietyComponent(copiedSC);
    }
    // copy impacts & metrics the same
    for (int i = 0; i < impacts.size(); i++) {
      ImpactComponent impact = (ImpactComponent)impacts.get(i);
      ImpactComponent copiedImpact = organizer.copyImpact(impact, context);
      experimentCopy.addImpact(copiedImpact);
    }
    for (int i = 0; i < metrics.size(); i++) {
      Metric metric = (Metric)metrics.get(i);
      Metric copiedMetric = organizer.copyMetric(metric, context);
      experimentCopy.addMetric(copiedMetric);
    }
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

  /**
   * The following code to support hosts and nodes was removed from
   * ScalabilityXSociety.
   */

  public void initProperties() {
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

  /**
   * Get the agents from the societies in the experiment.
   */

  private List getAgentsInSocieties() {
    List agents = new ArrayList();
    for (int i = 0; i < societies.size(); i++) {
      SocietyComponent society = (SocietyComponent)societies.get(i);
      agents.addAll(Arrays.asList(society.getAgents()));
    }
    return agents;
  }

  private List getAgentsInComponents() {
    List agents = new ArrayList();
    agents.addAll(getAgentsInSocieties());
    agents.addAll(getAgentsInImpacts());
    agents.addAll(getAgentsInMetrics());
    return agents;
  }

  private List getAgentsInImpacts() {
    List agents = new ArrayList();
    for (int i = 0; i < impacts.size(); i++) {
      ImpactComponent impact = (ImpactComponent)impacts.get(i);
      agents.addAll(Arrays.asList(impact.getAgents()));
    }
    return agents;
  }

  private List getAgentsInMetrics() {
    List agents = new ArrayList();
    for (int i = 0; i < metrics.size(); i++) {
      Metric impact = (Metric)impacts.get(i);
      //      MetricComponent impact = (MetricComponent)impacts.get(i);
      //      agents.addAll(Arrays.asList(impact.getAgents()));
    }
    return agents;
  }

  public List getAgentsList() {
    List agents = getAgentsInSocieties();
    agents.addAll(getAgentsInImpacts());
    agents.addAll(getAgentsInMetrics());
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

  public File getMetricsDirectory() {
    return metricsDirectory;
  }

  public void setMetricsDirectory(File metricsDirectory) {
    this.metricsDirectory = metricsDirectory;
    fireModification();
  }

  /**
   * An Experiment now has a ConfigurationWriter -
   * one which lets all the components write themselves out
   *
   * @param nodes a <code>NodeComponent[]</code> list of the nodes in the run
   * @return a <code>ConfigurationWriter</code> to write out the config data
   */
  public ConfigurationWriter getConfigurationWriter(NodeComponent[] nodes) {
    return new ExperimentConfigWriter(societies, impacts, nodes);
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
      ModifiableConfigurableComponent society = getComponent(i);
      List propertyNames = society.getPropertyNamesList();
      for (Iterator j = propertyNames.iterator(); j.hasNext(); ) {
	Property property = society.getProperty((CompositeName)j.next());
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
      trials = new ArrayList(1);
      trials.add(trial);
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
      ModifiableConfigurableComponent society = getComponent(i);
      Iterator names = society.getPropertyNames();
      while (names.hasNext()) {
	Property property = society.getProperty((CompositeName)names.next());
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
}






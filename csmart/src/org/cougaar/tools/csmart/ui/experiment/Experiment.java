/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
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

  public Experiment(String name, SocietyComponent[] societyComponents,
		    Impact[] impacts, Metric[] metrics)
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
    return getName().last().toString();
  }
  public void setSocietyComponents(SocietyComponent[] ary) {
    societies.clear();
    societies.addAll(Arrays.asList(ary));
  }
  public void setImpacts(Impact[] ary) {
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
  public void addImpact(Impact impact) {
    if (!impacts.contains(impact)) impacts.add(impact);
  }
  public void addMetric(Metric metric) {
    if (!metrics.contains(metric)) metrics.add(metric);
  }
  public void removeSociety(SocietyComponent sc) {
    societies.remove(sc);
  }
  public void removeImpact(Impact impact) {
    impacts.remove(impact);
  }
  public void removeMetric(Metric metric) {
    metrics.remove(metric);
  }
  public int getSocietyComponentCount() {
    return societies.size();
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
  public Impact getImpact(int i) {
    return (Impact) impacts.get(i);
  }
  public Metric getMetric(int i) {
    return (Metric) metrics.get(i);
  }
  public String toString() {
    return getName().last().toString();
  }

  /**
   * Returns true if experiment is editable,
   * i.e. it is not running and has no saved data.
   * Returns true if all societies in the experiment are editable
   * and the experiment has no saved data.
   * TODO: determine when there is saved data from an experiment & return false
   * @return true if experiement is editable, false otherwise
   */

  public boolean isEditable() {
    for (int i = 0; i < societies.size(); i++)
      if (!((SocietyComponent)societies.get(i)).isEditable())
	return false;
    return true;
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
      Impact impact = (Impact)impacts.get(i);
      Impact copiedImpact = organizer.copyImpact(impact, context);
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

  private List getAgentsInImpacts() {
    List agents = new ArrayList();
    for (int i = 0; i < impacts.size(); i++) {
      Impact impact = (Impact)impacts.get(i);
      agents.addAll(Arrays.asList(impact.getAgents()));
    }
    return agents;
  }

  public List getAgentsList() {
    List agents = getAgentsInSocieties();
    agents.addAll(getAgentsInImpacts());
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
}

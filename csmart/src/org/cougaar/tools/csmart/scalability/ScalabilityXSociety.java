/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.tools.csmart.scalability;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.cougaar.tools.server.ConfigurationWriter;
import org.cougaar.tools.csmart.ui.component.*;
import org.cougaar.tools.csmart.ui.experiment.ExperimentNode;
import org.cougaar.tools.csmart.ui.viewer.Organizer;
import org.cougaar.tools.scalability.scalability.*;

/**
 * Create configuration files to run an COUGAAR society with given
 * sizes. The society consists of a number of nodes. Each node is
 * populated with a number of agents. There are three kinds of agents:
 * root, middle, and leaf. There is only one root agent. The root
 * agent is a middle agent with the addition of a plugin that
 * injects root tasks. A leaf agent allocates to physical assets.
 * The middle agents allocate to other agents. The allocation
 * algorithm equalizes the number of tasks allocated to each agent.
 * The agents are evenly distributed among the nodes to equalize the
 * number of tasks processed by each node.
 *
 * Each agent has a fanout. The agent fanout is a function of the
 * number of plugins per agent and the number of subtasks per input
 * task. To insure that every agent has about the same workload, the
 * number of agents at a level is proportional to the sum of the
 * fanout of all agents at the previous level.
 *
 * The size of the society is characterized by 2 values:
 *   numPlugInsPerAgent -- the number of plugins per agent
 *   numTasksPerPlugIn    -- the number of tasks handled by each
 *                           plugin per root task
 *   numLevels            -- the number of levels
 *
 * Each agent multiplies the number of tasks by the product of the
 * first two. This is the agent fanout. It is also the level fanout
 * so the number of leaf tasks is that product raised to the power of
 * the numLevels.
 **/
public class ScalabilityXSociety
extends ModifiableConfigurableComponent
implements PropertiesListener, Serializable, SocietyComponent, ModificationListener
{
  private static final long serialVersionUID = 4800304251932120182L;

  private boolean isRunning = false;
  private static FileFilter metricsFileFilter = new ScalabilityMetricsFileFilter();

  private static final String DESCRIPTION_RESOURCE_NAME = "description.html";
  private static final String BACKUP_DESCRIPTION =
  "ScalabilitySociety description not available";

  private static final Integer ONE = new Integer(1);
  private static final Integer ZERO = new Integer(0);
  public static final String PROP_LEVELCOUNT = "Level Count";
  public static final Object PROP_LEVELCOUNT_DFLT = ONE;
  public static final String PROP_LEVELCOUNT_TT = "Number of levels (integer)";
  public static final String PROP_LEVELCOUNT_HELP = "levelcount";
  public static final String PROP_PROVIDERCOUNT = ScalabilityXAgent.PROP_PROVIDERCOUNT;
  public static final String PROP_AGENTCOUNT = ScalabilityXLevel.PROP_AGENTCOUNT;
  public static final String PROP_AGENTCOUNT_TT =
  "Number of agents at each level (int[])";
  public static final String PROP_AGENTCOUNT_HELP = "agentcount";
  public static final String PROP_EXPANSION = ScalabilityXAgent.PROP_EXPANSION;
  public static final String PROP_EXPANSION_TT = "Task expansion factor (int[])";
  public static final String PROP_EXPANSION_HELP = "expansion";
  public static final String PROP_ASSETCOUNT = ScalabilityXAgent.PROP_ASSETCOUNT;
  public static final String PROP_ASSETCOUNT_TT = "Assets created (int[])";
  public static final String PROP_ASSETCOUNT_HELP = "assetcount";
  public static final String PROP_AGENTBYTESPERTASK =
  ScalabilityXAgent.PROP_AGENTBYTESPERTASK;
  public static final String PROP_AGENTBYTESPERTASK_TT =
  "Memory used per task within an agent (int[])";
  public static final String PROP_AGENTBYTESPERTASK_HELP = "agentbytes";
  public static final String PROP_CPUPERTASK = ScalabilityXAgent.PROP_CPUPERTASK;
  public static final String PROP_CPUPERTASK_TT =
  "CPU used per task within an agent (int[])";
  public static final String PROP_CPUPERTASK_HELP = "cpu";
  public static final String PROP_LEAFALLOCATORCOUNT =
  ScalabilityXAgent.PROP_LEAFALLOCATORCOUNT;
  public static final String PROP_LEAFALLOCATORCOUNT_TT =
  "Number of leaf allocators by level (int[])";
  public static final String PROP_LEAFALLOCATORCOUNT_HELP = "leafallocators";
  public static final String PROP_ORGALLOCATORCOUNT =
  ScalabilityXAgent.PROP_ORGALLOCATORCOUNT;
  public static final String PROP_ORGALLOCATORCOUNT_TT =
  "Number of organization allocators by level (int[])";
  public static final String PROP_ORGALLOCATORCOUNT_HELP = "orgallocators";
  public static final String PROP_SUBSCRIPTIONCOUNT =
  ScalabilityXAgent.PROP_SUBSCRIPTIONCOUNT;
  public static final String PROP_SUBSCRIPTIONCOUNT_TT =
  "Total subscriptions per agent (int[])";
  public static final String PROP_SUBSCRIPTIONCOUNT_HELP ="subscriptioncount";
  private Property propLevelCount;
  private Property propProviderCount;
  private static MirroredPropertyInfo agentCountMP =
  new MirroredPropertyInfo(PROP_AGENTCOUNT,         new int[] {1}, null, null,
  PROP_AGENTCOUNT_TT, PROP_AGENTCOUNT_HELP);
  private static MirroredPropertyInfo[] mirroredProperties = {
    agentCountMP,
    new MirroredPropertyInfo(PROP_EXPANSION,          new int[] {1}, null, null,
    PROP_EXPANSION_TT, PROP_EXPANSION_HELP),
    new MirroredPropertyInfo(PROP_ASSETCOUNT,         new int[] {1}, null, null,
    PROP_ASSETCOUNT_TT, PROP_ASSETCOUNT_HELP),
    new MirroredPropertyInfo(PROP_AGENTBYTESPERTASK,  new int[] {0}, null, null,
    PROP_AGENTBYTESPERTASK_TT, PROP_AGENTBYTESPERTASK_HELP),
    new MirroredPropertyInfo(PROP_CPUPERTASK,         new int[] {0}, null, null,
    PROP_CPUPERTASK_TT, PROP_CPUPERTASK_HELP),
    new MirroredPropertyInfo(PROP_LEAFALLOCATORCOUNT, new int[] {1}, null, null,
    PROP_LEAFALLOCATORCOUNT_TT, PROP_LEAFALLOCATORCOUNT_HELP),
    new MirroredPropertyInfo(PROP_ORGALLOCATORCOUNT,  new int[] {0}, null, ZERO,
    PROP_ORGALLOCATORCOUNT_TT, PROP_ORGALLOCATORCOUNT_HELP),
    new MirroredPropertyInfo(PROP_SUBSCRIPTIONCOUNT,  new int[] {1}, null, null,
    PROP_SUBSCRIPTIONCOUNT_TT, PROP_SUBSCRIPTIONCOUNT_HELP),
  };
  private static class MirroredPropertyInfo implements Serializable {
    public String name;
    public int[] value;
    public Integer lastValue;
    public Integer firstValue;
    public String tooltip;
    public URL help;
    public MirroredPropertyInfo(String name, int[] value,
				Integer firstValue, Integer lastValue,
				String tooltip, String help)
    {
      this.name = name;
      this.value = value;
      this.firstValue = firstValue;
      this.lastValue = lastValue;
      this.tooltip = tooltip;
      this.help = ScalabilityHelp.getURL(help);
    }
  }

  private Property addProperty(String name, Object value,
			       PropertyListener l,
			       String tooltip,
			       String help)
  {
    Property p = addProperty(name, value, value.getClass());
    p.addPropertyListener(l);
    p.setToolTip(tooltip);
    p.setHelp(ScalabilityHelp.getURL(help));
    return p;
  }

  public ScalabilityXSociety() {
    this("Scalability");
  }

  public ScalabilityXSociety(String name) {
    super(name);
  }

  public void modified(ModificationEvent e) {
    fireModification();
  }

  public void setName(String newName) {
    super.setName(newName);
    fireModification();
  }

  public String getSocietyName() {
    return getName().last().toString();
  }

  public void initProperties() {
    PropertyListener listener =
      new ConfigurableComponentPropertyAdapter() {
      public void propertyValueChanged(PropertyEvent e) {
	changeLevels(e.getPreviousValue());
      }
    };
    propLevelCount = 
      addProperty(PROP_LEVELCOUNT, PROP_LEVELCOUNT_DFLT,
		  listener,
		  PROP_LEVELCOUNT_TT,
		  PROP_LEVELCOUNT_HELP);
    for (int i = 0; i < mirroredProperties.length; i++) {
      final MirroredPropertyInfo mp = mirroredProperties[i];
      Property prop = addProperty(mp.name, mp.value, int[].class);
      prop.setToolTip(mp.tooltip);
      prop.setHelp(mp.help);
      prop.addPropertyListener(new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	  try {
	    distributeIntArrayProperty(mp, e);
	  } catch (InvalidPropertyValueException err) {
	  }
	}
      });
    }
    changeLevels(new Integer(0));
    changeLevels(new Integer(1));
  }

  /**
   * Distributes an array ints to the child level properties with
   * the same name. The MirroredPropertyInfo names the property (for
   * convenience) and indicates how to handle the 0-th child and the
   * last child. These children might be required to have particular
   * values.
   **/
  private void distributeIntArrayProperty(MirroredPropertyInfo mp, PropertyEvent e) throws InvalidPropertyValueException {
    int nlvls = getChildCount();
    Property prop = e.getProperty();
    // We expect the value here to have a certain kind of look.
    // What is it? check it!
    Object o = PropertyHelper.validateValue(prop, prop.getValue());
    int[] values = null;
    if (o != null) {
      values = (int[]) prop.getValue();
      if (values.length <= 0)
	values = new int[0];
    } else {
      // throw an exception?
      return;
    }
    for (int level = 0; level < nlvls; level++) {
      ConfigurableComponent child = getChild(level);
      setMirroredProperty(child, mp, level, values);
    }
    if (mp == agentCountMP) {
      values[0] = 1;
      setProviderCount();
    }
  }

  private void setMirroredProperty(ConfigurableComponent child,
				   MirroredPropertyInfo mp,
				   int level,
				   int[] values)
  {
    Property cp = child.getProperty(mp.name);
    if (cp == null) {
      System.out.println("Child has no " + mp.name);
      return;
    }
    int nlvls = getChildCount();
    Integer cv;
    if (level == 0 && mp.firstValue != null) {
      cv = mp.firstValue;
    } else if (level == nlvls - 1 && mp.lastValue != null) {
      cv = mp.lastValue;
    } else if (level < values.length) {
      cv = new Integer(values[level]);
    } else if (values.length <= 1) {
      // the next line will cause an eror if we're not careful
      cv = null;
    } else {
      cv = new Integer(values[values.length - 1]);
    }
    cp.setValue(cv);
  }


  private int getLevelCount() {
    return ((Integer) getProperty(PROP_LEVELCOUNT).getValue()).intValue();
  }

  public void propertyAdded(PropertyEvent e) {
    Property addedProperty = e.getProperty();
    String lastName = addedProperty.getName().last().toString();
    if (lastName.equals(ScalabilityXLevel.PROP_LEVEL)) {
      setPropertyVisible(addedProperty, false);
      return;
    }
    if (lastName.equals(PROP_PROVIDERCOUNT)) {
      setPropertyVisible(addedProperty, false);
      propProviderCount = addedProperty;
      setProviderCount();
      return;
    }
    Property myProperty = getProperty(lastName);
    if (myProperty != null) {
      setPropertyVisible(addedProperty, false);
    } else {
      ConfigurableComponent cc = addedProperty.getConfigurableComponent();
      if (cc == getChild(0)) {
	addProperty(new PropertyAlias(this, lastName, addedProperty));
	setPropertyVisible(addedProperty, false);
      }
    }
  }

  public void propertyRemoved(PropertyEvent e) {
  }

  private void setProviderCount() {
    int[] agentCount = (int[]) getProperty(PROP_AGENTCOUNT).getValue();
    int nLevels = getLevelCount();
    int providerCount = 0;
    for (int i = 0; i < nLevels; i++) {
      providerCount += agentCount[Math.min(i, agentCount.length)];
    }
    if (propProviderCount != null) {
      propProviderCount.setValue(new Integer(providerCount));
    }
  }

  private void changeLevels(Object prev) {
    int newLevels = getLevelCount();
    if (newLevels < 1) throw new RuntimeException("Must have > 0 levels");
    while (newLevels < getChildCount()) {
      removeChild(getChildCount() - 1);
    }
    if (newLevels > getChildCount()) {
      int[] agentCount = (int[]) getProperty(PROP_AGENTCOUNT).getValue();
      agentCount[0] = 1;
      while (newLevels > getChildCount()) {
	int level = getChildCount();
	ScalabilityXAgent superior = null;
	if (level > 0) {
	  superior = ((ScalabilityXLevel) getChild(0)).getSuperior();
	}
	ScalabilityXLevel child = new ScalabilityXLevel(level, superior);
	addChild(child);
	child.addPropertiesListener(this);
	child.initProperties();
	setPropertyVisible(child.getProperty(ScalabilityXLevel.PROP_LEVEL), false);
	for (int i = 0; i < mirroredProperties.length; i++) {
	  MirroredPropertyInfo mp = mirroredProperties[i];
	  setMirroredProperty(child, mp, level, agentCount);
	}
      }
    }
  }

  public void finishConfiguration() {
    ScalabilityXLevel prevLvl = (ScalabilityXLevel) getChild(0);
    for (int i = 1, n = getChildCount(); i < n; i++) {
      ScalabilityXLevel thisLvl = (ScalabilityXLevel) getChild(i);
      thisLvl.setCustomers(prevLvl.getAgents());
      prevLvl = thisLvl;
    }
  }

  protected void fireChildConfigurationChanged() {
    super.fireChildConfigurationChanged();
    fireModification();
  }

  public AgentComponent[] getAgents() {
    Collection agents = getDescendentsOfClass(ScalabilityXAgent.class);
    return (AgentComponent[]) agents.toArray(new AgentComponent[agents.size()]);
  }

  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  /**
   * Returns whether or not the society can be edited.
   * @return true if society can be edited and false otherwise
   */

  public boolean isEditable() {
    return !isRunning;
  }

  /**
   * Set by the experiment controller to indicate that the
   * society is running.
   * The society is running from the moment that any node
   * is successfully created 
   * until all nodes are terminated (aborted, self terminated, or
   * manually terminated).
   * @param flag indicating whether or not the society is running
   */

  public void setRunning(boolean isRunning) {
    this.isRunning = isRunning;
  }

  /**
   * Returns whether or not the society is running, 
   * i.e. can be dynamically monitored.
   * Running societies are not editable, but they can be copied,
   * and the copy can be edited.
   * @return true if society is running and false otherwise
   */

  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Return a deep copy of the society.
   * @return society component created
   */

  public SocietyComponent copy(Organizer organizer, Object context) {
    String societyName = organizer.generateSocietyName(getSocietyName());
    ScalabilityXSociety result = new ScalabilityXSociety(societyName);
    result.initProperties();
    Property propLevelCount = result.getProperty(PROP_LEVELCOUNT);
    propLevelCount.setValue(getProperty(PROP_LEVELCOUNT).getValue());
    Property propAgentCount = result.getProperty(PROP_AGENTCOUNT);
    propAgentCount.setValue(getProperty(PROP_AGENTCOUNT).getValue());
    for (Iterator i = getPropertyNames(); i.hasNext(); ) {
      CompositeName name = (CompositeName) i.next();
      Property myProp = getProperty(name);
      if (myProp == propLevelCount) continue;
      if (myProp == propAgentCount) continue;
      // TODO: this appears to return null for some properties
      Property hisProp = result.getProperty(name);
      try {
	Object o = PropertyHelper.validateValue(myProp, myProp.getValue());
	if (o != null)
	  hisProp.setValue(o);
      } catch (InvalidPropertyValueException e) {
	System.out.println("ScalabilityXSociety: " + e);
      }
    }
    return (SocietyComponent)result;
  }

  /**
   * Return a file filter which can be used to fetch
   * the metrics files for this experiment.
   * @return file filter to get metrics files for this experiment
   */

  public FileFilter getMetricsFileFilter() {
    return metricsFileFilter;
  }

  /**
   * Return a file filter which can be used to delete
   * the files generated by this experiment.
   * @return file filter for cleanup
   */

  public FileFilter getCleanupFileFilter() {
    // TODO: return cleanup file filter
    return null;
  }


  /**
   * Returns whether the society is self terminating or must
   * be manually terminated.
   * Self terminating nodes cause a NODE_DESTROYED event
   * to be generated (see org.cougaar.tools.server.NodeEvent).
   * @return true if society is self terminating
   * @see org.cougaar.tools.server.NodeEvent
   */

  public boolean isSelfTerminating() {
    return true;
  }

  /**
   * Get a configuration writer for this society.
   */

  public ConfigurationWriter getConfigurationWriter(NodeComponent[] nodes, String nodeFileAddition) {
    return new ScalabilityConfigurationWriter(this, nodes, nodeFileAddition);
  }

}

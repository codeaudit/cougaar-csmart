/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society.scalability;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import org.cougaar.tools.csmart.society.SocietyBase;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.InvalidPropertyValueException;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.core.property.ModificationListener;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.PropertyAlias;
import org.cougaar.tools.csmart.core.property.PropertyHelper;
import org.cougaar.tools.csmart.core.property.ModificationEvent;
import org.cougaar.tools.csmart.core.property.PropertiesListener;
import org.cougaar.tools.csmart.core.property.PropertyListener;
import org.cougaar.tools.csmart.core.cdata.ComponentData;

import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.util.ResultsFileFilter;

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
 *   numPluginsPerAgent -- the number of plugins per agent
 *   numTasksPerPlugin    -- the number of tasks handled by each
 *                           plugin per root task
 *   numLevels            -- the number of levels
 *
 * Each agent multiplies the number of tasks by the product of the
 * first two. This is the agent fanout. It is also the level fanout
 * so the number of leaf tasks is that product raised to the power of
 * the numLevels.
 **/
public class ScalabilityXSociety
extends SocietyBase
implements Serializable, ModificationListener
{
  private static final long serialVersionUID = 4800304251932120182L;

  private static FileFilter metricsFileFilter = new ResultsFileFilter();

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
    public int[] values;
    public Integer lastValue;
    public Integer firstValue;
    public String tooltip;
    public URL help;
    public MirroredPropertyInfo(String name, int[] values,
				Integer firstValue, Integer lastValue,
				String tooltip, String help)
    {
      this.name = name;
      this.values = values;
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
    setEditable(true);
    setSelfTerminating(true);
  }

  public void modified(ModificationEvent e) {
    fireModification();
  }

  public void setName(String newName) {
    super.setName(newName);
    fireModification();
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
    getLevelCount();
    for (int i = 0; i < mirroredProperties.length; i++) {
      final MirroredPropertyInfo mp = mirroredProperties[i];
      Property prop = addProperty(mp.name, mp.values, int[].class);
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
  private void distributeIntArrayProperty(MirroredPropertyInfo mp, PropertyEvent e)
    throws InvalidPropertyValueException
  {
    int nlvls = getChildCount();
    Property prop = e.getProperty();
    // We expect the value here to have a certain kind of look.
    // What is it? check it!
    Object o = PropertyHelper.validateValue(prop, prop.getValue());
    if (o != null) {
      mp.values = (int[]) prop.getValue();
      if (mp.values.length <= 0)
	mp.values = new int[0];
    } else {
      // throw an exception?
      return;
    }
    for (int level = 0; level < nlvls; level++) {
      ConfigurableComponent child = (ConfigurableComponent)getChild(level);
      setMirroredProperty(child, mp, level);
    }
    if (mp == agentCountMP) {
      mp.values[0] = 1;
      setProviderCount();
    }
  }

  private void setMirroredProperty(ConfigurableComponent child,
				   MirroredPropertyInfo mp,
				   int level)
  {
    Property cp = child.getProperty(mp.name);
    if (cp == null) {
      if(log.isDebugEnabled()) {
        log.debug("Child has no " + mp.name);
      }
      return;
    }
    int nlvls = getChildCount();
    Integer cv;
    if (level == 0 && mp.firstValue != null) {
      cv = mp.firstValue;
    } else if (level == nlvls - 1 && mp.lastValue != null) {
      cv = mp.lastValue;
    } else if (level < mp.values.length) {
      cv = new Integer(mp.values[level]);
    } else if (mp.values.length <= 0) {
      // the next line will cause an eror if we're not careful
      cv = null;
    } else {
      cv = new Integer(mp.values[mp.values.length - 1]);
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
    //    System.err.println(this + " in setProviderCount");
    int[] agentCount = (int[]) getProperty(PROP_AGENTCOUNT).getValue();
    int nLevels = getLevelCount();
    int providerCount = 0;
    for (int i = 0; i < nLevels; i++) {
      // AMH - dont access spot in array equal to length, only to length-1
      //providerCount += agentCount[Math.min(i, agentCount.length)];
      providerCount += agentCount[Math.min(i, agentCount.length - 1)];
    }
    if (propProviderCount != null) {
      //System.err.println("Setting new val " + providerCount);
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
//        agentCount[0] = 1;
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
	  setMirroredProperty(child, mp, level);
	}
      }
    }
    setProviderCount();
  }

  public void finishConfiguration() {
    ScalabilityXLevel prevLvl = (ScalabilityXLevel) getChild(0);
    for (int i = 1, n = getChildCount(); i < n; i++) {
      ScalabilityXLevel thisLvl = (ScalabilityXLevel) getChild(i);
      thisLvl.setCustomers(prevLvl.getAgents());
      prevLvl = thisLvl;
    }
    setProviderCount();
  }

  protected void fireChildConfigurationChanged() {
    super.fireChildConfigurationChanged();
    fireModification();
  }

  public AgentComponent[] getAgents() {
    Collection agents = getDescendentsOfClass(ScalabilityXAgent.class);
    return (AgentComponent[]) agents.toArray(new AgentComponent[agents.size()]);
  }

  /**
   * Return a file filter which can be used to fetch
   * the metrics files for this experiment.
   * @return file filter to get metrics files for this experiment
   */
  public FileFilter getResultFileFilter() {
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

  public ComponentData addComponentData(ComponentData data) {
    ComponentData[] children = data.getChildren();

    finishConfiguration();

    // This seams like it can be more efficient.
    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
      if(child.getType() == ComponentData.AGENT) {

	Iterator iter = ((Collection)getDescendentsOfClass(ScalabilityXAgent.class)).iterator();

	while(iter.hasNext()) {
	  ScalabilityXAgent agent = (ScalabilityXAgent)iter.next();
	  if(child.getName().equals(agent.getFullName().toString())) {
	    child.setOwner(this);
	    agent.addComponentData(child);
	  }
	}		
      } else {
	// Process it's children.
	addComponentData(child);
      }      
    }

    return data;
  }
}

/* 
 * <copyright>
 * Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.recipe;

import java.io.FileFilter;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

import org.cougaar.tools.csmart.util.ResultsFileFilter;

import org.cougaar.tools.csmart.experiment.Experiment;

import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;

/**
 * A Metric that adds the CSMART Metric collection Plugin to any society.<br>
 * These add no agents, but do add 1 plugin to every agent in the society,
 * plus one controlling plugin to one of the agents, picked at random.<br>
 * The user can specify the sample interval, the start delay, and the maximum
 * number of samples to take.<br>
 */
public class BasicMetric extends RecipeBase
  implements MetricComponent, Serializable {
  private static final String DESCRIPTION_RESOURCE_NAME = "basic-metric-description.html";
  private static final String BACKUP_DESCRIPTION =
    "BasicMetric provides basic runtime performance metrics";

  /** Sample Interval Property Definitions **/
  public static final String PROP_SAMPLEINTERVAL = "Metrics: Sample Interval";
  public static final Integer PROP_SAMPLEINTERVAL_DFLT = new Integer(10);
  public static final String PROP_SAMPLEINTERVAL_DESC = "Sample Interval to collect metrics";

  /** Start Delay Property Definitions **/
  public static final String PROP_STARTDELAY = "Metrics: Start Delay";
  public static final Integer PROP_STARTDELAY_DFLT = new Integer(0);
  public static final String PROP_STARTDELAY_DESC = "Delay to wait before metric collection";

  /** Maximum Number of Samples Property Definitions **/
  public static final String PROP_MAXNUMBSAMPLES = "Metrics: Maximum Number of Samples";
  public static final Integer PROP_MAXNUMBSAMPLES_DFLT = new Integer(20);
  public static final String PROP_MAXNUMBSAMPLES_DESC = "Number of metric samples to collect";

  // Props for metrics
  private Property propSampleInterval;
  private Property propStartDelay;
  private Property propMaxSamples;

  // Properties to define parameters to MetricsPlugin
  // Directory to saveresults files in - default "."
  public static final String PROP_RESULTSDIR = "Metrics results directory";
  public static final String PROP_RESULTSDIR_DFLT = ".";
  public static final String PROP_RESULTSDIR_DESC = "Relative path in which to save results files";
  private Property propResultsDir;
  
  // Verb to search for - default null
  public static final String PROP_TVERB = "Task Verb";
  public static final String PROP_TVERB_DFLT = org.cougaar.glm.ldm.Constants.Verb.GETLOGSUPPORT;
  public static final String PROP_TVERB_DESC = "Task Verb to search for and count";
  private Property propTVerb;
  
  // 1/0 for Blackboard Service, default 0
  public static final String PROP_BBSERV = "Blackboard Statistics Service On/Off";
  public static final Integer PROP_BBSERV_DFLT = new Integer(0);
  public static final String PROP_BBSERV_DESC = "Turn Blackboard metrics collection on or off";
  private Property propBBServ;
  
  // 1/0 for PrototypeRegistryService, default 0
  public static final String PROP_PRSERV = "Prototype Registry Service On/Off";
  public static final Integer PROP_PRSERV_DFLT = new Integer(0);
  public static final String PROP_PRSERV_DESC = "Turn Prototype Registry metrics on or off";
  private Property propPRServ;
  
  // 1/0 for NodeMetricsService, default 0
  public static final String PROP_NODESERV = "Node Metrics Service On/Off";
  public static final Integer PROP_NODESERV_DFLT = new Integer(0);
  public static final String PROP_NODESERV_DESC = "Turn Node metrics on or off";
  private Property propNodeServ;  
  
  // 1/0 for MessageStatsService, default 1
  public static final String PROP_MSTATSSERV = "Message Stats Service On/Off";
  public static final Integer PROP_MSTATSSERV_DFLT = new Integer(1);
  public static final String PROP_MSTATSSERV_DESC = "Turn Message Transport metrics on or off";
  private Property propMStatsServ;  
  
  // 1/0 for MessageWatcherService, default 0
  public static final String PROP_MWATCHSERV = "Message Watcher Service On/Off";
  public static final Integer PROP_MWATCHSERV_DFLT = new Integer(0);
  public static final String PROP_MWATCHSERV_DESC = "Turn Message Watcher on or off";
  private Property propMWatchServ;
  
  private static final String MetricsPlugIn_name =
    "org.cougaar.tools.csmart.runtime.plugin.MetricsPlugin";
  private static final String MetricsInitializerPlugIn_name =
    "org.cougaar.tools.csmart.runtime.plugin.MetricsInitializerPlugin";
  private static final String MetricControl_Role = "MetricsControlProvider";

  private transient int numAgs2 = 0;

  // FileFilter for metrics:
  private static FileFilter metricsFileFilter = new ResultsFileFilter();

  public BasicMetric() {
    this("Basic Metric");
  }
  
  public BasicMetric(String name) {
    super(name);
  }

  public void initProperties() {
    
    propSampleInterval = addProperty(PROP_SAMPLEINTERVAL, PROP_SAMPLEINTERVAL_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propSampleInterval.setToolTip(PROP_SAMPLEINTERVAL_DESC);

    propMaxSamples = addProperty(PROP_MAXNUMBSAMPLES, PROP_MAXNUMBSAMPLES_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propMaxSamples.setToolTip(PROP_MAXNUMBSAMPLES_DESC);

    propStartDelay = addProperty(PROP_STARTDELAY, PROP_STARTDELAY_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propStartDelay.setToolTip(PROP_STARTDELAY_DESC);

    propResultsDir = addProperty(PROP_RESULTSDIR, PROP_RESULTSDIR_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propResultsDir.setToolTip(PROP_RESULTSDIR_DESC);

    // For now, don't show this property - we need the results filter to change
    // appropriately before we can let the user change this
    // FIXME!
    setPropertyVisible(propResultsDir, false);

    // Task Verb to search for
    propTVerb = addProperty(PROP_TVERB, PROP_TVERB_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propTVerb.setToolTip(PROP_TVERB_DESC);

    propBBServ = addProperty(PROP_BBSERV, PROP_BBSERV_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propBBServ.setToolTip(PROP_BBSERV_DESC);

    propPRServ = addProperty(PROP_PRSERV, PROP_PRSERV_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propPRServ.setToolTip(PROP_PRSERV_DESC);

    propNodeServ = addProperty(PROP_NODESERV, PROP_NODESERV_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propNodeServ.setToolTip(PROP_NODESERV_DESC);

    propMStatsServ = addProperty(PROP_MSTATSSERV, PROP_MSTATSSERV_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propMStatsServ.setToolTip(PROP_MSTATSSERV_DESC);

    propMWatchServ = addProperty(PROP_MWATCHSERV, PROP_MWATCHSERV_DFLT,
                                new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	}
    });
    propMWatchServ.setToolTip(PROP_MWATCHSERV_DESC);

  }

  /**
   * Gets the name of the html help file for this component.
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }
  
  public String getMetricName() {
    return getShortName();
  }

  /**
   * Return a file filter which can be used to fetch
   * the metrics files for this experiment.
   * @return file filter to get metrics files for this experiment
   */
  public FileFilter getResultFileFilter() {
    // FIXME: Make this more specific?
    return metricsFileFilter;
  }

  /**
   * Return a file filter which can be used to delete
   * the files generated by this experiment.
   * @return file filter for cleanup
   */
  public FileFilter getCleanupFileFilter() {
    // Fixme: make this more specific
    // Also, should this delete additional ini files (of which there are none)?
    return metricsFileFilter;
  }

  private transient int numAgents = 0; // numAgents collecting stats
  private transient RelationshipData metricRelate = null; // name of agent doing controling
  
  private ComponentData addInitPICD(ComponentData data) {
    GenericComponentData plugin = new GenericComponentData();
    plugin.setType(ComponentData.PLUGIN);
    plugin.setName(MetricsInitializerPlugIn_name);
    plugin.setClassName(MetricsInitializerPlugIn_name);
    plugin.addParameter(new Integer(numAgents)); // numProviders
    plugin.addParameter(getProperty(PROP_SAMPLEINTERVAL).getValue()); // sampleInterval
    plugin.addParameter(getProperty(PROP_STARTDELAY).getValue()); // startDelay
    plugin.addParameter(getProperty(PROP_MAXNUMBSAMPLES).getValue()); // maxSamples
    plugin.setParent(data);
    plugin.setOwner(this);
    // Add the plugin such that it replaces any previous version
    data.addChildDefaultLoc(plugin);
    return plugin;
  }

  private void addCollectorCD(ComponentData data) {
    //Only add this if its not there already
    ComponentData[] children = data.getChildren();
    for (int i = 0; i < data.childCount(); i++) {
      if (children[i].getName().equals(MetricsPlugIn_name))
	return;
    }
    GenericComponentData plugin = new GenericComponentData();
    plugin.setType(ComponentData.PLUGIN);
    //plugin.setName("MetricPlugin");
    plugin.setName(MetricsPlugIn_name);
    plugin.setClassName(MetricsPlugIn_name);
    plugin.setParent(data);
    plugin.setOwner(this);

    // Add parameters here:
    // Why can't I find it by name? Cause it's hidden?
    plugin.addParameter(propResultsDir.getValue()); // dir for Results files
    plugin.addParameter(getProperty(PROP_TVERB).getValue()); // Task Verb to search for
    plugin.addParameter(getProperty(PROP_BBSERV).getValue()); // Turn on/off BBoard Serv
    plugin.addParameter(getProperty(PROP_PRSERV).getValue()); // Turn on/off Proto Reg. Serv
    plugin.addParameter(getProperty(PROP_NODESERV).getValue()); // Turn on/off Node Metrics Serv
    plugin.addParameter(getProperty(PROP_MSTATSSERV).getValue()); // Turn on/off Msg Stats Serv, def. on
    plugin.addParameter(getProperty(PROP_MWATCHSERV).getValue()); // Turn on/off MsgWatcher Serv

    // Add the plugin such that it replaces any previous version
    data.addChildDefaultLoc(plugin);
  }

  private void addRelationship(AgentComponentData data) {
    if (metricRelate == null) 
      return;
    // Only add the relationship if its not already there
    AgentAssetData aad = data.getAgentAssetData();
    if (aad == null) return;    // No agent asset data available
    RelationshipData[] relats = aad.getRelationshipData();
    for (int i = 0; i < relats.length; i++) {
      // FIXME: This should be equals based on the Role not the agent
      // Or rather, we must be careful....
      if (relats[i].equals(metricRelate))
	return;
    }
    aad.addRelationship(metricRelate);
  }
  
  public ComponentData modifyComponentData(ComponentData data) {
    numAgs2 = 0;                // Gets counted as metrics plugins are inserted
    ComponentData picd = addMetricsComponentData(data);

    // OK, now set numAgs2 val in the numProviders slot
    // First, find the MetricsInitializer Plugin
    if (picd == null) {
      // couldn't find the initializer plugin. Big problem
      System.err.println("BasicMetric: Could not insert initializer?");
      return data;
    }
    
    // Then, reset the value of the first parameter
    picd.setParameter(0, new Integer(numAgs2));
   
    return data;
  }

  private ComponentData addMetricsComponentData(ComponentData data) {

    ComponentData picd = null;

    // Find the Experiment.
    // From it, get a count of the Agents in the Society & set the numAgents
    if (numAgents == 0 && data.getOwner() instanceof Experiment) {
      Experiment exp = (Experiment)data.getOwner();
      List ags = exp.getAgentsList();
      if (ags != null)
	numAgents = ags.size();
      else
	numAgents = 0;
    }
    // recurse down from the top, finding all of the AgentComponents
    // save the name of the first I find
    if (data.getType().equals(ComponentData.AGENT)) {
      numAgs2++;
      if (metricRelate == null) {
	metricRelate = new RelationshipData();
	metricRelate.setRole(MetricControl_Role);
	metricRelate.setItem(data.getName()); // name
	metricRelate.setType(data.getName().substring(data.getName().lastIndexOf(".") + 1));
	metricRelate.setSupported(data.getName()); // name
	// and to the first, add the MetricsInitializerPlugIn
	picd = addInitPICD(data);
      }
      // for each Agent, add the MetricsPlugIn & the relationship
      addCollectorCD(data);
      addRelationship((AgentComponentData) data);
    } else if (data.childCount() > 0) {
      // for each child, call this same method.
      ComponentData[] children = data.getChildren();
      for (int i = 0; i < children.length; i++) {
	ComponentData xpicd = addMetricsComponentData(children[i]);
        if (xpicd != null) picd = xpicd;
      }
    }
    return picd;
  }
} // end of BasicMetric.java

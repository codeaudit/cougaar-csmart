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
package org.cougaar.tools.csmart.configgen.abcsociety;

import java.io.FileFilter;
import java.io.Serializable;
import java.net.URL;

import org.cougaar.tools.server.ConfigurationWriter;

import org.cougaar.tools.csmart.scalability.ScalabilityMetricsFileFilter;
import org.cougaar.tools.csmart.ui.component.AgentComponent;
import org.cougaar.tools.csmart.ui.component.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.ui.component.MetricComponent;
import org.cougaar.tools.csmart.ui.component.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.ui.component.NodeComponent;
import org.cougaar.tools.csmart.ui.component.Property;
import org.cougaar.tools.csmart.ui.component.PropertyEvent;
import org.cougaar.tools.csmart.ui.component.PropertiesListener;
import org.cougaar.tools.csmart.ui.viewer.Organizer;

/**
 * A Metric that adds the CSMART Metric collection Plugin to any society.<br>
 * These add no agents, but do add 1 plugin to every agent in the society,
 * plus one controlling plugin to one of the agents, picked at random.<br>
 * The user can specify the sample interval, the start delay, and the maximum
 * number of samples to take.<br>
 */
public class BasicMetric extends ModifiableConfigurableComponent
  implements MetricComponent, PropertiesListener, Serializable {
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
  
  private static final String MetricsPlugIn_name =
    "org.cougaar.tools.csmart.plugin.MetricsPlugin";
  private static final String MetricsInitializerPlugIn_name =
    "org.cougaar.tools.csmart.plugin.MetricsInitializerPlugin";

  private boolean editable = true;

  // FileFilter for metrics:
  private static FileFilter metricsFileFilter = new ScalabilityMetricsFileFilter();

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

  }
  
  public String getMetricName() {
    return getShortName();
  }

  /**
   * Get the agents, both assigned and unassigned.
   * Only return new agents.
   * @return array of agent components
   */
  public AgentComponent[] getAgents() {
    // This metric adds no new agents
    return null;
  }

  //  public HostComponent[] getHosts() {return null;}
  //  public NodeComponent[] getNodes() {return null;}

  /**
   * Return a file filter which can be used to fetch
   * the metrics files for this experiment.
   * @return file filter to get metrics files for this experiment
   */
  public FileFilter getResultFileFilter() {
    // Fixme: Make this more specific?
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

  /**
   * Get a configuration writer for this metric.
   * @param nodes the <code>NodeComponent[]</code> of the full experiment
   * @param nodeFileAddition a <code>String</code> to add Services to every Node file
   * @return a <code>ConfigurationWriter</code> to write out all config data for this metric
   */
  public ConfigurationWriter getConfigurationWriter(NodeComponent[] nodes, String nodeFileAddition) {
    // FIXME -- I don't want to need this method at all!
    return null;
  }

  public ComponentData addComponentData(ComponentData data) {
    // The Basic Metric needs to add its plugin to each Agent in the society.
    // Plus, it should pick one agent in the society, and add the initializer to that agent
    // plus, it needs one of the args to the initializer to be the total number of agents in the society
    // Finally, it needs to add a relationship to every agent in the society with
    // that initializer
    // FIXME!!!
    return data;
  }

  public ComponentData modifyComponentData(ComponentData data) {
    // FIXME!!!
    return data;
  }

  ///////////////////////////////////////////
  // Boilerplate stuff added below... Necessary?
  
  // Implement PropertyListener
  /**
   * Called when a new property has been added to the
   * society. 
   *
   * @param PropertyEvent Event for the new property
   */
  public void propertyAdded(PropertyEvent e) {
    Property addedProperty = e.getProperty();
    Property myProperty = getProperty(addedProperty.getName().last().toString());
    if (myProperty != null) {
      setPropertyVisible(addedProperty, true);
    }
  }

  /**
   * Called when a property has been removed from the society
   */
  public void propertyRemoved(PropertyEvent e) {
    // FIXME - do something?
  }
  // end of PropertyListener implementation

  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  /**
   * Returns whether or not the component can be edited.
   * @return true if component can be edited and false otherwise
   */
  public boolean isEditable() {
    //    return !isRunning;
    return editable;
  }

  /**
   * Set whether or not the component can be edited.
   * @param editable true if component is editable and false otherwise
   */
  public void setEditable(boolean editable) {
    this.editable = editable;
  }
} // end of BasicMetric.java


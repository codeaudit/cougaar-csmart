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

package org.cougaar.tools.csmart.ui.monitor.generic;

import java.awt.Color;
import java.io.*;
import java.util.*;

import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Utility class that supports a common set of properties (especially color),
 * for all the user interfaces (both graphs and charts).
 */

public class UIProperties {
  public static final String DEFAULT_COLOR_GVT = "255,255,255"; // white
  public static final String DEFAULT_COLOR_SELECT = "238,44,44"; // firebrick2
  public static final String DEFAULT_COLOR_CAUSES = "0,0,205"; // blue3
  public static final String DEFAULT_COLOR_EFFECTS = "238,238,0"; // yellow2
  public static final String DEFAULT_COLOR_BEFORE = "137,104,205"; // mediumpurple3
  public static final String DEFAULT_COLOR_AFTER = "118,238,0"; // chartreuse2
  public static final String DEFAULT_COLOR_CLUSTER1 = "155,205,155"; // darkseagreen3
  public static final String DEFAULT_COLOR_CLUSTER2 = "238,180,34"; // goldenrod2
  public static final String DEFAULT_COLOR_CLUSTER3 = "238,121,66"; // sienna2
  public static final String DEFAULT_COLOR_CLUSTER4 = "216,191,216"; // thistle
  public static final String DEFAULT_COLOR_CLUSTER5 = "224,238,238"; // azure2
  public static final String DEFAULT_COLOR_CLUSTER6 = "30,144,25"; // dodgerblue
  // must match highest DEFAULT_COLOR_CLUSTER 
  public static final int DEFAULT_MAX_COLOR = 10; // number of cluster colors

  public static final String DEFAULT_COLOR_CLUSTER_ATTACKER = "155,205,155"; // darkseagreen3
  public static final String DEFAULT_COLOR_CLUSTER_FIREWALL = "238,180,34"; // goldenrod2
  public static final String DEFAULT_COLOR_CLUSTER_VICTIM = "238,121,66"; // sienna2

  // Default line sizes for the metrics.
  public static final String DEFAULT_GRAPH_LINE_SIZE = "2";
  public static final String DEFAULT_GRAPH_LEGEND_SIZE = "8";

  private transient Logger log;

  String[] clusterNames = { "Attacker", "Firewall", "Victim" };
  int nextColor = 1;
  Properties properties;
  int maxColor = DEFAULT_MAX_COLOR; // read from ui.properties file

  /**
   * Set default colors for known cluster names, and define a set
   * of colors that are used for other clusters.
   */

  public UIProperties() {
    createLogger();
    // define default properties
    Properties defaults = new Properties();
    setDefaults(defaults);
    // define new properties from config file; default to defaults
    properties = new Properties(defaults);
    ConfigFinder configFinder = ConfigFinder.getInstance();
    File file = configFinder.locateFile("ui.properties");
    if (file != null) {
      try {
	properties.load(new FileInputStream(file));
      } catch (Exception e) {
        if(log.isDebugEnabled()) {
          log.warn("Could not read properties file: " + 
                             file.getPath() + " " + e);
        }
      }
    }
    int maxColor =
      Integer.parseInt(properties.getProperty("color.cluster.max"));
    //    printColors(); // for debugging
  }

  private void createLogger() {
    log = CSMART.createLogger("org.cougaar.tools.csmart.ui.monitor.generic");
  }

  private void printColors() {
      if(log.isDebugEnabled()) {
        log.debug("Maximum colors: " + maxColor);
      }
    for (int i = 1; i <= maxColor; i++) {
      String s = "color.cluster." + i; // color.cluster.i
      if(log.isDebugEnabled()) {
        log.debug(s + " is: " + properties.getProperty(s));
      }
    }
  }

  /**
   * Save properties in the specified file.
   * @param file    file to which to save properties
   */

  public void saveProperties(File file) {
    try {
      properties.store(new FileOutputStream(file), "CSMART UI Properties");
    } catch (Exception e) {
      if(log.isDebugEnabled()) {
        log.error("Could not save CSMART UI Properties in: " +
                  file.getPath() + " " + e);
      }
    }
  }

  /**
   * Save the defaults defined by this class in the ui.properties file
   * found by ConfigFinder.
   */

  public void saveDefaults() {
    setDefaults(properties);
    ConfigFinder configFinder = new ConfigFinder();
    File file = configFinder.locateFile("ui.properties");
    if (file == null) 
      file = new File("ui.properties");
    if (file != null)
      saveProperties(file);
  }

  /**
   * Set the default properties.
   * @param defaults properties to set
   */

  private void setDefaults(Properties defaults) {
    defaults.setProperty("color.gvt", DEFAULT_COLOR_GVT);
    defaults.setProperty("color.select", DEFAULT_COLOR_SELECT);
    defaults.setProperty("color.causes", DEFAULT_COLOR_CAUSES);
    defaults.setProperty("color.effects", DEFAULT_COLOR_EFFECTS);
    defaults.setProperty("color.before", DEFAULT_COLOR_BEFORE);
    defaults.setProperty("color.after", DEFAULT_COLOR_AFTER);
    defaults.setProperty("color.cluster.Attacker", DEFAULT_COLOR_CLUSTER_ATTACKER);
    defaults.setProperty("color.cluster.Firewall", DEFAULT_COLOR_CLUSTER_FIREWALL);
    defaults.setProperty("color.cluster.Victim", DEFAULT_COLOR_CLUSTER_VICTIM);
    defaults.setProperty("color.cluster.1", DEFAULT_COLOR_CLUSTER1);
    defaults.setProperty("color.cluster.2", DEFAULT_COLOR_CLUSTER2);
    defaults.setProperty("color.cluster.3", DEFAULT_COLOR_CLUSTER3);
    defaults.setProperty("color.cluster.4", DEFAULT_COLOR_CLUSTER4);
    defaults.setProperty("color.cluster.5", DEFAULT_COLOR_CLUSTER5);
    defaults.setProperty("color.cluster.6", DEFAULT_COLOR_CLUSTER6);
    
    // Metric Defaults.
    defaults.setProperty("metric.line.size", DEFAULT_GRAPH_LINE_SIZE);
    defaults.setProperty("metric.legend.size", DEFAULT_GRAPH_LEGEND_SIZE);
  }

  /**
   * Convert a string of "r,g,b" to a color.
   * @param s   string to convert
   * @return    color
   */

  private Color stringToColor(String s) {
    StringTokenizer st = new StringTokenizer(s, ",");
    if (st.countTokens() < 3)
      return Color.green; // bad color string, return default color

    int[] rgb = new int[3];
    try {
      for (int i = 0; i < 3; i++)
	rgb[i] = Integer.parseInt(st.nextToken());
    } catch (Exception e) {
      if(log.isDebugEnabled()) {
        log.error("Exception parsing color string: " + e);
      }
      return Color.green;
    }
    return new Color(rgb[0], rgb[1], rgb[2]);
  }

  /**
   * Return color defined to show selected nodes.
   * @return color
   */

  public Color getColorSelect() {
    return stringToColor(properties.getProperty("color.select"));
  }

  /**
   * Return color defined to show causes of the selected node.
   * TODO: move to ia specific code
   * @return color
   */

  public Color getColorCauses() {
    return stringToColor(properties.getProperty("color.causes"));
  }

  /**
   * Return color defined to show effects of the selected node.
   * TODO: move to ia specific code
   * @return color
   */

  public Color getColorEffects() {
    return stringToColor(properties.getProperty("color.effects"));
  }

  /**
   * Return color defined to display nodes before the selected node.
   * TODO: move to ia specific code
   * @return color
   */

  public Color getColorBefore() {
    return stringToColor(properties.getProperty("color.before"));
  }

  /**
   * Return color defined to display nodes after the selected node.
   * @return color
   */

  public Color getColorAfter() {
    return stringToColor(properties.getProperty("color.after"));
  }

  /**
   * Return color defined to show gvt.
   * @return color
   */

  public Color getColorGVT() {
    return stringToColor(properties.getProperty("color.gvt"));
  }

  /**
   * Return a color for the specified cluster.
   * Does case-insensitive matches on cluster name.
   * The color is either:
   * a color explicitly defined in this file as the default color for
   * the cluster; or
   * a color assigned from a group of cluster colors defined in this file;
   * or a color specifically set for this cluster.
   * @param  clusterName  the name of the cluster for which to get a color
   * @return              color to use for nodes associated with the cluster
   */

  public Color getClusterColor(String clusterName) {
    String propertyName = "color.cluster." + clusterName.toLowerCase();
    String colorString = properties.getProperty(propertyName);
    if (colorString != null) 
      return stringToColor(colorString);
      
    String s = "color.cluster." + nextColor; // color.cluster.n
    colorString = properties.getProperty(s);
    if (colorString == null)
      return Color.green; // shouldn't get here

    // save color.cluster.clustername, color
    properties.setProperty(propertyName, colorString);
    nextColor++;
    if (nextColor > maxColor)
      nextColor = 1;
      if(log.isDebugEnabled()) {
        log.debug("Assigned: " + colorString + " to: " + clusterName);
      }
    return stringToColor(colorString);
  }

  /**
   * Set the color to use for a cluster.
   * @param clusterName  the cluster for which to set a color
   * @param return       the color to use for nodes associated with the cluster
   */

  public void setClusterColor(String clusterName, Color color) {
    String propertyName = "color.cluster." + clusterName;
    String colorString = color.getRed() + "," + color.getGreen() + "," +
      color.getBlue();
    properties.setProperty(propertyName, colorString);
  }

  /**
   * Get the size of graph lines for metric views.
   * @return Size of the graph line.
   */
  public int getMetricLineSize() {
    return Integer.parseInt(properties.getProperty("metric.line.size"));
  }    

  /**
   * Get the Size for the diameter of the legend color
   * @return Size of the legend color
   */
  public int getMetricLegendSize() {
    return Integer.parseInt(properties.getProperty("metric.legend.size"));
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

  /**
   * Rewrites the configuration file to contain the property
   * defaults as defined in this class.
   */

  public static void main(String args[]) {
    UIProperties p = new UIProperties();
    p.saveDefaults();
  }
}


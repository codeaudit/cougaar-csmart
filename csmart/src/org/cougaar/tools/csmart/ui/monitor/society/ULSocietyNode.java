/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.society;

import java.util.*;

import org.cougaar.core.society.UID;
import org.cougaar.tools.csmart.ui.monitor.generic.NodeObject;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTGraph;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.util.PropertyTree;

/**
 * Creates a NodeObject, i.e. an object that can be graphed, from
 * a PropertyTree object derived from Organization assets.
 * These objects are used to populate Society graphs.
 */

public class ULSocietyNode implements NodeObject {
  boolean visible;
  String UID;
  String label;
  String toolTip;
  String color;
  String shape;
  String sides = "0";
  String distortion = "0";
  String orientation = "0";
  PropertyTree properties;
  Vector bidirectionalLinks;

  /**
   * Create a NodeObject from a PropertyTree based on an AssetEvent.
   * @param properties    properties from an AssetEvent; supplied by the PSP
   */

  public ULSocietyNode(PropertyTree properties) {
    UID = (String)properties.get(PropertyNames.UID_ATTR);
    color = (String)properties.get(PropertyNames.ORGANIZATION_NAME);
    label = (String)properties.get(PropertyNames.ORGANIZATION_NAME);
    toolTip = "";
    setNodeShapeParameters();
    visible = true;
    properties.put(PropertyNames.TABLE_TITLE,
		   "Agent " + 
		   (String)properties.get(PropertyNames.ORGANIZATION_NAME));
    this.properties = properties;
    bidirectionalLinks = new Vector();
  }

  public String getUID() {
    return UID;
  }

  /**
   * Force quotations at start and end of label, otherwise dot
   * mis-interprets labels that start with a digit.
   */

  public String getLabel() {
    //    return "\"" + label + "\"";
    return label;
  }
    
  public String getToolTip() {
    return toolTip;
  }

  public String getColor() {
    return color;
  }

  public String getBorderColor() {
    return null;
  }

  public String getFontStyle() {
    return "normal";
  }

  public String getShape() {
    return shape;
  }

  public String getSides() {
    return sides;
  }

  public String getDistortion() {
    return distortion;
  }

  public String getOrientation() {
    return orientation;
  }

  public PropertyTree getProperties() {
    return properties;
  }

  public Vector getIncomingLinks() {
    return null;
  }

  public Vector getOutgoingLinks() {
    return null;
  }

  public Vector getBidirectionalLinks() {
    return bidirectionalLinks;
  }

  public void addBidirectionalLink(String UID) {
    bidirectionalLinks.addElement(UID);
  }

  /**
   * Make all nodes ellipses for now.
   */

  private void setNodeShapeParameters() {
    shape = "ellipse";
  }

  public void addOutgoingLink(String UID) {
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public String getWidth() {
    return "0"; // default, ignored
  }

  public String getHeight() {
    return "0"; // default, ignored
  }
}





/*
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.community;

import org.cougaar.core.util.UID;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.monitor.generic.NodeObject;
import org.cougaar.util.PropertyTree;

import java.util.Vector;

/**
 * Creates a NodeObject, i.e. an object that can be graphed, from
 * a PropertyTree based on an AssetEvent.
 * These objects are used to populate a Community graph.
 */

public class ULCommunityNode implements NodeObject {
  boolean visible = true;
  String UID; // use the UID of the asset that's used to create this
  String label;
  String toolTip = "";
  String color;
  String shape = "roundedbox";
  String sides = "0";
  String distortion = "0";
  String orientation = "0";
  Vector outgoingLinks;
  PropertyTree properties;

  /**
   * Create a NodeObject from a PropertyTree based on an AssetEvent.
   * @param properties  properties from an AssetEvent; supplied by the servlet
   */
  public ULCommunityNode(PropertyTree properties) {
    UID = (String)properties.get(PropertyNames.UID_ATTR);
    // use community name as color map
    //    label = (String)properties.get(PropertyNames.AGENT_LABEL);
    label = (String)properties.get(PropertyNames.AGENT_COMMUNITY_NAME);
    color = (String)properties.get(PropertyNames.AGENT_COMMUNITY_NAME);
    properties.put(PropertyNames.TABLE_TITLE,
		   "Community <" + label + ">");
    outgoingLinks = new Vector();
    this.properties = properties;
  }

  public String getUID() {
    return UID;
  }

  public String getLabel() {
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
    return outgoingLinks;
  }

  public Vector getBidirectionalLinks() {
    return null;
  }

  /**
   * Make all nodes ellipses for now.
   */

  private void setNodeShapeParameters() {
    shape = "ellipse";
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  /**
   * Add the names of the agents in the community to the information
   * associated with this community.
   * @param agentNames vector of agent names (Strings)
   */

  public void addMembers(Vector agentNames) {
    String s = (String)properties.get(PropertyNames.COMMUNITY_MEMBERS);
    if (s == null) {
      StringBuffer buffer = new StringBuffer(200);
      int maxIndex = agentNames.size() - 1;
      for (int i = 0; i <= maxIndex; i++) {
	buffer.append((String)agentNames.get(i));
	if (i < maxIndex)
	  buffer.append(", ");
      }
      properties.put(PropertyNames.COMMUNITY_MEMBERS, buffer.toString());
    }
  }

  public void addOutgoingLink(String UID) {
    outgoingLinks.add(UID);
  }

  public String getWidth() {
    return "0"; // default, ignored
  }

  public String getHeight() {
    return "0"; // default, ignored
  }
}





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

package org.cougaar.tools.csmart.ui.monitor.plan;

import java.text.NumberFormat;
import java.util.*;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.monitor.generic.NodeObject;
import org.cougaar.util.PropertyTree;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.io.ObjectInputStream;
import java.io.IOException;

public class ULPlanNode implements NodeObject {
  PropertyTree properties;
  String UID;
  String color;
  String objectType;
  String label = "";
  Vector incomingLinks = null;
  Vector outgoingLinks = null;
  //  Vector bidirectionalLinks = null;
  String shape;
  String width = "0"; // default, ignored
  String height = "0"; // default, ignored
  String orientation = "0";
  String sides = "0";
  static int maxLabelLength = 20; // maximum label length in characters/line
  String tooltip = "";

  private transient Logger log; 

  /**
   * Create node object from properties for a plan object,
   * with the following links:
   * tasks->plan elements
   * allocation->asset
   * expansion->workflow
   * aggregation->mptask
   * tasks->aggregation
   * parent(s)->task
   * workflow->tasks
   */

  public ULPlanNode(PropertyTree p, String communityName) {
    createLogger();

    this.properties = p;
    UID = (String)properties.get(PropertyNames.UID_ATTR);
    tooltip = UID; // default tooltip if none other is set
    objectType = (String)properties.get(PropertyNames.OBJECT_TYPE);
    setNodeShapeParameters();

    // set color, special case assets
    color = (String)properties.get(PropertyNames.AGENT_ATTR);
    if (objectType.equals(PropertyNames.ASSET_OBJECT)) {
      String s = (String)properties.get(PropertyNames.ASSET_CLUSTER);
      if (s != null)
	color = s;
    }

    // add community name to properties
    properties.put(PropertyNames.PLAN_OBJECT_COMMUNITY_NAME, communityName);

    // set Attribute Table Title from properties
    // set task type for single or multi-parent
    String taskType = null;
    if (objectType.equals(PropertyNames.TASK_OBJECT)) {
      taskType = (String)properties.get(PropertyNames.TASK_TYPE);
      if (taskType == null)
        if(log.isDebugEnabled()) {
          log.info("TASK WITH NO TASK TYPE");
        }
      if (taskType.equals(PropertyNames.MPTASK))
	properties.put(PropertyNames.TABLE_TITLE, 
		       PropertyNames.MPTASK + " " + UID);
      else
	properties.put(PropertyNames.TABLE_TITLE, objectType + " " + UID);
    } else if (objectType.equals(PropertyNames.PLAN_ELEMENT_OBJECT))
      properties.put(PropertyNames.TABLE_TITLE, 
		     (String)properties.get(PropertyNames.PLAN_ELEMENT_TYPE) +
		     " " + UID);
    else
      properties.put(PropertyNames.TABLE_TITLE, objectType + " " + UID);

    // set remaining node attributes from properties
    if (objectType.equals(PropertyNames.ASSET_OBJECT)) {
      label = (String)properties.get(PropertyNames.ASSET_NAME);
      if (label != null) {
	if (label.length() > maxLabelLength)
	  label = makeMultiLineLabel(label);
      } else {
	label = (String)properties.get(PropertyNames.ASSET_GROUP_NAME);
	if (label != null) {
	  if (label.length() > maxLabelLength)
	    label = makeMultiLineLabel(label);
	}
      }
    } else if (objectType.equals(PropertyNames.PLAN_ELEMENT_OBJECT)) {
      label = (String)properties.get(PropertyNames.PLAN_ELEMENT_TYPE);
      String taskUID = 
	(String)properties.get(PropertyNames.PLAN_ELEMENT_TASK_UID);
      //      bidirectionalLinks = new Vector(1);
      //      bidirectionalLinks.addElement(taskUID);
      outgoingLinks = new Vector();
      String peType = (String)properties.get(PropertyNames.PLAN_ELEMENT_TYPE);
      if (peType.equals(PropertyNames.PLAN_ELEMENT_ALLOCATION)) {
	String assetUID = 
	  (String)properties.get(PropertyNames.ALLOCATION_ASSET_UID);
	// allocation points to asset
	if (assetUID != null) 
	  outgoingLinks.addElement(assetUID);
      } else if (peType.equals(PropertyNames.PLAN_ELEMENT_EXPANSION)) {
	String workflowUID =
	  (String)properties.get(PropertyNames.EXPANSION_WORKFLOW_UID);
	// expansion points to workflow
	if (workflowUID != null)
	  outgoingLinks.addElement(workflowUID);
      } else if (peType.equals(PropertyNames.PLAN_ELEMENT_AGGREGATION)) {
	// aggregation points to its MPTask
	outgoingLinks.addElement(properties.get(PropertyNames.AGGREGATION_MPTASK_UID));
	// these links duplicate the task-to-plan element links
	// and its parent UIDs point to it
	//	String s = (String)properties.get(PropertyNames.AGGREGATION_PARENT_UIDS);
	//	if (s != null) {
	//	  StringTokenizer st = new StringTokenizer(s, ",");
	//	  incomingLinks = new Vector(st.countTokens());
	//	  while (st.hasMoreTokens()) {
	//	    incomingLinks.addElement(st.nextToken());
	//	  }
	//	}
      }
    } else if (objectType.equals(PropertyNames.TASK_OBJECT)) {
      label = (String)properties.get(PropertyNames.TASK_VERB);
      if (taskType.equals(PropertyNames.SINGLE_TASK)) {
	String parent = (String)properties.get(PropertyNames.TASK_PARENT_UID);
	if (parent != null) {
	  incomingLinks = new Vector(1);
	  incomingLinks.addElement(parent);
	}
      } else if (taskType.equals(PropertyNames.MPTASK)) {
	Object o = properties.get(PropertyNames.TASK_PARENT_UID);
	ArrayList parentUIDs = 
	  (ArrayList)properties.get(PropertyNames.TASK_PARENT_UID);
	incomingLinks = new Vector(parentUIDs);
      }
      String pe = (String)properties.get(PropertyNames.TASK_PLAN_ELEMENT_UID);
      if (pe != null) {
	//	bidirectionalLinks = new Vector(1);
	//	bidirectionalLinks.addElement(pe);
	outgoingLinks = new Vector(1);
	outgoingLinks.addElement(pe);
      }
    } else if (objectType.equals(PropertyNames.WORKFLOW_OBJECT)) {
      label = "workflow";
      // workflow points to its tasks
      String s = (String)properties.get(PropertyNames.WORKFLOW_TASK_UIDS);
      if (s == null) {
	s = "";
      }
      StringTokenizer st = new StringTokenizer(s, ",");
      outgoingLinks = new Vector(st.countTokens());
      while (st.hasMoreTokens()) {
	outgoingLinks.addElement(st.nextToken());
      }
    } else if (objectType.equals(PropertyNames.EVENT_HAPPINESS_CHANGE)) {
      NumberFormat nf = NumberFormat.getInstance();
      nf.setMaximumFractionDigits(0);
      String s = (String)properties.get(PropertyNames.EVENT_CUMULATIVE);
      double happiness = 1;
      try {
	happiness = Double.parseDouble(s);
      } catch (Exception e) {
        if(log.isDebugEnabled()) {
          log.error("ULPlanNode: " + e);
        }
      }
      label = nf.format(happiness * 100) + "%";
      tooltip = (String)properties.get(PropertyNames.EVENT_DESCRIPTION);
      outgoingLinks = new Vector(1);
      outgoingLinks.addElement((String)properties.get(PropertyNames.EVENT_REGARDING));
    }
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Break a label into lines, each less than maxLabelLength.
   * Assumes that uppercase letters are the start of words and
   * inserts spaces or newlines between them.
   */

  private String makeMultiLineLabel(String originalLabel) {
    int i = 0;
    StringBuffer newLabel = new StringBuffer(maxLabelLength * 3);
    StringBuffer nextLine = new StringBuffer(maxLabelLength);
    int n = originalLabel.length();
    while (i < n) {
      int j = getUppercaseIndex(originalLabel, i);
      String tmp = originalLabel.substring(i, j);
      if (nextLine.length() + tmp.length() + 1 >= maxLabelLength) {
	if (nextLine.length() > 0)
	  newLabel.append(nextLine.toString().trim() + "\\n");
	nextLine = new StringBuffer(maxLabelLength);
      }
      nextLine.append(tmp + " ");
      i = j;
    }
    newLabel.append(nextLine.toString().trim());
    return newLabel.toString();
  }

  // helper function for above, return index of next uppercase letter
  // start search at index+1

  private int getUppercaseIndex(String s, int index) {
    for (int i = index+1; i < s.length(); i++) {
      if (!Character.isLowerCase(s.charAt(i)))
	return i;
    }
    return s.length();
  }

  public String getUID() {
    return UID;
  }

  public String getLabel() {
    return label;
  }

  public String getToolTip() {
    return tooltip;
  }

  /**
   * The string that will be used to determine the color of the 
   * node; the agent, or for an asset with a clusterPG, the clusterPG agent.
   */

  public String getColor() {
    return color;
  }

  /**
   * Return null to mean "use same color as fill color".
   * Currently unused.
   */

  public String getBorderColor() {
    return null;
  }

  public String getShape() {
    return shape;
  }

  private void setNodeShapeParameters() {
    if (objectType.equals(PropertyNames.TASK_OBJECT)) {
      shape = "ellipse";
      height = "1.0";
      width = "1.0";
    } else if (objectType.equals(PropertyNames.PLAN_ELEMENT_OBJECT)) {
      String peType = (String)properties.get(PropertyNames.PLAN_ELEMENT_TYPE);
      if (peType.equals(PropertyNames.PLAN_ELEMENT_AGGREGATION)) {
	shape = "trapezium";
	orientation = "-90";
	height = "1.0";
	width = "1.0";
      } else if (peType.equals(PropertyNames.PLAN_ELEMENT_ALLOCATION)) {
	shape = "triangle";
	orientation = "-90";
	height = "1.0";
	width = "1.0";
      } else if (peType.equals(PropertyNames.PLAN_ELEMENT_ASSET_TRANSFER)) {
	shape = "hexagon";
	height = "1.0";
	width = "1.0";
      } else if (peType.equals(PropertyNames.PLAN_ELEMENT_DISPOSITION)) {
	shape = "triangle";
	orientation = "90";
	height = "1.0";
	width = "1.0";
      } else if (peType.equals(PropertyNames.PLAN_ELEMENT_EXPANSION)) {
	shape = "trapezium";
	orientation = "90";
	height = "1.0";
	width = "1.0";
      } else if (peType.equals(PropertyNames.PLAN_ELEMENT_UNKNOWN)) {
	shape = "roundedbox";
	height = "1.0";
	width = "1.0";
      }
    } else if (objectType.equals(PropertyNames.ASSET_OBJECT)) {
      shape = "box";
      height = "1.0";
      width = "1.0";
    } else if (objectType.equals(PropertyNames.WORKFLOW_OBJECT)) {
      shape = "triangle";
      height = "1.0";
      width = "1.0";
    } else if (objectType.equals(PropertyNames.EVENT_HAPPINESS_CHANGE)) {
      shape = "circle";
    } else
      shape = "pentagon"; // unknown object type
  }

  /**
   * The number of sides.  A returned value of "0" is ignored (i.e. the
   * default is used).
   */

  public String getSides() {
    return sides;
  }

  /**
   * The width aspect ratio.  A returned value of "0" is ignored 
   * (i.e. the default aspect ratio is used).
   */

  public String getWidth() {
    return width;
  }

  /**
   * The height aspect ratio.  A returned value of "0" is ignored 
   * (i.e. the default aspect ratio is used).
   */

  public String getHeight() {
    return height;
  }

  /**
   * The distortion.  A returned value of "0" is ignored (i.e. the
   * default is used).
   */

  public String getDistortion() {
    return "0";
  }

  /**
   * The orientation.  A returned value of "0" is ignored (i.e. the
   * default is used).
   */

  public String getOrientation() {
    return orientation;
  }

  /**
   * The orientation.  A returned value of "normal" is ignored (i.e. the
   * default is used).
   */

  public String getFontStyle() {
    return "normal";
  }

  /**
   * Return a PropertyTree containing properties (names/values)
   * associated with this node.  Note that names must not
   * have spaces (but values can have spaces).
   * By convention, attribute names are defined with underbars in place
   * of spaces, and the underbars are removed by the table model
   * attached to the JTable that displays the attributes.
   * @see org.cougaar.tools.csmart.ui.monitor.generic.CSMARTFrame#getAttributeTableModel
   */

  public PropertyTree getProperties() {
    return properties;
  }

  /**
   * Return a vector of the nodes at the tail ends of
   * links coming in to this node.  The nodes are identified by UID (i.e.
   * the vector returned is a vector of Strings which are UIDs as defined
   * by the getUID method in this interface).
   * Return null if there are no links.
   */

  public Vector getIncomingLinks() {
    return incomingLinks;
  }

  /**
   * Return a vector of the nodes at the head ends of
   * links going out of this node.  The nodes are identified by UID (i.e.
   * the vector returned is a vector of Strings which are UIDs as defined
   * by the getUID method in this interface).
   * Return null if there are no links.
   */

  public Vector getOutgoingLinks() {
    return outgoingLinks;
  }

  public void addOutgoingLink(String UID) {
    if (outgoingLinks == null)
      outgoingLinks = new Vector(1);
    outgoingLinks.addElement(UID);
  }

  /**
   * Return a vector of the nodes at the other end of bidirectional links.
   * Nodes are identified by UID.
   * Return null if there are no bidirectional links.
   */

  public Vector getBidirectionalLinks() {
    //    return bidirectionalLinks;
    return null;
  }

  public boolean isVisible() {
    return true;
  }

  /**
   * For debugging, print properties.
   */

  private void printProperties(PropertyTree properties) {
    Set keys = properties.keySet();
    System.out.println("Property names/values........");
    for (Iterator j = keys.iterator(); j.hasNext(); ) {
      String s = (String)j.next();
      System.out.println(s + "," + properties.get(s));
    }
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

/*
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

import att.grappa.Attribute;
import att.grappa.Node;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.PropertyTree;
import org.cougaar.util.log.Logger;

import javax.swing.table.AbstractTableModel;
import java.util.Enumeration;
import java.util.Vector;

public class ULPlanTableModel extends AbstractTableModel {
  Vector names;
  Vector values;
  Node node;
  private transient Logger log;

  /**
   * Create table model for the specified node.
   * TODO: determine what type of object the node represents
   * and only add attributes for that object type
   * @param node the node for which to create the table model
   */

  public ULPlanTableModel(Node node) {
    this.node = node;
    createLogger();
    names = new Vector();
    values = new Vector();
    Enumeration keys = node.getLocalAttributeKeys();
    // add attributes for indexed properties:
    // TASK_PARENT_UID, TASK_PREP_PHRASE, ASSET_PROPERTY, ASSET_GROUP_NAME
    while (keys.hasMoreElements()) {
      String key = (String)keys.nextElement();
      // TASK_PARENT_UID_n, add as TASK_PARENT_UID
      if (key.startsWith(PropertyNames.TASK_PARENT_UID))
	addPrefixedAttribute(key, PropertyNames.TASK_PARENT_UID);
      // i.e. "Prepositional_Phrase_To", add as To
      else if (key.startsWith(PropertyNames.TASK_PREP_PHRASE))
	addPrepAttribute(key, PropertyNames.TASK_PREP_PHRASE);
      // these are "Asset_Property_i_PGName:Attribute, add as PGName:Attribute
      else if (key.startsWith(PropertyNames.ASSET_PROPERTY))
	addAssetPropertyAttribute(key, PropertyNames.ASSET_PROPERTY);
      // ASSET_GROUP_NAME_n, add as ASSET_GROUP_NAME
      else if (key.startsWith(PropertyNames.ASSET_GROUP_ASSETS))
	addPrefixedAttribute(key, PropertyNames.ASSET_GROUP_ASSETS);
    }
    addAttribute(PropertyNames.UID_ATTR);
    addAttribute(PropertyNames.AGENT_ATTR);
    addAttribute(PropertyNames.PLAN_OBJECT_COMMUNITY_NAME);
    addAttribute(PropertyNames.TASK_DIRECT_OBJECT_UID);
    addAttribute(PropertyNames.TASK_PLAN_ELEMENT_UID);
    addAttribute(PropertyNames.TASK_VERB);
    addAttribute(PropertyNames.PLAN_ELEMENT_TASK_UID);
    addAttribute(PropertyNames.PLAN_ELEMENT_TYPE);
    addAttribute(PropertyNames.PLAN_ELEMENT_ESTIMATED_RESULT);
    addAttribute(PropertyNames.PLAN_ELEMENT_REPORTED_RESULT);
    addAttribute(PropertyNames.PLAN_ELEMENT_OBSERVED_RESULT);
    addAttribute(PropertyNames.AGGREGATION_MPTASK_UID);
    addAttribute(PropertyNames.AGGREGATION_PARENT_UIDS);
    addAttribute(PropertyNames.ALLOCATION_ASSET_UID);
    addAttribute(PropertyNames.ALLOCATION_LOCAL_ORG_UID);
    addAttribute(PropertyNames.ALLOCATION_TO_AGENT);
    addAttribute(PropertyNames.ALLOCATION_TASK_UID);
    addAttribute(PropertyNames.ALLOCATION_REMOTE_AGENT_UID);
    addAttribute(PropertyNames.ASSET_GROUP_NAME);
    addAttribute(PropertyNames.ASSET_DESC);
    addAttribute(PropertyNames.ASSET_NAME);
    addAttribute(PropertyNames.ASSET_TRANSFER_ASSET_UID);
    addAttribute(PropertyNames.ASSET_TRANSFER_ASSIGNEE_UID);
    addAttribute(PropertyNames.ASSET_TRANSFER_ASSIGNOR);
    addAttribute(PropertyNames.ASSET_AGENT);
    addAttribute(PropertyNames.DISPOSITION_SUCCESS);
    addAttribute(PropertyNames.EXPANSION_WORKFLOW_UID);
    addAttribute(PropertyNames.WORKFLOW_PARENT_TASK_UID);
    addAttribute(PropertyNames.WORKFLOW_TASK_UIDS);
    addAttribute(PropertyNames.EVENT_TYPE);
    addAttribute(PropertyNames.EVENT_TIME_COMPLETED);
    addAttribute(PropertyNames.EVENT_CUMULATIVE);
    addAttribute(PropertyNames.EVENT_DELTA);
    addAttribute(PropertyNames.EVENT_DESCRIPTION);
    addAttribute(PropertyNames.EVENT_REGARDING);
    addAttribute(PropertyNames.EVENT_VISIBILITY_TIME);
    addAttribute(PropertyNames.EVENT_PUBLISHER);
    addAttribute(PropertyNames.EVENT_SOURCE);
    addAttribute(PropertyNames.ESTIMATED_ALLOCATION_RESULT_END_TIME);
    addAttribute(PropertyNames.ESTIMATED_ALLOCATION_RESULT_FAILURE_REASON);
    addAttribute(PropertyNames.REPORTED_ALLOCATION_RESULT_END_TIME);
    addAttribute(PropertyNames.REPORTED_ALLOCATION_RESULT_FAILURE_REASON);
    addAttribute(PropertyNames.OBSERVED_ALLOCATION_RESULT_END_TIME);
    addAttribute(PropertyNames.OBSERVED_ALLOCATION_RESULT_FAILURE_REASON);
    makePrettyNames();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public int getColumnCount() { 
    return 2;
  }

  public int getRowCount() { 
    return names.size();
  }

  public String getColumnName(int col) {
    if (col == 0)
      return "Name";
    else if (col == 1)
      return "Value";
    return "";
  }

  public Object getValueAt(int row, int col) { 
    if (col == 0)
      return names.elementAt(row);
    else if (col == 1)
      return values.elementAt(row);
    return "";
  }

  /** 
   * Modify the names by substituting spaces for underlines.
   */

  private void makePrettyNames() {
    for (int i = 0; i < names.size(); i++) {
      String name = (String)names.elementAt(i);
      name = name.replace('_', ' ');
      names.setElementAt(name, i);
    }
  }

  private void addAttribute(String name) {
    Attribute a = node.getLocalAttribute(name);
    if (a == null) {
      // Bug 1921: Just too verbose? Or interesting?
//       if(log.isDebugEnabled()) {
//         log.debug("ULPlanTableModel: attribute not found: " +
//                   name);
//       }
      return;
    }
    names.addElement(name);
    values.addElement(a.getValue());
  }

  /**
   * Add a prefixed asset property; dropping the prefix and index.
   * Prefixes are of the form: prefix_index_
   */

  private void addAssetPropertyAttribute(String name, String prefix) {
    int i = name.indexOf('_', prefix.length()+1);
    if (i == -1) {
      if(log.isDebugEnabled()) {
        log.debug("Bad prefix: " + prefix + " for name: " + name);
      }
      names.addElement(name); // badly formed prefix
    } else
      names.addElement(name.substring(i+1));
    values.addElement(node.getLocalAttribute(name).getValue());
  }

  /**
   * Add an attribute which may or may not be indexed;
   * i.e. of the form AttributeName_n; drop the final _n if present,
   * which is the same as simply using the prefix.
   */

  private void addPrefixedAttribute(String name, String prefix) {
    names.addElement(prefix);
    values.addElement(node.getLocalAttribute(name).getValue());
  }

  /**
   * Add a prepositional phrase attribute; stripping off prefix_.
   */

  private void addPrepAttribute(String name, String prefix) {
    if (name.startsWith(prefix+"_"))
      names.addElement(name.substring(prefix.length()+1));
    else
      names.addElement(name);
    values.addElement(node.getLocalAttribute(name).getValue());
  }

  private void addNamedAttribute(String name) {
    names.addElement(name);
    values.addElement(node.getLocalAttribute(name).getValue());
  }

}

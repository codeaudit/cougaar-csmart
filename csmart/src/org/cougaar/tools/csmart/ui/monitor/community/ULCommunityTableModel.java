/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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

import java.util.*;
import javax.swing.table.AbstractTableModel;
import att.grappa.*;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Provide the values for the attribute table for the specified node
 * in a community graph.
 * This gets the attributes from the grappa Node and returns the
 * values to display in the attribute table.
 */

public class ULCommunityTableModel extends AbstractTableModel {
  Vector names;
  Vector values;
  Node node;
  private transient Logger log;

  /**
   * Create table model for the specified node.
   * @param node the node for which to create the table model
   */

  public ULCommunityTableModel(Node node) {
    this.node = node;
    names = new Vector();
    values = new Vector();
    addAttribute(PropertyNames.COMMUNITY_MEMBERS);
    makePrettyNames();
    log = CSMART.createLogger("org.cougaar.tools.csmart.ui.monitor.community");
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
      if(log.isDebugEnabled()) {
        log.warn("ULCommunityTableModel: attribute not found: " +
                 name);
      }
      return;
    }
    names.addElement(name);
    values.addElement(a.getValue());
  }
    
}

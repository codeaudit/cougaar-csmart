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

package org.cougaar.tools.csmart.ui.monitor.society;

import java.util.*;
import javax.swing.table.AbstractTableModel;
import att.grappa.*;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * Provide the values for the attribute table for the specified node
 * in an agents (society) graph.
 * This gets the attributes from the grappa Node and returns the
 * values to display in the attribute table.
 */

public class ULSocietyTableModel extends AbstractTableModel {
  Vector names;
  Vector values;
  Node node;
  private transient Logger log;

  /**
   * Create table model for the specified node.
   * @param node the node for which to create the table model
   */

  public ULSocietyTableModel(Node node) {
    createLogger();
    this.node = node;
    names = new Vector();
    values = new Vector();
    addAttribute(PropertyNames.UID_ATTR);
    addAttribute(PropertyNames.ORGANIZATION_NAME);
    // TODO: for debugging, probably don't really want to show this
    addAttribute(PropertyNames.ORGANIZATION_KEY_NAME);
    addAttribute(PropertyNames.ORGANIZATION_LOCATION_START_TIME);
    addAttribute(PropertyNames.ORGANIZATION_LOCATION_END_TIME);
    Enumeration keys = node.getLocalAttributeKeys();
    Vector orderedKeys = new Vector();
    while (keys.hasMoreElements())
      orderedKeys.add(keys.nextElement());
    Collections.sort(orderedKeys, new KeyComparator());
    // add attributes for properties that are vectors
    for (int i = 0; i < orderedKeys.size(); i++) {
      String key = (String)orderedKeys.elementAt(i);
      if (key.startsWith(PropertyNames.ORGANIZATION_RELATED_TO)) {
	names.addElement(key.substring(key.lastIndexOf('_')+1));
	values.addElement(node.getLocalAttribute(key).getValue());
      } else if (key.startsWith(PropertyNames.ORGANIZATION_LOCATION_ELEMENT)) {
        names.addElement(key);
	values.addElement(node.getLocalAttribute(key).getValue());
      }
    }
    makePrettyNames();
  }

  private void createLogger() {
    log = CSMART.createLogger("org.cougaar.tools.csmart.ui.monitor.society");
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
        log.debug("ULSocietyTableModel: attribute not found: " +
                  name);
      }
      return;
    }
    names.addElement(name);
    values.addElement(a.getValue());
  }

  class KeyComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      String s1 = (String)o1;
      if (s1.startsWith(PropertyNames.ORGANIZATION_RELATED_TO))
	s1 = s1.substring(s1.lastIndexOf('_')+1);
      String s2 = (String)o2;
      if (s2.startsWith(PropertyNames.ORGANIZATION_RELATED_TO))
	s2 = s2.substring(s2.lastIndexOf('_')+1);
      return s1.compareTo(s2);
    }

    public boolean equals(Object o) {
      return this.equals(o);
    }
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

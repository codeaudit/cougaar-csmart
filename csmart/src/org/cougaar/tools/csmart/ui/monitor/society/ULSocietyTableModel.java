/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.society;

import java.util.*;
import javax.swing.table.AbstractTableModel;
import att.grappa.*;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames;

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

  /**
   * Create table model for the specified node.
   * @param node the node for which to create the table model
   */

  public ULSocietyTableModel(Node node) {
    this.node = node;
    names = new Vector();
    values = new Vector();
    addAttribute(PropertyNames.UID_ATTR);
    addAttribute(PropertyNames.ORGANIZATION_NAME);
    // TODO: for debugging, probably don't really want to show this
    addAttribute(PropertyNames.ORGANIZATION_KEY_NAME);
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
      }
    }
    makePrettyNames();
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
      System.out.println("ULSocietyTableModel: attribute not found: " +
			 name);
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
}

/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.community;

import java.util.*;
import javax.swing.table.AbstractTableModel;
import att.grappa.*;

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
      System.out.println("ULCommunityTableModel: attribute not found: " +
			 name);
      return;
    }
    names.addElement(name);
    values.addElement(a.getValue());
  }
    
}

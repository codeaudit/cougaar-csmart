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
package org.cougaar.tools.csmart.ui.experiment;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.Component;
import java.util.*;
import java.lang.reflect.Array;

import org.cougaar.tools.csmart.core.property.Property;

public abstract class PropTableModelBase extends AbstractTableModel {
    public static final int LABEL_COL = 0;
    public static final int VALUE_COL = 1;
    public static final int NCOLS = 2;
    private List properties = new ArrayList();

    protected TableCellRenderer myRenderer = new DefaultTableCellRenderer() {
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column)
        {
            Component result =
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (result instanceof JComponent) {
                JComponent jc = (JComponent) result;
                Property prop = getProperty(row);
                if (column == LABEL_COL && prop.getHelp() != null) {
                    jc.setToolTipText("Click for help");
                } else {
                    jc.setToolTipText(prop.getToolTip());
                }
            }
            return result;
        }
    };

    public TableCellRenderer getCellRenderer() {
        return myRenderer;
    }

    public Property getProperty(int row) {
        return (Property) properties.get(row);
    }

    public void addProperty(Property prop) {
        int sz = properties.size();
        properties.add(prop);
        // ensure that properties are in alphabetical order
        Collections.sort(properties, propertyComparator);
        fireTableRowsInserted(sz, sz);
    }

    private static Comparator propertyComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
          Property p1 = (Property) o1;
          Property p2 = (Property) o2;
          return p1.getName().compareTo(p2.getName());
        }
      };

    public void removeProperty(Property prop) {
        int sz = properties.indexOf(prop);
        if (sz >= 0) {
            properties.remove(sz);
            fireTableRowsDeleted(sz, sz);
        }
    }

    public void clear() {
        int sz = properties.size();
        properties.clear();
        if (sz > 0) fireTableRowsDeleted(0, sz - 1);
    }

    public int getColumnCount() {
        return NCOLS;
    }

    public Class getColumnClass(int col) {
        switch (col) {
        case LABEL_COL: return String.class;
        case VALUE_COL: return String.class;
        }
        return null;
    }

    public String getColumnName(int col) {
        switch (col) {
        case LABEL_COL: return "Name";
        case VALUE_COL: return "Value";
        }
        return null;
    }

    public int getRowCount() {
        return properties.size();
    }

    public boolean isCellEditable(int row, int col) {
        return col == VALUE_COL;
    }

    protected abstract String render(Property prop);

    protected abstract void setValue(Property prop, Object newValue);

    public Object getValueAt(int row, int col) {
        Property prop = getProperty(row);
        switch (col) {
        case LABEL_COL: return prop.getLabel();
        case VALUE_COL: return render(prop);
        }
        return null;
    }

    public void setValueAt(Object newValue, int row, int col) {
        Property prop = getProperty(row);
        setValue(prop, newValue);
    }

  public int getRowForProperty(Property p) {
    return properties.indexOf(p);
  }
}

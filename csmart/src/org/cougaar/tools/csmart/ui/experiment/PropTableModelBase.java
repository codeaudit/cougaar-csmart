/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.experiment;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.Component;
import java.util.*;
import java.lang.reflect.Array;
import org.cougaar.tools.csmart.ui.component.*;

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
        fireTableRowsInserted(sz, sz);
    }

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

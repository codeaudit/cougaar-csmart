/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.configbuilder;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.lang.reflect.Array;
import java.util.SortedSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.EtchedBorder;
import org.cougaar.tools.csmart.core.property.InvalidPropertyValueException;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyHelper;
import org.cougaar.tools.csmart.core.property.range.Range;
import org.cougaar.tools.csmart.ui.Browser;
import org.cougaar.tools.csmart.ui.experiment.PropTableModelBase;

/**
 * A JTable that displays property names and values.
 */

public class PropertyTable extends JTable {
  private class Model extends PropTableModelBase {
    boolean isEditable;

    public Model(boolean isEditable) {
      this.isEditable = isEditable;
    }

    public boolean isCellEditable(int row, int col) {
      if (!isEditable)
	return false;
      return super.isCellEditable(row, col);
    }

    /**
     * If property value is not set, then render it as "<not set>".
     */
    public String render(Property prop) {
      Object o = null;
      if (prop.isValueSet())
	o = prop.getValue();
      if (o == null) return "<not set>";
      if (o.getClass().isArray()) {
        StringBuffer buf = new StringBuffer();
        buf.append('{');
        for (int i = 0, n = Array.getLength(o); i < n; i++) {
          if (i > 0) buf.append(",");
          buf.append(Array.get(o, i));
        }
        buf.append("}");
        return buf.toString();
      } else {
        return o.toString();
      }
    }
    public void setValue(Property p, Object value) {
      try {
        if (value instanceof String && value.toString().equals("")) {
          value = null;           // Use default
        } else {
          value = PropertyHelper.validateValue(p, value);
        }
        p.setValue(value);
      } catch (InvalidPropertyValueException e) {
        Object[] goodValues = new TreeSet(p.getAllowedValues()).toArray();
        JList goodValueList = new JList(goodValues);
        goodValueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Object[] msg = {
          "Invalid Property Value: " + value,
          "Use one of the following:",
          goodValueList
        };
        JOptionPane.showMessageDialog(PropertyTable.this,
                                      msg,
                                      "Invalid Property Value",
                                      JOptionPane.ERROR_MESSAGE);
        int selected = goodValueList.getSelectedIndex();
        if (selected >= 0) {
          Range goodRange = (Range) goodValues[selected];
          if (goodRange.getMinimumValue().equals(goodRange.getMaximumValue())) {
            p.setValue(goodRange.getMinimumValue());
          }
        }
      }
    }
  }

  //  Model model = new Model();
  Model model;
      
  public PropertyTable(boolean isEditable) {
    model = new Model(isEditable);
    setModel(model);
    setDefaultRenderer(String.class, model.getCellRenderer());
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int col = columnAtPoint(e.getPoint());
        int row = rowAtPoint(e.getPoint());
        if (col == Model.LABEL_COL) {
          Property prop = model.getProperty(row);
          if (prop != null) {
            URL url = prop.getHelp();
            if (url != null) Browser.setPage(url);
          }
        }
      }
    });
  }

  /**
   * Set the cell editor to be a combo box or check box if appropriate.
   * @param row row for which to return editor
   * @param column column for which to return editor
   * @return TableCellEditor editor for the row and column
   */

  public TableCellEditor getCellEditor(int row, int column) {
    if (column == model.VALUE_COL) {
      Property prop = model.getProperty(row);
      Class propClass = prop.getPropertyClass();
      if (propClass.equals(Boolean.class))
        return new DefaultCellEditor(new JCheckBox());
      Set allowedValues = prop.getAllowedValues();
      if (allowedValues != null) {
        JComboBox comboBox = null;
        for (Iterator i = allowedValues.iterator(); i.hasNext(); ) {
          Range allowedRange = (Range) i.next();
          Object min = allowedRange.getMinimumValue();
          if (min.equals(allowedRange.getMaximumValue())) {
            if (comboBox == null) {
              comboBox = new JComboBox();
              comboBox.setEditable(true);
            }
            comboBox.addItem(min);
          } else {
            comboBox = null;
            break;
          }
        }
        if (comboBox != null) return new DefaultCellEditor(comboBox);
      }
    }
    return super.getCellEditor(row, column);
  }

  // borrowed from javax.swing.JTable
  private static class BooleanRenderer extends JCheckBox implements TableCellRenderer
  {
    public BooleanRenderer() {
      super();
      setHorizontalAlignment(JLabel.CENTER);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, 
                                                   boolean hasFocus, 
                                                   int row, int column) {
      if (isSelected) {
        setForeground(table.getSelectionForeground());
        super.setBackground(table.getSelectionBackground());
      } else {
        setForeground(table.getForeground());
        setBackground(table.getBackground());
      }
      //      setSelected((value != null && ((Boolean)value).booleanValue()));
      setSelected(value != null && 
                  new Boolean(value.toString()).booleanValue());
      return this;
    }
  } // end BooleanRenderer

  public TableCellRenderer getCellRenderer(int row, int column) {
    if (column == model.VALUE_COL) {
      Property prop = model.getProperty(row);
      Class propClass = prop.getPropertyClass();
      if (propClass.equals(Boolean.class))
        return new BooleanRenderer();
    }
    return super.getCellRenderer(row, column);
  }

  public void addProperty(Property prop) {
    model.addProperty(prop);
  }

  public void removeProperty(Property prop) {
    model.removeProperty(prop);
  }

  public void removeAll() {
    model.clear();
  }
}

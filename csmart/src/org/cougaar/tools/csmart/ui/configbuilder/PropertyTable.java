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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.lang.reflect.Array;
import java.util.EventObject;
import java.util.SortedSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import javax.swing.border.EtchedBorder;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
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
  private PropertyChangeListener editorRemover = null;

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
        Set allowedValues = p.getAllowedValues();
        if (allowedValues == null) {
          JOptionPane.showMessageDialog(PropertyTable.this,
                                        "Invalid Property Value, must be of class: " + p.getPropertyClass().toString(),
                                        "Invalid Property Value",
                                        JOptionPane.ERROR_MESSAGE);
          return;
        } else {
          Object[] goodValues = new TreeSet(allowedValues).toArray();
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
  } // end Model class

  Model model;
      
  /**
   * Create a <code>PropertyTable</code> which is
   * used to edit or view properties of societies and recipes.
   * @param isEditable true if component can be edited
   */
  public PropertyTable(boolean isEditable) {
    model = new Model(isEditable);
    setModel(model);
    setDefaultRenderer(String.class, model.getCellRenderer());
    addMouseListener(new MyMouseAdapter(this));
  }

  /**
   * Set the cell editor to be a combo box or check box if appropriate.
   * @param row row for which to return editor
   * @param column column for which to return editor
   * @return <code>TableCellEditor</code> editor for the row and column
   */
  public TableCellEditor getCellEditor(int row, int column) {
    if (column == model.VALUE_COL) {
      Property prop = model.getProperty(row);
      Class propClass = prop.getPropertyClass();
      if (propClass.equals(Boolean.class)) {
        return new DefaultCellEditor(new JCheckBox());
      }
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
        if (comboBox != null) {
          return new DefaultCellEditor(comboBox);
        }
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

  /**
   * Set the cell renderer to be a check box if editing a boolean value.
   * @param row row for which to return renderer
   * @param column column for which to return renderer
   * @return <code>TableCellRenderer</code> renderer for the row and column
   */
  public TableCellRenderer getCellRenderer(int row, int column) {
    if (column == model.VALUE_COL) {
      Property prop = model.getProperty(row);
      Class propClass = prop.getPropertyClass();
      if (propClass != null && propClass.equals(Boolean.class))
        return new BooleanRenderer();
    }
    return super.getCellRenderer(row, column);
  }

  /**
   * Add a property to the table.
   * @param <code>Property</code> to add
   */
  public void addProperty(Property prop) {
    model.addProperty(prop);
  }

  /**
   * Remove a property from the table.
   * @param <code>Property</code> to remove
   */
  public void removeProperty(Property prop) {
    model.removeProperty(prop);
  }

  /**
   * Remove all properties from the table.
   */
  public void removeAll() {
    model.clear();
  }


  /**
   * Pops up a menu with Help and Delete actions
   * for the property in the row in which the mouse is located.
   */

  public class MyMouseAdapter extends MouseAdapter {
    JTable table;
    JPopupMenu menu;
    int row;

    private AbstractAction helpAction =
      new AbstractAction("Help") {
          public void actionPerformed(ActionEvent e) {
            Property prop = model.getProperty(row);
            if (prop != null) {
              URL url = prop.getHelp();
              if (url != null) Browser.setPage(url);
            }
          }
        };

    /**
     * Just delete property from configurable component.
     * Listeners on the configurable components,
     * in PropertyEditorPanel actually update the UI.
     */

    private AbstractAction deleteAction =
      new AbstractAction("Delete") {
          public void actionPerformed(ActionEvent e) {
            Property prop = model.getProperty(row);
            ConfigurableComponent cc = prop.getConfigurableComponent();
            cc.removeProperty(prop);
          }
        };

    private Action[] actions = {
      helpAction,
      deleteAction
    };

    public MyMouseAdapter(JTable table) {
      super();
      this.table = table;
      menu = new JPopupMenu();
      for (int i = 0; i < actions.length; i++)
        menu.add(actions[i]);
    }

    public void mouseClicked(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }
  
    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }
  
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) doPopup(e);
    }

    private void doPopup(MouseEvent e) {
      row = table.rowAtPoint(e.getPoint());
      if (row == -1) return;
      menu.show(table, e.getX(), e.getY());
    }
  }

  /** 
   * Override this to install our own focus manager property change listener.
   * @param row row to edit
   * @param column column to edit
   * @param e event passed to super.editCellAt
   */
  public boolean editCellAt(int row, int column, EventObject e) {
    if (editorRemover == null) { 
      KeyboardFocusManager fm =
        KeyboardFocusManager.getCurrentKeyboardFocusManager();
      editorRemover = new CellEditorRemover(fm);
      fm.addPropertyChangeListener("focusOwner", editorRemover);
    }
    return super.editCellAt(row, column, e);
  }

  /**
   * Override this to deinstall our focus manager property change listener.
   */

  public void removeNotify() {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().
      removePropertyChangeListener("focusOwner", editorRemover);
    editorRemover = null;
    super.removeNotify();
  }

  /**
   * Override this to deinstall our focus manager property change listener.
   */

  public void removeEditor() {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().
      removePropertyChangeListener("focusOwner", editorRemover);
    editorRemover = null;
    super.removeEditor();
  }

  /** 
   * This class tracks changes in the keyboard focus state. It is used
   * when the JTable is editing to determine when to stop the edit.
   * If focus switches to a component outside of the jtable, but in the
   * same window, this will stop editing.
   * Adapted from JTable.
   */

  class CellEditorRemover implements PropertyChangeListener {
    KeyboardFocusManager focusManager;
    
    public CellEditorRemover(KeyboardFocusManager fm) {
      this.focusManager = fm;
    }

    public void propertyChange(PropertyChangeEvent ev) {
      if (!isEditing()) {
        return;
      }
      Component c = focusManager.getFocusOwner();
      while (c != null) {
        if (c == PropertyTable.this) {
          // focus remains inside the table
          return;
        } else if ((c instanceof Window) ||
                   (c instanceof java.applet.Applet && 
                    c.getParent() == null)) {
          // focus is inside window and outside table
          if (c == SwingUtilities.getRoot(PropertyTable.this)) {
            getCellEditor().stopCellEditing();
          }
          break;
        }
        c = c.getParent();
      }
    }
  } // end CellEditorRemover

}

/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.community;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;

public class CommunityTable extends JTable {
  CommunityTableUtils communityTableUtils;
  MyTableModelListener myTableModelListener;

  public CommunityTable() {
    super(new TableSorter(new DatabaseTableModel()));
    setAutoResizeMode(AUTO_RESIZE_OFF);
    addMouseListener(new CommunityTableMouseAdapter(this));
    ((TableSorter)getModel()).addMouseListenerToHeaderInTable(this);
    communityTableUtils = (CommunityTableUtils)getModel();
    myTableModelListener = new MyTableModelListener();
  }
  
  public void setAssemblyId(String id) {
    ((DatabaseTableModel)((TableSorter)getModel()).getModel()).setAssemblyId(id);
  }

  public boolean isCellEditable(int row, int column) {
    return getModel().isCellEditable(row, column);
  }

  public TableCellEditor getCellEditor(int row, int column) {
    if (getModel().getColumnClass(column).equals(String.class)) {
      ArrayList values = communityTableUtils.getKnownValues(column);
      if (values != null && values.size() != 0) {
        JComboBox comboBox = new JComboBox(new Vector(values));
        comboBox.setEditable(true);
        return new DefaultCellEditor(comboBox);
      }
    } 
    return super.getCellEditor(row, column);
  }

  /**
   * Handle table change event and then notify MyTableModelListener
   * which recomputes the column widths.
   * It's necessary to implement the table model listeners this way,
   * rather than simply adding MyTableModelListener to the table,
   * because MyTableModelListener must be the last listener invoked
   * to ensure that any table model changes have been made before
   * the column widths are computed.
   */
  public void tableChanged(TableModelEvent e) {
    super.tableChanged(e);
    if (myTableModelListener != null)
      myTableModelListener.tableChanged(e);
  }

  /**
   * Recompute the column widths when the whole table changes.
   */
  private class MyTableModelListener implements TableModelListener {
    public void tableChanged(TableModelEvent e) {
      if (e == null) // ignore these
        return;
      if ((e.getType() == TableModelEvent.INSERT ||
           e.getType() == TableModelEvent.UPDATE) &&
          e.getColumn() == TableModelEvent.ALL_COLUMNS) {
        TableModel model = getModel();
        int nColumns = model.getColumnCount();
        int nRows = model.getRowCount();
        TableCellRenderer defaultHeaderRenderer = 
          getTableHeader().getDefaultRenderer();
        for (int i = 0; i < nColumns; i++) {
          TableColumn column = getColumnModel().getColumn(i);
          TableCellRenderer headerRenderer = column.getHeaderRenderer();
          if (headerRenderer == null)
            headerRenderer = defaultHeaderRenderer;
          Component comp =
            headerRenderer.getTableCellRendererComponent(CommunityTable.this,
                                                   column.getHeaderValue(), 
                                                   false, false, 0, 0);
          int width = comp.getPreferredSize().width;
          TableCellRenderer cellRenderer = column.getCellRenderer();
          if (cellRenderer == null)
            cellRenderer = getDefaultRenderer(model.getColumnClass(i));
          // use the JTable.getValueAt method which gets the correct
          // value even if the user reorders the columns
          for (int j = 0; j < nRows; j++) {
            comp = 
              cellRenderer.getTableCellRendererComponent(CommunityTable.this, 
                                                         getValueAt(j, i),
                                                         false, false, j, i);
            width = Math.max(width, comp.getPreferredSize().width);
          }
          width += 10; // fudge factor
          column.setPreferredWidth(width);
          column.setMinWidth(width);
        }
      }
    }
  }

}


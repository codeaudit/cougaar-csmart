/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.community;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Pops up a menu with the Delete action to delete
 * the row in which the mouse is located.
 */

public class CommunityTableMouseAdapter extends MouseAdapter {
  JTable table;
  JPopupMenu menu;
  int row;

  private AbstractAction deleteAction =
    new AbstractAction("Delete") {
        public void actionPerformed(ActionEvent e) {
          delete();
        }
      };

  private Action[] actions = {
      deleteAction
  };

  public CommunityTableMouseAdapter(JTable table) {
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

  /**
   * The menu only contains the "Delete" action.
   * Deletions can only be done from tables that
   * display the attribute ids and values for 
   * a community or entity, in other words, not for tables
   * that are a join of information; hence if the table
   * doesn't have exactly 3 columns, don't display a popup menu.
   * TODO: should be a better way to detect the join?
   */

  private void doPopup(MouseEvent e) {
    row = table.rowAtPoint(e.getPoint());
    if (row == -1) return;
    if (table.getModel().getColumnCount() == 3)
      menu.show(table, e.getX(), e.getY());
  }

  private void delete() {
    ((CommunityTableUtils)table.getModel()).deleteRow(row);
  }
}

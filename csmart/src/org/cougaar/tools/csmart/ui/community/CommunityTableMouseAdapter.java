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

package org.cougaar.tools.csmart.ui.community;

import java.awt.event.*;
import javax.swing.*;

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

  private void doPopup(MouseEvent e) {
    row = table.rowAtPoint(e.getPoint());
    if (row == -1) return;
    menu.show(table, e.getX(), e.getY());
  }

  private void delete() {
    ((CommunityTableUtils)table.getModel()).deleteRow(row);
  }
}

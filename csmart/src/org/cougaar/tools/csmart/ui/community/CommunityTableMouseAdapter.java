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

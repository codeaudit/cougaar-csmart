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

package org.cougaar.tools.csmart.ui.viewer;

import org.cougaar.tools.csmart.core.db.DBConflictHandler;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Methods to put up a wait cursor and consume mouse events while
 * a time consuming task is being done in the background.
 * Also a method to create a UI based <code>DBConflictHandler</code>
 */
public class GUIUtils {

  static final Cursor waitCursor    =
    Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
  static final Cursor defaultCursor =
    Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

  public static void timeConsumingTaskStart(Component c) {
    JFrame frame = null;
    if (c != null)
      frame = (JFrame) SwingUtilities.getRoot(c);
    if (frame == null)
      return;
    Component glass = frame.getGlassPane();
    glass.setCursor(waitCursor);
    glass.addMouseListener(new MyMouseListener());
    glass.setVisible(true);
  }

  public static void timeConsumingTaskEnd(Component c) {
    JFrame frame = null;
    if (c != null)
      frame = (JFrame) SwingUtilities.getRoot(c);
    if (frame == null) 
      return;
    Component glass = frame.getGlassPane();
    MouseListener[] mls = 
      (MouseListener[])glass.getListeners(MouseListener.class);
    for (int i = 0; i < mls.length; i++)
      if (mls[i] instanceof MyMouseListener)
        glass.removeMouseListener(mls[i]);
    glass.setCursor(defaultCursor);
    glass.setVisible(false);
  }

  static  class MyMouseListener extends MouseInputAdapter {
    public void mouseClicked(MouseEvent e) {
      e.consume();
    }
    public void mouseDragged(MouseEvent e) {
      e.consume();
    }
    public void mouseEntered(MouseEvent e) {
      e.consume();
    }
    public void mouseExited(MouseEvent e) {
      e.consume();
    }
    public void mouseMoved(MouseEvent e) {
      e.consume();
    }
    public void mousePressed(MouseEvent e) {
      e.consume();
    }
    public void mouseReleased(MouseEvent e) {
      e.consume();
    }
  }

  /**
   * Create a new window to prompt the user to handle conflicts in 
   * saving to the database.
   *
   * @param parent a <code>Component</code> frame to base this one on, may be null
   * @return a <code>DBConflictHandler</code> to show
   */
  public static DBConflictHandler createSaveToDbConflictHandler(final Component parent) {
    return new GUIDBConflictHandler(parent);
  }

}

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

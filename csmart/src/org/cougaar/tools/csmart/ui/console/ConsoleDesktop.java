/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.console;

import java.beans.PropertyVetoException;
import java.util.Hashtable;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Insets;
import javax.swing.*;
import javax.swing.event.InternalFrameListener;

import org.cougaar.tools.csmart.ui.component.NodeComponent;
import org.cougaar.tools.server.NodeServesClient;

public class ConsoleDesktop extends JDesktopPane {
  private static final int M = 20;
  private static final int frameXOffset = 20, frameYOffset = 20;
  private int frameCount = 0;
  Hashtable myFrames = new Hashtable();
  ComponentListener myComponentListener = new ComponentAdapter() {
    public void componentMoved(ComponentEvent e) {
      ConsoleDesktop.this.componentMoved(e.getComponent(), true);
    }
    public void componentShown(ComponentEvent e) {
      ConsoleDesktop.this.componentMoved(e.getComponent(), true);
    }
    public void componentResized(ComponentEvent e) {
      ConsoleDesktop.this.componentResized(e.getComponent());
    }
  };

  public ConsoleDesktop() {
    this.addComponentListener(myComponentListener);
  }

  /**
   * Add a node output frame to the desktop.
   * The frame is iconified and not selected.
   */

  public void addNodeFrame(NodeComponent node, 
                           ConsoleNodeListener listener,
                           InternalFrameListener frameListener,
                           JScrollPane pane,
                           JRadioButton statusButton,
                           String logFileName,
                           NodeServesClient nsc,
                           CSMARTConsole console) {
    JInternalFrame frame = 
      new ConsoleInternalFrame(node, listener, pane,
                               statusButton, logFileName, nsc, console);
    //Set the window's location.
    frameCount++;
    Insets insets = this.getInsets();
    int dx = getWidth() - insets.left - insets.right - frame.getWidth();
    int dy = getHeight() - insets.top - insets.bottom - frame.getHeight();
    int x = (dx <= 0) ? 0 : ((frameXOffset * frameCount) % dx);
    int y = (dy <= 0) ? 0 : ((frameYOffset * frameCount) % dy);
    frame.setLocation(x, y);
    frame.addInternalFrameListener(frameListener);
    addFrame(frame, true);
    myFrames.put(node.getShortName(), frame);
  }

  public ConsoleInternalFrame getNodeFrame(String nodeName) {
    return (ConsoleInternalFrame)myFrames.get(nodeName);
  }

  protected void addImpl(Component c, Object constraints, int index) {
    componentMoved(c, false);
    super.addImpl(c, constraints, index);
    c.addComponentListener(myComponentListener);
  }

  private void componentMoved(Component c, boolean peek) {
    if (c == this) return;      // Don't care about this
    Rectangle bb = c.getBounds();
    Insets insets = this.getInsets();
    int mx = peek ? M : bb.width;
    int my = peek ? M : bb.height;
    int x = Math.max(0, Math.min(bb.x, this.getWidth() - insets.left - insets.right - mx));
    int y = Math.max(0, Math.min(bb.y, this.getHeight() - insets.top - insets.bottom - my));
    if (bb.x > x || bb.y > y) {
      c.setLocation(x, y);
    }
  }

  private void componentResized(Component c) {
    if (c != this) return;      // Don't care about these
    JInternalFrame[] frames = getAllFrames();
    for (int i = 0; i < frames.length; i++) {
      componentMoved(frames[i], true);
    }
  }

  /**
   * Add an internal frame to the desktop, and optionally iconify it.
   */

  public void addFrame(JInternalFrame frame, boolean iconify) {
    frame.setVisible(true);
    add(frame, JLayeredPane.DEFAULT_LAYER);
    if (iconify) {
      try {
        frame.setIcon(true);
      } catch (PropertyVetoException e) {
      }
    }
  }
}

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

package org.cougaar.tools.csmart.ui.console;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyVetoException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The internal desktop in the console where the Node output windows appear.
 * Tracks frame locations to help keep them on the screen.
 **/
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
   * @param frame the frame to add
   * @param nodeName the name of the node
   */
  public void addNodeFrame(JInternalFrame frame, String nodeName) {
    //Set the window's location.
    frameCount++;
    Insets insets = this.getInsets();
    int dx = getWidth() - insets.left - insets.right - frame.getWidth();
    int dy = getHeight() - insets.top - insets.bottom - frame.getHeight();
    int x = (dx <= 0) ? 0 : ((frameXOffset * frameCount) % dx);
    int y = (dy <= 0) ? 0 : ((frameYOffset * frameCount) % dy);
    frame.setLocation(x, y);
    addFrame(frame, true);
    myFrames.put(nodeName, frame);
  }

  /**
   * When we're done with a Node Frame, remove it, and remove
   * our listener. Then dispose of the Frame -- which
   * will recurse to get rid of the ConsoleTextPane,
   * ConsoleStyledDocument.
   **/
  public void removeNodeFrame(String nodeName) {
    //    System.out.println("Desktop.removeNodeFrame " + nodeName);
    JInternalFrame frame = (JInternalFrame) myFrames.remove(nodeName);
    cleanFrame(frame);
  }

  public void removeFrame(JInternalFrame frame) {
    // loop through myFrames to find the one, and remove it
    Enumeration names = myFrames.keys();
    while (names.hasMoreElements()) {
      String title = (String) names.nextElement();
      JInternalFrame cand = (JInternalFrame) myFrames.get(title);
      if (cand != null && cand.equals(frame)) {
        //System.out.println("Found frame to remove: " + title);
        cleanFrame(cand);
        myFrames.remove(title);
        return;
      }
    }
    // If get here it wasnt a node frame. Must remove seperately.
    cleanFrame(frame);
  }

  // FIXME: Add a version to remove a frame by the Frame?
  public void cleanFrame(JInternalFrame frame) {
    if (frame != null) {
      //      System.out.println("Desktop.cleanFrame");
      frame.removeComponentListener(myComponentListener);
      frame.dispose();
    }
  }

  /**
   * Get the frame for this node.
   * @param nodeName the name of the node
   * @return the frame for the node
   */
  public NodeView getNodeFrame(String nodeName) {
    return (NodeView) myFrames.get(nodeName);
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

  /**
   * When done with the desktop, clean up. Remove my ComponentListener
   * and recurse to call dispose on the InternalFrames, which in turn
   * kills the documents, etc.
   **/
  public void dispose() {
    //    System.out.println("Desktop.dispose");
    if (myComponentListener != null) {
      removeComponentListener(myComponentListener);
      // loop through frames and remove the listener from them
      JInternalFrame[] frames = getAllFrames();
      for (int i = 0; i < frames.length; i++) {
        frames[i].removeComponentListener(myComponentListener);
        frames[i].dispose();
      }
      myComponentListener = null;
    }
    myFrames.clear();
    myFrames = null;
    removeAll();
  }
}

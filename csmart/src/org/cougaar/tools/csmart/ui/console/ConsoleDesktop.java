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
import javax.swing.*;
import javax.swing.event.InternalFrameListener;

import org.cougaar.tools.csmart.ui.component.NodeComponent;

public class ConsoleDesktop extends JDesktopPane {
  Hashtable myFrames = new Hashtable();

  /**
   * Add a node output frame to the desktop.
   * The frame is iconified and not selected.
   */

  public void addNodeFrame(NodeComponent node, 
                           ConsoleNodeListener listener,
                           InternalFrameListener frameListener,
                           JScrollPane pane,
                           JRadioButton statusButton,
                           String logFileName) {
    JInternalFrame frame = new ConsoleInternalFrame(node, listener, pane,
                                                    statusButton, logFileName);
    frame.addInternalFrameListener(frameListener);
    addFrame(frame, true);
    myFrames.put(node.getShortName(), frame);
  }

  public ConsoleInternalFrame getNodeFrame(String nodeName) {
    return (ConsoleInternalFrame)myFrames.get(nodeName);
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

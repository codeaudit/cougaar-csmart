/*
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.generic;

import att.grappa.*;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

/**
 * Panel that contains graph.
 * Override mouse listener methods to intercept double clicks
 * and use them to display node attributes.
 */

public class CSMARTGrappaPanel extends GrappaPanel
{
  CSMARTFrame frame;

  public CSMARTGrappaPanel(Subgraph graph, CSMARTFrame frame) {
    super(graph);
    this.frame = frame;
  }

  /**
   * On a double left click, display the node attributes of the node containing
   * the mouse point, if any.
   * @param mev mouse event received
   */

  public void mouseClicked(MouseEvent mev) {
    if ((mev.getClickCount() == 2) && 
	((mev.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK))
      frame.displaySelectedNodeAttributes();
    else
      super.mouseClicked(mev);
  }

  /**
   * Ignore mouse pressed if click count is 2.
   * Note that a mouse click, generates a mouse pressed, released, and clicked
   * in that order.
   * @param mev mouse event received
   */

  public void mousePressed(MouseEvent mev) {
    if (mev.getClickCount() != 2)
      super.mousePressed(mev);
  }

  /**
   * Ignore mouse released if click count is 2.
   * Note that a mouse click, generates a mouse pressed, released, and clicked
   * in that order.
   * @param mev mouse event received
   */

  public void mouseReleased(MouseEvent mev) {
    if (mev.getClickCount() != 2)
      super.mouseReleased(mev);
  }

}


/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.generic;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import att.grappa.*;

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


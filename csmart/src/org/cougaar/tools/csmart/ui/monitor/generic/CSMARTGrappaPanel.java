/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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


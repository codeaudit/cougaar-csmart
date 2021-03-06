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

package org.cougaar.tools.csmart.ui.monitor.generic;

import att.grappa.*;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;

/**
 * Adds functionality to GrappaAdapter which is the class that handles
 * mouse events for grappa graphs.
 * This clears highlighting of nodes done by the CSMARTGraph select method
 * in response to the user selecting nodes using some criteria
 * (i.e. all events from some source), as opposed to using mouse clicks.
 * It also removes marks (for before/after/cause/effect) if the selection
 * changes, and updates menus (enables/disables menu items) that are dependent
 * on having selected nodes.
 * Note that a mouse click produces these events: pressed, released, clicked,
 * and a mouse drag produces: pressed, released.
 */

public class CSMARTGrappaAdapter extends GrappaAdapter {
  BirdFrame birdFrame = null;
  CSMARTFrame frame = null;
  private transient Logger log;

  /**
   * Create an adapter for the overview frame and graph frame.
   * @param birdFrame the overview frame
   * @param frame     the frame that displays the graph
   */

  public CSMARTGrappaAdapter(BirdFrame birdFrame, CSMARTFrame frame) {
    super();
    this.birdFrame = birdFrame;
    this.frame = frame;
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Clear the previous selection and select the node clicked on if any.
   * @param subg   the graph in which the mouse was clicked
   * @param elem   the element (node or edge) on which the mouse was clicked
   * @param pt     the point at which the mouse was clicked
   * @param modifiers mouse modifiers
   * @param panel  the graph panel in which the mouse was clicked
   */

   public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, int clickCount, GrappaPanel panel) {
     // get selected elements
     Vector selectedElements = ((CSMARTGraph)subg).getSelectedElements();
     // process left mouse click or control-click
     super.grappaClicked(subg, elem, pt, modifiers, clickCount, panel);
     updateMarksAndMenus((CSMARTGraph)subg, selectedElements);
   }

  /**
   * Clear the previous selection and select the nodes enclosed
   * in the area swept out by the mouse.
   * If the area is selected in the bird's eye view, then
   * scroll the main view to the selected area (TODO: get this working).
   * @param subg             the graph in which the mouse was released
   * @param elem             the element on which the mouse was released
   * @param pt               the point at which the mouse was released
   * @param modifiers        mouse modifiers when mouse was released
   * @param pressedElem      the element on which the mouse was pressed
   * @param pressedPt        the point at which the mouse was pressed
   * @param pressedModifiers mouse modifiers when mouse was pressed
   * @param outline          the outline box from mouse pressed to released
   * @param panel            the graph panel in which the mouse was released
   */
  public void grappaReleased(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, Element pressedElem, GrappaPoint pressedPt, int pressedModifiers, GrappaBox outline, GrappaPanel panel) {
    // get selected elements
    Vector selectedElements = ((CSMARTGraph)subg).getSelectedElements();
    // process release
    super.grappaReleased(subg, elem, pt, modifiers,
			 pressedElem, pressedPt, pressedModifiers,
 			 outline, panel);
    updateMarksAndMenus((CSMARTGraph)subg, selectedElements);
    // if this action occurred in the overview, then scroll the main frame
    if (birdFrame != null) {
      GrappaBox box = 
	GrappaSupport.boxFromCorners(pressedPt.x, pressedPt.y,
				     pt.x, pt.y);
      frame.scrollToOutline(box);
    }
    // enable/disable menus based on whether any elements are selected
    if (((CSMARTGraph)subg).getSelectedElements() != null)
      frame.enableSelectedMenus(true);
    else
      frame.enableSelectedMenus(false);
    
//     //    if (birdFrame != null) {
//       //      int x = (int)outline.getX();
//       //      int y = (int)outline.getY();
//       //      int width = (int)outline.getWidth();
//       //      int height = (int)outline.getHeight();
//       //      Rectangle r = new Rectangle(x, y, width, height);
//       //      Rectangle r = new Rectangle(outline.getX(), outline.getY(),
//       //				  outline.getWidth(), outline.getHeight());
//       //       if(log.isDebugEnabled()) {
//       //      log.debug("Scrolling to: " + r);
//       //      }
//       //      eventFrame.scrollRectToVisible();
//     //    }
   }

  /**
   * Ignore button2 and button3 presses.
   */

  public void grappaPressed(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, GrappaPanel panel) {
    return;
  }

  /**
   * If the selection changed, then reset the "marks" 
   * (for cause/effect/before/after).
   * Update the frame menus that are dependent on whether or not
   * anything is selected.
   */

  private void updateMarksAndMenus(CSMARTGraph graph, 
				   Vector oldSelectedElements) {
    // if same elements still selected, do nothing
    Vector newSelectedElements = graph.getSelectedElements();
    if (oldSelectedElements == null && newSelectedElements == null)
       return;
    if (oldSelectedElements != null)
      if (oldSelectedElements.equals(newSelectedElements))
	return;
    // reset before/after/cause/effect/etc.
    graph.resetColors();
    // enable/disable menus based on whether any elements are selected
    if (newSelectedElements != null)
      frame.enableSelectedMenus(true);
    else
      frame.enableSelectedMenus(false);
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}





/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.monitor.generic;

import att.grappa.*;
import java.awt.Rectangle;
import java.awt.event.*;
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

  /**
   * Create an adapter for the overview frame and graph frame.
   * @param birdFrame the overview frame
   * @param frame     the frame that displays the graph
   */

  public CSMARTGrappaAdapter(BirdFrame birdFrame, CSMARTFrame frame) {
    super();
    this.birdFrame = birdFrame;
    this.frame = frame;
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
   * @param pressedmodifiers mouse modifiers when mouse was pressed
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
//       //      System.out.println("Scrolling to: " + r);
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
}





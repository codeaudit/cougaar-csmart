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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import att.grappa.*;

/**
 * This class should support displaying a rectangle on the overview frame
 * indicating the area displayed in the main frame.
 */

public class BirdPanel extends GrappaPanel {
  GrappaBox outline;

  public BirdPanel(Graph graph) {
    super(graph);
  }

//   public void setVisibleOutline(GrappaBox r) {
//     outline = r;
//   }

//   public void paintComponent(Graphics g) {
//     super.paintComponent(g);
//     System.out.println("Painting component");
//     // draw outline box in grappa panel
//     if (outline != null) {
//       Graphics2D g2d = (Graphics2D)g;
//       System.out.println("Drawing box in paintComponent: " + outline);
//       g2d.setPaint(Color.red);
//       g2d.draw(outline);
//     }
//   }

}

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

package org.cougaar.tools.csmart.ui.monitor.generic;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import att.grappa.*;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/** 
 * Overview (bird's eye view) of graph.
 */

public class BirdFrame extends JFrame implements ChangeListener {
  private static final int initSize = 300;
  private transient Logger log;

  CSMARTGraph overviewGraph;
  CSMARTFrame frame;
  //  GrappaPanel gp;
  BirdPanel gp;
  GrappaBox previousBox = null;

  /**
   * Create a new frame which contains the entire graph.
   * @param title    the title for the new frame
   * @param graph    the graph to display
   * @param CSMARTFrame the associated graph frame
   */

  public BirdFrame(String title, Graph graph, CSMARTFrame frame) {
    super(title);
    createLogger();
    // if user closes window, this hides the window, so we can re-use it
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	e.getWindow().setVisible(false);
      }
    });
    JViewport viewport = frame.getViewport();
    viewport.addChangeListener(this);
    overviewGraph = (CSMARTGraph)graph;
    this.frame = frame;
    setSize(400, 600);
    gp = new BirdPanel(overviewGraph);
    gp.setToolTipText("");
    gp.addGrappaListener(new CSMARTGrappaAdapter(this, frame));
    gp.setScaleToFit(true);
    getContentPane().add("Center", gp);
    setVisible(true);
  }

  private void createLogger() {
    log = CSMART.createLogger("org.cougaar.tools.csmart.ui.monitor.generic");
  }


  /**
   * ChangeListener interface.
   * TODO: This should support displaying a box in the BirdFrame
   * that indicates the portion of the graph being displayed in the main frame.
   */

  public void stateChanged(ChangeEvent e) {
//     JViewport view = (JViewport)e.getSource();
//     GrappaPanel eventGP = eventFrame.getGrappaPanel();
//     Point p = eventGP.getLocation();
//     int viewX = (int)view.getViewPosition().getX();
//     int viewY = (int)view.getViewPosition().getY();
//     viewX = viewX - p.x;
//     viewY = viewY - p.y;
//     AffineTransform inverseTransform = eventGP.getInverseTransform();
//       if(log.isDebugEnabled()) {
//         log.debug("Inverse transform: " + inverseTransform.toString());
//       }
//     double[] pts = new double[] {
//       viewX, viewY, 
// 	viewX + view.getExtentSize().width,
// 	viewY + view.getExtentSize().height
// 	};
//     inverseTransform.transform(pts, 0, pts, 0, 2);
//     GrappaBox box = new GrappaBox(pts[0], pts[1],
// 				  pts[2] - pts[0], pts[3] - pts[1]);
//     Graphics2D g2d = (Graphics2D)gp.getGraphics();
//     AffineTransform orig = g2d.getTransform();
//     g2d.setTransform(gp.getTransform());
//       if(log.isDebugEnabled()) {
//     log.debug("Transform: " + gp.getTransform().toString());
//       }
//     g2d.setXORMode(Color.darkGray);
//     if (previousBox != null) {
//       g2d.draw(previousBox);
//     }
//     g2d.draw(box);
//     previousBox = box;
//     g2d.setPaintMode();
//     g2d.setTransform(orig);
  }

  // this is supposed to be a direct inverse of the overview-to-main frame
  // outline algorithm, but it doesn't work

//   public void updateVisibleArea(Rectangle r) {
//     GrappaPanel eventGP = eventFrame.getGrappaPanel();
//     double x = r.getX();
//     double y = r.getY();
//     Point p = eventGP.getLocation();
//     x = x - p.x;
//     y = y - p.y;
//     double[] pts = new double[] {
//       x, y, x + r.getWidth(), y + r.getHeight()
// 	};
//     AffineTransform inverseTransform = eventGP.getInverseTransform();
//     inverseTransform.transform(pts, 0, pts, 0, 2);
//     GrappaBox box = new GrappaBox(pts[0], pts[1],
// 				  pts[2] - pts[0], pts[3] - pts[1]);
//       if(log.isDebugEnabled()) {
//         log.debug("Update visible area to: " + box);
//       }
//     Graphics2D g2d = (Graphics2D)gp.getGraphics();
//     AffineTransform orig = g2d.getTransform();
//     g2d.setTransform(gp.getTransform());
//     g2d.setXORMode(Color.red);
//     // erase previous outline if any
//     if (previousBox != null) {
//       if(log.isDebugEnabled()) {
//         log.debug("Drawing previous box: " + previousBox);
//       }
//       g2d.draw(previousBox);
//     }
//     gp.setVisibleOutline(box);
//     previousBox = box;
//     g2d.setPaintMode();
//     g2d.setTransform(orig);
//   }

}

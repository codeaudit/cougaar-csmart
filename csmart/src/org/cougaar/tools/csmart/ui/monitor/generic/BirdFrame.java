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

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
   * @param frame the associated graph frame
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
    log = CSMART.createLogger(this.getClass().getName());
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

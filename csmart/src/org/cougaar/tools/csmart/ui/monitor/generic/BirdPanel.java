/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

/**
 * This class should support displaying a rectangle on the overview frame
 * indicating the area displayed in the main frame.
 */

public class BirdPanel extends GrappaPanel {
  GrappaBox outline;
  private transient Logger log;

  public BirdPanel(Graph graph) {
    super(graph);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }


//   public void setVisibleOutline(GrappaBox r) {
//     outline = r;
//   }

//   public void paintComponent(Graphics g) {
//     super.paintComponent(g);
//       if(log.isDebugEnabled()) {
//         log.debug("Painting component");
//       }
//     // draw outline box in grappa panel
//     if (outline != null) {
//       Graphics2D g2d = (Graphics2D)g;
//       if(log.isDebugEnabled()) {
//         log.debug("Drawing box in paintComponent: " + outline);
//       }
//       g2d.setPaint(Color.red);
//       g2d.draw(outline);
//     }
//   }

}

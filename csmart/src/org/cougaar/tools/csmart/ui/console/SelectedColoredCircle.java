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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Map;
import javax.swing.Icon;

/** Create a SelectedColored circle icon from the color specified.
 *  Used in legends.
 */

public class SelectedColoredCircle implements Icon {
  Image img;
  Color color;
  int width;
  static final Map hints =
  java.util.Collections.singletonMap(RenderingHints.KEY_ANTIALIASING, 
	                             RenderingHints.VALUE_ANTIALIAS_ON);
  static final Stroke wideStroke = new BasicStroke(2);

  /**
   * Create a SelectedColored circle icon.
   * @param c the color
   * @param width the width
   */

  public SelectedColoredCircle(Color c, int width, Image img) {
    this.color = c;
    this.width = width;
    this.img = img;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    Color oldColor = g.getColor();
    RenderingHints oldHints = ((Graphics2D)g).getRenderingHints();
    Stroke oldStroke = ((Graphics2D)g).getStroke();
    ((Graphics2D)g).addRenderingHints(hints);
    ((Graphics2D)g).setStroke(wideStroke);
    g.setColor(Color.darkGray);
    g.drawOval(x, y, width, width);
    g.setColor(color);
    g.fillOval(x+1,y+1,width-2,width-2);
    if (img != null)
      ((Graphics2D)g).drawImage(img, new AffineTransform(1f,0f,0f,1f,x+2,y+2), null);
    g.setColor(oldColor);
    ((Graphics2D)g).setRenderingHints(oldHints);
    ((Graphics2D)g).setStroke(oldStroke);
  }
  
  public int getIconWidth() { return width; }
  
  public int getIconHeight() { return width; }

}


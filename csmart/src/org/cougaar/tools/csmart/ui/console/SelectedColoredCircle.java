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
import java.util.Map;
import javax.swing.Icon;

/** Create a SelectedColored circle icon from the color specified.
 *  Used in legends.
 */

public class SelectedColoredCircle implements Icon {
  Color color;
  int width;
  static final Map hints =
  java.util.Collections.singletonMap(RenderingHints.KEY_ANTIALIASING, 
	                             RenderingHints.VALUE_ANTIALIAS_ON);

  /**
   * Create a SelectedColored circle icon.
   * @param c the color
   * @param width the width
   */

  public SelectedColoredCircle(Color c, int width) {
    this.color = c;
    this.width = width;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    Color oldColor = g.getColor();
    RenderingHints oldHints = ((Graphics2D)g).getRenderingHints();
    g.setColor(color);
    ((Graphics2D)g).addRenderingHints(hints);
    g.fillOval(x, y, width, width);
    g.setColor(oldColor);
    ((Graphics2D)g).setRenderingHints(oldHints);
  }
  
  public int getIconWidth() { return width; }
  
  public int getIconHeight() { return width; }

}


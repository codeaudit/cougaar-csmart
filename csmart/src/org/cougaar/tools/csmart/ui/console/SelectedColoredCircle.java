/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
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


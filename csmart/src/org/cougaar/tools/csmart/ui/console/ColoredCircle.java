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
import java.util.Collections;
import java.util.Map;
import javax.swing.Icon;

/** Create a colored circle icon from the color specified.
 *  Used in legends.
 */

public class ColoredCircle implements Icon {
  Color color;
  int width;
  static final Map hints =
  Collections.singletonMap(RenderingHints.KEY_ANTIALIASING, 
			   RenderingHints.VALUE_ANTIALIAS_ON);
  static final Stroke wideStroke = new BasicStroke(4);

  /**
   * Create a colored circle icon.
   * @param c the color
   * @param width the width
   */

  public ColoredCircle(Color c, int width) {
    this.color = c;
    this.width = width;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    Color oldColor = g.getColor();
    RenderingHints oldHints = ((Graphics2D)g).getRenderingHints();
    Stroke oldStroke = ((Graphics2D)g).getStroke();
    g.setColor(color);
    ((Graphics2D)g).addRenderingHints(hints);
    ((Graphics2D)g).setStroke(wideStroke);
    g.drawOval(x+2,y+2,width-4,width-4);
    g.setColor(oldColor);
    ((Graphics2D)g).setRenderingHints(oldHints);
    ((Graphics2D)g).setStroke(oldStroke);
  }
  
  public int getIconWidth() { return width; }
  
  public int getIconHeight() { return width; }

}


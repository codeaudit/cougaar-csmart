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
import javax.swing.Icon;

/** Create a colored square icon from the color specified.
 *  Used in legends.
 */

public class ColoredSquare implements Icon {
  Color color;
  int width;
  int height;

  /**
   * Create a colored square icon.
   * @param c the color
   * @param width the width
   * @param height the height
   */

  public ColoredSquare(Color c, int width, int height) {
    this.color = c;
    this.width = width;
    this.height = height;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    Color oldColor = g.getColor();
    g.setColor(color);
    g.fill3DRect(x,y,getIconWidth(), getIconHeight(), true);
    g.setColor(oldColor);
  }
  
  public int getIconWidth() { return width; }
  
  public int getIconHeight() { return height; }

}


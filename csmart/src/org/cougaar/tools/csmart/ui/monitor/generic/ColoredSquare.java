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

import javax.swing.*;
import java.awt.*;

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


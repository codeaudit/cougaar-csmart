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

package org.cougaar.tools.csmart.ui.console;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.Map;

/** Create a Colored circle icon from the color specified.
 *  Used in legends.
 */

public class ColoredCircle implements Icon {
  /** The width of the outline, if any **/
  private static final int STROKE_WIDTH = 2;
  /** The margin around the circle to prevent clipping **/
  private static final int MARGIN = STROKE_WIDTH / 2;
  /** An image to draw on top of the circle, if desired. **/
  protected Image img;
  /** The main color of the circle. **/
  Color color;
  /** The size of the circle (width and height) **/
  int width;
  /** Should the outline be drawn. Set by subclass **/
  protected boolean selected = false;
  /** Hints on how to render the circle. **/
  static final Map hints =
    Collections.singletonMap(RenderingHints.KEY_ANTIALIASING, 
                             RenderingHints.VALUE_ANTIALIAS_ON);
  /** A wide stroke to draw the outline if any. **/
  static final Stroke wideStroke = new BasicStroke(STROKE_WIDTH);

  /**
   * Create a Colored circle icon.
   * @param c the color
   * @param width the width
   **/
  public ColoredCircle(Color c, int width, Image img) {
    this.color = c;
    this.width = width;
    this.img = img;
  }

  /**
   * Paint the icon into the given Component as the specified
   * location.
   **/
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2d = (Graphics2D) g;
    Color oldColor = g2d.getColor();
    RenderingHints oldHints = g2d.getRenderingHints();
    g2d.addRenderingHints(hints);
    g2d.setColor(color);
    g2d.fillOval(x + MARGIN, y + MARGIN, width - 2 * MARGIN, width - 2 * MARGIN);
    if (selected) {
      Stroke oldStroke = g2d.getStroke();
      g2d.setStroke(wideStroke);
      g2d.setColor(Color.darkGray);
      g2d.drawOval(x + MARGIN, y + MARGIN, width - 2 * MARGIN, width - 2 * MARGIN);
      g2d.setStroke(oldStroke);
    }
    if (img != null)
      g2d.drawImage(img, new AffineTransform(1f, 0f, 0f, 1f, x+2, y+2), null);
    g2d.setColor(oldColor);
    g2d.setRenderingHints(oldHints);
  }
  
  public int getIconWidth() { return width; }
  
  public int getIconHeight() { return width; }

}


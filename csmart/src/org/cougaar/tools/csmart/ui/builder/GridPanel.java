/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.builder;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;

/**
 * A JPanel that provides support for adding labels and components
 * using a GridBagLayout.
 */

public class GridPanel extends JPanel {
  int gridx;
  int gridy;
  int topInset;

  public GridPanel() {
    super(new GridBagLayout());
    gridx = 0;
    gridy = 0;
    topInset = 10;
  }

  /**
   * Add a label to this panel using the grid bag layout.
   * This adds a label at the 0th x-position, and the current y-position
   * anchored to the west, with no expansion, and 
   * padded on the left, bottom and right.
   * It advances the x-position to the next column.
   */

  public void addLabel(JLabel label) {
    gridx = 0;
    add(label, new GridBagConstraints(gridx, gridy, 1, 1, 0.0, 0.0,
				      GridBagConstraints.WEST,
				      GridBagConstraints.NONE,
				      new Insets(topInset, 10, 10, 10),
				      0, 0));
    gridx++;
  }

  /**
   * Add a component to this panel using the grid bag layout.
   * This adds a component, anchored to the west,
   * expanding horizontally, and padded on the bottom and right.
   * It advances the y-position to the next row.
   */

  public void addComponent(JComponent c) {
    add(c, new GridBagConstraints(gridx, gridy, 1, 1, 1.0, 0.0,
				  GridBagConstraints.WEST,
				  GridBagConstraints.HORIZONTAL,
				  new Insets(topInset, 0, 10, 10),
				  0, 0));
    gridy++; // next row
    topInset = 0; // only need non-zero value for first label & component
  }
}

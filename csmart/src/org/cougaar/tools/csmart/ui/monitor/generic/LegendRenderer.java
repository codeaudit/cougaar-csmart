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

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Used to render legends which contain labels and colored icons.
 */

public class LegendRenderer extends JLabel implements ListCellRenderer {

  public LegendRenderer() {
    setOpaque(true);
  }

  public Component getListCellRendererComponent(JList list,
						Object value,
						int index,
						boolean isSelected,
						boolean cellHasFocus) {
    if (value == null) {
      setText("");
      setIcon(null);
      return this;
    } 
    return (Component)value;
  }
}
 

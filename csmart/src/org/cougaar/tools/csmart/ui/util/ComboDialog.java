/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.util;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class ComboDialog {

  public static Object showDialog(JPanel parent, String label, Vector values) {
    JPanel panel = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    panel.add(new JLabel(label),
              new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(0, 5, 0, 5), 0, 0));
    JComboBox cb = new JComboBox(values);
    cb.setEditable(true);
    int maxWidth = 40; // reasonable default
    for (int i = 0; i < values.size(); i++)
      maxWidth = 
        Math.max(maxWidth,
                 SwingUtilities.computeStringWidth(cb.getFontMetrics(cb.getFont()),
                                                   (String)values.get(i)));
    // add approximate width of scroll bar
    cb.setPreferredSize(new Dimension(maxWidth + 30,
                                      (int)cb.getPreferredSize().getHeight()));
    panel.add(cb,
              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.HORIZONTAL,
                                     new Insets(0, 0, 0, 5), 0, 0));
    int result = JOptionPane.showConfirmDialog(parent, panel, label,
                                               JOptionPane.OK_CANCEL_OPTION,
                                               JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION)
      return null;
    return cb.getSelectedItem();
  }

}

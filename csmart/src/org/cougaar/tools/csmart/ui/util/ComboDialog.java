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

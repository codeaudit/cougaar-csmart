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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class Legend extends JDialog {
  Box box = new Box(BoxLayout.Y_AXIS);
  JPanel colorPanel;

  int x =0;
  int y = 0;

  public Legend() {
    JPanel legendPanel = new JPanel(new BorderLayout());

    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    buttonPanel.add(okButton);

    colorPanel = new JPanel();
    colorPanel.setLayout(new GridBagLayout());
    TitledBorder colorPanelTitledBorder = new TitledBorder("Status Legend");
    colorPanel.setBorder(colorPanelTitledBorder);

    makeLegend(CSMARTConsole.busyStatus);
    makeLegend(CSMARTConsole.highBusyStatus);
    makeLegend(CSMARTConsole.mediumHighBusyStatus);
    makeLegend(CSMARTConsole.mediumBusyStatus);
    makeLegend(CSMARTConsole.mediumLowBusyStatus);
    makeLegend(CSMARTConsole.lowBusyStatus);
    makeLegend(CSMARTConsole.idleStatus);
    makeLegend(CSMARTConsole.errorStatus);
    makeLegend(CSMARTConsole.noAnswerStatus);
    makeLegend(CSMARTConsole.unknownStatus);
    makeLegend(CSMARTConsole.stdErrStatus);
    makeLegend(CSMARTConsole.notifyStatus);
    box.add(colorPanel);

    legendPanel.add(buttonPanel, BorderLayout.SOUTH);
    legendPanel.add(box, BorderLayout.CENTER);
    getContentPane().add(legendPanel);
    pack();
  }
  public static void main(String[] args) {
    Legend legend = new Legend();
    legend.setVisible(true);
  }
  private void makeLegend(Color color) {
    JButton newButton = new JButton(CSMARTConsole.getStatusColorDescription(color));
    newButton.setBackground(color);
    colorPanel.add(newButton,
                   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
					  GridBagConstraints.CENTER,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
  }
}

    

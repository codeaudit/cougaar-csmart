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
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    String[] descriptions = NodeStatusButton.getStatusDescriptions();
    Color[] colors = NodeStatusButton.getStatusColors();
    for (int i = 0; i < descriptions.length; i++)
      makeLegend(descriptions[i], colors[i]);
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
  private void makeLegend(String s, Color color) {
    JButton newButton = new JButton(s);
    newButton.setBackground(color);
    colorPanel.add(newButton,
                   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
					  GridBagConstraints.CENTER,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
  }
}



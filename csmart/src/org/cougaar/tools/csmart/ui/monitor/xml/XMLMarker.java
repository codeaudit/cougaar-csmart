/* 
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.xml;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.util.*;

import att.grappa.GrappaColor;
import org.cougaar.tools.csmart.ui.monitor.generic.ColoredSquare;
import org.cougaar.tools.csmart.ui.monitor.generic.CSMARTGraph;
import org.cougaar.tools.csmart.ui.monitor.generic.UIProperties;

/**
 * Dialog to allow user to select a color mapping 
 * if a named attribute exists
 * or if a name/value pair exists.
 */

public class XMLMarker extends JDialog {
  private CSMARTGraph graph;
  private JTextField nameField;
  private JTextField valueField;
  private JComboBox colorCB;
  private static String[] colorNames = { "xmlcolor1", "xmlcolor2", "xmlcolor3", "xmlcolor4" };

  public XMLMarker(CSMARTGraph graph) {
    super((java.awt.Frame)null, "Mark", false);
    this.graph = graph;
    JPanel panel = new JPanel(new GridBagLayout());
    int x = 0;
    int y = 0;
    panel.add(new JLabel("Name:"),
              new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 5, 5, 5),
                                     0, 0));
    nameField = new JTextField(10);
    panel.add(nameField,
              new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.HORIZONTAL,
                                     new Insets(5, 0, 5, 5),
                                     0, 0));
    x = 0;
    panel.add(new JLabel("Value:"),
              new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(0, 5, 5, 5),
                                     0, 0));
    valueField = new JTextField(10);
    panel.add(valueField,
              new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.HORIZONTAL,
                                     new Insets(0, 0, 5, 5),
                                     0, 0));

    // code to allow user to pick mark color
    //    x = 0;
    //    panel.add(new JLabel("Color:"),
    //              new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
    //                                     GridBagConstraints.WEST,
    //                                     GridBagConstraints.NONE,
    //                                     new Insets(0, 5, 5, 5),
    //                                     0, 0));
    //    Vector colors = new Vector();
    // get some colors that the graphing package knows
    //    UIProperties properties = CSMARTGraph.getProperties();
//     GrappaColor.addColor(colorNames[0], properties.getColorBefore());
//     GrappaColor.addColor(colorNames[1], properties.getColorAfter());
//     GrappaColor.addColor(colorNames[2], properties.getColorCauses());
//     GrappaColor.addColor(colorNames[3], properties.getColorEffects());
//     colors.add(properties.getColorBefore());
//     colors.add(properties.getColorAfter());
//     colors.add(properties.getColorCauses());
//     colors.add(properties.getColorEffects());
//    colorCB = new JComboBox(colors);
//    colorCB.setRenderer(new ComboBoxRenderer(colors));
//    panel.add(colorCB,
//              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
//                                     GridBagConstraints.WEST,
//                                     GridBagConstraints.HORIZONTAL,
//                                     new Insets(0, 0, 5, 5),
//                                     0, 0));
    GrappaColor.addColor("xmlcolor", new Color(230, 30, 230));
    x = 1;
    JButton markButton = new JButton("Mark");
    panel.add(markButton,
              new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                                     GridBagConstraints.CENTER,
                                     GridBagConstraints.NONE,
                                     new Insets(0, 0, 5, 5),
                                     0, 0));
    markButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mark();
      }
    });
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(BorderFactory.createLineBorder(Color.black));
    JButton dismissButton = new JButton("Dismiss");
    dismissButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    });
    buttonPanel.add(dismissButton);
    getContentPane().add(panel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    pack();
    show();
  }

  private void mark() {
    String nameRegex = nameField.getText().trim();
    String valueRegex = valueField.getText().trim();
    if (nameRegex.length() == 0 || valueRegex.length() == 0)
      return;
    //    int i = colorCB.getSelectedIndex();
    //    if (i == -1)
    //      return;
    // clear selected nodes so that marks override those
    graph.clearSelection();
    // clear any previous marks
    graph.resetColors();
    // to compare names against names with indices
    nameRegex = "[0-9]+-" + nameRegex;
    //    graph.mark(graph.getNodesByRegex(nameRegex, valueRegex), colorNames[i]);
    graph.mark(graph.getNodesByRegex(nameRegex, valueRegex), "xmlcolor");
  }

  public class ComboBoxRenderer extends JLabel implements ListCellRenderer {
    Vector colors;

    public ComboBoxRenderer(Vector colors) {
      this.colors = colors;
      setOpaque(true);
      setHorizontalAlignment(CENTER);
      setVerticalAlignment(CENTER);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
      setIcon(new ColoredSquare((Color)value, 200, 10));
      setText("");
      return this;
    }
  }

}

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

package org.cougaar.tools.csmart.ui.console;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Allow users to select fonts for use in Console
 **/
public class ConsoleFontChooser extends JDialog {
  private JComboBox msgTypesCB;
  private JComboBox fontSizeCB;
  private JComboBox fontStyleCB;
  private JButton colorButton;
  private String SELECTONE = "Select One";
  private String SEARCH = "Search";
  private String NOTIFY = "Notify";
  private static final String STANDARDOUT = "Standard Out";
  private static final String ERRORMSGS = "Standard Error";
  private static final String NODECREATION = "Node Creation";
  private static final String AGENTADD = "Agent Addition";
  private static final String IDLENESS = "Idleness";
  private static final String NODEDESTROYED = "Node Destroyed";

  private transient Logger log;

  private static final String FONTSIZE[] = {"8","9","10","11","12","14","16","18","20","22","24","26","28","36","48","72"};
  private static final String FONTS[] = {"Regular","Italic","Bold","BoldItalic"};
  int[] fontSizes = new int[8];
  String[] fontTypes = new String[8];
  Color[] fontColors = new Color[8];
  Color selectedColor;

  public ConsoleFontChooser() {
    // create a modal dialog
    super((java.awt.Frame)null, "Select Font", true); 

    createLogger();
    JPanel panel = new JPanel(new BorderLayout());
    // define the gui here

    // apply and cancel buttons panel
    JPanel buttonPanel = new JPanel();
    JButton applyButton = new JButton("Apply");
    // when user clicks apply add the selections to the following
    // arrays - fontSizes, fontTypes, fontColors
    applyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = msgTypesCB.getSelectedIndex()-1;
        if (index <0) return;
        if(log.isDebugEnabled()) {
          log.debug("index is - " + index + 
                    "and font style is " + fontStyleCB.getSelectedItem());
        }
        fontSizes[index]=new Integer((String)fontSizeCB.getSelectedItem()).intValue();
        fontTypes[index]=(String)fontStyleCB.getSelectedItem();
        fontColors[index]=selectedColor;
      }
    });
    buttonPanel.add(applyButton);
    JButton dismissButton = new JButton("Dismiss");
    dismissButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    buttonPanel.add(dismissButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    buttonPanel.add(cancelButton);


    // Search Panel
    JPanel selectPanel = new JPanel();
    selectPanel.setLayout(new GridBagLayout());
    TitledBorder selectTitledBorder = new TitledBorder("Select Font");
    Font selectfont = selectTitledBorder.getTitleFont();
    Font selecttitleFont = selectfont.deriveFont(Font.ITALIC);
    selectTitledBorder.setTitleFont(selecttitleFont);
    selectPanel.setBorder(selectTitledBorder);

    JLabel msgTypesLabel = new JLabel("Message Type");
    msgTypesCB = new JComboBox();
    msgTypesCB.addItem(SELECTONE);
    msgTypesCB.addItem(STANDARDOUT);
    msgTypesCB.addItem(ERRORMSGS);
    msgTypesCB.addItem(NODECREATION);
    msgTypesCB.addItem(AGENTADD);
    msgTypesCB.addItem(IDLENESS);
    msgTypesCB.addItem(NODEDESTROYED);
    msgTypesCB.addItem(SEARCH);
    msgTypesCB.addItem(NOTIFY);

    JLabel fontSizeLabel = new JLabel("Font Size");
    fontSizeCB = new JComboBox(FONTSIZE);
    fontSizeCB.setSelectedIndex(4);

    JLabel fontStyleLabel = new JLabel("Font Style");
    fontStyleCB = new JComboBox(FONTS);

    // Color choser
    JLabel colorLabel = new JLabel("Font Color");
    colorButton = new JButton();
    colorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      selectedColor = JColorChooser.showDialog(null, "Please Chose a Font Color", null);
      colorButton.setBackground(selectedColor);
      }
    });
    int x = 0;
    int y = 0;
    selectPanel.add(msgTypesLabel,
		   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(10, 5, 5, 5), 0, 0));
    selectPanel.add(msgTypesCB,
		   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(10, 0, 5, 5), 0, 0));
    x=0;
    selectPanel.add(fontSizeLabel,
		   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(10, 5, 5, 5), 0, 0));
    selectPanel.add(fontSizeCB,
		   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(10, 0, 5, 5), 0, 0));
    x=0;
    selectPanel.add(fontStyleLabel,
		   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(10, 5, 5, 5), 0, 0));
    selectPanel.add(fontStyleCB,
		   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(10, 0, 5, 5), 0, 0));
    x=0;
    selectPanel.add(colorLabel,
		   new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(10, 5, 5, 5), 0, 0));
    selectPanel.add(colorButton,
		   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(10, 0, 5, 5), 0, 0));

    panel.add(buttonPanel, BorderLayout.SOUTH);
    panel.add(selectPanel, BorderLayout.CENTER);
    getContentPane().add(panel);
    pack();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  // implement these methods to return the values the user selects
  // message type will be one of the types defined by the
  // type of the node output (std-out, std-err, destroyed, etc)
  // plus define two new types in this file (SEARCH and NOTIFY)

  public int getFontSize(int messageType) {
    int size = fontSizes[messageType];
      if(log.isDebugEnabled()) {
        log.debug("Font Size: " + size);
      }
    return size;
  }

  public String getFontStyle(int messageType) {
    String style = fontTypes[messageType];
      if(log.isDebugEnabled()) {
        log.debug(style);
      }
    return style;
  }

  public Color getFontColor(int messageType) {
    Color fontColor = fontColors[messageType];
      if(log.isDebugEnabled()) {
        log.debug("Font Color: " + fontColor.toString());
      }
    return fontColor;
  }

  // for testing
  public static void main(String[] args) {
    ConsoleFontChooser fontChooser = new ConsoleFontChooser();
    fontChooser.setVisible(true);
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}

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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.cougaar.tools.server.NodeEvent;

/**
 * Dialog for selecting what Node output to filter
 **/
public class ConsoleNodeOutputFilter extends JDialog {
  private static final String ALL = "All";
  private static final String STANDARDOUT = "Standard Output";
  private static final String ERRORMSGS = "Standard Error";
  private static final String NODECREATION = "Node Created";
  private static final String NODEDESTROYED = "Node Destroyed";
  private static final String IDLENESS = "Idle";
  Box box = new Box(BoxLayout.Y_AXIS);
  private JPanel filterPanel;
  private JCheckBox allCB;
  private JCheckBox standardCB;
  private JCheckBox errorCB;
  private JCheckBox createCB;
  private JCheckBox destroyCB;
  private JCheckBox idlenessCB;
  boolean[] msgArray = { false, false, false, false, false, false };
  private boolean allSelected = true;

  public ConsoleNodeOutputFilter(Frame parent,
                                 boolean[] initialValues, boolean acceptAll) {
    super(parent, "Filter", true); // modal dialog
    filterPanel = new JPanel(new BorderLayout());

    // ok and cancel buttons panel
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton("OK");
    // only update the filter if the user selects "OK"
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        allSelected = allCB.isSelected();
        if (!allSelected) {
          msgArray[NodeEvent.STANDARD_OUT] = standardCB.isSelected();
          msgArray[NodeEvent.STANDARD_ERR] = errorCB.isSelected();
          msgArray[NodeEvent.PROCESS_CREATED] = createCB.isSelected();
          msgArray[NodeEvent.PROCESS_DESTROYED] = destroyCB.isSelected();
          msgArray[NodeEvent.IDLE_UPDATE] = idlenessCB.isSelected();
        }
        setVisible(false);
      }
    });
    buttonPanel.add(okButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    buttonPanel.add(cancelButton);

    // Message Types Panel
    JPanel msgTypesPanel = new JPanel();
    msgTypesPanel.setLayout(new GridBagLayout());
    TitledBorder msgTypesTitledBorder = 
      new TitledBorder("Message Types To Display");
    Font msgfont = msgTypesTitledBorder.getTitleFont();
    Font msgtitleFont = msgfont.deriveFont(Font.ITALIC);
    msgTypesTitledBorder.setTitleFont(msgtitleFont);
    msgTypesPanel.setBorder(msgTypesTitledBorder);

    allCB = new JCheckBox(ALL);
    allCB.setSelected(acceptAll);
    allCB.addActionListener(allCBSelected);
    allSelected = acceptAll;

    standardCB = new JCheckBox(STANDARDOUT);
    standardCB.addActionListener(unselectAllCB);
    errorCB = new JCheckBox(ERRORMSGS);
    errorCB.addActionListener(unselectAllCB);
    createCB = new JCheckBox(NODECREATION);
    createCB.addActionListener(unselectAllCB);
    destroyCB = new JCheckBox(NODEDESTROYED);
    destroyCB.addActionListener(unselectAllCB);
    idlenessCB = new JCheckBox(IDLENESS);
    idlenessCB.addActionListener(unselectAllCB);
    int x = 0;
    int y = 0;
    msgTypesPanel.add(allCB,
		   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    msgTypesPanel.add(standardCB,
		   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    msgTypesPanel.add(errorCB,
		   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    msgTypesPanel.add(createCB,
		   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    msgTypesPanel.add(destroyCB,
		   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    msgTypesPanel.add(idlenessCB,
		   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    box.add(msgTypesPanel);

    filterPanel.add(buttonPanel, BorderLayout.SOUTH);
    filterPanel.add(box, BorderLayout.CENTER);
    getContentPane().add(filterPanel);
    pack();
    if (initialValues != null) {
      msgArray = initialValues;
      standardCB.setSelected(msgArray[NodeEvent.STANDARD_OUT]);
      errorCB.setSelected(msgArray[NodeEvent.STANDARD_ERR]);
      createCB.setSelected(msgArray[NodeEvent.PROCESS_CREATED]);
      destroyCB.setSelected(msgArray[NodeEvent.PROCESS_DESTROYED]);
      idlenessCB.setSelected(msgArray[NodeEvent.IDLE_UPDATE]);
    }
    setSize(225, 300);
    // make dialog display over the middle of the caller's frame
    Point p = parent.getLocation();
    Dimension d = parent.getSize();
    int centerX = p.x + d.width/2;
    int centerY = p.y + d.height/2;
    Dimension myD = getSize();
    setLocation(new Point(centerX - myD.width/2, centerY - myD.height/2));
    setVisible(true);
  }
  
  /**
   * Return whether or not the event should be displayed in the console.
   */
  public boolean includeEventInDisplay(NodeEvent event) {
    if (allSelected)
      return true;
    else
      return msgArray[event.getType()];
  }

  /**
   * Return whether or not the event type should be displayed in the
   * console.  Event types are defined in NodeEvent.
   */
  public boolean includeEventTypeInDisplay(int eventType) {
    if (allSelected)
      return true;
    else
      return msgArray[eventType];
  }

  /**
   * Return whether or not the event should be written in the log file.
   */
  public boolean includeEventInLogFile(NodeEvent event) {
    return true;
  }

  ActionListener allCBSelected = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      if (allCB.isSelected()) {
        standardCB.setSelected(false);
        errorCB.setSelected(false);
        createCB.setSelected(false);
        destroyCB.setSelected(false);
        idlenessCB.setSelected(false);
      }
    }
  };
  
  ActionListener unselectAllCB = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      if (allCB.isSelected())
      allCB.setSelected(false);
    }
  };

  public boolean[] getValues() {
    return msgArray;
  }

  public boolean isAllSelected() {
    return allSelected;
  }


  // FIXME: Remove WindowListener on close?
  // note that the "parent" slot points to CSMART, but I see no way to 
  // get rid of that

  public static void main(String[] args) {
    ConsoleNodeOutputFilter filter = 
      new ConsoleNodeOutputFilter(null, null, true);
    filter.setVisible(true);
  }
} 

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

import org.cougaar.tools.server.NodeEvent;

public class ConsoleNodeOutputFilter extends JDialog {
  private static final String ALL = "All";
  private static final String STANDARDOUT = "Standard Out";
  private static final String ERRORMSGS = "Error Messages";
  private static final String NODECREATION = "Node Creation";
  private static final String NODEDESTROYED = "Node Destroyed";
  private static final String CLUSTERADD = "Cluster Addition";
  private static final String IDLENESS = "Idleness";
  private static final String HEARTBEAT = "Heartbeat";
  private final String OFF = "Off";
  private final String ON = "On";
  Box box = new Box(BoxLayout.Y_AXIS);
  private JPanel filterPanel;
  private JRadioButton allButton;
  private JRadioButton sizeButton;
  private JTextField sizeTF;
  private JCheckBox allCB;
  private JCheckBox standardCB;
  private JCheckBox errorCB;
  private JCheckBox createCB;
  private JCheckBox destroyCB;
  private JCheckBox clusterAddCB;
  private JCheckBox idlenessCB;
  private JCheckBox heartbeatCB;
  boolean[] msgArray = { false, false, false, false, false, false, false };
  private boolean allSelected = true;

  public ConsoleNodeOutputFilter() {
    super((java.awt.Frame)null, "Filter", true); // modal dialog
    filterPanel = new JPanel(new BorderLayout());

    // ok and cancel buttons panel
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
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
    TitledBorder msgTypesTitledBorder = new TitledBorder("Messages Types");
    Font msgfont = msgTypesTitledBorder.getTitleFont();
    Font msgtitleFont = msgfont.deriveFont(Font.ITALIC);
    msgTypesTitledBorder.setTitleFont(msgtitleFont);
    msgTypesPanel.setBorder(msgTypesTitledBorder);

    allCB = new JCheckBox(ALL);
    allCB.setSelected(true);
    allCB.addActionListener(allCBSelected);

    standardCB = new JCheckBox(STANDARDOUT);
    standardCB.addActionListener(unselectAllCB);
    standardCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (standardCB.isSelected())
          msgArray[NodeEvent.STANDARD_OUT]=true;
        else
          msgArray[NodeEvent.STANDARD_OUT]=false;
      }
    });
    errorCB = new JCheckBox(ERRORMSGS);
    errorCB.addActionListener(unselectAllCB);
    errorCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (errorCB.isSelected())
          msgArray[NodeEvent.STANDARD_ERR]=true;
        else
          msgArray[NodeEvent.STANDARD_ERR]=false;
      }
    });
    createCB = new JCheckBox(NODECREATION);
    createCB.addActionListener(unselectAllCB);
    createCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (createCB.isSelected())
          msgArray[NodeEvent.NODE_CREATED]=true;
        else
          msgArray[NodeEvent.NODE_CREATED]=false;
      }
    });
    destroyCB = new JCheckBox(NODEDESTROYED);
    destroyCB.addActionListener(unselectAllCB);
    destroyCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (destroyCB.isSelected())
          msgArray[NodeEvent.NODE_DESTROYED]=true;
        else
          msgArray[NodeEvent.NODE_DESTROYED]=false;
      }
    });
    clusterAddCB = new JCheckBox(CLUSTERADD);
    clusterAddCB.addActionListener(unselectAllCB);
    clusterAddCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (clusterAddCB.isSelected())
          msgArray[NodeEvent.CLUSTER_ADDED]=true;
        else
          msgArray[NodeEvent.CLUSTER_ADDED]=false;
      }
    });
    idlenessCB = new JCheckBox(IDLENESS);
    idlenessCB.addActionListener(unselectAllCB);
    idlenessCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (idlenessCB.isSelected())
          msgArray[NodeEvent.IDLE_UPDATE]=true;
        else
          msgArray[NodeEvent.IDLE_UPDATE]=false;
      }
    });
    heartbeatCB = new JCheckBox(HEARTBEAT);
    heartbeatCB.addActionListener(unselectAllCB);
    heartbeatCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (heartbeatCB.isSelected())
          msgArray[NodeEvent.HEARTBEAT]=true;
        else
          msgArray[NodeEvent.HEARTBEAT]=false;
      }
    });
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
    msgTypesPanel.add(clusterAddCB,
		   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    msgTypesPanel.add(idlenessCB,
		   new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    msgTypesPanel.add(heartbeatCB,
		   new GridBagConstraints(x, y++, 1, 1, 1.0, 0.0,
					  GridBagConstraints.WEST,
					  GridBagConstraints.HORIZONTAL,
					  new Insets(0, 0, 0, 0), 0, 0));
    box.add(msgTypesPanel);

    // Buffer Events Panel
    JPanel bufferEventsPanel = new JPanel();
    bufferEventsPanel.setLayout(new GridBagLayout());
    TitledBorder bufferEventsTitledBorder = new TitledBorder("Buffer Events");
    Font font = bufferEventsTitledBorder.getTitleFont();
    Font titleFont = font.deriveFont(Font.ITALIC);
    bufferEventsTitledBorder.setTitleFont(titleFont);
    bufferEventsPanel.setBorder(bufferEventsTitledBorder);
    allButton = new JRadioButton("All");
    sizeButton = new JRadioButton("Buffer Size");
    allButton.setSelected(true);
    ButtonGroup bufferButtonGroup = new ButtonGroup();
    bufferButtonGroup.add(allButton);
    bufferButtonGroup.add(sizeButton);
    sizeTF = new JTextField(15);
    bufferEventsPanel.add(allButton);
    bufferEventsPanel.add(sizeButton);
    bufferEventsPanel.add(sizeTF);
    box.add(bufferEventsPanel);

    filterPanel.add(buttonPanel, BorderLayout.SOUTH);
    filterPanel.add(box, BorderLayout.CENTER);
    getContentPane().add(filterPanel);
    pack();

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
   * Return whether or not the event should be written in the log file.
   */

  public boolean includeEventInLogFile(NodeEvent event) {
    return true;
  }

  /**
   * Set buffer size displayed in dialog.
   */

  public void setBufferSize(int n) {
    if (n != -1) {
      sizeTF.setText(Integer.toString(n));
      sizeButton.setSelected(true);
    } else {
      sizeTF.setText("");
      allButton.setSelected(true);
    }
  }

  /**
   * Return buffer size; -1 implies buffer is unlimited; 0 implies 
   * error (couldn't parse user input).
   * @return buffer size; -1 for unlimited; 0 implies error 
   */

  public int getBufferSize() {
    if (sizeButton.isSelected()) {
      String size = sizeTF.getText();
      System.out.println("text is " + size);
      try {
        int bufferSize = Integer.parseInt(size);
        return bufferSize;
      }catch (NumberFormatException e) { 
        System.err.println("Couldn't parse int");
        return 0;
      }
    } else
      return -1;
  }

  ActionListener allCBSelected = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      allSelected = true;
      standardCB.setSelected(false);
      msgArray[NodeEvent.STANDARD_OUT]=true;
      errorCB.setSelected(false);
      msgArray[NodeEvent.STANDARD_ERR]=true;
      createCB.setSelected(false);
      msgArray[NodeEvent.NODE_CREATED]=true;
      destroyCB.setSelected(false);
      msgArray[NodeEvent.NODE_DESTROYED]=true;
      clusterAddCB.setSelected(false);
      msgArray[NodeEvent.CLUSTER_ADDED]=true;
      idlenessCB.setSelected(false);
      msgArray[NodeEvent.IDLE_UPDATE]=true;
      heartbeatCB.setSelected(false); 
      msgArray[NodeEvent.HEARTBEAT]=true;
    }
  };
  
  ActionListener unselectAllCB = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      allSelected = false;
      if (allCB.isSelected())
      allCB.setSelected(false);
    }
  };

  public static void main(String[] args) {
    ConsoleNodeOutputFilter filter = new ConsoleNodeOutputFilter();
    filter.setVisible(true);
  }
} 

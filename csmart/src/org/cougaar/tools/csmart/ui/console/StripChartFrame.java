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
import com.klg.jclass.chart.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Frame for a strip chart.
 **/

public class StripChartFrame extends JInternalFrame {
  JCChart chart;

  public StripChartFrame(JCChart chart, String title) {
    super(title + ":Load",   // title
          true, //resizable
          true, //closable
          true, //maximizable
          true);//iconifiable
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.lightGray);
    panel.setForeground(Color.black);
    panel.setOpaque(true);
    panel.add("Center", chart);
    getContentPane().add("Center", panel);
    pack();
    setSize(500, 500);
    setVisible(true);
  }

  // for debugging

  public static void main(String args[]) {
    StripChart chart = new StripChart();
    StripChartSource source = new StripChartSource(chart);
    chart.init(source);
    StripChartFrame f = new StripChartFrame(chart, "Test");
    // experimental random data generated periodically
    Thread kicker = new Thread(source);
    kicker.start();
  }

}

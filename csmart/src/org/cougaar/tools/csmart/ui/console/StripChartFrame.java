/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
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

public class StripChartFrame extends JFrame {
  JCChart chart;
  private StripChartSource data;
  private JCAxis xaxis;

  public StripChartFrame(JCChart chart) {
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
    StripChartFrame f = new StripChartFrame(chart);
    // experimental random data generated periodically
    Thread kicker = new Thread(source);
    kicker.start();
  }

}

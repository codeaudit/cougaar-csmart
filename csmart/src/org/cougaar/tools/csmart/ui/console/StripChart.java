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

import com.klg.jclass.chart.*;
import com.klg.jclass.util.legend.*;

import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import javax.swing.BorderFactory;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * StripChart.java
 *
 * StripChart is a dynamically updating strip chart.
 *
 **/

public class StripChart extends JCChart {
  Color tan = new Color(0xe2,0xc4,0x9c);
  Color lightTan = new Color(0xff,0xe2,0xba);
  Logger log;

  public StripChart() {
    super(JCChart.PLOT);
    Logger log = CSMART.createLogger(this.getClass().getName());
  }

  public void init(ChartDataModel data) {
    // Create data source
    //    data = new StripChartSource(chart);

    // Add chart as a data source listener
    getDataView(0).setDataSource(data);

    // for customizing, when debugging only
    //    setTrigger(0, new EventTrigger(Event.META_MASK, EventTrigger.CUSTOMIZE));
    //    setAllowUserChanges(true);

    // set up xaxis
    JCAxis xaxis = getDataView(0).getXAxis();
    //    xaxis.setTimeBase(new Date(System.currentTimeMillis()));
    xaxis.setAnnotationMethod(JCAxis.TIME_LABELS);
    xaxis.setTimeFormat("mm:ss");
    xaxis.setTimeFormatIsDefault(false, true);
    JCAxisTitle xTitle = new JCAxisTitle("Time");
    xTitle.setPlacement(JCLegend.SOUTH);
    xTitle.setPlacementIsDefault(false);
    xaxis.setTitle(xTitle);
    
    // set up yaxis
    JCAxis yaxis = getDataView(0).getYAxis();
    yaxis.setGridVisible(true);
    //    yaxis.setMin(-1);
    //    yaxis.setMax(1);
    yaxis.setMin(0);
    yaxis.setMax(100);
    //    yaxis.setNumSpacing(.2);
    yaxis.setNumSpacing(10);
    JCAxisTitle yTitle = new JCAxisTitle("Relative Load");
    yTitle.setPlacement(JCLegend.WEST);
    yTitle.setPlacementIsDefault(false);
    try {
      yTitle.setRotation(ChartText.DEG_270);
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("StripChart: " + e);
      }
    }
    yaxis.setTitle(yTitle);

    // Set Colors
    setBackground(Color.lightGray);
    setForeground(Color.black);
    setOpaque(true);
    getChartArea().setBackground(tan);
    getChartArea().setForeground(Color.black);
    getChartArea().setOpaque(true);
    getChartArea().getPlotArea().setBackground(lightTan);
    yaxis.getGridStyle().getLineStyle().setColor(Color.black);

    getDataView(0).getSeries(0).getStyle().getSymbolStyle().setShape(JCSymbolStyle.NONE);
    getDataView(0).getSeries(0).getStyle().getLineStyle().setColor(Color.blue);

    // Borders
    getChartArea().setBorder(BorderFactory.createRaisedBevelBorder());
    getChartArea().setAxisBoundingBox(true);

    // Fonts
    getChartArea().setFont(new Font("Helvetica",Font.PLAIN,12));
    setFont(new Font("Helvetica",Font.PLAIN,12));
  }

}

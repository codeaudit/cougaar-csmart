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

import com.klg.jclass.chart.*;
import com.klg.jclass.util.legend.*;

import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import javax.swing.BorderFactory;

/**
 * StripChart.java
 *
 * StripChart is a dynamically updating strip chart.
 *
 **/

public class StripChart extends JCChart {
  Color tan = new Color(0xe2,0xc4,0x9c);
  Color lightTan = new Color(0xff,0xe2,0xba);

  public StripChart() {
    super(JCChart.PLOT);
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
      System.out.println("StripChart: " + e);
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

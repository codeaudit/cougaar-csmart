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

import java.util.Date;
import javax.swing.*;
import com.klg.jclass.chart.JCChart;
import com.klg.jclass.chart.JCAxis;
import com.klg.jclass.chart.ChartDataViewSeries;
import com.klg.jclass.chart.data.JCDefaultDataSource;
import com.klg.jclass.chart.ChartDataEvent;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * StripChartSource is a Chart Data Source that 
 * displays a chart of (value, timestamp) points and allows
 * the client to append additional (value, timestamp) points.
 */

public class StripChartSource extends JCDefaultDataSource implements Runnable {
  // number of points by which to scroll
  private int scrollBack = 10;
  // number of points displayed at once
  private int numberOfPoints = 30;
  // number of points to scroll at a time
  private int scrollBackPoints = 1;
  // marks current position in value arrays
  private int currentLastIndex = 0;
  // time at which the graph starts displaying
  long startTime;
  // label
  final String[] labels = { "Idle Time" };
  private JCChart chart;

  private transient Logger log;

  /**
   * Constructor.
   * @param c the chart this data source is a part of.
   */

  public StripChartSource(JCChart c) {
    super(null, null, null, null, "Strip Chart");
    chart = c;
    init();
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Init the data source.  Create the x and y value arrays,
   * and set the axis min and max according to the number of
   * points we're using.
   */

  private void init() {
    xvalues = new double[1][numberOfPoints];
    yvalues = new double[1][numberOfPoints];
    //    pointLabels = new String[numberOfPoints];
    //    seriesLabels = labels;
    currentLastIndex = 0;
    for (int i = 0; i < numberOfPoints; i++) {
      xvalues[0][i] = System.currentTimeMillis() / 1000;
      yvalues[0][i] = Double.MAX_VALUE;
    }
    chart.setBatched(true);
    chart.setBatched(false);
  }

  /**
   * Add a new point. Scroll the chart if necessary.
   * Only the y value is specified because it's a time based graph
   * so the x value is the current time in seconds.
   * @param value the y value to add
   */

  public void addValue(double value, long timestamp) {
    addNewPoint(value, timestamp);
    fireChartDataEvent(ChartDataEvent.RESET, 0, 0);

    if (currentLastIndex == numberOfPoints) {
      double xMinValue = xvalues[0][scrollBackPoints];
      System.arraycopy(xvalues[0], scrollBackPoints, xvalues[0], 0, 
		       numberOfPoints - scrollBackPoints);
      System.arraycopy(yvalues[0], scrollBackPoints, yvalues[0], 0, 
		       numberOfPoints - scrollBackPoints);
      currentLastIndex = currentLastIndex - scrollBackPoints;
      chart.setBatched(true);
      JCAxis xaxis = chart.getDataView(0).getXAxis();
      xaxis.setMin(xMinValue);
      chart.setBatched(false);
      fireChartDataEvent(ChartDataEvent.RESET, 0, 0);
    }
  }


  /**
   * Add new point.
   */

  private void addNewPoint(double yValue, long timestamp) {
    // time based x-axis defaults to seconds
    xvalues[0][currentLastIndex] = timestamp / 1000;
    yvalues[0][currentLastIndex] = yValue;
    currentLastIndex++;
  }

  // sample updating thread for debugging

  public void run() {
      if(log.isDebugEnabled()) {
        log.debug("Running....");
      }
    while (true) {
      try {
	Thread.sleep(1000);
	addValue((System.currentTimeMillis() % 100)*.01,
		 System.currentTimeMillis());
      } catch (Exception e) {
        if(log.isErrorEnabled()) {
          log.error("StripChartSource: ", e);
        }
      }
    }
  }

}



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

package org.cougaar.tools.csmart.ui.monitor.metrics;

import com.klg.jclass.chart.*;
import com.klg.jclass.util.legend.JCLegend;
import com.klg.jclass.chart.data.*;
import com.klg.jclass.util.swing.JCExitFrame;

import org.cougaar.util.log.Logger;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.tools.csmart.ui.util.ClientServletUtil;
import org.cougaar.tools.csmart.ui.monitor.generic.ExtensionFileFilter;
import org.cougaar.tools.csmart.ui.monitor.viewer.CSMARTUL;
import org.cougaar.tools.csmart.util.Sorting;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;

/**
 * Show a rough graph of Task completion
 */
public class CSMARTMetrics extends JPanel {

  String[] seriesLabels = {"Completed Tasks", "Unallocated", "Low Confidence Result"};

  private transient Logger log;

  Color seriesColors[] = {
    new Color(0x0a, 0x64, 0x0a), // Dark Green
    new Color(0xff, 0x7f, 0x50), // Coral
    new Color(0x32, 0xc3, 0x32), // Lime Green
    new Color(0xd2, 0xb4, 0x8c), // Tan 
  };

  protected JCChart chart;
  protected JTable dataTable;
  protected TableModel chartDataTableModel;
  protected String[] namearray = null;
  protected File inputFile;

  public CSMARTMetrics() {
    this(null, null);
  }

  public CSMARTMetrics(File f) {
    log = CSMART.createLogger("org.cougaar.tools.csmart.ui.monitor.metrics");
    try {
      ObjectInputStream ois = 
	new ObjectInputStream(new FileInputStream(f));
      chartDataTableModel = (TableModel)ois.readObject();
      namearray = (String[])ois.readObject();
    } catch( Exception e) {
      if(log.isDebugEnabled()) {
        log.error("Object read exception: " + e);
        log.error("CSMARTMetrics: Could not read the file.");
      }
    }

    init();
  }

  public CSMARTMetrics(ArrayList names, ArrayList data ) {
    createTableModel(names, data);
    init();
  }

  private void init() {
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(600, 400));

    chart = new JCChart(JCChart.STACKING_BAR);

    chart.getLegend().setVisible(true);

    add("Center", chart);

    // set bar data source from table model
    ChartDataView barDV = chart.getDataView(0);
    JCChartSwingDataSource csDataSource = 
      new JCChartSwingDataSource(chartDataTableModel, 
				 namearray, "CSMART Metric Data");
    barDV.setDataSource(csDataSource);

    // Grid lines on the Y axis
    barDV.getYAxis().setGridVisible(true);

    // Position X Axis at the bottom
    barDV.getXAxis().setPlacement(JCAxis.MIN);

    // Invert the chart; x-axis is drawn vertical.
    barDV.setInverted(true);

    List barSeriesList = barDV.getSeries();
    Iterator barIter = barSeriesList.iterator();
    for (int i = 0; barIter.hasNext(); i++) {
      ChartDataViewSeries thisSeries = (ChartDataViewSeries) barIter.next();
      thisSeries.getStyle().setFillColor(seriesColors[i]);
      thisSeries.setVisibleInLegend(true);
    }

  }

  /**
   * Creates the Data Model used for the graph.
   */
  private void createTableModel(ArrayList names, ArrayList data) {

    namearray = new String[names.size()];
    for(int i=0; i < names.size(); i++) {
      namearray[i] = (String)names.get(i);
    }

    // data is:
    //  -0- # of Tasks at all
    //  -1- # of those that are Unallocated Tasks
    //  -2- # of those allocated that have Low Confidence Results

    Vector metricData = new Vector();
    for(int i=0; i < data.size(); i ++) {
      Vector rowV = new Vector();
      Integer[] d = (Integer[])data.get(i);
      // Completed Tasks = Tasks - (Unalloced + Low Confidence)
      int tasks = ((Integer)d[0]).intValue();
      int unalloc = ((Integer)d[1]).intValue();
      int low = ((Integer)d[2]).intValue();
      rowV.addElement(new Integer(tasks - (unalloc + low)));
      rowV.addElement(d[1]);
      rowV.addElement(d[2]);
      metricData.addElement(rowV);
    }

    Vector agentNames = new Vector();
    for (int i = 0; i < seriesLabels.length; i++) {
      agentNames.addElement(seriesLabels[i]);
    }

    chartDataTableModel = new DefaultTableModel(metricData, agentNames);

  }

  private void saveMetrics(File outputFile) {
    if( outputFile == null ) {
      return;
    }
    String pathname = outputFile.getPath();
    String extension = "";
    int i = pathname.lastIndexOf('.');
    if(i > 0 && i < pathname.length()-1) {
      extension = pathname.substring(i+1).toLowerCase();
    }

    if(extension.length() == 0 || !extension.equals("mtr")) {
      pathname = pathname + ".mtr";
      outputFile = new File(pathname);
    }

    try {
    ObjectOutputStream oos = 
      new ObjectOutputStream(new FileOutputStream(outputFile));
    oos.writeObject(chartDataTableModel);
    oos.writeObject(namearray);
    oos.flush();
    oos.close();
    } catch(Exception e) {
      if(log.isDebugEnabled()) {
        log.error("Exception: " + e);
      }
    }

    inputFile = outputFile;
  }

  public void saveMetrics() {
    if(inputFile != null) {
      saveMetrics(inputFile);
    } else {
      saveAsMetrics();
    }
  }

  public void saveAsMetrics() {
    JFileChooser jfc = 
      new JFileChooser(System.getProperty("org.cougaar.install.path"));
    ExtensionFileFilter filter;
    String[] filters = { "mtr" };
    filter = new ExtensionFileFilter(filters, "Tasks Metric files");
    jfc.addChoosableFileFilter(filter);
    if(jfc.showSaveDialog(null) == JFileChooser.CANCEL_OPTION) {
      return;
    }
    File file = jfc.getSelectedFile();
    if(file != null) {
      saveMetrics(file);
    }
  }

  public void refresh() {
    Collection objectsFromServlet = CSMARTUL.getObjectsFromServlet(ClientServletUtil.METRICS_SERVLET);
    if (objectsFromServlet == null)
      return;
      if(log.isDebugEnabled()) {
        log.info("Received metrics: " + objectsFromServlet.size());
      }

    ArrayList names = new ArrayList();
    ArrayList data = new ArrayList();

    Iterator iter = objectsFromServlet.iterator();
    while(iter.hasNext()) {
      Object obj = iter.next();
      if(obj instanceof String) {
	names.add(obj);
      }	else if (obj instanceof Integer[]) { 
	data.add(obj);
      }
    }

    createTableModel(names, data);

    // set bar data source from table model
    ChartDataView barDV = chart.getDataView(0);
    JCChartSwingDataSource csDataSource = 
      new JCChartSwingDataSource(chartDataTableModel, 
				 namearray, "CSMART Metric Data");
    barDV.setDataSource(csDataSource);

    chart.update();
  }

  public static void main(String args[]) {
    JCExitFrame f = new JCExitFrame("Testing");
    CSMARTMetrics b = new CSMARTMetrics();
    f.getContentPane().add(b);
    f.pack();
    f.setVisible(true);
  }
}


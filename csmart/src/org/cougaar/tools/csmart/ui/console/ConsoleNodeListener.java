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

import java.awt.Color;
import java.io.*;
import java.util.Hashtable;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.server.NodeEvent;
import org.cougaar.tools.server.NodeEventListener;
import org.cougaar.tools.server.NodeServesClient;

import com.klg.jclass.chart.JCChart;
import com.klg.jclass.chart.ChartDataModel;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Listener for "pushed" Node activities.
 */

public class ConsoleNodeListener implements NodeEventListener {
  private CSMARTConsole console;
  private NodeComponent nodeComponent;
  private String nodeName;
  private String logFileName;
  private Writer logFile;
  private SimpleAttributeSet[] atts;
  private StripChartFrame chartFrame;
  private JCChart chart;
  private StripChartSource idleTimeDataModel;
  private NodeStatusButton statusButton;
  private long firsttime = 0l;
  private String notifyCondition = null;
  private boolean haveError = false;
  private ConsoleStyledDocument doc;
  private ConsoleNodeOutputFilter filter = null;
  private Object logFileLock = new Object();
  private int nLogEvents = 0;

  private transient Logger log;

  public ConsoleNodeListener(CSMARTConsole console,
			     NodeComponent nodeComponent,
			     String logFileName, 
			     NodeStatusButton statusButton,
                             ConsoleStyledDocument doc) throws IOException {


    createLogger();
    this.console = console;
    this.nodeComponent = nodeComponent;
    nodeName = nodeComponent.toString();

    // status button for node on console display
    this.statusButton = statusButton;
			       
    // wrap and capture to a log
    this.logFileName = logFileName;
    this.logFile = new BufferedWriter(new FileWriter(logFileName));

    // save the document that contains node output
    this.doc = doc;

    // create our attributes
    // stdout	0,0,0 (black)
    // stderr	192,64,64 (dark red)
    // Idle, Heartbeat	205,205,205 (light gray)
    // Node created, Node destroyed, Cluster Added	64,64,192 (dark blue)
    atts = new SimpleAttributeSet[4];
    atts[0] = new SimpleAttributeSet();
    StyleConstants.setForeground(atts[0], Color.black);
    atts[1] = new SimpleAttributeSet();
    StyleConstants.setForeground(atts[1], new Color(192, 64, 64));
    atts[2] = new SimpleAttributeSet();
    StyleConstants.setForeground(atts[2], new Color(205, 205, 205));
    atts[3] = new SimpleAttributeSet();
    StyleConstants.setForeground(atts[3], new Color(64, 64, 192));
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void setIdleChart(JCChart chart, ChartDataModel dataModel) {
    this.chart = chart;
    idleTimeDataModel = (StripChartSource)dataModel;
  }

  /**
   * Pull out the idleness number from a NodeEvent message.<br>
   * The idleness is:
   * <pre>(actual time difference between wakeups) - (expected difference) / (expected difference)</pre>
   *
   * @param nodemsg a <code>String</code> NodeEvent message to parse
   * @return a <code>double</code> idleness number, 0 on error
   */

  private double getIdleness(String nodemsg) {
    // node message is idletime:time of snapshot
    // where idletime is (actual time diff) - (expected time diff) / (expected time diff)
    // as a double
    // and time of snapshot is a long, as returned by System.currentTimeMillis()
    if (nodemsg == null || nodemsg.equals("")) {
      return 0.0;
    }
    String idle = nodemsg.substring(0, nodemsg.indexOf(':'));
    double ret = 0.0;
    try {
      ret = Double.parseDouble(idle);
    } catch (NumberFormatException e) {
      if(log.isDebugEnabled()) {
        log.error("Couldn't parse that idle string");
        e.printStackTrace();
      }
    }
    return ret;
  }

  /**
   * Get the timestamp of the idleness snapshot
   *
   * @param nodemsg a <code>String</code> NodeEvent message
   * @return a <code>long</code> timestamp in milliseconds
   */
  private long getTimestamp(String nodemsg) {
    // node message is idletime:time of snapshot
    // where idletime is (actual time diff) - (expected time diff) / (expected time diff)
    // as a double
    // and time of snapshot is a long, as returned by System.currentTimeMillis()
    if (nodemsg == null || nodemsg.equals("")) {
      return 0L;
    }
    String time = nodemsg.substring(nodemsg.indexOf(':') + 1);
    long ret = 0L;
    try {
      ret = Long.parseLong(time);
    } catch (NumberFormatException e) {
      if(log.isDebugEnabled()) {
        log.error("Couldn't parse that time string");
        e.printStackTrace();
      }
    }
    return ret;
  }
  
  /**
   * Get stdout, stderr, and any other messages from the node event;
   * write these to the log file and display these in the GUI.
   * TODO: rewrite this so that handle and handleAll don't repeat the same code
   */

  public void handle(NodeServesClient nsc, NodeEvent nodeEvent) {
    final NodeEvent myNodeEvent = nodeEvent; // for swing utilities
    final int nodeEventType = nodeEvent.getType();
    final String nodeEventDescription = getNodeEventDescription(nodeEvent);

    synchronized (logFileLock) {
      // write stdout/stderr/etc to log file
      try {
        logFile.write(nodeEventDescription);
        nLogEvents++;
        if (nLogEvents > 100) {
          logFile.flush();
          nLogEvents = 0;
        }
      } catch (Exception e) {
        if(log.isDebugEnabled()) {
          log.error("Exception writing to log file: " + e);
        }
      }
    }

    // ignore events user isn't interested in
    //    if (filter != null && !filter.includeEventInDisplay(nodeEvent))
    //      return;

    final SimpleAttributeSet style = getNodeEventStyle(nodeEventType);
    double nodeEventValue = 0;
    long nodeTimestamp = 0;
    if (nodeEventType == NodeEvent.IDLE_UPDATE) {
      String s = nodeEvent.getMessage();
      nodeEventValue = getIdleness(s);
      nodeTimestamp = getTimestamp(s);
    }
    final double idleTime = nodeEventValue;
    final long timestamp = nodeTimestamp;

    // must use swing "invokeLater" to be thread-safe
    try {
      SwingUtilities.invokeLater(new Runnable() {
	public void run() {
          if (nodeEventType == NodeEvent.IDLE_UPDATE) 
            handleIdleUpdate(idleTime, timestamp);
          else {
            updateStatus(myNodeEvent);
            // don't append events user isn't interested in
            if (filter != null && !filter.includeEventInDisplay(myNodeEvent))
              return;
            doc.appendString(nodeEventDescription, style);
          }
	}
      });
    } catch (RuntimeException e) {
    }
  }

  /**
   * Handle the specified list of node events (List of NodeEvent)
   * from a node. Same as handle, but for a list of events.
   */

  public void handleAll(final NodeServesClient nsc, 
			final java.util.List nodeEvents) {
    final int n = nodeEvents.size();
    if (n <= 0) {
      return;
    }

    // write a description of the event to the log file
    synchronized (logFileLock) {
      try {
        int i = 0;
        do {
          NodeEvent nodeEvent = (NodeEvent)nodeEvents.get(i);
          logFile.write(getNodeEventDescription(nodeEvent));
          nLogEvents++;
          if (nLogEvents > 100) {
            logFile.flush();
            nLogEvents = 0;
          }
        } while (++i < n);
      } catch (Exception e) {
        if(log.isDebugEnabled()) {
          log.error("Exception writing to log file: " + e);
        }
      }
    }

    // must use swing "invokeLater" to be thread-safe
    // collects descriptions of the same type and batch writes them to display
    // handles idle updates separately
    try {
      SwingUtilities.invokeLater(new Runnable() {
	public void run() {
	  int prevType = -1;
	  String prevDescription = "";
	  int nextEventIndex = -1;
	  // get the first description which is not an idle update
	  for (int i = 0; i < n; i++) {
	    NodeEvent nodeEvent = (NodeEvent)nodeEvents.get(i);
            //            if (filter != null && !filter.includeEventInDisplay(nodeEvent))
            //              continue; // skip events user isn't interested in
	    int nodeEventType = nodeEvent.getType();
	    if (nodeEventType == NodeEvent.IDLE_UPDATE) {
	      String s = nodeEvent.getMessage();
	      handleIdleUpdate(getIdleness(s), getTimestamp(s));
            } else {
	      updateStatus(nodeEvent);
              if (filter != null && !filter.includeEventInDisplay(nodeEvent))
                continue; // don't append events user isn't interested in
	      prevType = nodeEventType;
	      prevDescription = getNodeEventDescription(nodeEvent);
	      nextEventIndex = i+1;
	      break;
	    }
	  }
	  if (nextEventIndex == -1)
	    return; // all the events were idle updates or ignored
	  // start batching descriptions
	  for (int j = nextEventIndex; j < n; j++) {
	    NodeEvent nodeEvent = (NodeEvent)nodeEvents.get(j);
            //            if (filter != null && !filter.includeEventInDisplay(nodeEvent))
            //              continue; // skip events user isn't interested in
	    int nodeEventType = nodeEvent.getType();
	    String description = getNodeEventDescription(nodeEvent);
	    updateStatus(nodeEvent);
	    if (nodeEventType == NodeEvent.IDLE_UPDATE) {
	      String s = nodeEvent.getMessage();
	      handleIdleUpdate(getIdleness(s), getTimestamp(s));
	    } else if (nodeEventType == prevType) {
	      prevDescription += description;
	    } else {
              if (filter == null || filter.includeEventTypeInDisplay(prevType))
                doc.appendString(prevDescription, 
                                 getNodeEventStyle(prevType));
	      prevDescription = description;
	      prevType = nodeEventType;
	    }
	  }
          if (filter == null || filter.includeEventTypeInDisplay(prevType))
            doc.appendString(prevDescription,
                             getNodeEventStyle(prevType));
	}
      });
    } catch (RuntimeException e) {
    }
  }

  /**
   * Get Node Event description.
   */

  private final String getNodeEventDescription(final NodeEvent nodeEvent) {
    switch (nodeEvent.getType()) {
    case NodeEvent.STANDARD_OUT:
    case NodeEvent.STANDARD_ERR:
      return nodeEvent.getMessage();
    case NodeEvent.HEARTBEAT:
      return "@";
    default:
      return nodeEvent.toString();
    }
  }

  /**
   * Returns attribute set for a style of output: 
   * stdout, stderr, heartbeat, or default.
   */

  private final SimpleAttributeSet getNodeEventStyle(final int type) {
    switch (type) {
    case NodeEvent.STANDARD_OUT:
      return atts[0];
    case NodeEvent.STANDARD_ERR:
      return atts[1];
    case NodeEvent.HEARTBEAT:
      return atts[2];
    default:
      return atts[3];
    }
  }

  private void handleIdleUpdate(double idleTime, long timestamp) {
    double result = 1/Math.log(idleTime);
    result = (result + 1)*50; // in range 0 to 100
    if (result <= 16)
      statusButton.setStatus(NodeStatusButton.STATUS_LOW_BUSY);
    else if (result > 16 && result <= 33)
      statusButton.setStatus(NodeStatusButton.STATUS_MEDIUM_LOW_BUSY);
    else if (result > 33 && result <= 50)
      statusButton.setStatus(NodeStatusButton.STATUS_MEDIUM_BUSY);
    else if (result > 50 && result <= 67)
      statusButton.setStatus(NodeStatusButton.STATUS_MEDIUM_HIGH_BUSY);
    else if (result > 67 && result <= 83)
      statusButton.setStatus(NodeStatusButton.STATUS_HIGH_BUSY);
    else if (result > 83)
      statusButton.setStatus(NodeStatusButton.STATUS_BUSY);

    // reset times to 0. Maybe this looks better?
    if (firsttime > 0) {
      firsttime = timestamp;
      timestamp = 0l;
    }
    
    if (idleTimeDataModel != null) 
      idleTimeDataModel.addValue(result, timestamp);
  }

  private void updateStatus(NodeEvent nodeEvent) {
    int nodeEventType = nodeEvent.getType();
      if (nodeEventType == NodeEvent.NODE_CREATED) 
        statusButton.setStatus(NodeStatusButton.STATUS_NODE_CREATED);
      else if (nodeEventType == NodeEvent.NODE_DESTROYED) {
        statusButton.setStatus(NodeStatusButton.STATUS_NODE_DESTROYED);
        console.nodeStopped(nodeComponent);
      } else if (nodeEventType == NodeEvent.STANDARD_ERR) {
        statusButton.setStatus(NodeStatusButton.STATUS_STD_ERROR);
      }
  }

  /**
   * Clear an error; if the node status button was set because of a notify
   * condition or output on the standard error channel, then resume updating
   * the status button using messages from the node.
   */

  public void clearError() {
    haveError = false;
  }

  /**
   * Flush and close the log file.
   */

  public void closeLogFile() {
    synchronized (logFileLock) {
      try {
        logFile.close();
      } catch (Exception e) {
        if(log.isDebugEnabled()) {
          log.error("Exception closing log file: " + e);
        }
      }
    }
  }

  /**
   * Close the log file, fill the display with the contents of the log file,
   * and re-open the log file for appending.
   */

  public void fillFromLogFile() {
    synchronized (logFileLock) {
      try {
        logFile.close();
        doc.fillFromLogFile(logFileName);
        logFile = new BufferedWriter(new FileWriter(logFileName, true));
      } catch (Exception e) {
        if(log.isDebugEnabled()) {
          log.error(e.toString());
        }
      }
    }
  }

  /**
   * Set the console node output filter used to filter events displayed.
   */

  public void setFilter(ConsoleNodeOutputFilter filter) {
    this.filter = filter;
  }

  /**
   * Get the console node output filter used to filter events displayed.
   */

  public ConsoleNodeOutputFilter getFilter() {
    return filter;
  }

  /**
   * Return the document being filled by this listener.
   */

  public ConsoleStyledDocument getDocument() {
    return doc;
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}




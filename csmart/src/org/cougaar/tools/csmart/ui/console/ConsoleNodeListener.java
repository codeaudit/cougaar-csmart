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
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.cougaar.tools.csmart.ui.component.NodeComponent;
import org.cougaar.tools.server.NodeEvent;
import org.cougaar.tools.server.NodeEventListener;
import org.cougaar.tools.server.NodeServesClient;

import com.klg.jclass.chart.JCChart;
import com.klg.jclass.chart.ChartDataModel;

/**
 * Listener for "pushed" Node activities.
 */

public class ConsoleNodeListener implements NodeEventListener {
  private CSMARTConsole console;
  private NodeComponent nodeComponent;
  private String nodeName;
  private Writer logFile;
  //  private StyledDocument userDisplay;
  private ConsoleStyledDocument userDisplay;
  private SimpleAttributeSet[] atts;
  private StripChartFrame chartFrame;
  private JCChart chart;
  private StripChartSource idleTimeDataModel;
  private JRadioButton statusButton;
  private long firsttime = 0l;
  private String notifyCondition = null;
  private boolean haveError = false;

  public ConsoleNodeListener(CSMARTConsole console,
			     NodeComponent nodeComponent,
			     String logFileName, 
			     ConsoleStyledDocument userDisplay,
			     JRadioButton statusButton) throws IOException {

    this.console = console;
    this.nodeComponent = nodeComponent;
    nodeName = nodeComponent.toString();

    // status button for node on console display
    this.statusButton = statusButton;
			       
    // wrap and capture to a log
    this.logFile = new BufferedWriter(new FileWriter(logFileName));

    // save the GUI output
    this.userDisplay = userDisplay;

    // create our attributes
    // stdout is black, stderr is red, heartbeat messages are green,
    // others (what?) are blue
    atts = new SimpleAttributeSet[4];
    atts[0] = new SimpleAttributeSet();
    StyleConstants.setForeground(atts[0], Color.black);
    atts[1] = new SimpleAttributeSet();
    StyleConstants.setForeground(atts[1], Color.red);
    atts[2] = new SimpleAttributeSet();
    StyleConstants.setForeground(atts[2], Color.green);
    atts[3] = new SimpleAttributeSet();
    StyleConstants.setForeground(atts[3], Color.blue);
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
    //    System.err.println("From " + nodemsg + " got idle string " + idle);
    double ret = 0.0;
    try {
      ret = Double.parseDouble(idle);
    } catch (NumberFormatException e) {
      System.err.println("Couldn't parse that idle string");
      e.printStackTrace();
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
    //    System.err.println("From " + nodemsg + " got time string " + time);
    long ret = 0L;
    try {
      ret = Long.parseLong(time);
    } catch (NumberFormatException e) {
      System.err.println("Couldn't parse that time string");
      e.printStackTrace();
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

    // write stdout/stderr/etc to log file
    try {
      logFile.write(nodeEventDescription);
    } catch (Exception e) {
    }

    // must use swing "invokeLater" to be thread-safe
    try {
      SwingUtilities.invokeLater(new Runnable() {
	public void run() {
	  try {
	    if (nodeEventType == NodeEvent.IDLE_UPDATE) 
	      handleIdleUpdate(idleTime, timestamp);
	    else {
	      updateStatus(myNodeEvent);
              userDisplay.appendString(nodeEventDescription, style);
	    } 
	  } catch (Exception e) {
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
    try {
      int i = 0;
      do {
	NodeEvent nodeEvent = (NodeEvent)nodeEvents.get(i);
	logFile.write(getNodeEventDescription(nodeEvent));
      } while (++i < n);
    } catch (Exception e) {
      System.out.println("Exception writing to log file: " + e);
    }

    // 
    // bug here if "userDisplay" is destroyed
    //

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
	    int nodeEventType = nodeEvent.getType();
	    if (nodeEventType == NodeEvent.IDLE_UPDATE) {
	      String s = nodeEvent.getMessage();
	      handleIdleUpdate(getIdleness(s), getTimestamp(s));
	    } else {
	      updateStatus(nodeEvent);
	      prevType = nodeEventType;
	      prevDescription = getNodeEventDescription(nodeEvent);
	      nextEventIndex = i+1;
	      break;
	    }
	  }
	  if (nextEventIndex == -1)
	    return; // all the events were idle updates
	  // start batching descriptions
	  for (int j = nextEventIndex; j < n; j++) {
	    NodeEvent nodeEvent = (NodeEvent)nodeEvents.get(j);
	    int nodeEventType = nodeEvent.getType();
	    String description = getNodeEventDescription(nodeEvent);
	    updateStatus(nodeEvent);
	    if (nodeEventType == NodeEvent.IDLE_UPDATE) {
	      String s = nodeEvent.getMessage();
	      handleIdleUpdate(getIdleness(s), getTimestamp(s));
	    } else if (nodeEventType == prevType) {
	      prevDescription += description;
	    } else {
	      try {
                userDisplay.appendString(prevDescription, 
                                         getNodeEventStyle(prevType));
	      } catch (Exception e) {
		break;
	      }
	      prevDescription = description;
	      prevType = nodeEventType;
	    }
	  }
	  // write the last batch of descriptions
	  try {
            userDisplay.appendString(prevDescription,
                                     getNodeEventStyle(prevType));
	  } catch (Exception e) {
	  }
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
      colorStatusButton(CSMARTConsole.lowBusyStatus);
    else if (result > 16 && result <= 33)
      colorStatusButton(CSMARTConsole.mediumLowBusyStatus);
    else if (result > 33 && result <= 50)
      colorStatusButton(CSMARTConsole.mediumBusyStatus);
    else if (result > 50 && result <= 67)
      colorStatusButton(CSMARTConsole.mediumHighBusyStatus);
    else if (result > 67 && result <= 83)
      colorStatusButton(CSMARTConsole.highBusyStatus);
    else if (result > 83)
      colorStatusButton(CSMARTConsole.busyStatus);

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
      colorStatusButton(CSMARTConsole.idleStatus);
    else if (nodeEventType == NodeEvent.NODE_DESTROYED) {
      colorStatusButton(CSMARTConsole.errorStatus);
      console.nodeStopped(nodeComponent);
    } else if (nodeEventType == NodeEvent.STANDARD_ERR) {
      colorStatusButton(CSMARTConsole.stdErrStatus);
      haveError = true;
    }
    else if ((nodeEventType == NodeEvent.STANDARD_OUT) &&
             notifyCondition != null) {
      String s = nodeEvent.getMessage().toLowerCase();
      if (s.indexOf(notifyCondition) != -1) {
        colorStatusButton(CSMARTConsole.notifyStatus);
        haveError = true;
      }
    }
  }

  // color the node's status button according to type of node event received
  // but don't change the color after an error has occurred
  private void colorStatusButton(Color statusColor) {
    if (!haveError) {
      statusButton.setIcon(new ColoredCircle(statusColor, 20));
      statusButton.setSelectedIcon(new SelectedColoredCircle(statusColor, 20));
    } 
  }

  public void setNotifyCondition(String s) {
    notifyCondition = s.toLowerCase();
  }

  public void clearError() {
    haveError = false;
  }
}




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
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;

public class NodeStatusButton extends JRadioButton {
  boolean error;
  int status = STATUS_UNKNOWN;
  public static int STATUS_BUSY = 0;
  public static int STATUS_HIGH_BUSY = 1;
  public static int STATUS_MEDIUM_HIGH_BUSY = 2;
  public static int STATUS_MEDIUM_BUSY = 3;
  public static int STATUS_MEDIUM_LOW_BUSY = 4;
  public static int STATUS_LOW_BUSY = 5;
  public static int STATUS_IDLE = 6;
  public static int STATUS_ERROR = 7;
  public static int STATUS_NO_ANSWER = 8;
  public static int STATUS_UNKNOWN = 9;
  public static int STATUS_STD_ERROR = 10;
  public static int STATUS_NOTIFY = 11;

  // status button colors
  public static Color busyStatus = new Color(230, 255, 230); // shades of green
  public static Color highBusyStatus = new Color(175, 255, 175);
  public static Color mediumHighBusyStatus = new Color(0, 255, 0);
  public static Color mediumBusyStatus = new Color(0, 235, 0);
  public static Color mediumLowBusyStatus = new Color(0, 215, 0);
  public static Color lowBusyStatus = new Color(0, 195, 0);
  public static Color idleStatus = new Color(0, 175, 0);
  public static Color errorStatus = new Color(215, 0, 0); // red
  public static Color noAnswerStatus = new Color(245, 245, 0); // yellow
  public static Color unknownStatus = new Color(180, 180, 180); //gray
  public static Color stdErrStatus = new Color(215, 145, 0); //orange
  public static Color notifyStatus = Color.blue;
  public static Color[] statusColors = {
    busyStatus,
    highBusyStatus,
    mediumHighBusyStatus,
    mediumBusyStatus,
    mediumLowBusyStatus,
    lowBusyStatus,
    idleStatus,
    errorStatus,
    noAnswerStatus,
    unknownStatus,
    stdErrStatus,
    notifyStatus
  };
  private static String[] descriptions = {
    "extremely busy",
    "very busy",
    "busy",
    "somewhat busy",
    "somewhat idle",
    "idle",
    "node created",
    "node destroyed",
    "no answer",
    "unknown",
    "error",
    "notify"
  };

  public NodeStatusButton(Icon icon) {
    super(icon);
  }

  /**
   * Set the status for a node.  
   * Status setting is ignored if an error condition exists.
   */

  public void setStatus(int status) {
    if (error)
      return;
    if (status < 0 || status > (statusColors.length-1))
      return; // invalid status
    this.status = status;
    if (status == STATUS_NOTIFY || status == STATUS_STD_ERROR) {
      error = true;
      URL iconURL = getClass().getResource("Bang.gif");
      if (iconURL != null) {
        ImageIcon icon = new ImageIcon(iconURL);
        setIcon(icon);
        setSelectedIcon(new SelectedErrorIcon(icon.getImage(), 20));
      }
    } else {
      Color statusColor = statusColors[status];
      setIcon(new ColoredCircle(statusColor, 20));
      setSelectedIcon(new SelectedColoredCircle(statusColor, 20));
    }
    String s = getToolTipText((java.awt.event.MouseEvent)null);
    if (s == null)
      return;
    int index = s.lastIndexOf(':');
    if (index != -1) {
      s = s.substring(0, index+1) + descriptions[status];
      setToolTipText(s);
    }
  }

  public String getStatusDescription() {
    return descriptions[status];
  }

  /**
   * Clear error flag, so that status is again updated dynamically.
   */

  public void clearError() {
    error = false;
  }

  public static String[] getStatusDescriptions() {
    return descriptions;
  }

  public static Color[] getStatusColors() {
    return statusColors;
  }

}

/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import java.net.URL;

public class NodeStatusButton extends JRadioButton implements Observer {

  public static int STATUS_BUSY = 0;
  public static int STATUS_HIGH_BUSY = 1;
  public static int STATUS_MEDIUM_HIGH_BUSY = 2;
  public static int STATUS_MEDIUM_BUSY = 3;
  public static int STATUS_MEDIUM_LOW_BUSY = 4;
  public static int STATUS_LOW_BUSY = 5;
  public static int STATUS_NODE_CREATED = 6;
  public static int STATUS_NODE_DESTROYED = 7;
  public static int STATUS_NO_ANSWER = 8;
  public static int STATUS_UNKNOWN = 9;
  public static int STATUS_STD_ERROR = 10;
  public static int STATUS_NOTIFY = 11;
  public static int STATUS_MAX = 11;

  // status button colors
  public static Color busyStatus = new Color(230, 255, 230); // shades of green
  public static Color highBusyStatus = new Color(175, 255, 175);
  public static Color mediumHighBusyStatus = new Color(0, 255, 0);
  public static Color mediumBusyStatus = new Color(0, 235, 0);
  public static Color mediumLowBusyStatus = new Color(0, 215, 0);
  public static Color lowBusyStatus = new Color(0, 195, 0);
  public static Color nodeCreatedStatus = new Color(0, 175, 0);
  public static Color nodeDestroyedStatus = new Color(215, 0, 0); // red
  public static Color noAnswerStatus = new Color(245, 245, 0); // yellow
  public static Color unknownStatus = new Color(180, 180, 180); //gray
  public static Color[] statusColors = {
    busyStatus,
    highBusyStatus,
    mediumHighBusyStatus,
    mediumBusyStatus,
    mediumLowBusyStatus,
    lowBusyStatus,
    nodeCreatedStatus,
    nodeDestroyedStatus,
    noAnswerStatus,
    unknownStatus,
  };

  private static String[] descriptions = {
    "Extremely busy",
    "Very busy",
    "Busy",
    "Somewhat busy",
    "Somewhat idle",
    "Idle",
    "Node created",
    "Node destroyed",
    "No answer",
    "Unknown",
  };


  private Image img = null; // error image
  private NodeStatusButtonModel model = new NodeStatusButtonModel();

  /**
   * Creates a new <code>NodeStatusButton</code> icon.
   * @param icon  Icon for this button.
   */
  public NodeStatusButton(Icon icon) {
    super(icon);
    model.addObserver(this);
  }

  /**
   * Updates the state of the button, based on the contents of the model.
   *
   * @param o the Oberved model that caused the change.
   * @param arg Unique argument to identify the exact change.
   */
  public void update(Observable o, Object arg) {
    if(arg.equals(model.CLEAR_ERROR_EVENT)) {
      img = null;
      setIcon(new ColoredCircle(statusColors[model.getStatus()], 20, null));
      setSelectedIcon(new SelectedColoredCircle(statusColors[model.getStatus()], 20, null));
    }

    if(arg.equals(model.NOTIFY_SET_WARNING)) {
      URL iconURL = getClass().getResource("Bang.gif");
      if (iconURL != null) {
        ImageIcon icon = new ImageIcon(iconURL);
        img = icon.getImage();
      }
    }

    if(arg.equals(model.NOTIFY_EVENT)) {
      Color statusColor = statusColors[model.getStatus()];
      setIcon(new ColoredCircle(statusColor, 20, img));
      setSelectedIcon(new SelectedColoredCircle(statusColor, 20, img));
      String s = getToolTipText(null);
      if (s != null) {
        int index = s.lastIndexOf("), ");
        if (index != -1) {
          s = s.substring(0, index + 3) + descriptions[model.getStatus()];
          setToolTipText(s);
        }
      }
    }
  }

  /**
   * Gets a list of all Status Descriptions.
   *
   * @return <code>String</code> array of all descriptions.
   */
  public static String[] getStatusDescriptions() {
    return descriptions;
  }

  /**
   * Get a list of all possible status colors.
   * @return array of <code>Color</code>s
   */
  public static Color[] getStatusColors() {
    return statusColors;
  }


  /**
   * Gets a handle to the model for this button.
   *
   * @return <code>NodeStatusButtonModel</code> object
   */
  public NodeStatusButtonModel getMyModel() {
    return model;
  }

  public class NodeStatusButtonModel extends Observable {

    public String CLEAR_ERROR_EVENT = "Clear";
    public String NOTIFY_SET_WARNING = "Warning";
    public String NOTIFY_EVENT = "Notify";

    private int status = STATUS_UNKNOWN;
    private boolean notifyOnStandardError = false;


    public NodeStatusButtonModel() {
    }

    /**
     * Gets the current status of the button.
     *
     * @return current status
     */
    public int getStatus() {
      return this.status;
    }

    /**
     * Sets the current status of the button
     *
     * @param newStatus -- The new status
     */
    public void setStatus(int newStatus) {
      boolean warning = false;

      if (newStatus < 0 || newStatus > STATUS_MAX)
        return; // invalid status
      // if there's an error, add the warning icon
      if (newStatus == STATUS_NOTIFY || (newStatus == STATUS_STD_ERROR && notifyOnStandardError)) {
        setChanged();
        notifyObservers(NOTIFY_SET_WARNING);
      } else if (newStatus != STATUS_STD_ERROR) {
        status = newStatus;
      }
      setChanged();
      notifyObservers(NOTIFY_EVENT);
    }

    /**
     * Gets the description for the current status
     *
     * @return Current status description
     */
    public String getStatusDescription() {
      return descriptions[status];
    }

    /**
     * Remove error icon from the button.
     */
    public void clearError() {
      setChanged();
      notifyObservers(CLEAR_ERROR_EVENT);
    }


    /**
     * Set whether to display warning icon when output is received on standard error.
     *
     * @param notifyOnStandardError  - State to set.
     */
    public void setNotifyOnStandardError(boolean notifyOnStandardError) {
      this.notifyOnStandardError = notifyOnStandardError;
    }

    /**
     * Get the current state of the NotifyOnStandardError flag.
     *
     * @return Current state of NotifyOnStandardError
     */
    public boolean getNotifyOnStandardError() {
      return notifyOnStandardError;
    }
  }
}

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

package org.cougaar.tools.csmart.ui.util;

import java.util.*;
import javax.swing.JFrame;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * Ensure that window names are unique within the CSMARTUL application.
 * Notify observers (generally, just the CSMARTUL application) when a window
 * is created or destroyed, so that it
 * can maintain a list of window menu items.
 * Do not create this object directly.  Use Util.getNamedFrame() to
 * obtain the single NamedFrame object for the CSMARTUL application.
 * @see Util#getNamedFrame
 */

public class NamedFrame extends Observable {
  public static final String AGENT = "Agents";
  public static final String COMMUNITY = "Community";
  public static final String EVENTS = "Events";
  public static final String PLAN = "Plan";
  public static final String SOCIETY = "Society";
  public static final String THREAD = "Thread";
  public static final String HAPPINESS = "Happiness";
  public static final String ALLOC_FAILURE = "Allocations";
  public static final String INV_LEVEL = "Inventory";
  public static final String METRICS = "Metrics";
  public static final String TOPOLOGY = "Topology";

  private static transient Hashtable titleToFrame = new Hashtable();
  private static String titles[] = {
    COMMUNITY,
    AGENT,
    SOCIETY,
    PLAN,
    EVENTS,
    THREAD,
    HAPPINESS,
    ALLOC_FAILURE,
    INV_LEVEL,
    METRICS,
    TOPOLOGY
  };
  private static int index[] = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }; // used for common titles

  // If CSMART dies do not store the info on what windows are open
  private static transient NamedFrame singleton = null;

  private transient Logger log;

  public static synchronized NamedFrame getNamedFrame() {
    if (singleton == null) {
      singleton = new NamedFrame();
    }
    return singleton;
  }

  /**
   * Disallow new. Must use singleton instance via getNamedFrame
   **/
  private NamedFrame() {
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Add a frame with the specified title and notify observers.
   * The titles are either "common" titles as specified by the
   * Strings in this class, or they are the names of files from
   * which the graphs were read.
   * Returns a title which is unique (by appending a number if necessary).
   * @param title frame title
   * @param frame the frame
   */
  public String addFrame(String title, JFrame frame) {
    if (titleToFrame == null)
      titleToFrame = new Hashtable();

    // number commonly used titles
    for (int i = 0; i < titles.length; i++) {
      if (title.equals(titles[i])) {
	title = titles[i] + " " + index[i]++;
	break;
      }
    }
    String baseTitle = title;
    int otherIndex = 1;
    while (titleToFrame.get(title) != null)
      title = baseTitle + " " + otherIndex++;
    titleToFrame.put(title, frame);
    setChanged();
    // notify observers with title and frame
    notifyObservers(new Event(frame, title, Event.ADDED, null));
    frame.setTitle(title);
    return title;
  }

  public JFrame getFrame(String title) {
    if (titleToFrame == null)
      titleToFrame = new Hashtable();
    return (JFrame) titleToFrame.get(title);
  }

  public JFrame getToolFrame(String toolTitle) {
    if (titleToFrame == null)
      titleToFrame = new Hashtable();
    Enumeration titles = titleToFrame.keys();
    while (titles.hasMoreElements()) {
      String title = (String)titles.nextElement();
      if (title.startsWith(toolTitle)) 
	return (JFrame)titleToFrame.get(title);
    }
    return null;
  }

  public JFrame getToolFrame(String toolTitle, String societyName) {
    if (titleToFrame == null)
      titleToFrame = new Hashtable();
    String newTitle = toolTitle + ": " + societyName;
    Enumeration titles = titleToFrame.keys();
    while (titles.hasMoreElements()) {
      String title = (String)titles.nextElement();
      if (title.startsWith(toolTitle)) {
	JFrame frame = (JFrame)titleToFrame.get(title);
	if (title.equals(newTitle))
	  return frame;
	else {
	  // rename frame by removing and adding
	  titleToFrame.remove(title);
	  titleToFrame.put(newTitle, frame);
	  frame.setTitle(newTitle);
	  setChanged();
	  notifyObservers(new Event(frame, newTitle, Event.CHANGED, title));
	  return frame;
	}
      }
    }  
    return null;
  }

  /**
   * Remove a frame and notify observers.
   * @param frame the frame to remove
   */
  public void removeFrame(JFrame frame) {
    if (titleToFrame == null)
      titleToFrame = new Hashtable();
    //    String s = removeTitleColon(frame.getTitle());
    String s = frame.getTitle();
    titleToFrame.remove(s);
    setChanged();
    notifyObservers(new Event(frame, s, Event.REMOVED, null));
  }

//   public static String removeTitleColon(String title) {
//     int colon = title.indexOf(':');
//     if (colon >= 0) title = title.substring(0, colon).trim();
//     return title;
//   }

  public static class Event {
    // types of events
    public static int ADDED = 0;
    public static int REMOVED = 1;
    public static int CHANGED = 2;

    public JFrame frame;
    public String title;
    public int eventType;
    public String prevTitle;
    public Event(JFrame frame, String title, int eventType, String prevTitle) {
      this.frame = frame;
      this.title = title;
      this.eventType = eventType;
      this.prevTitle = prevTitle;
    }
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}


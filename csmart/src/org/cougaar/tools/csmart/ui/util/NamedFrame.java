/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.util;

import java.util.*;
import javax.swing.JFrame;

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

  private static Hashtable titleToFrame = new Hashtable();
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
    METRICS
  };
  private static int index[] = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }; // used for common titles

  private static NamedFrame singleton = null;

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
    //    System.out.println("NamedFrame: adding: " + title);
    titleToFrame.put(title, frame);
    setChanged();
    // notify observers with title and frame
    notifyObservers(new Event(frame, title, Event.ADDED, null));
    frame.setTitle(title);
    return title;
  }

  public JFrame getFrame(String title) {
    //    System.out.println("NamedFrame: getFrame: " + title);
    return (JFrame) titleToFrame.get(title);
  }

  public JFrame getToolFrame(String toolTitle, String societyName) {
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
    //    String s = removeTitleColon(frame.getTitle());
    String s = frame.getTitle();
    //    System.out.println("NamedFrame: removing: " + s);
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
}


/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.util;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;

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
  //  public static final String TOPOLOGY = "Topology";
  public static final String XML = "XML";

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
    XML
    //    METRICS,
    //    TOPOLOGY
  };
  private static int index[] = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}; // used for common titles

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
   * @return title of the frame
   */
  public String addFrame(String title, JFrame frame) {
    if (frame == null)
      return null;
    if (title == null)
      title = "Unknown Frame";

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
    return (JFrame) titleToFrame.get(title);
  }

  public JFrame getToolFrame(String toolTitle) {
    Enumeration titles = titleToFrame.keys();
    while (titles.hasMoreElements()) {
      String title = (String) titles.nextElement();
      if (title.startsWith(toolTitle))
        return (JFrame) titleToFrame.get(title);
    }
    return null;
  }

  public JFrame getToolFrame(String toolTitle, String societyName) {
    String newTitle = toolTitle + ": " + societyName;
    Enumeration titles = titleToFrame.keys();
    while (titles.hasMoreElements()) {
      String title = (String) titles.nextElement();
      if (title.startsWith(toolTitle)) {
        JFrame frame = (JFrame) titleToFrame.get(title);
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
    if (log.isDebugEnabled()) {
      log.debug("Removing frame " + s);
    }

    if (titleToFrame.remove(s) == null) {
      if (log.isInfoEnabled()) {
        log.info("Couldnt find frame " + s);
      }
      // Nothing changed so dont tell people it did
    } else {
      setChanged();
      notifyObservers(new Event(frame, s, Event.REMOVED, null));
    }
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
      throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    createLogger();
    titleToFrame = new Hashtable();
  }

}

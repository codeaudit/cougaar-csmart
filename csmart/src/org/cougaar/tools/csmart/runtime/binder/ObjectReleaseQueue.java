/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.runtime.binder;

import java.util.*;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

/**
 * An <code>Object</code> queue which also maintains the release-rate.
 * <p>
 * Objects are buffered until <tt>getReady(List)</tt> is called for a
 * specific time interval, at which point all the Objects that are "due"
 * are released to the "toL".  The buffer has a maximum "objectsPerSecond"
 * and one can degrade this rate by using <tt>degradeReleaseRate(..)</tt>.
 */
public class ObjectReleaseQueue {

  private double objectsPerMS;
  private CapacityChart capChart;
  private TrimmableArrayList objQ;
  private double nDueObjects;
  private transient Logger log;

  public ObjectReleaseQueue(double objectsPerSecond) {
    if (objectsPerSecond < 0.0) {
      throw new IllegalArgumentException(
          "Expecting maximum release rate be >= 0.0 per-second");
    }
    this.objectsPerMS = (objectsPerSecond / 1000.0);
    // begin with the base-line 100% capacity
    capChart = CapacityChartImpl.getRootInstance();
    // initially zero objects are due
    nDueObjects = 0.0;
    // really want a circular queue, but a List is okay:
    objQ = new TrimmableArrayList();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void degradeReleaseRate(
      double factor,
      long duration) {
    long nowMS = System.currentTimeMillis();
    capChart = capChart.create(factor, nowMS, (nowMS+duration));
  }

  public int size() {
    return objQ.size();
  }

  public boolean isEmpty() {
    return objQ.isEmpty();
  }

  public void add(Object o) {
    objQ.add(o);
  }

  /**
   * Get due Objects.
   */
  public void getDueObjects(
      List toL, 
      long prevTime, 
      long nowTime) {
    if (objQ.isEmpty()) {
      return;
    }
    //
    if (prevTime < capChart.getTime()) {
      // the capacity-chart has changed during the time interval
      //
      // there's a minor "slop" here, but it's okay...
      prevTime = capChart.getTime();
      if (prevTime == nowTime) {
        return;
      }
    }
    //
    double area = capChart.getArea(prevTime, nowTime);
    if (log.isDebugEnabled()) {
      log.debug("  getArea("+(nowTime-prevTime)+") -> "+area);
    }
    if (area > 0.0) {
      double d = (objectsPerMS * area);
      nDueObjects += d;
      if (log.isDebugEnabled()) {
        log.debug(
            "    objectsPerMS: "+objectsPerMS+" * area: "+area+" = "+d+
            ", nDueObjects -> "+nDueObjects);
      }
      if (nDueObjects >= 1.0) {
        // take nDueObjects
        int qsize = objQ.size();
        double flr = Math.floor(nDueObjects);
        int nDue = (int)flr;
        if (log.isDebugEnabled()) {
          log.debug(
              "release min(qsize: "+qsize+", nDue: "+nDue+
              " from flr("+nDueObjects+"), p -> "+(nDueObjects - flr));
        }
        nDueObjects -= flr;
        if (nDue >= qsize) {
          toL.addAll(objQ);
          objQ.clear();
        } else {
          for (int i = 0; i < nDue; i++) {
            toL.add(objQ.get(i));
          }
          objQ.removeRange(0, nDue);
        }
      }
    }
  }

  static class TrimmableArrayList extends ArrayList {
    public TrimmableArrayList() {
      super();
    }
    public TrimmableArrayList(int n) {
      super(n);
    }
    public void removeRange(int fromIdx, int toIdx) {
      super.removeRange(fromIdx, toIdx);
    }
  }

}


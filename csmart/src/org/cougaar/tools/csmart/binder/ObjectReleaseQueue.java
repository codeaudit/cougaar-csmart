/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.binder;

import java.util.*;

/**
 * An <code>Object</code> queue which also maintains the release-rate.
 * <p>
 * Objects are buffered until <tt>getReady(List)</tt> is called for a
 * specific time interval, at which point all the Objects that are "due"
 * are released to the "toL".  The buffer has a maximum "objectsPerSecond"
 * and one can degrade this rate by using <tt>degradeReleaseRate(..)</tt>.
 */
public class ObjectReleaseQueue {

  private static final boolean VERBOSE = false;

  private double objectsPerMS;
  private CapacityChart capChart;
  private TrimmableArrayList objQ;
  private double nDueObjects;

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
    if (VERBOSE) {
      System.out.println("  getArea("+(nowTime-prevTime)+") -> "+area);
    }
    if (area > 0.0) {
      double d = (objectsPerMS * area);
      nDueObjects += d;
      if (VERBOSE) {
        System.out.println(
            "    objectsPerMS: "+objectsPerMS+" * area: "+area+" = "+d+
            ", nDueObjects -> "+nDueObjects);
      }
      if (nDueObjects >= 1.0) {
        // take nDueObjects
        int qsize = objQ.size();
        double flr = Math.floor(nDueObjects);
        int nDue = (int)flr;
        if (VERBOSE) {
          System.out.println(
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


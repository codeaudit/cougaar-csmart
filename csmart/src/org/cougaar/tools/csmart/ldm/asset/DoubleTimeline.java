/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ldm.asset;

/**
 * Simple timeline of <tt>double</tt> values, for use by 
 * <code>HappinessBG</code> and <code>ThermalResourceModelBG</code>.
 */
public class DoubleTimeline 
implements java.io.Serializable, Cloneable {

  private int maxIdx;
  private long[] times;
  private double[] values;

  public DoubleTimeline() {
    times = new long[10];
    values = new double[10];
    times[0] = Long.MIN_VALUE;
    maxIdx = 0;
  }
  
  public long getStartTime() {
    // assume that (maxIdx >= 0), since creator should add an
    //   initial entry
    return times[1];
  }

  public long getEndTime() {
    return times[maxIdx];
  }

  public long getClosestTime(long time) {
    return times[getClosestIndex(time)];
  }

  public double getValueAt(long time) {
    return values[getClosestIndex(time)];
  }

  private final int getClosestIndex(final long time) {
    // assume that (maxIdx >= 0), since creator should add an
    //   initial entry
    if (time <= times[1]) {
      return 1;
    }
    if (time >= times[maxIdx]) {
      return maxIdx;
    }

    // binary search for closest (>=) time
    int low = 0;
    int high = maxIdx-1;
    while (true) {
      int mid = ((low + high) >> 1);  // ((low+high)/2);
      long midTime = times[mid];
      if (midTime < time) {
        low = mid + 1;
      } else if (midTime > time) {
        high = mid - 1;
      } else {
        // exact time found
        return mid;
      }
      if (low > high) {
        return (low-1);
      }
    }
  }

  public void setValueAt(long time, double value) {
    long endTime = times[maxIdx];
    if (time > endTime) {
      // add an entry
      //
      // ensure capacity
      if (++maxIdx >= times.length) {
        long[] oldTimes = times;
        int newLength = (maxIdx * 3)/2 + 1;
        times = new long[newLength];
        System.arraycopy(oldTimes, 0, times, 0, maxIdx);
        double[] oldValues = values;
        values = new double[newLength];
        System.arraycopy(oldValues, 0, values, 0, maxIdx);
      }
      // add the new entry
      times[maxIdx] = time;
      values[maxIdx] = value;
    } else if (time == endTime) {
      // reset the last entry
      values[maxIdx] = value;
    } else {
      throw new IllegalArgumentException(
          "Can only modify value in the future, not "+
          time+" < "+endTime);
    }
  }

  /**
   * Sample between the given time ranges.
   */
  public double[] toArray(
      long startTime, long endTime, long timeIncrement) {
    return toArray(
        null, 
        startTime, endTime, timeIncrement);
  }

  public double[] toArray(
      double[] toA, 
      long startTime, long endTime, long timeIncrement) {

    // check arguments
    if (startTime < getStartTime()) {
      throw new IllegalArgumentException(
          "Start time ("+startTime+") must be at least the "+
          "\"getStartTime()\" ("+
          getStartTime()+")");
    }
    if (endTime > getEndTime()) {
      throw new IllegalArgumentException(
          "End time ("+endTime+") must be less than or equal to the "+
          "\"getEndTime()\" ("+
          getEndTime()+")");
    }
    if (timeIncrement < 1) {
      throw new IllegalArgumentException(
          "Time increment ("+timeIncrement+") must be at least 1");
    }

    // figure out the minimal array size
    long tspan = (endTime - startTime);
    long lsize = 1 + (long)(((double)tspan)/timeIncrement);
    if (lsize > Integer.MAX_VALUE) {
      throw new IllegalArgumentException(
          "Can not have over "+Integer.MAX_VALUE+" entries ("+tspan+")");
    }
    int size = (int)lsize;

    // make sure the "toA" is large enough
    if ((toA == null) ||
        (toA.length < size)) {
      toA = new double[size];
    }

    // fill the array
    //
    // FIXME can optimize this to not use "getValueAt", which would
    //   save needless "times" array scanning.  Turns O(N*N*log(N)) to O(N).
    //   Of course, if the increments are large, this might slow things down...
    long t = startTime;
    for (int i = 0; i < size; i++) {
      toA[i] = getValueAt(t);
      t += timeIncrement;
    }

    return toA;
  }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException noClone) {
      // should never happen, since "this" is Cloneable
      throw new InternalError();
    }
  }

  public String toString() {
    return toString(false);
  }

  public String toString(boolean verbose) {
    StringBuffer buf = new StringBuffer();
    buf.append("Timeline(");
    buf.append(getStartTime());
    buf.append(" .. ");
    buf.append(getEndTime());
    buf.append(")");
    if (verbose) {
      buf.append("[");
      buf.append(maxIdx);
      buf.append("]:");
      for (int i = 1; i <= maxIdx; i++) {
        buf.append("\n\t");
        buf.append((i-1));
        buf.append("\t(");
        buf.append(times[i]);
        buf.append(",\t");
        buf.append(values[i]);
        buf.append(")");
      }
    }
    return buf.toString();
  }


  /** testing utility! */
  public static void main(String[] args) {
    DoubleTimeline dtl = new DoubleTimeline();
    System.out.println("create:\n"+dtl.toString(true));
    dtl.setValueAt(10, 0.5);
    System.out.println("init:\n"+dtl.toString(true));
    for (int i = 0; i <= 20; i++) {
      dtl.setValueAt((10+(3*i)), (((double)i) / 20.0));
    }
    System.out.println("configured:\n"+dtl.toString(true));
    for (int i = 0; i < 10; i++) {
      long t = 10 + (long)(Math.random() * 80.0);
      System.out.print("Random-Get("+t+"): ");
      System.out.println(dtl.getValueAt(t));
    }
    System.out.println("toA(15, 62, 3):");
    double[] da = dtl.toArray(15, 62, 3);
    for (int i = 0; i < da.length; i++) {
      System.out.println("  t: "+(15+(3*i))+", da["+i+"]: "+da[i]);
    }
    try {
      dtl.setValueAt(20, 0.1);
    } catch (Exception e) {
      System.out.println("Good -- caught invalid \"set(20, 0.1)\"");
    }
  }

}

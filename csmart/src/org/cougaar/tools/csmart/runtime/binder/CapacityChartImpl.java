/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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

import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;


/**
 * @see CapacityChart
 */
public class CapacityChartImpl implements CapacityChart {

  private static final boolean DEBUG = false;

  private static final CapacityChart ROOT_CAPACITY_CHART = 
    new RootCapacityChartImpl();

  private transient Logger log;

  /**
   * Create a 100% capacity chart for time <tt>0</tt> to 
   * <tt>Long.MAX_VALUE</tt>.
   * <p>
   * Once this is created one can use 
   * <tt>create(double, long, long)</tt> to alter 
   * the capacity.
   *
   * @see #create(double,long,long)
   */
  public static CapacityChart getRootInstance() {
    return ROOT_CAPACITY_CHART;
  }

  // 
  // Note coding convention:
  //   times are compared with "<" and "<="
  //   other values are compared with ">" and ">="
  // For example:
  //   if (start < end) ...
  //   if (f >= 0.0) ...
  //

  private double f;
  private long begin;
  private long end;
  private CapacityChart post;

  /**
   * @see #getRootInstance()
   * @see #create(double,long,long)
   */
  protected CapacityChartImpl() {
    log = CSMART.createLogger("org.cougaar.tools.csmart.runtime.binder");
  }

  /**
   * @see #getRootInstance()
   * @see #create(double,long,long)
   */
  protected CapacityChartImpl(
      double f, 
      long begin, 
      long end, 
      CapacityChart post) {

    log = CSMART.createLogger("org.cougaar.tools.csmart.runtime.binder");

    if (DEBUG) {
      ASSERT(f >= 0.0);
      ASSERT(1.0 > f);
      ASSERT(0 <= begin);
      ASSERT(begin < end);
      ASSERT(post != null);
    }
    //
    this.f = f;
    this.begin = begin;
    this.end = end;
    this.post = post;
  }

  public long getTime() {
    return begin;
  }

  public double getCapacity(long time) {
    if (log.isDebugEnabled()) {
      log.debug("--["+((int)(f*100))+"%].getCapacity("+time+")--");
    }
    //
    if (time < begin) {
      throw new IllegalArgumentException(
          "Request time ("+time+") must be at least this "+
          "CapacityChart's \"getTime()\" of ("+begin+")");
    }
    //
    if (end <= time) {
      // -begin -end (time ..
      return post.getCapacity(time);
    } else {
      // -begin (time .. end] (end ..
      return f;
    }
  }

  public double getArea(long start, long stop) {
    if (log.isDebugEnabled()) {
      log.debug(
          "--["+((int)(f*100))+"%].getArea("+
          start+", "+stop+")--");
    }
    //
    if (stop <= start) {
      throw new IllegalArgumentException(
          "Start time ("+start+") must be less than stop time ("+stop+")");
    } else if (start < begin) {
      throw new IllegalArgumentException(
          "Start time ("+start+") must be at least this "+
          "CapacityChart's \"getTime()\" of ("+begin+")");
    }
    //
    if (end <= start) {
      // -begin -end (start ..
      return post.getArea(start, stop);
    } else if (end < stop) {
      // -begin (start .. end] (end .. stop]
      //
      // figure out portion covered by "this"
      double covArea = 
        ((0.0 >= f) ?
         (0.0) :
         (f*((double)(end - start))));
      return covArea + post.getArea(end, stop);
    } else {
      //   -begin (start .. stop] (stop .. end]
      //
      // figure out portion covered by "this"
      double covArea = 
        ((0.0 >= f) ?
         (0.0) :
         (f*((double)(stop - start))));
      return covArea;
    }
  }

  /**
   * @see #getRootInstance()
   */
  public CapacityChart create(
      double factor, long start, long stop) {
    if (log.isDebugEnabled()) {
      log.debug(
          "--["+((int)(f*100))+"%].create("+
          factor+", "+start+", "+stop+")");
    }
    //
    if (factor == 1.0) {
      return this;
    }
    //
    if (stop <= start) {
      throw new IllegalArgumentException(
          "Start time ("+start+") must be less than stop time ("+stop+")");
    } else if (start < begin) {
      throw new IllegalArgumentException(
          "Start time ("+start+") must be at least this "+
          "CapacityChart's \"getTime()\" of ("+begin+")");
    } else if (0.0 > factor) {
      throw new IllegalArgumentException(
          "Capacity change ("+factor+") must be at least 0.0");
    } else if (factor > 1.0) {
      throw new IllegalArgumentException(
          "Capacity change ("+factor+
          ") must be less than or equal to 1.0");
    }
    //
    if (start < end) {
      // portion of time range interacts with "this"
      //
      // could get clever if both factor and this.f are zero,
      //   but let's keep it simple for now...
      // (factor * this.f) is "newF"
      double newF = factor * f;
      if (stop < end) {
        // override portion of "this"
        // -begin (start .. stop] -end
        return new CapacityChartImpl(newF, start, stop, this);
      } else {
        // override remainder of "this" and some of post
        // -begin (start .. end] (end .. stop]
        CapacityChart newPost = post.create(factor, end, stop);
        return new CapacityChartImpl(newF, start, end, newPost);
      }
    } else {
      // let post override
      // -begin -end (start .. stop]
      return post.create(factor, start, stop);
    }
  }

  private static final void ASSERT(boolean b) {
    if (!b) {
      throw new RuntimeException("ASSERT FAILED");
    }
  }

  /**
   * Inner class representing a 100% capacity from time <tt>0</tt> to
   * <tt>Long.MAX_VALUE</tt>.
   *
   * @see #getRootInstance()
   */
  private static class RootCapacityChartImpl
    implements CapacityChart {

    private transient Logger log;
      public RootCapacityChartImpl() {
        log = CSMART.createLogger("org.cougaar.tools.csmart.runtime.binder");
      }

      public long getTime() {
        return 0;
      }

      public double getCapacity(long time) {
        if (log.isDebugEnabled()) {
          log.debug("--[1].getCapacity("+time+")");
        }
        //
        return 1.0;
      }

      public double getArea(long start, long stop) {
        if (log.isDebugEnabled()) {
          log.debug("--[1].getArea("+start+", "+stop+")");
        }
        //
        if (stop <= start) {
          throw new IllegalArgumentException(
              "Start time ("+start+") must be less than "+
              "stop time ("+stop+")");
        } else if (start < 0) {
          throw new IllegalArgumentException(
              "Start time ("+start+") must be at least this "+
              "Chart's \"getTime()\" of (0)");
        }
        //
        return (double)(stop - start);
      }

      public CapacityChart create(
          double factor, long start, long stop) {
        if (log.isDebugEnabled()) {
          log.debug(
              "--[1].create("+factor+", "+start+", "+stop+")");
        }
        //
        if (factor == 1.0) {
          return this;
        }
        //
        if (stop <= start) {
          throw new IllegalArgumentException(
              "Start time ("+start+") must be less than "+
              "stop time ("+stop+")");
        } else if (start < 0) {
          throw new IllegalArgumentException(
              "Start time ("+start+") must be at least this "+
              "Chart's \"getTime()\" of (0)");
        } else if (0.0 > factor) {
          throw new IllegalArgumentException(
              "Capacity change ("+factor+") must be at least 0.0");
        } else if (factor > 1.0) {
          throw new IllegalArgumentException(
              "Capacity change ("+factor+
              ") must be less than or equal to 1.0");
        }
        //
        // (factor * 100%) is factor
        return new CapacityChartImpl(factor, start, stop, this);
      }

      private static final void ASSERT(boolean b) {
        if (!b) {
          throw new RuntimeException("ASSERT FAILED");
        }
      }
    }
}

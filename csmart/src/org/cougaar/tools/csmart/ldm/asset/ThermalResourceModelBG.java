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
package org.cougaar.tools.csmart.ldm.asset;

/**
 * ThermalResourceModelBG contains all information used by a local Asset.  
 * This includes data for the Heat Model graph.
 * Note that it works by decrementing local inventory immediately on request,
 * by the amount supplied in <code>decrementRate</code>
 * and slowly refilling based on the supplied <code>avgTime</code>.
 * This inventory and refill rate are subject to chaotic variations.
 * <p>
 * The semantics of this asset are that resources are used
 * immediately on request, but the use does not complete for
 * <code>avgTime</code>, and therefore usually a successful
 * response will not arrive until that point.
 *
 * @see SimpleInventoryBG
 * @see DoubleTimeline
 * @see #consume
 */
public class ThermalResourceModelBG 
extends DoubleTimeline
implements SimpleInventoryBG {

  /** Maximum inventory level */
  public static final double MAX_VALUE = 100.0;

  /** Name of the Asset **/
  private String name;

  /** Decrement Rate for Heat Model **/
  private double decrementRate;

  /** Average Time to complete asset **/
  private long avgTime;

  /** Rate to perturb inventory level **/
  private double invRate;

  /** Deviation from Inventory Rate **/
  private double invDev;

  /** Rate to perturb Completion Time **/
  private double timeRate;

  /** Deviation from Completion Rate **/
  private double timeDev;

  private double slope;

  public ThermalResourceModelBG() {
    super();
  }

  /**
   * Sets the name of the Asset
   *
   * @param name Name of the Asset
   */
  public void setName(String name) {
    if (this.name != null) {
      throw new IllegalArgumentException("Name already exists");
    } else {
      this.name = name;
    }
  }

  /**
   * Returns the name of the asset
   * <br>
   * @return Asset Name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the amount to decrement resource inventory for each use of the resource
   * <br>
   * @param rate The decrement rate for this resource
   */
  public void setDecrementRate(double rate) {
    if (this.decrementRate != 0) {
      throw new IllegalArgumentException("Decrement Rate already exists");
    } else {
      this.decrementRate = rate;
      if (this.avgTime > 0) {
        this.slope = this.decrementRate / this.avgTime;
      }
    }
  }

  /**
   * Gets the rate at which resources usage is decremented
   * <br>
   * @return Decrement Rate
   */
  public double getDecrementRate() {
    return this.decrementRate;
  }

  /**
   * Get the value of the average time.
   * @return Value of avgTime.
   */
  public long getAverageTime() {
    return avgTime;
  }

  /**
   * Set the value of avgTime.
   * @param v  Value to assign to avgTime.
   */
  public void setAverageTime(long  v) {
    if (v <= 0) {
      throw new IllegalArgumentException("Average Time must be greater than 0");
    }

    if (this.avgTime != 0) {
      throw new IllegalArgumentException("Average Time already exists");
    } else {
      this.avgTime = v;
      this.slope = this.decrementRate / this.avgTime;
    }
  }

  /**
   * Gets the deviation from the rate to perturb the inventory level.
   */
  public double getInventoryDeviation() {
    return this.invDev;
  }

  /**
   * Sets the deviation from the rate to perturb the inventory level.
   * <br>
   * @param dev The deviation rate. Strictly positive.
   */
  public void setInventoryDeviation(double dev) {
    if (this.invDev != 0) {
      throw new IllegalArgumentException(
          "Inventory Deviation already exists");
    } else if (dev < 0) {
      throw new IllegalArgumentException(
          "Inventory Deviation must be strictly positive");
    } else {
      this.invDev = dev;
    }
  }

  /**
   * Gets the deviation from the rate to perturb the time level.
   * <br>
   * @return The deviation rate
   */
  public double getTimeDeviation() {
    return this.timeDev;
  }

  /**
   * Sets the deviation from the rate to perturb the time level.
   * <br>
   * @param dev The deviation rate. Strictly positive.
   */
  public void setTimeDeviation(double dev) {
    if (this.timeDev != 0) {
      throw new IllegalArgumentException("Deviation Time already exists");
    } else if (dev < 0) {
      throw new IllegalArgumentException("Deviation Time must be positive");
    } else {
      this.timeDev = dev;
    }
  }

  public double getSlope() {
    return this.slope;
  }

  /**
   * Gets the current Inventory Level from the timeline.
   * <p>
   * Also see <tt>toArray</tt>.
   * <p>
   * Note that users should rely solely on the result of this
   * method to get the inventory level, and not on any values
   * from published Events.  Nor should users rely on the result of this
   * method to anticipate the success of a future request.
   * @see #consume
   **/
  public double getInventoryLevelAt(long time) {
    // somewhat wasteful double-lookup:
    long nearTime = super.getClosestTime(time);
    double nearValue = super.getValueAt(time);

    if (time == nearTime) {
      return nearValue;
    } else {      
      // Interpolate the value.   (y = m*x + b)
      double newValue = getSlope() * (time - nearTime) + nearValue;
      if (newValue > MAX_VALUE) {
        return MAX_VALUE;
      } else {
        return newValue;
      }
    }
  }

  /**
   * Sets the new inventory level in the TimeLine.
   * This should never be negative.
   **/
  public void setInventoryLevelAt(long time, double level) {
    if (level > MAX_VALUE) {
      throw new IllegalArgumentException(
          "Value greater than MAX_VALUE ("+MAX_VALUE+")!");
    } else if (level < 0) {
      throw new IllegalArgumentException(
          "Inventory cannot be negative");
    } 
    
    super.setValueAt(time, level);
  }

  /**
   * Note: This method has odd semantics.  The inventory level is decreased 
   * <em>immediately</em> if the request can be satisfied.  The method 
   * returns the time at which the request has been completely satisfied.
   * <p>
   * For example, if this is a request to deliver some goods, the resources
   * for delivering goods are used up immediately, but the goods are not 
   * delivered until later.  Subsequent requests take into account the 
   * partial utilization of the resources, regardless of whether the 
   * request has completed.<br>  Therefore, any published Event using the 
   * returned time indicates <em>completion</em> of the request.  That 
   * Event should not be taken to mean that the request <em>will be</em> 
   * successful, but rather that it <em>was</em> successful.
   * <p>
   * Any published Event using the current time (plus epsilon) would indicate
   * the <em>acceptance</em> of the request, but not the completion.  In 
   * either case, users of this asset should rely on it to indicate the 
   * current inventory level, and make no assumptions about the inventory 
   * level from the visible events.
   *
   * @see SimpleInventoryPG#consume
   */
  public long consume(long time) {
    double crntInvLevel;

    // Get the current inventory level
    crntInvLevel = getInventoryLevelAt(time);

    // Tweak the inventory level via the InvDev value, randomly
    crntInvLevel = 
      nextNormalSample(
          crntInvLevel, getInventoryDeviation());

    // If the randomized current inventory is less than
    // the amount each request requires, return failure (-1)
    if (crntInvLevel < getDecrementRate()) {
      return -1;
    } else if (crntInvLevel > MAX_VALUE) {
      // The inventory can never be more than the maximum????
      crntInvLevel = MAX_VALUE;
    }

    // Reset the inventory level after this request has been satisfied
    setInventoryLevelAt(time, (crntInvLevel - getDecrementRate()));

    // Now calculate when this request will be satisfied
    // It is the getAverageTime, modulo a random deviation
    long doneTime = 
      time + 
      (long)nextNormalSample(getAverageTime(), getTimeDeviation());

    // This if block ensures that time progresses - the request must be
    // done in the future
    if (doneTime <= time) {
      doneTime = time + 1;
    }

    return doneTime;
  } // end of consume

  public SimpleInventoryBG copy(SimpleInventoryPG hapPG) {
    return (SimpleInventoryBG)super.clone();
  }

  private static java.util.Random sharedRandom = 
    new java.util.Random();

  /**
   * Take a sampling from a normal distribution with the specified
   * <tt>median</tt> and <tt>stdDev</tt>.
   * @return double, possibly negative
   */
  public static final double nextNormalSample(
      final double median, 
      final double stdDev) {
    return (median + stdDev * sharedRandom.nextGaussian());
  }

} // end of ThermalResourceModelBG

/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */

package org.cougaar.tools.csmart.ldm.event;

import java.util.Iterator;

import org.cougaar.domain.planning.ldm.plan.DirectiveImpl;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.society.UID;

import org.cougaar.tools.csmart.util.EarthConstants;

import org.cougaar.tools.csmart.Constants;

/**
 * Implementation of <code>InfrastructureEvent</code>.
 *
 * @see InfrastructureEvent
 */
public class InfrastructureEventImpl
    extends DirectiveImpl
    implements NewInfrastructureEvent, Cloneable {

  // fields

  private UID uid = null;
  private boolean wire;
  private long duration; // in milliseconds
  private double intensity;
  private long time = 0l; // in milliseconds
  private String publisher;
  private RealWorldEvent parent = null;
  public ClusterIdentifier getOwner() { return source; }
  public UID getUID() { return uid; }
  public void setUID(UID uid) {
    if (this.uid != null) throw new IllegalArgumentException("UID already set");
    this.uid = uid;
  }

  // constructor

  public InfrastructureEventImpl() {}
  
  public InfrastructureEventImpl(UID uid) {
    setUID(uid);
  }

  public InfrastructureEventImpl(
      UID uid,
      ClusterIdentifier destination,
      String type,
      long duration,
      double intensity) {
    setUID(uid);
    setDestination(destination);
    setDuration(duration);
    setIntensity(intensity);
    setType(type);
  }

  // setters and getters

  public String getType() {
    if (isWireType()) {
      if (isBusyType()) {
        return Constants.InfEventType.WIRE_BUSY;
      } else {
        return Constants.InfEventType.WIRE_DOWN;
      }
    } else {
      if (isBusyType()) {
        return Constants.InfEventType.NODE_BUSY;
      } else {
        return Constants.InfEventType.NODE_DOWN;
      }
    }
  }

  public void setType(String type) {
    if (Constants.InfEventType.WIRE_BUSY.equals(type)) {
      this.wire = true;
      // need to set intensity
    } else if (Constants.InfEventType.WIRE_DOWN.equals(type)) {
      this.wire = true;
      this.intensity = 1.0;
    } else if (Constants.InfEventType.NODE_BUSY.equals(type)) {
      this.wire = false;
      // need to set intensity
    } else if (Constants.InfEventType.NODE_DOWN.equals(type)) {
      this.wire = false;
      this.intensity = 1.0;
    } else {
      throw new IllegalArgumentException(
        "Unknown Constant.InfEventType: "+type);
    }
  }

  public long getDuration() {
    return this.duration;
  }

  public void setDuration(long duration) {
    if (duration <= 0) {
      throw new IllegalArgumentException(
        "InfrastructureEvent duration must be >= 0, not "+duration);
    }
    this.duration = duration;
  }

  public double getIntensity() {
    return this.intensity;
  }

  public void setIntensity(double intensity) {
    if ((intensity < 0.0) || (intensity > 1.0)) {
      throw new IllegalArgumentException(
        "InfrastructureEvent intensity must be between 0.0 and 1.0, not "+
        intensity);
    }
    this.intensity = intensity;
  }

  public boolean isWireType() {
    return this.wire;
  }

  public boolean isNodeType() {
    return (!(this.wire));
  }

  public boolean isBusyType() {
    return (!isDownType());
  }

  public boolean isDownType() {
    //   (this.intensity >= 0.999)
    return (EarthConstants.approximately_equal(this.intensity, 1.0, 0.001));
  }

  // labels, toString, etc

  /**
   * Description, such as "AgentX Wire Down" or "AgentY Node Busy 89%".
   */
  public String getDescription() {
    // could cache this as "transient" and/or "WeakReference"
    StringBuffer buf = new StringBuffer();
    // Clean this up so the ClusterID is not so long?
    buf.append((this.destination != null) ? this.destination.toString() : "?");
    buf.append(this.wire ? " Wire " : " Node ");
    // FIXME should compare doubles within some epsilon value
    if (this.intensity < 1.0) {
      buf.append("Busy ");
      buf.append((int)(100.0 * this.intensity));
      buf.append("%");
    } else {
      buf.append("Down");
    }
    return buf.toString();
  }

  public String getMajorLabel() {
    return getDescription();
  }

  public String getMinorLabel() {
    return "@ " + getTime() + " for " + getDuration();
  }

  public String toString() {
    return
      "<InfrastructureEvent " +
      "  " + getDescription() +
      "  " + super.toString() + ">";
  }
  
  public boolean equals(Object ob) {
    if (ob == this) return true;
    if (ob instanceof InfrastructureEvent) {
      return uid.equals(((InfrastructureEvent)ob).getUID());
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    // just use the hashcode of the UID.  
    // this means Don't mix UIDs and InfrastructureEvents in the same hash table...
    return getUID().hashCode();
  }

  public void setSource(ClusterIdentifier asource) {
    ClusterIdentifier old = getSource();
    if (old != null) {
      if (! asource.equals(old)) {
        System.err.println("Bad InfrastructureEvent.setSource("+asource+") was "+old+":");
        Thread.dumpStack();
      }
    } else {
      super.setSource(asource);
    }
  }

  public void setDestination(ClusterIdentifier dest) {
    super.setDestination(dest);
  }
  
  // Private setter without destination check
  public void privately_setDestination(ClusterIdentifier dest) {
    super.setDestination(dest);
  }

  // must add stuff from Transferable, Event
  public void setTime(long time) {
    if (this.time != 0) {
      throw new IllegalArgumentException("Time already set");
    }
    this.time = time;
  }
  
  // Time at which this deadline timer goes off
  public long getTime() {
    return time;
  }

  // Who is expecting this answer? What Cluster is it in?
  // should I put in something like that?
  public void setPublisher(String publisher) {
    if (this.publisher != null) {
      throw new IllegalArgumentException("Publisher already set");
    }
    this.publisher = publisher;
  }

  public String getPublisher() {
    return this.publisher;
  }
  
  public void setParent(RealWorldEvent parent) {
    // check to avoid double-setting?
    this.parent = parent;
  }

  public RealWorldEvent getParent() {
    return this.parent;
  }
} // end of InfrastructureEventImpl.java

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

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.cougaar.util.EmptyIterator;
import org.cougaar.core.society.UID;

import org.cougaar.tools.csmart.Constants;
import org.cougaar.tools.csmart.util.*;
import org.cougaar.tools.csmart.ldm.plugin.transducer.Society;
import org.cougaar.tools.csmart.ldm.plugin.transducer.Agent;


/**
 * Implementation of the SimpleKEvent.
 * See <a href="https://www.ultralog.net/workinggroups/ExtEventPositionPaper-rbl8.pdf">CSMART Event paper</a> for model details.<br>
 *
 * @author <a href="mailto:wfarrell@bbn.com">Wilson Farrell</a>
 * @see RealWorldEvent
 */
public class SimpleKEventImpl extends KineticEventImpl
  implements NewSimpleKEvent {  
  
  private static final float MAX_FLOOD_RADIUS = 150F;
  private static final long FLOOD_RECOVERY_MULTIPLIER = 864000000L; // 10 days
  private static final float FLOOD_NODE_NET_RECOVER_RATIO = .5F;
  
  private static final float MAX_BOMB_RADIUS = 15F;
  // private static final long BOMB_RECOVERY_MULTIPLIER = 31536000000L; // 10 years
  private static final long BOMB_RECOVERY_MULTIPLIER = 10800000L; // 3 hours 
  private static final float BOMB_NODE_NET_RECOVER_RATIO = .5F;
  
  private static final float MAX_EARTHQUAKE_RADIUS = 50F;
  private static final long EARTHQUAKE_RECOVERY_MULTIPLIER = 432000000L; // 5 days
  private static final float EARTHQUAKE_NODE_NET_RECOVER_RATIO = .5F;
  private long duration = 0L;
  
  private double intensity = 0.0D;
  
  private LatLonPoint location = null;
  
  private class ImpMod implements ImpactModel {
    
    public Iterator getImpact(Society world, IEFactory theIEF) {
      ArrayList iEvents = new ArrayList();
      if (location == null) {
	//System.out.println("SKEImpl model: Empty iterator cause null location");
	return EmptyIterator.iterator();
      }
      float radius = 0F;
      long recoveryMultiplier = 0L;
      float nodeNetRecoverRatio = 1F;
      if (getType().equals(Constants.RWEType.FLOOD)) {
	radius = MAX_FLOOD_RADIUS * (float) getIntensity();
	recoveryMultiplier = FLOOD_RECOVERY_MULTIPLIER;
	nodeNetRecoverRatio = FLOOD_NODE_NET_RECOVER_RATIO;
      } else if (getType().equals(Constants.RWEType.BOMB)) {
	radius = MAX_BOMB_RADIUS * (float) getIntensity();
	recoveryMultiplier = BOMB_RECOVERY_MULTIPLIER;
	nodeNetRecoverRatio = BOMB_NODE_NET_RECOVER_RATIO;
      } else if (getType().equals(Constants.RWEType.EARTHQUAKE)) {
	radius = MAX_EARTHQUAKE_RADIUS * (float) getIntensity();
	recoveryMultiplier = EARTHQUAKE_RECOVERY_MULTIPLIER;
	nodeNetRecoverRatio = EARTHQUAKE_NODE_NET_RECOVER_RATIO;
      } else {
	//System.out.println("SKEImpl model: Empty iterator cause not known RWE type");
	return EmptyIterator.iterator();
      }
      
      Collection affectedAgents = world.getAgentsWithinKM(location,radius);
      //System.out.println("SKEImpl model: # of affectedAgents: " + affectedAgents.size());
      for (Iterator it = affectedAgents.iterator(); it.hasNext(); ) {
	Agent agent = (Agent) it.next();
	double distanceFromEpic = agent.distanceFrom(location);
	long wireDownTime = 0L;
	long nodeDownTime = 0L;
	
	if ( nodeNetRecoverRatio > 1L ) {
	  wireDownTime = duration + (long) (recoveryMultiplier * getIntensity() * ((radius - distanceFromEpic) / radius) / nodeNetRecoverRatio);
	  nodeDownTime = duration + (long) (recoveryMultiplier * getIntensity() * ((radius - distanceFromEpic) / radius));
	} else if (nodeNetRecoverRatio > 0L) {
	  wireDownTime = duration + (long) (recoveryMultiplier * getIntensity() * ((radius - distanceFromEpic) / radius));
	  nodeDownTime = duration + (long) (recoveryMultiplier * getIntensity() * ((radius - distanceFromEpic) / radius) * nodeNetRecoverRatio);
	}
	
	if (wireDownTime > 0L) {	  
	  NewInfrastructureEvent wireDownIE = theIEF.newInfrastructureEvent(
									    agent.getClusterIdentifier(),
									    Constants.InfEventType.WIRE_DOWN,
									    wireDownTime,
									    getIntensity());
	  wireDownIE.setParent(SimpleKEventImpl.this);
	  wireDownIE.setTime(getTime() + 1);
	  iEvents.add(wireDownIE);
	}
	
	if (nodeDownTime > 0L) {	  
	  NewInfrastructureEvent nodeDownIE = theIEF.newInfrastructureEvent(
									    agent.getClusterIdentifier(),
									    Constants.InfEventType.NODE_DOWN,
									    nodeDownTime,
									    getIntensity());
	  nodeDownIE.setParent(SimpleKEventImpl.this);
	  nodeDownIE.setTime(getTime() + 1);
	  iEvents.add(nodeDownIE);
	}
      } // end of iterator over affected Agents
      return iEvents.iterator();
    } // end of getImpact
  } // end of ImpMode inner class definition
  
    /** Simple Constructor **/
  public SimpleKEventImpl(UID uid) {
    super(uid);
  }
  
  public ImpactModel getModel() {
    return new ImpMod();
  }
  
  public void setType(String type) {
    if (    !type.equals(Constants.RWEType.FLOOD) &&
	    !type.equals(Constants.RWEType.BOMB) &&
	    !type.equals(Constants.RWEType.EARTHQUAKE)) {
      
      throw new IllegalArgumentException("type must be an kinetic event type defined in Constants.RWEType");
    }
    super.setType(type);
  }
  
  public void setDuration(long duration) {
    if (duration < 0) {
      throw new IllegalArgumentException("duration cannot be negative");
    }
    this.duration = duration;
  }
  
  public long getDuration() {
    return duration;
  }
  
  public void setIntensity(double intensity) {
    if ((intensity < 0.0) || (intensity > 1.0)) {
      throw new IllegalArgumentException("intensity must be between 0.0 and 1.0");
    }
    this.intensity = intensity;
  }
  
  public double getIntensity() {
    return intensity;
  }
  
  public void setLocation(LatLonPoint location) {
    if (location == null) {
      throw new IllegalArgumentException("location must not be null");
    }
    this.location = location;
  }
  
  public LatLonPoint getLocation() {
    return this.location;
  }
  
  /**
   * Return a list of Name-Value pairs for use by the UI.
   * The Names are Strings and the Values are to-Stringable Objects.<br>
   * It returns a list of <code>org.cougaar.tools.csmart.util.ArgValue</code>s
   *
   * @return a <code>List</code> of Name-Value pairs
   */
  public List getParameters() {
    
    ArrayList list = new ArrayList();
    list.add(new ArgValue("Type", this.getType()));
    list.add(new ArgValue("Location", new String(
						 this.location.getLatitude() +
						 "," +
						 this.location.getLongitude())));
    list.add(new ArgValue("Duration", new Long(this.duration)));
    list.add(new ArgValue("Intensity", new Double(this.intensity)));
    return list;
  }
  
  /**
   * <code>getDescription</code> method
   * Give a short (25-35) character description of the Event,
   * one which does not include the Class, times, or ID.
   * For use by the UI.
   *
   * @return a <code>String</code> description
   */
  public String getDescription() {
    return "Kinetic Event: " + getType()
      + " at " + getLocation() + ", Intensity: " + getIntensity() +
      ", and duration: " + getDuration();
  }
  
  /**
   * <code>getMajorLabel</code> method
   * Give a one or 2 word description of the Event, for use in a UI
   * This description should not include the Cluster name,
   * or Event class.
   *
   * @return a <code>String</code> label for the Event
   */
  public String getMajorLabel() {
    return "Type: " + getType();
  }
  
  /**
   * <code>getMinorLabel</code> method
   * Give a one or 2 word description of the Event, for use in a UI
   * This description should not include the Cluster name,
   * or Event class.  This second label should give more detail
   * to the major label.
   *
   * @return a <code>String</code> secondary label for the Event
   */
  public String getMinorLabel() {
    return "At " + getLocation();
  }
  
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("<SimpleKEvent ");
    buf.append(getUID().toString());
    buf.append(" VisibilityTime: ");
    buf.append(getTime());
    if (getPublisher() != null) {
      buf.append(" Published by: ");
      if (getSource() != null) {
        buf.append(getSource().toString() + ".");
      }
      buf.append(getPublisher());
    }
    buf.append(super.toString());
    buf.append(" Type: ");
    buf.append(getType());
    buf.append(" Location: ");
    buf.append(getLocation());
    buf.append(" Intensity: ");
    buf.append(getIntensity());
    buf.append(" Duration: ");
    buf.append(getDuration());
    buf.append(">");

    return buf.toString();
  }
  
  public boolean equals(Object ob) {
    if (ob == this) return true;
    if (ob instanceof SimpleKEvent) {
      return getUID().equals(((SimpleKEvent)ob).getUID());
    } else {
      return false;
    }
  }

  public int hashCode() {
    // just use the hashcode of the UID.  
    // this means Don't mix UIDs and SimpleKEvents in the same hash table...
    return getUID().hashCode();
  }
} // end of SimpleKEventImpl.java


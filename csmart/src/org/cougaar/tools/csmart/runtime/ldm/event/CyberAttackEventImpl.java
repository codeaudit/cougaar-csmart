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
package org.cougaar.tools.csmart.runtime.ldm.event;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.cougaar.util.EmptyIterator;

import org.cougaar.core.util.UID;
import org.cougaar.core.agent.ClusterIdentifier;

import org.cougaar.tools.csmart.util.ArgValue;
import org.cougaar.tools.csmart.Constants;
import org.cougaar.tools.csmart.runtime.ldm.plugin.transducer.Society;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Implementation of the CyberAttackEvent. See <a href="https://www.ultralog.net/workinggroups/ExtEventPositionPaper-rbl8.pdf">CSMART Event paper</a> for model details.
 *
 * @author <a href="mailto:wfarrell@bbn.com">Wilson Farrell</a>
 * @see RealWorldEvent
 */
public class CyberAttackEventImpl extends RealWorldEventImpl
  implements NewCyberAttackEvent {
  
  private long duration = 0l; // in milliseconds
  
  private double intensity = 0.0d;
  
  private ClusterIdentifier targetAgentCID = null;
  
  private class ImpMod implements ImpactModel {
    
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.runtime.ldm.event");

    public Iterator getImpact(Society world, IEFactory theIEF) {
      
      ArrayList iEvents = new ArrayList();
      if (world.existsAgent(targetAgentCID)) {
        if(log.isDebugEnabled()) {
          log.debug("CAEImpl model: calcing impacts on existing Agent " + 
                    targetAgentCID.toString());
        }
	NewInfrastructureEvent ie = theIEF.newInfrastructureEvent();
	ie.setDestination(getTarget());
	ie.setDuration(getDuration());
	ie.setIntensity(getIntensity());
	ie.setParent(CyberAttackEventImpl.this);
	ie.setTime(getTime() + 1);
	if (getType() != null) {
	  if (getType().equals(Constants.RWEType.DOSNODE)) {
	    ie.setType(Constants.InfEventType.NODE_BUSY);
	    iEvents.add(ie);
	  } else if (getType().equals(Constants.RWEType.DOSNET)) {
	    ie.setType(Constants.InfEventType.WIRE_BUSY);
	    iEvents.add(ie);
	  } else if (getType().equals(Constants.RWEType.ISOLATENODE)) {
	    ie.setType(Constants.InfEventType.WIRE_DOWN);
	    iEvents.add(ie);
	  } else if (getType().equals(Constants.RWEType.KILLNODE)) {
	    ie.setType(Constants.InfEventType.NODE_DOWN);
	    iEvents.add(ie);
	  }
	} // else, we don't know about it and we don't add the event
        if(log.isDebugEnabled()) {
          log.debug("CAE model done handling event " + ie);
        }
      } else {
	// else, publish nothing
        if(log.isDebugEnabled()) {
          log.debug("CAE model says world " + world + " doesnt know about agent " + targetAgentCID.toString());
        }
	return EmptyIterator.iterator();
      }
      return iEvents.iterator();
    }
  }
  
  /** Simple Constructor **/
  public CyberAttackEventImpl(UID uid) {
    super(uid);
  }

  public ImpactModel getModel() {    
    return new ImpMod();
  }
  
  public void setType(String type) {    
    if (    !type.equals(Constants.RWEType.DOSNODE) &&
	    !type.equals(Constants.RWEType.DOSNET) &&
	    !type.equals(Constants.RWEType.ISOLATENODE) &&
	    !type.equals(Constants.RWEType.KILLNODE)) {
      
      throw new IllegalArgumentException("type must be an attack type defined in Constants.RWEType");
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
  
  public void setTarget(ClusterIdentifier targetAgentCID) {
    if (targetAgentCID == null) {
      throw new IllegalArgumentException("targetAgentCID must not be null");
    }
    this.targetAgentCID = targetAgentCID;
  }
  
  public ClusterIdentifier getTarget() {
    return this.targetAgentCID;
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
    list.add(new ArgValue("Target", this.targetAgentCID));
    list.add(new ArgValue("Duration", new Long(this.duration)));
    list.add(new ArgValue("Intensity", new Double(this.intensity)));
    return list;
  }

  /**
   * Give a short (25-35) character description of the Event,
   * one which does not include the Class, times, or ID.
   * For use by the UI.
   *
   * @return a <code>String</code> description
   */
  public String getDescription() {
    return "Cyber Attack: " + getType()
     + " against " + getTarget().toString() + ", Intensity: " + getIntensity() +
     ", and duration: " + getDuration();
  }

  /**
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
   * Give a one or 2 word description of the Event, for use in a UI
   * This description should not include the Cluster name,
   * or Event class.  This second label should give more detail
   * to the major label.
   *
   * @return a <code>String</code> secondary label for the Event
   */
  public String getMinorLabel() {
      return "Target: " + getTarget().toString();
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("<CyberAttackEvent ");
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
    buf.append(" Target: ");
    buf.append(getTarget().toString());
    buf.append(" Intensity: ");
    buf.append(getIntensity());
    buf.append(" Duration: ");
    buf.append(getDuration());
    buf.append(">");

    return buf.toString();
  }

  public boolean equals(Object ob) {
    if (ob == this) return true;
    if (ob instanceof CyberAttackEvent) {
      return getUID().equals(((CyberAttackEvent)ob).getUID());
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    // just use the hashcode of the UID.  
    // this means Don't mix UIDs and CyberAttackEvents in the same hash table...
    return getUID().hashCode();
  }

} // End of CyberAttackEventImpl


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

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.plan.DirectiveImpl;

import org.cougaar.tools.csmart.util.ArgValue;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Parent for all adverse real world events, with which you impact
 * as large scale system. Do not use this event, but rather a
 * subclass - <code>KineticEvent</code> or <code>CyberAttackEvent</code>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see NewRealWorldEvent
 */
public abstract class RealWorldEventImpl extends DirectiveImpl
  implements NewRealWorldEvent {
  
  private UID uid = null;
  private String publisher;
  private long time = 0l; // in millis

  /**
   * The type of this RWE - a constant from the Constants file
   */
  private String myType = null;
  private transient Logger log;

  public ClusterIdentifier getOwner() {
    return source;
  }
  
  public UID getUID() {
    return uid;
  }
  
  public void setUID(UID uid) {
    if (this.uid != null) throw new IllegalArgumentException("UID already set");
    this.uid = uid;
  }
  
  /**
   * Creates a new <code>RealWorldEventImpl</code> instance.
   *
   * @param uid an <code>UID</code> for the Event
   */
  public RealWorldEventImpl (UID uid) {
    createLogger();
    setUID(uid);
  }

  private void createLogger() {
    log = CSMART.createLogger("org.cougaar.tools.csmart.runtime.ldm.event");
  }

  /**
   * Set the type of adverse event, using a String value
   * from <code>org.cougaar.tools.csmart.Constants</code>
   * <br>
   * @param type a <code>String</code> event type constant
   */
  public void setType(String type) {
    this.myType = type;
  }
  
  /**
   * The type of Attack or Event occuring.  The value is a
   * constant from <code>org.cougaar.tools.csmart.Constants</code>
   *<br>
   * @return a <code>String</code> event type
   */
  public String getType() {
    return this.myType;
  }

  /**
   * Each <code>RealWorldEvent<code> must return a model of its impact.
   * This method does that.
   *
   * Note that this method is not implemented here. Subclasses must
   * implement it.
   *
   * @return an <code>ImpactModel</code> of the events impact.
   */
  public abstract ImpactModel getModel();

  /**
   * Return a list of Name-Value pairs for use by the UI.
   * The Names are Strings and the Values are to-Stringable Objects.<br>
   * It returns a list of <code>org.cougaar.tools.csmart.util.ArgValue</code>s
   * 
   * @return a <code>List</code> of Name-Value pairs
   */
  public List getParameters() {
    // return a list containing just the type and the start time
    ArrayList list = new ArrayList();
    list.add(new ArgValue("Type", "RealWorldEvent"));
    return list;
  }
  
  public void setSource(ClusterIdentifier asource) {
    ClusterIdentifier old = getSource();
    if (old != null) {
      if (! asource.equals(old)) {
        if(log.isDebugEnabled()) {
          log.error("Bad RealWorldEvent.setSource("+asource+") was "+old+":");
          Thread.dumpStack();
        }
      }
    } else {
      super.setSource(asource);
    }
  }

  /**
   * @param time a <code>long</code> time in millis at which this event occurs
   */
  public void setTime(long time) {
    if (this.time != 0) {
      throw new IllegalArgumentException("Time already set");
    }
    this.time = time;
  }
  
  /**
   * @return a <code>long</code> time in millis at which this event occurs
   */
  public long getTime() {
    return time;
  }

  public void setPublisher(String publisher) {
    if (this.publisher != null) {
      throw new IllegalArgumentException("Publisher already set");
    }
    this.publisher = publisher;
  }

  public String getPublisher() {
    return this.publisher;
  }
  
}// RealWorldEventImpl

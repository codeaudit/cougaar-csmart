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
package org.cougaar.tools.csmart.ldm.event;

import java.io.Serializable;

import org.cougaar.core.cluster.Publishable;
import org.cougaar.core.society.UniqueObject;
import org.cougaar.core.society.UID;
import org.cougaar.domain.planning.ldm.plan.Directive;

import org.cougaar.tools.csmart.Constants;

/**
 * A low-level disturbance/"attack" that is targeted upon an agent.
 * <p>
 * The attack can be targeted on the WIRE (i.e. modelled inter-agent 
 * communication load) or NODE (i.e. modelled agent CPU load).  The
 * intensity can be between 1.0 (100% == shut-down for the specified
 * duration) and 0.0 (0% == no effect), with in-between values 
 * representing a slow-down.
 */
public interface InfrastructureEvent 
    extends Directive, UniqueObject, Publishable, Serializable {

  /**
   * Get the <tt>Constants.InfEventType</tt> "type" of this event.
   * <pre>
   * The valid types are:
   *   <tt>Constants.InfEventType.WIRE_BUSY</tt>
   *   <tt>Constants.InfEventType.WIRE_DOWN</tt>
   *   <tt>Constants.InfEventType.NODE_BUSY</tt>
   *   <tt>Constants.InfEventType.NODE_DOWN</tt>
   * </pre>
   * @see org.cougaar.tools.csmart.Constants
   * @see #isWireType()
   * @see #isNodeType()
   * @see #isBusyType()
   * @see #isDownType()
   */
  String getType();
  
  /**
   * Get the duration in simulation cycles.
   */
  long getDuration();

  /**
   * Get the intensity value (0.0 &lt;= <tt>getIntensity()</tt> &lt;= 1.0).
   * <p>
   * If <tt>isDownType()</tt> then the intensity is defined to be 1.0,
   * otherwise (<tt>isBusyType()</tt>) the intensity is less than 1.0.
   */
  double getIntensity();

  //
  // Helpers based on getType():
  //

  /**
   * Equivalent to<pre>
   *   <tt>(getType().equals(org.cougaar.tools.csmart.Constants.WIRE_BUSY) ||
   *        getType().equals(org.cougaar.tools.csmart.Constants.WIRE_DOWN))</tt>
   * also
   *   <tt>(!(isNodeType()))</tt>
   * </pre>
   */
  boolean isWireType();

  /**
   * Equivalent to<pre>
   *   <tt>(getType().equals(org.cougaar.tools.csmart.Constants.NODE_BUSY) ||
   *        getType().equals(org.cougaar.tools.csmart.Constants.NODE_DOWN))</tt>
   * also
   *   <tt>(!(isWireType()))</tt>
   * </pre>
   */
  boolean isNodeType();

  /**
   * Equivalent to<pre>
   *   <tt>(getType().equals(org.cougaar.tools.csmart.Constants.NODE_BUSY) ||
   *        getType().equals(org.cougaar.tools.csmart.Constants.WIRE_BUSY))</tt>
   * also
   *   <tt>(getIntensity() &lt; 1.0)</tt>
   * also
   *   <tt>(!(isDownType()))</tt>
   * </pre>
   */
  boolean isBusyType();

  /**
   * Equivalent to<pre>
   *   <tt>(getType().equals(org.cougaar.tools.csmart.Constants.NODE_DOWN) ||
   *        getType().equals(org.cougaar.tools.csmart.Constants.WIRE_DOWN))</tt>
   * also
   *   <tt>(getIntensity() == 1.0)</tt>
   * also
   *   <tt>(!(isBusyType()))</tt>
   * </pre>
   */
  boolean isDownType();
  
  /**
   * Time at which this event should occur.
   */
  long getTime();

  /**
   * Get the <code>String</code> name of the publisher of this Event.
   */
  String getPublisher();

  /**
   * Get the event which caused this one.
   *
   * @return a <code>RealWorldEvent</code> cause
   */
  RealWorldEvent getParent();
}

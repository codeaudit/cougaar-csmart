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
  public String getType();
  
  /**
   * Get the duration in simulation cycles.
   */
  public long getDuration();

  /**
   * Get the intensity value (0.0 &lt;= <tt>getIntensity()</tt> &lt;= 1.0).
   * <p>
   * If <tt>isDownType()</tt> then the intensity is defined to be 1.0,
   * otherwise (<tt>isBusyType()</tt>) the intensity is less than 1.0.
   */
  public double getIntensity();

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
  public boolean isWireType();

  /**
   * Equivalent to<pre>
   *   <tt>(getType().equals(org.cougaar.tools.csmart.Constants.NODE_BUSY) ||
   *        getType().equals(org.cougaar.tools.csmart.Constants.NODE_DOWN))</tt>
   * also
   *   <tt>(!(isWireType()))</tt>
   * </pre>
   */
  public boolean isNodeType();

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
  public boolean isBusyType();

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
  public boolean isDownType();
  
  /**
   * Time at which this event should occur.
   */
  public long getTime();

  /**
   * Get the <code>String</code> name of the publisher of this Event.
   */
  public String getPublisher();

  /**
   * Get the event which caused this one.
   *
   * @return a <code>RealWorldEvent</code> cause
   */
  public RealWorldEvent getParent();
}

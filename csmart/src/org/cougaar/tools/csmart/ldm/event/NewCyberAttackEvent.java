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

import org.cougaar.core.cluster.ClusterIdentifier;

/**
 * Create a new <code>CyberAttackEvent</code>.
 * You specify (in addition to type, etc) the intensity, duration,
 * and target (Agent to attack).
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see CyberAttackEvent
 * @see NewRealWorldEvent
 */
public interface NewCyberAttackEvent extends CyberAttackEvent, NewRealWorldEvent {

  /**
   * Set the Agent to attack, specifying the <code>ClusterIdentifier</code>.
   *
   * @param agent a <code>ClusterIdentifier</code> Agent to attack
   */
  public void setTarget(ClusterIdentifier agent);

  /**
   * Set the peak intensity of this attack, as a value from 0 to 1.
   *
   * @param mag a <code>double</code> peak intensity
   */
  public void setIntensity(double mag);

  /**
   * Set the length of the attack, including time for the attack
   * to ramp up and down.
   *
   * @param length a <code>long</code> length of the attack in milliseconds
   */
  public void setDuration(long length);
} // end of NewCyberAttackEvent.java

/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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
import org.cougaar.core.agent.ClusterIdentifier;

/**
 * Event modelling attacks to the information infrastructure of a system.
 * These are simple models for now: They have a target, duration, and intensity.
 * The type of attack indicates how the attack is effected.  As more
 * attacks are modeled, a more generic CyberAttackEvent should be
 * created and this implementation should be a subclass such as
 * DoSAttackEvent.
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see RealWorldEvent
 */
public interface CyberAttackEvent extends RealWorldEvent {

  /**
   * Get the Agent or Node to Attack.
   *
   * @return a <code>ClusterIdentifier</code> Agent to attack
   */
  ClusterIdentifier getTarget();

  /**
   * Get the peak intensity of the attack.
   * Values are <code>0</code> to <code>1</code>.
   * @return a <code>double</code> attack peak intensity
   */
  double getIntensity();

  /**
   * Get the duration of the Attack, including time for the
   * attack to begin to take effect and to wear off.
   *
   * @return a <code>long</code> attack duration
   */
  long getDuration();
} // end of CyberAttackEvent.java

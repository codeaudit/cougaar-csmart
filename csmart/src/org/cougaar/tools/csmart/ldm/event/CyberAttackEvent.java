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
import org.cougaar.core.cluster.ClusterIdentifier;

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
  public ClusterIdentifier getTarget();

  /**
   * Get the peak intensity of the attack.
   * Values are <code>0</code> to <code>1</code>.
   * @return a <code>double</code> attack peak intensity
   */
  public double getIntensity();

  /**
   * Get the duration of the Attack, including time for the
   * attack to begin to take effect and to wear off.
   *
   * @return a <code>long</code> attack duration
   */
  public long getDuration();
} // end of CyberAttackEvent.java

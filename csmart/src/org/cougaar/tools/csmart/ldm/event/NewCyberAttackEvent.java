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

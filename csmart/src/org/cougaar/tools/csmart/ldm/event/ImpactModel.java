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

import org.cougaar.tools.csmart.ldm.plugin.transducer.Society;

/**
 * A model of a <code>RealWorldEvent</code> and how it impacts a system.
 * The model exists solely to calculate the impact on the simulated
 * system, in terms of <code>InfrastructureEvent</code>s
 * <br>
 * The model is returned by a <code>RealWorldEvent</code>
 * and used in the <code>TransducerPlugIn</code>
 * to get a set of <code>InfrastructureEvent</code>s.<br>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see RealWorldEvent
 * @see InfrastructureEvent
 * @see org.cougaar.tools.csmart.plugin.TransducerPlugIn
 * @see Society
 */
public interface ImpactModel {
  /**
   * Calculate the impact on the given world of the Event modeled
   * The world is a list of Agents with their locations, etc.
   * The impact is specified as a set of InfrastructureEvents.<br>
   * This model sets all values on the InfrastructureEvents.
   * <br>
   * @param world a <code>Society</code> representing the simulated world
   * @param theIEF a <code>IEFactory</code> for creating <code>InfrastructureEvent</code>s
   * @return an <code>Iterator</code> of <code>InfrastructureEvent</code>s
   * @see InfrastructureEvent
   */
  public Iterator getImpact(Society world, IEFactory theIEF);
} // end of ImpactModel


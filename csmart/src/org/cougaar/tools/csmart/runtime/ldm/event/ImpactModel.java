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

import java.util.Iterator;

import org.cougaar.tools.csmart.runtime.ldm.plugin.transducer.Society;

/**
 * A model of a <code>RealWorldEvent</code> and how it impacts a system.
 * The model exists solely to calculate the impact on the simulated
 * system, in terms of <code>InfrastructureEvent</code>s
 * <br>
 * The model is returned by a <code>RealWorldEvent</code>
 * and used in the <code>TransducerPlugin</code>
 * to get a set of <code>InfrastructureEvent</code>s.<br>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see RealWorldEvent
 * @see InfrastructureEvent
 * @see org.cougaar.tools.csmart.runtime.plugin.TransducerPlugin
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
  Iterator getImpact(Society world, IEFactory theIEF);
} // end of ImpactModel


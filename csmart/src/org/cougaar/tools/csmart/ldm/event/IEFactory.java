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
import org.cougaar.domain.planning.ldm.Factory;

import org.cougaar.tools.csmart.ldm.CSMARTFactory;
import org.cougaar.tools.csmart.ldm.event.NewInfrastructureEvent;

/**
 * Special factory that can only create <code>InfrastructureEvent</code>s
 * for use by the <code>ImpactModel</code>s.
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see CSMARTFactory
 * @see InfrastructureEvent
 * @see ImpactModel
 */
public class IEFactory implements Factory {
  private CSMARTFactory theULF;
  public IEFactory (CSMARTFactory theULF) {
    this.theULF = theULF;
  }

  /**
   * Create a new InfrastructureEvent
   *
   * @return a <code>NewInfrastructureEvent</code>
   */
  public NewInfrastructureEvent newInfrastructureEvent() {
    return theULF.newInfrastructureEvent();
  }

  /**
   * Create a new InfrastructureEvent with the given values
   *
   * @param dest a <code>ClusterIdentifier</code> Agent to impact
   * @param type a <code>String</code> constant from <code>org.cougaar.tools.csmart.Constants.InfEventType</code>
   * @param duration a <code>long</code> impact duration
   * @param intensity a <code>double</code> magnitude from 0 to 1
   * @return a <code>NewInfrastructureEvent</code>
   */
  public NewInfrastructureEvent newInfrastructureEvent(ClusterIdentifier dest,
						       String type,
						       long duration,
						       double intensity) {
    return theULF.newInfrastructureEvent(dest,
					     type,
					     duration,
					     intensity);
  }
} // end of IEFactory

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

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

import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.core.cluster.ClusterIdentifier;

/**
 * Create a new <code>HappinessChangeEvent</code> to indicate the
 * impact on overall customer satisfaction of a Tasks' result.
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 */
public interface NewHappinessChangeEvent extends HappinessChangeEvent {
  public void setRegarding(PlanElement pe);

  public void setTime(long time);

  public void setCurrentHappiness(double hap);

  public void setTimeCompleted(long time);

  public void setRating(float rate);

  /**
   * Set the <code>String</code> name of the publisher of this <code>Event</code>.
   */
  public void setPublisher(String publisher);

  /**
   * Set the <code>ClusterIdentifier</code> "source" publisher of 
   * this <code>Event</code>.
   */
  public void setSource(ClusterIdentifier source);

}

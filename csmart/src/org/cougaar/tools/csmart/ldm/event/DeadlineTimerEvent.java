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

import java.io.Serializable;

import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.core.society.UniqueObject;
import org.cougaar.core.cluster.Publishable;
import org.cougaar.core.cluster.ClusterIdentifier;

/**
 * An object whose appearance indicates that a response was expected
 * on the given <code>PlanElement</code>.  It should appear at roughly
 * the time indicated.<br>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see PlanElement
 */
public interface DeadlineTimerEvent extends Serializable, UniqueObject, Publishable {
  // Need access to the thing for which you are expecting an answer
  PlanElement getRegarding();

  // Time at which this deadline timer goes off
  long getTime();

  /**
   * Get the <code>String</code> name of the publisher of this <code>Event</code>.
   */
  String getPublisher();

  /**
   * Get the <code>ClusterIdentifier</code> "source" publisher of 
   * this <code>Event</code>.
   */
  ClusterIdentifier getSource();


  // Publishable gives us boolean isPersistable(), for which for now we can say no
  // UniqueObject gives UID getUID() and void setUID(UID uid)

  // What about description?
  String getDescription();
}


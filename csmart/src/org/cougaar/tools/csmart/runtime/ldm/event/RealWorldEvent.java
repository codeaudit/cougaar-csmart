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

package org.cougaar.tools.csmart.runtime.ldm.event;

import java.io.Serializable;
import java.util.List;

import org.cougaar.core.blackboard.Publishable;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.planning.ldm.plan.Directive;

/**
 * Parent class for External world events with which to stress
 * a system.<br>
 * These Events have parameters for display purposes. They also <br>
 * have an <code>ImpactModel</code> to calculate the impact on the world.<br>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see ImpactModel
 * @see Directive
 */
public interface RealWorldEvent extends Directive, UniqueObject, Publishable, Serializable {
  /**
   * The type of Attack or Event occuring.  The value is a
   * constant from <code>org.cougaar.tools.csmart.Constants</code>
   *<br>
   * @return a <code>String</code> event type
   */
  String getType();

  /**
   * Each <code>RealWorldEvent<code> must return a model of its impact.<br>
   * This method does that.
   *
   * @return an <code>ImpactModel</code> of the events impact.
   */
  ImpactModel getModel();

  /**
   * Return a list of Name-Value pairs for use by the UI.
   * The Names are Strings and the Values are to-Stringable Objects.<br>
   *
   * @return a <code>List</code> of Name-Value pairs, using <code>org.cougaar.tools.csmart.util.ArgValue</code>
   */
  List getParameters();

  /**
   * Get the time in milliseconds at which this Event occurs.
   */
  long getTime();

  /**
   * Get the <code>String</code> name of the publisher of this Event.
   */
  String getPublisher();
} // End of RealWorldEvent.java

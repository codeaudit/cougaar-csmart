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

import java.io.Serializable;
import java.util.List;

import org.cougaar.core.cluster.Publishable;
import org.cougaar.core.society.UniqueObject;
import org.cougaar.domain.planning.ldm.plan.Directive;

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
  public String getType();

  /**
   * Each <code>RealWorldEvent<code> must return a model of its impact.<br>
   * This method does that.
   *
   * @return an <code>ImpactModel</code> of the events impact.
   */
  public ImpactModel getModel();

  /**
   * Return a list of Name-Value pairs for use by the UI.
   * The Names are Strings and the Values are to-Stringable Objects.<br>
   *
   * @return a <code>List</code> of Name-Value pairs, using <code>org.cougaar.tools.csmart.util.ArgValue</code>
   */
  public List getParameters();

  /**
   * Get the time in milliseconds at which this Event occurs.
   */
  public long getTime();

  /**
   * Get the <code>String</code> name of the publisher of this Event.
   */
  public String getPublisher();
} // End of RealWorldEvent.java

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
package org.cougaar.tools.csmart.society.abc;

import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyAlias;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.range.DoubleRange;
import org.cougaar.tools.csmart.core.property.range.LongRange;
import org.cougaar.tools.csmart.core.property.range.IntegerRange;

import java.io.*;
import java.util.Collections;

/** 
 * Defines a specific Task that is used in the ABCSociety Generation.   <br>
 * An ABC Society can have 1 or more tasks.  <br>
 * The defined task is used when creating a Task File.
 * <br>
 * @see ABCTaskFile
 */

public class ABCTask
  extends ConfigurableComponent
  implements Serializable 
{

  /** Properties associated with a Task **/
  public static final String PROP_TASKVERB = "Task Verb";
  public static final String PROP_TASKVERB_DFLT = "";

  public static final String PROP_TASKVERB_DESC = "Task Verb used through out the Society";

  public static final String PROP_WORLDSTATE= "World State";
  public static final String PROP_WORLDSTATE_DFLT = "PEACE";
  public static final String PROP_WORLDSTATE_DESC = "The state of the world for this task";

  public static final String PROP_VITAL = "Vitalness Factor";
  public static final Double PROP_VITAL_DFLT = new Double(0.5);
  public static final String PROP_VITAL_DESC = "How Vital this task is to overall happiness";

  public static final String PROP_DURATION = "Deadline";
  public static final Long PROP_DURATION_DFLT = new Long(12000);
  public static final String PROP_DURATION_DESC = "Time (milliseconds) when task should be completed by";

  public static final String PROP_RATE = "Interval";
  public static final Long PROP_RATE_DFLT = new Long(2500);
  public static final String PROP_RATE_DESC = "How often (milliseconds) a new task is generated";

  public static final String PROP_CHAOS = "Chaos Factor";
  public static final Integer PROP_CHAOS_DFLT = new Integer(0);
  public static final String PROP_CHAOS_DESC = "Amount of Chaos to apply to task rate";

  private Property propTaskVerb;
  private Property propWorldState;
  private Property propVital;
  private Property propDuration;
  private Property propRate;
  private Property propChaos;

  ABCTask() {
    this("Tasks");
  }

  ABCTask(String name) {
    super(name);
  }

  /**
   * Initializes all properties
   */
  public void initProperties() {
    // Need to init TaskVerb from Parent.
    propTaskVerb = addProperty(PROP_TASKVERB, PROP_TASKVERB_DFLT);
    propTaskVerb.setToolTip(PROP_TASKVERB_DESC);

    propWorldState = addProperty(PROP_WORLDSTATE, PROP_WORLDSTATE_DFLT);
    propWorldState.setToolTip(PROP_WORLDSTATE_DESC);
    // Hide World State since it is not being used.
    setPropertyVisible(propWorldState, false);

    propVital = addProperty(PROP_VITAL, PROP_VITAL_DFLT);
    propVital.setToolTip(PROP_VITAL_DESC);
    propVital.setAllowedValues(Collections.singleton(new DoubleRange(0.0, 1.0)));

    propDuration = addProperty(PROP_DURATION, PROP_DURATION_DFLT);
    propDuration.setToolTip(PROP_DURATION_DESC);
    propDuration.setAllowedValues(Collections.singleton(new LongRange(0, Integer.MAX_VALUE)));

    propRate = addProperty(PROP_RATE, PROP_RATE_DFLT);
    propRate.setToolTip(PROP_RATE_DESC);
    propRate.setAllowedValues(Collections.singleton(new LongRange(0, Integer.MAX_VALUE)));

    propChaos = addProperty(PROP_CHAOS, PROP_CHAOS_DFLT);
    propChaos.setToolTip(PROP_CHAOS_DESC);
    propChaos.setAllowedValues(Collections.singleton(new IntegerRange(0, Integer.MAX_VALUE)));
  }

  /**
   * Generates a configuration line containing all values required
   * for a Tasks file.  <br>
   * A task file line is in the form of:   <br>
   * <World State>, <Task Verb>, <Rate>, <Chaos>, <Vital>, <Duration>  <br>
   *
   * @return String Configuration Line
   */  
  public String getConfigLine() {
    return (String)propWorldState.getValue() + ", " +
      (String)propTaskVerb.getValue() + ", " +
      (String)((Long)propRate.getValue()).toString() + ", " +
      (String)((Integer)propChaos.getValue()).toString() + ", " +
      (String)((Double)propVital.getValue()).toString() + ", " +
      (String)((Long)propDuration.getValue()).toString();
  }
}



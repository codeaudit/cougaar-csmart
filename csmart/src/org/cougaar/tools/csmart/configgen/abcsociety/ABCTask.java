/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.configgen.abcsociety;

import org.cougaar.tools.csmart.ui.component.*;

import java.io.*;
import java.util.Collections;

/** 
 * Defines a specific Task that is used in the ABCSociety Generation.   <br>
 * An ABC Society can have 1 or more tasks.  <br>
 * The defined task is used when creating a Task File.
 * <br>
 * @see ABCTaskFile.java
 */

public class ABCTask
  extends ConfigurableComponent
  implements Serializable 
{

  /** Properties associated with a Task **/
  public static final String PROP_TASKTYPE = "Task Verb";
  public static final String PROP_TASKTYPE_DFLT = "Supply500MREs";
  public static final String PROP_TASKTYPE_DESC = "Task Verb used through out the Society";

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

  private Property propTaskType;
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
    // Need to init TaskType from Parent.
    propTaskType = addProperty(PROP_TASKTYPE, PROP_TASKTYPE_DFLT);
    propTaskType.setToolTip(PROP_TASKTYPE_DESC);

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
   * <World State>, <Task Type>, <Rate>, <Chaos>, <Vital>, <Duration>  <br>
   *
   * @return String Configuration Line
   */  
  public String getConfigLine() {
    return (String)propWorldState.getValue() + ", " +
      (String)propTaskType.getValue() + ", " +
      (String)((Integer)propRate.getValue()).toString() + ", " +
      (String)((Integer)propChaos.getValue()).toString() + ", " +
      (String)((Double)propVital.getValue()).toString() + ", " +
      (String)((Integer)propDuration.getValue()).toString();
  }
}



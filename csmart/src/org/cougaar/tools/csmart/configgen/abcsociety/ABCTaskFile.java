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
package org.cougaar.tools.csmart.configgen.abcsociety;

import org.cougaar.tools.csmart.ui.component.*;
import java.io.*;

/**
 * Define a Task File which consists of 1 or more tasks that will
 * be submitted by the customer.  <br><br>
 *
 * A Task file contains one or more lines of the format: <br><br>
 *
 * <WorldState>, <TaskVerb>, <Rate>, <Deviation>, <Vitality>, <Duration>
 * <br><br>
 * Each of these lines represents a child <code>ABCTask</code>component.
 *
 * @see ConfigurableComponent
 */

public class ABCTaskFile
  extends ConfigurableComponent
  implements Serializable 
{

  /** Name of the Task File **/
  public static final String PROP_TASKFILENAME = "Task File Name";
  public static final String PROP_TASKFILENAME_DESC = "Name of the Task File";

  public static final String PROP_TASKCOUNT = "Number of Tasks";
  public static final String PROP_TASKCOUNT_DESC = "Number of Tasks know by this agent";
  public static final Long   PROP_TASKCOUNT_DFLT = new Long(1);

  private Property propTaskCount;

  ABCTaskFile() {
    this("Task File");
  }

  ABCTaskFile(String name) {
    super(name);
  }

  /**
   * Initializes all Properties.
   */
  public void initProperties() {
    propTaskCount = addProperty(PROP_TASKCOUNT, PROP_TASKCOUNT_DFLT);
    propTaskCount.addPropertyListener(new ConfigurableComponentPropertyAdapter() {
	public void propertyValueChanged(PropertyEvent e) {
	  updateTaskCount((Long)e.getProperty().getValue());
	  }
      });
    propTaskCount.setToolTip(PROP_TASKCOUNT_DESC);
  }

  private void updateTaskCount(Long taskCount) {
    long count = taskCount.longValue();

    if(count == 0) {
      // Remove all Tasks.
      while(getChildCount() != 0) {
	removeChild(getChildCount() -1);
      }
    } else if( count > getChildCount() ) {
      long newRules = count - getChildCount();
      
      for(int i=0; i < newRules; i++) {
	ABCTask task = new ABCTask("NewTask" + i);
	task.initProperties();
	addChild(task);
      }
    } else if( count < getChildCount() ) {
      while(getChildCount() != count) {
	removeChild(getChildCount() -1 );
      }
    }
  }

  /**
   * Writes a Task File to the specified directory. <br>
   * Task file is of the format: <br>
   * <WorldState>, <TaskVerb>, <Rate>, <Chaos>, <Vitality>, <Duration>
   * <br><br>
   * @param File directory to place taskfile in.
   * @throws IOException if the file cannot be created.
   */
  public void writeTaskFile(File configDir) throws IOException {

    File taskFile = new File(configDir, (String)getProperty(PROP_TASKFILENAME).getValue());
    PrintWriter writer = new PrintWriter(new FileWriter(taskFile));
    try {
      writer.println("# <WorldState>, <TaskVerb>, <Rate>, <Chaos>, <Vitality>, <Duration>");
      for(int i=0; i < getChildCount(); i++) {
	writer.println(((ABCTask)getChild(i)).getConfigLine());
      }
    } 
    finally {
      writer.close();
    }
  }
}

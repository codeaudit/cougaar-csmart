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

/**
 * Define a Task File which consists of 1 or more tasks that will
 * be submitted by the customer.  <br><br>
 *
 * A Task file contains one or more lines of the format: <br><br>
 *
 * <WorldState>, <TaskType>, <Rate>, <Deviation>, <Vitality>, <Duration>
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
  }

  /**
   * Writes a Task File to the specified directory. <br>
   * Task file is of the format: <br>
   * <WorldState>, <TaskType>, <Rate>, <Chaos>, <Vitality>, <Duration>
   * <br><br>
   * @param File directory to place taskfile in.
   * @throws IOException if the file cannot be created.
   */
  public void writeTaskFile(File configDir) throws IOException {

    File taskFile = new File(configDir, (String)getProperty(PROP_TASKFILENAME).getValue());
    PrintWriter writer = new PrintWriter(new FileWriter(taskFile));
    try {
      writer.println("# <WorldState>, <TaskType>, <Rate>, <Chaos>, <Vitality>, <Duration>");
      for(int i=0; i < getChildCount(); i++) {
	writer.println(((ABCTask)getChild(i)).getConfigLine());
      }
    } 
    finally {
      writer.close();
    }
  }
}

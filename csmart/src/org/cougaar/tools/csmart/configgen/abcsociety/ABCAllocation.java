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
import java.lang.StringBuffer;
import java.util.Collections;

/**
 * Defines an Allocation File.  An allocation file contains
 * a <i>configuration</i> line and one or more <i>rules</i>.
 * <br>
 * The configuration line contains:    
 * <ul>
 * <li> Success:  Defines a successful allocation
 * <li> Response Time: Amount of time it takes allocator to respond to a task
 * <li> Publish Time: Amount of time it takes to publish an allocation
 * <li> Transport Time: Amount of time it takes to transport an Allocation into a task on Remote Agent
 * <li> Execute Time: Minimum time to try to execute an Allocation.
 * </ul>
 * <br>
 * The rule line contains:
 * <ul>
 * <li> Task: Task to allocate
 * <li> Role: list of roles for the task.
 * </ul>
 *
 * @see ConfigurableComponent
 */

public class ABCAllocation
  extends ConfigurableComponent
  implements Serializable 
{

  /** Allocation Success Property Definitions **/
  public static final String PROP_ALLOCSUCCESS = "Successful Allocation";
  public static final Float  PROP_ALLOCSUCCESS_DFLT = new Float(0.5);
  public static final String PROP_ALLOCSUCCESS_DESC = "Defines a successful Allocation";
  
  /** Response Time Property Definitions **/
  public static final String PROP_RESPONSETIME= "Response Time";
  public static final Long   PROP_RESPONSETIME_DFLT = new Long(50);
  public static final String PROP_RESPONSETIME_DESC = "Amount of time (milliseconds) it takes the allocator to respond to a task";

  /** Publish Time Property Definitions **/
  public static final String PROP_PUBALLOC = "Publish Time";
  public static final Long   PROP_PUBALLOC_DFLT = new Long(60);
  public static final String PROP_PUBALLOC_DESC = "Amount of time (milliseconds) it takes to publish an allocation";

  /** Transport Time Property Definitions **/
  public static final String PROP_TRANSPORT = "Transport Time";
  public static final Long   PROP_TRANSPORT_DFLT = new Long(150);
  public static final String PROP_TRANSPORT_DESC = "Amount of time (milliseconds) it takes to transport an Allocation into a task on a Remote Agent";

  /** Execute Time Property Definitions **/
  public static final String PROP_EXECUTE = "Execute Time";
  public static final Long   PROP_EXECUTE_DFLT = new Long(1100);
  public static final String PROP_EXECUTE_DESC = "The minimum time, in milliseconds, to try to execute an Allocation";

  /** Filename Property Definitions **/
  public static final String PROP_ALLOCFILENAME = "Allocation File Name";
  public static final String PROP_ALLOCFILENAME_DESC = "Name of the Allocation File";

  private Property propSuccess;
  private Property propResponse;
  private Property propPublish;
  private Property propTransport;
  private Property propExecute;
  private Property propFileName;

  ABCAllocation() {
    this("Allocations");
  }

  ABCAllocation(String name) {
    super(name);
  }

  /**
   * Initializes all the properties
   */
  public void initProperties() {
    propSuccess = addProperty(PROP_ALLOCSUCCESS, PROP_ALLOCSUCCESS_DFLT);
    propSuccess.setToolTip(PROP_ALLOCSUCCESS_DESC);
    propSuccess.setAllowedValues(Collections.singleton(new FloatRange(0.0F, 1.0F)));

    propResponse = addProperty(PROP_RESPONSETIME, PROP_RESPONSETIME_DFLT);
    propResponse.setToolTip(PROP_RESPONSETIME_DESC);
    propResponse.setAllowedValues(Collections.singleton(new LongRange(0, Long.MAX_VALUE)));

    propPublish = addProperty(PROP_PUBALLOC, PROP_PUBALLOC_DFLT);
    propPublish.setToolTip(PROP_PUBALLOC_DESC);
    propPublish.setAllowedValues(Collections.singleton(new LongRange(0, Long.MAX_VALUE)));

    propTransport = addProperty(PROP_TRANSPORT, PROP_TRANSPORT_DFLT);
    propTransport.setToolTip(PROP_TRANSPORT_DESC);
    propTransport.setAllowedValues(Collections.singleton(new LongRange(0, Long.MAX_VALUE)));

    propExecute = addProperty(PROP_EXECUTE, PROP_EXECUTE_DFLT);
    propExecute.setToolTip(PROP_EXECUTE_DESC);
    propExecute.setAllowedValues(Collections.singleton(new LongRange(0, Long.MAX_VALUE)));

  }

  /**
   * Generates a configuration line containing all values required
   * for an Allocation file.  <br>
   * An allocation file line is in the form of:   <br>
   * [config, <fSuccess>, <tResp> (, <tAlloc>, <tTrans>, <tTry>)]
   *
   * @return String Configuration Line
   */  
  private String getConfigLine() {
    StringBuffer sb = new StringBuffer(35);
    
    sb.append("config, ");
    sb.append((Float)propSuccess.getValue());
    sb.append(", ");
    sb.append((Long)propResponse.getValue());
    sb.append(", ");
    sb.append((Long)propPublish.getValue());
    sb.append(", ");
    sb.append((Long)propTransport.getValue());
    sb.append(", ");
    sb.append((Long)propExecute.getValue());

    return sb.toString();
  }

  /**
   * Writes an Allocation File to the specified directory. <br>
   * Allocation files are of the format:
   * [config, <fSuccess>, <tResp> (, <tAlloc>, <tTrans>, <tTry>)]
   * [[rule, ] <task>, <role> (, <role>)*]
   * <br><br>
   * @param File directory to place taskfile in.
   * @throws IOException if the file cannot be created.
   */
  public void writeAllocationFile(File configDir) throws IOException {

    File taskFile = new File(configDir, (String)getProperty(PROP_ALLOCFILENAME).getValue());
    PrintWriter writer = new PrintWriter(new FileWriter(taskFile));

    try {
      writer.println("# [config, <fSuccess>, <tResp> (, <tAlloc>, <tTrans>, <tTry>)]");
      writer.println("# [[rule, ] <task>, <role> (, <role>)*]");
      writer.println("# <more \"rule\" lines as necessary>");
      writer.println(getConfigLine());
      for(int i=0; i < getChildCount(); i++) {
	ABCAllocationRule rule = (ABCAllocationRule)getChild(i);
	writer.println(rule.getConfigLine());
      }
    } 
    finally {
      writer.close();
    }
  }
}

/* 
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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

import java.io.Serializable;
import java.lang.StringBuffer;
import java.util.Collections;

import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.range.FloatRange;
import org.cougaar.tools.csmart.core.property.range.LongRange;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.core.property.PropertyEvent;

import org.cougaar.tools.csmart.core.cdata.GenericLeafComponentData;
import org.cougaar.tools.csmart.core.cdata.LeafComponentData;

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

  public static final String PROP_NUMBEROFRULES = "Number of Allocation Rules";
  public static final String PROP_NUMEROFRULES_DESC = "Total Number of allocation rules for this agent";
  public static final Long   PROP_NUMBEROFRULES_DFLT = new Long(1);

  private Property propSuccess;
  private Property propResponse;
  private Property propPublish;
  private Property propTransport;
  private Property propExecute;
  private Property propFileName;
  private Property propNumberOfRules;

  /**
   * Creates a new <code>ABCAllocation</code> instance.
   *
   */
  ABCAllocation() {
    this("Allocations");
  }

  /**
   * Creates a new <code>ABCAllocation</code> instance.
   *
   * @param name Name of the Allocation
   */
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

    propNumberOfRules = addProperty(PROP_NUMBEROFRULES, PROP_NUMBEROFRULES_DFLT,
				    new ConfigurableComponentPropertyAdapter() {
				      public void propertyValueChanged(PropertyEvent e) {
					updateAllocationRuleCount((Long)e.getProperty().getValue());
				      }
				    });
    propNumberOfRules.setToolTip(PROP_NUMBEROFRULES);
    propNumberOfRules.setAllowedValues(Collections.singleton(new LongRange(0, Long.MAX_VALUE)));

  }

  private void updateAllocationRuleCount(Long newCount) {
    long count = newCount.longValue();

    if(count == 0) {
      // Remove all allocation rules.
      while(getChildCount() != 0) {
	removeChild(getChildCount() -1);
      }
    } else if( count > getChildCount() ) {
      long newRules = count - getChildCount();
      
      for(int i=0; i < newRules; i++) {
	ABCAllocationRule rule = new ABCAllocationRule("NewRule" + i);
	rule.initProperties();
	addChild(rule);
      }
    } else if( count < getChildCount() ) {
      while(getChildCount() != count) {
	removeChild(getChildCount() -1 );
      }
    }
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
   * Creates a Allocation file associated with this Allocation.
   * Allocation files are of the format:
   * [config, <fSuccess>, <tResp> (, <tAlloc>, <tTrans>, <tTry>)]
   * [[rule, ] <task>, <role> (, <role>)*]
   * <br><br>
   * @return a <code>LeafComponentData</code> value
   */
  public LeafComponentData createAllocationLeaf() {
    GenericLeafComponentData lcd = new GenericLeafComponentData();
    StringBuffer sb = new StringBuffer();
    
    sb.append("# [config, <fSuccess>, <tResp> (, <tAlloc>, <tTrans>, <tTry>)]\n");
    sb.append("# [[rule, ] <task>, <role> (, <role>)*]\n");
    sb.append("# <more \"rule\" lines as necessary>\n");
    sb.append(getConfigLine() + "\n");
    for(int i=0; i < getChildCount(); i++) {
      ABCAllocationRule rule = (ABCAllocationRule)getChild(i);
      sb.append(rule.getConfigLine() + "\n");
    }

    lcd.setName((String)getProperty(PROP_ALLOCFILENAME).getValue());
    lcd.setType(LeafComponentData.FILE);
    lcd.setValue(sb);
    
    return (LeafComponentData)lcd;
  }

}

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

import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;

import java.io.Serializable;
import java.lang.StringBuffer;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

public class ABCAllocationRule
  extends ConfigurableComponent
  implements Serializable 
{

  /** Defines a Tasktype Property **/
  public static final String PROP_TASKVERB = ABCTask.PROP_TASKVERB;
  public static final String PROP_TASKVERB_DFLT = ABCTask.PROP_TASKVERB_DFLT;
  public static final String PROP_TASKVERB_DESC = ABCTask.PROP_TASKVERB_DESC;

  /** Defines a Role Property **/
  public static final String PROP_ROLES = ABCLocalAsset.PROP_ROLES;
  public static final String[] PROP_ROLES_DFLT = ABCLocalAsset.PROP_ROLES_DFLT;
  public static final String PROP_ROLES_DESC = ABCLocalAsset.PROP_ROLES_DESC;

  private Property propTaskVerb;
  private Property propRoles;

  /**
   * Creates a new <code>ABCAllocationRule</code> instance.
   *
   */
  ABCAllocationRule() {
    this("Alloc");
  }

  /**
   * Creates a new <code>ABCAllocationRule</code> instance.
   *
   * @param name Name of the Allocation Rule Component
   */
  ABCAllocationRule(String name) {
    super("Rule" + "-" + name);
  }

  /**
   * Initializes all Properties
   */
  public void initProperties() {
    propTaskVerb = addProperty(PROP_TASKVERB, PROP_TASKVERB_DFLT);
    propTaskVerb.setToolTip(PROP_TASKVERB_DESC);

    propRoles = addProperty(PROP_ROLES, PROP_ROLES_DFLT);
    propRoles.setToolTip(PROP_ROLES_DESC);
  }

  /**
   * Generates a configuration line containing all values required
   * for am Allocation rule.  <br>
   * An allocation rule is in the form of:   <br>
   * rule, <Task Type>, <role>*
   *
   * @return String Configuration Line
   */  
  public String getConfigLine() {
    StringBuffer sb = new StringBuffer(40);

    sb.append("rule, ");
    sb.append((String)propTaskVerb.getValue());

    String[] roles = (String[])propRoles.getValue();
    for(int i=0; i < roles.length; i++) {
      sb.append(", ");
      sb.append(roles[i]);
    }

    return sb.toString();
  }
}

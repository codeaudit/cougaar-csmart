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
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

public class ABCAllocationRule
  extends ConfigurableComponent
  implements Serializable 
{

  /** Defines a Tasktype Property **/
  public static final String PROP_TASKVERB = ABCTask.PROP_TASKVERB;
  public static final String[] PROP_TASKVERB_DFLT = ABCTask.PROP_TASKVERB_DFLT;
  public static final String PROP_TASKVERB_DESC = ABCTask.PROP_TASKVERB_DESC;

  /** Defines a Role Property **/
  public static final String PROP_ROLES = ABCLocalAsset.PROP_ROLES;
  public static final String[] PROP_ROLES_DFLT = ABCLocalAsset.PROP_ROLES_DFLT;
  public static final String PROP_ROLES_DESC = ABCLocalAsset.PROP_ROLES_DESC;

  private Property propTaskVerb;
  private Property propRoles;

  ABCAllocationRule() {
    super("rule");
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

/**
 * <copyright>
 *  Copyright 2002-2003 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.core.cdata;

import org.cougaar.tools.csmart.util.DebugUtils;

/**
 * CDataDebugUtils.java
 *
 *
 * Created: Wed Jun  5 11:55:51 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class CDataDebugUtils extends DebugUtils {
  public CDataDebugUtils (){
  }

  public static final String createDataDump(ComponentData data) {
    StringBuffer buff = new StringBuffer();
    buff.append(data.getType() + " with name " + data.getName() + "\n");
    buff.append(dumpChildren(data, 2));
    return buff.substring(0);
  }

  private static String dumpChildren(ComponentData data, int indent) {
    ComponentData[] children = data.getChildren();
    StringBuffer buf = new StringBuffer();
    for(int i=0; i < children.length; i++) {
      ComponentData child = children[i];
      for(int j=0; j < indent; j++) {
        buf.append("  ");
      }
      buf.append("Child of type: ");
      buf.append(child.getType());
      buf.append(" with name: ");
      buf.append(child.getName());
      buf.append(" and parent: ");
      buf.append(child.getParent().getName());
      buf.append("\n");
      if(child.getChildren().length > 0) {
        buf.append(dumpChildren(child, indent+2));
      }
    }
    return buf.substring(0);
  }
  
}// CDataDebugUtils

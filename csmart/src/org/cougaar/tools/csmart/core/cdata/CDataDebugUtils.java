/**
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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

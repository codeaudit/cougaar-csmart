/**
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
 *  </copyright>
 */
package org.cougaar.tools.csmart.util;

import org.cougaar.util.log.Logger;

/**
 * DebugUtils.java
 *
 *
 * Created: Tue May  7 14:46:47 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class DebugUtils {

  /**
   * Dumps a nice formatted stack trace to the 
   * logger.  
   *
   * Simple to use: DebugUtils.debugStackDump(log);
   *
   * @param log - Handle to your logger instance
   */
  public static void debugStackDump(Logger log) {
    if(log.isDebugEnabled()) {
      log.debug(createDump());
    }
  }

  private static String createDump() {
    StackTraceElement[] ste = new Throwable().getStackTrace();
    StringBuffer msg = new StringBuffer();
    msg.append("Stack Trace: \n");
    for(int i=2; i < ste.length; i++) {
      msg.append("  " + ste[i].getClassName());
      msg.append(".");
      msg.append(ste[i].getMethodName());
        msg.append("(");
        msg.append(ste[i].getLineNumber());
        msg.append(")\n");
    }

    return msg.substring(0);
  }

}// DebugUtils

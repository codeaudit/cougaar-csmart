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
package org.cougaar.tools.csmart.util;

import org.cougaar.util.log.Logger;

/**
 * DebugUtils.java
 *
 *
 * Created: Tue May  7 14:46:47 2002
 *
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

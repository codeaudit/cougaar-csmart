/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.util;

import org.cougaar.util.*;

/**
 * Interface to describe a logging mechanism. Each log entry contains
 * a <i>Class Name</i>, <i>message</i> and a <i>severity</i> level.  
 * <br><br>
 * The class name should be passed containing the entire package:<br>
 * Example: <code>org.cougaar.tools.csmart.util</code> <br>
 * This can be easily obtained with: <code>this.getClass().getName()</code><br>
 * To determine filtering, a properties file: <code>debug.properties</code> which
 * is located in the <code>data</code> directory is loaded at start.  The properties
 * file consists of key/value pairs denoting the package and/or class name and a boolean indicating
 * if the package should be logged.<br>
 * Example properties file entry: <code>org.cougaar.tools.csmart.util=true</code> -- Indicates log everything
 * from this package.<br>
 * Another entry can look like: <code>org.cougaar.tools.csmart.util.LogStreamImpl=true</code><br>
 * If the properties file cannot be found, all packages are logged.<br><br>
 * Actual Logging is controlled by the severity level.  The default level
 * should be set rather low to prevent the log from getting huge.
 * <br><br>
 * The severity order level from minimal logging to extreme logging is:<br>
 * &nbsp;&nbsp;SEVERE<br>
 * &nbsp;&nbsp;PROBLEM<br>
 * &nbsp;&nbsp;DEBUG<br>
 * &nbsp;&nbsp;VERBOSE<br>
 * &nbsp;&nbsp;VERY_VERBOSE<br> 
 * <br>
 * Log Message strings should be in the format:<br>
 * &nbsp;<i>Method Name</i>:<i>Class Name</i>:<i>Debug Message</i><br>
 * This format will make it easier to filter for specific log messages during debugging.
 * <br><br>
 * An example usage:
 * <br><br>
 * <code>
 * if( ls.isApplicable(ls.DEBUG) ) {   <br>
 * &nbsp;&nbsp;String msg = "execute:MyPlugin:Debug statement message"; <br>
 * &nbsp;&nbsp;ls.log(this.getClass().getName(), ls.DEBUG, msg);  <br>
 * }
 * </code>
 *
 */
 
public interface LogStream extends GenericStateModel {

  /** All Possible Debug Severity Levels **/
 
  /**	Indicates a severe problem; Usually program died **/
  public static final int SEVERE       = 0;
 
  /** Indicates a problematic, but not fatal error occurred **/
  public static final int PROBLEM      = 1;
 
  /** Indicates a level one debug message, useful but not verbose **/
  public static final int DEBUG        = 2;
 
  /** Indicates a level two debug, more verbose than a debug entry **/
  public static final int VERBOSE      = 3;
 
  /** Indicates a level three debug, very verbose.  
     Used for extreme debugging 
  */
  public static final int VERY_VERBOSE = 4;

 
  /**
   *
   * Determines if the specified log level is an applicable log level.
   *
   * @param severity The severity being checked for applicability  
   * @return Boolean indicating applicability of the specified severity level
   *
   */
  boolean isApplicable(int severity);
 
 /**
  *
  * Logs a message at the specified severity level.
  *
  * @param severity The severity level of the log entry
  * @param message The message content of the log entry
  *
  * deprecated Relpaced by (@link #log(String, int, String)) which allows package filtering.
  * 
  */
  
  void log(int severity, String message);

 /**
  *
  * Logs a message at the specified severity level.  This log
  * method specifies a class name to allow turning on and off
  * packages and classes during debugging.  The <code>className</code> must specify the
  * entire package.  Use: <code>getClass().getName()</code>
  *
  * Example Usage: <code>log(this.getClass().getName(), .... </code>
  *
  * @param className The name of the calling class.
  * @param severity The severity level of the log entry
  * @param message The message content of the log entry
  */
  
  void log(String className, int severity, String message);

 /**
  *
  * Logs a message at the specified severity level.  This log
  * method specifies a class name to allow turning on and off
  * packages and classes during debugging.
  *
  * Example Usage: <code>log(this, .... </code>
  *
  * @param objRef Reference to the calling object (this)
  * @param severity The severity level of the log entry
  * @param message The message content of the log entry
  */
  
  void log(Object objRef, int severity, String message);
 
  /**
   *
   * Flushes any remaining data in the log buffer.
   *
   */
  void flush();
}

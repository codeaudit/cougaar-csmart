/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.organization;

import java.util.StringTokenizer;

import org.cougaar.util.log.Logger;

/**
 * String tokenizer that returns empty strings between successive delimiters.
 */
class CSVStringTokenizer extends StringTokenizer {
  private transient Logger log;
  private String s;
  private String delim;
  
  public CSVStringTokenizer(String s, String delim) {
    super(s, delim, true);
    this.s = s;
    this.delim = delim;
    // create logger
    log = LoggerSupport.createLogger(this.getClass().getName());
  }
  
  public String nextToken() {
    try {
      String tmp = super.nextToken();
      if (tmp.equals(delim))
        return "";
      else {
        // reads following delimiter if not at end of string
        if (hasMoreTokens())
          super.nextToken();
        return tmp;
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Error reading CSV file", e);
      }
      return "";
    }
  }
}

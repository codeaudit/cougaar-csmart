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

package org.cougaar.tools.csmart.util.parser;

import java.io.*;
import java.util.*;

import org.cougaar.util.ConfigFinder;

/**
 * GenericParser is an abstract class that implements the
 * <code>Parser</code> interface.
 */
public abstract class GenericParser implements Parser {

  protected String commentChar = "#";
  protected String separatorChar = ",";
  protected BufferedReader in;

  /**
   * The Default is: '#'
   * @see Parser
   */
  public void setCommentChar(String chr) {
     this.commentChar = chr;
  }

  /**
   * The Default is: ','
   * @see Parser
   */
  public void setSeparatorChar(String chr) {
     this.separatorChar = chr;
  }

  /**
   * @see Parser
   */         
  public void parse() throws IOException {
  }

  /**
   * @see Parser
   */         
  public void load(String filename) throws IOException {
    InputStream ins = ConfigFinder.getInstance().open(filename);

    in = new BufferedReader( new InputStreamReader(ins));     
  }
	
} // End of GenericParser.java

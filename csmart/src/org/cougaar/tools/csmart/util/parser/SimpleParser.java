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

package org.cougaar.tools.csmart.util.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * A Simple Parser that parses comma delimited files.
 * The parser has no knowledge of the contents of the file.
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 */ 
public class SimpleParser extends GenericParser {
  
  private ArrayList list = null;

  public SimpleParser() {
    list = new ArrayList(25);
  }

  /**
   * @see Parser
   */         
  public void parse() throws IOException {
    while( in.ready() ) {
      String line = in.readLine();
      if( line != null && line.trim().length() != 0 &&
	  !line.startsWith(commentChar)) {
	parseLine(line);
      }
    }
    in.close();
  }


  /**
   * Parses out all the values for a  line and
   * inserts all entries into a new array.
   * <br>
   * @param line to be parsed into a new task.
   *
   */
  protected void parseLine(String line) {
    StringTokenizer st = new StringTokenizer(line, separatorChar);
    ArrayList entry = new ArrayList(25);

    while(st.hasMoreTokens()) {
      entry.add(st.nextToken().trim());
    }
    list.add(entry);
  }
  
  /**
   * Returns all parsed elements.
   * <br>
   * @return Enumeration of all elements
   */
  public ArrayList getList() {
     return this.list;     
  }
  
} // SimpleParser

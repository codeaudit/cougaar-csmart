/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.util.parser;

import java.io.*;
import java.util.*;

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

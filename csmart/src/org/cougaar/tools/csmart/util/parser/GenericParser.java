/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000 BBNT Solutions LLC
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
    ConfigFinder cf = new ConfigFinder();

    cf = ConfigFinder.getInstance();

    InputStream ins = ConfigFinder.getInstance().open(filename);

    in = new BufferedReader( new InputStreamReader(ins));     
  }
	
} // End of GenericParser.java

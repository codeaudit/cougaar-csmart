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

import java.util.*;
import java.io.*;

/**
 * Parser    <br><br>
 *
 * An interface to define a Properties file parser.
 */
interface Parser {
	
  /**
   * parse     <br><br>
   *
   * Actual parse method that parses the file.
   * <br>
   * @throws IOException
   */
	public void parse() throws IOException;

  /**
   * setCommentChar      <br><br>
   *
   * Sets the character that represents a comment
   * in the file.
   * <br>
   * @param chr Comment start character.
   */
  
	public void setCommentChar(String chr);

  /**
   * setSeparatorChar      <br><br>
   *
   * Sets the character that is used to separate fields
   * in the file.
   * <br>
   * @param chr Separator character.
   */
  public void setSeparatorChar(String chr);
  
  /**
   * load        <br><br>
   *
   * Loads in a file to be parsed.
   * <br>
   * @param filename The name of the file to load
   * @throws IOException
   */
   
  public void load(String filename) throws IOException;
}

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

import java.io.IOException;

/**
 * Parser    <br><br>
 *
 * An interface to define a Properties file parser.
 */
public interface Parser {
	
  /**
   * parse     <br><br>
   *
   * Actual parse method that parses the file.
   * <br>
   * @throws IOException
   */
  void parse() throws IOException;

  /**
   * setCommentChar      <br><br>
   *
   * Sets the character that represents a comment
   * in the file.
   * <br>
   * @param chr Comment start character.
   */
  void setCommentChar(String chr);

  /**
   * setSeparatorChar      <br><br>
   *
   * Sets the character that is used to separate fields
   * in the file.
   * <br>
   * @param chr Separator character.
   */
  void setSeparatorChar(String chr);
  
  /**
   * load        <br><br>
   *
   * Loads in a file to be parsed.
   * <br>
   * @param filename The name of the file to load
   * @throws IOException
   */ 
  void load(String filename) throws IOException;
}

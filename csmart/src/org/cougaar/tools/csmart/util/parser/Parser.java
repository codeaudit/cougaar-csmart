/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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

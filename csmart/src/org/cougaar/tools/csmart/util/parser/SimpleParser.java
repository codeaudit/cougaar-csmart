/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

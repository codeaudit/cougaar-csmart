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

import org.cougaar.util.ConfigFinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    InputStream ins = ConfigFinder.getInstance("csmart").open(filename);

    in = new BufferedReader( new InputStreamReader(ins));     
  }
	
} // End of GenericParser.java

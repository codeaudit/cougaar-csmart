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

package org.cougaar.tools.csmart.ui.util;

import java.util.ArrayList;

public class ServletResult {
  ArrayList responses;
  boolean limitExceeded;

  /**
   * Results from a servlet.
   */

  public ServletResult() {
    responses = new ArrayList();
    limitExceeded = false;
  }

  /**
   * Set limit exceeded flag; this flag is true if the servlet had
   * more objects to send, but the user limited the number of
   * objects it wished to receive.  The default is false.
   * @param limitExceeded whether or not the limit was exceeded
   */

  public void setLimitExceeded(boolean limitExceeded) {
    this.limitExceeded = limitExceeded;
  }

  /**
   * Returns true if the servlet had more objects to send than the client
   * was willing to receive.
   * @return true if limit exceeded, else false
   */

  public boolean isLimitExceeded() {
    return limitExceeded;
  }

//    /**
//     * Add the collection received from an agent; the collection may be null.
//     * @param c collection of objects received from an agent
//     */
//    public void addCollection(Collection c) {
//      results.add(c);
//    }

//    /**
//     * Return the collections received from all agents;
//     * there is one collection for each agent contacted;
//     * the collection may be null.
//     * @return array list of Collection
//     */
//    public ArrayList getCollections() {
//      return results;
//    }
   

  public void addServletResponse(ServletResponse response) {
    responses.add(response);
  }

  public ServletResponse getServletResponse(int index) {
    if (index < 0 || index >= responses.size())
      return null;
    return (ServletResponse)responses.get(index);
  }

  public int getNumberOfResponses() {
    return responses.size();
  }

}


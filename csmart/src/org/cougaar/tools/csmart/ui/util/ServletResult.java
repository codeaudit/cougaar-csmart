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


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

package org.cougaar.tools.csmart.core.cdata;

import java.util.ArrayList;
import java.util.Iterator;
import java.text.ParseException;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Data structure used to store Community Time Phased data.
 * Contains a list of community names and start and stop times
 * associated with each commuity.  Allows multiple communities
 * to be stored with the same start and stop time.
 */
public class CommunityTimePhasedData extends TimePhasedData {
  private ArrayList communities;
  private transient Logger log;

  /** Default Constructor **/
  public CommunityTimePhasedData() {
    super();
    createLogger();
    communities = new ArrayList();
    try {
      setStartTime("");
    } catch (ParseException pe) {
      if(log.isDebugEnabled()) {
        log.error("Caught an exception setting startTime");
      }
    }
    try {
      setStopTime("");
    } catch (ParseException pe) {
      if(log.isDebugEnabled()) {
        log.error("Caught an exception setting stopTime");
      }
    }
  }
  
  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Sets all time-phased communities
   *
   * @param String[] array of community names.
   */
  public void setCommunities(String[] communities) {
    for(int i=0; i < communities.length; i++) {
      this.communities.add(communities[i]);
    }
  }

  /**
   * Gets all time-phased communities.
   *
   * @return iterator of all communities
   */
  public Iterator getCommunityIterator() {
    return communities.iterator();
  }

  /**
   * Gets all time-phased communities.
   *
   * @return array all communities
   */
  public String[] getCommunities() {
    return (String[])communities.toArray(new String[communities.size()]);
  }

  /**
   * Adds a single community to the list of
   * time-phased communities.
   * @param String community name.
   */
  public void addCommunity(String community) {
    communities.add(community);
  }

  /**
   * Sets a single community at a specific index.
   * 
   * @param int index to replace community at.
   * @param String community name.
   */ 
  public void setCommunity(int index, String community) 
    throws IndexOutOfBoundsException {
    communities.set(index, community);
  }

  /**
   * Returns the Community at the given index.
   *
   * @param index Location to obtain community from.
   */
  public String getCommunity(int index) {
    return (String)communities.get(index);
  }

  /**
   * Number of time-phased communities.
   * 
   * @return community count.
   */
  public int size() {
    return communities.size();
  }

}

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

package org.cougaar.tools.csmart.core.cdata;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

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
      if(log.isErrorEnabled()) {
        log.error("Caught an exception setting startTime");
      }
    }
    try {
      setStopTime("");
    } catch (ParseException pe) {
      if(log.isErrorEnabled()) {
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
   * @param communities array of community names.
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
   * @param community name.
   */
  public void addCommunity(String community) {
    communities.add(community);
  }

  /**
   * Sets a single community at a specific index.
   * 
   * @param index to replace community at.
   * @param community name.
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

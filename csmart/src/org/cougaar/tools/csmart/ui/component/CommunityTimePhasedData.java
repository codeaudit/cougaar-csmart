/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.component;

import java.util.*;

/**
 * Data structure used to store Community Time Phased data.
 * Contains a list of community names and start and stop times
 * associated with each commuity.  Allows multiple communities
 * to be stored with the same start and stop time.
 */
public class CommunityTimePhasedData extends TimePhasedData {
  private ArrayList communities;

  /** Default Constructor **/
  public CommunityTimePhasedData() {
    super();
    communities = new ArrayList();
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
    return (String[])communities.toArray(new String[0]);
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
   * Adds a single community at a specific index.
   * 
   * @param int index to add community at.
   * @param String community name.
   */ 
  public void addCommunity(int index, String community) 
    throws IndexOutOfBoundsException {
    communities.add(index, community);
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

/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.configgen.community;

import java.util.List;
import java.util.Map;

import org.cougaar.tools.csmart.util.LatLonPoint;

/**
 * A Community based on a CommunityConfig.
 */
public abstract class Community {

  private CommunityConfig cc;

  /** initialized only once. */
  public void setCommunityConfig(CommunityConfig cc) {
    if (this.cc != null) {
      throw new IllegalArgumentException(
          "CommunityConfig already set");
    }
    this.cc = cc;
  }

  //
  // configuration based (from CommunityConfig):
  //

  public String getName() {
    return cc.getName();
  }

  public long getStartMillis() {
    return cc.getStartMillis();
  }

  public long getStopMillis() {
    return cc.getStopMillis();
  }

  public long getSniffInterval() {
    return cc.getSniffInterval();
  }

  public long getDemand() {
    return cc.getDemand();
  }

  public long getProduction() {
    return cc.getProduction();
  }

  public List getSupportedCommunityNames() {
    return cc.getSupportedCommunityNames();
  }

  public float getLatitude() {
    return cc.getLatitude();
  }

  public float getLongitude() {
    return cc.getLongitude();
  }

  public LatLonPoint getLatLonPoint() {
    return cc.getLatLonPoint();
  }

  //
  // template based:
  //

  public abstract int getNumberOfAgents();

  public abstract int getRepresentativeAgentIndex();


  public abstract String getNameForAgent(int i);

  public abstract long getDistanceForAgent(int i);

  public abstract float getDirectionForAgent(int i);

  public abstract List getSupportedAgentNamesForAgent(int i);

  public abstract Object getRolesForAgent(int i);
  
  public abstract long getFactorForAgent(int i);

  public abstract List getLocalAssetsForAgent(int i);

  public abstract List getCustomerTasksForAgent(int i);

  public abstract Map getAllocationTableForAgent(int i);

} // Community

/*
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cougaar.tools.csmart.util.LatLonPoint;
import org.cougaar.tools.csmart.util.EarthConstants;

import org.cougaar.tools.csmart.configgen.Agent;

/**
 * Simple Agent implementation based on a Community.
 */
public class CommunityBasedAgentImpl implements Agent  {
  
  private final Community comm;
  private final int i;

  // mutable List of supported agents names
  private List suppAgents;

  // cache some values
  private final String name;
  private final float lat;
  private final float lon;

  public CommunityBasedAgentImpl(Community comm, int agentNum) {
    this.comm = comm;
    this.i = agentNum;

    this.name = comm.getName() + "-" + comm.getNameForAgent(i);

    List l = comm.getSupportedAgentNamesForAgent(i);
    int n = ((l != null) ? l.size() : 0);
    if (n > 0) {
      suppAgents = new ArrayList(n);
      for (int i = 0; i < n; i++) {
        String si = comm.getName() + "-" + (String)l.get(i);
        suppAgents.add(si);
      }
    }
    
    LatLonPoint llp = 
      EarthConstants.spherical_between(
          comm.getLatLonPoint(), 
          EarthConstants.arcRadius(comm.getDistanceForAgent(i)), 
          EarthConstants.degToRad(comm.getDirectionForAgent(i)));
    this.lat = llp.getLatitude();
    this.lon = llp.getLongitude();
  }

  /** only setter!  all other "getters" are based on the Community. */
  public void addSupportedAgentName(String agentName) {
    suppAgents.add(agentName);
  }

  /** @see #addSupportedAgentName */
  public List getSupportedAgentNames() {
    return suppAgents;
  }

  public String getName() {
    return name;
  }
  
  public Object getRoles() {
    return comm.getRolesForAgent(i);
  }

  public float getLatitude() {
    return lat;
  }

  public float getLongitude() {
    return lon;
  }

  public long getStartMillis() {
    return comm.getStartMillis();
  }

  public long getStopMillis() {
    return comm.getStopMillis();
  }

  public long getSniffInterval() {
    return comm.getSniffInterval();
  }

  public long getCustomerDemand() {
    return 
      comm.getDemand() *
      comm.getFactorForAgent(i);
  }

  public List getCustomerTasks() {
    return comm.getCustomerTasksForAgent(i);
  }

  public long getLocalProduction() {
    return comm.getProduction();
  }

  public List getLocalAssets() {
    return comm.getLocalAssetsForAgent(i);
  }

  public Map getAllocationTable() {
    return comm.getAllocationTableForAgent(i);
  }

} // CommunityBasedAgentImpl

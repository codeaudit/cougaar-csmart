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

package org.cougaar.tools.csmart.configgen.community.templates;

import java.util.*;

import org.cougaar.tools.csmart.configgen.*;
import org.cougaar.tools.csmart.configgen.community.*;

/**
 * Base class for all community templates.
 */
public abstract class GenericAgentTemplate 
extends org.cougaar.tools.csmart.configgen.community.Community {

  /** 
   * <u>Single</u> provider that will support all requests from outside 
   * the community.
   *
   * With some work one could fix this to support multiple representatives...
   */
  private int repAgentIndex = -1;

  /** List of all TempAgents in the Community **/
  private List tAgents;

  /** Constructor **/
  public GenericAgentTemplate() {    
    tAgents = new ArrayList(5);
  }

  private static class TempAgent {
    public String name;
    public long distance;
    public float direction;
    public long customerDemandFactor;
    public long production;
    public List supportedAgentNames;
    public Object roles;
    public List localAssets;
    public List custTasks;
    public Map allocationTable;
    public List providerTasks;
  }

  private TempAgent getAgent(int i) {
    return (TempAgent)tAgents.get(i);
  }

  private TempAgent getAgent(String name) {
    for (int i = 0; i < tAgents.size(); i++) {
      TempAgent ta = (TempAgent)tAgents.get(i);
      if (ta.name.equals(name)) {
        return ta;
      }
    }
    return null;
  }

  private void addAgent(TempAgent ta) {
    tAgents.add(ta);
  }

  public int getNumberOfAgents() {
    return tAgents.size();
  }

  public int getRepresentativeAgentIndex() {
    return repAgentIndex;
  }

  public String getNameForAgent(int i) {
    return getAgent(i).name;
  }

  public long getDistanceForAgent(int i) {
    return getAgent(i).distance;
  }

  public float getDirectionForAgent(int i) {
    return getAgent(i).direction;
  }

  public List getSupportedAgentNamesForAgent(int i) {
    return getAgent(i).supportedAgentNames;
  }

  public Object getRolesForAgent(int i) {
    return getAgent(i).roles;
  }
  
  public long getFactorForAgent(int i) {
    return getAgent(i).customerDemandFactor;
  }

  public long getLocalProductionForAgent(int i) {
    return getAgent(i).production;
  }

  public List getLocalAssetsForAgent(int i) {
    return getAgent(i).localAssets;
  }

  public List getCustomerTasksForAgent(int i) {
    return getAgent(i).custTasks;
  }

  public Map getAllocationTableForAgent(int i) {
    return getAgent(i).allocationTable;
  }

  /** 
   */
  public void addCustomer(
      String name, 
      long customerDemandFactor, 
      long distance, 
      float direction) {
    addAgent(name, false, null, customerDemandFactor, distance, direction);
  }

  /**
   */
  public void addProvider(
      String name, 
      boolean isRep, 
      Object supportedAgentNames,
      long distance, 
      float direction) {
    addAgent(name, isRep, supportedAgentNames, 0, distance, direction);
  }

  /**
   */
  public void addAgent(
      String name,
      boolean isRep,
      Object supportedAgentNames,
      long customerDemandFactor,
      long distanceFromCommunityCenter,
      float directionFromCommunityCenter) {

    TempAgent ta = new TempAgent();

    ta.name = name;

    ta.customerDemandFactor = customerDemandFactor;
    ta.distance = distanceFromCommunityCenter;
    ta.direction = directionFromCommunityCenter;

    if (supportedAgentNames == null) {
      // zero supported agent names
    } else if (supportedAgentNames instanceof String) {
      List l = new ArrayList(1);
      l.add(supportedAgentNames);
      ta.supportedAgentNames = l;
    } else if (supportedAgentNames instanceof List) {
      List l = (List)supportedAgentNames;
      for (Iterator iter = l.iterator();
          iter.hasNext();
          ) {
        if (!(iter.next() instanceof String)) {
          throw new IllegalArgumentException(
              "Expecting a List of String supported names");
        }
      }
      ta.supportedAgentNames = l;
    } else {
      throw new IllegalArgumentException(
          "Unexpected \"supported\" type");
    }

    int i = getNumberOfAgents();
    if (isRep) {
      if (repAgentIndex < 0) {
        repAgentIndex = i;
      } else {
        throw new UnsupportedOperationException(
            "Currently only support one \"remote\" (ie representative) "+
            "Agent per Community -- already set to \""+
            getAgent(repAgentIndex).name+"\", given \""+
            name+"\"");
      }
    }

    addAgent(ta);
  }

  /**
   */
  public void addLocalAsset(
      String agentName, 
      String assetName, 
      Object roles,
      long invChaos, 
      long timeChaos, 
      long avgCompleteTime,
      long depleteFactor) {

    TempAgent ta = getAgent(agentName);
    if (ta == null)  {
      throw new IllegalArgumentException(
          "Unknown agent name: \""+agentName+"\"");
    }

    List toL = ta.localAssets;
    if (toL == null) {
      toL = new ArrayList(5);
      ta.localAssets = toL;
    }

    LocalAsset la = new LocalAsset();

    la.setAssetName(assetName);
    la.setInventoryChaos(invChaos);
    la.setTimeChaos(timeChaos);
    la.setAvgCompleteTime(avgCompleteTime);
    la.setDepleteFactor(depleteFactor);

    MergeUtils.assertIsValid(roles);
    la.setRoles(roles);
    ta.roles = MergeUtils.merge(ta.roles, roles);

    toL.add(la);
  }


  /** 
   */
  public void addCustomerTask(
      String agentName, 
      String taskName, 
      String worldState, 
      double vital, 
      long duration, 
      long chaos) {

    TempAgent ta = getAgent(agentName);
    if (ta == null)  {
      throw new IllegalArgumentException(
          "Unknown agent name: \""+agentName+"\"");
    }

    List toL = ta.custTasks;
    if (toL == null) {
      toL = new ArrayList(5);
      ta.custTasks = toL;
    }

    CustomerTask ct = new CustomerTask();

    ct.setTaskName(taskName);
    ct.setWorldState(worldState);
    ct.setVital(vital);
    ct.setDuration(duration);
    ct.setChaos(chaos);

    toL.add(ct);
  }

  /**
   */
  public void addAllocation(
      String agentName, 
      String taskName, 
      Object roles) {

    TempAgent ta = getAgent(agentName);
    if (ta == null)  {
      throw new IllegalArgumentException(
          "Unknown agent name: \""+agentName+"\"");
    }

    Map toM = ta.allocationTable;
    if (toM == null) {
      toM = new HashMap(5);
      ta.allocationTable = toM;
    }

    MergeUtils.assertIsValid(roles);
    ta.roles = MergeUtils.merge(ta.roles, roles);

    toM.put(taskName, roles);
  }

} // GenericAgentTemplate

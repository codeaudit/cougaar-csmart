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
package org.cougaar.tools.csmart.runtime.ldm.plugin.transducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.cougaar.core.agent.ClusterIdentifier;

import org.cougaar.tools.csmart.util.LatLonPoint;
import org.cougaar.tools.csmart.util.NamedLocation;

/**
 * Represent the simulated world for the Transducer
 * Consists of a <code>List</code> of <code>Agent</code>s.
 * Ask it for the agents in the society, and which are within a given radius.
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @version 1.0
 */
public class Society {
  private ArrayList agents = new ArrayList(); // collection of Agents

  public Society() {}

  /**
   * Creates a new <code>Society</code> from a set of <code>Agent</code>s
   *
   * @param agents a <code>Collection</code> of <code>Agent</code>s
   */
  public Society(Collection agents) {
    this.agents.addAll(agents);
  }

  /**
   * Get the full set of <code>Agent</code>s in the society
   *
   * @return a <code>Collection</code> of <code>Agent</code>s
   */
  public Collection getAgents() {
    return agents;
  }

  /**
   * Get the <code>Agent</code>s within the given radius from the given point.
   *
   * @param pos a <code>LatLonPoint</code> center
   * @param kmDistance a <code>float</code> radius from that point
   * @return a <code>Collection</code> of <code>Agent</code>s in that circle
   */
  public Collection getAgentsWithinKM(LatLonPoint pos, float kmDistance) {
    ArrayList subList = new ArrayList();
    for (Iterator iAgt = agents.iterator(); iAgt.hasNext(); ) {
      Agent curr = (Agent)iAgt.next();
      // <= or =??????
      if (curr.distanceFrom(pos) < kmDistance) {
	subList.add(curr);
      }
    }
    return subList;
  }

  /**
   * Does an <code>Agent</code> with the given name exist?
   *
   * @param ag an <code>Agent</code>'s <code>String</code> name 
   * @return a <code>boolean</code>
   */
  public boolean existsAgent(String ag) {
    // Agents are the same if their names are the same
    //return (getAgent(ag) != null);
    return agents.contains(new Agent(ag));
  }

  /**
   * Get the <code>Agent</code> with the given name
   *
   * @param name a <code>String</code> <code>Agent</code> name
   * @return an <code>Agent</code> with that name, <code>null</code> if none exists
   */
  public Agent getAgent(String name) {
    for (Iterator iAgt = agents.iterator(); iAgt.hasNext(); ) {
      Agent curr = (Agent)iAgt.next();
      if (curr.getName().equals(name)) {
	return curr;
      }
    }
    return null;
  }
  
  /**
   * Does an <code>Agent</code> with the given id exist?
   *
   * @param agcid an <code>Agent</code>'s <code>ClusterIdentifier</code> 
   * @return a <code>boolean</code>
   */
  public boolean existsAgent(ClusterIdentifier agcid) {
    // Agents are the same if their ClusterIdentifiers are the same
    //return (getAgent(agcid) != null);
    return agents.contains(new Agent(agcid));
  }

  /**
   * Get the <code>Agent</code> with the given <code>ClusterIdentifier</code>
   *
   * @param cid a <code>ClusterIdentifier</code> <code>Agent</code> cluster id
   * @return an <code>Agent</code> with that Cluster ID, <code>null</code> if none exists
   */
  public Agent getAgent(ClusterIdentifier cid) {
    for (Iterator iAgt = agents.iterator(); iAgt.hasNext(); ) {
      Agent curr = (Agent)iAgt.next();
      if (curr.getClusterIdentifier().equals(cid)) {
	return curr;
      }
    }
    return null;
  }
  
  /**
   * Add the given Agent to the society
   *
   * @param ag an <code>Agent</code> 
   */
  public void addAgent(Agent ag) {
    // no check for duplicates!
    agents.add(ag);
  }

  /**
   * Set the collection of <code>Agent</code>s in the society to this list
   *
   * @param ags a <code>Collection</code> of <code>Agent</code>s
   */
  public void setAgents(Collection ags) {
    this.agents.clear();
    this.agents.addAll(ags);
  }
  
} // Society.java

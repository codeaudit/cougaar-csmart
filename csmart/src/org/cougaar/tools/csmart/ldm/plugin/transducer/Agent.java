/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ldm.plugin.transducer;

import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.tools.csmart.util.*;

/**
 * Represent an Agent in the simulated society.  
 * For use by the Transducer.<br>
 * This adds a <code>ClusterIdentifier</code> version of the name.
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see NamedLocationImpl
 */
public class Agent extends NamedLocationImpl {
  private ClusterIdentifier cid = null;
  public Agent (ClusterIdentifier cid) {
    super(cid.toString());
    this.cid = cid;
  }
  public Agent (ClusterIdentifier cid, float lat, float lon) {
    super(cid.toString(), lat, lon);
    this.cid = cid;
  }
  public Agent (String name) {
    super(name);
    this.cid = new ClusterIdentifier(name);
  }
  public Agent (String name, float lat, float lon) {
    super(name, lat, lon);
    this.cid = new ClusterIdentifier(name);
  }

  public ClusterIdentifier getClusterIdentifier() {
    return this.cid;
  }
} // Agent.java


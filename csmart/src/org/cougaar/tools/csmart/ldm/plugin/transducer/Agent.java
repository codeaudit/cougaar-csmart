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


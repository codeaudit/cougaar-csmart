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
package org.cougaar.tools.csmart.runtime.ldm.lps;

import java.util.Collection;
import java.util.Iterator;

import org.cougaar.core.blackboard.LogPlanLogicProvider;
import org.cougaar.core.blackboard.MessageLogicProvider;
import org.cougaar.core.blackboard.EnvelopeLogicProvider;
import org.cougaar.core.agent.ClusterServesLogicProvider;
import org.cougaar.core.blackboard.LogPlanServesLogicProvider;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.blackboard.EnvelopeTuple;
import org.cougaar.core.blackboard.SubscriberException;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.planning.ldm.plan.Directive;

import org.cougaar.tools.csmart.runtime.ldm.event.RealWorldEvent;
import org.cougaar.tools.csmart.runtime.ldm.event.InfrastructureEvent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

/**
 * LP to transfer <code>RealWorldEvent</code>s and <code>InfrastructureEvent</code>s
 * between Agents (in both directions).
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see RealWorldEvent
 * @see InfrastructureEvent
 * @see org.cougaar.tools.csmart.runtime.ldm.CSMARTDomain
 */
public class ImpactsLP extends LogPlanLogicProvider implements MessageLogicProvider, EnvelopeLogicProvider {

  private transient Logger log;

  public ImpactsLP(LogPlanServesLogicProvider logplan, ClusterServesLogicProvider cluster) {
    super(logplan, cluster);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * If it is a Directive of the appropriate type, with a destination, send it remotely
   *
   * @param obj an <code>Object</code> to send remotely
   * @param changes a <code>Collection</code>
   */
  private void examine(Object obj, Collection changes) {
    if (! (obj instanceof RealWorldEvent || obj instanceof InfrastructureEvent)) return;
    Directive dir = (Directive)obj;
    // get the destination
    ClusterIdentifier destination = dir.getDestination();
    if (destination == null) return;
    // Ensure that the destination is not the local cluster
    if (destination.equals(cluster.getClusterIdentifier())) return;
    logplan.sendDirective(dir, changes);
  }
  
  /**
   * Handle one EnvelopeTuple. Call examine to check for objects that
   * are Impact related and go to a remote Cluster.
   **/
  public void execute(EnvelopeTuple o, Collection changes) {
    Object obj = o.getObject();
    if (o.isAdd()) {
      examine(obj, changes);
    } else if (o.isBulk()) {
      Collection c = (Collection) obj;
      for (Iterator e = c.iterator(); e.hasNext(); ) {
        examine(e.next(), changes);
      }
    }
  }

  /**
   * If the incoming Directive is an appropriate type, add it to the local logplan
   *
   * @param dir a <code>Directive</code> to receive
   * @param changes a <code>Collection</code>
   */
  public void execute(Directive dir, Collection changes) {
    // figure out what type it is:
    if (!(dir instanceof RealWorldEvent || dir instanceof InfrastructureEvent)) return;
    
    // make sure it isn't already there? Otherwise...
    UniqueObject exists = logplan.findUniqueObject(((UniqueObject)dir).getUID());
    if (exists != null && exists.equals(dir)) {
      // it already exists in the logplan. for us, do nothing
      return;
    }
    try {
      logplan.add(dir);
    } catch (SubscriberException se) {
      if(log.isDebugEnabled()) {
        log.error("Could not add RWE or IE to logplan: " + dir);
        se.printStackTrace();
      }
    }
  }
} // ImpactsLP.java


/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ldm.lps;

import java.util.Collection;
import java.util.Iterator;

import org.cougaar.core.cluster.LogPlanLogicProvider;
import org.cougaar.core.cluster.MessageLogicProvider;
import org.cougaar.core.cluster.EnvelopeLogicProvider;
import org.cougaar.core.cluster.ClusterServesLogicProvider;
import org.cougaar.core.cluster.LogPlanServesLogicProvider;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.EnvelopeTuple;
import org.cougaar.core.cluster.SubscriberException;
import org.cougaar.core.society.UniqueObject;
import org.cougaar.domain.planning.ldm.plan.Directive;

import org.cougaar.tools.csmart.ldm.event.RealWorldEvent;
import org.cougaar.tools.csmart.ldm.event.InfrastructureEvent;

/**
 * LP to transfer <code>RealWorldEvent</code>s and <code>InfrastructureEvent</code>s
 * between Agents (in both directions).
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see RealWorldEvent
 * @see InfrastructureEvent
 * @see CSMARTDomain
 */
public class ImpactsLP extends LogPlanLogicProvider implements MessageLogicProvider, EnvelopeLogicProvider {
  public ImpactsLP(LogPlanServesLogicProvider logplan, ClusterServesLogicProvider cluster) {
    super(logplan, cluster);
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
      System.err.println("Could not add RWE or IE to logplan: " + dir);
      se.printStackTrace();
    }
  }
} // ImpactsLP.java


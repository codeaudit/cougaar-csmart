/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.runtime.plugin;

import java.util.Enumeration;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.planning.ldm.asset.Asset;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AuxiliaryQueryType;

import org.cougaar.tools.csmart.runtime.ldm.asset.LocalAsset;
import org.cougaar.tools.csmart.runtime.ldm.asset.SimpleInventoryPG;
import org.cougaar.tools.csmart.Constants;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**  
 * The ExecutorPlugIn intercepts allocations to local assets, and asks
 * the asset to satisfy the request given in the allocation.  The Asset returns
 * false if it could not satisfy the request, and the PlugIn generates a failure
 * response, visible immediately.  Otherwise, it returns the time
 * when the response has completed, some time in the future.  The PlugIn will generate
 * a successful allocation result visible at that future time.  <br>Any incoming
 * request that arrives after the first request (including those arriving in the
 * same execute cycle, but later in the Subscription list) will "see" a lower
 * asset inventory, even though the response to the allocation request
 * is not yet visible.
 * <br>
 * The returned response will have Aspect values for END_TIME.<br>
 * @see org.cougaar.tools.csmart.runtime.ldm.asset.ThermalResourceModelBG
 * @see SimpleInventoryPG
 * @see CSMARTPlugIn
 */
public class ExecutorPlugIn
  extends CSMARTPlugIn {
  // NOTE: This PlugIn is immune to infrastructure hooks currently!
  
  /**
   * Subscription to Allocations.
   *
   * Only want the "addedList" -- this could be substituted
   * for a Subscription implementation that doesn't keep
   * a Collection of everything that has matched in the past.
   */
  private IncrementalSubscription allocSub;

  private transient Logger log;

  private UnaryPredicate allocP = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Allocation) {
	Allocation alloc = (Allocation)o;
        Asset asst = alloc.getAsset();
        if (asst instanceof LocalAsset) {
	  if (((LocalAsset)asst).hasSimpleInventoryPG()) {
	    return true;
	  }
        }
      }
      return false;
    }
  };

  public void setupSubscriptions() {
    log = CSMART.createLogger(this.getClass().getName());

    if (log.isDebugEnabled()) {
      log.debug("setupSubscriptions: " + this + ":Entering");
    }

    allocSub = (IncrementalSubscription)subscribe(allocP);
  }

  public void execute() {
//      if (log.isDebugEnabled()) {
//        log.info("execute:" + this);
//      }

    long currentTime = currentTimeMillis();

    for (Enumeration en = allocSub.getAddedList(); 
        en.hasMoreElements();
        ) {
      if (log.isDebugEnabled()) {
	log.info("execute:" + this + 
          ": got something from Alloc Subscription");
      }

      Allocation alloc = (Allocation)en.nextElement();
      LocalAsset la = (LocalAsset)alloc.getAsset();

      SimpleInventoryPG  trmPG = la.getSimpleInventoryPG();

      // This call will immediately decrement the available 
      // resources in the asset.  The returned time is the time 
      // when the request will have been fully satisfied - some
      // time in the future.  Therefore, if this PlugIn were to 
      // make an additional request at the same time, that 
      // second request would come in _after_ the first, event 
      // though they both happen simultaneously in some sense.
      // And due to chaos, the returned time on the second request 
      // could be earlier than that on the first request.
      // Note that this makes the PlugIn somewhat non-deterministic

      // Also note the semantics of this call: the Request is 
      // immediate, but the response is delayed.  Therefore, an 
      // event visible at the returned time indicates when
      // the request is fully completed and visible to the world,
      // not when the request was received, nor when the ability 
      // of the local asset to satisfy another request was impacted.

      long assetTime = trmPG.consume(currentTime);

      // Create a new AllocationResult
      AllocationResult nar = null;
      //      AllocationResult earlynar = null; // send at start of successful alloc
      
      // Set some aspect values that are relevant
      int[] aspect_types = {AspectType.END_TIME}; // the Aspects that have been satisfied
      double []results = {(double)assetTime}; // matching values for those Aspects
      
      if(assetTime > 0) {
	if (log.isDebugEnabled()) {
	  log.info("execute:" + this + ": Alloc request met at time: " + assetTime);
	}
	nar = theLDMF.newAllocationResult(1.0, // confidence rating
					  true, // success
					  aspect_types, // the metrics of our results
					  results); // and what we got

	nar.addAuxiliaryQueryInfo(AuxiliaryQueryType.FAILURE_REASON, Constants.Rating.SUCCESS);

	// Now create another AR for sending immediately with low confidence
//  	earlynar = theLDMF.newAllocationResult(0.0, // confidence rating
//  					  true, // success
//  					  aspect_types, // the metrics of our results
//  					  results); // and what we got

//          // earlynar.setRatingDescription(Constants.Rating.SUCCESS);
//  	earlynar.addAuxiliaryQueryInfo(AuxiliaryQueryType.FAILURE_REASON, Constants.Rating.SUCCESS);
	// When Allocator / customer are re-done to handle these low-confidence answers,
	// send them.

      } else {
	if (log.isDebugEnabled()) {
	  log.info("execute:" + this + ": Alloc request failed!");
	}

	// do I need different results?
	nar = theLDMF.newAllocationResult(1.0, // confidence rating
					  false, // success
					  aspect_types, // the metrics of our results
					  results); // and what we got
	nar.addAuxiliaryQueryInfo(AuxiliaryQueryType.FAILURE_REASON, Constants.Rating.FAIL_INV_EMPTY);

	// Publish a Report as well?
      }

      // Set the estimated result now, and the observed later.
      // Question: Must I wake myself up later and make the change later?
//        if (assetTime > 0) {
//  	alloc.setEstimatedResult(nar);
//  	publishChange(alloc);
//        }


      // create a future "change" predicate
      final AllocationResult finalNAR = nar;
      UnaryPredicate futureChange =
        new UnaryPredicate() {
          public boolean execute(Object o) {
            // o is our alloc, but cast here for simplicity
            Allocation al = (Allocation)o;
            // we could rethink our change, e.g. by examination of the alloc,
            //   and return "false" -- for now we'll always do the change.
            //
            // put this new AllocationResult on the Allocation as Observed
            al.setObservedResult(finalNAR);
            return true;
          }
        };

      // Make the change visible now for failure or at the returned time
      // for sucess
      publishChangeAt(
          alloc, 
          (assetTime > 0 ? assetTime : currentTime),
          futureChange);
    }

  } // end of execute()

} // end of ExecutorPlugIn

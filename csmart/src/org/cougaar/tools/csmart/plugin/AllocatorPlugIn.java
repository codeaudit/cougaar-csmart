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

package org.cougaar.tools.csmart.plugin;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.plugin.Annotation;
import org.cougaar.core.plugin.util.PlugInHelper;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.Entity;
import org.cougaar.domain.planning.ldm.asset.EntityPG;
import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.domain.planning.ldm.plan.Disposition;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.StringUtility;

import org.cougaar.tools.csmart.Constants;
import org.cougaar.tools.csmart.ldm.event.DeadlineTimerEvent;
import org.cougaar.tools.csmart.ldm.event.NewDeadlineTimerEvent;
import org.cougaar.tools.csmart.ldm.asset.LocalAsset;
import org.cougaar.tools.csmart.ldm.asset.RolesPG;


/**
 * The <code>AllocatorPlugIn</code> models an Ultra*Log "Allocator" PlugIn.
 * <p>
 * This PlugIn reads a file of Allocation rules, which is specified as 
 * the first argument to this PlugIn.  These rules associate
 * <code>Task</code> "getVerb()" with a List of Roles, which are
 * matched to Assets (CSMART LocalAsset and Organizations) that have those
 * roles (<code>RolesPG</code>).  The configuration file also can contain
 * a "config" entry to override the default time durations, such as the
 * <tt>tryTime</tt>.
 * <p>
 * The configuration file is a Comma-Separated-Value file of the following
 * BNF format: 
 * <pre>
 *   [config, fSuccess, tResp (, tAlloc, tTrans, tTry)]
 *   [rule, ] task, role(, role)*
 *   // more "rule" lines as necessary 
 * </pre>
 * For example:
 * <pre>
 *   config, 0.5, 50, 60, 150, 540000
 *   rule, Supply500MREs, SubsistenceProvider
 * </pre>
 * By convention the rules should match Local Assets before Organizations, ie 
 * Allocations to ask another Agent to perform the Task.  These rules also 
 * list organizations that we expect to be more successful, trust more, or 
 * are closer, before others.
 * <p>
 * This allocator also generates a "DeadlineTimerEvent" to catch
 * allocation attempts that take too long, as defined by the "config"
 * durations.  Multiple allocations will be attempted as long as there
 * is remaining time according to the Tasks END_TIME preference and matching
 * allocation rules.  An AllocationResult will be propogated back once
 * this allocator either 1) successfully allocates, or, 2) fails due
 * to deadlines or no more applicable rules.
 *
 * @see CSMARTPlugIn
 * @see Allocation
 */
public class AllocatorPlugIn
    extends CSMARTPlugIn {

  // constant "default" values
  private static final float DEFAULT_SUCCESS_BORDER = 0.5f;
  private static final long DEFAULT_TASK_RESP_PUB_DELAY = 50;
  private static final boolean DEFAULT_USE_SIMPLE_DEADLINE = false;
  private static final long DEFAULT_ALLOC_PUB_DELAY = 60;
  private static final long DEFAULT_TRANS_DELAY = 150;
  private static final long DEFAULT_TRY_TIME = 540000;

  /** 
   * @see #minAllocTime 
   */
  private static final long DEFAULT_MIN_ALLOC_TIME = 
    2 * DEFAULT_TRANS_DELAY + 
    2 * DEFAULT_ALLOC_PUB_DELAY + 
    DEFAULT_TRY_TIME + 
    DEFAULT_TASK_RESP_PUB_DELAY;

  /**
   * An AllocationResult rating of this value or above indicates success.
   * a perusal of how the Rating is acually used shows
   * that numbers are always 0 or 1, so far.
   */
  private float successBorder = DEFAULT_SUCCESS_BORDER;

  /**
   * time for this PlugIn to publish a TaskResponse.
   * my DeadlineExceeded will be for Task's deadline less this
   * to ensure this allocator has time to give the customer an answer.
   */
  private long taskRespPubDelay = DEFAULT_TASK_RESP_PUB_DELAY;

  /**
   * <tt>simpleDeadline</tt> is <tt>false</tt> if using richer model, else 
   * the a simple model using <tt>taskRespPubDelay</tt> will be used to 
   * calculate the deadlines.
   */
  private boolean useSimpleDeadline = DEFAULT_USE_SIMPLE_DEADLINE;

  /**
   * Time for this PlugIn to publish an Allocation.
   */
  private long allocPubDelay = DEFAULT_ALLOC_PUB_DELAY;

  /**
   * The following long section of numbers are used in helping to
   * calculate what the deadline should be on Allocations
   * that this PlugIn publishes.
   *
   * There are 2 models that this PlugIn can use.  In the Simple Model,
   * the deadline is reduced only to account for delays in sending answers
   * around, and no attempt is made to accomodate trying multiple
   * allocations.  In the richer model,  The deadline on
   * each allocation is reduced from the maximum allowed, to leave
   * time to try another allocation if the first one fails.
   *
   * When using the simple deadline model, we reduce the deadlines
   * by the time required to transmit the allocation
   * NOTE: THIS IS A GUESS!!
   * Trans delay = time for a TR to be turned into an AR on another Agent
   * = TR-to-RemoteEvent + wire delay(100) + RE-to-AR(50)
   * or for an Allocation to be turned into a Task on the remote Agent
   */
  private long transDelay = DEFAULT_TRANS_DELAY;

  /**
   * There is also a minimum <tt>tryTime</tt> - for a remote Org to try 
   * to do the Task AGAIN, THIS IS A COMPLETE GUESS - and inaccurate 
   * if the Agent allocated to in turn re-allocates to another Agent
   * This is exactly the time for the Executor to publish the Allocation 
   * Result.
   */
  private long tryTime = DEFAULT_TRY_TIME;

  /**
   * Minimum time to do one allocation:<pre><tt>
   *   2 * this.transDelay + 
   *   2 * this.allocPubDelay + 
   *   this.tryTime + 
   *   this.taskRespPubDelay;</tt></pre>.
   *
   * This will be used in dividing up the available time.
   * This is time for this plugin to publish an allocation and for it to u
   * come back as an allocation result, but does not include the time for 
   * this plugin to turn that into a Task Result.
   */
  private long minAllocTime = DEFAULT_MIN_ALLOC_TIME;

  /**
   * Subscriptions for:
   *   LocalAssets
   *   Entitys
   *   Tasks we allocate
   *   Allocations we generate (where the contained
   *     Allocation's "getParents" matches (Tasks we generate))
   *   DeadlineTimerEvents for Allocations we generate
   */
  private IncrementalSubscription localAssetSub;
  // FIXME -- this could get Organizations too later, if I choose to do so
  private IncrementalSubscription entitySub;
  private IncrementalSubscription taskSub;
  private IncrementalSubscription allocationSub;
  private IncrementalSubscription deadlineSub;

  /**
   * Predicate for the creation of <code>LocalAsset</code>s.
   */
  private static UnaryPredicate localAssetP = 
    new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof LocalAsset) {
	  return true;
	}
        return false;
      }
    };

  /**
   * Predicate for the creation of <code>Entity</code>s.
   */
  // FIXME -- this could also grab organizations, if I choose to be careful...
  private static UnaryPredicate entityP = 
    new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Entity) {
	  return true;
	}
        return false;
      }
    };
  
  /**
   * Predicate for Tasks we allocate.
   * <p>
   * Here we filter to only gather Tasks that match our 
   * rules.
   */
  private final UnaryPredicate createTaskP() {
    // build a unique Set of rule "text" values
    int rsize = templateRules.size();
    final Set textSet = new HashSet(rsize);
    for (int i = 0; i < rsize; i++) {
      String text = ((TemplateRule)templateRules.get(i)).getTask();
      if (text != null) {
        textSet.add(text);
      }
    }

    // create our filter
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return
          ((o instanceof Task) &&
           (textSet.contains(((Task)o).getVerb().toString())));
      }
    };
  }

  /**
   * Predicate for our allocations.
   * <p>
   * This could be done by using the "taskP", but for simplicity
   * we'll check for allocations that this PlugIn
   * generated (i.e. a matching SubscriptionClientName).
   */
  private final UnaryPredicate createAllocationP() {
    final String selfPiId = super.toString();
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Allocation) {
	  Annotation annot = ((Allocation)o).getAnnotation();
	  if (annot instanceof MyAnnot) {
	    String piID = ((MyAnnot)annot).getPlugInID();
	    return (piID == selfPiId);
	  }
        }
        return false;
      }
    };
  } // end of createAllocationP

  /**
   * Predicate for deadline "timers" to our allocations.
   * <p>
   * Similar to <tt>createAllocationP</tt>.
   */
  private final UnaryPredicate createDeadlineP() {
    final String selfPiId = super.toString();
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof DeadlineTimerEvent) {
          String piId = 
            ((DeadlineTimerEvent)o).getPublisher();
          // in the same cluster, so "==" is fine
          return (piId == selfPiId);
        }
        return false;
      }
    };
  }

  /**
   * My "pretty identifier", which is based upon the PlugIn's 
   * UID.
   * <p>
   * This is simply ("Allocator ("+getUID())+")") and will prefix
   * all logger messages.
   */
  private String myId;

  /**
   * Filler for empty/non-configured rule table.
   */
  private static final ExpandedRule[] ZERO_EXPANDED_RULES = 
    new ExpandedRule[0];

  /**
   * Our <tt>parseTemplateRules</tt>-based configuration, which is a
   * <code>List</code> of <code>TemplateRule</code>s.
   */
  private List templateRules;

  /**
   * Keep lists of sorted assets as they arrive<br>
   * local, entity, and both together, local before entity<br>
   * Initializing these as null indicates for the code that none
   * have arrived yet.<br>
   * All local assets should arrive together (there may be none),
   * and the entity assets may trickle in (there should be some)
   */
  private List sortedlocalassets = null;
  private List entityassets = null;
  private List comboassets = null;
  
  /**
   * Our runtime-expanded table of rules that are associated
   * with actual <code>Asset</code>s.
   */
  private ExpandedRule[] expandedRules = ZERO_EXPANDED_RULES;

  /**
   * Track if we've touched the end of the list of local assets
   * to know if we might have a problem
   */
  private boolean haveAllocatedPastLocals = false;

  /**
   * Parse our rules from the given file, and create our subscriptions.<br>
   * This PlugIn subscribes to:
   * Asset Creations - to get the possible Assets to allocate to
   * Tasks - to get the Tasks that need to be allocated
   * Allocations - to get AllocationResults that indicate how
   * our allocations fared.
   * And finaly DeadlineTimer Events, in case an allocation took too long.
   */
  public void setupSubscriptions() {
    // set my logging identifier
    this.myId = "Allocator ("+getUID()+")";

    if (log.isApplicable(log.VERY_VERBOSE)) {
      log.log(this, log.VERY_VERBOSE, 
          (myId+" entering setupSubscriptions"));
    }

    // parse the "template" rules from our configuration info
    try {
      parseTemplateRules();
    } catch (Exception e) {
      // unable to configure!
      if (log.isApplicable(log.SEVERE)) {
        log.log(this, log.SEVERE, 
            (myId+" configuration error: "+e.getMessage()));
      }
      return;
    }

    //
    // subscribe to the creation of LocalAssets we allocate to.  A PlugIn
    //   creates the assets, so we must wait one execution cycle to
    //   see any Assets.
    //
    localAssetSub = (IncrementalSubscription)subscribe(localAssetP);

    //
    // subscribe to the creation of org Assets we allocate to.  A PlugIn
    //   creates the assets, then sends them via tasks, so they come later
    // and may dribble in
    //
    entitySub = (IncrementalSubscription)subscribe(entityP);

    //
    // subscribe to Tasks we Allocate, based upon the "text" value
    //   defined in the rules.  For now let's group all our tasks 
    //   together into one subscription.
    //
    taskSub = (IncrementalSubscription)subscribe(createTaskP());

    //
    // subscribe to Allocations.  This is the typical feedback 
    //   channel for allocations.  We should assume that multiple
    //   Allocators might be loaded.
    //
    allocationSub = (IncrementalSubscription)subscribe(createAllocationP());

    //
    // subscribe to DeadlineTimerEvents.  Allocator should generate
    //   these "wakeup" events to catch allocations take too long (say 
    //   expected + 100 cycles).
    //
    deadlineSub = (IncrementalSubscription)subscribe(createDeadlineP());

    if (log.isApplicable(log.VERY_VERBOSE)) {
      log.log(this, log.VERY_VERBOSE,
          (myId+" waiting for Tasks"));
    }
  } // end of setupSubscriptions

  /**
   * Parse our parameters and configuration file(s) to construct
   * the initial "template" rule table, plus set the "Delay" and
   * other configuration variables.
   * <p>
   * The config file name is the single parameter this PlugIn expects.
   * Then that file is a Comma-Separated-Value file of the following
   * BNF format: 
   * <pre>
   *   [config, fSuccess, tResp (, tAlloc, tTrans, tTry)]
   *   [rule, ] task, role(, role)*
   *   // more "rule" lines as necessary 
   * </pre>
   * For example:
   * <pre>
   *   config, 0.5, 50, 60, 150, 540000
   *   rule, Supply500MREs, SubsistenceProvider
   * </pre>
   * If the "config" line is not specified then the "DEFAULT_*" values
   * will be used.  If one specifies a "config" with two arguments then
   * <tt>useSimpleDeadline</tt> will be set to <tt>true</tt>.
   * <p>
   * The "rule, " prefix is optional for backwards compatibility.
   * <p>
   * The Text is the name of the Task type, and the Role indicates the 
   * role that a matching Asset must have in order to be able to perform 
   * the given Task.
   * <p>
   * Multiple "rule" entries for the same Task type are possible, even 
   * expected.  Any entry for a Local Asset comes first, by convention.  
   * Later rules are for increasingly remote, less trustworthy 
   * organizations.
   * <p>
   * If multiple roles are indicated for a given Task type, then that means
   * all the roles given must be present on an Asset for it to
   * be capable of performing the task.
   * <p>
   * At some point we might use Regular Expressions in the
   * pattern matching, such as GNU's RegEx for Java:
   *   http://www.cacas.org/~wes/java/
   * For now we'll keep things simple and postpone this enhancement 
   * until it's required.
   */
  protected void parseTemplateRules() throws Exception {
    // read the filename from our parameters
    Vector params = getParameters() != null ? new Vector(getParameters()) : null;
    int nparams = ((params != null) ? params.size() : 0);

//      if(log.isApplicable(log.VERY_VERBOSE)) {
//        log.log(this, log.VERY_VERBOSE, "parseTemplateRules:" + this + ":Entering");
//      }

    if (nparams == 0) {
      // no file specified
      throw new RuntimeException(
          "Expecting at least one filename as a PlugIn parameter");
    }

    // initialize the TemplateRule List
    this.templateRules = new ArrayList();

    // set the default "time" configuration

    // for all filenames, typically 1
    for (int i = 0; i < nparams; i++) {
      String filename = (String)params.elementAt(i);

      if(log.isApplicable(log.VERY_VERBOSE)) {
	log.log(this, log.VERY_VERBOSE, "parseTemplateRules:" + this + ":Parsing - " +
		filename);
      }

      // open our configuration stream[i]
      InputStream in = 
        getConfigFinder().open(filename);

      // read "(String, String)" pairs
      BufferedReader bufIn =
        new BufferedReader(
            new InputStreamReader(in));
      while (bufIn.ready()) {
        String line = bufIn.readLine();
        if (line == null) {
          // end of file
          break;
        }
        line = line.trim();
        if ((line.length() == 0) || 
            (line.charAt(0) == '#')) {
          // skip empty/comment line
          continue;
        }
        // parse Comma-Separated-Values
        List csv = StringUtility.parseCSV(line);
        int csvSize = csv.size();
        if (csvSize < 1) {
          throw new RuntimeException(
              "Illegal format for Allocator line: \""+line+
              "\": expecting at least one element");
        }
        String arg = (String)csv.get(0);
        if (arg.equalsIgnoreCase("config")) {
          // configure.  See "RuntimeException" below for parsing details
          try {
            this.successBorder = Float.parseFloat((String)csv.get(1));
            this.taskRespPubDelay = Long.parseLong((String)csv.get(2));
            if (csvSize == 3) {
              this.useSimpleDeadline = true;
            } else {
              this.useSimpleDeadline = false;
              this.allocPubDelay = Long.parseLong((String)csv.get(3));
              this.transDelay = Long.parseLong((String)csv.get(4));
              this.tryTime = Long.parseLong((String)csv.get(5));
              this.minAllocTime = 
                2 * this.transDelay + 
                2 * this.allocPubDelay + 
                this.tryTime + 
                this.taskRespPubDelay;
            }
          } catch (Exception e) {
            throw new RuntimeException(
                "Illegal format for Allocator line: \""+line+
                "\":\n"+
                "\"config\" expecting at least three arguments:\n"+
                "  config, float_successBorder, long_taskRespPubDelay\n"+
                "and three more optional arguments:\n"+
                "  , long_allocPubDelay, long_transDelay, long_tryTime");
          }
        } else {
          if (!(arg.equalsIgnoreCase("rule"))) {
            // insert "rule, " for backwards compatibility
            csv.add(0, "rule");
            csvSize++;
          }
          if (csvSize < 3) {
            throw new RuntimeException(
                "Illegal format for Allocator line: \""+line+
                "\": expecting at least three arguments:\n"+
                "  rule, String_text, String_role\n"+
                "and optional additional roles:\n"+
                "  , String_role_2, ..., String_role_N");
          }
          String text = (String)csv.get(1);
          Object roles;
          if (csvSize == 3) {
            // just a single String_ROLE
            roles = csv.get(2);
          } else {
            // a List of the remaining String_ROLEs
            csv.remove(0);
            csv.remove(0);
            roles = csv;
          }
	  if(log.isApplicable(log.VERY_VERBOSE)) {
	    log.log(this, log.VERY_VERBOSE, "parseTemplateRules:" + this + 
		    ": Adding new rule: " + text + ":" + roles.toString());
	  }  
          this.templateRules.add(new TemplateRule(text, roles));
        }
      }

      // close our configuration stream[i]
      in.close();
    }
  } // end of parseTemplateRules

  /**
   * Examine subscriptions, use rules to generate allocations,
   * propagate task responses upward, handle deadline events.
   */
  public void execute() {
    // get the current time
    long currTime = currentTimeMillis();

//      if(log.isApplicable(log.VERY_VERBOSE)) {
//        log.log(this, log.VERY_VERBOSE, "execute:" + this + ":entering");
//      }

    //
    // Watch for added Assets
    // LocalAssets should all happen at once in the first execute cycle.
    // Entitys however will likely dribble in over time
    // So create sorted lists of these separately, then combine them
    // with entities after local.
    // However, if we happen to do an allocation before all the assets
    // are around, the rule indexes will be screwy, and we may have used
    // the "wrong" rule.  Cant prevent it, but at least can log it.
    if (localAssetSub.hasChanged()) {
      // verify that the current time is 1
      if (this.sortedlocalassets == null) {
        // not efficient but okay.  Better would be to use
        // a subclass of Subscription that maintains a sort
	Collection addedC = localAssetSub.getAddedCollection();

	// use Todd's sorting mechanism to sort this list of local assets
	// Note that all local assets should arrive simultaneously
	sortedlocalassets = sortAssets(addedC);
	if (comboassets != null) {
	  // UhOh! there are already some orgs in here! Error?
	  if (log.isApplicable(log.DEBUG)) {
	    log.log(this, log.DEBUG, "execute: " + this + " got Entitys before local assets!");
	  }
	  // We could:
	  //1: ignore the problem that some allocations may have happened,
	  // and stick these in the front of the list
	  List old = comboassets;
	  comboassets = new ArrayList(sortedlocalassets);
	  comboassets.addAll(old);
	  // 2: we could give up on the original order intended, and put these at
	  // the end of the list:
	  // comboassets.addAll(sortedlocalassets);
	} else {
	  // These are the first assets to arrive
	  if (log.isApplicable(log.VERBOSE)) {
	    log.log(this, log.VERBOSE, "execute: " + this + " got first assets, locals");
	  }
	  if (haveAllocatedPastLocals) {
	    if (log.isApplicable(log.PROBLEM)) {
	      log.log(this, log.PROBLEM, "execute: " + this + " failed a previous allocation due to no rules at all, and now there are some. Why did they arrive late?");
	    }
	  }
	  comboassets = sortedlocalassets;
	}

	if(log.isApplicable(log.VERY_VERBOSE)) {
	  log.log(this, log.VERY_VERBOSE, "execute:" + "asset size = " + 
		  comboassets.size());
	}

        // save the array of "expanded" rules
        //   catch rollback by clobbering the field if already set
        this.expandedRules = 
          handleCreatedAsset((List)comboassets);
      } else {
        if (log.isApplicable(log.SEVERE)) {
          log.log(this, log.SEVERE,
              (myId+" ignoring assets created at time ("+
               currTime+") > 1"));
        }
        // System.exit(-1);
      }
    } // end of block checking for new Assets

    // Handle incoming Entitys
    // Note that these will trickle in, as some of them get sent over by tasks
    // so we need to be careful in creating our list of expanded rules
    if (entitySub.hasChanged()) {
      Collection addedC = entitySub.getAddedCollection();

      // If the subscription changed but no new entities were created, we dont care
      // Although I suppose one might have been removed?
      if (addedC == null || addedC.isEmpty()) {
	// log something? 
      } else if (this.entityassets == null) {
	// These are the first Entitys that are arriving
	entityassets = sortAssets(addedC);
	if (comboassets != null) {
	  // this is sort of expected. We already have some local assets
	  // Tack these on to the end
	  // however, its conceivable that a Task will have been allocated already!!!
	  // FIXME!!!
	  if (log.isApplicable(log.VERY_VERBOSE)) {
	    log.log(this, log.VERY_VERBOSE, "execute: " + this + " got entities after some local assets. Tasks may have been allocated!");
	  }
	  if (log.isApplicable(log.PROBLEM)) {
	    if (haveAllocatedPastLocals) {
	      log.log(this, log.PROBLEM, "execute: " + this + " and weve done an allocation that either ran out of rules and so would have been failed!");
	    }
	  }
	  comboassets.addAll(entityassets);
	} else {
	  // Entitys arrived before local assets? Maybe there are none.
	  if (log.isApplicable(log.DEBUG)) {
	    log.log(this, log.DEBUG, "execute: " + this + " got entities before any locals");
	  }
	  // Could I have a flag to notice if any tasks have been allocated?
	  if (haveAllocatedPastLocals) {
	    if (log.isApplicable(log.PROBLEM)) {
	      log.log(this, log.PROBLEM, "execute: " + this + " previous allocation used an org or ran out of rules, and so might legitimately have used one of these new orgs. Next re-alloc might do something funny cause of rule index change.");
	    }
	  }
	  // The problem is that I can't really stop the allocations altogether
	  // FIXME!!
	  comboassets = entityassets;
	} // end of checks whether there are previous comboassets	
      } else {
	// entitys are trickling in.  Its possible we've already allocated a task!!!
	if (log.isApplicable(log.VERBOSE)) {
	  log.log(this, log.VERBOSE, "execute: " + this + " entitys are trickling in!");
	}
	if (haveAllocatedPastLocals) {
	  if (log.isApplicable(log.PROBLEM)) {
	    log.log(this, log.PROBLEM, "execute: " + this + " previously allocated to a non-local, so that allocation likely screwy.");
	  }
	}
	// be sure to use the previous entity assets as well
	entityassets.addAll(addedC);
	entityassets = sortAssets(entityassets);
	// Make combo have locals before these entities

	// sortedlocalassets might be empty so far
	if (sortedlocalassets != null && ! sortedlocalassets.isEmpty()) {
	  comboassets = new ArrayList(sortedlocalassets);
	} else {
	  comboassets = new ArrayList();
	}
	comboassets.addAll(entityassets);
      } // end of if block checking have entity assets already
      
      if(log.isApplicable(log.VERY_VERBOSE)) {
	log.log(this, log.VERY_VERBOSE, "execute:" + "asset size = " + 
		comboassets.size());
      }
      
      // save the array of "expanded" rules
      //   catch rollback by clobbering the field if already set
      this.expandedRules = 
	handleCreatedAsset((List)comboassets);
    } // end of loop over entity assets

    //
    // Watch for Tasks and generate an Allocation to a matching
    // Asset, as specified in our rules.  If no such asset exists then 
    // generate an immedate "failed" Disposition
    //
    // Need to also generate a DeadlineTimerEvent on the Allocation.
    //
    if (taskSub.hasChanged()) {
      // for each new task
      for (Enumeration en = taskSub.getAddedList(); 
          en.hasMoreElements(); 
          ) {
        Task t = (Task)en.nextElement();
        if (log.isApplicable(log.VERY_VERBOSE)) {
          log.log(this, log.VERY_VERBOSE,
              (myId+" received new task: "+t.getUID()));
        }
        // allocate the task
	// So this method we would expect to
	// a) Generate an Allocation
	// b) Generate a DeadlineTimer
	// c) If however there is no rule that matches to allow us to do
	// a & b above, then publish a "failed" Disposition.
	// d) Else if there is not enought time, pub a Disposition saying so

        allocate(t, -1, currTime, null);

	if (t.getPlanElement() == null) {
	  if (log.isApplicable(log.PROBLEM)) {
	    log.log(this, log.PROBLEM, "execute: " + this + " allocated a task, but it says it has not PlanElement! Task: " + t);
	  }
	}
      }

      // AMH 7/8/2001: Should I look for removed tasks, to propagate the removal?
      for (Enumeration en = taskSub.getRemovedList();
	   en.hasMoreElements();) {
	Task t = (Task)en.nextElement();
	if (log.isApplicable(log.VERY_VERBOSE)) {
	  log.log(this, log.VERY_VERBOSE, "execute: " + this + " notified of removed Task: " + t.getUID());
	}
	
	// Look in my list of Allocations and remove any that match.
	for (Enumeration en1 = allocationSub.elements(); en1.hasMoreElements(); ) {
	  Allocation alloc = (Allocation)en1.nextElement();
	  if (alloc.getTask().equals(t)) {
	    // This Allocation must be removed.
	    // But also, we could do a search through
	    // my subscription for the deadline event, and remove that as well
	    for (Enumeration en2 = deadlineSub.elements(); en2.hasMoreElements(); ) {
	      DeadlineTimerEvent deadline = (DeadlineTimerEvent)en2.nextElement();
	      if (deadline.getRegarding() instanceof Allocation && ((Allocation)deadline.getRegarding()).equals(alloc)) {
		// This is the deadline corresponding to the Allocation
		rescindDeadlineTimerEvent(deadline);
		break;
	      }
	    } // end of loop to find deadline for this Allocation
	    // Now rescind the Allocation
	    rescindAllocation(alloc);
	    break;
	  }
	} // end of loop to find Allocation of removed task

	// Now I also might have published a Disposition of that Task
	// I don't have a subscription to those, but I could query
	// the Blackboard
	// FIXME!!!
      } // end of loop over removed tasks.
    } // end of block checking for new tasks to allocate

    // Handle changes to published Allocations
    
    //  On getting a change in an Alloc:
    //  - If new AR not high-conf, do nothing
    //  - else if high conf AR is success, copy reported to Estimated
    //  - else if have additional matching rules
    //    -- rescind previous Alloc
    //    -- pub new Alloc, Deadline
    //  - else its a failure with no more matching rules.
    //  -- rescind the Allocation and instead publish a failure Disposition,
    // whose reason lists the failure reason listed on the Alloc's failed AR.
    if (allocationSub.hasChanged()) {
      // For each allocation which has changed (new AR)
      for (Enumeration en = allocationSub.getChangedList(); 
          en.hasMoreElements(); 
          ) {
        Allocation alloc = (Allocation)en.nextElement();

	// FIXME! Maybe make sure that the change was a change to the ReportedResult
	if (alloc.getReportedResult() == null || alloc.getReportedResult().equals(alloc.getEstimatedResult())) {
	  // either there is no reported result or its already the same as the estimated result
	  // so ignore this
	  if (log.isApplicable(log.VERBOSE)) {
	    log.log(this, log.DEBUG, "execute: " + this + " allocationSub fired but this alloc did not have a ReportedResult or its same as the Estimated, skipping: " + alloc);
	  }
	  continue;
	}
	AllocationResult estAR = alloc.getEstimatedResult();
	if (estAR != null && estAR.getConfidenceRating() > successBorder) {
	  // this Allocation already has a high confidence EstimatedResult - theres nothing
	  // to do here
	  if (log.isApplicable(log.VERBOSE)) {
	    log.log(this, log.DEBUG, "execute: " + this + " allocationSub fired but this alloc allready has a high conf. Estimated result, skipping: " + alloc);
	  }
	  continue;
	}

	if (getAllocRuleIndex(alloc) <= -100) {
	  // Our marker that we rescinded an Allocation
	  // or propagated its success up is already here
	  // dont do anything more
	  if (log.isApplicable(log.PROBLEM)) {
	    log.log(this, log.PROBLEM, "execute: " + this + " woke up with Allocation Id otherwise try to handle, but it has the rule index marker: " + alloc);
	  }
	  continue;
	}

	// What about using changeReports? Only works if Executor fixed
//  	if (! PlugInHelper.checkChangeReports(allocationSub.getChangeReports(alloc), PlanElement.ReportedResultChangeReport.class)) {
//  	  // This Allocation did not change due to a ReportedResult change
//  	  if (log.isApplicable(log.VERBOSE)) {
//  	    log.log(this, log.DEBUG, "execute: " + this + " allocationSub fired but this alloc did not have the ReportedResult change according to the change reports, skipping: " + alloc);
//  	  }
//  	  continue;
//  	}
        handleAllocation(
            alloc,
            currTime);
      }
    } // end of block checking for new responses

    if (deadlineSub.hasChanged()) {
      // for each new deadline ExceededEvent
      for (Enumeration en = deadlineSub.getAddedList();
          en.hasMoreElements();
          ) {
        DeadlineTimerEvent d = 
          (DeadlineTimerEvent)en.nextElement();
        handleDeadlineEvent(
            d,
            currTime);
      }
    } // end of check of new DeadlineTimerEvents
//      if (log.isApplicable(log.VERY_VERBOSE)) {
//        log.log(this, log.VERY_VERBOSE, "execute: " + this + " exitting execute loop");
//      }
  } // end of execute()

  /**
   * Given a <code>List</code> of <code>Asset</code>s, create
   * new array of <code>ExpandedRule</code>s.<br>
   * Note that this method can be called as many times as you like, and will
   * regenerate the list each time.
   */
  private final ExpandedRule[] handleCreatedAsset(
      final List assetList) {
    // create a new table of "expanded" rules
    List expRules = new ArrayList();

    int nAssets = assetList.size();
//      if (log.isApplicable(log.VERY_VERBOSE)) {
//        log.log(this, log.VERY_VERBOSE,
//            (myId+" received "+nAssets+" assets"+
//             "\nnow matching to rules"));
//      }
    // for all "template" rules
    int nTemplateRules = this.templateRules.size();
    for (int i = 0; i < nTemplateRules; i++) {
      TemplateRule templateRuleI = 
        ((TemplateRule)this.templateRules.get(i));
      // for all assets
      for (int j = 0; j < nAssets; j++) {
	// These assets are either LocalAsset's
	// or org.cougaar.domain.glm.mlm.ldm.asset.Organization's
	// or Entity's
	// at any rate, they are all org.cougaar.domain.planning.ldm.asset.Asset's
	org.cougaar.domain.planning.ldm.asset.Asset assetJ = (org.cougaar.domain.planning.ldm.asset.Asset)assetList.get(j);
	
        // if asset matches the "template" rule
        if (templateRuleI.matches(assetJ)) {
          // create a new "expanded" rule to associate the
          //   template rule to the matching asset
          ExpandedRule expandedRuleIJ = 
            new ExpandedRule(templateRuleI, assetJ);
          if (log.isApplicable(log.VERY_VERBOSE)) {
            log.log(this, log.VERY_VERBOSE,
                (myId+" matched asset "+assetJ.getUID()+
                 " to rule "+templateRuleI+" as expanded rule["+
                 expRules.size()+"]"));
          }
          // append the new "expanded" rule to the table
          expRules.add(expandedRuleIJ);
        } else {
          if (log.isApplicable(log.VERY_VERBOSE)) {
            log.log(this, log.VERY_VERBOSE,
                (myId+" ignoring non-matching asset "+assetJ+
                 " with rule "+templateRuleI));
          }
        }
      }
    }

    // currently the rule "text" is a String.equals, so we could 
    //   construct a useful Map here for easy lookup. At some 
    //   point we might switch to a pattern-match, so let's not
    //   optimize this...

    // return the array of "expanded" rules
    return
      (ExpandedRule[])expRules.toArray(new ExpandedRule[expRules.size()]);
  } // end of handleCreatedAsset()

  /**
   * Handle a response to an Allocation, possibly re-allocate if
   * this is a "failure" response and we haven't already timed-out.<br>
   *  On getting a change in an Alloc:<br>
   *  - If new AR not high-conf, do nothing<br>
   *  - else if high conf AR is success, copy reported to Estimated<br>
   *  - else if have additional matching rules<br>
   *    -- rescind previous Alloc<br>
   *    -- pub new Alloc, Deadline<br>
   *  - else its a failure with no more matching rules. <br>
   *  -- rescind the Allocation and instead publish a failure Disposition,
   * whose reason lists the failure reason listed on the Alloc's failed AR. It
   * could in fact be the same AR, right!<br>
   * @param allocI <code>Allocation</code> with new result to handle
   * @param currTime <code>long</code> time at which the new result appeared
   */
  private final void handleAllocation(
				      final Allocation allocI,
				      final long currTime) {
    
    AllocationResult responseI = allocI.getReportedResult();
    if (responseI == null) {
      if (log.isApplicable(log.PROBLEM)) {
	log.log(this, log.PROBLEM, "handleAllocation: " + this + " got alloc with null AR: " + allocI);
      }
      // Rescind the alloc? Wait for an AR to appear? 
      // For now, try waiting
      return;
    }
    
    // Find the single parent of the Allocation which is a Task
    Task task = allocI.getTask();
    if (task == null) {
      // Allocation couldn't find the Task its allocating
      if (log.isApplicable(log.PROBLEM)) {
        log.log(this, log.PROBLEM, 
		"execute: " + this + 
		" got Allocation with no Task parent: " + allocI);
      }
      rescindAllocation(allocI);
      return;
    }
    
    // If the Allocation's Task doesnt point back to the Allocation, we have trouble
    if (task.getPlanElement() == null || ! task.getPlanElement().equals(allocI)) {
      if (log.isApplicable(log.SEVERE)) {
	log.log(this, log.SEVERE, "handleAllocation: " + this + " got alloc whose Task doesn't point back to it! Alloc: " + allocI + " with RepResult: " + responseI + " and Task: " + task);
      }
      rescindAllocation(allocI);
      return;
    }

    // Make sure we didnt get here by mistake
    if (getAllocRuleIndex(allocI) <= -100) {
      // Weve previously handled this Allocation. Don't do it again
      if (log.isApplicable(log.SEVERE)) {
	log.log(this, log.SEVERE, "handleAllocation: " + this + " got an Allocation and about to update the PlanElement on it, but its marked with rule index: " + getAllocRuleIndex(allocI) + " for alloc: " + allocI);
      }
      return;
    }

    // get out the deadline from the Task
    long taskDeadline = getTaskDeadline(task);

    // AMH 6/20: I've removed code here that checked the deadline to see if the result
    // is too late. We want to favor a response over a deadline thats simulatneous,
    // and if the deadline comes first, we'll rescind the Allocation anyhow.

    if (responseI.getConfidenceRating() <= successBorder) {
      // This response is not high confidence. For now, we do nothing
      if (log.isApplicable(log.DEBUG)) {
	log.log(this, log.DEBUG, "handleAllocation: " + this + " got low confidence AR on allocation " + allocI);
      }
      return;
    } // end of block on low-confidence AR

    // OK, so the Allocation has a high confidence result.
    // Was it a success?
    if (responseI.isSuccess()) {
      // Then we're done. Report the result up the chain.

      // This copies the Reported result into the Estimated slot.
      if (PlugInHelper.updatePlanElement(allocI)) {

	// Mark this Allocation as having been handled, such that
	// I dont try to re-handle it later.
	// Do so by resetting the alloc ruleID to an obvious thing
	setAllocRuleIndex(allocI, (-1 * (getAllocRuleIndex(allocI) + 100)));
      
	// Advertise the change
	// Note that this PlugIn will see this change unless it is careful
	// Also, we really out to use the new mechanism for delaying
	// making the change
	// The trick being that within that UnaryPredicate, we really want
	// to use the PlugInHelper again, and only then do the 
	// setAllocRuleIndex and log.
	// FIXME!!!
	publishChangeAfter(allocI, taskRespPubDelay);
	//publishChange(allocI);
	if (log.isApplicable(log.VERBOSE)) {
	  log.log(this, log.VERBOSE, "handleAllocation: " + this + " reporting succesful Allocation up: " + allocI);
	}
      }
      return;

    } // end of block on succesfull AR

    // OK: It's a failure. So try to re-allocate.
    int ruleIndex = 0; // fill in the AllocRuleIndex,
    ruleIndex = getAllocRuleIndex(allocI);
    
    if (log.isApplicable(log.VERBOSE)) {
      // get the reason out of the AR
      String reason = null;
      // How do I retrieve the reason from the AR?
      reason = responseI.auxiliaryQuery(AuxiliaryQueryType.FAILURE_REASON);
      if (reason == null) 
	reason = "(no reason given)";
      log.log(this, log.VERBOSE, "handleAllocation: " + this + " got failed response due to " + reason + " for alloc " + allocI);
    }
    // However if there are no more rules, just do a failure Disposition saving the original
    // failure reason
    // The following method will take care of rescinding the Allocation for us
    allocate(task, ruleIndex, currTime, allocI);
  } // end of handleAllocation()

  /**
   * Delete the old Allocation given.
   *
   * @param alloc an <code>Allocation</code> to rescind
   */
  private void rescindAllocation(Allocation alloc) {

    // Lets make sure we don't do anything with this Allocation later.
    if (getAllocRuleIndex(alloc) > -100) 
      setAllocRuleIndex(alloc, (-1 * (getAllocRuleIndex(alloc) + 100)));

    // log something
    if (log.isApplicable(log.VERBOSE)) {
      log.log(this, log.VERBOSE, "rescindAllocation: " + this + " deleting Allocation " + alloc);
    }
    // rescind the allocation
    publishRemove(alloc);
  } // end of rescindAllocation
  
  /**
   * Delete the old DeadlineTimerEvent given.
   *
   * @param deadline an <code>DeadlineTimerEvent</code> to rescind
   */
  private void rescindDeadlineTimerEvent(DeadlineTimerEvent deadline) {
    // log something
    if (log.isApplicable(log.VERBOSE)) {
      log.log(this, log.VERBOSE, "rescindDeadlineTimerEvent: " + this + " deleting DeadlineTimerEvent " + deadline);
    }
    // rescind the deadline
    publishRemove(deadline);
  } // end of rescindDeadlineTimerEvent

  /**
   * When a DeadlineTimerEvent is received (from an allocation), first
   * check if the corresponding Allocation has a high confidence EstimatedResult
   * already.<br>
   * If so then rescind the (non-applicable) Deadline.
   * <br>
   * otherwise:<br>
   *    rescind this DeadlineEvent, re-allocate the task, rescind that prev. allocation<br>
   *     -- if there are no more rules then instead its a new failure Disposition
   *        due to Deadline exceeded<br>
   * @param deadlineI <code>DeadlineTimerEvent</code> thats gone off
   * @param currTime <code>long</code> time at which the event arrived
   */
  private final void handleDeadlineEvent(
      final DeadlineTimerEvent deadlineI,
      final long currTime) {
    Allocation allocI = 
      (Allocation)deadlineI.getRegarding();
    
    if (allocI == null) {
      // I guess the Allocation in question was rescinded or something.
      // rescind the DeadlineEvent & log something
      rescindDeadlineTimerEvent(deadlineI);
      return;
    } else if (getAllocRuleIndex(allocI) <= -100) {
      // This is our marker that weve handled this Allocation (ie rescinded it
      // or propagated the success up. dont do any more with it
      rescindDeadlineTimerEvent(deadlineI);
      return;
    }
    
    AllocationResult estAR = allocI.getEstimatedResult();
    if (estAR != null && estAR.getConfidenceRating() > successBorder) {
      // Note - this block should also catch the case where the AllocationResult
      // appeared just now...
      // This Allocation already done. Deadline is irrelevant
      rescindDeadlineTimerEvent(deadlineI);
      return;
    }
    
    // the deadline has been exceeded

    // get the task from the allocation
    Task task = allocI.getTask();
    if (task == null) {
      // Allocation couldn't find the Task its allocating
      if (log.isApplicable(log.PROBLEM)) {
        log.log(this, log.PROBLEM, 
		"execute: " + this + 
		" got Allocation with no Task: " + allocI);
      }
      return;
    }
    
    if (log.isApplicable(log.VERBOSE)) {
      log.log(this, log.VERBOSE,
	      (myId+" exceeded deadline: "+deadlineI.getUID()+
	       "\nfor allocating task: "+task.getUID()+
	       "\nwill attempt another allocation["+
	       (getAllocRuleIndex(allocI)+1)+"]"));
    }
    
    // attempt to re-allocate to the next rule
    // Note that this method someplace should rescind this Deadline
    // and the assoc. Allocation
    allocate(task, getAllocRuleIndex(allocI), currTime, deadlineI);
  } // end of handleDeadlineEvent()

  /**
   * Allocate the <code>Task</code> using our 
   * <code>ExpandedRule</code>s.<br>
   *
   * These rules are ordered by convention: First any <code>LocalAsset</code>,
   * then <code>OrgAsset</code>s.  The Org rules are ordered,
   * with those most likely to succeed, and "closest" coming first.<br>
   * This method find the first matching rule starting with the
   * provided index.<br>
   * It then generates an Allocation for that rule,
   * for the given Task, calculating a reasonable deadline,
   * and including the given Event as a parent (allowing us
   * to indicate a previous failed allocation as a parent).<br>
   * The deadline calculation can be complex.  First, the deadline is
   * earlier than that on the Task, to allow time to propagate any
   * response back. Secondly, it is shortened if possible, to permit
   * future re-allocations to other assets if the first allocation fails.<br>
   *
   * @param task a <code>Task</code> to Allocate
   * @param startingRuleIndex one less than the <code>int</code> rule index
   *   to start with; typically this is the index of the last allocation
   * @param currTime a <code>long</code> current time
   * @param parent a <code>DeadlineTimerEvent</code> or <code>Allocation</code>
   * to use to grab previous attempt
   *
   * @return true if allocation+deadline generated, otherwise a 
   *    failure-response was generated
   */
  protected final boolean allocate(
      final Task task, 
      final int startingRuleIndex, 
      final long currTime,
      final Object parent) {
    
    DeadlineTimerEvent deadlineI = null;
    Allocation allocI = null;
    
    if (parent instanceof DeadlineTimerEvent) {
      // called from handleDeadline - prev. allocation timed out
      // we'll want to re-allocate if possible, rescinding the previous
      // allocation and deadline
      // if not possible, publish a failure Disposition whose reason is DeadlineExceeded
      deadlineI = (DeadlineTimerEvent)parent;
      allocI = (Allocation)deadlineI.getRegarding();
    } else if (parent instanceof Allocation) {
      // called from handleAllocation - Allocation failed before the deadline went off
      // We'll want to re-allocate if possible, rescinding the previous
      // allocation
      // if we're out of time, publish a Disposition that says so and rescind prev.
      // if there are more rules, pub new alloc, deadline and rescind prev.
      // Else rescind prev, pub a failure Disposition whose reason
      // is same as that on the old Allocation
      allocI = (Allocation)parent;
      deadlineI = null;
    } else {
      // Initial allocation of this Task
      // We'll want to create an allocation and deadline if possible
      // If no time available, publish a Disposition that says so
      // if no matching rules, publish a Disposition that says so
    }

    long taskDeadline = getTaskDeadline(task);

    // Do an extra little check to see if the Task is already late
    // make sure the deadline has not been exceeded
    if (taskDeadline <= currTime) {
      // Task deadline is already past the current time!
      if (log.isApplicable(log.VERBOSE)) {
        log.log(this, log.VERBOSE,
		(myId+" unable to allocate task: "+task.getUID()+
		 "\ndue to deadline ("+taskDeadline+" <= "+
		 currTime+")"));
      }
      int[] aspect_types = {AspectType.END_TIME};
      double[] results = {(double)getTaskDeadline(task)};
      
      if (allocI != null) {
	rescindAllocation(allocI);
      }
      if (deadlineI != null) {
	rescindDeadlineTimerEvent(deadlineI);
      }
      // Should this reason instead be the failure reason on the previous
      // allocation, if there was one?
      // FIXME!!!
      //      publishFailureDisposition(Constants.Rating.FAIL_DEADLINE, aspect_types, results, task);
      String reason = Constants.Rating.FAIL_DEADLINE;
      if (deadlineI == null) {
	if (allocI != null) {
	  AllocationResult repAR = allocI.getReportedResult();
	  if (repAR != null) {
	    reason = repAR.auxiliaryQuery(AuxiliaryQueryType.FAILURE_REASON);
	  }
	  if (reason.equals(Constants.Rating.SUCCESS)) 
	    reason = Constants.Rating.FAIL_DEADLINE;
	  if (reason == null)
	    reason = "(no reason given)";
	}
      }
      publishFailureDisposition(reason, aspect_types, results, task);
      return false;
    } // end of check if Task already late

    // find a matching rule
    int matchingRuleIndex = startingRuleIndex;
    String text = task.getVerb().toString();
    String failureReason = null;  // Reason to put in failure area
    int[] aspect_types = new int[1];; // Aspects to specify
    double[] results = new double[1]; // and the values

    // Loop over available rules
    while (true) {
      if (++matchingRuleIndex >= expandedRules.length) {
	haveAllocatedPastLocals = true;
        // no more rules apply
        if (log.isApplicable(log.VERBOSE)) {
	  if (expandedRules.length == 0) {
	    log.log(this, log.VERBOSE, "allocate: " + myId + " couldn't allocate cause have no expanded rules at all!");
	  } else if (startingRuleIndex < 0) {
	    log.log(this, log.VERBOSE,
		  (myId+" unable to allocate task: "+task.getUID()+
		   "\ndue to no rule at all in " +
		   expandedRules.length+" matching \""+
		   text+"\""));
	  } else {
	    log.log(this, log.VERBOSE,
		  (myId+" unable to allocate task: "+task.getUID()+
		   "\ndue to no rule remaining from index "+(startingRuleIndex + 1)+" to "+
		   expandedRules.length+" matching \""+
		   text+"\""));
	  }
        }

//  	if (log.isApplicable(log.VERBOSE)) {
//  	  String par;
//  	  if (parent == null) {
//  	    par = "(none)";
//  	  } else {
//  	    par = parent.toString();
//  	  }
//  	  log.log(this, log.VERBOSE, "allocate:" + this + ":parent = " + par);
//  	}
 
        if (parent instanceof DeadlineTimerEvent) {
	  // We ran out of time
	  aspect_types[0] = AspectType.END_TIME;
	  results[0] = (double)getTaskDeadline(task);
	  failureReason = Constants.Rating.FAIL_DEADLINE;
        } else if (parent instanceof Allocation) {
	  // give same reasone as in allocation
	  AllocationResult ar = allocI.getReportedResult();
	  // get out the aspect_types, values, and failureReason
	  aspect_types = ar.getAspectTypes();
	  results = ar.getResult();
	  failureReason = ar.auxiliaryQuery(AuxiliaryQueryType.FAILURE_REASON);
	} else {
	  // Never was a matching rule
	  // FIXME!!! Is this the right aspect_type and results?
	  aspect_types[0] = AspectType.UNDEFINED;
	  results[0] = (double)0;
	  failureReason = Constants.Rating.FAIL_ALLOCATION;
	}
	if (allocI != null) {
	  rescindAllocation(allocI);
	}
	if (deadlineI != null) {
	  rescindDeadlineTimerEvent(deadlineI);
	}
	publishFailureDisposition(failureReason, aspect_types, results, task);
	
        return false;
      } // end of block to handle no more matching rules

      // OK, got a rule to look at
      // examine this rule[i]
      // We must see if this rule matches
      String ruleTask = expandedRules[matchingRuleIndex].getTask();
      if (ruleTask.equals(text)) {
        // found matching rule -- use it below
        break;
      }
    } // end of while(true) loop over available rules

    // found a matching rule, expandedRules[matchingRuleIndex]
    // 
    // check if asset is available?  currently we (blindly)
    //   attempt the allocation.
    Asset allocAsset = 
      expandedRules[matchingRuleIndex].getAsset();

    // Now calculate the deadlines.  If they've passed, we'll
    // handle that specially
    
    // There are 2 deadlines: 
    //   1) "deadlineTime" by which, if the allocator has not heard 
    //      anything else, it will assume that this allocation failed.  
    //      That is the time for the DeadlineTimerEvent.
    //   2) "taskDeadlineTime" is the deadline included in the new 
    //      Task, to instruct the other Agent to finish by that time, 
    //      giving the answer enough time to get back to this allocator, 
    //      and perhaps allow this allocator time to re-allocate this 
    //      Task.

    
    // FIXME!!! Since we're not reducing the deadline for future
    // re-allocations, should I just
    // always use the taskDeadline retrieved above?
    // Accomplish roughly the same thing by saying we'll always
    // use the simple deadline method
    this.useSimpleDeadline = true;
    // FIXME!!! Change this later!!!!
    
    long deadlineTime =
      calculateDeadlineTime(
			    currTime,
			    task,
			    allocAsset,
			    matchingRuleIndex);

    // Also reduce the deadline for completing the new task
    //   to account for time for a remote allocation to transfer
    //   over the wire.  Local allocations do not incur this cost.
    long taskDeadlineTime; // deadline for new task
    long fastResponseTime; // quickest we could possibly respond by

    if (allocAsset instanceof LocalAsset) {
      // this is an allocation to a local asset
      taskDeadlineTime = deadlineTime;
      // The "fastest" response for an allocation to a local asset:
      //    (local-ALLOC + executor-response)
      // which we'll estimate down to just (local-Alloc) = allocPubDelay
      fastResponseTime = 
	currTime + 
	this.allocPubDelay;
    } else {
      haveAllocatedPastLocals = true;
      // this is an allocation to an Organization
      taskDeadlineTime = deadlineTime - this.transDelay;
      // Calculation the expected "fastest" response for an 
      //   allocation to the organization:  
      //     (org-ALLOC + TRANS-out + remote-local-ALLOC + TRANS-back)
      //     = (allocPubDelay + transDelay + taskRespPubDelay + transDelay)
      //     = (allocPubDelay + 2*transDelay + taskRespPubDelay)
      fastResponseTime = 
	currTime + 
	(this.allocPubDelay + 
	 (2 * this.transDelay) + 
	 this.taskRespPubDelay);
    }
      
    // make sure deadline is reasonable
    // HACK!!! Lets not fail tasks with deadline exceeded
    // on this account just now.
    // FIXME!!!!
    if (false && taskDeadlineTime <= fastResponseTime) {
      // The deadlines are too soon!  There's no way they can 
      //   be satisfied, so don't try.
      //
      // Note that we used the guess-timated travel times, so this 
      //   may fail tasks that might have succeeded.
      if (log.isApplicable(log.VERBOSE)) {
        log.log(this, log.VERBOSE, 
		"Unable to allocate task cause calculated "+
		"deadline too soon");
      }
      aspect_types[0] = AspectType.END_TIME;
      results[0] = (double)taskDeadlineTime;
      failureReason = Constants.Rating.FAIL_DEADLINE;
      if (allocI != null) {
	rescindAllocation(allocI);
      }
      if (deadlineI != null) {
	rescindDeadlineTimerEvent(deadlineI);
      }
      publishFailureDisposition(failureReason, aspect_types, results, task);
      return false;
    }
    // If get here the calculated deadlines are OK on this new matching rule

    // Rescind any old objects
    if (allocI != null) {
      rescindAllocation(allocI);
    }
    if (deadlineI != null) {
      rescindDeadlineTimerEvent(deadlineI);
    }
    
    // generate an allocation
    publishAllocation(task, matchingRuleIndex, deadlineTime, taskDeadlineTime);
    return true;
  } // end of allocate()
  
  /**
   * Get the END_TIME maximum allowed value from the Task.
   * Hack: Allocation "knows" this is the PreferredValue for the END_TIME aspect
   *
   * @param task a <code>Task</code> whose deadline to find
   * @return a <code>long</code> time by which the Task must be performed
   */
  private long getTaskDeadline(Task task) {
    // The deadline is the END_TIME AspectType
    // or the BEST value on the scoring function on the END_TIME AspectType
    return (long)task.getPreferredValue(AspectType.END_TIME);
  }

  /**
   * Retrieve the index of the rule used in generating the given Allocation.
   *
   * @param alloc an <code>Allocation</code> whose rule index to retrieve
   * @return an <code>int</code> index of the last allocation rule index used
   */
  private int getAllocRuleIndex(Allocation alloc) {
    int index = 0;
    // get out the Rule Index from the Allocation
    // it is an Annotation on the Allocation
    // use items of class MyAnnot
    Annotation annot = alloc.getAnnotation();
    if (annot instanceof MyAnnot) {
      index = ((MyAnnot)annot).getRuleIndex();
    }
    return index;
  }
  
  /**
   * Set the rule index in the Allocation annotation
   * Do this usually when re-setting the index as a marker
   *
   * @param alloc an <code>Allocation</code> to mark
   * @param index an <code>int</code> allocation rule index to save
   */
  private void setAllocRuleIndex(Allocation alloc, int index) {
    // get out the Rule Index from the Allocation
    // it is an Annotation on the Allocation
    // use items of class MyAnnot
    Annotation annot = alloc.getAnnotation();
    if (annot instanceof MyAnnot) {
      ((MyAnnot)annot).setIndex(index);
    }
  }
  
  /**
   * Compute the "timeout" deadline for this allocation attempt.
   *
   * @param currTime current simulation time (<tt>currentTimeMillis()</tt>)
   * @param task Task being allocated
   * @param allocAsset asset being allocated to
   * @param matchingRuleIndex index in <tt>expandedRules</tt> that is
   *    being used for this allocation
   *
   * @return timestamp for "wakeup" (DeadlineTimerEvent)
   */
  private final long calculateDeadlineTime(
      final long currTime,
      final Task task,
      final Asset allocAsset,
      final int matchingRuleIndex) {

    long deadlineTime;
    long taskDeadline = getTaskDeadline(task);

    // The Simple deadline calculation model gives each
    //   possible allocation all of the remaining time.
    // Unfortunately, this means that an un-reachable Node,
    //   for example, will result in a completely failed Task,
    //   when simply trying the next rule in this Allocator's list
    //   might have given success.
    // The second, richer model tries a (still simple) method
    //   to allow some chance of re-allocating a timed out attempt
    if ((this.useSimpleDeadline) ||
        (allocAsset instanceof LocalAsset)) {
      // Give local assets as much time as they want
      //
      // Reduce the deadline for this step, to give
      //   the Allocator time to report its result up the chain.
      // Note that the "useSimpleDeadline" won't allow time to
      //   re-allocate if the first allocation is to an OrgAsset
      //   and doesn't respond
      deadlineTime = taskDeadline - taskRespPubDelay;
    } else {
      // Give the next rule half the remaining time, or at least 
      //   some minimum amount
      // Boundary cases:  
      // always give any local rule all the time (less the minimum to 
      //   report back)
      // If there is only one Org rule remaining, give it all the 
      //   (remaining) time (less...)
      // If there are 2 Org rules left, give the next rule all the 
      //   remaining time, less the minimum
      // for the last rule to have a chance, unless that gives the 
      //   first rule less than the minimum
      // If there are 3 Org rules left, give the next half the 
      //   remaining, and at least the minimum.  And on like this.

      // what kind of rule the next rule is, 
      //   (if allocAsset instanceof LocalAsset) ....

      // Go through the possibilities.
      // Generally LocalAssets get as much time as possible
      // OrgAssets get some minimum amount, and we try to leave time
      // for all the assets to have a go
      // But of the remaining OrgAssets, the first gets at least half 
      //   the remining time

      // Need to consider: 
      //   task.getDeadline(),
      //   Minimum time to do a task remotely (which is time to pub 
      //     an allocation,
      //   have it turned into an allocation on a remote host,
      //   have the remote asset perform it, and have the answer get 
      //     back to this allocator)
      //   That is, this is a guess that any deadline set for 
      //     this allocator earlier than now + this can never be 
      //     satisfied

      // Number of remaining matching rules that allocate to an OrgAsset
      int numOrgRules = 
        countOrgRules(matchingRuleIndex, task.getVerb().toString());

      // Amount of time betwen now and when the AllocationResult 
      //   must be visible
      // That is, if a result is visible at now + remaining, we are OK
      long remaining = 
        taskDeadline - this.taskRespPubDelay - currTime;

      if (numOrgRules == 1) {
        // Give this single remining task all the time possible
        deadlineTime = taskDeadline - this.taskRespPubDelay;
      } else if (remaining <= this.minAllocTime) {
        // there is (hardly) enough time for one OrgRule to finish.
        // give it all the remaining time
        deadlineTime = taskDeadline - this.taskRespPubDelay;
      } else if (numOrgRules == 2) {
        // Give the first rule all the remaining, less some minimum
        // amount for the last rule, ensuring that that it gets
        // at least enough for one rule
        long diff = 
          Math.max(remaining - this.minAllocTime, this.minAllocTime);
        deadlineTime = currTime + diff;
      } else {
        // give the next rule half the remaining time, and at least 
        //   the minimum needed
        long diff = Math.max(remaining / 2, this.minAllocTime);
        deadlineTime = currTime + diff;
      }
    }
    return deadlineTime;
  } // end of calculateDeadlineTime

  /**
   * Publish an <code>Allocation</code> and a corresponding 
   * <code>DeadlineTimerEvent</code> to wake this allocator if the 
   * response to the allocation is late or never arrives.
   *
   * @param task Task being allocated
   * @param LocalEvent optional non-null non-task LocalEvent that caused 
   *    this allocation attempt (e.g. a past allocation failure)
   * @param matchingRuleIndex index in <tt>expandedRules</tt> that is
   *    being used for this allocation
   * @param deadlineTime time for wakeup event, allowing this allocator
   *    enough time to generate a "timeout" response
   * @param taskDeadlineTime if this is an allocation to an OrgAsset, this
   *    is the latest time the remote agent should generate it's response
   *    and expect this allocator to receive it before the timeout
   */
  private final void publishAllocation(
      final Task task,
      final int matchingRuleIndex,
      final long deadlineTime,
      final long taskDeadlineTime) {

    // set the new deadline for the resulting task, if this is an 
    //   allocation to a remote Org.  (for local allocations, this 
    //   is meaningless)

    AllocationResult estAR = null;

    // !!! FIXME!!!
    // How do I know which aspects to use?
    // Are these the right values to guess?
    int []aspect_types = {AspectType.END_TIME};
    double []results = {(double)(taskDeadlineTime)};

    // For now, leave the estimate blank so the customer
    // can deal with it.
    // Later should set it and have customer handle low-confidence results
    //estAR = theLDMF.newAllocationResult(0.0, true, aspect_types, results);
    
    // FIXME: Is that the correct Role to use?
    Allocation alloc =
      theLDMF.createAllocation(task.getPlan(), task, 
		     expandedRules[matchingRuleIndex].getAsset(), estAR, Role.ASSIGNED);
    
    // Annotate this allocation with myPlugInID and the rule index
    alloc.setAnnotation(new MyAnnot(super.toString(), matchingRuleIndex));

    // FIXME!!! At some point, must set the taskDeadlineTime here!
    // perhaps by putting it in the Annotation
    // Then must also modify code elsewhere in this class to reset the END_TIME aspect on
    // the Task to the taskDeadlineTime, and try to meet that deadline, right?

    // setTaskDeadline(alloc, taskDeadlineTime);
    
    publishAddAfter(alloc, this.allocPubDelay);

    // also generate the deadline-exceeded event
    NewDeadlineTimerEvent deadlineE =
      theCSMARTF.newDeadlineTimerEvent();
    deadlineE.setRegarding(alloc);
    deadlineE.setTime(deadlineTime);
    deadlineE.setPublisher(super.toString());
    deadlineE.setSource(getAgentIdentifier());
    publishAddAt(deadlineE, deadlineTime);

    // done handling task
    if (log.isApplicable(log.VERBOSE)) {
      log.log(this, log.VERBOSE,
          (myId+" allocated task: "+task.getUID()+
           "\nto asset: "+alloc.getAsset().getUID()+
	   "\nusing new Allocation " + alloc.getUID()+
           "\nvia rule["+matchingRuleIndex+"]: "+
           expandedRules[matchingRuleIndex]));
    }
  } // end of publishAllocation

  // For a deadline passing us by, we must create a new
  // AllocationResult with some high (1?) confidence, saying it failed
  // and attaching the appropriate RatingDescription as AuxiliaryQueryInfo
  // Then set it as the EstimatedResult on the Allocation
  // and publishChange the Allocation
  
  /**
   * Immediately fail a task - for example, because no rule matches.
   * @param reason a <code>String</code> why the Task failed
   * @param aspect_types an <code>int[]</code> array of aspects
   * @param results a <code>double[]</code> array of aspect values
   * @param task a <code>Task</code> to fail
   */
  private void publishFailureDisposition(
					 String reason,
					 int[] aspect_types,
					 double[] results,
					 Task task) {
    AllocationResult estAR = null;

    // Should this only include the Aspects on which this failed?
    // All? How do I know which are relevant?
    // FIXME!!!!
    estAR = theLDMF.newAllocationResult(1.0, false, aspect_types, results);
    estAR.addAuxiliaryQueryInfo(AuxiliaryQueryType.FAILURE_REASON, reason);
    
    Disposition disp = theLDMF.createFailedDisposition(task.getPlan(), task, estAR);

    publishAddAfter(disp, this.taskRespPubDelay);
    if (log.isApplicable(log.VERBOSE)) {
      log.log(this, log.VERBOSE, "publishFailureDisposition: " + this + " publishing failure: " + disp);
    }
  }

  // Given a starting rule index, return the count of matching rules
  // where the asset is an OrgAsset, including the starting index
  private final int countOrgRules(
      final int startIndex, 
      final String taskDesc) {
    int matches = 0;
    for (int i = startIndex; i < expandedRules.length; i++) {
      String ruleTextI = expandedRules[i].getTask();
      if ((ruleTextI.equals(taskDesc))) { // &&
	// the rule appears to match
	if (expandedRules[i].getAsset().hasEntityPG()) {
	  // and is an org
	  matches++;
	}
      }
    }
    return matches;
  } // end of countOrgRules

  ///////////////
  // Helper classes follow
  //////////////

  /**
   * Helper class -- a "template" Rule.
   *
   * This is the basic rule as read from the configuration file,
   * but not associated with any runtime information.
   */
  private static class TemplateRule {
    /**
     * Task matching the Task.
     */
    private final String task;

    /**
     * String for the single required asset role, or a Collection of 
     *   Strings for multiple (UNION) required roles.
     */
    private final Object assetRoles;

    public TemplateRule(String task, Object assetRoles) {
      this.task = task;
      this.assetRoles = assetRoles;
      // check arguments
      if (task == null) {
        throw new IllegalArgumentException(
            "Expecting non-null \"task\"");
      } else if (assetRoles instanceof String) {
        // okay
      } else if (assetRoles instanceof Collection) {
        // make sure all are Strings
        Iterator iter = ((Collection)assetRoles).iterator();
        while (iter.hasNext()) {
          Object oi = iter.next();
          if (!(oi instanceof String)) {
            throw new IllegalArgumentException(
                "Collection of \"assetRoles\" contains non-String \"" +
                ((oi != null) ? oi.getClass().getName() : "null") +
                "\"");
          }
        }
      } else {
        throw new IllegalArgumentException(
            "Expecting String or Collection of \"assetRoles\", not \"" +
            ((assetRoles != null) ? assetRoles.getClass().getName() : "null") +
            "\"");
      }
    }

    /**
     * Simply clone the <tt>other</tt>.
     */
    protected TemplateRule(TemplateRule other) {
      this.task = other.task;
      this.assetRoles = other.assetRoles;
    }

    public String getTask() {
      return task;
    }

    public Object getAssetRoles() {
      return assetRoles;
    }

    /**
     * See if this <code>TemplateRule</code>'s asset roles matches the given
     * <code>Asset</code>'s <code>List</code> of roles.<br>
     * All of the roles in this rule must be present in an Asset for
     * it to be capable of performing the given task.
     * @param asset an <code>Asset</code> to attempt to match
     * @return a <code>boolean</code>, true if the asset could perform 
     *   this rule's Task
     */
    public boolean matches(Asset asset) {
      // If its an ABC LocalAsset, treat it separately
      if (asset instanceof LocalAsset) {
	LocalAsset lasset = (LocalAsset)asset;
	//System.out.println("TESTING- got a local Asset to match: " + lasset);
	RolesPG rolesPG = 
	  ((lasset != null) ?  lasset.getRolesPG() : null);
	if (rolesPG == null) {
	  // no roles?
	  return false;
	}
	// Our asset my have only one role
	if (this.assetRoles instanceof String) {
	  String myRole = (String)this.assetRoles;
	  // NOTE Next line synchronizes on an internal Role lock
	  if (rolesPG.inRoles(Role.getRole(myRole))) {
	    // rolesPG has a Role that matches!
	    return true;
	  } else {
	    // If the Asset does not have my needed role, it cant help
	    return false;
	  }
	} else {
	  // The asset has a collection of (Strings) roles
	  Collection myRoles = (Collection)this.assetRoles;
	  // see if asset has ALL of my roles
	  for (Iterator myRolesIter = myRoles.iterator();
	       myRolesIter.hasNext();
	       ) {
	    String myRoleI = (String)myRolesIter.next();
	    // NOTE Next line synchronizes on an internal Role lock
	    if (! rolesPG.inRoles(Role.getRole(myRoleI))) {
	      // missing one of my roles
	      return false;
	    }
	  } // finished checking all my roles
	  return true;
	} // finished case of collection of roles
      } else if (asset instanceof Entity) {
	// This is some sort of Organization asset
	Entity entity = (Entity)asset;
	// must avoid self org
  	if (entity.isLocal()) {
  	  // this Entity resides on the local Agent (there may be many)
  	  // no sense allocating to it, since I'll then just
  	  // deal with one of the local assets I've already been through
  	  return false;
  	}	
	// is this the right way to do this?
	EntityPG rolesPG = entity.getEntityPG();
	if (rolesPG == null)
	  return false;
	
	// Our asset my have only one role
	if (this.assetRoles instanceof String) {
	  String myRole = (String)this.assetRoles;
	  // See if the asset contains a matching role
	  // NOTE Next line synchronizes on an internal Role lock
	  if (rolesPG.inRoles(Role.getRole(myRole))) {
	    // rolesPG has a Role that matches!
	    return true;
	  } else {
	    return false;
	  }
	} else {
	  // The asset has a collection of (Strings) roles
	  Collection myRoles = (Collection)this.assetRoles;
	  // see if asset has ALL of my roles
	  for (Iterator myRolesIter = myRoles.iterator();
	       myRolesIter.hasNext();
	       ) {
	    String myRoleI = (String)myRolesIter.next();
	  // NOTE Next line synchronizes on an internal Role lock
	    if (! rolesPG.inRoles(Role.getRole(myRoleI))) {
	      // missing one of my roles
	      return false;
	    }
	  } // finished checking all my roles
	  return true;
	} // finished case of collection of roles
      } else {
	// what is this?
	return false;
      }
    } // end of matches(Asset) method

    public String toString() {
      // could cache this "final" value
      return
        "TemplateRule {"+
        "\n  task: "+getTask()+
        "\n  roles: "+getAssetRoles()+
        "\n}";
    }
  } // end of TemplateRule definition

  /**
   * Helper class -- an "expanded" Rule.
   *
   * An <code>ExpandedRule</code> is just a <code>TemplateRule</code>
   * that has been matched at runtime with a <code>Asset</code>.  A
   * single TemplateRule might expand to many ExpandedRules, but an
   * ExpandedRule points to only one Asset.
   *
   * Here we simply extend TemplateRule, but an equivalent implementation
   * could point to the original TemplateRule.
   */
  private static class ExpandedRule 
    extends TemplateRule {

      /**
       * Runtime "bound" Asset that matches the <tt>assetRoles</tt>.
       */
      private final Asset asset;

      public ExpandedRule(
          String task, Object assetRoles, Asset asset) {
        super(task, assetRoles);
        this.asset = asset;
      }

      public ExpandedRule(
          TemplateRule tRule, Asset asset) {
        super(tRule);
        this.asset = asset;
      }

      public Asset getAsset() {
        return asset;
      }

      public String toString() {
        // could cache this "final" value
        return
          "ExpandedRule {"+
          "\n  task: "+getTask()+
          "\n  roles: "+getAssetRoles()+
          "\n  asset: "+getAsset()+
          "\n}";
      }
    } // end of ExpandedRule definition

  /**
   * Simple implementation of Annotation to hold a String
   * for use to hold this PlugInID on Allocations
   * and an int to hold the RuleIndex
   *
   * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
   */
  static class MyAnnot implements Annotation {
    String annot = null;
    int index = 0;
    MyAnnot(String annotation) {
      annot = annotation;
    }

    MyAnnot(String annotation, int index) {
      annot = annotation;
      this.index = index;
    }

    public void setIndex(int index) {
      this.index = index;
    }

    public String getPlugInID() {
      return annot;
    }

    public int getRuleIndex() {
      return index;
    }
  }

  /**
   * Sort Assets by their list of roles
   * <p>
   * If you want LocalAssets before Organizations then you should
   * split the Collection of both into two separate Collections and
   * call this method for each, then merge the Lists (via "List.addAll");<br>
   * Note that this handles <code>LocalAsset</code>s or <code>Entity</code>s only
   */
  public static List sortAssets(Collection c) {
    // given Collection of Assets
    int csize = ((c != null) ? c.size() : 0);
    // make an empty List "l" that will hold ComparableRolesWrappers
    List l = new ArrayList(csize);
    // for each Asset...
    Iterator citer = c.iterator();
    for (int i = 0; i < csize; i++) {
      // get Asset[i]
      Asset ai = (Asset)citer.next();
      // get the collection of roles for the Asset
      Collection ci = null;
      if (ai instanceof LocalAsset) {
	RolesPG rpg = ((LocalAsset)ai).getRolesPG();
	ci = (rpg != null ? rpg.getRoles() : new ArrayList(0));
      } else if (ai instanceof Entity) {
	EntityPG epg = ((Entity)ai).getEntityPG();
	ci = (epg != null ? epg.getRoles() : new ArrayList(0)); 
      } else {
	ci = new ArrayList(0);
      }
      // wrap
      l.add(new ComparableRolesWrapper(ai, ci));
    }
    // sort using the wrapper's "compareTo"
    Collections.sort(l);
    // remove the wrappers
    for (int i = 0; i < csize; i++) {
      ComparableRolesWrapper crw = (ComparableRolesWrapper)l.get(i);
      l.set(i, crw.getAsset());
    }
    // return the sorted List of Assets
    return l;
  } // end of sortAssets method
    
  /**
   * A Comparable that sorts List of Role "roles"
   * @see #sortAssets
   */
  static class ComparableRolesWrapper implements Comparable {
    private final Object a;
    private final List l;

    public ComparableRolesWrapper(Object a, Collection c) {
      this.a = a;
      this.l = sortRoles(c);
    }

    public Object getAsset() {
      return a;
    }

    public int compareTo(Object o2) {
      List l2 = ((ComparableRolesWrapper)o2).l;
      return compare(this.l, l2);
    }

    /**
     * @param c a <code>Collection</code> of <code>Role</code>s
     * @return a <code>List</code> of same objects, sorted
     */
    private static List sortRoles(Collection c) {
      int csize = ((c != null) ? c.size() : 0);
      List uniqueL = new ArrayList(csize);
      if (csize < 1) {
	// empty list
      } else if (csize == 1) {
	// single item
	uniqueL.add(
		    (Role)((c instanceof List) ?
			     (((List)c).get(0)) :
			     (c.iterator().next())));
      } else {
	// multiple items - sort and remove duplicates

	// Create a list of the String versions of the Roles.
	List l = new ArrayList(csize);
	for (Iterator iter = c.iterator(); iter.hasNext(); ) {
	  l.add(((Role)iter.next()).getName());
	}
	// Strings are comparable, so sort them
	Collections.sort(l);
	
	Role prev = null;
	for (int i = 0; i < csize; i++) {
	  Role si = Role.getRole((String)l.get(i));
	  if (!(si.equals(prev))) {
	    uniqueL.add(si);
	    prev = si;
	  }
	}
      }
      return uniqueL;
    } // end of sortRoles() method

    /**
     * Compare two lists of Roles by <br>
     * First, <code>String</code> compare on the roles' name<br>
     * Second, by length of the list of Roles (shorter is first)
     *
     * @param l1 a <code>List</code> of <code>Roles</code>s
     * @param l2 a <code>List</code> of <code>Roles</code>s
     * @return an <code>int</code>: 0 for equals, etc.
     */
    private static int compare(List l1, List l2) {
      int n1 = ((l1 != null) ? l1.size() : 0);
      int n2 = ((l2 != null) ? l2.size() : 0);
      int ndiff = (n1 - n2);
      int nmin = ((ndiff < 0) ? n1 : n2);
      for (int i = 0; i < nmin; i++) {
	String s1 = ((Role)l1.get(i)).getName();
	String s2 = ((Role)l2.get(i)).getName();
	int cmp = s1.compareTo(s2);
	if (cmp != 0) {
	  // string names of Roles define order
	  return cmp;
	}
      }
      // shorter list is "smallest"
      return ndiff;
    }
  } // end of ComparableRolesWrapper class

} // End of AllocatorPlugIn

//    For now, no new Task specific Deadlines on Allocation/Task.  Later Allocator must do an Expansion with one child Task with the different deadline, later maybe keep old failed tasks as a way to track state

/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.util.PlugInHelper;

import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;

import org.cougaar.util.UnaryPredicate;

import java.util.*;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * MetricsInitializerPlugIn : Launch a number of tasks and wait
 * for them to flow through the system, measuring the time and memory
 * expended. We can also open a back door to a controller process to
 * publish the outcome.
 *
 * Startup Procedure
 *
 * As organizations with MetricsProvider capability report in, we
 * send them an "AreYouReady" task. This task is intended to yield a
 * 100% confidence allocation result when all the Agents have
 * initialized.
 *
 * When 100% ready reponses have been received from a number of
 * MetricsProvider organizations that equals the numProviders
 * parameter, we send a "Start" task to them indicating that they
 * should begin collecting statistics.
 *
 * If sampleInterval is non-zero, then after every sampleInterval seconds
 * have been completed a statistics sample is taken by issuing a
 * "Sample" control task. 
 *
 * The 100% completion of the sample tasks causes a "Finish" task to be
 * sent which gathers the final statistics.
 *
 **/
public class MetricsInitializerPlugin 
  extends CSMARTPlugIn
  implements MetricsConstants

{


  // Numeric parameters
  private int numProviders = 0;
  private int sampleInterval = 0;
  private int startDelay = 0;
  private int maxNumSamples = 0;

  private long startTime;

  //  private int taskRate;
  //  private int messageBytesPerTask;

  static final int INITIAL   = 0;
  static final int WAITING   = 1;
  static final int STARTING  = 2;
  static final int RUNNING   = 3;
  static final int FINISHING = 4;
  static final int FINISHED  = 5;
  static final int SAMPLING  = 6;
  static final int SAMPLING_AND_DONE  = 7;

  // Have we started processing?
  int state = INITIAL;

  private HashSet activeControlTasks = new HashSet();
  private Alarm sampleTimer = null; // Times sampling
  private transient Logger log;

  HashMap metricsProviders = new HashMap();

  /**
   * Subscription to our subordinates. When all subordinates have
   * checked in, we begin the sampling.
   **/
  private IncrementalSubscription localAssets;
  private UnaryPredicate localPredicate = new UnaryPredicate() {
    public boolean execute (Object o) {
      return ((o instanceof Asset) &&
              (o instanceof HasRelationships) &&
              (((HasRelationships) o).isLocal()));
    }
  };

  protected void addMetricsProviders(HasRelationships localAsset) {
    Collection relationships = 
      localAsset.getRelationshipSchedule().getMatchingRelationships(Role_MetricsControlProvider);

    for (Iterator iterator = relationships.iterator();
         iterator.hasNext();) {
      Relationship relationship = (Relationship) iterator.next();
      HasRelationships metricsProvider = 
        localAsset.getRelationshipSchedule().getOther(relationship);
      metricsProviders.put(((Asset) metricsProvider).getKey(), 
                           metricsProvider);
    }
  }

  private IncrementalSubscription readyAllocations;
  private IncrementalSubscription startAllocations;
  private IncrementalSubscription sampleAllocations;
  private IncrementalSubscription finishAllocations;

  private static class ControlPredicate implements UnaryPredicate {
    

    private Verb verb;
    ControlPredicate(Verb verb) {
      this.verb = verb;
    }
    public boolean execute(Object o) {
      if (o instanceof Allocation) {
        Allocation alloc = (Allocation) o;
        Task task = alloc.getTask();
        return task.getVerb().equals(verb);
      }
      return false;
    }
  }
  
  public void setupSubscriptions() 
  {
    log = CSMART.createLogger("org.cougaar.tools.csmart.runtime.plugin");
    Vector params = getParameters() != null ? new Vector(getParameters()) : null;

    if ((params.size() < 2) ||
        (params.size() > 4)) {
      if (log.isDebugEnabled()) {
	log.debug("MetricsInitializerPlugin Usage: <numProviders> <sampleInterval> [<startDelay> [<maxNumSamples>]]");
      }
      return;
    }
      
    // How many Agents are doing measuring?
    // For each one, need an Entity to appear with the appropriate Role
    //    numProviders = Integer.parseInt((String) params.elementAt(3));
    numProviders = Integer.parseInt((String) params.elementAt(0));
    

    // Time in seconds between samples. If 0, do no sampling
    //    sampleInterval = Integer.parseInt((String) params.elementAt(5));
    sampleInterval = Integer.parseInt((String) params.elementAt(1));

    // Number of seconds to wait before starting the run.
    if (params.size() > 2) {
      startDelay = Integer.parseInt((String) params.elementAt(2));
    }

    if (params.size() > 3) {
      maxNumSamples = Integer.parseInt((String) params.elementAt(3));
    }
    if (log.isDebugEnabled()) {
      log.debug("MetricsInitializerPlugin: " +
                       " numProviders = " + numProviders +
                       ", sampleInterval = " +  sampleInterval +
                       ", startDelay = " + startDelay +
                       ", maxNumSamples = " + maxNumSamples);
    }
                       
    localAssets = (IncrementalSubscription) subscribe(localPredicate);
    // Verb.getVerb("Ready");
    readyAllocations  = (IncrementalSubscription) subscribe(new ControlPredicate(Verb_Ready));
    // Verb.getVerb("Start");
    startAllocations  = (IncrementalSubscription) subscribe(new ControlPredicate(Verb_Start));
    // Verb.getVerb("Sample");
    sampleAllocations  = (IncrementalSubscription) subscribe(new ControlPredicate(Verb_Sample));
    // Verb.getVerb("Finish");
    finishAllocations = (IncrementalSubscription) subscribe(new ControlPredicate(Verb_Finish));
  }

  /**
   * Execute plugin. We use a simple state machine to take us through
   * the steps needed. The only somewhat complicated part involves the
   * SAMPLING state. Periodically (every so many tasks), we can
   * initiate a statistics sample. The statistics sample is taken in a
   * fashion similar to all the other control steps: send a control
   * task and wait for everyone to respond. 
   **/
  public void execute()
  {
    switch (state) {
    case INITIAL:
      // No point in looking for metrics providers if there are no local
      // Assets
      if (localAssets.size() > 0) {
        for (Iterator iterator = localAssets.getCollection().iterator();
             iterator.hasNext();) {
          addMetricsProviders((HasRelationships) iterator.next());
        }
        if (checkProviders()) {
          sendControl(Verb_Ready);
          setCurrentState(WAITING);
        }
      }
      break;

    case WAITING:
      if (checkControlTasks(readyAllocations)) {
        sendControl(Verb_Start);
        setCurrentState(STARTING);
      }
      break;

    case STARTING:
      if (checkControlTasks(startAllocations)) {
        startTime = System.currentTimeMillis();

	startRunning();
	
        setCurrentState(RUNNING);

        startSampleTimer();
      }
      break;

    case SAMPLING:
      // Keep tabs on the society
      if (checkControlTasks(sampleAllocations)) {
	// OK, done with sampling. Go back to running
        setCurrentState(RUNNING);
      } else {
	// Only want to break and wait to be called again
	// if we are still in the SAMPLING state.
	// If we are in the RUNNING state now, we want
	// to check if we're done now, if the time has expired,
	// etc. So we fall through to the RUNNING case.
	break;
      }

    case RUNNING:
      // Without this next, there's no way to stop!
      // we'll never go to the "FINISHING" state
      if (doneRunning()) {
	finish();               // All root tasks have been sent,
	break;
      }

      // If it's time again, do another sample
      if (sampleTimer != null && sampleTimer.hasExpired()) {
        setCurrentState(SAMPLING);
        sendControl(Verb_Sample);
        startSampleTimer();     // Restart
        break;
      }
      break;

    case FINISHING:
      // the society is done running. Make sure we've done all our sampling
      if (checkControlTasks(finishAllocations)) {
        finishEverything(); // in Ray's version, this does a System.exit()
      }
      break;
    }
  }

  private void startRunning() {
    // Send out your root tasks here, for example.
    // So Statistics would do sendRootTasks()
  }
  
  private boolean doneRunning() {
    return (new Date().getTime() >= 
            (startTime + ((sampleInterval * 1000L) * maxNumSamples)));
  }
    
  private void startSampleTimer() {
    if (startDelay > 0) {
      sampleTimer = wakeAfterRealTime((startDelay * 1000L) + (sampleInterval * 1000L));
      startDelay = 0;
    } else if (sampleInterval > 0) {
      sampleTimer = wakeAfterRealTime(sampleInterval * 1000L);
    }
  }

  private void setCurrentState(int newState) {
    state = newState;
  }

  private void finish() {
    sendControl(Verb_Finish);
    setCurrentState(FINISHING);
  }
  
  private boolean checkProviders() {
    if (log.isDebugEnabled()) {
      log.info("Num providers: " + numProviders + 
                       " metricsProviders.size(): " + metricsProviders.size());
    }
    return (metricsProviders.size() >= numProviders);
  }

  private boolean checkControlTasks(IncrementalSubscription sub) {
    if (sub.hasChanged()) {
      PlugInHelper.updateAllocationResult(sub);
      return (checkControlTasks(sub.getAddedList()) ||
              checkControlTasks(sub.getChangedList()));
    }
    return false;
  }

  private boolean checkControlTasks(Enumeration elements) {
    while (elements.hasMoreElements()) {
      PlanElement pe = (PlanElement) elements.nextElement();
      AllocationResult estAR = pe.getEstimatedResult();
      Task task = pe.getTask();
      if (estAR != null) {
        if (estAR.getConfidenceRating() >= 1.0) {
          activeControlTasks.remove(task);
        }
      }
    }
    return activeControlTasks.isEmpty();
  }

  private void sendControl(Verb verb) {
    for (Iterator iterator = metricsProviders.values().iterator();
         iterator.hasNext();) {
      Asset metricsProvider = (Asset) iterator.next();
      NewTask task = theLDMF.newTask();
      task.setDirectObject(metricsProvider);
      task.setVerb(verb);
      task.setPlan(theLDMF.getRealityPlan());
      // Set null set of preferences
      Vector preferences = new Vector();
      // if it's a sample task
      // I could specify:
      // time to (start) sampling at (optional)
      // time between samples
      // number of samples

      // How would this be used? Realistically, you'd just send 1 sample task
      // with all that data included
      // So the time between samples and number of samples would be your initial
      // input parameters,
      // and whatever is doing the measuring would respond only when it had finished
      
      task.setPreferences(preferences.elements());
      activeControlTasks.add(task);
      publishAdd(task);
      if (!((HasRelationships)metricsProvider).isLocal()) {
        Allocation alloc = theLDMF.createAllocation(task.getPlan(), task, 
                                                    metricsProvider, null,
                                                    Role_MetricsControlProvider);
        publishAdd(alloc);
      }
    }
  }

  private void finishEverything() {
    if (log.isDebugEnabled()) {
      log.debug("All finished!");
    }
    //System.exit(0);
  }

  //////////////////////////////////////////////
  // Helper functions below here
  
  /** like super.wakeAfter() except always in real (wallclock) time.
   **/
  private Alarm wakeAfterRealTime(long delayTime) { 
    if (delayTime<=0) {
      if(log.isDebugEnabled()) {
        log.error("\nwakeAfterRealTime("+delayTime+") is in the past!");
      }
      Thread.dumpStack();
      delayTime=1000;
    }

    long absTime = System.currentTimeMillis()+delayTime;
    PluginAlarm pa = new PluginAlarm(absTime);
    alarmService.addRealTimeAlarm(pa);
    return pa;
  }

  /**
   * Helper class - a simple Alarm
   */
  class PluginAlarm implements Alarm {
    private long expiresAt;
    private boolean expired = false;

    public PluginAlarm (long expirationTime) {
      expiresAt = expirationTime;
    }
    public long getExpirationTime() { return expiresAt; }

    public synchronized void expire() {
      if (!expired) {
        expired = true;
        MetricsInitializerPlugin.this.blackboard.signalClientActivity();
      }
    }
    public boolean hasExpired() { return expired; }
    public synchronized boolean cancel() {
      boolean was = expired;
      expired=true;
      return was;
    }
    public String toString() {
      return "<PluginAlarm "+expiresAt+
        (expired?"(Expired) ":" ")+
        "for "+this.toString()+">";
    }
  } // end of PluginAlarm class definition
}

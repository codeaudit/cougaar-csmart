/* 
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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

import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;
import java.util.Enumeration;
import java.io.IOException;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.plugin.Annotation;
import org.cougaar.core.plugin.util.PluginHelper;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.predicate.PlanElementPredicate;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.tools.csmart.runtime.ldm.CSMARTFactory;
import org.cougaar.tools.csmart.runtime.ldm.asset.HappinessAsset;
import org.cougaar.tools.csmart.runtime.ldm.asset.HappinessPG;
import org.cougaar.tools.csmart.runtime.ldm.event.NewHappinessChangeEvent;
import org.cougaar.tools.csmart.runtime.ldm.plugin.customer.*;
import org.cougaar.tools.csmart.util.parser.TaskFileParser;

/**
 * A simple Customer - inject new <code>Task</code>s into the sytem,
 * and adjust a simple happiness metric based on the executed results. <br>
 * Task verbs, required completion times, and rate of insertion
 * are all specified in an initialization file. Time between new <code>Task</code>s
 * is modulated by a specified randomization amount.<br>
 *
 * @author <a href="mailto: ahelsing@bbn.com">Aaron Helsinger</a>
 */
public class CustomerPlugin extends CSMARTPlugin {

  /** Asset containing Happiness Info **/
  private HappinessAsset happiness;

  /** Map of all Tasks **/
  private HashMap tasks;

  /** Scenario Start Time **/
  private double startSIMTime;

  /** Scenario Stop Time **/
  private double stopSIMTime;

  /** These should be read in from a config file **/
  private double startingHappiness = 1.0;

  /** Subscription to Tasks **/
  private IncrementalSubscription peSub;
  private IncrementalSubscription taskSub;

  private static final String PROTO_NAME = "HAPPINESS_ASSET";

  private java.util.Random sharedRandom = new java.util.Random();

  private long pubDelay = 2000L;

  private PlanElementPredicate peP = new PlanElementPredicate() {
      public boolean execute(PlanElement o) {
	Task t = ((PlanElement)o).getTask();
	Annotation annot = t.getAnnotation();
	if(annot instanceof CustAnnot) {
	  String id = ((CustAnnot)annot).getPluginID();
	  if (! ((CustAnnot)annot).getHandled())
	    return (id.equals(getUID().toString()));
	}
	return false;
      }
    };

  // I need to subscribe to my own tasks so I can publish 
  // a new one when the currently published task becomes
  // visible.
  private UnaryPredicate taskP = new UnaryPredicate() {
      public boolean execute(Object o) {
	if(o instanceof Task) {
	  Annotation annot = ((Task)o).getAnnotation();
	  if(annot instanceof CustAnnot) {
	    String id = ((CustAnnot)annot).getPluginID();
	    return( id.equals(getUID().toString()));
	  }
	}
	return false;
      }
    };

  /**
   * Subscribe to <code>PlanElement</code>s that refer to <code>Task</code>s
   * I published, as well as those <code>Task</code>s.<br>
   * Create an <code>HappinessAsset</code> to track my state.<br>
   * Parse my config file to determine what <code>Task</code> <code>Verb</code>
   * to publish, how often, and with what specified<br>
   * <code>AspectType.END_TIME</code>.
   *
   */
  public void setupSubscriptions() {
    if (log.isDebugEnabled()) {
      log.debug("setupSubscriptions:" + this + ":Entering");
    }

    peSub = (IncrementalSubscription) subscribe(peP);
    taskSub = (IncrementalSubscription) subscribe(taskP);

    Asset proto = theLDMF.createPrototype(
		 "org.cougaar.tools.csmart.runtime.ldm.asset.HappinessAsset", PROTO_NAME);

    happiness = (HappinessAsset) theLDMF.createInstance(proto);

    HappinessPG hPG  = (HappinessPG) theLDMF.createPropertyGroup(HappinessPG.class);  
    hPG.setHappinessAt(currentTimeMillis(), startingHappiness);
    happiness.setHappinessPG(hPG);
    publishAdd(happiness);

    // Load in the config file.
    Vector pv = getParameters() != null ? new Vector(getParameters()) : null;
    if (pv == null || pv.size() < 3) {
      throw new RuntimeException(
				 "CustomerPlugin expects parameters, SIMStartTime, SIMStopTime and TaskFileName.");
    }

    try {
      startSIMTime = Long.parseLong((String)pv.elementAt(0));
      stopSIMTime = Long.parseLong((String)pv.elementAt(1));
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(
				 "CustomerPlugin unable to parse Sim start or stop: " + 
				 (String)pv.elementAt(0) + ", " + (String)pv.elementAt(1));
    } catch (ArrayIndexOutOfBoundsException aoe) {
      throw new RuntimeException("CustomerPlugin expects parameters: StartSIMTime, StopSIMTime, TaskFileName");
    }
 
    double timeDiff = (stopSIMTime - startSIMTime);
    if (timeDiff <= 0.0) {
      throw new RuntimeException(
  				 "SIM Stop time must be greater than Start Time");
    }
    double nowTime = (double)currentTimeMillis();
    if (startSIMTime < nowTime) {
      startSIMTime = nowTime;
      stopSIMTime = startSIMTime + timeDiff;
    }

    String taskFileName = (String)pv.elementAt(2);
    if (taskFileName == null) {
      throw new RuntimeException(
				 "CustomerPlugin expected param[0] to be taskFile name");
    }

    if (taskFileName.equals("none")) {
      // New CSMART config UI puts this in if the customer is not to publish any tasks
      // So we're done here....
      if (log.isDebugEnabled()) {
	log.debug("setupSubscriptions: " + this + " has no task file. Not publishing any tasks!");
      }
    } else {
      loadTasks(taskFileName);

      // Publish one of each task.
      Iterator iter = tasks.values().iterator();
      while(iter.hasNext()) {
	SingleCustomerTask ct = (SingleCustomerTask) iter.next();
	publishTask(ct);
      }
    }
    pubDelay = 0l;
  } // end of setupSubscriptions()
  
  /**
   * Publish a new <code>Task</code> of the given type,
   * to appear after the type's calculated interval, module
   * some randomized amount.<br>
   *
   * @param task a <code>SingleCustomerTask</code> value
   */
  private void publishTask(SingleCustomerTask task) {
    if (log.isDebugEnabled()) {
      log.debug("publishTask: " + this + " entered");
    }
    long pubTime;

    // get at the time
    long currentTime = currentTimeMillis();

//      if (log.isApplicable(log.VERY_VERBOSE)) {
//        log.log(this, log.VERY_VERBOSE, "publishTask:" + this + ": Current Time: " + currentTime);
//      }

    // Interpret task.getRate as avg number of requests per
    // 10000 MSec or sim-cycles.
    // Therefore, time between cycles is 10000/rate
    // double rate = 10000 / ((task.getRate() == 0) ? .001 : task.getRate());

    // Interpret task.getRate as avg number of requests over
    // length of the simulation
     double rate = 
       (stopSIMTime - startSIMTime) / 
       ((task.getRate() == 0) ? .001 : task.getRate());

    // But time between events cant be less than 1
    if (rate < 1) {
      rate = 1;
    }

//      if (log.isApplicable(log.VERY_VERBOSE)) {
//        log.log(this, log.VERY_VERBOSE, "publishTask: " + this + " about to get pubTime");
//      }
    pubTime = currentTime +
      (long)nextNormalSample(rate, (double)task.getDeviation());

    // Cannot publish things in the past or now
    if (pubTime <= currentTime) {
      pubTime = currentTime + 1;
    }
    
    pubTime += pubDelay;

    if (log.isDebugEnabled()) {
      log.debug("publishTask:" + this + ": Rate: " + rate +
	      ": pubTime: " + pubTime + " at curr time: " + currentTimeMillis());
    }

    if (pubTime > stopSIMTime) {
      if (log.isInfoEnabled()) {
	log.info(
		"publishTask: " + this + 
		" not publishing task cause pubTime(" + pubTime + 
		") > stopSimTime(" + stopSIMTime + ")");
      }
      return;
    }
    
    // Now create the new Task
    NewTask nTask = theLDMF.newTask();

    // the verb is task.getTaskType()
    nTask.setVerb(Verb.getVerb(task.getTaskType()));

    // Define the END_TIME Preference.
    ScoringFunction scorefcn =
      ScoringFunction.createStrictlyBetweenWithBestValues
      (new AspectValue(AspectType.START_TIME, pubTime),
       new AspectValue(AspectType.END_TIME, task.getDuration() + pubTime),
       new AspectValue(AspectType.END_TIME, (task.getDuration() + pubTime)));

    nTask.setPreference(theLDMF.newPreference(AspectType.END_TIME, scorefcn));

    // Create an annotation.  This is used for identification later.
    nTask.setAnnotation(new CustAnnot(getUID().toString()));

    // then publishAfter it with Wake Time == pubTime
    nTask.setPlan(theLDMF.getRealityPlan());
    if (log.isDebugEnabled()) {
      log.debug("publishTask: " + this + " about to publishAddAt new task: " + nTask);
    }
    publishAddAt(nTask, pubTime);

  } // end of publishTask

  /**
   * Parses the ini file containing the new tasks and creates a Map of all new tasks.
   * <br>
   *
   * @param Filename of the Tasks ini File.
   */
  private void loadTasks(String fileName) {
  
    Enumeration taskList;

    tasks = new HashMap();

    TaskFileParser tfp = new TaskFileParser();
    try {
      tfp.load(fileName);
      tfp.parse();
    } catch (IOException e) {
      if (log.isDebugEnabled()) {
	log.debug("loadTasks:" + this + ":Error parsing TaskFile");
      }
    }

    taskList = tfp.elements();

    while (taskList.hasMoreElements()) {
      SingleCustomerTask ct = (SingleCustomerTask) taskList.nextElement();
      if (this.tasks.containsKey(ct.getTaskType())) {
        throw new IllegalArgumentException("TaskType already exists");
      } else {
        this.tasks.put(ct.getTaskType(), ct);
      }
    }
  } // end of loadTasks


  /**
   * Take a sampling from a normal distribution with the specified
   * <tt>median</tt> and <tt>stdDev</tt>.
   * @return double, possibly negative
   */
  public double nextNormalSample(final double median, 
				 final double stdDev) {
    return (median + stdDev * sharedRandom.nextGaussian());
  }
  

  /**
   * Loop over changed <code>PlanElement</code>s - if the
   * <code>EstimatedResult</code> has changed and is of high
   * confidence, update happiness. <br>
   * Similarly on receiving a new <code>Disposition</code>.<br>
   * On getting a new <code>Task</code>, publish another
   * of the same <code>Verb</code>.<br>
   *
   */
  public void execute() {
    // Publish Tasks as read in at prescribed intervals
    // If one of my Tasks is changed, look at the AllocationResult
    // Based on it being Success or Failure, publish a HappinessChangedEvent
//      if (log.isApplicable(log.VERY_VERBOSE)) {
//        log.log(this, log.VERY_VERBOSE, "execute:" + this);
//      }

    Enumeration changedPEs = peSub.getChangedList();
    while(changedPEs.hasMoreElements()) {
      if(log.isDebugEnabled()) {
	log.debug("execute:" + this +
		": got changed PlanElement from Subscription");
      }

      PlanElement pe = (PlanElement) changedPEs.nextElement();

      AllocationResult estR = pe.getEstimatedResult();
      if(estR != null && estR.getConfidenceRating() > 0.9) {
//       if (PluginHelper.checkChangeReports(peSub.getChangeReports(pe), 
// 		             PlanElement.EstimatedResultChangeReport.class)) {

	// Make sure that this is the 
	// Estimated Result changed.  Update Customer Happiness.
	if(log.isDebugEnabled()) {
	  log.debug("execute:" + this +
		  ": got changed, high-conf EstimatedResult from PlanElement");
	}
	
	double currHappy = -1.0;
	float changeval = 0.0F;
	if (estR.isSuccess()) {
	  changeval = 1.0F;
	} else {
	  String reason = estR.auxiliaryQuery(AuxiliaryQueryType.FAILURE_REASON);
	  if (reason == null) 
	    reason = "(no reason given)";
	  if (log.isDebugEnabled()) {
	    log.debug("execute: " + this + " got failed task for reason: " + reason);
	  }
	}

	// Flag this as having been handled such that we dont
	// end up
	// a) process the EstR cause someone did publishChange(set the Reported)
	// b) wake up immediately due to publishedChange(EstR is set)
	CustAnnot annot = (CustAnnot)pe.getTask().getAnnotation();
	if (! annot.getHandled()) {
	  annot.setHandled();
	  currHappy = updateHappiness(changeval, pe, currHappy);
	}
      } else {
	if(log.isDebugEnabled()) {
	  log.debug("execute:" + this + ": EstimatedResult not high-conf on PlanElement");
	}
      }
    } // end of changedPEs while loop
//      if (log.isApplicable(log.VERY_VERBOSE)) {
//        log.log(this, log.VERY_VERBOSE, "execute: " + this + " done changedPEs while loop");
//      }

    // Must also look at new PlanElements - to handle Dispositions
    Enumeration newPEs = peSub.getAddedList();
    while(newPEs.hasMoreElements()) {
      PlanElement pe = (PlanElement)newPEs.nextElement();
      if (pe instanceof Disposition) {
	if (log.isDebugEnabled()) {
	  log.debug("execute: " + this + " got new Disposition");
	}
	// This is worth handling
	AllocationResult estR = pe.getEstimatedResult();
	if (estR != null && estR.getConfidenceRating() > 0.9) {
	  //       if (PluginHelper.checkChangeReports(peSub.getChangeReports(pe), 
	  // 		             PlanElement.EstimatedResultChangeReport.class)) {
	  
	  // Estimated Result changed.  Update Customer Happiness.
//   	  if(log.isApplicable(log.VERY_VERBOSE)) {
//  	    log.log(this, log.VERY_VERBOSE, "execute:" + this +
//  		    ": got set EstimatedResult from Disposition");
//  	  }
	  
	  double currHappy = -1.0;
	  float changeval = 0.0F;
	  if (estR.isSuccess()) {
	    changeval = 1.0F;
	  } else {
	    String reason = estR.auxiliaryQuery(AuxiliaryQueryType.FAILURE_REASON);
	    if (reason == null) 
	      reason = "(no reason given)";
	    if (log.isDebugEnabled()) {
	      log.debug("execute: " + this + " got failed task for reason: " + reason);
	    }
	  }
	  CustAnnot annot = (CustAnnot)pe.getTask().getAnnotation();
	  if (! annot.getHandled()) {
	    annot.setHandled();
	    currHappy = updateHappiness(changeval, pe, currHappy);
	  }
	} else {
	  // estR didn't change
	  if(log.isDebugEnabled()) {
	    log.debug("execute:" + this + ": EstimatedResult not set on Disposition");
	  }
	}
      } else {
	// don't bother -- not a Disposition
	continue;
      }
    } // end of newPEs while loop
 
    // Also, publish the next task, once a previous one is added to my list of incoming tasks
    Enumeration addedTasks = taskSub.getAddedList();
    while(addedTasks.hasMoreElements()) {
//        if(log.isApplicable(log.VERY_VERBOSE)) {
//  	log.log(this, log.VERY_VERBOSE, "execute:" + this +
//  		": got added Task from Subscription");
//        }

      Task t = (Task)addedTasks.nextElement();
      if (log.isDebugEnabled()) {
	log.debug("execute: " + this + " got added Task " + t.getUID() + " of Verb " + t.getVerb());
      }
      double now = (double)currentTimeMillis();
      if(now < stopSIMTime) {
//  	if(log.isApplicable(log.VERY_VERBOSE)) {
//  	  log.log(this, log.VERY_VERBOSE, "execute:" + this + ":\n" +
//  		  "[" + (long)now + " < " + (long)stopSIMTime + "]");
//  	}
	publishTask((SingleCustomerTask)tasks.get(t.getVerb().toString()));
      } else {
	if(log.isInfoEnabled()) {
	  log.info("execute: " + this + " not pubbing new Task cause " +
		  "[" + (long)now + " >= " + (long)stopSIMTime + "]");
	}
      }
    } // end of while loop over added Tasks
  } // end of execute()

  /**
   * Updates the customer happiness based on <code>AllocationResult</code>s and sends out a
   * <code>HappinessChangeEvent</code>.
   * <br>
   * Customer happiness is calculated with the following function:<br>
   * <code>newHappiness = (1 - x) * crntHappiness + s * x</code><br>
   * Where:<br>
   * &nbsp;&nbsp;&nbsp;x = (0..1) Vitality of the Task to customer overall happiness
   * <br>
   * &nbsp;&nbsp;&nbsp;s = (1/0) Value indicated if Task was a Success / Fail <br>
   * <br><br>
   * Vitality is specified in start time by the NewTask init file.
   * Customer happiness starts at a default value specified in 
   * <code>HappinessPG</code>
   * <br>
   * @param taskResponse - a 0 or 1 value indicating if the requested Task was
   * performed successfully.
   * @param pe - the <code>PlanElement</code> causing the change
   * @param currentHappiness level of Customer. If negative, re-retrieve the value.
   * @return a double, the new Happiness level of the Customer
   *
   */
  protected double updateHappiness(float taskResponse, PlanElement pe, 
                                   double currentHappiness) {

//      if (log.isApplicable(log.VERY_VERBOSE)) {
//        log.log(this, log.VERY_VERBOSE, "updateHappiness:" + this + 
//  	      "Entering: taskResponse = " + taskResponse);
//      }

    double newHappiness = 0.0;

    HappinessPG hPG = happiness.getHappinessPG();

//      if (log.isApplicable(log.VERY_VERBOSE)) {
//        log.log(this, log.VERY_VERBOSE, "updateHappiness: " + this + " got happinesspg");
//      }
    
    // If the incoming happiness level is negative, that means
    // we have not previously updated the happiness level during this execute cycle.
    // Retrieve the happiness level.
    // Otherwise, we already know the happiness level, as we just modified it.
    // Re-retrieving it would give the old value, which is incorrect
    if (currentHappiness < 0) {
      currentHappiness = hPG.getHappinessAt(currentTimeMillis());
    }

//      if (log.isApplicable(log.VERY_VERBOSE)) {
//        log.log(this, log.VERY_VERBOSE, "updateHappiness: " + this + " got current happiness");
//      }
    
    double vitality = 0.0;

    SingleCustomerTask st = 
      (SingleCustomerTask) tasks.get(pe.getTask().getVerb().toString());
    vitality = st.getVitality();

    if (log.isDebugEnabled()) {
      log.debug("updateHappiness:" + this + "(1 - " + vitality + 
	      ") * " + currentHappiness + " + " + taskResponse + " * " + vitality);
    }

    newHappiness = (1 - vitality) * currentHappiness + taskResponse * vitality;
 
//      if (log.isApplicable(log.DEBUG)) {
//        log.log(this, log.DEBUG, "updateHappiness:" + this + "New Happiness = " + newHappiness);
//      }

    long currentTime = currentTimeMillis();

//      if (log.isApplicable(log.VERY_VERBOSE)) {
//        log.log(this, log.VERY_VERBOSE, "updateHappiness:" + this + ": Current Time: " + 
//  	      currentTime);
//      }

    // Set the new happiness level to be visible in the future,
    // at the same time as the happiness change event announcing this asset change
    // These 2 should be kept in sync as much as possible, to avoid simulator errors
    // In addition, the meaning is that it takes a finit time for the customer to get
    // happier.
    hPG.setHappinessAt(currentTime, newHappiness);

    // Send a Happiness Change Event
    NewHappinessChangeEvent hce = theCSMARTF.newHappinessChangeEvent();

    hce.setRegarding(pe);
    hce.setRating(taskResponse);

    // Set the time that the Task was completed.  For now, we have no
    // better information than the time the Customer heard it was completed, so use that.
    hce.setTimeCompleted(currentTime);
    hce.setCurrentHappiness(newHappiness);

    // must fill in the Publisher and Source here
    hce.setSource(getAgentIdentifier());
    hce.setPublisher(this.toString());
    
    if (log.isDebugEnabled()) {
      log.debug("updateHappiness:" + this + ": Created Event "+hce);
    }

    publishAdd(hce);

    return newHappiness;
  } // end of updateHappiness()

  /**
   * Implementation of Annotation to hold a String
   * identifier used to determine that this customer
   * actually sent out this task.
   */
  class CustAnnot implements Annotation {

    String annotation = null;
    boolean handled = false;
    
    CustAnnot(String annotation) {
      this.annotation = annotation;
    }
    
    public String getPluginID() {
      return this.annotation;
    }
    public void setHandled() {
      handled = true;
    }
    public boolean getHandled() {
      return handled;
    }
  } // end of CustAnnot class

}// CustomerPlugin

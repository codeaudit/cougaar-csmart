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
package org.cougaar.tools.csmart.plugin;


import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;

import org.cougaar.core.society.MessageStatistics.Statistics;
import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.component.ServiceRevokedEvent;
import org.cougaar.core.mts.MessageStatisticsService;

import org.cougaar.util.UnaryPredicate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import org.cougaar.tools.scalability.performance.jni.CpuClock;

/**
 * Collect statistics on Agent operation.<br>
 * Driven by Control tasks. On Start task, intialize counters.<br>
 * On Sample, record all our statistics to (one line in) a file.<br>
 * On Finish, do a last sample and close the file.<br>
 * The file written is named <ClusterID>_results.txt.<br>
 * If provided, the first parameter indicated the directory
 * in which to write the file. By default, it is written
 * in the working directory.<br>
 * Statistics include counts of <code>PlanElement</code>s with high confidence
 * results associated with<code>Task</code>s of the given scalability verb.<br>
 * See the code for a complete set of collected statistics.<br>
 * @see CSMARTPlugIn
 * @see MetricsConstants
 */
public class MetricsPlugin 
  extends CSMARTPlugIn
  implements MetricsConstants
{
  public static final String RESULTS_FILENAME_SUFFIX = "_results.txt";
  private static final String DEFAULT_DIRECTORY = "."; // cwd
  private Role MetricsProviderRole = Role_MetricsProvider;
  private Asset dummyAsset;     // We assign the statistics tasks to this asset
  private ClusterIdentifier ourCluster;
  private long startTime;
  private long startCPU;
  private int completedPlanElementCount = 0;   // Count of completed plan elements for (manage tasks)
  private int startPlanElementCount = 0; // count since last stats record
  private Set completedPlanElements = new HashSet();
  private PrintWriter writer;
  private MessageStatisticsService messageStatsService = null;

  boolean started = false;
  
  static NumberFormat timeFormat = new DecimalFormat("0.000 seconds");
  static NumberFormat memoryFormat = new DecimalFormat("0.000 MBi");

  // This plugin only wants the statistics gathering tasks.
  private IncrementalSubscription myTasks;
  public UnaryPredicate myTasksPredicate = new UnaryPredicate() {
    public boolean execute(Object o) { 
      if (o instanceof Task) {
	Task task = (Task) o;
        Verb verb = task.getVerb();
	if (verb.equals(Verb_Start) || verb.equals(Verb_Finish) || verb.equals(Verb_Sample) || verb.equals(Verb_Ready)) {
	  Asset directObject = (Asset) task.getDirectObject();
	  if (directObject.hasClusterPG()) {
	    ClusterIdentifier theCluster =
	      directObject.getClusterPG().getClusterIdentifier();
	    if (theCluster.equals(ourCluster)) {
	      return true;
	    }
	  } else {
	    return false;
	  }
        }
      }
      return false;
    }
  };

  // Predicate looks for manage plan elements
  private IncrementalSubscription managePlanElements;
  private static UnaryPredicate managePlanElementsPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof PlanElement) {
        PlanElement pe = (PlanElement) o;
        Task task = pe.getTask();
	Verb verb = task.getVerb();
	// We want to count non-control tasks
	if (verb.equals(Verb_Start) || verb.equals(Verb_Finish) || verb.equals(Verb_Sample) || verb.equals(Verb_Ready)) {
	  return false;
	} else {
	  // This is not a control task. The Scalability stuff here checks for the Verb_Manage.
	  // But for us, this should be OK.
	  return true;
	}
      }
      return false;
    }
  };
          
  public void setupSubscriptions()
  {
    // Is this necessary?
    // This asset, in this plugin, is only used to be the place
    // control tasks are allocated to
    Asset prototype = getFactory().createPrototype(AbstractAsset.class, "Statistics");
    dummyAsset = getFactory().createInstance(prototype);
    publishAdd(dummyAsset);

    ourCluster = getClusterIdentifier();

    // Open up the file for the results
    String path = DEFAULT_DIRECTORY;
    Vector params = getParameters();
    if (params != null && ! params.isEmpty()) {
      path = ((! (params.elementAt(0) instanceof String)) ? path : (String)params.elementAt(0));
      // could do File.isDirectory() and File.canWrite() on this
    }
    // If the path doesn't end in the separator character, add one
    if (path.lastIndexOf(File.separatorChar) != path.length() - 1) {
      path = path + File.separator;
    }

    if (log.isApplicable(log.DEBUG)) {
      log.log(this, log.DEBUG, "Writing " + path + ourCluster + RESULTS_FILENAME_SUFFIX);
    }
    
    try {
      writer = new PrintWriter(new FileWriter(path + ourCluster + RESULTS_FILENAME_SUFFIX));
    } catch (IOException ioe) {
      if (log.isApplicable(log.SEVERE)) {
	log.log(this, log.SEVERE, "Fatal IOException opening statistics file: " + path + ourCluster + RESULTS_FILENAME_SUFFIX);
      }
      // should this really exit?
      //System.exit(1);
    }

    // Here I could loop over remaining parameters. For each of them, treat
    // it as a verb. Then create a complex predicate for
    // subscribing to PlanElements

    managePlanElements = (IncrementalSubscription) subscribe(managePlanElementsPredicate);
    myTasks = (IncrementalSubscription) subscribe(myTasksPredicate);

    //        startStatistics();
    messageStatsService = (MessageStatisticsService)
        getBindingSite().getServiceBroker().getService(this, MessageStatisticsService.class, 
                                                       new ServiceRevokedListener() {
                                                               public void serviceRevoked(ServiceRevokedEvent re) {
                                                                   if (MessageStatisticsService.class.equals(re.getService())){
                                                                       messageStatsService = null;
                                                                   }
                                                               }
                                                           }); 
    
  }

  // process statistics tasks and count completed planelements
  public void execute()
  {
    // Count up the PlanElements for our statistics
    if (managePlanElements.hasChanged()) {
      processPlanElements(managePlanElements.getAddedList());
      processPlanElements(managePlanElements.getChangedList());
      // remove from our cache of PEs any removed PEs
      removePlanElements(managePlanElements.getRemovedList());
    }
    
    // If we have any control tasks to handle, do so
    if (myTasks.hasChanged()) {
      processTasks(myTasks.getAddedList());
    }
    //sampleStatistics();
    //   finishStatistics();
  }

  // remove removed PEs from our collection
  private void removePlanElements(Enumeration e) {
    while (e.hasMoreElements()) {
      PlanElement pe = (PlanElement) e.nextElement();
      if (!completedPlanElements.remove(pe)) {
//          completedPlanElementCount++;       // Removed before we could count it.
      }
    }
  }

  /**
   * Count up PEs with high confidence Estimated ARs
   *
   * @param e an <code>Enumeration</code> of PlanElements
   */
  private void processPlanElements(Enumeration e) {
    while (e.hasMoreElements()) {
      PlanElement pe = (PlanElement) e.nextElement();
      AllocationResult estAR = pe.getEstimatedResult();
      if (estAR != null) {
        if (estAR.getConfidenceRating() >= 1.0) {
          if (!completedPlanElements.contains(pe)) {
            completedPlanElements.add(pe);
            completedPlanElementCount++;
          }
        }
      }
    }
  }

  /**
   * Call start/sample/finishStatistics as appropriate, and publish
   * a new Allocation with a high confidence AR in response.
   *
   * @param tasks an <code>Enumeration</code> of control Tasks
   */
  private void processTasks(Enumeration tasks) {
    while(tasks.hasMoreElements()) {
      Task task = (Task) tasks.nextElement();
      Verb verb = task.getVerb();
      if (verb.equals(Verb_Start)) {
        startStatistics();
      } else if (verb.equals(Verb_Sample)) {
        sampleStatistics();
      } else if (verb.equals(Verb_Finish)) {
        sampleStatistics();
        finishStatistics();
      } else if (verb.equals(Verb_Ready)) {
	// No need to do anything more. By simply responding to these,
	// the initializer PlugIn knows the agent is ready
	// so to keep the current functionality, where
	// the LDMPlugIn is the one who signals when the Agent
	// is ready, comment out the following line
	//continue;
      }
      
      // Dummy aspect/results
      int []types = {AspectType.COST};
      double []results = {(double) 0.0};
      AllocationResult estAR = 
        theLDMF.newAllocationResult(1.0, // rating
                                    true, // success
                                    types, results);
        
      // Create allocation of task to asset with given estimated result
      Allocation alloc = 
        theLDMF.createAllocation(task.getPlan(), task, dummyAsset, estAR,
                                 MetricsProviderRole);
      publishAdd(alloc);
    }
  }

  /**
   * Initialize our statistics
   *
   */
  private void startStatistics() {
    if (started)
      return;
    started = true;
    startTime = System.currentTimeMillis();
    startCPU = CpuClock.cpuTimeMillis();

    // This forces the MessageStatistics to be reset
    // Deprecated...
    //    getMetricsSnapshot(mstats, true);
    messageStatsService.getMessageStatistics(true);
//      System.out.println("Statistics start " + ourCluster);

    // Write out column headers in output file
    writer.print("Sample length(sec)\t");
    writer.print("CPU used in sample (sec)\t");
    writer.print("Mem (MB) Used\t");
    writer.print("Total Mem (MB)\t");
    writer.print("Msg Q Length\t");
    writer.print("Msg Bytes Sent\t");
    writer.print("Msgs Sent\t");
    writer.println("Tasks Done During Sample");
  }

  /**
   * Close the statistics output stream
   *
   */
  private void finishStatistics() {
    writer.close();
    if (writer.checkError()) {
      if (log.isApplicable(log.PROBLEM)) {
	log.log(this, log.PROBLEM, "Error writing statistics file");
      }
    }
  }

  /**
   * Gather and write out the current statistics
   *
   */
  private void sampleStatistics() {
    // fill in the current value of the various
    // statistics, forcing the message statistics to be reset
    // Note that most of these values are cumulative, with the
    // exception of the message statistics if you
    // supply "true"

    // Deprecated....
//      for (int i = 0; i < 5; i++) {
//        try {
//  	mstats = getMetricsSnapshot(mstats, true);
//  	break;
//        } catch (java.util.ConcurrentModificationException e) {
//        }
//      }
    // This includes:
    // long time -- already present here
    // int directivesIn (metrics must be turned on in cluster)
    // int directivesOut
    // int notificationsIn (see above)
    // int notificationsOut

    // From the message statistics (the ones that can be made incremental):
    // double averageMessageQueueLength
    // long totalMessageBytes
    // long totalMessageCount

    // These next 4 are the elements counted by the MetricsLP
    // int assets
    // int planelements -- here we count only those completed related to our Tasks
    // int tasks
    // int workflows
    
    // int pluginCount (pluginManager.size())
    // int thinPluginCount -- not filled in any more by Cluster!!
    // int prototypeProviderCount (prototypePrivers.size())
    // int propertyProviderCount (propertyProviders.size())
    // int cachedPrototypeCount (getRigstry().size())
    // long idleTime (accurate to 5 seconds)

    // Do these next 2 suffer since ClusterImpl does no gc() before recording?
    // long freeMemory (Runtime.freeMemory()) - already being counted
    // long totalMemory (Runtime.totalMemory()) - already being counted
    
    // int threadCount (in main COUGAAR group) getThreadGroup().activeCount()
    
    long currentTime = System.currentTimeMillis();
    // compare with mstats.time
    // Runtime.getRuntime().gc();
    long totalMemory = Runtime.getRuntime().totalMemory();
    //long totalMemory = mstats.totalMemory;
    long freeMemory = Runtime.getRuntime().freeMemory();
    //long freeMemory = mstats.freeMemory;
    long usedMemory = totalMemory - freeMemory;
    long elapsedTime = currentTime - startTime;
    long cpu = CpuClock.cpuTimeMillis();
    long cpuTime = cpu - startCPU;
    int deltaPlanElementCount = completedPlanElementCount - startPlanElementCount;
    startTime += elapsedTime;
    startCPU += cpuTime; // aka startCPU = cpu
    startPlanElementCount += deltaPlanElementCount;
    writer.print(elapsedTime/1000.0);
    writer.print("\t");
    writer.print(cpuTime/1000.0);
    writer.print("\t");
    writer.print(usedMemory/(1024.0*1024.0));
    writer.print("\t");
    writer.print(totalMemory/(1024.0*1024.0));
    
    writer.print("\t");
    //    if (mstats.averageMessageQueueLength == -1) {
    if (messageStatsService.getMessageStatistics(false).averageMessageQueueLength == -1) {
        writer.print("0.0");
    } else {
        writer.print(messageStatsService.getMessageStatistics(false).averageMessageQueueLength);
    }
    
    writer.print("\t");
    //    if (mstats.totalMessageBytes == -1) {
    if (messageStatsService.getMessageStatistics(false).totalMessageBytes == -1) {
        writer.print("0.0");
    } else {
        writer.print(messageStatsService.getMessageStatistics(false).totalMessageBytes);
    } 
   
    writer.print("\t");
    //    if (mstats.totalMessageCount == -1) {
    if (messageStatsService.getMessageStatistics(false).totalMessageCount == -1) {
      writer.print("0.0");
      } else {
        writer.print(messageStatsService.getMessageStatistics(false).totalMessageCount);
      }    

    writer.print("\t");
    writer.print(deltaPlanElementCount);
    writer.println();
    if (log.isApplicable(log.DEBUG)) {
      log.log(this, log.DEBUG, "Duration     : " + timeFormat.format(elapsedTime/1000.0) + "\n" + 
	      "CPU          : " + timeFormat.format(cpuTime/1000.0) + "\n" + 
	      "Used Memory  : " + memoryFormat.format(usedMemory/(1024.0*1024.0)) + "\n" + 
	      "Total Memory : " + memoryFormat.format(totalMemory/(1024.0*1024.0)) + "\n" + 
  	      "Message Queue: " + messageStatsService.getMessageStatistics(false).averageMessageQueueLength + "\n" + 
  	      "Message Bytes: " + messageStatsService.getMessageStatistics(false).totalMessageBytes + "\n" + 
  	      "Message Count: " + messageStatsService.getMessageStatistics(false).totalMessageCount + "\n" + 
	      "Message Queue: 0.0\n" + 
	      "Message Bytes: 0.0\n" + 
	      "Message Count: 0.0\n" + 
	      "Tasks Done   : " + deltaPlanElementCount);
    }
    // Flush the writer?
    writer.flush();
  }
} // end of MetricsPlugin.java

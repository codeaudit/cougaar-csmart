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

import org.cougaar.core.service.BlackboardMetricsService;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.blackboard.DirectiveMessage;
import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.component.ServiceRevokedEvent;
import org.cougaar.core.service.MessageStatisticsService;
import org.cougaar.core.service.MessageWatcherService;
import org.cougaar.core.mts.MessageTransportWatcher;
import org.cougaar.core.service.NodeMetricsService;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.Message;

import org.cougaar.core.service.PrototypeRegistryService;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AbstractAsset;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.Directive;
import org.cougaar.planning.ldm.plan.Notification;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;

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
 * This Plugin should be included in each Agent for which you want to collect statistics.<br>
 * Note that some statistics are per-Node however.<br><br>
 * This plugin is driven by control tasks, sent from a single instance of the
 * <code>MetricsInitializerPlugin</code>. <br>
 * Statistics are written to the standard log file, and also written, one line per sample,
 * to a separate statistics results file.<br>
 * The file written is named <ClusterID>_results.txt.<br>
 * If provided, the first parameter indicates the directory
 * in which to write the file. By default, it is written
 * in the working directory.<br>
 * The following statistics are always written:<br>
 * Sample length in seconds<br>
 * CPU seconds used by Node during sample<br>
 * (NOTE: This CPU statistic required the separate CpuClock shared library,
 * included with the scalability module.  See the Scalability module's install
 * instructions for installing the CpuClock library. If you do not install
 * this library, you will see a warning exception, which can be ignored.<br>
 * <br>x1
 * Memory used by Node in Megabytes<br>
 * Total memory allocated to the Node in megabytes<br>
 * <br>
 * Then come some statistics recorded only if the MessageStatisticsService was requested,
 * which is the default setting:<Br>
 * Message Queue Length (a per-Node statistic)<br>
 * MessageBytes Sent (per-Node)<br>
 * Messages sent (per-Node)<br>
 * <br>
 * Then this statistic is always written:<br>
 * Count of non-Metrics related Tasks with an associated high-confidence <code>AllocationResult</code><br>
 * <br>
 * Optional argument, statistics: If a second optional parameter is given, the
 * plugin will count the tasks with the given Verb that go by.<br>
 * It will also count the time since the first task with that Verb went by.<br>
 * For example, use this to count the Transport tasks, or to see how long
 * after a DetermineRequirements Task things get busy.<br>
 * <br>
 * So this optional argument results in 3 additional statistics:<br>
 * Time since first Task with the Verb<br>
 * Number of Tasks in this interval with the verb<br>
 * Total Tasks seen with the verb<br>
 * <br>
 * Other statistics are gathered from the various services which provide
 * measurements.<br>
 * These statistics can be turned on/off via additional optional 1/0 arguments.<br>
 * By default however, only the basic set of statistics is gathered.<br>
 * First, Blackboard statistics (off by default): <br>
 * Count of Assets on this Agent's Blackboard<br>
 * PlanElements on this Blackboard<br>
 * Tasks on the Blackboard<br>
 * Total Objects on this Blackboard<br>
 * <br>
 * Prototype Registry (off by default):<br>
 * Cached Prototypes<br>
 * Property Providers<br>
 * Prototype Providers<br>
 * <br>
 * Node Metrics (off by default): <br>
 * Active threads in the COUGAAR group (a per-Node stat)<br>
 * Free memory (bytes) in this Node's allocation<br>
 * Total memory (bytes) in this Node's allocation<br>
 * <br>
 * Message Watcher (off by default): <br>
 * Directives received by this Agent<br>
 * Directives sent<br>
 * Notifications received by this Agent<br>
 * Notifications sent by this Agent<br>
 * <br>
 * So the full usage is: <br>
 * MetricsPlugin Usage: [[<directory name to write results in>],[<Task Verb to search for>],
 * [<1 or 0>], [<1 or 0>],[<1 or 0>],[<1 or 0>],[<1 or 0>]] -- where the [1/0] indicates
 * turning on or off the following services for Metrics collection: BlackboardService,
 * PrototypeRegistryService, NodeMetricsService, MessageStatsService, and
 * MessageWatcherService. Default is to use only the MessageStatsService<br>
 * <br>
 * @see CSMARTPlugIn
 * @see MetricsConstants
 * @see MetricsInitializerPlugin
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
  private PrintWriter writer; // write out the metrics
  
  // Services to use to get metrics
  private PrototypeRegistryService protoRegistryService = null;
  private BlackboardMetricsService bbMetricsService = null;
  private MessageStatisticsService messageStatsService = null;
  private MessageWatcherService  messageWatchService = null;
  private NodeMetricsService  nodeMetricsService = null;

  // Flags to indicate which optional metrics to collect
  private boolean wantBBStats = false;
  private boolean wantProtoRegStats = false;
  private boolean wantNodeStats = false;
  private boolean wantMessStats = true;  //default statistics obtained
  private boolean wantMessWatchStats = false;

  boolean started = false; // dont call start twice by mistake
  
  static NumberFormat timeFormat = new DecimalFormat("0.000 seconds");
  static NumberFormat memoryFormat = new DecimalFormat("0.000 MBi");

  private int countMyTasks = 0;
  private int countMyTasksThisInterval = 0;
  private long timeFirstMyTask = 0l;
  private String searchVerb = null;

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

  private IncrementalSubscription myTaskSearch = null;
  private UnaryPredicate taskSearchPred;
  private UnaryPredicate createTSPred(final String sverb) {
    return new UnaryPredicate() {
	public boolean execute(Object o) { 
	  if (o instanceof Task) {
	    Task task = (Task) o;
	    Verb verb = task.getVerb();
	    if (verb.equals(sverb))
	      return true;
	  }
	  return false;
	}
      };
  }

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
    Asset prototype = theLDMF.createPrototype(AbstractAsset.class, "Statistics");
    dummyAsset = theLDMF.createInstance(prototype);
    publishAdd(dummyAsset);

    ourCluster = getAgentIdentifier();

    // Open up the file for the results
    String path = DEFAULT_DIRECTORY;
    Vector params = getParameters() != null ? new Vector(getParameters()) : null;
    if (params != null && ! params.isEmpty()) {
      path = ((! (params.elementAt(0) instanceof String)) ? path : (String)params.elementAt(0));
      // could do File.isDirectory() and File.canWrite() on this

      // Optional second argument - verb for which tasks are searched for
      if (params.size() > 1) {
	// 2nd slot, number 1
	searchVerb = (String)params.elementAt(1);
	if (searchVerb.equals("") || searchVerb.equals(" "))
	  searchVerb = null;
      }

      // Take the additional parameters here
      if (params.size() > 2) {
	if (params.size() >= 3) {
	  // Blackboard statistics are the 3rd parameter, number 2
          if (Integer.parseInt((String)params.elementAt(2)) != 0 ) {
            wantBBStats = true;
            bbMetricsService = (BlackboardMetricsService)
              getServiceBroker().getService(this, BlackboardMetricsService.class, 
                                            new ServiceRevokedListener() {
                                                public void serviceRevoked(ServiceRevokedEvent re) {
                                                  if (BlackboardMetricsService.class.equals(re.getService())) {
                                                    bbMetricsService = null;
                                                  }
                                                }
                                              });
          } // end of parsed non-0 int
        } // end of have at least 3 params
	
        if ( params.size() >= 4) {
	  // ProtypeRegistry stats are the 4th parameter, number 3
          if (Integer.parseInt((String)params.elementAt(3)) != 0 ) {
            wantProtoRegStats = true;
            protoRegistryService = (PrototypeRegistryService)
              getServiceBroker().getService(this, PrototypeRegistryService.class, 
                                            new ServiceRevokedListener() {
                                                public void serviceRevoked(ServiceRevokedEvent re) {
                                                  if (PrototypeRegistryService.class.equals(re.getService()))
                                                    protoRegistryService  = null;
                                                }
                                              });
      
          } // end of parsed non-0 int              
        } // end of if have at least 4 params
        
        if (params.size() >= 5 ) {
	  // Node metrics stats are the 5th param, number 4
          if (Integer.parseInt((String)params.elementAt(4)) != 0 ) {
            wantNodeStats = true;  
            nodeMetricsService = (NodeMetricsService)
              getServiceBroker().getService(this, NodeMetricsService.class, 
                                            new ServiceRevokedListener() {
                                                public void serviceRevoked(ServiceRevokedEvent re) {
                                                  if (NodeMetricsService.class.equals(re.getService())) {
                                                    nodeMetricsService = null;
                                                  }
                                                }
                                              });          
      
          } // end of if parsed non-0 int                  
        } // end of if have at least 5 params

	if (params.size() >= 6) {
	  // Message Transport stats are the 6th parameter, number 5: They are the only one ON by default
          if (Integer.parseInt((String)params.elementAt(5)) == 0 ) {
            wantMessStats = false;
          }
	}

        if (params.size() == 7) {
	  // Message Watcher statistics are the 7th parameter, number 6
          if (Integer.parseInt((String)params.elementAt(6)) != 0 ) {
            wantMessWatchStats = true;      

            messageWatchService = (MessageWatcherService)
              getServiceBroker().getService(this,MessageWatcherService.class, 
                                            new ServiceRevokedListener() {
                                                public void serviceRevoked(ServiceRevokedEvent re) {
                                                  if (MessageWatcherService.class.equals(re.getService()))
                                                    messageWatchService = null;
                                                }
                                              });   
            messageWatchService.addMessageTransportWatcher(_messageWatcher = new MessageWatcher());
          } // end of if parsed non-0 int
        } // end of if have 7 params
		
      } // end of if have more than 2 params
      
      if (params.size() > 7) {
	if (log.isApplicable(log.PROBLEM)) {
	  // Explain the parameters correctly...
	  // wantBBStats
	  // wantProtoRegStats
	  // wantNodeStats
	  // wantMessStats -- ON by default
	  // wantMessWatchStats
	  log.log(this, log.PROBLEM, "MetricsPlugin Usage: [[<directory name to write results in>],[<Task Verb to search for>],[<1 or 0>], [<1 or 0>],[<1 or 0>],[<1 or 0>],[<1 or 0>]] -- where the [1/0] indicates turning on or off the following services for Metrics collection: BlackboardService, ProtypeRegistryService, NodeMetricsService, MessageStatsService, and MessageWatcherService. Default is to use only the MessageStatsService");
	}
	// return;
      }
    }
    // If the path doesn't end in the separator character, add one
    if (path.lastIndexOf(File.separatorChar) != path.length() - 1) {
      path = path + File.separator;
    }

    if (log.isApplicable(log.DEBUG)) {
      log.log(this, log.DEBUG, this + "Writing " + path + ourCluster + RESULTS_FILENAME_SUFFIX);
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

    managePlanElements = (IncrementalSubscription) subscribe(managePlanElementsPredicate);
    myTasks = (IncrementalSubscription) subscribe(myTasksPredicate);

    // Subscribe to tasks with the given verb
    if (searchVerb != null) {
      myTaskSearch = (IncrementalSubscription) subscribe(createTSPred(searchVerb));
    }

    //        startStatistics();
    if (wantMessStats) {
      messageStatsService = (MessageStatisticsService)
        getBindingSite().getServiceBroker().getService(this, MessageStatisticsService.class, 
                                                       new ServiceRevokedListener() {
							   public void serviceRevoked(ServiceRevokedEvent re) {
							     if (MessageStatisticsService.class.equals(re.getService())){
							       messageStatsService = null;
							     }
							   }
							 });
      if (log.isApplicable(log.VERBOSE)) {
	log.log(this, log.VERBOSE, this + ": MessageStatsService is: " + messageStatsService);
      }
    } // end of block on messStats
  } // end of setupSubscriptions()
    
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

    // Count up the tasks I see with my verb
    if (myTaskSearch != null && myTaskSearch.hasChanged())
      processMyTasks(myTaskSearch.getAddedList());
    
    // If we have any control tasks to handle, do so
    if (myTasks.hasChanged()) {
      processTasks(myTasks.getAddedList());
    }
    //sampleStatistics();
    //   finishStatistics();
  }

  private void processMyTasks(Enumeration e) {
    while (e.hasMoreElements()) {
      if (timeFirstMyTask == 0)
	timeFirstMyTask = System.currentTimeMillis();
      countMyTasks++;
      countMyTasksThisInterval++;
      e.nextElement();
    }
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

    // Write out column headers in output file
    writer.print("Sample length(sec)\t");
    writer.print("CPU used in sample (sec)\t");

    // FIXME: Next 2 overlap with Node service
    writer.print("Mem (MB) Used\t");
    writer.print("Total Mem Allocated to Node (MB)");
    
    if (wantMessStats && messageStatsService != null)
      messageStatsService.getMessageStatistics(true);
    
    if (wantMessStats) {
      writer.print("\tMsg Q Length\t");
      writer.print("Msg Bytes Sent\t");
      writer.print("Msgs Sent");
    } // end of if ()
    
    writer.print("\tTasks Done During Sample");
    
    if (myTaskSearch != null) {
      // Print headers for optional results
      writer.print("\tTime since first Task with Verb " + searchVerb + "\t");
      writer.print("Num Tasks this Interval with Verb " + searchVerb + "\t");
      writer.print("Total Tasks seen with Verb " + searchVerb);
    }

    if (wantBBStats) {
      writer.print("\tTotal Blackboard Asset Count\t");
      writer.print("Total Plan Element Count\t");
      writer.print("Total Task Count\t");
      writer.print("Blackboard Object Count");      
    } // end of if ()

    // FIXME What exactly are these counting?
    if (wantProtoRegStats) {
      writer.print("\tCached Prototype Cnt\t");
      writer.print("Property Provider Cnt\t");
      writer.print("Prototype Provider Cnt");      
    } // end of if ()

    if (wantNodeStats) {
      writer.print("\tActive Thread Cnt\t");
      // FIXME: These next 2 overlap with those above!!
      writer.print("Free Mem (bytes)\t");
      writer.print("Total Mem (bytes)");
    } // end of if ()
    
    if (wantMessWatchStats) {
      writer.print("\tDirectives Into Agent\t");
      writer.print("Directives Out\t");
      writer.print("Notifications In\t");
      writer.print("Notifications Out");      
    } // end of if ()
    writer.println();
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
    long currentTime = System.currentTimeMillis();

    // MessageStatsService
    double averageMessageQueueLength = 0.0;
    long totalMessageBytes = 0l;
    long totalMessageCount = 0l;
    if (messageStatsService != null) {
      averageMessageQueueLength = messageStatsService.getMessageStatistics(false).averageMessageQueueLength;
      averageMessageQueueLength = (averageMessageQueueLength == -1 ? 0.0 : averageMessageQueueLength);
      totalMessageBytes = messageStatsService.getMessageStatistics(false).totalMessageBytes;
      totalMessageBytes = (totalMessageBytes == -1 ? 0 : totalMessageBytes);
      totalMessageCount = messageStatsService.getMessageStatistics(false).totalMessageCount;
      totalMessageCount = (totalMessageCount == -1 ? 0 : totalMessageCount);
    }

    // Blackboard Statistics
    int assetCount = 0;
    int planElementCount = 0;
    int bbTaskCount = 0;
    int bbObjCount = 0;
    if (bbMetricsService != null) {
      assetCount = bbMetricsService.getAssetCount();
      planElementCount = bbMetricsService.getPlanElementCount();
      bbTaskCount = bbMetricsService.getTaskCount();
      bbObjCount = bbMetricsService.getBlackboardObjectCount();
    }

    // Prototype Registry Statistics
    int cachedPrototypeCount = 0;
    int propProvCount = 0;
    int protoProvCount = 0;
    if (protoRegistryService != null) {
      cachedPrototypeCount = protoRegistryService.getCachedPrototypeCount();
      propProvCount = protoRegistryService.getPropertyProviderCount();
      protoProvCount = protoRegistryService.getPrototypeProviderCount();
    }
    
    // NodeMetricsService
    long freeMemory = 0l;
    long totalMemory = 0l;
    int activeThreadCount = 0;
    // FIXME! These duplicate the Node statistics!!
    if (nodeMetricsService != null) {
      freeMemory = nodeMetricsService.getFreeMemory();
      totalMemory = nodeMetricsService.getTotalMemory();
      activeThreadCount = nodeMetricsService.getActiveThreadCount();
    } else {
      freeMemory = Runtime.getRuntime().freeMemory();
      totalMemory = Runtime.getRuntime().totalMemory();
    }

    // MessageTransport Stats
    int directivesIn = 0;
    int directivesOut = 0;
    int notificationsIn = 0;
    int notificationsOut = 0;
    if (messageWatchService != null) {
      directivesIn = _messageWatcher.getDirectivesIn();
      directivesOut = _messageWatcher.getDirectivesOut();
      notificationsIn = _messageWatcher.getNotificationsIn();
      notificationsOut = _messageWatcher.getNotificationsOut();
    }
    
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
    
    if (wantMessStats) {
      writer.print(averageMessageQueueLength + "\t");
      writer.print(totalMessageBytes + "\t");
      writer.print(totalMessageCount);
    }
    
    writer.print("\t");
    writer.print(deltaPlanElementCount);

    double timeDelta = (timeFirstMyTask == 0l ? 0l : (System.currentTimeMillis() - timeFirstMyTask) / 1000.0);
    if (myTaskSearch != null) {
      if (timeFirstMyTask == 0l) {
	writer.print("\t-1\t");
      } else {
	writer.print("\t" + timeDelta + "\t");
      }
      writer.print(countMyTasksThisInterval + "\t");
      writer.print(countMyTasks);
    }

    if (wantBBStats) {
      writer.print("\t" + assetCount);
      writer.print("\t" + planElementCount);
      writer.print("\t" + bbTaskCount);
      writer.print("\t" + bbObjCount);
    } // end of if ()
  
    if (wantProtoRegStats) {
      writer.print("\t" + cachedPrototypeCount);
      writer.print("\t" + propProvCount);
      writer.print("\t" + protoProvCount);
    } // end of if ()
    
    if (wantNodeStats) {
      writer.print("\t" + activeThreadCount);
      writer.print("\t" + freeMemory);
      writer.print("\t" + totalMemory);
    } // end of if ()

    if (wantMessWatchStats) {
      writer.print("\t" + directivesIn);
      writer.print("\t" + directivesOut);
      writer.print("\t" + notificationsIn);
      writer.print("\t" + notificationsOut);
    } // end of if ()

    writer.println();
    
    if (log.isApplicable(log.DEBUG)) {
      log.log(this, log.DEBUG, "Duration     : " + timeFormat.format(elapsedTime/1000.0) + "\n" + 
	      "CPU          : " + timeFormat.format(cpuTime/1000.0) + "\n" + 
	      "Used Memory  : " + memoryFormat.format(usedMemory/(1024.0*1024.0)) + "\n" + 
	      "Total Memory : " + memoryFormat.format(totalMemory/(1024.0*1024.0)) + "\n");
      if (wantMessStats) {
	log.log(this, log.DEBUG, 
  	      "Message Queue: " + averageMessageQueueLength + "\n" + 
  	      "Message Bytes: " + totalMessageBytes + "\n" + 
		"Message Count: " + totalMessageCount + "\n");
      }
      
      log.log(this, log.DEBUG, 
	      "Tasks Done   : " + deltaPlanElementCount);
      
      if (myTaskSearch != null) {
	if (timeFirstMyTask == 0l) {
	  log.log(this, log.DEBUG, "Time (sec) since first task of Verb " + searchVerb + ": <Not seen yet>\n");
	} else {
	  log.log(this, log.DEBUG, "Time (sec) since first task of Verb " + searchVerb + ": " + timeDelta + "\n");
	}
	log.log(this, log.DEBUG, "Num Tasks this interval of Verb " + searchVerb + ": " + countMyTasksThisInterval + "\n");
	log.log(this, log.DEBUG, "Total Tasks seen with Verb " + searchVerb + ": " + countMyTasks + "\n");
      }
      
      // Blackboard stats
      if (wantBBStats) {
	log.log(this, log.DEBUG, "Total Assets on BBoard: " + assetCount + "\n");
	log.log(this, log.DEBUG, "Total PlanElements on BBoard: " + planElementCount + "\n");
	log.log(this, log.DEBUG, "Total Tasks on BBoard: " + bbTaskCount + "\n");
	log.log(this, log.DEBUG, "Total Objects on BBoard: " + bbObjCount + "\n");
      }
      
      // ProtypeRegistryStats
      if (wantProtoRegStats) {
	log.log(this, log.DEBUG, "Cached Prototypes: " + cachedPrototypeCount + "\n");
	log.log(this, log.DEBUG, "Property Providers: " + propProvCount + "\n");
	log.log(this, log.DEBUG, "Prototype Providers: " + protoProvCount + "\n");
      }
      
      // Node Statistics
      if (wantNodeStats) {
	log.log(this, log.DEBUG, "Active COUGAAR Threads: " + activeThreadCount + "\n");
	log.log(this, log.DEBUG, "Free memory in Node's allocation (bytes): " + freeMemory + "\n");
	log.log(this, log.DEBUG, "Total memory for Node (bytes): " + totalMemory + "\n");
      }

      // MessageWatcher Stats
      if (wantMessWatchStats) {
	log.log(this, log.DEBUG, "Directives received by Agent: " + directivesIn + "\n");
	log.log(this, log.DEBUG, "Directives sent by Agent: " + directivesOut + "\n");
	log.log(this, log.DEBUG, "Notifications received by Agent: " + notificationsIn + "\n");
	log.log(this, log.DEBUG, "Notifications sent by Agent: " + notificationsOut + "\n");
      }
    }
    
    // reset task count variable
    countMyTasksThisInterval = 0;
    
    // Flush the writer?
    writer.flush();
  } // end of sampleStatistics()

  protected MessageWatcher _messageWatcher = null;

  class MessageWatcher implements MessageTransportWatcher {

    MessageAddress me;
    private int directivesIn = 0;
    private int directivesOut = 0;
    private int notificationsIn = 0;
    private int notificationsOut = 0;
        
    public MessageWatcher() {
      //            me = getAgentIdentifier();
      me = MetricsPlugin.this.getBindingSite().getAgentIdentifier();
    }
        
    public void messageSent(Message m) {
      if (m.getOriginator().equals(me)) {
        if (m instanceof DirectiveMessage) {
          Directive[] directives = ((DirectiveMessage)m).getDirectives();
          for (int i = 0; i < directives.length; i++) {
            if (directives[i] instanceof Notification)
              notificationsOut++;
            else
              directivesOut++;
          }
        }
      }
    } // close messageSent

    public void messageReceived(Message m) {
      if (m.getTarget().equals(me)) {
        if (m instanceof DirectiveMessage) {
          Directive[] directives = ((DirectiveMessage)m).getDirectives();
          for (int i = 0; i < directives.length; i++) {
            if (directives[i] instanceof Notification)
              notificationsIn++;
            else
              directivesIn++;
          }
        }
      }
    } // close messageReceived

    public int getDirectivesIn()     {  return directivesIn; }
    public int getDirectivesOut()    {  return directivesOut; }
    public int getNotificationsIn()  {  return notificationsIn;  }
    public int getNotificationsOut() {  return notificationsOut; }
  }   // end of MessageWatcher
} // end of MetricsPlugin.java

/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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
 
package org.cougaar.tools.csmart.ui.servlet;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AssetGroup;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.planning.ldm.measure.AbstractMeasure;

import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationforCollections;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AssetAssignment;
import org.cougaar.planning.ldm.plan.AssetTransfer;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.planning.ldm.plan.Composition;
import org.cougaar.planning.ldm.plan.Disposition;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.ItineraryElement;
import org.cougaar.planning.ldm.plan.Location;
import org.cougaar.planning.ldm.plan.LocationScheduleElement;
import org.cougaar.planning.ldm.plan.LocationRangeScheduleElement;
import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;

import org.cougaar.util.PropertyTree;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames;

/**
 * Translate <code>UniqueObject</code>s to <code>PropertyTree</code>s.
 */
public final class TranslateUtils {

  private TranslateUtils() {
    // just static utilities!
  }

  /**
   * Translate all the <code>Object</code>s in the given <code>List</code>
   * into <code>PropertyTree</code>s.
   * <p>
   * Simply uses <tt>toPropertyTree(Object)</tt>.
   *
   * @param fromL a List of Objects
   * @param agent the name of the Agent (MessageAddress) for these Objects; i.e. the name of the agent in which this code is executing
   *
   * @return a List of non-null PropertyTrees; list may be empty, but is not null
   */
  public static List toPropertyTrees(List objects, String agent) {
    int n = ((objects != null) ? objects.size() : 0);
    if (n <= 0) {
      return new ArrayList(0);
    }
    List results = new ArrayList(n);
    for (int i = 0; i < n; i++) {
      PropertyTree pt = toPropertyTree(objects.get(i), agent);
      if (pt != null) {
        results.add(pt);
      }
    }
    return results;
  }

  /**
   * Translate the given <code>Object</code>s into an equivalent
   * <code>PropertyTree</code>.  The Object must be an instance of
   * org.cougaar.core.util.UniqueObject or this will return null.
   *
   * @param o an Object to convert into a PropertyTree
   * @param agent the name of the Agent (MessageAddress) for this Object
   *
   * @return a PropertyTree, or null if an error occurred
   */
  public static PropertyTree toPropertyTree(Object o, String agent) {
    return
      (o instanceof UniqueObject) ?  toPropertyTree((UniqueObject)o, agent) :
      null;
  }

  /**
   * Return property trees for Tasks, PlanElements, Assets and Workflows.
   * If object is not one of these, then returns null.
   */
  public static PropertyTree toPropertyTree(UniqueObject uo, String agent) {
    return
      (uo instanceof        Task) ?  toPropertyTree(       (Task)uo, agent) :
      (uo instanceof PlanElement) ?  toPropertyTree((PlanElement)uo, agent) :
      (uo instanceof       Asset) ?  toPropertyTree(      (Asset)uo, agent) :
      (uo instanceof    Workflow) ?  toPropertyTree(   (Workflow)uo, agent) :
      null;
  }

  /**
   * Return a PropertyTree for a task.
   * @param Task the task for which to return a property tree
   * @param agent the name of the agent
   * @param a non-null PropertyTree
   */
  public static PropertyTree toPropertyTree(Task task, String agent) {
    PropertyTree pt = 
      toBasicPropertyTree(PropertyNames.TASK_OBJECT,
			  getUIDAsString(task.getUID()),
			  agent);
    pt.put(PropertyNames.TASK_VERB, task.getVerb().toString());
    Asset a = task.getDirectObject();
    if (a != null) {
      pt.put(PropertyNames.TASK_DIRECT_OBJECT_UID,
	     getUIDAsString(a.getUID()));
    }
    // prepositional phrases
    Enumeration phrases = task.getPrepositionalPhrases();
    while (phrases.hasMoreElements()) {
      PrepositionalPhrase phrase = 
        (PrepositionalPhrase)phrases.nextElement();
      pt.put(PropertyNames.TASK_PREP_PHRASE + "_" + phrase.getPreposition(), 
	     getPrepositionalObjectDescription(phrase.getIndirectObject()));
    }
    // indicate if it's a multi-parent task, and return the parents
    if (task instanceof MPTask) {
      pt.put(PropertyNames.TASK_TYPE, PropertyNames.MPTASK);
      Enumeration parents = ((MPTask)task).getParentTasks();
      ArrayList parentUIDs = new ArrayList();
      while (parents.hasMoreElements()) {
        Task parent = (Task)parents.nextElement();
        parentUIDs.add(getUIDAsString(parent.getUID()));
      }
      pt.put(PropertyNames.TASK_PARENT_UID, parentUIDs);
    } else {
      pt.put(PropertyNames.TASK_TYPE, PropertyNames.SINGLE_TASK);
      pt.put(PropertyNames.TASK_PARENT_UID,
	     getUIDAsString(task.getParentTaskUID()));
      pt.put(PropertyNames.TASK_SOURCE, trimAngles(task.getSource().toString()));
    }
    double endTime = task.getPreferredValue(AspectType.END_TIME);
    if (!(endTime == -1))
      pt.put(PropertyNames.TASK_END_TIME, new Double(endTime));
    if (task.getPlanElement() != null) {
      pt.put(PropertyNames.TASK_PLAN_ELEMENT_UID,
	     getUIDAsString(task.getPlanElement().getUID()));
    }
    return pt;
  }

  public static PropertyTree toPropertyTree(Workflow wf, String agent) {
    PropertyTree pt = 
      toBasicPropertyTree(PropertyNames.WORKFLOW_OBJECT,
			  getUIDAsString(wf.getUID()),
			  agent);
    pt.put(PropertyNames.WORKFLOW_PARENT_TASK_UID,
	   getUIDAsString(wf.getParentTask().getUID()));
    Enumeration tasks = wf.getTasks();
    StringBuffer sb = new StringBuffer(100);
    while (tasks.hasMoreElements()) {
      Task task = (Task)tasks.nextElement();
      sb.append(getUIDAsString(task.getUID()));
      sb.append(",");
    }
    int n = sb.length();
    if (n > 0)  {
      pt.put(PropertyNames.WORKFLOW_TASK_UIDS, sb.substring(0, n-1));
    }
    return pt;
  }

  public static PropertyTree toPropertyTree(PlanElement pe, String agent) {
    return
      (pe instanceof     Expansion) ? toPropertyTree(    (Expansion)pe, agent) :
      (pe instanceof    Allocation) ? toPropertyTree(   (Allocation)pe, agent) :
      (pe instanceof   Aggregation) ? toPropertyTree(  (Aggregation)pe, agent) :
      (pe instanceof   Disposition) ? toPropertyTree(  (Disposition)pe, agent) :
      (pe instanceof AssetTransfer) ? toPropertyTree((AssetTransfer)pe, agent) :
      toPlanElementPropertyTree(pe, agent, PropertyNames.PLAN_ELEMENT_UNKNOWN);
  }

  public static PropertyTree toPropertyTree(Expansion exp, String agent) {
    PropertyTree pt = 
      toPlanElementPropertyTree(exp, agent,
				PropertyNames.PLAN_ELEMENT_EXPANSION);
    pt.put(PropertyNames.EXPANSION_WORKFLOW_UID, 
	   getUIDAsString(exp.getWorkflow().getUID()));
    return pt;
  }

  public static PropertyTree toPropertyTree(Allocation alloc, String agent) {
    PropertyTree pt = 
      toPlanElementPropertyTree(alloc, agent,
				PropertyNames.PLAN_ELEMENT_ALLOCATION);
    // ALLOCATION TO REMOTE or LOCAL ORGANIZATION ASSET
    Asset asset = alloc.getAsset();
    if (asset.hasClusterPG()) {
      if (alloc instanceof AllocationforCollections) {
        AllocationforCollections ac = (AllocationforCollections)alloc;
        Task at = (Task)ac.getAllocationTask();
        if (at != null) {
          pt.put(PropertyNames.ALLOCATION_TO_CLUSTER,
		 trimAngles(at.getDestination().toString()));
          pt.put(PropertyNames.ALLOCATION_TASK_UID,
		 at.getUID().toString());
        }
	// Need to add pointer to Asset representing remote 
	pt.put(PropertyNames.ALLOCATION_ASSET_UID, 
	       getUIDAsString(asset.getUID()));
      } else {
        Task task = alloc.getTask();
        pt.put(PropertyNames.ALLOCATION_REMOTE_CLUSTER_UID,
	       trimAngles(task.getDestination().toString()));
        pt.put(PropertyNames.ALLOCATION_LOCAL_ORG_UID, 
	       getUIDAsString(asset.getUID()));
      }
    } else {
      pt.put(PropertyNames.ALLOCATION_ASSET_UID, 
	     getUIDAsString(asset.getUID()));
    }
    return pt;
  }

  // Some things, like ClusterID.toString(), have these ugly angle brackets
  public static String trimAngles(String input) {
    if (input == null)
      return input;
    if (input.startsWith("<"))
      input = input.substring(1);
    if (input.endsWith(">"))
      input = input.substring(0, input.length() - 1);
    return input;
  }

  public static PropertyTree toPropertyTree(Aggregation agg, String agent) {
    PropertyTree pt = 
      toPlanElementPropertyTree(agg, agent,
				PropertyNames.PLAN_ELEMENT_AGGREGATION);
    Composition comp = agg.getComposition();
    MPTask mp = comp.getCombinedTask();
    if (mp.getUID() != null)
      pt.put(PropertyNames.AGGREGATION_MPTASK_UID, 
	     getUIDAsString(mp.getUID()));

    List parents = comp.getParentTasks();
    StringBuffer sb = new StringBuffer(100);
    for (Iterator i = parents.iterator(); i.hasNext(); ) {
      Object o = i.next();
      if (o instanceof Task) {
        sb.append(getUIDAsString(((Task)o).getUID()));
        sb.append(",");
      }
    }
    int n = sb.length();
    if (n > 0) {
      pt.put(PropertyNames.AGGREGATION_PARENT_UIDS, 
	     sb.substring(0, n-1));
    } 

    return pt;
  }

  public static PropertyTree toPropertyTree(Disposition disp, String agent) {
    PropertyTree pt = 
      toPlanElementPropertyTree(disp, agent,
				PropertyNames.PLAN_ELEMENT_DISPOSITION);
    pt.put(PropertyNames.DISPOSITION_SUCCESS, 
	   (disp.isSuccess() ? "true" : "false"));
    return pt;
  }

  public static PropertyTree toPropertyTree(AssetTransfer atr, String agent) {
    PropertyTree pt = 
      toPlanElementPropertyTree(atr, agent,
				PropertyNames.PLAN_ELEMENT_ASSET_TRANSFER);
    // get asset UID
    pt.put(PropertyNames.ASSET_TRANSFER_ASSET_UID, 
	   getUIDAsString(atr.getAsset().getUID()));

    // get assignee, which is an asset
    pt.put(PropertyNames.ASSET_TRANSFER_ASSIGNEE_UID,
	   getUIDAsString(atr.getAssignee().getUID()));

    // get assignor, which is a cluster id
    pt.put(PropertyNames.ASSET_TRANSFER_ASSIGNOR,
	   trimAngles(atr.getAssignor().toString()));

    return pt;
  }

  private static PropertyTree toPlanElementPropertyTree(PlanElement pe, String agent, String peType){
    PropertyTree pt = toBasicPropertyTree(PropertyNames.PLAN_ELEMENT_OBJECT,
					  getUIDAsString(pe.getUID()),
					  agent);
    pt.put(PropertyNames.PLAN_ELEMENT_TYPE, peType);
    pt.put(PropertyNames.PLAN_ELEMENT_TASK_UID,
	   getUIDAsString(pe.getTask().getUID()));
    AllocationResult estimated = pe.getEstimatedResult();
    if (estimated != null) {
      pt.put(PropertyNames.PLAN_ELEMENT_ESTIMATED_RESULT,
	     estimated.toString());
      addAllocationEndTime(pt,
			   PropertyNames.ESTIMATED_ALLOCATION_RESULT_END_TIME,
			   estimated);
      addAllocationReason(pt,
			  PropertyNames.ESTIMATED_ALLOCATION_RESULT_FAILURE_REASON,
			  estimated);
    }
    AllocationResult reported = pe.getReportedResult();
    if (reported != null) {
      pt.put(PropertyNames.PLAN_ELEMENT_REPORTED_RESULT,
	     reported.toString());
      addAllocationEndTime(pt,
			   PropertyNames.REPORTED_ALLOCATION_RESULT_END_TIME,
			   reported);
      addAllocationReason(pt,
			  PropertyNames.REPORTED_ALLOCATION_RESULT_FAILURE_REASON,
			  reported);
    }
    AllocationResult observed = pe.getObservedResult();
    if (observed != null) {
      pt.put(PropertyNames.PLAN_ELEMENT_OBSERVED_RESULT,
	     observed.toString());
      addAllocationEndTime(pt,
			   PropertyNames.OBSERVED_ALLOCATION_RESULT_END_TIME,
			   observed);
      addAllocationReason(pt,
			  PropertyNames.OBSERVED_ALLOCATION_RESULT_FAILURE_REASON,
			  observed);

    }
    return pt;
  }

  /**
   * Create property tree with properties that all objects have.
   */
  private static PropertyTree toBasicPropertyTree(String objectType,
						  String UID,
						  String agent) {
    PropertyTree pt = new PropertyTree();
    pt.put(PropertyNames.OBJECT_TYPE, objectType);
    pt.put(PropertyNames.UID_ATTR, UID);
    pt.put(PropertyNames.AGENT_ATTR, agent);
    return pt;
  }

  private static void addAllocationEndTime(PropertyTree properties,
					   String propertyName,
					   AllocationResult result) {
    if (result.isDefined(AspectType.END_TIME)) {
      properties.put(propertyName,
		     Double.toString(result.getValue(AspectType.END_TIME)));
    }
  }

  private static void addAllocationReason(PropertyTree properties,
					  String propertyName,
					  AllocationResult result) {
    if (! result.isSuccess()) {
      try {
	String s = result.auxiliaryQuery(AuxiliaryQueryType.FAILURE_REASON);
	properties.put(propertyName, s);
      } catch (IllegalArgumentException e) {
	System.out.println("TranslateUtils: can't get AllocationQueryType FAILURE_REASON: " + e);
      }
    }
  }

  /**
   * This always returns the property tree for an asset.
   * In most cases, non-local agent assets should be ignored;
   * this is the responsibility of the caller.
   * The agent in the property tree returned is always the agent in which
   * this code is executing.
   * Assets with clusterPG also return an ASSET_CLUSTER
   * which is the cluster ID in asset.getClusterPG().getMessageAddress
   */

  public static PropertyTree toPropertyTree(Asset asset, String agent) {
    PropertyTree pt = 
      toBasicPropertyTree(PropertyNames.ASSET_OBJECT,
			  getUIDAsString(asset.getUID()),
			  agent);
    pt.put(PropertyNames.ASSET_KEY, trimAngles(asset.getKey().toString()));
    if (asset.hasClusterPG())
      pt.put(PropertyNames.ASSET_CLUSTER,
	     trimAngles(asset.getClusterPG().getMessageAddress().toString()));

    String className = asset.getClass().getName();
    int index = className.lastIndexOf('.');
    if (index >= 0) {
      className = className.substring(index+1);
    }
    if (asset instanceof AssetGroup) {
      pt.put(PropertyNames.ASSET_GROUP_NAME, className);
      Vector assets = ((AssetGroup)asset).getAssets();
      ArrayList groupUIDs = new ArrayList(assets.size());
      for (int i = 0; i < assets.size(); i++) {
        Asset a = (Asset)assets.elementAt(i);
        groupUIDs.add(getUIDAsString(a.getUID()));
      }
      pt.put(PropertyNames.ASSET_GROUP_ASSETS, groupUIDs);
    } else {
      pt.put(PropertyNames.ASSET_NAME, className);
      pt.put(PropertyNames.ASSET_DESC, asset.toString());
    }
    // get property groups and values
    PropertyDescriptor[] pd = asset.getPropertyDescriptors();
    int n = 0;
    for (int i = 0; i < pd.length; i++) {
      Method readMethod = pd[i].getReadMethod();
      Object assetPropertyObj;
      // get a propertygroupimpl object which extends SimpleBeanInfo
      try {
        assetPropertyObj = readMethod.invoke(asset, null);
      } catch (Exception ex1) {
        System.err.println("TranslateUtils: Unable to get asset property group["+i+"]: "+
			   ex1);
        continue;
      }
      if (!(assetPropertyObj instanceof SimpleBeanInfo)) {
        continue;
      }
      SimpleBeanInfo assetProperty = (SimpleBeanInfo)assetPropertyObj;
      // get the descriptors for the attributes in the property group
      PropertyDescriptor [] attributePDs = 
        assetProperty.getPropertyDescriptors();
      for (int j = 0; j < attributePDs.length; j++) {
        readMethod = attributePDs[j].getReadMethod();
        Object result = null;
        try {
          result = readMethod.invoke(assetProperty, null);
        } catch (Exception ex2) {
          System.err.println(
			     "TranslateUtils: Unable to get asset property["+i+"]["+j+"]: "+
			     ex2);
          continue;
        }
        if (result != null) {
	  // adds name of the form ASSET_PROPERTY_n_name_name
          pt.put(PropertyNames.ASSET_PROPERTY +
		 "_" + (n++) + "_" + 
		 pd[i].getName() + "_" + attributePDs[j].getName(),
		 result.toString());
        }
      }
    }
    return pt;
  }

  private static final String getUIDAsString(final UID uid) {
    return
      ((uid != null) ? uid.toString() : "null");
  }

  /**
   * Get description of object that is an indirect object
   * of a prepositional phrase.
   * The indirect object of a prepositional
   * phrase is defined as an object, so there's no way to know
   * what object types are legal.  The documentation claims one of:
   * Asset, Location, Schedule, Requisition, Vector, or OPLAN
   */
  private static String getPrepositionalObjectDescription(
							  Object indirectObject) {
    if (indirectObject == null) {
      return "null";
    }
    // STRING
    if (indirectObject instanceof String) {
      return (String)indirectObject;
    }
    // LOCATION
    if (indirectObject instanceof Location) {
      return indirectObject.toString();
    }
    // ASSET
    if (indirectObject instanceof Asset) {
      Asset asset = (Asset)indirectObject;
      TypeIdentificationPG typeIdPG = asset.getTypeIdentificationPG();
      StringBuffer sb = new StringBuffer(100);
      if (typeIdPG != null) {
        String nomenclature = typeIdPG.getNomenclature();
        if (nomenclature != null)
          sb.append(nomenclature);
      }
      sb.append(" (asset type=" + asset.getClass().getName() +
		", asset uid=" + getUIDAsString(asset.getUID()) + ")");
      return sb.toString();
    }
    // SCHEDULE
    if (indirectObject instanceof Schedule) {
      return getScheduleDescription((Schedule)indirectObject);
    }
    // CLUSTERIDENTIFIER
    if (indirectObject instanceof MessageAddress) {
      return trimAngles(indirectObject.toString());
    }
    // ASSETTRANSFER
    if (indirectObject instanceof AssetTransfer) {
      return ((AssetTransfer)indirectObject).getAsset().getName();
    }
    // ASSETASSIGNMENT
    if (indirectObject instanceof AssetAssignment) {
      return ((AssetAssignment)indirectObject).getAsset().getName();
    }
    // ASSETGROUP
    if (indirectObject instanceof AssetGroup) {
      List assets = ((AssetGroup)indirectObject).getAssets();
      for (int i = 0; i < assets.size(); i++) {
        Asset asset = (Asset)assets.get(i);
        // recursive
        getPrepositionalObjectDescription(asset);
      }
    }
    // ABSTRACTMEASURE
    if (indirectObject instanceof AbstractMeasure) {
      AbstractMeasure measure = (AbstractMeasure)indirectObject;
      String s = measure.getClass().getName();
      int index = s.lastIndexOf('.');
      return s.substring(0, index-1) + ": " + indirectObject.toString();
    }
    // DEFAULT
    return indirectObject.getClass().getName() + ": " +
      indirectObject.toString();
  }

  /**
   * TODO: Probably necessary to break this down into individual properties.
   */
  private static String getScheduleDescription(Schedule schedule) {
    StringBuffer sb = new StringBuffer(100);
    sb.append("Schedule: Type: " + schedule.getScheduleType());
    sb.append(" Start Time = ");
    sb.append(getTimeString(schedule.getStartTime()));
    sb.append(" End Time = ");
    sb.append(getTimeString(schedule.getEndTime()));
    if (schedule.isEmpty()) {
      return sb.toString();
    }

    // schedule elements
    sb.append(" Elements:");
    Enumeration elements = schedule.getAllScheduleElements();
    while (elements.hasMoreElements()) {
      ScheduleElement scheduleElement = 
        (ScheduleElement)elements.nextElement();
      sb.append(" Start Time = ");
      sb.append(getTimeString(scheduleElement.getStartTime()));
      sb.append(" End Time = ");
      sb.append(getTimeString(scheduleElement.getEndTime()));
      if (scheduleElement instanceof LocationRangeScheduleElement) {
        LocationRangeScheduleElement locSE = 
          (LocationRangeScheduleElement)scheduleElement;
        sb.append(" Start Location = ");
        sb.append(locSE.getStartLocation());
        sb.append(" End Location = ");
        sb.append(locSE.getEndLocation());
        if (locSE instanceof ItineraryElement) {
          sb.append(" Verb = ");
          sb.append(((ItineraryElement)locSE).getRole());
        }
      } else if (scheduleElement instanceof LocationScheduleElement) {
        sb.append(" Location = ");
        sb.append(((LocationScheduleElement)scheduleElement).getLocation());
      }
    }
    return sb.toString();
  }

  private static SimpleDateFormat myDateFormat = 
    new SimpleDateFormat("MM_dd_yyyy_h:mma");

  /**
   * Format long time as String.
   */
  private static String getTimeString(long time) {
    synchronized (myDateFormat) {
      return myDateFormat.format(new Date(time));
    }
  }

}

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

package org.cougaar.tools.csmart.ui.monitor;

/**
 * Names and values passed from PSPs to clients.
 * Property names must not have spaces as they are used
 * as attribute names in grappa nodes.
 */

public class PropertyNames {
  // names for common properties
  public static final String OBJECT_TYPE = "Object_Type";
  public static final String TABLE_TITLE = "title";
  public static final String UID_ATTR = "UID";
  public static final String AGENT_ATTR = "Agent_Name";
  // common in all plan objects
  public static final String PLAN_OBJECT_AGENT_NAME = AGENT_ATTR;
  public static final String PLAN_OBJECT_COMMUNITY_NAME = "Community_Name";
  // values of OBJECT_TYPE from Event Environment
  public static final String EVENT_OBJECT = "Event";
  public static final String AGENT_OBJECT = "Agent";
  // values of OBJECT_TYPE from Plan Object Environment
  public static final String ASSET_OBJECT = "Asset";
  public static final String ORGANIZATION_OBJECT = "Organization";
  public static final String PLAN_ELEMENT_OBJECT = "Plan_Element";
  public static final String TASK_OBJECT = "Task";
  public static final String WORKFLOW_OBJECT = "Workflow";
  // pseudo-type, actually an asset that is the direct object of a task
  public static final String DIRECT_OBJECT = "Direct_Object";
  // names for Event properties
  public static final String EVENT_ASSET_LABEL = "Asset";
  public static final String EVENT_ASSET_TYPE = "Asset_Type";
  public static final String EVENT_ASSET_UID = "Asset_Id";
  public static final String EVENT_BODY_CLASS = "Body_Class";
  public static final String EVENT_CAUSES = "Causes";
  public static final String EVENT_CUMULATIVE = "Cumulative";
  public static final String EVENT_CYBER_ATTACK_TYPE = "Cyber_Attack_Type";
  public static final String EVENT_DEADLINE = "Deadline";
  public static final String EVENT_DELTA = "Delta";
  public static final String EVENT_DESCRIPTION = "Description";
  public static final String EVENT_DESTINATION = "Destination";
  public static final String EVENT_DURATION = "Duration";
  public static final String EVENT_FROM_TIME = "From_Time";
  public static final String EVENT_GENERATION_TIME = "Generation_Time";
  public static final String EVENT_INFRASTRUCTURE_TYPE = "Infrastructure_Event_Type";
  public static final String EVENT_INTENSITY = "Intensity";
  public static final String EVENT_LABEL = "Label";
  public static final String EVENT_LATITUDE = "Latitude";
  public static final String EVENT_LONGITUDE = "Longitude";
  public static final String EVENT_MANAGER_NAME = "Manager_Name";
  public static final String EVENT_ORIGINAL_VISIBILITY_TIME = "Undelayed_Visibility_Time";
  public static final String EVENT_PARENTS = "Parents";
  public static final String EVENT_PREVIOUS_ALLOCATION_RESULT = "Previous_Allocation_Result";
  public static final String EVENT_PUBLISHER = "Publisher";
  public static final String EVENT_RATING = "Rating";
  public static final String EVENT_RATING_DESCRIPTION = "Rating_Description";
  public static final String EVENT_REAL_GENERATION_TIME = "Real_Generation_Time";
  public static final String EVENT_REGARDING = "Regarding";
  public static final String EVENT_RESPONSE_TO = "Response_To";
  public static final String EVENT_SOURCE = "Source";
  public static final String EVENT_TARGET = "Target";
  public static final String EVENT_TASK_NAME = "Task";
  public static final String EVENT_TIME_COMPLETED = "Time_Completed";
  public static final String EVENT_TRANSMISSION_DELAY = "Transmission_Delay";
  public static final String EVENT_TYPE = "Event_Type";
  public static final String EVENT_UID = "UID";
  public static final String EVENT_UNTIL_TIME = "Until_Time";
  public static final String EVENT_VISIBILITY_TIME = "Visibility_Time";
  // values of EVENT_TYPE
  public static final String EVENT_ALLOCATION = "Allocation Event";
  public static final String EVENT_ASSET = "Asset Event";
  public static final String EVENT_COMMUNITY_NAME = "Community_Name";
  public static final String EVENT_CYBER_ATTACK = "Cyber Attack Event";
  public static final String EVENT_DEADLINE_EXCEEDED = "Deadline Exceeded Event";
  public static final String EVENT_HAPPINESS_CHANGE = "Happiness Change Event";  
  public static final String EVENT_INFRASTRUCTURE = "Infrastructure Event";
  public static final String EVENT_INTERNAL_VISIBILITY_TIME = "Internal_Visibility_Time";
  public static final String EVENT_KINETIC = "Kinetic Event";
  public static final String EVENT_REMOTE_RESPONSE = "Remote Response Event";
  public static final String EVENT_REMOTE_TASK = "Remote Task Event";
  public static final String EVENT_RESPONSE = "Response Event";
  public static final String EVENT_SNIFFER = "Sniffer Event";
  public static final String EVENT_TASK = "Task Event";
  public static final String EVENT_TIMER = "Timer Event";
  public static final String EVENT_WRAPPED = "Wrapped Event";
  // values of ASSET_TYPE
  public static final String EVENT_ASSET_LOCAL = "Local";
  public static final String EVENT_ASSET_REMOTE = "Remote";
  // names for Agent properties
  public static final String AGENT_SELF = "Self";
  public static final String AGENT_LABEL = "Label";
  public static final String AGENT_NAME = AGENT_ATTR;
  public static final String AGENT_SOURCE = "Source";
  public static final String AGENT_URL = "URL";
  public static final String AGENT_COMMUNITY_NAME = "Community_Name";
  public static final String AGENT_LATITUDE = "Latitude";
  public static final String AGENT_LONGITUDE = "Longitude";
  public static final String AGENT_TYPE = "Type";
  public static final String AGENT_ROLES = "Roles";

  // FROM TASK ENVIRONMENT
  // names for Plan Element properties
  public static final String PLAN_ELEMENT_ESTIMATED_RESULT = "Estimated_Result";
  public static final String PLAN_ELEMENT_REPORTED_RESULT = "Reported_Result";
  public static final String PLAN_ELEMENT_OBSERVED_RESULT = "Observed_Result";
  public static final String PLAN_ELEMENT_TASK_UID = "Plan_Element_Task_UID";
  public static final String PLAN_ELEMENT_TYPE = "Plan_Element_Type";
  // values for types of plan elements
  public static final String PLAN_ELEMENT_AGGREGATION = "Aggregation";
  public static final String PLAN_ELEMENT_ALLOCATION = "Allocation";
  public static final String PLAN_ELEMENT_ASSET_TRANSFER = "Asset Transfer";
  public static final String PLAN_ELEMENT_DISPOSITION = "Disposition";
  public static final String PLAN_ELEMENT_EXPANSION = "Expansion";
  public static final String PLAN_ELEMENT_UNKNOWN = "Unknown";
  // names for Aggregation properties
  public static final String AGGREGATION_MPTASK_UID = "Multi_Parent_Task_UID";
  public static final String AGGREGATION_PARENT_UIDS = "Parent_UIDs";
  // names for Allocation properties
  public static final String ALLOCATION_ASSET_UID = "Asset_UID";
  public static final String ALLOCATION_LOCAL_ORG_UID = "Allocated_to_Local_Organization";
  public static final String ALLOCATION_TO_CLUSTER = "Allocated_to_Cluster";
  public static final String ALLOCATION_TASK_UID = "Allocation_Task_UID";
  public static final String ALLOCATION_REMOTE_CLUSTER_UID = "Remote_Cluster_UID";
  // names for AssetTransfer properties
  public static final String ASSET_TRANSFER_ASSET_UID = "Asset_Transfer_Asset_UID";
  public static final String ASSET_TRANSFER_ASSIGNEE_UID = "Assignee_UID";
  public static final String ASSET_TRANSFER_ASSIGNOR = "Assignor";
  // names for Disposition properties
  public static final String DISPOSITION_SUCCESS = "Success";
  // names for Expansion properties
  public static final String EXPANSION_WORKFLOW_UID = "Workflow_UID";
  // names for Workflow properties
  public static final String WORKFLOW_PARENT_TASK_UID = "Parent_Task_UID";
  public static final String WORKFLOW_TASK_UIDS = "Task_UIDs";
  // names for Asset properties  
  public static final String ASSET_KEY = "Asset_Key";
  public static final String ASSET_DESC = "Asset_Description";
  public static final String ASSET_GROUP_ASSETS = "Asset_Group_Asset";
  public static final String ASSET_GROUP_NAME = "Asset_Group_Name";
  public static final String ASSET_IS_DIRECT_OBJECT = "Is_Direct_Object";
  public static final String ASSET_NAME = "Asset_Name";
  public static final String ASSET_PROPERTY = "Asset_Property";
  public static final String ASSET_CLUSTER = "Asset_Cluster";
  // names for Task properties
  public static final String TASK_DIRECT_OBJECT_UID = "Direct_Object_UID";
  public static final String TASK_END_TIME = "Task_End_Time";
  public static final String TASK_INPUT_TASK_UID = "Input_Task_UID";
  public static final String TASK_PARENT_UID = "Parent_UID";
  public static final String TASK_PLAN_ELEMENT_UID = "Plan_Element_UID";
  public static final String TASK_PREP_PHRASE = "Prepositional_Phrase";
  public static final String TASK_SOURCE = "Task_Source";
  public static final String TASK_TYPE = "Task_Type";
  public static final String TASK_VERB = "Verb";
  // values for types of tasks
  public static final String MPTASK = "MPTask";
  public static final String SINGLE_TASK = "Task";
  // names for Organization properties
  public static final String ORGANIZATION_NAME = "Organization_Name";
  // ORGANIZATION_KEY_NAME is compared to ORGANIZATION_RELATED_TO
  public static final String ORGANIZATION_KEY_NAME = "Organization_Key_Name";
  public static final String ORGANIZATION_RELATED_TO = "Related_To";
  public static final String ORGANIZATION_ROLE = "Role";
  // names for Allocation Result properties
  public static final String ESTIMATED_ALLOCATION_RESULT_END_TIME = "Estimated_End_Time";
  public static final String ESTIMATED_ALLOCATION_RESULT_FAILURE_REASON = "Estimated_Failure_Reason";
  public static final String REPORTED_ALLOCATION_RESULT_END_TIME = "Reported_End_Time";
  public static final String REPORTED_ALLOCATION_RESULT_FAILURE_REASON = "Reported_Failure_Reason";
  public static final String OBSERVED_ALLOCATION_RESULT_END_TIME = "Observed_End_Time";
  public static final String OBSERVED_ALLOCATION_RESULT_FAILURE_REASON = "Observed_Failure_Reason";
  // argument to plan object filter URL
  public static final String PLAN_OBJECTS_TO_IGNORE = "planObjectsToIgnore";
  // for communities
  public static final String COMMUNITY_MEMBERS = "Members";
}





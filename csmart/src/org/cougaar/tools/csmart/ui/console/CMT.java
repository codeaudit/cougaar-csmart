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

package org.cougaar.tools.csmart.ui.console;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import javax.swing.*;
import java.io.IOException;
import org.cougaar.util.Parameters;
import org.cougaar.util.DBProperties;
import org.cougaar.util.DBConnectionPool;

import org.cougaar.tools.csmart.societies.database.DBUtils;

public class CMT {

    public static boolean execute = true; // do go to db

    public static final String DATABASE = "org.cougaar.configuration.database";
    public static final String USER = "org.cougaar.configuration.user";
    public static final String PASSWORD = "org.cougaar.configuration.password";
    public static final String QUERY_FILE = "CMT.q";
    public static final String ASSEMBLYID_QUERY = "queryAssemblyID";
    public static final String asbPrefix="V4_";
    public static boolean traceQueries = true;

    // SHOULD REVISE CODE TO USE SINGLE dbp INSTANCE FOR ALL QUERIES
    protected static DBProperties dbp = null;

    public CMT() {
    }

    public static void setTraceQueries(Boolean b){
	traceQueries = b.booleanValue();
    }


    //NEW CODE
    public static void clearAllCMTAssemblies(){
	deleteItems(asbPrefix+"asb_component_hierarchy","assembly_id","assembly_id");
	deleteItems(asbPrefix+"asb_agent_pg_attr","assembly_id","assembly_id");
	deleteItems(asbPrefix+"asb_agent_relation","assembly_id","assembly_id");
	deleteItems(asbPrefix+"asb_component_arg","assembly_id","assembly_id");
	deleteItems(asbPrefix+"alib_component","assembly_id","assembly_id");
	deleteItems(asbPrefix+"asb_assembly","assembly_id","assembly_id");
    }

    public static void clearUnusedCMTassemblies(){
	Set unusedAssemblies=querySet("unusedAssemblies",new HashMap());
	Iterator i = unusedAssemblies.iterator();
	while (i.hasNext()) {
	    clearCMTasb((String)i.next());
	}
    }

    
    public static void clearCMTAssembly(String cfw_g_id, String[] threads){
	String assembly_id = getAssemblyID(cfw_g_id,threads,new HashMap());
	clearCMTasb(assembly_id);
    }
    
    public static String tabbrev(String thread){
	if(thread.equals("STRATEGIC-TRANS")) return "S";
	else if(thread.equals("THEATER-TRANS")) return "T";
	else if(thread.equals("CLASS-1")) return "1";
	else if(thread.equals("CLASS-3")) return "3";
	else if(thread.equals("CLASS-4")) return "4";
	else if(thread.equals("CLASS-5")) return "5";
	else if(thread.equals("CLASS-8")) return "8";
	else if(thread.equals("CLASS-9")) return "9";
	else return "";
    }

    public static String getAssemblyID(String cfwGroupId,String[] threads, Map clones){
	String threadIDs = "";
	for (int i=0;i<threads.length;i++){threadIDs=threadIDs+tabbrev(threads[i]);}
	String cloneCTs = "";
	Iterator i=clones.keySet().iterator();
	while(i.hasNext()){cloneCTs=cloneCTs+clones.get(i.next());}
	return "CMT-"+cfwGroupId+"{"+threadIDs+"}"+cloneCTs;
    }

    public static void clearCMTasb(String assembly_id){
	Set unusedAssemblies=querySet("unusedAssemblies",new HashMap());
	if(unusedAssemblies.contains(assembly_id)){
	    reallyClearCMTasb(assembly_id);
	}
    }

    public static void reallyClearCMTasb(String assembly_id){
	deleteItems(asbPrefix+"asb_component_hierarchy", "assembly_id", sqlQuote(assembly_id));
	deleteItems(asbPrefix+"asb_agent", "assembly_id", sqlQuote(assembly_id));
	deleteItems(asbPrefix+"asb_agent_pg_attr", "assembly_id", sqlQuote(assembly_id));
	deleteItems(asbPrefix+"asb_agent_relation", "assembly_id", sqlQuote(assembly_id));
	deleteItems(asbPrefix+"asb_component_arg", "assembly_id", sqlQuote(assembly_id));
	deleteItems(asbPrefix+"asb_oplan_agent_attr", "assembly_id", sqlQuote(assembly_id));
	deleteItems(asbPrefix+"asb_oplan", "assembly_id", sqlQuote(assembly_id));
	deleteItems(asbPrefix+"asb_assembly", "assembly_id", sqlQuote(assembly_id));

    }


    public static boolean hasRows (String query,Map substitutions ) {
	String dbQuery = DBUtils.getQuery(query, substitutions, QUERY_FILE);
	return hasRows(dbQuery);
    }

    public static boolean hasRows(String table, String column, String val){
	String dbQuery = "select * from "+table.toUpperCase()+ " where "+column+"="+val;
	return hasRows(dbQuery);
    }

    public static boolean hasRows(String dbQuery){
	ResultSet rs = null;
	try {
	    StringBuffer q = new StringBuffer();
	    Connection conn = DBUtils.getConnection(QUERY_FILE);
	    Statement stmt = null;
	    try {
		stmt = conn.createStatement();
		rs = stmt.executeQuery(dbQuery);
		if(rs.next()){
		    return true;
		} else {
		    return false;
		}
	    } finally {
		conn.close();
		rs.close();
		stmt.close();	
	    } 
	} catch (Exception e) {
	    System.out.println("hasRows"+dbQuery);
	    e.printStackTrace();
	    throw new RuntimeException("Error" + e);
	}
    }

    public static String sqlThreads(String[] threads){
	String threadIDs = "('BASE','STRATEGIC-TRANS','THEATER-TRANS','"+threads[0]+"'";
	for (int i=1;i<threads.length;i++){threadIDs=threadIDs+",'"+threads[i]+"'";}
	threadIDs=threadIDs+")";
	return threadIDs;
    }

    private static String sqlListFromQuery (String query, Map substitutions) {
	String dbQuery = DBUtils.getQuery(query, substitutions, QUERY_FILE);
	try {
	    StringBuffer q = new StringBuffer();
	    Connection conn = DBUtils.getConnection(QUERY_FILE);
	    try {
		Statement stmt = conn.createStatement();	
		ResultSet rs = stmt.executeQuery(dbQuery);
		boolean first = true;
		q.append(" (");
		while (rs.next()) {
		    if (first) {
			first = false;
		    } else {
			q.append(", ");
		    }
		    q.append("'").append(rs.getString(1)).append("'");
		}
		rs.close();
		stmt.close();
	    } finally {
		conn.close();
	    }
	    q.append(')');
	    return q.toString();
	}
	catch (Exception e) {
	    System.out.println("sqlListFromQuery: "+dbQuery);
	    e.printStackTrace();
	    throw new RuntimeException("Error" + e);
	}
    }

    public static Set allThreads = new HashSet();
    public static String[] getAllThreads(){
	allThreads.add("STRATEGIC-TRANS");
	allThreads.add("THEATER-TRANS");
	allThreads.add("CLASS-1");
	allThreads.add("CLASS-3");
	allThreads.add("CLASS-4");
	allThreads.add("CLASS-5");
	allThreads.add("CLASS-9");
	return orderThreads(allThreads);
    }

    public static void main(String[] args) {
	createBaseExperiment(args[0], args[1]);
    }

    public static void createBaseExperiment(String expt_name, String cfw_group_id){
	String cmtASB = createCMTasb(expt_name, cfw_group_id ,getAllThreads(),new HashMap());
	createCSMARTExperiment(expt_name, cfw_group_id, cmtASB);
    }

    public static String createCSMARTExperiment(String expt_id, String cfw_group_id, String assembly_id){
	createExperiment(expt_id,cfw_group_id);
	return addAssembly(expt_id,assembly_id,"TRIAL");
    }

    /**
     * Add the named trial to the experiment.  The name supplied is
     * the human readable name; the value returned is the new trial id or null.
     */

    public static String addTrialName(String experiment_id, String trialName) {
	String trial_id = experiment_id+"."+trialName;
	Map subs = new HashMap();
	subs.put(":trial_name",trialName);
	subs.put(":experiment_id",experiment_id);
	subs.put(":trial_id",trial_id);
	executeQuerySet("addTrialName",subs);
	return trial_id;
    }

    public static String addAssembly(String experiment_id, String assemblyId, String trialName) {
	String trial_id = addTrialName(experiment_id,trialName);
	Map subs = new HashMap();
	subs.put(":trial_name",trialName);
	subs.put(":experiment_id",experiment_id);
	subs.put(":trial_id",trial_id);
	subs.put(":assembly_id",assemblyId);
	executeQuerySet("addAssembly",subs);
	return trial_id;
    }


    /*
     * Create a new experiment based on the given societyTemplate
     *  returns the experiment_id
     */

    public static String createExperiment(String experimentName,String cfw_group_id) {
	Map subs = new HashMap();
	subs.put(":cfw_group_id",cfw_group_id);
	subs.put(":experiment_id",experimentName);
	executeQuerySet("createExperiment",subs);
	return experimentName;
    }


    public static String createCMTasb(String assembly_description,String cfw_g_id,String[] threads, Map clones){
	clearUnusedCMTassemblies();
	String assembly_id = getAssemblyID(cfw_g_id, threads, clones);
	if (hasRows(asbPrefix+"asb_assembly", "assembly_id", sqlQuote(assembly_id))){
	    System.out.println("Not creating new assembly for: "+assembly_id+" which is already in the DB");
	    return assembly_id;
	} else {
	    String sqlThreads = sqlThreads(threads);
	    Map subs = new HashMap();
	    addSubs(subs,":cfw_group_id",cfw_g_id);
	    addSubs(subs,":assembly_id",assembly_id);
	    // to make a short name for temporary tables
	    String shortASBId = ((String)assembly_id.substring(assembly_id.length()/2)).replace('-','_');
	    shortASBId = shortASBId.replace('{','_');
	    shortASBId = shortASBId.replace('}',' ');
	    addSubs(subs,":short_assembly_id",shortASBId);
	    addSubs(subs,":threads", sqlThreads);
	    addSubs(subs,":oplan_ids","('093FF')");
	    addSubs(subs,":cfw_instances",sqlListFromQuery("getCFWInstancesFromGroup",subs));

	    //  	    System.out.println("subs = "+subs);

	    if(!hasRows(asbPrefix+"asb_assembly","assembly_id",sqlQuote(assembly_id))){
				// this query is legal in both Oracle and mySQL
		dbUpdate("insertASBAssembly",
			 addSubs(addSubs(new HashMap(),":assembly_id",sqlQuote(assembly_id)),
				 ":assembly_description",sqlQuote(assembly_description)));

		executeQuerySet("addNewBaseAgentAlibComponents",subs);
		Iterator i=clones.keySet().iterator();
		while(i.hasNext()){
		    executeQuerySet("addNewClonedAgentAlibComponents",
					  addSubs(addSubs(new HashMap(),":org_group_id",i.next()),
						  ":n",clones.get(i.next())));
		}

		executeQuerySet("addBaseASBAgents", subs);
		i=clones.keySet().iterator();
		while(i.hasNext()){
		    executeQuerySet("addClonedASBAgents",
					  addSubs(addSubs(addSubs(new HashMap(),":org_group_id",i.next()),
							  ":n",clones.get(i.next())),
						  ":assembly_id",assembly_id));
		}

                // this must occur AFTER the ASB_agents table is filled in, to allow for handling multiplicity
		executeQuerySet("addNewPluginAlibComponents",subs);
		executeQuerySet("addPluginASBComponentHierarchy",subs);
		executeQuerySet("addAgentNameComponentArg",subs);
		executeQuerySet("addASBAgentPGAttr",subs);
		executeQuerySet("addASBAgentRelationToBase",subs);
		executeQuerySet("addASBAgentRelationToCloneset",subs);
		executeQuerySet("addASBAgentHierarchyRelationToBase",subs);
		executeQuerySet("addASBAgentHierarchyRelationToCloneset",subs);
		executeQuerySet("addPluginAgentASBComponentArg",subs);
		executeQuerySet("addPluginOrgtypeASBComponentArg",subs);
		executeQuerySet("addPluginAllASBComponentArg",subs);
		executeQuerySet("addASBOplans",subs);
		executeQuerySet("addASBOplanAgentAttr",subs);
	    }
	}
	return assembly_id;
    }

    /*
     * Returns Map where the keys are human readable names (Strings) and
     * the values are experiment ids (Strings).
     */

    public static SortedMap getExperimentNames() {
	return queryHT("getExperimentNames",new HashMap());
    }

    public static SortedMap getTrialNames(String experiment_id) {
	return queryHT("getTrialNames",addSubs(new HashMap(),":experiment_id",experiment_id));
    }

    public static String getTrialId(String experiment_id) {
	return query1String("getTrialId",addSubs(new HashMap(),":experiment_id",experiment_id)); 
    }

    /*
     * Returns society templates for an experiment.
     * Returns Map where human readable names are keys
     * and ids are values.
     */

    public static String getSocietyTemplateForExperiment(String experiment_id) {
	Map subs = new HashMap();
	subs.put(":experiment_id",experiment_id);
	return query1String("getSocietyTemplateForExperiment", subs);
    }



    public static void addCSMARTAssembly(String assembly_id, String assembly_description) {
	if (!hasRows("V4_ASB_ASSEMBLY", "assembly_id", assembly_id)){
	    Map subs = new HashMap();
	    subs.put(":assembly_id",assembly_id);
	    subs.put(":assembly_description",assembly_description);
	    executeQuerySet("addCSMARTAssembly",subs);
	}
    }

    public static String addNodeAssignments(Map nodeTable ,String assemblyName) {
	addCSMARTAssembly(assemblyName,assemblyName);
	Map subs = new HashMap();
	subs.put(":assembly_id",assemblyName);

	Iterator nt = nodeTable.keySet().iterator();
	while(nt.hasNext()){
	    String nodename = (String)nt.next();
	    Iterator at = ((Set)nodeTable.get(nodename)).iterator();
	    while(at.hasNext()){
		subs = new HashMap();
		subs.put(":assembly_id",assemblyName);
		subs.put(":nodename",nodename);
		subs.put(":agentname",(String)at.next());
		executeQuerySet("addNodeAssignments",subs);
	    }
	}
	return assemblyName;
    }


    public static String addMachineAssignments(Map machineTable ,String assemblyName) {
	Iterator mt = machineTable.keySet().iterator();
	while(mt.hasNext()){
	    String machinename = (String)mt.next();
	    Iterator nt = ((Set)machineTable.get(machinename)).iterator();
	    while(nt.hasNext()){
		Map subs = new HashMap();
		subs.put(":assembly_id",assemblyName);
		subs.put(":machinename",machinename);
		subs.put(":nodename",(String)nt.next());
		if(!hasRows("addMachineAssignmentsUpdate",subs)){// Already exists
		    executeQuerySet("addMachineAssignmentsInsert",subs);
		}
	    }
	}
	return assemblyName;
    }


    /**
     * Returns Map of all society templates in database.
     */

    public static SortedMap getSocietyTemplates() {
	return queryHT("getSocietyTemplates",new HashMap());
    }

    public static SortedMap getOrganizationGroups(String experiment_id) {
	Map subs = new HashMap();
	subs.put(":experiment_id",experiment_id);
	return queryHT("getOrganizationGroups",subs);
    }


    public static Set getOrganizationsInGroup(String experiment_id, String groupId){
	Map subs = new HashMap();
	subs.put(":experiment_id",experiment_id);
	subs.put(":group_id",groupId);
	return querySet("getOrganizationsInGroup",subs);
    }

    public static boolean isULThreadSelected(String trialId, String threadName) {
	Map subs = new HashMap();
	subs.put(":trial_id",trialId);
	subs.put(":thread_id",getThreadId(threadName));
	return (hasRows("isULThreadSelected",subs));
    }

    public static String getThreadId(String thread_name){
	if (thread_name.equals("Subsistence (Class 1)")){
	    return "CLASS-1";
	} else if(thread_name.equals("Fuel (Class 3)")){
	    return "CLASS-3";
	} else if (thread_name.equals("Construction Material (Class 4)")){
	    return"CLASS-4";
	} else if (thread_name.equals("Ammunition (Class 5)")){
	    return"CLASS-5";}
	else if	(thread_name.equals("Spare Parts (Class 9)")){
	    return"CLASS-9";
	} else if (thread_name.equals("CLASS-1")){
	    return "CLASS-1";
	} else if (thread_name.equals("CLASS-3")){
	    return "CLASS-3";
	} else if (thread_name.equals("CLASS-4")){
	    return"CLASS-4";
	} else if (thread_name.equals("CLASS-5")){
	    return"CLASS-5";
	} else if (thread_name.equals("CLASS-9")){
	    return "CLASS-9";
	} else return null;
    }

    public static void setULThreadSelected(String trialId, String threadName, boolean selected) {
	Map subs = new HashMap();
	subs.put(":trial_id",trialId);
	subs.put(":thread_id",getThreadId(threadName));
	if(selected){
	    if(!isULThreadSelected(trialId,threadName)){
		executeQuerySet("setULThreadSelected",subs);
	    }
	} else {
	    if(isULThreadSelected(trialId,threadName)){
		dbDelete("setULThreadNotSelected",subs);
	    }
	}
    }

    public static boolean isGroupSelected(String trial_id, String group_name) {
	String group_id = getGroupId(trial_id,group_name);
	Map subs = new HashMap();
	subs.put(":trial_id",trial_id);
	subs.put(":group_id",group_id);
	return (hasRows("isGroupSelected",subs));
    }

    public static String getGroupId(String trial_id,String group_name){
	Map subs = new HashMap();
	subs.put(":trial_id",trial_id);
	subs.put(":group_name",group_name);
	return query1String("getGroupId",subs);
    }
    
    public static void setGroupSelected(String trial_id, String group_name, boolean selected) {
	String group_id = getGroupId(trial_id,group_name);
	Map subs = new HashMap();
	subs.put(":trial_id",trial_id);
	subs.put(":group_id",group_id);
	if(isGroupSelected(trial_id,group_name)) {
	    if(!selected){
		dbDelete("setGroupNotSelected",subs);
	    }
	} else {
	    if(selected){
		executeQuerySet("setGroupSelected",subs);
	    }
	}
    }

    public static int getMultiplier(String trial_id, String group_name) {
	String group_id = getGroupId(trial_id,group_name);
	Map subs = new HashMap();
	subs.put(":trial_id",trial_id);
	subs.put(":group_id",group_id);
	Integer i = query1Int("getMultiplier",subs);
	if(i==null) return 1;
	return i.intValue();
    }

    public static void setMultiplier(String trial_id, String group_name, int value) {
	String group_id = getGroupId(trial_id,group_name);
	Map subs = new HashMap();
	subs.put(":trial_id",trial_id);
	subs.put(":group_id",group_id);
	subs.put(":value", new Integer(value));
	// this query is legal in both Oracle and mySQL
	dbUpdate("setMultiplier",subs);
    }

    /**
     * Clone specified experiment_id with the specified human readable name.
     * Returns new experiment id.
     * Creates new trial id for new experiment id.
     */

    public static String cloneExperiment(String experiment_id, String newName) {
	Map subs = new HashMap();
	//String new_expt_id = "EXPT-"+ query1Int("nextExperimentId", subs);
	String new_expt_id = getNextId("nextExperimentIdHack","EXPT-");

	subs.put(":experiment_id",experiment_id);
	subs.put(":new_expt_id",new_expt_id);
	subs.put(":new_name",newName);
	addSubs(subs,":short_assembly_id","CLONING");
	executeQuerySet("cloneExperimentEXPT_EXPERIMENT",subs);
	executeQuerySet("cloneExperimentEXPT_TRIAL",subs);
	executeQuerySet("cloneExperimentEXPT_TRIAL_THREAD",subs);
	executeQuerySet("cloneExperimentEXPT_TRIAL_ORG_MULT",subs);
	executeQuerySet("cloneExperimentEXPT_TRIAL_ASSEMBLY",subs);
	return new_expt_id;
    }

    public static String getNextId(String queryName, String prefix) {
        DecimalFormat format = new DecimalFormat(prefix + "0000");
	Map substitutions = new HashMap();
        substitutions.put(":max_id_pattern", prefix + "____");
        String id = format.format(1); // Default
        try {
	    Connection dbConnection = DBUtils.getConnection(QUERY_FILE);
            Statement stmt = dbConnection.createStatement();
            try {
		if(dbp==null)
		    dbp = DBProperties.readQueryFile(QUERY_FILE);
                String query = dbp.getQuery(queryName, substitutions);
		System.out.println("getNextId: "+query);
		ResultSet rs = stmt.executeQuery(query);
                try {
                    if (rs.next()) {
                        String maxId = rs.getString(1);
                        if (maxId != null) {
                            int n = format.parse(maxId).intValue();
  			    System.out.println("getNextId: n="+n+", maxId ="+maxId);
                            id = format.format(n + 1);
  			    System.out.println("getNextId: id="+id);
                        }
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Ignore exceptions and use default
        }
        return id;
    }

	

    public static String[] orderThreads(Set set){
	String[] threads = new String[set.size()];
	int i=0;
	if ( set.contains("STRATEGIC-TRANS")) {threads[i]="STRATEGIC-TRANS"; i++;}
	if ( set.contains("THEATER-TRANS")) {threads[i]="THEATER-TRANS"; i++;}
	if ( set.contains("CLASS-1")) {threads[i]="CLASS-1"; i++;}
	if ( set.contains("CLASS-3")){threads[i]= "CLASS-3"; i++;}
	if ( set.contains("CLASS-4")){threads[i]= "CLASS-4"; i++;}
	if ( set.contains("CLASS-5")){threads[i]= "CLASS-5"; i++;}
	if ( set.contains("CLASS-9")){threads[i]= "CLASS-9"; i++;}
	return threads;
    }


    /**
     * This MUST be called after calling setGroups, setThreads, or setMultipliers
     * to actually update the experiment.
     */

    public static void updateCMTAssembly(String experiment_id) {
	Map subs = new HashMap();
	subs.put(":experiment_id",experiment_id);
	Set threads = querySet("updateCMTAssemblyThreadID",subs);
	threads.add("STRATEGIC-TRANS");
	threads.add("THEATER-TRANS");
	String cfw_group_id = query1String("updateCMTAssemblyCFW_GROUP_ID",subs);
	Map clones = queryHT("updateCMTAssemblyClones",subs);
	String assembly_description = "assembly for: "+experiment_id;

	String assembly_id = createCMTasb(assembly_description,cfw_group_id,orderThreads(threads), clones);

	subs.put(":assembly_id",assembly_id);
	// this query is legal in both Oracle and mySQL
	dbUpdate("updateCMTAssembly",subs);
    }


    /*
     * Deletes the given experiment from the database. 
     * Gets rid of the trial and associated other data.
     * Does not get rid of the CMT assembly or other assemblies.
     */

    public static void deleteExperiment(String experiment_id) {
	boolean doIt = true;
	if(experiment_id.equals("EXPT_TRANS")){
	    doIt=false;
	    int response = 
		JOptionPane.showConfirmDialog(null,
					      "You seem to be trying to delete the base experiment -- do you REALLY want to do this?",
					      "Confirm Delete",
					      JOptionPane.YES_NO_OPTION);
      
	    if (response == JOptionPane.YES_OPTION) {
		doIt = true;
	    }
	}

	if (!doIt)
	    return;
	String expt_id = sqlQuote(experiment_id);
	String trial_id = sqlQuote(getTrialId(experiment_id));
	ArrayList queries = new ArrayList();
	queries.add(makeDeleteQuery(asbPrefix+"expt_trial_assembly", "expt_id", expt_id));
	queries.add(makeDeleteQuery(asbPrefix+"expt_trial_thread", "expt_id", expt_id));
	queries.add(makeDeleteQuery(asbPrefix+"expt_trial_org_mult", "expt_id", expt_id));
	queries.add(makeDeleteQuery(asbPrefix+"expt_trial_metric_prop", "trial_id", trial_id));
	queries.add(makeDeleteQuery(asbPrefix+"expt_trial_metric", "trial_id", trial_id));
	queries.add(makeDeleteQuery(asbPrefix+"expt_trial_mod_recipe", "trial_id", trial_id));
	queries.add(makeDeleteQuery(asbPrefix+"expt_trial", "expt_id", expt_id));
	queries.add(makeDeleteQuery(asbPrefix+"expt_experiment", "expt_id", expt_id));
	executeQueries(queries);
	clearUnusedCMTassemblies();
    }

    public static String makeDeleteQuery(String table, String column, String val) {
	return "delete from "+ table.toUpperCase() +" where "+column+"="+val;
    }
 

    public static void iterateUpdate(String op, String subs, Set items){
	Iterator i = items.iterator();
	Map m = new HashMap();
	while (i.hasNext()) {
	    dbUpdate(op,addSubs(m,subs,(String)i.next()));
	}
    }

    public static void executeQueries(ArrayList queries) {
	if (!execute)
	    return;
	String dbQuery = "";
	try {
	    Connection conn = DBUtils.getConnection(QUERY_FILE);
	    try {
		Statement stmt = conn.createStatement();	
		for (int i = 0; i < queries.size(); i++) {
		    dbQuery = (String)queries.get(i);
		    //          System.out.println("Query: " + dbQuery);
		    int count = stmt.executeUpdate(dbQuery);
		    //          System.out.println("Deleted "+count+" items from the database");
		}
		stmt.close();
	    } finally {
		conn.close();
	    }
	} catch (Exception e) {
	    System.out.println("dbDelete: "+dbQuery);
	    e.printStackTrace();
	    throw new RuntimeException("Error" + e);
	}
    }


    public static int deleteItems(String table, String column, String val){
	String dbQuery = "delete from "+ table.toUpperCase() +" where "+column+"="+val;
	int count=0;
	try {
	    Connection conn = DBUtils.getConnection(QUERY_FILE);
	    if(execute){
		try {
		    Statement stmt = conn.createStatement();	
                    //System.out.println("Query: " + dbQuery);
		    count = stmt.executeUpdate(dbQuery);
		    //  		    System.out.println("Deleted "+count+" items from the database");
		    stmt.close();

		} finally {
		    conn.close();
		}
	    }
	} catch (Exception e) {
	    System.out.println("dbDelete: "+dbQuery);
	    e.printStackTrace();
	    throw new RuntimeException("Error" + e);
	}
	return count;
    }

    public static Set querySet(String query, Map substitutions){
	String dbQuery = DBUtils.getQuery(query, substitutions, QUERY_FILE);
	Set s = new HashSet();
	try {
	    Connection conn = DBUtils.getConnection(QUERY_FILE);
	    try {
		Statement stmt = conn.createStatement();	
		ResultSet rs = stmt.executeQuery(dbQuery);
		while (rs.next()) {s.add(rs.getString(1));}
		rs.close();
		stmt.close();
	    } finally {
		conn.close();
	    }

	} catch (Exception e) {
	    System.out.println("querySet: "+dbQuery);
	    e.printStackTrace();
	    throw new RuntimeException("Error" + e);
	}
	return s;
    }


    public static SortedMap queryHT(String query, Map substitutions){
	String dbQuery = DBUtils.getQuery(query, substitutions, QUERY_FILE);
        Connection conn = null;
	Map ht = new TreeMap();
	try {
	    conn = DBUtils.getConnection(QUERY_FILE);
	    if (conn == null) {
		System.out.println("CMT:queryHT: Got no connection");
		return (SortedMap)ht;
	    }
	    try {
		Statement stmt = conn.createStatement();	
		ResultSet rs = stmt.executeQuery(dbQuery);
		while (rs.next()) {
		    ht.put(rs.getString(1),rs.getString(2));
		}
		rs.close();
		stmt.close();
	    } finally {
		if (conn != null)
		    conn.close();
	    }

	} catch (Exception e) {
	    System.out.println("queryHT: "+dbQuery);
	    e.printStackTrace();
	    throw new RuntimeException("Error" + e);
	}
	return (SortedMap)ht;
    }


    public static String query1String(String query, Map substitutions){
	String dbQuery = DBUtils.getQuery(query, substitutions, QUERY_FILE);

	String res = null;
	try {
	    Connection conn = DBUtils.getConnection(QUERY_FILE);
	    try {
		Statement stmt = conn.createStatement();	
		ResultSet rs = stmt.executeQuery(dbQuery);
		if (rs.next()) {
		    
		    res=rs.getString(1);
		}
		rs.close();
		stmt.close();
	    } finally {
		conn.close();
	    }

	} catch (Exception e) {
	    System.out.println("query1String: "+dbQuery);
	    e.printStackTrace();
	    throw new RuntimeException("Error" + e);
	}
	return res;
    }

    public static Integer query1Int(String query, Map substitutions){
	String dbQuery = DBUtils.getQuery(query, substitutions, QUERY_FILE);
	Integer res = null;
	try {
	    Connection conn = DBUtils.getConnection(QUERY_FILE);
	    try {
		Statement stmt = conn.createStatement();	
		ResultSet rs = stmt.executeQuery(dbQuery);
		if (rs.next()) {
		    res=new Integer(rs.getInt(1));
		}
		rs.close();
		stmt.close();
	    } finally {
		conn.close();
	    }

	} catch (Exception e) {
	    System.out.println("query1Int: "+dbQuery);
	    e.printStackTrace();
	    throw new RuntimeException("Error" + e);
	}
	return res;
    }


    //  	while (i.hasNext()) {
    //  	    r = (Relationship)i.next();

    public static Map addSubs(Map subs, String subName, Object subItem){
	subs.put(subName,subItem);
	return subs;
    }
    
    public static void executeQuerySet(String queryBase, Map substitutions){
	if(traceQueries){
	    System.out.println("\nexecuteQuerySet "+queryBase);
	}
	String qs = null;
	try{
	    qs = DBUtils.getQuery(queryBase+"Queries", substitutions, QUERY_FILE);
	} catch (IllegalArgumentException e)
	    {
		// this is expected if there is no "Queries" item
	    }
	String dbQuery=null;
	if(qs==null){
	    dbQuery = DBUtils.getQuery(queryBase, substitutions, QUERY_FILE);
	    tryDBExecute(dbQuery,queryBase+"Queries");
	} else {
	    StringTokenizer queries = new StringTokenizer(qs);
	    while (queries.hasMoreTokens()) {
		String queryName = queries.nextToken();
		dbQuery = DBUtils.getQuery(queryName, substitutions,QUERY_FILE);
		tryDBExecute(dbQuery,queryName);
	    }
	}
    }

    public static void tryDBExecute(String dbQuery, String queryName){
	if(dbQuery!=null){
	    dbExecute(dbQuery,"Execute");
	} else {
	    System.out.println("Error -- can't find query: "+queryName);
	    Thread.dumpStack();
	}
    }

    public static int dbExecute(String dbQuery, String type){
	int count=0;
	try {
	    Connection conn = DBUtils.getConnection(QUERY_FILE);
	    if(traceQueries){
		System.out.println("\nStarting dbExecute at "+new Date()+"\n\n"+dbQuery);
	    }
	    if(execute){
		try {
		    Statement stmt = conn.createStatement();	
		    count = stmt.executeUpdate(dbQuery);
		    //  		    System.out.println("db"+type+" "+type+"d "+count+" items in the database"); 
		    stmt.close();
		} finally {
		    conn.close();
		}
	    }
	} catch (Exception e) {
	    System.out.println("db"+type+": "+dbQuery);
	    e.printStackTrace();
	    throw new RuntimeException("Error" + e);
	}
	if(traceQueries){
	    System.out.println("\nexiting dbExecute with "+count+" at "+new Date());
	}
	return count;
    }

    public static int dbInsert(String query, Map substitutions){
	String dbQuery = DBUtils.getQuery(query, substitutions, QUERY_FILE);
	return dbExecute(dbQuery,"Insert");
    }

    public static int dbUpdate(String query, Map substitutions){
	String dbQuery = DBUtils.getQuery(query, substitutions, QUERY_FILE);
	return dbExecute(dbQuery,"Update");
    }

    public static int dbDelete(String query, Map substitutions){
	String dbQuery = DBUtils.getQuery(query, substitutions, QUERY_FILE);
	return dbExecute(dbQuery,"Delete");
    }
    private static String sqlQuote(String s) {
        if (s == null) return "null";
        int quoteIndex = s.indexOf('\'');
        while (quoteIndex >= 0) {
            s = s.substring(0, quoteIndex) + "''" + s.substring(quoteIndex + 1);
            quoteIndex = s.indexOf('\'', quoteIndex + 2);
        }
        return "'" + s + "'";
    }

}


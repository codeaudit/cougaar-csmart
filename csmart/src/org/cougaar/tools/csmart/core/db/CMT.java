/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.tools.csmart.core.db;


import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.DBProperties;
import org.cougaar.util.Parameters;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Pattern;

/**
 * Methods for constructing a society in the configuration database' assembly
 * portion, from the CFW portion of the database. <br>
 * That is, create and manage the CMT assemblies.
 */
public class CMT {
  
  static final String QUERY_FILE = "CMT.q";
  static final String asbPrefix="";
  // SHOULD REVISE CODE TO USE SINGLE dbp INSTANCE FOR ALL QUERIES
  protected static DBProperties dbp = null;
  
  public CMT() {
  }
  
  static void clearUnusedCMTassemblies(){
    // FIXME!! This query may have problems in MySQL!!!
    Set unusedAssemblies=DBUtils.querySet("unusedAssemblies",new HashMap(), QUERY_FILE);
    Iterator i = unusedAssemblies.iterator();
    while (i.hasNext()) {
      reallyClearCMTasb((String)i.next());
    }
  }  
  
  static String tabbrev(String thread){
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
  
  /**
   * Constructs an Assembly ID, based on the GroupID and the 
   * selected threads.  <br>
   * A final constructed Assembly ID will look like: <br>
   * CMT-<i>GroupId</i>{<i>thread numbers</i>}<i>clonset</i>
   *
   * @param cfwGroupId 
   * @param threads 
   * @param clones 
   * @return a <code>String</code> value
   */
  public static String getAssemblyID(String cfwGroupId, String[] threads, Map clones){

    String threadIDs = "";
    for (int i=0;i<threads.length;i++){threadIDs=threadIDs+tabbrev(threads[i]);}
    String cloneCTs = "";
    Iterator i=clones.keySet().iterator();
    while(i.hasNext()){cloneCTs=cloneCTs+clones.get(i.next());}
    return "CMT-"+cfwGroupId+"{"+threadIDs+"}"+cloneCTs;
  }
  
  public static String getDescription(String cfwGroupId, String[] threads) {
    String name = cfwGroupId.substring(0, cfwGroupId.indexOf("-TRANS"));
    String threadIDs = "";
    for (int i=0;i<threads.length;i++) {
      threadIDs=threadIDs+tabbrev(threads[i]);
    }

    return name + prettyThreads(cfwGroupId, threadIDs);
  }

  private static String prettyThreads(String name, String threads) {
    String result = "";

    if(Pattern.compile("-STUB").matcher(name).find()) {
      result = " Threads ";
    } else {
      result = " TRANSCOM, Threads ";
    }
    
    result = result + threads.charAt(2);
    for(int i=3; i < threads.length(); i++) {
      result = result + ", " + threads.charAt(i);
    }

    return result;
  }

  /**
   * Despite the name, this method
   * deletes the contents of any assembly type.
   **/
  public static void reallyClearCMTasb(String assembly_id){
    if (assembly_id == null || assembly_id.equals(""))
      return;

    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.CMT");

    // Avoid base assemblies here
    if (assembly_id.startsWith("CMT") && assembly_id.indexOf("134589") != -1) {
      if (log.isInfoEnabled()) {
	log.info("Asked to delete base assembly " + assembly_id, new Throwable());
      }
      // base assembly I think.
      int response = 
	JOptionPane.showConfirmDialog(null,
				      "You seem to be trying to delete a base society (id " + assembly_id + ") -- do you REALLY want to do this?",
				      "Confirm Delete",
				      JOptionPane.YES_NO_OPTION);
      
      if (response != JOptionPane.YES_OPTION) {
	return;
      }
    }

    // Add in base community assembly avoidance here too?
    if (assembly_id.equals("COMM-DEFAULT_CONFIG")) {
      if (log.isInfoEnabled()) {
	log.info("Asked to delete base comm assembly " + assembly_id, new Throwable());
      }
      // base assembly I think.
      int response = 
	JOptionPane.showConfirmDialog(null,
				      "You seem to be trying to delete the base community definition (id " + assembly_id + ") -- do you REALLY want to do this?",
				      "Confirm Delete",
				      JOptionPane.YES_NO_OPTION);
      
      if (response != JOptionPane.YES_OPTION) {
	return;
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("Deleting assembly " + assembly_id);
    }

    DBUtils.deleteItems(asbPrefix+"asb_component_hierarchy", "assembly_id", DBUtils.sqlQuote(assembly_id), QUERY_FILE);
    DBUtils.deleteItems(asbPrefix+"asb_agent", "assembly_id", DBUtils.sqlQuote(assembly_id), QUERY_FILE);
    DBUtils.deleteItems(asbPrefix+"asb_agent_pg_attr", "assembly_id", DBUtils.sqlQuote(assembly_id), QUERY_FILE);
    DBUtils.deleteItems(asbPrefix+"asb_agent_relation", "assembly_id", DBUtils.sqlQuote(assembly_id), QUERY_FILE);
    DBUtils.deleteItems(asbPrefix+"asb_component_arg", "assembly_id", DBUtils.sqlQuote(assembly_id), QUERY_FILE);
    DBUtils.deleteItems(asbPrefix+"asb_assembly", "assembly_id", DBUtils.sqlQuote(assembly_id), QUERY_FILE);    

    // Added community tables to those that get cleaned out
    DBUtils.deleteItems("community_entity_attribute", "assembly_id", DBUtils.sqlQuote(assembly_id), QUERY_FILE);    
    DBUtils.deleteItems("community_attribute", "assembly_id", DBUtils.sqlQuote(assembly_id), QUERY_FILE);    

  }

  static boolean hasRows (String query,Map substitutions ) {
    String dbQuery = DBUtils.getQuery(query, substitutions, QUERY_FILE);
    return hasRows(dbQuery);
  }
  
  static boolean hasRows(String table, String column, String val){
    String dbQuery = "select * from "+table+ " where "+column+"="+val;
    return hasRows(dbQuery);
  }
  
  static boolean hasRows(String dbQuery){
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.CMT");

    ResultSet rs = null;
    boolean ret = false;
    try {
      Connection conn = DBUtils.getConnection(QUERY_FILE);
      Statement stmt = null;
      try {
	stmt = conn.createStatement();
	rs = stmt.executeQuery(dbQuery);
	if(rs.next()){
	  ret = true;
	} else {
	  ret = false;
	}
      } finally {
	try {
	  rs.close();
	  stmt.close();	
	} catch (SQLException e) {
	  if(log.isErrorEnabled()) {
	    log.error("hasRows"+dbQuery + " while closing statement", e);
	  }
	}
	conn.close();
      } 
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
         log.error("hasRows"+dbQuery, e);
      }
      throw new RuntimeException("Error" + e);
    }
    
    return ret;
  }
  
  static String sqlThreads(String[] threads){
    String threadIDs = "('BASE','STRATEGIC-TRANS','THEATER-TRANS','"+threads[0]+"'";
    for (int i=1;i<threads.length;i++){threadIDs=threadIDs+",'"+threads[i]+"'";}
    threadIDs=threadIDs+")";
    return threadIDs;
  }
  
  static String sqlListFromQuery (String query, Map substitutions) {
    Logger qLog = CSMART.createLogger("queries");
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.CMT");

    if (qLog.isDebugEnabled()){
      qLog.debug("\nsqlListFromQuery "+query);
    }
    String dbQuery = DBUtils.getQuery(query, substitutions, QUERY_FILE);
    if (qLog.isDebugEnabled()){
      qLog.debug("\nsqlListFromQuery "+dbQuery);
    }
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
      if(log.isErrorEnabled()) {
         log.error("sqlListFromQuery: "+dbQuery, e);
      }
      throw new RuntimeException("Error" + e);
    }
  }

  // The way selectable threads are named in the DB
  public static final String[] ULDBThreads = {
    "CLASS-1", "CLASS-3", "CLASS-4", "CLASS-5", "CLASS-9"
  };
  
  static Set allThreads = new HashSet();
  static String[] getAllThreads(){
    allThreads.add("STRATEGIC-TRANS");
    allThreads.add("THEATER-TRANS");
    allThreads.add("CLASS-1");
    allThreads.add("CLASS-3");
    allThreads.add("CLASS-4");
    allThreads.add("CLASS-5");
    allThreads.add("CLASS-8");
    allThreads.add("CLASS-9");
    return orderThreads(allThreads);
  }
  
  public static void main(String[] args) {
    createBaseExperiment(args[0], args[1]);
  }
  
  static void createBaseExperiment(String expt_name, String cfw_group_id){
    String cmtASB = createCMTasb(cfw_group_id ,getAllThreads(),new HashMap());
    createCSMARTExperiment(expt_name, cfw_group_id, cmtASB);
  }
  
  static String createCSMARTExperiment(String expt_id, String cfw_group_id, String assembly_id){
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
    DBUtils.executeQuerySet("addTrialName",subs, QUERY_FILE);
    return trial_id;
  }
  
  public static String addAssembly(String experiment_id, String assemblyId, String trialName) {
    String trial_id = addTrialName(experiment_id,trialName);
    Map subs = new HashMap();
    subs.put(":trial_name",trialName);
    subs.put(":experiment_id",experiment_id);
    subs.put(":trial_id",trial_id);
    subs.put(":assembly_id",assemblyId);
    DBUtils.executeQuerySet("addAssembly",subs, QUERY_FILE);
    DBUtils.executeQuerySet("addRuntimeAssembly",subs, QUERY_FILE);
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
    DBUtils.executeQuerySet("createExperiment",subs, QUERY_FILE);
    return experimentName;
  }

  static String createCMTasb(String cfw_g_id,String[] threads, Map clones){
    Logger qLog = CSMART.createLogger("queries");

    clearUnusedCMTassemblies();
    String assembly_id = getAssemblyID(cfw_g_id, threads, clones);

    if (hasRows(asbPrefix+"asb_assembly", "assembly_id", DBUtils.sqlQuote(assembly_id))){
      if (qLog.isDebugEnabled()) {
	qLog.debug("Not creating new assembly for: "+assembly_id+" which is already in the DB");
      }
      return assembly_id;
    } else {
      String sqlThreads = sqlThreads(threads);
      Map subs = new HashMap();
      DBUtils.addSubs(subs,":cfw_group_id",cfw_g_id);
      DBUtils.addSubs(subs,":assembly_id",assembly_id);
      // to make a short name for temporary tables
      String shortASBId = ((String)assembly_id.substring(assembly_id.length()/2)).replace('-','_');
      shortASBId = shortASBId.replace('{','_');
      shortASBId = shortASBId.replace('}',' ');
      // Can shortASBId ever now start with a space or other
      // whitespace char? That would be bad, so trim it.
      DBUtils.addSubs(subs,":short_assembly_id",shortASBId.trim());
      DBUtils.addSubs(subs,":threads", sqlThreads);
      DBUtils.addSubs(subs,":oplan_ids","('093FF')");
      DBUtils.addSubs(subs,":cfw_instances",sqlListFromQuery("getCFWInstancesFromGroup",subs));
      
      if(!hasRows(asbPrefix+"asb_assembly","assembly_id",DBUtils.sqlQuote(assembly_id))){
	// this query is legal in both Oracle and mySQL
	DBUtils.dbUpdate("insertASBAssembly",
		 DBUtils.addSubs(DBUtils.addSubs(new HashMap(),":assembly_id",DBUtils.sqlQuote(assembly_id)),
			 ":assembly_description",DBUtils.sqlQuote(getDescription(cfw_g_id, threads))), QUERY_FILE);
	
	DBUtils.executeQuerySet("addNewBaseAgentAlibComponents",subs, QUERY_FILE);
	Iterator i=clones.keySet().iterator();
	while(i.hasNext()){
	  DBUtils.executeQuerySet("addNewClonedAgentAlibComponents",
			  DBUtils.addSubs(DBUtils.addSubs(new HashMap(),":org_group_id",i.next()),
				  ":n",clones.get(i.next())), QUERY_FILE);
	}
	
	DBUtils.executeQuerySet("addBaseASBAgents", subs, QUERY_FILE);
	i=clones.keySet().iterator();
	while(i.hasNext()){
	  DBUtils.executeQuerySet("addClonedASBAgents",
			  DBUtils.addSubs(DBUtils.addSubs(DBUtils.addSubs(new HashMap(),":org_group_id",i.next()),
					  ":n",clones.get(i.next())),
				  ":assembly_id",assembly_id), QUERY_FILE);
	}
	
	// this must occur AFTER the ASB_agents table is filled in, to allow for handling multiplicity
	DBUtils.executeQuerySet("addNewPluginAlibComponents",subs, QUERY_FILE);
	DBUtils.executeQuerySet("addPluginASBComponentHierarchy",subs, QUERY_FILE);
	DBUtils.executeQuerySet("addAgentNameComponentArg",subs, QUERY_FILE);
	DBUtils.executeQuerySet("addASBAgentPGAttr",subs, QUERY_FILE);
	DBUtils.executeQuerySet("addASBAgentRelationToBase",subs, QUERY_FILE);
	DBUtils.executeQuerySet("addASBAgentRelationToCloneset",subs, QUERY_FILE);
	DBUtils.executeQuerySet("addASBAgentHierarchyRelationToBase",subs, QUERY_FILE);
	DBUtils.executeQuerySet("addASBAgentHierarchyRelationToCloneset",subs, QUERY_FILE);
	DBUtils.executeQuerySet("addPluginAgentASBComponentArg",subs, QUERY_FILE);
	DBUtils.executeQuerySet("addPluginOrgtypeASBComponentArg",subs, QUERY_FILE);
	DBUtils.executeQuerySet("addPluginAllASBComponentArg",subs, QUERY_FILE);
      }
    }
    return assembly_id;
  }
  
  /*
   * Returns Map where the keys are human readable names (Strings) and
   * the values are experiment ids (Strings).
   */
  public static SortedMap getExperimentNames() {
    return DBUtils.queryHT("getExperimentNames",new HashMap(), QUERY_FILE);
  }
  
  public static SortedMap getTrialNames(String experiment_id) {
    if (experiment_id == null)
      return null;
    return DBUtils.queryHT("getTrialNames",DBUtils.addSubs(new HashMap(),":experiment_id",experiment_id), QUERY_FILE);
  }
  
  public static String getTrialId(String experiment_id) {
    if (experiment_id == null)
      return null;
    return DBUtils.query1String("getTrialId",DBUtils.addSubs(new HashMap(),":experiment_id",experiment_id), QUERY_FILE); 
  }
  
  public static SortedMap getOrganizationGroups(String experiment_id) {
    if (experiment_id == null)
      return null;
    Map subs = new HashMap();
    subs.put(":experiment_id",experiment_id);
    return DBUtils.queryHT("getOrganizationGroups",subs, QUERY_FILE);
  }
  
  public static Set getOrganizationsInGroup(String experiment_id, String groupId){
    Map subs = new HashMap();
    subs.put(":experiment_id",experiment_id);
    subs.put(":group_id",groupId);
    return DBUtils.querySet("getOrganizationsInGroup",subs, QUERY_FILE);
  }
  
  public static boolean isULThreadSelected(String trialId, String threadName) {
    Map subs = new HashMap();
    subs.put(":trial_id",trialId);
    subs.put(":thread_id",getThreadId(threadName));
    return (hasRows("isULThreadSelected",subs));
  }
  
  static String getThreadId(String thread_name){
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
	DBUtils.executeQuerySet("setULThreadSelected",subs, QUERY_FILE);
      }
    } else {
      if(isULThreadSelected(trialId,threadName)){
	DBUtils.dbDelete("setULThreadNotSelected",subs, QUERY_FILE);
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
  
  static String getGroupId(String trial_id,String group_name){
    Map subs = new HashMap();
    subs.put(":trial_id",trial_id);
    subs.put(":group_name",group_name);
    return DBUtils.query1String("getGroupId",subs, QUERY_FILE);
  }
  
  public static void setGroupSelected(String trial_id, String group_name, boolean selected) {
    String group_id = getGroupId(trial_id,group_name);
    Map subs = new HashMap();
    subs.put(":trial_id",trial_id);
    subs.put(":group_id",group_id);
    if(isGroupSelected(trial_id,group_name)) {
      if(!selected){
	DBUtils.dbDelete("setGroupNotSelected",subs, QUERY_FILE);
      }
    } else {
      if(selected){
	DBUtils.executeQuerySet("setGroupSelected",subs, QUERY_FILE);
      }
    }
  }
  
  public static int getMultiplier(String trial_id, String group_name) {
    String group_id = getGroupId(trial_id,group_name);
    Map subs = new HashMap();
    subs.put(":trial_id",trial_id);
    subs.put(":group_id",group_id);
    Integer i = DBUtils.query1Int("getMultiplier",subs, QUERY_FILE);
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
    DBUtils.dbUpdate("setMultiplier",subs, QUERY_FILE);
  }
  
  /**
   * Clone specified experiment_id with the specified human readable name.
   * Returns new experiment id.
   * Creates new trial id for new experiment id.
   */
  public static String cloneExperiment(String experiment_id, String newName) {
    Map subs = new HashMap();
    //String new_expt_id = "EXPT-"+ DBUtils.query1Int("nextExperimentId", subs, QUERY_FILE);
    String new_expt_id = getNextId("nextExperimentIdHack","EXPT-");
    
    subs.put(":experiment_id",experiment_id);
    subs.put(":new_expt_id",new_expt_id);
    subs.put(":new_name",newName);
    DBUtils.addSubs(subs,":short_assembly_id","CLONING");
    DBUtils.executeQuerySet("cloneExperimentEXPT_EXPERIMENT",subs, QUERY_FILE);
    DBUtils.executeQuerySet("cloneExperimentEXPT_TRIAL",subs, QUERY_FILE);
    DBUtils.executeQuerySet("cloneExperimentEXPT_TRIAL_THREAD",subs, QUERY_FILE);
    DBUtils.executeQuerySet("cloneExperimentEXPT_TRIAL_ORG_MULT",subs, QUERY_FILE);

    // do both runtime & config!!
    DBUtils.executeQuerySet("cloneExperimentEXPT_TRIAL_ASSEMBLY",subs, QUERY_FILE);
    DBUtils.executeQuerySet("cloneExperimentEXPT_TRIAL_CONFIG_ASSEMBLY",subs, QUERY_FILE);

    // what about recipes?
    //    DBUtils.executeQuerySet("cloneExperimentEXPT_TRIAL_MOD_RECIPE",subs, QUERY_FILE);
    return new_expt_id;
  }
  
  static String getNextId(String queryName, String prefix) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.core.db.CMT");
    DecimalFormat format = new DecimalFormat(prefix + "0000");
    Map substitutions = new HashMap();
    substitutions.put(":max_id_pattern", prefix + "____");
    String id = format.format(1); // Default
    Logger qLog = CSMART.createLogger("queries");
    try {
      Connection dbConnection = DBUtils.getConnection(QUERY_FILE);
      Statement stmt = dbConnection.createStatement();
      try {
	if(dbp==null)
	  dbp = DBProperties.readQueryFile(QUERY_FILE, "csmart");
	String query = dbp.getQuery(queryName, substitutions);
	if (qLog.isDebugEnabled()) {
	  qLog.debug("getNextId: "+query);
	}
	ResultSet rs = stmt.executeQuery(query);
	try {
	  if (rs.next()) {
	    String maxId = rs.getString(1);
	    if (maxId != null) {
	      int n = format.parse(maxId).intValue();
	      if (qLog.isDebugEnabled()) {
		qLog.debug("getNextId: n="+n+", maxId ="+maxId);
	      }
	      id = format.format(n + 1);
	      if (qLog.isDebugEnabled()) {
		 qLog.debug("getNextId: id="+id);
	      }
	    }
	  }
	} finally {
	  rs.close();
	}
      } finally {
	try {
	  stmt.close();
	} catch (SQLException e) {
	  if(log.isErrorEnabled()) {
	    log.error("Exception closing statement in getNextId: ", e);
	  }
	}
        dbConnection.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception: ", e);
      }
      // Ignore exceptions and use default
    }
    return id;
  }
  
  static String[] orderThreads(Set set){
    String[] threads = new String[set.size()];
    int i=0;
    if (set.contains("BASE")) {threads[i]="BASE"; i++;}
    if (set.contains("STRATEGIC-TRANS")) {threads[i]="STRATEGIC-TRANS"; i++;}
    if (set.contains("THEATER-TRANS")) {threads[i]="THEATER-TRANS"; i++;}
    if (set.contains("CLASS-1")) {threads[i]="CLASS-1"; i++;}
    if (set.contains("CLASS-3")) {threads[i]= "CLASS-3"; i++;}
    if (set.contains("CLASS-4")) {threads[i]= "CLASS-4"; i++;}
    if (set.contains("CLASS-5")) {threads[i]= "CLASS-5"; i++;}
    if (set.contains("CLASS-8")) {threads[i]= "CLASS-8"; i++;}
    if (set.contains("CLASS-9")) {threads[i]= "CLASS-9"; i++;}
    return threads;
  }
  
  public static String getAssemblyId(String experimentId) {
    Map subs = new HashMap();
    subs.put(":experiment_id", experimentId);
    //subs.put(":match_pattern", "%}");
    return DBUtils.query1String("getAssemblyIDOnExpt", subs, QUERY_FILE);
  }
  
  /**
   * This MUST be called after calling setGroups, setThreads, or setMultipliers
   * to actually update the experiment.
   */
  public static String updateCMTAssembly(String experiment_id) {
    Map subs = new HashMap();
    subs.put(":experiment_id",experiment_id);
    Set threads = DBUtils.querySet("updateCMTAssemblyThreadID",subs, QUERY_FILE);
    threads.add("STRATEGIC-TRANS");
    threads.add("THEATER-TRANS");
    String cfw_group_id = DBUtils.query1String("updateCMTAssemblyCFW_GROUP_ID",subs, QUERY_FILE);
    Map clones = DBUtils.queryHT("updateCMTAssemblyClones",subs, QUERY_FILE);
    
    String assembly_id = createCMTasb(cfw_group_id,orderThreads(threads), clones);
    
    subs.put(":assembly_id",assembly_id);
    // this query is legal in both Oracle and mySQL
    DBUtils.dbUpdate("updateCMTAssembly",subs, QUERY_FILE);

    //Also update the CMT assembly in the runtime table
    DBUtils.dbUpdate("updateRuntimeCMTAssembly",subs, QUERY_FILE);

    DBUtils.dbUpdate("updateAssemblyIDOnExpt",subs, QUERY_FILE);
    DBUtils.dbUpdate("updateRuntimeAssemblyIDOnExpt",subs, QUERY_FILE);
    return assembly_id;
  }
  
  /*
   * Deletes the given experiment from the database. 
   * Gets rid of the trial and associated other data.
   * Does not get rid of the CMT assembly or other assemblies.
   */
  public static void deleteExperiment(String experiment_id, String experiment_name) {
    if (experiment_id == null)
      return;
    boolean doIt = true;
    
    // HACK: Avoid deleting base experiments
    if (! experiment_id.startsWith("EXPT-"))
      doIt = false;
    
    if (doIt == false) {
      int response = 
	JOptionPane.showConfirmDialog(null,
				      "You seem to be trying to delete a base experiment -- do you REALLY want to do this?",
				      "Confirm Delete",
				      JOptionPane.YES_NO_OPTION);
      
      if (response == JOptionPane.YES_OPTION) {
	doIt = true;
      }
    }
    
    if (!doIt)
      return;
    
    String expt_id = DBUtils.sqlQuote(experiment_id);
    String trial_id = DBUtils.sqlQuote(getTrialId(experiment_id));
    //    String society_id = DBUtils.sqlQuote(PopulateDb.getSocietyAlibId(experiment_name));
    ArrayList queries = new ArrayList();

    queries.add(DBUtils.makeDeleteQuery(asbPrefix+"expt_trial_assembly", "expt_id", expt_id));
    queries.add(DBUtils.makeDeleteQuery(asbPrefix+"expt_trial_config_assembly", "expt_id", expt_id));
    queries.add(DBUtils.makeDeleteQuery(asbPrefix+"expt_trial_thread", "expt_id", expt_id));
    queries.add(DBUtils.makeDeleteQuery(asbPrefix+"expt_trial_org_mult", "expt_id", expt_id));
    queries.add(DBUtils.makeDeleteQuery(asbPrefix+"expt_trial_mod_recipe", "trial_id", trial_id));
    queries.add(DBUtils.makeDeleteQuery(asbPrefix+"expt_trial", "expt_id", expt_id));
    queries.add(DBUtils.makeDeleteQuery(asbPrefix+"expt_experiment", "expt_id", expt_id));
    DBUtils.executeQueries(queries, QUERY_FILE);

    // Despite the name, this deletes all unused assemblies, including COMM, CSA, CMT, etc.
    clearUnusedCMTassemblies();

  }
  
} // end of CMT.java



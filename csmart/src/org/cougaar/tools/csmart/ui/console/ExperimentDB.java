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

package org.cougaar.tools.csmart.ui.console;

import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import org.cougaar.tools.csmart.societies.database.DBUtils;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.DBProperties;
import silk.InputPort;
import silk.Pair;
import silk.Procedure;
import silk.SI;
import silk.Scheme;
import silk.Symbol;
import silk.U;

  /**
   * Interface to the database.
   * Currently returns dummy values.
   */

public class ExperimentDB {
    public static boolean cmtLoaded=false;
    public static void loadCMT(){
	if(!cmtLoaded){
	    File cmfFile = new File(System.getProperty("org.cougaar.install.path"), "/csmart/data/cmt/scm/cmt.scm");
	    try{
		System.out.println("Trying to load file: "+cmfFile);
		load(new FileReader(cmfFile));
	    } catch (java.io.FileNotFoundException e){
		e.printStackTrace();
	    }
	    cmtLoaded=true;
	    try{
		DBProperties dbProps = DBProperties.readQueryFile("org.cougaar.configuration.database", "CSMART.q");
//  		String database = dbProps.getProperty("database");
//  		String username = dbProps.getProperty("username");
//  		String password = dbProps.getProperty("password");
		String database = CSMART.getDatabaseConfiguration();
		String username = CSMART.getDatabaseUserName();
		String password = CSMART.getDatabaseUserPassword();
//  		System.out.println("call(use-database,"+database+","+username+","+password+")");
//		call("use-database",database,username,password);
		call("setDBConnection",DBUtils.getConnection());
	    } catch (java.io.IOException e){
		e.printStackTrace();
	    } catch (java.sql.SQLException e){
		e.printStackTrace();
	    }

	}
    }


    /*
     * Returns hashtable where the keys are human readable names (Strings) and
     * the values are experiment ids (Strings).
     */

    public static Hashtable getExperimentNames() {
	return (Hashtable)call("getExperimentNames");
    }



    /*
     * Deletes the given experiment from the database. Gets rid of the trial and associated other data.
     * Does not get rid of the CMT assembly or other assemblies.
     */
    public static void deleteExperiment(String experimentId) {
	call("deleteExperiment",experimentId);
    }

    /**
     * Returns true if experiment name is in database and false otherwise.
     * Used to guarantee that experiment names in database are unique.
     * @param experimentName human readable experiment name
     * @return true if name is unique and false otherwise
     */

    public static boolean isExperimentNameInDatabase(String experimentName) {
	Hashtable experimentNamesHT = getExperimentNames();
	Set namesInDB = experimentNamesHT.keySet();
	if (namesInDB != null && namesInDB.contains(experimentName))
	    return true;
	return false;
    }

    /*
     * Returns hashtable where the keys are human readable names (Strings) and
     * the values are trial ids (Strings).
     */

    public static Hashtable getTrialNames(String experimentId) {
	return (Hashtable)call("getTrialNames",experimentId);
    }


    public static String getTrialId(String experimentId) {
	return (String)call("getUniqueTrialName",experimentId); 
    }

    /**
     * Add the named trial to the experiment.  The name supplied is
     * the human readable name; the value returned is the new trial id or null.
     */

    public static String addTrialName(String experimentId, String name) {
	return (String)call("addTrialName",experimentId,name);
    }

    public static String addAssembly(String experimentId,
				     String assemblyId,
				     String trialId) {
	return (String)call("addAssembly",experimentId,assemblyId,trialId);
    }

    /*
     * Returns society templates for an experiment.
     * Returns hashtable where human readable names are keys
     * and ids are values.
     */

    public static String getSocietyTemplateForExperiment(String experimentId) {
	return (String)call("getSocietyTemplateForExperiment",experimentId);
    }

    /*
     * Create a new experiment based on the given societyTemplate
     *  returns the experimentId
     */

    public static String createExperiment(String experimentName,String societyTemplate) {
	return (String)call("createExperiment",experimentName,societyTemplate);
    }

    public static String addNodeAssignments(Hashtable nodeTable ,String assemblyName) {
	return (String)call("addNodeAssignments",nodeTable,assemblyName);
    }

    public static String addMachineAssignments(Hashtable machineTable ,String assemblyName) {
	return (String)call("addMachineAssignments",machineTable,assemblyName);
    }


    /**
     * Returns hashtable of all society templates in database.
     */

    public static Hashtable getSocietyTemplates() {
	return (Hashtable)call("getSocietyTemplates");

    }

    public static Hashtable getOrganizationGroups(String experimentId) {
	return (Hashtable)call("getOrganizationGroups",experimentId);
    }

    public static HashSet getOrganizationsInGroup(String experimentId, String groupId){
	return (HashSet)call("getOrganizationsInGroup",experimentId,groupId);
    }

    public static boolean isULThreadSelected(String trialId, String threadName) {
	Boolean b =(Boolean)call("isULThreadSelected", trialId,threadName);
	return b.booleanValue();
    }

    public static void setULThreadSelected(String trialId, String threadName, 
					   boolean selected) {
	if(selected){
	    call("setULThreadSelected",trialId,threadName);
	} else {
	    call("setULThreadNotSelected",trialId,threadName);
	}

	//System.out.println("Thread: " + threadName + " selected: " + selected);
    }

    public static boolean isGroupSelected(String trialId, String groupName) {
	Boolean b =(Boolean)call("isGroupSelected", trialId,groupName);
	return b.booleanValue();
    }

    public static void setGroupSelected(String trialId, String groupName, 
					boolean selected) {
	call("setGroupSelected", trialId,groupName,new Boolean(selected));
    }
    
    public static int getMultiplier(String trialId, String groupName) {
	Integer i = (Integer)call("getMultiplier", trialId,groupName);
	return i.intValue();
    }

    public static void setMultiplier(String trialId, String groupName, int value) {
	call("setMultiplier",trialId, groupName, new Integer(value));
    }

    /**
     * Clone specified experimentId with the specified human readable name.
     * Returns new experiment id.
     * Creates new trial id for new experiment id.
     */

    public static String cloneExperiment(String experimentId, String newName) {
	return (String)call("cloneExperiment",experimentId, newName);
    }

    /**
     * This MUST be called after calling setGroups, setThreads, or setMultipliers
     * to actually update the experiment.
     */

    public static void updateCMTAssembly(String experimentId) {
	call("updateCMTAssembly",experimentId);
    }

    /**Silk utility functions
     **/

    /** Returns the global procedure named s. **/
    public static Procedure getGlobalProcedure(String s) {
	return U.toProc(getGlobalValue(s)); }
    public static Object getGlobalValue(String s) {
	// KRA 01MAY01: Kludge. You can't do
	// Symbol.intern(s).getGlobalValue() because global values of
	// Generic methods are looked up at analyze() time.
	return eval(Symbol.intern(s)); }
    public static Object eval(Object it) {
	return Scheme.evalToplevel(it); }

    /** Load Scheme expressions from a  Reader, or String. **/
    public static Object load(java.io.Reader in) {
	return Scheme.load(new InputPort(in)); }
    public static Object load(String in) {
	//  	System.out.println("Trying to load: "+in);
	return load(new java.io.StringReader(in)); }
    public static Object call(String p) {
	//  	System.out.println("call("+p+")");
	loadCMT();
	return getGlobalProcedure(p).apply(Pair.EMPTY); }
    public static Object call(String p, Object a1) {
	//  	System.out.println("call("+p+","+a1+")");
	loadCMT();
	return getGlobalProcedure(p).apply(list(a1)); }
    public static Object call(String p, Object a1, Object a2) {
	//  	System.out.println("call("+p+","+a1+","+a2+")");
	loadCMT();
	return getGlobalProcedure(p).apply(list(a1, a2)); }
    public static Object call(String p, Object a1, Object a2, Object a3) {
	//  	System.out.println("call("+p+","+a1+","+a2+","+a3+")");
	loadCMT();
	return getGlobalProcedure(p).apply(list(a1, a2, a3)); }

    public static Pair list() { return Pair.EMPTY; }
    public static Pair list(Object a1) { return new Pair(a1, Pair.EMPTY); }
    public static Pair list(Object a1, Object a2) {
	return new Pair(a1, new Pair(a2, Pair.EMPTY)); }
    public static Pair list(Object a1, Object a2, Object a3) {
	return new Pair(a1, new Pair(a2, new Pair(a3, Pair.EMPTY))); }
    public static Pair list(Object a1, Object a2, Object a3, Object a4) {
	return new Pair(a1, list(a2,a3,a4));}
    public static Pair list(Object a1, Object a2, Object a3, Object a4, Object a5) {
	return new Pair(a1, new Pair (a2,list(a3,a4,a5)));}
    public static Pair list(Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) {
	return new Pair(a1, new Pair (a2,new Pair(a3, list(a4,a5,a6))));}

}

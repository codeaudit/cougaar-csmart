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
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.DBProperties;

  /**
   * Interface to the database.
   * Currently returns dummy values.
   */

public class ExperimentDB {

    /*
     * Returns Map where the keys are human readable names (Strings) and
     * the values are experiment ids (Strings).
     */

    public static Map getExperimentNames() {
	return (Map) CMT.getExperimentNames();
    }



    /*
     * Deletes the given experiment from the database. Gets rid of the trial and associated other data.
     * Does not get rid of the CMT assembly or other assemblies.
     */
    public static void deleteExperiment(String experimentId, String experimentName) {
	CMT.deleteExperiment(experimentId, experimentName);
    }

    /**
     * Returns true if experiment name is in database and false otherwise.
     * Used to guarantee that experiment names in database are unique.
     * @param experimentName human readable experiment name
     * @return true if name is unique and false otherwise
     */

    public static boolean isExperimentNameInDatabase(String experimentName) {
	Map experimentNamesHT = getExperimentNames();
	Set namesInDB = experimentNamesHT.keySet();
	if (namesInDB != null && namesInDB.contains(experimentName))
	    return true;
	return false;
    }

    /*
     * Returns hashtable where the keys are human readable names (Strings) and
     * the values are trial ids (Strings).
     */

    public static Map getTrialNames(String experimentId) {
	return CMT.getTrialNames(experimentId);
    }


    public static String getTrialId(String experimentId) {
	return CMT.getTrialId(experimentId); 
    }

    /**
     * Add the named trial to the experiment.  The name supplied is
     * the human readable name; the value returned is the new trial id or null.
     */

    public static String addTrialName(String experimentId, String name) {
	return (String)CMT.addTrialName(experimentId,name);
    }

    public static String addAssembly(String experimentId,
				     String assemblyId,
				     String trialId) {
	return (String)CMT.addAssembly(experimentId,assemblyId,trialId);
    }

    /*
     * Returns society templates for an experiment.
     * Returns hashtable where human readable names are keys
     * and ids are values.
     */

    public static String getSocietyTemplateForExperiment(String experimentId) {
	return (String)CMT.getSocietyTemplateForExperiment(experimentId);
    }

    /*
     * Create a new experiment based on the given societyTemplate
     *  returns the experimentId
     */

    public static String createExperiment(String experimentName,String societyTemplate) {
	return (String)CMT.createExperiment(experimentName,societyTemplate);
    }

    public static String addNodeAssignments(Map nodeTable ,String assemblyName) {
	return (String)CMT.addNodeAssignments(nodeTable,assemblyName);
    }

    public static String addMachineAssignments(Map machineTable ,String assemblyName) {
	return (String)CMT.addMachineAssignments(machineTable,assemblyName);
    }


    /**
     * Returns hashtable of all society templates in database.
     */

    public static Map getSocietyTemplates() {
	return CMT.getSocietyTemplates();

    }

    public static Map getOrganizationGroups(String experimentId) {
	return CMT.getOrganizationGroups(experimentId);
    }

    public static Set getOrganizationsInGroup(String experimentId, String groupId){
	return CMT.getOrganizationsInGroup(experimentId,groupId);
    }

    public static boolean isULThreadSelected(String trialId, String threadName) {
	return CMT.isULThreadSelected( trialId,threadName);
    }

    public static void setULThreadSelected(String trialId, String threadName, 
					   boolean selected) {
	
	    CMT.setULThreadSelected(trialId,threadName,selected);
	//System.out.println("Thread: " + threadName + " selected: " + selected);
    }

    public static boolean isGroupSelected(String trialId, String groupName) {
	return CMT.isGroupSelected( trialId,groupName);
    }

    public static void setGroupSelected(String trialId, String groupName, 
					boolean selected) {
	CMT.setGroupSelected( trialId,groupName,selected);
    }
    
    public static int getMultiplier(String trialId, String groupName) {
	return CMT.getMultiplier( trialId,groupName);
    }

    public static void setMultiplier(String trialId, String groupName, int value) {
	CMT.setMultiplier(trialId, groupName, value);
    }

    /**
     * Clone specified experimentId with the specified human readable name.
     * Returns new experiment id.
     * Creates new trial id for new experiment id.
     */

    public static String cloneExperiment(String experimentId, String newName) {
	return (String)CMT.cloneExperiment(experimentId, newName);
    }

    /**
     * This MUST be called after calling setGroups, setThreads, or setMultipliers
     * to actually update the experiment.
     */

    public static String updateCMTAssembly(String experimentId) {
	return CMT.updateCMTAssembly(experimentId);
    }

//   public static String getAssemblyId(String experimentId) {
//     return CMT.getAssemblyId(experimentId);
//   }

  public static void deleteCMTAssembly(String experimentId) {
      String cmtASB = CMT.getAssemblyId(experimentId);
      System.out.println("Deleting CMT assembly: "+cmtASB+" for experiment: " +experimentId );
      CMT.reallyClearCMTasb(cmtASB);
  }
}
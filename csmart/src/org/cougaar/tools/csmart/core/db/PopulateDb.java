/* 
 * <copyright>
 * Copyright 2001-2002 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).

 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.tools.csmart.core.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import org.cougaar.util.DBProperties;
import org.cougaar.util.Parameters;
import org.cougaar.util.DBConnectionPool;

import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.core.cdata.*;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * This class takes a structure of ComponentData objects and populates
 * the configuration database with some or all of the components
 * described by the data. The selection of applicable components is
 * still an issue.
 **/
public class PopulateDb extends PDbBase {
  private String exptId;
  private String trialId;
  private String cmtType;
  private String hnaType;
  private String csmiType;
  private String hnaAssemblyId;
  private String cmtAssemblyId;
  private String csmiAssemblyId;
  private static String csaType = "CSA";
  private String csaAssemblyId;

  private Map propertyInfos = new HashMap();
  private Set alibComponents = new HashSet();
  private Map componentArgs = new HashMap();

  // Unused flag to decide if the whole society
  // must be written out, or only the additions
  // Gets set in constructor
  private boolean writeEverything = false;

  private DBConflictHandler conflictHandler;
  private boolean keepAll = false;
  private boolean overwriteAll = false;

  private transient Logger log; 

  /**
   * Inner class to serve as the key to information about
   * PropertyInfo that has been resolved. The key consists of the
   * property group name and the property name within that group.
   **/
  private static class PropertyKey {
    private String pgName;
    private String propName;
    private int hc;

    /**
     * Constructor from property group name and property name
     **/
    public PropertyKey(String pgName, String propName) {
      this.pgName = pgName;
      this.propName = propName;
      hc = pgName.hashCode() + propName.hashCode();
    }

    /**
     * Get the property group name part of the key
     * @return the property group name of this key
     **/
    public String getPGName() {
      return pgName;
    }

    /**
     * Get the property name part of the key
     * @return the property name of this key
     **/
    public String getPropName() {
      return propName;
    }

    /**
     * The usual Object.hashCode() method
     **/
    public int hashCode() {
      return hc;
    }

    /**
     * Equality comparison.
     * @return true if both the property group name and the
     * property name are equal
     **/
    public boolean equals(Object o) {
      if (!(o instanceof PropertyKey)) return false;
      PropertyKey that = (PropertyKey) o;
      return this.pgName.equals(that.pgName) &&
	this.propName.equals(that.propName);
    }

    /**
     * @return concatenation of property group name and property
     * name separated with vertical bar.
     **/
    public String toString() {
      return pgName + "|" + propName;
    }
  }

  /**
   * Inner class for recording information about a property within a
   * property group. Used to record information from the database to
   * avoid fetching the same information multiple times.
   **/
  private static class PropertyInfo {
    PropertyKey key;
    String attributeLibId;
    String attributeType;
    String aggregateType;
    public PropertyInfo(PropertyKey key,
			String attributeLibId,
			String attributeType,
			String aggregateType)
    {
      this.key = key;
      this.attributeLibId = attributeLibId;
      this.attributeType = attributeType;
      this.aggregateType = aggregateType;
    }

    public String getAttributeLibId() {
      return attributeLibId;
    }

    public boolean isCollection() {
      return !aggregateType.equals("SINGLE");
    }
    public String toString() {
      return key.toString()
	+ "=" + attributeLibId
	+ "(" + attributeType + "," + aggregateType + ")";
    }
  }

  /**
   * Initialize a PopulateDb that expects a CMT assembly
   * @param cmtType the cmt assembly type 
   * @param hnaType the hna assembly type
   * @param csmiType the csmi assembly type
   * @param experimentName the name of the experiment being written
   * @param exptId the experiment id of the source experiment
   * @param trialId the trial id
   **/
  public PopulateDb(String cmtType, String hnaType, String csmiType,
		    String experimentName,
		    String exptId, String trialId,
		    DBConflictHandler ch)
    throws SQLException, IOException
  {
    super();
    createLogger();
    if (cmtType == null) throw new IllegalArgumentException("null cmtType");
    if (hnaType == null) throw new IllegalArgumentException("null hnaType");
    if (csmiType == null) throw new IllegalArgumentException("null csmiType");
    if (exptId == null) throw new IllegalArgumentException("null exptId");
    if (trialId == null) throw new IllegalArgumentException("null trialId");
    if (ch == null) throw new IllegalArgumentException("null conflict handler");
    this.cmtType = cmtType;
    this.hnaType = hnaType;
    this.csmiType = csmiType;
    this.exptId = exptId;
    this.trialId = trialId;
    this.conflictHandler = ch;
    substitutions.put(":expt_id:", exptId);
    substitutions.put(":cmt_type:", cmtType);
    String oldExperimentName = getOldExperimentName();
    // If user changed the experiment name
    // then create a new trial ID with the old assemblies and threads
    if (!experimentName.equals(oldExperimentName)) {
      cloneTrial(trialId, experimentName, "Modified " + oldExperimentName);
      writeEverything = true; // unused
    } else {
      // Otherwise drop the old HNA assembly, recipes in prep for saving
      cleanTrial(cmtType, hnaType, csmiType, trialId);
      writeEverything = false; // unused
    }
    // Construct experiments HNA, etc assemblies if necc, and add
    // to the runtime assemblies table
    // and set up the :assembly_match: substitution to have all 
    // these runtime assemblies
    setNewAssemblyIds();

    // FIXME: Where / when does the CMT assembly ID get recorded
    // in the runtime table
  }

  /**
   * Construct a PDB for saving just a non-CMT society. 
   * However, the society may have come froma CMT society,
   * in which case we get the previous CMT assembly ID.
   */
  public PopulateDb(String cmtAsbID, String societyName,
		    String assemblyId, DBConflictHandler ch)
    throws SQLException, IOException
  {
    super();
    createLogger();
    if (ch == null) throw new IllegalArgumentException("null conflict handler");
    this.conflictHandler = ch;
    this.cmtType = csaType;
    substitutions.put(":cmt_type:", cmtType);
    if (assemblyId != null) {
      // Society already in DB.
      // check by looking it up?

      // Treat this assemblyId as a CSA? As a CMT?
      // If its a CMT could the other be a CMT?
      // Would the other every be filled in? Only if the new
      // was a CSA and the other indicated the config time CMT
      // assembly this comes from.
      // FIXME!!

      // Remove the previous definition of that assembly
      // in preparation for saving this definition?
      // Or will the over-write work OK?
      substitutions.put(":assembly_id:", sqlQuote(assemblyId));
      //      executeUpdate(dbp.getQuery("deleteAssembly"), substitutions);
      //      executeUpdate(dbp.getQuery("cleanASBAssembly", substitutions));
      // must set :assemblies_to_clean:
      //substitutions.put(":assemblies_to_clean:", "('" + assemblyId + "')");
      //      executeUpdate(dbp.getQuery("cleanASBComponentArg", substitutions));
      //executeUpdate(dbp.getQuery("cleanASBComponentHierarchy", substitutions));
      // What about clearing out the asb_agent tables?
    } else {
      // Create new CSA assembly to hold the data, copying OPLAN info if any
      // Also set the csaAssemblyId and cmtAssemblyId parameters
      try {
        assemblyId = createCSAAssembly(cmtAsbID, societyName);
      } catch( SQLException se ) {
        if(log.isErrorEnabled()) {
          log.error("createCSAAssembly error: ", se);
        }
      } 
    }

    // OK, at this point we're ready to save out the society: asb_component_arg,
    // asb_component_hierarchy, and the asb_agent tables

  }

  /**
   * Construct a PopulateDb where there is no CMT assembly
   * @param hnaType the hna assembly type
   * @param csmiType the csmi assembly type
   * @param experimentName the name of the experiment being written
   * @param exptId ExperimentId, if one exists, else null
   * @param trialId the trial id
   * @param ch 
   * @exception SQLException if an error occurs
   * @exception IOException if an error occurs
   */
  public PopulateDb(String hnaType, String csmiType, String experimentName,
		    String exptId, String trialId, DBConflictHandler ch)
    throws SQLException, IOException
  {
    super();
    createLogger();
    if (hnaType == null) throw new IllegalArgumentException("null hnaType");
    if (csmiType == null) throw new IllegalArgumentException("null csmiType");
    if (ch == null) throw new IllegalArgumentException("null conflict handler");
    this.cmtType = cmtType;
    this.hnaType = hnaType;
    this.csmiType = csmiType;
    if(exptId == null) {
      // Create a new exptId here.
      exptId = newExperiment("EXPT", experimentName, "NON-CFW EXPERIMENT");
      // Trial and experiment have same name
      trialId = newTrial(exptId + ".TRIAL", experimentName, "NON-CFW TRIAL");
    }
    this.trialId = trialId;
    this.exptId = exptId;
    this.conflictHandler = ch;
    substitutions.put(":expt_id:", exptId);
    String oldExperimentName = getOldExperimentName();
    // If the experiment name was changed, get a new ID with 
    // all the old assemblies, threads
    if (!experimentName.equals(oldExperimentName)) {
      cloneTrial(trialId, experimentName, "Modified " + oldExperimentName);
      writeEverything = true;
    } else {
      // We're starting fresh.
      //            cleanTrial(cmtType, hnaType, csmiType, trialId);
      writeEverything = false;
    }
    // Make sure all the necessary assembly IDs are created, saved
    setNewAssemblyIds();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  private String getOldExperimentName() throws SQLException {
    ResultSet rs = executeQuery(stmt, dbp.getQuery("queryExptName", substitutions));
    if (rs.next()) {
      return rs.getString(1);
    }
    return "";
  }

  // Create the HNA and CSMI assemly IDs if necessary, 
  // saving them to the runtime table of Assemblies
  private void setNewAssemblyIds() throws SQLException {
    hnaAssemblyId = addAssembly(hnaType);
    if (hnaType.equals(csmiType))
      csmiAssemblyId = hnaAssemblyId;
    else
      csmiAssemblyId = addAssembly(csmiType);
    setAssemblyMatch();
  }

  // Grab all the RUNTIME assemblies in the experiment
  // and set up the :assembly_match: substition with them
  // FIXME: Does this substitution ever get used where
  // config time is more appropriate?
  private void setAssemblyMatch() throws SQLException {
    ResultSet rs = executeQuery(stmt, dbp.getQuery("queryTrialAssemblies", substitutions));
    StringBuffer q = new StringBuffer();
    boolean first = true;
    q.append("in (");
    while (rs.next()) {
      if (first) {
	first = false;
      } else {
	q.append(", ");
      }
      q.append("'").append(rs.getString(1)).append("'");
    }
    q.append(')');
    if (first) {            // No matches
      substitutions.put(":assembly_match:", "is null");
    } else {
      substitutions.put(":assembly_match:", q.toString());
    }
  }

  public String getExperimentId() {
    return exptId;
  }

  public String getHNAAssemblyId() {
    return hnaAssemblyId;
  }

  public String getCSMIAssemblyId() {
    return csmiAssemblyId;
  }

  public String getCSAAssemblyId() {
    return csaAssemblyId;
  }

  public String getCMTAssemblyId() {
    return cmtAssemblyId;
  }

  // Create a new experiment, trial where the trial has all the CMT threads 
  // and assemblies that the old one did. The only thing not copied is the recipes.
  private void cloneTrial(String oldTrialId, String experimentName, String description)
    throws SQLException
  {
    // Create new experiment in DB with given name,
    // set global expt_id variable
    newExperiment("EXPT", experimentName, description);
    // greate trial with given name, set global variable
    newTrial(exptId + ".TRIAL", experimentName, description); // Trial and experiment have same name
    // copy assemblies of type cmt_type in RUNTIME table under old ID to new
    copyCMTAssemblies(oldTrialId, trialId);
    // Copy stuff in expt_trial_thread from old trial id to new
    copyCMTThreads(oldTrialId, trialId);
  }

  /**
   * Clean out a trial by removing all assemblies except the CMT and
   * idType assemblies
   **/
  private void cleanTrial(String cmtType, String hnaType, String csmiType, String oldTrialId)
    throws SQLException
  {
    // Basically we'll drop the HNA asembly and recipes. It will drop
    // the CMT assembly too, but only if cmtType is null
    if(log.isDebugEnabled()) {
      log.debug("Cleaning: " + cmtType +", " + hnaType+", " + csmiType+", " + oldTrialId);
    }
    substitutions.put(":trial_id:", oldTrialId);
    substitutions.put(":cmt_type:", cmtType);
    substitutions.put(":hna_type:", hnaType);
    substitutions.put(":csmi_type:", csmiType);

    //FIXME: Must not delete CSA assembly if same assembly listed
    // in config-time table. Maybe do this by saying the cmtType is CSA?
    // Get all non-cmtType assemblies in runtime table
    // Should it also look in config table?

    // check that each Assembly it finds is not
    // referenced by another experiment. If it is, then we only delete from 
    // trial_assembly and trial_config_assembly
    ResultSet rs =
      executeQuery(stmt, dbp.getQuery("queryAssembliesToClean", substitutions));
    if (rs.next()) {
      boolean first = true;
      StringBuffer assembliesToDelete = new StringBuffer();
      assembliesToDelete.append("(");
      StringBuffer assembliesToReallyDelete = new StringBuffer();
      assembliesToReallyDelete.append("(");
      boolean rfirst = true;
      String asb = null;
      do {
	if (first) {
	  first = false;
	} else {
	  assembliesToDelete.append(", ");
	}
	asb = rs.getString(1);
	// if asb not used
	if (! isAssemblyUsed(asb)) {
	  if (log.isDebugEnabled()) {
	    log.debug("assembly " + asb + " not used, except in trial " + trialId + " which we are deleting");
	  }
	  if (rfirst) {
	    rfirst = false;
	  } else {
	    assembliesToReallyDelete.append(", ");
	  }
	  assembliesToReallyDelete.append(sqlQuote(asb));
	}
	assembliesToDelete.append(sqlQuote(asb));
      } while (rs.next());
      assembliesToDelete.append(")");
      assembliesToReallyDelete.append(")");
      substitutions.put(":assemblies_to_clean:", assembliesToDelete.toString());
      if (log.isDebugEnabled()) {
	log.debug("Assemblies to delete from trial " + trialId + ": " + assembliesToDelete);
      }
      // This deletes the references in the runtime table
      executeUpdate(dbp.getQuery("cleanTrialAssembly", substitutions));
      // Add delete from config table. I expect this to do nothing,
      // but it doesnt hurt
      executeUpdate(dbp.getQuery("cleanTrialConfigAssembly", substitutions));

      // now really delete those that are unused.
      // where if this is true there were none
      if (! rfirst) {
	substitutions.put(":assemblies_to_clean:", assembliesToReallyDelete.toString());
	cleanAssemblies();
      }
    }
    rs.close();
    if(log.isDebugEnabled()) {
      log.debug("Substitutions: " + substitutions);
    }
    executeUpdate(dbp.getQuery("cleanTrialRecipe", substitutions));
  }

  private void removeConfigAssembly(String assembly_id) throws SQLException {
    if (assembly_id == null || assembly_id.equals(""))
      return;
    substitutions.put(":assemblies_to_clean:", sqlQuote("(" + assembly_id + ")"));
    executeUpdate(dbp.getQuery("cleanTrialConfigAssembly", substitutions));
  }

  private void removeRuntimeAssembly(String assembly_id) throws SQLException {
    if (assembly_id == null || assembly_id.equals(""))
      return;
    substitutions.put(":assemblies_to_clean:", sqlQuote("(" + assembly_id + ")"));
    executeUpdate(dbp.getQuery("cleanTrialAssembly", substitutions));
  }

  // Delete complete definition of a single assembly
  // This assembly must not be referenced by any trial,
  // but nothing checks this
  private void cleanAssembly(String assembly_id) throws SQLException {
    if (assembly_id == null || assembly_id.equals(""))
      return;
    substitutions.put(":assemblies_to_clean:", sqlQuote("(" + assembly_id + ")"));
    cleanAssemblies();
  }

  // Delete complete definition of the assemblies listed
  // in the assemblies_to_clean substitution
  // These assemblies should not be used by any trial,
  // but nothing checks this
  private void cleanAssemblies() throws SQLException {
    if (log.isDebugEnabled()) {
      log.debug("Deleting assemblies using substitutions: " + substitutions);
    }
    
    executeUpdate(dbp.getQuery("cleanASBAssembly", substitutions));
    executeUpdate(dbp.getQuery("cleanASBComponentArg", substitutions));
    executeUpdate(dbp.getQuery("cleanASBComponentHierarchy", substitutions));
    
    // Add in asb_agent and asb_oplan tables
    executeUpdate(dbp.getQuery("cleanASBAgent", substitutions));
    executeUpdate(dbp.getQuery("cleanASBAgentPGAttr", substitutions));
    executeUpdate(dbp.getQuery("cleanASBAgentRel", substitutions));
    executeUpdate(dbp.getQuery("cleanASBOplan", substitutions));
    executeUpdate(dbp.getQuery("cleanASBOplanAAttr", substitutions));
  }

  // Deals solely with expt_experiment table
  private String newExperiment(String idType, String experimentName, String description)
    throws SQLException
  {
    String exptIdPrefix = idType + "-";
    ResultSet rs = executeQuery(stmt, dbp.getQuery("queryExptCFWGroupId", substitutions));
    if (rs.next()) {
      substitutions.put(":cfw_group_id:", rs.getString(1));
    } else {
      substitutions.put(":cfw_group_id:", "unknown");
    }
    substitutions.put(":expt_type:", idType);
    substitutions.put(":expt_name:", experimentName);
    substitutions.put(":description:", description);
    exptId = getNextId("queryMaxExptId", exptIdPrefix);
    if(log.isDebugEnabled()) {
      log.debug("Created New exptId: " + exptId);
    }
    substitutions.put(":expt_id:", exptId);
    executeUpdate(dbp.getQuery("insertExptId", substitutions));
    return exptId;
  }

  public String getTrialId() {
    if(log.isDebugEnabled()) {
      log.debug("trialId: " + trialId);
    }
    return trialId;
  }

  // Deals solely with expt_trial table
  private String newTrial(String idType, String trialName, String description)
    throws SQLException
  {
    String trialIdPrefix = idType + "-";
    substitutions.put(":trial_type:", idType);
    trialId = getNextId("queryMaxTrialId", trialIdPrefix);
    if(log.isDebugEnabled()) {
      log.debug("TrialId: " + trialId);
    }
    substitutions.put(":trial_id:", trialId);
    substitutions.put(":description:", description);
    substitutions.put(":trial_name:", trialName);
    executeUpdate(dbp.getQuery("insertTrialId", substitutions));
    return trialId;
  }

  /**
   * Create a new CSA assembly ID, copying the OPLAN info
   * for the given base assembly if any.
   *
   * @param cmtAsbID a <code>String</code> base society assembly which this will be copied from, possibly null
   * @param societyName a <code>String</code> name for the new society
   * @return a <code>String</code> CSA assembly ID
   */
  public String createCSAAssembly(String cmtAsbID, String societyName) throws SQLException {
    // whatever the current cmtAssemblyId (possibly really a CSA)
    // create a new CSA assembly from it:
    // entry in asb_assembly and copy any oplan rows
    // and return the new ID
    // which will be 
    String assemblyIdPrefix = csaType + "-";
    substitutions.put(":assembly_id_pattern:", assemblyIdPrefix + "____");
    substitutions.put(":assembly_type:", csaType);
    String assemblyId = getNextId("queryMaxAssemblyId", assemblyIdPrefix);
    csaAssemblyId = assemblyId;
    substitutions.put(":assembly_id:", assemblyId);
    executeUpdate(dbp.getQuery("insertAssemblyId", substitutions));
    if (cmtAsbID == null || cmtAsbID.equals("")) {
      substitutions.put(":soc_desc:", csaType + " Assembly defining society " + societyName);
    } else {
      cmtAssemblyId = cmtAsbID;
      substitutions.put(":soc_desc:", csaType + " Assembly defining society " + societyName + " based on assembly " + cmtAsbID);
      // Came from a CMT assembly. Copy the OPLAN stuff
      copyOPLANData(cmtAsbID, assemblyId);
    }
    executeUpdate(dbp.getQuery("updateAssemblyDesc", substitutions));

    return assemblyId;
  }

  // Add a new assembly of the given type to the current trial's
  // RUNTIME assemblies, returning the new ID
  private String addAssembly(String idType)
    throws SQLException
  {
    String assemblyId;
    String assemblyIdPrefix = idType + "-";
    substitutions.put(":assembly_id_pattern:", assemblyIdPrefix + "____");
    substitutions.put(":assembly_type:", idType);
    assemblyId = getNextId("queryMaxAssemblyId", assemblyIdPrefix);
    substitutions.put(":assembly_id:", sqlQuote(assemblyId));
    substitutions.put(":trial_id:", trialId);
    executeUpdate(dbp.getQuery("insertAssemblyId", substitutions));
    executeUpdate(dbp.getQuery("insertTrialAssembly", substitutions));
    return assemblyId;
  }

  private void addAssemblyToConfig(String assembly_id) throws SQLException {
    substitutions.put(":assembly_id:", sqlQuote(assembly_id));
    substitutions.put(":trial_id:", trialId);
    executeUpdate(dbp.getQuery("insertTrialConfigAssembly", substitutions));
  }    

  /**
   * See if the given Assembly is referenced by _any_ Trial.
   *
   * @param assembly_id a <code>String</code> assembly to check
   * @return a <code>boolean</code>, true if used
   * @exception SQLException if an error occurs
   */
  public boolean isAssemblyUsed(String assembly_id) throws SQLException {
    // See if anyone uses this assembly ID anywhere
    substitutions.put(":assembly_id:", sqlQuote(assembly_id));
    substitutions.put(":trial_id:", trialId);
    ResultSet rs = executeQuery(stmt, dbp.getQuery("checkUsedAssembly",
					     substitutions));
    // If not
    if (!rs.next()) { 
      rs.close();
      return false;
    }
    rs.close();
    return true;
  }

  // Copy assemblies of type cmtType in RUNTIME expt_trial_assembly from old ID to new
  // Do we need to do the config table too?
  private void copyCMTAssemblies(String oldTrialId, String newTrialId)
    throws SQLException
  {
    substitutions.put(":old_trial_id:", oldTrialId);
    substitutions.put(":new_trial_id:", newTrialId);
    String qs = dbp.getQuery("copyCMTAssembliesQueryNames", substitutions);
    StringTokenizer queries = new StringTokenizer(qs);
    while (queries.hasMoreTokens()) {
      String queryName = queries.nextToken();
      executeUpdate(dbp.getQuery(queryName, substitutions));
    }
  }
  
  // Copy stuff in expt_trial_thread from old ID to new
  private void copyCMTThreads(String oldTrialId, String newTrialId)
    throws SQLException
  {
    substitutions.put(":old_trial_id:", oldTrialId);
    substitutions.put(":new_trial_id:", newTrialId);
    String qs = dbp.getQuery("copyCMTThreadsQueryNames", substitutions);
    StringTokenizer queries = new StringTokenizer(qs);
    while (queries.hasMoreTokens()) {
      String queryName = queries.nextToken();
      executeUpdate(dbp.getQuery(queryName, substitutions));
    }
  }

  /**
   * Copy all OPLAN entries for the given old Assembly into the given new one.
   * No check is made first to see if there are any such rows.
   *
   * @param oldAssemblyID a <code>String</code> assembly with OPLAN data to copy from
   * @param newAssemblyID a <code>String</code> new assembly to copy OPLAN data to
   * @exception SQLException if an error occurs
   */
  public void copyOPLANData(String oldAssemblyID, String newAssemblyID) 
    throws SQLException
  {
    substitutions.put(":old_assembly_id:", oldAssemblyID);
    substitutions.put(":new_assembly_id:", newAssemblyID);
    String qs = dbp.getQuery("copyOPLANQueryNames", substitutions);
    StringTokenizer queries = new StringTokenizer(qs);
    while (queries.hasMoreTokens()) {
      String queryName = queries.nextToken();
      executeUpdate(dbp.getQuery(queryName, substitutions));
    }
  }

  /**
   * Ensure the experiment's assemblies are recorded appropriately. 
   * Called after saving an experiment. 
   * This will: <ul>
   * <li>Ensure have only the new HNA assembly, both runtime and config</li>
   * <li>Ensure have a society assembly (CMT or CSA)</li>
   * <li>Ensure runtime has a complete definition, possibly
   * including a CSMI assembly</li>
   * </ul><br>
   *
   * @return a <code>boolean</code> false on error
   * @exception SQLException if an error occurs
   */
  public boolean fixAssemblies() throws SQLException {
    // return false on error
    // 1: delete any HNA assembly referenced in config (if not referenced elsewhere)
    // --- find any HNA in config
    // Use queryConfigTrialAssemblies and loop through? (returns assembly_ids
    // and just uses the trial_id)
    ResultSet rs = executeQuery(stmt, dbp.getQuery("queryConfigTrialAssemblies", substitutions));
    String assid;
    
    boolean configHasNewHNA = false;
    boolean configShouldHaveCMT = false;
    // Should we have a CMT?
    // Answer: yes if cmtAssemblyId is not null?
    // I should probably know before my loop through what I should have:
    // In general however, cmtAssemblyId might really have a CSA assembly
    // so be careful.
    
    while (rs.next()) {
      assid = rs.getString(1);
      if (assid.startsWith(hnaType)) {
	// This is an HNA assembly ID
	if (! assid.equals(hnaAssemblyId)) {
	  // And not the current one
	  // So remove it for this Trial and potentially all
	  removeConfigAssembly(assid);
	  if (! isAssemblyUsed(assid))
	    cleanAssembly(assid);
	} else {
	  // This is the new HNA assembly somehow already there
	  configHasNewHNA = true;
	}
      } else {
	// This should be a CMT or CSA assembly, and only one.
	if (assid.startsWith(cmtType)) {
	  if (! assid.equals(cmtAssemblyId)) {
	    // an old CMT assembly still listed somehow. Delete it!
	    removeConfigAssembly(assid);
	    
	    // Completely, if necessary
	    if (! isAssemblyUsed(assid))
	      cleanAssembly(assid);	    
	  } else {
	    // This is the current CMT in the config area. Good.
	    // But should it be a CSA instead? If so, probably an error, right?
	    // Also, do we have a CSA in here already? If so, an error.
	  }
	} else if (assid.startsWith(csaType)) {
	  if (! assid.equals(csaAssemblyId)) {
	    // and old CSA assembly still listed somehow. Delete it!
	    removeConfigAssembly(assid);

	    // Completely, if necessary
	    if (! isAssemblyUsed(assid))
	      cleanAssembly(assid);	    
	  } else {
	    // This is the current CSA in the config area.
	    // This is only good if there's no CMT.
	    // Is there?
	    // If so, error
	  }
	} else {
	  // What kind of assembly is this. Delete it!
	    removeConfigAssembly(assid);

	  // Completely, if necessary
	  if (! isAssemblyUsed(assid))
	    cleanAssembly(assid);	    
	}
      }
    }
    rs.close();
    
    // 2: copy into config-time ref to runtime HNA if not already there
    if (! configHasNewHNA)
      addAssemblyToConfig(hnaAssemblyId);
    
    // 3: Ensure config-time has a CMT _or_ CSA
    // use queryConfigTrialAssemblies -- see above
    // and cmtType and csaType
    // if has both => error
    //--- do what? Delete the CSA, assuming it was erroneously written
    // there when should have only been in runtime?
    // Maybe check that there are at least some recipes first?
    // Or assume that this is an experiment originally created
    // from a CMT society, and the CSA is correct?

    // if has none then no society. This is OK, but then it should
    // not have  CMT, CSA, or CSMI in runtim

    // if no CMT
    // and no CSA
    // Then ensure no CSMI in runtime. nor CMT or CSA

    // 4: Ensure run-time has a CMT _or_ CSA (only CSA if CSA in config_time)
    // use queryTrialAssemblies
    // and cmtType and csaType
    rs = executeQuery(stmt, dbp.getQuery("queryConfigTrialAssemblies", substitutions));
    assid = null;
    while (rs.next()) {
      assid = rs.getString(1);
      if (assid.startsWith(hnaType)) {
	// This is an HNA assembly ID
	if (! assid.equals(hnaAssemblyId)) {
	  // And not the current one
	  // So remove it for this Trial and potentially all
	  removeRuntimeAssembly(assid);

	  if (! isAssemblyUsed(assid))
	    cleanAssembly(assid);

	  // But more importantly, this is really an error!
	} else {
	  // This is the new HNA assembly already there, as expected
	}
      } else {
	// This should be a CMT or CSA assembly
	if (assid.startsWith(cmtType)) {
	  if (! assid.equals(cmtAssemblyId)) {
	    // an old CMT assembly still listed somehow. Delete it!
	    removeRuntimeAssembly(assid);

	    // Completely, if necessary
	    if (! isAssemblyUsed(assid))
	      cleanAssembly(assid);	    
	  } else {
	    // This is the current CMT in the runtime area. Fine, 
	    // but there better not be a CSA in runtime
	    // or a CSA in config,
	    // and better be same CMT in config
	    // Otherwise, an error
	  }
	} else if (assid.startsWith(csaType)) {
	  if (! assid.equals(csaAssemblyId)) {
	    // an old CSA assembly still listed somehow Or perhaps
	    // it is allowed for societies configured from CSA?

	    // If it is wrong, remove it.
	    removeRuntimeAssembly(assid);

	    // Completely, if necessary
	    if (! isAssemblyUsed(assid))
	      cleanAssembly(assid);	    
	  } else {
	    // This is the current CSA in the runtime area.
	    // This is only good if there's no CMT also in runtime
	    // and either same CSA in config time or no CSMI here
	    // otherwise, error
	  }
	} else if (assid.startsWith(csmiType)) {
	  if (! assid.equals(csmiAssemblyId)) {
	    // not an expected CSMI. delete it
	    removeRuntimeAssembly(assid);

	    // Completely, if necessary
	    if (! isAssemblyUsed(assid))
	      cleanAssembly(assid);	    
	  } else {
	    // Got our CSMI. Make sure have either CMT or CSA as well, and if CSA,
	    // it is also in config time
	  }
	} else {
	  // What kind of assembly is this. Delete it!
	  removeRuntimeAssembly(assid);

	  // Completely, if necessary
	  if (! isAssemblyUsed(assid))
	    cleanAssembly(assid);	    
	}
      }
    }
    rs.close();

    // Should have neither if config-time had neither
    // if config had and runtime does not: this will not
    // be runnable. It at least should not have a CSMI either. But
    // really it makes no sense.
    // if config did not and runtime does not, that is OK (should
    // not have a CSMI either)
    // if config had CMT and runtime has CMT, it had better be the _same_.
    // if it is not same, then ERROR
    // if config had CMT and runtme has CSA, that is OK.
    // if config has CSA and runtime has CMT - ERROR
    // if runtime has _both_ => ERROR. If config has same CMT then likely
    // could remove the CMT ref in runtime, but don't know for sure

    // 5: Ensure run-time only has a CSMI if run-time has a CMT or run-time
    // has same CSA as in config-time
    // 6: Ensure config-time has no CSMI
    return true;
  }

  /**
   * Add the given List of recipes to the experiment in order.
   *
   * @param recipes a <code>List</code> of recipes in the experiment
   * @exception SQLException if an error occurs
   * @exception IOException if an error occurs
   */
  public void setModRecipes(List recipes) throws SQLException, IOException {
    //        dbp.setDebug(true);
    int order = 0;
    for (Iterator i = recipes.iterator(); i.hasNext(); ) {
      RecipeComponent rc = (RecipeComponent) i.next();
      addTrialRecipe(rc, order++);
    }
  }

  /**
   * Add the given Recipe to the trial in the database at the given spot.
   *
   * @param rc a <code>RecipeComponent</code> to add
   * @param recipeOrder an <code>int</code> spot to add it
   * @exception SQLException if an error occurs
   */
  public void addTrialRecipe(RecipeComponent rc, int recipeOrder)
    throws SQLException
  {
    String recipeId = insureLibRecipe(rc);
    substitutions.put(":recipe_id:", recipeId);
    substitutions.put(":recipe_order:", String.valueOf(recipeOrder));
    executeUpdate(dbp.getQuery("insertTrialRecipe", substitutions));
  }


  /**
   * Populate the HNA assembly for a particular item. Children are
   * populated recursively. Agent components get additional
   * processing related to the organization they represent.
   * @param data the ComponentData of the starting point
   * @return true if doing this population added a component, argument, or agent
   **/
  public boolean populateHNA(ComponentData data)
    throws SQLException
  {
    return populate(data, 1f, hnaAssemblyId);
  }

  /**
   * Populate the CSMI assembly for a particular item. Children are
   * populated recursively. Agent components get additional
   * processing related to the organization they represent.
   * @param data the ComponentData of the starting point
   * @return true if doing this population added a component, argument, or agent
   **/
  public boolean populateCSMI(ComponentData data)
    throws SQLException
  {
    return populate(data, 1f, csmiAssemblyId);
  }

  public boolean populateCSA(ComponentData data)
    throws SQLException
  {
    //Save the full given tree into a CSA assembly, which should already
    // exist. Someone else must put the entry for this assembly
    // in the runtime or config time places, as necessary
    return populate(data, 1f, csaAssemblyId);
    // or should that be cmtAssemblyId);
  }

  /**
   * Force save of component data as new CMT assembly in component_arg and hierachy tables.
   * Does not effect OPLAN, PGs, relationships, etc. 
   * Does not try to save only non-recipe stuff.
   * @param data the ComponentData of the starting point
   * @return true if doing this population added a component, argument, or agent
   **/
  public boolean repopulateCMT(ComponentData data)
    throws SQLException
  {
    // I don't think this is ever used.
    // Also, I wonder if it would work: Will the CMT ID match to line
    // up with the OPLAN, etc stuff? 

    // FIXME: Careful! If this is applied with a real CMT assembly,
    // the newly written version of cleanTrial will delete stuff from the 
    // OPLAN tables, which can't be restored!!!

    // Force the CMT assembly to be removed
    cleanTrial("", "", "", trialId);
    String cmtAssemblyId = addAssembly(cmtType);
    setNewAssemblyIds();
    componentArgs.clear();
    return populate(data, 1f, cmtAssemblyId);
  }

  /**
   * Save the given data tree into the given assembly ID.
   * Call itself recursively if necessary.
   * Return true if it added stuff.
   * This is inordinately difficult.
   **/
  private boolean populate(ComponentData data, float insertionOrder, String assemblyId)
    throws SQLException
  {
    boolean result = false;
    ComponentData parent = data.getParent();
    String id = getComponentAlibId(data);
    boolean isAdded = false;
    addComponentDataSubstitutions(data);
    substitutions.put(":assembly_id:", sqlQuote(assemblyId));
    substitutions.put(":insertion_order:", String.valueOf(insertionOrder));
    SortedSet oldArgs = (SortedSet) componentArgs.get(id);
    if (oldArgs == null) {  // Don't know what the current args are
      insureAlib();       // Insure that the alib and lib components are defined
      ResultSet rs;
      rs = executeQuery(stmt, dbp.getQuery("queryComponentArgs", substitutions));
      oldArgs = new TreeSet();
      while (rs.next()) {
	oldArgs.add(new Argument(rs.getString(1), rs.getFloat(2)));
      }
      rs.close();
      if (parent != null) {
	// Is given component in runtime hierarchy?
	rs = executeQuery(stmt, dbp.getQuery("checkComponentHierarchy",
					     substitutions));
	// If not, add it
	if (!rs.next()) {
	  executeUpdate(dbp.getQuery("insertComponentHierarchy",
				     substitutions));
	  isAdded = true;
	  result = true; // Modified the experiment in the database 
	}
	rs.close();
      }
      componentArgs.put(id, oldArgs); // Avoid doing this again
    }
    // Now build a SortedSet of what the args should be
    SortedSet newArgs = new TreeSet();
    Object[] params = data.getParameters();
    for (int i = 0; i < params.length; i++) {
      newArgs.add(new Argument(params[i].toString(), i));
    }

    // FIXME: THIS MUST BE CHANGED SOMEHOW!!!!
    // If I sense a removal / modification, set some 
    // flag and abort probably...


    // The new args must only contain additions. There must be no
    // deletions or alterations of the old args nor is it allowed
    // to change the relative order of the existing arguments. If
    // there are fewer newArgs than oldArgs, there is clearly a
    // violation of this premise.
    int excess = newArgs.size() - oldArgs.size();
    if (excess < 0) {
      throw new IllegalArgumentException("Attempt to remove "
					 + (-excess)
					 + " args from "
					 + data);
    }
    // Prepare to iterate through both old and new args.
    // argsToInsert accumulates the new arguments to be inserted.
    // Each of the new args is compared with the next unaccounted
    // for old arg. If the new arg has a value different from the
    // next old arg it is assumed to be an insertion since
    // deletions and modifications are disallowed. But, if the
    // excess new arg count has been reduced to zero, then no new
    // args are possible because there won't be enought old args
    // to match the remaining new ones so we throw an exception.
    // The argument order is interpolated between the preceding
    // and following old argument.
    Iterator newIter = newArgs.iterator();
    Iterator oldIter = oldArgs.iterator();
    List argsToInsert = new ArrayList();
    float prev = Float.NaN;
    while (oldIter.hasNext()) {
      Argument newArg = (Argument) newIter.next();
      Argument oldArg = (Argument) oldIter.next();
      while (!newArg.argument.equals(oldArg.argument)) {
	if(log.isWarnEnabled()) {
	  log.warn("newArg != oldArg, new: " +newArg.toString() + " old: " + oldArg.toString());
	}
	// Assume arguments were inserted
	excess--;
	if (excess < 0) {
	  throw new IllegalArgumentException("Component args cannot be modified or removed: " + data);
	}
	if (Float.isNaN(prev)) prev = oldArg.order - 1f;
	argsToInsert.add(new Argument(newArg.argument, (prev + oldArg.order) * 0.5f));
	newArg = (Argument) newIter.next();
      }
      prev = oldArg.order;
    }
    // Finally, any remaining new args are appended
    if (Float.isNaN(prev)) prev = 0f;
    while (newIter.hasNext()) {
      Argument newArg = (Argument) newIter.next();
      prev += 1f;
      argsToInsert.add(new Argument(newArg.argument, prev));
    }
    // Now write all the new arguments that were not previously
    // present to the database.
    for (Iterator i = argsToInsert.iterator(); i.hasNext(); ) {
      Argument arg = (Argument) i.next();
      substitutions.put(":argument_value:", sqlQuote(arg.argument));
      substitutions.put(":argument_order:", sqlQuote(String.valueOf(arg.order)));
      executeUpdate(dbp.getQuery("insertComponentArg", substitutions));
      oldArgs.add(arg);
      result = true;
    }

    // FIXME:::
    // Save in the CSHNA only the fact that an agent is within a Node
    // if I can, Probably I have to save its args as well - which 
    // is just the name, no biggy.
    // But I don't want to save the Agent definition there.
    // Also, the insureAlibId call above creates the component_alib_id
    // which I suppose is OK.

    // So far so good. But I also only want to call the populateAgent thing
    // if I have to, right? And then only save out the info
    // I have to, right? That's the isAdded thing.
    // Or maybe I can call this whenever I have an Agent
    // and am not in the HNA assembly?
    // No, I only want to write that out if 
    // it isn't already someplace else...
    // How do I do that?

    // Already we only save in the hierarchy if its not already there.
    // That takes care of the existence of plugins, binders
    // in the society. Then the logic above is all crazy
    // to only do added arguments that were new
    // And this populateAgent is probably doing similar.
    // What I need is more complex....

    if (data.getType().equals(ComponentData.AGENT)) {
      if (!assemblyId.equals(hnaAssemblyId)) {
	// isAdded: was this agent just added to the hierarchy
	populateAgent(data, isAdded);
	result = true;
      } else {
	// If this is an Agent and we're in the CSHNA assembly,
	// don't save the plugins, etc now. Just return.
	return result;
      }
    }

    ComponentData[] children = data.getChildren();

    // The following assumes that the insertion order of old
    // children equals their index in the array and that the
    // insertion order of all new children should be the their
    // index in the array as well.
    for (int i = 0, n = children.length; i < n; i++) {
      result |= populate(children[i], i, assemblyId);
    }
    return result;
  }

  private boolean isOverwrite(Object msg) {
    if (overwriteAll) return true;
    if (keepAll) return false;
    if (conflictHandler == null) return false;
    switch (conflictHandler.handleConflict(msg,
					   DBConflictHandler.STANDARD_CHOICES,
					   DBConflictHandler.STANDARD_CHOICES[DBConflictHandler.KEEP]))
      {
      case DBConflictHandler.KEEP:
	return false;
      case DBConflictHandler.OVERWRITE:
	return true;
      case DBConflictHandler.KEEP_ALL:
	keepAll = true;
	return false;
      case DBConflictHandler.OVERWRITE_ALL:
	overwriteAll = true;
	return true;
      }
    return false;
  }

  private void insureLib() throws SQLException {
    ResultSet rs = executeQuery(stmt, dbp.getQuery("checkLibComponent", substitutions));
    if (!rs.next()) {
      executeUpdate(dbp.getQuery("insertLibComponent", substitutions));
    } else {
      String[] subvars = {
	":component_category:",
	":component_class:",
	":insertion_point:"
      };
      StringBuffer diff = compareQueryResults(rs, subvars);
      if (diff == null) {
	// Value is ok
      } else if (conflictHandler != null) {
	String id = (String) substitutions.get(":component_lib_id:");
	String query = dbp.getQuery("updateLibComponent", substitutions);
	Object[] msg = {
	  "You are attempting to redefine lib component: " + id,
	  diff,
	  "Do you want to overwrite the definition?"
	};
	if (isOverwrite(msg)) {
	  executeUpdate(query);
	}
      }
    }
    rs.close();
  }

  private static Class[] sac = {String.class};

  private StringBuffer compareQueryResults(ResultSet rs, String[] keys) throws SQLException {
    StringBuffer diff = null;
    for (int i = 0; i < keys.length; i++) {
      Object oldValue = rs.getObject(i + 1);
      boolean isEqual;
      if (oldValue instanceof byte[]) { // MySQL crock returns byte array instead of strings
	oldValue = rs.getString(i + 1);
      }
      String newString = (String) substitutions.get(keys[i]);
      Object[] sas = {newString.substring(1, newString.length() - 1)};
      Object newValue = newString;
      try {
	newValue = oldValue.getClass().getConstructor(sac).newInstance(sas);
      } catch (Exception nsme) {
      }
      if (oldValue instanceof Comparable && oldValue.getClass() == newValue.getClass()) {
	isEqual = ((Comparable) newValue).compareTo(oldValue) == 0;
      } else {
	isEqual = newValue.equals(oldValue);
      }
      if (!isEqual) {
	if (diff == null) diff = new StringBuffer();
	diff.append(keys[i].substring(1))
	  .append(" changing ")
	  .append(oldValue)
	  .append(" to ")
	  .append(newValue)
	  .append("\n");
      }
    }
    return diff;
  }

  private void insureAlib() throws SQLException {
    String id = (String) substitutions.get(":component_alib_id:");
    if (alibComponents.contains(id)) return; // Already present
    // may need to be added
    insureLib();
    String query1 = dbp.getQuery("checkAlibComponent", substitutions);
    if (log.isDebugEnabled()) {
      log.debug("insureAlib doing query " + query1);
    }
    ResultSet rs = executeQuery(stmt, dbp.getQuery("checkAlibComponent", substitutions));
    if (!rs.next()) {
      if (log.isDebugEnabled()) {
	log.debug("insureAlib query got nothing - will insert alibcomponent " + id);
      }
      executeUpdate(dbp.getQuery("insertAlibComponent", substitutions));
    } else {
      String[] subvars = {
	":component_name:",
	":component_lib_id:",
	":component_category:",
	":clone_set_id:"
      };
      StringBuffer diff = compareQueryResults(rs, subvars);
      if (diff == null) {
	// Value is ok
      } else if (conflictHandler != null) {
	Object[] msg = {
	  "You are attempting to redefine alib component: " + id,
	  diff,
	  "Do you want to overwrite the definition?"
	};
	if (isOverwrite(msg)) {
	  String query = dbp.getQuery("updateAlibComponent", substitutions);
	  executeUpdate(query);
	}
      }
    }
    alibComponents.add(id); // Avoid re-querying later
    rs.close();
  }

  private void addComponentDataSubstitutions(ComponentData data) {
    substitutions.put(":component_name:", sqlQuote(data.getName()));
    substitutions.put(":component_lib_id:", getComponentLibId(data));
    substitutions.put(":component_alib_id:", sqlQuote(getComponentAlibId(data)));
    substitutions.put(":component_category:", getComponentCategory(data));
    substitutions.put(":component_class:", sqlQuote(data.getClassName()));
    substitutions.put(":insertion_point:", getComponentInsertionPoint(data));
    substitutions.put(":clone_set_id:", getComponentCloneSetId(data));
    substitutions.put(":description:", sqlQuote("Added " + data.getType()));
    ComponentData parent = data.getParent();
    if (parent != null) {
      substitutions.put(":parent_component_alib_id:",
			sqlQuote(getComponentAlibId(parent)));
    } else {
      substitutions.remove(":parent_component_alib_id:");
    }
  }

  /**
   * Special processing for an agent component because agents
   * represent organizations having relationships and property groups.
   * The substitutions Map already has most of the needed substitutions
   **/
  private void populateAgent(ComponentData data, boolean isAdded) throws SQLException {
      
    if(log.isDebugEnabled()) {
      log.debug("populateAgent: " + data.getName());
    }
    AgentAssetData assetData = data.getAgentAssetData();
    if (assetData == null) return;
    substitutions.put(":agent_org_class:", sqlQuote(assetData.getAssetClass()));
    substitutions.put(":agent_lib_name:", sqlQuote(data.getName()));
    substitutions.put(":component_name:", sqlQuote(data.getName()));
    // Only put in the PGs if the Agent is new
    if (isAdded) {
      // finish populating a new agent
      ResultSet rs = executeQuery(stmt, dbp.getQuery("checkAgentOrg", substitutions));
      if (!rs.next()) {
	executeUpdate(dbp.getQuery("insertAgentOrg", substitutions));
      }
      rs.close();
      rs = executeQuery(stmt, dbp.getQuery("checkAsbAgent", substitutions));
      if (!rs.next()) {
	executeUpdate(dbp.getQuery("insertAsbAgent", substitutions));
      }
      rs.close();
      PropGroupData[] pgs = assetData.getPropGroups();
      for (int i = 0; i < pgs.length; i++) {
	PropGroupData pg = pgs[i];
	String pgName = pg.getName();
	PGPropData[] props = pg.getProperties();
	for (int j = 0; j < props.length; j++) {
                  
	  PGPropData prop = props[j];
	  PropertyInfo propInfo = getPropertyInfo(pgName, prop.getName());
          if(propInfo == null) {
            // This is a new PG, and must be stored in the db.
            setPropertyInfo(pgName, prop.getName(), prop.getType(), prop.getSubType());
            propInfo = getPropertyInfo(pgName, prop.getName());
            if(log.isErrorEnabled() && propInfo == null) {
              log.error("Error: Cannot obtain property info for: " + 
                        pgName + ", " + prop.getName());
            }
          }

	  substitutions.put(":component_alib_id:", sqlQuote(getComponentAlibId(data)));
	  substitutions.put(":pg_attribute_lib_id:", sqlQuote(propInfo.getAttributeLibId()));
	  // Are these defaults?
	  substitutions.put(":start_date:", sqlQuote("2000-01-01 00:00:00"));
	  substitutions.put(":end_date:", sqlQuote(null));
	  if (propInfo.isCollection()) {
	    if (!prop.isListType())
	      throw new RuntimeException("Property is not a collection: "
					 + propInfo.toString());
	    String[] values = ((PGPropMultiVal) prop.getValue()).getValuesStringArray();
	    for (int k = 0; k < values.length; k++) {
	      substitutions.put(":attribute_value:", sqlQuote(values[k]));
	      substitutions.put(":attribute_order:", String.valueOf(k + 1));
	      executeUpdate(dbp.getQuery("insertAttribute", substitutions));
	    }
	  } else {

	    if (prop.isListType())
	      throw new RuntimeException("Property is not a single value: "
					 + propInfo.toString());
	    substitutions.put(":attribute_value:", sqlQuote(prop.getValue().toString()));
	    substitutions.put(":attribute_order:", "1");
	    executeUpdate(dbp.getQuery("insertAttribute", substitutions));
	  }
	}
      }
    }

    // Add relationships even to Agents that
    // aren't new here
    RelationshipData[] relationships = assetData.getRelationshipData();
    //        dbp.setDebug(true);
    for (int i = 0; i < relationships.length; i++) {
      RelationshipData r = relationships[i];
      long startTime = r.getStartTime();
      long endTime = r.getEndTime();
      substitutions.put(":role:", sqlQuote(r.getRole()));
      substitutions.put(":supporting:", sqlQuote(getComponentAlibId(data)));
      substitutions.put(":supported:", getAgentAlibId(r.getSupported()));
      // Huh???
      substitutions.put(":start_date:", "?");
      substitutions.put(":end_date:", "?");
      String query = dbp.getQuery("checkRelationship", substitutions);
      PreparedStatement pstmt = dbConnection.prepareStatement(query);
      pstmt.setTimestamp(1, new Timestamp(startTime));
      ResultSet rs = executeQuery(pstmt, query);
      if (!rs.next()) {
	query = dbp.getQuery("insertRelationship", substitutions);
	PreparedStatement pstmt2 = dbConnection.prepareStatement(query);
	pstmt2.setTimestamp(1, new Timestamp(startTime));
	if (endTime > 0L) {
	  pstmt2.setTimestamp(2, new Timestamp(endTime));
	} else {
	  pstmt2.setNull(2, Types.TIMESTAMP);
	}
	try {
	  executeUpdate(pstmt2, query);
	} catch (SQLException e) {
	  if(log.isErrorEnabled()) {
	    log.error("SQLException query: " + query, e);
	  }
	  throw e;
	} finally {
	  pstmt2.close();
	}
      }
      pstmt.close();
    }
  }

  public Set executeQuery(String queryName) throws SQLException {
    Statement stmt = dbConnection.createStatement();
    ResultSet rs = executeQuery(stmt, dbp.getQuery(queryName, substitutions));
    Set results = new HashSet();
    while (rs.next()) {
      results.add(rs.getString(1));
    }
    rs.close();
    stmt.close();
    return results;
  }

  public String[][] executeQueryForComponent(String queryName, ComponentData cd) throws SQLException {
    addComponentDataSubstitutions(cd);
    Statement stmt = dbConnection.createStatement();
    ResultSet rs = executeQuery(stmt, dbp.getQuery(queryName, substitutions));
    List rows = new ArrayList();
    int ncols = rs.getMetaData().getColumnCount();
    while (rs.next()) {
      String[] row = new String[ncols];
      for (int i = 0; i < ncols; i++) {
	row[i] = rs.getString(i + 1);
      }
      rows.add(row);
    }
    rs.close();
    stmt.close();
    return (String[][]) rows.toArray(new String[rows.size()][]);
  }

  private void setPropertyInfo(String pgName, String propName,
                               String propType, String propSubtype) throws SQLException {
    if(log.isDebugEnabled()) {
      log.debug("setPropertyInfo("+pgName+", "+propName+", "+
                propType+", "+propSubtype+")");
    }

    Statement stmt = dbConnection.createStatement();
    substitutions.put(":attribute_lib_id:", sqlQuote(pgName + "|" + propName));
    substitutions.put(":pg_name:", pgName);
    substitutions.put(":attribute_name:", propName);
    substitutions.put(":attribute_type:", propSubtype);
    if(propType.equalsIgnoreCase("Collection")) {
      substitutions.put(":aggregate_type:", "COLLECTION");
    } else {
      substitutions.put(":aggregate_type:", "SINGLE");
    }
    executeUpdate(dbp.getQuery("insertLibPGAttribute", substitutions));
  }

  /**
   * Get the PropertyInfo for a pg/prop pair. If the information is
   * not in the propertyInfos cache, the cache is filled from the
   * database.
   **/
  private PropertyInfo getPropertyInfo(String pgName, String propName) throws SQLException {
    if(log.isDebugEnabled()) {
      log.debug("getPropertyInfo("+pgName+", "+propName+")");
    }
    PropertyKey key = new PropertyKey(pgName, propName);
    PropertyInfo result = (PropertyInfo) propertyInfos.get(key);
    if (result == null) {
      Statement stmt = dbConnection.createStatement();
      substitutions.put(":pg_name:", pgName);
      substitutions.put(":attribute_name:", propName);
      ResultSet rs = executeQuery(stmt, dbp.getQuery("queryLibPGAttribute", substitutions));
      while (rs.next()) {
	String s1 = rs.getString(1);
	String s2 = rs.getString(2);
	PropertyKey key1 = new PropertyKey(s1, s2);
	PropertyInfo info = new PropertyInfo(key1,
					     rs.getString(3),
					     rs.getString(4),
					     rs.getString(5));
                
	propertyInfos.put(key1, info);
	if (key1.equals(key)) {
	  result = info;
	}

      }
      rs.close();
      stmt.close();
    }
    return result;
  }

  /**
   * Get the component lib id of the underlying lib component for
   * the component described by the specified ComponentData. Each
   * type of component has a different convention for constructing
   * its lib id.
   **/
  private String getComponentLibId(ComponentData data) {
    if (data == null) return sqlQuote(null);
    String componentType = data.getType();
    if (componentType.equals(ComponentData.PLUGIN)) {
      return sqlQuote(data.getType() + "|" + data.getClassName());
    }
    if (componentType.equals(ComponentData.NODEBINDER)) {
      return sqlQuote(data.getType() + "|" + data.getClassName());
    }
    if (componentType.equals(ComponentData.AGENTBINDER)) {
      return sqlQuote(data.getType() + "|" + data.getClassName());
    }
    if (componentType.equals(ComponentData.AGENT)) {
      String agentName = data.getName();
      return sqlQuote(agentName);
    }
    if (componentType.equals(ComponentData.NODE)) {
      String nodeName = data.getName();
      return sqlQuote(nodeName);
    }
    if (componentType.equals(ComponentData.HOST)) {
      String hostName = data.getName();
      return sqlQuote(hostName);
    }
    if (componentType.equals(ComponentData.SOCIETY)) {
      String societyName = data.getName();
      return sqlQuote(data.getType() + "|" + societyName);
    }
    ComponentData parent = data.getParent();
    return sqlQuote(data.getType() + "|" + getFullName(data));
  }

  /**
   * Get the component insertion point for the component described
   * by the specified ComponentData. Each type of component has a
   * different insertion point. Some have none.
   **/
  private String getComponentInsertionPoint(ComponentData data) {
    if (data == null) return sqlQuote(null);
    String componentType = data.getType();
    if (componentType.equals(ComponentData.PLUGIN)) {
      return sqlQuote("Node.AgentManager.Agent.PluginManager.Plugin");
    }
    if (componentType.equals(ComponentData.NODEBINDER)) {
      return sqlQuote("Node.AgentManager.Binder");
    }
    if (componentType.equals(ComponentData.AGENTBINDER)) {
      return sqlQuote("Node.AgentManager.Agent.PluginManager.Binder");
    }
    if (componentType.equals(ComponentData.AGENT)) {
      return sqlQuote("Node.AgentManager.Agent");
    }
    if (componentType.equals(ComponentData.NODE)) {
      return sqlQuote("Node");
    }
    if (componentType.equals(ComponentData.HOST)) {
      return sqlQuote("Host");
    }
    if (componentType.equals(ComponentData.SOCIETY)) {
      return sqlQuote("Society");
    }
    // FIXME: Node Agent? ENCLAVE? Randome others?
    return sqlQuote(null);
  }

  /**
   * Get the component clone set id for the component described
   * by the specified ComponentData.
   **/
  private String getComponentCloneSetId(ComponentData data) {
    if (data == null) return sqlQuote(null);
    return sqlQuote("0");
  }

  /**
   * Create a component alib id for this component. Again, each kind
   * of component has a different convention for constructing its
   * alib id.
   **/
  public String getComponentAlibId(ComponentData data) {
    // This relies on being able to find the ancestors
    // for plugins and binders, which you cant always do
    // FIXME!!
    if (data == null) return null;
    String result = data.getAlibID();
    if (result == null) {
      String componentType = data.getType();
      if (componentType.equals(ComponentData.PLUGIN)) {
	ComponentData anc = findAncestorOfType(data, ComponentData.AGENT);
	if (anc == null) {
	  return data.getName();
	} else {
	  String agentName = anc.getName();
	  // Create the alibID using the component name, not the class
	  // This allows users to insert 2 items of same class at same point
	  // Note that if all the args are the same they really shouldnt
	  // do this.
	  //result = agentName + "|" + data.getClassName();
	  result = agentName + "|" + data.getName();
	}
      } else if (componentType.equals(ComponentData.NODEBINDER)) {
	ComponentData anc = findAncestorOfType(data, ComponentData.NODE);
	if (anc == null) {
	  return data.getName();
	} else {
	  String nodeName = anc.getName();
	  result = nodeName + "|" + data.getName();
	  //result = nodeName + "|" + data.getClassName();
	}
      } else if (componentType.equals(ComponentData.AGENTBINDER)) {
	ComponentData anc = findAncestorOfType(data, ComponentData.AGENT);
	if (anc == null) {
	  return data.getName();
	} else {
	  String agentName = anc.getName();
	  result = agentName + "|" + data.getName();
	  //result = agentName + "|" + data.getClassName();
	}
      } else if (componentType.equals(ComponentData.SOCIETY)) {
	result = ComponentData.SOCIETY + "|" + data.getName();
      } else {
	result = data.getName();
      }
      data.setAlibID(result);
    }
    return result;
  }

  /**
   * The convention for the alib id of an agent component is that it
   * is the base agent name prefixed with a clone set id and a
   * hyphen. We are not present concerned with clone set ids, so we
   * use a fixed CLONE_SET_ID.
   **/
  private String getAgentAlibId(String agentName) {
    return sqlQuote("" + agentName);
  }

  private String getFullName(ComponentData data) {
    ComponentData parent = data.getParent();
    if (parent == null) return data.getName();
    return getFullName(parent) + "|" + data.getName();
  }

  /**
   * We have conveniently arranged that the type of a ComponentData
   * is the same as the category of a database component. We simple
   * wrap it in quotes and return.
   **/
  private String getComponentCategory(ComponentData data) {
    return sqlQuote(data.getType());
  }

  /**
   * Search up the parent links for an ancestor of a particular type.
   **/
  private ComponentData findAncestorOfType(ComponentData data, String type) {
    for (ComponentData parent = data.getParent(); parent != null; parent = parent.getParent()) {
      if (parent.getType().equals(type)) return parent;
    }
    return null;
  }

  private static class Argument implements Comparable {
    public String argument;
    public float order;
    public Argument(String a, float f) {
      argument = a;
      order = f;
    }

    public int compareTo(Object o) {
      Argument that = (Argument) o;
      float diff = this.order - that.order;
      if (diff < 0) return -1;
      if (diff > 0) return 1;
      return this.argument.compareTo(that.argument);
    }

    public boolean equals(Object o) {
      if (o instanceof Argument) return compareTo(o) == 0;
      return false;
    }

    public String toString() {
      return "[" + order + "]=" + argument;
    }
  }
} // end of PopulateDb

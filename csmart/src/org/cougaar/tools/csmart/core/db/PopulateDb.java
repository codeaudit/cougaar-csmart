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
import org.cougaar.util.log.Logger;

import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.core.cdata.*;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * This class takes a structure of ComponentData objects and populates
 * the configuration database with some or all of the components
 * described by the data. It can save just a society, or an experiment.
 **/
public class PopulateDb extends PDbBase {
  private String exptId;
  private String trialId;

  private String cmtType;
  private String hnaType;
  private String csmiType;
  private static String csaType = "CSA";
  private static String realcmtType = "CMT";
  private static String realcsmiType = "CSMI";
  private static String realcshnaType = "CSHNA";

  // The cmtAssemblyId defines the baseline society - 
  // the one in the Config assemblies table. Note however
  // that sometimes it is of type csaType
  private String cmtAssemblyId;
  private String csmiAssemblyId;
  private String csaAssemblyId;
  private String hnaAssemblyId;

  private Map propertyInfos = new HashMap();
  private Set alibComponents = new HashSet();
  private Map componentArgs = new HashMap();

  // Track what Agents got added to the experiment
  // If an Agent is in here, then when we're not
  // doing an HNA assembly, we'll save the OrgAsset data
  // This is a list of component ALIB IDs
  private List addedAgents = new ArrayList();

  // Flag to indicate that during a save, a component
  // was apparently removed.
  // This indicates to the caller that they must
  // call populateCSA
  public boolean componentWasRemoved = false;

  private DBConflictHandler conflictHandler;

  // Flags used to decide what to do when DB has different entries
  // in the lib and alib tables for components
  private boolean keepAll = false;
  private boolean overwriteAll = false;

  public transient Logger log; 

  private static Class[] sac = {String.class};

  /**
   * Construct a PDB for saving just a non-CMT society. 
   * However, the society may have come from a CMT society,
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

      // Remove the previous definition of that assembly
      // in preparation for saving this definition
      // However, no point in removing the OPLAN stuff
      // or the line in asb_assembly. Hence the second argument.
      cleanAssembly(assemblyId, true);
    } else {
      // Create new CSA assembly to hold the data, copying OPLAN info if any
      // Also set the csaAssemblyId and cmtAssemblyId parameters
      try {
	// Save this ID someplace? The creator stuffs
	// it in the CSA slot
        assemblyId = createCSAAssembly(cmtAsbID, societyName);
	cmtAssemblyId = assemblyId;
      } catch( SQLException se ) {
        if(log.isErrorEnabled()) {
          log.error("createCSAAssembly error: ", se);
        }
      } 
    }

    substitutions.put(":assembly_id:", sqlQuote(assemblyId));
    cmtAssemblyId = assemblyId;

    // OK, at this point we're ready to save out the society

    // Must set the assembly_match variable
    List tmp = new ArrayList();
    tmp.add(cmtAssemblyId);
    String asbmatch = DBUtils.getAssemblyMatch(tmp);
    substitutions.put(":assembly_match:", asbmatch);
  } // End of constructor for saving just a society

  /**
   * Creates a new <code>PopulateDb</code> for saving an Experiment
   *
   * @param cmtType a <code>String</code> value
   * @param hnaType a <code>String</code> value - unused
   * @param csmiType a <code>String</code> value - unused
   * @param experimentName a <code>String</code> value
   * @param exptId a <code>String</code> value
   * @param trialId a <code>String</code> value
   * @param ch a <code>DBConflictHandler</code> value
   * @param societyId a <code>String</code> value
   * @exception SQLException if an error occurs
   * @exception IOException if an error occurs
   */
  public PopulateDb(String cmtType, String hnaType, String csmiType, String experimentName,
		    String exptId, String trialId, DBConflictHandler ch, String societyId)
    throws SQLException, IOException
   {
    super();
    createLogger();
    // FIXME: I'm not using these types, why require them?
    if (ch == null) throw new IllegalArgumentException("null conflict handler");
    this.cmtType = cmtType;
    this.hnaType = realcshnaType;
    this.csmiType = realcsmiType;
    if(exptId == null) {
      // Create a new exptId here.
      exptId = newExperiment("EXPT", experimentName, "NON-CFW EXPERIMENT");
      // Trial and experiment have same name
      trialId = newTrial(exptId + ".TRIAL", experimentName, "NON-CFW TRIAL");
    }
    this.trialId = trialId;
    this.exptId = exptId;
    this.conflictHandler = ch;
    this.cmtAssemblyId = societyId;
    substitutions.put(":cmt_type:", cmtType);
    substitutions.put(":csa_type:", csaType);
    substitutions.put(":expt_id:", exptId);
    String oldExperimentName = getOldExperimentName();
    // If the experiment name was changed, get a new ID with 
    // all the old assemblies, threads
    if (!experimentName.equals(oldExperimentName)) {
      cloneTrial(trialId, experimentName, "Modified " + oldExperimentName);
    }
    substitutions.put(":trial_id:", trialId); 

    // Society was previously saved.
    if (societyId != null) {
      // is it already in the config DB and runtime DB? If not, add it
      if (! assemblyInConfig(societyId)) {
	if (log.isDebugEnabled()) {
	  log.debug("Didn't have soc in Config: " + societyId);
	}
	// first remove any other CMT or CSA assembly in config for this experiment
	cleanOldConfigSocietyAssemblies();

	// then add it to config
	addAssemblyToConfig(societyId);
      }

      // I can't think of an easy solution. You just
      // have to delete the old CSMI, CSHNA assemblies
      // That way, when you go to save the current setup, you have
      // a chance. So this method should remove the current
      // CSHNA (both tables), CSMI, and, if societyId is not in the runtime
      // but is in the config, then any CSA is in the runtime as well.
      cleanTrial(getAssemblyType(societyId), hnaType, csmiType, trialId);
	
      if (! assemblyInRuntime(societyId)) {
	if (log.isDebugEnabled()) {
	  log.debug("Didn't have soc in Runtime: " + societyId);
	}
	// first remove any CMT or CSA assembly in runtime for this experiment
	cleanOldRuntimeSocietyAssemblies();

	// then put this in
	addAssemblyToRuntime(societyId);
      }

      // Do I have to set types somehow?
    } // end of handling of existing societyId

    // Make sure all the necessary assembly IDs are created, saved
    setNewAssemblyIds();
   } // End of constructor for saving an experiment

  /**
   * A trivial constructor for use basically only when you want
   * to run some queries based on what is in your experiment. 
   * Used when dumping INI files.
   *
   * @param exptId a <code>String</code> ID for the experiment
   * @param trialId a <code>String</code> Trial ID
   * @exception SQLException if an error occurs
   * @exception IOException if an error occurs
   */
  public PopulateDb(String exptId, String trialId) 
    throws SQLException, IOException {
    super();
    createLogger();
    if (exptId != null) {
      this.exptId = exptId;
    } else {
      this.exptId = "";
    }
    substitutions.put(":expt_id:", this.exptId);
       
    if (trialId != null) {
      this.trialId = trialId;
    } else {
      this.trialId = "";
    }
    substitutions.put(":trial_id:", this.trialId);
    setAssemblyMatch();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Compare given assembly with the real assembly types,
   * and return the real type that matches, or Unknown if no match.
   *
   * @param assemblyId a <code>String</code> assembly to compare
   * @return a <code>String</code> assembly type
   */
  private String getAssemblyType(String assemblyId) {
    if (assemblyId.startsWith(realcmtType)) {
      return realcmtType;
    } else if (assemblyId.startsWith(csaType)) {
      return csaType;
    } else if (assemblyId.startsWith(hnaType)) {
      return hnaType;
    } else if (assemblyId.startsWith(csmiType)) {
      return csmiType;
    } else {
      return "Unknown";
    }
  }

  // Get the name of the experiment with the ID already put in the substitutions
  // table
  private String getOldExperimentName() throws SQLException {
    ResultSet rs = executeQuery(stmt, dbp.getQuery("queryExptName", substitutions));
    if (rs.next()) {
      return rs.getString(1);
    }
    return "";
  }

  // Get the current description of the assembly given from the DB,
  // empty String if not found
  private String getAssemblyDesc(String assemblyId) throws SQLException {
    substitutions.put(":assembly_id:", sqlQuote(assemblyId));
    ResultSet rs = executeQuery(stmt, dbp.getQuery("queryAssemblyDesc", substitutions));
    String desc = "";
    if (rs.next()) {
      desc = rs.getString(1);
    }
    rs.close();
    return desc;
  }


  public String getExperimentId() {
    return exptId;
  }

  public String getTrialId() {
    if(log.isDebugEnabled()) {
      log.debug("trialId: " + trialId);
    }
    return trialId;
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

  /**
   * Delete CMT and CSA assemblies in configtime table for this Trial. If that was
   * the last use, completely remove the assembly. Uses the cmtType and csaType
   * substitutions, which must already be present
   *
   * @exception SQLException if an error occurs
   */
  private void cleanOldConfigSocietyAssemblies() throws SQLException {
    ResultSet rs =
      executeQuery(stmt, dbp.getQuery("queryOldSocietyConfigAssembliesToClean", substitutions));
    while (rs.next()) {
      String asb = rs.getString(1);
      if (asb == null || asb.equals(""))
	continue;
      if (log.isDebugEnabled()) {
	log.debug("Cleaning asb from config: " + asb);
      }
      substitutions.put(":assemblies_to_clean:", "(" + sqlQuote(asb) + ")");
      executeUpdate(dbp.getQuery("cleanTrialConfigAssembly", substitutions));

      // So we've just removed the assembly from the config time.
      // Will remove it now unless it is also in runtime
      if (log.isDebugEnabled() && assemblyInRuntime(asb)) {
	log.debug("In CleanOld with asb " + asb + " but wont really delete cause its in runtime for trial " + trialId);
      }
      if (! isAssemblyUsed(asb))
	cleanAssembly(asb);
    }
    rs.close();
  }

  /**
   * Delete CMT and CSA assemblies in runtime table for this Trial. If that was
   * the last use, completely remove the assembly. Uses the cmtType and csaType
   * substitutions, which must already be present
   *
   * @exception SQLException if an error occurs
   */
  private void cleanOldRuntimeSocietyAssemblies() throws SQLException {
    ResultSet rs =
      executeQuery(stmt, dbp.getQuery("queryOldSocietyRuntimeAssembliesToClean", substitutions));
    while (rs.next()) {
      String asb = rs.getString(1);
      if (asb == null || asb.equals(""))
	continue;
      substitutions.put(":assemblies_to_clean:", "(" + sqlQuote(asb) + ")");
      if (log.isDebugEnabled()) {
	log.debug("Cleaning asb from runtime: " + asb);
      }
      executeUpdate(dbp.getQuery("cleanTrialAssembly", substitutions));

      // So we've just removed the assembly from the config time.
      // Will remove it now unless it is also in runtime
      if (log.isDebugEnabled() && assemblyInConfig(asb)) {
	log.debug("In CleanOld with asb " + asb + " but wont really delete cause its in configtime for trial " + trialId);
      }
      if (! isAssemblyUsed(asb))
	cleanAssembly(asb);
    }
    rs.close();
  }

  /**
   * Clean out a trial for re-saving. So remove the old HNA assembly
   * (from both tables) and CSMI assembly if any. 
   * Also remove any current trials - must be reset. 
   * The caller must make sure that any extra CSA assembly not in runtime,
   * when the society is defined by a CSA assembly.
   **/
  private void cleanTrial(String cmtType, String hnaType, String csmiType, String oldTrialId)
    throws SQLException
  {
    // Basically we'll drop the HNA, CSMI assembly and recipes. It will drop
    // the CMT assembly too, but only if cmtType is null
    // Also drop any CSA in runtime if cmtType is not a CSA
    if(log.isDebugEnabled()) {
      log.debug("Cleaning all in trial except " + cmtType +", but including " + hnaType+", " + csmiType+", from " + oldTrialId);
    }
    substitutions.put(":trial_id:", oldTrialId);
    substitutions.put(":cmt_type:", cmtType);
    substitutions.put(":hna_type:", hnaType);
    substitutions.put(":csmi_type:", csmiType);
    substitutions.put(":csa_type:", csaType);

    // check that each Assembly it finds is not
    // referenced by another experiment. If it is, then we only delete from 
    // trial_assembly and trial_config_assembly

    // OK. This will succesfully grab CSMI and CSHNA assemblies in runtime.
    // And if the real society is a CMT society, it will also get a CSA assembly.
    // So I just need an extra check to delete any CSA assembly in runtime if
    // if the input cmtType is "CSA" and the CSA assembly in the runtime
    // is not the same as the one in config time (or the one I got as input)
    // Question: do those cleanOldConfigSocietyAssemblies, etc methods
    // do what I need already? No: It removes from the config/runtime table
    // _all_ CSA & CMT assemblies. I could do that after most of this,
    // and then re-insert the real society into the runtime, I suppose.
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

	// if asb not used other than the current trial
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
	} else if (log.isDebugEnabled()) {
	  log.debug("cleanTrial not deleting asb " + asb + " cause its still in use outside trial " + trialId);
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

      // Add delete from config table. Must get rid of the HNA from there
      executeUpdate(dbp.getQuery("cleanTrialConfigAssembly", substitutions));

      // now really delete those that are unused.
      // where if this is true there were none
      if (! rfirst) {
	substitutions.put(":assemblies_to_clean:", assembliesToReallyDelete.toString());
	cleanAssemblies(false);
      }
    }
    rs.close();
    if(log.isDebugEnabled()) {
      log.debug("cleanTrial Substitutions: " + substitutions);
    }

    // OK, now if the incoming cmtType is really a CSA, and cmtAssemblyId not in
    // runtime, get the CSA assembly in runtime, remove it from runtime, & if not
    // used, completely remove it
    // However - no need to do this: cleanOldRuntimeAssemblies will already do this

    // Delete all recipes out of this Trial - why would I do this?
    // Cause I re-add them when I finish saving the experiment
    executeUpdate(dbp.getQuery("cleanTrialRecipe", substitutions));
  }

  /**
   * Delete the given assembly ID from this Trials config time list
   *
   * @param assembly_id a <code>String</code> assembly to no longer use at configtime
   * @exception SQLException if an error occurs
   */
  private void removeConfigAssembly(String assembly_id) throws SQLException {
    if (assembly_id == null || assembly_id.equals(""))
      return;
    if (log.isDebugEnabled()) {
      log.debug("Removing assembly from config: " + assembly_id);
    }
    substitutions.put(":assemblies_to_clean:", "(" + sqlQuote(assembly_id) + ")");
    executeUpdate(dbp.getQuery("cleanTrialConfigAssembly", substitutions));
  }

  /**
   * Delete the given assembly ID from this Trials runtime list
   *
   * @param assembly_id a <code>String</code> assembly to no longer use at runtime
   * @exception SQLException if an error occurs
   */
  private void removeRuntimeAssembly(String assembly_id) throws SQLException {
    if (assembly_id == null || assembly_id.equals(""))
      return;
    substitutions.put(":assemblies_to_clean:", "(" + sqlQuote(assembly_id) + ")");
    if (log.isDebugEnabled()) {
      log.debug("Removing assembly from runtime: " + assembly_id);
    }
    executeUpdate(dbp.getQuery("cleanTrialAssembly", substitutions));
  }

  /**
   * Useful external method for completely deleting a Society
   * if it is not in use. Used in Organizer.
   * Note that this silently creates its own pdb. Also note that this
   * really deletes any old assembly, by ID.
   *
   * @param assembly_id a <code>String</code> society ID to delete if not used
   * @exception SQLException if an error occurs
   * @exception IOException if an error occurs
   */
  public static void deleteSociety(String assembly_id) throws SQLException, IOException {
    if (assembly_id == null || assembly_id.equals("")) 
      return;
    PopulateDb pdb = new PopulateDb(null, null);
    if (!pdb.isAssemblyUsed(assembly_id)) {
      if (pdb.log.isInfoEnabled()) {
	pdb.log.info("Deleting assembly from the database: " + assembly_id);
      }
      pdb.cleanAssembly(assembly_id);
    } else if (pdb.log.isInfoEnabled()) {
      pdb.log.info("deleteSociety not deleting asb " + assembly_id + " cause its still in use");
    }
    pdb.close();
  }

  // Delete complete definition of a single assembly
  // This assembly must not be referenced by any trial,
  // The caller must check this.
  // If partial is true, leave any OPLAN info and the assembly ID, so 
  // we can reuse this ID
  private void cleanAssembly(String assembly_id, boolean partial) throws SQLException {
    if (assembly_id == null || assembly_id.equals(""))
      return;
    substitutions.put(":assemblies_to_clean:", "(" + sqlQuote(assembly_id) + ")");
    cleanAssemblies(partial);
  }

  // Delete complete definition of a single assembly
  // This assembly must not be referenced by any trial,
  // The caller must check this
  private void cleanAssembly(String assembly_id) throws SQLException {
    if (assembly_id == null || assembly_id.equals(""))
      return;
    substitutions.put(":assemblies_to_clean:", "(" + sqlQuote(assembly_id) + ")");
    cleanAssemblies(false);
  }

  // Delete complete definition of the assemblies listed
  // in the assemblies_to_clean substitution
  // These assemblies should not be used by any trial,
  // and the caller must check that
  // If partial is true, don't touch
  // the oplan tables or asb_assembly - in this case we expect
  // to re-use the assembly ID
  private void cleanAssemblies(boolean partial) throws SQLException {
    if (log.isDebugEnabled()) {
      log.debug("Deleting assemblies using substitutions: " + substitutions);
    }

    if (! partial)
      executeUpdate(dbp.getQuery("cleanASBAssembly", substitutions));
    executeUpdate(dbp.getQuery("cleanASBComponentArg", substitutions));
    executeUpdate(dbp.getQuery("cleanASBComponentHierarchy", substitutions));
    
    // Add in asb_agent and asb_oplan tables
    executeUpdate(dbp.getQuery("cleanASBAgent", substitutions));
    executeUpdate(dbp.getQuery("cleanASBAgentPGAttr", substitutions));
    executeUpdate(dbp.getQuery("cleanASBAgentRel", substitutions));
    if (! partial) {
      executeUpdate(dbp.getQuery("cleanASBOplan", substitutions));
      executeUpdate(dbp.getQuery("cleanASBOplanAAttr", substitutions));
    }
  }

  // Find all the unused CSMI and CSHNA assemblies and completely delete them
  public void removeOrphanNonSocietyAssemblies() throws SQLException {
    // Do a query for assemblyIDs in asb_assembly, where it is not
    // of type cmtType or csaType
    substitutions.put(":cmt_type:", realcmtType);
    substitutions.put(":csa_type:", csaType);
    substitutions.put(":trial_id:", "");
    ResultSet rs = executeQuery(stmt, dbp.getQuery("queryNonSocietyAssemblies", substitutions));
    String assid = null;

    // This loop is over _all_ CSMI and CSHNA assemblies
    while (rs.next()) {
      assid = rs.getString(1);
      // So be sure to avoid ours
      // FIXME: Does this really do that?
      if (assid == null || assid.equals(csmiAssemblyId) || assid.equals(hnaAssemblyId)) {
	if (log.isDebugEnabled()) {
	  log.debug("removeOrphan not touching my asb: " + assid);
	}
	continue;
      }
      // for each such assembly
      // FIXME: This will not delete a CSHNA or CSMI assembly that in the DB
      // is associated with this Trial, but whose assId we don't have
      // stashed away. Could this be an old one worth removing?
      if (! isAssemblyUsed(assid)) 
	cleanAssembly(assid);
    }
    rs.close();
  }

  // Create the HNA and CSMI assemly IDs if necessary, 
  // saving them to the runtime table of Assemblies. Also set
  // the hnaAssemblyId and csmiAssemblyId variables,
  // and set the assembly_match substitution
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
      if (log.isDebugEnabled()) {
	log.debug("assembly_match set to: " + q.toString());
      }
    }
  }

  // Deals solely with expt_experiment table
  // Create a new experiemnt with the given name and description
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

  // Deals solely with expt_trial table
  // Create a new trial with the given name & description
  // and add it to this experiment
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
    if (log.isDebugEnabled()) {
      log.debug("Creating new CSA : " + (cmtAsbID != null ? "based on soc: " + cmtAsbID : "from scratch") + ": " + societyName);
    }
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
    substitutions.put(":assembly_id:", assemblyId);
    substitutions.put(":trial_id:", trialId);
    // This appears to cause an SQLE sometimes with a double open and close-quote
    // on the assembly_id
    // One to many calls to sqlQuote I think.
    executeUpdate(dbp.getQuery("insertAssemblyId", substitutions));
    executeUpdate(dbp.getQuery("insertTrialAssembly", substitutions));
    return assemblyId;
  }

  /**
   * See if the given assembly is used by this trial at config time
   *
   * @param assembly_id a <code>String</code> to look for in config time
   * @return a <code>boolean</code>, true if this assembly used at config time by this trial
   * @exception SQLException if an error occurs
   */
  private boolean assemblyInConfig(String assembly_id) throws SQLException {
    substitutions.put(":assembly_id:", sqlQuote(assembly_id));
    substitutions.put(":trial_id:", trialId);
    ResultSet rs = executeQuery(stmt, dbp.getQuery("checkThisConfigUsesAssembly",
					     substitutions));
    // If not
    if (!rs.next()) { 
      rs.close();
      return false;
    }
    rs.close();
    return true;
  }

  /**
   * See if the given assembly is used by this trial at runtime
   *
   * @param assembly_id a <code>String</code> to look for in runtime
   * @return a <code>boolean</code>, true if this assembly used at runtime by this trial
   * @exception SQLException if an error occurs
   */
  private boolean assemblyInRuntime(String assembly_id) throws SQLException {
    substitutions.put(":assembly_id:", sqlQuote(assembly_id));
    substitutions.put(":trial_id:", trialId);
    ResultSet rs = executeQuery(stmt, dbp.getQuery("checkThisRuntimeUsesAssembly",
					     substitutions));
    // If not
    if (!rs.next()) { 
      rs.close();
      return false;
    }
    rs.close();
    return true;
  }

  /**
   * Insert the given assembly into this trials config-time configuration
   *
   * @param assembly_id a <code>String</code> to add under this trials configtime
   * @exception SQLException if an error occurs
   */
  private void addAssemblyToConfig(String assembly_id) throws SQLException {
    substitutions.put(":assembly_id:", sqlQuote(assembly_id));
    substitutions.put(":trial_id:", trialId);
    substitutions.put(":assembly_type:", getAssemblyType(assembly_id));
    if (log.isDebugEnabled()) {
      log.debug("Adding asb to config: " + assembly_id);
    }
    executeUpdate(dbp.getQuery("insertTrialConfigAssembly", substitutions));
  }    

  /**
   * Insert the given assembly into this trials runtime configuration
   *
   * @param assembly_id a <code>String</code> to add under this trials runtime
   * @exception SQLException if an error occurs
   */
  private void addAssemblyToRuntime(String assembly_id) throws SQLException {
    substitutions.put(":assembly_id:", assembly_id);
    substitutions.put(":trial_id:", trialId);
    substitutions.put(":assembly_type:", getAssemblyType(assembly_id));
    if (log.isDebugEnabled()) {
      log.debug("Adding asb to runtime: " + assembly_id);
    }
    executeUpdate(dbp.getQuery("insertTrialAssembly", substitutions));
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
      if (log.isDebugEnabled()) {
	log.debug("CopyOplan: doing query " + queryName);
      }
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
   * <li>Clean up old CSMI & CSHNA assemblies</li>
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
    while (rs.next()) {
      assid = rs.getString(1);
      if (assid.startsWith(hnaType)) {
	// This is an HNA assembly ID
	if (hnaAssemblyId != null && ! assid.equals(hnaAssemblyId)) {
	  // And not the current one
	  // So remove it for this Trial and potentially all
	  if (log.isDebugEnabled()) {
	    log.debug("Removing old hna " + assid + " from config for trial " + trialId);
	  }
	  removeConfigAssembly(assid);
	  removeRuntimeAssembly(assid);
	  // This should find the asb not in use locally, so this is used anywhere test
	  if (! isAssemblyUsed(assid)) {
	    cleanAssembly(assid);
	  } else if (log.isDebugEnabled()) {
	    log.debug("fixAsb not deleting in use HNA asb " + assid);
	  }
	} else if (hnaAssemblyId != null) {
	  // This is the new HNA assembly somehow already there
	  if (log.isDebugEnabled()) {
	    log.debug("Found current HNA (" + hnaAssemblyId + ") already in config for trial " + trialId);
	  }
	  configHasNewHNA = true;
	}
      } else if (assid.startsWith(realcmtType)) {
	// This should be the real society definition
	if (cmtAssemblyId != null && ! assid.equals(cmtAssemblyId)) {
	  // an old CMT assembly still listed somehow. Delete it!
	  if (log.isDebugEnabled()) {
	    log.debug("Found unknown CMT assembly " + assid + " in config for trial " + trialId);
	  }
	  removeConfigAssembly(assid);
	  removeRuntimeAssembly(assid);
	  
	  // Completely, if necessary - this will delete the asb if no-one uses
	  // it. FIXME: Do we not want to delete CMT asbs?
	  if (! isAssemblyUsed(assid)) {
	    cleanAssembly(assid);	
	  } else if (log.isDebugEnabled()) {
	    log.debug("fixAsb not deleting used CMT " + assid);
	  }
	} else if (cmtAssemblyId != null) {
	  if (log.isDebugEnabled()) {
	    log.debug("Found current CMT cmtAssID (" + cmtAssemblyId + ") in config for trial " + trialId);
	  }
	  // This is the current CMT society def in the config area. Good.
	  // But if we also have a diff CSA assembly ID, we don't want
	  // this assembly in the runtime area, nor do we want
	  // a CSMI assembly
	  if (csaAssemblyId != null && ! csaAssemblyId.equals(cmtAssemblyId)) {
	    // Dont want the cmt assembly ID, this assembly, in the runtime - 
	    // using the CSA instead
	    if (log.isDebugEnabled()) {
	      log.debug("But also have a CSA (" + csaAssemblyId + "), so this CMT better not be in runtime");
	    }
	    removeRuntimeAssembly(assid);
	    // Remove any CSMI as well...
	    if (csmiAssemblyId != null) {
	      if (log.isDebugEnabled()) {
		log.debug("Since have a CSA we also know we don't want a CSMI in runtime");
	      }
	      removeRuntimeAssembly(csmiAssemblyId);
	      removeConfigAssembly(csmiAssemblyId); // should be no-op
	      if (! isAssemblyUsed(csmiAssemblyId)) {
		cleanAssembly(csmiAssemblyId);
	      } else if (log.isDebugEnabled()) {
		log.debug("fixAsb not removing used CSMI " + csmiAssemblyId);
	      }
	      csmiAssemblyId = null;
	    }
	  }   
	} else {
	  // Null cmtAssemblyId!!!!
	  if (log.isWarnEnabled()) {
	    log.warn("Trial " + trialId + " has null cmtAssemblyId");
	  }
	}
      } else if (assid.startsWith(csaType)) {
	// This could be the cmtAssemblyId 
	// and if so, fine.
	if (cmtAssemblyId != null && ! assid.equals(cmtAssemblyId)) {
	  if (log.isDebugEnabled()) {
	    log.debug("Got a CSA asb in config (" + assid + ") thats not the society assembly (" + cmtAssemblyId + ") for trial " + trialId);
	  }
	  // A CSA in configtime that isnt the society definition
	  removeConfigAssembly(assid);
	  
	  // Completely, if necessary. Note this wont delete the CSA
	  // from this Trias runtime, which is probably good.
	  // First case of this?
	  if (! isAssemblyUsed(assid)) {
	    cleanAssembly(assid);	
	  } else if (log.isDebugEnabled()) {
	    log.debug("fixAsb not deleting used CSA " + assid);
	  }
	} else if (cmtAssemblyId == null) {
	  // null society def!!!
	  if (log.isWarnEnabled()) {
	    log.warn("Null cmtAssemblyID for trial " + trialId);
	  }
	} else {
	  // This CSA assembly is the society def in config time. Good.
	  if (log.isDebugEnabled()) {
	    log.debug("Found the society def CSA (" + assid + ") in config for trial " + trialId);
	  }
	  // Note that we now know a CMT asb in runtime would be bad
	  // But we could still have a CSMI in runtime
	}
      } else {
	// What kind of assembly is this? CSMI? Delete it!
	if (log.isDebugEnabled()) {
	  log.debug("Found non CSA/CSHNA/CMT assembly (" + assid + ") in config for trial " + trialId);
	}
	removeConfigAssembly(assid);
	
	// Completely, if necessary. This, correctly, will leave a CSMI
	// in runtime if used by this trial
	if (! isAssemblyUsed(assid)) {
	  cleanAssembly(assid);	    
	} else if (log.isDebugEnabled()) {
	  log.debug("fixAsb not removing used CSMI found in config " + assid);
	}
      }
    } // end of loop over config assemblies
    rs.close();
    
    // 2: copy into config-time ref to runtime HNA if not already there
    if (! configHasNewHNA) {
      if (log.isDebugEnabled()) {
	log.debug("Adding HNA (" + hnaAssemblyId + ") to config for trial " + trialId);
      }
      addAssemblyToConfig(hnaAssemblyId);
    }
    
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
    rs = executeQuery(stmt, dbp.getQuery("queryTrialAssemblies", substitutions));
    assid = null;

    // Loop overl all assemblies in runtime for this assembly
    while (rs.next()) {
      assid = rs.getString(1);
      if (assid.startsWith(hnaType)) {
	// This is an HNA assembly ID
	if (! assid.equals(hnaAssemblyId)) {
	  if (log.isDebugEnabled()) {
	    log.debug("Found old HNA (" + assid + ") in runtime for trial " + trialId);
	  }
	  // And not the current one
	  // So remove it for this Trial and potentially all
	  removeRuntimeAssembly(assid);
	  
	  // While we're here, remove it from config-time as well
	  removeConfigAssembly(assid);
	  
	  // This should now be able to completely remove this asb
	  if (! isAssemblyUsed(assid)) {
	    cleanAssembly(assid);
	  } else if (log.isDebugEnabled()) {
	    log.debug("fixAsb in run loop not removing used HNA " + assid);
	  }
	  
	  // But more importantly, this is really an error!
	} else {
	  // This is the new HNA assembly already there, as expected
	  if (log.isDebugEnabled()) {
	    log.debug("Found current hna (" + assid + ") in run for trial " + trialId);
	  }
	}
      } else if (assid.startsWith(realcmtType)) {
	if (! assid.equals(cmtAssemblyId)) {
	  // an old CMT assembly still listed somehow. Delete it!
	  
	  if (log.isWarnEnabled()) {
	    log.warn("Found a CMT (" + assid + ") in runtime thats not the current society def for trial " + trialId);
	  }
	  removeRuntimeAssembly(assid);

	  // While we're here, remove it from config as well
	  removeConfigAssembly(assid);
	  
	  // Completely, if necessary
	  if (! isAssemblyUsed(assid)) {
	    cleanAssembly(assid);	
	  } else if (log.isDebugEnabled()) {
	    log.debug("fixasb in run loop not removing used bogus CMT I found: " + assid);
	  }
	} else {
	  // This is the current CMT in the runtime area. Fine,
	  if (log.isDebugEnabled()) {
	    log.debug("Found current CMT (" + assid + ") in runtime for trial " + trialId);
	  }
	  if (csaAssemblyId != null) {
	    // Have a CSA. That's odd.
	    // if this is in the runtime, should remove the CMT from the runtime.
	    if (assemblyInRuntime(csaAssemblyId)) {
	      if (log.isDebugEnabled()) {
		log.debug("Found CMT cmtAssId (" + assid + ") in runtime. But also have a CSA (" + csaAssemblyId + ") in runtime, for trial " + trialId + ". Remove the CMT from the runtime.");
	      }
	      removeRuntimeAssembly(assid);
	      
	      // Also should remove any CSMI in runtime
	      // FIXME!!!
	      if (csmiAssemblyId != null) {
		if (log.isDebugEnabled()) {
		  log.debug("fixAsb run loop found CSA (" + csaAssemblyId + ") in runtime thats not the society def, but also have a CSMI defined (" + csmiAssemblyId + "), which I'll remove");
		}
		removeRuntimeAssembly(csmiAssemblyId);
		removeConfigAssembly(csmiAssemblyId); // no-op probably
		if (! isAssemblyUsed(csmiAssemblyId)) {
		  cleanAssembly(csmiAssemblyId);
		} else if (log.isDebugEnabled()) {
		  log.debug("fixAsb run not removing bogus used CSMI " + csmiAssemblyId);
		}
		csmiAssemblyId = null;
	      }
	    } else {
	      // if this is not in the runtime, what is it?
	      // Do I add it?
	      if (log.isWarnEnabled()) {
		log.warn("Have CMT cmtAssID (" + cmtAssemblyId + ") in runtime, but also have a CSA which is not in runtime. What is this CSA? Should I remove the CMT and add the CSA? Trial: " + trialId);
	      }
	    }
	  } // end of loop where found CMT in runtime but have a CSA
	  else {
	    // csaAssemblyId is null. So would expect to have a CSMI
	    // assembly, and no CSA assemblies in the DB
	  }
	  
	  // OK: Found curent society def CMT in runtime
	  // and better be same CMT in config
	  if (! assemblyInConfig(assid)) {
	    // Otherwise, an error
	    if (log.isWarnEnabled()) {
	      log.warn("Found current CMT cmtAssID (" + cmtAssemblyId + ") in runtime but not in config, trial : " + trialId);
	    }
	    addAssemblyToConfig(assid);
	  }
	  
	  // Is there more than one CMT now in config? If so, get rid of the one
	  // that is not cmtAssemblyId
	  
	  // FIXME!!
	} // end of block dealing with finding the CMT cmtAssemblyId in runtime
      } else if (assid.startsWith(csaType)) {
	if (assid.equals(cmtAssemblyId)) {
	  // Found the CSA society def in runtime
	  // It better also be in config time
	  if (! assemblyInConfig(cmtAssemblyId)) {
	    if (log.isInfoEnabled()) {
	      log.info("Found CSA cmtAssemblyId (" + cmtAssemblyId + ") in runtime but not in config, adding it, trial: " + trialId);
	    }
	    addAssemblyToConfig(assid);
	  }
	  // and we'd better have only one CSA in runtime
	  // FIXME!!!
	  if (csaAssemblyId != null && ! csaAssemblyId.equals(cmtAssemblyId)) {
	    // But have another CSA assembly than this
	    // which is probably the real runtime def
	    if (log.isDebugEnabled()) {
	      log.debug("fixAsb run found soc def CSA (" + assid + ") in runtime, but have other CSA in local var (" + csaAssemblyId);
	    }
	    if (assemblyInRuntime(csaAssemblyId)) {
	      if (log.isDebugEnabled()) {
		log.debug("fixAsb run found that other CSA also in runtime. Remove orig (" + assid + ") from runtime");
	      }

	      // I guess I need to remove the societyDef CSA asb
	      // from runtime
	      removeRuntimeAssembly(assid);
	    } else {
	      // Bogus CSA ID in my local var?
	      // or somehow didn't save CSA ID in runtime
	      // and remove old society def CSA?

	      // FIXME!!!
	      if (log.isDebugEnabled()) {
		log.debug("fixAsb run says 2nd CSA in var not in runtime, while 1st is. Is local var wrong? Or does runtime have the wrong CSA in runtime?");
	      }
	    }
	  } // end of block checking for 2nd CSA asb
	} else if (! assid.equals(csaAssemblyId)) {
	  // Found CSA in runtime that is neither the base soc def or
	  // separate CSA.
	  // an old CSA assembly still listed somehow 
	  
	  // it is wrong, remove it.
	  removeRuntimeAssembly(assid);
	  removeConfigAssembly(assid); // should be a no-op
	  
	  // Completely, if necessary
	  if (! isAssemblyUsed(assid)) {
	    cleanAssembly(assid);	
	  } else if (log.isDebugEnabled()) {
	    log.debug("fixAsb run not removing unknown CSA in use " + assid);
	  }
	} else {
	  // This is the current non original society-def CSA in the runtime area.
	  // It better not be in config
	  if (assemblyInConfig(assid)) {
	    if (log.isWarnEnabled()) {
	      log.warn("Found non society-def CSA (" + csaAssemblyId + ") in config, must remove, trial: " + trialId);
	    }
	    removeConfigAssembly(assid);
	  }
	  // Better not be a CMT in runtime
	  if (assemblyInRuntime(cmtAssemblyId)) {
	    if (log.isWarnEnabled()) {
	      log.warn("Have non-orig soc CSA (" + csaAssemblyId + ") in runtime, but also orig CMT/CSA (" + cmtAssemblyId + "), which is wrong. Remove it: trial " + trialId);
	    }
	    removeRuntimeAssembly(cmtAssemblyId);
	  }
	  
	  // or a CSMI in runtime
	  if (csmiAssemblyId != null && assemblyInRuntime(csmiAssemblyId)) {
	    if (log.isWarnEnabled()) {
	      log.warn("Have non-orig soc CSA (" + csaAssemblyId + ") in runtime, but also a CSMI (" + csmiAssemblyId + "), which is wrong. Remove it: trial " + trialId);
	    }
	    removeRuntimeAssembly(csmiAssemblyId);
	    removeConfigAssembly(csmiAssemblyId); // should be a no-op
	    // completely, if necc.
	    if (! isAssemblyUsed(csmiAssemblyId)) {
	      cleanAssembly(csmiAssemblyId);
	    } else if (log.isDebugEnabled()) {
	      log.debug("fixAsb run removed bogus CSMI, but not completely: " + csmiAssemblyId);
	    }
	    csmiAssemblyId = null;
	  } // end of block dealing with bogus CSMI
	} // end of block handling finding non-orig soc CSA in runtime
	// Should be done with finding a CSA type assembly altogether
      } else if (assid.startsWith(realcsmiType)) {
	if (! assid.equals(csmiAssemblyId)) {
	  if (log.isWarnEnabled()) {
	    log.warn("Got unknown csmi (" + assid + ") in runtime for trial " + trialId);
	  }
	  // not an expected CSMI. delete it
	  removeRuntimeAssembly(assid);
	  removeConfigAssembly(assid); // should be a no-op
	  
	  // Completely, if necessary
	  if (! isAssemblyUsed(assid)) {
	    cleanAssembly(assid);	
	  } else if (log.isDebugEnabled()) {
	    log.debug("fixAsb run didn't completely delete used but unknown CSMI " + assid);
	  }
	} else if (csmiAssemblyId != null) {
	  // Got our CSMI. Make sure have either CMT or CSA as well, and if CSA,
	  // it is also in config time
	  if (log.isDebugEnabled()) {
	    log.debug("Found our csmi (" + csmiAssemblyId + ") in runtime for trial " + trialId);
	  }
	  
	  // FIXME: Check on CMT/CSA, etc
	  if (csaAssemblyId != null) {
	    if (csaAssemblyId.equals(cmtAssemblyId)) {
	      // Fine. it should be in both runtime & config time
	      if (! assemblyInConfig(csaAssemblyId)) {
		if (log.isDebugEnabled()) {
		  log.debug("fixAsb run found our CSMI, and our single CSA (" + csaAssemblyId + ") was not in config. Adding it");
		}
		addAssemblyToConfig(csaAssemblyId);
	      }
	      if (! assemblyInRuntime(csaAssemblyId)) {
		if (log.isDebugEnabled()) {
		  log.debug("fixAsb run found our CSMI, and our single CSA (" + csaAssemblyId + ") was not in runtime. Adding it");
		}
		addAssemblyToRuntime(csaAssemblyId);
	      }
	    } else {
	      // Error. This suggests we had a removal / mod. Probably want
	      // to delete the CSMI altoghether
	      if (log.isInfoEnabled()) {
		log.info("fixAsb run found our CSMI, but have 2 CSAs. I'll assume we had a mod and want to remove the CSMI");
	      }
	      removeRuntimeAssembly(assid);
	      removeConfigAssembly(assid); // should be a no-op
	      if (! isAssemblyUsed(assid)) {
		cleanAssembly(assid);
	      } else if (log.isDebugEnabled()) {
		log.debug("fixAsb run didnt completely remove rogue CSMI " + assid);
	      }
	      csmiAssemblyId = null;
	    } // end of bogus CSMI block
	  } else {
	    // OK. Have no CSA, just this cmt. It should be in runtime & config
	    if (! assemblyInConfig(cmtAssemblyId)) {
	      if (log.isDebugEnabled()) {
		log.debug("fixAsb run found our CSMI, and our single CSA/CMT (" + cmtAssemblyId + ") was not in config. Adding it");
	      }
	      addAssemblyToConfig(cmtAssemblyId);
	    }
	    if (! assemblyInRuntime(cmtAssemblyId)) {
	      if (log.isDebugEnabled()) {
		log.debug("fixAsb run found our CSMI, and our single CSA/CMT (" + cmtAssemblyId + ") was not in runtime. Adding it");
	      }
	      addAssemblyToRuntime(cmtAssemblyId);
	    }
	  } // end of block dealing with having just cmtAsbID & csmiAsbId
	} else {
	  // csmiAssemblyId is null
	  // I'd expect to have cmt != csa
	  if (csaAssemblyId == null || csaAssemblyId.equals(cmtAssemblyId)) {
	    // Just have one society def, no separate assembly
	    // for recipe add/remove/modify. That's odd!
	    if (log.isDebugEnabled()) {
	      log.debug("fixAsb run says we have no CSMI, but have just one soc assembly: " + cmtAssemblyId);
	    }
	  }
	}
      } else {
	// What kind of assembly is this. Delete it!
	if (log.isInfoEnabled()) {
	  log.info("Found unknown assembly (" + assid + ") in runtime for trial " + trialId);
	}
	removeRuntimeAssembly(assid);
	removeConfigAssembly(assid);
	
	// Completely, if necessary
	if (! isAssemblyUsed(assid)) {
	  cleanAssembly(assid);	    
	} else if (log.isDebugEnabled()) {
	  log.debug("fixAsb run found unknown asb type thats in use: " + assid);
	}
      } // end of block for unknown soc type
    } // end of loop over runtime assemblies
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


    // Do some cleanup: Use this opportunity to remove any orphaned
    // assemblies
    removeOrphanNonSocietyAssemblies();

    return true;
  } // end of fixAssemblies

  /**
   * Add the given List of recipes to the experiment in order.
   *
   * @param recipes a <code>List</code> of recipes in the experiment
   * @exception SQLException if an error occurs
   * @exception IOException if an error occurs
   */
  public void setModRecipes(List recipes) throws SQLException, IOException {
    //        dbp.setDebug(true);

    // FIXME: If this is a set, not an add, I should make sure there aren't already
    // any recipes in there
    // Delete all recipes out of this Trial - why would I do this?
    // Cause I re-add them when I finish saving the experiment
    //executeUpdate(dbp.getQuery("cleanTrialRecipe", substitutions));

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
    // FIXME: Test that the recipe isnt already there.
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
    if (log.isDebugEnabled()) {
      log.debug("populateHNA saving into " + hnaAssemblyId);
    }

    // Maybe wrap this in a try/catch to note unexpected modifications,
    // and when I get one, remove any old HNA and try again?
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
    if (log.isDebugEnabled()) {
      log.debug("populateCSMI saving into " + csmiAssemblyId);
    }
    return populate(data, 1f, csmiAssemblyId);
  }

  /**
   * Populate the CSA assembly for a particular item. 
   * Used to save a society originally. Also used when noticied
   * a modification/removal, when saving an experiment. In this case,
   * create a new CSA assembly, briefly pretend its the cmtAssemblyId
   * for use elsewhere. Children are
   * populated recursively. Agent components get additional
   * processing related to the organization they represent.
   *
   * @param data the ComponentData of the starting point
   * @return true if doing this population added a component, argument, or agent
   **/
  public boolean populateCSA(ComponentData data)
    throws SQLException  {
    // For storing the old cmtAssid if we have to change it
    String oldid = null;

    // If this is saving an Experiment, we create a CSA assembly:
    if (exptId != null && trialId != null) {
      // reset the flag noticing that something was modified while saving
      componentWasRemoved = false;

      // Create a new CSA based on what was in cmtAssemblyID
      csaAssemblyId = createCSAAssembly(cmtAssemblyId, getAssemblyDesc(cmtAssemblyId));
      
      // Delete any existing CSA / CMT in runtime for this experiment, completely if necc
      cleanOldRuntimeSocietyAssemblies();

      // Must also delete any CSMI assembly that exists so far. We won't be using one.
      if (csmiAssemblyId != null) {
	removeRuntimeAssembly(csmiAssemblyId);
	if (! isAssemblyUsed(csmiAssemblyId))
	  cleanAssembly(csmiAssemblyId);

	// FIXME: Do I need to create a new one?
	csmiAssemblyId = null;
      }

      // FIXME: What if a recipe modified the Agents in use? Must I
      // remove the CSHNA assembly as well?
      
      // Add this new assembly to the runtime assemblies for this experiment
      substitutions.put(":assembly_type:", csaType);
      addAssemblyToRuntime(csaAssemblyId);

      // For purposes of populate, this is the society definition
      cmtAssemblyId = csaAssemblyId;

      setAssemblyMatch();
    } // End of case where this is an Experiment

    if (log.isDebugEnabled()) {
      log.debug("populateCSA saving into " + cmtAssemblyId);
    }

    // 
    componentArgs.clear();

    //Save the full given tree into a CSA assembly, which should already
    // exist. Someone else must put the entry for this assembly
    // in the runtime or config time places, as necessary
    boolean result = populate(data, 1f, cmtAssemblyId);

    // Restore the original cmtAssid if necc, for use by fixAssemblies
    if (oldid != null)
      cmtAssemblyId = oldid;

    return result;
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
    // FIXME: Careful! If this is applied with a real CMT assembly,
    // the newly written version of cleanTrial will delete stuff from the 
    // OPLAN tables, which can't be restored!!!

    // I believe this is only used by TestPopulateDb, which does just
    // about nothing anyhow.

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
	String cchq = dbp.getQuery("checkComponentHierarchy", substitutions);
	if (log.isDebugEnabled()) {
	  log.debug("populate for assembly " + assemblyId + " and alib_id " + id + ": Doing query: " + cchq);
	}
	rs = executeQuery(stmt, cchq);
	// If not, add it
	if (!rs.next()) {
	  executeUpdate(dbp.getQuery("insertComponentHierarchy",
				     substitutions));
	  // if it is an agent
	  if (data.getType().equals(ComponentData.AGENT)) {
	    // Added this Agent. Add it to the list
	    // WARNING: This just means the agent
	    // was not previously in the hierarchy.
	    // It does not mean that the Agent itself
	    // was not already in the CMT assembly, for example
	    // It doesn't really matter: 
	    // all the insertions in populateAgent check
	    // for themselves before doing inserts
	    if (log.isDebugEnabled()) {
	      log.debug("Found new contained agent for this hierarchy");
	    }
	    addedAgents.add(id);
	  }
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
    
    // For debugging, dump out the old & new arguments now:


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
      if (log.isInfoEnabled()) {
	log.info("Attempt to remove "	 + (-excess)
					 + " args from "
					 + data);
      }
      // Throw something I'll recognize
      // Catch it in the Experiment, so I know to call populateCSA
      // FIXME!!!
      componentWasRemoved = true;
      throw new IllegalArgumentException("Attempt to remove "
					 + (-excess)
					 + " args from "
					 + data);
    } else {
      if (log.isInfoEnabled()) {
	log.info("Count new args minus old is: " + excess);
      }
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
      if (log.isInfoEnabled()) {
	log.info("Comp args for " + data.getName() + ". New " + newArg.toString() + ", Old: " + oldArg.toString());
	// print out the number, the values....
      }
		 
      while (!newArg.argument.equals(oldArg.argument)) {
	if(log.isWarnEnabled()) {
	  log.warn("newArg != oldArg, new: " +newArg.toString() + " old: " + oldArg.toString());
	}
	// Assume arguments were inserted
	excess--;
	if (excess < 0) {
	  if (log.isInfoEnabled()) {
	    log.info("Component args cannot be modified or removed: " + data);
	  }
	  // FIXME: Throw something recognizable that the experiment
	  // can catch and call populateCSA
	  componentWasRemoved = true;
	  throw new IllegalArgumentException("Component args cannot be modified or removed: " + data);
	}
	if (Float.isNaN(prev)) prev = oldArg.order - 1f;
	argsToInsert.add(new Argument(newArg.argument, (prev + oldArg.order) * 0.5f));
	newArg = (Argument) newIter.next();
      } // end of loop while argument values differ
      prev = oldArg.order;
    } // loop over old arguments

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
      if (log.isDebugEnabled()) {
	log.debug("Adding new argument to " + data.getName() + ": " + arg.argument);
      }
      substitutions.put(":argument_value:", sqlQuote(arg.argument));
      substitutions.put(":argument_order:", sqlQuote(String.valueOf(arg.order)));
      executeUpdate(dbp.getQuery("insertComponentArg", substitutions));
      oldArgs.add(arg);
      result = true;
    }

    // For agents, only keep going if this isn't the HNA assembly
    if (data.getType().equals(ComponentData.AGENT)) {
      if (!assemblyId.equals(hnaAssemblyId)) {
	// was this agent added to the hierarchy
	// since we saved its AgentData
	// Note however that this means that recipes
	// will have trouble adding relationships!!!
	// If the checks in this method are good,
	// I should get rid of this whole addedAgents thing
	// FIXME!!!!!!!!
	if (addedAgents.contains(id)) {
	  if (log.isDebugEnabled()) {
	    log.debug("Calling populateAgent for agent alib_id: " + id + " in assembly " + assemblyId);
	  }
	  populateAgent(data, true);
	  addedAgents.remove(id);
	  result = true;
	}
      } else {
	// If this is an Agent and we're in the CSHNA assembly,
	// don't save the plugins, etc now. Just return.
	return result;
      }
    } else {
      if (log.isDebugEnabled()) {
	log.debug("Not an agent: " + data.getName() + ", its a " + data.getType());
      }
    }
     

    ComponentData[] children = data.getChildren();

    // The following assumes that the insertion order of old
    // children equals their index in the array and that the
    // insertion order of all new children should be the their
    // index in the array as well.
    for (int i = 0, n = children.length; i < n; i++) {
      if (log.isDebugEnabled()) {
	log.debug("Recursing into populate from " + data.getName() + " for " + children[i].getName());
      }
      result |= populate(children[i], i, assemblyId);
    }
    return result;
  }

  /**
   * Special processing for an agent component because agents
   * represent organizations having relationships and property groups.
   * The substitutions Map already has most of the needed substitutions
   **/
  private void populateAgent(ComponentData data, boolean isAdded) throws SQLException {
      
    if(log.isDebugEnabled()) {
      log.debug("populating Agent: " + data.getName());
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
	if (log.isDebugEnabled()) {
	  log.debug("pAgent: insertingAgentOrg ag: " + data.getName() + ", orgtype: " + assetData.getAssetClass());
	}
	executeUpdate(dbp.getQuery("insertAgentOrg", substitutions));
      }
      rs.close();
      rs = executeQuery(stmt, dbp.getQuery("checkAsbAgent", substitutions));
      if (!rs.next()) {
	if (log.isDebugEnabled()) {
	  log.debug("pAgent " + data.getName() + " inserting asb_agent under assembly " + substitutions.get(":assembly_id:"));
	}
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


	      /////////////////
	      // FIXME: CMT assemblies have all multi-valed PGAttr use attribute_order
	      // of 0, while we give them a real order.
	      // As a result, we end up adding extra, for example, Roles



	      substitutions.put(":attribute_value:", sqlQuote(values[k]));
	      substitutions.put(":attribute_order:", String.valueOf(k));
	      // if val not already there for this runtime
	      ResultSet rschck = executeQuery(stmt, dbp.getQuery("checkAttribute", substitutions));
	      if (! rschck.next()) {

		// FIXME: Add a query: Get # of entries for this PG/slot
		// with attribute_order of 0. If its > 1, then,
		// maybe doesnt a checkAttribute variant that doesn't
		// check order. IF it matches, don't do an insert

		if (log.isDebugEnabled()) {
		  log.debug("pagent " + data.getName() + " inserting pgval under pgattid " + propInfo.getAttributeLibId() + ", val " + values[k] + " into assembly " + substitutions.get(":assembly_id:"));
		}
		executeUpdate(dbp.getQuery("insertAttribute", substitutions));
	      }
	      rschck.close();
	    }
	  } else {

	    if (prop.isListType())
	      throw new RuntimeException("Property is not a single value: "
					 + propInfo.toString());
	    substitutions.put(":attribute_value:", sqlQuote(prop.getValue().toString()));
	    substitutions.put(":attribute_order:", "0");
	      // if val not already there for this runtime
	      // if val not already there for this runtime
	      ResultSet rschck = executeQuery(stmt, dbp.getQuery("checkAttribute", substitutions));
	      if (! rschck.next()) {
		if (log.isDebugEnabled()) {
		  log.debug("pagent " + data.getName() + " inserting pgval under pgattid " + propInfo.getAttributeLibId() + ", val " + prop.getValue().toString() + " into assembly " + substitutions.get(":assembly_id:"));
		}
		executeUpdate(dbp.getQuery("insertAttribute", substitutions));
	      }
	      rschck.close();
	  }
	} // loop over properties in pg
      } // loop over PGs
    }

    // Add relationships even to Agents that
    // aren't new here
    RelationshipData[] relationships = assetData.getRelationshipData();
    //        dbp.setDebug(true);
    for (int i = 0; i < relationships.length; i++) {
      RelationshipData r = relationships[i];
      long startTime = r.getStartTime();
      long endTime = r.getEndTime();

      // Role is empty if its really Subordinate, in which
      // case interesting stuff is in the Type
      // FIXME: Or maybe it should be the literal?
      substitutions.put(":role:", sqlQuote((r.getRole() == null || r.getRole().equals("")) ? r.getType() : r.getRole()));
      substitutions.put(":supporting:", sqlQuote(getComponentAlibId(data)));
      substitutions.put(":supported:", sqlQuote(getAgentAlibId(r.getSupported())));

      // These appear to be magic with the preparedStatement below....
      substitutions.put(":start_date:", "?");
      substitutions.put(":end_date:", "?");
      
      String query = dbp.getQuery("checkRelationship", substitutions);
      PreparedStatement pstmt = dbConnection.prepareStatement(query);
      pstmt.setTimestamp(1, new Timestamp(startTime));
      ResultSet rs = executeQuery(pstmt, query);
      if (!rs.next()) {
	if (log.isDebugEnabled()) {
	  log.debug("pagent " + data.getName() + " inserting rel " + r.getRole() + " with " + getAgentAlibId(r.getSupported()) + " into assembly " + substitutions.get(":assembly_id:"));
	}
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

  /**
   * Fill in the lib info for a PG name/slot. Only used if getPropertyInfo 
   * got nothing.
   *
   * @param pgName a <code>String</code> PG name
   * @param propName a <code>String</code> property in PG name
   * @param propType a <code>String</code> type of value of property
   * @param propSubtype a <code>String</code> sub-type of value, often null
   * @exception SQLException if an error occurs
   */
  private void setPropertyInfo(String pgName, String propName,
                               String propType, String propSubtype) throws SQLException {
    if(log.isDebugEnabled()) {
      log.debug("setPropertyInfo("+pgName+", "+propName+", "+
                propType+", "+propSubtype+")");
    }

    Statement stmt = dbConnection.createStatement();
    substitutions.put(":attribute_lib_id:", sqlQuote(pgName + "|" + propName));
    substitutions.put(":pg_name:", sqlQuote(pgName));
    substitutions.put(":attribute_name:", sqlQuote(propName));
    // FIXME:  Below is a hack, as an attempt to run past this point.
    // Really should figure out why propSubtype is sometimes null.
    if(propSubtype == null && log.isWarnEnabled()) {
      log.warn("PropSubtype is NULL for: " + propName + "(type)" + propType);
    }

    if(propType.equalsIgnoreCase("Collection")) {
      substitutions.put(":aggregate_type:", sqlQuote("COLLECTION"));
      substitutions.put(":attribute_type:", 
                        sqlQuote((propSubtype == null) ? "String" : propSubtype));
    } else {
      substitutions.put(":aggregate_type:", sqlQuote("SINGLE"));
      substitutions.put(":attribute_type:", sqlQuote(propType)); 
    }

    if (log.isDebugEnabled()) {
      log.debug("attribute_type: " + substitutions.get(":attribute_type:"));
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

  ///////////////////////////////////////////////////
  // Basic DB methods

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

  /**
   * Use the conflict handler to decide what to do when the DB
   * conflicts with current data. Used mostly for lib entries.
   *
   * @param msg an <code>Object</code> message to give the user
   * @return a <code>boolean</code> value
   */
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

  /**
   * Compute difference between DB (using ResultSet columns in order)
   * against the values in the substitutions (must be preset). Return StringBuffer
   * describing differences, null if none.
   *
   * @param rs a <code>ResultSet</code> from the DB as baselines
   * @param keys a <code>String[]</code> set of substitution keys to compare against
   * @return a <code>StringBuffer</code> message describing differences, null if none
   * @exception SQLException if an error occurs
   */
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

  /////////////////////////////////////////////////
  // Methods to deal with determining Insertion Points, LibIds, AlibIds,
  // and otherwise deal with ComponentData


  /**
   * Make sure the DB has an alib entry for the current component. Use ConflictHandler
   * if DB has an entry already that differs. This also makes sure the lib table
   * is appropriately filled in.
   *
   * @exception SQLException if an error occurs
   */
  private void insureAlib() throws SQLException {
    String id = (String) substitutions.get(":component_alib_id:");
    if (alibComponents.contains(id)) return; // Already present
    // may need to be added
    insureLib();
    String query1 = dbp.getQuery("checkAlibComponent", substitutions);
    if (log.isDebugEnabled()) {
      log.debug("insureAlib doing query " + query1);
    }
    ResultSet rs = executeQuery(stmt, query1);
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

  /**
   * Make sure the lib data for a component is in the DB.
   * Note the user will be prompted if there is already a definition
   * which differs.
   *
   * @exception SQLException if an error occurs
   */
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

  /**
   * Used in populate as we begin handling a component to make sure
   * all the appropriate substitutions are filled in.
   *
   * @param data a <code>ComponentData</code> to use to set the substitutions
   */
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
   * Get the component lib id of the underlying lib component for
   * the component described by the specified ComponentData. Each
   * type of component has a different convention for constructing
   * its lib id.
   **/
  private String getComponentLibId(ComponentData data) {
    if (data == null) return sqlQuote(null);
    String componentType = data.getType();
    if (componentType.equals(ComponentData.PLUGIN)) {
      return sqlQuote(componentType + "|" + data.getClassName());
    }
    if (componentType.equals(ComponentData.NODEBINDER)) {
      return sqlQuote(componentType + "|" + data.getClassName());
    }
    if (componentType.equals(ComponentData.AGENTBINDER)) {
      return sqlQuote(componentType + "|" + data.getClassName());
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
      return sqlQuote(componentType + "|" + societyName);
    }
    ComponentData parent = data.getParent();
    return sqlQuote(componentType + "|" + getFullName(data));
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
    // FIXME: Node Agent? ENCLAVE? Random others?
    return sqlQuote(componentType);
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
	  if (data.getName().startsWith(agentName + "|")) {
	    result = data.getName();
	  } else {
	    result = agentName + "|" + data.getName();
	  }
	}
      } else if (componentType.equals(ComponentData.NODEBINDER)) {
	ComponentData anc = findAncestorOfType(data, ComponentData.NODE);
	if (anc == null) {
	  return data.getName();
	} else {
	  String nodeName = anc.getName();
	  if (data.getName().startsWith(nodeName + "|")) {
	    result = data.getName();
	  } else {
	    result = nodeName + "|" + data.getName();
	  }
	  //result = nodeName + "|" + data.getClassName();
	}
      } else if (componentType.equals(ComponentData.AGENTBINDER)) {
	ComponentData anc = findAncestorOfType(data, ComponentData.AGENT);
	if (anc == null) {
	  return data.getName();
	} else {
	  String agentName = anc.getName();
	  if (data.getName().startsWith(agentName + "|")) {
	    result = data.getName();
	  } else {
	    result = agentName + "|" + data.getName();
	  }
	  //result = agentName + "|" + data.getClassName();
	}
      } else if (componentType.equals(ComponentData.SOCIETY)) {
	result = ComponentData.SOCIETY + "|" + data.getName();
      } else if (componentType.equals(ComponentData.AGENT)) {
	result = getAgentAlibId(data.getName());
      } else {
	// This says that unexpected componentTypes
	// have AlibIDs that are their name. Is this right?
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
    return "" + agentName;
  }

  /**
   * Get the component clone set id for the component described
   * by the specified ComponentData.
   **/
  private String getComponentCloneSetId(ComponentData data) {
    if (data == null) return sqlQuote(null);
    return sqlQuote("0");
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


  //////////////////////////////////////////
  // Inner classes follow

  /**
   * Arguments have a value and an order for easy comparison. Used in populate
   *
   */
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
} // end of PopulateDb

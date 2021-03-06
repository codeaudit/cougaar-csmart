/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.viewer;

import org.cougaar.core.agent.Agent;
import org.cougaar.tools.csmart.core.db.DBConflictHandler;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.experiment.DBExperiment;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.recipe.ComplexRecipeBase;
import org.cougaar.tools.csmart.recipe.RecipeBase;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.db.SocietyDBComponent;
import org.cougaar.tools.csmart.society.file.SocietyFileComponent;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Helper functions for manipulating objects in the Organizer
 */
public class OrganizerHelper {
  private static final String EXPT_DESC_QUERY = "queryExptDescriptions";
  private static final String EXPT_ASSEM_QUERY = "queryExperiment";
  private static final String EXPT_TRIAL_QUERY = "queryTrials";
  private static final String EXPT_NODE_QUERY = "queryNodes";
  private static final String EXPT_HOST_QUERY = "queryHosts";
  private static final String COMPONENT_ARGS_QUERY = "queryComponentArgs";

  private transient Logger log;

  private static final String CMT_START_STRING = "CMT";
  private static final String CSA_START_STRING = "CSA";

  private DBConflictHandler saveToDbConflictHandler =
    GUIUtils.createSaveToDbConflictHandler(null);

  private Organizer organizer;

  public OrganizerHelper(Organizer organizer) {
    this.organizer = organizer;
  }

  /**
   * Create an experiment.
   * @return Experiment the new experiment or null if any error
   */
  public DBExperiment createExperiment(String originalExperimentName,
                                     String experimentName,
                                     String experimentId,
                                     String trialId) {

    createLogger();

    // get assembly ids for trial
    ArrayList assemblyIds = getTrialAssemblyIds(experimentId, trialId);
    String assemblyMatch = DBUtils.getAssemblyMatch(assemblyIds);
    // get nodes for trial
    ArrayList nodes = getNodes(trialId, assemblyMatch);
    ArrayList hosts = getHosts(trialId, assemblyMatch);
    SocietyDBComponent soc = null;
    if (assemblyIds.size() != 0) {
      // Get the Society Assembly Id (CMT or CSA).
      String id = null;
      Iterator iter = assemblyIds.iterator();
      while(iter.hasNext()) {
        id = (String)iter.next();
        if(id.startsWith(CMT_START_STRING) || id.startsWith(CSA_START_STRING)) {
          break;
        }
      }

      if (id == null || (! id.startsWith(CMT_START_STRING) && ! id.startsWith(CSA_START_STRING))) {
	// PROBLEM! Not a society assembly!
	if (log.isErrorEnabled()) {
	  log.error("createExpt found no society Assembly for experiment " + experimentId);
	  if (id == null)
	    log.error("Found no assembly at all!");
	  else
	    log.error("Found only assembly " + id);
	}
	return null;
      }

      if(id != null) {
        String societyName = DBUtils.getSocietyName(id);
        SocietyComponent sc = organizer.getSociety(societyName);
        if (sc != null && (sc instanceof SocietyDBComponent)) {
	  if (log.isDebugEnabled()) {
	    log.debug("Found already loaded society " + societyName);
	  }
          soc = (SocietyDBComponent)sc;
        } else {
	  if (log.isDebugEnabled()) {
	    log.debug("Loading society " + societyName + " from id " + id);
	  }
          soc = new SocietyDBComponent(societyName, id);
          soc.initProperties();
        }
      }
    } else { // We need to create a new trial.
      // Need to have the experiment id, trial id, and multiplicity
      // for the call that will generate the assembly here.
      if(log.isWarnEnabled()) {
        log.warn("No assemblies for: " + experimentId + " " + trialId);
      }
      return null; // creating an experiment from scratch not implemented yet
    }

    DBExperiment experiment = new DBExperiment((String)experimentName,
                                           experimentId, trialId);

    // The following is ugly
    setDefaultNodeArguments(experiment, assemblyMatch,
			    PopulateDb.getSocietyAlibId(originalExperimentName));
    //    experiment.setCloned(isCloned);

    //to hold all potential agents
    List agents = new ArrayList();

    Map experimentNamesMap = ExperimentDB.getExperimentNames();
    String origExperimentId = (String)experimentNamesMap.get(originalExperimentName);
    String origTrialId = null;
    if (origExperimentId == null) {
      if (log.isErrorEnabled()) {
	log.error("Found no experiment ID for name " + originalExperimentName);
	log.error("Will have null trialID, and therefore will lose the recipes that were in this experiment!");
      }
    } else {
      origTrialId = ExperimentDB.getTrialId(origExperimentId);
    }

    // This gets all the recipes for the experiment from which this was created
    List defaultRecipes = checkForRecipes(origTrialId, origExperimentId);

//     if (log.isDebugEnabled()) {
//       log.debug("default recipes list from OLD exptID has " + defaultRecipes.size() + " items");
//       Iterator rI = defaultRecipes.iterator();
//       while (rI.hasNext()) {
// 	DbRecipe rn = (DbRecipe) rI.next();
// 	log.debug("default recipes list: " + rn.name);
//       }
//     }

    List recipes = checkForRecipes(trialId, experimentId);

//     if (log.isDebugEnabled()) {
//       log.debug("recipes list from this exptID has " + recipes.size() + " items");
//       Iterator rI = recipes.iterator();
//       while (rI.hasNext()) {
// 	DbRecipe rn = (DbRecipe) rI.next();
// 	log.debug("recipes list: " + rn.name);
//       }
//     }

    // FIXME: Maybe deal with CMT societies & DEFAULT comms
    // here??
    // And in general, must grab comm stuff from orig ID and resave it
    // under a new ID and attach it to the new Expt.
    // FIXME: Do something with community stuff here?
    // That is, find any comm info, stash it away in new ASB
    // ref'ed in Expt, so that save can then save it
    // in the correct assembly in the Expt.


    // maybe change this if to be:
    //    if (! recipes.contains(defaultRecipes)
    // except of course, looping over the items....

    // When creating an experiment from one of the base experiments
    // copy over the recipes that came with the base
    if (origExperimentId != null && ! origExperimentId.equals(experimentId)) {
      if (log.isDebugEnabled()) {
	log.debug("Combining old expts recipes with those from this and grabbing Comm info from orig expt");
      }
      recipes.addAll(defaultRecipes);


      // Now community stuff
      // FIXME: I think the old COMM ASB is in the config
      // table here already at this point, which I don't really want
      PopulateDb pdbc = null;
      String commAsb = null;
      try {
	pdbc = new PopulateDb(experimentId, trialId);
	commAsb = pdbc.getNewCommAsbFromExpt(origExperimentId, origTrialId);
	if (commAsb != null)
	  pdbc.setCommAssemblyId(commAsb);
      } catch (SQLException sqle) {
	log.error("createExperiment couldnt get new commAsb based on orig expt " + origExperimentId + " for new experiment " + experimentId, sqle);
      } catch (IOException ioe) {
	log.error("createExperiment couldnt get new commAsb based on orig expt " + origExperimentId + " for new experiment " + experimentId, ioe);
      } finally {
	try {
	  if (pdbc != null)
	    pdbc.close();
	} catch (SQLException se) {}
      }
      experiment.setCommAsbID(commAsb);
      if (log.isDebugEnabled()) {
	log.debug("createExpt got new comm asb based on old expt " + origExperimentId + " of " + commAsb);
      }
    } else if (origExperimentId != null && origExperimentId.equals(experimentId)) {
      if (log.isDebugEnabled()) {
	log.debug("createExpt had orig same as this. Try reading previous comm ASB from db");
      }

      // just loading a previous experiment from the DB.
      // get its comm ASB
      PopulateDb pdbc = null;
      String commAsb = null;
      try {
	pdbc = new PopulateDb(experimentId, trialId);
	commAsb = pdbc.getCommAsbForExpt(experimentId, trialId);
      } catch (SQLException sqle) {
	log.error("createExperiment couldnt get commAsb for expt " + experimentId, sqle);
      } catch (IOException ioe) {
	log.error("createExperiment couldnt get commAsb for expt " + experimentId, ioe);
      } finally {
	try {
	  if (pdbc != null)
	    pdbc.close();
	} catch (SQLException se) {}
      }
      experiment.setCommAsbID(commAsb);
      if (log.isDebugEnabled()) {
	log.debug("createExpt got comm asb for expt " + experimentId + " of " + commAsb);
      }
    } else if (origExperimentId == null) {
      if (log.isDebugEnabled()) {
	log.debug("createExpt got null orig ExptId when loading expt " + experimentId);
      }

      // just re-loading an experiment
      // get its comm ASB
      PopulateDb pdbc = null;
      String commAsb = null;
      try {
	pdbc = new PopulateDb(experimentId, trialId);
	commAsb = pdbc.getCommAsbForExpt(experimentId, trialId);
      } catch (SQLException sqle) {
	log.error("createExperiment couldnt get commAsb for expt " + experimentId, sqle);
      } catch (IOException ioe) {
	log.error("createExperiment couldnt get commAsb for expt " + experimentId, ioe);
      } finally {
	try {
	  if (pdbc != null)
	    pdbc.close();
	} catch (SQLException se) {}
      }
      experiment.setCommAsbID(commAsb);
      if (log.isDebugEnabled()) {
	log.debug("createExpt got comm asb for expt " + experimentId + " of " + commAsb);
      }
    }

    // If somehow still have no comm ASB, create a new one
    if (experiment.getCommAsbID() == null) {
      if (log.isInfoEnabled()) {
	log.info("createExpt somehow still no Comm ASB. Create a new one. OrigExptID: " + origExperimentId + ", exptID: " + experimentId);
      }
      PopulateDb pdbc = null;
      String commAsb = null;
      try {
	pdbc = new PopulateDb(experimentId, trialId);
	commAsb = pdbc.getNewCommAssembly();
	if (commAsb != null)
	  pdbc.setCommAssemblyId(commAsb);
      } catch (SQLException sqle) {
	log.error("createExperiment couldnt get new commAsb for expt " + experimentId, sqle);
      } catch (IOException ioe) {
	log.error("createExperiment couldnt get new commAsb for expt " + experimentId, ioe);
      } finally {
	try {
	  if (pdbc != null)
	    pdbc.close();
	} catch (SQLException se) {}
      }
      experiment.setCommAsbID(commAsb);
      if (log.isDebugEnabled()) {
	log.debug("createExpt got comm asb for expt " + experimentId + " of " + commAsb);
      }
    }

    if (recipes.size() != 0) {
      Iterator metIter = recipes.iterator();
      while (metIter.hasNext()) {
        DbRecipe dbRecipe = (DbRecipe) metIter.next();
	// If recipe is in organizer, use that
	// Else, create from db
	RecipeComponent mc = getDatabaseRecipe(dbRecipe);
	if (mc != null) {
	  AgentComponent[] recagents = mc.getAgents();
	  if (recagents != null && recagents.length > 0) {
	    agents.addAll(Arrays.asList(recagents));
	  }
	  if (log.isDebugEnabled()) {
	    log.debug("Adding recipe to experiment " + experiment.getExperimentName() + ": " + mc.getRecipeName());
	  }
	  experiment.addRecipeComponent(mc);
	}
      }
    }

    AgentComponent[] socagents = soc.getAgents();
    if (socagents!= null && socagents.length > 0) {
      for (int i = 0; i < socagents.length; i++) {
        AgentComponent ac = socagents[i];
        setComponentProperties(ac, PopulateDb.getAgentAlibId(ac.getShortName()), assemblyMatch);
      }
      agents.addAll(Arrays.asList(socagents));
    }
    AgentComponent[] allagents = (AgentComponent[])agents.toArray(new AgentComponent[agents.size()]);

    experiment.addSocietyComponent((SocietyComponent)soc);

    if (log.isDebugEnabled()) {
      log.debug("createExperiment about to do Agent/Node mapping");
    }
    // Retrieve the Agent/Node mapping. Add the Nodes to the Experiment
    // And set the properties for the Nodes.
    // Add all Nodes.
    addAgents(experiment, nodes, allagents, trialId, assemblyMatch);

    // Add all Hosts.
    Iterator hostIter = hosts.iterator();
    while (hostIter.hasNext()) {
      String hostName = (String) hostIter.next();
      HostComponent hc = experiment.addHost(hostName);
      setComponentProperties(hc, PopulateDb.getHostAlibId(hostName), assemblyMatch);
    }
    mapNodesToHosts(experiment, assemblyMatch);

    try {
      if (!ExperimentDB.isExperimentNameInDatabase(experimentName)) {
	if (log.isDebugEnabled()) {
	  log.debug("Saving expt " + experimentName + " not in DB to DB");
	}
	experiment.save(saveToDbConflictHandler);
      }
      else {
	// Add the recipes to the expt_trial_mod_recipe table
	List rcs = new ArrayList();
	for (int i = 0; i < experiment.getRecipeComponentCount(); i++) {
	  rcs.add(experiment.getRecipeComponent(i));
	}

	if (log.isDebugEnabled()) {
	  log.debug("Setting recipes on experiment " + experiment.getExperimentID() + " in DB");
	}

	// Save the recipes on the experiment
	// Note that the recipe are not re-saved here
	PopulateDb pdb = null;
	try {
	  pdb = new PopulateDb(experiment.getExperimentID(), experiment.getTrialID());
	  pdb.setAndCleanModRecipes(rcs);
	} catch (Exception e) {
	  if (log.isErrorEnabled()) {
	    log.error("Problem saving recipes on experiment " + experiment.getExperimentID(), e);
	  }
	} finally {
	  try {
	    pdb.close();
	  } catch (Exception e) {}
	}
      }
    } catch (RuntimeException e) {
      if(log.isErrorEnabled()) {
        log.error("RuntimeException saving experiment " + experimentName, e);
      }
    }
    experiment.resetModified(); // the experiment is NOT modified at this point
    return experiment;
  }

  private void createLogger() {
    if (log == null)
      log = CSMART.createLogger(this.getClass().getName());
  }

  /**
   * Get assembly ids for trial.
   */
  private ArrayList getTrialAssemblyIds(String experimentId, String trialId) {
    ArrayList assemblyIds = new ArrayList();
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Map substitutions = new HashMap();
        substitutions.put(":expt_id", experimentId);
        substitutions.put(":trial_id", trialId);
        Statement stmt = conn.createStatement();
        query = DBUtils.getQuery(EXPT_ASSEM_QUERY, substitutions);
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          String asmid = rs.getString(1);
          assemblyIds.add(asmid);
        }
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      if(log.isErrorEnabled()) {
        log.error("Caught SQL exception getting Trial pieces: " + query, se);
      }
    }

    return assemblyIds;
  }

  /**
   * Get nodes for a trial.
   */
  private ArrayList getNodes(String trialId, String assemblyMatch) {
    ArrayList nodes = new ArrayList();
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Map substitutions = new HashMap();
        substitutions.put(":trial_id", trialId);
        substitutions.put(":assemblyMatch", assemblyMatch);
        Statement stmt = conn.createStatement();
        query = DBUtils.getQuery(EXPT_NODE_QUERY, substitutions);
        if(log.isDebugEnabled()) {
          log.debug("Organizer: Nodes query: " + query);
        }
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          nodes.add(rs.getString(1));
        }
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      if(log.isErrorEnabled()) {
        log.error("Caught SQL exception getting Nodes " + query, se);
      }
    }
    return nodes;
  }

  private ArrayList getHosts(String trialId, String assemblyMatch) {
    ArrayList hosts = new ArrayList();
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Map substitutions = new HashMap();
        substitutions.put(":assemblyMatch", assemblyMatch);
        Statement stmt = conn.createStatement();
        query = DBUtils.getQuery(EXPT_HOST_QUERY, substitutions);
        if(log.isDebugEnabled()) {
	  log.debug("Organizer: Get hosts query: " + query);
        }
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          hosts.add(rs.getString(1));
        }
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      if(log.isErrorEnabled()) {
        log.error("Caught SQL exception getting hosts: " + query, se);
      }
    }

    return hosts;
  }

  private void setDefaultNodeArguments(Experiment experiment,
                                       String assemblyMatch,
                                       String socAlibId)
  {
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Statement stmt = conn.createStatement();
        Map substitutions = new HashMap();
        substitutions.put(":assemblyMatch", assemblyMatch);
        substitutions.put(":comp_alib_id", socAlibId);
        getComponentArguments(stmt, substitutions, experiment.getDefaultNodeArguments());
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      if(log.isErrorEnabled()) {
        log.error("Caught SQL exception setting NodeArgs", se);
      }
    }
  }


  private void mapNodesToHosts(Experiment experiment,
			       String assemblyMatch ) {

    // Note that getNodeC will do a node/agent reconcile,
    // and getHostC will do it again
    // As a result, loading an experiment from the DB, does a lot
    // of work trying to match Agents
    NodeComponent[] nodeComponents = experiment.getNodeComponents();
    HostComponent[] hostComponents = experiment.getHostComponentsNoReconcile();
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Map substitutions = new HashMap();
        substitutions.put(":assemblyMatch", assemblyMatch);
        Statement stmt = conn.createStatement();
        query = DBUtils.getQuery("queryHostNodes", substitutions);
        if(log.isDebugEnabled()) {
          log.debug("mapNodesToHosts: " + query);
        }
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          String hostName = rs.getString(1);
          String nodeName = rs.getString(2);
          for (int i=0; i < hostComponents.length; i++) {
            HostComponent hc = hostComponents[i];
            if (hc.getShortName().equals(hostName)) {
              for (int j=0; j < nodeComponents.length; j++) {
                NodeComponent nc = nodeComponents[j];
                if (nc.getShortName().equals(nodeName)) {
                  hc.addNode(nc);
                  break;
                }
              }
              break;
            }
          }
        }
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      if(log.isErrorEnabled()) {
        log.error("Caught SQL exception getting HN map " + query, se);
      }
    }
  }

  private void addAgents(Experiment experiment,
                         ArrayList nodes,
                         AgentComponent[] agents,
                         String trialId, String assemblyMatch) {
    Iterator iter = nodes.iterator();
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Map substitutions = new HashMap();
        substitutions.put(":trial_id", trialId);
        substitutions.put(":assemblyMatch", assemblyMatch);
        substitutions.put(":insertion_point", Agent.INSERTION_POINT);
        Statement stmt = conn.createStatement();
        NodeComponent nodeComponent = null;
        while(iter.hasNext()) {
          query = null;
          ResultSet rs;
          String nodeName = (String)iter.next();
          nodeComponent = experiment.addNode(nodeName);
// 	  if (log.isDebugEnabled()) {
// 	    log.debug("Adding node " + nodeName + " to experiment");
// 	  }
          setComponentProperties(nodeComponent, PopulateDb.getNodeAlibId(nodeName), assemblyMatch);
          substitutions.put(":parent_name", nodeName);
          substitutions.put(":comp_alib_id", PopulateDb.getNodeAlibId(nodeName));
          // Get args of the node
          Properties nodeProps = nodeComponent.getArguments();
          getComponentArguments(stmt, substitutions, nodeProps);
          // Query All Agents for each node.
          // Loop Query for every node.
          query = DBUtils.getQuery("queryComponents", substitutions);
          //         if(log.isDebugEnabled()) {
          //           log.debug("Organizer: Get agents: " + query);
          //         }
          rs = stmt.executeQuery(query);
          while(rs.next()) {
            // Find AgentComponent.
            String aName = rs.getString(1);
            for (int i=0; i < agents.length; i++) {
              AgentComponent ac = agents[i];
              if (ac.getShortName().equals(aName)) {
                nodeComponent.addAgent(ac);
// 		if(log.isDebugEnabled()) {
// 		  log.debug("OrganizerHelper:  Adding agent named:  " + ac.getShortName() + " to Node " + nodeComponent.getShortName());
// 		}
                break;
              }
            }
          }
          rs.close();
        }
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      if(log.isErrorEnabled()) {
        log.error("Caught SQL exception getting agents " + query, se);
      }
    }
  }

  private void getComponentArguments(Statement stmt,
                                     Map substitutions,
                                     Properties props)
    throws SQLException
  {
    boolean first = true;
    String query = DBUtils.getQuery(COMPONENT_ARGS_QUERY, substitutions);
    ResultSet rs = stmt.executeQuery(query);
    while (rs.next()) {
      String arg = rs.getString(1);
      if (arg.startsWith("-D")) {
        if (first) {
          props.clear();
          first = false;
        }
        int equalsIx = arg.indexOf('=', 2);
        String pname = arg.substring(2, equalsIx);
        String value = arg.substring(equalsIx + 1);
        props.setProperty(pname, value);
      }
    }
    rs.close();
  }

  private void setComponentProperties(BaseComponent cc,
                                      String comp_alib_id,
                                      String assemblyMatch)
  {
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Map substitutions = new HashMap();
        substitutions.put(":assemblyMatch", assemblyMatch);
        substitutions.put(":comp_alib_id", comp_alib_id);
        Statement stmt = conn.createStatement();
        query = DBUtils.getQuery(COMPONENT_ARGS_QUERY, substitutions);
//         if(log.isDebugEnabled()) {
//           log.debug("OrganizerHelper " + COMPONENT_ARGS_QUERY + ": "  + query);
//         }

        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          String param = rs.getString(1);
          if (param.startsWith(Experiment.PROP_PREFIX)) {
            int ix1 = Experiment.PROP_PREFIX.length();
            int ix2 = param.indexOf('=', ix1);
            String pname = param.substring(ix1, ix2);
            String pvalue = param.substring(ix2 + 1);
            Property prop = cc.getProperty(pname);
            if (prop == null) {
              //             if(log.isDebugEnabled()) {
              //               log.debug("adding " + pname + "=" + pvalue);
              //             }
              cc.addProperty(pname, pvalue);
            } else {
              //             if(log.isDebugEnabled()) {
              //               log.debug("setting " + pname + "=" + pvalue);
              //             }
              prop.setValue(pvalue);
            }
          }
        }
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      if(log.isErrorEnabled()) {
        log.error("Caught SQL exception getting compArgs " + query, se);
      }
    }
  }

  // Get a list of DBRecipe objects for the recipes in an Experiment/Trial
  private List checkForRecipes(String trialId, String exptId) {
    List recipeList = new ArrayList();
    if (trialId == null || exptId == null) {
      if (log.isWarnEnabled())
	log.warn("Null trialId or ExptId when looking for recipes. Returning none.");
      return recipeList;
    }
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Map substitutions = new HashMap();
        substitutions.put(":trial_id", trialId);
        substitutions.put(":expt_id", exptId);
        Statement stmt = conn.createStatement();
        query = DBUtils.getQuery("queryRecipes", substitutions);
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          try {
            DbRecipe dbRecipe =
              new DbRecipe(rs.getString(2), Class.forName(rs.getString(3)));
            String recipeId = rs.getString(1);
	    dbRecipe.id = recipeId;
            substitutions.put(":recipe_id", recipeId);
            getRecipeProperties(dbRecipe, conn, substitutions);
            recipeList.add(dbRecipe);
          } catch (ClassNotFoundException cnfe) {
            if(log.isErrorEnabled()) {
              log.error("for recipe", cnfe);
            }
          }
        }
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      if(log.isErrorEnabled()) {
        log.error("Caught SQL exception getting recipes " + query, se);
      }
    }

    return recipeList;
  }

  /**
   * Get the available recipe names from the database for user selection.
   * @return Map of Recipe names to database IDs
   **/
  public static Map getRecipeNamesFromDatabase() {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.ui.viewer.OrganizerHelper");

    Map recipes = new TreeMap();
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Statement stmt = conn.createStatement();
        Map substitutions = new HashMap();
        query = DBUtils.getQuery("queryLibRecipes", substitutions);
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
	  String id = rs.getString(1).trim();
	  String name = rs.getString(2).trim();

	  // Ignore bad Recipe name / IDs
	  if (id == null || id.equals("") || name == null || name.equals("")) {
	    if (log.isWarnEnabled()) {
	      log.warn("getRecipeNames: Got bad Recipe name / ID: " + name + "/ " + id);
	    }
	    continue;
	  }
          recipes.put(name, id);
        }
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      if(log.isErrorEnabled()) {
        log.error("Caught SQL exception getting recipe names " + query, se);
      }
    }

    return recipes;
  }

  // This must match PDbBase.isRecipeEqual
  private void getRecipeProperties(DbRecipe dbRecipe,
                                   Connection conn,
                                   Map substitutions) throws SQLException
  {
    Statement stmt = conn.createStatement();
    String query = DBUtils.getQuery("queryRecipeProperties", substitutions);
    ResultSet rs = stmt.executeQuery(query);

    while(rs.next()) {
      String rs1 = rs.getString(1);
      if(rs1.equalsIgnoreCase(ComplexRecipeBase.ASSEMBLY_PROP)) {
	// Query the Assembly Table for the correct data.
	continue;
      } else {
	String rs2 = rs.getString(2);
	// FIXME: This loses the ordering of the recipe properties!
	dbRecipe.props.put(rs1, rs2);
      }
    }
    rs.close();
  }

  // Get a recipe whose ID and name are as specified in the enclosed
  // object, using following method
  protected RecipeComponent getDatabaseRecipe(DbRecipe dbr) {
    if (dbr == null)
      return null;
    if (dbr.id == null)
      return null;
    if (dbr.name == null)
      return null;
    return getDatabaseRecipe(dbr.id, dbr.name);
  }

  // If we have a recipe by this name in the workspace, return it
  // Otherwise, get it from the DB
  // The ID is one that exists in the DB, and the name
  // is the new one we want, possibly same as the one in the DB
  protected RecipeComponent getDatabaseRecipe(String recipeId,
                                              String recipeName) {
    createLogger();

    String query = null;
    RecipeComponent rc = null;

    // Right up here, try to get the recipe from the workspace,
    // and return it if its there
    rc = organizer.getRecipe(recipeName);
    if (rc != null)
      return rc;

    try {
      Connection conn = DBUtils.getConnection();
      try {
        Map substitutions = new HashMap();
        substitutions.put(":recipe_id", recipeId);
        Statement stmt = conn.createStatement();
        query = DBUtils.getQuery("queryRecipe", substitutions);
        ResultSet rs = stmt.executeQuery(query);
	String name = null; // the original recipe name
	String className = null;
        if (rs.next()) {
	  name = rs.getString(2);
	  className = rs.getString(3);
	}

	// Done with these DB resources - close them up
	try {
	  rs.close();
	  stmt.close();
	} catch (SQLException sqe) {
	}

	// If we found the recipe in the DB
	if (name != null) {
	  // get the Class
	  Class recipeClass = null;
	  if (className != null) {
	    try {
	      recipeClass = Class.forName(className);
	    } catch (ClassNotFoundException cnfe) {
	      if (log.isErrorEnabled())
		log.error("Creating recipe from " + recipeId, cnfe);
	    }
	  }

	  // Is it complex?
          if(isComplexRecipe(conn, substitutions)) {
            if(log.isDebugEnabled()) {
              log.debug("Creating Complex Recipe from Database");
            }
            String assemblyId = getRecipeAssembly(conn, substitutions);

            if(assemblyId == null) {
              if(log.isErrorEnabled()) {
                log.error("Cannot locate proper assemblyId loading RecipeID " + recipeId + ".  Load Failed");
              }
	      rc = null;
            }

	    // FIXME: Save it before returning it?
	    // FIXME: Use name (original) or recipeName (new)??
	    // if dont use orig, cant correctly load comps
	    // if dont use new then we're getting potentially 2nd
	    // copy of existing recipe
	    if (recipeName.equals(name)) {
	      rc = createRecipeUsingCon(new String[] {recipeName, assemblyId, recipeId}, recipeClass, threeStringConstructor);
	    } else {
	      rc = createRecipeUsingCon(new String[] {recipeName, assemblyId, recipeId, name}, recipeClass, fourStringConstructor);
	      if (rc != null)
		rc.saveToDatabase();
	    }

          } else {
            if(log.isDebugEnabled()) {
              log.debug("Creating Simple Recipe " + recipeName + " from ID " + recipeId + " from Database");
            }
	    // Start with the old name for getting the recipe props
	    DbRecipe dbRecipe = new DbRecipe(name, recipeClass);
	    dbRecipe.id = recipeId;
	    getRecipeProperties(dbRecipe, conn, substitutions);
	    // Now set the new name
	    dbRecipe.name = recipeName;
	    rc = createRecipe(dbRecipe.name, dbRecipe.cls);
	    if (rc != null) {
	      setRecipeComponentProperties(dbRecipe, rc);
	      rc.saveToDatabase();
	    }
          } // end of SimpleRecipe handling
        } // end of block for found the recipe

        if(rc == null && log.isErrorEnabled()) {
          log.error("Recipe not found: " + recipeId);
        }
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      if(log.isErrorEnabled()) {
        log.error("Caught SQL exception getting DBRecipe " + query, se);
      }
    }
    return rc;
  }

  // Is the recipe whose ID is in the substitutions Complex?
  private boolean isComplexRecipe(Connection conn, Map substitutions) {
    boolean retVal = false;
    try {
      Statement stmt = conn.createStatement();
      try {
        String query = DBUtils.getQuery("queryRecipeProperties", substitutions);
        ResultSet rs = stmt.executeQuery(query);

	// If the recipe has an Assembly, it is Complex
        while(rs.next()) {
          if(rs.getString(1).equalsIgnoreCase(ComplexRecipeBase.ASSEMBLY_PROP)) {
            retVal = true;
	    break;
          }
        }
	rs.close();
	stmt.close();
      } catch(SQLException sqle) {
        if(log.isErrorEnabled()) {
          log.error("SQLException checking recipe type", sqle);
        }
      }
    } catch (SQLException sqe) {
      if(log.isErrorEnabled()) {
        log.error("SQLException checking recipe type", sqe);
      }
    }

    return retVal;
  }

  private String getRecipeAssembly(Connection conn, Map substitutions) {
    String assemblyId = null;
    try {
      Statement stmt = conn.createStatement();
      try {
        String query = DBUtils.getQuery("queryRecipeProperties", substitutions);
        ResultSet rs = stmt.executeQuery(query);

        while(rs.next()) {
          if(rs.getString(1).equalsIgnoreCase(ComplexRecipeBase.ASSEMBLY_PROP)) {
            assemblyId = rs.getString(2);
          }
        }
        rs.close();
      } catch(SQLException sqle) {
        if(log.isErrorEnabled()) {
          log.error("SQLException getting recipe Assembly Id", sqle);
        }
      }
    } catch (SQLException sqe) {
      if(log.isErrorEnabled()) {
        log.error("SQLException getting recipe Assembly Id", sqe);
      }
    }

    return assemblyId;
  }

  private void setRecipeComponentProperties(DbRecipe dbRecipe,
                                            RecipeComponent rc) {
    if (rc != null && dbRecipe != null && dbRecipe.props != null)
      rc.setProperties(dbRecipe.props);
  }
  private static Class[] singleStringConstructor = {String.class};

  private static Class[] twoStringConstructor = {String.class, String.class};

  private static Class[] threeStringConstructor = {String.class, String.class, String.class};

  private static Class[] fourStringConstructor = {String.class, String.class, String.class, String.class};

  private static Class[] multiConstructor = {String.class, String[].class};

  // Warning: only use this for _simple_ recipes or for
  // and empty Complex recipe
  protected RecipeComponent createRecipe(String name, Class cls) {
    return createRecipeUsingCon(new String[] {name}, cls, singleStringConstructor);
  }

  // Generic method to create a recipe. Takes an array of strings
  // suitable for passing to the given constructor
  protected RecipeComponent createRecipeUsingCon(String[] names, Class cls, Class[] con) {
    if (cls == null)
      return null;

    if (names == null || names.length == 0 || names[0] == null)
      return null;

    if (con == null)
      return null;

    createLogger();

    try {
      Constructor constructor = cls.getConstructor(con);
      RecipeComponent recipe =
  	(RecipeComponent) constructor.newInstance(names);
      recipe.initProperties();
      ((RecipeBase)recipe).resetModified();
      ((RecipeBase)recipe).installListeners();
      return recipe;
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
	log.error("Exception creating recipe " + names[0] + " of class " + cls.getName(), e);
      }
      return null;
    }
  }


  /**
   * Load a recipe by name. Note that the returned recipe may be null
   * if no such recipe exists.
   *
   * @param name a <code>String</code> recipe name to try to load from teh DB
   * @return a <code>RecipeComponent</code> with that name from the DB, possibly null if none found
   */
  protected RecipeComponent loadRecipeNamed(String name) {
    if (name == null || name.trim().equals(""))
      return null;
    // First, get any recipe assembly ID for the Recipe
    Map names = getRecipeNamesFromDatabase();
    String recipeId = (String)names.get(name);
    if (recipeId == null || recipeId.trim().equals(""))
      return null;
    return getDatabaseRecipe(recipeId, name);
  }

  /**
   * Delete the named recipe from the database.
   * @param name the name of the recipe to delete
   */
  public void deleteRecipe(String name) throws Exception {
    // First, get any recipe assembly ID for the Recipe
    Map names = getRecipeNamesFromDatabase();
    String recipeId = (String)names.get(name);
    String asb = null;
    if (recipeId != null && ! recipeId.trim().equals("")) {
      try {
	Connection conn = DBUtils.getConnection();
	try {
	  Map subs = new HashMap();
	  subs.put(":recipe_id", recipeId);
	  asb = getRecipeAssembly(conn, subs);
	} catch (Exception sqe) {
	  if (log.isWarnEnabled()) {
	    log.warn("deleteRecipe failed getting recipe assemblyID for recipe " + name, sqe);
	  }
	} finally {
	  conn.close();
	}
      } catch (SQLException sqe) {
	if (log.isInfoEnabled()) {
	  log.info("Error getting connection in deleteRecipe", sqe);
	}
      }
    }

    // Now delete the basic definition of the recipe.
    PDbBase pdb = new PDbBase();
    try {
      pdb.removeLibRecipeNamed(name);
    } finally {
      pdb.close();
    }

    // Finally, delete the assembly portion of the recipe
    // (now that the assembly is not used anymore)
    if (asb != null) {
      try {
	PopulateDb.deleteSociety(asb);
      } catch (SQLException sqe) {
	if (log.isWarnEnabled())
	  log.warn("deleteSoc failed removing asb " + asb + " for recipe " + name, sqe);
      } catch (IOException ioe) {
	if (log.isWarnEnabled())
	  log.warn("deleteSoc failed removing asb " + asb + " for recipe " + name, ioe);
      }
    }
  } // end of deleteRecipe

  /**
   * Create a new society from a node file which enumerates
   * the names of the agents in the society.
   * @param name a <code>String</code> node file name
   * @param socName a <code>String</code> name for the new society
   * @param cls a <code>Class</code> Class of Society to create
   * @return a <code>SocietyComponent</code>
   */
  public SocietyComponent createSociety(String name, String socName, Class cls) {
    createLogger();

    try {
      Constructor constructor = cls.getConstructor(twoStringConstructor);
      SocietyComponent sc =
        (SocietyComponent) constructor.newInstance(new String[] {name, socName});
      sc.initProperties();
      sc.saveToDatabase();
      return sc;
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
	log.error("Exception creating society " + name + " of " + cls.toString(), e);
      }
      return null;
    }
  }

  /**
   * Create a society from files, each of which defines an agent.
   * @param name the name of the society
   * @param filenames the names of files, each of which defines an agent
   * @param cls the class to create
   */

  public SocietyComponent createSociety(String name, String[] filenames,
                                        Class cls) {
    createLogger();

    try {
      Constructor constructor = cls.getConstructor(multiConstructor);
      SocietyComponent sc =
        (SocietyComponent) constructor.newInstance(new Object[] {name, filenames});
      sc.initProperties();
      sc.saveToDatabase();
      return sc;
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
	log.error("Exception creating society " + name + " of " + cls.toString(), e);
      }
      return null;
    }
  }


  protected SocietyComponent createSocietyFromFile() {
    // display file chooser to allow user to select file that defines society
    JFileChooser chooser =
      new JFileChooser(System.getProperty("org.cougaar.install.path"));
    chooser.setDialogTitle("Select directory of Agent INI files or a single Node INI to load");
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    File file = null;
    SocietyComponent sc = null;
    while (file == null) {
      int result = chooser.showDialog(organizer, "OK");
      if (result != JFileChooser.APPROVE_OPTION)
	return null;
      file = chooser.getSelectedFile();
      if (file != null && ! file.canRead())
	file = null;
    }

    // create society from agent files or single node file
    String name = "";
    name = file.getName();
    if (name.endsWith(".ini"))
      name = name.substring(0, name.length()-4);
    if (!organizer.isUniqueSocietyName(name))
      name = organizer.getUniqueSocietyName(name, false);
    if (name == null) return null;

    if (file.isDirectory()) {
      String[] filenames =
        SocietyFinder.getInstance().getAgentFilenames(file);

      if (filenames == null || filenames.length == 0) {
	// Found no Agents
	if (log.isWarnEnabled()) {
	  log.warn("Found no agent in dir " + file.getPath());
	}
	return null;
      }

      sc = createSociety(name, filenames, SocietyFileComponent.class);
    } else {
      sc = createSociety(file.getPath(), name, SocietyFileComponent.class);
    }
    return sc;
  }

  // Class for holding name/Class pairs in UIs
  // TODO: this is defined in Organizer as well
  // possibly needs to be in some utility class
  // it's not really UI related at all;
  // it's simply a way to store both a name and class
  private static class NameClassItem {
    public String name;
    public Class cls;
    public NameClassItem(String name, Class cls) {
      this.cls = cls;
      this.name = name;
    }
    public String toString() {
      return name;
    }

    // 2 NameClassItems are equals if they have the same name & class
    public boolean equals (Object o) {
      if (o instanceof NameClassItem) {
	NameClassItem nci = (NameClassItem)o;
	if (name != null && name.equals(nci.name)) {
	  if (cls != null && cls.equals(nci.cls))
	    return true;
	  else if (cls == null && nci.cls == null)
	    return true;
	  else
	    return false;
	} else if (name == null && nci.name == null) {
	  if (cls != null && cls.equals(nci.cls))
	    return true;
	  else if (cls == null && nci.cls == null)
	    return true;
	  else
	    return false;
	}
      }
      return false;
    }
  }

  private static class DbRecipe extends NameClassItem {
    public Map props = new TreeMap();
    public String id = null;
    public DbRecipe(String name, Class cls) {
      super(name, cls);
    }
  }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
    createLogger();
  }

}


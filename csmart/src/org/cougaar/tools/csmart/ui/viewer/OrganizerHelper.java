/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.viewer;

import java.io.IOException;
import java.io.File;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.*;
import javax.swing.JFileChooser;

import org.cougaar.util.log.Logger;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.db.ExperimentDB;
import org.cougaar.tools.csmart.core.db.PDbBase;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.db.DBConflictHandler;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.experiment.Experiment;
import org.cougaar.tools.csmart.experiment.HostComponent;
import org.cougaar.tools.csmart.experiment.NodeComponent;
import org.cougaar.tools.csmart.recipe.RecipeBase;
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.db.SocietyDBComponent;
import org.cougaar.tools.csmart.society.file.SocietyFileComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

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
  public Experiment createExperiment(String originalExperimentName,
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
      
      if(id != null) {
        String societyName = DBUtils.getSocietyName(id);
        SocietyComponent sc = organizer.getSociety(societyName);
        if (sc != null && (sc instanceof SocietyDBComponent))
          soc = (SocietyDBComponent)sc;
        else {
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
    Experiment experiment = new Experiment((String)experimentName, 
                                           experimentId, trialId);
    // The following is ugly
    
    setDefaultNodeArguments(experiment, assemblyMatch,
                            ComponentData.SOCIETY
                            + "|"
                            + originalExperimentName);
    //    experiment.setCloned(isCloned);

    //to hold all potential agents
    List agents = new ArrayList(); 

    Map experimentNamesMap = ExperimentDB.getExperimentNames();
    String origExperimentId = (String)experimentNamesMap.get(originalExperimentName);
    String origTrialId = ExperimentDB.getTrialId(origExperimentId);
    
    List defaultRecipes = checkForRecipes(origTrialId, origExperimentId);
  
    List recipes = checkForRecipes(trialId, experimentId);

    // When creating an experiment from one of the base experiments
    // copy over the recipes that came with the base
    if (! origExperimentId.startsWith("EXPT-"))
      recipes.addAll(defaultRecipes);

    if (recipes.size() != 0) {
      Iterator metIter = recipes.iterator();
      while (metIter.hasNext()) {
        DbRecipe dbRecipe = (DbRecipe) metIter.next();
        RecipeComponent mc = organizer.getRecipe(dbRecipe.name);
        if (mc == null) {
          mc = createRecipe(dbRecipe.name, dbRecipe.cls);
          setRecipeComponentProperties(dbRecipe, mc);
          ((RecipeBase)mc).resetModified();
          ((RecipeBase)mc).installListeners();
        }
        AgentComponent[] recagents = mc.getAgents(); 
        if (recagents != null && recagents.length > 0) {
          agents.addAll(Arrays.asList(recagents));
        }      
        experiment.addRecipeComponent(mc);
      }
    }
    AgentComponent[] socagents = soc.getAgents();
    if (socagents!= null && socagents.length > 0) {
      for (int i = 0; i < socagents.length; i++) {
        AgentComponent ac = socagents[i];
        setComponentProperties(ac, ac.getShortName(), assemblyMatch);
      }
      agents.addAll(Arrays.asList(socagents));
    }
    AgentComponent[] allagents = (AgentComponent[])agents.toArray(new AgentComponent[agents.size()]); 

    experiment.addSocietyComponent((SocietyComponent)soc);
    // Add all Nodes.
    addAgents(experiment, nodes, allagents, trialId, assemblyMatch);
    // Add all Hosts.
    Iterator hostIter = hosts.iterator();
    while (hostIter.hasNext()) {
      String hostName = (String) hostIter.next();
      HostComponent hc = experiment.addHost(hostName);
      setComponentProperties(hc, hostName, assemblyMatch);
    }
    mapNodesToHosts(experiment, assemblyMatch);
    try {
      if (!ExperimentDB.isExperimentNameInDatabase(experimentName))
	experiment.saveToDb(saveToDbConflictHandler);
      else {
	// Add the recipes to the expt_trial_mod_recipe table
	PopulateDb pdb = null;
	try {
	  pdb = new PopulateDb(experiment.getExperimentID(), experiment.getTrialID());
	  List rcs = new ArrayList();
	  for (int i = 0; i < experiment.getRecipeComponentCount(); i++) {
	    rcs.add(experiment.getRecipeComponent(i));
	  }
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

  // Currently the passed in args are not used however I expect
  // this to change very soon.
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

    NodeComponent[] nodeComponents = experiment.getNodeComponents();
    HostComponent[] hostComponents = experiment.getHostComponents();
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
        substitutions.put(":insertion_point", "Node.AgentManager.Agent");	 
        Statement stmt = conn.createStatement();
        NodeComponent nodeComponent = null;
        while(iter.hasNext()) {
          query = null;
          ResultSet rs;
          String nodeName = (String)iter.next();
          nodeComponent = experiment.addNode(nodeName);
          setComponentProperties(nodeComponent, nodeName, assemblyMatch);
          substitutions.put(":parent_name", nodeName);
          substitutions.put(":comp_alib_id", nodeName);
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
                //                if(log.isDebugEnabled()) {
                //                  log.debug("OrganizerHelper:  Adding agent named:  " + ac.getShortName());
                //                }
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
        if(log.isDebugEnabled()) {
          log.debug("OrganizerHelper " + COMPONENT_ARGS_QUERY + ": "  + query);
        }
        
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

  private List checkForRecipes(String trialId, String exptId) {
    List recipeList = new ArrayList();
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
          recipes.put(rs.getString(2), rs.getString(1));
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

  private void getRecipeProperties(DbRecipe dbRecipe, 
                                   Connection conn, 
                                   Map substitutions) throws SQLException
  {
    Statement stmt = conn.createStatement();
    String query = DBUtils.getQuery("queryRecipeProperties", substitutions);
    ResultSet rs = stmt.executeQuery(query);
    while(rs.next()) {
      dbRecipe.props.put(rs.getString(1), rs.getString(2));
    }
    rs.close();
  }

  public RecipeComponent getDatabaseRecipe(String recipeId,
                                           String recipeName) {
    createLogger();
    
    String query = null;
    RecipeComponent rc = null;
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Map substitutions = new HashMap();
        substitutions.put(":recipe_id", recipeId);
        Statement stmt = conn.createStatement();
        query = DBUtils.getQuery("queryRecipe", substitutions);
        ResultSet rs = stmt.executeQuery(query);
        if (rs.next()) {
          try {
            DbRecipe dbRecipe = 
              new DbRecipe(rs.getString(2), Class.forName(rs.getString(3)));
            getRecipeProperties(dbRecipe, conn, substitutions);
            dbRecipe.name = recipeName;
            rc = organizer.getRecipe(dbRecipe.name);
            if (rc == null) {
              rc = createRecipe(dbRecipe.name, dbRecipe.cls);
              setRecipeComponentProperties(dbRecipe, rc);
              ((RecipeBase)rc).resetModified();
              ((RecipeBase)rc).installListeners();
            }
            return rc;
          } catch (ClassNotFoundException cnfe) {
            if(log.isErrorEnabled()) {
              log.error("for recipe", cnfe);
            }
          }
        }
        if(rc == null && log.isErrorEnabled()) {
          log.error("Recipe not found: " + recipeId);
        }
        rs.close();
        stmt.close();
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

  private void setRecipeComponentProperties(DbRecipe dbRecipe, 
                                            RecipeComponent rc) {
    if (rc != null && dbRecipe != null && dbRecipe.props != null)
      rc.setProperties(dbRecipe.props);
  }
  private static Class[] singleStringConstructor = {String.class};

  private static Class[] twoStringConstructor = {String.class, String.class};

  private static Class[] multiConstructor = {String.class, String[].class};
  
  public RecipeComponent createRecipe(String name, Class cls) {
    createLogger();
    
    try {
      Constructor constructor = cls.getConstructor(singleStringConstructor);
      RecipeComponent recipe =
  	(RecipeComponent) constructor.newInstance(new String[] {name});
      recipe.initProperties();
      ((RecipeBase)recipe).resetModified();
      ((RecipeBase)recipe).installListeners();
      return recipe;
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
	log.error("Exception creating recipe " + name + " of class " + cls.toString(), e);
      }
      return null;
    }
  }

  /**
   * Delete the named recipe from the database.
   * @param name the name of the recipe to delete
   */
  public void deleteRecipe(String name) throws Exception {
    PDbBase pdb = new PDbBase();
    try {
      pdb.removeLibRecipeNamed(name);
    } finally {
      pdb.close();
    }
  }

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
      new JFileChooser(SocietyFinder.getInstance().getPath());
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    File file = null;
    SocietyComponent sc = null;
    while (file == null) {
      int result = chooser.showDialog(organizer, "OK");
      if (result != JFileChooser.APPROVE_OPTION)
	return null;
      file = chooser.getSelectedFile();
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
  }

  private static class DbRecipe extends NameClassItem {
    public Map props = new TreeMap();
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


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

package org.cougaar.tools.csmart.ui.viewer;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.*;

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
import org.cougaar.tools.csmart.recipe.RecipeComponent;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.tools.csmart.society.SocietyComponent;
import org.cougaar.tools.csmart.society.cmt.CMTSociety;

public class OrganizerHelper {
  private static final String EXPT_DESC_QUERY = "queryExptDescriptions";
  private static final String EXPT_ASSEM_QUERY = "queryExperiment";
  private static final String EXPT_TRIAL_QUERY = "queryTrials";
  private static final String EXPT_NODE_QUERY = "queryNodes";
  private static final String EXPT_HOST_QUERY = "queryHosts";
  private static final String COMPONENT_ARGS_QUERY = "queryComponentArgs";

  private DBConflictHandler saveToDbConflictHandler =
    GUIUtils.createSaveToDbConflictHandler(null);


  /**
   * Create an experiment.
   * @return Experiment the new experiment or null if any error
   */

  public Experiment createExperiment(String originalExperimentName,
                                     String experimentName,
                                     String experimentId,
                                     String trialId) {
    // get assembly ids for trial
    ArrayList assemblyIds = getTrialAssemblyIds(experimentId, trialId);
    String assemblyMatch = getAssemblyMatch(assemblyIds);
    // get nodes for trial
    ArrayList nodes = getNodes(trialId, assemblyMatch);
    ArrayList hosts = getHosts(trialId, assemblyMatch);
    CMTSociety soc = null;
    if (assemblyIds.size() != 0) {
      soc = new CMTSociety(assemblyIds);      
      soc.initProperties();
    } else { // We need to create a new trial.
      // Need to have the experiment id, trial id, and multiplicity
      // for the call that will generate the assembly here.
      System.out.println("No assemblies for: " + experimentId + " " + trialId);
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

    List recipes = checkForRecipes(trialId, experimentId);
    if (recipes.size() != 0) {
      Iterator metIter = recipes.iterator();
      while (metIter.hasNext()) {
        DbRecipe dbRecipe = (DbRecipe) metIter.next();
        RecipeComponent mc = createRecipeComponent(dbRecipe.name, dbRecipe.cls);
        setRecipeComponentProperties(dbRecipe, mc);
        AgentComponent[] recagents = mc.getAgents(); 
        if (recagents != null && recagents.length > 0) {
          agents.addAll(Arrays.asList(recagents));
        }      
        experiment.addRecipe(mc);
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
    } catch (RuntimeException e) {
      System.err.println(e);
    }
    return experiment;
  }

  /**
   * Get assembly ids for trial.
   */

  private ArrayList getTrialAssemblyIds(String experimentId, String trialId) {
    ArrayList assemblyIds = new ArrayList();
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
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
    } catch (SQLException se) {
      System.err.println("Caught SQL exception getting Trial pieces: " + query + ": " + se);
      se.printStackTrace();
    }
    return assemblyIds;
  }

  private String getAssemblyMatch(ArrayList assemblyIDs) {
    StringBuffer assemblyMatch = new StringBuffer();
    assemblyMatch.append("in (");
    Iterator iter = assemblyIDs.iterator();
    boolean first = true;
    while (iter.hasNext()) {
      String val = (String)iter.next();
      if (first) {
        first = false;
      } else {
        assemblyMatch.append(", ");
      }
      assemblyMatch.append("'");
      assemblyMatch.append(val);
      assemblyMatch.append("'");
    }
    // Avoid ugly exceptions if got no assemblies:
    if (assemblyIDs.size() < 1) {
      System.out.println("Got no assemblies!");
      assemblyMatch.append("''");
    }
    assemblyMatch.append(")");
    return assemblyMatch.toString();
  }

  /**
   * Get nodes for a trial.
   */

  private ArrayList getNodes(String trialId, String assemblyMatch) {
    ArrayList nodes = new ArrayList();
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
      Map substitutions = new HashMap();
      substitutions.put(":trial_id", trialId);
      substitutions.put(":assemblyMatch", assemblyMatch);
      Statement stmt = conn.createStatement();
      query = DBUtils.getQuery(EXPT_NODE_QUERY, substitutions);
      //      System.out.println("Organizer: Nodes query: " + query);
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
        nodes.add(rs.getString(1));
      }
      rs.close();
      stmt.close();
      conn.close();
    } catch (SQLException se) {      
      System.err.println("Caught SQL exception getting Nodes " + query + ": " + se);
      se.printStackTrace();
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
      Map substitutions = new HashMap();
      substitutions.put(":assemblyMatch", assemblyMatch);
      Statement stmt = conn.createStatement();
      query = DBUtils.getQuery(EXPT_HOST_QUERY, substitutions);
      //      System.out.println("Organizer: Get hosts query: " + query);
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
        hosts.add(rs.getString(1));
      }
      rs.close();
      stmt.close();
      conn.close();
    } catch (SQLException se) {      
      System.err.println("Caught SQL exception getting hosts: " + query + ": " + se);
      se.printStackTrace();
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
      System.err.println("Caught SQL exception setting NodeArgs: " + se);
      se.printStackTrace();
    }
  }


  private void mapNodesToHosts(Experiment experiment, 
			       String assemblyMatch ) {

    NodeComponent[] nodeComponents = experiment.getNodes();
    HostComponent[] hostComponents = experiment.getHosts();
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
      Map substitutions = new HashMap();
      substitutions.put(":assemblyMatch", assemblyMatch);
      Statement stmt = conn.createStatement();
      query = DBUtils.getQuery("queryHostNodes", substitutions);
      //      System.out.println(query);
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
      conn.close();   
    } catch (SQLException se) {
      System.err.println("Caught SQL exception getting HN map " + query + ": " + se);
      se.printStackTrace();
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
	//        System.out.println("Organizer: Get agents: " + query);
        rs = stmt.executeQuery(query);
        while(rs.next()) {
          // Find AgentComponent.
          String aName = rs.getString(1);
          for (int i=0; i < agents.length; i++) {
            AgentComponent ac = agents[i];
            if (ac.getShortName().equals(aName)) {
              nodeComponent.addAgent(ac);
              //System.out.println("Organizer:  Adding agent named:  " + ac.getShortName());
              break;
            }	    
          } 	  
        }
        rs.close();
      }
      stmt.close();
      conn.close();   
    } catch (SQLException se) {
      System.err.println("Caught SQL exception getting agents " + query + ": " + se);
      se.printStackTrace();
    }
  }

  private void getComponentArguments(Statement stmt,
                                     Map substitutions,
                                     Properties props)
    throws SQLException
  {
    boolean first = true;
    String query = DBUtils.getQuery("queryComponentArgs", substitutions);
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
      Map substitutions = new HashMap();
      substitutions.put(":assemblyMatch", assemblyMatch);
      substitutions.put(":comp_alib_id", comp_alib_id);
      Statement stmt = conn.createStatement();
      query = DBUtils.getQuery(COMPONENT_ARGS_QUERY, substitutions);
      //      System.out.println("Organizer " + COMPONENT_ARGS_QUERY + ": "  + query);
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
	    //            System.out.println("adding " + pname + "=" + pvalue);
            cc.addProperty(pname, pvalue);
          } else {
	    //            System.out.println("setting " + pname + "=" + pvalue);
            prop.setValue(pvalue);
          }
        }
      }
      rs.close();
      stmt.close();
      conn.close();
    } catch (SQLException se) {      
      System.err.println("Caught SQL exception gettin compArgs " + query + ": " + se);
      se.printStackTrace();
    }
  }

  private List checkForRecipes(String trialId, String exptId) {
    List recipeList = new ArrayList();
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
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
          System.err.println(cnfe + ": for recipe");
        }
      }
      rs.close();
      stmt.close();
      conn.close();   
    } catch (SQLException se) {
      System.err.println("Caught SQL exception getting recipes " + query + ": " + se);
      se.printStackTrace();
    }    
    
    return recipeList;
  }

  public Map getRecipeNamesFromDatabase() {
    Map recipes = new TreeMap();
    String query = null;
    try {
      Connection conn = DBUtils.getConnection();
      Statement stmt = conn.createStatement();
      Map substitutions = new HashMap();
      query = DBUtils.getQuery("queryLibRecipes", substitutions);
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
        recipes.put(rs.getString(2), rs.getString(1));
      }
      rs.close();
      stmt.close();
      conn.close();   
    } catch (SQLException se) {
      System.err.println("Caught SQL exception getting recipe names " + query + ": " + se);
      se.printStackTrace();
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
    String query = null;
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
            RecipeComponent rc = 
              createRecipeComponent(dbRecipe.name, dbRecipe.cls);
            setRecipeComponentProperties(dbRecipe, rc);
            return rc;
          } catch (ClassNotFoundException cnfe) {
            System.err.println(cnfe + ": for recipe");
          }
        }
        System.err.println("Recipe not found: " + recipeId);
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      System.err.println("Caught SQL exception gettin DBRecipe " + query + ": " + se);
      se.printStackTrace();
    }    
    return null;
  }

  private void setRecipeComponentProperties(DbRecipe dbRecipe, 
                                            RecipeComponent rc) {
    for (Iterator i = dbRecipe.props.keySet().iterator(); i.hasNext(); ) {
      try {
        String propName = (String) i.next();
        String propValue = (String) dbRecipe.props.get(propName);
        Property prop = rc.getProperty(propName);
        if (prop == null) {
          System.err.println("Unknown property: " + propName + "=" + propValue);
        } else {
          Class propClass = prop.getPropertyClass();
          Constructor constructor = 
            propClass.getConstructor(new Class[] {String.class});
          Object value = constructor.newInstance(new Object[] {propValue});
          prop.setValue(value);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static Class[] constructorArgTypes = {String.class};

  private RecipeComponent createRecipeComponent(String name, Class cls) {
    try {
      Constructor constructor = cls.getConstructor(constructorArgTypes);
      RecipeComponent recipe =
  	(RecipeComponent) constructor.newInstance(new String[] {name});
      recipe.initProperties();
      return recipe;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  // Create a new built-in society.
  public SocietyComponent createSociety(String name, Class cls) {
    try {
      Constructor constructor = cls.getConstructor(constructorArgTypes);
      SocietyComponent sc = 
        (SocietyComponent) constructor.newInstance(new String[] {name});
      sc.initProperties();
      return sc;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
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
}


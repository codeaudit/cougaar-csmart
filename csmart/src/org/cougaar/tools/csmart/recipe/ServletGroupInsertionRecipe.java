/* 
 * <copyright>
 *  Copyright 2002-2003 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.recipe;




import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Recipe to easily select a set of servlets for inclusion in a society
 * It gets a basic set of recipes from csmart/data/common/servlets.txt
 * See that file for format.
 * Users can use this recipe to add any other servlet they like
 */
public class ServletGroupInsertionRecipe extends RecipeBase
  implements Serializable
{
  private static final String DESCRIPTION_RESOURCE_NAME = 
    "servlet-group-insertion-recipe-description.html";
  private static final String BACKUP_DESCRIPTION = 
    "ServletGroupInsertionRecipe provides a method for adding groups of servlets into an experiment";

  private static final boolean PROP_SERVLETS_DFLT = false;
  private static final String PROP_SERVLETS_DESC = "The complete list of all available Servlets";

  private static final String PROP_TARGET_AGENT_QUERY = "Target Agent Selection Query";
  private static final String PROP_TARGET_AGENT_QUERY_DFLT = "recipeQueryAllAgents";
  private static final String PROP_TARGET_AGENT_QUERY_DESC = 
    "The query name for selecting agents to which to add servlets.";

  private static final String DFLT_SERVLET_CLASS = "org.cougaar.core.servlet.SimpleServletComponent";


  private Property[] propServlets;
  private Vector servletList = null;
  private Property propTargetAgentQuery;
  //  private Property propNewServletCount;
//   private Property[] propNewServlets = null; // class of servlet
//   private Property[] propNewServletNumArgs = null; // how many args it has
//   private Property[][] propNewServletArgs = null; // args for each new servlet


  public ServletGroupInsertionRecipe() {
    super("Servlet Group Insertion Recipe");
  }

  public ServletGroupInsertionRecipe(String name) {
    super(name);
  }

  public void initProperties() {

    servletList = getServletList();
    propServlets = new Property[servletList.size()];

    if (servletList != null) {
      for (int i=0; i < servletList.size(); i++) {
        propServlets[i] = addBooleanProperty(((ServletListRecord)servletList.elementAt(i)).getName(), PROP_SERVLETS_DFLT);
        ((Property)propServlets[i]).setToolTip(PROP_SERVLETS_DESC);
      }
    }
   
    propTargetAgentQuery =
      addRecipeQueryProperty(PROP_TARGET_AGENT_QUERY,
                             PROP_TARGET_AGENT_QUERY_DFLT);
    propTargetAgentQuery.setToolTip(PROP_TARGET_AGENT_QUERY_DESC);
  }

  private Property addRecipeQueryProperty(String name, String dflt) {
    Property prop = addProperty(new RecipeQueryProperty(this, name, dflt));
    prop.setPropertyClass(String.class);
    return prop;
  }

  
  /**
   * Gets the name of the html help file for this component.
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  public String getRecipeName() {
    return getShortName();
  }

  private Vector getServletList() {
    Vector vec = null;

    ConfigFinder cf = ConfigFinder.getInstance("csmart");
    File inputFile = null;
    try {
      inputFile = cf.locateFile("servlets.txt");
    } catch(Exception e) {
      if (log.isErrorEnabled()) {
	log.error("Could not read servlets file.", e);
      }
    } 
    if (vec == null) {
      vec = new Vector();
      if (inputFile != null) {
	try { 
	  RandomAccessFile servletFile = null;
	  // read servlets, one per line
	  servletFile = new RandomAccessFile(inputFile, "r");      
	  while (true) {
	    String isrv = servletFile.readLine(); // get servlet line
	    // Skip comment lines
	    if (isrv == null) {
	      break;
	    }
	    isrv = isrv.trim();
	    if (isrv.startsWith("#") || isrv.equals(""))
	      continue;
	    ServletListRecord servletRecord = parseServletLine(isrv);
	    vec.add(servletRecord);
	  }
	  servletFile.close();
	} catch (IOException e) {
	  if (log.isErrorEnabled()) {
	    log.error("Error during read/open from file: " + inputFile, e);
	  }
	}
      } else {
	if (log.isWarnEnabled()) {
	  log.warn("Unable to find servlets.txt! Report bug 2000.");
	  log.warn("config.path= " + System.getProperty("org.cougaar.config.path"));
	  log.warn("Trying again with verbose on: ");
          // MIK: removed because the method was removed (it hasn't done anything for a long time)
	  //cf.setVerbose(true);
	  try {
	    inputFile = cf.locateFile("servlets.txt");
	  } catch(Exception e) {
	    log.error("Could not read servlets file.", e);
	  } 
	  if (inputFile != null) {
	    log.warn(".. this time we found it?");
	  } else {
	    log.warn("... still couldn't find it?");
	  }
	}
      }
    }
    return vec;    
  }


  private ServletListRecord parseServletLine(String line) {
    StringTokenizer tokens = new StringTokenizer(line, ",");
    String name = tokens.nextToken();
    String classname = tokens.nextToken();
    name = name.trim();
    classname = classname.trim();
    ArrayList args = new ArrayList();
    while (tokens.hasMoreTokens()) {
      args.add(tokens.nextToken().trim());
    }
    String[] arguments = new String[args.size()];
    arguments = (String[]) args.toArray(arguments);

    ServletListRecord servRecord =  new ServletListRecord(name, classname, arguments);
    return servRecord;
  }
  
  public ComponentData modifyComponentData(ComponentData data, PopulateDb pdb) {
    try {
      Set targets = pdb.executeQuery(propTargetAgentQuery.getValue().toString());
      modifyComponentData(data, pdb, targets);
    } catch (SQLException sqle) {
      if (log.isErrorEnabled()) {
	log.error("Cant run agent target query", sqle);
      }
    }
    return data;
  }

  private void modifyComponentData(ComponentData data, PopulateDb pdb, Set targets)
    throws SQLException
  {
    if (targets.contains(pdb.getComponentAlibId(data))) { 
      if (log.isDebugEnabled()) {
	log.debug("Adding servlets to " + pdb.getComponentAlibId(data));
      }
      //if the set of targets (agents) contains the one we're at now
      // do insertion of the correct set of servlets into this agent
      // need to get the set of servlets the user selected
     
      if (propServlets != null) {
        for(int i=0; i < propServlets.length; i++) {
	  ServletListRecord servlet = (ServletListRecord)servletList.elementAt(i);
          if (((Boolean)(propServlets[i].getValue())).booleanValue()) {
            GenericComponentData plugin = new GenericComponentData();
            plugin.setType(ComponentData.PLUGIN);

	    String[] arguments = servlet.getArguments();
	    for (int j = 0; j < arguments.length; j++) 
	      plugin.addParameter(arguments[j]);

            plugin.setClassName(servlet.getClassname());
            plugin.setParent(data);
            plugin.setOwner(this);
	    if (GenericComponentData.alreadyAdded(data, plugin)) {
	      if (log.isDebugEnabled()) {
		log.debug("Not re-adding servlet. " + data.getName() + " already contains " + plugin);
	      }
	    } else {
	      plugin.setName(GenericComponentData.getSubComponentUniqueName(data, plugin));
	      data.addChildDefaultLoc(plugin);
	    }

          }
        }
      }
    }

    if (data.childCount() > 0) {
      // for each child, call this same method.
      ComponentData[] children = data.getChildren();
      for (int i = 0; i < children.length; i++) {
	// If the child is a plugins or AgentBinder, no need to look at it
	if (children[i].getType().equals(ComponentData.PLUGIN) || children[i].getType().equals(ComponentData.AGENTBINDER))
	  continue;
// 	if (log.isDebugEnabled()) {
// 	  log.debug("modify recursing into " + children[i]);
// 	}
	modifyComponentData(children[i], pdb, targets);
      }
    }
  }

  class ServletListRecord extends Object
    implements Serializable {

    private String name;
    private String classname;
    private String[] argument;

    public ServletListRecord() {}
    
    public ServletListRecord(String name, String classname, String argument[]) {
      this.name = name;
      this.classname = classname;
      this.argument = argument;
    }

    public String getName() {
      return name;
    }
    public String getClassname() {
      return classname;
    }
    public String[] getArguments() {
      return argument;
    }
    public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append(name + " " +classname);
      for (int i = 0; i < argument.length; i++) {
	buf.append(" " +argument[i]);
      }
      return buf.toString();
    }
  }
}

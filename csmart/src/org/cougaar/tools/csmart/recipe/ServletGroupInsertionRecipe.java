/* 
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redsitribute it and/or modify
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

import java.sql.SQLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.Collections;
import java.util.StringTokenizer;
import java.io.Serializable;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;

import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.db.PopulateDb;

import org.cougaar.tools.csmart.society.AgentComponent;

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

  private static final String PROP_NEW_SERVLETS_COUNT = "_Number of New Servlets";
  private static final Integer PROP_NEW_SERVLETS_COUNT_DFLT = new Integer(0);
  private static final Integer PROP_NEW_SERVLETS_ARG_COUNT_DFLT = new Integer(2);
  private static final String PROP_NEW_SERVLETS_COUNT_DESC = "Number of servlets to be added that are not listed above.";

  private static final String DFLT_SERVLET_CLASS = "org.cougaar.core.servlet.SimpleServletComponent";


  private Property[] propServlets;
  private Vector servletList = null;
  private Property propTargetAgentQuery;
  private Property propNewServletCount;
  private Property[] propNewServlets = null; // class of servlet
  private Property[] propNewServletNumArgs = null; // how many args it has
  private Property[][] propNewServletArgs = null; // args for each new servlet


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

    propNewServletCount = addProperty(PROP_NEW_SERVLETS_COUNT, PROP_NEW_SERVLETS_COUNT_DFLT);
    propNewServletCount.addPropertyListener(new ConfigurableComponentPropertyAdapter() {
        public void propertyValueChanged(PropertyEvent e) {
          updateNewServletCount((Integer)e.getProperty().getValue());
        }
      });
    propNewServletCount.setToolTip(PROP_NEW_SERVLETS_COUNT_DESC);
  }

  private Property addRecipeQueryProperty(String name, String dflt) {
    Property prop = addProperty(new RecipeQueryProperty(this, name, dflt));
    prop.setPropertyClass(String.class);
    return prop;
  }

  private void updateNewServletCount(Integer newCount) {
    int count = newCount.intValue();

    // For now delete all variable props and start fresh.
    // Annoying for the user, but it works.
    if( propNewServlets != null && count != propNewServlets.length ) {
      for(int i=0; i < propNewServlets.length; i++) {
        removeProperty(propNewServlets[i]); // class
	// Now the arguments
	int numargs = 0;
	if (propNewServletNumArgs[i] != null) {
	  Integer na = (Integer)propNewServletNumArgs[i].getValue();
	  numargs = na.intValue();
	}
	for (int j = 0; j < numargs; j++) {
	  removeProperty(propNewServletArgs[i][j]);
	}
	// Now the number of arguments
        removeProperty(propNewServletNumArgs[i]);
      }
    }

    propNewServlets = new Property[count];
    propNewServletNumArgs = new Property[count];
    propNewServletArgs = new Property[count][0];

    for(int i=0; i < count; i++) {
      propNewServlets[i] = addProperty("Classname of servlet " + (i+1), DFLT_SERVLET_CLASS);
      ((Property) propNewServlets[i]).setToolTip("Full classname of the new servlet (loader) #" + (i+1));

      propNewServletNumArgs[i] = addProperty("Number of args for servlet " + (i+1), PROP_NEW_SERVLETS_ARG_COUNT_DFLT);
      ((Property)propNewServletNumArgs[i]).setToolTip("Number of arguments for servlet " + (i+1));
      ((Property)propNewServletNumArgs[i]).addPropertyListener(new ConfigurableComponentPropertyAdapter() {
	  public void propertyValueChanged(PropertyEvent e) {
	    String name = e.getProperty().getName().toString();
	    String servnum = name.substring(name.indexOf("servlet") + 8);
	    int snum = -1;
	    try {
	      snum = Integer.parseInt(servnum) - 1;
	    } catch (NumberFormatException nfe) {}
	    if (snum != -1) {
	      updateNewServletArgCount(snum, (Integer)e.getProperty().getValue());
	    } else {
	      if (log.isErrorEnabled()) {
		log.error("Couldn't figure out servlet being edited. name was " + name + ", servnum was " + servnum);
	      }
	    }
	  }
	});
      updateNewServletArgCount(i, PROP_NEW_SERVLETS_ARG_COUNT_DFLT);
    }
  }

  // Update the arguments for a new servlet
  private void updateNewServletArgCount(int servnum, Integer newCount) {
    int count = newCount.intValue();
    // First remove the old ones
    if (propNewServletArgs != null && propNewServletArgs[servnum] != null && propNewServletArgs[servnum].length != count) {
      for (int i = 0; i < (propNewServletArgs[servnum]).length; i++) {
	removeProperty(propNewServletArgs[servnum][i]);
      }
    }

    // Now add the new set in
    propNewServletArgs[servnum] = new Property[count];
    // The first argument is usually the real class of the servlet
    if (count > 0) {
      propNewServletArgs[servnum][0] = addProperty("New Servlet " + (servnum+1) + " arg 1", "");
      ((Property) propNewServletArgs[servnum][0]).setToolTip("New Servlet " + (servnum+1) + " First argument, usu class");
    }
    for (int i = 1; i < count; i++) {
      propNewServletArgs[servnum][i] = addProperty("New Servlet " + (servnum+1) + " arg " + (i+1), "");
      ((Property) propNewServletArgs[servnum][i]).setToolTip("New Servlet " + (servnum+1) + " argument " + (i+1));
    }
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

    ConfigFinder cf = ConfigFinder.getInstance();
    File inputFile = null;
    try {
      inputFile = cf.locateFile("servlets.txt");
    } catch(Exception e) {
      if (log.isErrorEnabled()) {
	log.error("Could not read servlets file.", e);
      }
    } 
    if (vec == null) {
      try { 
        vec = new Vector();
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
    if (targets.contains(pdb.getComponentAlibId(data))) {  //if the set of targets (agents) contains the one we're at now
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

	    // Component name is the class of the servlet plus its first
	    // argument, or a number if none
	    if (arguments.length > 0) {
	      plugin.setName(servlet.getClassname().substring(servlet.getClassname().lastIndexOf('.')+1) + "-" + arguments[0]);
	    } else {
	      plugin.setName(servlet.getClassname() + (i+1));
	    }
            plugin.setClassName(servlet.getClassname());
            plugin.setParent(data);
            plugin.setOwner(this);
            data.addChildDefaultLoc(plugin);
          }
        }
      }
      if (propNewServlets != null) {
        for(int i=0; i < propNewServlets.length; i++) { 
            GenericComponentData comp = new GenericComponentData();
            comp.setType(ComponentData.PLUGIN);
	    String newcls = (String)propNewServlets[i].getValue();
	    // How many args does the servlet have
	    int numargs = ((Integer)propNewServletNumArgs[i].getValue()).intValue();
	    // Component name is the class of the servlet plus its first
	    // argument, or a number if none
	    if (numargs > 0) {
	      comp.setName(newcls.substring(newcls.lastIndexOf('.')+1) + "-" +  ((String)propNewServletArgs[i][0].getValue()));
	    } else {
	      comp.setName(newcls +(i+1));
	    }
            comp.setClassName(newcls);
            comp.setParent(data);
            comp.setOwner(this);

	    // For each arg it has, write it out
	    for (int j = 0; j < numargs; j++)
	      comp.addParameter((String)propNewServletArgs[i][j].getValue());
            data.addChildDefaultLoc(comp);
        }
      }
    }
    if (data.childCount() > 0) {
      // for each child, call this same method.
      ComponentData[] children = data.getChildren();
      for (int i = 0; i < children.length; i++) {
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

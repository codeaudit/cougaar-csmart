/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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

import java.io.Serializable;
import java.sql.SQLException;
import java.net.URL;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.StringTokenizer;

import org.cougaar.tools.csmart.ui.util.Util;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.util.ConfigFinder;
import org.cougaar.tools.csmart.core.db.PopulateDb;

import org.cougaar.tools.csmart.society.AgentComponent;

public class ServletGroupInsertionRecipe extends RecipeBase
  implements Serializable
{
  private static final String DESCRIPTION_RESOURCE_NAME = 
    "servlet-group-insertion-recipe-description.html";
  private static final String BACKUP_DESCRIPTION = 
    "ServletGroupInsertionRecipe provides a method for adding groups of servlets into an experiment";

  private static final String TRUE = "True";
  private static final String FALSE = "False";

  private static final String PROP_SERVLETS_DFLT = "False";
  private static final String PROP_SERVLETS_DESC = "The complete list of all available Servlets";

  private static final String PROP_TARGET_AGENT_QUERY = "Target Agent Selection Query";
  private static final String PROP_TARGET_AGENT_QUERY_DFLT = "recipeQueryAllAgents";
  private static final String PROP_TARGET_AGENT_QUERY_DESC = 
    "The query name for selecting agents to which to add servlets.";

  private static final String PROP_NEW_SERVLETS_COUNT = "_Number of New Servlets";
  private static final Integer PROP_NEW_SERVLETS_COUNT_DFLT = new Integer(0);
  private static final String PROP_NEW_SERVLETS_COUNT_DESC = "Number of servlets to be added that are not listed below.";


  private Property[] propServlets;
  private Vector servletList = null;
  private Property propTargetAgentQuery;
  private Property propNewServletCount;
  private Property[] propNewServlets = null;
  private Property[] propNewServletArgs = null;


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
        propServlets[i] = addBooleanProperty(((ServletListRecord)servletList.elementAt(i)).getName(),PROP_SERVLETS_DFLT);
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
        removeProperty(propNewServlets[i]);
        removeProperty(propNewServletArgs[i]);
      }
    }

    propNewServlets = new Property[count];
    propNewServletArgs = new Property[count];

    for(int i=0; i < count; i++) {
      propNewServlets[i] = addProperty("Classname of servlet" + (i+1), "");
      ((Property) propNewServlets[i]).setToolTip("Full classname of the new servlet." + (i+1));

      propNewServletArgs[i] = addProperty("Path of servlet" + (i+1), "");
      ((Property)propNewServletArgs[i]).setToolTip("Path to servlet." + (i+1));
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
      System.err.println("Could not read servlets file.");
      e.printStackTrace();
    } 
    if (vec == null) {
      try { 
        vec = new Vector();
        RandomAccessFile servletFile = null;
        // read servlets, one per line
        servletFile = new RandomAccessFile(inputFile, "r");      
        while (true) {
          String isrv = servletFile.readLine(); // get servlet line      
          if (isrv == null) {
            break;
          }
          isrv = isrv.trim();
          ServletListRecord servletRecord = parseServletLine(isrv);
          vec.add(servletRecord);
        }
        servletFile.close();

      } catch (IOException e) {
        e.printStackTrace();
        System.err.println("Error during read/open from file: " + inputFile + 
                           " " + e.toString()); 
      }
    }
    return vec;    
  }


  private ServletListRecord parseServletLine(String line) {
    StringTokenizer tokens = new StringTokenizer(line, ",");
    String name = tokens.nextToken();
    String classname = tokens.nextToken();
    String argument = tokens.nextToken();
    name = name.trim();
    classname = classname.trim();
    argument = argument.trim();

    ServletListRecord servRecord =  new ServletListRecord(name, classname, argument);
    return servRecord;
  }
  
  public ComponentData modifyComponentData(ComponentData data, PopulateDb pdb) {
    try {
      Set targets = pdb.executeQuery(propTargetAgentQuery.getValue().toString());
      modifyComponentData(data, pdb, targets);
    } catch (SQLException sqle) {
      sqle.printStackTrace();
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
          if (((Property)propServlets[i]).getValue().toString().equals(TRUE)) {
            GenericComponentData plugin = new GenericComponentData();
            plugin.setType(ComponentData.PLUGIN);
            plugin.setName("SimpleServletComponent" +(i+1));
            plugin.setClassName("org.cougaar.core.servlet.SimpleServletComponent");
            plugin.setParent(data);
            plugin.setOwner(this);
 
            plugin.addParameter(((ServletListRecord)servletList.elementAt(i)).getClassname());
            plugin.addParameter(((ServletListRecord)servletList.elementAt(i)).getArgument());
            data.addChildDefaultLoc(plugin);
          }
        }
      }
      if (propNewServlets != null) {
        for(int i=0; i < propNewServlets.length; i++) { 
            GenericComponentData comp = new GenericComponentData();
            comp.setType(ComponentData.PLUGIN);
            comp.setName("SimpleServletComponent" +(i+1));
            comp.setClassName("org.cougaar.core.servlet.SimpleServletComponent");
            comp.setParent(data);
            comp.setOwner(this);
 
            comp.addParameter((String)propNewServlets[i].getValue());
            comp.addParameter((String)propNewServletArgs[i].getValue());
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
    private String argument;

    public ServletListRecord() {}
    
    public ServletListRecord(String name, String classname, String argument) {
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
    public String getArgument() {
      return argument;
    }
    public String toString() {
      return (name + " " +classname + " " +argument);
    }
  }
}

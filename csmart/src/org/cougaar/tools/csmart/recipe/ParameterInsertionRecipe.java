/**
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.recipe;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.Property;

import java.io.Serializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.Set;

public class ParameterInsertionRecipe extends RecipeBase
  implements Serializable
{
  
  private static final String DESCRIPTION_RESOURCE_NAME 
    = "parameter-insertion-recipe-description.html";
  private static final String BACKUP_DESCRIPTION = 
    "ParameterInsertionRecipe provides a method for inserting new Parameters into a Component";

  private static final String PROP_QUERY = "Agent Query";
  private static final String PROP_QUERY_DFLT = "";
  private static final String PROP_QUERY_DESC = "Query for Agents to whose components to add a parameter";

  private static final String PROP_PLUGINNAME = "Plugin Name";
  private static final String PROP_PLUGINNAME_DFLT = "";
  private static final String PROP_PLUGINNAME_DESC = "Name of the Plugin(s) to add a Parameter to";

  private static final String PROP_PARAMETER = "Parameter";
  private static final String PROP_PARAMETER_DFLT = "";
  private static final String PROP_PARAMETER_DESC = "The new Parameter";

  private Property propQuery;
  private Property propPluginName;
  private Property propParameter;

  public ParameterInsertionRecipe() {
    super("Parameter Insertion Recipe");
  }

  public ParameterInsertionRecipe(String name) {
    super(name);
  }

  public void initProperties() {
    propQuery = addRecipeQueryProperty(PROP_QUERY, PROP_QUERY_DFLT);
    propQuery.setToolTip(PROP_QUERY_DESC);

    propPluginName = addProperty(PROP_PLUGINNAME, PROP_PLUGINNAME_DFLT);
    propPluginName.setToolTip(PROP_PLUGINNAME_DESC);

    propParameter = addProperty(PROP_PARAMETER, PROP_PARAMETER_DFLT);
    propParameter.setToolTip(PROP_PARAMETER_DESC);

  }

  /**
   * Gets the name of the html help file for this component.
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  private Property addRecipeQueryProperty(String name, String dflt) {
    Property prop = addProperty(new RecipeQueryProperty(this, name, dflt));
    prop.setPropertyClass(String.class);
    return prop;
  }

  public ComponentData modifyComponentData(ComponentData data, PopulateDb pdb) {
    try {
      Set targets = pdb.executeQuery(propQuery.getValue().toString());
      modifyComponentData(data, pdb, targets);
    } catch (SQLException sqle) {
      if(log.isErrorEnabled()) {
        log.error("Exception", sqle);
      }
    }
    return data;
  }

  private void modifyComponentData(ComponentData data, PopulateDb pdb, Set targets)
    throws SQLException
  {
    // FIXME!!
    // This should ask PopulateDb to construct the alib id.
    // Also of course this isn't enough
    String pluginAlib = data.getName() + "|" + propPluginName.getValue().toString();
    // FIXME: This code does not check that the newly modified child is not now a duplicate within the Agent!

    // FIXME: Is the slot really NAME or CLASS or must it be the same?!!!
    if (targets.contains(pdb.getComponentAlibId(data))) {
      ComponentData[] children = data.getChildren();
      for (int i=0; i < children.length; i++ ) {        
	//if (children[i].getName().equals(pluginAlib)) {
	// Compare the plugins name, not the alib id, right?
	if (children[i].getAlibID() != null && children[i].getAlibID().equals(pluginAlib)) {
          if(log.isDebugEnabled()) {
            log.debug("Got match for pluginalibid: " + 
                      pluginAlib + " on child " + children[i].toString());
          }
          children[i].addParameter(propParameter.getValue().toString());
          continue;
        } else if (children[i].getClassName().equals(propPluginName.getValue().toString())) {
          if(log.isDebugEnabled()) {
            log.debug("Got match for plugin class: " + children[i].toString());
          }
	  // FIXME: If this agent has 2 plugins with the same class,
	  // which do I do? This currently does the first only
	  // FIXME: Make sure this parameter isnt already there?
          children[i].addParameter(propParameter.getValue().toString());
          continue;
        } else if (children[i].getName().equals(propPluginName.getValue().toString())) {
          if(log.isDebugEnabled()) {
            log.debug("Got match for plugin name: " + children[i].toString());
          }
	  // FIXME: If this agent has 2 plugins with the same name,
	  // which do I do? This currently does the first only
	  // FIXME: Make sure this parameter isnt already there?
          children[i].addParameter(propParameter.getValue().toString());
          continue;
        } else if (children[i].getName().equals(data.getName() + "|" + propPluginName.getValue().toString())) {
          if(log.isDebugEnabled()) {
            log.debug("Got match for plugin name with parent name in front: " + children[i].toString());
          }
	  // FIXME: If this agent has 2 plugins with the same name,
	  // which do I do? This currently does the first only
	  // FIXME: Make sure this parameter isnt already there?
          children[i].addParameter(propParameter.getValue().toString());
          continue;
	  // Here is the broken original version....
//  	} else if (children[i].getName().equals(pluginAlib)) {
//           if(log.isDebugEnabled()) {
//             log.debug("Got match AGAINST NAME(?) for pluginalibid: " + 
//                       pluginAlib + " on child " + children[i].toString());
//           }
//            children[i].addParameter(propParameter.getValue().toString());
//            break;
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

}// ParameterInsertionRecipe

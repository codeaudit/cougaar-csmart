/* 
 * <copyright>
 *  Copyright 2001,2002 BBNT Solutions, LLC
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
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.cdata.PGPropData;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentPropertyAdapter;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyEvent;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.core.property.range.IntegerRange;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.tools.csmart.society.AgentComponent;
import org.cougaar.util.log.Logger;

/**
 * Recipe to add an empty Agent to the society. 
 * Use the new Complete Agent Insertion recipe instead.
 */
public class AgentInsertionRecipe extends RecipeBase
  implements Serializable
{
  private static final String DESCRIPTION_RESOURCE_NAME = 
    "agent-insertion-recipe-description.html";

  private static final String PROP_NAME = "Agent Names";
  private static final String PROP_NAME_DFLT = "Agent";
  private static final String PROP_NAME_DESC = "Names for each Agent, seperated by a ','";

  private static final String PROP_CLASSNAME = "Class Name";
  private static final String PROP_CLASSNAME_DFLT = "org.cougaar.core.agent.ClusterImpl";
  private static final String PROP_CLASSNAME_DESC = "Class of the Agent";

  private static final String PROP_RELATIONCOUNT = "Number of Relationships";
  private static final Integer PROP_RELATIONCOUNT_DFLT = new Integer(0);
  private static final String PROP_RELATIONCOUNT_DESC = "Number of Relationships for this agent";

  private static final String PROP_RELATION = "Relationship";
  private static final String PROP_RELATION_DFLT = "";
  private static final String PROP_RELATION_DESC = "Agent Relationship";

  private static final String PROP_ROLE = "Role";
  private static final String PROP_ROLE_DFLT = "";
  private static final String PROP_ROLE_DESC = "Agent Role";

  private static final String PROP_TYPE = "Type Identification";
  private static final String PROP_TYPE_DFLT = "UTC/RTOrg";
  private static final String PROP_TYPE_DESC = "Type Identification";

  private static final String PROP_NOMENCLATURE = "Nomenclature";
  private static final String PROP_NOMENCLATURE_DFLT = "UTC/RTOrg";
  private static final String PROP_NOMENCLATURE_DESC = "Nomenclature";

  private static final String PROP_ALTTYPEID = "Alternate Type Identification";
  private static final String PROP_ALTTYPEID_DFLT = "";
  private static final String PROP_ALTTYPEID_DESC = "Alternate Type Identification";

  private static final String PROP_ASSETCLASS = "Asset Class";
  private static final String PROP_ASSETCLASS_DFLT = "MilitaryOrganization";
  private static final String PROP_ASSETCLASS_DESC = "Asset class for this organization";

  private Property propName;
  private Property propClassName;
  private Property propRelationCount;
  private Property[] propRelations = null;
  private Property[] propRoles = null;
  private Property propType;
  private Property propNomenclature;
  private Property propAltTypeId;
  private Property propAssetClass;

  /**
   * Creates a new <code>AgentInsertionRecipe</code> instance.
   *
   */
  public AgentInsertionRecipe() {
    this("Agent Component Recipe");
  }

  /**
   * Creates a new <code>AgentInsertionRecipe</code> instance.
   *
   * @param name Name of the Component
   */
  public AgentInsertionRecipe(String name) {
    super(name);
  }

  /**
   * Initialize any local Properties
   *
   */
  public void initProperties() {
    propName = addProperty(PROP_NAME, PROP_NAME_DFLT);
    propName.setToolTip(PROP_NAME_DESC);

    propClassName = addProperty(PROP_CLASSNAME, PROP_CLASSNAME_DFLT);
    propClassName.setToolTip(PROP_CLASSNAME_DESC);

    propType = addProperty(PROP_TYPE, PROP_TYPE_DFLT);
    propType.setToolTip(PROP_TYPE_DESC);

    propNomenclature = addProperty(PROP_NOMENCLATURE, PROP_NOMENCLATURE_DFLT);
    propNomenclature.setToolTip(PROP_NOMENCLATURE_DESC);

    propAltTypeId = addProperty(PROP_ALTTYPEID, PROP_ALTTYPEID_DFLT);
    propAltTypeId.setToolTip(PROP_ALTTYPEID_DESC);

    propAssetClass = addProperty(PROP_ASSETCLASS, PROP_ASSETCLASS_DFLT);
    propAssetClass.setToolTip(PROP_ASSETCLASS_DESC);

    propRelationCount = addProperty(PROP_RELATIONCOUNT, PROP_RELATIONCOUNT_DFLT);
    propRelationCount.addPropertyListener(new ConfigurableComponentPropertyAdapter() {
        public void propertyValueChanged(PropertyEvent e) {
          updateRelationCount((Integer)e.getProperty().getValue());
        }
      });
    propRelationCount.setToolTip(PROP_RELATIONCOUNT_DESC);
    propRelationCount.setAllowedValues(Collections.singleton(new IntegerRange(0, Integer.MAX_VALUE)));
   
  }

  /**
   * Gets the name of the html help file for this component.
   *
   * @return an <code>URL</code> value
   */
  public URL getDescription() {
    return getClass().getResource(DESCRIPTION_RESOURCE_NAME);
  }

  private Property addAgentQueryProperty(String name, String dflt) {
    Property prop = addProperty(new AgentQueryProperty(this, name, dflt));
    prop.setPropertyClass(String.class);
    return prop;
  }

  private void updateRelationCount(Integer newCount) {
    int count = newCount.intValue();

    // For now delete all variable props and start fresh.
    // Annoying for the user, but it works.
    if( propRelations != null && count != propRelations.length ) {
      for(int i=0; i < propRelations.length; i++) {
        removeProperty(propRelations[i]);
        removeProperty(propRoles[i]);
      }
    }

    propRelations = new Property[count];
    propRoles = new Property[count];

    // Note: If you feel you want to change the name of these
    // properties, see the "Hack Note" above.
    for(int i=0; i < count; i++) {
      propRelations[i] = addAgentQueryProperty("Relationship " + (i+1), "");
      ((Property)propRelations[i]).setToolTip("Agent to relate to for Relationship " + (i+1));

      propRoles[i] = addProperty("Role " + (i+1), "");
      ((Property)propRoles[i]).setToolTip("Role to play for Agent in Relationship " + (i+1));
    }      
  }

  /**
   * Get the name of this Recipe.
   *
   * @return a Recipe Name as a <code>String</code> value
   */
  public String getRecipeName() {
    return getShortName();
  }

  private void initAgents() {
    boolean childrenOK = true;
    StringTokenizer tokens = new StringTokenizer((String)propName.getValue(),","); 
    String[] agents = new String[tokens.countTokens()];
    for (int i=0; i<agents.length; i++) {
      agents[i] = tokens.nextToken();
    }
    int childCount = getChildCount();
    if (childCount != agents.length ) {
      childrenOK=false;
    } // end of if ()
    else {
      for (int i=0; i < agents.length; i++) {
        if ( !agents[i].equals(((BaseComponent)getChild(i)).getShortName()) ) {
          childrenOK=false;
          break;
        } // end of if ()        
      } // end of for ()             
    } // end of else
    
    if ( childrenOK) {
      return;
    } // end of if ()
        
    removeAllChildren();
    if(log.isDebugEnabled()) {
      log.debug("Length = " + agents.length);
    }
    for(int i=0; i < agents.length; i++) {
      InsertAgentComponent iAgent = new InsertAgentComponent(agents[i]);
      iAgent.initProperties(propRelations, propRoles);
      if(log.isDebugEnabled()) {
        log.debug("AddChild: " + agents[i]);
      }
      addChild(iAgent);
    }
  }

  /**
   * Gets an array of all agents created by this recipe.
   *
   * @see AgentComponent
   * @return an <code>AgentComponent[]</code> array of all agents
   */
  public AgentComponent[] getAgents() {
    initAgents();
    Collection agents = getDescendentsOfClass(AgentComponent.class);    
    return (AgentComponent[]) agents.toArray(new AgentComponent[agents.size()]);
  }

  /**
   * Adds any new data to the global <code>ComponentData</code> tree.
   * No existing data is modified in this method.
   * Warning: Assumes it is handed a ComponentData which contains
   * all Agents below it.
   *
   * @param data Pointer to the Global <code>ComponentData</code> tree
   * @return an updated <code>ComponentData</code> value
   */
  public ComponentData addComponentData(ComponentData data) {
    ComponentData[] children = data.getChildren();

    // Search from the bottom of the list up, agents
    // in recipes are inserted at the bottom of the list
    // This will result in a faster search and find.
    for(int i=children.length-1; i >= 0; i--) {
      ComponentData child = children[i];
        
      if(child.getType() == ComponentData.AGENT) {
        Iterator iter = ((Collection)getDescendentsOfClass(AgentComponent.class)).iterator();

        while(iter.hasNext()) {
          AgentComponent agent = (AgentComponent)iter.next();
          if(child.getName().equals(agent.getShortName().toString())) {
	    // This is the same agent: replace the old with the new
	    // to ensure get all new values
	    ComponentData nchild = new AgentComponentData();
	    nchild.setName(child.getName());
            nchild.setClassName((String)propClassName.getValue());
            nchild.setOwner(this);            
            nchild.addParameter(agent.getShortName().toString());
	    nchild.setAlibID(child.getAlibID());
	    // Shouldn't the parent equivalently be the data object?
	    nchild.setParent(child.getParent());

	    // FIXME: what about the alib ids for the plugins?
	    // Is it OK to try to over-write?
            nchild = addAssetData(nchild);
            nchild = agent.addComponentData(nchild);

	    // Replace the existing AgentComponentData
	    // with a new one, to ensure we replace all values
            int idx;
            if((idx = data.getChildIndex(child)) != -1 ) {
              data.setChild(idx, nchild);
            }
	    break; // we found the agent. Break out of the loop!
          }
        }
      } else if( child.getType() == ComponentData.NODE ||
                 child.getType() == ComponentData.HOST ) {
	// Recurse in case of hosts / nodes
        addComponentData(child);
      }
    }
    return data;
  }

  /**
   * Modifies the global <code>ComponentData</code> tree.
   * This method is free to make any modifications it needs
   * to the global tree, not just to it's own Component.
   * <br>
   * Currently, this component makes no modifications.
   *
   * @param data Pointer to the global <code>ComponentData</code>
   * @param pdb Access to the database via <code>PopulateDb</code> object
   * @return a modified <code>ComponentData</code> value
   */
  public ComponentData modifyComponentData(ComponentData data, PopulateDb pdb) {
    return data;
  }

  private ComponentData addAssetData(ComponentData data) {
    AgentAssetData assetData = new AgentAssetData((AgentComponentData)data);

    if(log.isDebugEnabled()) {
      log.debug("Adding Asset Data");
    }

    assetData.setType(AgentAssetData.ORG);
    assetData.setAssetClass(propAssetClass.getValue().toString());
    assetData.setUniqueID("UTC/RTOrg");
    if(propRelations != null ) {
      for(int i=0; i < propRelations.length; i++) {        
        RelationshipData rd = new RelationshipData();
	// supported is ALIB_ID = ClusterID
        String supported = (String)propRelations[i].getValue();
        String role = (String)propRoles[i].getValue();
        rd.setSupported(supported);
        rd.setRole(role);
        // Add the role to the asset as well.
        assetData.addRole(role);
        assetData.addRelationship(rd);
      }
    }

    assetData.addPropertyGroup(createTypeIdentificationPG());

    assetData.addPropertyGroup(createClusterPG());

    assetData.addPropertyGroup(createItemIdentificationPG());

    data.addAgentAssetData(assetData);
      
    return data;
  }

  private PropGroupData createClusterPG() {
    PropGroupData pgd = new PropGroupData(PropGroupData.CLUSTER);

    if(log.isDebugEnabled()) {
      log.debug("Creating ClusterPG");
    }
    
    PGPropData pgData = new PGPropData();
    pgData.setName("ClusterIdentifier");
    pgData.setType("ClusterIdentifier");
    pgData.setValue(propName.getValue().toString());
    pgd.addProperty(pgData);

    return pgd;
  }

  private PropGroupData createTypeIdentificationPG() {
    PropGroupData pgd = new PropGroupData(PropGroupData.TYPE_IDENTIFICATION);

    if(log.isDebugEnabled()) {
      log.debug("Creating TypeIDPG");
    }
    
    // Add Type Identification
    PGPropData pgData = new PGPropData();
    pgData.setName("TypeIdentification");
    pgData.setType("String");

    // FIXME: This causes the recipe to be modified when you try
    // to save an experiment or dump INIs
    if(propType.getValue().toString().equals(PROP_TYPE_DFLT)) {
      //      propType.setValue("UTC/" + propName.getValue().toString());
    }
    pgData.setValue(propType.getValue().toString());
    pgd.addProperty(pgData);

    // Add Nomenclature
    pgData = new PGPropData();
    pgData.setName("Nomenclature");
    pgData.setType("String");
    pgData.setValue(propNomenclature.getValue().toString());
    pgd.addProperty(pgData);
      
    // Add AltTypeIdentification
    pgData = new PGPropData();
    pgData.setName("AlternateTypeIdentification");
    pgData.setType("String");
    pgData.setValue(propAltTypeId.getValue().toString());
    pgd.addProperty(pgData);

    return pgd;
  }

  private PropGroupData createItemIdentificationPG() {
    PropGroupData pgd = new PropGroupData(PropGroupData.ITEM_IDENTIFICATION);

    if(log.isDebugEnabled()) {
      log.debug("Creating itemIdentificationPG");
    }
    PGPropData pgData = new PGPropData();
    pgData.setName("ItemIdentification");
    pgData.setType("String");
    pgData.setValue(propName.getValue().toString());
    pgd.addProperty(pgData);

    pgData = new PGPropData();
    pgData.setName("Nomenclature");
    pgData.setType("String");
    pgData.setValue(propName.getValue().toString());
    pgd.addProperty(pgData);

    return pgd;
  }

  // Simple Agent Component.
  class InsertAgentComponent extends ModifiableConfigurableComponent 
    implements AgentComponent, Serializable {

    private Property[] propRelations = null;
    private Property[] propRoles = null;

    public InsertAgentComponent() {
      this("InsertAgent");
    }

    public InsertAgentComponent(String name) {
      super(name);
    }

    public void initProperties(Property[] relations, Property[] roles) {
      this.initProperties();
      this.propRelations = relations;
      this.propRoles = roles;
    }

    public void initProperties() {
      // No Props to init.
    }

    public ComponentData addComponentData(ComponentData data) {
      if (this.getShortName().equals(data.getName())) {
	
	// Add the OrgData plugin.
	ComponentData plugin = new GenericComponentData();
	plugin.setType(ComponentData.PLUGIN);
	plugin.setName("org.cougaar.mlm.plugin.organization.OrgDataPlugin");
	plugin.setParent(data);
	plugin.setClassName("org.cougaar.mlm.plugin.organization.OrgDataPlugin");
	plugin.setOwner(this);
	data.addChild(plugin);
	
	// Add the OrgReport plugin.
	plugin = new GenericComponentData();
	plugin.setType(ComponentData.PLUGIN);
	plugin.setName("org.cougaar.mlm.plugin.organization.OrgReportPlugin");
	plugin.setParent(data);
	plugin.setClassName("org.cougaar.mlm.plugin.organization.OrgReportPlugin");
	plugin.setOwner(this);
	data.addChild(plugin);
      } else if (data.childCount() > 0) {
        // for each child, call this same method.
        ComponentData[] children = data.getChildren();
        for (int i = 0; i < children.length; i++) {
          data = addComponentData(children[i]);
        }
      }
      return data;
    }

    public ComponentData modifyComponentData(ComponentData data) {
      return data;
    }
        
    public boolean equals(Object o) {
      if (o instanceof AgentComponent) {
        AgentComponent that = (AgentComponent)o;
        if (!this.getShortName().equals(that.getShortName())  ) {
          return false;
        }     
        return true;
      }
      return false;
    }
  }  
}

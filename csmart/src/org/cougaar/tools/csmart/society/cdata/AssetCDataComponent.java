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
package org.cougaar.tools.csmart.society.cdata;

import java.util.Collection;
import java.util.Iterator;

import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AssetComponent;
import org.cougaar.tools.csmart.society.ContainerBase;
import org.cougaar.tools.csmart.society.PropGroupBase;
import org.cougaar.tools.csmart.society.RelationshipBase;

/**
 * ConfigurableComponent that represents an <code>AssetComponentData</code>
 */
public class AssetCDataComponent
  extends ModifiableConfigurableComponent
  implements AssetComponent {

  /** Property Definitions **/

  /** Type Property Definition **/
  public static final String PROP_TYPE = "Asset Type";

  /** Type Description Definition **/
  public static final String PROP_TYPE_DESC = "Type of Asset";

  /** Class Definition **/
  public static final String PROP_CLASS = "Asset Class";

  /** Class Description Definition **/
  public static final String PROP_CLASS_DESC = "Class of the Asset";

  /** UID Definition **/
  public static final String PROP_UID = "UID";

  /** UID Description Definition **/
  public static final String PROP_UID_DESC = "UID of the Asset";

  /** Unit Name Definition **/
  public static final String PROP_UNITNAME = "Unit Name";

  /** Unit Name Description Definition **/
  public static final String PROP_UNITNAME_DESC = "Unit Name of the Asset";

  /** UIC Definition **/
  public static final String PROP_UIC = "UIC";

  /** UID Description Definition **/
  public static final String PROP_UIC_DESC = "UIC of the Asset";

  private Property propAssetType;
  private Property propAssetClass;
  private Property propUniqueID;
  private Property propUnitName;
  private Property propUIC;
  private AgentAssetData createdFromData;

  /**
   * Creates a new <code>AssetCDataComponent</code> instance.
   *
   * @param data <code>AgentAssetData</code> to create from
   */
  public AssetCDataComponent(AgentAssetData data) {
    super("AssetData");
    createdFromData = data;
  }

  /**
   * Init properties from AgentAssetData
   */

  public void initProperties() {
    propAssetType = addProperty(PROP_TYPE, 
                                new Integer(createdFromData.getType()));
    propAssetType.setToolTip(PROP_TYPE_DESC);
    propAssetClass = addProperty(PROP_CLASS, createdFromData.getAssetClass());
    propAssetClass.setToolTip(PROP_CLASS_DESC);
    if (createdFromData.getUniqueID() != null) {
      propUniqueID = addProperty(PROP_UID, createdFromData.getUniqueID());
      propUniqueID.setToolTip(PROP_UID_DESC);
    }
    // Unit name is allowed to be null, if it is, give an empty string.
    String unitname = (createdFromData.getUnitName() == null) ? 
      "" : createdFromData.getUnitName();
    propUnitName = addProperty(PROP_UNITNAME, unitname);
    propUnitName.setToolTip(PROP_UNITNAME_DESC);
    
    if(createdFromData.getUIC() != null) {
      propUIC = addProperty(PROP_UIC, createdFromData.getUIC());
      propUIC.setToolTip(PROP_UIC_DESC);
    }

    addPropGroups(createdFromData);
    addRelationships(createdFromData.getRelationshipData());
  }

  /**
   * Add component data for asset properties, relationships,
   * and property groups. Only additions are made in this method,
   * no modifications to existing data.
   *
   * @param data Pointer to the global <code>ComponentData</code> tree
   * @return an updated <code>ComponentData</code> value
   */
  public ComponentData addComponentData(ComponentData data) {
    AgentAssetData assetData = new AgentAssetData((AgentComponentData)data);
    assetData.setType(((Integer)propAssetType.getValue()).intValue());
    assetData.setAssetClass((String)propAssetClass.getValue());
    assetData.setUniqueID((String)propUniqueID.getValue());
    assetData.setUnitName((String)propUnitName.getValue());
    assetData.setUIC((String)propUIC.getValue());

    // Add Relationships.
    Iterator iter = 
      ((Collection)getDescendentsOfClass(ContainerBase.class)).iterator();
    while(iter.hasNext()) {
      ContainerBase container = (ContainerBase)iter.next();
      if(container.getShortName().equals("Relationships")) {
        for(int i=0; i < container.getChildCount(); i++) {
          RelationshipBase rel = (RelationshipBase) container.getChild(i);
          RelationshipData rData = new RelationshipData();
	  if (rel != null) {
	    if (rel.getProperty(PROP_TYPE) == null) {
	      if (log.isErrorEnabled()) {
		log.error(this + " got null relationship type in rel #" + i);
	      }
	    }
	    rData.setType((String)rel.getProperty(PROP_TYPE).getValue());
	    rData.setRole((String)rel.getProperty(RelationshipBase.PROP_ROLE).getValue());
	    rData.setItemId((String)rel.getProperty(RelationshipBase.PROP_ITEM).getValue());
            rData.setTypeId((String)rel.getProperty(RelationshipBase.PROP_TYPEID).getValue());
	    rData.setSupported((String)rel.getProperty(RelationshipBase.PROP_SUPPORTED).getValue());
	    assetData.addRelationship(rData);
	  } else {
	    // null relationship?
	    if (log.isErrorEnabled()) {
	      log.error(this + " Got null relationship #" + i);
	    }
	  }
        }
      }
    }

    // Add Property Groups.
    iter = 
      ((Collection)getDescendentsOfClass(ContainerBase.class)).iterator();
    while(iter.hasNext()) {
      ContainerBase container = (ContainerBase)iter.next();
      if(container.getShortName().equals("Property Groups")) {
        for(int i=0; i < container.getChildCount(); i++) {
          PropGroupBase pg = (PropGroupBase)container.getChild(i);
          assetData.addPropertyGroup(pg.getPropGroupData());
        }
      }
    }

    data.addAgentAssetData(assetData);
    return data;
  }

  private void addRelationships(RelationshipData[] rel) {
    ContainerBase relContainer = new ContainerBase("Relationships");
    relContainer.initProperties();
    addChild(relContainer);
    for(int i=0; i < rel.length; i++) {
      RelationshipBase newR = new RelationshipBase(rel[i]);
      newR.initProperties();
      relContainer.addChild(newR);
    }
  }

  private void addPropGroups(AgentAssetData aad) {
    ContainerBase pgContainer = new ContainerBase("Property Groups");
    pgContainer.initProperties();
    addChild(pgContainer);
    Iterator iter = aad.getPropGroupsIterator();
    while(iter.hasNext()) {
      PropGroupData pgd = (PropGroupData)iter.next();
      PropGroupBase newPG = new PropGroupBase(pgd);
      if(log.isDebugEnabled()) {
        log.debug("Adding: " + pgd.getName());
      }
      newPG.initProperties();
      pgContainer.addChild(newPG);
    }
  }

}

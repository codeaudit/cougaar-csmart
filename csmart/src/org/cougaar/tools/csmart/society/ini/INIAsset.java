/**
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
 *  </copyright>
 */
package org.cougaar.tools.csmart.society.ini;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.util.PrototypeParser;

/**
 * INIAsset.java
 *
 *
 * Created: Thu Feb 21 15:55:00 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class INIAsset extends ConfigurableComponent implements Serializable {

  /** Agent Name Property Definitions **/
  public static final String PROP_TYPE = "Asset Type";
  public static final String PROP_TYPE_DESC = "Type of Asset";

  /** Agent Name Property Definitions **/
  public static final String PROP_CLASS = "Asset Class";
  public static final String PROP_CLASS_DESC = "Class of the Asset";

  /** Agent Name Property Definitions **/
  public static final String PROP_UID = "UID";
  public static final String PROP_UID_DESC = "UID of the Asset";

  /** Agent Name Property Definitions **/
  public static final String PROP_UNITNAME = "Unit Name";
  public static final String PROP_UNITNAME_DESC = "Unit Name of the Asset";

  /** Agent Name Property Definitions **/
  public static final String PROP_UIC = "UIC";
  public static final String PROP_UIC_DESC = "UIC of the Asset";

  private String agentName;

  private Property propAssetType;
  private Property propAssetClass;
  private Property propUniqueID;
  private Property propUnitName;
  private Property propUIC;
  
  public INIAsset (String agentName){
    super("AssetData");
    this.agentName = agentName;
  }

  public void initProperties() {
    AgentAssetData aad = PrototypeParser.parse(agentName);

    // This will be an int, need to convert.
    propAssetType = addProperty(PROP_TYPE, new Integer(aad.getType()));
    propAssetType.setToolTip(PROP_TYPE_DESC);

    propAssetClass = addProperty(PROP_CLASS, aad.getAssetClass());
    propAssetClass.setToolTip(PROP_CLASS_DESC);

    propUniqueID = addProperty(PROP_UID, aad.getUniqueID());
    propUniqueID.setToolTip(PROP_UID_DESC);
    
    // Unit name is allowed to be null, if it is, give an empty string.
    String unitname = (aad.getUnitName() == null) ? "" : aad.getUnitName();
    propUnitName = addProperty(PROP_UNITNAME, unitname);
    propUnitName.setToolTip(PROP_UNITNAME_DESC);

    propUIC = addProperty(PROP_UIC, aad.getUIC());
    propUIC.setToolTip(PROP_UIC_DESC);

    addPropGroups(aad);
    addRelationships(aad.getRelationshipData());
  }

  public ComponentData addComponentData(ComponentData data) {
    AgentAssetData assetData = new AgentAssetData((AgentComponentData)data);
    assetData.setType(((Integer)propAssetType.getValue()).intValue());
    assetData.setAssetClass((String)propAssetClass.getValue());
    assetData.setUniqueID((String)propUniqueID.getValue());
    assetData.setUnitName((String)propUnitName.getValue());
    assetData.setUIC((String)propUIC.getValue());

    // Add Relationships.
    Iterator iter = ((Collection)getDescendentsOfClass(INIContainer.class)).iterator();
    while(iter.hasNext()) {
      INIContainer container = (INIContainer)iter.next();
      if(container.getShortName().equals("Relationships")) {
        for(int i=0; i < container.getChildCount(); i++) {
          INIRelationship rel = (INIRelationship) container.getChild(i);
          RelationshipData rData = new RelationshipData();
          rData.setType((String)rel.getProperty(PROP_TYPE).getValue());
          rData.setRole((String)rel.getProperty(INIRelationship.PROP_ROLE).getValue());
          rData.setItem((String)rel.getProperty(INIRelationship.PROP_ITEM).getValue());
          assetData.addRelationship(rData);
        }
      }
    }

    // Add Property Groups.
    iter = ((Collection)getDescendentsOfClass(INIPropGroup.class)).iterator();
    while(iter.hasNext()) {
      INIPropGroup pg = (INIPropGroup)iter.next();
      assetData.addPropertyGroup(pg.getPropGroupData());
    }

    data.addAgentAssetData(assetData);
    return data;
  }

  public ComponentData modifyComponentData(ComponentData data) {
    return data;
  }

  private void addRelationships(RelationshipData[] rel) {
    INIContainer relContainer = new INIContainer("Relationships");
    relContainer.initProperties();
    addChild(relContainer);
    for(int i=0; i < rel.length; i++) {
      INIRelationship newR = new INIRelationship(rel[i], i);
      newR.initProperties();
      relContainer.addChild(newR);
    }
  }

  private void addPropGroups(AgentAssetData aad) {
    Iterator iter = aad.getPropGroupsIterator();
    while(iter.hasNext()) {
      PropGroupData pgd = (PropGroupData)iter.next();
      INIPropGroup newPG = new INIPropGroup(pgd);
      if(log.isDebugEnabled()) {
        log.debug("Adding: " + pgd.getName());
      }
      newPG.initProperties();
      addChild(newPG);
    }
  }

}// INIAsset

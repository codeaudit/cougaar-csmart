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
package org.cougaar.tools.csmart.society.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
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
import org.cougaar.tools.csmart.util.PrototypeParser;
import org.cougaar.util.TimeSpan;

public class AssetUIComponent
  extends ModifiableConfigurableComponent
  implements AssetComponent {

  //  private Property propAssetType;
  private Property propAssetClass;
  private Property propUniqueID;
  private Property propUnitName;
  private Property propUIC;

  public AssetUIComponent() {
    super("AssetData");
  }

  public void initProperties() {
    //    propAssetType = addProperty(AssetComponent.PROP_TYPE, new Integer(0));
    //    propAssetType.setToolTip(PROP_TYPE_DESC);
    propAssetClass = addProperty(AssetComponent.PROP_CLASS, "MilitaryOrganization");
    propAssetClass.setToolTip(PROP_CLASS_DESC);
    propUniqueID = addProperty(AssetComponent.PROP_UID, "UTC/RTOrg");
    propUniqueID.setToolTip(PROP_UID_DESC);
    propUnitName = addProperty(AssetComponent.PROP_UNITNAME, "");
    propUnitName.setToolTip(PROP_UNITNAME_DESC);
    propUIC = addProperty(PROP_UIC, "");
    propUIC.setToolTip(PROP_UIC_DESC);
  }

  /**
   * Add component data for asset properties, relationships,
   * and property groups.
   */
  public ComponentData addComponentData(ComponentData data) {
    AgentAssetData assetData = new AgentAssetData((AgentComponentData)data);

    // String agent = data.getName();

    // FIXME: IF asset class is null or empty, perhaps abort? Or
    // fill in the agent name inside these?
    // Of course, doing it in the addComponentData is bad,
    // cause it modifies the Component

    //    assetData.setType(((Integer)propAssetType.getValue()).intValue());
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
          rData.setType((String)rel.getProperty(RelationshipBase.PROP_TYPE).getValue());
          rData.setRole((String)rel.getProperty(RelationshipBase.PROP_ROLE).getValue());
          rData.setItemId((String)rel.getProperty(RelationshipBase.PROP_ITEM).getValue());
          rData.setTypeId((String)rel.getProperty(RelationshipBase.PROP_TYPEID).getValue());
          rData.setSupported((String)rel.getProperty(RelationshipBase.PROP_SUPPORTED).getValue());

          DateFormat df = DateFormat.getInstance();
          try {
            Date start = df.parse((String)rel.getProperty(RelationshipBase.PROP_STARTTIME).getValue());
            Date end = df.parse((String)rel.getProperty(RelationshipBase.PROP_STOPTIME).getValue());
            rData.setStartTime(start.getTime());
            rData.setEndTime(end.getTime());
          } catch(ParseException pe) {
            if(log.isErrorEnabled()) {
              log.error("Caught Exception parsing Date, using default dates.", pe);
            }
            rData.setStartTime(TimeSpan.MIN_VALUE);
            rData.setEndTime(TimeSpan.MAX_VALUE);
          }
          assetData.addRelationship(rData);
        }
      }
    }

    // FIXME: Perhaps check that ClusterPG (MessageAddress),
    // ItemIdentificationPG (ItemIdentifiation), TypeIdentificationPG (TypeIdentification)
    // are, at a minimum, among those filled in?
    // What would I do though if they're not?

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

}

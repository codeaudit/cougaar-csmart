/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society.ui;

import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AssetComponent;
import org.cougaar.tools.csmart.society.ContainerBase;
import org.cougaar.tools.csmart.society.PropGroupBase;
import org.cougaar.tools.csmart.society.RelationshipBase;
import org.cougaar.util.TimeSpan;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

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

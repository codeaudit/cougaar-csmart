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
package org.cougaar.tools.csmart.society.cdata;


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
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.TimeSpan;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * ConfigurableComponent that represents an <code>AssetComponentData</code>
 */
public class AssetCDataComponent
  extends ModifiableConfigurableComponent
  implements AssetComponent {

  /** Property Definitions **/

  /** Type Property Definition **/
  //  public static final String PROP_TYPE = "Asset Type";

  /** Type Description Definition **/
  //  public static final String PROP_TYPE_DESC = "Type of Asset";

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

  //  private Property propAssetType;
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
    //    propAssetType = addProperty(PROP_TYPE, 
    //                                new Integer(createdFromData.getType()));
    //    propAssetType.setToolTip(PROP_TYPE_DESC);
    if (createdFromData == null)
      return;
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
	  if (rel != null) {
	    if (rel.getProperty(RelationshipBase.PROP_TYPE) == null) {
	      if (log.isErrorEnabled()) {
		log.error(this + " using CSMART " + CSMART.writeDebug() + " got null relationship type for " + data.getName() + " in rel #" + i + " with role=" + rel.getProperty(RelationshipBase.PROP_ROLE) + ", ItemId=" + rel.getProperty(RelationshipBase.PROP_ITEM) + ", please report this on bug# 1304");
	      }
	    } else {
	      rData.setType((String)rel.getProperty(RelationshipBase.PROP_TYPE).getValue());
	    }
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

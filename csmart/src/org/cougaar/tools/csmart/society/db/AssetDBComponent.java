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
package org.cougaar.tools.csmart.society.db;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.*;
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AssetComponent;
import org.cougaar.tools.csmart.society.ContainerBase;
import org.cougaar.tools.csmart.society.PropGroupBase;
import org.cougaar.tools.csmart.society.RelationshipBase;

public class AssetDBComponent
  extends ModifiableConfigurableComponent
  implements AssetComponent {

  // query names
  private static final String QUERY_AGENT_ASSET_CLASS = "queryAgentAssetClass";
  private static final String QUERY_AGENT_RELATIONS = "queryAgentRelationships";

  /** Property Definitions **/
  public static final String PROP_TYPE = "Asset Type";
  public static final String PROP_TYPE_DESC = "Type of Asset";
  public static final String PROP_CLASS = "Asset Class";
  public static final String PROP_CLASS_DESC = "Class of the Asset";
  public static final String PROP_UID = "UID";
  public static final String PROP_UID_DESC = "UID of the Asset";
  public static final String PROP_UNITNAME = "Unit Name";
  public static final String PROP_UNITNAME_DESC = "Unit Name of the Asset";
  public static final String PROP_UIC = "UIC";
  public static final String PROP_UIC_DESC = "UIC of the Asset";

  private Map substitutions = new HashMap();
  private Property propAssetType;
  private Property propAssetClass;
  private Property propUniqueID;
  private Property propUnitName;
  private Property propUIC;
  private String agentName;
  private List assemblyID;

  public AssetDBComponent(String agentName, List assemblyID) {
    super("AssetData");
    this.agentName = agentName;
    this.assemblyID = assemblyID;
  }

  public void initProperties() {
    propAssetType = addProperty(PROP_TYPE, new Integer(AgentAssetData.ORG));
    propAssetType.setToolTip(PROP_TYPE_DESC);

    propAssetClass = addProperty(PROP_CLASS, queryOrgClass());
    propAssetClass.setToolTip(PROP_CLASS_DESC);

    // TODO: where do you get the UID?
    propUniqueID = addProperty(PROP_UID, "");
    propUniqueID.setToolTip(PROP_UID_DESC);
    
    propUnitName = addProperty(PROP_UNITNAME, "");
    propUnitName.setToolTip(PROP_UNITNAME_DESC);

    propUIC = addProperty(PROP_UIC, "UIC/" + agentName);
    propUIC.setToolTip(PROP_UIC_DESC);

    //    addPropGroups();
    RelationshipData[] relationshipData = getRelationshipData();
    addRelationships(relationshipData);
  }

  /**
   * Add component data for asset properties, relationships,
   * and property groups.
   * TODO: same as AssetFileComponent: should this be moved to a base class?
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
          rData.setType((String)rel.getProperty(PROP_TYPE).getValue());
          rData.setRole((String)rel.getProperty(RelationshipBase.PROP_ROLE).getValue());
          rData.setItem((String)rel.getProperty(RelationshipBase.PROP_ITEM).getValue());
          assetData.addRelationship(rData);
        }
      }
    }

    // Add Property Groups.
    iter = 
      ((Collection)getDescendentsOfClass(PropGroupBase.class)).iterator();
    while(iter.hasNext()) {
      PropGroupBase pg = (PropGroupBase)iter.next();
      assetData.addPropertyGroup(pg.getPropGroupData());
    }

    data.addAgentAssetData(assetData);
    return data;
  }

  private RelationshipData[] getRelationshipData() {
    // Get list of assemblies for use in query, ignoring CMT assemblies
    String assemblyMatch = DBUtils.getListMatch(assemblyID, "CMT");

    if (assemblyMatch != null) {
      substitutions.put(":assemblyMatch", assemblyMatch);
      substitutions.put(":agent_name", agentName);
    }

    // creates relationships by creating RelationshipData
    // from the database and passing that as an argument
    // to RelationshipBase
    ArrayList relationshipData = new ArrayList();
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Statement stmt = conn.createStatement();	
        String query = DBUtils.getQuery(QUERY_AGENT_RELATIONS, substitutions);
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
          RelationshipData rd = new RelationshipData();
          String supported = rs.getString(1);
          String role = rs.getString(2);
          Timestamp startDate = rs.getTimestamp(3);
          Timestamp endDate = rs.getTimestamp(4);
          //          rd.setType("");
          //          rd.setItem("");
          //          rd.setSupported(supported);
          //          rd.setRole(role);
          // TODO: is this correct handling of relationship data?
          // start new code
          rd.setItem(supported);
          rd.setSupported(supported);
          if (role.equals("Subordinate")) {
            rd.setRole("");
            rd.setType("Subordinate");
          } else {
            rd.setRole(role);
            rd.setType("Supporting");
          }
          // end new code
          if (startDate != null) {
            rd.setStartTime(startDate.getTime());
          }
          if (endDate != null) rd.setEndTime(endDate.getTime());
          relationshipData.add(rd);
        }
        rs.close();
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
      throw new RuntimeException("Error" + e);
    }
    return (RelationshipData[])relationshipData.toArray(new RelationshipData[relationshipData.size()]);
  }

  private void addRelationships(RelationshipData[] rel) {
    ContainerBase relContainer = new ContainerBase("Relationships");
    relContainer.initProperties();
    addChild(relContainer);
    for(int i=0; i < rel.length; i++) {
      RelationshipBase newR = new RelationshipBase(rel[i], i);
      newR.initProperties();
      relContainer.addChild(newR);
    }
  }

//    private void addPropGroups(AgentAssetData aad) {
//      Iterator iter = aad.getPropGroupsIterator();
//      while(iter.hasNext()) {
//        PropGroupData pgd = (PropGroupData)iter.next();
//        PropGroupBase newPG = new PropGroupBase(pgd);
//        if(log.isDebugEnabled()) {
//          log.debug("Adding: " + pgd.getName());
//        }
//        newPG.initProperties();
//        addChild(newPG);
//      }
//    }

  private String queryOrgClass() {
    String orgClass = null;
    substitutions.put(":agent_name", agentName);
    try {
      Connection conn = DBUtils.getConnection();
      try {
	Statement stmt = conn.createStatement();	
	String query = DBUtils.getQuery(QUERY_AGENT_ASSET_CLASS, substitutions);
	ResultSet rs = stmt.executeQuery(query);
	while (rs.next()) {
	  orgClass = rs.getString(1);	  
	}
	rs.close();
	stmt.close();

      } finally {
	conn.close();
      }
    } catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception", e);
      }
      throw new RuntimeException("Error" + e);
    }
    return orgClass;
  }
}

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

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.*;
import org.cougaar.tools.csmart.core.cdata.AgentAssetData;
import org.cougaar.tools.csmart.core.cdata.AgentComponentData;
import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.PGPropData;
import org.cougaar.tools.csmart.core.cdata.PGPropMultiVal;
import org.cougaar.tools.csmart.core.cdata.PropGroupData;
import org.cougaar.tools.csmart.core.cdata.RelationshipData;
import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.society.AssetComponent;
import org.cougaar.tools.csmart.society.ContainerBase;
import org.cougaar.tools.csmart.society.PropGroupBase;
import org.cougaar.tools.csmart.society.PropGroupComponent;
import org.cougaar.tools.csmart.society.RelationshipBase;

public class AssetDBComponent
  extends ModifiableConfigurableComponent
  implements AssetComponent {

  // query names
  private static final String QUERY_AGENT_ASSET_CLASS = "queryAgentAssetClass";
  private static final String QUERY_AGENT_RELATIONS = "queryAgentRelationships";
  private static final String QUERY_PG_ID = "queryPGId";
  private static final String QUERY_PG_ATTRS = "queryPGAttrs";
  private static final String QUERY_PG_VALUES = "queryPGValues";

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
  private String assemblyID;
  private String assemblyMatch;
  private Hashtable pgAttributes = new Hashtable();
  private Hashtable propertyGroups = new Hashtable();

  public AssetDBComponent(String agentName, String assemblyID) {
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

    getAssemblies(); // for use in database lookups
    addPropGroups(); // add property groups from the database
    addRelationships(getRelationshipData()); // add relationships from database
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
          rData.setType((String)rel.getProperty(RelationshipBase.PROP_TYPE).getValue());
          rData.setRole((String)rel.getProperty(RelationshipBase.PROP_ROLE).getValue());
          rData.setItemId((String)rel.getProperty(RelationshipBase.PROP_ITEM).getValue());
          rData.setSupported((String)rel.getProperty(RelationshipBase.PROP_SUPPORTED).getValue());

	  // FIXME Where are start & end time???
          assetData.addRelationship(rData);
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

  private void getAssemblies() {
    // Get list of assemblies for use in query, ignoring CMT assemblies
    assemblyMatch = DBUtils.getListMatch(assemblyID, "CMT");

    if (assemblyMatch != null) {
      substitutions.put(":assemblyMatch", assemblyMatch);
      substitutions.put(":agent_name", agentName);
    }
  }

  private RelationshipData[] getRelationshipData() {
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
          rd.setItemId(supported);
          rd.setSupported(supported);
          if (role.equals("Subordinate")) {
            rd.setRole("");
            rd.setType("Subordinate");
          } else {
            rd.setRole(role);
            rd.setType("Supporting");
          }
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
      RelationshipBase newR = new RelationshipBase(rel[i]);
      newR.initProperties();
      relContainer.addChild(newR);
    }
  }

  private void addPropGroups() {
    String pgName = "";
    String pgAttrName = "";
    String pgAttrType = "";
    String pgValue = "";
    String pgAggregateType = "";
    Timestamp pgStartDate;
    Timestamp pgEndDate;
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Statement stmt = conn.createStatement();	
        // get the propery group attribute lib ids for this agent
        ArrayList pgAttrLibIds = new ArrayList();
        String query = DBUtils.getQuery(QUERY_PG_ID, substitutions);
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
          pgAttrLibIds.add(rs.getString(1));
        }
        // get pg name and attribute names and types for each property group
        for (int i = 0; i < pgAttrLibIds.size(); i++) {
          substitutions.put(":pgAttrLibId", (String)pgAttrLibIds.get(i));
          query = DBUtils.getQuery(QUERY_PG_ATTRS, substitutions);
          rs = stmt.executeQuery(query);
          while (rs.next()) {
            pgName = rs.getString(1);
            pgAttrName = rs.getString(2);
            pgAttrType = rs.getString(3);
            pgAggregateType = rs.getString(4);
          }
          pgAttributes.put((String)pgAttrLibIds.get(i),
                           new PGAttr(pgName, pgAttrName, 
                                      pgAttrType, pgAggregateType));
        }
        // get values for each attribute in each property group
        PGPropMultiVal multiValueData = null;
        for (int i = 0; i < pgAttrLibIds.size(); i++) {
          String pgAttrLibId = (String)pgAttrLibIds.get(i);
          substitutions.put(":pgAttrLibId", pgAttrLibId);
          PGAttr pgAttr = (PGAttr)pgAttributes.get(pgAttrLibId);
          String attrType = pgAttr.getAttrType();
          String aggregateType = pgAttr.getAggregateType();
          query = DBUtils.getQuery(QUERY_PG_VALUES, substitutions);
          rs = stmt.executeQuery(query);
          while (rs.next()) {
            if (aggregateType.equals("SINGLE")) {
              pgValue = rs.getString(1);
              pgStartDate = rs.getTimestamp(2);
              pgEndDate = rs.getTimestamp(3);
              addPGAttributeData(pgAttrLibId, pgValue, pgStartDate, pgEndDate);
            } else {
              pgValue = rs.getString(1);
              pgStartDate = rs.getTimestamp(2);
              pgEndDate = rs.getTimestamp(3);
              if (multiValueData == null) 
                multiValueData = new PGPropMultiVal();
              multiValueData.addValue(pgValue);
            }
          }
          if (multiValueData != null) {
            addPGAttributeData(pgAttrLibId, multiValueData, null, null);
            multiValueData = null;
          }
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
    // after creating all the property group data
    // create the property groups, initialize them and add them as children
    ContainerBase pgContainer = new ContainerBase("Property Groups");
    pgContainer.initProperties();
    addChild(pgContainer);
    Iterator propGroupData = propertyGroups.values().iterator();
    while (propGroupData.hasNext()) {
      PropGroupData pgd = (PropGroupData)propGroupData.next();
      PropGroupComponent newPG = new PropGroupBase(pgd);
      newPG.initProperties();
      pgContainer.addChild(newPG);
    }
  }

  /**
   * Creates PropGroupData and PGPropData
   * for each property group and attribute respectively.
   * TODO: save start and end dates?
   */

  private void addPGAttributeData(String attrLibId, Object value,
                                  Timestamp startDate, Timestamp endDate) {
    PGAttr pgAttr = (PGAttr)pgAttributes.get(attrLibId);
    PropGroupData pgd = (PropGroupData)propertyGroups.get(pgAttr.getName());
    if (pgd == null) {
      pgd = new PropGroupData(pgAttr.getName());
      propertyGroups.put(pgAttr.getName(), pgd);
    }
    PGPropData pgPropData = new PGPropData();
    pgPropData.setName(pgAttr.getAttrName());
    String aggregateType = pgAttr.getAggregateType();
    if (aggregateType.equals("SINGLE"))
      pgPropData.setType(pgAttr.getAttrType());
    else
      pgPropData.setType(aggregateType, pgAttr.getAttrType());
    pgPropData.setValue(value);
    pgd.addProperty(pgPropData);
  }

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

  /**
   * Information on a property group attribute.
   */

  class PGAttr implements Serializable {
    String name;
    String attrName;
    String attrType;
    String aggregateType;

    public PGAttr(String name, String attrName, 
                  String attrType, String aggregateType) {
      this.name = name;
      this.attrName = attrName;
      this.attrType = attrType;
      this.aggregateType = aggregateType;
    }

    public String getName() {
      return name;
    }

    public String getAttrName() {
      return attrName;
    }

    public String getAttrType() {
      return attrType;
    }

    public String getAggregateType() {
      return aggregateType;
    }
  }
}

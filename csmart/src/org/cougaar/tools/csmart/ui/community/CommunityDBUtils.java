/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.community;

import org.cougaar.tools.csmart.core.db.DBUtils;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Methods for interfacing to database and supporting community displays.
 * These methods do not modify the community table display.
 */

public class CommunityDBUtils {
  private static final String GET_COMMUNITIES_QUERY = "queryCommunities";
  private static final String GET_MY_COMMUNITIES_QUERY = "queryMyCommunities";
  private static final String GET_ENTITIES_QUERY = "queryEntities";
  private static final String GET_ENTITY_TYPE_QUERY = "queryEntityType";
  private static final String INSERT_COMMUNITY_INFO_QUERY = 
    "queryInsertCommunityInfo";
  private static final String INSERT_COMMUNITY_ATTRIBUTE_QUERY = 
    "queryInsertCommunityAttribute";
  private static final String INSERT_ENTITY_INFO_QUERY = 
    "queryInsertEntityInfo";
  private static final String INSERT_ENTITY_ATTRIBUTE_QUERY = 
    "queryInsertEntityAttribute";
  private static final String IS_COMMUNITY_IN_USE_QUERY = 
    "queryIsCommunityInUse";
  private static final String UPDATE_COMMUNITY_ATTRIBUTE_ID_QUERY =
    "queryUpdateCommunityAttributeId";
  private static final String UPDATE_COMMUNITY_ATTRIBUTE_VALUE_QUERY =
    "queryUpdateCommunityAttributeValue";
  private static final String UPDATE_ENTITY_ATTRIBUTE_ID_QUERY =
    "queryUpdateEntityAttributeId";
  private static final String UPDATE_ENTITY_ATTRIBUTE_VALUE_QUERY =
    "queryUpdateEntityAttributeValue";
  private static final String DELETE_COMMUNITY_INFO_QUERY = 
    "queryDeleteCommunityInfo";
  private static final String DELETE_ENTITY_INFO_QUERY = 
    "queryDeleteEntityInfo";
  private static final String DELETE_COMMUNITY_ATTRIBUTE_QUERY =
    "queryDeleteCommunityAttribute";
  private static final String DELETE_ENTITY_ATTRIBUTE_QUERY =
    "queryDeleteEntityAttribute";

  private static ArrayList doQuery(String query, Map substitutions) {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.ui.community.CommunityDBUtils");
    ArrayList results = new ArrayList();
    try {
      Connection conn = DBUtils.getConnection();
      try {
        Statement stmt = conn.createStatement();
        query = DBUtils.getQuery(query, substitutions);
        // execute query that may or may not return results
        if (stmt.execute(query)) {
          ResultSet rs = stmt.getResultSet();
          while(rs.next()) 
            results.add(rs.getString(1));
          rs.close();
        }
        stmt.close();
      } finally {
        conn.close();
      }
    } catch (SQLException se) {
      if (se.toString().indexOf("Duplicate") != -1 || se.toString().indexOf("Invalid argument value") != -1)
	throw new IllegalArgumentException(se.toString());
      else if (log.isErrorEnabled()) {
        log.error("Caught SQL exception executing query: " + query, se);
      }
    } 
    if (results == null || results.isEmpty() && log.isDebugEnabled())
      log.debug("doQuery got no results for " + query);
    return results;
  }

  /**
   * Get all known community IDs
   **/
  public static ArrayList getCommunities() {
    ArrayList communityIds = doQuery(GET_COMMUNITIES_QUERY, new HashMap());
    Collections.sort(communityIds);
    return communityIds;
  }

  /**
   * Get all community IDs for this experiment
   **/
  public static ArrayList getCommunitiesForExperiment(String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    ArrayList communityIds = doQuery(GET_MY_COMMUNITIES_QUERY, substitutions);
    Collections.sort(communityIds);
    return communityIds;
  }

  /**
   * Get all Entity IDs in the given community
   *
   * @param communityId a <code>String</code> value
   * @param assemblyId a <code>String</code> Assembly to limit ourselves to
   * @return an <code>ArrayList</code> value
   */
  public static ArrayList getEntities(String communityId, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    ArrayList entityIds = doQuery(GET_ENTITIES_QUERY, substitutions);
    Collections.sort(entityIds);
    return entityIds;
  }

  /**
   *
   * @param entityId a <code>String</code> value
   * @param assemblyId a <code>String</code> Assembly to limit ourselves to
   * @return a <code>String</code> value
   */
  public static String getEntityType(String entityId, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":entity_id", entityId);
    ArrayList results = doQuery(GET_ENTITY_TYPE_QUERY, substitutions);
    if (results.size() != 0)
      return (String)results.get(0);
    return "Entity"; // default
  }

  /**
   *
   * @param communityId a <code>String</code> value
   * @param assemblyId a <code>String</code> Assembly to limit ourselves to
   * @return a <code>boolean</code> value
   */
  public static boolean isCommunityInUse(String communityId, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    ArrayList results = doQuery(IS_COMMUNITY_IN_USE_QUERY, substitutions);
    return (results.size() != 0);
  }

  /**
   * Insert the community into the database.
   * @param communityId the community
   * @param communityType the community type
   * @param assemblyId a <code>String</code> Assembly to limit ourselves to
   */
  public static void insertCommunityInfo(String communityId, String communityType, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    substitutions.put(":community_type", communityType);
    doQuery(INSERT_COMMUNITY_INFO_QUERY, substitutions);
  }

  /**
   * Insert a new parameter for the community into the database.
   * @param communityId the community
   * @param assemblyId a <code>String</code> Assembly to limit ourselves to
   */
  public static void insertCommunityAttribute(String communityId, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    doQuery(INSERT_COMMUNITY_ATTRIBUTE_QUERY, substitutions);
  }

  /**
   * Insert the entity into the database.
   * @param communityId the community
   * @param entityId the ID of the entity
   * @param attributeName the attribute to enter
   * @param attributeValue the value of the new attribute
   * @param assemblyId a <code>String</code> Assembly to limit ourselves to
   */
  public static void insertEntityInfo(String communityId, String entityId,
                                      String attributeName, String attributeValue, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    substitutions.put(":entity_id", entityId);
    substitutions.put(":attribute_id", attributeName);
    substitutions.put(":attribute_value", attributeValue);
    doQuery(INSERT_ENTITY_INFO_QUERY, substitutions);
  }

  /**
   * Insert a new parameter for the entity into the database.
   * @param communityId the community
   * @param entityId the entity
   * @param assemblyId a <code>String</code> Assembly to limit ourselves to
   */
  public static void insertEntityAttribute(String communityId, String entityId, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    substitutions.put(":entity_id", entityId);
    doQuery(INSERT_ENTITY_ATTRIBUTE_QUERY, substitutions);
  }

  public static void setCommunityAttributeId(String communityId,
                                             String attributeId,
                                             String prevAttributeId,
                                             String attributeValue, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    substitutions.put(":attribute_id", attributeId);
    substitutions.put(":prev_attribute_id", prevAttributeId);
    substitutions.put(":attribute_value", attributeValue);
    doQuery(UPDATE_COMMUNITY_ATTRIBUTE_ID_QUERY, substitutions);
  }

  public static void setCommunityAttributeValue(String communityId,
                                                String attributeValue,
                                                String attributeId,
                                                String prevAttributeValue, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    substitutions.put(":attribute_value", attributeValue);
    substitutions.put(":attribute_id", attributeId);
    substitutions.put(":prev_attribute_value", prevAttributeValue);
    doQuery(UPDATE_COMMUNITY_ATTRIBUTE_VALUE_QUERY, substitutions);
  }

  public static void setEntityAttributeId(String communityId,
                                          String entityId,
                                          String attributeId,
                                          String prevAttributeId,
                                          String attributeValue, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    substitutions.put(":entity_id", entityId);
    substitutions.put(":attribute_id", attributeId);
    substitutions.put(":prev_attribute_id", prevAttributeId);
    substitutions.put(":attribute_value", attributeValue);
    doQuery(UPDATE_ENTITY_ATTRIBUTE_ID_QUERY, substitutions);
  }

  public static void setEntityAttributeValue(String communityId,
                                             String entityId,
                                             String attributeValue,
                                             String attributeId,
                                             String prevAttributeValue, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    substitutions.put(":entity_id", entityId);
    substitutions.put(":attribute_value", attributeValue);
    substitutions.put(":attribute_id", attributeId);
    substitutions.put(":prev_attribute_value", prevAttributeValue);
    doQuery(UPDATE_ENTITY_ATTRIBUTE_VALUE_QUERY, substitutions);
  }

  public static void deleteCommunityInfo(String communityId, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    doQuery(DELETE_COMMUNITY_INFO_QUERY, substitutions);
  }

  public static void deleteEntityInfo(String communityId, String entityId, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    substitutions.put(":entity_id", entityId);
    doQuery(DELETE_ENTITY_INFO_QUERY, substitutions);
  }

  public static void deleteCommunityAttribute(String communityId,
                                              String attributeId,
                                              String attributeValue, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    substitutions.put(":attribute_id", attributeId);
    substitutions.put(":attribute_value", attributeValue);
    doQuery(DELETE_COMMUNITY_ATTRIBUTE_QUERY, substitutions);
  }

  public static void deleteEntityAttribute(String communityId,
                                           String entityId,
                                           String attributeId,
                                           String attributeValue, String assemblyId) {
    Map substitutions = DatabaseTableModel.getSubstitutions(assemblyId);
    substitutions.put(":community_id", communityId);
    substitutions.put(":entity_id", entityId);
    substitutions.put(":attribute_id", attributeId);
    substitutions.put(":attribute_value", attributeValue);
    doQuery(DELETE_ENTITY_ATTRIBUTE_QUERY, substitutions);
  }
}

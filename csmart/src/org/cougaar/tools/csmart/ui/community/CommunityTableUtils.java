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

import java.util.ArrayList;

/**
 * Defines methods for interfacing to the table that are
 * not part of TableModel.
 */

public interface CommunityTableUtils {

  /**
   * Return all values in the specified column; removes duplicates.
   * @param column index of column
   * @return unique values in that column
   */
  ArrayList getKnownValues(int column);

  /**
   * Delete the specified row.
   * @param rowIndex the index of the row to delete
   */
  void deleteRow(int rowIndex);

  /**
   * Make the table empty.
   */
  void clear();

  /**
   * Display all information for the community in the table.
   * @param communityId the community
   */
  void getAllCommunityInfo(String communityId);

  /**
   * Display attributes for the community in the table.
   * @param communityId the community
   */
  void getCommunityInfo(String communityId);

  /**
   * Display attributes for the entity in the community in the table.
   * @param communityId the community
   * @param entityId the entity
   */
  void getEntityInfo(String communityId, String entityId);

  /**
   * Display attributes for the child entities of the selected tree node
   * in the table.
   * @param communityId the community
   * @param entityId the entity
   */
  void getChildrenEntityInfo(String communityId, String entityId);
}

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

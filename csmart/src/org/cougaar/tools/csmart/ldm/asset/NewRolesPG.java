/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

/* @generated Tue May 15 11:07:43 EDT 2001 from csmartProps.def - DO NOT HAND EDIT */
/** Additional methods for RolesPG
 * offering mutators (set methods) for the object's owner
 **/

package org.cougaar.tools.csmart.ldm.asset;

import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;
import java.util.*;




public interface NewRolesPG extends RolesPG, NewPropertyGroup, org.cougaar.domain.planning.ldm.dq.HasDataQuality {
  void setRoles(List roles);
  void clearRoles();
  boolean removeFromRoles(Role _element);
  boolean addToRoles(Role _element);
}

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
/** AbstractFactory implementation for Properties.
 * Prevents clients from needing to know the implementation
 * class(es) of any of the properties.
 **/

package org.cougaar.tools.csmart.ldm.asset;

import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;
import java.util.*;



public class PropertyGroupFactory extends org.cougaar.domain.planning.ldm.asset.PropertyGroupFactory {
  // brand-new instance factory
  public static NewHappinessPG newHappinessPG() {
    return new HappinessPGImpl();
  }
  // instance from prototype factory
  public static NewHappinessPG newHappinessPG(HappinessPG prototype) {
    return new HappinessPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewSimpleInventoryPG newSimpleInventoryPG() {
    return new SimpleInventoryPGImpl();
  }
  // instance from prototype factory
  public static NewSimpleInventoryPG newSimpleInventoryPG(SimpleInventoryPG prototype) {
    return new SimpleInventoryPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewRolesPG newRolesPG() {
    return new RolesPGImpl();
  }
  // instance from prototype factory
  public static NewRolesPG newRolesPG(RolesPG prototype) {
    return new RolesPGImpl(prototype);
  }

  /** Abstract introspection information.
   * Tuples are {<classname>, <factorymethodname>}
   * return value of <factorymethodname> is <classname>.
   * <factorymethodname> takes zero or one (prototype) argument.
   **/
  public static String properties[][]={
    {"org.cougaar.tools.csmart.ldm.asset.HappinessPG", "newHappinessPG"},
    {"org.cougaar.tools.csmart.ldm.asset.SimpleInventoryPG", "newSimpleInventoryPG"},
    {"org.cougaar.tools.csmart.ldm.asset.RolesPG", "newRolesPG"}
  };
}

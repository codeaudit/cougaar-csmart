/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

/* @generated Thu Oct 18 13:27:08 EDT 2001 from D:\cougaar\csmart\src\org\cougaar\tools\csmart\ldm\asset\csmartProps.def - DO NOT HAND EDIT */
/** AbstractFactory implementation for Properties.
 * Prevents clients from needing to know the implementation
 * class(es) of any of the properties.
 **/

package org.cougaar.tools.csmart.runtime.ldm.asset;

import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import java.util.*;



public class PropertyGroupFactory extends org.cougaar.planning.ldm.asset.PropertyGroupFactory {
  // brand-new instance factory
  public static NewSimpleInventoryPG newSimpleInventoryPG() {
    return new SimpleInventoryPGImpl();
  }
  // instance from prototype factory
  public static NewSimpleInventoryPG newSimpleInventoryPG(SimpleInventoryPG prototype) {
    return new SimpleInventoryPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewHappinessPG newHappinessPG() {
    return new HappinessPGImpl();
  }
  // instance from prototype factory
  public static NewHappinessPG newHappinessPG(HappinessPG prototype) {
    return new HappinessPGImpl(prototype);
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
    {"org.cougaar.tools.csmart.runtime.ldm.asset.SimpleInventoryPG", "newSimpleInventoryPG"},
    {"org.cougaar.tools.csmart.runtime.ldm.asset.HappinessPG", "newHappinessPG"},
    {"org.cougaar.tools.csmart.runtime.ldm.asset.RolesPG", "newRolesPG"}
  };
}

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
/** Primary client interface for RolesPG.
 * a list of roles for LocalAssets to specify their capabilities
 *  @see NewRolesPG
 *  @see RolesPGImpl
 **/

package org.cougaar.tools.csmart.ldm.asset;

import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;
import java.util.*;



public interface RolesPG extends PropertyGroup, org.cougaar.domain.planning.ldm.dq.HasDataQuality {
  List getRoles();
  /** test to see if an element is a member of the roles Collection **/
  boolean inRoles(Role element);

  /** array getter for beans **/
  Role[] getRolesAsArray();

  /** indexed getter for beans **/
  Role getIndexedRoles(int index);


  // introspection and construction
  /** the method of factoryClass that creates this type **/
  public static final String factoryMethod = "newRolesPG";
  /** the (mutable) class type returned by factoryMethod **/
  public static final String mutableClass = "org.cougaar.tools.csmart.ldm.asset.NewRolesPG";
  /** the factory class **/
  public static final Class factoryClass = org.cougaar.tools.csmart.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
  public static final Class primaryClass = org.cougaar.tools.csmart.ldm.asset.RolesPG.class;
  public static final String assetSetter = "setRolesPG";
  public static final String assetGetter = "getRolesPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  public static final RolesPG nullPG = new Null_RolesPG();

/** Null_PG implementation for RolesPG **/
static final class Null_RolesPG
  implements RolesPG, Null_PG
{
  public List getRoles() { throw new UndefinedValueException(); }
  public boolean inRoles(Role element) { return false; }
  public Role[] getRolesAsArray() { return null; }
  public Role getIndexedRoles(int index) { throw new UndefinedValueException(); }
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
  public NewPropertyGroup unlock(Object key) { return null; }
  public PropertyGroup lock(Object key) { return null; }
  public PropertyGroup lock() { return null; }
  public PropertyGroup copy() { return null; }
  public Class getPrimaryClass(){return primaryClass;}
  public String getAssetGetMethod() {return assetGetter;}
  public String getAssetSetMethod() {return assetSetter;}
  public Class getIntrospectionClass() {
    return RolesPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.domain.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for RolesPG **/
public final static class Future
  implements RolesPG, Future_PG
{
  public List getRoles() {
    waitForFinalize();
    return _real.getRoles();
  }
  public boolean inRoles(Role element) {
    waitForFinalize();
    return _real.inRoles(element);
  }
  public Role[] getRolesAsArray() {
    waitForFinalize();
    return _real.getRolesAsArray();
  }
  public Role getIndexedRoles(int index) {
    waitForFinalize();
    return _real.getIndexedRoles(index);
  }
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
  public NewPropertyGroup unlock(Object key) { return null; }
  public PropertyGroup lock(Object key) { return null; }
  public PropertyGroup lock() { return null; }
  public PropertyGroup copy() { return null; }
  public Class getPrimaryClass(){return primaryClass;}
  public String getAssetGetMethod() {return assetGetter;}
  public String getAssetSetMethod() {return assetSetter;}
  public Class getIntrospectionClass() {
    return RolesPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.domain.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private RolesPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof RolesPG) {
      _real=(RolesPG) real;
      notifyAll();
    } else {
      throw new IllegalArgumentException("Finalization with wrong class: "+real);
    }
  }
  private synchronized void waitForFinalize() {
    while (_real == null) {
      try {
        wait();
      } catch (InterruptedException _ie) {}
    }
  }
}
}

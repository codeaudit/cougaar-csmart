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

/* @generated Tue May 15 11:07:46 EDT 2001 from csmartAssets.def - DO NOT HAND EDIT */
package org.cougaar.tools.csmart.ldm.asset;
import org.cougaar.domain.planning.ldm.asset.*;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Vector;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
public class LocalAsset extends CSMARTAsset {

  public LocalAsset() {
    myRolesPG = null;
    mySimpleInventoryPG = null;
  }

  public LocalAsset(LocalAsset prototype) {
    super(prototype);
    myRolesPG=null;
    mySimpleInventoryPG=null;
  }

  /** For infrastructure only - use org.cougaar.domain.planning.ldm.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    LocalAsset _thing = (LocalAsset) super.clone();
    if (myRolesPG!=null) _thing.setRolesPG(myRolesPG.lock());
    if (mySimpleInventoryPG!=null) _thing.setSimpleInventoryPG(mySimpleInventoryPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new LocalAsset();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new LocalAsset(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getRolesPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getSimpleInventoryPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient RolesPG myRolesPG;

  public RolesPG getRolesPG() {
    RolesPG _tmp = (myRolesPG != null) ?
      myRolesPG : (RolesPG)resolvePG(RolesPG.class);
    return (_tmp == RolesPG.nullPG)?null:_tmp;
  }
  public void setRolesPG(PropertyGroup arg_RolesPG) {
    if (!(arg_RolesPG instanceof RolesPG))
      throw new IllegalArgumentException("setRolesPG requires a RolesPG argument.");
    myRolesPG = (RolesPG) arg_RolesPG;
  }

  private transient SimpleInventoryPG mySimpleInventoryPG;

  public SimpleInventoryPG getSimpleInventoryPG() {
    SimpleInventoryPG _tmp = (mySimpleInventoryPG != null) ?
      mySimpleInventoryPG : (SimpleInventoryPG)resolvePG(SimpleInventoryPG.class);
    return (_tmp == SimpleInventoryPG.nullPG)?null:_tmp;
  }
  public void setSimpleInventoryPG(PropertyGroup arg_SimpleInventoryPG) {
    if (!(arg_SimpleInventoryPG instanceof SimpleInventoryPG))
      throw new IllegalArgumentException("setSimpleInventoryPG requires a SimpleInventoryPG argument.");
    mySimpleInventoryPG = (SimpleInventoryPG) arg_SimpleInventoryPG;
  }

  // generic search methods
  public PropertyGroupSchedule searchForPropertyGroupSchedule(Class c) {
    return super.searchForPropertyGroupSchedule(c);
  }

  public PropertyGroup getLocalPG(Class c, long t) {
    if (RolesPG.class.equals(c)) {
      return (myRolesPG==RolesPG.nullPG)?null:myRolesPG;
    }
    if (SimpleInventoryPG.class.equals(c)) {
      return (mySimpleInventoryPG==SimpleInventoryPG.nullPG)?null:mySimpleInventoryPG;
    }
    return super.getLocalPG(c,t);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (RolesPG.class.equals(c)) {
      myRolesPG=(RolesPG)pg;
    } else
    if (SimpleInventoryPG.class.equals(c)) {
      mySimpleInventoryPG=(SimpleInventoryPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public PropertyGroup generateDefaultPG(Class c) {
    if (RolesPG.class.equals(c)) {
      return (myRolesPG= new RolesPGImpl());
    } else
    if (SimpleInventoryPG.class.equals(c)) {
      return (mySimpleInventoryPG= new SimpleInventoryPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myRolesPG instanceof Null_PG || myRolesPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myRolesPG);
      }
      if (mySimpleInventoryPG instanceof Null_PG || mySimpleInventoryPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(mySimpleInventoryPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myRolesPG=(RolesPG)in.readObject();
      mySimpleInventoryPG=(SimpleInventoryPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[2];
      properties[0] = new PropertyDescriptor("RolesPG", LocalAsset.class, "getRolesPG", null);
      properties[1] = new PropertyDescriptor("SimpleInventoryPG", LocalAsset.class, "getSimpleInventoryPG", null);
    } catch (IntrospectionException ie) {}
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pds = super.getPropertyDescriptors();
    PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+2];
    System.arraycopy(pds, 0, ps, 0, pds.length);
    System.arraycopy(properties, 0, ps, pds.length, 2);
    return ps;
  }
}

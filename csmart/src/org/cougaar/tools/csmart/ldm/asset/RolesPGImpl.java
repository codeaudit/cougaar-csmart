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

/* @generated Tue May 15 11:07:43 EDT 2001 from csmartProps.def - DO NOT HAND EDIT */
/** Implementation of RolesPG.
 *  @see RolesPG
 *  @see NewRolesPG
 **/

package org.cougaar.tools.csmart.ldm.asset;

import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;
import java.util.*;



import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;

public class RolesPGImpl extends java.beans.SimpleBeanInfo
  implements NewRolesPG, Cloneable
{
  public RolesPGImpl() {
  };

  // Slots

  private List theRoles = new ArrayList();
  public List getRoles(){ return theRoles; }
  public boolean inRoles(Role _element) {
    return theRoles.contains(_element);
  }
  public Role[] getRolesAsArray() {
    if (theRoles == null) return new Role[0];
    int l = theRoles.size();
    Role[] v = new Role[l];
    int i=0;
    for (Iterator n=theRoles.iterator(); n.hasNext(); ) {
      v[i]=(Role) n.next();
      i++;
    }
    return v;
  }
  public Role getIndexedRoles(int _index) {
    if (theRoles == null) return null;
    for (Iterator _i = theRoles.iterator(); _i.hasNext();) {
      Role _e = (Role) _i.next();
      if (_index == 0) return _e;
      _index--;
    }
    return null;
  }
  public void setRoles(List roles) {
    theRoles=roles;
  }
  public void clearRoles() {
    theRoles.clear();
  }
  public boolean removeFromRoles(Role _element) {
    return theRoles.remove(_element);
  }
  public boolean addToRoles(Role _element) {
    return theRoles.add(_element);
  }


  public RolesPGImpl(RolesPG original) {
    theRoles = original.getRoles();
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.domain.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends RolesPGImpl implements org.cougaar.domain.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(RolesPG original) {
    super(original);
   }
   public Object clone() { return new DQ(this); }
   private transient org.cougaar.domain.planning.ldm.dq.DataQuality _dq = null;
   public boolean hasDataQuality() { return (_dq!=null); }
   public org.cougaar.domain.planning.ldm.dq.DataQuality getDataQuality() { return _dq; }
   public void setDataQuality(org.cougaar.domain.planning.ldm.dq.DataQuality dq) { _dq=dq; }
   private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    if (out instanceof org.cougaar.core.cluster.persist.PersistenceOutputStream) out.writeObject(_dq);
   }
   private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    if (in instanceof org.cougaar.core.cluster.persist.PersistenceInputStream) _dq=(org.cougaar.domain.planning.ldm.dq.DataQuality)in.readObject();
   }
    
    private final static PropertyDescriptor properties[]=new PropertyDescriptor[1];
    static {
      try {
        properties[0]= new PropertyDescriptor("dataQuality", DQ.class, "getDataQuality", null);
      } catch (Exception e) { e.printStackTrace(); }
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
      PropertyDescriptor[] pds = super.properties;
      PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+properties.length];
      System.arraycopy(pds, 0, ps, 0, pds.length);
      System.arraycopy(properties, 0, ps, pds.length, properties.length);
      return ps;
    }
  }


  private transient RolesPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)
      _locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    RolesPGImpl _tmp = new RolesPGImpl(this);
    return _tmp;
  }

  public PropertyGroup copy() {
    try {
      return (PropertyGroup) clone();
    } catch (CloneNotSupportedException cnse) { return null;}
  }

  public Class getPrimaryClass() {
    return primaryClass;
  }
  public String getAssetGetMethod() {
    return assetGetter;
  }
  public String getAssetSetMethod() {
    return assetSetter;
  }

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[1];
  static {
    try {
      properties[0]= new IndexedPropertyDescriptor("roles", RolesPG.class, "getRolesAsArray", null, "getIndexedRoles", null);
    } catch (Exception e) { System.err.println("Caught: "+e); e.printStackTrace(); }
  };

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements RolesPG, Cloneable, LockedPG
  {
    private transient Object theKey = null;
    _Locked(Object key) { 
      if (this.theKey == null){  
        this.theKey = key; 
      } 
    }  

    /** public constructor for beaninfo - probably wont work**/
    public _Locked() {}

    public PropertyGroup lock() { return this; }
    public PropertyGroup lock(Object o) { return this; }

    public NewPropertyGroup unlock(Object key) throws IllegalAccessException {
       if( theKey.equals(key) )
         return RolesPGImpl.this;
       else 
         throw new IllegalAccessException("unlock: mismatched internal and provided keys!");
    }

    public PropertyGroup copy() {
      return new RolesPGImpl(RolesPGImpl.this);
    }

    public Object clone() throws CloneNotSupportedException {
      return new RolesPGImpl(RolesPGImpl.this);
    }

    public List getRoles() { return RolesPGImpl.this.getRoles(); }
  public boolean inRoles(Role _element) {
    return RolesPGImpl.this.inRoles(_element);
  }
  public Role[] getRolesAsArray() {
    return RolesPGImpl.this.getRolesAsArray();
  }
  public Role getIndexedRoles(int _index) {
    return RolesPGImpl.this.getIndexedRoles(_index);
  }
  public final boolean hasDataQuality() { return RolesPGImpl.this.hasDataQuality(); }
  public final org.cougaar.domain.planning.ldm.dq.DataQuality getDataQuality() { return RolesPGImpl.this.getDataQuality(); }
    public Class getPrimaryClass() {
      return primaryClass;
    }
    public String getAssetGetMethod() {
      return assetGetter;
    }
    public String getAssetSetMethod() {
      return assetSetter;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
      return properties;
    }

    public Class getIntrospectionClass() {
      return RolesPGImpl.class;
    }

  }

}

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
/** Implementation of HappinessPG.
 *  @see HappinessPG
 *  @see NewHappinessPG
 **/

package org.cougaar.tools.csmart.ldm.asset;

import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import java.util.*;



import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;

public class HappinessPGImpl extends java.beans.SimpleBeanInfo
  implements NewHappinessPG, Cloneable
{
  public HappinessPGImpl() {
    hBG = new HappinessBG(this);
  }

  // Slots


  private HappinessBG hBG = null;
  public long getStartTime() { return hBG.getStartTime();  }
  public long getEndTime() { return hBG.getEndTime();  }
  public double getHappinessAt(long time) { return hBG.getHappinessAt(time);  }
  public void setHappinessAt(long time, double happiness) { hBG.setHappinessAt(time, happiness);  }
  public double[] toArray(long startTime, long endTime, long timeIncrement) { return hBG.toArray(startTime, endTime, timeIncrement);  }
  public double[] toArray(double[] toA, long startTime, long endTime, long timeIncrement) { return hBG.toArray(toA, startTime, endTime, timeIncrement);  }
  public String toString() { return hBG.toString();  }
  public String toString(boolean verbose) { return hBG.toString(verbose);  }

  public HappinessPGImpl(HappinessPG original) {
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends HappinessPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(HappinessPG original) {
    super(original);
   }
   public Object clone() { return new DQ(this); }
   private transient org.cougaar.planning.ldm.dq.DataQuality _dq = null;
   public boolean hasDataQuality() { return (_dq!=null); }
   public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return _dq; }
   public void setDataQuality(org.cougaar.planning.ldm.dq.DataQuality dq) { _dq=dq; }
   private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    if (out instanceof org.cougaar.core.persist.PersistenceOutputStream) out.writeObject(_dq);
   }
   private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    if (in instanceof org.cougaar.core.persist.PersistenceInputStream) _dq=(org.cougaar.planning.ldm.dq.DataQuality)in.readObject();
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


  private transient HappinessPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)
      _locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    HappinessPGImpl _tmp = new HappinessPGImpl(this);
    if (hBG != null) {
      _tmp.hBG = (HappinessBG) hBG.copy(_tmp);
    }
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

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[0];

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements HappinessPG, Cloneable, LockedPG
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
         return HappinessPGImpl.this;
       else 
         throw new IllegalAccessException("unlock: mismatched internal and provided keys!");
    }

    public PropertyGroup copy() {
      try {
        return (PropertyGroup) clone();
      } catch (CloneNotSupportedException cnse) { return null;}
    }


    public Object clone() throws CloneNotSupportedException {
      HappinessPGImpl _tmp = new HappinessPGImpl(this);
      if (hBG != null) {
        _tmp.hBG = (HappinessBG) hBG.copy(_tmp);
      }
      return _tmp;
    }

  public long getStartTime() {
    return HappinessPGImpl.this.getStartTime();
  }
  public long getEndTime() {
    return HappinessPGImpl.this.getEndTime();
  }
  public double getHappinessAt(long time) {
    return HappinessPGImpl.this.getHappinessAt(time);
  }
  public void setHappinessAt(long time, double happiness) {
    HappinessPGImpl.this.setHappinessAt(time, happiness);
  }
  public double[] toArray(long startTime, long endTime, long timeIncrement) {
    return HappinessPGImpl.this.toArray(startTime, endTime, timeIncrement);
  }
  public double[] toArray(double[] toA, long startTime, long endTime, long timeIncrement) {
    return HappinessPGImpl.this.toArray(toA, startTime, endTime, timeIncrement);
  }
  public String toString() {
    return HappinessPGImpl.this.toString();
  }
  public String toString(boolean verbose) {
    return HappinessPGImpl.this.toString(verbose);
  }
  public final boolean hasDataQuality() { return HappinessPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return HappinessPGImpl.this.getDataQuality(); }
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
      return HappinessPGImpl.class;
    }

  }

}

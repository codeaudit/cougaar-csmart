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

/* @generated Thu Sep 06 13:26:09 EDT 2001 from D:\CSMART\Cougaar\csmart\src\org\cougaar\tools\csmart\ldm\asset\csmartProps.def - DO NOT HAND EDIT */
/** Implementation of SimpleInventoryPG.
 *  @see SimpleInventoryPG
 *  @see NewSimpleInventoryPG
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

public class SimpleInventoryPGImpl extends java.beans.SimpleBeanInfo
  implements NewSimpleInventoryPG, Cloneable
{
  public SimpleInventoryPGImpl() {
  }

  // Slots


  private SimpleInventoryBG invBG = null;
  public SimpleInventoryBG getInvBG() {
    return invBG;
  }
  public void setInvBG(SimpleInventoryBG _invBG) {
    if (invBG != null) throw new IllegalArgumentException("invBG already set");
    invBG = _invBG;
  }
  public long consume(long time) { return invBG.consume(time);  }
  public long getStartTime() { return invBG.getStartTime();  }
  public long getEndTime() { return invBG.getEndTime();  }
  public double getInventoryLevelAt(long time) { return invBG.getInventoryLevelAt(time);  }
  public void setInventoryLevelAt(long time, double level) { invBG.setInventoryLevelAt(time, level);  }
  public double[] toArray(long startTime, long endTime, long timeIncrement) { return invBG.toArray(startTime, endTime, timeIncrement);  }
  public double[] toArray(double[] toA, long startTime, long endTime, long timeIncrement) { return invBG.toArray(toA, startTime, endTime, timeIncrement);  }
  public String toString() { return invBG.toString();  }
  public String toString(boolean verbose) { return invBG.toString(verbose);  }

  public SimpleInventoryPGImpl(SimpleInventoryPG original) {
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.domain.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends SimpleInventoryPGImpl implements org.cougaar.domain.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(SimpleInventoryPG original) {
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


  private transient SimpleInventoryPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)
      _locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    SimpleInventoryPGImpl _tmp = new SimpleInventoryPGImpl(this);
    if (invBG != null) {
      _tmp.invBG = (SimpleInventoryBG) invBG.copy(_tmp);
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
    implements SimpleInventoryPG, Cloneable, LockedPG
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
         return SimpleInventoryPGImpl.this;
       else 
         throw new IllegalAccessException("unlock: mismatched internal and provided keys!");
    }

    public PropertyGroup copy() {
      try {
        return (PropertyGroup) clone();
      } catch (CloneNotSupportedException cnse) { return null;}
    }


    public Object clone() throws CloneNotSupportedException {
      SimpleInventoryPGImpl _tmp = new SimpleInventoryPGImpl(this);
      if (invBG != null) {
        _tmp.invBG = (SimpleInventoryBG) invBG.copy(_tmp);
      }
      return _tmp;
    }

  public long consume(long time) {
    return SimpleInventoryPGImpl.this.consume(time);
  }
  public long getStartTime() {
    return SimpleInventoryPGImpl.this.getStartTime();
  }
  public long getEndTime() {
    return SimpleInventoryPGImpl.this.getEndTime();
  }
  public double getInventoryLevelAt(long time) {
    return SimpleInventoryPGImpl.this.getInventoryLevelAt(time);
  }
  public void setInventoryLevelAt(long time, double level) {
    SimpleInventoryPGImpl.this.setInventoryLevelAt(time, level);
  }
  public double[] toArray(long startTime, long endTime, long timeIncrement) {
    return SimpleInventoryPGImpl.this.toArray(startTime, endTime, timeIncrement);
  }
  public double[] toArray(double[] toA, long startTime, long endTime, long timeIncrement) {
    return SimpleInventoryPGImpl.this.toArray(toA, startTime, endTime, timeIncrement);
  }
  public String toString() {
    return SimpleInventoryPGImpl.this.toString();
  }
  public String toString(boolean verbose) {
    return SimpleInventoryPGImpl.this.toString(verbose);
  }
  public final boolean hasDataQuality() { return SimpleInventoryPGImpl.this.hasDataQuality(); }
  public final org.cougaar.domain.planning.ldm.dq.DataQuality getDataQuality() { return SimpleInventoryPGImpl.this.getDataQuality(); }
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
      return SimpleInventoryPGImpl.class;
    }

  }

}

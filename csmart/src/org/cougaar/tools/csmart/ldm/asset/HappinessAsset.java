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

/* @generated Thu Oct 18 13:27:06 EDT 2001 from D:\cougaar\csmart\src\org\cougaar\tools\csmart\ldm\asset\csmartAssets.def - DO NOT HAND EDIT */
package org.cougaar.tools.csmart.ldm.asset;
import org.cougaar.planning.ldm.asset.*;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Vector;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
public class HappinessAsset extends CSMARTAsset {

  public HappinessAsset() {
    myHappinessPG = null;
  }

  public HappinessAsset(HappinessAsset prototype) {
    super(prototype);
    myHappinessPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    HappinessAsset _thing = (HappinessAsset) super.clone();
    if (myHappinessPG!=null) _thing.setHappinessPG(myHappinessPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new HappinessAsset();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new HappinessAsset(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getHappinessPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient HappinessPG myHappinessPG;

  public HappinessPG getHappinessPG() {
    HappinessPG _tmp = (myHappinessPG != null) ?
      myHappinessPG : (HappinessPG)resolvePG(HappinessPG.class);
    return (_tmp == HappinessPG.nullPG)?null:_tmp;
  }
  public void setHappinessPG(PropertyGroup arg_HappinessPG) {
    if (!(arg_HappinessPG instanceof HappinessPG))
      throw new IllegalArgumentException("setHappinessPG requires a HappinessPG argument.");
    myHappinessPG = (HappinessPG) arg_HappinessPG;
  }

  // generic search methods
  public PropertyGroupSchedule searchForPropertyGroupSchedule(Class c) {
    return super.searchForPropertyGroupSchedule(c);
  }

  public PropertyGroup getLocalPG(Class c, long t) {
    if (HappinessPG.class.equals(c)) {
      return (myHappinessPG==HappinessPG.nullPG)?null:myHappinessPG;
    }
    return super.getLocalPG(c,t);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (HappinessPG.class.equals(c)) {
      myHappinessPG=(HappinessPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (HappinessPG.class.equals(c)) {
      removed=myHappinessPG;
      myHappinessPG=null;
    } else
      removed=super.removeLocalPG(c);
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    PropertyGroup removed = null;
    Class pgc = pg.getPrimaryClass();
    if (HappinessPG.class.equals(pgc)) {
      removed=myHappinessPG;
      myHappinessPG=null;
    } else
      removed= super.removeLocalPG(pg);
    return removed;
  }

  public PropertyGroupSchedule removeLocalPGSchedule(Class c) {
    PropertyGroupSchedule removed = null;
    return removed;
  }

  public PropertyGroup generateDefaultPG(Class c) {
    if (HappinessPG.class.equals(c)) {
      return (myHappinessPG= new HappinessPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myHappinessPG instanceof Null_PG || myHappinessPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myHappinessPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myHappinessPG=(HappinessPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[1];
      properties[0] = new PropertyDescriptor("HappinessPG", HappinessAsset.class, "getHappinessPG", null);
    } catch (IntrospectionException ie) {}
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pds = super.getPropertyDescriptors();
    PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+1];
    System.arraycopy(pds, 0, ps, 0, pds.length);
    System.arraycopy(properties, 0, ps, pds.length, 1);
    return ps;
  }
}

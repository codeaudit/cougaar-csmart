/*
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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
package  org.cougaar.tools.csmart.util;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;

public class ReadOnlyProperties extends Properties implements Serializable {
    protected Set readOnlyKeys = null;
    private PropertiesObservable observable;

    public ReadOnlyProperties() {
      super();
      init();
    }

    public ReadOnlyProperties(Set readOnlyKeys) {
        super();
        this.readOnlyKeys = readOnlyKeys;
        init();
    }

    public ReadOnlyProperties(Set readOnlyKeys, Properties defaults) {
        super(defaults);
        this.readOnlyKeys = readOnlyKeys;
        init();
    }

  private void init() {
    if (observable == null)
      observable = new PropertiesObservable();
  }

  public void addObserver(Observer o) {
    observable.addObserver(o);
  }

  public void deleteObserver(Observer o) {
    observable.deleteObserver(o);
  }

  private void fireChange() {
    if (observable == null)
      observable = new PropertiesObservable();
    observable.fireChange();
  }

    public void setReadOnlyProperty(String key, String newValue) {
        super.put(key, newValue);
        //        System.out.println("Setting read only property: " + key);
        fireChange();
    }

    public Object setProperty(String key, String newValue) {
      //        System.out.println("Setting property: " + key);
        fireChange();
        return super.setProperty(key, newValue);
    }

    public boolean isReadOnly(Object key) {
        if (readOnlyKeys == null) return false;
        if (readOnlyKeys.contains(key)) return true;
        if (defaults instanceof ReadOnlyProperties) {
            return ((ReadOnlyProperties) defaults).isReadOnly(key);
        }
        return false;
    }

    // Override the Property interface implementation to enforce read-only keys

    public Object put(Object key, Object val) {
        if (isReadOnly(key)) return null;
        if (val.equals(super.get(key))) return val; // no change
        //        System.out.println("Putting property: " + key);
        fireChange();
        return super.put(key, val);
    }

    public void putAll(Map map) {
        for (Iterator entries = map.entrySet().iterator(); entries.hasNext(); ) {
            Map.Entry entry = (Map.Entry) entries.next();
            Object key = entry.getKey();
            if (isReadOnly(key)) continue;
            super.put(key, entry.getValue());
        }
        //        System.out.println("Put all");
        fireChange();
    }

    public Object remove(Object key) {
        if (isReadOnly(key)) return null;
        if (!super.contains(key)) return null; // no change
        //        System.out.println("Removing: " + key);
        fireChange();
        return super.remove(key);
    }

    public void clear() {
        for (Iterator keys = keySet().iterator(); keys.hasNext(); ) {
            Object key = keys.next();
            if (isReadOnly(key)) continue;
            keys.remove();
        }
        //        System.out.println("Clearing");
        fireChange();
    }

  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    ois.defaultReadObject();
  }

}

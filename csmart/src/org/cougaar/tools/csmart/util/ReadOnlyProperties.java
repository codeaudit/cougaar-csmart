/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package  org.cougaar.tools.csmart.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
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
    Object o = get(key);
    if (o != null && o.equals(newValue)) return; // no change
    super.put(key, newValue);
    //        System.out.println("Setting read only property: " + key);
    fireChange();
  }
  
  public Object setProperty(String key, String newValue) {
    //        System.out.println("Setting property: " + key);
    //        fireChange(); // change is fired in put if necessary

    // Dont call super.setProperty which is synchronized
    // Wait to let super.put do the synchronization
    //    return super.setProperty(key, newValue);
    return put(key, newValue);
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
    Object o = get(key);
    if (o != null && o.equals(val)) return val; // no change
    fireChange();
    return super.put(key, val);
  }
  
  public void putAll(Map map) {
    boolean changeHappened = false;
    for (Iterator entries = map.entrySet().iterator(); entries.hasNext(); ) {
      Map.Entry entry = (Map.Entry) entries.next();
      Object key = entry.getKey();
            if (isReadOnly(key)) continue;
            Object o = get(key);
            if (o != null && o.equals(entry.getValue())) continue;
            super.put(key, entry.getValue());
            changeHappened = true;
    }
    //        System.out.println("Put all");
    if (changeHappened)
      fireChange();
  }
  
  public Object remove(Object key) {
    if (isReadOnly(key)) return null;
    if (!super.containsKey(key)) return null; // no change
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

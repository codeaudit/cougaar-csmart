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
import java.util.Properties;
import java.util.Set;

public class ReadOnlyProperties extends Properties implements Serializable {
    protected Set readOnlyKeys = null;

    public ReadOnlyProperties(Set readOnlyKeys) {
        super();
        this.readOnlyKeys = readOnlyKeys;
    }

    public ReadOnlyProperties(Set readOnlyKeys, Properties defaults) {
        super(defaults);
        this.readOnlyKeys = readOnlyKeys;
    }

    public void setReadOnlyProperty(String key, String newValue) {
        super.put(key, newValue);
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
        return super.put(key, val);
    }

    public void putAll(Map map) {
        for (Iterator entries = map.entrySet().iterator(); entries.hasNext(); ) {
            Map.Entry entry = (Map.Entry) entries.next();
            Object key = entry.getKey();
            if (isReadOnly(key)) continue;
            super.put(key, entry.getValue());
        }
    }

    public Object remove(Object key) {
        if (isReadOnly(key)) return null;
        return super.remove(key);
    }

    public void clear() {
        for (Iterator keys = keySet().iterator(); keys.hasNext(); ) {
            Object key = keys.next();
            if (isReadOnly(key)) continue;
            keys.remove();
        }
    }
}

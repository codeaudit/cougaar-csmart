/* 
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.ui.experiment;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.lang.reflect.Array;
import org.cougaar.tools.csmart.ui.component.*;

public class PropTableModel extends PropTableModelBase {
    private Map bindings = new HashMap();

    public void setComponentProperties(ComponentProperties cp) {
        for (Iterator i = cp.getPropertyNames(); i.hasNext(); ) {
            Property prop = (Property) cp.getProperty((CompositeName) i.next());
            if (!prop.isValueSet()) {
                addProperty(prop);
            }
        }
    }

  /**
   * Add experiment values to bindings.
   */

    public void addProperty(Property prop) {
      bindings.put(prop.getName(), prop.getExperimentValues());
      super.addProperty(prop);
    }

  /**
   * Render null experiment values as "<not set>".
   * Render nested arrays.
   */

    protected String render(Property prop) {
        Object o = bindings.get(prop.getName());
	if (o == null)
	  return "<not set>";
        if (o.getClass().isArray()) {
            StringBuffer buf = new StringBuffer();
            buf.append('{');
            for (int i = 0, n = Array.getLength(o); i < n; i++) {
                if (i > 0) buf.append(",");
                Object tmpValue = Array.get(o, i);
                if (!tmpValue.getClass().isArray())
                  buf.append(tmpValue);
                else {
                  buf.append("{");
                  for (int j = 0, m = Array.getLength(tmpValue); j < m; j++) {
                    if (j > 0) buf.append(",");
                    buf.append(Array.get(tmpValue, j));
                  }
                  buf.append("}");
                }
            }
            buf.append("}");
            return buf.toString();
        } else {
            return o.toString();
        }
    }

  /**
   * If new value is an empty string, then delete the previous
   * experiment values (set to null).
   * If the property class is a single value, then the experiment values
   * must be an array; if the property class is an array, then the
   * experiment values must be an array of arrays (represented by
   * nested braces).
   */

  public void setValue(Property prop, Object newValue) {
    // treat empty string as null, delete previous experiment values
    if (newValue instanceof String && ((String)newValue).length() == 0) {
      bindings.put(prop.getName(), null);
      prop.setExperimentValues(null);
      // notify table model listeners
      fireTableCellUpdated(getRowForProperty(prop), 1);
      return;
    }
    Class cls = prop.getPropertyClass();
    // strips off first level of braces
    String[] values =
      (String[]) PropertyHelper.convertStringToArray(newValue.toString());
    if (cls == null) {
      newValue = values;
    } else if (!cls.isArray()) {
      // if the class is a single value, then the experiment values
      // are an array of that class
      newValue = Array.newInstance(cls, values.length);
      for (int i = 0; i < values.length; i++) {
        try {
          Array.set(newValue, i,
                    PropertyHelper.validateValue(prop, values[i]));
        } catch (InvalidPropertyValueException e) {
          e.printStackTrace();
        }
      }
    } else {
      // if the class is an array, then the experiment values
      // are an array of arrays
      Object[] tmpValue = new Object[values.length];
      for (int i = 0; i < values.length; i++) {
        String[] expValues =
          (String[]) PropertyHelper.convertStringToArray(values[i]);
        try {
          tmpValue[i] = PropertyHelper.validateValue(prop, expValues);
        } catch (InvalidPropertyValueException e) {
          e.printStackTrace();
        }
      }
      newValue = tmpValue;
    }
    bindings.put(prop.getName(), newValue);
    // set new values as experiment values in property
    prop.setExperimentValues(Arrays.asList((Object [])newValue));
    // notify table model listeners
    fireTableCellUpdated(getRowForProperty(prop), 1);
  }

}


/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
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

    public void addProperty(Property prop) {
        bindings.put(prop.getName(), prop.getDefaultValue());
        super.addProperty(prop);
    }

    protected String render(Property prop) {
        Object o = bindings.get(prop.getName());
        if (o.getClass().isArray()) {
            StringBuffer buf = new StringBuffer();
            buf.append('{');
            for (int i = 0, n = Array.getLength(o); i < n; i++) {
                if (i > 0) buf.append(",");
                buf.append(Array.get(o, i));
            }
            buf.append("}");
            return buf.toString();
        } else {
            return o.toString();
        }
    }

    public void setValue(Property prop, Object newValue) {
        Class cls = prop.getPropertyClass();
        String[] values =
            (String[]) PropertyHelper.convertStringToArray(newValue.toString());
        if (cls == null) {
            newValue = values;
        } else {
            newValue = Array.newInstance(cls, values.length);
            for (int i = 0; i < values.length; i++) {
                try {
                    Array.set(newValue, i,
                              PropertyHelper.validateValue(prop, values[i]));
                } catch (InvalidPropertyValueException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("newValue " + newValue);
        bindings.put(prop.getName(), newValue);
    }
}

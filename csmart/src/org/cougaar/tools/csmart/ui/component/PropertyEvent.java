/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.component;

import java.util.EventObject;

public class PropertyEvent extends EventObject {
    static final long serialVersionUID = -2000572256849383880L;

    public static final int            VALUE_CHANGED = 0;
    public static final int     DEFAULTVALUE_CHANGED = 1;
    public static final int            LABEL_CHANGED = 2;
    public static final int             NAME_CHANGED = 3;
    public static final int            CLASS_CHANGED = 4;
    public static final int    ALLOWEDVALUES_CHANGED = 5;
    public static final int EXPERIMENTVALUES_CHANGED = 6;
    public static final int         PROPERTY_ADDED   = 7;
    public static final int         PROPERTY_REMOVED = 8;

    private Object previousValue;
    private int whatChanged;

    public PropertyEvent(Object src, int whatChanged) {
        this(src, whatChanged, null);
    }

    public PropertyEvent(Object src, int whatChanged, Object previousValue) {
        super(src);
        this.whatChanged = whatChanged;
        this.previousValue = previousValue;
    }

    public int getWhatChanged() {
        return whatChanged;
    }

    public Object getPreviousValue() {
        return previousValue;
    }

    public Property getProperty() {
        Object result = getSource();
        if (result instanceof Property)
            return (Property) result;
        return null;
    }
}

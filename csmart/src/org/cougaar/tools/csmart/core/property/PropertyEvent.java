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
    public static final int          TOOLTIP_CHANGED = 9;
    public static final int             HELP_CHANGED =10;

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

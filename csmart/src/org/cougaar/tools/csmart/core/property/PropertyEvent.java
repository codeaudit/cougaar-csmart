/* 
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.core.property;

import java.util.EventObject;

/**
 * Defines an Custom Event class for Properties
 *
 */
public class PropertyEvent extends EventObject {
    static final long serialVersionUID = -2000572256849383880L;

  /** Possible Changes **/
  public static final int            VALUE_CHANGED = 0;
  public static final int     DEFAULTVALUE_CHANGED = 1;
  public static final int            LABEL_CHANGED = 2;
  public static final int             NAME_CHANGED = 3;
  public static final int            CLASS_CHANGED = 4;
  public static final int    ALLOWEDVALUES_CHANGED = 5;
  public static final int         PROPERTY_ADDED   = 7;
  public static final int         PROPERTY_REMOVED = 8;
  public static final int          TOOLTIP_CHANGED = 9;
  public static final int             HELP_CHANGED =10;

  private Object previousValue;
  private int whatChanged;

  /**
   * Creates a new <code>PropertyEvent</code> instance.
   *
   * @param src 
   * @param whatChanged 
   */
  public PropertyEvent(Object src, int whatChanged) {
    this(src, whatChanged, null);
  }

  /**
   * Creates a new <code>PropertyEvent</code> instance.
   *
   * @param src 
   * @param whatChanged 
   * @param previousValue 
   */
  public PropertyEvent(Object src, int whatChanged, Object previousValue) {
    super(src);
    this.whatChanged = whatChanged;
    this.previousValue = previousValue;
  }

  /**
   * Gets what changed to cause this event.
   *
   * @return an <code>int</code> value
   */
  public int getWhatChanged() {
    return whatChanged;
  }

  /**
   * Gets the previous value, before the change.
   *
   * @return an <code>Object</code> value
   */
  public Object getPreviousValue() {
    return previousValue;
  }

  /**
   * Gets the Property that changed.
   *
   * @return a <code>Property</code> value
   */
  public Property getProperty() {
    Object result = getSource();
    if (result instanceof Property)
      return (Property) result;
    return null;
  }
}

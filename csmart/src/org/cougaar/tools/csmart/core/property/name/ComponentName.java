/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.core.property.name;

import org.cougaar.tools.csmart.core.property.BaseComponent;

/**
 * The name in the namespace of a configurable component.
 **/
public class ComponentName extends MultiName implements CompositeName {
  static final long serialVersionUID = -4393441897050449170L;

  private BaseComponent component;

  public ComponentName(BaseComponent cc, String name) {
    super(SimpleName.getSimpleName(name));
    component = cc;
  }

  public void setComponent(BaseComponent cc) {
    component = cc;
    decache();
  }

  public BaseComponent getConfigurableComponent() {
    return component;
  }

  protected CompositeName getParentName() {
    if (component == null) return null;
    return component.getFullName();
  }
}

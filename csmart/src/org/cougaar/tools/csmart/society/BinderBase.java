/**
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
 *  </copyright>
 */
package org.cougaar.tools.csmart.society;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.cdata.GenericComponentData;
import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.PropertyAlias;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.core.component.ComponentDescription;

/**
 * BinderBase is the basic implementation for editing & configuring
 * binders. It is particularly suited to Binders of Plugins (that sit
 * inside Agents). However, it has a slot to allow specicying the type.
 */
public class BinderBase
  extends ComponentBase
  implements BinderComponent {

  /** Binder Classname Property Definition **/
  public static final String PROP_CLASSNAME = "Binder Class Name";
  public static final String PROP_CLASSNAME_DESC = "Name of the Binder Class";

  // FIXME:
  // Need to specify the insertion point
  // and actual type of this component

  protected String folderLabel = "Binders";

  public static final String PROP_TYPE = "Binder Type";
  public static final String PROP_TYPE_DESC = "Type of Binder (Agent, Node)";
  protected String type = ComponentData.AGENTBINDER;
  public static final String PROP_PARAM = "param-";

  public BinderBase(String name) {
    super(name);
    createLogger();
  }

  public BinderBase(String name, String classname, String priority) {
    super(name);
    this.classname = classname;
    if(priority != null) {
      this.priority = priority;
    } else {
      this.priority = 
        ComponentDescription.priorityToString(ComponentDescription.PRIORITY_BINDER);
    }
    createLogger();
  }

  public BinderBase(String name, String classname, String priority, String type) {
    super(name);
    this.classname = classname;
    if(priority != null) {
      this.priority = priority;
    } else {
      this.priority = 
        ComponentDescription.priorityToString(ComponentDescription.PRIORITY_BINDER);
    }
    this.type = getCanonicalType(type);
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

} // End of BinderBase

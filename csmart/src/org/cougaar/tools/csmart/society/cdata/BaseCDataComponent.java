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
package org.cougaar.tools.csmart.society.cdata;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.db.PopulateDb;
import org.cougaar.tools.csmart.core.property.BaseComponent;
import org.cougaar.tools.csmart.core.property.ComposableComponent;
import org.cougaar.tools.csmart.core.property.PropertiesListener;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.core.property.name.CompositeName;
import org.cougaar.tools.csmart.society.ComponentBase;


/**
 * A BaseComponent initialized from ComponentData. Used when copying Complex Recipes.
 */
public class BaseCDataComponent extends ComponentBase implements BaseComponent {
  
  private ComponentData cdata;

  public BaseCDataComponent(ComponentData cdata) {
    super(cdata.getName());
    this.cdata = cdata;
    this.classname = cdata.getClassName();
    this.priority = cdata.getPriority();
    this.type = cdata.getType();
  }

  /**
   * Initialize the Properties using the super class. Then copy over the
   * other slots: LIB_ID, ALIB_ID, and the Parameters.
   */
  public void initProperties() {
    super.initProperties();
    setLibID(cdata.getLibID());
    setAlibID(cdata.getAlibID());
    // for each parameter of the component, call addParameter
    for (int i = 0; i < cdata.parameterCount(); i++)
      addParameter(cdata.getParameter(i));
  }

}// BaseCDataComponent

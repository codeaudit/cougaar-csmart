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
package org.cougaar.tools.csmart.recipe;

import java.util.Set;
import java.util.HashSet;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentProperty;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.tools.csmart.core.cdata.ComponentData;


/**
 * ComponentTypeProperty.java
 *
 * Defines all possible values of Component types as a set for a recipe.
 *
 * Created: Wed Jan 16 15:41:11 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 * @version 1.0
 */

public class ComponentTypeProperty extends ConfigurableComponentProperty {

  private static Set allComponents = null;

  public ComponentTypeProperty (ConfigurableComponent c, String name, Object value){
    
//     super(c, name, new StringRange((String)value));
    super(c, name, value);
    
    // Build up the set from the known values in ComponentData
    allComponents = new HashSet();
    allComponents.add(new StringRange(ComponentData.SOCIETY));
    allComponents.add(new StringRange(ComponentData.HOST));
    allComponents.add(new StringRange(ComponentData.AGENT));
    allComponents.add(new StringRange(ComponentData.NODE));
    allComponents.add(new StringRange(ComponentData.PLUGIN));
    allComponents.add(new StringRange(ComponentData.NODEBINDER));
    allComponents.add(new StringRange(ComponentData.AGENTBINDER));
    //    allComponents.add(new StringRange(ComponentData.SERVICE));

    // FIXME!!! Must allow arbitrary things here!

    //    setAllowedValues(allComponents);
    setPropertyClass(String.class);
  }

}// ComponentTypeProperty

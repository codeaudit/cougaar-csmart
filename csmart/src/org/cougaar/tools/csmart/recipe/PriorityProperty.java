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

import org.cougaar.core.component.ComponentDescription;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentProperty;
import org.cougaar.tools.csmart.core.property.range.StringRange;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * PriorityProperty: Legitimate ComponentPrioritys come from <code>ComponentDescription</code>
 *
 *
 * Created: Wed May 15 14:17:33 2002
 *
 * @author <a href="mailto:bkrisler@bbn.com">Brian Krisler</a>
 */
public class PriorityProperty extends ConfigurableComponentProperty {
  public PriorityProperty (ConfigurableComponent c, String name, Object value){
    super(c, name, value);
    setPropertyClass(StringRange.class);
  }

  public Set getAllowedValues() {
    return getAvailableProperties();
  }

  private static Set availableProperties = null;

  private static Set getAvailableProperties() {
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.recipe.PriorityProperty");
    if(availableProperties == null) {
      StringRange value = null;
      availableProperties = new HashSet();
      try {
        value = new StringRange(
         ComponentDescription.priorityToString(ComponentDescription.PRIORITY_HIGH));
        availableProperties.add(value);

        value = new StringRange(
         ComponentDescription.priorityToString(ComponentDescription.PRIORITY_INTERNAL));
        availableProperties.add(value);

        value = new StringRange(
         ComponentDescription.priorityToString(ComponentDescription.PRIORITY_BINDER));
        availableProperties.add(value);

        value = new StringRange(
           ComponentDescription.priorityToString(
             ComponentDescription.PRIORITY_COMPONENT));
        availableProperties.add(value);

        value = new StringRange(
           ComponentDescription.priorityToString(ComponentDescription.PRIORITY_LOW));
        availableProperties.add(value);

      } catch(IllegalArgumentException e) {
        if(log.isErrorEnabled()) {
          log.error("Unknown Priority Level: ", e);
        }
        availableProperties = Collections.EMPTY_SET;
      }
    }
    return availableProperties;
  }

}// PriorityProperty

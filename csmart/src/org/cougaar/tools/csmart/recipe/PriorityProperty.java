/**
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
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

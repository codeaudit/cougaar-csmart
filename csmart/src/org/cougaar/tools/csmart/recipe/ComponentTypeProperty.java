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

import org.cougaar.tools.csmart.core.cdata.ComponentData;
import org.cougaar.tools.csmart.core.property.ConfigurableComponent;
import org.cougaar.tools.csmart.core.property.ConfigurableComponentProperty;
import org.cougaar.tools.csmart.core.property.range.StringRange;

import java.util.HashSet;
import java.util.Set;


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

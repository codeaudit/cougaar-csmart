/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.society;

import org.cougaar.tools.csmart.core.property.ModifiableComponent;
import org.cougaar.tools.csmart.core.property.Property;

/**
 * A simple interface implemented by configurable components 
 * that represent generic components. <br>
 */
public interface MiscComponent extends ModifiableComponent {
 
  /**
   * Get the label for folders in the UI and containers
   *
   * @return a <code>String</code> label
   */
  String getFolderLabel();

  /**
   * Gets the classname for this component
   *
   * @return a <code>String</code> value
   */
  String getComponentClassName();

  /**
   * Get the type of this Component, from the property. If it is not
   * one of the values in ComponentData, it should be the full 
   * insertion point.
   *
   * @return a <code>String</code> type from the constants in ComponentData
   */
  String getComponentType();

  /**
   * Allow outside users to set the Component type to one of the
   * values in the constants in ComponentData. If not one
   * of those values, it should be a full insertion point.
   *
   * @param type a <code>String</code> component type or insertion point
   */
  void setComponentType(String type);

  /**
   * Adds a Parameter to this component.
   *
   * @param param Unique Integer for this parameter
   * @return a <code>Property</code> value
   */
  Property addParameter(int param);

  /**
   * Adds a Parameter to this component
   *
   * @param param Unique Object for this parameter's value
   * @return a <code>Property</code> value
   */
  Property addParameter(Object param);

  /**
   * Add a parameter that is based on some other property (typically a
   * property of our parnt.
   **/
  Property addParameter(Property prop);
}

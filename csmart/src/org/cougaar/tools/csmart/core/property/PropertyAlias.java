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

package org.cougaar.tools.csmart.core.property;

import org.cougaar.tools.csmart.core.property.name.ComponentName;
import org.cougaar.tools.csmart.core.property.name.CompositeName;

import java.net.URL;
import java.util.Set;

/**
 * Defines a property in terms of another. Only the name is changed.
 * Additionally, other properties can be added and their values will
 * track the aliased property.
 **/
public class PropertyAlias extends PropertyBase implements Property, PropertyListener {
  static final long serialVersionUID = -3114178146196112865L;

  private Property prop;
  private CompositeName name;

  /**
   * Creates a new <code>PropertyAlias</code> instance.
   *
   * @param c 
   * @param name 
   * @param refProp 
   */
  public PropertyAlias(ConfigurableComponent c, String name, Property refProp) {
    super(c);
    if (refProp == null) throw new IllegalArgumentException("null refProp for " + name);
    this.name = new ComponentName(c, name);
    this.prop = refProp;
  }

  public void addPropertyListener(PropertyListener l) {
    if (!haveListeners()) prop.addPropertyListener(this);
    super.addPropertyListener(l);
  }

  public void removePropertyListener(PropertyListener l) {
    super.removePropertyListener(l);
    if (!haveListeners()) prop.removePropertyListener(this);
  }

  public void propertyValueChanged(PropertyEvent e) {
    fireValueChanged(e.getPreviousValue());
  }

  public void propertyOtherChanged(PropertyEvent e) {
    fireOtherChanged(e.getPreviousValue(), e.getWhatChanged());
  }

  public void setPropertyClass(Class c) {
    prop.setPropertyClass(c);
  }

  public void setLabel(String label) {
    prop.setLabel(label);
  }

  public void setDefaultValue(Object defaultValue) {
    prop.setDefaultValue(defaultValue);
  }

  public void setValue(Object value) {
    prop.setValue(value);
  }

  public void setAllowedValues(Set allowedValues) {
    prop.setAllowedValues(allowedValues);
  }

  public CompositeName getName() {
    return name;
  }

  public Class getPropertyClass() {
    return prop.getPropertyClass();
  }

  public String getLabel() {
    return prop.getLabel();
  }

  public Object getDefaultValue() {
    return prop.getDefaultValue();
  }

  public Object getValue() {
    return prop.getValue();
  }

  public Set getAllowedValues() {
    return prop.getAllowedValues();
  }

  public boolean isValueSet()
  { return prop.isValueSet();
  }

  public String getToolTip() {
    String result = super.getToolTip();
    if (result != null) return result;
    return prop.getToolTip();
  }

  public URL getHelp() {
    URL result = super.getHelp();
    if (result != null) return result;
    return prop.getHelp();
  }
}

/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.component;

import java.util.Vector;

/**
 * The name in the namespace of a configurable component.
 **/
public class ComponentName extends MultiName implements CompositeName {
  static final long serialVersionUID = -4393441897050449170L;

  private ConfigurableComponent component;

  public ComponentName(ConfigurableComponent cc, String name) {
    super(new SimpleName(name));
    component = cc;
  }

  public void setComponent(ConfigurableComponent cc) {
    component = cc;
  }

  public ConfigurableComponent getConfigurableComponent() {
    return component;
  }

  protected CompositeName getParentName() {
    if (component == null) return null;
    return component.getName();
  }
}

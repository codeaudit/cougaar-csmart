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
public class ConcatenatedName extends MultiName implements CompositeName {
  static final long serialVersionUID = 9110041606335929236L;

  private CompositeName parentName;

  public ConcatenatedName(CompositeName head, String tail) {
    super(new SimpleName(tail));
    parentName = head;
  }

  protected CompositeName getParentName() {
      return parentName;
  }
}

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

import java.net.URL;

/**
 * The interface for ConfigurableComponents that can be modified from
 * a society.
 **/

public interface ModifiableComponent {
  /**
   * Add a listener for non-property changes to the society. E.g.
   * adding a host.
   * @param l the ModificationListener
   **/
  void addModificationListener(ModificationListener l);

  /**
   * Remove a listener for non-property changes to the society.
   * @param l the ModificationListener
   **/
  void removeModificationListener(ModificationListener l);
}

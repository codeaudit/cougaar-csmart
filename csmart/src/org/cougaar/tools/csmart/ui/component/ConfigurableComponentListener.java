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

import java.io.Serializable;
import java.util.EventListener;

/**
 * Marker interface to identify all listeners that should be saved
 * when serializing a configurable component. Generally, only
 * listeners serving a ConfigurableComponent should use this marker.
 **/
public interface ConfigurableComponentListener extends EventListener, Serializable {
}

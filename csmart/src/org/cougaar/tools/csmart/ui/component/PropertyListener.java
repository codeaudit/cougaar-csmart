/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.component;

import java.util.EventListener;
import java.io.Serializable;

public interface PropertyListener extends EventListener, Serializable {
    void propertyValueChanged(PropertyEvent e);
    void propertyOtherChanged(PropertyEvent e);
}

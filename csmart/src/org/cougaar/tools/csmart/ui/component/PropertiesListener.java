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

public interface PropertiesListener extends EventListener, Serializable {
    static final long serialVersionUID = -7670483003143673509L;

    void propertyAdded(PropertyEvent e);
    void propertyRemoved(PropertyEvent e);
}

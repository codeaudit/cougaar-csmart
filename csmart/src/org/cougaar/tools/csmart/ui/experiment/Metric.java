/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.experiment;

import org.cougaar.tools.csmart.ui.viewer.Organizer;

public interface Metric extends java.io.Serializable {
    void setName(String newName);
    String getName();
    Metric copy(Organizer organizer, Object context);
}

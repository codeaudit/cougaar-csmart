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
/**
 * The interface for values that can be get/set on a configurable component.
 * There are implementations of this for Integer and String.
 */

public interface Range extends Serializable {
    /**
     * Get the minimum value of the range. Values are allowed to be
     * equal to the minimum value
     * @return an object having the minimum value
     **/
    Object getMinimumValue();

    /**
     * Get the maximum value of the range. Values are allowed to
     * be equal to the maximum value
     * @return an object having the maximum value
     **/
    Object getMaximumValue();

    /**
     * Test if an Object is in this Range
     * @param o the Object to test
     **/
    boolean isInRange(Object o);
}


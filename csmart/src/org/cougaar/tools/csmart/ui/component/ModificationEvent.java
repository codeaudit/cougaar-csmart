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

import java.util.EventObject;

public class ModificationEvent extends EventObject {
    static final long serialVersionUID = 792752318075945807L;

    public static final int SOMETHING_CHANGED = 0;

    private int whatChanged;

    public ModificationEvent(Object src, int whatChanged) {
        super(src);
        this.whatChanged = whatChanged;
    }

    public int getWhatChanged() {
        return whatChanged;
    }
}

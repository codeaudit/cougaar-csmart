/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ldm.event;

import org.cougaar.core.society.UID;

/**
 * We need this class for symmetry. See subclasses.
 *
 * @author <a href="mailto:wfarrell@bbn.com">Wilson Farrell</a>
 * @see RealWorldEvent
 */
public abstract class KineticEventImpl extends RealWorldEventImpl
        implements NewKineticEvent {

    public KineticEventImpl(UID uid) {
        super(uid);
    }
}

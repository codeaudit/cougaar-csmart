/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ldm.asset;

import java.util.*;

/**
 *
 */
public class HappinessBG 
extends DoubleTimeline 
implements java.io.Serializable, Cloneable {

  public HappinessBG(HappinessPG hapPG) {
    super();
  }
  
  public double getHappinessAt(long time) {
    return super.getValueAt(time);
  }

  public void setHappinessAt(long time, double happiness) {
    // make sure it's between 0 and 1
    if (happiness > 1.0) {
      happiness = 1.0;
    } else if (happiness < 0.0) {
      happiness = 0.0;
    }

    super.setValueAt(time, happiness);
  }

  public HappinessBG copy(HappinessPG hapPG) {
    return (HappinessBG)super.clone();
  }

}

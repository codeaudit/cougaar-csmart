/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.tools.csmart.runtime.ldm.asset;

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

/*
 * <copyright>
 * Copyright 1997-2000 Defense Advanced Research Projects Agency (DARPA)
 * and ALPINE (A BBN Technologies (BBN) and Raytheon Systems Company
 * (RSC) Consortium). This software to be used in accordance with the
 * COUGAAR license agreement.  The license agreement and other
 * information on the Cognitive Agent Architecture (COUGAAR) Project can
 * be found at http://www.cougaar.org or email: info@cougaar.org.
 * </copyright>
 */

package org.cougaar.tools.csmart.ldm.asset;

import java.util.Vector;
import java.util.Enumeration;
import java.io.Serializable;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;
import java.util.*;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;

public class CSMARTAsset
  extends org.cougaar.tools.csmart.ldm.asset.AssetSkeleton 
{
  protected CSMARTAsset() {
    super();
  }

  protected CSMARTAsset(CSMARTAsset prototype) {
    super(prototype);
  }
}


/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ldm;

import java.util.*;

import org.cougaar.domain.planning.ldm.Domain;
import org.cougaar.domain.planning.ldm.Factory;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.core.cluster.LogPlan;
import org.cougaar.core.cluster.BlackboardServesLogicProvider;
import org.cougaar.core.cluster.LogPlanServesLogicProvider;
import org.cougaar.core.cluster.ClusterServesLogicProvider;
import org.cougaar.core.cluster.XPlanServesBlackboard;
import org.cougaar.core.cluster.LogicProvider;

import org.cougaar.tools.csmart.Constants;
import org.cougaar.tools.csmart.ldm.lps.*;

/**
 * Create a barebones CSMARTDomain.  We have our own Factory, and
 * one LogicProvider
 * The property to load this Domain is:<pre>
 *         -Dorg.cougaar.domain.csmart=org.cougaar.tools.csmart.ldm.CSMARTDomain
 * </pre>
 **/

public class CSMARTDomain implements Domain {
  public CSMARTDomain() { }

  /**
   * Create the CSMARTFactory for creating Events and things
   * @return the CSMARTFactory instance
   **/
  public Factory getFactory(LDMServesPlugIn ldm) {
    return new CSMARTFactory(ldm);
  }

  /**
   * CSMART has almost no Domain initialization to do - just initialize our Constants
   **/
  public void initialize() {
    Constants.Role.init();
  }

  /**
   * Create a simple dummy CSMARTPlan
   * This Plan is just a placeholder for now, with no subscriptions
   * @return the <code>CSMARTPlan</code>
   **/
  public XPlanServesBlackboard createXPlan(Collection existingXPlans) {

    for (Iterator plans = existingXPlans.iterator(); plans.hasNext(); ) {
      XPlanServesBlackboard xPlan = (XPlanServesBlackboard) plans.next();
      if (xPlan != null) return xPlan;
    }
    
    return new LogPlan();
  }  

  /**
   * CSMART does have its own LogicProviders
   * For now, exactly one -- the <code>ImpactsLP</code>
   * @return a Collection of the CSMART LogicProviders or null
   * @see ImpactsLP
   **/
  public Collection createLogicProviders(BlackboardServesLogicProvider logplan,
					 ClusterServesLogicProvider cluster) {

      ArrayList l = new ArrayList(1);

      l.add(new ImpactsLP((LogPlanServesLogicProvider)logplan, cluster));
      return l;
  }

} // end of CSMARTDomain.java
















/*
 * <copyright>
 *  Copyright 2001-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.runtime.ldm;

import java.util.*;

import org.cougaar.core.domain.Domain;
import org.cougaar.core.domain.Factory;
import org.cougaar.core.domain.LDMServesPlugIn;
import org.cougaar.core.blackboard.LogPlan;
import org.cougaar.core.blackboard.BlackboardServesLogicProvider;
import org.cougaar.core.blackboard.LogPlanServesLogicProvider;
import org.cougaar.core.agent.ClusterServesLogicProvider;
import org.cougaar.core.blackboard.XPlanServesBlackboard;
import org.cougaar.core.agent.LogicProvider;

import org.cougaar.tools.csmart.Constants;
import org.cougaar.tools.csmart.runtime.ldm.lps.*;

/**
 * Create a barebones CSMARTDomain.  We have our own Factory, and
 * one LogicProvider
 * The property to load this Domain is:<pre>
 *         -Dorg.cougaar.domain.csmart=org.cougaar.tools.csmart.runtime.ldm.CSMARTDomain
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
















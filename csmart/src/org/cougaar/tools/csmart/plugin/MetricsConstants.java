/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.tools.csmart.plugin;

import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.Role;
/**
 * Constants used by Metrics base.
 **/
public interface MetricsConstants 
{
  // special verbs
  Verb Verb_Manage = Verb.getVerb("Manage");
  Verb Verb_Ready = Verb.getVerb("Ready");
  Verb Verb_Start = Verb.getVerb("Start");
  Verb Verb_Finish = Verb.getVerb("Finish");
  Verb Verb_Sample = Verb.getVerb("Sample");

 // special Roles
  String Role_METRICSPROVIDER = "MetricsProvider";
  Role Role_MetricsProvider = Role.getRole(Role_METRICSPROVIDER);
  Role Role_MetricsControlProvider = Role.getRole("MetricsControlProvider");
  Role Role_MetricsStatisticsProvider = Role.getRole("MetricsStatisticsProvider");
}














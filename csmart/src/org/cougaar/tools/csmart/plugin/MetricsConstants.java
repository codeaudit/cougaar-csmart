/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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














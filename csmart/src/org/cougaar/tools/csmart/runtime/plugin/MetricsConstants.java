/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package org.cougaar.tools.csmart.runtime.plugin;

import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Verb;
/**
 * Constants used by Metrics base.
 **/
public interface MetricsConstants 
{
  // special verbs
  Verb Verb_Manage = Verb.get("Manage");
  Verb Verb_Ready = Verb.get("Ready");
  Verb Verb_Start = Verb.get("Start");
  Verb Verb_Finish = Verb.get("Finish");
  Verb Verb_Sample = Verb.get("Sample");

 // special Roles
  String Role_METRICSPROVIDER = "MetricsProvider";
  Role Role_MetricsProvider = Role.getRole(Role_METRICSPROVIDER);
  Role Role_MetricsControlProvider = Role.getRole("MetricsControlProvider");
  Role Role_MetricsStatisticsProvider = Role.getRole("MetricsStatisticsProvider");
}













